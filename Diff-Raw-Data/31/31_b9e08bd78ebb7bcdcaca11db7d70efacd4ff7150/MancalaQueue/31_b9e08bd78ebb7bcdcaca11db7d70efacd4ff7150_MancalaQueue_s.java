 package ProducerConsumer;
 
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
  * TODO javadocs
  *
  * @author Chad Zawistowski <crz8448@rit.edu>
  */
 public class MancalaQueue extends LinkedBlockingQueue<BoardEntry> {
 
    protected boolean queueClosed;
 
    public MancalaQueue() {
         super();
        queueClosed = false;
     }
 
     public void close() {
        queueClosed = true;
     }

    public boolean isClosed() {
        return queueClosed;
    }

 }
