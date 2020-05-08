 /*
 4.3.7. Ring buffer. A ring buffer or circular queue is a FIFO data structure of a fixed size N. It is useful for transferring data between asynchronous processes or storing log files. When the buffer is empty, the consumer waits until data is deposited; when the buffer is full, the producer waits to deposit data. A ring buffer has the following methods: 
 isEmpty(), isFull(), enqueue(), and dequeue(). 
 Write an generic data type RingBuffer using an array (with circular wrap-around for efficiency). 
 
  RingBuffer может быть как фифо, так и лифо. Тут просят фифо. Для этого надо контролить и точку вставки, и точку чтения. wrap-around означает, что когда ты достигаешь конца массива в добавлении, то надо начинать вставлять сначала. И так до бесконечности. При этом надо помнить позицию самого старого неперезаписанного элемента.
 
 abcdfghjkl
 1234567890
 
 
 */
 
 package JavaBook.Algo_4.StacksQueues_4_3;
 
 public class RingBuffer {
 
     private int[] values;
     private int lastAdded;
     private int firstToRead;
     
     public RingBuffer(int size) {
         this.values = new int[size];
        this.lastAdded = -1;
         this.firstToRead = -1;
     }
 
     // public RingBuffer(int[] _values) {
     //     this(_values, _values.length);
     // }    
     
     // public RingBuffer(int[] _values, int size) {
     //     this.values = new int[size];
     //     for (int i = 0; i < _values.length; i++) {
     //         if (_values[i] == 0) {
     //             this.last = i-1;
     //             break;
     //         }
     //         this.values[i] = _values[i]; 
     //     }
 
     //     this.last = _values.length -1;
     // }
     
     
     public boolean isEmpty() {
         return (lastAdded == -1 || firstToRead == -1);
     }
 
     public boolean isFull() {
         return (lastAdded + 1)%values.length == firstToRead;
     }
     
     public void enqueue(int item){
         if (this.isFull()) {
             System.out.println("Failed to add item, buffer is full.");
         }
         else {
            if (this.isEmpty()) {
                this.firstToRead = 0;
            }
             if (lastAdded == values.length - 1) {
                 values[0] = item;
                 lastAdded = 0;    
             }
             else {
                 values[++lastAdded] = item;
             }
         }
     }
     
     public int dequeue() {
         if (this.isEmpty()) {
             throw new IllegalArgumentException();
             //return null;
         }
         else {
             if (firstToRead == lastAdded) {
                 int i = firstToRead;
                 firstToRead = -1;
                 lastAdded = -1;
                 return values[i];
             }
             else if (firstToRead == values.length - 1) {
                 firstToRead = 0;
                 return values[values.length - 1];   
             }
             else {
                 return values[firstToRead++];
             }          
         }
     }
 }
 
