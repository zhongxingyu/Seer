 package com.bsg.pcms.stats.svc;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.bsg.pcms.sale.company.dto.CompanyContractDTOEx;
 import com.bsg.pcms.stats.dao.StatisticsDao;
 import com.bsg.pcms.stats.dto.StatisticsDTO;
 
 @Service
 public class StatisticsService {
 	
 	@Autowired
 	StatisticsDao statDao;
 	
 	public List<StatisticsDTO> saleCompanys(StatisticsDTO requestParam) {
 		if(requestParam == null){
 			requestParam = new StatisticsDTO();
 		}
 		
 		if(StringUtils.isEmpty(requestParam.getSortingType())){
 			requestParam.setSortingType("2");
 		}
 		requestParam.setStartRownum((requestParam.getPageNum() - 1) * requestParam.getPerPage());
 		
 		return statDao.saleCompanys(requestParam);
 	}
 	
 	public List<Map> saleCompanysForMap(StatisticsDTO requestParam) {
 		
 		List<StatisticsDTO> saleCompanyTable = statDao.saleCompanys(requestParam);
 		
 		List<Map> tableListMap = new ArrayList<Map>();
 		
 		for(StatisticsDTO staDTO : saleCompanyTable){
 			Map<String, Object> tableMap = new HashMap<String, Object>();
 			tableMap.put("company_name", staDTO.getCompany_name());
 			tableMap.put("total_sale_count", staDTO.getTotal_sale_count());
 			tableMap.put("total_sale_price", staDTO.getTotal_sale_price());
 			tableMap.put("sale_device", staDTO.getSale_device());
 			tableMap.put("sale_str_date", staDTO.getSale_str_date());
 			tableMap.put("sale_end_date", staDTO.getSale_end_date());
 			tableListMap.add(tableMap);
 		}
 		
 		return tableListMap;
 	}
 
 	public List<StatisticsDTO> saleCompanysLineGraph(String searchYear){
 		
 		if(StringUtils.isBlank(searchYear)){
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
 			searchYear = sdf.format(System.currentTimeMillis());
 		}
 		
 		List<StatisticsDTO> companyList = statDao.saleCompanysLineGraph();
 		for(StatisticsDTO companyMgmtno : companyList){
 			companyMgmtno.setSearchEndDate(searchYear);
 			companyMgmtno.setMonthSaleCount(statDao.saleCompanysLineGraphMonthCount(companyMgmtno));
 		}
  		return companyList;
 	}
 
 	public List<Map> saleCompanysPieGraph(StatisticsDTO param) {
 		
 		List<StatisticsDTO> pieGraphResult = statDao.saleCompanysPieGraph(param);
 		List<Map> pieGraphGForMap = new ArrayList<Map>();
 		int etcSaleCount = 0;
 		String etcCompanyName = "ETC";
 		Map<String, Object> etcCompany = new HashMap<String, Object>();
 		
 		for(int x=0; x<pieGraphResult.size();x++){
 			if(x > param.getContentViewCount()){
 				etcSaleCount += pieGraphResult.get(x).getTotal_sale_count();
 			}else{
 				Map<String, Object> pieMap = new HashMap<String, Object>();
 				pieMap.put("saleCompany", pieGraphResult.get(x).getCompany_name());
 				pieMap.put("saleValue", pieGraphResult.get(x).getTotal_sale_price());
 				pieGraphGForMap.add(pieMap);
 			}
 		}
 		
 		if(etcSaleCount > 0){
 			etcCompany.put("saleCompany", etcCompanyName);
 			etcCompany.put("saleCount", etcSaleCount);
 			pieGraphGForMap.add(etcCompany);
 		}
 		
 		return pieGraphGForMap;
 	}
 	
 	public List<StatisticsDTO> products(StatisticsDTO param) {
 		if(param == null){
 			param = new StatisticsDTO();
 		}
 		
 		if(StringUtils.isBlank(param.getSortingType())){
 			param.setSortingType("2");
 		}
 		
 		param.setStartRownum((param.getPageNum() - 1) * param.getPerPage());
 		
 		return statDao.products(param);
 	}
 	
 	public List<StatisticsDTO> productsLineGraphMonthCount(String searchYear){
 		
 		if(StringUtils.isBlank(searchYear)){
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
 			searchYear = sdf.format(System.currentTimeMillis());
 		}
 		
 		List<StatisticsDTO> companyList = statDao.productsLineGraph();
 		for(StatisticsDTO companyMgmtno : companyList){
 			companyMgmtno.setSearchEndDate(searchYear);
 			companyMgmtno.setMonthSaleCount(statDao.productsLineGraphMonthCount(companyMgmtno));
 			
 		}
  		return companyList;
 	}
 	
 	public List<Map> productsPieGraph(StatisticsDTO param) {
 		
 		List<StatisticsDTO> pieGraphResult = statDao.productsPieGraph(param);
 		
 		int etcSalePrice = 0;
 		String etcCompanyName = "ETC";
 		Map<String, Object> etcCompany = new HashMap<String, Object>();
 		
 		List<Map> pieGraphGForMap = new ArrayList<Map>();
 		for(int x=0; x<pieGraphResult.size();x++){
 			if(x > param.getContentViewCount()){
 				etcSalePrice += pieGraphResult.get(x).getTotal_sale_price();
 			}else{
 				Map<String, Object> pieMap = new HashMap<String, Object>();
 				pieMap.put("contentName", pieGraphResult.get(x).getContents_name());
 				pieMap.put("saleValue", pieGraphResult.get(x).getTotal_sale_price());
 				pieGraphGForMap.add(pieMap);
 			}
 		}
 		if(etcSalePrice > 0){
 			etcCompany.put("contentName", etcCompanyName);
 			etcCompany.put("saleValue", etcSalePrice);
 		}
		pieGraphGForMap.add(etcCompany);
 		return pieGraphGForMap;
 		
 	}
 
 	public List<Map> productsForMap(StatisticsDTO param) {
 		
 		if(param == null){
 			param = new StatisticsDTO();
 		}
 		param.setStartRownum((param.getPageNum() - 1) * param.getPerPage());
 		
 		List<StatisticsDTO> saleCompanyTable = statDao.products(param);
 		List<Map> tableListMap = new ArrayList<Map>();
 		
 		for(StatisticsDTO staDTO : saleCompanyTable){
 			Map<String, Object> tableMap = new HashMap<String, Object>();
 			tableMap.put("contens_name", staDTO.getContents_name());
 			tableMap.put("contents_cd", staDTO.getContents_cd());
 			tableMap.put("total_sale_count", staDTO.getTotal_sale_count());
 			tableMap.put("total_sale_price", staDTO.getTotal_sale_price());
 			tableMap.put("sale_device", staDTO.getSale_device());
 			tableMap.put("sale_str_date", staDTO.getSale_str_date());
 			tableMap.put("sale_end_date", staDTO.getSale_end_date());
 			tableListMap.add(tableMap);
 		}
 		return tableListMap;
 	}
 
 	public int saleCompanysCount(StatisticsDTO requestParam) {
 		return statDao.saleCompanysCount(requestParam);
 	}
 
 	public int productsCount(StatisticsDTO requestParam) {
 		return statDao.productsCount(requestParam);
 	}
 
 	public List<Map> saleCompanysStickGraph(StatisticsDTO param) {
 		return statDao.saleCompanysStickGraph(param);
 	}
 
 	
 
 }
