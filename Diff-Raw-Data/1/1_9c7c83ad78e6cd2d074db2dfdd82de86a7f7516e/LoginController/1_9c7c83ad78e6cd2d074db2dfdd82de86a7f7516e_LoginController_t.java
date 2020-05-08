 package com.xlthotel.core.controller;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
 
 import com.xlthotel.core.service.MailService;
 import com.xlthotel.foundation.common.Constant;
 import com.xlthotel.foundation.common.DateUtils;
 import com.xlthotel.foundation.common.MD5Utils;
 import com.xlthotel.foundation.common.SimpleServletRequestUtils;
 import com.xlthotel.foundation.common.ValidateCodeUtil;
 import com.xlthotel.foundation.exception.XltHotelException;
 import com.xlthotel.foundation.orm.entity.UserInfo;
 import com.xlthotel.foundation.service.UserInfoService;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 
 @Controller
 @RequestMapping("/servlet/user/login.do")
 public class LoginController {
 
 	private static final Log logger = LogFactory.getLog(RegController.class);
 	@Autowired
 	private UserInfoService userInfoService;
 
 	@Autowired
 	private ValidateCodeUtil validateCodeUtil;
 	
 	@Autowired
 	private MailService mailService;
 	
 	@Autowired
 	private FreeMarkerConfigurer freeMarkerConfigurer;
 
 	@RequestMapping(method = RequestMethod.GET, params = "method=merberRights")
 	public ModelAndView merberRights(Model model, HttpServletRequest request,
 			HttpServletResponse response) {
 		return new ModelAndView("/login/merberRights");
 	}
 
 	@RequestMapping(method = RequestMethod.GET, params = "method=initLogin")
 	public ModelAndView initLogin(Model model, HttpServletRequest request,
 			HttpServletResponse response) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String error_msg = SimpleServletRequestUtils.getStringParameter(
 				request, "error_msg", "");
 		returnModel.put("error_msg", error_msg);
 		return new ModelAndView("/login/login", returnModel);
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "method=userLogin")
 	public ModelAndView userLogin(Model model, HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String nameLogin = SimpleServletRequestUtils.getStringParameter(
 				request, "nameLogin", "");
 		String password = SimpleServletRequestUtils.getStringParameter(request,
 				"password", "");
 		String error_msg = "";
 		if (nameLogin == null || nameLogin.equals("")) {
 			error_msg = "登录失败:请输入用户名.";
 			model.addAttribute("error_msg", error_msg);
 			return new ModelAndView("/login/error_login", returnModel);
 		}
 		if (password == null || password.equals("")) {
 			error_msg = "登录失败:请输入密码.";
 			model.addAttribute("error_msg", error_msg);
 			return new ModelAndView("/login/error_login", returnModel);
 		}
 		// 检查验证码是否正确
 		String checkCode = SimpleServletRequestUtils.getStringParameter(
 				request, "checkCode", "");
 		if (checkCode == null || checkCode.equals("")) {
 			error_msg = "登录失败:请输入验证码.";
 			model.addAttribute("error_msg", error_msg);
 			return new ModelAndView("/login/error_login", returnModel);
 		}
 		HttpSession session = request.getSession();
 		String checkCodeS = (String) session
 				.getAttribute(Constant.USER_CHECKCODE_KEY);
 		if (!checkCodeS.equals(checkCode)) {
 			error_msg = "登录失败：验证码不正确.";
 			model.addAttribute("error_msg", error_msg);
 			session.removeAttribute(Constant.USER_CHECKCODE_KEY);
 			return new ModelAndView("/login/error_login", returnModel);
 		}
 		session.removeAttribute(Constant.USER_CHECKCODE_KEY);
 		// 密码编码
 		String userPwd = MD5Utils.MD5forPwd(password);
 		UserInfo ui = null;
 		if (nameLogin
 				.matches("^\\w+(([-+.]*)\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")) {
 			ui = this.userInfoService.findUserInfoByEmail(nameLogin);
 		} else if (nameLogin
 				.matches("^((\\(\\d{3}\\))|(\\d{3}\\-))?1(3|4|5|8)\\d{9}$")) {
 			ui = this.userInfoService.findUserInfoByMOBLE(nameLogin);
 		} else if (nameLogin.startsWith("P")) {
 			ui = this.userInfoService.findUserInfoByUserId(nameLogin);
 		}
 		try {
 			if (ui != null) {
 				String uiPwd = ui.getRePasswd();
 				if (!uiPwd.equals(userPwd)) {
 					error_msg = "登录失败：该用户密码错误,请重新登录.";
 					model.addAttribute("error_msg", error_msg);
 					return new ModelAndView("/login/error_login", returnModel);
 				}
 				if (ui.getIsValidated() == 0) {
 					error_msg = "登录失败：该用户还未邮箱激活，请登录注册邮箱查收验证邮件.";
 					model.addAttribute("error_msg", error_msg);
 					return new ModelAndView("/login/error_login", returnModel);
 				}
 				if (ui.getStatus() == 0) {
 					error_msg = "登录失败：该用户已经被锁定，暂时不能登录，请联系管理员.";
 					model.addAttribute("error_msg", error_msg);
 					return new ModelAndView("/login/error_login", returnModel);
 				}
 				ui.setLastLoginIP(ui.getLoginIP());
 				ui.setLastLoginTime(ui.getLoginTime());
 				ui.setLoginIP(request.getRemoteAddr());
 				ui.setLoginTime(new Date());
 				ui.setLoginTimes(ui.getLoginTimes() + 1);
 				UserInfo ui_save = this.userInfoService.saveUserInfo(ui);
 				if (ui_save != null) {
 					// 设置Session中用户
 					request.getSession().setAttribute(
 							Constant.USER_WEBSEESION_KEY, ui_save);
 					response.sendRedirect(request.getContextPath()
 							+ "/servlet/user/userinfo.do?method=initUserInfo");
 					return null;
 				} else {
 					error_msg = "登录失败:系统异常,请联系系统管理员.";
 					model.addAttribute("error_msg", error_msg);
 					return new ModelAndView("/login/error_login", returnModel);
 				}
 
 			} else {
 				error_msg = "登录失败:没有该用户信息，请重新登录.";
 				model.addAttribute("error_msg", error_msg);
 				return new ModelAndView("/login/error_login", returnModel);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>登录失败:登录出现异常，请重新登录.", e);
 		}
 
 	}
 	
 	/*********************************** 用户重置密码 *******************************************************************/
 	@RequestMapping(method = RequestMethod.GET, params = "method=initResetPassWord")
 	public ModelAndView initResetPassWord(HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			return new ModelAndView("/userinfo/resetPassWord", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>系统出现异常,请稍后尝试。", e);
 		}
 
 	}
 	
 	/*********************************** 发送密码 *******************************************************************/
 	@RequestMapping(method = RequestMethod.POST, params = "method=reSendPassWord")
 	public void reSendPassWord(HttpServletRequest request,
 			HttpServletResponse response) {
 		response.setHeader("Pragma", "No-cache");
 		response.setHeader("Cache-Control", "no-cache");
 		response.setDateHeader("Expires", 0);
 		response.setContentType("text/html;charset=utf-8");
 		String email = SimpleServletRequestUtils.getStringParameter(
 				request, "email", "");
 		
 		try {
 			PrintWriter out = response.getWriter();
 			if (email == null || "".equals(email)) {
 				out.println(false);
 			} else {
 				UserInfo ui = this.userInfoService.findUserInfoByEmail(email);
 				if(ui==null){
 					out.println(false);
 				}else{
 					ui.setRePasswd(MD5Utils.MD5forPwd(ui.getMobile()));
 					ui = this.userInfoService.saveUserInfo(ui);
 					Map<String, String> root = new HashMap<String, String>();
 		             root.put("nickName", ui.getNickName());
 		             root.put("userName", ui.getUserName());
 		             root.put("userId", ui.getUserId());
 		             root.put("email", ui.getEmail());
 		             root.put("mobile", ui.getMobile());
 		             root.put("repassword", ui.getMobile());
 		             root.put("date", DateUtils.getNowDate());
 					 mailService.send("仙龙潭会员信息",generateEmailContents("reSendPassword.ftl",root), email);
 					out.println(true);
 				}
 			}
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			throw new XltHotelException("<li>重新发送密码出现异常。", e);
 		}
 	}
 
 	@RequestMapping(params = "method=checkCode")
 	public void checkCode(HttpServletRequest request,
 			HttpServletResponse response,
 			@RequestParam("timestamp") String timestamp)
 			throws XltHotelException {
 
 		try {
 			response.setContentType("image/jpeg");
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			/**
 			 * 生成图片
 			 */
 			BufferedImage image = validateCodeUtil.creatImage();
 			/**
 			 * 输出图片
 			 */
 			ServletOutputStream sos = response.getOutputStream();
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			ImageIO.write(image, "JPEG", bos);
 			byte[] buf = bos.toByteArray();
 			response.setContentLength(buf.length);
 			sos.write(buf);
 			bos.close();
 			sos.close();
 			/**
 			 * 获取随机数
 			 */
 			HttpSession session = request.getSession();
 			session.setAttribute(Constant.USER_CHECKCODE_KEY,
 					validateCodeUtil.getSRand());
 		} catch (Exception e) {
 			throw new XltHotelException("<li>随机验证码出现异常。", e);
 		}
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "method=validateCode")
 	public void validateCode(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String validateC = (String) request.getSession().getAttribute(
 					Constant.USER_CHECKCODE_KEY);
 			String veryCode = SimpleServletRequestUtils.getStringParameter(
 					request, "checkcode", "");
 			PrintWriter out = response.getWriter();
 			if (veryCode == null || "".equals(veryCode)) {
 				out.println(false);
 			} else {
 				if (validateC.equals(veryCode)) {
 					out.println(true);
 				} else {
 					out.println(false);
 				}
 			}
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			throw new XltHotelException("<li>随机验证码出现异常。", e);
 		}
 	}
 	
 	/**   
      * 通过模版产生邮件正文   
      */ 
 	public String generateEmailContents(String templatsName,Map map){
 		try{
 			Configuration configuration = freeMarkerConfigurer.getConfiguration();
 			Template t = configuration.getTemplate(templatsName);
 			return FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
 		}catch(Exception e){
 			e.printStackTrace();
 			return null;
 		}
 	}	
 }
