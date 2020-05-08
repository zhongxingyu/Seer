 package pl.edu.pjwstk.mteam.jcsync.core;
 
 import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
 import pl.edu.pjwstk.mteam.jcsync.core.concurrency.ConsistencyModel;
 import pl.edu.pjwstk.mteam.jcsync.core.concurrency.EventInvoker.InvokerType;
 import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncCreateCollectionIndication;
 import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncCreateCollectionRequest;
 import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncInvokeMethodIndication;
 import pl.edu.pjwstk.mteam.jcsync.core.messages.JCSyncMessage;
 import pl.edu.pjwstk.mteam.jcsync.core.pubsubwrapper.JCSyncMessageCarrier;
 import pl.edu.pjwstk.mteam.jcsync.exception.CollectionExistException;
 import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncCreateCollectionMethod;
 import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
 
 /**
  * @author Piotr Bucior
  * @version 1.0
  */
 public class DefaultCustomisableAlgorithm extends CustomisableAlgorithm {
 
     public DefaultCustomisableAlgorithm() {
     }
 
     public void finalize() throws Throwable {
         super.finalize();
     }
 
 
    @Override
     protected boolean onDeliverOperation(JCSyncMessage operation) {
         return false;
     }
 
    @Override
     protected boolean onDeliverNotify(JCSyncMessage notify) {
        return false;
     }
 
     @Override
     protected boolean onDeliverCreateCollectionResponse(JCSyncMessage response) {
         return false;
     }
 
     @Override
     protected boolean onDeliverOperationResponse(JCSyncMessage response) {
         return false;
     }
 
     @Override
     protected boolean requestReadOperation(JCSyncMessage operation) {
         return false;
     }
 
     @Override
     protected boolean requestWriteOperation(JCSyncMessage operation) {
         return false;
     }
 
     @Override
     protected boolean onDeliverResponse(JCSyncMessage msg) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public JCSyncAbstractCollection getCollection(String id) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void onDeliverJCSyncMessage(JCSyncMessageCarrier jCSyncMessageCarrier) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     protected boolean onDeliverCreateCollectionIndication(JCSyncCreateCollectionIndication request) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Object publishWriteOperation(JCSyncMethod operation) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     
     @Override
     public void invoke(JCSyncInvokeMethodIndication indication) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public int requestCreateCollection(JCSyncCreateCollectionMethod op, boolean subscribe_if_exist, InvokerType type, ConsistencyModel model) throws CollectionExistException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     /**
      *
      * @param operation
      */
 }//end DefaultCustomisableAlgorithm
