 package org.george.service;
 
 import org.george.dao.DailyVerseJavaDAO;
 import org.george.domain.Word;
 import org.george.enums.Book;
 import org.george.factory.DAOFactory;
 import org.junit.Before;
 import org.junit.Test;
 
 import static junit.framework.Assert.*;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: george
  * Date: 9/8/13
  * Time: 8:32 PM
  * To change this template use File | Settings | File Templates.
  */
 public class WordDAOTest {
 
     private DailyVerseJavaDAO mockedDao = mock(DailyVerseJavaDAO.class);
     private DAOFactory factory = DAOFactory.getInstance();
     private Word validWord = new Word();
 
     @Before
     public void setUp() {
         validWord.setBook(Book.Luke);
         validWord.setReference("1:1");
         validWord.setVersicle("Test");
 
         if (Boolean.getBoolean(System.getProperty("mock"))) {
             factory.setWordDAO(mockedDao);
         }
     }
 
     @Test
     public void testGetDailyVerseDay2() {
        when(mockedDao.getVerse(anyInt())).thenReturn(validWord);
         Word word = factory.getWordDAO().getVerse(2);
         assertNotNull(word);
         assertEquals(word.getBook(), Book.Luke);
         assertEquals(word.getReference(), "1:1");
         assertEquals(word.getVersicle(), "Test");
     }
 
 }
