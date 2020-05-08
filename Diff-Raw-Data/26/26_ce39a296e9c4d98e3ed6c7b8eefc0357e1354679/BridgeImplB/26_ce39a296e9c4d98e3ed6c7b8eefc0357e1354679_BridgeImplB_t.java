 
package bridgeb;
 
 import bridge.DependencyBridge;
 import common.Message;
import depb.MessageMaker;
 
/** Bridge to the "B" implementation. **/
public class BridgeImplB extends DependencyBridge {
   /** {@inheritDoc} */
   @Override
   public Message getMessage() {
     MessageMaker maker = new MessageMaker();
     maker.createMessage();
     return maker.getMessage();
   }
 }
