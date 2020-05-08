 package player;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 
 public class NoteTest {
 
     //Test representation of time in simplest form, 
     //Maintaining a denominator as a multiple of 4.
     @Test
     public void NoteTest1() {
     	//test basic functionality of time representation: 4/8 becomes 2/4	
     	Note test = new Note("C", "", "", 4, 8);
<<<<<<< HEAD
    	
    	String output = test.toString();
    	assertEquals(" C  2/4",output);
 
    }
=======
     	assertEquals(" C  2/4",test.toString());
     }
     
     @Test
     public void NoteTest2() {
         //test basic functionality of time representation: 1/2 becomes 2/4  
         Note test = new Note("C", "", "", 1, 2);
         assertEquals(" C  2/4",test.toString());
     }
     
     @Test
     public void NoteTest3() {
         //test basic functionality of time representation: 6/9 becomes 8/12  
         Note test = new Note("C", "", "", 6, 9);
         assertEquals(" C  8/12",test.toString());
     }
     
     @Test
     public void NoteTest4() {
         //test accidental, octaves.   
         Note test = new Note("C", "^^", ",,", 2, 4);
         assertEquals("^^ C ,, 2/4",test.toString());
     }
     
    

>>>>>>> 4ae08fce474cbcff0eb6a3982962686eab6f584e
 }
