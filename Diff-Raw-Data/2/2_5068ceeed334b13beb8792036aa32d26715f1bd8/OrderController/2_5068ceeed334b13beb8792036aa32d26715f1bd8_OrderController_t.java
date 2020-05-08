 /******************************************************************************
  * Copyright (C) 2013 ShenZhen 1000funs Information Technology Co.,Ltd
  * All Rights Reserved.
  *****************************************************************************/
 package com.magiccube.order.action;
 
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.magiccube.address.model.RegionVO;
 import com.magiccube.address.service.AddressService;
 import com.magiccube.food.model.FoodQueryCondition;
 import com.magiccube.food.model.GroupFoods;
 import com.magiccube.food.model.GroupPackages;
 import com.magiccube.food.service.FoodService;
 
 /**
  * 
  * @since 2013-4-20
  * @author ksfifa
  */
 @Controller
 @RequestMapping("/order")
 public class OrderController {
 
 	@Resource
 	AddressService addressService;
 
 	@Resource
 	FoodService foodService;
 
 	@RequestMapping("")
 	public String main(Model model) {
 
 		// 读取地址列表
 		List<RegionVO> addressList = addressService.queryChildRegion(-1);
 		model.addAttribute("addressList", addressList);
 
 		// 获取食物列表，包括配餐和套餐
 //		List<GroupFoods> groupFoods = foodService
 //				.queryAvailableGroupAndFoods(new FoodQueryCondition(1, 1));
 		//model.addAttribute("groupFoods", groupFoods); //暂时屏蔽自由配餐
 		//判断是否在服务时间以及判断是否是午餐时间还是晚餐时间
 		GregorianCalendar curDate = new GregorianCalendar();
 		int hour = curDate.get(GregorianCalendar.HOUR_OF_DAY);
 		if(hour > 19 || hour < 9) {
			//return "order/no-service";
 		}
 		
 		Boolean isLunchTime = hour <= 11 ? true : false;
 		Boolean isDinnerTime = (hour >=13 && hour<= 19) ? true : false;
 		model.addAttribute("isLunchTime", isLunchTime);
 		model.addAttribute("isDinnerTime", isDinnerTime);
 		
 		List<GroupPackages> groupPackages = foodService
 				.queryAllGroupPackages(1);
 		model.addAttribute("groupPackages", groupPackages);
 
 		// 获取当前时间，判断是否为午餐时间
 
 		return "order/order-main";
 	}
 }
