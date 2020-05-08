 package it.nicogiangregorio.core.impl;
 
 import it.nicogiangregorio.core.ICaptchaAction;
 import it.nicogiangregorio.utils.CaptchaEnum;
 import it.nicogiangregorio.utils.CaptchaGenerator;
 import it.nicogiangregorio.utils.WebConstants;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Strategy for Refreshing action: Reset captcha order and right answer to
  * captcha question, then forward to correct jsp otherwise forward to a courtesy
  * jsp
  * 
  * @author Nico Giangregorio
  * 
  */
 public class CaptchaRefreshAction implements ICaptchaAction {
 
 	@Override
 	public String process(HttpServletRequest request,
 			HttpServletResponse response) {
 
 		Map<CaptchaEnum, String> captchaCodes = new HashMap<CaptchaEnum, String>();
 
 		try {
 			captchaCodes.put(CaptchaEnum.STAR,
 					CaptchaGenerator.createCaptchaCodes());
 			captchaCodes.put(CaptchaEnum.HEART,
 					CaptchaGenerator.createCaptchaCodes());
 			captchaCodes.put(CaptchaEnum.BWM,
 					CaptchaGenerator.createCaptchaCodes());
 			captchaCodes.put(CaptchaEnum.DIAMOND,
 					CaptchaGenerator.createCaptchaCodes());
 		} catch (IllegalStateException e) {
 			return WebConstants.ERROR_FORWARD_JSP;
 		}
 
 		int index = new Random().nextInt(captchaCodes.size());
 		CaptchaEnum rightAnswer = CaptchaEnum.values()[index];
 
 		request.getSession().setAttribute(WebConstants.ATTR_CAPTCHA_ANSWER,
 				captchaCodes.get(rightAnswer));
 
 		request.getSession().setAttribute(WebConstants.ATTR_RIGHT_ANSWER,
 				rightAnswer);
 
 		request.getSession().setAttribute(WebConstants.ATTR_CAPTCHA_CODES,
 				captchaCodes);
 
 		return WebConstants.REFRESH_FORWARD_JSP;
 	}
 }
