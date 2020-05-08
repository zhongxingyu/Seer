 /**
  * Copyright U-wiss
  */
 package com.pingpong.portal.controller;
 
 import com.pingpong.domain.Account;
 import com.pingpong.portal.command.ForgotPasswordCommand;
 import com.pingpong.portal.command.ResetPasswordCommand;
 import com.pingpong.portal.validator.ResetPasswordValidator;
 import com.pingpong.shared.AppService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.persistence.EntityNotFoundException;
 import javax.validation.Valid;
 import java.util.Map;
 
 /**
  * @author Artur Zhurat
  * @version 3.0
  * @since 12/04/2012
  */
 @Controller
 @RequestMapping("/account")
 public class AccountController extends AbstractBaseController {
 	private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);
 
 	@Autowired
 	private AppService appService;
 	@Autowired
 	private ResetPasswordValidator resetPasswordValidator;
 
 	@RequestMapping(value = "/forgot_password", method = RequestMethod.GET)
 	/*@Secured(value = "isAnonymous()")*/
 	public String showForgotPasswordForm(Map model) {
 		model.put("command", new ForgotPasswordCommand());
 		return "account/forgotPassword";
 	}
 
 	@RequestMapping(value = "/reset_password/{id}", method = RequestMethod.GET)
 	/*@Secured(value = "isAnonymous()")*/
 	public String showResetPasswordForm(@PathVariable("id") String id, Map model) {
 		final ResetPasswordCommand command = new ResetPasswordCommand();
 		command.setForgotPasswordId(id);
 		model.put("command", command);
 
 		try {
 			final Account account = appService.getAccountByForgotPasswordId(id);
 			model.put("account", account);
 		} catch (EntityNotFoundException enfe) {
 			LOG.error("Reset link is not valid anymore", enfe);
 			model.put(ERROR_MSG_VAR, "Reset link is not valid anymore");
 			return "account/resetPasswordError";
 		} catch (Exception e) {
 			LOG.error("Unknown error", e);
 			model.put(ERROR_MSG_VAR, "Unknown error");
 			return "account/resetPasswordError";
 		}
 
 		return "account/resetPassword";
 	}
 
 	@RequestMapping(value = "reset_password/resetPasswordProcess", method = RequestMethod.POST)
 	/*@Secured(value = "isAnonymous()")*/
 	public String resetPasswordProcess(@ModelAttribute("command") @Valid ResetPasswordCommand command, BindingResult result, Model model) {
 		final Account account = appService.getAccountByForgotPasswordId(command.getForgotPasswordId());
 
 		resetPasswordValidator.validate(command, result);
 
 		if(result.hasErrors()) {
 			model.addAttribute("account", account);
 			return "account/resetPassword";
 		}
 
 		try {
 			appService.resetForgottenPassword(command.getForgotPasswordId(), command.getPass1());
 		} catch(EntityNotFoundException enfe) {
 			LOG.error("Not found account", enfe);
 			model.addAttribute(ERROR_MSG_VAR, "Can't find such account");
 			return "account/resetPassword";
 		} catch(Exception e) {
 			LOG.error("ERROR", e);
 			model.addAttribute(ERROR_MSG_VAR, "Couldn't send request about forgot password, try again please");
 			return "account/resetPassword";
 		}
 		return "account/resetPasswordSuccess";
 	}
 
 	@RequestMapping(value = "/forgotPasswordProcess", method = RequestMethod.POST)
 	/*@Secured(value = "isAnonymous()")*/
 	public String forgotPasswordProcess(@ModelAttribute("command") @Valid ForgotPasswordCommand command, BindingResult result, Model model) {
 		if(result.hasErrors()) {
 			return "account/forgotPassword";
 		}
 		try {
 			appService.requestForgotPassword(command.getUsername());
 		} catch(EntityNotFoundException enfe) {
 			LOG.error("Not found account", enfe);
 			model.addAttribute(ERROR_MSG_VAR, "Can't find such account");
 			return "account/forgotPassword";
 		} catch(Exception e) {
 			LOG.error("ERROR", e);
 			model.addAttribute(ERROR_MSG_VAR, "Couldn't send request about forgot password, try again please");
 			return "account/forgotPassword";
 		}
 		return "account/forgotPasswordThanks";
 	}
 
 	@RequestMapping(value = "/forgotPasswordThanks", method = RequestMethod.GET)
 	/*@Secured(value = "isAnonymous()")*/
 	public String showForgotPasswordThanks() {
 		return "account/forgotPasswordThanks";
 	}
 
 	@RequestMapping(value = "/resetPasswordError", method = RequestMethod.GET)
 	/*@Secured(value = "isAnonymous()")*/
 	public String showResetPasswordError() {
 		return "account/resetPasswordError";
 	}
 
 	@RequestMapping(value = "/resetPasswordSuccess", method = RequestMethod.GET)
 	/*@Secured(value = "isAnonymous()")*/
 	public String showResetPasswordSuccess() {
 		return "account/resetPasswordSuccess";
 	}
 }
