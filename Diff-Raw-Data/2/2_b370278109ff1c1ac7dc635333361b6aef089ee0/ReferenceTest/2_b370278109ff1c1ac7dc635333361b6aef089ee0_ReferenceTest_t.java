 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ohtu.domain;
 
 import java.util.UUID;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author annisall
  */
 public class ReferenceTest {
 
     private Reference ref;
     private Book book;
     private Article art;
     private Inproceedings inproc;
 
     @Before
     public void setUp() {
         ref = createReference();
         book = new Book();
         art = new Article();
         inproc = new Inproceedings();
     }
 
     @Test
     public void bookAttributesContainsRightAttributes() {
         book.setAuthor("Anniina");
         book.setTitle("Kokkauskirja");
         book.setPubYear(2001);
 
         assertEquals(book.getAttributes().size(), 5, 0.00001);
         assertTrue(book.getAttributes().containsKey("author"));
         assertTrue(book.getAttributes().containsKey("title"));
         assertTrue(book.getAttributes().containsKey("pubYear"));
         assertTrue(book.getAttributes().containsKey("editor"));
         assertTrue(book.getAttributes().containsKey("publisher"));
 
     }
 
     @Test
     public void atricleAttributesContainsRightAttributes() {
         art.setAuthor("Anniina");
         art.setTitle("Kokkauskirja");
         art.setPubYear(2001);
 
         assertEquals(art.getAttributes().size(), 10, 0.00001);
         assertTrue(art.getAttributes().containsKey("author"));
         assertTrue(art.getAttributes().containsKey("title"));
         assertTrue(art.getAttributes().containsKey("pubYear"));
         assertTrue(art.getAttributes().containsKey("journal"));
         assertTrue(art.getAttributes().containsKey("publisher"));
         assertTrue(art.getAttributes().containsKey("volume"));
         assertTrue(art.getAttributes().containsKey("number"));
         assertTrue(art.getAttributes().containsKey("pubMonth"));
         assertTrue(art.getAttributes().containsKey("pages"));
         assertTrue(art.getAttributes().containsKey("address"));
 
     }
 
     @Test
     public void inprocAttributesContainsRightAttributes() {
         inproc.setAuthor("Anniina");
         inproc.setTitle("Kokkauskirja");
         inproc.setPubYear(2001);
 
         assertEquals(inproc.getAttributes().size(), 6, 0.00001);
         assertTrue(inproc.getAttributes().containsKey("author"));
         assertTrue(inproc.getAttributes().containsKey("title"));
         assertTrue(inproc.getAttributes().containsKey("pubYear"));
        assertTrue(inproc.getAttributes().containsKey("pubMonth"));
         assertTrue(inproc.getAttributes().containsKey("booktitle"));
         assertTrue(inproc.getAttributes().containsKey("organisation"));
 
     }
 
     @Test
     public void setIdChangesIdCorrectly() {
         Reference ref = createReference();
         ref.setId(Long.MIN_VALUE);
         assertTrue(Long.MIN_VALUE==ref.getId());
     }
 
  
 
     private Reference createReference() {           //Luo uuden referencen jolle se antaa testauthorin ja titlen
         Reference ref = new Reference();
         ref.setAuthor("testAuthor " + UUID.randomUUID());
         ref.setTitle("testTitle " + UUID.randomUUID());
         ref.setPubYear(2001);
         return ref;
     }
 }
