 package logic;
 /**
  * This class does the sorting.
  */
 public class Sorter {
 	String [] array;
 	int numberOfThreads;
 	String [][] arraysOfArrays;
 	// for debugging
 		int numberOfArrays = 0;
 	public Sorter(int numberOfThreads, String [] strings) {
 		this.numberOfThreads = numberOfThreads;
 		this.array = strings;
 		// denne trengs å endre til å bruke numberOfThreads og forholde seg til
 		// stringene::
		arraysOfArrays = fillArrays(arraysOfArrays);
 		// 1. dele opp arrayet i like mange array som tråder
 		//		sette opp disse i et array med pekere til disse
 		//		sette opp et bool-array med true / false
 		//
 		// 2. starte opp sortering i hver tråd.
 		// 3. når en tråd er ferdig sjekker den om det er noen andre ferdige, sjekk
 		// bool-array..
 		// 4. hvis ingen andre ferdige wait.
 		// 5. hvis andre ferdige notify.
 		// 6. hvis melding fra en annen om at det er på tide å våkne, sjekke
 		//		bool-array med hvilken som er ferdig.
 		// 7. hvis ingen andre ferdige, vent
 		// 8. 
 		System.out.println(numberOfArrays);
 	}
 	/**
 	 * @return the sorted array.
 	 */
 	public String [] getArray() {
 		return array;
 	}
	private String[][] fillArrays(String [][] allArrays) {
 		allArrays = new String[numberOfThreads][];
 		for (int i = 0; i < array.length; i+=arrayLength()) {
 			if (beforeLastArray(i)) 
 				allArrays[whichArray(i)] = fillArray(array, arrayLength(), i);
 			if (lastArray(i)) 
 				allArrays[whichArray(i)] = fillArray(array, lastArrayLength(), i);
 		}
 		return allArrays;
 	}
 	private String [] fillArray(String [] oldArray, int length, int start) {
 		String [] newArray = new String [length];
 		for (int i = 0; i < length; i++)
 			newArray[i] = oldArray[start + i];
 		numberOfArrays++;
 		return newArray;
 	}
 	private boolean beforeLastArray(int i) {
 		if (i / arrayLength() < numberOfThreads - 1) return true;
 		return false;
 	}
 	private boolean lastArray(int i) {
 		if (i / arrayLength() == numberOfThreads - 1) return true;
 		return false;
 	}
 	private int lastArrayLength() {
 		return arrayLength() + array.length % numberOfThreads;
 	}
 	private int arrayLength() {
 		return array.length / numberOfThreads;
 	}
 	private int whichArray(int i) {
 		return i / arrayLength();
 	}
 }
