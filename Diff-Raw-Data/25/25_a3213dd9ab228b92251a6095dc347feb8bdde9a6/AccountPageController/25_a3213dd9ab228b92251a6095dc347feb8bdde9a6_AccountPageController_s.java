 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.emr.page.controller;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Person;
 import org.openmrs.Provider;
 import org.openmrs.api.APIException;
 import org.openmrs.module.emr.EmrConstants;
 import org.openmrs.module.emr.account.Account;
 import org.openmrs.module.emr.account.AccountService;
 import org.openmrs.module.emr.account.AccountValidator;
 import org.openmrs.ui.framework.annotation.BindParams;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.PageModel;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestParam;
 
 public class AccountPageController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	public void get(PageModel model, @RequestParam(value = "personId", required = false) Person person,
 	                @SpringBean("accountService") AccountService accountService) {
 		
 		Account account = accountService.getAccountByPerson(person);
 		if (account == null)
 			throw new APIException("Failed to find user account matching person with id:" + person.getPersonId());
 		
 		model.addAttribute("account", account);
 		model.addAttribute("capabilities", accountService.getAllCapabilities());
 		model.addAttribute("privilegeLevels", accountService.getAllPrivilegeLevels());
 	}
 	
 	public String post(@RequestParam("personId") @BindParams Account account, BindingResult errors,
 	                   @RequestParam(value = "enabled", required = false) Boolean enabled,
 	                   @RequestParam(value = "interactsWithPatients", required = false) Boolean interactsWithPatients,
 	                   @RequestParam(value = "createProviderAccount", required = false) Boolean createProviderAccount,
 	                   @SpringBean("accountService") AccountService accountService,
 	                   @SpringBean("accountValidator") AccountValidator accountValidator, PageModel model,
 	                   HttpServletRequest request) {
 		
 		accountValidator.validate(account, errors);
 		
 		if (!errors.hasErrors()) {
			if (enabled == null && account.getEnabled())
				account.setEnabled(false);
			if (createProviderAccount) {
				Provider provider = new Provider();
				account.setProvider(provider);
			}
			if (interactsWithPatients == null && account.getInteractsWithPatients())
				account.setInteractsWithPatients(false);
 			
 			try {
 				accountService.saveAccount(account);
 				request.getSession().setAttribute(EmrConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, "emr.account.saved");
 				
 				return "redirect:/emr/account.page?personId=" + account.getPerson().getPersonId();
 			}
 			catch (Exception e) {
 				log.warn("Some error occured while saving account details:", e);
 				request.getSession().setAttribute(EmrConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE,
 				    "emr.account.error.save.fail");
 			}
 		} else {
 			request.getSession().setAttribute(EmrConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE,
 			    "emr.error.foundValidationErrors");
 		}
 		
 		model.addAttribute("account", account);
 		model.addAttribute("capabilities", accountService.getAllCapabilities());
 		model.addAttribute("privilegeLevels", accountService.getAllPrivilegeLevels());
 		model.addAttribute("errors", errors);
 		//redisplay the form
 		return null;
 	}
 }
