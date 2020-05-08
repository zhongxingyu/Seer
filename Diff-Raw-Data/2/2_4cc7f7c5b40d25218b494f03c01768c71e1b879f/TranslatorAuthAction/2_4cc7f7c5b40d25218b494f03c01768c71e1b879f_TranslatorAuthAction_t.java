 package com.omartech.tdg.action.translator;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.mapper.NoticeMapper;
 import com.omartech.tdg.model.Notice;
 import com.omartech.tdg.model.Translator;
 import com.omartech.tdg.service.TranslatorAuthService;
 import com.omartech.tdg.utils.UserType;
 
 @Controller
 public class TranslatorAuthAction {
 	
 	@Autowired
 	private TranslatorAuthService translatorAuthService;
 	@Autowired
 	private NoticeMapper noticeMapper;
 	Logger logger = Logger.getLogger(TranslatorAuthAction.class);
 	
 	@RequestMapping(value="/loginastranslator")
 	public String loginAsTranslator(){
 		return "translator/auth/login";
 	}
 	@RequestMapping(value="/translator/logout")
 	public String logout(HttpSession session){
 		session.invalidate();
		return "redirect:/loginastranslator";
 	}
 	@RequestMapping(value="/translatorlogin", method=RequestMethod.POST)
 	public String translatorLogin(
 			@RequestParam(value = "email", required = true) String email,
 			@RequestParam(value = "password", required = true) String password,
 			HttpSession session){
 		
 		Translator translator = translatorAuthService.getTranslatorByEmailAndPassword(email, password);
 		if(translator !=null ){
 			session.setAttribute("translator", translator);
 			return "redirect:/translatorindex";
 		}else{
 			logger.info("translator input a wrong email || password");
 			return "translator/auth/login";
 		}
 	}
 	
 	@RequestMapping(value="/translatorforgetpwd")
 	public String translatorForgetPwd(){
 		return "translator/auth/forget";
 	}
 	
 	@RequestMapping(value="/registastranslator")
 	public String registAsTranslator(){
 		return "translator/auth/register";
 	}
 	
 	
 	@RequestMapping(value="/translatorregister", method=RequestMethod.POST)
 	public ModelAndView translatorRegister(
 			@RequestParam(value = "email", required = true) String email,
 			@RequestParam(value = "password", required = true) String password,
 			HttpSession session
 			){
 		boolean flag = translatorAuthService.isEmailExist(email);
 		Translator translator = null;
 		if(!flag){
 			translator = new Translator(email,password);
 			translatorAuthService.insertTranslator(translator);
 			session.setAttribute("translator", translator);
 		}
 		return new ModelAndView("translator/auth/confirm").addObject("translator", translator);
 	}
 	@RequestMapping("/translator/auth/welcome")
 	public ModelAndView welcome(){
 		Notice notice = noticeMapper.getNoticeByUserType(UserType.TRANSLATOR);
 		return new ModelAndView("translator/auth/welcome").addObject("notice", notice);
 	}
 
 	public TranslatorAuthService getTranslatorAuthService() {
 		return translatorAuthService;
 	}
 
 	public void setTranslatorAuthService(TranslatorAuthService translatorAuthService) {
 		this.translatorAuthService = translatorAuthService;
 	}
 	public NoticeMapper getNoticeMapper() {
 		return noticeMapper;
 	}
 	public void setNoticeMapper(NoticeMapper noticeMapper) {
 		this.noticeMapper = noticeMapper;
 	}
 
 }
