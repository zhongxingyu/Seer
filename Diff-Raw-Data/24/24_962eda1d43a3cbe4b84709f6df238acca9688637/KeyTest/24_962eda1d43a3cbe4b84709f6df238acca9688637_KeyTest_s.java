 package chordest.chord;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import chordest.model.Chord;
 import chordest.model.Key;
 import chordest.model.Note;
 import chordest.model.Scale.NaturalMajor;
 import chordest.model.Scale.NaturalMinor;
 
 
 public class KeyTest {
 
 	@Test
 	public void testCMajNoOffset() {
 		double[] intensities = new double[] { 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0 };
 		Key mode = Key.recognizeKey(intensities, Note.A);
 		Assert.assertNotNull(mode);
 		Assert.assertEquals(Note.C, mode.getRoot());
		Assert.assertEquals(Chord.MAJ, mode.getType());
 	}
 
 //	@Test
 //	public void testCMajOffset1() {
 //		List<Note> notes = new ArrayList<Note>(7);
 //		notes.add(Note.D);
 //		notes.add(Note.E);
 //		notes.add(Note.F);
 //		notes.add(Note.G);
 //		notes.add(Note.A);
 //		notes.add(Note.B);
 //		notes.add(Note.C);
 //		Mode mode = Mode.recognizeMode(notes);
 //		Assert.assertNotNull(mode);
 //		Assert.assertEquals(Note.C, mode.getRoot());
 //		Assert.assertEquals(Chord.MAJ, mode.getType());
 //	}
 //
 //	@Test
 //	public void testCMajOutOfOrder() {
 //		List<Note> notes = new ArrayList<Note>(7);
 //		notes.add(Note.E);
 //		notes.add(Note.B);
 //		notes.add(Note.F);
 //		notes.add(Note.D);
 //		notes.add(Note.A);
 //		notes.add(Note.C);
 //		notes.add(Note.G);
 //		Mode mode = Mode.recognizeMode(notes);
 //		Assert.assertNotNull(mode);
 //		Assert.assertEquals(Note.C, mode.getRoot());
 //		Assert.assertEquals(Chord.MAJ, mode.getType());
 //	}
 
 	@Test
 	public void testFD() {
 		double[] intensities = new double[] { 0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1 };
 		List<Note> notes = new ArrayList<Note>(7);
 		notes.add(Note.GD);
 		notes.add(Note.FD);
 		notes.add(Note.F);
 		notes.add(Note.DD);
 		notes.add(Note.CD);
 		notes.add(Note.B);
 		notes.add(Note.AD);
 		Key mode = Key.recognizeKey(intensities, Note.A);
 		
 		Assert.assertNotNull(mode);
 		Assert.assertEquals(Note.FD, mode.getRoot());
		Assert.assertEquals(Chord.MAJ, mode.getType());
 		
 		Assert.assertTrue(mode.getNotes().contains(Note.GD));
 		Assert.assertTrue(mode.getNotes().contains(Note.FD));
 		Assert.assertTrue(mode.getNotes().contains(Note.F));
 		Assert.assertTrue(mode.getNotes().contains(Note.DD));
 		Assert.assertTrue(mode.getNotes().contains(Note.CD));
 		Assert.assertTrue(mode.getNotes().contains(Note.B));
 		Assert.assertTrue(mode.getNotes().contains(Note.AD));
 		
 		Assert.assertTrue(mode.getChords().contains(Chord.major(Note.FD)));
 		Assert.assertTrue(mode.getChords().contains(Chord.major(Note.CD)));
 		Assert.assertTrue(mode.getChords().contains(Chord.major(Note.B)));
 		Assert.assertTrue(mode.getChords().contains(Chord.minor(Note.DD)));
 		Assert.assertTrue(mode.getChords().contains(Chord.minor(Note.GD)));
 		Assert.assertTrue(mode.getChords().contains(Chord.minor(Note.AD)));
 	}
 
 	@Test
 	public void testMajorMinor() {
 		Key modeAMaj = new Key(Note.A, new NaturalMajor());
 		List<Note> notesAMaj = modeAMaj.getNotes();
 		Assert.assertEquals(7, notesAMaj.size());
 		Assert.assertTrue(notesAMaj.contains(Note.A));
 		Assert.assertTrue(notesAMaj.contains(Note.B));
 		Assert.assertTrue(notesAMaj.contains(Note.CD));
 		Assert.assertTrue(notesAMaj.contains(Note.D));
 		Assert.assertTrue(notesAMaj.contains(Note.E));
 		Assert.assertTrue(notesAMaj.contains(Note.FD));
 		Assert.assertTrue(notesAMaj.contains(Note.GD));
 		
 		Key modeAMin = new Key(Note.A, new NaturalMinor());
 		List<Note> notesAMin = modeAMin.getNotes();
 		Assert.assertEquals(7, notesAMin.size());
 		Assert.assertTrue(notesAMin.contains(Note.A));
 		Assert.assertTrue(notesAMin.contains(Note.B));
 		Assert.assertTrue(notesAMin.contains(Note.C));
 		Assert.assertTrue(notesAMin.contains(Note.D));
 		Assert.assertTrue(notesAMin.contains(Note.E));
 		Assert.assertTrue(notesAMin.contains(Note.F));
 		Assert.assertTrue(notesAMin.contains(Note.G));
 	}
 
 }
