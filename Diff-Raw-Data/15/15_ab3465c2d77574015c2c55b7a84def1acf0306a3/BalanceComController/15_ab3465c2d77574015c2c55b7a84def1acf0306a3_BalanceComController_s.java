 package com.bsg.pcms.balance;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.bsg.pcms.balance.dto.BalanceDTOEx;
 import com.bsg.pcms.balance.svc.BalanceService;
 import com.bsg.pcms.dto.BalanceDTO;
 import com.bsg.pcms.utility.BigstarConstant;
 
 @Controller
 @RequestMapping( value = "balance/sale-company" )
 public class BalanceComController {
 
 	private Logger logger = LoggerFactory.getLogger(BalanceComController.class);
 	
 	@Autowired
 	BigstarConstant bigstarConstant;
 	
 	@Autowired
 	private BalanceService balanceService;
 	
 	
 	@RequestMapping(value = "list.do")
 	public ModelAndView list(BalanceDTOEx balanceDTOEx) {
 		
 		List<BalanceDTOEx> balanceList = balanceService.saleList(balanceDTOEx);
 		
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("balance-sale-list");
 		mav.addObject("navSeq", bigstarConstant.getHEADER_BALANCE());
 		mav.addObject("leftMenuSeq", bigstarConstant.getLEFT_BALANCE_SALE());
 		mav.addObject("balanceList", balanceList);
 		mav.addObject("sorting_type", balanceDTOEx.getSorting_type());
 		return mav;
 		
 	}
 	
 	@RequestMapping(value = "search.do")
 	public ModelAndView search(BalanceDTOEx balanceDTOEx) {
 		
 		List<BalanceDTOEx> balanceList = balanceService.searchSale(balanceDTOEx);
 		
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("balance-sale-list");
 		mav.addObject("navSeq", bigstarConstant.getHEADER_BALANCE());
 		mav.addObject("leftMenuSeq", bigstarConstant.getLEFT_BALANCE_SALE());
 		mav.addObject("balanceList", balanceList);
 		
 		return mav;
 		
 	}
 	
 	@RequestMapping(value = "create.do", method = RequestMethod.GET)
 	public String create(BalanceDTOEx balanceDto) {
 		logger.info("balance/sale-company/create.do");
 		
 		balanceService.create(balanceDto);
 		
 		return "redirect:/balance/sale-company/list.to";
 	}
 	
 	@RequestMapping(value = "createView.do", method = RequestMethod.GET)
 	public ModelAndView creatView(BalanceDTOEx balanceDto) {
 		
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("balance-sale-info");
 		mav.addObject("navSeq", bigstarConstant.getHEADER_BALANCE());
 		mav.addObject("leftMenuSeq", bigstarConstant.getLEFT_BALANCE_SALE());
 		mav.addObject("isCreate", 1);
 		return mav;
 		
 	}
 	
 	
 }
