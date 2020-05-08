 package org.devilry.core.session.dao;
 
 import org.devilry.core.dao.DeliveryCandidateImpl;
 import org.devilry.core.dao.DeliveryImpl;
 import org.devilry.core.daointerfaces.DeliveryCandidateRemote;
 import org.devilry.core.daointerfaces.DeliveryRemote;
 import org.junit.Before;
 import org.junit.After;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import javax.naming.NamingException;
 
 
 public class DeliveryImplTest extends AbstractDeliveryDaoTst {
 	
 	DeliveryRemote delivery;
 	DeliveryCandidateRemote deliveryCandidate;
 	long deliveryId;
 
 	@Before
 	public void setUp() throws NamingException {
 		super.setUp();
 		delivery = getRemoteBean(DeliveryImpl.class);
 		deliveryCandidate = getRemoteBean(DeliveryCandidateImpl.class);
 		deliveryId = delivery.create(assignmentId);
 	}
 
 
 	@Test
 	public void remove() {
 		delivery.remove(deliveryId);
 		assertFalse(delivery.exists(deliveryId));
 
 		long id1 = delivery.create(assignmentId);
 		long id2 = delivery.create(assignmentId);
		periodNode.remove(uioId);
 		assertFalse(assignmentNode.exists(assignmentId));
 		assertFalse(delivery.exists(id1));
 		assertFalse(delivery.exists(id2));
 	}
 
 	@Test
 	public void getAssignment() {
 		assertEquals(assignmentId, delivery.getAssignment(deliveryId));
 	}
 	
 	@Test
 	public void getStatus() {
 		// TODO
 	}
 
 	@Test
 	public void setStatus() {
 		// TODO
 	}
 	
 
 	@Test
 	public void getDeliveryCandidates() {
 		long id = deliveryCandidate.create(deliveryId);
 		assertEquals(1, delivery.getDeliveryCandidates(deliveryId).size());
 		assertEquals(id, (long) delivery.getDeliveryCandidates(deliveryId).get(0));
 
 		deliveryCandidate.create(deliveryId);
 		deliveryCandidate.create(deliveryId);
 		assertEquals(3, delivery.getDeliveryCandidates(deliveryId).size());
 	}
 
 	@Test
 	public void getLastDeliveryCandidate() {
 		// TODO
 	}
 	
 	@Test
 	public void getLastValidDeliveryCandidate() {
 		// TODO
 	}
 
 	
 	@Test
 	public void getCorrectors() {
 		// TODO
 	}
 	
 	@Test
 	public void getStudents() {
 		// TODO
 	}
 
 	@Test
 	public void exists() {
 		assertTrue(delivery.exists(deliveryId));
 		assertFalse(delivery.exists(deliveryId + 1));
 	}
 }
