 package spellcheck;
 
 import java.io.File;
 import java.io.*;
 import java.util.Iterator;
 
 public class TestHashDictionary {
   
     public static void main(String[] args) {
 
 	// Test 1: try empty constructor
 	try
 	{
 		HashDictionary h = new HashDictionary();
 		System.out.println("***Test 1 failed");
 	}
 	catch (DictionaryException e) {System.out.println("   Test 1 succeeded");}
 
        
 	StringHashCode sH = new StringHashCode();
      
 	float lF = (float) 0.5;
 	HashDictionary h = new HashDictionary(sH,lF);
 
 	// Test 2: add and find an entry .
 	Integer i = new Integer(1);
	
	try {
		h.insert("R3C1");
	} catch (DictionaryException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
 	if (!h.find("R3C1")) 
 	    System.out.println("***Test 2 failed");
 	else System.out.println("   Test 2 succeeded");
 
 	// Test 3: look for an inexistent key
 	if (h.find("R4C4")) 
 	  System.out.println("***Test 3 failed");
 	else System.out.println("   Test 3 succeeded");
 
 	// Test 4: try to delete a nonexistent entry.
 	// Should throw an exception.
 	try {
 	    h.remove("R6C8");
 	    System.out.println("***Test 4 failed");
 	} catch (DictionaryException e) {
 	    System.out.println("   Test 4 succeeded");
 	}
 
 	// Test 5: delete an actual entry.
 	// Should not throw an exception.
 	try {
 	    h.remove( "R3C1");
             if (!h.find("R3C1") )
 	         System.out.println("   Test 5 succeeded");
             else  System.out.println("***Test 5 failed");
 	} catch (DictionaryException e) {
 	    System.out.println("***Test 5 failed");
 	}
 
 	String s;
 
 	// Test 6: insert 200 different values into the Dictionary
 	// and check that all these values are in the Dictionary       
 	 Integer temp = new Integer(0);
 	 for (int k = 0; k < 200; ++k) {
 	     s = (new Integer(k)).toString();
 	     h.insert("R"+s+"C"+s);
 	 }
 	boolean fail = false;
 	for (int k = 0; k < 200; ++k) {
 	    s = (new Integer(k)).toString();
 	    if (!h.find("R"+s+"C"+s) ) {
 		 fail = true;
 	    }
 	}
 	if ( fail ) System.out.println("***Test 6 failed");
 	else System.out.println("   Test 6 succeeded");
 
 	// Test 7: Delete a lot of entries from the  Dictionary
 	fail = false;
 	try
 	{
 	    for ( int k = 0; k < 200; ++k )
 	    {
 		s = (new Integer(k)).toString();
 		h.remove("R"+s+"C"+s);
 	    }
 	   for (int k = 0; k < 200; ++k) {
 	      s = (new Integer(k)).toString();
 	      if (h.find("R"+s+"C"+s) ) {
 		  fail = true; }	    
 	   }
 	}
 	catch (DictionaryException e) 
 	{
 		System.out.println("***Test 7 failed");
 		fail = true;
 	}
 	if ( !fail) System.out.println("   Test 7 succeeded");
 	else  System.out.println("***Test 7 failed");
 
 
 	// Test 8: Get iterator over all keys
 	fail = false;
         h = new HashDictionary(sH,lF);
 	
 	for (int k = 0; k < 100; k++) 
 	{
            s = (new Integer(k)).toString();	       
            h.insert(s);
 	}
 	try
 	{
 	    for (int k = 10; k < 30; ++k) 
 	    {
                 s = (new Integer(k)).toString();
 		h.remove(s);
 	    }
 	}
 	catch(DictionaryException e)
 	{
 	    fail = true;
 	}
         Iterator it = h.elements();
 	int count = 0;
 
 	while (it.hasNext()){
 	    count= count+1;
 	    it.next();
 	}
 	if ( count == 80 && ! fail ) 
 	    System.out.println("   Test 8 succeeded");
 	else  System.out.println("****Test 8 failed ");
 
 
 	lF = (float) 0.05;
 
 	long startTime,finishTime;
 
 	double time;
 
 	while (lF < 0.99 ){
             startTime = System.currentTimeMillis();
 	    h = new HashDictionary(sH,lF);
 	
 	    for (int k = 0; k < 10000; ++k) {
 		s = (new Integer(k)).toString();
 		h.insert(s+"C"+s);
 	    }
 	    try{
 		for ( int k = 0; k < 10000; ++k ){
 		 s = (new Integer(k)).toString();
 	         h.remove(s+"C"+s);
 		}
 	    }
 	    catch (DictionaryException e) { System.out.print("Failure");return;}
             finishTime = System.currentTimeMillis();
             time = finishTime - startTime;
 	    System.out.println("For load factor "+lF+ " , average num. of  probes is " + h.averNumProbes()
                                + " time in milseconds is "+ time);
 	    lF = lF+ (float) 0.05;
 	}
 
     }
     
 }
     
 
 
 
