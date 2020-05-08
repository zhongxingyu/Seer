 package org.codehaus.xfire.security.wss4j;
 
 import java.security.cert.X509Certificate;
 import java.util.Vector;
 
 import javax.security.auth.callback.CallbackHandler;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.ws.security.WSConstants;
 import org.apache.ws.security.WSSecurityEngineResult;
 import org.apache.ws.security.WSSecurityException;
 import org.apache.ws.security.handler.RequestData;
 import org.apache.ws.security.handler.WSHandlerConstants;
 import org.apache.ws.security.handler.WSHandlerResult;
 import org.apache.ws.security.message.token.Timestamp;
 import org.apache.ws.security.util.WSSecurityUtil;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.exchange.AbstractMessage;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.handler.Handler;
 import org.codehaus.xfire.handler.Phase;
 import org.codehaus.xfire.soap.handler.ReadHeadersHandler;
 import org.codehaus.xfire.util.dom.DOMInHandler;
 import org.w3c.dom.Document;
 
 /**
  * Performs WS-Security inbound actions.
  *  
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  */
 public class WSS4JInHandler
     extends AbstractWSS4JHandler
     implements Handler
 {
     protected static Log log = LogFactory.getLog(WSS4JInHandler.class.getName());
 
     private static Log tlog = LogFactory.getLog("org.apache.ws.security.TIME");
 
     public WSS4JInHandler()
     {
         super();
         
         setPhase(Phase.PARSE);
         getBefore().add(ReadHeadersHandler.class.getName());
         getAfter().add(DOMInHandler.class.getName());
     }
     
     public void invoke(MessageContext msgContext)
         throws XFireFault
     {
 
         boolean doDebug = log.isDebugEnabled();
 
         if (doDebug)
         {
             log.debug("WSS4JInSecurityHandler: enter invoke()");
         }
 
         long t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0;
         if (tlog.isDebugEnabled())
         {
             t0 = System.currentTimeMillis();
         }
 
         RequestData reqData = new RequestData();
         /*
          * The overall try, just to have a finally at the end to perform some
          * housekeeping.
          */
         try
         {
             reqData.setMsgContext(msgContext);
 
             Vector actions = new Vector();
             String action = null;
             if ((action = (String) getOption(WSHandlerConstants.ACTION)) == null)
             {
                 action = (String) msgContext.getProperty(WSHandlerConstants.ACTION);
             }
             if (action == null)
             {
                 throw new XFireRuntimeException("WSS4JInHandler: No action defined");
             }
 
             int doAction = WSSecurityUtil.decodeAction(action, actions);
 
             String actor = (String) getOption(WSHandlerConstants.ACTOR);
 
             AbstractMessage sm = msgContext.getCurrentMessage();
             Document doc = (Document) sm.getProperty(DOMInHandler.DOM_MESSAGE);
 
             if (doc == null)
                 throw new XFireRuntimeException("DOMInHandler must be enabled for WS-Security!");
 
             /*
              * Check if it's a response and if its a fault. Don't process
              * faults.
              */
             if (sm.getBody() instanceof XFireFault)
                 return;
 
             /*
              * To check a UsernameToken or to decrypt an encrypted message we
              * need a password.
              */
             CallbackHandler cbHandler = null;
             if ((doAction & (WSConstants.ENCR | WSConstants.UT)) != 0)
             {
                 cbHandler = getPasswordCB(reqData);
             }
 
             /*
              * Get and check the Signature specific parameters first because
              * they may be used for encryption too.
              */
             doReceiverAction(doAction, reqData);
 
             Vector wsResult = null;
             if (tlog.isDebugEnabled())
             {
                 t1 = System.currentTimeMillis();
             }
 
             try
             {
                 wsResult = secEngine.processSecurityHeader(doc, actor, cbHandler, reqData
                         .getSigCrypto(), reqData.getDecCrypto());
             }
             catch (WSSecurityException ex)
             {
                 throw new XFireFault("WSS4JInHandler: security processing failed", ex,
                         XFireFault.SENDER);
             }
 
             if (tlog.isDebugEnabled())
             {
                 t2 = System.currentTimeMillis();
             }
 
             if (wsResult == null)
             { // no security header found
                 if (doAction == WSConstants.NO_SECURITY)
                 {
                     return;
                 }
                 else
                 {
                     throw new XFireFault(
                             "WSS4JInHandler: Request does not contain required Security header",
                             XFireFault.SENDER);
                 }
             }
 
             if (reqData.getWssConfig().isEnableSignatureConfirmation())
             {
                 checkSignatureConfirmation(reqData, wsResult);
             }
 
             /*
              * Now we can check the certificate used to sign the message. In the
              * following implementation the certificate is only trusted if
              * either it itself or the certificate of the issuer is installed in
              * the keystore.
              * 
              * Note: the method verifyTrust(X509Certificate) allows custom
              * implementations with other validation algorithms for subclasses.
              */
 
             // Extract the signature action result from the action vector
             WSSecurityEngineResult actionResult = WSSecurityUtil
                     .fetchActionResult(wsResult, WSConstants.SIGN);
 
             if (actionResult != null)
             {
                 X509Certificate returnCert = actionResult.getCertificate();
 
                 if (returnCert != null)
                 {
                     if (!verifyTrust(returnCert, reqData))
                     {
                         throw new XFireFault(
                                 "WSS4JInHandler: The certificate used for the signature is not trusted",
                                 XFireFault.SENDER);
                     }
                 }
             }
 
             /*
              * Perform further checks on the timestamp that was transmitted in
              * the header. In the following implementation the timestamp is
              * valid if it was created after (now-ttl), where ttl is set on
              * server side, not by the client.
              * 
              * Note: the method verifyTimestamp(Timestamp) allows custom
              * implementations with other validation algorithms for subclasses.
              */
 
             // Extract the timestamp action result from the action vector
             actionResult = WSSecurityUtil.fetchActionResult(wsResult, WSConstants.TS);
 
             if (actionResult != null)
             {
                 Timestamp timestamp = actionResult.getTimestamp();
 
                 if (timestamp != null)
                 {
                     if (!verifyTimestamp(timestamp, decodeTimeToLive(reqData)))
                     {
                         throw new XFireFault(
                                 "WSS4JInHandler: The timestamp could not be validated",
                                 XFireFault.SENDER);
                     }
                 }
             }
 
             /*
              * now check the security actions: do they match, in right order?
              */
             if (!checkReceiverResults(wsResult, actions))
             {
                 throw new XFireFault(
                         "WSS4JInHandler: security processing failed (actions mismatch)",
                         XFireFault.SENDER);
 
             }
             /*
              * All ok up to this point. Now construct and setup the security
              * result structure. The service may fetch this and check it.
              */
             Vector results = null;
             if ((results = (Vector) msgContext.getProperty(WSHandlerConstants.RECV_RESULTS)) == null)
             {
                 results = new Vector();
                 msgContext.setProperty(WSHandlerConstants.RECV_RESULTS, results);
             }
             WSHandlerResult rResult = new WSHandlerResult(actor, wsResult);
             results.add(0, rResult);
             if (tlog.isDebugEnabled())
             {
                 t4 = System.currentTimeMillis();
                 tlog.debug("Receive request: total= " + (t4 - t0) + " request preparation= "
                         + (t1 - t0) + " request processing= " + (t2 - t1) + " request to Axis= "
                         + (t3 - t2) + " header, cert verify, timestamp= " + (t4 - t3) + "\n");
             }
 
             if (doDebug)
             {
                 log.debug("WSS4JInHandler: exit invoke()");
             }
         }
         catch (WSSecurityException e)
         {
             throw new XFireFault(e.getMessage(), e, XFireFault.SENDER);
         }
         finally
         {
             reqData.clear();
             reqData = null;
         }
     }
 }
