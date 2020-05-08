 package tagEditor;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.file.Paths;
 
 import org.junit.Test;
 
 public class CoreTests {
 
 	@Test
 	public void TestDirectory (){
 		
 		Directory dir = new Directory("MPGI3");
 		assertEquals("MPGI3", dir.toString()); 
 	}
 	
 	@Test
 	public void TestMp3Parser (){
 	
 		MP3Parser parser =new MP3Parser();
 		MP3File myMp3;
 		
 		try {
			myMp3 = parser.readMP3(Paths.get("01_What_i_hope.mp3"));
 		
 			assertEquals("House Plants", myMp3.getAlbum());
 			assertEquals("2010",myMp3.getYear());
 			assertEquals("Pinkle",myMp3.getArtist());
 			assertEquals("What I Hope",myMp3.getSong());
			assertEquals(63815,myMp3.getCover().length);
 			
 		} 
 		catch (IOException e) {	
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void TestMp3Saver (){
 	
 		MP3Parser parser =new MP3Parser();
 		MP3Saver saver = new MP3Saver();
 		
 		MP3File myMp3;
 		
 		try {
			myMp3 = parser.readMP3(Paths.get("01_What_i_hope.mp3"));
 			
 			myMp3.setAlbum("MPGI3");
 			myMp3.setYear("0815");
 			myMp3.setArtist("Tom");
 			myMp3.setSong("HA2");
 			
			String myPath = myMp3.getPath();
			myPath = "test_" + myPath;
 			myMp3.setPath(myPath);
 			
 			saver.saveMP3(myMp3);
 			myMp3 = parser.readMP3(Paths.get(myPath));
 			
 			assertEquals("MPGI3", myMp3.getAlbum());
 			assertEquals("0815",myMp3.getYear());
 			assertEquals("Tom",myMp3.getArtist());
 			assertEquals("HA2",myMp3.getSong());
			assertEquals(63815,myMp3.getCover().length);
 			
 		} 
 		catch (IOException e) {	
 			e.printStackTrace();
 		}
 	}
 	
 }
