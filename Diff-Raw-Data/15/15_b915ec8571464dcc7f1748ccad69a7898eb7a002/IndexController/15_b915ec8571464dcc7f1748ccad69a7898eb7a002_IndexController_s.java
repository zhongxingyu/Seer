 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.solairis.yourcarslife.controller;
 
 import com.solairis.yourcarslife.data.dao.UserDao;
 import com.solairis.yourcarslife.data.dao.VehicleFuelLogDao;
 import com.solairis.yourcarslife.data.domain.User;
 import com.solairis.yourcarslife.data.domain.Vehicle;
 import com.solairis.yourcarslife.data.domain.VehicleFuelLog;
 import com.solairis.yourcarslife.data.exception.UserDaoException;
 import com.solairis.yourcarslife.data.exception.VehicleLogDaoException;
 import com.solairis.yourcarslife.data.input.VehicleFuelLogInputData;
 import com.solairis.yourcarslife.service.VehicleFuelLogService;
 import com.solairis.yourcarslife.service.VehicleService;
 import com.solairis.yourcarslife.service.exception.ServiceException;
 import com.solairis.yourcarslife.service.exception.VehicleServiceException;
 import com.solairis.yourcarslife.util.UrlUtil;
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author josh
  */
 @Controller
 public class IndexController {
 
 	@Autowired
 	private VehicleFuelLogDao vehicleFuelLogDao;
 	@Autowired
 	private UserDao userDao;
 	@Autowired
 	private VehicleService vehicleService;
 	@Autowired
 	private VehicleFuelLogService vehicleFuelLogService;
 	private final Logger logger = Logger.getLogger(this.getClass());
 
 	@ExceptionHandler(value = ServiceException.class)
 	public ModelAndView handleServiceException(Exception e) {
 		ModelAndView mav = new ModelAndView("error");
 		mav.addObject("errorMessage", e.getMessage());
 		return mav;
 	}
 
	@RequestMapping(value = "/")
	public String home(Model model) {
		return "home";
	}

	@RequestMapping(value = "/login")
	public void login() {
	}

 	@RequestMapping(value = "/dashboard")
 	@Transactional
 	public void dashboard(Model model) {
 
 		org.springframework.security.core.userdetails.User securityUser = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		User user = this.userDao.getUser(Long.parseLong(securityUser.getUsername()));
 
 		model.addAttribute("vehicles", this.vehicleService.getVehiclesByUser(user));
 
 		model.addAttribute("user", user);
 
 	}
 
 	@RequestMapping("/log/{vehicleName}")
 	@Transactional
 	public String log(@PathVariable String vehicleName, Model model) {
 		ModelAndView mav = new ModelAndView("log");
 		org.springframework.security.core.userdetails.User securityUser = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		User user = this.userDao.getUser(Long.parseLong(securityUser.getUsername()));
 		Vehicle vehicle = this.vehicleService.getVehicleByNameAndUser(UrlUtil.convertFromUrl(vehicleName), user.getUserId());
 
 		model.addAttribute("auth", SecurityContextHolder.getContext().getAuthentication());
 		model.addAttribute("vehicle", vehicle);
 
 		VehicleFuelLogInputData inputData = new VehicleFuelLogInputData();
 		inputData.setVehicleId(vehicle.getVehicleId());
 		List<VehicleFuelLog> allVehicleFuelLogs = this.vehicleFuelLogDao.getVehicleFuelLogs(inputData);
 		model.addAttribute("allVehicleFuelLogsSize", allVehicleFuelLogs.size());
 		return "log";
 	}

	@RequestMapping("/admin")
	public String admin() {
		return "admin";
	}
 }
