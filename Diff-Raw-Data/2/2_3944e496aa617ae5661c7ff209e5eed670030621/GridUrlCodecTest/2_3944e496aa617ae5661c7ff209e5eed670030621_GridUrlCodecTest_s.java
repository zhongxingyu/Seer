 package de.banapple.confluence;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.apache.log4j.BasicConfigurator;
 
 public class GridUrlCodecTest 
 	extends TestCase
 {
 	private GridUrlCodec codec;
 	
 	public void setUp()
 	{
 		BasicConfigurator.configure();
 		
 		codec = new DefaultGridUrlCodec();
 	}
 	
 	public void testCompressDecompress()
 	{
 		String text = 
 			"+----------+\n"+
 			"|Hallo Welt|\n"+
 			"+----------+\n";
 		
 		System.out.println(text);
 		String compressedText = codec.encode(text);
 		System.out.println(compressedText);
 		String decoded = codec.decode(compressedText);
 		System.out.println(decoded);
		Assert.assertEquals(text, decoded);
 	}
 }
