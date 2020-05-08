 /**
  * ESUP-Portail Blank Application - Copyright (c) 2006 ESUP-Portail consortium
  * http://sourcesup.cru.fr/projects/esup-opiR1
  */
 package org.esupportail.opi.web.controllers;
 
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.esupportail.commons.annotations.cache.RequestCache;
 import org.esupportail.commons.exceptions.ConfigException;
 import org.esupportail.commons.exceptions.UserNotFoundException;
 import org.esupportail.commons.services.exceptionHandling.ExceptionUtils;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.commons.utils.BeanUtils;
 import org.esupportail.commons.utils.ContextUtils;
 import org.esupportail.commons.utils.strings.StringUtils;
 import org.esupportail.commons.web.controllers.ExceptionController;
 import org.esupportail.commons.web.controllers.Resettable;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.User;
 import org.esupportail.opi.services.authentification.Authenticator;
 import org.esupportail.opi.web.beans.parameters.FormationInitiale;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.IndividuPojo;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 
 
 /**
  * A bean to memorize the context of the application.
  */
 public class SessionController extends AbstractDomainAwareBean {
 
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = -5936434246704000653L;
 
 	/**
	 * The name of the parameter that gives the logout URL service.
	 */
	private static final String LOGOUT_URL_PARAM_SERVICE = "edu.yale.its.tp.cas.client.logoutUrl.service";

	/**
 	 * The name of the request attribute that holds the current individu.
 	 */
 	private static final String CURRENT_INDIVIDU_ATTRIBUTE = SessionController.class.getName() + ".currentIndividu";
 
 	/**
 	 * A logger.
 	 */
 	private final Logger log = new LoggerImpl(getClass());	
 
 	/**
 	 * The CAS logout URL.
 	 */
 	private String casLogoutUrl;
 
 	/**
 	 * a true si c'est un gestionnaire.
 	 * Default value false.
 	 */
 	private Boolean isManager;
 
 	/**
 	 * a true si c'est un gestionnaire qui peut modifier 
 	 * les donnees de l'etudiant traite.
 	 * Default value false.
 	 */
 	private Boolean canUpdateStudent;
 	
 	/**
 	 * a true si un le regime d'inscription peut être modifié 
 	 * Default value false.
 	 */
 	private Boolean canUpdateRI;
 
 	/**
 	 * The student code.
 	 */
 	private String codEtu;
 
 	/**
 	 * At true if call in ENT.
 	 * Default value : false.
 	 */
 	private Boolean isInEnt;
 	
 	/**
 	 * The regime of inscription.
 	 */
 	private RegimeInscription regimeInsUser;
 	
 	/**
 	 * The exception controller (called when logging in/out).
 	 */
 	private ExceptionController exceptionController;
 
 	/**
 	 * The authentication service.
 	 */
 	private Authenticator authenticator;
 
 	/**
 	 * 
 	 */
 	private boolean allViewPJ;
 	
 
 	/**
 	 * Constructor.
 	 */
 	public SessionController() {
 		super();
 	}
 
 	/** 
 	 * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
 	 */
 	@Override
 	public void reset() {
 		canUpdateStudent = false;
 		canUpdateRI = getParameterService().isCampagnesFIAndFCEnServ();
 		isManager = false;
 		codEtu = null;
 		isInEnt = false;
 		regimeInsUser = getRegimeIns().get(FormationInitiale.CODE);
 	}
 
 
 
 	/**
 	 * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#afterPropertiesSetInternal()
 	 */
 	@Override
 	public void afterPropertiesSetInternal() {
 		Assert.notNull(this.exceptionController, "property exceptionController of class " 
 				+ this.getClass().getName() + " can not be null");
 		Assert.notNull(this.authenticator, "property authenticator of class " 
 				+ this.getClass().getName() + " can not be null");
 
 	}
 
 	/**
 	 * @return the current user, or null if guest.
 	 */
 	@Override
 	public User getCurrentUser() {
 		return authenticator.getUser();
 	}
 	
 	
 	/**
 	 * Manager connect in the student space.
 	 * @return Gestionnaire
 	 */
 	public Gestionnaire getManager() {
 		return authenticator.getManager();
 	}
 
 	/**
 	 * In ENT defined who is connect to redirect to good welcome page. 
 	 * @return String
 	 */
 	@RequestCache
 	public String getWhoIsConnectInPortlet() {
 		
 		User u = null;
 		String whoIsConnect = null;
 //		String currentUserId = authenticationService.getCurrentUserId();
 //		if (currentUserId == null) {
 //			return null;
 //		}
 		
 		//en portlet le currentUser peut etre un individu.
 		u = getCurrentUser();
 		if (u != null) {
 			if (u instanceof Gestionnaire) {
 				whoIsConnect = "manager";
 			} else if (u instanceof Individu) {
 //				Individu i = (Individu) u;
 //				numDossier = i.getNumDossierOpi();
 //				dateNaissance = i.getDateNaissance();
 				whoIsConnect = "individu.exist";
 			}
 		} else {
 			if (authenticator.getAuthId() != null) {
 				codEtu = Utilitaires.getCheckCodEtu(authenticator.getAuthId(),
 						getDomainService().getCodStudentRegex(),
 						getDomainService().getCodStudentPattern());
 			}
 			//c'est un nouveau etudiant donc redirection vers search etu et affichage du son code etu.
 			whoIsConnect = "individu.not_exist";
 		}
 		if (log.isDebugEnabled()) {
 			log.debug("leaving getWhoIsConnectInPortlet return = " + whoIsConnect);
 		}
 		return whoIsConnect;
 	}
 
 
 
 	/**
 	 * @return the current individu.
 	 */
 	@Override
 	public IndividuPojo getCurrentInd() {
 		if (ContextUtils.getRequestAttribute(CURRENT_INDIVIDU_ATTRIBUTE) == null) {	
 			Individu individu = null;
 			User u = getCurrentUser();
 			if (u != null && u instanceof Individu) {
 				//individu = getDomainService().getIndividu(numDossier, dateNaissance);
 				individu = (Individu) u;
 			}
 
 
 			IndividuPojo indPojo = null;
 			if (individu != null) {
				int codeRI = Utilitaires.getCodeRIIndividu(individu,
						getDomainService());
				RegimeInscription regime = getRegimeIns().get(codeRI);
 				//Test l etat de l'individu
 				individu = 
 					getDomainService().updateStateIndividu(
 							individu, authenticator.getManager()); 
 				//TODO : does it work without the following ?
 				//regime.getControlField());
 				indPojo = new IndividuPojo(
 						individu, getDomainApoService(),
 						getI18nService(), getParameterService(), 
 						getRegimeIns().get(Utilitaires.getCodeRIIndividu(individu,
 								getDomainService())), getParameterService().getTypeTraitements(),
 								getParameterService().getCalendarRdv(), null);
 			}
 			if (indPojo != null) { 
 				// put boolean for the management and rights of update
 				indPojo.setIsManager(isManager);
 				indPojo.setIsUpdaterOfThisStudent(canUpdateStudent);
 			}
 			resetSessionLocale();
 			ContextUtils.setRequestAttribute(CURRENT_INDIVIDU_ATTRIBUTE, indPojo);
 		}
 		return (IndividuPojo) ContextUtils.getRequestAttribute(CURRENT_INDIVIDU_ATTRIBUTE);
 	}
 
 	
 	
 	/**
 	 * Initialize the current Individu.
 	 * @param numeroDossier
 	 * @param dateDeNaissance
 	 * @param isManager 
 	 * @param canUpdateStudent 
 	 */
 	public void initCurrentInd(final String numeroDossier, final Date dateDeNaissance,
 			final Boolean isManager, final Boolean canUpdateStudent) {
 		User user = getCurrentUser();
 		if (user != null && user instanceof Gestionnaire) {
 			authenticator.storeManager(
 				(Gestionnaire) user, numeroDossier, dateDeNaissance);
 		} else {
 			authenticator.storeManager(null, numeroDossier, dateDeNaissance);
 		}
 		this.isManager = isManager;
 		this.canUpdateStudent = canUpdateStudent;
 		getCurrentInd();
 	}
 
 
 
 
 	/**
 	 * True is in servlet mode.
 	 * @return boolean
 	 */
 	public boolean getIsServlet() {
 		if (isInEnt) {
 			return false;
 		}
 		return ContextUtils.isServlet();
 	}
 
 	/**
 	 * @return a debug String.
 	 */
 	public String getDebug() {
 		return toString();
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return getClass().getName() + "#" + hashCode();
 	}
 
 
 
 	/**
 	 * JSF callback.
 	 * @return a String.
 	 * @throws IOException 
 	 */
 	public String logoutGest() throws IOException {
 		if (ContextUtils.isPortlet()) {
 			throw new UnsupportedOperationException("logoutGest() should not be called in portlet mode.");
 		}
 		FacesContext facesContext = FacesContext.getCurrentInstance();
 		ExternalContext externalContext = facesContext.getExternalContext();
 		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
 		String returnUrl = request.getRequestURL().toString().replaceFirst("/stylesheets/[^/]*$", "");
 		String forwardUrl;
 		Assert.hasText(
 				casLogoutUrl, 
 				"property casLogoutUrl of class " + getClass().getName() + " is null");
 		forwardUrl = String.format(casLogoutUrl, StringUtils.utf8UrlEncode(returnUrl));
 		// note: the session beans will be kept even when invalidating 
 		// the session so they have to be reset (by the exception controller).
 		// We invalidate the session however for the other attributes.
 		request.getSession().invalidate();
 		request.getSession(true);
 		// calling this method will reset all the beans of the application
 		exceptionController.restart();
 		externalContext.redirect(forwardUrl);
 		facesContext.responseComplete();
 		return null;
 	}
 
 	/**
 	 * JSF callback.
 	 * Disconnect an individu.
 	 * @return a String.
 	 */
 	public String logoutInd() {
 		FacesContext facesContext = FacesContext.getCurrentInstance();
 		ExternalContext externalContext = facesContext.getExternalContext();
 		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
 		// We invalidate the session however for the other attributes.
 		request.getSession().invalidate();
 		request.getSession(true);
 		// calling this method will reset all the beans of the application
 		exceptionController.restart();
 		facesContext.responseComplete();
 
 		return NavigationRulesConst.APPLI_RESTART;
 	}
 	/**
 	 * Call Back to WelcomeManager when a manager is consulting an individu.
 	 * @return a String
 	 */
 	public String goBackManager() {
 		// Reinitialise  null pour getCurrentID()
 		Boolean isEnt = isInEnt;
 		reset();
 		authenticator.storeManager(null, null, null);
 		isInEnt = isEnt;
 		return NavigationRulesConst.WELCOME_MANAGER;
 	}
 
 	
 	
 	
 	
 	
 	/**
 	 * JSF callback.
 	 * @return a String.
 	 */
 	public String restart() {
 		Map<String, Resettable> resettables = BeanUtils.getBeansOfClass(Resettable.class);
 		Boolean isManagerConnect = true;
 		Boolean isEnt = isInEnt;
 		if (getCurrentUser() == null && getCurrentInd() != null) {
 			isManagerConnect = false;
 		}
 		for (Entry<String, Resettable> nameEntry : resettables.entrySet()) {
 			String name = nameEntry.getKey();
 			if (log.isDebugEnabled()) {
 				log.debug("trying to reset bean [" + name + "]...");
 			}
 			Object bean = nameEntry.getValue();
 			if (bean == null) {
 				throw new ConfigException("bean [" + name 
 						+ "] is null, " 
 						+ "application can not be restarted.");
 			}
 			if (!(bean instanceof Resettable)) {
 				throw new ConfigException("bean [" + name 
 						+ "] does not implement Resettable, " 
 						+ "application can not be restarted.");
 			}
 			((Resettable) bean).reset();
 			if (log.isDebugEnabled()) {
 				log.debug("bean [" + name + "] was reset.");
 			}
 		}
 		ExceptionUtils.unmarkExceptionCaught();
 		isInEnt = isEnt;
 		if (isInEnt && isManagerConnect) {
 			return NavigationRulesConst.WELCOME_MANAGER;
 		} else if (isInEnt && !isManagerConnect) {
 			return NavigationRulesConst.ACCUEIL_CANDIDAT;
 		}
 		return "applicationRestarted";
 	}
 	
 	
 	
 	
 
 
 	/**
 	 * Return the diplayName of current user.
 	 * @return String
 	 */
 	public String getCurrentDisplayName() {
 		String disp = "";
 		try {
 		if (getCurrentInd() != null) {
 			Individu i = getCurrentInd().getIndividu();
 			disp = Utilitaires.upperCaseFirstChar(i.getPrenom(), true) + " "
 			+ Utilitaires.upperCaseFirstChar(i.getNomPatronymique(), true);
 		} else if (getCurrentUser() != null) {
 			Gestionnaire g = (Gestionnaire) getCurrentUser();
 			disp = g.getDisplayName();
 		}
 		} catch (UserNotFoundException e) {
 			log.warn("don't display name because UserNotFoundException");
 		}
 		return disp;
 	}
 
 
 	/** 
 	 * @see org.esupportail.commons.beans.AbstractI18nAwareBean#getTimezone()
 	 */
 	@Override
 	public String getTimezone() {
 		return super.getTimezone();
 	}
 
 	/**
 	 * @param exceptionController the exceptionController to set
 	 */
 	public void setExceptionController(final ExceptionController exceptionController) {
 		this.exceptionController = exceptionController;
 	}
 
 
 	/**
 	 * @return the authenticator
 	 */
 	public Authenticator getAuthenticator() {
 		return authenticator;
 	}
 
 	/**
 	 * @param authenticator the authenticator to set
 	 */
 	public void setAuthenticator(final Authenticator authenticator) {
 		this.authenticator = authenticator;
 	}
 
 	/**
 	 * @return the codEtu
 	 */
 	public String getCodEtu() {
 		return codEtu;
 	}
 
 	/**
 	 * @param codEtu the codEtu to set
 	 */
 	public void setCodEtu(final String codEtu) {
 		this.codEtu = codEtu;
 	}
 
 	/**
 	 * @return the isInEnt
 	 */
 	public Boolean getIsInEnt() {
 		return isInEnt;
 	}
 
 	/**
 	 * @param isInEnt the isInEnt to set
 	 */
 	public void setIsInEnt(final Boolean isInEnt) {
 		this.isInEnt = isInEnt;
 	}
 
 	/**
 	 * @param casLogoutUrl the casLogoutUrl to set
 	 */
 	public void setCasLogoutUrl(final String casLogoutUrl) {
 		this.casLogoutUrl = casLogoutUrl;
 	}
 
 	/**
 	 * @return the regimeInsUser
 	 */
 	public RegimeInscription getRegimeInsUser() {
 		return regimeInsUser;
 	}
 
 	/**
 	 * @param regimeInsUser the regimeInsUser to set
 	 */
 	public void setRegimeInsUser(final RegimeInscription regimeInsUser) {
 		this.regimeInsUser = regimeInsUser;
 	}
 	
 	/**
 	 * @return allViewPJ
 	 */
 	public boolean isAllViewPJ() {
 		return allViewPJ;
 	}
 	/**
 	 * @param allViewPJ
 	 */
 	public void setAllViewPJ(final boolean allViewPJ) {
 		this.allViewPJ = allViewPJ;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Boolean getCanUpdateRI() {
 		return canUpdateRI;
 	}
 
 	/**
 	 * 
 	 * @param canUpdateRI
 	 */
 	public void setCanUpdateRI(Boolean canUpdateRI) {
 		this.canUpdateRI = canUpdateRI;
 	}
 	
 	
 }
