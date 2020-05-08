 package com.bsg.pcms.stats;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.bsg.pcms.stats.dto.StatisticsDTO;
 import com.bsg.pcms.stats.svc.StatisticsService;
 import com.bsg.pcms.utility.BigstarConstant;
 import com.bsg.pcms.utility.JsonResponseMaker;
 
 @Controller
 @RequestMapping( value = "statistics" )
 public class StatisticsAjaxController {
 
 	private Logger logger = LoggerFactory.getLogger(StatisticsAjaxController.class);
 	
 	@Autowired
 	BigstarConstant bigstarConstant; 
 	
 	@Autowired
 	StatisticsService statService;
 	
 	@Autowired
 	private JsonResponseMaker _jsonResponseMaker;
 	
 	/**
 	 * 
 	 * param 
 	 * searchEndDate ex) : "2013-01" 
 	 * 
 	 * response
 	 * {
 			"pieGraph": [{
 				"saleCompany": "지순컴페니",
 				"saleCount": 235802
 			}],
 			"code": 200,
 			"msg": "OK"
 		}
 	 * @return
 	 */
 	@RequestMapping( value = "sale-company/pieGraph.ajax", produces = "application/json;charset=UTF-8")
 	public @ResponseBody String pieGraph(StatisticsDTO param) {
 		
 		List<Map> companyList = statService.saleCompanysPieGraph(param);
 		
 		String jsonString = _jsonResponseMaker.generateMapList("pieGraph", companyList);
 		
 		return jsonString;
 	}
 	
 	/**
 	 * param 
 	 * searchEndDate ex) : "2013-01" 
 	 * response
 	 * {
 		"pieGraph": [{
 				"saleCount": 131653,
 				"contentName": "Alphabet book"
 			}],
 			"code": 200,
 			"msg": "OK"
 		}
 	 * @param param
 	 * @return
 	 */
 	@RequestMapping( value = "product/pieGraph.ajax", produces = "application/json;charset=UTF-8")
 	public @ResponseBody String productPieGraph(StatisticsDTO param) {
 		
 		List<Map> companyList = statService.productsPieGraph(param);
 		
 		String jsonString = _jsonResponseMaker.generateMapList("pieGraph", companyList);
 		
 		return jsonString;
 	}
 	
 	/**
 	 * param
 	 *  searchQuery ex : "솔맷";
 		sortingType ex : "1";
 		searchStrDate ex : "2013-06"
 		searchEndDate ex : "2013-07"
 	   
 	 * response
 		{
 			"code": 200,
 			"msg": "OK",
 			"tableList": [{
 				"sale_end_date": "2013-07-31",
 				"company_name": "솔맷컴패니",
 				"sale_str_date": "2013-07-01",
 				"total_sale_count": 18257,
 				"sale_device": "ios",
 				"total_sale_price": 27092000
 			}]
 		}
 	 * 
 	 * @param param
 	 * @return
 	 */
 	@RequestMapping( value = "sale-company/list.ajax", produces = "application/json;charset=UTF-8")
 	public @ResponseBody String list(StatisticsDTO param) {
 		
 		List<Map> companyList = statService.saleCompanysForMap(param);
 		
 		String jsonString = _jsonResponseMaker.generateMapList("tableList", companyList);
 		
 		return jsonString;
 	}
 	/**
 	 * param
 	 *  searchQuery ex : "솜사탕";
 		sortingType ex : "1";
 		searchStrDate ex : "2013-06"
 		searchEndDate ex : "2013-07"
 		
 	 * response
 		{
 			"code": 200,
 			"msg": "OK",
 			"tableList": [{
 				"sale_end_date": "2013-07-31",
 				"sale_str_date": "2013-07-01",
 				"total_sale_count": 131653,
 				"contents_cd": "CP04_SE33P0001_PB",
 				"sale_device": "ios",
 				"total_sale_price": 133837000,
 				"contens_name": "Alphabet book"
 			}]
 		}
 	 * 
 	 * @param param
 	 * @return
 	 */
 	@RequestMapping( value = "product/list.ajax", produces = "application/json;charset=UTF-8")
 	public @ResponseBody String productList(StatisticsDTO param) {
 		
 		List<Map> companyList = statService.productsForMap(param);
 		
 		String jsonString = _jsonResponseMaker.generateMapList("tableList", companyList);
 		
 		return jsonString;
 	}

	/**
	 * @param param
	 * @return
	 */
	@RequestMapping( value = "product/columnGraph.ajax", produces = "application/json;charset=UTF-8")
	public @ResponseBody String productColumnGraph(StatisticsDTO param) {
		
		//List<Map> companyList = statService.productsForMap(param);
		
		String jsonString = "";//_jsonResponseMaker.generateMapList("tableList", companyList);
		
		return jsonString;
	}
 	
 	/**
 	 * param
 	 * searchEndDate ex) : "2013"
 	 * 
 	 * response
 	 * {
 		"saleCompanyCount": 1,
 		"lineGraph": [{
 			"saleCompanyName": "솔맷컴패니",
 			"monthCount": [0, 0, 0, 0, 0, 441, 17836, 0, 0, 0, 0,0]
 		}],
 		"code": 200,
 		"msg": "OK"
 	}
 	 * @param param
 	 * @return
 	 */
 	@RequestMapping( value = "sale-company/lineGraph.ajax", produces = "application/json;charset=UTF-8")
 	public @ResponseBody String lineGraph(String searchDate) {
 		
 		 List<StatisticsDTO> companyList = statService.saleCompanysLineGraph(searchDate);
 		
 		String jsonString = _jsonResponseMaker.generateLineGraph("lineGraph", companyList);
 		
 		return jsonString;
 	}
 	
 	
 	/**
 	 * param
 	 * searchEndDate ex) : "2013"
 	 * 
 	 * response
 	 * {
 			"lineGraph": [{
 				"contentName": "Alphabet book",
 				"monthCount": [
 					0,
 					0,
 					0,
 					0.0,
 					0,
 					30,
 					131180,
 					0.0,
 					0,
 					0,
 					0,
 					0]
 			}],
 			"contentCount": 10,
 			"code": 200,
 			"msg": "OK"
 		}
 	 * @param searchDate
 	 * @return
 	 */
 	@RequestMapping( value = "product/lineGraph.ajax", produces = "application/json;charset=UTF-8")
 	public @ResponseBody String productLineGraph(String searchDate) {
 		
 		List<StatisticsDTO> companyList = statService.productsLineGraphMonthCount(searchDate);
 		
 		String jsonString = _jsonResponseMaker.generateProductLineGraph("lineGraph", companyList);
 		
 		return jsonString;
 	}
 	
 }
