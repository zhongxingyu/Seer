 import java.util.*;
 
 public class C3Q11 {
     private Nody top1, top2, top3;
     private LinkedList<Integer> availableIndex = null;
     private int aSize = 6;
     private Nody[] baseArray = new Nody[aSize];
     
     public static void main(String... args) {
 	C3Q11 sample = new C3Q11();
 	Stackey s1 = sample.getStackey(1);
 	Stackey s2 = sample.getStackey(2);
 	Stackey s3 = sample.getStackey(3);
 	s1.push(1);
 	s1.push(2);
 	s2.push(11);
 	s1.push(3);
 	s2.push(12);
 	s3.push(111);
 	System.out.println(s1.pop());
 	s2.push(13);
 	System.out.println(s3.pop());
 	System.out.println(s3.pop());
 	s1.push(4);
 	s1.pop();
 	s1.pop();
 	s3.push(111);
 	s3.push(112);
 	sample.printArray();
     }
 
     public C3Q11() {
 	top1 = new Nody(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
 	top2 = new Nody(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
 	top3 = new Nody(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
 	availableIndex = new LinkedList<Integer>();
 	for (int i = 0; i < aSize; i++) {
 	    availableIndex.add(i);
 	}
     }
 
     public Stackey getStackey(int index) {
 	Nody top = null;
 	switch (index) {
 	case 1 : top = top1;
 	    break;
 	case 2 : top = top2;
 	    break;
 	case 3 : top = top3;
 	}
 	return new Stackey(top, availableIndex, baseArray);
     }
 
     public void printArray() {
 	for (Nody node : baseArray) {
 	    System.out.printf("%4d", node == null ? 0 : node.getValue());
 	}
 	System.out.println();
     }
 }
 
 class Stackey {
     private Nody top = null;
     private LinkedList<Integer> availableIndex = null;
     private Nody[] baseArray = null;
     
     public Stackey(Nody top, LinkedList<Integer> availableIndex, Nody[] baseArray) {
 	this.top = top;
 	this.availableIndex = availableIndex;
 	this.baseArray = baseArray;
     }
 
     public boolean push(int value) {
	if (availableIndex.peek() == null) return false;
 	int index = availableIndex.pop();
 	Nody newNode = new Nody(value, index, top.getIndex());
 	top = newNode;
 	baseArray[index] = top;
 	return true;
     }
 
     public int pop() {
 	if (top.getIndex() == Integer.MIN_VALUE) return Integer.MIN_VALUE;
 	int result = top.getValue();
 	availableIndex.push(top.getIndex());
 	if (top.getNext() != Integer.MIN_VALUE) top = baseArray[top.getNext()];
 	else top = new Nody(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
 	return result;
     }
 }
 
 class Nody {
     private int value = 0;
     private int cIndex = 0;
     private int preIndex = 0;
 
     public Nody(int value, int cIndex, int preIndex) {
 	this.value = value;
 	this.cIndex = cIndex;
 	this.preIndex = preIndex;
     }
 
     public int getIndex() {
 	return cIndex;
     }
     
     public int getNext() {
 	return preIndex;
     }
 
     public int getValue() {
 	return value;
     }
 }
