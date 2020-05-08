 package ProducerConsumer;
 
 /**
  * TODO javadocs
  *
  * @author Chad Zawistowski <crz8448@rit.edu>
  */
 public class MancalaConsumer implements Runnable {
 
     private final MancalaQueue m_queue;
     private final String m_name;
 
     public MancalaConsumer(MancalaQueue queue, String name) {
         m_queue = queue;
         m_name = name;
     }
 
     public void run() {
        while (true) {
             try {
                 BoardEntry entry = m_queue.take();
                 if (entry.getBoard() != null)
                     System.out.println(m_name + " took " + Integer.toString(entry.getNumInSequence()));
                else
                    break;
             } catch (InterruptedException e) {
                 System.out.println("MancalaConsumer interrupted while waiting on queue: " + e.getMessage());
             }
         }
        System.out.println(m_name + " Finished Consuming");
     }
 }
