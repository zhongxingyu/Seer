 package toctep.skynet.backend.test;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import toctep.skynet.backend.dal.domain.Country;
 
 public class CountryTest extends DomainTest{
 
 	@Override
 	public void testCreate() { 
 		Country country = new Country();
 		assertNotNull(country);
 		
 		String code = "NL";
 		country.setCode(code);
 		assertTrue(code.equals(country.getCode()));
 		
 		String text = "Netherlands";
		country.setText(text);
 		assertTrue(text.equals(country.getText()));
 	}
 
 	@Override
 	public void testDelete() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void testInsert() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void testUpdate() {
 		// TODO Auto-generated method stub
 		
 	}	
 }
