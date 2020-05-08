 package org.seerc.seqos.dao;
 
 import static org.junit.Assert.*;
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.seerc.seqos.to.ListObservationTO;
 
 public class DummyObservationDAOImplTest {
 
 	@Test
 	public void testCreateObservations() {
 		ObservationDAO dao = new DummyObservationDAOImpl();
 		ListObservationTO status = dao.getObservations();
		Assert.assertEquals(2, status.getObservations().size());
 	}
 
 }
