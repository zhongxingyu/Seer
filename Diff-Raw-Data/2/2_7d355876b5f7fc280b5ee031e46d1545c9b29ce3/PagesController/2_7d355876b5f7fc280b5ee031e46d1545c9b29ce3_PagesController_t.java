 package info.beverlyshill.samples.controller;
 
 import java.util.List;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 import info.beverlyshill.samples.model.PagesManager;
 import info.beverlyshill.samples.model.Pages;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Controller for the Pages screen.
  * 
  * @author bhill
  */
 public class PagesController implements Controller {
 	private PagesManager pagesManager;
 	private Pages pages;
 	public static final String MAP_KEY = "pages";
 	Log log = LogFactory.getLog(PagesController.class);
 	private String successView;
 
 	/**
 	 * Returns a list of Pages database objects in ModelAndView.
 	 */
 	public ModelAndView handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		//Load data from the data.xml file
 		pagesManager.readXML(pages);
 		List pages = pagesManager.getPages();
 		// Log number of Pages records retrieved
 		log.info("Retrieved " + pages.size() + " records from Pages table.");
 		return new ModelAndView(getSuccessView(), MAP_KEY, pages);
 	}
 
 	/**
 	 * Forwards to success view
 	 */
 	public ModelAndView showForm(HttpServletRequest request,
 			HttpServletResponse response, BindException errors, Map controlModel)
 			throws Exception {
 		try {
 			return new ModelAndView(getSuccessView());
 		} catch (Exception e) {
			log.error("An error has occurred in showForm: " + e.getMessage());
 			throw e;
 		}
 	}
 
 	public PagesManager getPagesManager() {
 		return this.pagesManager;
 	}
 
 	public void setPagesManager(PagesManager pagesManager) {
 		this.pagesManager = pagesManager;
 	}
 	
 	public Pages getPages() {
 		return this.pages;
 	}
 
 	public void setPages(Pages pages) {
 		this.pages = pages;
 	}
 
 	public String getSuccessView() {
 		return this.successView;
 	}
 
 	public void setSuccessView(String successView) {
 		this.successView = successView;
 	}
 }
