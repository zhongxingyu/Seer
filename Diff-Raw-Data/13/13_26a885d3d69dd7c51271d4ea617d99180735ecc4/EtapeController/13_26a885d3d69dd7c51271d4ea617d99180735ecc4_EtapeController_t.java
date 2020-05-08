 /**
  *
  */
 package org.esupportail.opi.web.controllers.references;
 
 
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.opi.domain.beans.NormeSI;
 import org.esupportail.opi.domain.beans.parameters.Campagne;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.web.beans.BeanTrtCmi;
 import org.esupportail.opi.web.beans.beanEnum.WayfEnum;
 import org.esupportail.opi.web.beans.paginator.VETPaginator;
 import org.esupportail.opi.web.beans.pojo.PieceJustiVetPojo;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractContextAwareController;
 import org.esupportail.opi.web.controllers.parameters.NomenclatureController;
 import org.esupportail.wssi.services.remote.CentreGestion;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.primefaces.component.datatable.DataTable;
 import org.springframework.util.StringUtils;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.jsf.FacesContextUtils;
 
 import javax.faces.context.FacesContext;
 import java.util.*;
 
 
 /**
  * @author cleprous
  */
 public class EtapeController extends AbstractContextAwareController {
 
 
     /**
      * The serialization id.
      */
     private static final long serialVersionUID = -385061645426193790L;
 
 	
 	/*
      ******************* PROPERTIES ******************* */
 
     /**
      * The etape code.
      */
     private String codEtp;
 
     /**
      * The centre gestion code.
      */
     private String codCge;
 
     /**
      * The code de l'année universitaire.
      */
     private String codAnu;
 
     /**
      * The version Etape label.
      */
     private String libWebVet;
 
     /**
      * The version etape.
      */
     private List<VersionEtapeDTO> etapes;
 
     /**
      * The manager of the versionEtape to add to the cmi.
      */
     private Object[] objectToAdd;
 
     /**
      * Liste des campagnes en service.
      */
     private List<Campagne> campagnes;
 
     /**
      * Has true if all VersionEtape in etapes is selected.
      * Default value = false
      */
     private Boolean allChecked;
 
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
 
     /**
      * From where you are from.
      */
     private WayfEnum wayfEnum;
 
 	
 	/*
 	 ******************* INIT ************************* */
 
     /**
      * Paginator for the vets
      */
     private VETPaginator paginator;
     
 
    
 	/**
      * Constructors.
      */
     public EtapeController() {
         super();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
         codEtp = null;
         libWebVet = null;
         codCge = null;
         etapes = new ArrayList<VersionEtapeDTO>();
         allChecked = false;
         codAnu = null;
         campagnes = new ArrayList<Campagne>();
         objectToAdd = new Object[0];
 //		paginator.reset();
         this.wayfEnum = new WayfEnum();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#afterPropertiesSetInternal()
      */
     @Override
     public void afterPropertiesSetInternal() {
         reset();
     }
 	
     public void forcereload(){
     	paginator.forceReload();
     }
     
 	/*
 	 ******************* CALLBACK ********************** */
 
     /**
      * Callback to search version etape.
      *
      * @return String
      */
     public String goSearchEtpForCmi() {
         reset();
         codCge = getCurrentGest().getCodeCge();
         // on initialise la liste de campagne
         Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         int codeRI = gest.getProfile().getCodeRI();
         campagnes.addAll(getParameterService().getCampagnes(null,
                 String.valueOf(codeRI)));
         codAnu = getParameterService().getCampagneEnServ(codeRI).getCodAnu();
         // define from where we go to search Vet
         this.wayfEnum.setWhereAreYouFrom(this.wayfEnum.getMemberCmiValue());
         return NavigationRulesConst.SEARCH_VET;
     }
 
     /**
      * Callback to search version etape to add PJ.
      *
      * @return String
      */
     public String goSearchVetForGestPJ() {
         reset();
        WebApplicationContext wac = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        NomenclatureController nc = (NomenclatureController) wac.getBean("nomenclatureController");
        nc.reset();
         codCge = getCurrentGest().getCodeCge();
         // on initialise la liste de campagne
         Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         paginator.getRvd().setCodCge(getCurrentGest().getCodeCge());
         // on initialise la liste de campagne
         int codeRI = gest.getProfile().getCodeRI();
         if (paginator.getRvd().getCampagnes() != null)
             paginator.getRvd().getCampagnes().addAll(getParameterService().getCampagnes(null,
                     String.valueOf(codeRI)));
         paginator.getRvd().setCodAnu(getParameterService().getCampagneEnServ(codeRI).getCodAnu());
         paginator.getRvd().setIdCmi(null);
         paginator.getRvd().setCodeVet("");
         paginator.getRvd().setLibWebVet("");
 
         // define from where we go to search Vet
         //this.wayfEnum.setWhereAreYouFrom(this.wayfEnum.getMemberCmiValue());
         return NavigationRulesConst.SEARCH_VET_GEST_PJ;
     }
 
     /**
      * @return the list of PJs for the VET
      */
     public String goBackVET() {
         reset();
         WebApplicationContext wac = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
         NomenclatureController nc = (NomenclatureController) wac.getBean("nomenclatureController");
         nc.resetSpecial();
         nc.getPiecesJToNomenclaturePojo();
         return NavigationRulesConst.ENTER_VET;
     }
 
     /**
      * the list of PJs for the VET with a VET
      *
      * @return a String
      */
     public String goEnterVET() {
         reset();
         WebApplicationContext wac = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
         NomenclatureController nc = (NomenclatureController) wac.getBean("nomenclatureController");
         nc.getPiecesJToNomenclaturePojo();
         return NavigationRulesConst.ENTER_VET;
     }
 
 
     /**
      * Go back to form add PJ.
      *
      * @return String
      */
     public String goBackSearchEtape() {
         String callback = null;
         if (wayfEnum.getWhereAreYouFrom().equals(WayfEnum.PJ_VALUE)) {
             callback = NavigationRulesConst.ENTER_PJ;
         } else if (wayfEnum.getWhereAreYouFrom().equals(WayfEnum.AFFECT_PJ_VALUE)) {
             callback = NavigationRulesConst.AFFECT_PJ;
         } else if (wayfEnum.getWhereAreYouFrom().equals(WayfEnum.MEMBER_CMI_VALUE)) {
             callback = NavigationRulesConst.MANAGED_TRT_CMI;
         } else if (wayfEnum.getWhereAreYouFrom().equals(WayfEnum.EMPTY_VALUE)) {
             callback = NavigationRulesConst.DISPLAY_FONCTION;
         }
         reset();
         return callback;
     }
 	
 	
 
 	/*
 	 ******************* METHODS ********************** */
 
     /**
      * Look for the Version Etape by codEtp and/or libEtp and/or codCge.
      */
     public void searchEtape() {
         if (StringUtils.hasText(codCge) && StringUtils.hasLength(codAnu)) {
             etapes = getDomainApoService().getVersionEtapes(codEtp, libWebVet, codCge, codAnu);
             Collections.sort(etapes, new ComparatorString(VersionEtapeDTO.class));
         } else {
             if (!StringUtils.hasText(codCge)) {
                 addErrorMessage(null, "ERROR.FIELD.EMPTY", getString("FIELD_LABEL.CGE"));
             }
             if (!StringUtils.hasText(codAnu)) {
                 addErrorMessage(null, "ERROR.FIELD.EMPTY", getString("FIELD_LABEL.CAMPAGNES"));
             }
         }
 
     }
 
     /**
      * @return commissions by right
      */
     public Set<Commission> getCommissionsItemsByRight() {
         Set<Commission> cmi = new TreeSet<Commission>(new ComparatorString(NormeSI.class));
 
         cmi.addAll(getDomainApoService().getListCommissionsByRight(
                 getCurrentGest(),
                 true));
         return cmi;
 
     }
 
 
     /**
      * @return commissions by right
      */
     public List<CentreGestion> getCGEItemsByRight() {
         List<CentreGestion> cge = new ArrayList<CentreGestion>();
 
         cge.addAll(Utilitaires.getListCgeByRight(getCurrentGest(), getDomainApoService()));
         Collections.sort(cge, new ComparatorString(CentreGestion.class));
 
         return cge;
     }
 
     /**
      * Look for the Version Etape by codEtp and/or libEtp.
      *
      * @return List < BeanTrtCmi>
      */
     public List<BeanTrtCmi> searchEtapeInCmi() {
         Set<Commission> c = getDomainApoService().getListCommissionsByRight(
                 getCurrentGest(), true);
         // on récupère les campagnes en service
         Set<Campagne> camps = getParameterService().getCampagnes(true,
                 String.valueOf(getCurrentGest().getProfile().getCodeRI()));
         //on recupere la vetDTO
         etapes.clear();
         for (Campagne camp : camps) {
             etapes.addAll(getDomainApoService().getVersionEtapes(codEtp, libWebVet, null,
                     camp.getCodAnu()));
         }
         List<BeanTrtCmi> listTrt = new ArrayList<BeanTrtCmi>();
         for (Commission cmi : c) {
             for (TraitementCmi t : cmi.getTraitementCmi()) {
                 for (VersionEtapeDTO vDTO : etapes) {
                     if (t.getVersionEtpOpi().getCodEtp().equals(vDTO.getCodEtp())
                             && t.getVersionEtpOpi().getCodVrsVet()
                             .equals(vDTO.getCodVrsVet())) {
                         //init proxy hib
                         t.setCommission(cmi);
                         BeanTrtCmi b = new BeanTrtCmi(t, null);
                         b.setEtape(vDTO);
                         listTrt.add(b);
                         break;
                     }
                 }
 
             }
         }
         reset();
         if (log.isDebugEnabled()) {
             log.debug("leaving searchEtapeInCmi with listTrt = " + listTrt);
         }
         return listTrt;
 
     }
     
 
     /**
      * 
      */
     public void lookForVets() {
     	paginator.lookForVets();
         etapes = paginator.getData();
     }
 
     /**
      * @return boolean
      */
     private boolean isVuPjEtp() {
         return wayfEnum.getWhereAreYouFrom().equals(wayfEnum.getPJValue())
                 || wayfEnum.getWhereAreYouFrom().equals(wayfEnum.getAffectPJValue());
     }
 
     /**
      * @return boolean
      */
     public boolean isRightOnCge() {
         return isVuPjEtp()
                 && StringUtils.hasText(getCurrentGest().getCodeCge())
                 && !getSessionController().isAllViewPJ();
     }
 
     /**
      * @return boolean
      */
     public boolean isRightOneEtp() {
         return isVuPjEtp()
                 && getCurrentGest().getRightOnCmi() != null
                 && !getCurrentGest().getRightOnCmi().isEmpty()
                 && !getSessionController().isAllViewPJ();
     }
 
     public List<CentreGestion> getCentreGestion() {
         if (isRightOnCge()) {
             List<CentreGestion> listCge = new ArrayList<CentreGestion>();
             for (CentreGestion cge : getDomainApoService().getCentreGestion()) {
                 if (cge.getCodCge().equals(getCurrentGest().getCodeCge())) {
                     listCge.add(cge);
                     return listCge;
                 }
             }
         }
 
         return getDomainApoService().getCentreGestion();
     }
 
     /**
      * @return the code of the current user's inscription regime
      */
     public int getCodeRI() {
         Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         return gest.getProfile().getCodeRI();
 
     }
 
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 
 
     /**
      * @return the codEtp
      */
     public String getCodEtp() {
         return codEtp;
     }
 
     /**
      * @param codEtp the codEtp to set
      */
     public void setCodEtp(final String codEtp) {
         this.codEtp = codEtp;
     }
 
 
     /**
      * @return the codCge
      */
     public String getCodCge() {
         return codCge;
     }
 
     /**
      * @param codCge the codCge to set
      */
     public void setCodCge(final String codCge) {
         this.codCge = codCge;
     }
 
     /**
      * @return the etapes
      */
     public List<VersionEtapeDTO> getEtapes() {
         return etapes;
     }
 
     /**
      * @param etapes the etapes to set
      */
     public void setEtapes(final List<VersionEtapeDTO> etapes) {
         this.etapes = etapes;
     }
 
     /**
      * @return the allChecked
      */
     public Boolean getAllChecked() {
         return allChecked;
     }
 
     /**
      * @param allChecked the allChecked to set
      */
     public void setAllChecked(final Boolean allChecked) {
         this.allChecked = allChecked;
     }	
 
     /**
      * @return the wayfEnum
      */
     public WayfEnum getWayfEnum() {
         return wayfEnum;
     }
 
     /**
      * @param wayfEnum the wayfEnum to set
      */
     public void setWayfEnum(final WayfEnum wayfEnum) {
         this.wayfEnum = wayfEnum;
     }
 
     /**
      * @return the libWebVet
      */
     public String getLibWebVet() {
         return libWebVet;
     }
 
     /**
      * @param libWebVet the libWebVet to set
      */
     public void setLibWebVet(final String libWebVet) {
         this.libWebVet = libWebVet;
     }
 
     /**
      * @return the objectToAdd
      */
     public Object[] getObjectToAdd() {
         return objectToAdd;
     }
 
     /**
      * @param objectToAdd the objectToAdd to set
      */
     public void setObjectToAdd(final Object[] objectToAdd) {
         this.objectToAdd = objectToAdd;
     }
 
     /**
      * @return the codAnu
      */
     public String getCodAnu() {
         return codAnu;
     }
 
     /**
      * @param codAnu the codAnu to set
      */
     public void setCodAnu(final String codAnu) {
         this.codAnu = codAnu;
     }
 
     /**
      * @return the campagnes
      */
     public List<Campagne> getCampagnes() {
         return campagnes;
     }
 
     /**
      * @param campagnes the campagnes to set
      */
     public void setCampagnes(final List<Campagne> campagnes) {
         this.campagnes = campagnes;
     }
 
 
     /**
      * @return the paginator for the vets
      */
     public VETPaginator getPaginator() {
         return paginator;
     }
 
     /**
      * @param paginator
      */
     public void setPaginator(VETPaginator paginator) {
         this.paginator = paginator;
     }
 
     /**
 	 * Return all PieceJustiVetPojo in use.
 	 * @return List< PieceJustiVetPojo>
 	 */
 	public List<VersionEtapeDTO> getAllEtapesItems() {
 		return etapes;
 	}
 	
 	/**
 	 * @param allEtapes the allEtapes to set
 	 */
 	public void setAllEtapes(final List<VersionEtapeDTO> etapes) {
 		this.etapes = etapes;
 	}
 	
 }
