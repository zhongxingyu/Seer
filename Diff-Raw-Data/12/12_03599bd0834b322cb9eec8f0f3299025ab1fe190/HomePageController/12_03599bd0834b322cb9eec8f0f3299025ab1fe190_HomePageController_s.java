 package com.webloan.common;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import com.webloan.common.dict.RecommendType;
 import com.webloan.model.Region;
 import com.webloan.model.RegionIP;
 import com.webloan.product.service.ProductService;
 import com.webloan.question.QuestionConstant;
 import com.webloan.question.service.QuestionService;
 import com.webloan.region.service.RegionService;
 
 public class HomePageController implements Controller {
 
 	@Resource ProductService productService;
 	@Resource QuestionService questionService;
 	@Resource RegionService regionService;
 	
 	public ModelAndView handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("index");
 		
 		HttpSession session = request.getSession();
 		
 		List<Region> regions = regionService.queryRegionByCityLoan();
 		session.setAttribute("allowLoanRegions", regions);
 		
 		if (session.getAttribute("userDefinedRegion") == null) {
 			session.setAttribute("userDefinedRegion", "0");
 		}
 		
		String ip = request.getRemoteAddr();
		RegionIP rip = questionService.qryCityByIP(ip);
		
		if (rip != null && rip.getRegion() != null) {
			session.setAttribute("currentRegion", rip.getRegion());
 		}
 		
 		Page hcrProds = productService.pagingProductRecommend(1, 5, RecommendType.HOT_CREDIT);
 		mav.addObject("hcrProds", hcrProds);
 		
 		Page hchProds = productService.pagingProductRecommend(1, 5, RecommendType.HOT_CHARACTER);
 		mav.addObject("hchProds", hchProds);
 		
 		Page hiQusts = questionService.pagingQuestions(1, 5, QuestionConstant.TYPE_HIGH);
 		mav.addObject("hiQusts", hiQusts);
 		
 		return mav;
 	}
 
 }
