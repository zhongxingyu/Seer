 package no.anderska.wta.questions;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Random;
 
 import static no.anderska.wta.questions.CalculusGenerator.*;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class ComputationQuestionGenerationTest {
     CalculusGenerator generator = new CalculusGenerator();
     Random random = mock(Random.class);
 
     @Before
     public void setUp() throws Exception {
         generator.setRandom(random);
     }
 
 
     @Test
    public void shouldHandleSimple() throws Exception {
        when(random.nextInt(anyInt())).thenReturn(0, 41);
        checkEqAndAnswer("42", "42");
    }

    @Test
    public void basicAddition() throws Exception {
        when(random.nextInt(anyInt())).thenReturn(TYPE_ADD, TYPE_NUMBER, 3, TYPE_NUMBER, 2);
        checkEqAndAnswer("4+3", "7");
    }

    @Test
     public void basicMultplication() throws Exception {
        when(random.nextInt(anyInt())).thenReturn(TYPE_MULT, 3, 2);
         checkEqAndAnswer("4*3", "12");
     }
 
     @Test
     public void basicSubstraction() throws Exception {
        when(random.nextInt(anyInt())).thenReturn(TYPE_SUBSTR, 3, 2);
         checkEqAndAnswer("4-3", "1");
     }
 
     @Test
     public void complexComputation() throws Exception {
        when(random.nextInt(anyInt())).thenReturn(TYPE_ADD, TYPE_MULT, 3, 2, TYPE_SUBSTR, 4, 3);
         checkEqAndAnswer("4*3+5-4", "13");
     }
 
 
     private void checkEqAndAnswer(String expEquation, String expAnswer) {
         String[] qanda = generator.generateComputation();
         assertThat(qanda[0]).isEqualTo(expEquation);
         assertThat(qanda[1]).isEqualTo(expAnswer);
     }
 
 }
