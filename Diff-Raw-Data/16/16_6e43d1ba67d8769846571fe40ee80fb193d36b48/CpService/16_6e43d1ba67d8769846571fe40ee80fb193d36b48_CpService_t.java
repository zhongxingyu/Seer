 package com.bsg.pcms.provision.cp;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Isolation;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.bsg.pcms.dto.CompanyDTO;
 
 @Service
 public class CpService {
 
 	Logger logger = LoggerFactory.getLogger(getClass());
 	private final String COMPANY_MGMTNO = "company_mgmtno";
 	private final String PD_NAME = "pd_name";
 	
 	@Autowired
 	private CpDao cpDao;
 
 	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = false, rollbackFor = { SQLException.class })
 	public int createCp(CpDTOEx cpDTOEx) throws SQLException {
 		
 		//업체 생성
 		int companyResult = cpDao.createCp(cpDTOEx);
 		
 		//담당PD 생성
 		int pdResult = this.createPdList(cpDTOEx);
 		logger.debug("pdResult:" + pdResult);
 		return companyResult;
 	}
 
 	public int getCpCount(CompanyDTO companyDTO) {
 		return cpDao.getCpCount(companyDTO);
 	}
 
 	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = false, rollbackFor = { SQLException.class })
 	public int deleteCp(CompanyDTO companyDTO) throws SQLException {
 		String[] companyDTOList = companyDTO.getStrList().split(",");
 		List<String> list = Arrays.asList(companyDTOList);
 
 		int result = 0;
 		for (String companyDTO_id : list) {
 			companyDTO.setCompany_mgmtno(Integer.valueOf(companyDTO_id));
 			result = cpDao.deleteCp(companyDTO);
 		}
 		return result;
 	}
 	
 	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = false, rollbackFor = { SQLException.class })
 	public int updateCp(CpDTOEx cpDTOEx) throws SQLException {
 		
 		//담당PD 모두 지우고 새로 생성
 		cpDao.deletePdAll(cpDTOEx.getCompany_mgmtno());
 		int pdResult = this.createPdList(cpDTOEx);
 		logger.debug("pdResult:" + pdResult);
 		
 		//업체 정보 수정
 		return cpDao.updateCp(cpDTOEx);
 	}
 	
 	public int updateBank(CompanyDTO companyDTO) {
 		return cpDao.updateBank(companyDTO);
 	}
 
 	public CompanyDTO getCp(CompanyDTO companyDTO) {
 		return cpDao.getCp(companyDTO);
 	}
 
 	public List<CompanyDTO> getCpList() {
 		return this.getCpList(new CompanyDTO());
 	}
 	
 	/** 담당PD 목록을 가져온다
 	 * @return
 	 */
 	public List<CpDTOEx> getPdList(int companyMgmtno) {
 		return cpDao.getPdList(companyMgmtno);
 	}
 	
 	/** CP업체 전체 목록을 가져온다.
 	 * @return List<CompanyDTO>
 	 */
 	public List<CompanyDTO> getCpListAll() {
 		CompanyDTO cd = new CompanyDTO();
 		cd.setPageNum(0); //0을 주면 NO LIMIT 
 		return this.getCpList(cd);
 	}
 
 	public List<CompanyDTO> getCpList(CompanyDTO companyDTODTO) {
 		return cpDao.getCpList(companyDTODTO);
 	}
 	
 	/** 담당PD 리스트를 COMPANY_ADMIN 테이블에 INSERT
 	 * @param cpDTOEx
 	 * @return
 	 */
 	private int createPdList(CpDTOEx cpDTOEx) {
 		
		int pdResult = 0;
		
		List<Map<String, String>> pdList = new ArrayList<Map<String, String>>();
 		for(String pdName : cpDTOEx.getPdNameList()) {
 			Map<String, String> pdMap = new HashMap<String, String>();
 			pdMap.put(COMPANY_MGMTNO, String.valueOf(cpDTOEx.getCompany_mgmtno()));
 			pdMap.put(PD_NAME, pdName);
 			pdList.add(pdMap);
 		}
 		
		if( pdList.size() > 0 ) {
			pdResult = cpDao.createPd(pdList);
			logger.debug("pdResult : {}", pdResult);
		}
		
 		return pdResult;
 	}
 
 }
