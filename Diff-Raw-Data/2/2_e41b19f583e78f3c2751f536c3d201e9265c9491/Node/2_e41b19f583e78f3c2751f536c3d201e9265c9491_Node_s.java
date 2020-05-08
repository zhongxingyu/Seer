 package lists;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class Node {
 	
 	int value;
 	Node next;
 	
 	public Node(int value) {
 		this.value = value;
 	}
 	
 	/** Convenience method for construction/method chaining. */
 	public Node next(int value) {
 		return (this.next = new Node(value));
 	}
 	
 	/** Assuming this Node is the head of the list, 
 	 * @return head of this list, reversed. */
 	public Node reverseList() {
 		if (this.next == null) return this;
 		
 		Node newHead = this.next.reverseList();
 		this.next.next = this;
 		this.next = null; // new tail
 		
 		return newHead;
 	}
 	
 	@Test
 	public static void testReverseList() {
 		Node head;
 		
 		head = new Node(1); // need to keep head reference
 		Assert.assertEquals(head.reverseList().value, 1);
 		
 		head.next(2).next(3).next(4);
 		head = head.reverseList();
 		
 		Assert.assertEquals(head.value, 4);
 		Assert.assertEquals(head.next.value, 3);
 		Assert.assertEquals(head.next.next.value, 2);
 		Assert.assertEquals(head.next.next.next.value, 1);
 		
		Assert.assertNull(head.next.next.next.next.value);
 	}
 }
