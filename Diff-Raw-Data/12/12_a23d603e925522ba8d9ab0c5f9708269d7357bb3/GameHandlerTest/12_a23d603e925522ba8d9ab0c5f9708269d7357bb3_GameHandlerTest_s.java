 package no.anderska.wta.game;
 
 import static java.util.Arrays.asList;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 import java.util.Arrays;
 import java.util.List;
 
 import no.anderska.wta.AnswerStatus;
 import no.anderska.wta.QuestionList;
 import no.anderska.wta.dto.CategoriesAnsweredDTO;
 import no.anderska.wta.dto.CategoryDTO;
 import no.anderska.wta.questions.DummyQuestionGenerator;
 import no.anderska.wta.servlet.PlayerHandler;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 
 public class GameHandlerTest {
 
     private final GameHandler gameHandler = new GameHandler();
     private final PlayerHandler playerHandler = gameHandler.getPlayerHandler();
     private final DummyQuestionGenerator generator =
             new DummyQuestionGenerator(Arrays.asList(new Question("q", "a")));
     private final GameLogger gameLogger = mock(GameLogger.class);
 
     @Test
     public void shouldGiveQuestions() throws Exception {
         String playerid = playerHandler.createPlayer("Player name");
         generator.addQuestionSet(asList(new Question("one", "factone"), new Question("two", "facttwo")));
 
         QuestionList questions = gameHandler.questions(playerid, "one");
         assertThat(questions.isOk()).isTrue();
         assertThat(questions.getQuestions()).containsExactly("one", "two");
     }
 
     @Test
     public void shouldGiveErrorOnWrongPlayer() throws Exception {
         QuestionList questions = gameHandler.questions("playerone", "one");
 
         assertThat(questions.isOk()).isFalse();
         assertThat(questions.getErrormessage()).isEqualTo("Unknown player 'playerone'");
     }
 
     @Test
     public void shouldGiveErrorOnWrongCategory() throws Exception {
         String playerid = playerHandler.createPlayer("Player");
         QuestionList questions = gameHandler.questions(playerid, "two");
 
         assertThat(questions.isOk()).isFalse();
         assertThat(questions.getErrormessage()).isEqualTo("Unknown category 'two'");
     }
 
     @Test
     public void shouldHandleCorrectAnswer() throws Exception {
         String playerid = playerHandler.createPlayer("Player");
 
         generator.addQuestionSet(asList(new Question("one", "factone"), new Question("two", "facttwo")));
        gameHandler.setGameLogger(gameLogger);
 
         gameHandler.questions(playerid, "one");
 
         AnswerStatus answerStatus = gameHandler.answer(playerid, Arrays.asList("factone", "facttwo"));
 
         assertThat(answerStatus).isEqualTo(AnswerStatus.OK);
         Class<List<String>> stringListClass = (Class<List<String>>)(Class)List.class;
 
         ArgumentCaptor<List<String>> answerCaptor = ArgumentCaptor.forClass(stringListClass);
         ArgumentCaptor<List<String>> expectedCaptor = ArgumentCaptor.forClass(stringListClass);
 
         verify(gameLogger).answer(eq(playerid),answerCaptor.capture(),expectedCaptor.capture(),eq(AnswerStatus.OK),eq(110));
 
         assertThat(answerCaptor.getAllValues()).hasSize(1);
         assertThat(expectedCaptor.getAllValues()).hasSize(1);
         assertThat(answerCaptor.getValue()).containsExactly("factone","facttwo");
         assertThat(expectedCaptor.getValue()).containsExactly("factone","facttwo");
     }
 
     @Test
     public void shouldGiveCorrectStatus() throws Exception {
         String playerid = playerHandler.createPlayer("Player One");
 
         generator.addQuestionSet(asList(new Question("one", "factone"), new Question("two", "facttwo")));
 
         gameHandler.questions(playerid, "one");
 
 
         gameHandler.answer(playerid, Arrays.asList("factone", "facttwo"));
 
         List<CategoryDTO> categoryDTOs = gameHandler.categoryStatus();
         assertThat(categoryDTOs).hasSize(1);
         CategoryDTO categoryDTO = categoryDTOs.get(0);
 
         assertThat(categoryDTO.getId()).isEqualTo("one");
         assertThat(categoryDTO.getName()).isEqualTo(generator.description());
         assertThat(categoryDTO.getAnsweredBy()).isEqualTo("Player One");
     }
 
     @Test
     public void shouldHandleNoQuestion() throws Exception {
         AnswerStatus answerStatus = gameHandler.answer("playerone", Arrays.asList("factone", "facttwo"));
         assertThat(answerStatus).isEqualTo(AnswerStatus.ERROR);
     }
 
     @Test
     public void shouldRegisterCorrectAnswers() throws Exception {
         String playerid1 = playerHandler.createPlayer("Player One");
         String playerid2 = playerHandler.createPlayer("Player Two");
 
         Question question1 = new Question("one", "factone");
         Question question2 = new Question("two", "facttwo");
 
         generator.addQuestionSet(asList(question1, question2));
         gameHandler.questions(playerid1, "one");
         gameHandler.answer(playerid1, asList(question1.getCorrectAnswer(), question2.getCorrectAnswer()));
 
         generator.addQuestionSet(asList(question1, question2));
         gameHandler.questions(playerid2, "one");
         gameHandler.answer(playerid2, asList(question1.getCorrectAnswer(), question2.getCorrectAnswer()));
 
         List<CategoriesAnsweredDTO> answered = gameHandler.categoriesAnswered();
 
         assertThat(answered).onProperty("playerName")
             .containsOnly("Player One", "Player Two");
         assertThat(answered.get(0).getCategories()).containsExactly("one");
         assertThat(answered.get(1).getCategories()).containsExactly("one");
     }
 
 
     @Test
     public void shouldEditCategories() {
         String playerid = playerHandler.createPlayer("Player One");
 
         gameHandler.editCategories("secret", asList("FromRoman", "one"));
         assertThat(gameHandler.questions(playerid, "one").isOk()).isTrue();
         gameHandler.answer(playerid, Arrays.asList("a"));
         assertThat(gameHandler.categoryStatus("one").getAnsweredBy()).isEqualTo("Player One");
 
         gameHandler.editCategories("secret", asList("ToRoman", "one"));
         assertThat(gameHandler.questions(playerid, "FromRoman").getErrormessage()).startsWith("Unknown category");
         assertThat(gameHandler.questions(playerid, "ToRoman").isOk()).isTrue();
         assertThat(gameHandler.questions(playerid, "one").isOk()).isTrue();
         assertThat(gameHandler.categoryStatus("one").getAnsweredBy()).isEqualTo("Player One");
     }
 
 
     @Before
     public void setup() {
         gameHandler.addQuestionCategory("one", generator);
     }
 }
