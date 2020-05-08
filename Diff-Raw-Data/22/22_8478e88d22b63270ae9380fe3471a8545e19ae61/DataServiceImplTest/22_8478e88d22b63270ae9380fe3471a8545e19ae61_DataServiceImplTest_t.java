 package suncertify.db;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.rmi.RemoteException;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import suncertify.server.DataService;
 import suncertify.server.DataServiceImpl;
 import suncertify.shared.Contractor;
 
 public class DataServiceImplTest {
 
 	private DataService dataService;
 
	private static final int CREATE_REC_NO = 0;
 	private static final int UPDATE_REC_NO = 14;
 	private static final int READ_REC_NO = 15;
 	private static final int DELETE_REC_NO = 16;
 
 	@Before
 	public void setup() {
 		this.dataService = new DataServiceImpl();
 	}
 
 	@Test
 	public void testRead() throws RemoteException, RecordNotFoundException {
 		Contractor record = this.dataService.read(READ_REC_NO);
 		assertThat(record.getName(), is(equalTo("Bitter Homes & Gardens")));
 		assertThat(record.getLocation(), is(equalTo("EmeraldCity")));
 		assertThat(record.getSpecialites(), is(equalTo("Heating, Plumbing, Painting")));
 		assertThat(record.getNoStaff(), is(equalTo("6")));
 		assertThat(record.getRate(), is(equalTo("$90.00")));
 		assertThat(record.getCustomerId(), is(equalTo("")));
 
 	}
 
 	@Test
 	public void testUpdate() throws RemoteException, RecordNotFoundException {
 		Contractor origRecord = this.dataService.read(UPDATE_REC_NO);
 		assertThat(origRecord.getName(), is(equalTo("Hamner & Tong")));
 		assertThat(origRecord.getLocation(), is(equalTo("EmeraldCity")));
 		assertThat(origRecord.getSpecialites(), is(equalTo("Roofing, Electrical")));
 		assertThat(origRecord.getNoStaff(), is(equalTo("7")));
 		assertThat(origRecord.getRate(), is(equalTo("$45.00")));
 		assertThat(origRecord.getCustomerId(), is(equalTo("")));
 
 		Contractor newRecordData = new Contractor(UPDATE_REC_NO, "Belter", "Greenland", "Green house building", "32", "$53.50", "12");
 		this.dataService.update(newRecordData);
 		Contractor updatedRecord = this.dataService.read(UPDATE_REC_NO);
 		for (int i = 0; i < newRecordData.toArray().length; i++) {
 			assertThat(updatedRecord.toArray()[i], is(equalTo(newRecordData.toArray()[i])));
 		}
 
 		this.dataService.update(origRecord);
 	}
 
 	@Test(expected = RecordNotFoundException.class)
 	public void testDelete() throws RemoteException, RecordNotFoundException, DuplicateKeyException {
 		Contractor record = this.dataService.read(DELETE_REC_NO);
 		this.dataService.delete(record);
 		this.dataService.read(DELETE_REC_NO);
 	}
 
 	@Test
 	public void testFind() throws RemoteException, RecordNotFoundException {
 		String[] findCriteria = new String[] { "Hamner" };
 		List<Contractor> records = this.dataService.find(findCriteria, true);
 		assertThat(records.size(), is(equalTo(0)));
 
 		findCriteria = new String[] { "Buonarotti & Company" };
 		records = this.dataService.find(findCriteria, true);
 		assertThat(records.size(), is(equalTo(3)));
 		assertThat(records.get(0).getRecordId(), is(equalTo(21)));
 		assertThat(records.get(1).getRecordId(), is(equalTo(24)));
 		assertThat(records.get(2).getRecordId(), is(equalTo(26)));
 	}
 
 	@Test
	public void testCreate() throws RemoteException, DuplicateKeyException, RecordNotFoundException {
 		String[] newRecord = new String[] { "Hello Kitty", "Japan", "Bags", "12345", "$99.00", "" };
		this.dataService.delete(new Contractor(CREATE_REC_NO, "Dogs With Tools", "Smallville", "Roofing", "7", "$35.00", ""));
 		int newRecordNum = this.dataService.create(newRecord);
 
		assertThat(newRecordNum, is(equalTo(CREATE_REC_NO)));
 	}
 
 	@Test(expected = DuplicateKeyException.class)
 	public void testCreateDup() throws RemoteException, DuplicateKeyException {
 		String[] newRecord = new String[] { "DUPPLICATE", "DUPPLICATE", "DUPPLICATE", "DUPPLICATE", "DUPPLICATE", "DUPPLICATE" };
 		this.dataService.create(newRecord);
 		this.dataService.create(newRecord);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testCreateEmpty() throws RemoteException, DuplicateKeyException {
 		String[] newRecord = new String[] {};
 		this.dataService.create(newRecord);
 	}
 }
