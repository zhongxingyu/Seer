 package org.ldv.sio.getap.web;
 
 import org.ldv.sio.getap.app.AccPersonalise;
 import org.ldv.sio.getap.app.DVCTAPException;
 import org.ldv.sio.getap.app.DemandeValidationConsoTempsAccPers;
 import org.ldv.sio.getap.app.FormAjoutDctap;
 import org.ldv.sio.getap.app.FormDemandeConsoTempsAccPers;
 import org.ldv.sio.getap.app.User;
 import org.ldv.sio.getap.app.service.IFManagerGeTAP;
 import org.ldv.sio.getap.utils.UtilSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
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
   @Qualifier("DBServiceManager")
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
     model.addAttribute("mesdctaps", manager.getAllDVCTAPByEleve(me));
     Long id = me.getId();
     model.addAttribute("etat0", manager.getAllDVCTAPByEtat(0, id));
     model.addAttribute("etat1", manager.getAllDVCTAPByEtat(1, id));
     model.addAttribute("etat2", manager.getAllDVCTAPByEtat(2, id));
     model.addAttribute("etat4", manager.getAllDVCTAPByEtat(4, id));
     model.addAttribute("etat8", manager.getAllDVCTAPByEtat(8, id));
     model.addAttribute("etat16", manager.getAllDVCTAPByEtat(16, id));
     model.addAttribute("etat32", manager.getAllDVCTAPByEtat(32, id));
     model.addAttribute("etat64", manager.getAllDVCTAPByEtat(64, id));
     model.addAttribute("etatsup1000", manager.getAllDVCTAPModifByEtat(id));
 
     return "eleve/mesdctap";
   }
 
   @RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
   public String deleteDCTAPById(@PathVariable String id, Model model) {
     DemandeValidationConsoTempsAccPers currentDctap = manager
         .getDVCTAPById(Long.valueOf(id));
     // Test que la DCTAP appartient à la bonne personne
     if (currentDctap.getEleve().equals(UtilSession.getUserInSession())) {
       currentDctap.setDctapAnnule();
       manager.updateDVCTAP(currentDctap);
     }
 
     return "redirect:/app/eleve/mesdctap";
   }
 
   @RequestMapping(value = "edit", method = RequestMethod.GET)
   public String editDCTAPById(@RequestParam("id") String id,
       FormDemandeConsoTempsAccPers formDctap, Model model) {
 
     // System.out.println("TEST id recu :" + formDctap.getId());
 
     DemandeValidationConsoTempsAccPers currentDctap = manager
         .getDVCTAPById(Long.valueOf(id));
 
     System.out.println("DCTAP : " + currentDctap);
 
     // valorise le bean de vue avec le dctap courant
     formDctap.setId(currentDctap.getId()); // en provenance d'un champ caché
     formDctap.setDateAction(currentDctap.getDateAction());
     formDctap.setProfId(currentDctap.getProf().getId());
     // formDctap.setProfNom(currentDctap.getProf().getNom());
     formDctap.setIdEleve(currentDctap.getEleve().getId());
     formDctap.setAccPersId(currentDctap.getAccPers().getId());
     formDctap.setMinutes(currentDctap.getMinutes());
     model.addAttribute("minute", currentDctap.getMinutes());

     model.addAttribute("lesProfs", manager.getAllProf());
     model.addAttribute("etat", manager.getDVCTAPById(formDctap.getId())
         .getEtat());
     model.addAttribute("lesAP", manager.getAllAPForEleve());
     return "eleve/edit";
   }
 
   @RequestMapping(value = "doedit", method = RequestMethod.POST)
   public String doeditDCTAPById(FormDemandeConsoTempsAccPers formDctap,
       BindingResult bindResult, Model model) {
     User prof = manager.getUserById(formDctap.getProfId());
     if (prof == null)
       bindResult.rejectValue("profId", "required",
           "Erreur d'identifiant de professeur");
 
     if (bindResult.hasErrors()) {
       model.addAttribute("lesProfs", manager.getAllProf());
       return "eleve/edit";
     } else {
       User user = UtilSession.getUserInSession();
       DemandeValidationConsoTempsAccPers dctapForUpdate = manager
           .getDVCTAPById(Long.valueOf(formDctap.getId()));
       if (dctapForUpdate.getEtat() == 0 || dctapForUpdate.getEtat() == 4) {
 
         AccPersonalise acc = new AccPersonalise(null,
             formDctap.getAccPersNom(), 1, user.getId());
         if (manager.getAPById(formDctap.getAccPersId()) != null) {
           acc = manager.getAPById(formDctap.getAccPersId());
           dctapForUpdate
               .setAccPers(manager.getAPById(formDctap.getAccPersId()));
         } else {
           manager.addAP(acc);
           dctapForUpdate.setAccPers(manager.getAPByNom(formDctap
               .getAccPersNom()));
         }
 
         dctapForUpdate.setDateAction(formDctap.getDateAction());
         dctapForUpdate.setMinutes(formDctap.getMinutes());
 
         dctapForUpdate.setProf(manager.getUserById(formDctap.getProfId()));
         dctapForUpdate.setDctapModifEleve();
         manager.updateDVCTAP(dctapForUpdate);
       }
 
       return "redirect:/app/eleve/mesdctap";
     }
   }
 
   @RequestMapping(value = "ajoutdctap", method = RequestMethod.GET)
   public String ajoutUser(FormAjoutDctap formAjout, Model model) {
 
     model.addAttribute("lesProfs", manager.getAllProf());
     model.addAttribute("lesAP", manager.getAllAPForEleve());
 
     formAjout.setAnneeScolaire(UtilSession.getAnneeScolaireInSession());
     formAjout.setProfId(UtilSession.getUserInSession().getId());
     formAjout.setEtat(0);
 
     return "eleve/ajoutdctap";
   }
 
   @RequestMapping(value = "doajout", method = RequestMethod.POST)
   public String doajoutUser(FormAjoutDctap formAjout, BindingResult bindResult,
       Model model) {
     model.addAttribute("lesProfs", manager.getAllProf());
     model.addAttribute("lesAP", manager.getAllAPForEleve());
 
     formAjout.setAnneeScolaire(UtilSession.getAnneeScolaireInSession());
     formAjout.setEleveId(UtilSession.getUserInSession().getId());
     formAjout.setEtat(0);
 
     if (formAjout.getDate() == null) {
       bindResult.rejectValue("date", "required",
           "Une date valide est attendue !");
       return "eleve/doajout";
     }
 
     if (bindResult.hasErrors()) {
       return "eleve/ajoutdctap";
     } else {
       AccPersonalise acc = new AccPersonalise(null, formAjout.getAccPersNom(),
           1, formAjout.getEleveId());
       if (manager.getAPById(formAjout.getAccPersId()) != null) {
         acc = manager.getAPById(formAjout.getAccPersId());
       } else {
         manager.addAP(acc);
       }
       DemandeValidationConsoTempsAccPers dctap = new DemandeValidationConsoTempsAccPers(
           formAjout.getId(), manager.getCurrentAnneeScolaire(),
           formAjout.getDate(), formAjout.getMinutes(),
           manager.getUserById(formAjout.getProfId()), acc,
           manager.getUserById(formAjout.getEleveId()), formAjout.getEtat());
 
       if (dctap.getMinutes() % 10 == 0 && dctap.getMinutes() <= 300
           && dctap.getMinutes() != 0) {
         manager.addDVCTAP(dctap);
       } else {
         bindResult.rejectValue("date", "required",
             "La durée de l'AP est invalide !");
         return "eleve/doajout";
       }
 
       return "redirect:/app/eleve/index";
     }
   }
 
   @RequestMapping(value = "refuse/{id}", method = RequestMethod.GET)
   public String refuseDCTAPById(@PathVariable String id, Model model) {
     DemandeValidationConsoTempsAccPers dctap = manager.getDVCTAPById(Long
         .valueOf(id));
 
     // Test que la DCTAP appartient à la bonne personne
     if (dctap.getEleve().equals(UtilSession.getUserInSession())
         && dctap.getEtat() > 1023) {
       try {
         dctap.transitionRejeteeParEleve();
         manager.updateDVCTAP(dctap);
       } catch (DVCTAPException e) {
         // TODO redirect error view
       }
     }
 
     return "redirect:/app/eleve/mesdctap";
   }
 
   @RequestMapping(value = "valid/{id}", method = RequestMethod.GET)
   public String validDCTAPById(@PathVariable String id, Model model) {
     DemandeValidationConsoTempsAccPers dctap = manager.getDVCTAPById(Long
         .valueOf(id));
 
     // Test que la DCTAP appartient à la bonne personne
     if (dctap.getEleve().equals(UtilSession.getUserInSession())
         && dctap.getEtat() > 1023) {
       dctap.setDctapConfirme();
       manager.updateDVCTAP(dctap);
     }
 
     return "redirect:/app/eleve/mesdctap";
   }
 }
