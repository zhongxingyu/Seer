 package com.mebigfatguy.multifile;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class MultiFileTest {
 
 	private static final String TEST_FILE_NAME = "__test__.mff";
 	@Before
 	public void setUp() {
 		File f = new File(TEST_FILE_NAME);
 		f.delete();
 	}
 	
 	@Test
 	public void testCreate() throws IOException {
 		MultiFile f = new MultiFile(TEST_FILE_NAME);
 		Assert.assertEquals(0, f.getStreamNames().size());
 		f.close();	
 		
 		File rawFile = new File(TEST_FILE_NAME);
 		Assert.assertEquals(512, rawFile.length());
 		
 		f = new MultiFile(TEST_FILE_NAME);
 		Assert.assertEquals(0, f.getStreamNames().size());
 		f.close();
 	}
 	
 	@Test
 	public void testCreateStream() throws IOException {
 		MultiFile f = new MultiFile(TEST_FILE_NAME);
 		Assert.assertEquals(0, f.getStreamNames().size());
 		
 		OutputStream os = f.getWriteStream("stream1");
 		DataOutputStream dos = new DataOutputStream(os);
 		for (int i = 0; i < 100; i++) {
 			dos.writeUTF("The quick brown fox jumps over the lazy dog");
 		}
 		dos.close();
 		
 		InputStream is = f.getReadStream("stream1");
 		DataInputStream dis = new DataInputStream(is);
		for (int i = 0; i < 100; i++) {
			Assert.assertEquals("The quick brown fox jumps over the lazy dog", dis.readUTF());
		}
 		dis.close();
 		f.close();
 	}
 }
