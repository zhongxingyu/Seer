 package org.devilry.core.session;
 
 import java.util.List;
 import javax.naming.NamingException;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class DeliveryBeanTest {
 
 	DeliveryCandidateRemote bean;
 
 	@Before
 	public void setUp() throws NamingException, Exception {
 		bean = Helpers.loadBean(DeliveryCandidateRemote.class);
		System.out.println("hei");
 		bean.init(0);
 	}
 
 	@Test
 	public void addFile() {
//		bean.addFile("test.txt");
//		List<Long> fileIds = bean.getFileIds();
//		assertEquals(1, fileIds.size());
 	}
 }
