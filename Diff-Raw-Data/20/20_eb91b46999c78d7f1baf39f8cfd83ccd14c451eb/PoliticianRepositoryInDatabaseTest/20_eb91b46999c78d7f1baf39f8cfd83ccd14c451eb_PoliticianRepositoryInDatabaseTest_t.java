 package be.noselus.repository;
 
 import static org.junit.Assert.assertEquals;
 import be.noselus.model.Person;
 
 import org.junit.Assert;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 
 import java.util.List;
 
 
 public class PoliticianRepositoryInDatabaseTest {
 
 	List<Person> store;
 	
 	@Before
 	public void init() {
 		store = new PoliticianRepositoryInDatabase().getPoliticians();
 	}
 	
 	@Test
 	public void size() {
 		Assert.assertTrue(store.size() > 0);
 	}
 	
     @Test
    @Ignore("TODO with ORDER")
     public void data() {
         final Person person = store.get(1);
         assertEquals("COLLIGNON Christophe", person.full_name);
         assertEquals("PS", person.party.trim());
         assertEquals("rue du Marché, 45", person.address);
         assertEquals("4500", person.postal_code.trim());
         assertEquals("HUY", person.town);
         assertEquals("085 31 82 00", person.phone.trim());
         assertEquals("085 31 80 99", person.fax.trim());
         assertEquals("contact@christophe-collignon.be", person.email);
         assertEquals("www.christophe-collignon.be", person.site);
         assertEquals(50.5181351, person.latitude, 0.01);
         assertEquals(5.2443019, person.longitude, 0.01);
     }
 }
