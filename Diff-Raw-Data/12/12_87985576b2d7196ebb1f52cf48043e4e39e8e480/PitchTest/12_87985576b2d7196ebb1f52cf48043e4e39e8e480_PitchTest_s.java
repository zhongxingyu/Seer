 package sound;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class PitchTest {
 
     /*
      * Testing Strategy
      * 
      * To test equals(), we created instances of each class, each varying by the others slightly. We created instances that reflected different combinations of differences. 
      * Tested to make sure equals() was reflexive and that two structurally equal instances were equals().
      * 
      * To test toString(), we created instances of each class, each varying by the others slightly. We created instances that reflected different combinations of differences. 
      * Tested to make sure instances returned the correct string and that structurally equivalent instances returned the same string.
      * 
      * To test hashCode(), we tested to be sure that structurally equivalent instances returned the same hash code and that hashCode() was reflexive.
      * 
      * To test clone(), we tested to make sure that a cloned instance would be equals() to its parent, would return the same toString() and would return the same midi note.
      * 
      * To test toMidiNote(), we tested to make sure that transposing a note by 0 octaves or semitones would return the same result as a non altered note.
      * 
     * I encorporated testing of accidentalTranspose(), octaveTranspose(), and transpose() into almost all tests. I tested to check that tranposing by 0 of anything would be the same as the original note. I also looked at transposing up and down one or two octaves or semitones.
      * 
     * To test lessThan() and difference(), I used transposing by different amounts to check for differences and whether or not a note was less than another. I also started with different pitches to check if that had any effect as well. I also tested to make sure that one note was note less than itself or an equivalent note.
      * 
     * To test getAccidental(), I transposed by different amounts and checked for propper accidental returns. Transposing by an octave should not effect the accidental, and this was tested.
      * 
      */
 
     @Test
     public void testBasicPitchTranspose() {
         Pitch p = new Pitch('C').accidentalTranspose(1);
         assertEquals("^C", p.toString());
     }
 
    /*
     * Tests equals() as well as accidentalTranspose, transpose, and octaveTranspose
     */
     @Test
     public void testPitchEquals() {
         Pitch pitch1 = new Pitch('C');
         Pitch pitch2 = new Pitch('C');
         Pitch pitch3 = new Pitch('G');
 
         assertEquals(true, pitch1.equals(pitch1));
         assertEquals(true, pitch1.equals(pitch2));
         assertEquals(false, pitch1.equals(pitch3));
     }
 
     @Test
     public void testPitchEqualsAccidentalTranspose() {
         Pitch pitch1 = new Pitch('C');
         Pitch pitch4 = new Pitch('C').accidentalTranspose(1);
         Pitch pitch5 = new Pitch('C').accidentalTranspose(-1);
         Pitch pitch6 = new Pitch('C').accidentalTranspose(0);
         assertEquals(false, pitch1.equals(pitch4));
         assertEquals(false, pitch1.equals(pitch5));
         assertEquals(true, pitch1.equals(pitch6));
     }
 
     @Test
     public void testPitchEqualsOctaveTranspose() {
         Pitch pitch1 = new Pitch('C');
         Pitch pitch7 = new Pitch('C').octaveTranspose(1);
         Pitch pitch8 = new Pitch('C').octaveTranspose(-1);
         Pitch pitch9 = new Pitch('C').octaveTranspose(0);
         assertEquals(false, pitch1.equals(pitch7));
         assertEquals(false, pitch1.equals(pitch8));
         assertEquals(true, pitch1.equals(pitch9));
     }
 
     @Test
     public void testPitchEqualsTranspose() {
         Pitch pitch1 = new Pitch('C');
         Pitch pitch10 = new Pitch('C').transpose(1);
         Pitch pitch11 = new Pitch('C').transpose(-1);
         Pitch pitch12 = new Pitch('C').transpose(0);
         assertEquals(false, pitch1.equals(pitch10));
         assertEquals(false, pitch1.equals(pitch11));
         assertEquals(true, pitch1.equals(pitch12));
     }
 
     @Test
     public void testPitchHashCode() {
         Pitch pitch1 = new Pitch('C');
         Pitch pitch2 = new Pitch('C');
 
         assertEquals(true, pitch1.hashCode() == pitch1.hashCode());
         assertEquals(true, pitch1.hashCode() == pitch2.hashCode());
     }
 
     @Test
     public void testPitchToString() {
         Pitch p0 = new Pitch('C').accidentalTranspose(0);
         Pitch p1 = new Pitch('C').accidentalTranspose(1);
         Pitch p2 = new Pitch('C').accidentalTranspose(2);
         Pitch p3 = new Pitch('C').accidentalTranspose(-1);
         Pitch p4 = new Pitch('C').accidentalTranspose(-2);
         Pitch p5 = new Pitch('C').octaveTranspose(0);
         Pitch p6 = new Pitch('C').octaveTranspose(1);
         Pitch p7 = new Pitch('C').octaveTranspose(2);
         Pitch p8 = new Pitch('C').octaveTranspose(-1);
         Pitch p9 = new Pitch('C').octaveTranspose(-2);
         Pitch p10 = new Pitch('C');
         
         assertEquals("C", p0.toString());
         assertEquals("^C", p1.toString());
         assertEquals("^^C", p2.toString());
         assertEquals("_C", p3.toString());
         assertEquals("__C", p4.toString());
         assertEquals("C", p5.toString());
         assertEquals("c", p6.toString());
         assertEquals("c'", p7.toString());
         assertEquals("C,", p8.toString());
         assertEquals("C,,", p9.toString());
         assertEquals("C", p10.toString());
         
         
     }
     @Test
     public void testPitchDifference() {
         Pitch p0 = new Pitch('C').accidentalTranspose(0);
         Pitch p1 = new Pitch('C').accidentalTranspose(1);
         Pitch p2 = new Pitch('C').accidentalTranspose(2);
         Pitch p3 = new Pitch('C').accidentalTranspose(-1);
         Pitch p4 = new Pitch('C').accidentalTranspose(-2);
         Pitch p5 = new Pitch('C').octaveTranspose(0);
         Pitch p6 = new Pitch('C').octaveTranspose(1);
         Pitch p7 = new Pitch('C').octaveTranspose(2);
         Pitch p8 = new Pitch('C').octaveTranspose(-1);
         Pitch p9 = new Pitch('C').octaveTranspose(-2);
         Pitch p10 = new Pitch('C');
         
         assertEquals(0, p0.difference(p0));
         assertEquals(-1, p0.difference(p1));
         assertEquals(-2, p0.difference(p2));
         assertEquals(1, p0.difference(p3));
         assertEquals(2, p0.difference(p4));
         assertEquals(0, p0.difference(p5));
         assertEquals(-12, p0.difference(p6));
         assertEquals(-24, p0.difference(p7));
         assertEquals(12, p0.difference(p8));
         assertEquals(24, p0.difference(p9));
         assertEquals(0, p0.difference(p10));
     }
 
     @Test
     public void testPitchToMidiNote() {
         Pitch p0 = new Pitch('C').accidentalTranspose(0);
         Pitch p5 = new Pitch('C').octaveTranspose(0);
         Pitch p10 = new Pitch('C');
 
         
         assertEquals(true, p0.toMidiNote() == p5.toMidiNote());
         assertEquals(true, p0.toMidiNote() == p10.toMidiNote());
     }
 
     @Test
     public void testPitchLessThan() {
         Pitch p0 = new Pitch('C').accidentalTranspose(0);
         Pitch p1 = new Pitch('D').accidentalTranspose(1);
         Pitch p2 = new Pitch('E').accidentalTranspose(2);
         Pitch p3 = new Pitch('D').accidentalTranspose(-1);
         Pitch p4 = new Pitch('C').accidentalTranspose(-2);
         Pitch p5 = new Pitch('D').octaveTranspose(0);
         Pitch p6 = new Pitch('F').octaveTranspose(1);
         Pitch p7 = new Pitch('E').octaveTranspose(2);
         Pitch p8 = new Pitch('D').octaveTranspose(-1);
         Pitch p9 = new Pitch('A').octaveTranspose(-2);
         Pitch p10 = new Pitch('C');
         
         assertEquals(false, p0.lessThan(p0));
         assertEquals(true, p0.lessThan(p1));
         assertEquals(true, p0.lessThan(p2));
         assertEquals(true, p0.lessThan(p3));
         assertEquals(false, p0.lessThan(p4));
         assertEquals(true, p0.lessThan(p5));
         assertEquals(true, p0.lessThan(p6));
         assertEquals(true, p0.lessThan(p7));
         assertEquals(false, p0.lessThan(p8));
         assertEquals(false, p0.lessThan(p9));
         assertEquals(false, p0.lessThan(p10));
     }
 
     @Test
     public void testPitchGetAccidental() {
         Pitch p0 = new Pitch('C').accidentalTranspose(0);
         Pitch p1 = new Pitch('D').accidentalTranspose(1);
         Pitch p2 = new Pitch('E').accidentalTranspose(2);
         Pitch p3 = new Pitch('D').accidentalTranspose(-1);
         Pitch p4 = new Pitch('C').accidentalTranspose(-2);
         Pitch p5 = new Pitch('D').octaveTranspose(0);
         Pitch p6 = new Pitch('F').octaveTranspose(1);
         Pitch p7 = new Pitch('E').octaveTranspose(2);
         Pitch p8 = new Pitch('D').octaveTranspose(-1);
         Pitch p9 = new Pitch('A').octaveTranspose(-2);
         Pitch p10 = new Pitch('C');
         
         assertEquals(0, p0.getAccidental());
         assertEquals(1, p1.getAccidental());
         assertEquals(2, p2.getAccidental());
         assertEquals(-1, p3.getAccidental());
         assertEquals(-2, p4.getAccidental());
         assertEquals(0, p5.getAccidental());
         assertEquals(0, p6.getAccidental());
         assertEquals(0, p7.getAccidental());
         assertEquals(0, p8.getAccidental());
         assertEquals(0, p9.getAccidental());
         assertEquals(0, p10.getAccidental());   
     }
 
     @Test
     public void testPitchClone() {
         Pitch p1 = new Pitch('A').accidentalTranspose(1);
         Pitch clone = p1.clone();
         
         assertEquals(true, p1.equals(clone));
         assertEquals(true, p1.hashCode() == clone.hashCode());
         assertEquals(true, p1.toString().equals(clone.toString()));
         assertEquals(true, p1.toMidiNote() == clone.toMidiNote());
     }
 
 
 }
