 package com.orangeleap.tangerine.test.service.impl;
 
 import org.apache.commons.logging.Log;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.testng.annotations.Test;
 
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.service.exception.ConstituentValidationException;
import com.orangeleap.tangerine.service.exception.DuplicateConstituentException;
 import com.orangeleap.tangerine.test.BaseTest;
import com.orangeleap.tangerine.util.OLLogger;
 
 public class ConstituentServiceTest extends BaseTest {
 	   protected final Log logger = OLLogger.getLog(getClass());
 
 
 	    @Autowired
 	    private ConstituentService constituentService;
 	    
 	    @Test
 	    public void testDuplicateConstituent() throws Exception {
 	    	Constituent p = constituentService.createDefaultConstituent();
 	    	p.setFirstName("Pablo");
 	    	p.setLastName("Picasso");
 	    	p.setConstituentType("individual");
 	    
 	    	try {
 	    		constituentService.maintainConstituent(p);
	    	} 
	    	catch (DuplicateConstituentException ex) {
	    		assert true;
	    		return;
	    	}
	    	catch (ConstituentValidationException ex) {
 	    		assert true;
 	    		return;
 	    	}
 	    	assert false;
 	    }
 	    
 	    @Test
 	    public void testByPassDuplicateConstituent() throws Exception {
 	    	Constituent p = constituentService.createDefaultConstituent();
 	    	p.setFirstName("Pablo");
 	    	p.setLastName("Picasso");
 	    	p.setConstituentType("individual");
 	    	p.setByPassDuplicateDetection(true);
 	    	
 	    	try {
 	    		constituentService.maintainConstituent(p);
	    	} 
	    	catch (DuplicateConstituentException ex) {
	    		assert false;
	    		return;
	    	}
	    	catch (ConstituentValidationException ex) {
 	    		assert false;
 	    		return;
 	    	}
 	    	assert true;	    	
 	    }
 }
