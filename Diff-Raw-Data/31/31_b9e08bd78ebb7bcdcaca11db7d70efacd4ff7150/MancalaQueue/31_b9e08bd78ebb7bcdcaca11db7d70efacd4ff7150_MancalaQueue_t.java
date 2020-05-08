 package ProducerConsumer;
 
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
  * TODO javadocs
  *
  * @author Chad Zawistowski <crz8448@rit.edu>
  */
 public class MancalaQueue extends LinkedBlockingQueue<BoardEntry> {
 
    protected final int m_numConsumers;
 
    public MancalaQueue(int numConsumers) {
         super();
        m_numConsumers = numConsumers;
     }
 
     public void close() {
        for (int i=0; i < m_numConsumers; i++)
            offer(new BoardEntry(null, 0));
     }
 }
