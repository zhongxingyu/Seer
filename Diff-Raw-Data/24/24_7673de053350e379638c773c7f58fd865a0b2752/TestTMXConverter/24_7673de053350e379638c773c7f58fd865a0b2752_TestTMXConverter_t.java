 package domderrien.build;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.SAXException;
 
 public class TestTMXConverter {
 
     class MockOutputStream extends OutputStream {
         StringBuffer stream = new StringBuffer();
         public String getStream() {
             return stream.toString();
         }
         public int length() {
             return stream.length();
         }
         @Override
         public void write(int b) throws IOException {
             stream.append((char) b);
         }
     }
 
     @Before
     public void setUp() throws Exception {
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public void testMain() {
         // Cannot be unit tested because it will
         // make the application calling "System.exit(1)"
         // and this will break the automatic unit tests processing
     }
 
     @Test
     public void testStopProcess() {
         // Just verify the correct call
         // Note that the entire method paths can't be converted
         // because of the call to "System.exit(1)"
         TMXConverter converter = new TMXConverter(false);
         converter.stopProcess();
         assertTrue(converter.getProcessStopped());
     }
 
     @Test
     public void testErrorReport() {
         TMXConverter converter = new TMXConverter(true);
         converter.reportError("bla-bla");
         assertTrue(converter.isErrorReported());
         assertFalse(converter.getProcessStopped());
     }
 
     @Test
     public void testDisplayUsage() {
         // Just verify that no exception is thrown
         TMXConverter converter = new TMXConverter(true) {
             protected void stopProcess() {
                 // Nothing
             }
         };
         converter.displayUsage();
     }
 
     @Test(expected = NullPointerException.class)
     public void testSetContextI() {
         // Exception expected because the <code>null</code> value is not checked
         TMXConverter converter = new TMXConverter(false);
         converter.setContext(null);
         fail("NullPointerException should have been thrown");
     }
 
     @Test
     public void testSetContextII() {
         // Error because the number of arguments is even
         TMXConverter converter = new TMXConverter(false);
         converter.setContext(new String[] {"nop"});
         assertTrue(converter.getProcessStopped());
     }
 
     @Test
     public void testSetContextIII() {
         // Error because the number of arguments is odd but under 10
         TMXConverter converter = new TMXConverter(false);
         converter.setContext(new String[] {"nop", "nop"});
         assertTrue(converter.getProcessStopped());
     }
 
     @Test
     public void testSetContextIV() {
         // Error because one of the expected arguments is missing
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-nop", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-nop", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-nop", "nop",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-nop", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "-nop", "nop",
                     "-buildStamp", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-nop", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
     }
 
     @Test
     public void testSetContextV() {
         // Error because one of the value of the expected arguments is emtpy
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "",
                     "-buildStamp", "nop"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", ""
             });
             assertTrue(converter.getProcessStopped());
         }
     }
 
     @Test
     public void testSetContextVI() {
         // Error because one of the value of the expected arguments is missing
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop",
                     "-tmxFilenameBase"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop",
                     "-sourcePath"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "",
                     "-javaDestPath", "nop",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop",
                     "-jsDestPath"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "",
                     "-languageFilenameBase", "nop",
                     "-buildStamp", "nop",
                     "-javaDestPath"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "",
                     "-buildStamp", "nop",
                     "-languageFilenameBase"
             });
             assertTrue(converter.getProcessStopped());
         }
         {
             TMXConverter converter = new TMXConverter(false);
             converter.setContext(new String[] {
                     "-tmxFilenameBase", "nop",
                     "-sourcePath", "nop",
                     "-jsDestPath", "nop",
                     "-javaDestPath", "nop",
                     "languageFilenameBase", "nop",
                     "nop",
                     "-buildStamp"
             });
             assertTrue(converter.getProcessStopped());
         }
     }
 
     @Test
     public void testSetContextVII() {
         // Everything is fine, so no error expected
         TMXConverter converter = new TMXConverter(false);
         converter.setContext(new String[] {
                 "-tmxFilenameBase", "nop",
                 "-sourcePath", "nop",
                 "-jsDestPath", "nop",
                 "-javaDestPath", "nop",
                 "-languageFilenameBase", "nop",
                 "-buildStamp", "nop"
         });
         assertFalse(converter.getProcessStopped());
     }
 
     @Test(expected = MissingResourceException.class)
     public void testGetDocumentedLanguagesI()
     {
         TMXConverter converter = new TMXConverter(false);
         converter.setLanguageFilenameBase("nop");
         converter.getDocumentedLanguages();
         fail("Exception expected");
     }
 
     @Test
     public void testGetDocumentedLanguagesII()
     {
         TMXConverter converter = new TMXConverter(false);
         converter.setLanguageFilenameBase("domderrien-languages");
         ResourceBundle rb = converter.getDocumentedLanguages();
         assertEquals(rb, converter.getDocumentedLanguages());
     }
 
     @Test
     public void testProcessContextI()
     {
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected void getSourceFileDates(String arg0, String arg1, Map<String, Long> arg2) {}
             @Override
             protected void getJSFileDates(Map<String, Long> arg0, String arg1, Map<String, Long> arg2) {}
             @Override
             protected void getJavaFileDates(Map<String, Long> arg0, String arg1, Map<String, Long> arg2) {}
             @Override
             protected ResourceBundle getDocumentedLanguages() { return null; }
         };
 
         converter.processContext();
         assertFalse(converter.isErrorReported());
         assertFalse(converter.getProcessStopped());
     }
 
     @Test
     public void testProcessContextII()
     {
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected void getSourceFileDates(String arg0, String arg1, Map<String, Long> arg2) {}
             @Override
             protected void getJSFileDates(Map<String, Long> arg0, String arg1, Map<String, Long> arg2) {}
             @Override
             protected void getJavaFileDates(Map<String, Long> arg0, String arg1, Map<String, Long> arg2) {}
             @Override
             protected ResourceBundle getDocumentedLanguages() {
                 throw new MissingResourceException("Done in purpose", "nop", "nop");
             }
         };
 
         converter.processContext();
         assertTrue(converter.isErrorReported());
         assertFalse(converter.getProcessStopped());
     }
 
     @Test
     public void testGetFile()
     {
         (new TMXConverter(false)).getFile(System.getProperty("user.dir"));
     }
 
     @Test
     @SuppressWarnings("serial")
     public void testGetFileDateI()
     {
         // Check that when the file is not a directory
         File directory = new File("nop") {
             @Override
             public boolean  isDirectory() {
                 return false;
             }
         };
         Map<String, Long> fileDates = new HashMap<String, Long>();
         (new TMXConverter(false)).getFileDates("tmx", directory, fileDates);
         assertEquals(0, fileDates.keySet().size());
     }
 
     @Test
     @SuppressWarnings("serial")
     public void testGetFileDateII()
     {
         // Check that when the directory is empty
         File directory = new File("nop") {
             @Override
             public boolean  isDirectory() {
                 return true;
             }
             @Override
             public File[] listFiles() {
                 return new File[] {};
             }
         };
         Map<String, Long> fileDates = new HashMap<String, Long>();
         (new TMXConverter(false)).getFileDates("tmx", directory, fileDates);
         assertEquals(0, fileDates.keySet().size());
     }
 
     @Test
     @SuppressWarnings("serial")
     public void testGetFileDateIII()
     {
         // Check that when the directory has one file with a bad name
         File directory = new File("nop") {
             @Override
             public boolean  isDirectory() {
                 return true;
             }
             @Override
             public File[] listFiles() {
                 return new File[] {
                     new File("nop") {
                         @Override
                         public boolean  isFile() {
                             return true;
                         }
                         @Override
                         public String getName() {
                             return "nop";
                         }
                     }
                 };
             }
         };
         Map<String, Long> fileDates = new HashMap<String, Long>();
         (new TMXConverter(false)).getFileDates("tmx", directory, fileDates);
         assertEquals(0, fileDates.keySet().size());
     }
 
     @Test
     @SuppressWarnings("serial")
     public void testGetFileDateIV()
     {
         // Check that when the directory has one directory
         File directory = new File("nop") {
             @Override
             public boolean  isDirectory() {
                 return true;
             }
             @Override
             public File[] listFiles() {
                 return new File[] {
                     new File("nop") {
                         @Override
                         public boolean  isFile() {
                             return false;
                         }
                     }
                 };
             }
         };
         Map<String, Long> fileDates = new HashMap<String, Long>();
         (new TMXConverter(false)).getFileDates("tmx", directory, fileDates);
         assertEquals(0, fileDates.keySet().size());
     }
 
     @Test
     @SuppressWarnings("serial")
     public void testGetFileDateV()
     {
         // Check that when the directory has files with just the expected base name
         File directory = new File("nop") {
             @Override
             public boolean  isDirectory() {
                 return true;
             }
             @Override
             public File[] listFiles() {
                 return new File[] {
                     new File("nop") {
                         @Override
                         public boolean  isFile() {
                             return true;
                         }
                         @Override
                         public String getName() {
                             return "tmx.ext";
                         }
                         @Override
                         public long lastModified() {
                             return 111;
                         }
                     },
                     new File("nop") {
                         @Override
                         public boolean  isFile() {
                             return true;
                         }
                         @Override
                         public String getName() {
                             return "tmx_fr";
                         }
                         @Override
                         public long lastModified() {
                             return 222;
                         }
                     },
                     new File("nop") {
                         @Override
                         public boolean  isFile() {
                             return true;
                         }
                         @Override
                         public String getName() {
                             return "tmx_ja.ext";
                         }
                         @Override
                         public long lastModified() {
                             return 333;
                         }
                     }
                 };
             }
         };
         Map<String, Long> fileDates = new HashMap<String, Long>();
 
         (new TMXConverter(false)).getFileDates("tmx", directory, fileDates);
 
         assertEquals(3, fileDates.keySet().size());
         assertTrue(fileDates.containsKey("tmx"));
         assertEquals(Long.valueOf(111), fileDates.get("tmx"));
         assertTrue(fileDates.containsKey("tmx_fr"));
         assertEquals(Long.valueOf(222), fileDates.get("tmx_fr"));
         assertTrue(fileDates.containsKey("tmx_ja"));
         assertEquals(Long.valueOf(333), fileDates.get("tmx_ja"));
     }
 
     @Test
     public void testGetSourceFileDates()
     {
         TMXConverter converter = new TMXConverter(false) {
             @SuppressWarnings("serial")
             @Override
             protected File getFile(String filename)
             {
                 return new File("nop") {
                     @Override
                     public boolean  isDirectory() {
                         return false;
                     }
                 };
             }
         };
         converter.getSourceFileDates("nop", "nop", new HashMap<String, Long>());
     }
 
     @Test
     public void testGetJSFileDates()
     {
         Map<String, Long> sourceFileDates = new HashMap<String, Long>();
         sourceFileDates.put("tmx", Long.valueOf(111));
         sourceFileDates.put("tmx_fr", Long.valueOf(222));
         sourceFileDates.put("tmx_ja", Long.valueOf(333));
 
         TMXConverter converter = new TMXConverter(false) {
             @SuppressWarnings("serial")
             @Override
             protected File getFile(String filename)
             {
                 if (("nop" + File.separator + "tmx.js").equals(filename)) {
                     return new File("nop") {
                         @Override
                         public boolean  isFile() {
                             return true;
                         }
                         @Override
                         public long lastModified() {
                             return 111;
                         }
                     };
                 }
                 if (("nop" + File.separator + "fr" + File.separator + "tmx.js").equals(filename)) {
                     return new File("nop") {
                         @Override
                         public boolean  isFile() {
                             return true;
                         }
                         @Override
                         public long lastModified() {
                             return 222;
                         }
                     };
                 }
                 return new File("nop") {
                     @Override
                     public boolean  isFile() {
                         return false;
                     }
                 };
             }
         };
 
         Map<String, Long> jsFileDates = new HashMap<String, Long>();
 
         converter.getJSFileDates(sourceFileDates, "nop", jsFileDates);
 
         assertEquals(2, jsFileDates.keySet().size());
         assertTrue(jsFileDates.containsKey("tmx"));
         assertEquals(Long.valueOf(111), jsFileDates.get("tmx"));
         assertTrue(jsFileDates.containsKey("tmx_fr"));
         assertEquals(Long.valueOf(222), jsFileDates.get("tmx_fr"));
     }
 
     @Test
     public void testGetJavaFileDates()
     {
         Map<String, Long> sourceFileDates = new HashMap<String, Long>();
         sourceFileDates.put("tmx", Long.valueOf(111));
         sourceFileDates.put("tmx_fr", Long.valueOf(222));
         sourceFileDates.put("tmx_ja", Long.valueOf(333));
 
         TMXConverter converter = new TMXConverter(false) {
             @SuppressWarnings("serial")
             @Override
             protected File getFile(String filename)
             {
                 if (("nop" + File.separator + "tmx.properties-utf8").equals(filename)) {
                     return new File("nop") {
                         @Override
                         public boolean  isFile() {
                             return true;
                         }
                         @Override
                         public long lastModified() {
                             return 111;
                         }
                     };
                 }
                 if (("nop" + File.separator + "tmx_fr.properties-utf8").equals(filename)) {
                     return new File("nop") {
                         @Override
                         public boolean  isFile() {
                             return true;
                         }
                         @Override
                         public long lastModified() {
                             return 222;
                         }
                     };
                 }
                 return new File("nop") {
                     @Override
                     public boolean  isFile() {
                         return false;
                     }
                 };
             }
         };
 
         Map<String, Long> jsFileDates = new HashMap<String, Long>();
 
         converter.getJavaFileDates(sourceFileDates, "nop", jsFileDates);
 
         assertEquals(2, jsFileDates.keySet().size());
         assertTrue(jsFileDates.containsKey("tmx"));
         assertEquals(Long.valueOf(111), jsFileDates.get("tmx"));
         assertTrue(jsFileDates.containsKey("tmx_fr"));
         assertEquals(Long.valueOf(222), jsFileDates.get("tmx_fr"));
     }
 
     @Test
     public void testProcessTMXI()
     {
         // Verify that the sequences will old dates are correctly processed
         Map<String, Long> sourceFileDates = new HashMap<String, Long>();
         sourceFileDates.put("tmx", Long.valueOf(111));
         sourceFileDates.put("tmx_fr", Long.valueOf(222));
         sourceFileDates.put("tmx_ja", Long.valueOf(333));
         sourceFileDates.put("tmx_ru", Long.valueOf(444));
         sourceFileDates.put("tmx_pt_BR", Long.valueOf(555));
 
         Map<String, Long> jsFileDates = new HashMap<String, Long>();
         jsFileDates.put("tmx", Long.valueOf(111));
         jsFileDates.put("tmx_fr", Long.valueOf(000));
         jsFileDates.put("tmx_ja", Long.valueOf(333));
         jsFileDates.put("tmx_ru", Long.valueOf(444));
 
         Map<String, Long> javaFileDates = new HashMap<String, Long>();
         javaFileDates.put("tmx", Long.valueOf(111));
         javaFileDates.put("tmx_fr", Long.valueOf(222));
         javaFileDates.put("tmx_ja", Long.valueOf(000));
         javaFileDates.put("tmx_pt_BR", Long.valueOf(555));
 
         final List<String> names = new ArrayList<String>();
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected void convert(String filename)
             {
                 names.add(filename);
             }
         };
 
         converter.setSourceMap(sourceFileDates);
         converter.setJSMap(jsFileDates);
         converter.setJavaMap(javaFileDates);
         converter.processTMX();
 
         assertTrue(names.contains("tmx_fr"));
         assertTrue(names.contains("tmx_ja"));
         assertTrue(names.contains("tmx_ru"));
         assertTrue(names.contains("tmx_pt_BR"));
     }
 
     @Test
     public void testProcessTMXII()
     {
         // Verify the silent exception handling if a file is not found
         Map<String, Long> sourceFileDates = new HashMap<String, Long>();
         sourceFileDates.put("tmx", Long.valueOf(111));
 
         Map<String, Long> jsFileDates = new HashMap<String, Long>();
         jsFileDates.put("tmx", Long.valueOf(000));
 
         Map<String, Long> javaFileDates = new HashMap<String, Long>();
         javaFileDates.put("tmx", Long.valueOf(000));
 
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected void convert(String filename) throws FileNotFoundException
             {
                 throw new FileNotFoundException("done in purpose");
             }
         };
 
         converter.setSourceMap(sourceFileDates);
         converter.setJSMap(jsFileDates);
         converter.setJavaMap(javaFileDates);
         converter.processTMX();
 
         assertFalse(converter.getProcessStopped());
     }
 
     @Test
     public void testProcessTMXIII()
     {
         // Verify the exception handling if the conversion process fails
         Map<String, Long> sourceFileDates = new HashMap<String, Long>();
         sourceFileDates.put("tmx", Long.valueOf(111));
 
         Map<String, Long> jsFileDates = new HashMap<String, Long>();
         jsFileDates.put("tmx", Long.valueOf(000));
 
         Map<String, Long> javaFileDates = new HashMap<String, Long>();
         javaFileDates.put("tmx", Long.valueOf(000));
 
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected void convert(String filename) throws IOException
             {
                 throw new IOException("done in purpose");
             }
         };
 
         converter.setSourceMap(sourceFileDates);
         converter.setJSMap(jsFileDates);
         converter.setJavaMap(javaFileDates);
         converter.processTMX();
 
         assertTrue(converter.getProcessStopped());
     }
 
     @Test
     public void testProcessTMXIV()
     {
         // Verify that the found languages are correctly processed
         Map<String, String> languages = new HashMap<String, String>();
         languages.put("tmx", "English");
 
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected void saveSupportedLanguages()
             {
                 // Nothing
             }
         };
 
         converter.setLanguageMap(languages);
         converter.processTMX();
 
         assertFalse(converter.getProcessStopped());
     }
 
     @Test
     public void testProcessTMXV()
     {
         // Verify the exception handling if the language list cannot be processed
         Map<String, String> languages = new HashMap<String, String>();
         languages.put("tmx", "English");
 
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected void saveSupportedLanguages() throws IOException
             {
                 throw new IOException("done in purpose");
             }
         };
 
         converter.setLanguageMap(languages);
         converter.processTMX();
 
         assertTrue(converter.getProcessStopped());
     }
 
     @Test(expected = FileNotFoundException.class)
     public void testGetInputStream() throws FileNotFoundException
     {
         TMXConverter converter = new TMXConverter(false);
         // Delete the file to force the exception throwing
         (new File(System.getProperty("user.dir") + File.separator + "nop")).delete();
         converter.getInputStream(System.getProperty("user.dir") + File.separator + "nop");
         fail("FileNotFoundException expected");
     }
 
     @Test
     public void testGetOutputStream() throws FileNotFoundException
     {
         TMXConverter converter = new TMXConverter(false);
         // Delete the file after creation
         converter.getOutputStream(System.getProperty("user.dir") + File.separator + "nop");
         (new File(System.getProperty("user.dir") + File.separator + "nop")).delete();
     }
 
     @Test
     public void testConvertI() throws IOException
     {
         // Verify the process of the base file
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected InputStream getInputStream(String name)
             {
                 return new InputStream() {
                     @Override
                     public int read() throws IOException {
                         return 0;
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected OutputStream getOutputStream(String name)
             {
                 return new OutputStream() {
                     @Override
                     public void write(int b) throws IOException {
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected void convert(String locale, InputStream sourceIS, OutputStream jsOS, OutputStream javaOS) throws IOException
             {
                 // Nothing
             }
         };
 
         converter.setTMXFilenameBase("nop");
         converter.convert("nop");
     }
 
     @Test
     public void testConvertII() throws IOException
     {
         // Verify the process of a correctly localized file
         TMXConverter converter = new TMXConverter(false) {
             @SuppressWarnings("serial")
             @Override
             protected File getFile(String name)
             {
                 return new File("nop") {
                     @Override
                     public boolean exists() {
                         return false;
                     }
                     @Override
                     public boolean mkdir() {
                         return true;
                     }
                 };
             }
             @Override
             protected InputStream getInputStream(String name)
             {
                 return new InputStream() {
                     @Override
                     public int read() throws IOException {
                         return 0;
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected OutputStream getOutputStream(String name)
             {
                 return new OutputStream() {
                     @Override
                     public void write(int b) throws IOException {
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected void convert(String locale, InputStream sourceIS, OutputStream jsOS, OutputStream javaOS) throws IOException
             {
                 // Nothing
             }
         };
 
         converter.setTMXFilenameBase("nop");
         converter.convert("nop_fr");
     }
 
     @Test(expected = IOException.class)
     public void testConvertIII() throws IOException
     {
         // Verify the exception handling when a subfolder related to a new locale cannot be created
         TMXConverter converter = new TMXConverter(false) {
             @SuppressWarnings("serial")
             @Override
             protected File getFile(String name)
             {
                 return new File("nop") {
                     @Override
                     public boolean exists() {
                         return false;
                     }
                     @Override
                     public boolean mkdir() {
                         return false;
                     }
                 };
             }
             @Override
             protected InputStream getInputStream(String name)
             {
                 return new InputStream() {
                     @Override
                     public int read() throws IOException {
                         return 0;
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected OutputStream getOutputStream(String name)
             {
                 return new OutputStream() {
                     @Override
                     public void write(int b) throws IOException {
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected void convert(String locale, InputStream sourceIS, OutputStream jsOS, OutputStream javaOS) throws IOException
             {
                 // Nothing
             }
         };
 
         converter.setTMXFilenameBase("nop");
         converter.convert("nop_fr");
     }
 
     @Test
     public void testConvertIV() throws IOException
     {
         // Verify the process of a correctly localized file
         TMXConverter converter = new TMXConverter(false) {
             @SuppressWarnings("serial")
             @Override
             protected File getFile(String name)
             {
                 return new File("nop") {
                     @Override
                     public boolean exists() {
                         return true;
                     }
                 };
             }
             @Override
             protected InputStream getInputStream(String name)
             {
                 return new InputStream() {
                     @Override
                     public int read() throws IOException {
                         return 0;
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected OutputStream getOutputStream(String name)
             {
                 return new OutputStream() {
                     @Override
                     public void write(int b) throws IOException {
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected void convert(String locale, InputStream sourceIS, OutputStream jsOS, OutputStream javaOS) throws IOException
             {
                 // Nothing
             }
         };
 
         converter.setTMXFilenameBase("nop");
         converter.convert("nop_fr");
     }
 
     @Test
     public void testConvertV() throws IOException
     {
         // Verify the process of a incorrectly localized file
         TMXConverter converter = new TMXConverter(false) {
             @SuppressWarnings("serial")
             @Override
             protected File getFile(String name)
             {
                 return new File("nop") {
                     @Override
                     public boolean exists() {
                         return false;
                     }
                     @Override
                     public boolean mkdir() {
                         return true;
                     }
                 };
             }
             @Override
             protected InputStream getInputStream(String name)
             {
                 return new InputStream() {
                     @Override
                     public int read() throws IOException {
                         return 0;
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected OutputStream getOutputStream(String name)
             {
                 return new OutputStream() {
                     @Override
                     public void write(int b) throws IOException {
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected void convert(String locale, InputStream sourceIS, OutputStream jsOS, OutputStream javaOS) throws IOException
             {
                 // Nothing
             }
         };
 
         converter.setTMXFilenameBase("nop");
         converter.convert("nopfr");
     }
 
     @Test
     public void testGetEntityResolver() throws SAXException, IOException
     {
         // Normal call
         TMXConverter converter = new TMXConverter(false);
         EntityResolver resolver = converter.getEntityResolver();
         resolver.resolveEntity("nop", "nop");
         assertFalse(converter.getProcessStopped());
     }
 
     @Test
     public void testGetFactory()
     {
         // Normal call
         TMXConverter converter = new TMXConverter(false);
         converter.getFactory();
 
         assertFalse(converter.getProcessStopped());
     }
 
     @Test
     public void testGetParserI()
     {
         // Normal call
         TMXConverter converter = new TMXConverter(false);
         DocumentBuilder firstDocBuilder = converter.getParser();
         DocumentBuilder secondDocBuilder = converter.getParser();
 
         assertFalse(converter.getProcessStopped());
         assertEquals(firstDocBuilder, secondDocBuilder);
     }
 
     @Test
     public void testGetParserII()
     {
         // Normal call
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected DocumentBuilderFactory getFactory() {
                 return new DocumentBuilderFactory() {
                     @Override
                     public Object getAttribute(String name) throws IllegalArgumentException {
                         return null;
                     }
                     @Override
                     public boolean getFeature(String name) throws ParserConfigurationException {
                         return false;
                     }
                     @Override
                     public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
                         throw new ParserConfigurationException("Done in purpose");
                     }
                     @Override
                     public void setAttribute(String name, Object value) throws IllegalArgumentException {
                     }
                     @Override
                     public void setFeature(String name, boolean value) throws ParserConfigurationException {
                     }
                 };
             }
         };
         DocumentBuilder docBuilder = converter.getParser();
 
         assertTrue(converter.getProcessStopped());
         assertNull(docBuilder);
     }
 
     @Test
     public void testGetDocumentI()
     {
         // Normal buffer
         final String testStream = "<xml></xml>";
         InputStream stream = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         TMXConverter converter = new TMXConverter(false);
         converter.getDocument(stream);
 
         assertFalse(converter.getProcessStopped());
     }
 
     @Test
     public void testGetDocumentII()
     {
         // Corrupted buffer
         final String testStream = "<xml></>";
         InputStream stream = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         TMXConverter converter = new TMXConverter(false);
         converter.getDocument(stream);
 
         assertTrue(converter.getProcessStopped());
     }
 
     @Test
     public void testGetDocumentIII()
     {
         // Exception thrown because the stream cannot be read
         InputStream stream = new InputStream() {
             @Override
             public int read() throws IOException {
                 throw new IOException("Done in purpose");
             }
         };
 
         TMXConverter converter = new TMXConverter(false);
         converter.getDocument(stream);
 
         assertTrue(converter.getProcessStopped());
     }
 
     @Test
     public void testGetNodeListI()
     {
         final String testStream = "<tu><seg/></tu>";
         InputStream stream = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         TMXConverter converter = new TMXConverter(false);
         Document doc = converter.getDocument(stream);
         NodeList list = converter.getNodeList(doc, "tu/seg");
 
         assertFalse(converter.getProcessStopped());
         assertEquals(1, list.getLength());
     }
 
     @Test
     public void testGetNodeListII()
     {
         final String testStream = "<tu><seg>text</seg></tu>";
         InputStream stream = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         TMXConverter converter = new TMXConverter(false);
         Document doc = converter.getDocument(stream);
         converter.getNodeList(doc, "tu/seg/text()");
 
         assertFalse(converter.getProcessStopped());
     }
 
     @Test
     public void testGetNodeListIII()
     {
         final String testStream = "<tu><seg/></tu>";
         InputStream stream = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         TMXConverter converter = new TMXConverter(false);
         Document doc = converter.getDocument(stream);
         converter.getNodeList(doc, "tu/seg\\@");
 
         assertTrue(converter.getProcessStopped());
     }
 
     @Test
     public void testGetNodeListVI()
     {
         final String testStream = "<tu><seg/><seg/><seg/></tu>";
         InputStream stream = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         TMXConverter converter = new TMXConverter(false);
         Document doc = converter.getDocument(stream);
         NodeList tus = converter.getNodeList(doc, "tu");
         NodeList segs = converter.getNodeList(tus.item(0), "seg");
 
         assertFalse(converter.getProcessStopped());
         assertEquals(3, segs.getLength());
     }
 
     @Test
     public void testConvertStreamsI() throws IOException
     {
         String locale = "fr";
 
         // <tu/> with tuid
         final String testStream = "<tmx><body><tu/></body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         OutputStream jsOS = new MockOutputStream();
         OutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertTrue(converter.isErrorReported());
     }
 
     @Test
     public void testConvertStreamsII() throws IOException
     {
         String locale = "fr";
 
         // <tu/> with empty tuid
         final String testStream = "<tmx><body><tu tuid=''/></body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         OutputStream jsOS = new MockOutputStream();
         OutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertTrue(converter.isErrorReported());
     }
 
     @Test
     public void testConvertStreamsIII() throws IOException
     {
         String locale = "fr";
 
         // <tu/> without <prop/>
         final String testStream = "<tmx><body><tu tuid='1'/><tu tuid='2'/></body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         OutputStream jsOS = new MockOutputStream();
         OutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertTrue(converter.isErrorReported());
     }
 
     @Test
     public void testConvertStreamsIV() throws IOException
     {
         String locale = "fr";
 
         // <tu/> with <prop/> without an expected value in {DOJO_TK, JAVA_RB}
         final String testStream = "<tmx><body><tu tuid='1'><prop type=''/></tu></body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         OutputStream jsOS = new MockOutputStream();
         OutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertTrue(converter.isErrorReported());
     }
 
     @Test
     public void testConvertStreamsV() throws IOException
     {
         String locale = "fr";
         String BuildStamp = "bs";
 
         // Normal tag for the browser code
         final String testStream = "<tmx><body>" +
             "<tu tuid='1'>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<tuv>" +
                     "<seg>one</seg>" +
                 "</tuv>" +
             "</tu>" +
             "<tu tuid='2'>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<tuv>" +
                     "<seg>two</seg>" +
                 "</tuv>" +
             "</tu>" +
         "</body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         MockOutputStream jsOS = new MockOutputStream();
         MockOutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.setBuildStamp(BuildStamp);
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertFalse(converter.isErrorReported());
         assertEquals(converter.getMinimumJavaSize(), javaOS.length());
         assertTrue(converter.getMinimumJSSize() < jsOS.length());
         assertTrue(jsOS.getStream().startsWith(TMXConverter.JS_FILE_START));
         assertTrue(jsOS.getStream().endsWith(TMXConverter.JS_FILE_END));
         assertTrue(jsOS.getStream().indexOf(TMXConverter.JS_LINE_START + "1" + TMXConverter.JS_LINE_MIDDLE + "one" + TMXConverter.JS_LINE_END) != -1);
         assertTrue(jsOS.getStream().indexOf(TMXConverter.JS_LINE_START + "2" + TMXConverter.JS_LINE_MIDDLE + "two" + TMXConverter.JS_LINE_END) != -1);
     }
 
     @Test
     public void testConvertStreamsVI() throws IOException
     {
         String locale = "fr";
         String BuildStamp = "bs";
 
         // Normal tag for the webtier code
         final String testStream = "<tmx><body>" +
             "<tu tuid='1'>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<tuv>" +
                     "<seg>one</seg>" +
                 "</tuv>" +
             "</tu>" +
             "<tu tuid='2'>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<tuv>" +
                     "<seg>two</seg>" +
                 "</tuv>" +
             "</tu>" +
             "</body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         MockOutputStream jsOS = new MockOutputStream();
         MockOutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.setBuildStamp(BuildStamp);
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertFalse(converter.isErrorReported());
         assertEquals(converter.getMinimumJSSize(), jsOS.length());
         assertTrue(converter.getMinimumJavaSize() < javaOS.length());
         assertTrue(javaOS.getStream().indexOf(TMXConverter.JAVA_LINE_START + "1" + TMXConverter.JAVA_LINE_MIDDLE + "one" + TMXConverter.JAVA_LINE_END) != -1);
         assertTrue(javaOS.getStream().indexOf(TMXConverter.JAVA_LINE_START + "2" + TMXConverter.JAVA_LINE_MIDDLE + "two" + TMXConverter.JAVA_LINE_END) != -1);
     }
 
     @Test
     public void testConvertStreamsVII() throws IOException
     {
         String locale = "fr";
         String BuildStamp = "bs";
 
         // Normal tag for the webtier and the browser codes
         final String testStream = "<tmx><body>" +
             "<tu tuid='1'>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<tuv>" +
                     "<seg>one</seg>" +
                 "</tuv>" +
             "</tu>" +
             "<tu tuid='2'>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<tuv>" +
                     "<seg>two</seg>" +
                 "</tuv>" +
             "</tu>" +
         "</body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         MockOutputStream jsOS = new MockOutputStream();
         MockOutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.setBuildStamp(BuildStamp);
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertFalse(converter.isErrorReported());
         assertTrue(converter.getMinimumJavaSize() < javaOS.length());
         assertTrue(converter.getMinimumJSSize() < jsOS.length());
         assertTrue(jsOS.getStream().indexOf(TMXConverter.JS_LINE_START + "1" + TMXConverter.JS_LINE_MIDDLE + "one" + TMXConverter.JS_LINE_END) != -1);
         assertTrue(jsOS.getStream().indexOf(TMXConverter.JS_LINE_START + "2" + TMXConverter.JS_LINE_MIDDLE + "two" + TMXConverter.JS_LINE_END) != -1);
         assertTrue(javaOS.getStream().indexOf(TMXConverter.JAVA_LINE_START + "1" + TMXConverter.JAVA_LINE_MIDDLE + "one" + TMXConverter.JAVA_LINE_END) != -1);
         assertTrue(javaOS.getStream().indexOf(TMXConverter.JAVA_LINE_START + "2" + TMXConverter.JAVA_LINE_MIDDLE + "two" + TMXConverter.JAVA_LINE_END) != -1);
     }
 
     @Test
     public void testConvertStreamsVIII() throws IOException
     {
         String locale = "fr";
         String BuildStamp = "bs";
 
         // <tuv/> with no text
         final String testStream = "<tmx><body>" +
             "<tu tuid='1'>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<tuv>" +
                     "<seg></seg>" +
                 "</tuv>" +
             "</tu>" +
         "</body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         MockOutputStream jsOS = new MockOutputStream();
         MockOutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.setBuildStamp(BuildStamp);
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertTrue(converter.isErrorReported());
     }
 
     @Test
     public void testConvertStreamsIX() throws IOException
     {
         String locale = "fr";
         String BuildStamp = "bs";
 
         // <tuv/> with text to be escaped
         final String testStream = "<tmx><body>" +
             "<tu tuid='1'>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<tuv>" +
                     "<seg>{0}{1}</seg>" +
                 "</tuv>" +
             "</tu>" +
         "</body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         MockOutputStream jsOS = new MockOutputStream();
         MockOutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.setBuildStamp(BuildStamp);
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertFalse(converter.isErrorReported());
         assertTrue(converter.getMinimumJavaSize() < javaOS.length());
         assertTrue(converter.getMinimumJSSize() < jsOS.length());
        assertTrue(jsOS.getStream().indexOf(TMXConverter.JS_LINE_START + "1" + TMXConverter.JS_LINE_MIDDLE + "${0}${1}" + TMXConverter.JS_LINE_END) != -1);
         assertTrue(javaOS.getStream().indexOf(TMXConverter.JAVA_LINE_START + "1" + TMXConverter.JAVA_LINE_MIDDLE + "{0}{1}" + TMXConverter.JAVA_LINE_END) != -1);
     }
 
     @Test
     public void testConvertStreamsX() throws IOException
     {
         String locale = "fr";
         String BuildStamp = "bs";
 
         // <tuv/> defines the language
         final String testStream = "<tmx><body>" +
             "<tu tuid='" + TMXConverter.LANGUAGE_ID + "'>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<tuv>" +
                     "<seg>" + locale + "</seg>" +
                 "</tuv>" +
             "</tu>" +
             "<tu tuid='1'>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<tuv>" +
                     "<seg>one</seg>" +
                 "</tuv>" +
             "</tu>" +
         "</body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         MockOutputStream jsOS = new MockOutputStream();
         MockOutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.setBuildStamp(BuildStamp);
         assertEquals(0, converter.getLanguageMap().keySet().size());
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertFalse(converter.isErrorReported());
         assertTrue(converter.getMinimumJavaSize() < javaOS.length());
         assertTrue(converter.getMinimumJSSize() < jsOS.length());
         assertEquals(1, converter.getLanguageMap().keySet().size());
         assertEquals(locale, converter.getLanguageMap().get(locale));
     }
 
     @Test
     public void testConvertStreamsXI() throws IOException
     {
         String locale = null;
         String BuildStamp = "bs";
 
         // <tuv/> defines the language
         final String testStream = "<tmx><body>" +
             "<tu tuid='" + TMXConverter.LANGUAGE_ID + "'>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<tuv>" +
                     "<seg>" + locale + "</seg>" +
                 "</tuv>" +
             "</tu>" +
             "<tu tuid='1'>" +
                 "<prop type='x-tier'>" + TMXConverter.DOJO_TK + "</prop>" +
                 "<prop type='x-tier'>" + TMXConverter.JAVA_RB + "</prop>" +
                 "<tuv>" +
                     "<seg>one</seg>" +
                 "</tuv>" +
             "</tu>" +
         "</body></tmx>";
         InputStream tmxIS = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         MockOutputStream jsOS = new MockOutputStream();
         MockOutputStream javaOS = new MockOutputStream();
 
         TMXConverter converter = new TMXConverter(false);
         converter.setBuildStamp(BuildStamp);
         assertEquals(0, converter.getLanguageMap().keySet().size());
         converter.convert(locale, tmxIS, jsOS, javaOS);
 
         assertFalse(converter.isErrorReported());
         assertTrue(converter.getMinimumJavaSize() < javaOS.length());
         assertTrue(converter.getMinimumJSSize() < jsOS.length());
         assertEquals(1, converter.getLanguageMap().keySet().size());
         assertEquals("null", converter.getLanguageMap().get("en"));
     }
 
     @Test
     public void testGetMinimumJSSize()
     {
         TMXConverter converter = new TMXConverter(false);
 
         converter.setBuildStamp(null);
         converter.getMinimumJSSize();
 
         converter.setBuildStamp("bla-bla");
         converter.getMinimumJSSize();
     }
 
     @Test
     public void testGetMinimumJavaSize()
     {
         TMXConverter converter = new TMXConverter(false);
 
         converter.setBuildStamp(null);
         converter.getMinimumJavaSize();
 
         converter.setBuildStamp("bla-bla");
         converter.getMinimumJavaSize();
     }
 
     @Test
     public void testSaveSupportedLanguagesI() throws IOException
     {
         // Verify the process of the output stream
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected OutputStream getOutputStream(String name)
             {
                 return new OutputStream() {
                     @Override
                     public void write(int b) throws IOException {
                     }
                     @Override
                     public void close() throws IOException {
                         // Nothing
                     }
                 };
             }
             @Override
             protected void saveSupportedLanguages(OutputStream os) throws IOException
             {
                 // Nothing
             }
         };
 
         converter.saveSupportedLanguages();
     }
 
     @Test
     public void testSaveSupportedLanguagesII() throws IOException
     {
         // Verify the saving of the existing language definitions
         Map<String, String> foundLanguages = new HashMap<String, String>();
         foundLanguages.put("de", "Deutsch");
 
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected ResourceBundle getDocumentedLanguages() {
                 return null;
             }
         };
         converter.setLanguageMap(foundLanguages);
         MockOutputStream stream = new MockOutputStream();
 
         converter.saveSupportedLanguages(stream);
         System.out.println("stream: " + stream.getStream());
         assertTrue(stream.getStream().indexOf("de=Deutsch") != -1);
     }
 
     @Test
     public void testSaveSupportedLanguagesIII() throws IOException
     {
         // Verify the saving of the existing language definitions
         final String testStream = "en=English" + TMXConverter.NL + "pl=Polski" + TMXConverter.NL + "da=Danske";
         final InputStream rbFile = new InputStream() {
             int idx = 0;
             @Override
             public int read() throws IOException {
                 if (idx < testStream.length()) {
                     return (int) testStream.charAt(idx++);
                 }
                 return -1;
             }
         };
 
         Map<String, String> foundLanguages = new HashMap<String, String>();
         foundLanguages.put("da", "Dansk");
         foundLanguages.put("de", "Deutsch");
 
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected ResourceBundle getDocumentedLanguages() {
                 return new ResourceBundle() {
                     ResourceBundle rb;
                     @Override
                     public Enumeration<String> getKeys() {
                         try
                         {
                             rb = new PropertyResourceBundle(rbFile);
                             return rb.getKeys();
                         }
                         catch (IOException e)
                         {
                             fail("No expected exception");
                         }
                         return null;
                     }
                     @Override
                     protected Object handleGetObject(String key) {
                         return rb.getObject(key);
                     }
                 };
             }
         };
         converter.setLanguageMap(foundLanguages);
         MockOutputStream stream = new MockOutputStream();
 
         converter.saveSupportedLanguages(stream);
         System.out.println("stream: " + stream.getStream());
         assertTrue(stream.getStream().indexOf("de=Deutsch") != -1);
         assertTrue(stream.getStream().indexOf("en=English") != -1);
         assertTrue(stream.getStream().indexOf("pl=Polski") != -1);
     }
 
     @Test
     public void testSaveSupportedLanguagesIV() throws IOException
     {
         // Verify the saving of the existing language definitions
         Map<String, String> foundLanguages = new HashMap<String, String>();
         foundLanguages.put("de", "Deutsch");
 
         TMXConverter converter = new TMXConverter(false) {
             @Override
             protected ResourceBundle getDocumentedLanguages() {
                 throw new MissingResourceException("Done in purpose", null, null);
             }
         };
         converter.setLanguageMap(foundLanguages);
         MockOutputStream stream = new MockOutputStream();
 
         converter.saveSupportedLanguages(stream);
         System.out.println("stream: " + stream.getStream());
         assertTrue(stream.getStream().indexOf("de=Deutsch") != -1);
     }
 }
