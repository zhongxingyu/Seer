 package org.fiteagle.core.userdatabase;
 
 import static org.junit.Assert.*;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.fiteagle.core.userdatabase.PersonDB.DuplicateUIDException;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class InMemoryPersonDBTest {
 
 	private PersonDB database;
 	
 	private static final ArrayList<String> KEYS = new ArrayList<String>();
 	private static final String KEY = "ssh-rsa AAAAB3NzaC1yc2EACAADAQABAAABAQCybYW812Eb9aTxrXnFgIG7etEijX3/+pWlurrYpvqXi6rl0LZWnotWaC0TeBKWMwDAwPDnSeMxGtYDrZXQJNurrdsmYtzJSL79hhLJqsQCv4s5tK+d/GPRsPSfsGI0A+ckDiQ7yXErUSIgcmGXC4Jo6tuN0QI3x3wIlivDMwkVxZm4m82LwqVECtodnvzbct13a9rIhgjGTRyXXsLVt+X1MB45OlQJ+CWWkaO3emRHDDktZAjkhXNXYKeDtXj4yIhy+jPLTSKiObJnCQD79U+sQEDY+RBPu7Td5GzQx8tFdFAjghZaWgeDiRmpcr8tukR+jG1ynL0zrzumFf4Cg359 mitja@mitja-Precision-WorkStation-370";
 	private static final Person PERSON1 = new Person("nia", "mitja", "nikolaus", KEYS);
 	private static final Person PERSON2 = new Person("hschmidt", "hans", "schmidt", KEYS);
 	private static final Person PERSON3 = new Person("hschmidt", "herbert", "schmidt", KEYS);
 		
 	
 	@Before
 	public void setUp(){
 		this.database = new InMemoryPersonDB();
 	}
	
 	@Test
 	public void testAdd() throws Exception{	
 		assertTrue(database.getSize() == 0);
 		database.add(PERSON1);
 		assertTrue(database.getSize() == 1);
 	}
 	
 	@Test(expected=PersonDB.DuplicateUIDException.class)
 	public void testAddFails() throws Exception{
 		database.add(PERSON2);
 		database.add(PERSON3);
 	}
 		
 	@Test
 	public void testGet() throws Exception{		
 		database.add(PERSON1);	
 		assertEquals(PERSON1, database.get(PERSON1));		
 	}
 	
 	@Test(expected=PersonDB.RecordNotFoundException.class)
 	public void testGetFails() throws Exception{
 		database.add(PERSON1);
 		database.get(PERSON2);
 	}
 
 	@Test(expected=PersonDB.RecordNotFoundException.class)
 	public void testDelete() throws Exception{
 		database.add(PERSON1);
 		database.delete(PERSON1);
 		database.get(PERSON1);
 	}
 		
 	@Test
 	public void testUpdate() throws Exception{
 		database.add(PERSON2);
 		assertEquals("hans", database.get(PERSON2).getFirstName());
 		database.update(PERSON3);
 		assertEquals("herbert", database.get(PERSON3).getFirstName());
 	}
 	
 	@Test(expected=PersonDB.RecordNotFoundException.class)
 	public void testUpdateFails() throws Exception{
 		database.update(PERSON3);
 	}
 	
 	@Test
 	public void testAddKey() throws Exception{
 		database.add(PERSON1);
 		assertEquals(0, database.get(PERSON1).getPublicKeys().size());
 		database.addKey(PERSON1, KEY);
 		assertEquals(KEY, database.get(PERSON1).getPublicKeys().get(0));
 	}
 }
