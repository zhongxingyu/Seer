 package chordest.chord.parser;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import chordest.io.lab.chordparser.ChordParser;
 import chordest.model.Chord;
 import chordest.model.Note;
 
 
 
 public class ChordParserTest {
 
 	@Test
 	public void test0() {
 		try {
 			Chord c1 = ChordParser.parseString("D#:(b3,5,b7,9)/5");
 			Chord c2 = new Chord(Note.DD, Note.F, Note.AD, Note.FD, Note.CD);
 			Assert.assertEquals(c1, c2);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testCmin7() {
 		try {
 			Chord c1 = ChordParser.parseString("C:min7");
 			Chord c2 = ChordParser.parseString("C:(b3,5,b7)");
 			Assert.assertEquals(c1, c2);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testDmin7b7() {
 		try {
 			Chord c1 = ChordParser.parseString("D:min7/b7");
 			Chord c2 = ChordParser.parseString("D:min7");
 			Assert.assertEquals(c1, c2);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testFSharpMaj9() {
 		try {
 			Chord c1 = ChordParser.parseString("F#:(3,5,7,9)");
 			Chord c2 = new Chord(Note.FD, Note.AD, Note.CD, Note.F, Note.GD);
 			Assert.assertEquals(c2, c1);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testOmit() {
 		try {
 			Chord c1 = ChordParser.parseString("C:min7(*5,11)");
 			Chord c2 = ChordParser.parseString("C:(b3,b7,11)");
 			Assert.assertEquals(c1, c2);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testSept() {
 		try {
 			Chord c1 = ChordParser.parseString("C:7");
 			Chord c2 = new Chord(Note.C, Chord.DOM);
 			Assert.assertEquals(c2, c1);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testMajor() {
 		try {
 			Chord c1 = ChordParser.parseString("G#");
 			Chord c2 = new Chord(Note.GD, Chord.MAJ);
 			Assert.assertEquals(c2, c1);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testMajor2forms() {
 		try {
 			Chord c1 = ChordParser.parseString("G#");
 			Chord c2 = ChordParser.parseString("G#:maj");
 			Assert.assertEquals(c2, c1);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testMajorWithBass() {
 		try {
 			Chord c1 = ChordParser.parseString("C/2");
 			List<Note> notes = new Chord(Note.C, Chord.MAJ).getNotesAsList();
 			notes.add(Note.D);
 			Chord c2 = new Chord(notes.toArray(new Note[notes.size()]));
 			Assert.assertEquals(c2, c1);
 			Assert.assertEquals(c2.getRoot(), c1.getRoot());
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testMajorWithBassNoteOn3rd() {
 		try {
 			Chord c1 = ChordParser.parseString("C/3");
 			Chord c2 = new Chord(Note.C, Chord.MAJ);
 			Assert.assertEquals(c2, c1);
 			Assert.assertEquals(c2.getRoot(), c1.getRoot());
 			Assert.assertEquals(c2.getShortHand(), c1.getShortHand());
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testDomWithBassNoteOn3rd() {
 		try {
 			Chord c1 = ChordParser.parseString("A:7/3");
 			Chord c2 = new Chord(Note.A, Chord.DOM);
 			Assert.assertEquals(c2, c1);
 			Assert.assertEquals(c2.getRoot(), c1.getRoot());
 			Assert.assertEquals(c2.getShortHand(), c1.getShortHand());
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testMaj6WithBassNoteOn5th() {
 		try {
 			Chord c1 = ChordParser.parseString("G:maj6/5");
//			Chord c2 = new Chord(Note.G, Chord.MAJ6);
			Chord c2 = new Chord(Note.E, Chord.MIN7);
 			Assert.assertEquals(c2, c1);
 			Assert.assertEquals(c2.getRoot(), c1.getRoot());
 			Assert.assertEquals(c2.getShortHand(), c1.getShortHand());
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testSus4WithAdditionalb7() {
 		try {
 			Chord c1 = ChordParser.parseString("G:sus4(b7)");
 			Chord c2 = new Chord(Note.G, Note.C, Note.D, Note.F);
 			Assert.assertEquals(c2, c1);
 			Assert.assertEquals(c2.getRoot(), c1.getRoot());
 			Assert.assertEquals(c2.getShortHand(), c1.getShortHand());
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 }
