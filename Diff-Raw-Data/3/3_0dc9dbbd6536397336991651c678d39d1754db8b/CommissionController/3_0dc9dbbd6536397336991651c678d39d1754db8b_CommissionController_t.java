 /**
  *
  */
 package org.esupportail.opi.web.controllers.references;
 
 import fj.F;
 import fj.F2;
 import fj.F3;
 import fj.Unit;
 import org.esupportail.commons.exceptions.ConfigException;
 import org.esupportail.commons.exceptions.UserNotFoundException;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.beans.parameters.Transfert;
 import org.esupportail.opi.domain.beans.parameters.accessRight.Profile;
 import org.esupportail.opi.domain.beans.references.calendar.CalendarCmi;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.ContactCommission;
 import org.esupportail.opi.domain.beans.references.commission.Member;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.user.Adresse;
 import org.esupportail.opi.domain.beans.user.AdresseCommission;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.indcursus.IndBac;
 import org.esupportail.opi.domain.beans.user.indcursus.IndCursusScol;
 import org.esupportail.opi.services.export.CastorService;
 import org.esupportail.opi.services.export.ISerializationService;
 import org.esupportail.opi.services.mails.MailContentService;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.utils.ldap.LdapAttributes;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.beanEnum.WayfEnum;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.*;
 import org.esupportail.opi.web.beans.utils.ExportUtils;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.PDFUtils;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractContextAwareController;
 import org.esupportail.opi.web.controllers.user.AdressController;
 import org.esupportail.wssi.services.remote.BacOuxEqu;
 import org.esupportail.wssi.services.remote.SignataireDTO;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.springframework.util.StringUtils;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import static fj.Function.curry;
 import static fj.Semigroup.stringSemigroup;
 import static fj.Unit.unit;
 import static fj.data.IterableW.wrap;
 import static fj.data.Option.fromNull;
 import static fj.data.Stream.iterableStream;
 import static fj.data.Validation.validation;
 
 
 /**
  * @author cleprous
  *
  */
 @SuppressWarnings("serial")
 public class CommissionController
 		extends AbstractContextAwareController {
 
 
 	/**
 	 * The serialization id.
 	 */
 	private static final long serialVersionUID = -5769900637456057431L;
 
 	/**
 	 * Value for member is already in gestionnaire table.
 	 */
 	private static final String IS_GESTIONNAIRE = "IS_GEST";
 
 	/**
 	 * Value for member must be add to gestionnaire table.
 	 */
 	private static final String MUST_BE_ADD_GEST = "MUST_BE_ADD_GEST";
 
 	/**
 	 * Value for member is been enter and must not be add to gestionnaire table.
 	 */
 	private static final String ENTER_MBR = "ENTER_MBR";
 
 
 
 	/*
 	 ******************* PROPERTIES ******************* */
 
 	/**
 	 * The commission.
 	 */
 	private Commission commission;
 
 	/**
 	 * The contactCommission.
 	 */
 	private ContactCommission contactCommission;
 
 	/**
 	 * The actionEnum.
 	 */
 	private ActionEnum actionEnum;
 
 	/**
 	 * The WayfEnum.
 	 */
 	private WayfEnum wayfEnum;
 
 	/**
 	 * The manager of the versionEtape to add to the cmi.
 	 */
 	private List<Object> objectToAdd;
 
 	/**
 	 * key : he member to the cmi.
 	 * value : IS_GEST or IS_IN_SI or ENTER_MBR
 	 */
 	private Map<Member, String> membersToDisplay;
 
 	/**
 	 * The members to remove to list membersToDisplay.
 	 */
 	private Member memberToDelete;
 
 
 	/**
 	 * Liste des régimes d'inscription pour la recherche.
 	 */
 	private List<RegimeInscription> listeRI;
 
 	/**
 	 * true si le gestionnaire peut modifier.
 	 * le filtre sur le régime d'inscription, false sinon
 	 */
 	private Boolean canModifyRISearch;
 
 	/**
 	 * This list is used to the selection in the manager user and calendarIns.
 	 */
 	private List<Commission> selectedCommissions;
 
 	/**
 	 * The id of the selected commission when no address.
 	 */
 	private Integer idCmiForAdress;
 
 	/**
 	 * The list of members selected for the mail send.
 	 */
 	private Object[] membersSelected;
 
 	/**
 	 * see {@link Transfert}.
 	 */
 	private Transfert transfert;
 
 	/**
 	 * see {@link AdressController}.
 	 */
 	private AdressController adressController;
 
 	/**
 	 * Service to generate Xml.
 	 */
 	private ISerializationService castorService;
 
 	/**
 	 * mail send to member convocation.
 	 */
 	private MailContentService convocMember;
 
 	/**
 	 * The list of commitees.
 	 */
 	private List<CommissionPojo> listCmiPojo;
 
 	/**
 	 * The list of commitees.
 	 */
 	private List<CommissionPojo> filteredListCmiPojo;
 
 	/**
 	 * see {@link TrtCmiController}.
 	 */
 	private TrtCmiController trtCmiController;
 
 	/**
 	 * Used to know if the cmi's manager is used.
 	 */
 	private boolean managerUsed;
 
 	/**
 	 * Used to know if the list cmi has to be filtered by right
 	 */
 	private boolean listCmiByRight;
 
     private LdapAttributes ldapAttrs;
 
 	private Set<Commission> commissions;
 	private Set<Commission> comsInUse;
 	private List<Commission> comsInUseByRight;
 	private List<Commission> comsWithForms;
 	private Set<Commission> comsNoTrt;
 
 
 	private final Comparator<Commission> comparatorCmi = new Comparator<Commission>() {
 		@Override
 		public int compare(final Commission c1, final Commission c2) {
 			return c1.getLibelle().compareToIgnoreCase(c2.getLibelle());
 		}
 	};
 
 	/**
 	 * A logger.
 	 */
 	private final Logger log = new LoggerImpl(getClass());
 
 
 	/*
 	 ******************* INIT ************************* */
 
 	/**
 	 * Constructors.
 	 */
 	public CommissionController() {
 		super();
 	}
 
 	public void initCommissions() {
 		commissions = new TreeSet<Commission>(comparatorCmi);
 		commissions.addAll(getParameterService().getCommissions(null));
 
 		comsInUse = new TreeSet<Commission>(comparatorCmi);
 		comsInUse.addAll(getParameterService().getCommissions(true));
         getDomainApoService().emptyCommissionCache(getCurrentGest(), true);
         Set<Commission> cmi =
                 getDomainApoService().getListCommissionsByRight(getCurrentGest(), true);
 		comsInUseByRight = new ArrayList<Commission>();
         if (cmi != null) {
             comsInUseByRight.addAll(cmi);
         }
         Collections.sort(comsInUseByRight, comparatorCmi);
 		comsWithForms = Utilitaires.getListCommissionExitForm(
 				comsInUseByRight, listeRI, getParameterService());
 		comsNoTrt = new TreeSet<Commission>(comparatorCmi);
 		comsNoTrt.addAll(Utilitaires.getListCommissionsWithoutTrt(getParameterService()));
 	}
 
 	/**
 	 * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
 	 */
 	@Override
 	public void reset() {
 		super.reset();
 		commission = new Commission();
 		contactCommission = new ContactCommission();
 		actionEnum = new ActionEnum();
 		objectToAdd = new ArrayList<Object>();
 		membersToDisplay = new HashMap<Member, String>();
 		selectedCommissions = new ArrayList<Commission>();
 		idCmiForAdress = 0;
 		membersSelected = new Object[0];
 		wayfEnum = new WayfEnum();
 		adressController.reset();
 		trtCmiController.reset();
 		this.listeRI = new ArrayList<RegimeInscription>();
 		this.canModifyRISearch = false;
 		// on initialise indRechPojo selon le contexte du Gestionnaire connecté
 		if (getSessionController().getCurrentUser() != null
 				&& getSessionController().getCurrentUser() instanceof Gestionnaire) {
 			Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
 			int codeRI = gest.getProfile().getCodeRI();
 			RegimeInscription regimeIns = getSessionController().getRegimeIns().get(codeRI);
 			this.listeRI.add(regimeIns);
 			this.canModifyRISearch = regimeIns.canModifyRISearch();
 		}
 		filteredListCmiPojo = null;
 	}
 
 
 	/**
 	 * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#afterPropertiesSetInternal()
 	 */
 	@Override
 	public void afterPropertiesSetInternal() {
 		Assert.notNull(this.trtCmiController,
 				"property trtCmiController of class " + this.getClass().getName()
 				+ " can not be null");
 		Assert.notNull(this.adressController,
 				"property adressController of class " + this.getClass().getName()
 				+ " can not be null");
 		Assert.notNull(this.castorService,
 				"property castorService of class " + this.getClass().getName()
 				+ " can not be null");
 		Assert.notNull(this.transfert,
 				"property transfert of class " + this.getClass().getName()
 				+ " can not be null");
 		reset();
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "CommissionController#" + hashCode(); //+ "[commission.code =" + commission.getCode() + "]";
 	}
 	/*
 	 ******************* CALLBACK ********************** */
 
 	/**
 	 * Callback to Commission list.
 	 * @return String
 	 */
 	public String goSeeAllCmi() {
 		reset();
         initCommissions();
 		listCmiPojo = getData();
 		return NavigationRulesConst.MANAGED_CMI;
 	}
 
 
 	/**
 	 * Callback to Commission add.
 	 * @return String
 	 */
 	public String goAddCmi() {
 		reset();
         // TODO: get rid of that
 		//commission = getParameterService().getCommission(commission.getId(), null);
 		adressController.init(new AdresseCommission(), false);
 		Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
 		int codeRI = gest.getProfile().getCodeRI();
 		contactCommission = new ContactCommission(codeRI);
 		actionEnum.setWhatAction(ActionEnum.ADD_ACTION);
 		return NavigationRulesConst.ENTER_CMI;
 	}
 
 	/**
 	 * Callback to Commission update.
 	 * @return String
 	 */
 	public String goUpdateCmi() {
 		trtCmiController.reset();
 		commission = getParameterService().getCommission(commission.getId(), null);
 		if (membersToDisplay.isEmpty()) {
 			membersToDisplay = new HashMap<Member, String>();
 			for (Member m : commission.getMembers()) {
 				if (m.getGestionnaire() == null) {
 					membersToDisplay.put(m, ENTER_MBR);
 				} else {
 					membersToDisplay.put(m, IS_GESTIONNAIRE);
 				}
 			}
 		}
 		Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
 		Integer codeRI = gest.getProfile().getCodeRI();
 
 		AdresseCommission adresseComm = new AdresseCommission();
 		ContactCommission contactRI = commission.getContactsCommission().get(codeRI);
 		if (contactRI != null) {
 			// peut être null si autre régime d'inscription
 			adresseComm = contactRI.getAdresse();
 		}
 		adressController.init(adresseComm, true);
 		contactCommission = commission.getContactsCommission().get(codeRI);
 		if (contactCommission == null) {
 			contactCommission = new ContactCommission(codeRI);
 		}
 		return NavigationRulesConst.ENTER_CMI;
 	}
 
 	/**
 	 * Callback to read Commission.
 	 * @return String
 	 */
 	public String goSeeOneCmi() {
 		commission = getParameterService().getCommission(commission.getId(), null);
 		membersToDisplay = new HashMap<Member, String>();
 		for (Member m : commission.getMembers()) {
 			if (m.getGestionnaire() == null) {
 				membersToDisplay.put(m, ENTER_MBR);
 			} else {
 				membersToDisplay.put(m, IS_GESTIONNAIRE);
 			}
 		}
 
 		membersSelected = getKeySetMbrToDisplay().toArray();
 
 		initAllTraitementCmi(commission);
 
 		Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
 		Integer codeRI = gest.getProfile().getCodeRI();
 
 		AdresseCommission adresseComm = new AdresseCommission();
 		ContactCommission contactRI = commission.getContactsCommission().get(codeRI);
 		if (contactRI != null) {
 			adresseComm = contactRI.getAdresse();
 		} else {
 			addInfoMessage(null, "COMMISSION.CONTACT.EMPTY");
 		}
 		adressController.init(adresseComm, false);
 
 		contactCommission = commission.getContactsCommission().get(codeRI);
 		if (contactCommission == null) {
 			contactCommission = new ContactCommission(codeRI);
 		}
 
 		return NavigationRulesConst.SEE_CMI;
 	}
 
 	/**
 	 * Callback to look for member.
 	 * @return String
 	 */
 	public String goSearchMembers() {
 		objectToAdd = new ArrayList<Object>();
 		trtCmiController.reset();
 		return NavigationRulesConst.SEARCH_MEMBER;
 	}
 
 	/**
 	 * Callback to print the listes prepa.
 	 */
 	public String goPrintListsPrepa() {
 		reset();
 		return NavigationRulesConst.DISPLAY_PRINT_LISTS_PREPA;
 	}
 
 	/*
 	 ******************* METHODS ********************** */
 
 	/**
 	 * Add the members in objetToAdd in membersToDisplay.
 	 * @return String
 	 */
 	public String addMembers() {
         F3<String, String, String, F2<Gestionnaire, String, Unit>> buildAndPutMember =
                 new F3<String, String, String, F2<Gestionnaire, String, Unit>>() {
                     public F2<Gestionnaire, String, Unit> f(final String mail, final String prenom, final String nom) {
                         return new F2<Gestionnaire, String, Unit>() {
                             public Unit f(Gestionnaire gest, String message) {
                                 membersToDisplay.put(
                                         new Member(nom.toUpperCase(), prenom, mail, "", gest),
                                         message);
                                 return unit();
                             }
                         };
                     }
                 };
 
         String where = "";
 		for (Object o : objectToAdd) {
 			final Gestionnaire g = (Gestionnaire) o;
 			Gestionnaire g1 = null;
 			try {
 				g1 = getDomainService().getManager(g.getLogin());
 			} catch (UserNotFoundException e) {
 				log.info("Le gestionnaire " + g.getLogin() + " n'existe pas");
 			}
 
             final Gestionnaire gg1 = g1;
             where = validation(fromNull(g.getNomUsuel()).toEither(ldapAttrs.nomUsuelAttribute + ","))
                     .accumapply(stringSemigroup,
                             validation(fromNull(g.getPrenom()).toEither(ldapAttrs.prenomAttribute + ","))
                                     .accumapply(stringSemigroup,
                                             validation(fromNull(g.getAdressMail()).toEither(ldapAttrs.emailAttribute + ","))
                                                     .map(curry(buildAndPutMember))))
                     .validation(
                             new F<String, String>() {
                                 public String f(String missingVals) {
                                     addWarnMessage(null, "GESTIONNAIRE.WARN.LDAP", missingVals);
                                     return NavigationRulesConst.SEARCH_MEMBER;
                                 }
                             },
                             new F<F2<Gestionnaire, String, Unit>, String>() {
                                 public String f(F2<Gestionnaire, String, Unit> ff) {
                                     if (gg1 == null) ff.f(g, MUST_BE_ADD_GEST);
                                     else ff.f(gg1, IS_GESTIONNAIRE);
                                     return NavigationRulesConst.ENTER_CMI;
                                 }
                             }
                     );
         }
 		objectToAdd = new ArrayList<Object>();
 		return where;
 	}
 
 
 
 
 	/**
 	 * Add one member in membersToDisplay.
 	 */
 	public void addOneMember() {
 		//si un member n'est pas bien remplie on ajoute pas
 		if (ctrlAllMbrInMbrToDisplay()) {
 			membersToDisplay.put(new Member(), ENTER_MBR);
 		}
 	}
 
 
 
 	/**
 	 * Add a Commission to the dataBase.
 	 * @return String
 	 */
 	public String add() {
 		if (log.isDebugEnabled()) {
 			log.debug("enterind add with Commission = " + commission);
 		}
 		Adresse adresse = adressController.getFixAdrPojo().getAdresse();
 		if (ctrlEnter(commission, adresse, true)
 				&& adressController.ctrlEnter(adresse, true)
 				&& ctrlAllMbrInMbrToDisplay()) {
 			if (adresse.getId() == 0) {
 				adressController.addAdrComm();
 			} else {
 				adressController.update(adressController.getFixAdrPojo());
 			}
             commission.setTraitementCmi(new HashSet<TraitementCmi>());
 			commission = getDomainService().add(commission, getCurrentGest().getLogin());
 			getParameterService().addCommission(commission);
 
 			addOrUpdateMember();
 
 			// ajout du contactCommission
 			contactCommission.setAdresse((AdresseCommission) adresse);
 			contactCommission.setCommission(commission);
 			getParameterService().addContactCommission(contactCommission);
 
             // need that to see the just added Commission in the dataTables
             initCommissions();
 
             addInfoMessage(null, "INFO.ENTER.SUCCESS");
 			addInfoMessage(null, "COMMISSION.INFO.ADD.CAL_CMI", getParameterService().getDateBackDefault());
 			return NavigationRulesConst.SEE_CMI;
 		}
 		if (log.isDebugEnabled()) {
 			log.debug("leaving add");
 		}
 		return null;
 	}
 
 	/**
 	 * Update a Commission to the dataBase.
 	 * @return String
 	 */
 	public String update() {
 		if (log.isDebugEnabled()) {
 			log.debug("enterind update with Commission = " + commission);
 		}
 		Adresse adresse = adressController.getFixAdrPojo().getAdresse();
         commission = getParameterService().getCommission(commission.getId(), null);
         if (ctrlEnter(commission, adresse, true)
 				&& adressController.ctrlEnter(adresse, true)
 				&& ctrlAllMbrInMbrToDisplay()) {
 			List<Member> mToDelete = new ArrayList<Member>();
 			for (Member m : commission.getMembers()) {
 				if (!membersToDisplay.containsKey(m)) {
 					mToDelete.add(m);
 				}
 			}
 
 			//update adresse
 			if (adresse.getId() == 0) {
                 adressController.addAdrComm();
             } else {
                 adressController.update(adressController.getFixAdrPojo());
             }
 
 			//update cmi
 			commission = getDomainService().update(commission, getCurrentGest().getLogin());
 			getParameterService().updateCommission(commission);
 
 			//update Member
 			addOrUpdateMember();
 
 			//delete Member and trtCmi
 			getParameterService().deleteMember(mToDelete);
 
 			// ajout ou modification du contactCommission
             if (contactCommission.getId() == 0) {
                 contactCommission.setAdresse((AdresseCommission) adresse);
                 contactCommission.setCommission(commission);
                 getParameterService().addContactCommission(contactCommission);
             } else {
                 getParameterService().updateContactCommission(contactCommission);
             }
 
 			//reset();
 			addInfoMessage(null, "INFO.ENTER.SUCCESS");
 			initAllTraitementCmi(commission);
 			return NavigationRulesConst.SEE_CMI;
 		}
 
 		if (log.isDebugEnabled()) {
 			log.debug("leaving update");
 		}
 		return null;
 
 	}
 
 
 	/**
 	 * Delete a Commission to the dataBase.
 	 */
 	public String delete() {
 		getParameterService().deleteCommission(commission);
         reset();
         initCommissions();
         listCmiPojo = getData();
         addInfoMessage(null, "INFO.DELETE.SUCCESS");
         return null;
 	}
 
 
 	/**
 	 * The selected commission.
 	 */
 	public void selectCommission(final ValueChangeEvent event) {
 		Integer idCmi = (Integer) event.getNewValue();
 		if (idCmi != null) {
 			commission.setId(idCmi);
 			selectCommission();
 		}
 		commission = new Commission();
 		FacesContext.getCurrentInstance().renderResponse();
 	}
 
 
 	/**
 	 *  The selected commission.
 	 */
 	public void selectCommission() {
 		commission = getParameterService().getCommission(commission.getId(), null);
 		fj.data.List<Commission> list = fj.data.List.iterableList(selectedCommissions).cons(commission);
 		selectedCommissions = new ArrayList<Commission>(list.nub().toCollection());
 		actionEnum.setWhatAction(ActionEnum.EMPTY_ACTION);
 		commission = new Commission();
 	}
 
 	/**
 	 *  The selected commission.
 	 */
 	public void selectCommAdress() {
 		Commission commSelected = getParameterService().getCommission(idCmiForAdress, null);
 		Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         Integer codeRI = gest.getProfile().getCodeRI();
 		AdresseCommission adrRI =
                 commSelected.getContactsCommission().get(codeRI.toString()).getAdresse();
 		adressController.init(adrRI, true);
 		addInfoMessage(null, "COMMISSION.WARN_CHANGE_ADRESS");
 	}
 
 	/**
 	 * The selected commission.
 	 */
 	public void selectCommissionForLists(final ValueChangeEvent event) {
 		Integer idCmi = (Integer) event.getNewValue();
 		commission = new Commission();
 		commission.setId(idCmi);
 		selectCommissionForLists();
 	}
 
 	/**
 	 *  The selected commission for the print of the lists prepa.
 	 */
 	public void selectCommissionForLists() {
 		if (commission != null && commission.getId() != null) {
 			commission = getParameterService().getCommission(commission.getId(), null);
 		} else {
 			commission = new Commission();
 		}
 	}
 
 
 	/**
 	 * Remove member in membersToDisplay.
 	 */
 	public void removeMember() {
 		if (log.isDebugEnabled()) {
 			log.debug("enterind removeMember with memberToDelete = " + memberToDelete);
 		}
 		//TODO a voir le remove ne fonctionne pas pour le membre saisie
 		//TODO idem probleme d'affichage du flag (cf addCommission)
 		Map<Member, String> newMember = new HashMap<Member, String>();
 		for (Member m : membersToDisplay.keySet()) {
 			if (!m.equals(memberToDelete)) {
 				newMember.put(m, membersToDisplay.get(m));
 			}
 		}
 		membersToDisplay = newMember;
 		memberToDelete = null;
 	}
 
 
 	/**
 	 * remove commission in selectedCommissions.
 	 */
 	public void removeCmi() {
 		selectedCommissions.remove(commission);
 	}
 
 	/**
 	 * Add or update the members and the treatment CMI.
 	 */
 	private void addOrUpdateMember() {
 		if (!membersToDisplay.isEmpty()) {
 			//search profil member
 			Profile mbr = getParameterService().getProfile(null, Constantes.COD_PRO_MEMBER);
 			if (mbr == null) {
 				throw new ConfigException("Le profil MEMBRE n'existe pas. "
 						+ "Il faut qu'il soit enregistre dans la base de donner "
 						+ "pour ajouter des membres en tant que gestionnaires.");
 			}
 
 			//add Member
 			for (Member m : membersToDisplay.keySet()) {
 				String value = membersToDisplay.get(m);
 				if (log.isDebugEnabled()) {
 					log.debug("membre = " + m);
 					log.debug("value = " + value);
 				}
 				m.setCommission(commission);
 				if (m.getId().equals(0)) {
 					//TODO regarder probleme null
 					if (value != null && value.equals(MUST_BE_ADD_GEST)) {
 						//add to gestionnaire
 						m.getGestionnaire().setProfile(mbr);
 						m.getGestionnaire().setDateDbtValidite(new Date());
 						m.setGestionnaire(getDomainService().add(
                                 m.getGestionnaire(), getCurrentGest().getLogin()));
 						getDomainService().addUser(m.getGestionnaire());
 					}
 					getParameterService().addMember(m);
 				} else {
 					getParameterService().updateMember(m);
 				}
 			}
 		}
 
 	}
 
 
 	/**
 	 * Generate the PDF d'arrete de nomination d'une commission.
 	 */
 	public void makePDFNomination() {
 		commission = getParameterService().getCommission(commission.getId(), null);
 		String fileNameXml = String.valueOf(System.currentTimeMillis())
 								+ "_" + commission.getCode() + ".xml";
 		List<Object> list = new ArrayList<Object>();
 		SimpleDateFormat dateFormat = new SimpleDateFormat(Constantes.DATE_FORMAT);
 		list.add(dateFormat.format(new Date()));
 		// on trie les membres par ordre alphabetique, en mettant en tete de la liste le president
 		Set<Member> membersComm = new TreeSet<Member>(new ComparatorString(Member.class));
 		membersComm.addAll(commission.getMembers());
 		commission.setMembers(membersComm);
 		list.add(commission);
 		castorService.objectToFileXml(list, fileNameXml);
 
 		String fileNamePdf = "nomination_" + commission.getCode() + ".pdf";
 		CastorService cs = (CastorService) castorService;
 		PDFUtils.exportPDF(fileNameXml, FacesContext.getCurrentInstance(),
 					cs.getXslXmlPath(), fileNamePdf, Constantes.NOMINATION_XSL);
 
 	}
 
 	/**
 	 * Initialize the allTraitmentCmi(the treatment of commission) attribute.
 	 */
 	public void initAllTraitementCmi(final Commission c) {
 		trtCmiController.initAllTraitementCmi(c, null);
 	}
 
 	/**
 	 * Send the mail de convocation e une commission e tous les membres.
 	 */
 	public void sendMailConvocation() {
 		// recuperation du president de la commission
 		Member president = null;
 		for (Member m : commission.getMembers()) {
 			if (m.getType().compareToIgnoreCase(getString("COMMISSION.MBR.TYP_PRESI")) == 0) {
 				president = m;
 			}
 		}
 
 		// recuperation des adresses mail des membres de la commission
 		List<String> listTos = new ArrayList<String>();
 		for (Object member : membersSelected) {
 			Member m = (Member) member;
 			if (StringUtils.hasText(m.getAdressMail())) {
 				listTos.add(m.getAdressMail());
 			}
 		}
 
 		if (president == null || listTos.isEmpty()) {
 			if (president == null) {
 				addInfoMessage(null, "INFO.CANDIDAT.MAIL_NOT_PRESIDENT");
 			}
 			if (listTos.isEmpty()) {
 				addInfoMessage(null, "INFO.CANDIDAT.MAIL_NOT_MEMBER");
 			}
 			return;
 		}
 
 		if (convocMember != null) {
 			getDomainService().initOneProxyHib(commission, commission.getCalendarCmi(), CalendarCmi.class);
 			convocMember.send(listTos, commission, president);
 			addInfoMessage(null, "INFO.CANDIDAT.MAIL_OK");
 		}
 
 	}
 
 	/**
 	 * Generate the PDF des listes preparatoires par tri alphabetique.
 	 */
 	public void makePDFListesPreparatoireAlpha() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering makePDFListesPreparatoireAlpha");
 		}
 
 		// Recuperation de la liste des individus pour la generation
 		// de la liste preparatoire
 		List<IndListePrepaPojo> listeIndPrepa = createListeIndPrepa();
 
 		// on trie par ordre alphabetique la liste des individus
 		Collections.sort(listeIndPrepa, new ComparatorString(IndListePrepaPojo.class));
 
 		// on recupere le xsl correspondant e l'edition
 		// par ordre alphabetique
 		String fileNameXsl = Constantes.LISTE_PREPA_ALPHA_XSL;
 		String fileNamePdf = "listePreparatoire_" + commission.getCode() + ".pdf";
 		String fileNameXml = String.valueOf(System.currentTimeMillis())
 		+ "_" + commission.getCode() + ".xml";
 
 		// on genere le pdf
 		generatePDFListePreparatoire(fileNameXsl, fileNameXml, fileNamePdf, listeIndPrepa);
 
 		// remise d'actionEnum e EMPTY
 		actionEnum.setWhatAction(ActionEnum.EMPTY_ACTION);
 	}
 
 	/**
 	 * Generate the PDF des listes preparatoires par etape.
 	 */
 	public void makePDFListesPreparatoireEtape() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering makePDFListesPreparatoireEtape");
 		}
 
 		// Recuperation de la liste des individus pour la generation
 		// de la liste preparatoire
 		List<IndListePrepaPojo> listeIndPrepa = createListeIndPrepa();
 
 		// on repartie les individus par etape
 		Map<VersionEtapeDTO, List<IndListePrepaPojo>> mapIndListByEtape =
 			new TreeMap<VersionEtapeDTO, List<IndListePrepaPojo>>(
 					new ComparatorString(VersionEtapeDTO.class));
 
 		// boucle sur la liste des individus
 		for (IndListePrepaPojo ind : listeIndPrepa) {
 			// boucle sur la liste des voeux de chaque individu
 			for (IndVoeuPojo indVoeu : ind.getIndVoeuxPojo()) {
 				VersionEtapeDTO versEtp = indVoeu.getVrsEtape();
 				if (!mapIndListByEtape.containsKey(versEtp)) {
 					mapIndListByEtape.put(versEtp, new ArrayList<IndListePrepaPojo>());
 				}
 				// on cree un nouveau IndListePrepaPojo pour ne stocker que le voeu
 				IndListePrepaPojo newInd = new IndListePrepaPojo();
 				newInd.setCodeCmi(ind.getCodeCmi());
 				newInd.setNumDossierOpi(ind.getNumDossierOpi());
 				newInd.setNom(ind.getNom());
 				newInd.setPrenom(ind.getPrenom());
 				newInd.setBac(ind.getBac());
 				newInd.setTitreAccesDemande(ind.getTitreAccesDemande());
 				newInd.setDernierIndCursusScol(ind.getDernierIndCursusScol());
 				newInd.setIndVoeuxPojo(new HashSet<IndVoeuPojo>());
 				newInd.getIndVoeuxPojo().add(indVoeu);
 
 				mapIndListByEtape.get(versEtp).add(newInd);
 			}
 		}
 
 		// tri de chaque sous liste par ordre alphabetique
 		for (Map.Entry<VersionEtapeDTO, List<IndListePrepaPojo>>
 				indListForOneEtape : mapIndListByEtape.entrySet()) {
 			Collections.sort(indListForOneEtape.getValue(), new ComparatorString(IndListePrepaPojo.class));
 		}
 
 
 		// on recupere le xsl correspondant e l'edition
 		// par ordre alphabetique
 		String fileNameXsl = Constantes.LISTE_PREPA_ETAPE_XSL;
 		String fileNamePdf = "listePreparatoire_" + commission.getCode() + ".pdf";
 		String fileNameXml = String.valueOf(System.currentTimeMillis())
 		+ "_" + commission.getCode() + ".xml";
 
 		// on genere le pdf
 		generatePDFListePreparatoire(fileNameXsl, fileNameXml, fileNamePdf, mapIndListByEtape);
 
 		// remise d'actionEnum e EMPTY
 		actionEnum.setWhatAction(ActionEnum.EMPTY_ACTION);
 	}
 
 	/**
 	 * Generate the PDF des listes preparatoires par titre d'acces.
 	 */
 	public void makePDFListesPreparatoireTitre() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering makePDFListesPreparatoireTitre");
 		}
 
 		// Recuperation de la liste des individus pour la generation
 		// de la liste preparatoire
 		List<IndListePrepaPojo> listeIndPrepa = createListeIndPrepa();
 
 		// on repartie les individus par titre
 		Map<IndCursusScol, List<IndListePrepaPojo>> mapIndListByTitre =
 			new TreeMap<IndCursusScol, List<IndListePrepaPojo>>(new ComparatorString(IndCursusScol.class));
 		//new ComparatorString(IndCursusScol.class)
 		// boucle sur la liste des individus
 		for (IndListePrepaPojo ind : listeIndPrepa) {
 			IndCursusScol cursus = ind.getDernierIndCursusScol();
 			if (cursus != null ) {
 				if (!mapIndListByTitre.containsKey(cursus)) {
 					mapIndListByTitre.put(cursus, new ArrayList<IndListePrepaPojo>());
 				}
 				mapIndListByTitre.get(cursus).add(ind);
 			}
 		}
 
 		// tri de chaque sous liste par ordre alphabetique
 		for (Map.Entry<IndCursusScol, List<IndListePrepaPojo>>
 				indListForOneEtape : mapIndListByTitre.entrySet()) {
 			Collections.sort(indListForOneEtape.getValue(), new ComparatorString(IndListePrepaPojo.class));
 		}
 
 
 		// on recupere le xsl correspondant e l'edition
 		// par ordre alphabetique
 		String fileNameXsl = Constantes.LISTE_PREPA_TITRE_XSL;
 		String fileNamePdf = "listePreparatoire_" + commission.getCode() + ".pdf";
 		String fileNameXml = String.valueOf(System.currentTimeMillis())
 		+ "_" + commission.getCode() + ".xml";
 
 		// on genere le pdf
 		generatePDFListePreparatoire(fileNameXsl, fileNameXml, fileNamePdf, mapIndListByTitre);
 
 		// remise d'actionEnum e EMPTY
 		actionEnum.setWhatAction(ActionEnum.EMPTY_ACTION);
 	}
 
 	/**
 	 * Create the liste des individus pour la generation.
 	 * de la liste preparatoire
 	 */
 	public List<IndListePrepaPojo> createListeIndPrepa() {
 		if (log.isDebugEnabled()) {
 			log.debug("entering makePDFListesPreparatoire");
 		}
 		// hibernate session reattachment
 		commission = getParameterService().getCommission(
 				commission.getId(), commission.getCode());
 
 		 // recuperation de la liste des individus ayant fait un voeu dans la commission
 		List<Individu> listeInd = new ArrayList<>(
                 getDomainService().getIndividusCommission(
                         commission, null, new HashSet<>(wrap(listeRI).map(
                         new F<RegimeInscription, Integer>() {
                             public Integer f(RegimeInscription ri) {
                                 return ri.getCode();
                             }
                         }).toStandardList())).toCollection());
 
 		Set<Commission> listComm = new HashSet<Commission>();
 		listComm.add(commission);
 
 		List<IndividuPojo> listeIndPojo =
 			Utilitaires.convertIndInIndPojo(listeInd,
 					getParameterService(), getI18nService(),
 					getDomainApoService(), listComm, null,
 					getParameterService().getTypeTraitements(),
 					getParameterService().getCalendarRdv(), null, false);
 
 		for (IndividuPojo iP : listeIndPojo) {
 			iP.initIndCursusScolPojo(getDomainApoService(), getI18nService());
 		}
 
 		// boucle sur la liste des individu et creation de la liste de IndListePrepaPojo
 		List<IndListePrepaPojo> listeIndPrepa = new ArrayList<IndListePrepaPojo>();
 		for (IndividuPojo iP : listeIndPojo) {
 			// creation du nouveau IndListePrepaPojo
 			IndListePrepaPojo unIndPrepa = new IndListePrepaPojo();
 			// initialisation des champs
 
 			// code de la commission from Commission.code
 			unIndPrepa.setCodeCmi(commission.getCode());
 			// code du dossier de l'individu from IndividuPojo.individu.numDossierOpi
 			unIndPrepa.setNumDossierOpi(iP.getIndividu().getNumDossierOpi());
 			// nom de l'individu from IndividuPojo.individu.nomPatronymique
 			unIndPrepa.setNom(iP.getIndividu().getNomPatronymique());
 			// prenom de l'individu from IndividuPojo.individu.prenom
 			unIndPrepa.setPrenom(iP.getIndividu().getPrenom());
 			// bac de l'individu from IndividuPojo.individu.indBac (premier element de la liste)
 			for (IndBac i : iP.getIndividu().getIndBac()) {
 				BacOuxEqu b = getDomainApoService().getBacOuxEqu(
 						i.getDateObtention(),
 						ExportUtils.isNotNull(i.getCodBac()));
 				if (b != null) {
 					unIndPrepa.setBac(b.getLibBac());
 				} else {
 					unIndPrepa.setBac(i.getCodBac());
 				}
 				break;
 			}
 			if (iP.getDerniereAnneeEtudeCursus() != null) {
 				// titre fondant la demande from IndividuPojo.derniereAnneeEtudeCursus.libCur
 				unIndPrepa.setTitreAccesDemande(iP.getDerniereAnneeEtudeCursus().getLibCur());
 				// dernier cursus from  IndividuPojo.derniereAnneeEtudeCursus.cursus
 				unIndPrepa.setDernierIndCursusScol(
 						iP.getDerniereAnneeEtudeCursus().getCursus());
 			}
 
 			// boucle sur la liste des IndVoeuPojo
 			for (IndVoeuPojo iVoeuP : iP.getIndVoeuxPojo()) {
 
 				// on ajoute les voeux qui ne sont pas en transfert
 				if (!iVoeuP.getTypeTraitement().equals(transfert)) {
 					unIndPrepa.getIndVoeuxPojo().add(iVoeuP);
 				}
 			}
 
 
 			if (!unIndPrepa.getIndVoeuxPojo().isEmpty()) {
 				// ajout e la liste des individus
 				listeIndPrepa.add(unIndPrepa);
 			}
 		}
 		return listeIndPrepa;
 	}
 
 	/**
 	 * Generate the PDF e partir de la liste preparatoire precedemment generee.
 	 */
 	@SuppressWarnings("unchecked")
 	public void generatePDFListePreparatoire(
 			final String fileNameXsl, final String fileNameXml, final String fileNamePdf,
 			final Object listeIndPrepa) {
 		/**
 		 * Preparation de l'objet e envoyer e castor
 		 */
 		//List<Object> list = new ArrayList<Object>();
 		ListePrepaPojo list = new ListePrepaPojo();
 		// ajout de la date du jour de l'edition
 		SimpleDateFormat dateFormat = new SimpleDateFormat(Constantes.DATE_FORMAT);
 		list.setDate(dateFormat.format(new Date()));
 		Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
 		int codeRI = gest.getProfile().getCodeRI();
 		//Bug 7710: récupération dynamique de la date de début dépôt des dossiers
 		list.setDebut(dateFormat.format(getParameterService().getCampagneEnServ(codeRI).getDateDebCamp()));
 		// ajout de la commission pour afficher le libelle
 		list.setCommission(getParameterService().getCommission(
 				commission.getId(), commission.getCode()));
 
 		/**
 		 * Repartition selon les trois cas d'affichage de la liste preparatoire :
 		 * - affichage general par ordre alphabetique
 		 * - affichage par etape
 		 * - affichage par titre fondant la demande
 		 */
 		if (listeIndPrepa instanceof Map) {
 			list.setMapIndList((Map<?, ?>) listeIndPrepa);
 		} else {
 			list.setListeIndPrepa((List<IndListePrepaPojo>) listeIndPrepa);
 		}
 
 		/**
 		 * Generation du PDF
 		 */
 
 		castorService.objectToFileXml(list, fileNameXml);
 
 		CastorService cs = (CastorService) castorService;
 		PDFUtils.exportPDF(fileNameXml, FacesContext.getCurrentInstance(),
 					cs.getXslXmlPath(), fileNamePdf, fileNameXsl);
 
 	}
 
 	/* ### ALL CONTROL ####*/
 
 	/**
 	 * Control Commission attributes for the adding and updating.
 	 */
 	private Boolean ctrlEnter(final Commission c, final Adresse a,
 			final Boolean displayMessage) {
 		Boolean ctrlOk = true;
 		if (!StringUtils.hasText(c.getCode())) {
 			if (displayMessage) {
 				addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.CODE"));
 			}
 			ctrlOk = false;
 		} else {
 			if (!getParameterService().commissionCodeIsUnique(c)) {
 				ctrlOk = false;
 				if (displayMessage) {
 					addErrorMessage(null, "ERROR.FIELD.NOT_UNIQUE", getString("FIELD_LABEL.CODE"));
 				}
 			}
 		}
 		if (!StringUtils.hasText(c.getLibelle())) {
 			if (displayMessage) {
 				addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.LONG_LIB"));
 			}
 			ctrlOk = false;
 		}
 		if (StringUtils.hasText(a.getMail())
 				&& !Utilitaires.isFormatEmailValid(a.getMail())) {
 
 			if (displayMessage) {
 				addErrorMessage(null, "ERROR.FIELD.INVALID", getString("FIELD_LABEL.MAIL"));
 			}
 			ctrlOk = false;
 		}
 		if (log.isDebugEnabled()) {
 			log.debug("leaving ctrlAdd return = " + ctrlOk);
 		}
 		return ctrlOk;
 	}
 
 	/**
 	 * Control de saisie d'un membre.
 	 */
 	private Boolean ctrlMember(final Member r) {
 		Boolean ctrlOk = true;
 		if (!StringUtils.hasText(r.getNom())) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.NAME"));
 			ctrlOk = false;
 		}
 		if (!StringUtils.hasText(r.getPrenom())) {
 			addErrorMessage(null, Constantes.I18N_EMPTY, getString("INDIVIDU.PRENOM"));
 			ctrlOk = false;
 		}
 		if (StringUtils.hasText(r.getAdressMail())) {
 			if (!Utilitaires.isFormatEmailValid(r.getAdressMail())) {
 				addErrorMessage(null, "ERROR.FIELD.INVALID", getString("FIELD_LABEL.MAIL"));
 				ctrlOk = false;
 			}
 		}
 
 		if (!ctrlOk) {
 			addErrorMessage(null, "ERROR.MBR_CMI.NOT_VALID");
 		}
 
 		if (log.isDebugEnabled()) {
 			log.debug("leaving ctrlAdd return = " + ctrlOk);
 		}
 		return ctrlOk;
 	}
 
 
 	/**
 	 * @return boolean false if not valid.
 	 */
 	private boolean ctrlAllMbrInMbrToDisplay() {
 		boolean addOk = true;
 		for (Member m : membersToDisplay.keySet()) {
 			addOk = ctrlMember(m);
 		}
 		return addOk;
 	}
 
 	protected List<CommissionPojo> getData() {
 		List<CommissionPojo> result = new ArrayList<CommissionPojo>();
 		Gestionnaire currentGest = getCurrentGest();
 		if (currentGest != null) {
 			List <Commission> lesCommissions = new ArrayList<Commission>();
 			Set<Commission> s =	getDomainApoService().getListCommissionsByRight(currentGest, null);
 			lesCommissions.addAll(s);
 			Integer codRI = currentGest.getProfile().getCodeRI();
 
 			// création de la liste de commissionPojo
 			for (Commission comm : lesCommissions) {
 				List<TraitementCmi> trtCmi = new ArrayList<TraitementCmi>();
 				List<TraitementCmi> trtCmiOff = new ArrayList<TraitementCmi>();
 				trtCmi.addAll(comm.getTraitementCmi());
 
 				/**
 				 * Règle pour les flags :
 				 * - rouge si aucun trt en service (trtCmi vide)
 				 * - orange si certains trt hors service (trtCmi non vide et trtCmiOff non vide)
 				 * - vert sinon (trtCmi non vide et trtCmiOff vide)
 				 */
 				for (TraitementCmi trt : comm.getTraitementCmi()) {
 					if (Utilitaires.isTraitementCmiOff(trt, codRI)) {
 						trtCmi.remove(trt);
 						trtCmiOff.add(trt);
 					}
 				}
 				CommissionPojo commPojo = new CommissionPojo();
 				commPojo.setCommission(comm);
 				commPojo.setFlagWithoutTrtActive(trtCmi.isEmpty());
 				commPojo.setFlagWithSomeTrtInactive(!trtCmi.isEmpty() && !trtCmiOff.isEmpty());
 				result.add(commPojo);
 			}
 			Collections.sort(result, new ComparatorString(CommissionPojo.class));
 		}
 		return result;
 	}
 
 	/*
 	 ******************* ACCESSORS ******************** */
 
 	/**
 	 * @return true si l'on veut que la liste des commissions soit filtrée par droits
 	 */
 	public boolean isListCmiByRight() {
 		return listCmiByRight;
 	}
 
 	public void setListCmiByRight(boolean listCmiByRight) {
 		this.listCmiByRight = listCmiByRight;
 	}
 
 	/**
 	 * @return List<Commission> All commission in dataBase.
 	 */
 	public Set<Commission> getCommissions() {
 		return commissions;
 	}
 
 	/**
 	 * Commissions items for the select menu.
 	 * This list contains all cmi in use or all cmi in use wihtout cmi with calendar.
 	 * Depends to wayfEnum.
 	 * @return List<Commission>
 	 */
 	public Set<Commission> getCommissionsItems() {
 		return comsInUse;
 	}
 
 	/**
 	 * Commissions items for the select menu.
 	 * the list is function the commisions managed by the gestionnaire
 	 * @return List<Commission>
 	 */
 	public List<Commission> getCommissionsItemsByRight() {
 		return comsInUseByRight;
 	}
 
 	/**
 	 * Commissions items for the select menu.
 	 * the list is function the commisions managed by the gestionnaire if listCmiByRight is true
 	 * else return all the commissions
 	 * @return List<Commission>
 	 */
 	public List<Commission> getCommissionsItemsByRightParametrable(boolean doFilter) {
 		return (listCmiByRight || doFilter) ?
                 comsInUseByRight :
                 new ArrayList<Commission>(comsInUse);
 	}
 
 	/**
 	 * Commissions items for the select menu.
 	 * the list is function the commisions managed by the gestionnaire
 	 * @return List<Commission>
 	 */
 	public List<Commission> getCommissionsItemsByRightAndIsFormComp() {
 		return comsWithForms;
 	}
 
 	/**
 	 * Commissions items for the select menu.
 	 * the list is function the commissions without treatment
 	 * @return Set<Commission>
 	 */
 	public Set<Commission> getCommissionsItemsWithoutTrt() {
 		return comsNoTrt;
 	}
 
 	/**
 	 * Commissions items for the select menu.
 	 * the list is function the commissions without treatment
 	 * @return List<Commission>
 	 */
 	public List<Commission> getCommissionsItemsWithoutTrtAsList() {
 		return new ArrayList<Commission>(comsNoTrt);
 	}
 
 	/**
 	 * Commissions items for managedTrtCmi list.
 	 * the list is function the commissions managed by the gestionnaire
 	 * @return Set<Commission>
 	 */
 	@SuppressWarnings("synthetic-access")
 	public Set<Commission> getAllCommissionsItemsByRight() {
 		return new TreeSet<Commission>(comparatorCmi) {{
 			addAll(comsInUseByRight);
 			addAll(comsNoTrt);
 		}};
 	}
 
 	/**
 	 * Commissions items for the creation of an adress.
 	 * the list is function the commissions managed by the gestionnaire
 	 * @return Set<Commission>
 	 */
 	public List<Commission> getCommissionsForAdresses() {
 		Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
 		final Integer codeRI = gest.getProfile().getCodeRI();
 
 		return new ArrayList<Commission>(iterableStream(comsInUseByRight).filter(
 				new F<Commission, Boolean>() {
 					public Boolean f(Commission c) {
 						final Commission cmi = getParameterService().getCommission(c.getId(), null);
 						return cmi.getContactsCommission().get(codeRI.toString()) != null;
 					}}).toCollection());
 	}
 
 	/**
 	 * Type members items for the select menu.
 	 * @return List<SelectItem>
 	 */
 	public List<SelectItem> getTypMbrItems() {
 		List<SelectItem> list = new ArrayList<SelectItem>();
 		list.add(new SelectItem(getString("COMMISSION.MBR.TYP_MBR"), getString("COMMISSION.MBR.TYP_MBR")));
 		list.add(new SelectItem(getString("COMMISSION.MBR.TYP_PRESI"), getString("COMMISSION.MBR.TYP_PRESI")));
 
 		return list;
 	}
 
 	/**
 	 * All commission in use in dataBase are been managed by Manager.
 	 * @return Set<Commission>
 	 */
 	// TODO : à supprimer, méthode identique getCommissionsItemsByRight()
 	public List<Commission> getCommissionsByRight() {
 		return getCommissionsItemsByRight();
 	}
 
 	/**
 	 * @return Set<Commission> commissions in use.
 	 */
 	// TODO : à supprimer, méthode identique getCommissionsItems()
 	public Set<Commission> getCommissionsInUse() {
 		return getCommissionsItems();
 	}
 
 
 	/**
 	 * @return the selectedCommissions
 	 */
 	public List<Commission> getSelectedCommissions() {
 		return selectedCommissions;
 	}
 
 	/**
 	 * @param selectedCommissions the selectedCommissions to set
 	 */
 	public void setSelectedCommissions(final List<Commission> selectedCommissions) {
 		this.selectedCommissions = selectedCommissions;
 	}
 
 	/**
 	 * @return the membersSelected
 	 */
 	public Object[] getMembersSelected() {
 		return membersSelected;
 	}
 
 	/**
 	 * @param membersSelected the membersSelected to set
 	 */
 	public void setMembersSelected(final Object[] membersSelected) {
 		this.membersSelected = membersSelected;
 	}
 
 	/**
 	 * @return the transfert
 	 */
 	public Transfert getTransfert() {
 		return transfert;
 	}
 
 	/**
 	 * @param transfert the transfert to set
 	 */
 	public void setTransfert(final Transfert transfert) {
 		this.transfert = transfert;
 	}
 
 	/**
 	 * return membersToDisplay.keySet.
 	 * @return Set<Member>
 	 */
 	public List<Member> getKeySetMbrToDisplay() {
 		List<Member> members = new ArrayList<Member>();
 		members.addAll(getMembersToDisplay().keySet());
 		Collections.sort(members, new ComparatorString(Member.class));
 		return members;
 	}
 
 	/**
 	 * @return List<SignataireDTO>
 	 */
 	public List<SignataireDTO> getSignataireInUse() {
 		List<SignataireDTO> l = getDomainApoService().getSignataires();
 		Collections.sort(l, new ComparatorString(SignataireDTO.class));
 		return l;
 	}
 
 	/**
 	 * @return the commission
 	 */
 	public Commission getCommission() {
 		return commission;
 	}
 
 	/**
 	 * @param commission the commission to set
 	 */
 	public void setCommission(final Commission commission) {
 		//Clone est utilise afin que l'utilisateur puisse modifier
 		//l'objet sans toucher au CACHE (par reference)
 		//Probleme rencontre lors du modification annule(par exemple),
 		//le cache etait tout de meme modifier
 		//this.commission = commission.clone();
 		// TODO : does it still work without clone() ?
 		this.commission = commission;
 	}
 
 	/**
 	 * @return the contactCommission
 	 */
 	public ContactCommission getContactCommission() {
 		return contactCommission;
 	}
 
 	/**
 	 * @param contactCommission the contactCommission to set
 	 */
 	public void setContactCommission(final ContactCommission contactCommission) {
 		this.contactCommission = contactCommission;
 	}
 
 	/**
 	 * @return the actionEnum
 	 */
 	public ActionEnum getActionEnum() {
 		return actionEnum;
 	}
 
 	/**
 	 * @param actionEnum the actionEnum to set
 	 */
 	public void setActionEnum(final ActionEnum actionEnum) {
 		this.actionEnum = actionEnum;
 	}
 
 	/**
 	 * @return the objectToAdd
 	 */
 	public Object[] getObjectToAdd() {
 		return objectToAdd.toArray();
 	}
 
 	/**
      * @param objectToAdd the objectToAdd to set
      */
 	public void setObjectToAdd(final Object[] objectToAdd) {
 		this.objectToAdd = Arrays.asList(objectToAdd);
 	}
 
 	/**
 	 * @return the membersToDisplay
 	 */
 	public Map<Member, String> getMembersToDisplay() {
 		return membersToDisplay;
 	}
 
 	/**
 	 * @param membersToDisplay the membersToDisplay to set
 	 */
 	public void setMembersToDisplay(final Map<Member, String> membersToDisplay) {
 		this.membersToDisplay = membersToDisplay;
 	}
 
 	/**
 	 * @return the memberToDelete
 	 */
 	public Member getMemberToDelete() {
 		return memberToDelete;
 	}
 
 	/**
 	 * @param memberToDelete the memberToDelete to set
 	 */
 	public void setMemberToDelete(final Member memberToDelete) {
 		this.memberToDelete = memberToDelete;
 	}
 
 
 
 
 	/*----------------------------------------
 	 *  GETTERS POUR JSF
 	 */
 
 	/**
 	 * @return the IS_GESTIONNAIRE
 	 */
 	public String getIsGestionnaire() {
 		return IS_GESTIONNAIRE;
 	}
 
 	/**
 	 * @return the MUST_BE_ADD_GEST
 	 */
 	public String getMustBeGest() {
 		return MUST_BE_ADD_GEST;
 	}
 
 	/**
 	 * @return the ENTER_MBR
 	 */
 	public String getEnterMbr() {
 		return ENTER_MBR;
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
 	 * @return the listeRI
 	 */
 	public List<RegimeInscription> getListeRI() {
 		return listeRI;
 	}
 
 	/**
 	 * @param listeRI the listeRI to set
 	 */
 	public void setListeRI(final List<RegimeInscription> listeRI) {
 		this.listeRI = listeRI;
 	}
 
 	/**
 	 * @return the canModifyRISearch
 	 */
 	public Boolean getCanModifyRISearch() {
 		return canModifyRISearch;
 	}
 
 	/**
 	 * @param canModifyRISearch the canModifyRISearch to set
 	 */
 	public void setCanModifyRISearch(final Boolean canModifyRISearch) {
 		this.canModifyRISearch = canModifyRISearch;
 	}
 
 	/**
 	 * @return the idCmiForAdress
 	 */
 	public Integer getIdCmiForAdress() {
 		return idCmiForAdress;
 	}
 
 	/**
 	 * @param idCmiForAdress the idCmiForAdress to set
 	 */
 	public void setIdCmiForAdress(final Integer idCmiForAdress) {
 		this.idCmiForAdress = idCmiForAdress;
 	}
 
 	/**
 	 * @param adressController the adressController to set
 	 */
 	public void setAdressController(final AdressController adressController) {
 		this.adressController = adressController;
 	}
 
 	/**
 	 * @return the deep link for the students
 	 */
 	public Map<Integer, String> getDeepLinks() {
 		Map<Integer, String> deepLinks;
 		// TODO : optimiser !
 		// if (deepLinks == null) {
 			deepLinks = new HashMap<Integer, String>();
 //			for (BeanTrtCmi bean : allTraitementCmi) {
 //				String url = getDomainService().getFormationUrl(false,
 //						bean.getTraitementCmi().getVersionEtpOpi().getCodEtp(),
 //						bean.getTraitementCmi().getVersionEtpOpi().getCodVrsVet().toString());
 //				deepLinks.put(bean.getTraitementCmi().getId(), url);
 //			}
 			return deepLinks;
 		// }
 	}
 
 
 	/**
 	 * @return the castorService
 	 */
 	public ISerializationService getCastorService() {
 		return castorService;
 	}
 
 	/**
 	 * @param castorService the castorService to set
 	 */
 	public void setCastorService(final ISerializationService castorService) {
 		this.castorService = castorService;
 	}
 
 
 	/**
 	 * @return the listCmiPojo
 	 */
 	public List<CommissionPojo> getListCmiPojo() {
 		return listCmiPojo;
 	}
 
 	/**
 	 * @param listCmiPojo the listCmiPojo to set
 	 */
 	public void setListCmiPojo(List<CommissionPojo> listCmiPojo) {
 		this.listCmiPojo = listCmiPojo;
 	}
 
 	/**
 	 * @return the filteredListCmiPojo
 	 */
 	public List<CommissionPojo> getFilteredListCmiPojo() {
 		return filteredListCmiPojo;
 	}
 
 	/**
 	 * @param filteredListCmiPojo the filteredListCmiPojo to set
 	 */
 	public void setFilteredListCmiPojo(List<CommissionPojo> filteredListCmiPojo) {
 		this.filteredListCmiPojo = filteredListCmiPojo;
 	}
 
 	public void setTrtCmiController(final TrtCmiController trtCmiController) {
 		this.trtCmiController = trtCmiController;
 	}
 
 
 	/**
 	 * @param convocMember the convocMember to set
 	 */
 	public void setConvocMember(final MailContentService convocMember) {
 		this.convocMember = convocMember;
 	}
 
 	/**
 	 * @return the managerUsed
 	 */
 	public boolean isManagerUsed() {
 		return managerUsed;
 	}
 
 	/**
 	 * @param managerUsed the managerUsed to set
 	 */
 	public void setManagerUsed(final boolean managerUsed) {
 		this.managerUsed = managerUsed;
 	}
 
     public LdapAttributes getLdapAttrs() {
         return ldapAttrs;
     }
 
     public void setLdapAttrs(LdapAttributes ldapAttrs) {
         this.ldapAttrs = ldapAttrs;
     }
 }
