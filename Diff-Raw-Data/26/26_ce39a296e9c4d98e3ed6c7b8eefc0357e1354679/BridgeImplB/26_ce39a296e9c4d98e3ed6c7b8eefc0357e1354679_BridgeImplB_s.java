 
package bridgea;
 
 import bridge.DependencyBridge;
 import common.Message;
import depa.MessageMaker;
 
/** Bridge to the "A" implementation. **/
public class BridgeImplA extends DependencyBridge {
   /** {@inheritDoc} */
   @Override
   public Message getMessage() {
     MessageMaker maker = new MessageMaker();
     maker.createMessage();
     return maker.getMessage();
   }
 }
