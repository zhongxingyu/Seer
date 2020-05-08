 package de.netprojectev.JUnitTests;
 
 import static org.junit.Assert.*;
 
 import java.util.LinkedList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.netprojectev.GUI.ManagerFrame;
 import de.netprojectev.Media.MediaFile;
 import de.netprojectev.Media.VideoFile;
 import de.netprojectev.MediaHandler.DisplayHandler;
 import de.netprojectev.MediaHandler.MediaHandler;
 import de.netprojectev.Misc.Constants;
 
 public class DisplayHandlerTest {
 
 	
 	DisplayHandler displayHandler;
 	MediaFile[] testFiles;
 	MediaFile[] testFiles2;
 	MediaFile[] testFiles3;
 	MediaFile[] testFiles4;
 	MediaFile[] testFiles5;
 	MediaFile testFile0;
 	MediaFile testFile1;
 	MediaFile testFile2;
 	MediaFile testFile3;
 	MediaFile testFile4;
 	MediaFile testFile5;
 	
 	@Before
 	public void setUp() {
 		
 		DisplayHandler.reset();
 		displayHandler = DisplayHandler.getInstance();
		Constants.UNIT_TESTING = true;
 		testFiles = new MediaFile[3];
 		testFiles2 = new MediaFile[1];
 		testFiles3 = new MediaFile[2];
 		testFiles4 = new MediaFile[3];
 		testFiles5 = new MediaFile[2];
 
 		testFile0 = new VideoFile("0", "0");
 		testFile1 = new VideoFile("1", "1");
 		testFile2 = new VideoFile("2", "2");
 		testFile3 = new VideoFile("3", "3");
 		testFile4 = new VideoFile("4", "4");
 		testFile5 = new VideoFile("5", "5");
 
 		testFiles[0] = this.testFile0;
 		testFiles[1] = this.testFile1;
 		testFiles[2] = this.testFile2;
 
 		testFiles2[0] = this.testFile3;
 
 		testFiles3[0] = this.testFile0;
 		testFiles3[1] = this.testFile2;
 		
 		testFiles4[0] = this.testFile3;
 		testFiles4[1] = this.testFile4;
 		testFiles4[2] = this.testFile5;
 
 		testFiles5[0] = this.testFile2;
 		testFiles5[1] = this.testFile4;
 		
 		
 		displayHandler.add(testFiles);
 		
 		
 	}
 	
 
 	@Test
 	public void testStartShuffle() {
 		
 		displayHandler.add(testFiles4);
 		
 		MediaFile[] tmp =  new MediaFile[6];
 		tmp = displayHandler.getPlayingFiles().toArray(new MediaFile[0]);
 		
 		displayHandler.startShuffle();
 		assertEquals(true, displayHandler.getIsShufflingEnabled());
 		
 		assertEquals(6, displayHandler.getPlayingFiles().size());
 		/*
 		 * !!!!!
 		 * !!!!!
 		 * !!!!!
 		 * Schlägt vielleicht bei einem von tausend mal fehl, da der shuffle das original liefert
 		 */
 		assertFalse(tmp.equals( displayHandler.getPlayingFiles().toArray(new MediaFile[0])));
 		
 	}
 	
 
 	@Test
 	public void testStopShuffle() {
 		
 		displayHandler.add(testFiles4);
 		MediaFile[] tmp =  new MediaFile[6];
 		tmp = displayHandler.getPlayingFiles().toArray(new MediaFile[0]);
 		MediaHandler.getInstance().add(tmp);
 		displayHandler.startShuffle();
 		assertEquals(true, displayHandler.getIsShufflingEnabled());
 		displayHandler.stopShuffle();
 		assertEquals(false, displayHandler.getIsShufflingEnabled());
 		assertEquals(6, displayHandler.getPlayingFiles().size());
 		assertArrayEquals(tmp, displayHandler.getPlayingFiles().toArray(new MediaFile[0]));
 
 	}	
 	
 	
 	@Test
 	public void testShow() {
 		displayHandler.show(testFile0);
 		assertEquals(testFile0, displayHandler.getCurrentMediaFile());
 		displayHandler.show(testFile2);
 		assertEquals(testFile2, displayHandler.getCurrentMediaFile());
 
 	}
 
 	@Test
 	public void testShowNext() {
 		displayHandler.show(testFile1);
 		assertEquals(testFile1, displayHandler.getCurrentMediaFile());
 		displayHandler.showNext();
 		assertEquals(testFile2, displayHandler.getCurrentMediaFile());
 
 		
 	}
 	
 	@Test
 	public void testShowNext3() {
 		displayHandler.show(testFile2);
 		assertEquals(testFile2, displayHandler.getCurrentMediaFile());
 		displayHandler.showNext();
 		assertEquals(testFile0, displayHandler.getCurrentMediaFile());
 		
 	}
 
 	@Test
 	public void testShowBefore() {
 		displayHandler.show(testFile2);
 		assertEquals(testFile2, displayHandler.getCurrentMediaFile());
 		displayHandler.showPrevious();
 		assertEquals(testFile1, displayHandler.getCurrentMediaFile());
 	}
 	
 
 	
 	@Test
 	public void testShowBefore3() {
 		displayHandler.show(testFile0);
 		assertEquals(testFile0, displayHandler.getCurrentMediaFile());
 		displayHandler.showPrevious();
 		assertEquals(testFile2, displayHandler.getCurrentMediaFile());
 	}
 	
 	@Test
 	public void testAdd() {
 		
 		
 		displayHandler.add(testFiles);
 		assertEquals(testFiles.length, displayHandler.getPlayingFiles().size());
 	
 	}
 	
 	@Test
 	public void testAdd2() {
 		
 		
 		displayHandler.add(testFiles2);
 		assertEquals(testFile3, displayHandler.getPlayingFiles().get(3));
 	
 	}
 	
 	@Test
 	public void testRemove() {
 
 		displayHandler.remove(testFiles3);
 
 		assertEquals(1, displayHandler.getPlayingFiles().size());
 		assertEquals(testFile1, displayHandler.getPlayingFiles().getLast());
 
 	}
 
 	@Test
 	public void testRemove2() {
 	
 		displayHandler.remove(testFiles3);
 		
 		assertEquals(testFile1, displayHandler.getPlayingFiles().getFirst());
 
 	}
 	
 	@Test
 	public void testUp() {
 	
 		displayHandler.add(testFiles2);
 		displayHandler.up(testFiles2);
 		
 		assertEquals(testFile3, displayHandler.getPlayingFiles().get(2));
 		assertEquals(4, displayHandler.getPlayingFiles().size());
 		
 	}
 	
 	@Test
 	public void testUp2() {
 		
 		displayHandler.add(testFiles4);
 		displayHandler.up(testFiles5);
 		
 		assertEquals(testFile2, displayHandler.getPlayingFiles().get(1));
 		assertEquals(testFile4, displayHandler.getPlayingFiles().get(2));
 		assertEquals(6, displayHandler.getPlayingFiles().size());
 		
 	}
 	
 	@Test
 	public void testUp3() {
 		
 		displayHandler.setIsShufflingEnabled(true);
 		
 		displayHandler.add(testFiles4);
 		
 		LinkedList<MediaFile> tmp = displayHandler.getPlayingFiles();
 		
 		displayHandler.up(testFiles5);
 		
 		assertEquals(tmp, displayHandler.getPlayingFiles());
 		
 	}
 
 	@Test
 	public void testDown() {
 		
 		displayHandler.add(testFiles4);
 		displayHandler.down(testFiles5);
 		
 		assertEquals(testFile2, displayHandler.getPlayingFiles().get(3));
 		assertEquals(testFile4, displayHandler.getPlayingFiles().get(4));
 		assertEquals(6, displayHandler.getPlayingFiles().size());
 		
 	}
 	
 
 	@Test
 	public void testDown2() {
 	
 		displayHandler.add(testFiles4);
 		displayHandler.down(testFiles2);
 		
 		assertEquals(testFile3, displayHandler.getPlayingFiles().get(4));
 		assertEquals(6, displayHandler.getPlayingFiles().size());
 		
 	}
 	
 	@Test
 	public void testDown3() {
 		
 		displayHandler.setIsShufflingEnabled(true);
 		
 		displayHandler.add(testFiles4);
 		
 		LinkedList<MediaFile> tmp = displayHandler.getPlayingFiles();
 		
 		displayHandler.down(testFiles5);
 		
 		assertEquals(tmp, displayHandler.getPlayingFiles());
 		
 	}
 
 }
