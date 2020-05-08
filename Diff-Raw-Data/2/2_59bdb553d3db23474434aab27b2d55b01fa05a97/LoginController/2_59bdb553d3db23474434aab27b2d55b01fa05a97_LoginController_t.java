 package org.ldv.sio.getap.web;
 
 import org.ldv.sio.getap.app.FormAjoutAp;
 import org.ldv.sio.getap.app.User;
 import org.ldv.sio.getap.app.UserLoginCriteria;
 import org.ldv.sio.getap.app.UserSearchCriteria;
 import org.ldv.sio.getap.app.service.IFHauthLoginService;
 import org.ldv.sio.getap.app.service.IFManagerGeTAP;
 import org.ldv.sio.getap.utils.UtilSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Web controller for application actions.
  */
 @Controller
 @RequestMapping("/login/*")
 public class LoginController {
 
 	@Autowired
 	@Qualifier("serviceAuth")
 	private IFHauthLoginService hauthLoginService;
 
 	@Autowired
 	@Qualifier("DBServiceMangager")
 	private IFManagerGeTAP manager;
 
 	public void setHauthLoginService(IFHauthLoginService hauthLoginService) {
 		this.hauthLoginService = hauthLoginService;
 	}
 
 	/**
 	 * Default action, displays the login page.
 	 * 
 	 * @param UserLoginCriteria
 	 *            The criteria to authenticate
 	 */
 	@RequestMapping(value = "index", method = RequestMethod.GET)
 	public void index(UserLoginCriteria userSearchCriteria) {
 
 	}
 
 	/**
 	 * Valide user puis l'authentifie via le bean injecté hauthLoginService
 	 * 
 	 * @param userLoginCriteria
 	 *            The criteria to validate for
 	 * @param bindResult
 	 *            Holds userLoginCriteria validation errors
 	 * @param model
 	 *            Holds the resulting user (is authenticate)
 	 * @return Success or index view
 	 */
 	@RequestMapping(value = "authenticate", method = RequestMethod.POST)
 	public String authenticate(FormAjoutAp formAjout,
 			UserLoginCriteria userLoginCriteria, BindingResult bindResult,
 			Model model, UserSearchCriteria userSearchCriteria) {
 
 		if (userLoginCriteria.getLogin() == null
 				|| "".equals(userLoginCriteria.getLogin().trim())) {
 			bindResult.rejectValue("login", "required",
 					"SVP un identifiant est attendu !");
 		}
 		if (bindResult.hasErrors()) {
 			return "login/index";
 		} else {
 			User user = hauthLoginService.getAuthUser(userLoginCriteria);
 			if (user == null) {
 				bindResult.rejectValue("login", "required",
 						"SVP entrez un identifiant valide");
 				return "login/index";
 			}
 			UtilSession.setUserInSession(user);
 			UtilSession.setAnneeScolaireInSession("2011-2012");
 			model.addAttribute("userAuth", user);
 			User userIn = UtilSession.getUserInSession();
 			if (userIn.getRole().equals("prof-principal")) {
 				model.addAttribute("lesClasses",
 						manager.getAllClasseByProf(userIn.getId()));
 				model.addAttribute("lesEleves", manager.getAllEleveByClasse());
 				model.addAttribute("lesAP", manager.getApByType());
 			} else if (userIn.getRole().equals("admin")) {
 				model.addAttribute("lesAP", manager.getAllAP());
 			}
			return "redirect:/app/" + userIn.getRole() + "/index";
 		}
 	}
 
 	/**
 	 * supprime user de la session, et retourne au login
 	 * 
 	 * @param userLoginCriteria
 	 *            (pour fournir cette instance à la vue 'login/index')
 	 * @return Success view
 	 */
 	@RequestMapping(value = "logout", method = RequestMethod.GET)
 	public String logout(UserLoginCriteria userLoginCriteria) {
 		UtilSession.setUserInSession(null);
 		return "login/index";
 	}
 
 }
