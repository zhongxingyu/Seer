 package my.triviagame.bll;
 
 import com.sun.tools.javac.util.Pair;
 import java.util.ArrayList;
 import java.util.List;
 import junitx.framework.ComparableAssert;
 import my.triviagame.dal.IDAL;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 
 /**
  * Unit tests for SinglePlayerGameLogicTest
  */
 public class FakeSinglePlayerGameLogicTest {
     @Test
     public void testMatchTrackToAlbumQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.matchTrackToAlbumQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
         List<Pair<String, String>> matches = new ArrayList<Pair<String, String>>(4);
         for (int i=0; i<4; i++) {
             matches.add(new Pair<String,String>(Integer.toString(i), Integer.toString(i)));
         }
         ComparableAssert.assertEquals(4, ((MatchAnswer)question.correctAnswer).getPairs().size());
         ComparableAssert.assertEquals(4, question.getScore(new MatchAnswer(matches)));
     }
 
     @Test
     public void testMatchAlbumsToYearQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.matchAlbumsToYearQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
         List<Pair<String, String>> matches = new ArrayList<Pair<String, String>>(4);
         for (int i=0; i<4; i++) {
             matches.add(new Pair<String,String>(Integer.toString(i), Integer.toString(i)));
         }
         ComparableAssert.assertEquals(4, ((MatchAnswer)question.correctAnswer).getPairs().size());
         ComparableAssert.assertEquals(4, question.getScore(new MatchAnswer(matches)));
     }
 
     @Test
     public void testMatchTrackToArtistQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.matchTrackToArtistQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
         List<Pair<String, String>> matches = new ArrayList<Pair<String, String>>(4);
         for (int i=0; i<4; i++) {
             matches.add(new Pair<String,String>(Integer.toString(i), Integer.toString(i)));
         }
         ComparableAssert.assertEquals(4, ((MatchAnswer)question.correctAnswer).getPairs().size());
         ComparableAssert.assertEquals(4, question.getScore(new MatchAnswer(matches)));
     }
 
     @Test
     public void testMultipleAlbumHasNumberOfTracksQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.multipleAlbumHasNumberOfTracksQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
     }
 
     @Test
     public void testMultipleArtistWithMostAlbumsQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.multipleArtistWithMostAlbumsQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
     }
 
     @Test
     public void testMultipleCountAlbumsByArtistQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.multipleCountAlbumsByArtistQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
     }
 
     @Test
     public void testMultipleSongNotByArtistQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.multipleSongNotByArtistQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
     }
 
     /*
      * Testing that generateQuesions adds 10 questions types of multipleTrackToArtistQuestionType
      */
     @Test
     public void testMultipleTrackToArtistQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.multipleTrackToArtistQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
     }
 
     @Test
     public void testSortAlbumsByYearQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.sortAlbumsByYearQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
        List<Pair<String, String>> matches = new ArrayList<Pair<String, String>>(4);
         for (int i=0; i<4; i++) {
            matches.add(new Pair<String,String>(Integer.toString(i), Integer.toString(i)));
         }
         ComparableAssert.assertEquals(4, ((OrderedAnswer)question.correctAnswer).getPairs().size());
         int score = question.getScore(new OrderedAnswer(matches));
         ComparableAssert.assertEquals(4, score);
     }
 
     @Test
     public void testSortTracksByLengthQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.sortTracksByLengthQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
        List<Pair<String, String>> matches = new ArrayList<Pair<String, String>>(4);
         for (int i=0; i<4; i++) {
            matches.add(new Pair<String,String>(Integer.toString(i), Integer.toString(i)));
         }
         ComparableAssert.assertEquals(4, ((OrderedAnswer)question.correctAnswer).getPairs().size());
         int score = question.getScore(new OrderedAnswer(matches));
     }
 
     @Test
     public void testSortTrackByOrderInAlbumQuestionType() throws Throwable {
         Player player = new Player("Test", "Male");
         IDAL fakeDAL = new FakeDAL();
         QuestionFactory questionFactory = new QuestionFactory(fakeDAL);
         SinglePlayerGameLogic mockedGame = Mockito.spy(new SinglePlayerGameLogic(player, questionFactory));
         Mockito.doReturn(QuestionFactory.QuestionType.sortTrackByOrderInAlbumQuestionType).when(mockedGame).getRandomQuestionType();
         mockedGame.generateQuestions();
         ComparableAssert.assertGreater(0, mockedGame.getQuestionsCount());
         Question question = mockedGame.getQuestions().next();
        List<Pair<String, String>> matches = new ArrayList<Pair<String, String>>(4);
         for (int i=0; i<4; i++) {
            matches.add(new Pair<String,String>(Integer.toString(i), Integer.toString(i)));
         }
         ComparableAssert.assertEquals(4, ((OrderedAnswer)question.correctAnswer).getPairs().size());
         int score = question.getScore(new OrderedAnswer(matches));
     }
 }
