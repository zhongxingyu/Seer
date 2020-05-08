 package ddth.dasp.hetty.mvc;
 
 /**
  * This action handler simply returns the view name.
  */
 import ddth.dasp.hetty.message.IRequest;
 import ddth.dasp.hetty.qnt.ITopicPublisher;
 
 public class SimpleActionHandler extends AbstractActionHandler {
     /**
      * {@inheritDoc}
      */
     @Override
    protected Object internalHandleRequest(IRequest request, ITopicPublisher topicPublisher,
            String topicName) {
         return getViewName();
     }
 }
