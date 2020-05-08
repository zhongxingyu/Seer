 package com.thoughtworks.lirenlab.infrastructure.persistence.hibernate;
 
 import com.thoughtworks.lirenlab.domain.model.library.Library;
 import org.hibernate.Query;
 import org.junit.Test;
 
 import java.util.List;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.IsNull.notNullValue;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 public class LibraryRepositoryHibernateTest extends RepositoryTestBase {
 
     private LibraryRepositoryHibernate libraryRepositoryHibernate;
 
     @Override
     public void moreSetUp() {
         libraryRepositoryHibernate = new LibraryRepositoryHibernate();
         libraryRepositoryHibernate.setSessionFactory(sessionFactory);
     }
 
     @Test
     public void should_save_a_library() {
         Library library = new Library(1l, "ChengDu, Sichuan");
         libraryRepositoryHibernate.save(library);
 
         verify(session).saveOrUpdate(library);
     }
 
     @Test
     public void should_find_all_libraries() {
         Query query = mock(Query.class);
         given(session.createQuery(anyString())).willReturn(query);
 
         List<Library> all = libraryRepositoryHibernate.findAll();
 
         assertThat(all, notNullValue());
         verify(query).list();
     }
 
     @Test
     public void should_update_a_library() {
        given(session.load(any(Class.class), anyLong())).willReturn(mock(Library.class));

         libraryRepositoryHibernate.update(new Library(12l, "Chengdu, China"));
 
         verify(session).update(any());
     }
 
     @Test
     public void should_delete_a_library(){
         libraryRepositoryHibernate.delete(12l);
 
         verify(session).delete(any());
     }
 }
