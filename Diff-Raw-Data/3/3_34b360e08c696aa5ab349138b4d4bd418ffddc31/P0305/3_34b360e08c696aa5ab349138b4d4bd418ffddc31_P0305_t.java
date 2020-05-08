 /* 3.5 Implement a MyQueue class which implements a queue using two stacks.
  * */
 
 import java.util.Stack;
 
 public class P0305 {
    public class Queue<T> {
       private Stack<T> in, out;
       private int capacity;
       private int size;
       private int inCapacity;
       private int outCapacity;
 
       public Queue() {
          this(4);
       }
 
       public Queue(int capacity) {
          this.capacity = capacity;
          // we could randomly allocate the capacity to the two stacks,
          // for convenience, we constrict the capacity of each stack as
          // half of the queue.
          if (capacity % 2 == 0)
             inCapacity = outCapacity = capacity / 2;
          else {
             // it is the best to keep the capacity even, otherwise, we cannot
             // use the full capacity when pushing elements continuously.
            // Moreover, we cannot set the outCapacity < inCapacity, since the
            // first element will be printed out lastly after pushing 
            // continuously.
             outCapacity = capacity / 2 + 1;
             inCapacity = capacity / 2;
          }
          in = new Stack<T>();
          out = new Stack<T>();
          size = 0;
       }
 
       public void enqueue(T value) {
          if (isInStackFull()) {
             if (isStackEmpty(out))
                transport();
             else
                throw new IllegalStateException("The queue is full!");
          }
 
          in.push(value);
          size++;
       }
 
       public T dequeue() {
          if (isStackEmpty(out)) {
             if (!isStackEmpty(in))
                transport();
             else throw new IllegalStateException("The queue is empty");
          }
 
          size--;
          return out.pop();
       }
 
       public int getSize() {
          return size;
       }
 
       public int getCapacity() {
          return capacity;
       }
 
       public boolean isFull() {
          return size == capacity;
       }
 
       public boolean isEmpty() {
          return size == 0;
       }
 
       // transport elements from the in stack to the out stack
       private void transport() {
          if (!isStackEmpty(out))
             throw new IllegalStateException("The out stack is not empty, " +
                "CANNOT tranport!");
 
          while (!isOutStackFull() && !isStackEmpty(in)) {
             out.push(in.pop());
          }
       }
 
       private boolean isInStackFull() {
          return in.size() == inCapacity;
       }
 
       private boolean isOutStackFull() {
          return out.size() == outCapacity;
       }
 
       private boolean isStackEmpty(Stack<T> stack) {
          return stack.size() == 0;
       }
    }
 
    public static void main(String[] args) {
       int capacity = Integer.parseInt(args[0]);
       P0305 p0305 = new P0305();
       Queue<Integer> queue = p0305.new Queue<Integer>(capacity);
       //queue.dequeue();
       for (int i = 0; i < capacity; i++)
          queue.enqueue(i);
       for (int i = 0; i < capacity; i++)
          System.out.println(queue.dequeue());
    }
 }
