 package at.stefl.svm.test;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import at.stefl.svm.tosvg.SVGTranslator;
 
 public class SVGTranslatorTest {
 
     public static void main(String[] args) throws Throwable {
 	File file = File.createTempFile("svm2svg", ".svg");
 
 	InputStream in = SVMListingTest.class.getResourceAsStream("test.svm");
 	OutputStream out = new FileOutputStream(file);
 
	SVGTranslator.TRANSLATOR.translate(in, out);
 
 	Runtime.getRuntime().exec("google-chrome " + file.getAbsolutePath());
     }
 
 }
