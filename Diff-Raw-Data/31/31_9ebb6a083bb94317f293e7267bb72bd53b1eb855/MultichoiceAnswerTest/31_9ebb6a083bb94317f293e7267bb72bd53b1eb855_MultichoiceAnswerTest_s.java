 package universite.toulouse.moodlexmlapi.tartopom.impl.model;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import universite.toulouse.moodlexmlapi.core.data.QuestionTextFormat;
 
 /**
  * Tests unitaires de la classe MultichoiceTest
  */
 public class MultichoiceAnswerTest {
 
     MultichoiceAnswer multi;
 
     /**
      * Méthode exécutée avant les tests
      */
     @Before
     public void setUp() throws Exception {
         multi = new MultichoiceAnswer();
     }
 
     @Test
     public void testFeedback() {
         EnclosedText feedback = new EnclosedText("feedback");
         multi.setFeedback(feedback);
 
         Assert.assertEquals(feedback, multi.getFeedback());
     }
 
     @Test
     public void testFormat() {
         multi.setFormat(QuestionTextFormat.markdown);
 
         Assert.assertEquals(QuestionTextFormat.markdown, multi.getFormat());
     }
 
     @Test
     public void testFraction() {
        int fraction = 50;
         multi.setFraction(fraction);
 
        Assert.assertEquals(fraction, multi.getFraction());
     }
 
     @Test
     public void testText() {
         String text = "Texte";
         multi.setText(text);
 
         Assert.assertEquals(text, multi.getText());
     }
 }
