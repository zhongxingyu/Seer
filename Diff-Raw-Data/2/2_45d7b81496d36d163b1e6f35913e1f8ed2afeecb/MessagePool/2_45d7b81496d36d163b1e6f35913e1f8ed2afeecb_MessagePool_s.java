 package jp.ac.osaka_u.ist.sel.metricstool.main.io;
 
 
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter.MESSAGE_TYPE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.ConcurrentHashSet;
 
 
 /**
  * MꂽbZ[WXi[ɑ͂NX
  * 
  * bZ[W^CvɃCX^X쐬.
  * 
  * @author kou-tngt
  *
  */
 public class MessagePool {
 
     /**
      * ^CvƂ̃CX^XԂ\bh
      * @param type 擾CX^X̃^Cv
      * @return typeɑΉCX^X
      * @throws IllegalArgumentException typep̃CX^XȂꍇ
      */
     public static MessagePool getInstance(final MESSAGE_TYPE type) {
         for (final MessagePool instance : INSTANCES) {
             if (type == instance.getMessageType()) {
                 return instance;
             }
         }
         //bZ[W^CvɃCX^XpӂĂ͂Ȃ̂ŁCɗ̂͂肦Ȃ
         assert (false) : "Illegal state : unknown message type " + type.name() + " is found.";
 
         throw new IllegalArgumentException("unknown message type " + type.name());
     }
 
     /**
      * Xi[ǉ
      * @param listener ǉXi[
      * @throws NullPointerException listenernull̏ꍇ
      */
     public void addMessageListener(final MessageListener listener) {
         if (null == listener) {
             throw new NullPointerException("listner is null.");
         }
         synchronized (this) {
             this.listeners.add(listener);
         }
     }
 
     /**
      * ̃CX^XΉ郁bZ[W^CvԂ
      * @return bZ[W^Cv
      */
     public MESSAGE_TYPE getMessageType() {
         return this.messageType;
     }
 
     /**
      * Xi[폜
      * @param listener 폜郊Xi[
      */
    public void revmoeMessageListener(final MessageListener listener) {
         if (null != listener) {
             synchronized (this) {
                 this.listeners.remove(listener);
             }
         }
     }
 
     /**
      * bZ[W𑗐M郁\bh
      * @param source bZ[WM
      * @param message bZ[W
      * @throws NullPointerException source܂messagenull̏ꍇ
      */
     public void sendMessage(final MessageSource source, final String message) {
         if (null == message) {
             throw new NullPointerException("message is null.");
         }
         if (null == source) {
             throw new NullPointerException("source is null.");
         }
 
         this.fireMessage(new MessageEvent(source, this.messageType, message));
     }
 
     /**
      * bZ[WCxgXi[ɑM郁\bh
      * @param event MCxg
      * @throws NullPointerException eventnull̏ꍇ
      */
     private void fireMessage(final MessageEvent event) {
         if (null == event) {
             throw new NullPointerException("event is null");
         }
 
         synchronized (this) {
             for (final MessageListener listener : this.listeners) {
                 listener.messageReceived(event);
             }
         }
     }
 
     /**
      * bZ[W^CvɑΉCX^X쐬privateRXgN^
      * @param type
      */
     private MessagePool(final MESSAGE_TYPE type) {
         this.messageType = type;
     }
 
     /**
      * ̃CX^X̃bZ[W^Cv
      */
     private final MESSAGE_TYPE messageType;
 
     /**
      * o^Ă郁bZ[WXi
      */
     private final Set<MessageListener> listeners = new ConcurrentHashSet<MessageListener>();
 
     /**
      * CX^XQ
      */
     private static final MessagePool[] INSTANCES;
 
     static {
         //bZ[W^CvɃCX^X쐬
         final MESSAGE_TYPE[] types = MESSAGE_TYPE.values();
         final int size = types.length;
         INSTANCES = new MessagePool[size];
         for (int i = 0; i < size; i++) {
             INSTANCES[i] = new MessagePool(types[i]);
         }
     }
 
 }
