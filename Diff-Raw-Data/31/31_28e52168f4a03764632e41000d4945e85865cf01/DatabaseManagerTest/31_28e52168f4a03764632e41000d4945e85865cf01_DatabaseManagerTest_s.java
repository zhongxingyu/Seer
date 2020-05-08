 package org.kalibro.core.persistence.database;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import java.util.Arrays;
 
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.KalibroTestCase;
 import org.mockito.InOrder;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(DatabaseManager.class)
 public class DatabaseManagerTest extends KalibroTestCase {
 
 	private EntityManager entityManager;
 	private EntityTransaction transaction;
 
 	private DatabaseManager connectionManager;
 
 	@Before
 	public void setUp() {
 		entityManager = PowerMockito.mock(EntityManager.class);
 		transaction = PowerMockito.mock(EntityTransaction.class);
 		connectionManager = new DatabaseManager(entityManager);
 		PowerMockito.when(entityManager.getTransaction()).thenReturn(transaction);
 		PowerMockito.when(entityManager.merge("unmerged")).thenReturn("merged");
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldCreateQuery() throws Exception {
 		Query<String> query = PowerMockito.mock(Query.class);
 		TypedQuery<String> nativeQuery = PowerMockito.mock(TypedQuery.class);
 		PowerMockito.when(entityManager.createQuery("42", String.class)).thenReturn(nativeQuery);
 		PowerMockito.whenNew(Query.class).withArguments(nativeQuery).thenReturn(query);
 
 		assertSame(query, connectionManager.createQuery("42", String.class));
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldClearEntityManagerWhenCreatingTypedQuery() {
 		connectionManager.createQuery("42", String.class);
 		verify(entityManager).clear();
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldMergeBeforePersist() {
 		connectionManager.persist("unmerged");
 		verify(entityManager).persist("merged");
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldMergeBeforeRemove() {
 		connectionManager.remove("unmerged");
 		verify(entityManager).remove("merged");
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldSaveSingleEntityAsUnitaryList() throws Exception {
 		connectionManager = PowerMockito.spy(connectionManager);
 		connectionManager.save("42");
 		PowerMockito.verifyPrivate(connectionManager).invoke("save", Arrays.asList("42"));
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldSaveList() {
 		connectionManager.save(Arrays.asList("unmerged", "unmerged", "unmerged"));
 
 		InOrder order = inOrder(transaction, entityManager, transaction);
 		order.verify(transaction).begin();
 		order.verify(entityManager, times(3)).persist("merged");
 		order.verify(transaction).commit();
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void testDelete() {
 		connectionManager.delete("unmerged");
 
 		InOrder order = inOrder(transaction, entityManager, transaction);
 		order.verify(transaction).begin();
 		order.verify(entityManager).remove("merged");
 		order.verify(transaction).commit();
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldCloseEntityManagerOnFinalize() throws Throwable {
 		connectionManager.finalize();
 		verify(entityManager).close();
 	}
 }
