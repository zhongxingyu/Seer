 package com.res.controller;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.res.exception.ServiceException;
 import com.res.model.FoodCategory;
 import com.res.service.MenuService;
 import com.res.util.MessageLoader;
 
 @Controller
 @SessionAttributes
 public class MenuController {
 
 	private static Logger logger = Logger.getLogger(MenuController.class);
 	
 	@Autowired private MenuService menuService;
 	@Autowired private MessageLoader messageLoader;
 	
 	@RequestMapping(value="/menu", method=RequestMethod.GET)
 	public ModelAndView showMenu(HttpServletRequest req, HttpServletResponse res) throws ServiceException{
 		HttpSession session = req.getSession();
 		
 		String resId = (String)session.getAttribute("restaurantId");
 		if(StringUtils.isEmpty(resId)){
 			throw new ServiceException(messageLoader.getMessage("restaurantid.not.set"));
 		}
 		Long restaurantId = Long.parseLong(resId);
 		
 		ModelAndView mav = new ModelAndView("menu");
 		
 		logger.info("restaurantId = " + restaurantId);
 		
		List<FoodCategory> foodCategories = menuService.getFoodCategoriesFromMenu(restaurantId); 
 		
 		mav.addObject("restaurantId", restaurantId);
 		mav.addObject("foodCategories", foodCategories);
 		mav.addObject("foodCategoriesSize", foodCategories.size());
 		return mav;
 	}
 
 }
