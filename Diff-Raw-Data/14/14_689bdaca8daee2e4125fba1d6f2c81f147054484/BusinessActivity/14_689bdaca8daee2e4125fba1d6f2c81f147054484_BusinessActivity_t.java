 package org.apache.ode.bpel.wstx;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.ode.bpel.engine.MessageImpl;
 import org.apache.ode.bpel.iapi.Message;
 import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.arjuna.mw.wst.TxContext;
 import com.arjuna.mw.wst11.BusinessActivityManager;
 import com.arjuna.mw.wst11.TransactionManager;
 import com.arjuna.mw.wst11.UserBusinessActivity;
 import com.arjuna.mw.wst11.UserTransaction;
 import com.arjuna.mw.wst11.common.CoordinationContextHelper;
 import com.arjuna.mwlabs.wst11.ba.context.TxContextImple;
 import com.arjuna.webservices11.wscoor.CoordinationConstants;
 import com.arjuna.wst.SystemException;
 import com.arjuna.wst.TransactionRolledBackException;
 import com.arjuna.wst.UnknownTransactionException;
 import com.arjuna.wst.WrongStateException;
 
 /**
  * 
  * TODO: consider AtomicOutcome and MixedOutcome
  * 
  * @author ibek
  *
  */
 public class BusinessActivity implements WebServiceTransaction {
 
     private static final Log __log = LogFactory.getLog(BusinessActivity.class);
 
     protected UserBusinessActivity _uba;
     protected TxContext _txcontext;
     protected boolean _active;
     protected WebServiceTransactionType _type;
     protected boolean _subordinate;
 
     public BusinessActivity(WebServiceTransactionType type) {
         _type = type;
         _active = false;
         _subordinate = false;
     }
 
     /**
      * Begin of transaction must be performed with mutual exclusion in one thread
      * because the registration service cannot begin more transactions concurrently.
      */
     private static synchronized void begin(UserBusinessActivity uba) throws WrongStateException, SystemException{
         uba.begin();
     }
 
     public void begin(Message bpelRequest) throws WrongStateException, SystemException {
 		MessageImpl req = (MessageImpl)bpelRequest;
 		_subordinate = false;
         if(req._dao.getHeader() != null){
             try {
                 NodeList cc = req._dao.getHeader().getElementsByTagNameNS(CoordinationConstants.WSCOOR_NAMESPACE, 
                         CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT);
                 if (cc.getLength() > 0){
                     CoordinationContextType cct = CoordinationContextHelper.deserialise((Element)cc.item(0));
                     if (cct != null) {
                         TxContext ctx = new TxContextImple(cct);
                         BusinessActivityManager.getBusinessActivityManager().resume(ctx);
                         _subordinate = true;
                     }
                 }
             } catch (Exception e) {
                 __log.warn("Wrong coordination context. The transaction won't be subordinated.");
             }
         }
 
         _uba = UserBusinessActivity.getUserBusinessActivity();
 				if (_subordinate && _uba != null) {
             _uba = UserBusinessActivity.getUserBusinessActivity().getUserSubordinateBusinessActivity();
         }
 
         if (_uba == null)
             throw new SystemException(
                     "Distributed transaction has not been created. Check that JBoss XTS is runnning.");
         try {
             begin(_uba);
         } catch (WrongStateException wse) {
             BusinessActivityManager.getBusinessActivityManager().suspend(); // previous transaction will be resumed by another instance
             _uba = UserBusinessActivity.getUserBusinessActivity();
             begin(_uba); // we try again to create new transaction
         }
         _txcontext = BusinessActivityManager.getBusinessActivityManager().currentTransaction();
         _active = true;
     }
 
     public void commit() throws SecurityException, UnknownTransactionException, SystemException,
             WrongStateException {
         _active = false;
         try {
             resume();
             _uba.close();
         } catch (TransactionRolledBackException e) {
             __log.info("Web service transaction was aborted");
         } finally {
             _uba = null;
             _txcontext = null;
         }
     }
     
     public void complete() throws UnknownTransactionException, SystemException, WrongStateException {
        try {
            resume();
            _uba.complete();
        } catch (WrongStateException ex) {
            __log.warn("Business Activity Transaction WrongStateException for completion.");
        }
     }
 
     public boolean isActive() {
         return _uba != null && _active;
     }
     
     public boolean isSubordinate() {
         return _subordinate;
     }
 
     public void rollback() throws SecurityException, UnknownTransactionException, SystemException,
             WrongStateException {
         _active = false;
         try{
             resume();
             _uba.cancel();
         } finally {
             _uba = null;
             _txcontext = null;
         }
     }
 
     public String getTransactionIdentifier() {
         return _uba.transactionIdentifier();
     }
     
     public WebServiceTransactionType getType() {
         return _type;
     }
 
     public void resume() throws UnknownTransactionException, SystemException {
         if (!_txcontext.equals(BusinessActivityManager.getBusinessActivityManager().currentTransaction())) {
             BusinessActivityManager.getBusinessActivityManager().resume(_txcontext);
             _uba = UserBusinessActivity.getUserBusinessActivity();
         }
     }
 
     public void suspend() throws SystemException {
         _txcontext = BusinessActivityManager.getBusinessActivityManager().suspend();
     }
 
     public Element putCoordinationContext(Element headerElement)
             throws UnknownTransactionException, SystemException {
         resume();
         final TxContextImple txContext = (TxContextImple) BusinessActivityManager.getBusinessActivityManager().currentTransaction();
         CoordinationContextType ctx = txContext.context().getCoordinationContext();
         try {
             Document doc = headerElement.getOwnerDocument();
             Element coord = doc.createElementNS(CoordinationConstants.WSCOOR_NAMESPACE,
                     CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT);
             headerElement.appendChild(coord);
             CoordinationContextHelper.serialise(ctx, headerElement);
         } catch (Exception e) {
             e.printStackTrace();
             throw new SystemException("Coordination context has not been added to the header.");
         }
         return headerElement;
     }
 
 }
