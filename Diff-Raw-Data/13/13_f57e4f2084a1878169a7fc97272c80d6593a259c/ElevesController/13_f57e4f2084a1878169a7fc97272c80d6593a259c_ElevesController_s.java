 package org.ldv.sio.getap.web;
 
 import org.ldv.sio.getap.app.DemandeConsoTempsAccPers;
 import org.ldv.sio.getap.app.FormAjoutDctap;
 import org.ldv.sio.getap.app.FormDemandeConsoTempsAccPers;
 import org.ldv.sio.getap.app.User;
 import org.ldv.sio.getap.app.service.IFManagerGeTAP;
 import org.ldv.sio.getap.utils.UtilSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * Web controller for eleve related actions.
  */
 @Controller
 @RequestMapping("/eleve/*")
 public class ElevesController {
 
 	@Autowired
 	private IFManagerGeTAP manager;
 
 	public void setManagerEleve(IFManagerGeTAP serviceManager) {
 		this.manager = serviceManager;
 	}
 
 	/**
 	 * Default action, displays the use case page.
 	 * 
 	 * 
 	 */
 	@RequestMapping(value = "index", method = RequestMethod.GET)
 	public void index() {
 
 	}
 
 	@RequestMapping(value = "mesdctap", method = RequestMethod.GET)
 	public String mesdctap(Model model) {
 		User me = UtilSession.getUserInSession();
 		model.addAttribute("mesdctaps", manager.getAllDCTAPByEleve(me));
 		return "eleve/mesdctap";
 	}
 
 	@RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
 	public String deleteDCTAPById(@PathVariable String id, Model model) {
 
 		if (!manager.deleteDCTAPById(Long.valueOf(id))) {
 			return "redirect:/app/eleve/index";
 		}
 
 		return "redirect:/app/eleve/mesdctap";
 	}
 
 	@RequestMapping(value = "edit", method = RequestMethod.GET)
 	public String editDCTAPById(@RequestParam("id") String id,
 			FormDemandeConsoTempsAccPers formDctap, Model model) {
 
 		System.out.println("TEST id recu :" + formDctap.getId());
 
 		DemandeConsoTempsAccPers currentDctap = manager.getDCTAPById(Long
 				.valueOf(id));
 
 		// valorise le bean de vue avec le dctap courant
 		formDctap.setId(currentDctap.getId()); // en provenance d'un champ caché
 		formDctap.setDateAction(currentDctap.getDateAction());
 		formDctap.setProfId(currentDctap.getProf().getId());
 		// formDctap.setProfNom(currentDctap.getProf().getNom());
 		formDctap.setIdEleve(currentDctap.getEleve().getId());
 
 		model.addAttribute("lesProfs", manager.getAllProf());
 
 		return "eleve/edit";
 	}
 
 	@RequestMapping(value = "doedit", method = RequestMethod.POST)
 	public String doeditDCTAPById(FormDemandeConsoTempsAccPers formDctap,
 			BindingResult bindResult, Model model) {
 		System.out.println("TEST :" + formDctap.getId());
 		System.out.println("TEST id eleve :" + formDctap.getIdEleve());
 		System.out.println("TEST :" + model);
 
 		// java.sql.Date.valueOf(formDctap.getDateAction());
 		User prof = manager.getUserById(formDctap.getProfId());
 		if (prof == null)
 			bindResult.rejectValue("profId", "required",
 					"Erreur d'identifiant de professeur");
 
 		if (bindResult.hasErrors()) {
 			model.addAttribute("lesProfs", manager.getAllProf());
 			return "eleve/edit";
 		} else {
 
 			DemandeConsoTempsAccPers dctapForUpdate = manager.getDCTAPById(Long
 					.valueOf(formDctap.getId()));
 
 			// valorise l'objet de la base à partir du bean de vue
 			dctapForUpdate.setDateAction(formDctap.getDateAction());
 
 			dctapForUpdate.setProf(manager.getUserById(formDctap.getProfId()));
 			manager.updateDCTAP(dctapForUpdate);
 
 			return "redirect:/app/eleve/mesdctap";
 		}
 	}
 
 	@RequestMapping(value = "ajoutdctap", method = RequestMethod.GET)
 	public String ajoutUser(FormAjoutDctap formAjout, Model model) {
 
 		model.addAttribute("lesProfs", manager.getAllProf());
 		model.addAttribute("lesAP", manager.getAllAP());
 
 		formAjout.setAnneeScolaire(UtilSession.getAnneeScolaireInSession());
 		formAjout.setEleveId(UtilSession.getUserInSession().getId());
 		formAjout.setEtat(0);
 
 		return "eleve/ajoutdctap";
 	}
 
 	@RequestMapping(value = "doajout", method = RequestMethod.POST)
 	public String doajoutUser(FormAjoutDctap formAjout,
 			BindingResult bindResult, Model model) {
 		System.out.println("TEST :" + formAjout.getId());
 		System.out.println("TEST id eleve :" + formAjout.getEleveId());
		System.out.println("TEST etat :" + formAjout.getEtat());
 		System.out.println("TEST :" + model);
 
 		if (bindResult.hasErrors())
 			return "eleve/ajoutdctap";
 		else {
 
 			DemandeConsoTempsAccPers dctap = new DemandeConsoTempsAccPers(
 					formAjout.getId(), formAjout.getAnneeScolaire(),
 					formAjout.getDate(), formAjout.getMinutes(),
 					manager.getUserById(formAjout.getProfId()),
 					manager.getAPById(formAjout.getAccPersId()),
 					manager.getUserById(formAjout.getEleveId()),
 					formAjout.getEtat());
 
 			manager.addDCTAP(dctap);
 
 			return "redirect:/app/eleve/index";
 		}
 	}
 }
