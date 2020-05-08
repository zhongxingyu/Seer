 package com.bsg.pcms.provision.contract;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.bsg.pcms.dto.ContractContentsGroupDTO;
 import com.bsg.pcms.dto.ContractDetailDTO;
 import com.bsg.pcms.dto.SeriesDTO;
 import com.bsg.pcms.provision.content.ContentDTOEx;
 import com.bsg.pcms.provision.content.svc.ContentService;
 import com.bsg.pcms.provision.contract.ContractDTOEx;
 import com.bsg.pcms.provision.contract.ContractDao;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"classpath:servlet-contextForTest.xml"})
 public class ContractDaoTest {
 
 	private Logger logger = LoggerFactory.getLogger(ContractDaoTest.class);
 
 	@Autowired
 	ContractDao contractDao;
 	
 	@Autowired
 	ContentService contentService;
 	
 //	@Ignore
 	@Test
 	public void testCreateContract() {
 		
 		int companyMgmtno = 10; //솔맷컴패니
 		
 		ContractDTOEx ctd = new ContractDTOEx();
 		ctd.setCompany_mgmtno(companyMgmtno);
		ctd.setSale_price(7777777.0);
 		ctd.setLicense_cd("1");
 		ctd.setLicense_cd_detail("2");
 		ctd.setEtc("etc");
 		ctd.setStr_date(this.getSQLDate());
 		ctd.setEnd_date(this.getSQLDate());
 		
 		int result = contractDao.createContract(ctd);
 		
 		assertThat(result, is(1));
 		int contractMgmtno = ctd.getContract_mgmtno();
 		logger.info("ctd.getContract_mgmtno() {}", contractMgmtno);
 		
 		//시리즈 관리번호로 컨텐츠 코드 조회 후
 		ContentDTOEx cd = new ContentDTOEx();
 		cd.setSeries_mgmtno(46);
 		List<ContentDTOEx> contentList = contentService.getContentCodeListBySeriesMgmtno(cd);
 		
 		//계약그룹 테이블에 삽입
 		List<ContractContentsGroupDTO> ccgList = new ArrayList<ContractContentsGroupDTO>();
 		for(ContentDTOEx cde : contentList) {
 			
 			ContractContentsGroupDTO ccg = new ContractContentsGroupDTO();
 			ccg.setContract_mgmtno(contractMgmtno);
 			ccg.setContents_cd(cde.getContents_cd());
 			ccg.setCate_id(cde.getCate_id());
 			ccg.setSeries_mgmtno(cde.getSeries_mgmtno());
 			
 			ccgList.add(ccg);
 		}
 		logger.info("{}", contractDao.createContractContentsGroup(ccgList));
 		
 		//출판형태 삽입 로직
 		List<ContractDetailDTO> cddList = new ArrayList<ContractDetailDTO>();
 		ContractDetailDTO cdd = new ContractDetailDTO();
 		cdd.setContract_mgmtno(contractMgmtno);
 		cdd.setSale_type("ebook");
 		
 		cddList.add(cdd);
 		logger.info("{}", contractDao.createContractDetail(cddList));
 		
 		
 	}
 	
 	@Test
 	public void getContract() {
 		
 		ContractDTOEx cde = new ContractDTOEx();
 		cde.setContract_mgmtno(38);
 		ContractDTOEx resultInfo = contractDao.getContract(cde);
 		logger.info("{}", resultInfo);
 		assertNotNull(resultInfo);
 		
 	}
 	
 	@Test
 	public void getContractList() {
 		
 		ContractDTOEx cde = new ContractDTOEx();
 		List<ContractDTOEx> resultInfo = contractDao.getContractList(cde);
 		logger.info("{}", resultInfo);
 		assertNotNull(resultInfo);
 		assertThat(resultInfo.size(), is(not(0)));
 		
 	}
 	
 	@Test
 	public void getContractDetailList() {
 		
 		ContractDTOEx cdd = new ContractDTOEx();
 		cdd.setContract_mgmtno(21);
 		List<ContractDetailDTO> resultInfo = contractDao.getContractDetailList(cdd);
 		logger.info("{}", resultInfo);
 		assertNotNull(resultInfo);
 		assertThat(resultInfo.size(), is(not(0)));
 		
 	}
 	
 	public java.sql.Date getSQLDate(){
 		
 		java.util.Date utilDate = new java.util.Date(); // your util date
 		java.util.Calendar cal = Calendar.getInstance();
 		cal.setTime(utilDate);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);    
 		java.sql.Date sqlDate = new java.sql.Date(cal.getTime().getTime()); // your sql date
 		
 		logger.info("utilDate:" + utilDate);
 		logger.info("sqlDate:" + sqlDate);
 		
 		return sqlDate;
 	}
 
 }
