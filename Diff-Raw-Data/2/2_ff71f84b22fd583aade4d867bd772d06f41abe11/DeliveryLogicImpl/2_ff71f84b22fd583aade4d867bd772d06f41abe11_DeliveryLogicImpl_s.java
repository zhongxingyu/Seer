 /**
  * 
  */
 package com.chinarewards.qqgbvpn.mgmtui.logic.pos.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.OptimisticLockException;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.common.DateTimeProvider;
 import com.chinarewards.qqgbvpn.domain.PageInfo;
 import com.chinarewards.qqgbvpn.domain.event.DomainEntity;
 import com.chinarewards.qqgbvpn.domain.event.DomainEvent;
 import com.chinarewards.qqgbvpn.domain.status.DeliveryNoteStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosDeliveryStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosInitializationStatus;
 import com.chinarewards.qqgbvpn.logic.journal.JournalLogic;
 import com.chinarewards.qqgbvpn.mgmtui.dao.DeliveryDao;
 import com.chinarewards.qqgbvpn.mgmtui.dao.DeliveryDetailDao;
 import com.chinarewards.qqgbvpn.mgmtui.dao.agent.AgentDao;
 import com.chinarewards.qqgbvpn.mgmtui.dao.pos.PosDao;
 import com.chinarewards.qqgbvpn.mgmtui.exception.AgentNotException;
 import com.chinarewards.qqgbvpn.mgmtui.exception.DeliveryNoteWithNoDetailException;
 import com.chinarewards.qqgbvpn.mgmtui.exception.DeliveryWithWrongStatusException;
 import com.chinarewards.qqgbvpn.mgmtui.exception.PosNotExistException;
 import com.chinarewards.qqgbvpn.mgmtui.exception.PosWithWrongStatusException;
 import com.chinarewards.qqgbvpn.mgmtui.exception.ServiceException;
 import com.chinarewards.qqgbvpn.mgmtui.logic.exception.ParamsException;
 import com.chinarewards.qqgbvpn.mgmtui.logic.pos.DeliveryLogic;
 import com.chinarewards.qqgbvpn.mgmtui.model.agent.AgentVO;
 import com.chinarewards.qqgbvpn.mgmtui.model.delivery.DeliveryNoteDetailVO;
 import com.chinarewards.qqgbvpn.mgmtui.model.delivery.DeliveryNoteVO;
 import com.chinarewards.qqgbvpn.mgmtui.model.delivery.DeliverySearchVO;
 import com.chinarewards.qqgbvpn.mgmtui.model.pos.PosVO;
 import com.chinarewards.qqgbvpn.mgmtui.model.util.PaginationTools;
 import com.chinarewards.qqgbvpn.mgmtui.util.Tools;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.persist.Transactional;
 
 /**
  * @author cream
  * 
  */
 public class DeliveryLogicImpl implements DeliveryLogic {
 
 	Logger log = LoggerFactory.getLogger(DeliveryLogicImpl.class);
 
 	@Inject
 	Provider<DeliveryDao> deliveryDao;
 
 	@Inject
 	Provider<DeliveryDetailDao> deliveryDetailDao;
 
 	@Inject
 	Provider<AgentDao> agentDao;
 
 	// With no transaction.
 	@Inject
 	Provider<PosDao> posDao;
 
 	@Inject
 	DateTimeProvider dtProvider;
 
 	@Inject
 	Provider<JournalLogic> journalLogic;
 
 	protected DeliveryDao getDeliveryDao() {
 		return deliveryDao.get();
 	}
 
 	protected DeliveryDetailDao getDetailDao() {
 		return deliveryDetailDao.get();
 	}
 	
 	protected PosDao getPosDao() {
 		return posDao.get();
 	}
 
 	@Override
 	@Transactional
 	public List<DeliveryNoteVO> fetchAllDelivery() {
 		return getDeliveryDao().fetchAllDelivery();
 	}
 
 	@Override
 	@Transactional
 	public DeliveryNoteVO fetchById(String noteId) {
 		if (Tools.isEmptyString(noteId)) {
 			throw new IllegalArgumentException("delivery id is missing");
 		}
 		return getDeliveryDao().fetchDeliveryById(noteId);
 	}
 
 	@Override
 	@Transactional
 	public PageInfo<DeliveryNoteVO> fetchDeliveryList(PaginationTools pagination) {
 		log.debug(
 				" Process in method fetchDeliveryList, start-{}, limit-{}",
 				new Object[] { pagination.getStartIndex(),
 						pagination.getCountOnEachPage() });
 
 		long count = getDeliveryDao().countDelivertList();
 
 		log.debug("result.count:{}", count);
 
 		List<DeliveryNoteVO> list = getDeliveryDao().fetchDeliveryList(
 				pagination.getStartIndex(), pagination.getCountOnEachPage());
 
 		log.debug("result.list.size:{}", list == null ? "null" : list.size());
 
 		PageInfo<DeliveryNoteVO> result = new PageInfo<DeliveryNoteVO>();
 		result.setItems(list);
 		if (count > Integer.MAX_VALUE) {
 			result.setRecordCount(Integer.MAX_VALUE);
 		}
 		result.setRecordCount((int) count);
 
 		return result;
 	}
 
 	@Override
 	@Transactional
 	public List<DeliveryNoteDetailVO> fetchDetailListByNoteId(String deliveryId) {
 
 		log.debug(" Process in method fetchDeliveryNoteDetailList, note.id:{}",
 				deliveryId);
 		if (Tools.isEmptyString(deliveryId)) {
 			throw new IllegalArgumentException("deliveryNote ID is missing!");
 		}
 
 		List<DeliveryNoteDetailVO> resultList = getDetailDao()
 				.fetchDetailListByNoteId(deliveryId);
 
 		log.debug("result.size:{}",
 				resultList == null ? "null" : resultList.size());
 		return resultList;
 	}
 
 	@Override
 	public PageInfo<DeliveryNoteVO> fetchDeliverys(DeliverySearchVO deliverySearchVO) {
 		PageInfo<DeliveryNoteVO> pageInfo = getDeliveryDao().fetchDeliverys(
 				deliverySearchVO);
 
 		return pageInfo;
 	}
 
 	@Override
 	@Transactional
 	public DeliveryNoteVO createDeliveryNote() {
 		Date now = dtProvider.getTime();
 		// create new delivery note with status DeliveryNoteStatus#DRAFT
 		DeliveryNoteVO dn = new DeliveryNoteVO();
 		dn.setCreateDate(now);
 		dn.setDnNumber(Tools.getOnlyNumber("POSDN-DRAFT"));
 		dn.setStatus(DeliveryNoteStatus.DRAFT.toString());
 
 		return getDeliveryDao().create(dn);
 	}
 
 	@Override
 	@Transactional(rollbackOn = DeliveryWithWrongStatusException.class)
 	public void deleteDeliveryNote(String noteId)
 			throws DeliveryWithWrongStatusException {
 		if (Tools.isEmptyString(noteId)) {
 			throw new IllegalArgumentException("delivery note ID is missing.");
 		}
 		DeliveryNoteVO dn = getDeliveryDao().fetchDeliveryById(noteId);
 		if (!DeliveryNoteStatus.DRAFT.toString().equals(dn.getStatus())) {
 			throw new DeliveryWithWrongStatusException(
 					"delivery should be DRAFT, but now is " + dn.getStatus());
 		}
 		getDeliveryDao().deleteById(noteId);
 	}
 
 	@Override
 	@Transactional(rollbackOn = DeliveryWithWrongStatusException.class)
 	public DeliveryNoteVO associateAgent(String deliveryNoteId, String agentId)
 			throws DeliveryWithWrongStatusException {
 		if (Tools.isEmptyString(deliveryNoteId)) {
 			throw new IllegalArgumentException("Delivery note ID is missing.");
 		}
 
 		DeliveryNoteVO dn = null;
 		try {
 			DeliveryNoteVO note = getDeliveryDao().fetchDeliveryById(
 					deliveryNoteId);
 			if (!DeliveryNoteStatus.DRAFT.toString().equals(note.getStatus())) {
 				throw new DeliveryWithWrongStatusException(
 						"Delivery status must be DRAFT, but now is:"
 								+ note.getStatus());
 			}
 			AgentVO agent = agentDao.get().findById(agentId);
 			note.setAgent(agent);
 			note.setAgentName(agent == null ? null : agent.getName());
 			dn = getDeliveryDao().merge(note);
 		} catch (ServiceException e) {
 			log.error("unknow exception catched!", e);
 		}
 		return dn;
 	}
 
 	@Override
 	@Transactional(rollbackOn = { PosNotExistException.class, PosWithWrongStatusException.class, DeliveryWithWrongStatusException.class })
 	public DeliveryNoteDetailVO appendPosToNote(String deliveryNoteId,
 			String posNum) throws PosNotExistException,
 			PosWithWrongStatusException, DeliveryWithWrongStatusException
 //			,DeliveryDetailExistException 
 			{
 		if (Tools.isEmptyString(deliveryNoteId)) {
 			throw new IllegalArgumentException("Delivery note ID is missing.");
 		}
 		if (Tools.isEmptyString(posNum)) {
 			throw new IllegalArgumentException("POS Number is missing.");
 		}
 		// check delivery note status.
 		DeliveryNoteVO note = getDeliveryDao()
 				.fetchDeliveryById(deliveryNoteId);
 		if (!DeliveryNoteStatus.DRAFT.toString().equals(note.getStatus())) {
 			throw new DeliveryWithWrongStatusException(
 					"Delivery status must be DRAFT, but now is:"
 							+ note.getStatus());
 		}
 		
 		PosVO posVO = null;
 		try {
 			posVO = getPosDao().getPosByPosNum(posNum);
 		} catch (ParamsException e) {
 			throw new PosNotExistException(e);
 		}
 		if (!PosDeliveryStatus.RETURNED.toString().equals(posVO.getDstatus())) {	//=已交付    ，改成 !=已回收
 			throw new PosWithWrongStatusException(
 					"Should be PosDeliveryStatus.RETURNED, but was:"
 							+ posVO.getDstatus());
 		}
 		
 //		posVO.setDstatus(PosDeliveryStatus.LOCKED.toString());	//修改POS机的状态
 //		try {
 //			getPosDao().updatePos(posVO);
 //		} catch (Throwable e) {
 //			throw new PosWithWrongStatusException(e);
 //		}
 		
 		//如果当前交付单 已经添加该POS机
 		DeliveryNoteDetailVO deliveryNoteDetailVO = getDetailDao().fetchByDeliveryIdPosId(deliveryNoteId, posNum);
 		if(deliveryNoteDetailVO != null){
 			throw new PosWithWrongStatusException(
 					"Pos already in this Delivery, POS num:"+posNum+" ,  delivery ID: "+deliveryNoteId+" ");
 		}
 		
 		DeliveryNoteDetailVO detail = getDetailDao().create(deliveryNoteId, posVO);
 		
 		return detail;
 	}
 	
 	@Override
 	@Transactional(rollbackOn = DeliveryWithWrongStatusException.class)
 	public void deletePosFromNote(String deliveryNoteId, String detailId)
 			throws DeliveryWithWrongStatusException {
 		if (Tools.isEmptyString(deliveryNoteId)) {
 			throw new IllegalArgumentException("Delivery note ID is missing.");
 		}
 		if (Tools.isEmptyString(detailId)) {
 			throw new IllegalArgumentException(
 					"Delivery note detail ID is missing.");
 		}
 		// check status
 		DeliveryNoteVO dn = getDeliveryDao().fetchDeliveryById(deliveryNoteId);
 		if (!DeliveryNoteStatus.DRAFT.toString().equals(dn.getStatus())) {
 			throw new IllegalArgumentException(
 					"Delivery note status should be DRAFT, not "
 							+ dn.getStatus());
 		}
 
 		// fetch detail and remove it.
 		getDetailDao().delete(detailId);
 	}
 
 	@Override
 	@Transactional(rollbackOn = DeliveryNoteWithNoDetailException.class)
 	public List<DeliveryNoteDetailVO> getAllDeliveryNoteDetailVOByUnInitPosStatus(String deliveryNoteId)
 			throws DeliveryNoteWithNoDetailException {
 		log.debug("Process in delivery(), noteId:{}", deliveryNoteId);
 		// Check arguments.
 		if (Tools.isEmptyString(deliveryNoteId)) {
 			throw new IllegalArgumentException("Delivery note ID is missing.");
 		}
 
 		List<DeliveryNoteDetailVO> uninitList = new LinkedList<DeliveryNoteDetailVO>();
 
 		// fetch DeliveryNoteDetail list.
 		List<DeliveryNoteDetailVO> detailList = getDetailDao()
 				.fetchDetailListByNoteId(deliveryNoteId);
 
 		if (detailList == null || detailList.isEmpty()) {
 			throw new DeliveryNoteWithNoDetailException();
 		}
 
 		// check POS initialized status.
 		for (DeliveryNoteDetailVO detail : detailList) {
 			if (!detail.getIstatus().equals(
 					PosInitializationStatus.INITED.toString())) {
 				uninitList.add(detail);
 			}
 		}
 
 		return uninitList;
 	}
 
 	@Override
 	@Transactional(rollbackOn = { DeliveryWithWrongStatusException.class, PosWithWrongStatusException.class,
 			DeliveryNoteWithNoDetailException.class, AgentNotException.class,
 			PosNotExistException.class, RuntimeException.class })
 	public void confirmDelivery(String deliveryNoteId)
 			throws DeliveryWithWrongStatusException,
 			PosWithWrongStatusException, DeliveryNoteWithNoDetailException,
 			AgentNotException, PosNotExistException {
 		// check delivery note status - DeliveryNoteStatus#DRAFT
 		DeliveryNoteVO dn = getDeliveryDao().fetchDeliveryById(deliveryNoteId);
 		if (!DeliveryNoteStatus.DRAFT.toString().equals(dn.getStatus())) {
 			throw new DeliveryWithWrongStatusException(
 					"Delivery note status should be DRAFT, not "
 							+ dn.getStatus());
 		}
 
 		Date now = dtProvider.getTime();
 
 		// modify delivery note status - DeliveryNoteStatus#CONFIRMED
 		dn.setStatus(DeliveryNoteStatus.CONFIRMED.toString());
 		dn.setConfirmDate(now);
 		dn.setDnNumber(Tools.getOnlyNumber("POSDN"));
 		getDeliveryDao().merge(dn);
 
 		List<PosVO> posList = getDetailDao().fetchPosByNoteId(dn.getId());
 		List<String> posNums = new LinkedList<String>();
 		List<String> posIds = new LinkedList<String>();
 		
 		
 		Boolean b = false;
 		StringBuffer sbuff = new StringBuffer();
 		for (PosVO pos : posList) {
 			// check POS status.
 			if (!PosDeliveryStatus.RETURNED.toString().equals(pos.getDstatus())) {		//RETURNED -> x
 				b = true;
 				AgentVO agentVO = null;
 				try {
 					agentVO = getPosDao().findAgentFromAssignmentByPosId(pos.getId());
 					
 					if(sbuff.length() > 0){
 						sbuff.append(",");
 					}
 					
 					sbuff.append("[" + "POS:"+pos.getPosId()+" 所在第三方："+agentVO.getName() + "]");
 				} catch (Throwable e) {
 					log.warn(e.getMessage(), e);
 				}
 			}
 
 			posNums.add(pos.getPosId());
 			posIds.add(pos.getId());
 		}
 		if(b){
 			PosWithWrongStatusException ex = new PosWithWrongStatusException();
			ex.setErrorContent("以下POS机不是回收状态： "+sbuff);
 			throw ex;
 		}
 		
 		
 		log.debug("Try to update pos status, posIds:{}", posIds);
 		if (posIds == null || posIds.isEmpty()) {
 			throw new DeliveryNoteWithNoDetailException();
 		}
 		if (dn.getAgent() == null) {
 			throw new AgentNotException();
 		}
 		posDao.get().createPosAssignment(dn.getAgent().getId(), posIds);
 		
 		try{
 			posDao.get().updatePosStatusToWorking(posNums);
 		}catch(OptimisticLockException e){
 			throw new PosWithWrongStatusException(e);
 		}
 
 		// add journalLogic
 		try {
 			ObjectMapper map = new ObjectMapper();
 			String eventDetail;
 			eventDetail = map.writeValueAsString(dn);
 			journalLogic.get().logEvent(
 					DomainEvent.USER_CONFIRMED_DNOTE.toString(),
 					DomainEntity.DELIVERY_NOTE.toString(), deliveryNoteId,
 					eventDetail);
 		} catch (Exception e) {
 			log.error("Error in parse to JSON", e);
 		}
 	}
 	
 	@Override
 	@Transactional(rollbackOn = DeliveryWithWrongStatusException.class)
 	public void printDelivery(String deliveryNoteId)
 			throws DeliveryWithWrongStatusException {
 		// check delivery note status - not be DeliveryNoteStatus#DRAFT
 		DeliveryNoteVO dn = getDeliveryDao().fetchDeliveryById(deliveryNoteId);
 		if (DeliveryNoteStatus.DRAFT.toString().equals(dn.getStatus())) {
 			throw new DeliveryWithWrongStatusException(
 					"Delivery note status should not be DRAFT, it is "
 							+ dn.getStatus() + " now");
 		}
 
 		Date now = dtProvider.getTime();
 
 		// modify delivery note status - DeliveryNoteStatus#PRINTED
 		dn.setStatus(DeliveryNoteStatus.PRINTED.toString());
 		dn.setPrintDate(now);
 		getDeliveryDao().merge(dn);
 
 		// add journalLogic
 		try {
 			ObjectMapper map = new ObjectMapper();
 			String eventDetail;
 			eventDetail = map.writeValueAsString(dn);
 			journalLogic.get().logEvent(
 					DomainEvent.USER_PRINTED_DNOTE.toString(),
 					DomainEntity.DELIVERY_NOTE.toString(), deliveryNoteId,
 					eventDetail);
 		} catch (Exception e) {
 			log.error("Error in parse to JSON", e);
 		}
 	}
 
 }
