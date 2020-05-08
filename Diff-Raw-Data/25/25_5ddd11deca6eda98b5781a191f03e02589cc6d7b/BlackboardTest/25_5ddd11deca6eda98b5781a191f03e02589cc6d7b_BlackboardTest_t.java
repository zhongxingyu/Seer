 /**
  * 
  */
 package org.dlw.ai.blackboard;
 
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.dlw.ai.blackboard.domain.BlackboardObject;
 import org.dlw.ai.blackboard.domain.Letter;
 import org.dlw.ai.blackboard.domain.Sentence;
 import org.dlw.ai.blackboard.domain.Word;
 import org.dlw.ai.blackboard.util.UniversalContext;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author dlwhitehurst
  * @version 1.0.0
  */
 public class BlackboardTest {
 
     private Blackboard blackboard;
     private int count = 0;
     private int i = 0;
     private int j = 0;
     
     
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
        blackboard = BlackboardContext.getInstance().getBlackboard();
     }
     
     @Test
     public void testAssertProblem() throws AssertionError {
         assertNotNull(blackboard);
         
         blackboard.assertProblem("I HAVE SEEN THE SMALL SHELL");
         for (BlackboardObject obj: blackboard) {
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.Sentence.class)) {
                 Sentence sentence = (Sentence) obj;
                 System.out.println("SENTENCE: " + sentence.value());
             }
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.Word.class)) {
                 Word word = (Word) obj;
                 System.out.println("WORD: " + word.value());
             }
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.Letter.class)) {
                 Letter letter = (Letter) obj;
                 System.out.println("LETTER: " + letter.value());
             }
         }
         
         Sentence sentence = blackboard.getSentence();
         assertTrue(sentence.value() == "I HAVE SEEN THE SMALL SHELL");
         System.out.println(sentence.value());
     }
     
     @Test
     public void testWordCount() throws AssertionError {
         for (BlackboardObject obj: blackboard) {
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.Word.class)) {
                 Word word = (Word) obj;
                 System.out.println("WORD: " + word.value());
                 count++;
             }
         }
         System.out.println(count);
 
         assertTrue(count == 6);
     }
     
     @Test
     public void testHierarchy() throws AssertionError {
         Sentence sentence = blackboard.getSentence();
         
         List<Word> words = sentence.getWords();
         for (Word word: words) {
             System.out.println(i + ":" + word.value());
             i++;
             List<Letter> letters = word.getLetters();
             for (Letter letter: letters) {
                 System.out.println(j + ":" + letter.value());
                 j++;
             }
         }
         assertTrue(i == 6);
 
     }
 }
