 package webapp.controllers;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import webapp.Company;
 import webapp.CompanyDAO;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class CompanyProfileController implements Controller {
 
     protected final Log _logger = LogFactory.getLog(getClass());
 	private CompanyDAO _companyDao;
 	
 	CompanyProfileController(CompanyDAO companyDao) {
 		_companyDao = companyDao;
 	}
 	
 	public ModelAndView handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		String companyName = request.getServletPath().substring("/companies/".length());
 		companyName = companyName.substring(0, companyName.indexOf('.'));
 		_logger.info("Returning profile for " + companyName);
 		Company company = _companyDao.findByPermalink(companyName);
 		if (company != null) {
			String overview = company.getOverview();
			overview = overview.replaceAll("\\r\\n|\\r|\\n", "<br/>");
			company.setOverview(overview);
 			List<Company> companies = new ArrayList<Company>();
 			companies.add(company);
 			List<String> competitors = company.getCompetitors();
 			if(competitors!=null){
 				for(String comp : competitors){
 					if(_companyDao.findByPermalink(comp)!=null){
 						companies.add(_companyDao.findByPermalink(comp));
 					}
 				}
 			}
 			return new ModelAndView("companyprofile", "companies", companies);		
 		} else {
 			// Company went missing, just default to main page for now
 			return new ModelAndView("hello");
 		}
 	}
 
 }
