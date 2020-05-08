 package no.anderska.wta;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Arrays;
 import java.util.List;
 
 import no.anderska.wta.dto.AnswerResponseDTO;
 import no.anderska.wta.dto.AnswerResponseDTO.AnswerStatus;
 import no.anderska.wta.dto.QuestionCategoryDTO;
 import no.anderska.wta.dto.QuestionDTO;
 import no.anderska.wta.servlet.PlayerHandler;
 
 import org.junit.Test;
 
 public class GameEngineTest {
     private PlayerHandler playerHandler = mock(PlayerHandler.class);
     private QuestionCategoryEngine dummyQCE = mock(QuestionCategoryEngine.class);
 
     @Test
     public void shouldCallDescription() {
         List<QuestionCategoryEngine> dummyCategories = Arrays.asList(dummyQCE);
         GameEngine gameEngine = new GameEngine(dummyCategories,playerHandler);
         when(dummyQCE.getDescription()).thenReturn("The description");
         
         List<QuestionCategoryDTO> allCategories = gameEngine.allCategories();
         
         verify(dummyQCE).getDescription();
         
         assertThat(allCategories).hasSize(1);
         QuestionCategoryDTO questionCategoryDTO = allCategories.get(0);
         assertThat(questionCategoryDTO.getDescription()).isEqualTo("The description");
     }
     
     @Test
     public void shouldMapQuestionsAtCreation() {
         QuestionCategoryEngine anotherDummyQCE = mock(QuestionCategoryEngine.class);
         
         when(dummyQCE.myQuestions()).thenReturn(Arrays.asList(new Question(1,"Question one",10)));
         when(anotherDummyQCE.myQuestions()).thenReturn(Arrays.asList(new Question(1,"Question two",10)));
         
         List<QuestionCategoryEngine> dummyCategories = Arrays.asList(dummyQCE,anotherDummyQCE);
         
         GameEngine gameEngine = new GameEngine(dummyCategories,playerHandler);
         
         List<QuestionCategoryDTO> allCategories = gameEngine.allCategories();
         
         assertThat(allCategories).hasSize(2);
         
         QuestionCategoryDTO qcatOne = allCategories.get(0);
         QuestionCategoryDTO qcatTwo = allCategories.get(1);
         
         assertThat(qcatOne.getId()).isNotEqualTo(qcatTwo.getId());
         
         List<QuestionDTO> questionsCatOne = gameEngine.listCategory(qcatOne.getId());
         List<QuestionDTO> questionsCatTwo = gameEngine.listCategory(qcatTwo.getId());
         
         assertThat(questionsCatOne).hasSize(1);
         assertThat(questionsCatTwo).hasSize(1);
         
         assertThat(questionsCatOne.get(0).getId()).isNotEqualTo(questionsCatTwo.get(0).getId());
     }
     
     @Test
     public void shouldCheckIfCorrectAnswer() {
        when(dummyQCE.myQuestions()).thenReturn(Arrays.asList(new Question(54,"Question one",10)));
        when(dummyQCE.checkAnswer("3", 54, "42")).thenReturn(true);
        when(playerHandler.playerPlaying(3)).thenReturn(true);
         
         GameEngine gameEngine = new GameEngine(Arrays.asList(dummyQCE),playerHandler);
         QuestionDTO questionDTO = gameEngine.listCategory(gameEngine.allCategories().get(0).getId()).get(0);
         
         AnswerResponseDTO answerResponse = gameEngine.checkAnswer("3", "" + questionDTO.getId(), "42");
         
         assertThat(answerResponse.getAnswerStatus()).isEqualTo(AnswerStatus.OK);
         verify(playerHandler).addPoints(3, 10);
     }
     
     @Test
     public void shouldHandleWrongAnswer() {
         when(dummyQCE.myQuestions()).thenReturn(Arrays.asList(new Question(1,"Question one",10)));
         
         GameEngine gameEngine = new GameEngine(Arrays.asList(dummyQCE),playerHandler);
         QuestionDTO questionDTO = gameEngine.listCategory(gameEngine.allCategories().get(0).getId()).get(0);
         when(dummyQCE.checkAnswer("3", questionDTO.getId(), "42")).thenReturn(false);
         when(playerHandler.playerPlaying(3)).thenReturn(true);
         
         AnswerResponseDTO answerResponse = gameEngine.checkAnswer("3", "" + questionDTO.getId(), "42");
         
         assertThat(answerResponse.getAnswerStatus()).isEqualTo(AnswerStatus.WRONG);
         verify(playerHandler).addPoints(3, -10);
     }
     
     @Test
     public void shouldHandleUnknownQuestionId() {
         when(dummyQCE.myQuestions()).thenReturn(Arrays.asList(new Question(1,"Question one",10)));
         
         GameEngine gameEngine = new GameEngine(Arrays.asList(dummyQCE),playerHandler);
         when(playerHandler.playerPlaying(3)).thenReturn(true);
         
         AnswerResponseDTO answerResponse = gameEngine.checkAnswer("3", "456", "42");
         
         verify(dummyQCE,never()).checkAnswer(anyString(), anyInt(), anyString());
         
         assertThat(answerResponse.getAnswerStatus()).isEqualTo(AnswerStatus.MISSING_PARAMETER);
         assertThat(answerResponse.getDescription()).isEqualTo("Unknown question id");
         
     }
    
     @Test
     public void shouldHandleIllegalQuestionId() {
         when(dummyQCE.myQuestions()).thenReturn(Arrays.asList(new Question(1,"Question one",10)));
         
         GameEngine gameEngine = new GameEngine(Arrays.asList(dummyQCE),playerHandler);
         when(playerHandler.playerPlaying(3)).thenReturn(true);
         
         AnswerResponseDTO answerResponse = gameEngine.checkAnswer("3", "456x", "42");
         
         verify(dummyQCE,never()).checkAnswer(anyString(), anyInt(), anyString());
         
         assertThat(answerResponse.getAnswerStatus()).isEqualTo(AnswerStatus.MISSING_PARAMETER);
         assertThat(answerResponse.getDescription()).isEqualTo("Unknown question id");
     }
     
     @Test
     public void shouldHandleUnknownPlayer() throws Exception {
 when(dummyQCE.myQuestions()).thenReturn(Arrays.asList(new Question(1,"Question one",10)));
         
         GameEngine gameEngine = new GameEngine(Arrays.asList(dummyQCE),playerHandler);
         when(playerHandler.playerPlaying(3)).thenReturn(false);
 
         QuestionDTO questionDTO = gameEngine.listCategory(gameEngine.allCategories().get(0).getId()).get(0);
 
         verify(dummyQCE,never()).checkAnswer(anyString(), anyInt(), anyString());
         
         AnswerResponseDTO answerResponse = gameEngine.checkAnswer("3", "" + questionDTO.getId(), "42");
         assertThat(answerResponse.getAnswerStatus()).isEqualTo(AnswerStatus.MISSING_PARAMETER);
         assertThat(answerResponse.getDescription()).isEqualTo("Unknown player id");
     }
 
 }
