 package webapp;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 public class VisualizeIndustryController implements Controller {
 
     protected final Log _logger = LogFactory.getLog(getClass());
 	private CompanyDAO _companyDao;
 	
 	VisualizeIndustryController(CompanyDAO companyDao) {
 		_companyDao = companyDao;
 	}
 	
 	public ModelAndView handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		String industry = request.getServletPath().substring("/industry/".length());
 		industry = industry.substring(0, industry.indexOf('.')).toLowerCase();
 		
 		String location = request.getQueryString();
 		System.out.println(location);
		if(location.contains("%20")){
 			String beg = location.substring(0,location.indexOf("%20"));
 			String end = location.substring(location.indexOf("%20")+"%20".length());
 			location = beg + " " + end;
 		}
 		System.out.println(location);

 		int limit = 500;
		List<Company> companies = _companyDao.findByIndustryAndLocation(industry,location).limit(limit).asList();
		Collections.sort(companies,new LimitedMoneyCompanyComparator(5));
		Collections.reverse(companies);
 
 		_logger.info("Returning visualization for " + industry);
         return new ModelAndView("industryvis", "industrycompanies", companies);
 	}
 
 }
