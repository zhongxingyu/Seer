 package com.morgajel.spoe.model;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.morgajel.spoe.web.EditSnippetForm;
 
 import static org.mockito.Mockito.*;
 
 public class SnippetTest {
     private Account mockAccount;
     private Snippet snippet;
     private Date mockDate;
     private EditSnippetForm mockEditSnippetForm;
     private Long snippetId = 123123123L;
     private Long accountId = 456456L;
     private String title = "One fake Article";
     private String content = "here's a snippet I want you to read.";
 
     @Before
     public void setUp() throws Exception {
         mockAccount = mock(Account.class);
         mockDate = mock(Date.class);
         mockEditSnippetForm = mock(EditSnippetForm.class);
         snippet = new Snippet();
     }
 
     @After
     public void tearDown() throws Exception {
         snippet = null;
         mockAccount = null;
         mockEditSnippetForm = null;
     }
 
     @Test
     public void testSnippetConstructor() {
         Snippet newSnippet = new Snippet();
         assertEquals("", newSnippet.getContent());
     }
 
     @Test
     public void testSnippetConstructorForm() {
         when(mockEditSnippetForm.getContent()).thenReturn(content);
         when(mockEditSnippetForm.getTitle()).thenReturn(title);
         when(mockEditSnippetForm.getSnippetId()).thenReturn(snippetId);
         Snippet newSnippet = new Snippet(mockAccount, mockEditSnippetForm);
         assertEquals(content, newSnippet.getContent());
         assertEquals(mockAccount, newSnippet.getAuthor());
     }
 
     @Test
     public void testSnippetConstructorTitle() {
         Snippet newSnippet = new Snippet(mockAccount, title);
         assertEquals("", newSnippet.getContent());
         assertEquals(title, newSnippet.getTitle());
         assertEquals(mockAccount, newSnippet.getAuthor());
     }
 
     @Test
     public void testGetAndSetAccounts() {
         snippet.setAuthor(mockAccount);
         assertEquals(mockAccount, snippet.getAuthor());
     }
 
     @Test
     public void testGetAndSetSnippetId() {
         assertNull(snippet.getSnippetId());
         snippet.setSnippetId(snippetId);
         assertEquals(snippetId, snippet.getSnippetId());
     }
     @Test
     public void testGetAndSetAccountId() {
         assertNull(snippet.getAccountId());
         snippet.setAccountId(accountId);
         assertEquals(accountId, snippet.getAccountId());
     }
     @Test
     public void testGetAndSetTitle() {
         assertNull( snippet.getTitle());
         snippet.setTitle(title);
         assertEquals(title, snippet.getTitle());
     }
 
     @Test
     public void testGetAndSetContent() {
         assertEquals("", snippet.getContent());
         snippet.setContent(content);
         assertEquals(content, snippet.getContent());
     }
     @Test
     public void testGetAndSetLastModifiedDate() {
         when(mockDate.clone()).thenReturn(mockDate);
         assertNotNull(snippet.getLastModifiedDate());
         snippet.setLastModifiedDate(mockDate);
         assertEquals(mockDate, snippet.getLastModifiedDate());
     }
 
     @Test
     public void testGetAndSetCreationDate() {
         when(mockDate.clone()).thenReturn(mockDate);
         assertNotNull(snippet.getCreationDate());
         snippet.setCreationDate(mockDate);
         assertEquals(mockDate, snippet.getCreationDate());
     }
 
     @Test
     public void testToString() {
         snippet.setTitle(title);
         snippet.setSnippetId(snippetId);
         snippet.setAuthor(mockAccount);
         String toString = "Snippet "
                     + "[ snippetId=" + snippetId
                     + ", title=" + title
                    + ", author=" + "null"
                     +  "]";
         assertEquals(toString, snippet.toString());
     }
 
 }
 
