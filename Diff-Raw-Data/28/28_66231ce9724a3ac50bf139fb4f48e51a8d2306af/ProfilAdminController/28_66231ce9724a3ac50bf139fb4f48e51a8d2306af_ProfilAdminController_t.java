 package com.ossia.test.web.admin;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import org.hibernate.exception.ConstraintViolationException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.security.acls.model.NotFoundException;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.ossia.test.domain.Evaluation;
 import com.ossia.test.domain.EvaluationStatus;
 import com.ossia.test.domain.Profil;
 import com.ossia.test.domain.TestSheet;
 import com.ossia.test.service.EvaluationService;
 import com.ossia.test.service.ProfilService;
 import com.ossia.test.service.TestSheetService;
 import com.ossia.test.web.form.AssignTestForm;
 import com.ossia.test.web.sort.SortingInfo;
 
 @Controller @RequestMapping("/admin")
 public class ProfilAdminController extends AbstractAdminController {
 	
 	private final static String SESSION_ADMINISTRATOR_LIST_SORT = "administrator.list.sort";
 	private final static String SESSION_CANDIDATE_LIST_SORT = "candidate.list.sort";	
 	
 	@Autowired
 	ProfilService profilService;
 	
 	@Autowired
 	TestSheetService testSheetService;
 	
 	@Autowired
 	EvaluationService evaluationService;
 	
 	@RequestMapping(value = "/administrators", method = RequestMethod.GET)
 	public String displayAdministratorsList(@RequestParam(value = "sort", required = false) String sortingField, @RequestParam(value = "direction", required = false) String sortingDirection, ModelMap model, HttpServletRequest request) {
 		model.put("profilForm", new Profil());
 		
 		// Sorting information
 		SortingInfo sortingInfo = completeProfilSortingInfo((SortingInfo)request.getSession().getAttribute(SESSION_ADMINISTRATOR_LIST_SORT), sortingField, sortingDirection);	
 		request.getSession().setAttribute(SESSION_ADMINISTRATOR_LIST_SORT, sortingInfo);
 		
 		setLastActionInModel(model, request);
 		
 		model.put("administrators", getProfilList(true, request));
 		model.put("sortingInfo", sortingInfo);
 		model.put("selectedTab", TAB_ADMINISTRATOR);
 		
 		return "administrators";
 	}
 	
 	@RequestMapping(value = "/candidates", method = RequestMethod.GET)
 	public String displayCandidatesList(@RequestParam(value = "sort", required = false) String sortingField, @RequestParam(value = "direction", required = false) String sortingDirection, ModelMap model, HttpServletRequest request) {
 		model.put("profilForm", new Profil());
 		
 		// Sorting information
 		SortingInfo sortingInfo = completeProfilSortingInfo((SortingInfo)request.getSession().getAttribute(SESSION_CANDIDATE_LIST_SORT), sortingField, sortingDirection);	
 		request.getSession().setAttribute(SESSION_CANDIDATE_LIST_SORT, sortingInfo);
 				
 		setLastActionInModel(model, request);
 		
 		model.put("candidates", getProfilList(false, request));
 		model.put("sortingInfo", sortingInfo);
 		model.put("selectedTab", TAB_CANDIDATE);
 		return "candidates";
 	}
 	
 	@RequestMapping(value = "/candidate", method = RequestMethod.GET)
 	public String displayCandidateDetail(@RequestParam(value = "candidate", required = true) Integer candidateId, @RequestParam(value = "evaluation", required = false) Integer evalId, ModelMap model, HttpServletRequest request) {
 		Profil admin = (Profil)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		Profil profil = profilService.getProfilById(candidateId);
 		if (profil == null) {
 			return "redirect:/errors/404";
 		}
 		
 		setLastActionInModel(model, request);
 		
 		// Deleting the test assigned to this candidate
 		if (evalId != null) {
 			Evaluation deletedEval = evaluationService.deleteEvaluation(evalId, candidateId, true, admin);
 			if (deletedEval != null) {
 				model.put("lastAction", buildNotifyMessage("text.notify.deassign.test.to.candidate", deletedEval.getTest().getIntitule(), profil.getPrenom(), profil.getNom()));
 			}
 		}
 				
 		// Test assigning form
 		model.put("assignTest", new AssignTestForm());
 		model.put("tests", getAssignableTests(profil));
 		
 		model.put("profil",  profil);
 		model.put("profilForm",  profil);
 		model.put("selectedTab", TAB_CANDIDATE);
 		return "candidate";
 	}
 	
	@RequestMapping(value = "/candidate/result", method = RequestMethod.GET)
	public String displayCandidateResult(@RequestParam(value = "candidate", required = true) Integer candidateId, @RequestParam(value = "evaluation", required = false) Integer evalId, ModelMap model, HttpServletRequest request) {
		Profil candidate = profilService.getProfilById(candidateId);
		if (candidate == null) {
			return "redirect:/errors/404";
		}
		Evaluation evaluation = evaluationService.getEvaluationById(evalId);
		if (evaluation == null || evaluation.getProfil().getId() != candidateId) {
			return "redirect:/errors/404";
		}
			
		model.put("profil",  candidate);
		model.put("result",  evaluation);
		model.put("selectedTab", TAB_CANDIDATE);
		return "candidate-result";
	}
	
 	/**
 	 * Activates or deactivates a profile
 	 * @param profileId Id of the user
 	 * @param origin Origin (administrators | candidates) page
 	 * @param model
 	 * @param request
 	 * @return
 	 */
 	@RequestMapping(value = "/profile/activate", method = RequestMethod.GET)
 	public String changeProfileActivation(@RequestParam(value = "profile") Integer profileId, @RequestParam(value = "origin") String origin, ModelMap model, HttpServletRequest request) {
 		Profil admin = (Profil)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		
 		Profil changedProfile = profilService.changeActivation(profileId, admin);
 		if (changedProfile == null) {
 			return "redirect:/errors/404";
 		}
 		String messageCodeForLastAction = "text.notify.deactivate.";
 		if (changedProfile.isEnabled()) {
 			messageCodeForLastAction = "text.notify.activate."; 
 		}
 		if (changedProfile.isAdmin()) {
 			messageCodeForLastAction += "administrator";
 		} else {
 			messageCodeForLastAction += "candidate";
 		}
 		request.getSession().setAttribute(SESSION_LAST_ACTION, buildNotifyMessage(messageCodeForLastAction, changedProfile.getPrenom(), changedProfile.getNom()));
 		
 		String redirectUrl = "/admin/" + origin;
 		if (origin.equals("candidate")) {
 			redirectUrl += "?candidate=" + profileId;
 		}
 		return "redirect:" + redirectUrl;
 	}
 		
 	/**
 	 * Deletes a profile
 	 * @param profileId Id of the user
 	 * @param origin Origin (administrators | candidates) page
 	 * @param model
 	 * @param request
 	 * @return
 	 */
 	@RequestMapping(value = "/profile/delete", method = RequestMethod.GET)
 	public String deleteProfile(@RequestParam(value = "profile") Integer profileId, @RequestParam(value = "origin") String origin, ModelMap model, HttpServletRequest request) {
 		Profil admin = (Profil)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		
 		Profil deletedProfile = profilService.deleteProfil(profileId, admin);
 		if (deletedProfile == null) {
 			return "redirect:/errors/404";
 		}
 		String messageCodeForLastAction = "text.notify.delete.candidate";
 		if (deletedProfile.isAdmin()) {
 			messageCodeForLastAction = "text.notify.delete.administrator";
 		}
 		request.getSession().setAttribute(SESSION_LAST_ACTION, buildNotifyMessage(messageCodeForLastAction, deletedProfile.getPrenom(), deletedProfile.getNom()));
 		
 		String redirectUrl = "/admin/";
 		if (origin.startsWith("candidate")) {
 			redirectUrl += "candidates";
 		} else {
 			redirectUrl += "administrators";
 		}
 		return "redirect:" + redirectUrl;
 	}
 	
 	/**
 	 * Adds or edits a profile
 	 * @param profil Profile to add or edit (form data)
 	 * @param origin Origin (administrators | candidates) page
 	 * @param result
 	 * @param request
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value = "/profile/add-or-edit", method = RequestMethod.POST)
 	public String addOrEditProfile(@RequestParam(value = "origin") String origin, @Valid @ModelAttribute(value="profilForm") Profil profilForm, BindingResult result, HttpServletRequest request, ModelMap model) {
 		if (origin.equals("administrators")) {
 			model.put("selectedTab", TAB_ADMINISTRATOR);
 			model.put("sortingInfo", (SortingInfo)request.getSession().getAttribute(SESSION_ADMINISTRATOR_LIST_SORT));
 			model.put("administrators", getProfilList(true, request));
 		} else {
 			model.put("selectedTab", TAB_CANDIDATE);
 			if (origin.equals("candidates")) {
 				model.put("sortingInfo", (SortingInfo)request.getSession().getAttribute(SESSION_CANDIDATE_LIST_SORT));
 				model.put("candidates", getProfilList(false, request));
 			} else {
 				Profil candidate = profilService.getProfilById(profilForm.getId());
 				model.put("profil", candidate);
 				// Test assigning form
 				model.put("assignTest", new AssignTestForm());
 				model.put("tests", getAssignableTests(candidate));
 			}
 		}
 		if (result.hasErrors()) {			
 			return origin;
 		}	
 		model.remove("selectedTab");
 		
 		// Form data is valid
 		profilForm.setAdmin(false);
 		if (origin.equals("administrators")) {
 			profilForm.setAdmin(true);
 		}
 		
 		Profil admin = (Profil)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		String messageCodeForLastAction = null;
 		try {
 			if (profilForm.getMode().equals(Profil.MODE_ADD)) {
 				profilService.createProfil(profilForm, admin);
 				messageCodeForLastAction = "text.notify.add.candidate";
 				if (profilForm.isAdmin()) {
 					messageCodeForLastAction = "text.notify.add.administrator";
 				}
 			} else {
 				profilService.updateProfil(profilForm, admin);
 				messageCodeForLastAction = "text.notify.edit.candidate";
 				if (profilForm.isAdmin()) {
 					messageCodeForLastAction = "text.notify.edit.administrator";
 				}
 			}
 		} catch (NotFoundException nfe) { // Trying to edit a profil that does not exist
 			return "redirect:/errors/404";
 		} catch (ConstraintViolationException cve) { // Login is already used by someone else (ADD mode)
 			rejectDuplicateLogin(cve, result);
 			return origin;			
 		} catch (DataIntegrityViolationException dive) { // Login is already used by someone else (EDIT mode)
 			rejectDuplicateLogin(dive, result);
 			return origin;
 		}		
 		
 		request.getSession().setAttribute(SESSION_LAST_ACTION, buildNotifyMessage(messageCodeForLastAction, profilForm.getPrenom(), profilForm.getNom()));
 		String redirectUrl = "/admin/" + origin;
 		if (origin.equals("candidate")) {
 			redirectUrl += "?candidate=" + profilForm.getId();
 		}
 		return "redirect:" + redirectUrl;
 	}
 	
 	/**
 	 * Assign a test to a candidate
 	 * @param assignTestForm
 	 * @param result
 	 * @param request
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value = "/profile/assign-test", method = RequestMethod.POST)
 	public String assignTest(@Valid AssignTestForm assignTestForm, BindingResult result, HttpServletRequest request, ModelMap model) {
 		Profil admin = (Profil)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		model.put("sortingInfo", (SortingInfo)request.getSession().getAttribute(SESSION_CANDIDATE_LIST_SORT));
 	
 		String redirectUrl = "/admin/candidate?candidate=" + assignTestForm.getCandidateId();
 		
 		if (assignTestForm.getTestId() == 0) { // No test has been picked
 			return "redirect:" + redirectUrl;
 		}
 		
 		Evaluation evaluation = evaluationService.assignTest(assignTestForm.getTestId(), assignTestForm.getCandidateId(), admin);
 		
 		if (evaluation == null) {
 			String errorAction = buildNotifyMessage("text.notify.assign.test.to.candidate.error");
 			request.getSession().setAttribute(SESSION_ERROR_ACTION, errorAction);		
 		} else if (evaluation.getStatus() == EvaluationStatus.ALREADY_ASSIGNED.getCode()) {
 			String warningAction = buildNotifyMessage("text.notify.assign.test.to.candidate.already.assigned", evaluation.getTest().getIntitule(), evaluation.getProfil().getPrenom(), evaluation.getProfil().getNom());
 			request.getSession().setAttribute(SESSION_WARNING_ACTION, warningAction);			
 		} else {
 			String lastAction = buildNotifyMessage("text.notify.assign.test.to.candidate", evaluation.getTest().getIntitule(), evaluation.getProfil().getPrenom(), evaluation.getProfil().getNom());
 				request.getSession().setAttribute(SESSION_LAST_ACTION, lastAction);			
 		}
 		
 		return "redirect:" + redirectUrl;
 	}
 	
 	
 	
 	private SortingInfo completeProfilSortingInfo (SortingInfo sortingInfo, String sortingField, String sortingDirection) {
 		if (sortingInfo == null) {
 			sortingInfo = new SortingInfo();
 			sortingInfo.setSortingDirection(SortingInfo.DESC);
 			sortingInfo.setSortingField(SortingInfo.SORT_CREATION_DATE);
 		}
 		if (sortingField != null) {
 			if (SortingInfo.SORT_ACTIVE.equals(sortingField) || SortingInfo.SORT_CREATION_DATE.equals(sortingField) || SortingInfo.SORT_NAME.equals(sortingField) || SortingInfo.SORT_FIRSTNAME.equals(sortingField)) {
 				sortingInfo.setSortingField(sortingField);
 			}
 		}
 		if (sortingDirection != null) {
 			if (SortingInfo.ASC.equals(sortingDirection) || SortingInfo.DESC.equals(sortingDirection)) {
 				sortingInfo.setSortingDirection(sortingDirection);
 			}
 		}		
 		return sortingInfo;
 	}
 	
 	/**
 	 * Gets the list of profiles (administrators or candidates)
 	 * @param admin Flag indicating if we want the administrators (true) or the candidates (false)
 	 * @param request To get the sorting information
 	 * @return Collection of profiles
 	 */
 	private Collection<Profil> getProfilList(boolean admin, HttpServletRequest request) {
 		
 		SortingInfo sortingInfo = null;
 		if (admin) {
 			sortingInfo = (SortingInfo)request.getSession().getAttribute(SESSION_ADMINISTRATOR_LIST_SORT);
 		} else {
 			sortingInfo = (SortingInfo)request.getSession().getAttribute(SESSION_CANDIDATE_LIST_SORT);
 		}
 		if (sortingInfo == null) {
 			sortingInfo = new SortingInfo();
 			sortingInfo.setSortingDirection(SortingInfo.DESC);
 			sortingInfo.setSortingField(SortingInfo.SORT_CREATION_DATE);
 		}
 		
 		return profilService.getSortedProfilByRole(admin, sortingInfo);
 	}
 	
 	private void rejectDuplicateLogin (Exception e, BindingResult result) {
 		if (e.getMessage().contains("Duplicate") && e.getMessage().contains("login")) {
 			result.rejectValue("login", "form.error.login.already.exists");
 		}
 	}
 	
 	/**
 	 * Gets the list of tests that are not currently assigned to or runned by the candidate
 	 * @Param candidate
 	 * @return
 	 */
 	private Collection<TestSheet> getAssignableTests(Profil candidate) {
 		SortingInfo sortingInfo = new SortingInfo();
 		sortingInfo.setSortingField(SortingInfo.SORT_INTITULE);
 		sortingInfo.setSortingDirection(SortingInfo.ASC);
 		List<TestSheet> allTests = testSheetService.getSortedTestSheets(sortingInfo);
 		List<TestSheet> assignableTests = new ArrayList<TestSheet>();
 		
 		boolean assignable = true;
 		for(TestSheet test : allTests) {
 			assignable = true;
 			for (Evaluation evaluation : candidate.getEvaluations()) {
 				if (evaluation.getTest().getId() == test.getId() && !evaluation.isTestTaken()) {
 					assignable = false;
 				}
 			}
 			if (assignable) {
 				assignableTests.add(test);
 			}
 		}
 		
 		return assignableTests;
 	}	
 }
