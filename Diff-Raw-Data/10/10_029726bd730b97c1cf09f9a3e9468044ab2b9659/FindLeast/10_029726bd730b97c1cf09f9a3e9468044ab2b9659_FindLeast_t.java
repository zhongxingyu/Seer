 class FindLeast {
 	//private K [] array;
 	final static int MAX_ARRAY_LENGTH = 100;
 	FindLeast(int [] array) {
 		//this.array = array;
 		findLeast(array);//(this.array);
 		int n = array[0];
		int m = array[0];
 		// Linear least
		for (int i = 1; i < array.length; i++) {
			if (n > array[i]) n = array[i];
			if (m < array[i]) m = array[i];
		}
 		System.out.println("Linear least: " + n);
		System.out.println("Linear last: " + m);
 
 	}
 	private int findLeast(int [] array) {
 		int [] arrayPart1;
 		int [] arrayPart2;
 		int newLength;
 		if (array.length > MAX_ARRAY_LENGTH) {
 			newLength = array.length/2;
 
 			arrayPart1 = fillArray(array, newLength, 0);
 			// the length of this array is set the old length - the new length
 			// to get all numbers if the length of the old array were uneven.
 			arrayPart2 = fillArray(array, array.length - newLength, newLength);
 			System.out.println("First array: " + arrayPart1.length);
 			System.out.println("Second array: " + arrayPart2.length);
 
 		}
 		return -1;
 	}
 	private int [] fillArray(int [] oldArray, int length, int start) {
 		System.out.println("The old array's size: " + oldArray.length);
 		System.out.println("Point to start: " + start);
 		int [] newArray = new int[length];
 		for (int i = start; i < length; i++)
 			newArray[i] = oldArray[start + i];
 		System.out.println("The new array's size: " + newArray.length);
 		return newArray;
 
 	}
 }
