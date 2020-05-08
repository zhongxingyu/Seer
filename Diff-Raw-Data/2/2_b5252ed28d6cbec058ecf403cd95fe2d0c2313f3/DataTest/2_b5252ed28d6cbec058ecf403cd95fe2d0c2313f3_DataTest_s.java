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
 
	private final DBMain dataService = new Data("db-2x2.db");
 
 	@Test
 	public void testRead() throws RecordNotFoundException, RemoteException {
 		String[] record = dataService.read(0);
 
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
 		dataService.read(500000);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testReadNegativeException() throws RecordNotFoundException, RemoteException {
 		dataService.read(-1);
 	}
 
 	@Test
 	public void testUpdate() throws RecordNotFoundException, RemoteException {
 		int recNo = 5;
 
 		String[] newRec = new String[] { "Maggi's Gears", "Crazy town", "Cooking, Managing", "4", "$8.65", "00447799" };
 
 		dataService.update(recNo, newRec);
 		String[] afterRec = dataService.read(recNo);
 
 		assertThat(afterRec[0], is(equalTo(newRec[0])));
 		assertThat(afterRec[1], is(equalTo(newRec[1])));
 		assertThat(afterRec[2], is(equalTo(newRec[2])));
 		assertThat(afterRec[3], is(equalTo(newRec[3])));
 		assertThat(afterRec[4], is(equalTo(newRec[4])));
 		assertThat(afterRec[5], is(equalTo(newRec[5])));
 	}
 
 	@Test(expected = RecordNotFoundException.class)
 	public void testDelete() throws RecordNotFoundException, RemoteException {
 		dataService.delete(2);
 		dataService.read(2);
 	}
 
 	@Test(expected = RecordNotFoundException.class)
 	public void testDeleteTwice() throws RecordNotFoundException, RemoteException {
 		dataService.delete(2);
 		dataService.delete(2);
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
 			String[] record = dataService.read(recNo);
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
 
 		String[] results = dataService.read(newRecNo);
 		for (int i = 0; i < data.length; i++) {
 			assertSame(data[i], results[i]);
 		}
 	}
 
 	@Test(expected = DuplicateKeyException.class)
 	public void testCreateDuplicate() throws DuplicateKeyException, RecordNotFoundException, RemoteException {
 		String[] data = new String[] { "Jammies_DUPPED", "The Shire", "Door stop making/fitting", "57", "$0", "" };
 
 		int recNo = dataService.create(data);
 		String[] results = dataService.read(recNo);
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
