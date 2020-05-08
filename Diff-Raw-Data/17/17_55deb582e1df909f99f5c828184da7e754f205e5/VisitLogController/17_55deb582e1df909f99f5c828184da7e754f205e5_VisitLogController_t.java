 package com.ctb.pilot.stat.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.ctb.pilot.stat.model.VisitIpLog;
 import com.ctb.pilot.stat.model.DailyVisitLog;
 import com.ctb.pilot.stat.model.VisitUriLog;
 
 @Controller
 public class VisitLogController {
 
 	@Autowired
 	private VisitLogService visitLogService;
 
 	@RequestMapping("/services/admin/stat/list_daily_visit_logs.do")
 	public String listDailyVisitLogs(Model model) {
 		List<DailyVisitLog> dailyVisitLogs = visitLogService
 				.getDailyVisitLogs();
 		model.addAttribute("dailyVisitLogs", dailyVisitLogs);
 
 		return "/services/admin/stat/list_daily_visit_logs_view";
 	}
 
 	@RequestMapping("/services/admin/stat/list_daily_visit_ip_logs.do")
 	public String listDailyVisitIpLogs(@RequestParam String day, Model model) {
 		List<VisitIpLog> dailyVisitIpLogs = visitLogService
 				.getDailyVisitIpLogs(day);
 		model.addAttribute("day", day);
 		model.addAttribute("dailyVisitIpLogs", dailyVisitIpLogs);
 
 		return "/services/admin/stat/list_daily_visit_ip_logs_view";
 	}
 
 	@RequestMapping("/services/admin/stat/list_daily_visit_uri_logs.do")
 	public String listDailyVisitUriLogs(@RequestParam String day, Model model) {
 		List<VisitUriLog> dailyVisitUriLogs = visitLogService
 				.getDailyVisitUriLogs(day);
 		model.addAttribute("day", day);
 		model.addAttribute("dailyVisitUriLogs", dailyVisitUriLogs);
 
 		return "/services/admin/stat/list_daily_visit_uri_logs_view";
 	}
 
	@RequestMapping("/services/admin/stat/list_daily_visit_ip_uri_logs.do")
	public String listDailyVisitIpLogs(@RequestParam String day,
			@RequestParam String uri, Model model) {
 		List<VisitIpLog> dailyVisitIpLogs = visitLogService
				.getDailyVisitIpLogs(day, uri);
 		model.addAttribute("day", day);
 		model.addAttribute("uri", uri);
 		model.addAttribute("dailyVisitIpLogs", dailyVisitIpLogs);
 
 		return "/services/admin/stat/list_daily_visit_ip_logs_view";
 	}
 
	@RequestMapping("/services/admin/stat/list_daily_visit_uri_ip_logs.do")
	public String listDailyVisitUriLogs(@RequestParam String day,
			@RequestParam String ipAddress, Model model) {
 		List<VisitUriLog> dailyVisitUriLogs = visitLogService
				.getDailyVisitUriLogs(day, ipAddress);
 		model.addAttribute("day", day);
 		model.addAttribute("ipAddress", ipAddress);
 		model.addAttribute("dailyVisitUriLogs", dailyVisitUriLogs);
 
 		return "/services/admin/stat/list_daily_visit_uri_logs_view";
 	}
 
 }
