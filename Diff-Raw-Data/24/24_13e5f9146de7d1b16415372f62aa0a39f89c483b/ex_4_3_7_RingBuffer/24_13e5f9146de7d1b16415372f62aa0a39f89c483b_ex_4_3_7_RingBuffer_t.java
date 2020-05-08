 /*
 4.3.7. Ring buffer. A ring buffer or circular queue is a FIFO data structure of a fixed size N. It is useful for transferring data between asynchronous processes or storing log files. When the buffer is empty, the consumer waits until data is deposited; when the buffer is full, the producer waits to deposit data. A ring buffer has the following methods: 
 isEmpty(), isFull(), enqueue(), and dequeue(). 
 Write an generic data type RingBuffer using an array (with circular wrap-around for efficiency). 
 */
 
 package JavaBook.Algo_4.StacksQueues_4_3;
 
 public class ex_4_3_7_RingBuffer { 
 
     public static void main(String[] args) {
         int TESTS_NUMBER = 5;
         RingBuffer intsBuffer = new RingBuffer(TESTS_NUMBER);
         
         for (int i = 0; i < TESTS_NUMBER + 1; i++) {
             System.out.println("..enqueing " + i);
             intsBuffer.enqueue(i);
         }
   
         System.out.println("..dequeing:");
         for (int i = 0; i < TESTS_NUMBER + 1; i++) {
                 try { 
                     System.out.println("..dequeing " + intsBuffer.dequeue());
                 } 
                 catch (Exception e) { 
                     System.out.println("Failed to extract item, buffer is empty.");
                     continue; 
                 }
         }       
 
        for (int i = 0; i < TESTS_NUMBER + 3; i++) {
             System.out.println("..enqueing " + i);
             intsBuffer.enqueue(i);
            if (i > 2) {
                try { 
                System.out.println("..dequeing " + intsBuffer.dequeue());
                System.out.println("..dequeing " + intsBuffer.dequeue());
                }
                catch (Exception e) { 
                    System.out.println("Failed to extract item, buffer is empty.");
                    continue; 
                }                
            }
         }        
         
     } 
     
 }    
