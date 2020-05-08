 package org.imirsel.nema.dao;
 
 import org.imirsel.nema.dao.BaseDaoTestCase;
 import org.imirsel.nema.dao.PreferenceValueDao;
 import org.imirsel.nema.model.PreferenceValue;
 import org.springframework.dao.DataAccessException;
 
 
 public class PreferenceValueDaoTest extends BaseDaoTestCase {
 	
 	private PreferenceValueDao dao;
 	
 	public void setPreferenceValueDao(PreferenceValueDao pdao){
 		dao = pdao;
 	}
 	
 	
 	public void testInvalidPreferenceValue(){
 		 try {
			 dao.get(2l);
 			 fail("should not reach here..");
 		 }catch(DataAccessException ex){
 			 
 		 }
 	}
 	
 	public void testGetPreferenceValue(){
 		PreferenceValue pval=(PreferenceValue)dao.get(1l);
 		assertNotNull(pval);
 	}
 	
 	public void testAddAndRemovePreferenceValue(){
 		PreferenceValue pval = new PreferenceValue("favoriteFlow","Extraction Flows");
 		dao.save(pval);
 		PreferenceValue pval1=dao.getPreferenceValue("favoriteFlow");
 		assertTrue(pval1.getValue().equals(pval.getValue()));
 		
 		dao.removePreferenceWithKey("favoriteFlow");
 		
 		
 		flush();
 		
 		try{
 		dao.getPreferenceValue("favoriteFlow");
 		}catch(DataAccessException ex){
 			log.debug("Expected exception: "+ ex.getMessage());
 			assertNotNull(ex);
 		}
 		
 		
 	}
 	
 
 }
