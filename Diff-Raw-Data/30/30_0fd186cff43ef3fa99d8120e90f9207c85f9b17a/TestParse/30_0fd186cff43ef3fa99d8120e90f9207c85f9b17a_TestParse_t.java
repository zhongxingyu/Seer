 package com.winvector;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.zip.ZipOutputStream;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.junit.Test;
 
 import com.winvector.ExampleClipper.Clip;
 import com.winvector.ExampleClipper.ClipConsumer;
 import com.winvector.ExampleClipper.ClipZipper;
 
 
 
 public class TestParse {
 	@Test
 	public void testCz() throws Exception {
 		final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
 		final SAXParser saxParser = saxFactory.newSAXParser();
 		final File f = File.createTempFile("testZip",".zip");
 		//System.out.println("writing: " + f.getAbsolutePath());
 		final ZipOutputStream o = new ZipOutputStream(new FileOutputStream(f));
		final Date now = new Date();
		final ExampleClipper sectCounter = new ExampleClipper(new ErrorCollector(),new ClipZipper(o,"bd","TestText",".txt", now.getTime()), now);
 		final InputStream source = this.getClass().getClassLoader().getResourceAsStream("com/winvector/ExampleElt.xml");
 		saxParser.parse(source,sectCounter);
 		o.close();
 		f.delete();
 	}
 	
 	@Test
 	public void testCs() throws Exception {
 		final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
 		final SAXParser saxParser = saxFactory.newSAXParser();
 		final ClipConsumer clipDest = new ClipConsumer() {
 			@Override
 			public void takeClip(final Clip clip) throws IOException {
 				System.out.println(clip);
 			}
 		};
 		final ExampleClipper sectCounter = new ExampleClipper(new ErrorCollector(),clipDest, new Date());
 		final InputStream source = this.getClass().getClassLoader().getResourceAsStream("com/winvector/ExampleElt.xml");
 		saxParser.parse(source,sectCounter);
 	}
 }
