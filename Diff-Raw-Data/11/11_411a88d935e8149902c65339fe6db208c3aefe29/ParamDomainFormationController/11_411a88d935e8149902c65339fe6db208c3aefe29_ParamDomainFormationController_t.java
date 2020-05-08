 /**
  *
  */
 package org.esupportail.opi.web.controllers.formation;
 
 import org.apache.commons.beanutils.BeanComparator;
 import org.apache.commons.collections.comparators.NullComparator;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.opi.domain.beans.formation.Domaine2AnnuForm;
 import org.esupportail.opi.domain.beans.formation.DomaineAnnuForm;
 import org.esupportail.opi.services.remote.client.IApogee;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.pojo.DomaineAnnuFormPojo;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.controllers.AbstractAccessController;
 import org.esupportail.opi.web.controllers.PreferencesController;
 
 import javax.faces.model.SelectItem;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * @author gomez
  */
 public class ParamDomainFormationController extends AbstractAccessController {
 /*
  * ******************* PROPERTIES STATIC ******************* */
     /**
      * attribute serialVersionUID.
      */
     private static final long serialVersionUID = 33137252791580459L;
     /**
      *
      */
     protected static final String[] DEFAULT_TEMOIN_VALUES = {"O", "N"};
     /**
      *
      */
     private static final String FORMULAIRE_DOMAIN = "formAddDomain";
 
 /*
  * ******************* PROPERTIES ******************* */
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
     /**
      *
      */
     private PreferencesController preferencesController;
     /**
      * The actionEnum.
      */
     private ActionEnum actionEnum;
     /**
      * The actionLang.
      */
     private ActionEnum actionLang;
     /**
      * domaine de formation selectionne.
      */
     private DomaineAnnuFormPojo domain;
     /**
      * liste des domaines.
      */
     private List<DomaineAnnuFormPojo> listDomain;
     /**
      * langue selectionnee.
      */
     private String langueSelected;
     /**
      * libele saisie.
      */
     private String libSaisi;
     /**
      * temEnSveItems.
      */
     private List<SelectItem> temEnSveItems;
     /**
      * liste des langues.
      */
     private List<SelectItem> allLangue;
 
     private IApogee iApogee;
 	
 
 	/*
  * ******************* CONSTRUCTOR ************************* */
 
     /**
      * Constructors.
      */
     public ParamDomainFormationController() {
         super();
         temEnSveItems = new ArrayList<SelectItem>();
         allLangue = new ArrayList<SelectItem>();
     }
 
 	
 /*
  * ******************* RESET ************************* */
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
         actionEnum = null;
         actionLang = null;
         domain = null;
         listDomain = null;
         langueSelected = null;
         libSaisi = null;
         allLangue.clear();
     }
 
 	
 /*
  * ******************* CALLBACK ************************* */
 
     /**
      * Callback to domain list.
      *
      * @return String
      */
     public String goSeeAllDom() {
         reset();
         return NavigationRulesConst.DISPLAY_DOMAINES;
     }
 
     /**
      * Callback to add domain.
      *
      * @return String
      */
     public String goAddDom() {
         reset();
         getActionEnum().setWhatAction(ActionEnum.ADD_ACTION);
         return null;
     }
 
 	
 /*
  * ******************* ADD UPDATE DELETE ************************* */
 
     /**
      * Add a Domain to the dataBase.
      */
     @SuppressWarnings("unchecked")
     public void add() {
         if (testErreurSave()) {
             return;
         }
 
         if (log.isDebugEnabled()) {
             log.debug("enterind add with domain = " + domain.getDomaineAnnuForm().getCodDom());
         }
 
         //save DomaineAnnuForm
         iApogee.save(domain.getDomaineAnnuForm());
 
         for (Domaine2AnnuForm langLib : domain.getDomaine2AnnuForm()) {
             langLib.setCodDom(domain.getDomaineAnnuForm().getCodDom());
             langLib.setDomaineAnnuForm(domain.getDomaineAnnuForm());
             //save Domaine2AnnuForm
             iApogee.save(langLib);
         }
 
         getListDomain().add(domain);
         Collections.sort(getListDomain(), new BeanComparator("domaineAnnuForm",
                 new BeanComparator("codDom", new NullComparator())));
         reset();
 
         addInfoMessage(null, "INFO.ENTER.SUCCESS");
         if (log.isDebugEnabled()) {
             log.debug("leaving add");
         }
     }
 
     /**
      * Update a fonction to the dataBase.
      */
     public void update() {
         if (testErreurUpdate()) {
             return;
         }
 
         if (log.isDebugEnabled()) {
             log.debug("enterind update with domain = " + domain.getDomaineAnnuForm().getCodDom());
         }
 
         for (Domaine2AnnuForm langLib : domain.getDomaine2AnnuForm()) {
             langLib.setCodDom(domain.getDomaineAnnuForm().getCodDom());
             langLib.setDomaineAnnuForm(domain.getDomaineAnnuForm());
             //save or update Domaine2AnnuForm
             iApogee.saveOrUpdate(langLib);
         }
         //update DomaineAnnuForm
         iApogee.update(domain.getDomaineAnnuForm());
         reset();
 
         if (log.isDebugEnabled()) {
             log.debug("leaving update");
         }
     }
 
     /**
      * Delete a fonction to the dataBase.
      */
     public void delete() {
         if (log.isDebugEnabled()) {
             log.debug("enterind delete with domain = " + domain.getDomaineAnnuForm().getCodDom());
         }
 
         for (Domaine2AnnuForm langLib : domain.getDomaine2AnnuForm()) {
             //delete Domaine2AnnuForm
             iApogee.delete(langLib);
         }
         getListDomain().remove(domain);
         //delete DomaineAnnuForm
         iApogee.delete(domain.getDomaineAnnuForm());
         reset();
 
         if (log.isDebugEnabled()) {
             log.debug("leaving delete");
         }
     }
 	
 	
 /*
  * ******************* GESTION DES LIBELES ET DES LANGUES ************************* */
 
     /**
      *
      */
     public void addLangLib() {
         getActionLang().setWhatAction(ActionEnum.ADD_ACTION);
     }
 
     /**
      *
      */
     public void updateLangLib() {
         getActionLang().setWhatAction(ActionEnum.UPDATE_ACTION);
     }
 
     /**
      *
      */
     public void suppLangLib() {
         Domaine2AnnuForm langLibDelete = recupLangLib();
         if (langLibDelete != null) {
             getDomain().getDomaine2AnnuForm().remove(langLibDelete);
         }
     }
 
     /**
      *
      */
     public void validModLangLib() {
         if (libSaisi == null || libSaisi.equals("")) {
            addErrorMessage(FORMULAIRE_DOMAIN, "ERROR.FIELD.EMPTY", "Libelle");
             return;
         }
         recupLangLib().setLibDom(libSaisi);
         getActionLang().setWhatAction(ActionEnum.EMPTY_ACTION);
        langueSelected = "";
         libSaisi = null;
     }
 
     /**
      *
      */
     public void validAddLangLib() {
         if (libSaisi == null || libSaisi.equals("")) {
            addErrorMessage(FORMULAIRE_DOMAIN, "ERROR.FIELD.EMPTY", "Libelle");
             return;
         }
         Domaine2AnnuForm langLib = new Domaine2AnnuForm();
         langLib.setCodLang(langueSelected.toUpperCase());
         langLib.setLibDom(libSaisi);
         getDomain().getDomaine2AnnuForm().add(langLib);
         getActionLang().setWhatAction(ActionEnum.EMPTY_ACTION);
         libSaisi = null;
     }
 
     /**
      *
      */
     public void annulLangLib() {
         getActionLang().setWhatAction(ActionEnum.EMPTY_ACTION);
         libSaisi = null;
        langueSelected = "";
     }
 	
 	
 /*
  * ******************* TEST ************************* */
 
     /**
      * @return boolean
      */
     private boolean testErreurSave() {
         if (getDomain().getDomaineAnnuForm().getCodDom() == null || getDomain().getDomaineAnnuForm().getCodDom().equals("")) {
             addErrorMessage(FORMULAIRE_DOMAIN, "ERROR.FIELD.EMPTY", "Code");
             return true;
         }
 
         if (testDomain()) {
             addErrorMessage(FORMULAIRE_DOMAIN, "ERROR.FIELD.EXIST", "Domaine", "Code");
             return true;
         }
 
         return testErreurUpdate();
     }
 
     /**
      * @return boolean
      */
     private boolean testErreurUpdate() {
         if (getDomain().getDomaine2AnnuForm().isEmpty()) {
             addErrorMessage(FORMULAIRE_DOMAIN, "ERROR.LIST.EMPTY", "Libele en fonction de la langue");
             return true;
         }
 
         return false;
     }
 
     /**
      * @param langue
      * @return boolean
      */
     public boolean isNotExistLangueInDom(final String langue) {
         for (Domaine2AnnuForm langLib : getDomain().getDomaine2AnnuForm()) {
             if (langLib.getCodLang().equalsIgnoreCase(langue)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * @return boolean
      */
     private boolean testDomain() {
         String code = getDomain().getDomaineAnnuForm().getCodDom();
         for (DomaineAnnuFormPojo g : getListDomain()) {
             if (code.equals(g.getDomaineAnnuForm().getCodDom())) {
                 return true;
             }
         }
         return false;
     }
 	
 	
 /*
  * ******************* GETTERS ITEMS ************************* */
 
     /**
      * @return the list of temoins
      */
     public List<SelectItem> getTemEnSveItems() {
         if (temEnSveItems.isEmpty()) {
             for (String pageSizeValue : DEFAULT_TEMOIN_VALUES) {
                 temEnSveItems.add(new SelectItem(pageSizeValue));
             }
         }
         return temEnSveItems;
     }
 	
 	
 /*
  * ******************* OTHERS GETTERS ************************* */
 
     /**
      * @return liste de Items
      */
     @SuppressWarnings("unchecked")
     public List<SelectItem> getAllLangue() {
         allLangue.clear();
         String langue;
         for (SelectItem item : preferencesController.getLocaleItems()) {
             langue = ((Locale) item.getValue()).getLanguage();
             if (isNotExistLangueInDom(langue)) {
                 allLangue.add(new SelectItem(langue));
             }
         }
         return allLangue;
     }
 
     /**
      * @return Domaine2AnnuForm
      */
     public Domaine2AnnuForm recupLangLib() {
         for (Domaine2AnnuForm langLib : getDomain().getDomaine2AnnuForm()) {
             if (langLib.getCodLang().equalsIgnoreCase(langueSelected)) {
                 return langLib;
             }
         }
         return null;
     }
 	
 	
 /*
  * ******************* GETTERS ************************* */
 
     /**
      * @return the actionEnum
      */
     public ActionEnum getActionEnum() {
         if (actionEnum == null) {
             actionEnum = new ActionEnum();
         }
         return actionEnum;
     }
 
     /**
      * @return le domaine de formation selectionne
      */
     public DomaineAnnuFormPojo getDomain() {
         if (domain == null) {
             domain = new DomaineAnnuFormPojo();
             domain.setDomaineAnnuForm(new DomaineAnnuForm());
             domain.setDomaine2AnnuForm(new ArrayList<Domaine2AnnuForm>());
         }
         return domain;
     }
 
     /**
      * @return List(GrpTypDip)
      */
     @SuppressWarnings("unchecked")
     public List<DomaineAnnuFormPojo> getListDomain() {
         if (listDomain == null || listDomain.isEmpty()) {
             listDomain = new ArrayList<DomaineAnnuFormPojo>();
             for (DomaineAnnuForm dom : iApogee.getDomaineAnnuForm()) {
                 DomaineAnnuFormPojo domPojo = new DomaineAnnuFormPojo(dom, "fr");
                 domPojo.setDomaine2AnnuForm(iApogee.getDomaine2AnnuForm(dom.getCodDom()));
                 listDomain.add(domPojo);
             }
             Collections.sort(listDomain, new BeanComparator("domaineAnnuForm",
                     new BeanComparator("codDom", new NullComparator())));
         }
         return listDomain;
     }
 
     /**
      * @return actionLang
      */
     public ActionEnum getActionLang() {
         if (actionLang == null) {
             actionLang = new ActionEnum();
         }
         return actionLang;
     }
 
     /**
      * @return la langue selectionnee
      */
     public String getLangueSelected() {
         return langueSelected;
     }
 
     /**
      * @return le libele saisie
      */
     public String getLibSaisi() {
         return libSaisi;
     }
 
     /**
      * @return preferencesController
      */
     public PreferencesController getPreferencesController() {
         return preferencesController;
     }
 	
 	
 /*
  * ******************* SETTERS ************************* */
 
     /**
      * @param actionEnum the actionEnum to set
      */
     public void setActionEnum(final ActionEnum actionEnum) {
         this.actionEnum = actionEnum;
     }
 
     /**
      * @param domain
      */
     public void setDomain(final DomaineAnnuFormPojo domain) {
         this.domain = domain;
     }
 
     /**
      * @param actionLang
      */
     public void setActionLang(final ActionEnum actionLang) {
         this.actionLang = actionLang;
     }
 
     /**
      * @param langueSelected
      */
     public void setLangueSelected(final String langueSelected) {
         this.langueSelected = langueSelected;
     }
 
     /**
      * @param libSaisi
      */
     public void setLibSaisi(final String libSaisi) {
         this.libSaisi = libSaisi;
     }
 
     /**
      * @param preferencesController
      */
     public void setPreferencesController(final PreferencesController preferencesController) {
         this.preferencesController = preferencesController;
     }
 
     /**
      * @param iApogee the iApogee to set
      */
     public void setIApogee(final IApogee iApogee) {
         this.iApogee = iApogee;
     }
 
 }
