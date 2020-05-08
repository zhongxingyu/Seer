 /**
 *CRI - Universite de Rennes1 - 57SI-OPI - 2008
 * ????
 * Version de la norme de developpement : 0.9.0
 */
 /**
  * 
  */
 package org.esupportail.opi.web.beans.pojo;
 
 import org.esupportail.commons.services.i18n.I18nService;
 import org.esupportail.opi.domain.beans.etat.*;
 import org.esupportail.opi.domain.beans.parameters.TypeTraitement;
 import org.esupportail.opi.domain.beans.references.rendezvous.CalendarRDV;
 import org.esupportail.opi.domain.beans.references.rendezvous.IndividuDate;
 import org.esupportail.opi.domain.beans.user.candidature.Avis;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.springframework.util.StringUtils;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 
 
 /**
  * @author leproust cedric
  *
  */
 public class IndVoeuPojo implements Serializable {
 
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = 3401681855841611752L;
 	
 	
 	/*
 	 ******************* PROPERTIES ******************* */
 	/**
 	 * The IndVoeu.
 	 */
 	private IndVoeu indVoeu;
 	
 	/**
 	 * The VersionEtape.
 	 */
 	private VersionEtapeDTO vrsEtape;
 	
 	/**
 	 * The vows state. 
 	 */
 	private EtatVoeu etat;
 	
 	/**
 	 * Default value false.
 	 */
 	private Boolean calIsOpen;
 	
 	/**
 	 * see {@link TypeTraitement}.
 	 */
 	private TypeTraitement typeTraitement;
 	
 	/**
 	 * new opinion for the indVoeu.
 	 */
 	private Avis newAvis;
 	
 	/**
 	 * Avis en service.
 	 */
 	private Avis avisEnService;
 	
 	/**
 	 * true if the type of decision is LC.
 	 */
 	private Boolean isUsingLC;
 	
 	/**
 	 * true if the type of decision is DEF.
 	 */
 	private Boolean isUsingDEF;
 	
 	/**
 	 * The state of the confirmation.
 	 */
 	private String stateConf;
 	
 	/**
 	 * false if the current user can confirm.
 	 * depends of the date of confirmation of the commission,
 	 * the state of the voeu and if the user is a manager
 	 */
 	private Boolean disableConfirm;
 	
 	/**
 	 * The individuDate.
 	 */
 	private IndividuDate individuDate;
 	
 	/**
 	 * calendrier de rendez-vous.
 	 */
 	private CalendarRDV calendrierRdv;
 
 	private List<Avis> avisAsList;
 	
 
 	 // ******************* INIT *************************
 
     public IndVoeuPojo(final IndVoeu indVoeu, final VersionEtapeDTO vrsEtp,
                        final EtatVoeu etat, final Boolean calIsopen,
                        final TypeTraitement typeTraitement, final CalendarRDV calendrierRdv) {
         this.indVoeu = indVoeu;
         newAvis = new Avis();
         this.vrsEtape = vrsEtp;
         this.calIsOpen = calIsopen;
         this.etat = etat;
         this.typeTraitement = typeTraitement;
         isUsingLC = false;
         isUsingDEF = false;
         stateConf = "";
         initAvisInUse();
         this.calendrierRdv = calendrierRdv;
     }
 
 
     /**
 	 * Constructors.
      *
 	 * @param indVoeu
 	 * @param vrsEtp
 	 * @param i18Service
 	 * @param calIsopen
 	 * @param typeTraitement
      *
      * @deprecated Use {@link IndVoeuPojo#IndVoeuPojo(org.esupportail.opi.domain.beans.user.candidature.IndVoeu,
      * org.esupportail.wssi.services.remote.VersionEtapeDTO, org.esupportail.opi.domain.beans.etat.EtatVoeu, Boolean, org.esupportail.opi.domain.beans.parameters.TypeTraitement, org.esupportail.opi.domain.beans.references.rendezvous.CalendarRDV)}
 	 */
     @Deprecated
 	public IndVoeuPojo(final IndVoeu indVoeu, final VersionEtapeDTO vrsEtp,
 			final I18nService i18Service, final Boolean calIsopen, 
 			final TypeTraitement typeTraitement, final CalendarRDV calendrierRdv) {
 		super();
 		this.indVoeu = indVoeu;
 		newAvis = new Avis();
 		this.vrsEtape = vrsEtp;
 		this.calIsOpen = calIsopen;
 		if (i18Service != null) {
 			initEtat(indVoeu.getState(), i18Service);
 		}
 		this.typeTraitement = typeTraitement;
 		isUsingLC = false;
 		isUsingDEF = false;
 		stateConf = "";
 		initAvisInUse();
 		this.calendrierRdv = calendrierRdv;
 	}
 	
 	/**
 	 * Constructor.
 	 */
 	public IndVoeuPojo() {
 		super();
 		calIsOpen = false;
 		newAvis = new Avis();
 		isUsingLC = false;
 		isUsingDEF = false;
 		stateConf = "";
 		//initAvisInUse();
 	}
 
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "IndVoeuPojo#" + hashCode() + "[indVoeu: " + indVoeu + "], [etat: " + etat + "]";
 	}
 
 	/** 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((indVoeu == null) ? 0 : indVoeu.hashCode());
 		result = prime * result
 				+ ((vrsEtape == null) ? 0 : vrsEtape.hashCode());
 		return result;
 	}
 
 
 	/** 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) { return true; }
 		if (obj == null) { return false; }
 		if (getClass() != obj.getClass()) {	return false; }
 		IndVoeuPojo other = (IndVoeuPojo) obj;
 		if (indVoeu == null) {
 			if (other.indVoeu != null) { return false; }
 		} else if (!indVoeu.equals(other.indVoeu)) { return false; }
 		if (vrsEtape == null) {
 			if (other.vrsEtape != null) { return false; }
 		} else if (!vrsEtape.getCodEtp().equals(other.vrsEtape.getCodEtp()) 
 				&& !vrsEtape.getCodVrsVet().equals(other.vrsEtape.getCodVrsVet())) { 
 			return false; 
 		}
 		return true;
 	}
 
 	
 	
 	/*
 	 ******************* METHODS ********************** */
 	
 	
 	/**
 	 * @return String
 	 */
 	public Boolean getMyCalIsOpen() {
 		
 		return false;
 	}
 	
 	
 	/**
 	 * @return String
 	 */
 	public String getShortLibVet() {
 		return Utilitaires.limitStrLength(vrsEtape.getLibWebVet(),
                 Constantes.STR_LENGTH_LIMIT_SMALL);
 	}
 	
 	/**
 	 * Find and set the avis in use.
 	 */
 	public void initAvisInUse() {
 		if (this.indVoeu != null && this.indVoeu.getAvis() != null
 				&& !this.indVoeu.getAvis().isEmpty()) {
 			for (Avis a : this.indVoeu.getAvis()) {
 				if (a.getTemoinEnService()) { 
 					this.avisEnService =  a; 
 					break;
 				}
 			}
 		}
 	}
 	
 	public void initEtat(final String state, final I18nService i18Service) {
 		etat = (EtatVoeu) Etat.instanceState(state, i18Service);
 	}
 	
 	/**
 	 * @return true si l'etat est arrive et complet
 	 */
 	public Boolean getIsEtatArriveComplet() {
 		if (etat.getClass().equals(EtatArriveComplet.class)) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return true si l'etat est arrive et complet
 	 */
 	public Boolean getIsEtatConfirme() {
 		if (etat.getClass().equals(EtatConfirme.class)) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return true si l'etat est arrive et complet
 	 */
 	public Boolean getIsEtatDesiste() {
 		if (etat.getClass().equals(EtatDesiste.class)) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return true si : 
 	 * - cas primo entrant et Code CGE servant de temoin AD pour le Web primo entrant non null
 	 * - cas reinscription et Code CGE servant de temoin AD pour le Web reinscription non null
 	 */
 	public Boolean getHasIAForVoeu() {
 		String codeEtu = indVoeu.getIndividu().getCodeEtu();
 		if ((!StringUtils.hasText(codeEtu) && StringUtils.hasText(vrsEtape.getCodCgeMinpVet()))
 				|| (StringUtils.hasText(codeEtu) && StringUtils.hasText(vrsEtape.getCodCgeMinVet()))) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return true si le candidat a un numÃ©ro INE
 	 */
 	public Boolean getHasNNE() {
 		return StringUtils.hasText(indVoeu.getIndividu().getCodeNNE());
 	}
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 
 
 	/**
 	 * @return the indVoeu
 	 */
 	public IndVoeu getIndVoeu() {
 		return indVoeu;
 	}
 
 	/**
 	 * @param indVoeu the indVoeu to set
 	 */
 	public void setIndVoeu(final IndVoeu indVoeu) {
 		this.indVoeu = indVoeu;
 	}
 
 	/**
 	 * @return the vrsEtape
 	 */
 	public VersionEtapeDTO getVrsEtape() {
 		return vrsEtape;
 	}
 
 	/**
 	 * @param vrsEtape the vrsEtape to set
 	 */
 	public void setVrsEtape(final VersionEtapeDTO vrsEtape) {
 		this.vrsEtape = vrsEtape;
 	}
 
 	/**
 	 * @return the avis
 	 */
 	public List<Avis> getAvisAsList() {
		if (this.indVoeu.getAvis() != null && !this.indVoeu.getAvis().isEmpty()) {
 			this.avisAsList = new ArrayList<Avis>(indVoeu.getAvis());
 		}
 		return avisAsList;
 	}
 
 	/**
 	 * @return the etat
 	 */
 	public EtatVoeu getEtat() {
 		return etat;
 	}
 
 	/**
 	 * @param etat the etat to set
 	 */
 	public void setEtat(final EtatVoeu etat) {
 		this.etat = etat;
 	}
 
 
 	/**
 	 * @return the calIsOpen
 	 */
 	public Boolean getCalIsOpen() {
 		return calIsOpen;
 	}
 
 
 	/**
 	 * @param calIsOpen the calIsOpen to set
 	 */
 	public void setCalIsOpen(final Boolean calIsOpen) {
 		this.calIsOpen = calIsOpen;
 	}
 
 
 
 	/**
 	 * @return the typeTraitement
 	 */
 	public TypeTraitement getTypeTraitement() {
 		return typeTraitement;
 	}
 
 
 	/**
 	 * @param typeTraitement the typeTraitement to set
 	 */
 	public void setTypeTraitement(final TypeTraitement typeTraitement) {
 		this.typeTraitement = typeTraitement;
 	}
 
 	/**
 	 * @return the newAvis
 	 */
 	public Avis getNewAvis() {
 		return newAvis;
 	}
 
 	/**
 	 * @param newAvis the newAvis to set
 	 */
 	public void setNewAvis(final Avis newAvis) {
 		this.newAvis = newAvis;
 	}
 
 	/**
 	 * @return the avisEnService
 	 */
 	public Avis getAvisEnService() {
 		return avisEnService;
 	}
 
 	/**
 	 * @param avisEnService the avisEnService to set
 	 */
 	public void setAvisEnService(final Avis avisEnService) {
 		this.avisEnService = avisEnService;
 	}
 
 	/**
 	 * @return the isUsingLC
 	 */
 	public Boolean getIsUsingLC() {
 		return isUsingLC;
 	}
 
 	/**
 	 * @param isUsingLC the isUsingLC to set
 	 */
 	public void setIsUsingLC(final Boolean isUsingLC) {
 		this.isUsingLC = isUsingLC;
 	}
 
 	/**
 	 * @return the isUsingDEF
 	 */
 	public Boolean getIsUsingDEF() {
 		return isUsingDEF;
 	}
 
 	/**
 	 * @param isUsingDEF the isUsingDEF to set
 	 */
 	public void setIsUsingDEF(final Boolean isUsingDEF) {
 		this.isUsingDEF = isUsingDEF;
 	}
 
 	/**
 	 * @return the stateConf
 	 */
 	public String getStateConf() {
 		return stateConf;
 	}
 
 	/**
 	 * @param stateConf the stateConf to set
 	 */
 	public void setStateConf(final String stateConf) {
 		this.stateConf = stateConf;
 	}
 
 	/**
 	 * @return the disableConfirm
 	 */
 	public Boolean getDisableConfirm() {
 		return disableConfirm;
 	}
 
 	/**
 	 * @param disableConfirm the disableConfirm to set
 	 */
 	public void setDisableConfirm(final Boolean disableConfirm) {
 		this.disableConfirm = disableConfirm;
 	}
 
 	/**
 	 * @return the individuDate
 	 */
 	public IndividuDate getIndividuDate() {
 		return individuDate;
 	}
 
 	/**
 	 * @param individuDate the individuDate to set
 	 */
 	public void setIndividuDate(final IndividuDate individuDate) {
 		this.individuDate = individuDate;
 	}
 	
 	/**
 	 * @return calendrier de rendez-vous
 	 */
 	public CalendarRDV getCalendrierRdv() {
 		return calendrierRdv;
 	}
 	
 	/**
 	 * @param calendrierRdv
 	 */
 	public void setCalendrierRdv(final CalendarRDV calendrierRdv) {
 		this.calendrierRdv = calendrierRdv;
 	}
 }
 
