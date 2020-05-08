 package cn.seu.cose.controller.admin;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import cn.seu.cose.entity.Reporter;
 import cn.seu.cose.filter.SecurityContextHolder;
 import cn.seu.cose.service.ReporterService;
 import cn.seu.cose.view.util.ViewUtil;
 
 
 @Controller
 public class AdminReporterController extends AbstractController{
 	
 	@Autowired ReporterService reporterService;
 	
 	@RequestMapping(value="/reporter/index", method=RequestMethod.GET)
 	public String index() {
 		return "reporter/login";
 	}
 	
	@RequestMapping(value="/reporter", method=RequestMethod.GET)
	public String index2() {
		return "reporter/login";
	}
	
 	@RequestMapping(value="/reporter/login", method=RequestMethod.GET)
 	public String login() {
 		return "reporter/login";
 	}
 	
 	@RequestMapping(value="/reporter/login", method=RequestMethod.POST) 
 	public void login(@RequestParam("username") String username, @RequestParam("password") String password,
 			Model model, HttpServletResponse response) {
 		Reporter reporter = reporterService.logon(username, password);
 		String to = ViewUtil.getContextPath() + "/reporter/login";
 		if (reporter != null) {
 			if (!reporter.isCertificated()) {
 				to = ViewUtil.getContextPath() + "/reporter/nogranted";
 			} else
 			{
 				to = ViewUtil.getContextPath() + "/reporter/article_list-" + reporter.getId();
 			}
 		} 
 		try {
 			response.sendRedirect(to);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping("/reporter/nogranted")
 	public String noGranted(Model model, HttpServletResponse response) {
 		return "reporter/nogrant";
 		
 	}
 	
 	@RequestMapping(value="/reporter/register", method=RequestMethod.GET) 
 	public String register() {
 		return "reporter/register";
 	}
 	@RequestMapping(value="/reporter/register", method=RequestMethod.POST)
 	public void register(@RequestParam("username") String username, @RequestParam("password") String password,
 			@RequestParam("name") String name, @RequestParam("email") String email, 
 			@RequestParam("phone") String phone, Model model, HttpServletResponse response) {
 		Reporter reporter = new Reporter();
 		reporter.setUsername(username);
 		reporter.setPassword(password);
 		reporter.setName(name);
 		reporter.setEmail(email);
 		reporter.setPhone(phone);
 		reporterService.register(reporter);
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/reporter/login");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping(value="/reporter/changeinfo-{id}", method=RequestMethod.GET)
 	public String changeInfo(@PathVariable("id") int id, Model model, HttpServletResponse response) {
 		Reporter reporter = reporterService.getReporterById(id);
 		putReporter(model, response);
 		return "/reporter/change_info";
 	}
 	@RequestMapping(value="/reporter/changeinfo", method=RequestMethod.POST)
 	public void changeInfo(@RequestParam("id") int id, @RequestParam("username") String username, @RequestParam("password") String password,
 			@RequestParam("email") String email, @RequestParam("phone") String phone, Model model, HttpServletResponse response) {
 		// ！真名无法修改
 		Reporter reporter = new Reporter();
 		reporter.setId(id);
 		reporter.setUsername(username);
 		reporter.setPassword(password);
 		reporter.setEmail(email);
 		reporter.setPhone(phone);
 		reporterService.modifyInfo(reporter);
 		SecurityContextHolder.getSecurityContext().setReporter(null);
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/reporter/login");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping(value="/reporter/logout", method=RequestMethod.GET)
 	public void logout(Model model, HttpServletResponse response) {
 		SecurityContextHolder.getSecurityContext().setReporter(null);
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/reporter/login");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 //***********************管理员使用的函数 down below************************************//
 	@RequestMapping("/admin/reporter_list")
 	public String reporterList(Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		model.addAttribute("searchInput", "");
 		List<Reporter> list = reporterService.getAllReportersList();
 		model.addAttribute("el_list", list);
 		return "admin_reporter";
 	}
 	
 	@RequestMapping(value="/admin/reporter_list_search-{searchInput}", method=RequestMethod.GET)
 	public String searchDesignerByName(@PathVariable("searchInput") String searchInput, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		List<Reporter> list = reporterService.searchReporter(searchInput);
 		model.addAttribute("el_list", list);
 		return "admin_reporter";
 	}
 	
 	@RequestMapping(value="/admin/reporter_list-{type}_top{k}", method=RequestMethod.GET)
 	public String topKList(@PathVariable("type") String type, @PathVariable("k") int k, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		List<Reporter> list = null;
 		
 		if (k <= 0) {
 			k = 10;
 		}
 		if (type.equals("contribute")) {
 			list = reporterService.getTopKReportersByContribution(k);
 		} else if (type.equals("accept")) {
 			list = reporterService.getTopKReportersByAccept(k);
 		}
 		model.addAttribute("el_list", list);
 		model.addAttribute("topK", k);
 		model.addAttribute("type", type);
 		return "admin_reporter";
 	}
 	
 	@RequestMapping(value="/admin/del_reporter", method=RequestMethod.POST)
 	public void postDelDesigner(@RequestParam("id") int id, HttpServletResponse response) {
 		reporterService.deleteReporter(id);
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/reporter_list");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping(value="/admin/reporter_list_certificated", method=RequestMethod.GET)
 	public String getCertificatedDesigners(Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		List<Reporter> list = reporterService.getCertificatedReportersList();
 		model.addAttribute("el_list", list);
 		return "admin_reporter";
 	}
 	
 	@RequestMapping(value="/admin/reporter_list_uncertificated", method=RequestMethod.GET)
 	public String getUncertificatedDesigners(Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		List<Reporter> list = reporterService.getUncertificatedReportersList();
 		model.addAttribute("el_list", list);
 		return "admin_reporter";
 	}
 	
 	@RequestMapping("/admin/cer_reporter")
 	public void certificateReporter(@RequestParam("id") int id, Model model, HttpServletResponse response) {
 		reporterService.certificate(id);
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/reporter_list");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping("/admin/uncer_reporter")
 	public void uncertificateReporter(@RequestParam("id") int id, Model model, HttpServletResponse response) {
 		reporterService.uncertificate(id);
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/reporter_list");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping("/reporter/about")
 	public String about(Model model, HttpServletResponse response) {
 		putReporter(model, response);
 		return "reporter/about";
 	}
 	
 	@RequestMapping("reporter/checkUsername")
 	public void checkUsername(@RequestParam("username") String username, Model model, HttpServletResponse response) {
 		Reporter reporter = reporterService.getReporterByUsername(username);
 		int occupied = 1;
 		if (reporter == null) {
 			occupied = 0;
 		} 
 		try {
 			PrintWriter out = response.getWriter();
 			out.print("{\"occupied\":\"" + occupied + "\"}");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
