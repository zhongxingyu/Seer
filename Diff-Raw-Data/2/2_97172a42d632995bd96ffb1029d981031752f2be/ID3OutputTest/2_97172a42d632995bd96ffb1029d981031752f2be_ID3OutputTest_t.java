 package tests;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import org.junit.Test;
 
 import awesome.ID3Output;
 
 public class ID3OutputTest {
 
 	@Test
 	public void testWriteAsSynchSafe() throws IOException {
 		File tmp = File.createTempFile("id3outputtest", "bin");
 		ID3Output out = new ID3Output(tmp, false);
 		out.writeAsSynchSafe(1234567);
 		byte[] data = Files.readAllBytes(tmp.toPath());
 		assertArrayEquals(data, new byte[]{0,75,45,7});
 		out.close();
 		tmp.delete();
 	}
 
 	@Test
 	public void testWriteTextFrame() throws IOException {
 		File tmp = File.createTempFile("id3outputtest", "bin");
 		ID3Output out = new ID3Output(tmp, false);
 		out.writeTextFrame("TPE1", "John Lennon".getBytes("ISO-8859-1"));
 		byte[] data = Files.readAllBytes(tmp.toPath());
		assertArrayEquals(data, new byte[]{84,80,69,49,0,0,0,12,0,0,3,74,111,104,110,32,76,101,110,110,111,110});
 		out.close();
 		tmp.delete();
 	}
 
 	@Test
 	public void testWritePadding() throws IOException {
 		File tmp = File.createTempFile("id3outputtest", "bin");
 		ID3Output out = new ID3Output(tmp, false);
 		out.writePadding(7);
 		byte[] data = Files.readAllBytes(tmp.toPath());
 		assertArrayEquals(data, new byte[]{0,0,0,0,0,0,0});
 		out.close();
 		tmp.delete();
 	}
 
 }
