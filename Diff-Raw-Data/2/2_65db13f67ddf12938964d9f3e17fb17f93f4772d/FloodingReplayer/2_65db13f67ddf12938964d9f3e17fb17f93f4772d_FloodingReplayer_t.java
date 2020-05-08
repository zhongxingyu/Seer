 package adsim.handler;
 
 import java.util.Collections;
 
 import lombok.*;
 import adsim.core.INodeHandler;
 import adsim.core.Message;
 import adsim.core.Message.TellNeighbors;
 import adsim.core.Node;
 import adsim.core.Session;
 
 /**
  * 定期的にバッファのメッセージを順番に送信します。
  */
 public class FloodingReplayer extends NodeHandlerBase {
     private int index;
 
     public FloodingReplayer() {
         this(0);
     }
 
     // Copy constructor
     private FloodingReplayer(int index) {
         this.index = index;
     }
 
     @Override
     public void initialize(Node node) {
         // Do nothing
     }
 
     @Override
     public void interval(Session sess, Node node) {
         for (val msg : node.getCreatedMessages()) {
             node.pushMessage(msg);
         }
         node.getCreatedMessages().clear();
         val buffer = node.getBuffer();
         if (!buffer.isEmpty()) {
            val nextPointer = (index++) % buffer.size();
             node.broadcast(buffer.get(nextPointer));
         }
     }
 
     private void onTellNeighbors(Node self, TellNeighbors msg) {
         val buffer = self.getBuffer();
         for (val nbEntry : msg.getEntries()) {
             for (val bufMsg : buffer) {
                 if (bufMsg.getToId().equals(nbEntry.getSender())) {
                     self.broadcast(bufMsg);
                 }
             }
         }
     }
 
     @Override
     public void onReceived(Node self, Message packet) {
         if (packet.getType() == Message.TYPE_TELLNEIGHBORS) {
             val tn = (TellNeighbors) packet;
             onTellNeighbors(self, tn);
         }
     }
 
     @Override
     public INodeHandler clone() {
         return new FloodingReplayer(index);
     }
 
 }
