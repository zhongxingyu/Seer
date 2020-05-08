 package com.freeroom.persistence.proxy;
 
 import com.freeroom.persistence.beans.Book;
 import net.sf.cglib.proxy.Factory;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.sql.SQLException;
 
 import static com.freeroom.persistence.DBFixture.getDbProperties;
 import static com.freeroom.persistence.DBFixture.prepareDB;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class CharonTest
 {
     private static Hades hades;
 
     @Before
     public void setUp() throws SQLException
     {
         prepareDB();
         hades = new Hades(getDbProperties());
     }
 
     @Test
     public void should_get_absent_given_object_is_not_loaded()
     {
         final Book book = (Book)hades.create(Book.class, 1L, 1);
         final Book detachedBook = (Book)((Charon)((Factory)book).getCallback(0)).detach();
 
         assertThat(detachedBook, is(nullValue()));
     }
 
     @Test
     public void should_get_detached_object_given_no_relations()
     {
         final Book book = (Book)hades.create(Book.class, 1L, 1);
         book.getName();
         final Book detachedBook = (Book)((Charon)((Factory)book).getCallback(0)).detach();
 
         assertThat(detachedBook.getName(), is("JBoss Seam"));
         assertThat(detachedBook.getPrice(), is(18.39));
     }
 
 
     @Test
     public void should_get_detached_object_with_ONE_TO_ONE_relation()
     {
         final Book book = (Book)hades.create(Book.class, 1L, 1);
         book.getName();
        book.getPublisher().getName();
         final Book detachedBook = (Book)((Charon)((Factory)book).getCallback(0)).detach();
 
         assertThat(detachedBook.getName(), is("JBoss Seam"));
         assertThat(detachedBook.getPublisher().getName(), is("O Reilly"));
     }
 
     @Test
     public void should_get_detached_object_with_ONE_TO_MANY_relation()
     {
         final Book book = (Book)hades.create(Book.class, 1L, 1);
         book.getName();
         book.getOrders().get(0);
         final Book detachedBook = (Book)((Charon)((Factory)book).getCallback(0)).detach();
 
         assertThat(detachedBook.getName(), is("JBoss Seam"));
         assertThat(detachedBook.getOrders().get(0).getMemo(), is("Deliver at work time"));
     }
 }
