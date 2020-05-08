 package models;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.bson.types.ObjectId;
 import org.junit.Test;
 
 import com.google.code.morphia.DatastoreImpl;
 import com.google.code.morphia.mapping.Mapper;
 import com.google.code.morphia.query.FieldEnd;
 import com.google.code.morphia.query.Query;
 import com.google.code.morphia.query.UpdateOperations;
 import com.google.code.morphia.query.UpdateResults;
 
 public class BookDAOImplTest {
 
 	DatastoreImpl ds = mock(DatastoreImpl.class);
 
 	private void setupMocks() {
 		Mapper mapper = mock(Mapper.class);
 		when(ds.getMapper()).thenReturn(mapper);
 		when(mapper.addMappedClass(any(Class.class))).thenReturn(null);
 	}
 
 	@Test
 	public void findsAll() {
 
 		@SuppressWarnings("serial")
		List<Book> fakeList = new ArrayList<Book>() {
			{
				add(new Book("A fake book"));
			}
		};

 		setupMocks();
 
 		@SuppressWarnings("unchecked")
 		Query<Book> query = mock(Query.class);
 		when(ds.find(Book.class)).thenReturn(query);
 		when(query.order(any(String.class))).thenReturn(query);
 		when(query.asList()).thenReturn(fakeList);

 		BookDAOImpl bookDAO = new BookDAOImpl(ds);
 		List<Book> list = bookDAO.findAll();
 		assertThat(list).isEqualTo(fakeList);
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Test
 	public void safeUpdate() {
 		setupMocks();
 		Book originalBook = new Book("Another book");
 		int originalVersion = originalBook.getVersion();

 		Query<Book> query = mock(Query.class);
 		FieldEnd end = mock(FieldEnd.class);
 		when(ds.createQuery(Book.class)).thenReturn(query);
 		when(query.field(any(String.class))).thenReturn(end);
 		when(end.equal(any(ObjectId.class))).thenReturn(query);

 		UpdateOperations<Book> ops = mock(UpdateOperations.class);
 		when(ds.createUpdateOperations(Book.class)).thenReturn(ops);
 		when(ops.set(any(String.class), any(Object.class))).thenReturn(ops);
 		when(ops.inc(any(String.class))).thenReturn(ops);

 		UpdateResults<Book> result = mock(UpdateResults.class);
 		when(ds.update(query, ops)).thenReturn(result);
 		when(result.getUpdatedExisting()).thenReturn(true);

 		BookDAOImpl bookDAO = new BookDAOImpl(ds);
 		Book updatedBook = bookDAO.safeUpdate(originalBook);
 		assertThat(updatedBook.getVersion()).isEqualTo(originalVersion + 1);
 	}
 }
