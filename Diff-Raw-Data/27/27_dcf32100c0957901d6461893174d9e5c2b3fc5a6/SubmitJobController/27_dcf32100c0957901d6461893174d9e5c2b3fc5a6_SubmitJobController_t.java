 package it.italiangrid.portal.dirac.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.portlet.RenderRequest;
 
 import it.italiangrid.portal.dbapi.domain.UserInfo;
 import it.italiangrid.portal.dbapi.domain.Vo;
 import it.italiangrid.portal.dbapi.services.UserInfoService;
 import it.italiangrid.portal.dbapi.services.UserToVoService;
import it.italiangrid.portal.dirac.exception.DiracException;
 import it.italiangrid.portal.dirac.model.Jdl;
 import it.italiangrid.portal.dirac.util.DiracConfig;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.model.User;
 import com.liferay.portal.util.PortalUtil;
 
 @Controller("diracSubmitJobController")
 @RequestMapping(value = "VIEW")
 public class SubmitJobController {
 	/**
 	 * Logger
 	 */
 	private static final Logger log = Logger.getLogger(SubmitJobController.class);
 	
 	@Autowired
 	private UserInfoService userInfoService;
 	
 	@Autowired
 	private UserToVoService userToVoService;
 	
 	/**
 	 * Display the home page.
 	 * 
 	 * @return Return the portlet home page.
 	 */
 	@RenderMapping(params="myaction=showSubmitJob")
 	public String showHomePage(){
 		
 		Jdl jdl = new Jdl();
 		
 		List<String> list = new ArrayList<String>();
 		
 		list.add(jdl.getStdOutput());
 		list.add(jdl.getStdError());
 		
 		jdl.setOutputSandbox(list);
 		
 		log.info("Test JDL:\n" + jdl);
 		return "submit";
 	}
 	
 	@ModelAttribute("jdl")
 	public Jdl newJob(){
 		return new Jdl();
 	}
 
 	@ModelAttribute("vos")
 	public List<Vo> getUserVos(RenderRequest request){
 		try {
 			User user = PortalUtil.getUser(request);
 			if(user!=null){
 				log.info(user.getEmailAddress()); 
 				UserInfo userInfo = userInfoService.findByMail(user.getEmailAddress());
 				
 				List<Vo> vos = userToVoService.findVoByUserId(userInfo.getUserId());
 				
 				String[] excluded = DiracConfig.getProperties("Dirac.properties", "dirac.exclude.vos").split(";");
 				
 				List<Vo> result = new ArrayList<Vo>();
 				for (Vo vo : vos) {
 					if(notIn(vo.getVo(), excluded))
 						result.add(vo);
 				}
 				return result;
 			}
		}catch (DiracException e){
			log.error(e.getMessage());
			User user;
			try {
				user = PortalUtil.getUser(request);
				if(user!=null){
					log.info(user.getEmailAddress()); 
					UserInfo userInfo = userInfoService.findByMail(user.getEmailAddress());
					
					List<Vo> vos = userToVoService.findVoByUserId(userInfo.getUserId());
					
					return vos;
				}
			} catch (PortalException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SystemException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
		return new ArrayList<Vo>();
 	}
 	
 	private boolean notIn(String vo, String[] excluded) {
 		
 		for (String excludedVo : excluded) {
 			if(vo.equals(excludedVo)){
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 	@ModelAttribute("defaultVo")
 	public String getUserDefaultVo(RenderRequest request){
 		try {
 			
 			User user = PortalUtil.getUser(request);
 			
 			if(user!=null){
 			
 				UserInfo userInfo = userInfoService.findByMail(user.getEmailAddress());
 				
 				return userToVoService.findDefaultVo(userInfo.getUserId());
 			}
 		} catch (PortalException e) {
 			e.printStackTrace();
 		} catch (SystemException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 }
