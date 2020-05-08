 package edu.ucsb.cs56.projects.utilities.TSA_Encryption;
 
 /**
 Cryptography class. Only 2 methods: the constructor, and encrypt. 
 
 @author Callum Steele, Miranda Aperghis, Trevor Adelman
 @version CS56 S13, 05/09/2013
 
  */
 
 public class Cryptography{
 
     private long totalCheck = 0;
     private long total = 1;
 
     /**
        basic class constructor
        @return nothing (void function)
      */
 
     public Cryptography() {}
 
     /**
        The encrypt function receives an array of integers and transforms it into a long value.
        @return a long with the resulting value
        @param numlist an int[] (list on integers) that gets iterated through throughout the function, the integers must be greater than or equal to 1 and less than or equal to 1000, and the length of the list must be greater than or equal to 2 and less than or equal to 50.
      */
 
     public long encrypt(int[] numList) {
 
 	for (int r=0; r < numList.length; r++){
 	    if (numList[r] < 1 || numList[r] > 1000){
 		System.out.println("Number Error; Integer x must follow: 1 <= x <= 1000");
	    return totalCheck;}
 	}
 
 	if (numList.length > 50 || numList.length < 2){
 	    System.out.println("Number List Error; List length must be between 2 and 50 inclusive");
 	    return totalCheck;
 	}
 
 	for (int i=0; i < numList.length; i++){
 	    numList[i]++;
 	    total = 1;
 
 	    for (int t=0; t < numList.length; t++){
 		total = total * numList[t];
 		if (total > totalCheck){
 		    totalCheck = total;
 		}
 	    }
 	    numList[i]--;
 	 }
 
 	return totalCheck;
     }
 
     /**
        Sets up a Cryptography object and uses it to encrypt an int array of {1,2,3}, outputting the result
        @param args the command line arguments
      */
 
     public static void main(String[] args){
 	Cryptography c1 = new Cryptography();
 	int[] ex1 = {1,2,3};
 	long output = c1.encrypt(ex1);
 	System.out.println(output);
     }
 }
