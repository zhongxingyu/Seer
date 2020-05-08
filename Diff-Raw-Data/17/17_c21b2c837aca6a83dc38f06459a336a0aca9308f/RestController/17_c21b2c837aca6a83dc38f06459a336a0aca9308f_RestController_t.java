 package com.ryaltech.whitewater.gauges.web;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.ryaltech.log.Logger;
 import com.ryaltech.whitewater.gauges.model.RiverInfo;
 import com.ryaltech.whitewater.gauges.model.RiverLevel;
 import com.ryaltech.whitewater.gauges.services.GaugeDataCollectionHub;
 import com.ryaltech.whitewater.gauges.services.Scheduler;
 import com.ryaltech.whitewater.gauges.services.WaterWebDao;
 
 @Controller
 public class RestController {
 	private static Logger logger = Logger.getLogger(RestController.class);
 
 	@Autowired
 	private GaugeDataCollectionHub dataCollectionHub;
 
 	@Autowired
 	private WaterWebDao dao;
 
 	@Autowired
 	private Scheduler scheduler;
 
	@InitBinder
	public void initBinder(WebDataBinder binder){
		//binder.registerCustomEditor(RiverInfo.class, propertyEditor)
	}
 	@RequestMapping(value = "/getGaugeValue/{gaugeDataCollectorId}/{gaugeId}", method = RequestMethod.GET)
 	public ModelAndView getGaugeValue(
 			@PathVariable("gaugeDataCollectorId") String gaugeDataCollectorId,
 			@PathVariable("gaugeId") String gaugeId, Model model) {
 		logger.debug("Called getGaugeValue[gaugeCollectorId=%s, gaugeId=%s]",
 				gaugeDataCollectorId, gaugeId);
 		RiverLevel rv = dataCollectionHub.getRiverLevel(new RiverInfo()
 				.setGaugeDataCollectorId(gaugeDataCollectorId).setGaugeId(
 						gaugeId));
 
 		ModelAndView mv = new ModelAndView("jacksonJson");
 		mv.addObject(rv);
 		return mv;
 	}
 
 	/**
 	 * Obtains water level and saves it in the DB
 	 * 
 	 * @param gaugeDataCollectorId
 	 * @param gaugeId
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value = "/updateRiverLevel/{riverId}", method = RequestMethod.GET)
 	public ModelAndView updateRiverLevel(
 			@PathVariable("riverId") String riverId, Model model) {
 		logger.debug("Called updateRiverLevel[riverId=%s]", riverId);
 		RiverInfo ri = dao.getRiverInfo(riverId);
 		RiverLevel rv = dataCollectionHub.getRiverLevel(ri);
 		dao.insertRiverLevels(rv);
 		ModelAndView mv = new ModelAndView("jacksonJson");
 		mv.addObject(rv);
 		return mv;
 	}
 
 	@RequestMapping(value = "/scheduleRiverLevelsUpdate", method = RequestMethod.GET)
 	public ModelAndView scheduleRiverLevelsUpdate(Model model) {
 		RiverInfo[] ris = dao.getAllRiversInfo();
 		if (ris.length < 1) {
 			logger.debug("No rivers registered.");
 		}
 		for (RiverInfo ri : ris) {
 			logger.debug("Scheduled updateRiverLevel[riverId=%s]",
 					ri.getRiverId());
 			scheduler.scheduleRiverLevelUpdate(ri);
 		}
 		ModelAndView mv = new ModelAndView("jacksonJson");
 		return mv;
 	}
 
 	@RequestMapping(value = "/getRiverLevels", method = RequestMethod.GET)
 	public ModelAndView getRiverLevels(Model model) {
 
 		ModelAndView mv = new ModelAndView("jacksonJson");
 		mv.addObject(dao.getLatestRiverLevels());
 		return mv;
 	}
 
 	@RequestMapping(value = "/getRivers", method = RequestMethod.GET)
 	public ModelAndView getRivers(Model model) {
 
 		ModelAndView mv = new ModelAndView("jacksonJson");
 		mv.addObject(dao.getAllRiversInfo());
 		return mv;
 	}
 
	@RequestMapping(value = "/addRivers", method = RequestMethod.GET)
	public ModelAndView addRivers(@RequestParam("rivers") RiverInfo[] rivers, Model model) {
 
 		
 		dao.persistRunnableConditions(rivers);
 		ModelAndView mv = new ModelAndView("jacksonJson");
 		mv.addObject(dao.getAllRiversInfo());
 		return mv;
 	}
 
 }
