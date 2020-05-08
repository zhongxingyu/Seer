 package com.chinarewards.qqgbvpn.mgmtui.logic.finance.impl;
 
 import java.util.Date;
 import java.util.List;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.common.DateTimeProvider;
 import com.chinarewards.qqgbvpn.domain.Agent;
 import com.chinarewards.qqgbvpn.domain.FinanceReportHistory;
 import com.chinarewards.qqgbvpn.domain.PageInfo;
 import com.chinarewards.qqgbvpn.domain.event.DomainEntity;
 import com.chinarewards.qqgbvpn.domain.event.DomainEvent;
 import com.chinarewards.qqgbvpn.domain.status.FinanceReportHistoryStatus;
 import com.chinarewards.qqgbvpn.logic.journal.JournalLogic;
 import com.chinarewards.qqgbvpn.mgmtui.dao.finance.FinanceDao;
 import com.chinarewards.qqgbvpn.mgmtui.dao.finance.FinanceReportHistoryDao;
 import com.chinarewards.qqgbvpn.mgmtui.logic.finance.FinanceManager;
 import com.chinarewards.qqgbvpn.mgmtui.vo.FinanceReportSearchVO;
 import com.chinarewards.qqgbvpn.mgmtui.vo.FinanceReportVO;
 import com.chinarewards.utils.StringUtil;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.persist.Transactional;
 
 public class FinanceManagerImpl implements FinanceManager {
 	
 	Logger log = LoggerFactory.getLogger(getClass());
 	
 	@Inject
 	private Provider<FinanceDao> dao;
 	
 	@Inject
 	private Provider<FinanceReportHistoryDao> hdao;
 	
 	@Inject
 	private JournalLogic journalLogic;
 	
 	@Inject
 	private DateTimeProvider dtProvider;
 	
 	public List<FinanceReportVO> searchFinanceReport(FinanceReportSearchVO searchVO) {
 		return dao.get().searchFinanceReport(searchVO);
 	}
 	
 	public PageInfo<FinanceReportVO> searchFinanceReport(FinanceReportSearchVO searchVO, PageInfo pageInfo) {
 		return dao.get().searchFinanceReport(searchVO, pageInfo);
 	}
 
 	@Override
 	public PageInfo<FinanceReportHistory> searchFinanceReportHistory(
 			FinanceReportSearchVO searchVO, PageInfo pageInfo) {
 		return hdao.get().searchFinanceReportHistory(searchVO, pageInfo);
 	}
 
 	@Transactional
 	public FinanceReportHistory createFinanceReportHistory(
 			FinanceReportSearchVO searchVO) {
 		Date date = dtProvider.getTime();
 		FinanceReportHistory f = new FinanceReportHistory();
 		if (!StringUtil.isEmptyString(searchVO.getAgentId())) {
 			Agent agent = hdao.get().getAgentById(searchVO.getAgentId());
 			if (agent != null) {
 				f.setAgentId(agent.getId());
 				f.setAgentName(agent.getName());
 			}
 		}
 		f.setStartDate(searchVO.getStartDate());
 		if(searchVO.getEndDate() == null){
 			f.setEndDate(date);
 		}else{
 			f.setEndDate(searchVO.getEndDate());
 		}
 		f.setCreateDate(date);
 		f.setStatus(FinanceReportHistoryStatus.CREATING);
 		f = hdao.get().createFinanceReportHistory(f);
 		
 		// Add journal.
 		ObjectMapper mapper = new ObjectMapper();
 		String eventDetail = null;
 		try {
 			eventDetail = mapper.writeValueAsString(f);
 		} catch (Exception e) {
 			log.error("convert FinanceReportHistory to json error.", e);
 			eventDetail = e.toString();
 		}
 		journalLogic.logEvent(DomainEvent.FINANCE_REPORT_HISTORY_CREATE.toString(),
 				DomainEntity.FINANCE_REPORT_HISTORY.toString(), f.getId(), eventDetail);
 		
 		return f;
 	}
 	
 	public FinanceReportHistory getFinanceReportHistoryById(String id) {
 		FinanceReportHistory f = hdao.get().getFinanceReportHistoryById(id);
 		return f;
 	}
 	
 	@Transactional
 	public FinanceReportHistory saveFinanceReportHistory(FinanceReportHistory financeReportHistory) {
 		FinanceReportHistory f = hdao.get().saveFinanceReportHistory(financeReportHistory);
 
 		ObjectMapper mapper = new ObjectMapper();
 		String eventDetail = null;
 		try {
 			eventDetail = mapper.writeValueAsString(f);
 		} catch (Exception e) {
			e.printStackTrace();
 			eventDetail = e.toString();
 		}
 		journalLogic.logEvent(DomainEvent.FINANCE_REPORT_HISTORY_MODIFY.toString(),
 				DomainEntity.FINANCE_REPORT_HISTORY.toString(), f.getId(), eventDetail);
 		
 		return f;
 	}
 	
 	
 }
