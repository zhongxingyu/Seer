 /**
  * 
  */
 package net.frontlinesms.plugins.forms.data.repository.hibernate;
 
 import java.util.List;
 
 import net.frontlinesms.junit.HibernateTestCase;
 import net.frontlinesms.plugins.forms.data.domain.Form;
 import net.frontlinesms.plugins.forms.data.domain.FormResponse;
 import net.frontlinesms.plugins.forms.data.domain.ResponseValue;
 import net.frontlinesms.plugins.forms.data.repository.FormDao;
 import net.frontlinesms.plugins.forms.data.repository.FormResponseDao;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 
 /**
  * Test class for {@link HibernateFormResponseDao}
  * @author Alex Anderson <alex@frontlinesms.com>
  */
 public class HibernateFormResponseDaoTest1 extends HibernateTestCase {
 	
 //> STATIC CONSTANTS
 	
 //> INSTANCE PROPERTIES
 	@Autowired
 	private FormDao formDao;
 	@Autowired
 	private FormResponseDao formResponseDao;
 
 //> TEST METHODS
 	public void testGetFormResponses() {
 		testGetFormResponses(2, 31, 28);
 		testGetFormResponses(5, 47, 38);
 	}
 	
 	private void testGetFormResponses(int formId, int expectedFieldCount, int expectedResponseCount) {
 		Form form = this.formDao.getFromId(formId);
 		// There may be fields without values returned here
		assertFalse("There are no fields attached to this form.", form.getFields().size() == 0);
 		assertTrue(form.getFields().size() >= expectedFieldCount);
 		
 		List<FormResponse> rz = this.formResponseDao.getFormResponses(form, 0, expectedResponseCount * 2);
 		assertEquals(expectedResponseCount, rz.size());
 		for(FormResponse r : rz) {
 			assertEquals(expectedFieldCount, r.getResults().size());
 		}
 	}
 
 //> ACCESSORS
 	/** @param formDao The DAO to use for the test. */
 	@Required
 	public void setFormDao(FormDao formDao) {
 		this.formDao = formDao;
 	}
 
 	/** @param formDao The DAO to use for the test. */
 	@Required
 	public void setFormResponseDao(FormResponseDao formResponseDao) {
 		this.formResponseDao = formResponseDao;
 	}
 }
