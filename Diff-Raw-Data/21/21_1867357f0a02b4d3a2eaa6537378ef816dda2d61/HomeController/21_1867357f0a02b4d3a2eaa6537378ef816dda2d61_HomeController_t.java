 package com.vmware.armyants;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	@Autowired
 	private LuceneIndexer searchEngine;
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		logger.info("Welcome home! the client locale is "+ locale.toString());
 		
 		try {
 			List<AppType> results = searchEngine.search("latitude and longitude");
 			for(AppType result : results) {
 				model.addAttribute("result", result.getName());
 			}
 		} catch (Exception e) {
 			logger.info("Exception in Lucene" + e);
 		} 
 		String environmentName = (System.getenv("VCAP_APPLICATION") != null) ? "Cloud" : "Local";
 		model.addAttribute("environmentName", environmentName);
 		return "home";
 	}
 	
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public void loginPage(HttpServletRequest request, HttpServletResponse response) throws ServletException {
 	    Twitter twitter = new TwitterFactory().getInstance();
         request.getSession().setAttribute("twitter", twitter);
         try {
             StringBuffer callbackURL = request.getRequestURL();
             int index = callbackURL.lastIndexOf("/");
             callbackURL.replace(index, callbackURL.length(), "").append("/callback");
 
             RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL.toString());
             request.getSession().setAttribute("requestToken", requestToken);
             response.sendRedirect(requestToken.getAuthenticationURL());
             logger.info("redirecting" + requestToken.getAuthenticationURL());
         } catch (TwitterException e) {
         	logger.info(e.toString());
             throw new ServletException(e);
         } catch (IOException e) {
 			logger.info(e.toString());
 		}
 	}
 	
 	@RequestMapping(value = "/callback", method = RequestMethod.GET)
 	public void callbackPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
         RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
         String verifier = request.getParameter("oauth_verifier");
         
         try {
             AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
             String userName = twitter.verifyCredentials().getName();
             request.getSession().removeAttribute("requestToken");
             logger.info(userName);
             response.setHeader("name", userName);
             response.sendRedirect(request.getContextPath() + "/dashboard");	
             return;
         } catch (TwitterException e) {
             throw new ServletException(e);
         }
 	}
 	
 	@RequestMapping(value = "/dashboard", method = RequestMethod.GET)
 	public String dashboardPage(HttpServletRequest request, HttpServletResponse response, Model model) throws IllegalStateException, TwitterException {
 		Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
 		String userName = twitter.getScreenName();
 		model.addAttribute("user_name", userName);
 		List<RFPCollectionType> rfpsForUser = searchEngine.getAllRFPsForUser(userName);
 		model.addAttribute("count", rfpsForUser.size());
 		for (int i = 0; i < rfpsForUser.size(); i++) {
 			model.addAttribute("rfpname" + i, rfpsForUser.get(i).getRfpName());
 			model.addAttribute("rfpbody" + i, rfpsForUser.get(i).getRfpBody());
 		}
 		logger.info(userName);
 		return "dashboard";
 	}
 	
 	@RequestMapping(value = "/logout", method = RequestMethod.GET)
 	public String logoutPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		request.getSession().invalidate();
 		response.sendRedirect(request.getContextPath()+ "/");
 		return "home";
 	}

 	
	@RequestMapping(value = "upload", method = RequestMethod.POST)
    public String attachReceipt(@RequestParam("upload_file") MultipartFile file) {
        logger.info("File Uploaded");
		return "upload";
    }
 }
