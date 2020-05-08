 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.solairis.yourcarslife.controller;
 
 import com.solairis.yourcarslife.command.FuelLogFormData;
 import com.solairis.yourcarslife.data.domain.LogFuel;
 import com.solairis.yourcarslife.service.LogService;
 import com.solairis.yourcarslife.service.UserService;
 import com.solairis.yourcarslife.service.VehicleService;
 import java.beans.PropertyEditor;
 import java.util.Date;
 import javax.validation.Valid;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
 
 /**
  *
  * @author josh
  */
 @Controller
 public class FuelLogController {
 
 	@Autowired
 	private UserService userService;
 	@Autowired
 	private LogService logService;
 	@Autowired
 	private VehicleService vehicleService;
 	@Autowired
 	private org.springframework.validation.Validator fuelLogFormDataValidator;
 	@Autowired
 	private PropertyEditor customDateEditor;
 
 	@InitBinder
 	protected void initBinder(WebDataBinder binder) {
 		binder.setValidator(this.fuelLogFormDataValidator);
 		binder.registerCustomEditor(Date.class, customDateEditor);
 	}
 
 	@RequestMapping(value = "/vehicle/{vehicleId}/log/fuel/{logId}", method = RequestMethod.GET)
 	@Transactional
 	public String form(@PathVariable("logId") Long logId, @ModelAttribute FuelLogFormData formData, Model model) {
 		if (logId != null) {
 			LogFuel fuelLog = logService.getFuelLog(logId);
 			formData.setFuel(fuelLog.getFuel());
 			formData.setLogDate(fuelLog.getLogDate());
 			formData.setLogId(fuelLog.getLogId());
 			formData.setMissedFillup(fuelLog.isMissedFillup());
 			formData.setOctane(fuelLog.getOctane());
 			formData.setOdometer(fuelLog.getOdometer());
 		}
 		return "fuel-log";
 	}
 
 	@RequestMapping(value = "/vehicle/{vehicleId}/log/fuel/{logId}", method = RequestMethod.POST)
 	@Transactional
 	public String submit(@PathVariable("vehicleId") long vehicleId, @PathVariable("logId") Long logId, @Valid FuelLogFormData formData, BindingResult errors, Model model) {
 
 		if (!errors.hasFieldErrors()) {
 			LogFuel fuelLog;
 
 			if (formData.getLogId() == null) {
 				fuelLog = new LogFuel();
 				fuelLog.setActive(true);
 			} else {
 				fuelLog = this.logService.getFuelLog(formData.getLogId());
 			}
 
 			fuelLog.setFuel(formData.getFuel());
 			fuelLog.setLogDate(formData.getLogDate());
 			fuelLog.setMissedFillup(formData.isMissedFillup());
 			fuelLog.setOctane(formData.getOctane());
 			fuelLog.setOdometer(formData.getOdometer());
 			fuelLog.setVehicle(this.vehicleService.getVehicle(vehicleId));
 
 			this.logService.save(fuelLog);
 			model.addAttribute("vehicleFuelLogId", fuelLog.getLogId());
 		}
 		model.addAttribute("errors", errors.getFieldErrors());
 		return "fuel-log";
 	}
 }
