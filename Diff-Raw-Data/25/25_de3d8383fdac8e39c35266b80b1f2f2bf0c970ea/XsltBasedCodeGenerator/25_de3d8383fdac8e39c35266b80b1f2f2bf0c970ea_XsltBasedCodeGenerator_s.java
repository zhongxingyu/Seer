 /**
  * 
  */
 package handler.impl;
 
 import java.io.BufferedWriter;
 import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 
import javax.xml.transform.Result;
import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 /**
  * 
  * 
  * @author Denys Solyanyk <peacemaker@ukr.net>
  * @copyright 2010-2011 Denys Solyanyk <peacemaker@ukr.net>
  * @since 24 июня 2011
  */
 public abstract class XsltBasedCodeGenerator extends CodeGenerator {
 
     /*
      * (non-Javadoc)
      * 
      * @see handler.impl.FileHandler#processFile(java.io.File)
      */
     @Override
     protected boolean processFile(final File file) {
         logger.info("Start processFile for file : {}", file.getAbsolutePath());
         // final File output = createOutputFile(file);
        // logger.info(String.format("Output file : %1$s", output.getAbsolutePath()));
         StringWriter writer = new StringWriter();
         final TransformerFactory transformerFactory = TransformerFactory.newInstance();
         try {
             final Transformer transformer = transformerFactory.newTransformer(new StreamSource(template));
             transformer.transform(new StreamSource(file), new StreamResult(writer));
 
             String result = writer.toString();
 
            String outputFileName = getTemplate().getAbsolutePath() + File.pathSeparator + buildResultFilename(result);
 
             BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName));// IOException
             out.write(result);// IOException
             out.close();// IOException
 
             return true;
             // // transformer.transform(new StreamSource("howto.xml"), new
             // // StreamResult(new FileOutputStream("howto.html")));
         } catch (TransformerConfigurationException e) {
             // e.printStackTrace();
         } catch (TransformerException e) {
             // e.printStackTrace();
         } catch (IOException e) {
             // e.printStackTrace();
         }
 
         return false;
 
         // try {
         // // Create file
         // // FileWriter fstream = new FileWriter("out.txt");
        // BufferedWriter out = new BufferedWriter(new FileWriter("out.txt")); // fstream
         // out.write("Hello Java");
         // // Close the output stream
         // out.close();
         // } catch (Exception e) {// Catch exception if any
         // System.err.println("Error: " + e.getMessage());
         // }
 
         // try {
         // StringReader reader = new StringReader("<xml>blabla</xml>");
         // StringWriter writer = new StringWriter();
         // TransformerFactory tFactory = TransformerFactory.newInstance();
         // Transformer transformer = tFactory
        // .newTransformer(new javax.xml.transform.stream.StreamSource("styler.xsl"));
         //
        // transformer.transform(new javax.xml.transform.stream.StreamSource(reader),
         // new javax.xml.transform.stream.StreamResult(writer));
         //
         // String result = writer.toString();
         // } catch (Exception e) {
         // e.printStackTrace();
         // }
 
         // try {
         // // Here we'll write our data into a file called
         // // sample.txt, this is the output.
         // File file2 = new File("sample.txt");
         // // We'll write the string below into the file
         // String data = "Learning Java Programming";
         //
         // // To write a file called the writeStringToFile
         // // method which require you to pass the file and
         // // the data to be written.
         // FileUtils.writeStringToFile(file2, data);
         // } catch (IOException e) {
         // e.printStackTrace();
         // }
 
         // System.out.println("InterfaceRequestCodeGenerator.execute()");
         // System.out.println(file.getAbsolutePath());
 
     }
 
     protected abstract String buildResultFilename(String result);
 
     // protected Source createSource(File file) {
     // return new StreamSource(file);
     // }
 
     // protected Result createResult(File file) throws FileNotFoundException {
     // return new StreamResult(new FileOutputStream(file));
     // }
 
 }
