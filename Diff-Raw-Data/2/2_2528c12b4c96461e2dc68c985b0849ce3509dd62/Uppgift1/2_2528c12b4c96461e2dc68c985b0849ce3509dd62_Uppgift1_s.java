 package se.chalmers.datastrukt.lab1;
 
 
 public class Uppgift1 {
 
 	private String[] stringArr;
 	private String[] tmpStringArr;
 	private int numberOfElements = 0;
 	private int arrayPosition;
 	private int capacity;
 
 	public Uppgift1() {
		this.capacity = 10;
 	}
 	public Uppgift1(int length) {
 		this.stringArr = new String[length];
 	}
 
 	public void addFirst(String element) {
 		int tmpCount = 1;
 		// int number = numberOfElements(stringArr);
 		tmpStringArr = new String[numberOfElements + 1];
 		for (int i = 0; i < stringArr.length; i++) {
 			if (stringArr[i] != null) {
 				tmpStringArr[tmpCount] = stringArr[i];
 				tmpCount++;
 			}
 			
 		}
 		tmpStringArr[0] = element;
 		stringArr = tmpStringArr;
 
 		numberOfElements++;
 
 	}
 
 	// private int numberOfElements(String[] strArray) {
 	// int number = 0;
 	// for (int i = 0; i < strArray.length; i++) {
 	// if (strArray[i] != null) {
 	// number++;
 	// }
 	// }
 	// return number;
 	// }
 
 	public boolean empty() {
 		for (int i = 0; i < stringArr.length; i++) {
 			if (stringArr[i] != null) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public String getFirst() {
 		return stringArr[0];
 	}
 
 	public void removeFirst() {
 		if (!empty()) {
 			int nbrOfElem = numberOfElements - 1;
 			tmpStringArr = new String[nbrOfElem];
 			System.arraycopy(stringArr, 1, tmpStringArr, 0, nbrOfElem);
 			stringArr = tmpStringArr;
 		}
 		if (stringArr.length > 0) {
 			numberOfElements--;
 		}
 	}
 
 	public boolean existP(String elem) {
 		for (int i = 0; i < stringArr.length; i++) {
 			if (elem.equals(stringArr[i])) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String toString() {
 		StringBuilder stringBuilder = new StringBuilder();
 
 		for (int i = 0; i < stringArr.length; i++) {
 			stringBuilder.append(stringArr[i]).toString();
 			if (i < stringArr.length - 1) {
 				stringBuilder.append(" , ");
 			}
 		}
 		String str = "[ " + stringBuilder.toString();
 		str += " ] ";
 
 		return str;
 	}
 	public void setP(int p) {
 		if (p >= 0 || p <= numberOfElements) {
 			throw new IndexOutOfBoundsException();// bsta lsning?
 		} else {
 			arrayPosition = p;
 		}
 
 	}
 
 	public boolean hasNext() { // rtt metodsignatur?
 		return (arrayPosition + 1 <= numberOfElements);
 		// return (p >= 0 || p <= numberOfElements); // kastar exceptions p
 		// andra
 		// stllen
 	}
 
 	public void addAfterP(int index, String element) {
 		if (index >= 0 || index <= numberOfElements) {
 			throw new IndexOutOfBoundsException();
 		} else {
 			if (numberOfElements == capacity) {
 				reallocate();
 			}
 			// tmpStringArr = new String[numberOfElements + 1];
 			for (int i = numberOfElements; i > index; i--) {
 				stringArr[i] = stringArr[i - 1];
 			}
 			stringArr[index] = element; // capacity vad r
 										// det!?!?!?!??!?!?!?!?!
 			// tmpStringArr = new String[numberOfElements + 1];
 			// for (int i = 0; i < index; i++) {
 			// tmpStringArr[i] = stringArr[i];
 			// }
 			// tmpStringArr[index] = element;
 			// for (int i = index; i < numberOfElements; i++) {
 			// tmpStringArr[i + 1] = stringArr[i];
 			// }
 			// stringArr = tmpStringArr;
 			numberOfElements++;
 
 		}
 	}
 
 	private void reallocate() {
 		System.out.println(capacity);
 		capacity = capacity * 2;
 		tmpStringArr = new String[capacity];
 		for (int i = 0; i < stringArr.length; i++) {
 			tmpStringArr[i] = stringArr[i];
 		}
 		stringArr = tmpStringArr;
 
 	}
 
 	public String get(int p) {
 		if (p >= 0 || p <= numberOfElements) {
 			throw new IndexOutOfBoundsException();
 		}
 		return stringArr[p]; // + 1?
 	}
 
 	public void moveP(int val) {
 		if (arrayPosition + val > 0 || arrayPosition + val <= numberOfElements) {
 			throw new IndexOutOfBoundsException();
 		} else {
 			arrayPosition += val;
 		}
 	}
 
 	public void setPtoStringPos(String elem) {
 		for (int i = 0; i < stringArr.length; i++) {
 			if (elem.equals(stringArr[i])) {
 				arrayPosition = i;
 			}
 		}
 	}
 	public static void main(String[] args) {
 		int stringLength = 10;
 		Uppgift1 uppg = new Uppgift1(stringLength);
 
 		// First test for method empty
 		System.out.println("*" + uppg.empty() + " # br vara true");
 
 		// Test for method addFirst
 		uppg.addFirst("hej");
 		uppg.addFirst("Tomas");
 		uppg.addFirst("Anton");
 		uppg.addFirst("Henrik");
 		uppg.addFirst("hejsan");
 		System.out.println("*" + uppg + " # br vara hejsan frst");
 
 		// Second test for method empty
 		System.out.println("*" + uppg.empty() + " # br vara false");
 
 		// Test for method getFirst
 		System.out.println("*" + uppg.getFirst() + " # br vara hejsan");
 
 		// Test for method removeFirst
 		uppg.removeFirst();
 		System.out.println("*" + uppg + " # br vara Henrik frst");
 
 		// Test for method existP
 		System.out.println("*" + uppg.existP("Anton") + " # br vara true");
 		System.out.println("*" + uppg.existP("Adam") + " # br vara false");
 	}
 }
