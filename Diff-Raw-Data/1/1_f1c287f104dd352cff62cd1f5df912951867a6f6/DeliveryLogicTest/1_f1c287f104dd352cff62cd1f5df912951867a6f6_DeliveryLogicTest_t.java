 /**
  * 
  */
 package com.chinarewards.qqgbvpn.mgmtui.logic.pos.impl;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import com.chinarewards.qqgbvpn.domain.Agent;
 import com.chinarewards.qqgbvpn.domain.DeliveryNoteDetail;
 import com.chinarewards.qqgbvpn.domain.Pos;
 import com.chinarewards.qqgbvpn.domain.status.DeliveryNoteStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosDeliveryStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosInitializationStatus;
 import com.chinarewards.qqgbvpn.mgmtui.dao.DeliveryDao;
 import com.chinarewards.qqgbvpn.mgmtui.exception.DeliveryNoteWithNoDetailException;
 import com.chinarewards.qqgbvpn.mgmtui.exception.DeliveryWithWrongStatusException;
 import com.chinarewards.qqgbvpn.mgmtui.exception.PosNotExistException;
 import com.chinarewards.qqgbvpn.mgmtui.logic.pos.DeliveryLogic;
 import com.chinarewards.qqgbvpn.mgmtui.model.agent.AgentVO;
 import com.chinarewards.qqgbvpn.mgmtui.model.delivery.DeliveryNoteDetailVO;
 import com.chinarewards.qqgbvpn.mgmtui.model.delivery.DeliveryNoteVO;
 import com.chinarewards.qqgbvpn.mgmtui.util.JPATestCase;
 import com.chinarewards.qqgbvpn.mgmtui.util.Tools;
 
 /**
  * @author cream
  * 
  */
 public class DeliveryLogicTest extends JPATestCase {
 
 	protected DeliveryLogic getLogic() {
 		return injector.getInstance(DeliveryLogic.class);
 	}
 
 	@Test
 	public void testCreateDeliveryNote() {
 		DeliveryNoteVO note = getLogic().createDeliveryNote();
 		assertNotNull(note);
 		assertFalse(Tools.isEmptyString(note.getId()));
 		assertNull(note.getConfirmDate());
 		assertNotNull(note.getCreateDate());
 		assertFalse(Tools.isEmptyString(note.getDnNumber()));
 		assertNull(note.getPrintDate());
 		assertEquals(DeliveryNoteStatus.DRAFT.toString(), note.getStatus());
 	}
 
 	@Test
 	public void testAssociateAgent() throws Exception {
 		Agent agent = new Agent();
 		agent.setEmail("miao@mail.com");
 		agent.setName("MLGM");
 		em.persist(agent);
 		em.flush();
 
 		DeliveryNoteVO note = getLogic().createDeliveryNote();
 		DeliveryNoteVO newNote = getLogic().associateAgent(note.getId(),
 				agent.getId());
 		assertNotNull(newNote);
 
 		AgentVO avo = newNote.getAgent();
 		assertNotNull(avo);
 		assertEquals("miao@mail.com", avo.getEmail());
 		assertEquals("MLGM", avo.getName());
 
 		assertEquals("MLGM", newNote.getAgentName());
 		assertEquals(DeliveryNoteStatus.DRAFT.toString(), newNote.getStatus());
 	}
 
 	@Test
 	public void testAppendPosToNote_PosNotExist() {
 
 		DeliveryNoteVO note = getLogic().createDeliveryNote();
 
 		try {
 			getLogic().appendPosToNote(note.getId(), "MiaoLeGeMi");
 			fail("should not reach here.");
 		} catch (Exception e) {
 		}
 
 	}
 
 	@Test
 	public void testAppendPosToNote_Success() {
 		Pos pos = new Pos();
 		pos.setDstatus(PosDeliveryStatus.RETURNED);
 		pos.setIstatus(PosInitializationStatus.INITED);
 		pos.setPosId("MiaoLeGeMi");
 		pos.setModel("miao");
 		pos.setSimPhoneNo("simSimSim");
 		pos.setSn("SnSnSnSn");
 		em.persist(pos);
 		em.flush();
 
 		DeliveryNoteVO note = getLogic().createDeliveryNote();
 
 		try {
 			DeliveryNoteDetailVO detail = getLogic().appendPosToNote(
 					note.getId(), "MiaoLeGeMi");
 			assertNotNull(detail.getDn());
 			assertFalse(Tools.isEmptyString(detail.getDn().getId()));
 			assertFalse(Tools.isEmptyString(detail.getId()));
 			assertEquals("miao", detail.getModel());
 			assertEquals("MiaoLeGeMi", detail.getPosId());
 			assertEquals("simSimSim", detail.getSimPhoneNo());
 			assertEquals("SnSnSnSn", detail.getSn());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail("should not reach here.");
 		}
 
 	}
 
 	@Test
 	public void testDeletePosFromNote() throws Exception {
 		Pos pos = new Pos();
 		pos.setIstatus(PosInitializationStatus.INITED);
 		pos.setPosId("MiaoLeGeMi");
 		pos.setModel("miao");
 		pos.setSimPhoneNo("simSimSim");
 		pos.setSn("SnSnSnSn");
		pos.setDstatus(PosDeliveryStatus.RETURNED);
 		em.persist(pos);
 		em.flush();
 
 		DeliveryNoteVO note = getLogic().createDeliveryNote();
 		DeliveryNoteDetailVO detail = getLogic().appendPosToNote(note.getId(),
 				"MiaoLeGeMi");
 
 		assertFalse(Tools.isEmptyString(detail.getId()));
 
 		getLogic().deletePosFromNote(note.getId(), detail.getId());
 
 		DeliveryNoteDetail dnd = em.find(DeliveryNoteDetail.class,
 				detail.getId());
 		assertNull(dnd);
 
 	}
 
 	@Test
 	public void testDelivery_NoDetail() throws PosNotExistException, Exception {
 		Agent agent = new Agent();
 		agent.setEmail("miao@mail.com");
 		agent.setName("MLGM");
 		em.persist(agent);
 		em.flush();
 
 		DeliveryNoteVO note = getLogic().createDeliveryNote();
 		getLogic().associateAgent(note.getId(), agent.getId());
 
 		try {
 			getLogic().getAllDeliveryNoteDetailVOByUnInitPosStatus(note.getId());
 			fail("should not reach here.");
 		} catch (DeliveryNoteWithNoDetailException e) {
 		}
 
 	}
 
 	@Test
 	public void testDelivery() throws Exception {
 		Agent agent = new Agent();
 		agent.setEmail("miao@mail.com");
 		agent.setName("MLGM");
 		em.persist(agent);
 		em.flush();
 
 		Pos pos = new Pos();
 		pos.setDstatus(PosDeliveryStatus.RETURNED);
 		pos.setIstatus(PosInitializationStatus.UNINITED);
 		pos.setPosId("MiaoLeGeMi");
 		pos.setModel("miao");
 		pos.setSimPhoneNo("simSimSim");
 		pos.setSn("SnSnSnSn");
 		em.persist(pos);
 		em.flush();
 
 		DeliveryNoteVO note = getLogic().createDeliveryNote();
 		getLogic().associateAgent(note.getId(), agent.getId());
 		getLogic().appendPosToNote(note.getId(), "MiaoLeGeMi");
 
 		List<DeliveryNoteDetailVO> detailList = getLogic().getAllDeliveryNoteDetailVOByUnInitPosStatus(
 				note.getId());
 		assertNotNull(detailList);
 		assertFalse(detailList.isEmpty());
 	}
 
 	@Test
 	public void testConfirmDelivery() throws Exception {
 		Agent agent = new Agent();
 		agent.setEmail("miao@mail.com");
 		agent.setName("MLGM");
 		em.persist(agent);
 		em.flush();
 
 		Pos pos = new Pos();
 		pos.setIstatus(PosInitializationStatus.INITED);
 		pos.setDstatus(PosDeliveryStatus.RETURNED);
 		pos.setPosId("MiaoLeGeMi");
 		pos.setModel("miao");
 		pos.setSimPhoneNo("simSimSim");
 		pos.setSn("SnSnSnSn");
 		em.persist(pos);
 		em.flush();
 
 		DeliveryNoteVO note = getLogic().createDeliveryNote();
 		getLogic().associateAgent(note.getId(), agent.getId());
 		getLogic().appendPosToNote(note.getId(), "MiaoLeGeMi");
 
 		getLogic().confirmDelivery(note.getId());
 
 		DeliveryDao dao = injector.getInstance(DeliveryDao.class);
 		DeliveryNoteVO dn = dao.fetchDeliveryById(note.getId());
 		assertNotNull(dn);
 		assertEquals(DeliveryNoteStatus.CONFIRMED.toString(), dn.getStatus());
 
 		try {
 			getLogic().confirmDelivery(note.getId());
 			fail("should not reach here.");
 		} catch (DeliveryWithWrongStatusException e) {
 		}
 	}
 
 	@Test
 	public void testPrintDelivery() throws Exception {
 		Agent agent = new Agent();
 		agent.setEmail("miao@mail.com");
 		agent.setName("MLGM");
 		em.persist(agent);
 		em.flush();
 
 		Pos pos = new Pos();
 		pos.setIstatus(PosInitializationStatus.INITED);
 		pos.setDstatus(PosDeliveryStatus.RETURNED);
 		pos.setPosId("MiaoLeGeMi");
 		pos.setModel("miao");
 		pos.setSimPhoneNo("simSimSim");
 		pos.setSn("SnSnSnSn");
 		em.persist(pos);
 		em.flush();
 
 		DeliveryNoteVO note = getLogic().createDeliveryNote();
 		getLogic().associateAgent(note.getId(), agent.getId());
 		getLogic().appendPosToNote(note.getId(), "MiaoLeGeMi");
 
 		getLogic().confirmDelivery(note.getId());
 
 		DeliveryDao dao = injector.getInstance(DeliveryDao.class);
 		DeliveryNoteVO dn = dao.fetchDeliveryById(note.getId());
 		assertNotNull(dn);
 		assertEquals(DeliveryNoteStatus.CONFIRMED.toString(), dn.getStatus());
 
 		getLogic().printDelivery(note.getId());
 		DeliveryNoteVO pdn1 = dao.fetchDeliveryById(note.getId());
 		assertNotNull(pdn1);
 		assertEquals(DeliveryNoteStatus.PRINTED.toString(), pdn1.getStatus());
 
 		getLogic().printDelivery(note.getId());
 		DeliveryNoteVO pdn2 = dao.fetchDeliveryById(note.getId());
 		assertNotNull(pdn2);
 		assertEquals(DeliveryNoteStatus.PRINTED.toString(), pdn2.getStatus());
 
 	}
 
 }
