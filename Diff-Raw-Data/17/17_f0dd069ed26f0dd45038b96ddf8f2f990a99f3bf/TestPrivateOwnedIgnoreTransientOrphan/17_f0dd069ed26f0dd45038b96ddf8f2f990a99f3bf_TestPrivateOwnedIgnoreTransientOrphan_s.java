 package com.avaje.tests.cascade;
 
 import javax.persistence.OptimisticLockException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.avaje.ebean.BaseTestCase;
 import com.avaje.ebean.Ebean;
 import com.avaje.tests.model.basic.TSDetail;
 import com.avaje.tests.model.basic.TSMaster;
 
 public class TestPrivateOwnedIgnoreTransientOrphan extends BaseTestCase {
 	
 	@Test
 	public void test(){
 		
 		/** new object **/
 		TSMaster master0 = new TSMaster();
 		
 		/** recovered after first save **/
 		TSMaster master1 = null;
 		
 		/** recovered after transient child ignored **/
 		TSMaster master2 = null;
 		
 		Ebean.save(master0);
 		
 		master1 = Ebean.find(master0.getClass(), master0.getId());
 		
 		master1.getDetails().add(new TSDetail());
 		master1.getDetails().clear();
 
 		try{
 			Ebean.save(master1);
 		} catch (OptimisticLockException exception) {
 			Assert.fail("Optimistic lock exception wrongly thrown: " + exception.getMessage());
 			return;
 		}
 		
 		master2 = master1 = Ebean.find(master1.getClass(), master1.getId());
 		
 		Assert.assertTrue(master2.getDetails().isEmpty());
 	}
 }
