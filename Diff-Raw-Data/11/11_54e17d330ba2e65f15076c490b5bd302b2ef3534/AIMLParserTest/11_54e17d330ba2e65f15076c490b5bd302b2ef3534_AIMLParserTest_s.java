 /*
     jaiml - java AIML library
     Copyright (C) 2009  Kim Sullivan
     
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
     
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 /**
  * 
  */
 package aiml.parser;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 
 import junit.framework.TestCase;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import aiml.bot.Bot;
 import aiml.classifier.Classifier;
 import aiml.context.Context;
 import aiml.context.ContextInfo;
 import aiml.context.InputContext;
 import aiml.context.behaviour.PatternBehaviour;
 import aiml.context.data.EnvironmentInputSource;
 import aiml.context.data.StringSource;
 
 public class AIMLParserTest extends TestCase {
 
   private Classifier classifier;
   private AIMLParser ap;
   private Bot b;
   private ContextInfo contextInfo;
 
   public AIMLParserTest(String s) {
     super(s);
   }
 
   @Override
   protected void setUp() throws Exception {
     super.setUp();
 
     classifier = new Classifier();
 
     b = new Bot(classifier, "foobar");
     b.setProperty("name", "foobar");
     b.setProperty("baz", "bar");
 
     ap = new AIMLParser(b);
     PatternBehaviour patternBehaviour = new PatternBehaviour();
     contextInfo = classifier.getContextInfo();
     contextInfo.registerContext(new InputContext("input",
         new EnvironmentInputSource(), patternBehaviour));
     contextInfo.registerContext(new Context<String>("that", new StringSource(),
         patternBehaviour));
     contextInfo.registerContext(new Context<String>("topic",
         new StringSource(), patternBehaviour));
 
     contextInfo.registerContext(new Context<String>("alpha",
         new StringSource(), patternBehaviour));
     contextInfo.registerContext(new Context<String>("beta", new StringSource(),
         patternBehaviour));
     contextInfo.registerContext(new Context<String>("gama", new StringSource(),
         patternBehaviour));
     contextInfo.registerContext(new Context<String>("delta",
         new StringSource(), patternBehaviour));
 
     contextInfo.registerContext(new Context<String>("foo", new StringSource(),
         patternBehaviour));
     contextInfo.registerContext(new Context<String>("bar", new StringSource(),
         patternBehaviour));
 
     contextInfo.registerContext(new Context<String>("ichi", new StringSource(),
         patternBehaviour));
     contextInfo.registerContext(new Context<String>("ni", new StringSource(),
         patternBehaviour));
     contextInfo.registerContext(new Context<String>("san", new StringSource(),
         patternBehaviour));
 
     classifier.registerDefaultNodeHandlers();
   }
 
   private void loadFail(Reader in, Class<? extends Exception> exception)
       throws Exception {
     try {
       ap.load(in);
       fail("Expected AimlSyntaxException");
     } catch (Exception e) {
       if (exception.isAssignableFrom(e.getClass()))
         return;
       else
         throw e;
     }
     fail("Expected exception " + exception);
   }
 
   private void loadFail(InputStream in, String encoding,
       Class<? extends Exception> exception) throws Exception {
     try {
       ap.load(in, encoding);
       fail("Expected AimlSyntaxException");
     } catch (Exception e) {
       if (exception.isAssignableFrom(e.getClass()))
         return;
       else
         throw e;
     }
     fail("Expected exception " + exception);
   }
 
   public void testAimlRoot() throws Exception {
     ap.load(new StringReader("<aiml version='1.0'/>"));
     loadFail(new StringReader("<aiml></aiml>"), AimlSyntaxException.class);
     loadFail(new StringReader("<AIML></AIML>"), AimlSyntaxException.class);
     loadFail(new StringReader("<aiml version='1.0p'></aiml>"),
         InvalidAimlVersionException.class);
     loadFail(new StringReader("<aiml version='1.0'></aiml><foo></foo>"),
         XmlPullParserException.class);
 
   }
 
   public void testCategoryList() throws Exception {
     InputStream in = null;
     try {
      in = new FileInputStream("tests/categoryList-ok.aiml");
       ap.load(in, "UTF-8");
     } finally {
       if (in != null) {
         in.close();
       }
     }
 
   }
 
   public void testCategoryListBadStart() throws Exception {
     InputStream in = null;
     try {
      in = new FileInputStream("tests/categoryList-badstart.aiml");
       loadFail(in, "UTF-8", AimlSyntaxException.class);
     } finally {
       if (in != null) {
         in.close();
       }
     }
   }
 
   public void testCategoryListBadStart2() throws Exception {
     InputStream in = null;
     try {
      in = new FileInputStream("tests/categoryList-badstart2.aiml");
       loadFail(in, "UTF-8", AimlSyntaxException.class);
     } finally {
       if (in != null) {
         in.close();
       }
     }
   }
 
   public void testCategoryListBadEnd() throws Exception {
     InputStream in = null;
     try {
      in = new FileInputStream("tests/categoryList-badend.aiml");
       loadFail(in, "UTF-8", AimlSyntaxException.class);
     } finally {
       if (in != null) {
         in.close();
       }
     }
   }
 
   public void testLoadPatterns() throws Exception {
     InputStream in = null;
     try {
       in = new FileInputStream("tests/patterns.aiml");
       ap.load(in, "UTF-8");
 
     } finally {
       if (in != null) {
         in.close();
       }
     }
 
   }
 
   public void testLoadPatternsBad1() throws Exception {
     InputStream in = null;
     try {
       in = new FileInputStream("tests/patterns-bad1.aiml");
       loadFail(in, "UTF-8", AimlSyntaxException.class);
 
     } finally {
       if (in != null) {
         in.close();
       }
     }
   }
 
   public void testLoadPatternsBad2() throws Exception {
     InputStream in = null;
     try {
       in = new FileInputStream("tests/patterns-bad1.aiml");
       loadFail(in, "UTF-8", AimlSyntaxException.class);
     } finally {
       if (in != null) {
         in.close();
       }
     }
   }
 
   public void testLoadPatternsBad3() throws Exception {
     InputStream in = null;
     try {
       in = new FileInputStream("tests/patterns-bad1.aiml");
       loadFail(in, "UTF-8", AimlSyntaxException.class);
     } finally {
       if (in != null) {
         in.close();
       }
     }
   }
 
   public void testLoadPatternsBad4() throws Exception {
     InputStream in = null;
     try {
       in = new FileInputStream("tests/patterns-bad1.aiml");
       loadFail(in, "UTF-8", AimlSyntaxException.class);
     } finally {
       if (in != null) {
         in.close();
       }
     }
   }
 
   public void testLoadTemplate() throws Exception {
     InputStream in = null;
     try {
       in = new FileInputStream("tests/templates.aiml");
       ap.load(in, "UTF-8");
     } finally {
       if (in != null) {
         in.close();
       }
     }
 
   }
 }
