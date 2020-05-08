 package org.w3c.tidy;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 
 import junit.framework.TestCase;
 
 public class TidyTest extends TestCase {
 
 	private final String t;
 	private final int r;
 	private final String base;
 	
     public TidyTest(final String base, final String t, final int r) {
         super(t);
         this.t = t;
         this.r = r;
         this.base = base;
     }
     
     private static void write(final ByteArrayOutputStream os, final String path) throws IOException {
     	final OutputStream o = new FileOutputStream(path);
     	o.write(os.toByteArray());
     	o.flush();
     	o.close();
     }
     
     public void runTest() {
     	final Tidy tidy = new Tidy();
     	final String cfg = base + "input/cfg_" + t + ".txt";
     	tidy.setConfigurationFromFile(new File(cfg).exists() ? cfg : base + "input/cfg_default.txt" );
     	tidy.setTidyMark(false);
     	InputStream is = null;
     	try {
 			is = new FileInputStream(base + "input/in_" + t + ".html");
 		} catch (FileNotFoundException e) {
 			try {
 				is = new FileInputStream(base + "input/in_" + t + ".xml");
 			} catch (FileNotFoundException e1) {
 				try {
					is = new FileInputStream(base + "input/in_" + t + ".xhml");
 				} catch (FileNotFoundException e2) {
 					throw new RuntimeException("can't find input file");
 				}
 			}
 		}
     	final ByteArrayOutputStream os = new ByteArrayOutputStream();
     	final ByteArrayOutputStream es = new ByteArrayOutputStream();
     	tidy.setErrout(new PrintWriter(es));
     	tidy.parse(is, os);
     	int x = tidy.getParseErrors() > 0 ? 2 : tidy.getParseWarnings() > 0 ? 1 : 0;
     	try {
     		write(os, base + "tmp/out_" + t + ".html");
     		write(es, base + "tmp/msg_" + t + ".txt");
     		InputStream os2 = null;
     		try {
     			os2 = new FileInputStream(base + "output/out_" + t + ".html");
     		} catch (IOException e) {
     			os2 = new ByteArrayInputStream(new byte[0]);
     		}
 			diff("outputs", new ByteArrayInputStream(os.toByteArray()), os2);
 			diff("messages", new ByteArrayInputStream(es.toByteArray()), new FileInputStream(base + "output/msg_" + t + ".txt"));
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		assertEquals(r, x);
     }
 
     private static void diff(final String what, final InputStream s1, final InputStream s2) throws IOException {
     	for (int x = 1; ; ++x) {
     		int b1 = s1.read();
     		int b2 = s2.read();
     		if (b1 != b2) {
     			if (b1 == -1 && b2 == 10) {
     				final byte[] b = new byte[20];
     				s2.read(b);
     				final String s = new String(b);
     				if (s.equals("To learn more about ")) {
     					break;
     				}
     			}
     			fail(what + " differ at byte " + x);
     		}
     		if (b1 == -1) {
     			break;
     		}
     	}
     	s1.close();
     	s2.close();
     }
 }
