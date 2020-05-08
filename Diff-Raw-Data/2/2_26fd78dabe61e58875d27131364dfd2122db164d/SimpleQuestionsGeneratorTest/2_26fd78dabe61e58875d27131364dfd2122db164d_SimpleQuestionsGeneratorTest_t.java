package no.anderska.wta.questions;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Test;
 
 public class SimpleQuestionsGeneratorTest {
 
     @Test
     public void wordCount() {
         WordCountQuestionGenerator generator = new WordCountQuestionGenerator();
         assertThat(generator.createAnswer("Hello")).isEqualTo("5");
         assertThat(generator.createAnswer("År")).isEqualTo("2");
     }
 
     @Test
     public void firstCharacter() {
         FirstCharacterQuestionGenerator generator = new FirstCharacterQuestionGenerator();
         assertThat(generator.createAnswer("Hello")).isEqualTo("H");
         assertThat(generator.createAnswer("År")).isEqualTo("Å");
     }
 
     @Test
     public void lastCharacter() {
         LastCharacterQuestionGenerator generator = new LastCharacterQuestionGenerator();
         assertThat(generator.createAnswer("Hello")).isEqualTo("o");
         assertThat(generator.createAnswer("Også")).isEqualTo("å");
     }
 
 
 }
