 public class RandQueueTest {
 	public static void main(String[] args){
 		TestIsEmpty();
 		TestAddItem(10);
		TestSample(500);
		TestDequeue(500);
 	}
 
 	public static void TestIsEmpty(){
 		RandomizedQueue<Integer>myQueue = new RandomizedQueue<Integer>();
 		System.out.println("Queue should be empty");
 		System.out.println(myQueue.isEmpty() == true);
 	}
 
 	public static void TestAddItem(int N){
 		RandomizedQueue<Integer>myQueue = new RandomizedQueue<Integer>();
 		System.out.println("Testing adding items");
 		for(int i=0; i<N; i++) {
 			myQueue.enqueue(i);
 		}
 		System.out.println(myQueue.size());
 	}
 
 	public static void TestSample(int N){
 		RandomizedQueue<Integer>myQueue = new RandomizedQueue<Integer>();
 		System.out.println("Testing sampling");
 		for(int i=0; i<N; i++) {
 			myQueue.enqueue(i);
 		}
 		System.out.println(myQueue.sample());
 		System.out.println(myQueue.sample());
 		System.out.println(myQueue.sample());
 	}
 
 	public static void TestDequeue(int N){
 		RandomizedQueue<Integer>myQueue = new RandomizedQueue<Integer>();
 		System.out.println("Testing dequeuing");
 
 		for(int i=0; i<N; i++) {
 			myQueue.enqueue(i);
 		}
 
 		System.out.println("size"+myQueue.size());
 
 		System.out.println(myQueue.dequeue());
 		System.out.println("size"+myQueue.size());
 
 		System.out.println(myQueue.dequeue());
 		System.out.println("size"+myQueue.size());
 
 		System.out.println(myQueue.dequeue());
 		System.out.println(myQueue.size());
 	}
 
 }
