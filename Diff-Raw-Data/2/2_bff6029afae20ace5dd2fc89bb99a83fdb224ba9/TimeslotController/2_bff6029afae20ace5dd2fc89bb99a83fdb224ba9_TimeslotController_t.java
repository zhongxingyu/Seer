 package com.scheduler.controllers;
 
 import java.sql.Time;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.scheduler.models.Timeslot;
 import com.scheduler.services.TimeslotService;
 
 @RequestMapping("/timeslot")
 @Controller
 public class TimeslotController {
 
 	// TODO hardcoded clientId -- need to replace with session
 	private int clientId = 1;
 
 	@Autowired(required = true)
 	private TimeslotService timeslotService;
 
 	// Author - Sonny
 	// Usage - View the list of time slots for the clientId (from session)
 	@RequestMapping(value = "/view", method = RequestMethod.GET)
 	public String viewAllTimeslots(
 			@ModelAttribute("deleteResult") String deleteResult, Model model) {
 
 		List<Timeslot> timeslots = timeslotService.GetAllTimeslots(clientId);
 		model.addAttribute("timeslots", timeslots);
 		model.addAttribute("deleteResult", deleteResult);
 		model.addAttribute("timeslot", new Timeslot());
 		return "timeslot/timeslotview";
 	}
 
 	// Author - Sonny
 	// Usage - Save new timeslot to database
 	@RequestMapping(value = "/save", method = RequestMethod.POST)
 	public String addNewTimeslots(HttpServletRequest request, Model model,
 			RedirectAttributes redirectAttributes) {
 
 		boolean saved = false;
 		String result = "";
 		int timeslotId = 0;
 
 		String startTime = request.getParameter("startTime");
 		String endTime = request.getParameter("stopTime");
 		String description = request.getParameter("description");
 
 		// create new time slot
 		Time startTimeObj = timeslotService.convertTime(startTime);
 		Time endTimeObj = timeslotService.convertTime(endTime);
 
 		Timeslot newTimeslot = new Timeslot();
 		newTimeslot.setClientId(clientId);
 		newTimeslot.setStartTime(startTimeObj);
 		newTimeslot.setStopTime(endTimeObj);
 		newTimeslot.setDescription(description);
 		
 		String duplicate = timeslotService.checkDuplicate(newTimeslot);
 		if( duplicate.equals("")) {
 			// not duplicate
 			timeslotId = timeslotService.CreateNewTimeslot(newTimeslot);
 			if(timeslotId>0) { saved = true; }
 			result = "Timeslot added successfully";
 		} else {
 			// duplicate exists
 			saved = true;
 			result = "This is a duplicate entry. Similar timeslot exist with the name <b>" + duplicate + "</b>";
 		}
 		
 
 		if (saved) {
 			redirectAttributes.addFlashAttribute("result", result);
 		} else {
 			redirectAttributes.addFlashAttribute("result",
 					"Oops! Something went wrong");
 		}
 
 		return "redirect:/timeslot/view";
 	}
 
 	// Author - Sonny
 	// Usage - Edit time slot
 	@RequestMapping(value = "/edit/{timeslotId}", method = RequestMethod.GET)
 	public String editTimeslot(@PathVariable int timeslotId, Model model) {
 
 		Timeslot timeslot = timeslotService.GetTimeslotDetails(timeslotId);
 		model.addAttribute("timeslot", timeslot);
 		return "timeslot/edit";
 	}
 
 	// Author - Sonny
 	// Usage - Save updated timeslot to database
 	@RequestMapping(value = "/update", method = RequestMethod.POST)
 	public String updateTimeslots(HttpServletRequest request, Model model,
 			RedirectAttributes redirectAttributes) {
 
 		boolean saved = false;
 		String result = "";
 		int newTimeslotId = 0;
 		int timeslotId = Integer.parseInt(request.getParameter("timeslotId"));
 
 		String startTime = request.getParameter("startTime");
 		String endTime = request.getParameter("stopTime");
 		String description = request.getParameter("description");
 
 		Time startTimeObj = timeslotService.convertTime(startTime);
 		Time endTimeObj = timeslotService.convertTime(endTime);
 
 		Timeslot updateTimeslot = new Timeslot();
 		updateTimeslot.setTimeslotId(timeslotId);
 		updateTimeslot.setClientId(clientId);
 		updateTimeslot.setStartTime(startTimeObj);
 		updateTimeslot.setStopTime(endTimeObj);
 		updateTimeslot.setDescription(description);
 
 		String duplicate = timeslotService.checkDuplicate(updateTimeslot);
 		if( duplicate.equals("")) {
 			// not duplicate
			Boolean updated = timeslotService.UpdateTimeslot(updateTimeslot);
 			if(newTimeslotId > 0) { saved = true; }
 			result = "Timeslot updated successfully";
 		} else {
 			// duplicate exists
 			saved = true;
 			result = "This is a duplicate entry. Similar timeslot exist with the name <b>" + duplicate + "</b>";
 		}
 
 		if (saved) {
 			redirectAttributes.addFlashAttribute("result", result);
 		} else {
 			redirectAttributes.addFlashAttribute("result",
 					"Oops! Something went wrong");
 		}
 
 		return "redirect:/timeslot/view";
 	}
 
 	// Author - Sonny
 	// Usage - Delete the time slot from the database
 	@RequestMapping(value = "/delete/{timeslotId}", method = RequestMethod.GET)
 	public String removeTimeslot(@PathVariable int timeslotId, Model model,
 			RedirectAttributes redirectAttributes) {
 
 		boolean deleted = timeslotService.RemoveTimeslot(timeslotId);
 		if (deleted) {
 			redirectAttributes.addFlashAttribute("result","Timeslot removed successfully");
 
 		} else {
 			redirectAttributes.addFlashAttribute("result","Unable to remove because some departments are using this timeslot.");
 		}
 
 		return "redirect:/timeslot/view";
 	}
 
 }
