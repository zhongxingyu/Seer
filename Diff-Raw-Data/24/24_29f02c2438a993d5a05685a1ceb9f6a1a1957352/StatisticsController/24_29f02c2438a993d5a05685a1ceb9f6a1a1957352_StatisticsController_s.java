 package com.bsg.pcms.stats;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.bsg.pcms.utility.BigstarConstant;
 
 @Controller
 @RequestMapping( value = "statistics" )
 public class StatisticsController {
 
 	private Logger logger = LoggerFactory.getLogger(StatisticsController.class);
 	
 	@Autowired
 	BigstarConstant bigstarConstant; 
 	
 	/** 판매처 통계 대쉬보드
 	 * @return
 	 */
 	@RequestMapping( value = "sale-company/dashboard.do", method = RequestMethod.GET )
 	public ModelAndView saleCompanyDashboard() {
 		
 		ModelAndView mav = new ModelAndView();
<<<<<<< HEAD
		mav.setViewName("statistics");
 		mav.addObject("navSeq", bigstarConstant.HEADER_STATS);
 		mav.addObject("leftMenuSeq", bigstarConstant.LEFT_STATISTICS_SALE_COMPANY);
=======
		mav.setViewName("statistics-sale-company-dashboard");
		mav.addObject("navSeq", bigstarConstant.getHEADER_STATS());
		mav.addObject("leftMenuSeq", bigstarConstant.getLEFT_STATISTICS_SALE_COMPANY());
>>>>>>> soulmatt
 
 		//dummy work
 		Integer[] dummyArr = new Integer[37];
 		List<Integer> dummyList = Arrays.asList(dummyArr);
 		mav.addObject("dummyList", dummyList);
 
 		return mav;
 	}
 	
 	/** 상품 통계 대쉬보드
 	 * @return
 	 */
 	@RequestMapping( value = "product/dashboard.do", method = RequestMethod.GET )
 	public ModelAndView productDashboard() {
 		
 		ModelAndView mav = new ModelAndView();
<<<<<<< HEAD
		mav.setViewName("statistics");
 		mav.addObject("navSeq", bigstarConstant.HEADER_STATS);
 		mav.addObject("leftMenuSeq", bigstarConstant.LEFT_STATISTICS_PRODUCT);
=======
		mav.setViewName("statistics-product-dashboard");
		mav.addObject("navSeq", bigstarConstant.getHEADER_STATS());
		mav.addObject("leftMenuSeq", bigstarConstant.getLEFT_STATISTICS_PRODUCT());
>>>>>>> soulmatt
 		
 		return mav;
 	}
 }
