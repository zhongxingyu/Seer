 package org.eclipse.dltk.ruby.tests.parser.jruby;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.core.tests.model.AbstractModelTests;
 import org.eclipse.dltk.ruby.internal.parser.JRubySourceParser;
 import org.eclipse.dltk.ruby.tests.Activator;
 
 public class ZippedParserSuite extends TestSuite {
 
 
 	public ZippedParserSuite(String testsZip) {
 		super(testsZip);
 		ZipFile zipFile;
 		try {
 			zipFile = new ZipFile(AbstractModelTests.storeToMetadata(Activator
 					.getDefault().getBundle(), "parser.zip", testsZip));
 			Enumeration entries = zipFile.entries();
 			while (entries.hasMoreElements()) {
 				ZipEntry entry = (ZipEntry) entries.nextElement();
 				final String content = loadContent(zipFile
 						.getInputStream(entry));
 
 				addTest(new TestCase(entry.getName()) {
 
 					public void setUp() {
 
 					}
 
 					protected void runTest() throws Throwable {
 						JRubySourceParser parser = new JRubySourceParser(null);
						JRubySourceParser.setSilentState(false);
 						ModuleDeclaration module = parser.parse(content);
 						assertNotNull(module);
 					}
 
 				});
 			}
 		} catch (ZipException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static String loadContent(InputStream stream) throws IOException {
 		int length = stream.available();
 		byte[] data = new byte[length];
 		stream.read(data);
 		stream.close();
 		return new String(data, "utf-8");
 	}
 
 }
