 package org.codehaus.xfire.security.wssecurity;
 
 import java.io.IOException;
 import java.security.Key;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.UnsupportedCallbackException;
 
 import org.apache.ws.security.WSPasswordCallback;
 import org.apache.ws.security.WSSecurityEngine;
 import org.apache.ws.security.WSSecurityException;
 import org.apache.ws.security.components.crypto.Crypto;
 import org.codehaus.xfire.security.InSecurityProcessor;
 import org.codehaus.xfire.security.InSecurityProcessorBuilder;
 import org.codehaus.xfire.security.InSecurityResult;
 import org.w3c.dom.Document;
 
 /**
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  * 
  */
 public class WSS4JInSecurityProcessor
     implements InSecurityProcessor
 {
 
     protected static final WSSecurityEngine secEngine = WSSecurityEngine.getInstance();
 
     private Crypto crypto;
 
     private InSecurityProcessorBuilder builder = new WSS4JInProcessorBuilder();
 
     private CallbackHandler cbHandler;
 
     private Key decryptionKey;
 
     private Map passwords;
 
     private boolean isInitialized = false;
 
     public InSecurityProcessorBuilder getBuilder()
     {
         return builder;
     }
 
     void checkInitialized()
     {
         if (!isInitialized)
         {
             builder.build(this);
             isInitialized = true;
         }
     }
 
     public Map getPasswords()
     {
         return passwords;
     }
 
     public void setPasswords(Map passwords)
     {
         this.passwords = passwords;
         cbHandler = new CBPasswordHandler(passwords);
     }
 
     public Crypto getCrypto()
     {
         return crypto;
     }
 
     public void setCrypto(Crypto crypto)
     {
         this.crypto = crypto;
     }
 
     public void setDecryptionKey(Key decryptionKey)
     {
         this.decryptionKey = decryptionKey;
 
     }
 
     public InSecurityResult process(Document document)
         throws org.codehaus.xfire.security.exceptions.WSSecurityException
     {
         Vector wsResult = null;
 
         checkInitialized();
         try
         {
             wsResult = secEngine.processSecurityHeader(document, "", cbHandler, crypto, crypto);
         }
         catch (WSSecurityException ex)
         {
             
             throw ExceptionConverter.convert(ex);
            
         }
         
 
         InSecurityResult result = new InSecurityResult();
         if (wsResult != null)
         {
             SecurityResultHandler handler = new SecurityResultHandler(result);
             result = handler.process(wsResult);
 
         }
 
         result.setDocument(document);
         return result;
     }
 
     public void setBuilder(InSecurityProcessorBuilder builder)
     {
         this.builder = builder;
 
     }
 
     /**
      * @author Tomasz Sztelak
      * 
      */
     private static class CBPasswordHandler
         implements CallbackHandler
     {
         private Map passwords = null;
 
         public CBPasswordHandler(Map passwords)
         {
             this.passwords = passwords;
         }
 
         public void handle(Callback[] callbacks)
             throws IOException, UnsupportedCallbackException
         {
             for (int i = 0; i < callbacks.length; i++)
             {
                 WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                String pass = (String) passwords.get(pc.getIdentifer());
                if( pass != null ){
                  pc.setPassword(pass);
                }
             }
         }
 
     }
 }
