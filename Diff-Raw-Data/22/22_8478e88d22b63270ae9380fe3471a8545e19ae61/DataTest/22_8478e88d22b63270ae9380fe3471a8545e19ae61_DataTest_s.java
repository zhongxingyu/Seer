 package suncertify.db;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertThat;
 
 import java.rmi.RemoteException;
 
 import org.junit.Test;
 
 import suncertify.db.io.DBSchema;
 
 public class DataTest {
 
 	private static final int DELETE_TWICE_NO = 27;
 
 	private final DBMain dataService = ServerFactory.createDataService();
 
 	@Test
 	public void testRead() throws RecordNotFoundException, RemoteException {
		dataService.lock(0);
		String[] record = dataService.read(0);
		dataService.unlock(0);
 
 		assertThat(record, is(notNullValue()));
 		assertThat(record.length, is(equalTo(6)));
 		assertThat(record[0], is(not(equalTo(""))));
 		assertThat(record[1], is(not(equalTo(""))));
 		assertThat(record[2], is(not(equalTo(""))));
 		assertThat(Integer.parseInt(record[3]), is(notNullValue()));
 		assertThat(Float.parseFloat(record[4].substring(1)), is(notNullValue()));
 		// assertThat(record[5], is(not(equalTo(""))));
 	}
 
 	@Test(expected = RecordNotFoundException.class)
 	public void testReadException() throws RecordNotFoundException, RemoteException {
 		dataService.lock(500000);
 		dataService.read(500000);
 		dataService.unlock(500000);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testReadNegativeException() throws RecordNotFoundException, RemoteException {
 		dataService.lock(-1);
 		dataService.read(-1);
 		dataService.unlock(-1);
 	}
 
 	@Test
 	public void testUpdate() throws RecordNotFoundException, RemoteException {
 		int recNo = 5;
 
 		String[] newRec = new String[] { "Slappers Plasters", "In your head", "Plastering", "13", "$15.50", "" };
 
 		dataService.lock(recNo);
 		dataService.update(recNo, newRec);
 		dataService.unlock(recNo);
 		dataService.lock(recNo);
 		String[] afterRec = dataService.read(recNo);
 		dataService.unlock(recNo);
 
 		assertThat(afterRec[0], is(equalTo(newRec[0])));
 		assertThat(afterRec[1], is(equalTo(newRec[1])));
 		assertThat(afterRec[2], is(equalTo(newRec[2])));
 		assertThat(afterRec[3], is(equalTo(newRec[3])));
 		assertThat(afterRec[4], is(equalTo(newRec[4])));
 		assertThat(afterRec[5], is(equalTo(newRec[5])));
 	}
 
 	@Test(expected = RecordNotFoundException.class)
 	public void testDelete() throws RecordNotFoundException, RemoteException {
 		dataService.lock(2);
 		dataService.delete(2);
 		dataService.unlock(2);
 		dataService.lock(2);
 		dataService.read(2);
 		dataService.unlock(2);
 	}
 
 	@Test(expected = RecordNotFoundException.class)
 	public void testDeleteTwice() throws RecordNotFoundException, RemoteException {
 		dataService.lock(DELETE_TWICE_NO);
 		dataService.delete(DELETE_TWICE_NO);
 		dataService.unlock(DELETE_TWICE_NO);
 		dataService.lock(DELETE_TWICE_NO);
 		dataService.delete(DELETE_TWICE_NO);
 		dataService.unlock(DELETE_TWICE_NO);
 	}
 
 	@Test
 	public void testFindEmptyCriteria() throws RecordNotFoundException, RemoteException {
 		String[] criteria = new String[] { null, null, null, null, null, null };
 		int[] results = dataService.find(criteria);
 		assertThat(results.length, is(not(0)));
 
 		criteria = new String[] { "", "", "", "", "", "" };
 		results = dataService.find(criteria);
 		assertThat(results.length, is(not(0)));
 	}
 
 	@Test
 	public void testFindCriteriaShort() throws RecordNotFoundException, RemoteException {
 		String[] criteria = new String[DBSchema.NUMBER_OF_FIELDS - 2];
 		int[] results = dataService.find(criteria);
 		assertThat(results.length, is(not(0)));
 	}
 
 	@Test
 	public void testFindNoResults() throws RecordNotFoundException, RemoteException {
 		String[] criteria = new String[DBSchema.NUMBER_OF_FIELDS];
 		criteria[0] = "A fake name that doesn't exist in the database";
 		int[] empty = dataService.find(criteria);
 
 		assertArrayEquals(new int[] {}, empty);
 	}
 
 	@Test
 	public void testFindResults() throws RecordNotFoundException, RemoteException {
 		String[] criteria = new String[DBSchema.NUMBER_OF_FIELDS];
 		criteria[0] = "M";
 		int[] results = dataService.find(criteria);
 		assertThat(results.length, is(not(0)));
 
 		for (int recNo : results) {
 			dataService.lock(recNo);
 			String[] record = dataService.read(recNo);
 			dataService.unlock(recNo);
 			assertThat(record, is(notNullValue()));
 			assertThat(record.length, is(equalTo(6)));
 			assertThat(record[0], is(not(equalTo(""))));
 			assertThat(record[1], is(not(equalTo(""))));
 			assertThat(record[2], is(not(equalTo(""))));
 			assertThat(Integer.parseInt(record[3]), is(notNullValue()));
 			assertThat(Float.parseFloat(record[4].substring(1)), is(notNullValue()));
 		}
 	}
 
 	@Test
 	public void testCreate() throws DuplicateKeyException, RecordNotFoundException, RemoteException {
 		String streetNo = Integer.toString((int) (Math.random() * 100000));
 		String[] data = new String[] { "Jammies", String.format("The Shire %s", streetNo), "Door stop making/fitting", "57", "$0", "" };
 		int newRecNo = dataService.create(data);
 
 		dataService.lock(newRecNo);
 		String[] results = dataService.read(newRecNo);
 		dataService.unlock(newRecNo);
 		for (int i = 0; i < data.length; i++) {
 			assertSame(data[i], results[i]);
 		}
 	}
 
 	@Test(expected = DuplicateKeyException.class)
 	public void testCreateDuplicate() throws DuplicateKeyException, RecordNotFoundException, RemoteException {
 		String[] data = new String[] { "Jammies_DUPPED", "The Shire", "Door stop making/fitting", "57", "$0", "" };
 
 		int recNo = dataService.create(data);
 		dataService.lock(recNo);
 		String[] results = dataService.read(recNo);
 		dataService.unlock(recNo);
 		for (int i = 0; i < data.length; i++) {
 			assertSame(data[i], results[i]);
 		}
 
 		dataService.create(data);
 	}
 
 	@Test
 	public void testLock() throws RemoteException, RecordNotFoundException {
 		assertThat(dataService.isLocked(18), is(equalTo(false)));
 		dataService.lock(18);
 		assertThat(dataService.isLocked(18), is(equalTo(true)));
 		dataService.unlock(18);
 		assertThat(dataService.isLocked(18), is(equalTo(false)));
 	}
 
 	@Test
 	public void testUnlock() throws RemoteException, RecordNotFoundException {
 		assertThat(dataService.isLocked(18), is(equalTo(false)));
 		dataService.lock(18);
 		assertThat(dataService.isLocked(18), is(equalTo(true)));
 		dataService.unlock(18);
 		assertThat(dataService.isLocked(18), is(equalTo(false)));
 	}
 
 	@Test
 	public void testIsLocked() throws RemoteException, RecordNotFoundException {
 		assertThat(dataService.isLocked(18), is(equalTo(false)));
 		dataService.lock(18);
 		assertThat(dataService.isLocked(18), is(equalTo(true)));
 		dataService.unlock(18);
 		assertThat(dataService.isLocked(18), is(equalTo(false)));
 	}
 
 }
