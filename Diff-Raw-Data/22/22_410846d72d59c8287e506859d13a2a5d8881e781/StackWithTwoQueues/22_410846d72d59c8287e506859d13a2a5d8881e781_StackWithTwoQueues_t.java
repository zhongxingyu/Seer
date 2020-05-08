 package alex.datastructures;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 public class StackWithTwoQueues<T> {
 
 	private Queue<T> queue1 = new LinkedList<T>();
 	private Queue<T> queue2 = new LinkedList<T>();
 
 	public void push(T t) {
 		if (!queue2.isEmpty()) {
 			queue2.add(t);
 		} else {
 			queue1.add(t);
 		}
 	}
 
 	public T pop() {
 		if (queue1.isEmpty() && queue2.isEmpty()) {
 			throw new RuntimeException("Empty Stack");
 		}
 		Queue<T> empty = queue1;
 		Queue<T> nonEmpty = queue2;
 		if (empty.size() > 0) {
 			empty = queue2;
 			nonEmpty = queue1;
 		}
		while (nonEmpty.size() > 1) {
 			empty.add(nonEmpty.poll());
 		}
		return nonEmpty.poll();
 	}
 
	public boolean isEmpty() {
		return !(queue1.size() > 0 || queue2.size() > 0);
 	}
 
	public static void main(String[] args) {
		StackWithTwoQueues<Integer> stack = new StackWithTwoQueues<>();
		stack.push(1);
		stack.push(2);
		stack.push(3);
		System.out.println(stack.pop());
		stack.push(4);
		while (!stack.isEmpty()) {
			System.out.println(stack.pop());
		}
	}
 }
