 /**
  * 
  */
 package com.bsg.pcms.stats;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.bsg.pcms.stats.dto.StatisticsDTO;
 
 /**
  * @author Administrator
  *
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"classpath:servlet-contextForTest.xml"})
 public class StatisticsAjaxControllerTest {
 	
 	private Logger logger = LoggerFactory.getLogger(getClass());
 	
 	@Autowired
 	StatisticsAjaxController statisticsAjaxController;
 
 	
 	private StatisticsDTO param = null;
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		param = new StatisticsDTO();
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testPieGraphForJsonThisMonth() {
 		param.setContentViewCount(20);
 		String result = statisticsAjaxController.pieGraph(param);
 		assertThat(result, is(notNullValue()));
 		logger.info("pie json : {}", result);
 	}
 	
 	@Test
 	public void testProductPieGraphForJsonThisMonth() {
 		param.setContentViewCount(20);
		String result = statisticsAjaxController.productPieGraph(null);
 		assertThat(result, is(notNullValue()));
 		logger.info("{}", result);
 	}
 	
 	@Test
 	public void testPieGraphForJsonTargetMonth() {
 		param.setSearchEndDate("2013-01");
 		String result = statisticsAjaxController.pieGraph(param);
 		assertThat(result, is(notNullValue()));
 		logger.info("pie json : {}", result);
 	}
 	@Test
 	public void testLineGraphForJson() {
 		String result = statisticsAjaxController.lineGraph(null);
 		assertThat(result, is(notNullValue()));
 		logger.info("pie json : {}", result);
 	}
 	@Test
 	public void testProductLineGraphForJson() {
 		String result = statisticsAjaxController.productLineGraph(null);
 		assertThat(result, is(notNullValue()));
 		logger.info("pie json : {}", result);
 	}
 	
 	/**
 	 * {
 		"code": 200,
 		"msg": "OK",
 		"tableList": [
 			{
 			"sale_end_date": "2013-07-31",
 			"company_name": "성환컴페니",
 			"sale_str_date": "2013-07-01",
 			"total_sale_count": 147591,
 			"sale_device": "android",
 			"total_sale_price": 147591000}
 		]
 	}
 	 */
 	@Test
 	public void testList() {
 		String result = statisticsAjaxController.list(null);
 		assertThat(result, is(notNullValue()));
 		logger.info("pie json : {}", result);
 	}
 	
 	@Test
 	public void testSaleCompanySearchByOrderBy() {
 		param.setSortingType("1");
 		String result = statisticsAjaxController.list(param);
 		assertThat(result, is(notNullValue()));
 		logger.info("{}", result);
 	}
 	
 	@Test
 	public void testProductList() {
 		param.setSortingType("1");
 		String result = statisticsAjaxController.productList(param);
 		assertThat(result, is(notNullValue()));
 		logger.info("{}", result);
 	}
 	@Test
 	public void testStickGraph() {
 		param.setSortingType("1");
 		String result = statisticsAjaxController.stickGraph(param);
 		assertThat(result, is(notNullValue()));
 		logger.info("{}", result);
 	}
 
 }
