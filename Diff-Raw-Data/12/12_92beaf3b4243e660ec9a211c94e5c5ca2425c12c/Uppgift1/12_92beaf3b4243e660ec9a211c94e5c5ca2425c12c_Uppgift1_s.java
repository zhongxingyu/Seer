 package se.chalmers.datastrukt.lab1;
 
 /**
  * 
  * @author tomassellden and Anton Palmqvist group 36
  * 
  */
 
 //TODO (=) Ingen readme
 //TODO (=) Ni svarar inget p diskussionsfrgan i b-uppgiten om huruvida
 public class Uppgift1 {
 
 	private String[] stringArr;
 	private String[] tmpStringArr;
 	private int numberOfElements = 0;
 	private int pointer;
 	private int capacity;
 
 	/**
 	 * Default-constructor
 	 */
 	public Uppgift1() {
 		this(10);
 	}
 
 	/**
 	 * Constructor taking in an argument of the wanted length of the array
 	 */
 	public Uppgift1(int length) {
 		this.stringArr = new String[length];
 		capacity = length;
 
 	}
 
 	/**
 	 * Adding an element at the position in the beginning of the array. All
 	 * other elements are moved one step to the right
 	 */
 	public void addFirst(String element) {
 		if (numberOfElements >= capacity) {
 			reallocate();
 		}
 		tmpStringArr = new String[capacity];
 		tmpStringArr[0] = element;
 		for (int i = 0; i < numberOfElements; i++) {
 			tmpStringArr[i+1] = stringArr[i];
 		}
 		stringArr = tmpStringArr;
 		numberOfElements++;
 	}
 
 	/**
 	 * Checking if the array is empty, that is, has no elements.
 	 */
 	public boolean empty() {
 		for (int i = 0; i < stringArr.length; i++) {
 			if (stringArr[i] != null) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Returning the first string of the array
 	 */
 	public String getFirst() {
 		if (empty()) {
 			throw new ArrayIndexOutOfBoundsException("Array is empty");
 		}
 		return stringArr[0];
 	}
 
 	/**
 	 * Removing the first element of the array, 
 	 * adjusting all other elements one
 	 * step to the left
 	 */
 	public void removeFirst() {
 		if (empty()) {
 			throw new ArrayIndexOutOfBoundsException(
 					"Remove failed, array already empty");
 		}
 		int nbrOfElem = numberOfElements - 1;
 		tmpStringArr = new String[nbrOfElem];
 		System.arraycopy(stringArr, 1, tmpStringArr, 0, nbrOfElem);
 		stringArr = tmpStringArr;
 		if (stringArr.length > 0) {
 			numberOfElements--;
 		}
 	}
 
 	/**
 	 * Checking if a String exists in the array
 	 * 
 	 * @param elem
 	 * @return true if the String exists in the array
 	 */
 	public boolean existP(String elem) {
 		for (int i = 0; i < stringArr.length; i++) {
 			if (elem.equals(stringArr[i])) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Classic toString-method. Appending all array elements into one big
 	 * String.
 	 * 
 	 */
 	public String toString() {
 		StringBuilder stringBuilder = new StringBuilder();
 		for (int i = 0; i < numberOfElements; i++) {
 			stringBuilder.append(stringArr[i]).toString();
 			if (i < numberOfElements - 1) {
 				stringBuilder.append(", ");
 			}
 		}
 		String str = "[ " + stringBuilder.toString();
 		str += " ] ";
 		return str;
 	}
 
 	/**
 	 * Setting the pointer to a chosen position
 	 * 
 	 * @param p
 	 *            the pointer position
 	 */
 	public void setP(int p) {
 		/*Allowed to be set to the last place(numberOfElements). 
 		 * This pointer-position will not have a next value (hasNext will 
 		 * return false) however the possibility to put the pointer 
 		 * here should be allowed anyway.*/
 		if (p < 0 || p > numberOfElements) { 
 			throw new IndexOutOfBoundsException();
 		}
 		pointer = p;
 	}
 
 	/**
 	 * Checking if the array has an element to the right of the pointer
 	 * 
 	 * @return true if an element exists to the right of the pointer
 	 */
 	public boolean hasNext() {
 		return (pointer >= 0 && pointer < numberOfElements);
 	}
 
 	/**
 	 * Adding a String at the chosen indexposition, moving all elements to the
 	 * right one step to the right.
 	 * 
 	 * @param index
 	 * @param element
 	 */
 	public void addAfterP(int index, String element) {
 		if (index < 0 || index > numberOfElements) {
 			throw new IndexOutOfBoundsException();
 		}
 		if (numberOfElements == capacity) {// If the capacity is filled with
 			// elements the capacity should be reallocated
 			reallocate();
 		}
 		/*
 		 * Running the array backwards, shifting the elements one step to the
 		 * right. When the position of the pointerindex is reached the new
 		 * String-element is added there, which is the same as adding it to the
 		 * right of the thought pointer
 		 */
 		for (int i = numberOfElements; i > index; i--) {
 			stringArr[i] = stringArr[i - 1];
 		}
 		stringArr[index] = element; // capacity vad r
 		numberOfElements++;
 	}
 
 	/**
 	 * Reallocating the capacity of the array by increasing its length to twice
 	 * its length
 	 */
 	private void reallocate() {
 		capacity = capacity * 2;
 		tmpStringArr = new String[capacity];
 		for (int i = 0; i < numberOfElements; i++) {
 			tmpStringArr[i] = stringArr[i];
 		}
 		stringArr = tmpStringArr;
 	}
 
 	/**
 	 * Returning the string to the right of a chosen pointerposition
 	 * 
 	 * @param p
 	 * @return
 	 */
 	public String get(int p) {
 		if (p < 0 || p >= numberOfElements) {
 			throw new IndexOutOfBoundsException("No value exists at this position.");
 		}
 		return stringArr[p];
 	}
 
 	/**
 	 * Moving the pointer a desired amount of steps to the left or right. A
 	 * negative number moves it to the left and a positive number moves it to
 	 * the right.
 	 * 
 	 * @param val
 	 *            the chosen steps to move the pointer
 	 */
 	public void moveP(int val) {
 		if (pointer + val < 0 || pointer + val > numberOfElements) {
 			throw new IndexOutOfBoundsException();
 		}
 		pointer += val;
 	}
 
 	/**
 	 * Setting the pointer to the left of a chosen string, if it exists in the
 	 * array.
 	 * 
 	 * @param elem
 	 *            the string element we want to determine the position of, to
 	 *            set the pointer to the left of it
 	 */
 	// Remark: The method existP is not used here since 
 	// it would lead to running
 	// through the array
 	// twice, which would be redundant.
 	public void setPtoStringPos(String elem) {
 		boolean isChanged = false;
 		for (int i = 0; i < stringArr.length; i++) {
 			if (elem.equals(stringArr[i])) {
 				pointer = i;
 				isChanged = true;
 			}
 		}
 		if (!isChanged) {
 			throw new IllegalArgumentException(
 					"No such String, unable to set pointer");
 		}
 	}
 
 	public static void main(String[] args) {
 		// Testcases
 		int stringLength = 10;
 		Uppgift1 uppgA = new Uppgift1(stringLength);
 
 		// a)
 		// First test for method empty
 		System.out.println("*" + uppgA.empty() + " # br vara true");
 
 		// Test for method addFirst
 		uppgA.addFirst("Erland");
 		System.out.println("*" + uppgA + " # Br skriva ut: [ Erland ]");
 		uppgA.addFirst("Tomas");
 		uppgA.addFirst("Anton");
 		uppgA.addFirst("Henrik");
 		System.out.println("*" + uppgA + " # br vara Henrik frst");
 
 		// Second test for method empty
 		System.out.println("*" + uppgA.empty() + " # br vara false");
 
 		// Test for method getFirst
 		System.out.println("*" + uppgA.getFirst() + " # br vara Henrik");
 
 		// Test for method removeFirst
 		uppgA.removeFirst();
 		System.out.println("*" + uppgA + " # br vara Anton frst");
 
 		// Test for method existP
 		System.out.println("*" + uppgA.existP("Anton") + " # br vara true");
 		System.out.println("*" + uppgA.existP("Adam") + " # br vara false");
 
 		// Test for toString
 		System.out
 				.println("*" + uppgA + " # br vara [ Anton, Tomas, Erland ]");
 
 		// Test for reallocate
 		System.out.println("*" + uppgA.capacity + " # br vara 10");
 		uppgA.reallocate();
 		System.out.println("*" + uppgA.capacity + " # br vara 20");
 
 		// b)
 		Uppgift1 uppgB = new Uppgift1();
 		uppgB.addFirst("Julius");
 		uppgB.addFirst("Ingvar");
 		uppgB.addFirst("Henrik");
 		uppgB.addFirst("Gustav");
 		uppgB.addFirst("Felix");
 		uppgB.addFirst("Erik");
 		uppgB.addFirst("David");
 		uppgB.addFirst("Cecilia");
 		uppgB.addFirst("Bertil");
 		uppgB.addFirst("Anton");
 		System.out.println("*" + uppgB
 				+ " # br skriva ut 10 namn i alfabetisk ordning");
 
 		// Test for setP
 		uppgB.setP(4);
 		System.out.println("*" + uppgB.pointer + " # br skriva ut 4");
 
 		// Test for hasNext
 		System.out.println("*" + uppgB.hasNext() + " # br vara true");
 		uppgB.setP(10);
 		System.out.println("*" + uppgB.hasNext() + " # br vara false");
 
 		// Test for addAfterP
 		uppgB.addAfterP(5, "Sven");
 		System.out
 				.println("*"
 						+ uppgB
 						+ " # br skriva ut Sven p pekarposition 5," +
 						" allts efter Erik");
 
 		// Test for get
 		System.out
 				.println("*"
 						+ uppgB.get(2)
 						+ " # br returnera Cecilia, som ju ligger till " +
 						"hger om pekaren");
 		// Testar att IndexOutOfBoundsException kastas(bortkommenterad fr att
 		// krning ska g igenom fr andra test)
 		// System.out.println("*" + uppgB.get(13) +
 		// " # br returnera Cecilia, som ju ligger till hger om pekaren");
 		System.out.println("*" + uppgB.get(10)
 				+ " # br returnera Julius, som ju ligger sist");
 		/*Br ge IndexOutOfBoundsException eftersom det bara finns 11 element. 
 		P pekarplats 11 finns allts ingenting till hger eftersom man 
 		brjar rkna frn pekarplats 0.
 		Bortkommenterad fr att koden ska kompilera*/
 		//System.out.println("*" + uppgB.get(11));
 
 		// Test for moveP
 		uppgB.setP(0);
 		uppgB.moveP(5); // Flyttar pekaren 5 steg t hger
 		System.out.println("*" + uppgB.pointer + " # br vara 5");
 		uppgB.moveP(-2); // Flyttar pekaren tv steg t vnster
 		System.out.println("*" + uppgB.pointer + " # br vara 3");
 		uppgB.moveP(8); // Flyttar pekaren tv steg t vnster
 		System.out.println("*" + uppgB.pointer + " # br vara 11");
 		/*Br ge IndexOutOfBoundsException eftersom det bara finns 11 element.
 		Det gr inte att stlla pekaren till hger om ett element som inte 
 		finns.
 		Bortkommenterad fr att koden ska kompilera*/
 		//uppgB.moveP(1);
 		
 
 		// Test for setPtoStringPos
 		uppgB.setPtoStringPos("David"); // Setting the pointer to the left of
 										// the String "David" if it exists in
 										// the array
 		System.out.println("*" + uppgB.pointer + " # br vara 3");
 		uppgB.setPtoStringPos("Julius");
 		System.out.println("*" + uppgB.pointer + " # br vara 10");
 		// Kollar att exception kastas(bortkommenterad fr att vrig kod ska
 		// kras
 	}
 }
