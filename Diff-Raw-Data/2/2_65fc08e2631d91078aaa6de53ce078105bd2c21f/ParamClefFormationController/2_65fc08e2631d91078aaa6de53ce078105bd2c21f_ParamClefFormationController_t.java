 /**
  *
  */
 package org.esupportail.opi.web.controllers.formation;
 
 import org.apache.commons.beanutils.BeanComparator;
 import org.apache.commons.collections.comparators.NullComparator;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.opi.domain.beans.formation.*;
 import org.esupportail.opi.services.remote.client.IApogee;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.pojo.ClesAnnuFormPojo;
 import org.esupportail.opi.web.beans.pojo.DomaineAnnuFormPojo;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.controllers.AbstractAccessController;
 import org.esupportail.opi.web.controllers.PreferencesController;
 import org.esupportail.wssi.services.remote.Diplome;
 
 import javax.faces.model.SelectItem;
 import java.util.*;
 
 /**
  * @author gomez
  */
 public class ParamClefFormationController extends AbstractAccessController {
 /*
  * ******************* PROPERTIES STATIC ******************* */
     /**
      * attribute serialVersionUID
      */
     private static final long serialVersionUID = 4756615930472850162L;
     /**
      *
      */
     protected static final String[] DEFAULT_TEMOIN_VALUES = {"O", "N"};
     /**
      *
      */
     private static final String FORMULAIRE_CLEF = "formAddClef";
     
 
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
      *
      */
     private List<DomaineAnnuFormPojo> listDomain;
     /**
      * The actionEnum.
      */
     private ActionEnum actionEnum;
     /**
      * The actionLang.
      */
     private ActionEnum actionLang;
     /**
      * The actionLang.
      */
     private ActionEnum actionDom;
     /**
      * clef de formation selectionnee.
      */
     private ClesAnnuFormPojo cles;
     /**
      * liste des domaines.
      */
     private List<ClesAnnuFormPojo> listCles;
     /**
      * langue selectionnee.
      */
     private String langueSelected;
     /**
      * libele saisie.
      */
     private String libSaisi;
     /**
      * map du libele en fonction du code de diplemes.
      */
     private Map<String, String> mapCodeDipLib;
     /**
      * temEnSveItems.
      */
     private List<SelectItem> temEnSveItems;
     /**
      * temEnSveItems.
      */
     private List<SelectItem> itemDomaine;
     /**
      * liste des langues.
      */
     private List<SelectItem> allLangue;
     /**
      * dipsItems.
      */
     private List<SelectItem> dipsItems;
     /**
      * allDipsItems.
      */
     private List<SelectItem> allDipsItems;
     /**
      * selectDipsDI.
      */
     private List<String> selectDipsDI;
     /**
      * selectDipsADI.
      */
     private List<String> selectDipsADI;
     /**
      * selectDoms.
      */
     private String selectDoms;
 
     private IApogee iApogee;
 
 	/*
  * ******************* CONSTRUCTOR ************************* */
 
     /**
      * Constructors.
      */
     public ParamClefFormationController() {
         super();
         temEnSveItems = new ArrayList<SelectItem>();
         itemDomaine = new ArrayList<SelectItem>();
         selectDipsDI = new ArrayList<String>();
         selectDipsADI = new ArrayList<String>();
         allLangue = new ArrayList<SelectItem>();
         mapCodeDipLib = new HashMap<String, String>();
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
         actionDom = null;
         cles = null;
         listCles = null;
         langueSelected = null;
         libSaisi = null;
         dipsItems = null;
         allDipsItems = null;
         allLangue.clear();
     }
 
 /*
  * ******************* CALLBACK ************************* */
 
     /**
      * Callback to domain list.
      *
      * @return String
      */
     public String goSeeAllKeys() {
         reset();
         return NavigationRulesConst.DISPLAY_KEYWORDS;
     }
 
     /**
      * Callback to add domain.
      *
      * @return String
      */
     public String goAddKeys() {
         reset();
         getActionEnum().setWhatAction(ActionEnum.ADD_ACTION);
         return null;
     }
 
 /*
  * ******************* ADD ET UPDATE ************************* */
 
     /**
      * Add a Domain to the dataBase.
      */
     @SuppressWarnings("unchecked")
     public void add() {
         if (testErreurSave()) {
             return;
         }
 
         if (log.isDebugEnabled()) {
             log.debug("enterind add with domain = " + getCles().getClesAnnuForm().getCodCles());
         }
 
         //save ClesAnnuForm
         iApogee.save(getCles().getClesAnnuForm());
 
         for (Cles2AnnuForm langLib : getCles().getCles2AnnuForm()) {
             langLib.getId().setCodCles(getCles().getClesAnnuForm().getCodCles());
             langLib.setClesAnnuForm(getCles().getClesAnnuForm());
             //save Cles2AnnuForm
             iApogee.save(langLib);
         }
         for (SelectItem dipItem : dipsItems) {
             ClesDiplomeAnnuForm cleDip = new ClesDiplomeAnnuForm();
             cleDip.setCodCles(getCles().getClesAnnuForm().getCodCles());
             cleDip.setCodDip((String) dipItem.getValue());
             getCles().getClesDiplomeAnnuForm().add(cleDip);
             //save Cles2AnnuForm
             iApogee.save(cleDip);
         }
 
         getListCles().add(getCles());
         Collections.sort(getListCles(), new BeanComparator("ClesAnnuForm",
                 new BeanComparator("codCles", new NullComparator())));
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
             log.debug("enterind update with domain = " + getCles().getClesAnnuForm().getCodCles());
         }
 
         for (Cles2AnnuForm langLib : getCles().getCles2AnnuForm()) {
             langLib.getId().setCodCles(getCles().getClesAnnuForm().getCodCles());
             langLib.setClesAnnuForm(getCles().getClesAnnuForm());
            if (langLib.getLibCles().length()>50){
             	addErrorMessage(FORMULAIRE_CLEF, "ERROR.FIELD.TOO_LONG","Libelle",50);
             	return;
             }
             //save or update Cles2AnnuForm
             iApogee.saveOrUpdate(langLib);
         }
 
         int index;
         List<ClesDiplomeAnnuForm> listDip = new ArrayList<ClesDiplomeAnnuForm>();
 
         for (ClesDiplomeAnnuForm dip : getCles().getClesDiplomeAnnuForm()) {
             index = getExistDipsItems(dip.getCodDip());
             if (index == -1) {
                 listDip.add(dip);
             } else {
                 dipsItems.remove(index);
             }
         }
 
         for (ClesDiplomeAnnuForm dip : listDip) {
             getCles().getClesDiplomeAnnuForm().remove(dip);
             //delete Cles2AnnuForm
             iApogee.delete(dip);
         }
 
         for (SelectItem dipItem : dipsItems) {
             ClesDiplomeAnnuForm cleDip = new ClesDiplomeAnnuForm();
             cleDip.setCodCles(getCles().getClesAnnuForm().getCodCles());
             cleDip.setCodDip((String) dipItem.getValue());
             getCles().getClesDiplomeAnnuForm().add(cleDip);
             //save Cles2AnnuForm
             iApogee.save(cleDip);
         }
 
         //update ClesAnnuForm
         iApogee.update(getCles().getClesAnnuForm());
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
             log.debug("enterind delete with domain = " + getCles().getClesAnnuForm().getCodCles());
         }
 
         for (Cles2AnnuForm langLib : getCles().getCles2AnnuForm()) {
             //delete Cles2AnnuForm
             iApogee.delete(langLib);
         }
 
         for (ClesDiplomeAnnuForm cleDip : getCles().getClesDiplomeAnnuForm()) {
             //delete ClesDiplomeAnnuForm
             iApogee.delete(cleDip);
         }
 
         getListCles().remove(cles);
         //delete ClesAnnuForm
         iApogee.delete(getCles().getClesAnnuForm());
         reset();
 
         if (log.isDebugEnabled()) {
             log.debug("leaving delete");
         }
     }
 	
 /*
  * ******************* TEST ************************* */
 
     /**
      * @return boolean
      */
     private boolean testErreurSave() {
         if (getCles().getClesAnnuForm().getCodCles() == null || getCles().getClesAnnuForm().getCodCles().equals("")) {
             addErrorMessage(FORMULAIRE_CLEF, "ERROR.FIELD.EMPTY", "Code");
             return true;
         }
 
         if (testCles()) {
             addErrorMessage(FORMULAIRE_CLEF, "ERROR.FIELD.EXISTE", "Cle", "Code");
             return true;
         }
 
         return testErreurUpdate();
     }
 
     /**
      * @return boolean
      */
     private boolean testErreurUpdate() {
         if (getCles().getDomaineAnnuFormPojo() == null) {
             addErrorMessage(FORMULAIRE_CLEF, "ERROR.FIELD.EMPTY", "Domaine");
             return true;
         }
 
         if (getCles().getCles2AnnuForm().isEmpty()) {
             addErrorMessage(FORMULAIRE_CLEF, "ERROR.LIST.EMPTY", "Libele en fonction de la langue");
             return true;
         }
 
         if (dipsItems.isEmpty()) {
             addErrorMessage(FORMULAIRE_CLEF, "ERROR.LIST.EMPTY", "Dipleme");
             return true;
         }
 
         return false;
     }
 
     /**
      * @param langue
      * @return boolean
      */
     public boolean isNotExistLangueInDom(final String langue) {
         for (Cles2AnnuForm langLib : getCles().getCles2AnnuForm()) {
             if (langLib.getId().getCodLang().equalsIgnoreCase(langue)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * @return boolean
      */
     private boolean testCles() {
         String code = getCles().getClesAnnuForm().getCodCles();
         for (ClesAnnuFormPojo c : getListCles()) {
             if (code.equals(c.getClesAnnuForm().getCodCles())) {
                 return true;
             }
         }
         return false;
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
         Cles2AnnuForm langLibDelete = recupLangLib();
         if (langLibDelete != null) {
             getCles().getCles2AnnuForm().remove(langLibDelete);
         }
     }
 
     /**
      *
      */
     public void validModLangLib() {
         if (libSaisi == null || libSaisi.equals("")) {
             addErrorMessage(FORMULAIRE_CLEF, "ERROR.FIELD.EMPTY", "Libele");
             return;
         }
         recupLangLib().setLibCles(libSaisi);
         getActionLang().setWhatAction(ActionEnum.EMPTY_ACTION);
         libSaisi = null;
     }
 
     /**
      *
      */
     public void validAddLangLib() {
         if (libSaisi == null || libSaisi.equals("")) {
             addErrorMessage(FORMULAIRE_CLEF, "ERROR.FIELD.EMPTY", "Libele");
             return;
         }
         Cles2AnnuForm langLib = new Cles2AnnuForm();
         Cles2AnnuFormId cles2Id = new Cles2AnnuFormId();
         cles2Id.setCodLang(langueSelected.toUpperCase());
         langLib.setId(cles2Id);
         langLib.setLibCles(libSaisi);
         getCles().getCles2AnnuForm().add(langLib);
         getActionLang().setWhatAction(ActionEnum.EMPTY_ACTION);
         libSaisi = null;
     }
 
     /**
      *
      */
     public void annulLangLib() {
         getActionLang().setWhatAction(ActionEnum.EMPTY_ACTION);
         libSaisi = null;
     }
 
 /*
  * ******************* GESTION DU DOMAINES ************************* */
 
     /**
      *
      */
     public void updateDomaines() {
         getActionDom().setWhatAction(ActionEnum.UPDATE_ACTION);
     }
 
     /**
      *
      */
     public void validDomaines() {
         if (selectDoms == null || selectDoms.equals("")) {
             addErrorMessage(FORMULAIRE_CLEF, "ERROR.FIELD.EMPTY", "Domaine");
             return;
         }
         getCles().getClesAnnuForm().setCodDom(selectDoms);
         getCles().setDomaineAnnuFormPojo(getDomaine(selectDoms));
         getActionDom().setWhatAction(ActionEnum.EMPTY_ACTION);
     }
 
     /**
      *
      */
     public void annulDomaines() {
         getActionDom().setWhatAction(ActionEnum.EMPTY_ACTION);
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
 
     /**
      * @return the list of temoins
      */
     @SuppressWarnings("unchecked")
     public List<SelectItem> getItemDomaine() {
         if (itemDomaine.isEmpty()) {
             for (DomaineAnnuFormPojo dom : getListDomain()) {
                 itemDomaine.add(new SelectItem(dom.getDomaineAnnuForm().getCodDom()));
             }
             Collections.sort(itemDomaine, new BeanComparator("value", new NullComparator()));
         }
         return itemDomaine;
     }
 
     /**
      * @return the list of diplemes
      */
     @SuppressWarnings("unchecked")
     public List<SelectItem> getDipsItems() {
         if (dipsItems == null) {
             dipsItems = new ArrayList<SelectItem>();
             List<ClesDiplomeAnnuForm> listDiplomes = getCles().getClesDiplomeAnnuForm();
             if (listDiplomes != null) {
                 for (ClesDiplomeAnnuForm dip : listDiplomes) {
                     dipsItems.add(new SelectItem(dip.getCodDip(), dip.getCodDip() + " (" + getMapCodeDipLib().get(dip.getCodDip()) + ")"));
                 }
             }
             Collections.sort(dipsItems, new BeanComparator("value", new NullComparator()));
         }
         return dipsItems;
     }
 
     /**
      * @return the list of diplemes
      */
     @SuppressWarnings("unchecked")
     public List<SelectItem> getAllDipsItems() {
         if (allDipsItems == null) {
             allDipsItems = new ArrayList<SelectItem>();
             List<Diplome> allDiplomes = getAllDiplome();
             if (allDiplomes != null) {
                 for (Diplome dip : allDiplomes) {
                     if (getExistDipsItems(dip.getCodDip()) == -1) {
                         allDipsItems.add(new SelectItem(dip.getCodDip(), dip.getCodDip() + " (" + dip.getLibDip() + ")"));
                     }
                 }
             }
             Collections.sort(allDipsItems, new BeanComparator("value", new NullComparator()));
         }
         return allDipsItems;
     }
 
     /**
      * Ajoute la selection dans DipsItems.
      */
     @SuppressWarnings("unchecked")
     public void ajouDipsItems() {
         int index = -1;
         for (String c : selectDipsADI) {
             for (int i = 0; i < allDipsItems.size() && index == -1; i++) {
                 if (allDipsItems.get(i).getValue().equals(c)) {
                     index = i;
                 }
             }
             if (index >= 0) {
                 dipsItems.add(allDipsItems.get(index));
                 allDipsItems.remove(index);
             }
             index = -1;
         }
         Collections.sort(dipsItems, new BeanComparator("value", new NullComparator()));
         Collections.sort(allDipsItems, new BeanComparator("value", new NullComparator()));
         selectDipsADI.clear();
     }
 
     /**
      * Supprime la selection dans DipsItems.
      */
     @SuppressWarnings("unchecked")
     public void suppDipsItems() {
         int index = -1;
         for (String c : selectDipsDI) {
             for (int i = 0; i < dipsItems.size() && index == -1; i++) {
                 if (dipsItems.get(i).getValue().equals(c)) {
                     index = i;
                 }
             }
             if (index >= 0) {
                 allDipsItems.add(dipsItems.get(index));
                 dipsItems.remove(index);
             }
             index = -1;
         }
         Collections.sort(dipsItems, new BeanComparator("value", new NullComparator()));
         Collections.sort(allDipsItems, new BeanComparator("value", new NullComparator()));
         selectDipsDI.clear();
     }
 	
 /*
  * ******************* OTHERS GETTERS ************************* */
 
     /**
      * @param codDip
      * @return integer
      */
     @SuppressWarnings("cast")
     private int getExistDipsItems(final String codDip) {
         for (int i = 0; i < getDipsItems().size(); i++) {
             if (codDip.equals((String) getDipsItems().get(i).getValue())) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * @return List(TypDiplome)
      */
     public List<Diplome> getAllDiplome() {
         return getDomainApoService().getAllDiplomes();
     }
 
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
         Collections.sort(allLangue, new BeanComparator("value", new NullComparator()));
         return allLangue;
     }
 
     /**
      * @return Domaine2AnnuForm
      */
     public Cles2AnnuForm recupLangLib() {
         for (Cles2AnnuForm langLib : getCles().getCles2AnnuForm()) {
             if (langLib.getId().getCodLang().equalsIgnoreCase(langueSelected)) {
                 return langLib;
             }
         }
         return null;
     }
 
     /**
      * @param codDom
      * @return the list of temoins
      */
     public DomaineAnnuFormPojo getDomaine(String codDom) {
         for (DomaineAnnuFormPojo dom : getListDomain()) {
             if (dom.getDomaineAnnuForm().getCodDom().equals(codDom)) {
                 return dom;
             }
         }
         return null;
     }
 	
 /*
  * ******************* OTHERS GETTERS ************************* */
 
     /**
      * @return List(GrpTypDip)
      */
     @SuppressWarnings("unchecked")
     public List<ClesAnnuFormPojo> getListCles() {
         if (listCles == null) {
             listCles = new ArrayList<ClesAnnuFormPojo>();
             for (ClesAnnuForm cle : iApogee.getClesAnnuForm()) {
                 ClesAnnuFormPojo clePojo = new ClesAnnuFormPojo(cle, "fr");
                 clePojo.setCles2AnnuForm(iApogee.getCles2AnnuForm(cle.getCodCles()));
                 clePojo.setClesDiplomeAnnuForm(iApogee.getClesDiplomeAnnuForm(cle.getCodCles()));
                 clePojo.setDomaineAnnuFormPojo(getDomaineAnnuFormPojo(cle.getCodDom()));
                 listCles.add(clePojo);
             }
             Collections.sort(getListCles(), new BeanComparator("clesAnnuForm",
                     new BeanComparator("codCles", new NullComparator())));
         }
         return listCles;
     }
 
     public DomaineAnnuFormPojo getDomaineAnnuFormPojo(final String codDom) {
         DomaineAnnuFormPojo domPojo = new DomaineAnnuFormPojo(iApogee.getDomaineAnnuForm(codDom), "fr");
         domPojo.setDomaine2AnnuForm(iApogee.getDomaine2AnnuForm(domPojo.getDomaineAnnuForm().getCodDom()));
         return domPojo;
     }
 	
 	
 /*
  * ******************* GETTERS ************************* */
 
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
             Collections.sort(listDomain, new BeanComparator("DomaineAnnuForm",
                     new BeanComparator("codDom", new NullComparator())));
         }
         return listDomain;
     }
 
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
      * @return mapCodeDipLib
      */
     public Map<String, String> getMapCodeDipLib() {
         if (mapCodeDipLib.isEmpty()) {
             for (Diplome dip : getAllDiplome()) {
                 mapCodeDipLib.put(dip.getCodDip(), dip.getLibDip());
             }
         }
         return mapCodeDipLib;
     }
 
     /**
      * @return ActionEnum
      */
     public ActionEnum getActionLang() {
         if (actionLang == null) {
             actionLang = new ActionEnum();
         }
         return actionLang;
     }
 
     /**
      * @return ActionEnum
      */
     public ActionEnum getActionDom() {
         if (actionDom == null) {
             actionDom = new ActionEnum();
         }
         return actionDom;
     }
 
     /**
      * @return selectDoms
      */
     public String getSelectDoms() {
         return selectDoms;
     }
 
     /**
      * @return la clef de formation selectionnee
      */
     public ClesAnnuFormPojo getCles() {
         if (cles == null) {
             cles = new ClesAnnuFormPojo();
             cles.setClesAnnuForm(new ClesAnnuForm());
             cles.setCles2AnnuForm(new ArrayList<Cles2AnnuForm>());
             cles.setClesDiplomeAnnuForm(new ArrayList<ClesDiplomeAnnuForm>());
         }
         return cles;
     }
 
     /**
      * @return preferencesController
      */
     public PreferencesController getPreferencesController() {
         return preferencesController;
     }
 
     /**
      * @return langueSelected
      */
     public String getLangueSelected() {
         return langueSelected;
     }
 
     /**
      * @return libSaisi
      */
     public String getLibSaisi() {
         return libSaisi;
     }
 
     /**
      * @return selectDipsDI
      */
     public List<String> getSelectDipsDI() {
         return selectDipsDI;
     }
 
     /**
      * @return selectDipsADI
      */
     public List<String> getSelectDipsADI() {
         return selectDipsADI;
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
      * @param actionLang
      */
     public void setActionLang(ActionEnum actionLang) {
         this.actionLang = actionLang;
     }
 
     /**
      * @param actionDom
      */
     public void setActionDom(ActionEnum actionDom) {
         this.actionDom = actionDom;
     }
 
     /**
      * @param cles
      */
     public void setCles(final ClesAnnuFormPojo cles) {
         this.cles = cles;
     }
 
     /**
      * @param preferencesController
      */
     public void setPreferencesController(PreferencesController preferencesController) {
         this.preferencesController = preferencesController;
     }
 
     /**
      * @param langueSelected
      */
     public void setLangueSelected(String langueSelected) {
         this.langueSelected = langueSelected;
     }
 
     /**
      * @param libSaisi
      */
     public void setLibSaisi(String libSaisi) {
         this.libSaisi = libSaisi;
     }
 
     /**
      * @param selectDipsDI
      */
     public void setSelectDipsDI(List<String> selectDipsDI) {
         this.selectDipsDI = selectDipsDI;
     }
 
     /**
      * @param selectDipsADI
      */
     public void setSelectDipsADI(List<String> selectDipsADI) {
         this.selectDipsADI = selectDipsADI;
     }
 
     /**
      * @param selectDoms
      */
     public void setSelectDoms(String selectDoms) {
         this.selectDoms = selectDoms;
     }
 
     /**
      * @param iApogee the iApogee to set
      */
     public void setIApogee(final IApogee iApogee) {
         this.iApogee = iApogee;
     }
 }
