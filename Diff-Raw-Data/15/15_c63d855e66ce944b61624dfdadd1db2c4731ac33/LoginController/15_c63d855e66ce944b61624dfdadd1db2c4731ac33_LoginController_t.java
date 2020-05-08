 package portal.login.controller;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.portlet.RenderResponse;
 import javax.portlet.RenderRequest;
 import it.italiangrid.portal.dbapi.domain.UserInfo;
 import it.italiangrid.portal.dbapi.domain.UserToVo;
 import it.italiangrid.portal.dbapi.domain.Vo;
 import it.italiangrid.portal.dbapi.services.UserInfoService;
 import it.italiangrid.portal.dbapi.services.UserToVoService;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.*;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 
 import com.liferay.portal.kernel.util.WebKeys;
 import com.liferay.portal.model.User;
 
 /**
  * This class is the controller that authenticate the user and display some
  * information about the downloaded proxyes.
  * 
  * @author dmichelotto
  * 
  */
 
 @Controller("userInfoController")
 @RequestMapping(value = "VIEW")
 public class LoginController {
 
 	/**
 	 * Logger of the class DownloadCertificateController. 
 	 */
 	private static final Logger log = Logger.getLogger(LoginController.class);
 
 	/**
 	 * Attribute for access to the PortalUser database.
 	 */
 	@Autowired
 	private UserInfoService userInfoService;
 
 	/**
 	 * Attribute for access to the PortalUser database.
 	 */
 	@Autowired
 	private UserToVoService userToVoService;
 
 	/**
 	 * Method for render home.jsp page.
 	 * 
 	 * @return the page file name.
 	 */
 	@RenderMapping
 	public String showLogin(RenderResponse response) {
 		return "home";
 	}
 	
 	@RenderMapping(params = "myaction=success")
 	public String showSuccess(RenderResponse response, SessionStatus status) {
 		status.setComplete();
 		return "success";
 	}
 	
 	@RenderMapping(params = "myaction=successDM")
 	public String showSuccessDM(RenderResponse response, SessionStatus status) {
 		status.setComplete();
 		return "successDM";
 	}
 	
 	@RenderMapping(params = "myaction=error")
 	public String showError(RenderResponse response) {
 		return "error";
 	}
 	
 	@ModelAttribute("vaiqui")
 	public String getMydataUrl() {
 
 		String contextPath = LoginController.class.getClassLoader()
 				.getResource("").getPath();
 		
 		String result = "";
 
 		File test = new File(contextPath + "/content/MyProxy.properties");
 		if (test.exists()) {
 			
 			try {
 				FileInputStream inStream = new FileInputStream(contextPath
 						+ "/content/MyProxy.properties");
 
 				Properties prop = new Properties();
 
 				prop.load(inStream);
 
 				inStream.close();
 				
 				result = prop.getProperty("mydata.url");
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return result;
 	}
 	
 	@ModelAttribute("voNumber")
 	public int getVoNumber(RenderRequest request) {
 
 		User user = (User) request.getAttribute(WebKeys.USER);
 		
 		
 		if (user != null) {
			String mail = user.getEmailAddress();
 
			UserInfo userInfo = userInfoService.findByMail(	mail);
 			List<Vo> vos = userToVoService.findVoByUserId(userInfo.getUserId());
 			int count = 0;
 			for(Vo vo: vos){
 				if(vo.getConfigured().equals("true"))
 					count++;
 			}
 			return count;
 		}
 		return 0;
 	}
 
 	private long getExpirationTime(String proxyFile, String option) {
 
 		long result = 0;
 
 		try {
 			String cmd = "voms-proxy-info " + option + " -file " + proxyFile;
 
 			log.info("cmd = " + cmd);
 			Process p = Runtime.getRuntime().exec(cmd);
 			InputStream stdout = p.getInputStream();
 			InputStream stderr = p.getErrorStream();
 
 			BufferedReader output = new BufferedReader(new InputStreamReader(
 					stdout));
 			String line = null;
 
 			long totalSecs=0;
 			
 			while ((line = output.readLine()) != null) {
 				log.info("[Stdout] " + line);
 				totalSecs = Long.parseLong(line);
 				result = totalSecs;
 				
 				if(result <= 0){
 					File proxy = new File(proxyFile);
 					proxy.delete();
 				}
 				
 			}
 
 			output.close();
 
 			BufferedReader brCleanUp = new BufferedReader(
 					new InputStreamReader(stderr));
 			while ((line = brCleanUp.readLine()) != null) {
 
 				log.error("[Stderr] " + line);
 			}
 
 			brCleanUp.close();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 	
 	@ModelAttribute("shortProxies")
 	public List<String> shortProxies(RenderRequest request){
 		User user = (User)request.getAttribute(WebKeys.USER);
 		
 		List<String> result = new ArrayList<String>();
 		if(user!=null){
 			String dir = System.getProperty("java.io.tmpdir");
 			dir += "/users/"+user.getUserId()+"/";
 			
 			String proxyPrefix = dir + "x509up.";
 			
 			UserInfo userInfo = userInfoService.findByMail(user.getEmailAddress());
 			List<Vo> vos = userToVoService.findVoByUserId(userInfo.getUserId());
 			
 			for(Vo vo: vos){
 				File proxy = new File(proxyPrefix+vo.getVo());
 				
 				if(proxy.exists()){
 					
 //					long acTotalSecs = getExpirationTime(proxy.getAbsolutePath(), "-actimeleft");
 //					if(acTotalSecs)
 					
 					long totalSecs = getExpirationTime(proxy.getAbsolutePath(), "-timeleft");
 					long hours = totalSecs / 3600;
 					String color = "green";
 					if(hours<5)
 						color = "yellow";
 					if(hours<2)
 						color = "orange";
 					if(hours<1)
 						color = "black";
 					
 					if(totalSecs>0){
 						String role = getRole(userInfo, vo, proxy.getAbsolutePath());
 						result.add(vo.getVo()+"|"+color+"|"+vo.getIdVo()+"|"+role);
 					}
 				}
 			}
 			
 		}
 		return result;
 	}
 	
 	private String getRole(UserInfo userInfo, Vo vo, String proxyFile){
 		String role="no role";
 		try {
 			
 			UserToVo utv = userToVoService.findById(userInfo.getUserId(), vo.getIdVo());
 			
 			String toParse = utv.getFqans();
 			String roles[]=null;
 			if(toParse!=null){
 				roles=toParse.split(";");
 			}
 			
 			String cmd = "voms-proxy-info -all -file " + proxyFile;
 			
 			log.info("cmd = " + cmd);
 			Process p = Runtime.getRuntime().exec(cmd);
 			InputStream stdout = p.getInputStream();
 			InputStream stderr = p.getErrorStream();
 
 			BufferedReader output = new BufferedReader(new InputStreamReader(
 					stdout));
 			String line = null;
 			
 			List<String> tmpRole = new ArrayList<String>();
 			
 			
 			while ((line = output.readLine()) != null) {
 				log.info("[Stdout] " + line);
 				if(roles!=null){
 					if(line.contains("/"+vo.getVo()+"/Role=NULL")){
 						tmpRole.add(role);
 						log.info("trovato: "+ role);
 					}
 					for(int i=0; i<roles.length; i++){
 						if(line.contains(roles[i].trim())){
 							tmpRole.add(roles[i].trim());
 							log.info("trovato: "+ roles[i]);
 						}
 					}
 				}
 			}
 			if(!tmpRole.isEmpty())
 				role=tmpRole.get(0);
 			log.info("Fine trovato: "+ role);
 			output.close();
 
 			BufferedReader brCleanUp = new BufferedReader(
 					new InputStreamReader(stderr));
 			while ((line = brCleanUp.readLine()) != null) {
 				log.error("[Stderr] " + line);
 			}
 			brCleanUp.close();
 			
 			
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return role;
 	}
 }
