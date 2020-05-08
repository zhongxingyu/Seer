 import java.util.concurrent.SynchronousQueue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: michelle
  * Date: 11/1/12
  * Time: 8:25 PM
  * To change this template use File | Settings | File Templates.
  */

 public class QueueTest {
 
     public static void main(String[] args) {
         MyQueue que = new MyQueue();
 
         System.out.println("Start");
 
         for (int i = 0; i < 10; i++) {
             System.out.print("Enqueuing. ");
             que.enqueue(i);
             System.out.println("Front: " + que.getFront() + ", Back: " + que.getBack());
         }
 
         for (int i = 0; i < 9; i++) {
             System.out.print("Dequeuing. ");
             que.dequeue();
 
             System.out.println("Front: " + que.getFront() + ", Back: " + que.getBack());
         }
 
         System.out.println("Stop");
     }
 
 }
