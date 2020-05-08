 package ups.m2glre.rossf1.parser;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 import org.jdom.Document;
 import org.jdom.input.SAXBuilder;
 
 import ups.m2glre.rossf1.question.EssayQuestion;
 
 public class EssayQuestionParserTest extends TestCase {
     private EssayQuestionParser essayQuestionParser;
 
     public void setUp() {
         essayQuestionParser = new EssayQuestionParser();
     }
 
     public void testFile1() {
         try {
             Document document = new SAXBuilder().build(new File("src/test/TestEssayQuestion1.xml"));
             EssayQuestion essayQuestion = (EssayQuestion) essayQuestionParser.
                     parseQuestion(document.getRootElement().getChild("question"));

            assertEquals(0, essayQuestion.getAnswerFraction());
            assertEquals("Feedback test", essayQuestion.getAnswerText());

         } catch (Exception e) {
             System.out.println(e.getMessage());
             e.printStackTrace();
             fail();
         }
 
     }

     public void testFile2() {
         try {
             Document document = new SAXBuilder().build(new File("src/test/TestEssayQuestion2.xml"));
             EssayQuestion essayQuestion = (EssayQuestion) essayQuestionParser.
                     parseQuestion(document.getRootElement().getChild("question"));
            assertEquals(2, essayQuestion.getAnswerFraction());
            assertEquals("Good job", essayQuestion.getAnswerText());
         } catch (Exception e) {
             e.printStackTrace();
             fail();
         }
 
     }
 }
