 package net.berinle.caching;
 
 import java.util.Date;
import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.displaytag.tags.TableTagParameters;
 import org.displaytag.util.ParamEncoder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class EmployeeController {
 
 	private static Logger log = Logger.getLogger(EmployeeController.class);
 	
 	@Autowired private EmployeeService employeeService;	
 	@Autowired private DatePropertyEditor dateEditor;
 	
 	@RequestMapping(value="/paging", method=RequestMethod.GET)
 	public void paging(Model model, HttpServletRequest request){
 	
 		log.debug("paging called.");
 		int startIndex = 0;
 		/*Enumeration en = request.getParameterNames();
 		while(en.hasMoreElements()){			
 			String param = (String)en.nextElement();
 			if(param.startsWith("d-")){
 				startIndex = Integer.parseInt(request.getParameter(param.trim()));
 			}
 			
 		}*/
 		
 		int pageSize = 100;
 		String tableId = "employee_tbl";
 		
		String pgParam = new ParamEncoder(tableId).encodeParameterName(TableTagParameters.PARAMETER_PAGE);
		String pgValue = request.getParameter(pgParam);
		if(pgValue != null){
			startIndex = Integer.parseInt(pgValue) * pageSize;
		}		
 		
 		Map results = employeeService.getEmployees(startIndex);
 		List<Employee> employees = (List<Employee>) results.get("employees");
 		long totalSize = (Long) results.get("totalSize");
 		
		log.debug("employees : " + employees.size());
		log.debug("totalSize: " + totalSize);
		
 		model.addAttribute("employees", employees);		
 		model.addAttribute("totalSize", new Long(totalSize).intValue());
 	}
 	
 	@RequestMapping(value="/add", method=RequestMethod.GET)
 	public void add(Model model){
 	
 		log.debug("employee form");
 		Employee e = new Employee();
 		model.addAttribute("employee", e);		
 	}
 	
 	@RequestMapping(value="/add", method=RequestMethod.POST)
 	public String add(@ModelAttribute Employee employee, Model model){
 	
 		log.debug("saving employee");
 		employeeService.addEmployee(employee);
 		
 		List<Employee> employees = employeeService.getEmployees();
 		model.addAttribute("employees", employees);
 		
 		return "redirect:paging";
 	}
 	
 	@InitBinder
 	public void initBinder(WebDataBinder binder){
 		binder.registerCustomEditor(Date.class, "date", dateEditor);
 	}
 	
 }
