 package player;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 
 public class TupletTest {
 
     Note C = new Note("C", 0, "", 1, 4);
     Note D = new Note("C", 0, "", 1, 4);
     Note E = new Note("C", 0, "", 1, 4);
     
     //Test representation of time in simplest form, 
     //Maintaining a denominator as a multiple of 4.
     @Test
     public void TupletTest1() {
     	//test basic functionality of time representation: 4/8 becomes 2/4	
     	Tuplet t = new Tuplet(3);
     	t.addNote(C);
     	t.addNote(D);
     	t.addNote(E);
     	System.out.println(t);
     	assertEquals(" C  2/4",t.toString());
     }
     
}
