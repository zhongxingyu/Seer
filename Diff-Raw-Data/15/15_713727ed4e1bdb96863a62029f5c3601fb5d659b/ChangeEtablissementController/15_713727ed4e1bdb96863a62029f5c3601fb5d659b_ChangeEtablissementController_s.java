 /**
  * Copyright (C) 2012 RECIA http://www.recia.fr
  * @Author (C) 2012 Maxime Bossard <mxbossard@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  * 
  */
 package org.esco.portlet.changeetab.mvc.controller;
 
 import java.util.Collection;
 import java.util.Map;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 
 import org.esco.portlet.changeetab.model.Etablissement;
 import org.esco.portlet.changeetab.mvc.ChangeEtabCommand;
 import org.esco.portlet.changeetab.service.IEtablissementService;
 import org.esco.portlet.changeetab.service.IUserInfoService;
 import org.esco.portlet.changeetab.service.IUserService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.portlet.ModelAndView;
 import org.springframework.web.portlet.bind.annotation.ActionMapping;
 
 /**
  * @author GIP RECIA 2013 - Maxime BOSSARD.
  *
  */
 @Controller
 @RequestMapping("VIEW")
 public class ChangeEtablissementController implements InitializingBean {
 
 	/** Logger. */
 	private static final Logger LOG = LoggerFactory.getLogger(ChangeEtablissementController.class);
 
 	private static final String ETAB_LIST_KEY = "etabs";
 
 	private static final String CURRENT_ETAB_KEY = "currentEtab";
 
 	private static final String DISPLAY_PORTLET_KEY = "displayPortlet";
 
 	private static final String DISPLAY_MODE_KEY = "displayMode";
 
 	@Value("${redirectAfterChange:false}")
 	private boolean redirectAfterChange = false;
 
 	@Value("${logoutUrlRedirect:null}")
 	private String logoutUrlRedirect;
 
 	@Autowired
 	private IUserInfoService userInfoService;
 
 	@Autowired
 	private IEtablissementService etablissementService;
 
 	@Autowired
 	private IUserService userService;
 
 	@ActionMapping("changeEtab")
 	public void changeEtab(@ModelAttribute("command") final ChangeEtabCommand changeEtabCommand, final ActionRequest request
 			, final ActionResponse response) throws Exception {
 		final String selectedEtabId = changeEtabCommand.getSelectedEtabId();
 		Assert.hasText(selectedEtabId, "No Etablissement Id selected !");
 
 		ChangeEtablissementController.LOG.debug("Selected Etab Id to change: [{}]", selectedEtabId);
 
 		final Collection<String> changeableEtabIds = this.userInfoService.getChangeableEtabIds(request);
 		if (!changeableEtabIds.contains(selectedEtabId)) {
 			// If selected Id is not an allowed Id
 			ChangeEtablissementController.LOG.warn("Attempt to switch to a not allowed Etablissement !");
 		} else {
 			// Process the etablissement swap.
 			final String userId = this.userInfoService.getUserId(request);
 			if (StringUtils.hasText(userId)) {
 				this.userService.changeCurrentEtablissement(userId, selectedEtabId);
 
 				if (this.redirectAfterChange) {
 					request.getPortalContext();
 					response.sendRedirect(this.logoutUrlRedirect);
 				}
 			}
 		}
 
 		changeEtabCommand.reset();
 	}
 
 	@RequestMapping
 	public ModelAndView handleRenderRequest(final RenderRequest request, final RenderResponse response) throws Exception {
 		ChangeEtablissementController.LOG.debug("Rendering Change Etablissement portlet View...");
 
 		boolean display = false;
 		final ModelAndView mv = new ModelAndView("home");
 
 		final String currentEtabId = this.userInfoService.getCurrentEtabId(request);
 		final Collection<String> changeableEtabIds = this.userInfoService.getChangeableEtabIds(request);
 
		if ((currentEtabId != null) && !changeableEtabIds.isEmpty()) {
 			final Map<String, Etablissement> changeableEtabs = this.etablissementService.retrieveEtablissementsByIds(changeableEtabIds);
 
 			final Etablissement currentEtab = changeableEtabs.remove(currentEtabId);
 
 			ChangeEtablissementController.LOG.debug("Found [{}] etablissements whose user can change to.", changeableEtabs.size());
 
 			display = !changeableEtabs.values().isEmpty();
 
 			mv.addObject(ChangeEtablissementController.CURRENT_ETAB_KEY, currentEtab);
 			mv.addObject(ChangeEtablissementController.ETAB_LIST_KEY, changeableEtabs.values());
 		}
 
 		mv.addObject(ChangeEtablissementController.DISPLAY_PORTLET_KEY, display);
 
 		mv.addObject(ChangeEtablissementController.DISPLAY_MODE_KEY,
 				request.getPreferences().getValue(ChangeEtablissementController.DISPLAY_MODE_KEY, "All"));
 
 		return mv;
 	}
 
 	@ModelAttribute("command")
 	public ChangeEtabCommand getChangeEtabCommand() {
 		return new ChangeEtabCommand();
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		Assert.notNull(this.etablissementService, "No Etablissement service configured !");
 		Assert.notNull(this.userInfoService, "No User Info service configured !");
 		Assert.notNull(this.userService, "No User service configured !");
 
 		if (this.redirectAfterChange) {
 			Assert.hasText(this.logoutUrlRedirect, "No logout URL to redirect configured !");
 		}
 	}
 
 	/**
 	 * Getter of userInfoService.
 	 *
 	 * @return the userInfoService
 	 */
 	public IUserInfoService getUserInfoService() {
 		return this.userInfoService;
 	}
 
 	/**
 	 * Setter of userInfoService.
 	 *
 	 * @param userInfoService the userInfoService to set
 	 */
 	public void setUserInfoService(final IUserInfoService userInfoService) {
 		this.userInfoService = userInfoService;
 	}
 
 	/**
 	 * Getter of etablissementService.
 	 *
 	 * @return the etablissementService
 	 */
 	public IEtablissementService getEtablissementService() {
 		return this.etablissementService;
 	}
 
 	/**
 	 * Setter of etablissementService.
 	 *
 	 * @param etablissementService the etablissementService to set
 	 */
 	public void setEtablissementService(final IEtablissementService etablissementService) {
 		this.etablissementService = etablissementService;
 	}
 
 	/**
 	 * Getter of redirectAfterChange.
 	 *
 	 * @return the redirectAfterChange
 	 */
 	public boolean isRedirectAfterChange() {
 		return this.redirectAfterChange;
 	}
 
 	/**
 	 * Setter of redirectAfterChange.
 	 *
 	 * @param redirectAfterChange the redirectAfterChange to set
 	 */
 	public void setRedirectAfterChange(final boolean redirectAfterChange) {
 		this.redirectAfterChange = redirectAfterChange;
 	}
 
 	/**
 	 * Getter of userService.
 	 *
 	 * @return the userService
 	 */
 	public IUserService getUserService() {
 		return this.userService;
 	}
 
 	/**
 	 * Setter of userService.
 	 *
 	 * @param userService the userService to set
 	 */
 	public void setUserService(final IUserService userService) {
 		this.userService = userService;
 	}
 
 	/**
 	 * Getter of logoutUrlRedirect.
 	 *
 	 * @return the logoutUrlRedirect
 	 */
 	public String getLogoutUrlRedirect() {
 		return this.logoutUrlRedirect;
 	}
 
 	/**
 	 * Setter of logoutUrlRedirect.
 	 *
 	 * @param logoutUrlRedirect the logoutUrlRedirect to set
 	 */
 	public void setLogoutUrlRedirect(final String logoutUrlRedirect) {
 		this.logoutUrlRedirect = logoutUrlRedirect;
 	}
 
 }
