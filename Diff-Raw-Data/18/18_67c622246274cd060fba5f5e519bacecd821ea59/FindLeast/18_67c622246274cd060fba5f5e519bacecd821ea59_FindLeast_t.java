 class FindLeast {
 	//private K [] array;
 	final static int MAX_ARRAY_LENGTH = 100;
 	FindLeast(int [] array) {
 		//this.array = array;
 		System.out.println("Recursive least: " + findLeast(array));//(this.array);
 
 	}
 	private int findLeast(int [] array) {
 
 		int [] arrayPart1;
 		int [] arrayPart2;
 		int newLength;
 		int leastInFirstArray;
 		int leastInSecondArray;
 		int leastNumber = -1;
 		
 		if (array.length > MAX_ARRAY_LENGTH) {
 			newLength = array.length/2;
 			arrayPart1 = fillArray(array, newLength, 0);
 			// the length of this array is set the old length - the new length
 			// to get all numbers if the length of the old array were uneven.
 			arrayPart2 = fillArray(array, array.length - newLength, newLength);
 			leastInFirstArray = findLeast(arrayPart1);
 			leastInSecondArray = findLeast(arrayPart2);
 			//System.out.println("### " + leastInFirstArray + " # # # " + leastInSecondArray + " ###");
 			//System.out.println("First array: " + arrayPart1.length);
 			//System.out.println("Second array: " + arrayPart2.length);
			if (leastInSecondArray <= leastInFirstArray) 
				return leastInSecondArray;
			if (leastInFirstArray < leastInSecondArray) 
				return leastInFirstArray;
 		}
 		if (array.length <= MAX_ARRAY_LENGTH) {
 			int n = array[0];
 			for (int i = 0; i < array.length; i++) {
 				if (n > array[i]) n = array[i];
 			}
 			return n;
 		}
 		return -1;
 	}
 	private int [] fillArray(int [] oldArray, int length, int start) {
 		//System.out.println("The old array's size: " + oldArray.length);
 		//System.out.println("Point to start: " + start);
 		int [] newArray = new int[length];
 		for (int i = 0; i < length; i++)
 			newArray[i] = oldArray[start + i];
 		//for (int i = start; i < length; i++)
 		//System.out.println("The new array's size: " + newArray.length);
 		return newArray;
 
 	}
 }
