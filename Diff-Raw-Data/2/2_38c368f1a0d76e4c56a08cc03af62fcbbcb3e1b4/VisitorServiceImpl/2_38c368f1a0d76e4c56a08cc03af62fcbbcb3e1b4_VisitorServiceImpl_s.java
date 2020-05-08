 package egovframework.com.guruguru.dashboard.visit.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.google.common.base.Strings;
 
 import egovframework.com.guruguru.system.dao.CommonDao;
 import egovframework.com.guruguru.system.util.DateUtils;
 import egovframework.com.guruguru.system.util.NumberUtils;
 import egovframework.rte.fdl.cmmn.AbstractServiceImpl;
 
 @Service
 public class VisitorServiceImpl extends AbstractServiceImpl implements VisitorService {
 
 	@Autowired
 	private CommonDao commonDao;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<Map<String, Object>> retrieveVistCountInfo(Map param) {
 		List<Map<String, Object>> visitCountList = commonDao.selectList("visitor.selectVisitCountInfo", param);
 		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
 		
 		for (Map<String, Object> visitMap : visitCountList) {
 			Map<String, Object> map = new HashMap<String, Object>();
 			
 			map.put(String.valueOf(visitMap.get("period")), visitMap.get("cnt"));
 			
 			resultList.add(map);
 		}
 		
 		return resultList;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Map<String, Object>> retrieveAreaCountInfo(Map param) {
 		List<Map<String, Object>> areaCountList = commonDao.selectList("visitor.selectAreaCountInfo", param);
 		Map<String, Object> countMap = commonDao.selectObject("visitor.selectCountInfo", param);
 		Map<String, Object> lastCountMap = retrieveAreaLastCountInfo(param);
 		
 		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
 		
 		int sum = 0;
 		int value = 0;
 		
 		int periodIndex = 0;
 		int periodLength = getPeriodCount(param);
 		
 		int[] period = new int[periodLength];
 		
 		String prevArea = "";
 		String tempArea = "";
 		
 		int totalCount = (Integer) countMap.get("cnt");
 		
 		for (int i = 0; i < areaCountList.size(); i++) {
 			Map<String, Object> areaMap = areaCountList.get(i);
 			
 			tempArea = (String) areaMap.get("area");
 			
 			if (i == 0) prevArea = tempArea;
 			
 			if (!tempArea.equals(prevArea)) {
 				result.add(processResultMap(prevArea, sum, totalCount, period, lastCountMap));
 			
 				prevArea = tempArea;
 				
 				period = new int[periodLength];
 				sum = 0;
 			}
 			
 			periodIndex = Integer.parseInt((String) areaMap.get("period"));
 			value = (Integer) areaMap.get("cnt");
 			
 			period[periodIndex - 1] = value;
 			sum += value;
 			
 			if (i == areaCountList.size() - 1) {				
 				result.add(processResultMap(tempArea, sum, totalCount, period, lastCountMap));
 			}
 		}
 		
 		return result;
 	}
 	
 	private Map<String, Object> processResultMap(String area, int sum, int totalCount, int[] period, Map<String, Object> lastCountMap) {
 		int lastCount = 0;
 		
 		if (lastCountMap != null) {
			lastCount = (lastCountMap.size() == 0) ? 0 : (Integer) lastCountMap.get(area);
 		}
 		
 		Map<String, Object> tempMap = new HashMap<String, Object>();
 		
 		tempMap.put("location", area);
 		tempMap.put("sum", sum);
 		tempMap.put("percent", NumberUtils.convertToDecimal(((double) sum / (double) totalCount) * 100));
 		tempMap.put("period", period);
 		tempMap.put("change", (sum - lastCount));
 		
 		return tempMap;
 	}
 	
 	private int getPeriodCount(Map<String, Object> param) {
 		String day = (String) param.get("dd");
 		String month = (String) param.get("mm");
 		String year = (String) param.get("yyyy");
 		
 		if (!Strings.isNullOrEmpty(day)) {
 			return 24;
 		} else if (!Strings.isNullOrEmpty(month)) {
 			return DateUtils.getDayOfMonth(Integer.parseInt(year), Integer.parseInt(month));
 		} else {
 			return 12;
 		}
 	}
 	
 	private Map<String, Object> retrieveAreaLastCountInfo(Map<String, Object> param) {
 		String day = (String) param.get("dd");
 		String month = (String) param.get("mm");
 		String year = (String) param.get("yyyy");
 		
 		Map<String, Object> args = new HashMap<String, Object>();
 		
 		if (!Strings.isNullOrEmpty(day)) {
 			String yesterday = DateUtils.getYesterDay(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
 			
 			args.put("yyyy", yesterday.substring(0, 4));
 			args.put("mm", yesterday.substring(4, 6));
 			args.put("dd", yesterday.substring(6));
 			
 		} else if (!Strings.isNullOrEmpty(month)) {
 			String lastMonth = DateUtils.getLastMonth(Integer.parseInt(year), Integer.parseInt(month));
 			
 			args.put("yyyy", lastMonth.substring(0, 4));
 			args.put("mm", lastMonth.substring(4));
 			
 		} else {
 			String lastYear = DateUtils.getLastYear(Integer.parseInt(year));
 			
 			args.put("yyyy", lastYear);
 		}
 		
 		List<Map<String, Object>> areaLastCountList = commonDao.selectList("visitor.selectAreaLastCountInfo", args);
 		Map<String, Object> resultMap = new HashMap<String, Object>();
 		
 		for (Map<String, Object> areaMap : areaLastCountList) {
 			resultMap.put(String.valueOf(areaMap.get("area")), areaMap.get("cnt"));
 		}
 		
 		return resultMap;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public Map<String, Object> retrieveAgeCountInfo(Map param) {
 		return commonDao.selectObject("visitor.selectAgeCountInfo", param);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public Map<String, Object> retrieveGenderCountInfo(Map param) {
 		List<Map<String, Object>> genderCountList = commonDao.selectList("visitor.selectGenderCntInfo", param);
 		List<Map<String, Object>> genderUniqueCountList = commonDao.selectList("visitor.selectGenderUniqueCntInfo", param);
 		
 		Map<String, Object> resultMap = new HashMap<String, Object>();
 		
 		for (Map<String, Object> genderMap : genderCountList) {
 			if (genderMap.containsValue("Y")) {
 				resultMap.put("womenCount", genderMap.get("cnt"));
 			} else {
 				resultMap.put("menCount", genderMap.get("cnt"));
 			}
 		}
 		
 		for (Map<String, Object> genderUniqueMap : genderUniqueCountList) {
 			if (genderUniqueMap.containsValue("Y")) {
 				resultMap.put("womenUniqueCount", genderUniqueMap.get("cnt"));
 			} else {
 				resultMap.put("menUniqueCount", genderUniqueMap.get("cnt"));
 			}
 		}
 		
 		return resultMap;	
 	}
 	
 }
