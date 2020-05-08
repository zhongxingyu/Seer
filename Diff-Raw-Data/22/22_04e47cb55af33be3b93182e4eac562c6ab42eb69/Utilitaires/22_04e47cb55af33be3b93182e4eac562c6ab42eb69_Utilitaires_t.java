 package org.esupportail.opi.web.beans.utils;
 
 
 import fj.*;
 import fj.data.Stream;
 import org.esupportail.commons.annotations.cache.RequestCache;
 import org.esupportail.commons.services.i18n.I18nService;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.services.smtp.SmtpService;
 import org.esupportail.opi.domain.DomainApoService;
 import org.esupportail.opi.domain.DomainService;
 import org.esupportail.opi.domain.ParameterService;
 import org.esupportail.opi.domain.beans.parameters.Campagne;
 import org.esupportail.opi.domain.beans.parameters.TypeDecision;
 import org.esupportail.opi.domain.beans.parameters.TypeTraitement;
 import org.esupportail.opi.domain.beans.references.calendar.CalendarIns;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.LinkTrtCmiCamp;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.references.rendezvous.CalendarRDV;
 import org.esupportail.opi.domain.beans.references.rendezvous.VetCalendar;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 import org.esupportail.opi.domain.beans.user.candidature.VersionEtpOpi;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.CommissionPojo;
 import org.esupportail.opi.web.beans.pojo.IndVoeuPojo;
 import org.esupportail.opi.web.beans.pojo.IndividuPojo;
 import org.esupportail.wssi.services.remote.CentreGestion;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.hibernate.LazyInitializationException;
 import org.springframework.util.StringUtils;
 
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import java.io.File;
 import java.lang.Class;
 import java.text.ParseException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 
 
 /**
  * Utility class.
  * 
  */
 public class Utilitaires {
 
 	/**
 	 * 
 	 */
 	public static final Logger LOGGER = new LoggerImpl(Utilitaires.class);	
 
 	/**
 	 * Constructors.
 	 */
 	private Utilitaires() {
 		super();
 	}
 
 	/*
 	 ******************* METHODS ********************** */
 	/**
 	 * Check if the postal code is valid.
 	 * 
 	 * @param str
 	 *            The String to test.
 	 * @return True if the postal code is valid, False otherwise.
 	 */
 	public static boolean isCodePostalValid(final String str) {
 		boolean isCodePostValid = str.matches(Constantes.CODEPOSTREGEX);
 		return isCodePostValid;
 	}
 
 	/**
 	 * Check if the str is valid for this regex.
 	 * 
 	 * @param str
 	 *            The String to test.
 	 * @param regex 
 	 * @return True if the str is valid, False otherwise.
 	 */
 	public static boolean isStringValid(final String str, final String regex) {
 		boolean isCodeEtuValid = str.matches(regex);
 		return isCodeEtuValid;
 	}
 
 	/**
 	 * Check if the email is valid.
 	 * 
 	 * @param str
 	 *            The String to test.
 	 * @return True if the email is valid, False otherwise.
 	 */
 	public static boolean isFormatEmailValid(final String str) {
 		// TODO The pattern MAILREGEX do not match the mail adress with '+' in it (plus adressing)
 		if (StringUtils.hasText(str)) {
 			boolean isEmailValid = str.matches(Constantes.MAILREGEX);
 			return isEmailValid;
 		}
 		return false;
 	}
 	
 	/**
 	 * Escape special characters in HQL queries.
 	 * @param str
 	 * @return String
 	 */
 	public static String transformLikeHQL(final String str) {
 		String[] hqlRegex = Constantes.HQLREGEX;
 		String likeHql = str;
 		for (int i = 0; i < hqlRegex.length; i++) {
 			likeHql = likeHql.replaceAll(hqlRegex[i], hqlRegex[i] + hqlRegex[i]);
 		}
 		return likeHql;
 	}
 
 	/**
 	 * Check if the date is valid. 
 	 * 
 	 * @param date The String to test.
 	 * @param format 
 	 * @return True if the date is valid, not empty and not null, False otherwise.
 	 */
 	public static boolean isFormatDateValid(final String date, final String format) {
 		Boolean isValid = false;
 		if (StringUtils.hasText(date)) {
 			try {
 				SimpleDateFormat s = new SimpleDateFormat(format);
 				Date d = s.parse(date);
 				String t = s.format(d);
 				if (date.length() != Constantes.YEAR_FORMAT.length()
 						|| !t.equals(date)) {
 					isValid = false;
 				} else {
 					isValid = true;
 				}
 			} catch (ParseException e) {
 				isValid = false;
 			}
 
 		} 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("leaving isFormatDateValid with date = " 
 					+ date + "and format = " + format + " return " + isValid);
 		}
 		return isValid;
 	}
 
 
 	/**
 	 * Clean phone tel string (without '.' and ' ').
 	 * 
 	 * @param tel
 	 *            The String to clean.
 	 * @return a clean phone tel string.
 	 */
 	public static String cleanPhoneTel(final String tel) {
 		if (StringUtils.hasText(tel)) {
 			return tel.replaceAll("[. ]", "");
 		}
 		return tel;
 	}
 
 
 	/**
 	 * Upper case the first character.
 	 * @param str
 	 * @param lowerForEndWord true : lower case the end word
 	 * @return String with upper case the first character.
 	 */
 	public static String upperCaseFirstChar(final String str, final Boolean lowerForEndWord) {
 		if (!StringUtils.hasText(str)) {
 			return str;
 		}
 		if (lowerForEndWord) {
 			return Character.toUpperCase(str.charAt(0)) 
 			+ str.toLowerCase().substring(1);
 		}
 		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
 	}
 
 
 	/**
 	 * Return a truncated string with a [...] if the length exceeds 'limitLength'
 	 * 		otherwise return 'str'.
 	 * 
 	 * @param str
 	 * @param limitLength
 	 * @return String
 	 */
 	public static String limitStrLength(final String str, final int limitLength) {
 		if (str != null && (str.length() > limitLength)) {
 			return str.substring(0, limitLength) + "[...] ";
 		}
 		return str;
 	}
 
 	/**
 	 * The user's code that is one action (add or update).
 	 * the manager is a priority because he can connect to an individual
 	 * @param currentUser
 	 * @param currentInd
 	 * @return String
 	 */
 	public static String codUserThatIsAction(final Gestionnaire currentUser,
 			final IndividuPojo currentInd) {
 		String codUserThatIsTheAction = null;
 		if (currentUser != null) {
 			codUserThatIsTheAction = currentUser.getLogin();
 		} else if (currentInd != null) {
 			codUserThatIsTheAction = currentInd.getIndividu().getNumDossierOpi();
 		}
 		return codUserThatIsTheAction;
 	}
 
 
 	/**
 	 * Renvoie une map contenant en clef une commission et en valeur une liste de versioEtape.
 	 * Ce sont les Vet demandees par un candidat(voeux) et gerees par la commission.  
 	 * @param cmi
 	 * @param indVoeuxPojo
 	 * @param camp 
 	 * @return Map< Commission, Set< VersionEtapeDTO>>
 	 */
 	public static Map<Commission, Set<VersionEtapeDTO>> getCmiForIndVoeux(
 			final Set<Commission> cmi, 
 			final Stream<IndVoeuPojo> indVoeuxPojo,
 			final Campagne camp) {
 		//map contenant la commission et ses etapes sur lesquelles le candidat e deposer des voeux
 		Set<VersionEtapeDTO> list = new HashSet<VersionEtapeDTO>();
 		if (indVoeuxPojo != null) {
 			for (IndVoeuPojo indV : indVoeuxPojo) {
 				list.add(indV.getVrsEtape());
 			}		
 		}
 		return getCmiForVetDTO(cmi, list, camp);
 	}
 
 
 
 	/**
 	 * Renvoie une map contenant en clef une commission et en valeur une liste de versioEtape.
 	 * Ce sont les Vet demandees par un candidat(voeux) et gerees par la commission.  
 	 * @param cmi
 	 * @param list 
 	 * @param camp 
 	 * @return Map< Commission, Set< VersionEtapeDTO>>
 	 */
 	public static Map<Commission, Set<VersionEtapeDTO>> getCmiForVetDTO(
 			final Set<Commission> cmi, 
 			final Set<VersionEtapeDTO> list,
 			final Campagne camp) {
 		//map contenant la commission et ses etapes sur lesquelles le candidat e deposer des voeux
 		Map<Commission, Set<VersionEtapeDTO>> mapCmi = new HashMap<Commission, Set<VersionEtapeDTO>>();
 		for (Commission c : cmi) {
 			//parcours des voeux du candidat
 			for (VersionEtapeDTO v : list) {
 				if (c.getTraitementCmi() != null) {
 					for (TraitementCmi trt : c.getTraitementCmi()) {
 						if (trt.getVersionEtpOpi().getCodEtp().equals(v.getCodEtp())
 								&& trt.getVersionEtpOpi().getCodVrsVet()
 								.equals(v.getCodVrsVet())) {
 							// on vÃ©rifie que le traitement est rattachÃ©
 							// Ã  la campagne courante
 							if (isTraitementCmiInCamp(trt, camp)) {
 								if (mapCmi.get(c) == null) {
 									Set<VersionEtapeDTO> firstVrsEtp 
 										= new HashSet<VersionEtapeDTO>();
 									firstVrsEtp.add(v);
 									mapCmi.put(c.clone(), firstVrsEtp);
 								} else {
 									mapCmi.get(c).add(v);
 								}
 							}
 						} 
 					}
 				}
 			}
 		}
 
 		return mapCmi;
 	}
 
 
 	
 	
 	/**
 	 * @param n1
 	 * @param annee
 	 * @return Set
 	 */
 //	public static Set<VersionEtpOpi> getVersionEtpOpi(final Ren1ClesAnnuFormPojo n1,
 //			final String annee, final DomainApoService domainApoService) {
 //		Set<VersionEtapeDTO> listEtp = new HashSet<VersionEtapeDTO>();
 //		for (VersionDiplomeDTO vrsDip 
 //				: domainApoService.getVersionDiplomes(n1.getRen1ClesAnnuForm().getCodCles(), annee)) {
 //			listEtp.addAll(domainApoService.getVersionEtapes(vrsDip, annee));
 //		}
 //		return convertVetInVetOpi(listEtp);
 //	}
 
 
 	/**
 	 * Convert a Individu list in IndividuPojo list.
 	 * @param listInd
 	 * @param parameterService
 	 * @param iService
 	 * @param apoServ 
 	 * @param commissions 
 	 * @param typesDecisions 
 	 * @param versionsEtape 
 	 * @return List< IndividuPojo>
 	 */
 	public static List<IndividuPojo> convertIndInIndPojo(
 			final List<Individu> listInd,
 			final ParameterService parameterService,
 			final I18nService iService,
 			final DomainApoService apoServ,
 			final Set<Commission> commissions,
 			final Set<TypeDecision> typesDecisions,
 			final List<TypeTraitement> typeTraitements,
 			final List<CalendarRDV> listCalendrierParam,
 			final Set<VersionEtapeDTO> versionsEtape,
 			final boolean excludeWishProcessed) {
 		List<IndividuPojo> indPojo = new ArrayList<IndividuPojo>();
 		if (listInd != null) {
 			for (Individu i : listInd) {
 				IndividuPojo iPojo = new IndividuPojo(i, apoServ, iService,
 						parameterService, commissions, typesDecisions, typeTraitements, listCalendrierParam, versionsEtape);
 				if (!excludeWishProcessed || !iPojo.getHasAllVoeuxTraited()) {
 					indPojo.add(iPojo);
 				}
 			}
 		}
 		return indPojo;
 
 	}
 
 	/**
 	 * @param individu
 	 * @return the campagne en service.
 	 */
 	public static Campagne getCampagneEnServ(final Individu individu) {
 		for (Campagne camp : individu.getCampagnes()) {
 			if (camp.getTemoinEnService()) {
 				return camp;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * @param individu
 	 * @return the campagne en service.
 	 */
 	public static Campagne getCampagneEnServ(final Individu individu, 
 			final DomainService domainService) {
 		List<Campagne> listCamp = new ArrayList<Campagne>();
		Individu nonLazyInd = domainService.getIndividu(individu.getNumDossierOpi(), individu.getDateNaissance());
		listCamp.addAll(nonLazyInd.getCampagnes());
        for (Campagne camp : listCamp) {
            if (camp.getTemoinEnService()) {
                return camp;
            }
        }
        //TODO return a NullObject pattern to avoid NPE
 		return null;
 	}
 
 	/**
 	 * @param individu
 	 * @return the codeRI of the campagne en service.
 	 */
 	public static Integer getCodeRIIndividu(final Individu individu, 
 			final DomainService domainService) {
 		List<Campagne> listCamp = new ArrayList<Campagne>();
 		
 		try {
 			listCamp.addAll(individu.getCampagnes());
 		} catch (LazyInitializationException lazy) {
 			domainService.initOneProxyHib(individu, individu.getCampagnes(), Campagne.class);
 			listCamp.addAll(individu.getCampagnes());
 		} finally {
 			for (Campagne camp : listCamp) {
 				if (camp.getTemoinEnService()) {
 					return camp.getCodeRI();
 				}
 			}
 		}
 		
 		return null;
 	}
 
     public static F2<SmtpService, Boolean, Effect<P5<InternetAddress, String, String, String, List<File>>>> sendEmail =
             new F2<SmtpService, Boolean, Effect<P5<InternetAddress, String, String, String, List<File>>>>() {
                 public Effect<P5<InternetAddress, String, String, String, List<File>>> f(
                         final SmtpService smtpService,
                         final Boolean intercept) {
                     return new Effect<P5<InternetAddress, String, String, String, List<File>>>() {
                         public void e(P5<InternetAddress, String, String, String, List<File>> p5) {
                             if (intercept)
                                 smtpService.send(p5._1(), p5._2(), p5._3(), p5._4(), p5._5());
                             else
                                 smtpService.sendDoNotIntercept(p5._1(), p5._2(), p5._3(), p5._4(), p5._5());
                         }
                     };
                 }
             };
 
 	/**
 	 * send Email to the individual.
 	 */
 	public static void sendEmailIndividu(final String subject, 
 			final String htmlBody, 
 			final String endBody,
 			final Individu ind, 
 			final SmtpService smtpService,
 			final I18nService iService) {
 		InternetAddress to1 = null;
 		InternetAddress to2 = null;
 		try {
 			if (StringUtils.hasText(ind.getAdressMail())) {
 				to1 = new InternetAddress(ind.getAdressMail());
 
 			}
 			if (StringUtils.hasText(ind.getEmailAnnuaire())) {
 				to2 = new InternetAddress(ind.getEmailAnnuaire());
 			}
 			List<InternetAddress> listTos = new ArrayList<InternetAddress>();
 			if (to1 != null) { listTos.add(to1); }
 			if (to2 != null) { listTos.add(to2); }
 
 			InternetAddress[] tos = new InternetAddress[listTos.size()];
 			for (int i = 0; i < listTos.size(); ++i) { tos[i] = listTos.get(i); }
 			
 			StringBuilder s = new StringBuilder(makeHtmlBody(htmlBody, iService, ind.getNumDossierOpi()));
 			if (StringUtils.hasText(endBody)) {
 				s.append(endBody);
 			}
 //			smtpService.sendtocc(
 //					tos, null, null, subject, 
 //					s.toString(), null, null);
 			
 			smtpService.send(
 					tos[0], subject, 
 					s.toString(), null);
 		} catch (AddressException e) {
 			LOGGER.error("error in instance Internet adresse for individu with numDosOpi = " 
 					+ ind.getNumDossierOpi(), e);
 		}
 	}
 
 	/**
 	 * Tous les mails commencent et finissent par les memes textes.
 	 * On ajoute e htmlBody ces 2 parties.
 	 * @param htmlBody
 	 * @param iService
 	 * @param numDossier
 	 * @return String
 	 */
 	public static String makeHtmlBody(final String htmlBody, final I18nService iService, final String numDossier) {
 		if (iService != null) {
 			StringBuilder s = new StringBuilder();
 			s.append(iService.getString("MAIL.NOT_RESPONSE"));
 			s.append(htmlBody);
 			s.append(iService.getString("MAIL.END.RAPPEL_NUM_DOS", numDossier));
 			return s.toString();
 		}
 		return htmlBody;
 	}
 
 	/**
 	 * The current year for university is :
 	 * July to December = year = n + 1.
 	 * January to June = year = n.
 	 * @param period id true return current year - current year + 1 (Ex.: 2009 - 2010)
 	 * @return String
 	 */
 //	public static String getCurrentYear(final Boolean period) {
 //		Calendar c = Calendar.getInstance();
 //		int year = c.get(Calendar.YEAR);
 //		int month = c.get(Calendar.MONTH);
 //		if (month >= Calendar.DECEMBER && month <= Calendar.DECEMBER) {
 //			year += 1;
 //		}
 //		if (period) {
 //			return String.valueOf(year) + " - " + String.valueOf(year + 1);
 //		}
 //		return String.valueOf(year);
 //	}
 
 	/**
 	 * List of the commissions without treatment.
 	 * @param parameterService 
 	 * @return Set< Commission>
 	 */
 	public static Set<Commission> getListCommissionsWithoutTrt(
 			final ParameterService parameterService) {
 		Set<Commission> lesCommissions = new HashSet<Commission>();	
 		// renvoie les cmi qui ont aucune etape
 		for (Commission c : parameterService.getCommissions(null)) {
 			if (c.getTraitementCmi().isEmpty()) {
 				lesCommissions.add(c);
 			}
 		}
 
 		return lesCommissions;
 	}
 	
 	/**
 	 * A traitementCmi is off if all linkTrtCmiCamp are off.
 	 * @param trtCmi
 	 * @param codeRI
 	 * @return true if the traitement is off
 	 */
 	public static boolean isTraitementCmiOff(final TraitementCmi trtCmi, final Integer codeRI) {
 		// un traitement est off si tous ses link sont off
 		boolean isOff = true;
 		for (LinkTrtCmiCamp link : trtCmi.getLinkTrtCmiCamp()) {
 			// si au moins un link est en service pour le codeRI, le trtCmi est concidÃ©rÃ© en service
 			if (link.getTemoinEnService() && link.getCampagne().getCodeRI() == codeRI) {
 				isOff = false;
 			}
 		}
 		return isOff;
 	}
 	
 	/**
 	 * A traitementCmi is in the campagne.
 	 * if the corresponding link is in service
 	 * @param trtCmi
 	 * @param camp
 	 * @return boolean
 	 */
 	public static boolean isTraitementCmiInCamp(final TraitementCmi trtCmi, final Campagne camp) {
 		// un traitement est dans la campagne si le link correspondant est en service
 		boolean isInCamp = false;
 		for (LinkTrtCmiCamp link : trtCmi.getLinkTrtCmiCamp()) {
 			// si au moins un link est en service, le trtCmi est concidÃ©rÃ© en service
 			if (link.getCampagne().equals(camp) && link.getTemoinEnService()) {
 				isInCamp = true;
 			}
 		}
 		return isInCamp;
 	}
 	
 	/**
 	 * @param trtCmi
 	 * @param camp
 	 * @return the linkTrtCmiCamp corresponding to trtCmi and camp
 	 */
 	public static LinkTrtCmiCamp getLinkTrtCmiCamp(final TraitementCmi trtCmi, final Campagne camp) {
 		// un traitement est dans la campagne si le link correspondant est en service
 		LinkTrtCmiCamp linkTrtCmiCamp = null;
 		for (LinkTrtCmiCamp link : trtCmi.getLinkTrtCmiCamp()) {
 			// si au moins un link est en service, le trtCmi est concidÃ©rÃ© en service
 			if (link.getCampagne().equals(camp)) {
 				linkTrtCmiCamp = link;
 			}
 		}
 		return linkTrtCmiCamp;
 	}
 	
 	/**
 	 * List of the commisions managed by the gestionnaire.
 	 */
 	public static Set<CentreGestion> getListCgeByRight(
 			final Gestionnaire gest,
 			final DomainApoService domainApoService) {
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("getListCgeByRight (" + gest + ")");
 		}
 		
 		Set<CentreGestion> lesCge = new HashSet<CentreGestion>();
 		if (StringUtils.hasText(gest.getCodeCge())) {
 			lesCge.add(domainApoService.getCentreGestion(gest.getCodeCge()));
 		} else if (gest.getRightOnCmi() == null || gest.getRightOnCmi().isEmpty()) {
 			lesCge = new HashSet<CentreGestion>(domainApoService.getCentreGestion());
 		}
 		
 		return lesCge;
 	}
 	
 	/**
 	 * @return Set< VersionEtpOpi >
 	 */
 	public static Set<VersionEtpOpi> getListEtpByRight(final Gestionnaire gest) {
 		Set<VersionEtpOpi> listEtpByRight = new HashSet<VersionEtpOpi>();
 		
 		for (Commission comm : gest.getRightOnCmi()) {
 			for (TraitementCmi trt : comm.getTraitementCmi()) {
 				if (!listEtpByRight.contains(trt.getVersionEtpOpi())) {
 					listEtpByRight.add(trt.getVersionEtpOpi());
 				}
 			}
 		}
 		
 		return listEtpByRight;
 	}
 	
 	public static boolean isEtpInCge(final String codEtp, final String codCge,
 			final DomainApoService domainApoService) {
 		List<VersionEtapeDTO> listEtp = domainApoService.getVersionEtapes(codEtp, null, codCge, null);
 		
 		return listEtp != null && !listEtp.isEmpty();
 	}
 	
 	public static Set<VersionEtapeDTO> getListEtpDtoByRight(final Gestionnaire gest,
 			final DomainApoService domainApoService) {
 		Set<VersionEtapeDTO> listEtpByRight = new HashSet<VersionEtapeDTO>();
 		
 		for (Commission comm : gest.getRightOnCmi()) {
 			for (TraitementCmi trt : comm.getTraitementCmi()) {
 				VersionEtapeDTO vetDto = new VersionEtapeDTO();
 					vetDto.setCodEtp(trt.getVersionEtpOpi().getCodEtp());
 					vetDto.setCodVrsVet(trt.getVersionEtpOpi().getCodVrsVet());
 				if (!listEtpByRight.contains(vetDto)) {
 					listEtpByRight.add(vetDto);
 				}
 			}
 		}
 		
 		return listEtpByRight;
 	}
 	
 	public static boolean isVetByRight(final Set<VersionEtpOpi> listEtpByRight, final VersionEtpOpi vet,
 			final Gestionnaire gest, final DomainApoService domainApoService) {
 		if (StringUtils.hasText(gest.getCodeCge())) {
 			return isEtpInCge(vet.getCodEtp(), gest.getCodeCge(), domainApoService);
 		} else if (gest.getRightOnCmi() != null && !gest.getRightOnCmi().isEmpty()) {
 			return listEtpByRight.contains(vet);
 		}
 		
 		return true;
 	}
 	
 	public static Boolean calIsOpen(final CalendarIns cIns) {
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("entering calIsOpen. cIns = " + cIns);
 		}
 
 		SimpleDateFormat formatter = new SimpleDateFormat(Constantes.ENGLISH_DATE_FORMAT);
 		ParsePosition pos = new ParsePosition(0);
 		Date today = formatter.parse(convertDateToString(new Date(), Constantes.ENGLISH_DATE_FORMAT), pos);
 
 		if ((today.after(cIns.getStartDate()) 
 				|| today.equals(cIns.getStartDate())) 
 				&&	
 				(today.before(cIns.getEndDate())
 						|| today.equals(cIns.getEndDate()))) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("leaving calIsOpen. return true");
 			}
 			return true;
 		}
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("leaving calIsOpen. return false");
 		}
 		return false;
 	}
 
 
 	/**
 	 * The civility for the individu's sex.
 	 */
 	public static String getCivilite(final I18nService iService, final String sexe) {
 		String civilite = "";
 		if (!StringUtils.hasText(sexe)) {
 			civilite = iService.getString(Constantes.I18N_CIV_MM) 
 			+ ", " + iService.getString(Constantes.I18N_CIV_MR);
 		} else if (sexe.equals(Constantes.COD_SEXE_FEMININ)) {
 			civilite = iService.getString(Constantes.I18N_CIV_MM);
 		} else if (sexe.equals(Constantes.COD_SEXE_MASCULIN)) {
 			civilite = iService.getString(Constantes.I18N_CIV_MR);
 		}
 
 		return civilite;
 	}
 
 	/**
 	 * convert the type date to string.
 	 */
 	public static String convertDateToString(final Date d, final String format) {
 		if (d != null) {
 			SimpleDateFormat s = new SimpleDateFormat(format);
 			return s.format(d);
 		}
 		return "";
 	}
 
 	/**
 	 * Return address of cmi in format text html for send mail.
 	 * @param justMailAndTel if null all adr, true just mail and tel, false adr postale
 	 * @return String
 	 */
 	public static String getAdrCmiForSendMail(final I18nService iService, 
 			final CommissionPojo cmiPojo,
 			final Boolean justMailAndTel) {
 		String ville = "";
 		if (cmiPojo.getAdressePojo() != null 
 				&& cmiPojo.getAdressePojo().getCommune() != null) {
 			ville = cmiPojo.getAdressePojo().getAdresse().getCodBdi() 
 			+ " " 
 			+ cmiPojo.getAdressePojo().getCommune().getLibCommune();
 			if (cmiPojo.getAdressePojo().getIsCedex()) {
 				ville += "CEDEX " + cmiPojo.getAdressePojo().getAdresse().getCedex();
 			}
 		}
 		String corresp = "";
 		String label = "";
 		if (StringUtils.hasText(cmiPojo.getContactCommission().getCorresponding())) {
 			corresp = cmiPojo.getContactCommission().getCorresponding();
 		}
 		if (StringUtils.hasText(cmiPojo.getCommission().getLibelle())) {
 			label = cmiPojo.getCommission().getLibelle();
 		}
 
 		String htmlBody2 = iService.getString("MAIL.ADR_CMI.CORRESP", 
 				corresp, label);
 		if (cmiPojo.getAdressePojo() != null 
 				&& (justMailAndTel == null || !justMailAndTel)) {
 			// pour ticket 41115 decomposition pour eviter les null en adr2 et adr3
 			// pour um1, dans 4 tests pour Ã©viter les lignes blanches 
 			if (StringUtils.hasText(cmiPojo.getAdressePojo().getAdresse().getAdr1())) {
 				htmlBody2 += iService.getString("MAIL.ADR_CMI.ADR_POST", 
 						cmiPojo.getAdressePojo().getAdresse().getAdr1());
 			} 
 			if (StringUtils.hasText(cmiPojo.getAdressePojo().getAdresse().getAdr2())) {
 				htmlBody2 += iService.getString("MAIL.ADR_CMI.ADR_POST", 
 						cmiPojo.getAdressePojo().getAdresse().getAdr2());
 			}
 			if (StringUtils.hasText(cmiPojo.getAdressePojo().getAdresse().getAdr3())) {
 				htmlBody2 += iService.getString("MAIL.ADR_CMI.ADR_POST", 
 						cmiPojo.getAdressePojo().getAdresse().getAdr3());
 			}
 			if (StringUtils.hasText(ville)) {
 				htmlBody2 += iService.getString("MAIL.ADR_CMI.ADR_POST", 
 						ville);
 			} 
 			
 			
 		}
 
 		if (cmiPojo.getAdressePojo() != null 
 				&& (justMailAndTel == null || justMailAndTel)) {
 			String mail = "";
 			String phone = "";
 			String fax = "";
 			if (cmiPojo.getAdressePojo().getAdresse().getMail() != null) {
 				mail = cmiPojo.getAdressePojo().getAdresse().getMail();
 			}
 			if (cmiPojo.getAdressePojo().getAdresse().getPhoneNumber() != null) {
 				phone = cmiPojo.getAdressePojo().getAdresse().getPhoneNumber();
 			}
 			if (cmiPojo.getAdressePojo().getAdresse().getFaxNumber() != null) {
 				fax = cmiPojo.getAdressePojo().getAdresse().getFaxNumber();
 			}
 
 			htmlBody2 += iService.getString("MAIL.ADR_CMI.MAIL_TEL", 
 					mail, phone, fax, ville);
 		}
 		return htmlBody2;
 	}
 	
 	
 	public static String getCheckCodEtu(final String codEtu, final String regex, final String pattern) {
 		if (regex != null && !regex.isEmpty() && pattern != null && !pattern.isEmpty()) {
 			if (isStringValid(codEtu, regex)) {
 				String codEtuValid = codEtu.replaceAll(regex, pattern);
 				return codEtuValid;
 			}
 		}
 		return codEtu;
 	}
 	
 	public static Class< ? > getClass(final Object o) {
 		if (o.getClass().getName().matches(".*(_\\$\\$_javassist_).*")) {
 			return o.getClass().getSuperclass();
 		}
 		
 		return o.getClass();
 	}
 	
 	@RequestCache
 	public static CalendarRDV getRecupCalendarRdv(final IndVoeu voeu,
 			final List<CalendarRDV> listCalendrierParam) {
 		CalendarRDV calendar = null;
 		
 		Set<CalendarRDV> listCal = getRecupCalDuVeou(voeu, listCalendrierParam);
 		for (CalendarRDV calRdv : listCal) {
 			if (calendar == null 
 					&& calRdv.getCodeCge() != null) {
 				calendar = calRdv;
 			} else if (calRdv.getCommissions() != null 
 						&& !calRdv.getCommissions().isEmpty()) {
 				calendar = calRdv;
 			} else if (calRdv.getVets() != null 
 					&& !calRdv.getVets().isEmpty()) {
 				calendar = calRdv;
 				break;
 			}
 		}
 		
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("voeu : ###" + voeu 
 				+ "### --> calendrier de rendez-vous : ###" + calendar + "###");
 		}
 		
 		return calendar;
 	}
 
 	@RequestCache
 	public static Set<CalendarRDV> getRecupCalDuVeou(final IndVoeu voeu,
 			final List<CalendarRDV> listCalendrierParam) {
 		Set<CalendarRDV> listCalendarRdv = new HashSet<CalendarRDV>();
 		VersionEtpOpi etape = voeu.getLinkTrtCmiCamp().getTraitementCmi().getVersionEtpOpi();
 		
 		for (CalendarRDV calendarRDV : listCalendrierParam) {
 			if (calendarRDV.getCodeCge() != null
 					&& calendarRDV.getCodeCge().equals(etape.getCodCge())) {
 				listCalendarRdv.add(calendarRDV);
 			
 			} else if (calendarRDV.getCommissions() != null 
 						&& !calendarRDV.getCommissions().isEmpty()) {
 				boolean noExistInList = true;
 				
 				for (Commission commission : calendarRDV.getCommissions()) {
 					for (TraitementCmi traitementCmi : commission.getTraitementCmi()) {
 						if (noExistInList && traitementCmi.getVersionEtpOpi().getCodEtp().
 								equals(etape.getCodEtp())) {
 							//on ajoute le calendrier a la liste
 							listCalendarRdv.add(calendarRDV);
 							//on sort de la boucle for
 							noExistInList = false;
 							break;
 						}
 					}
 					if (!noExistInList) {
 						//on sort de la boucle for si la calendrier a ete ajoute
 						break;
 					}
 				}
 			
 			} else if (calendarRDV.getVets() != null 
 					&& !calendarRDV.getVets().isEmpty()) {
 				for (VetCalendar vet : calendarRDV.getVets()) {
 					if (vet.getCodEtp().equals(etape.getCodEtp())
 							&& vet.getCodVrsVet().equals(etape.getCodVrsVet())) {
 						//on ajoute le calendrier a la liste
 						listCalendarRdv.add(calendarRDV);
 						//on sort de la boucle for
 						break;
 					}
 				}
 			}
 		}
 		
 		return listCalendarRdv;
 	}
 	/**
 	 * Renvoie la liste des commissions contenant une Ã©tape avec un formulaire complÃ©mentaire.
 	 */
 	public static List<Commission> getListCommissionExitForm(final List<Commission> listComm,
 			final List<RegimeInscription> listeRI,
 			final ParameterService parameterService) {
 		List<Commission> listCommExitForm = new ArrayList<Commission>();
 		
 		for (Commission commission : listComm) {
 			boolean exitForm = false;
 			for (TraitementCmi trtCmi : commission.getTraitementCmi()) {
 				for (LinkTrtCmiCamp link : trtCmi.getLinkTrtCmiCamp()) {
 					if (link.getTemoinEnService()) {
 						for (RegimeInscription ri : listeRI) {
 							if (link.getCampagne().getCodeRI() == ri.getCode()
 								&& parameterService.isExitFormulaireEtp(
 										trtCmi.getVersionEtpOpi(), String.valueOf(ri.getCode()))) {
 								listCommExitForm.add(commission);
 								exitForm = true;
 								break;
 							}
 						}
 						if (exitForm) {
 							break;
 						}
 					}
 				}
 				if (exitForm) {
 					break;
 				}
 			}
 		}
 		
 		return listCommExitForm;
 	}
 	
 	/**
 	 * Renvoie une map :
 	 * - Voeux contenant un formulaire complï¿½mentaire en fonction de l'individu.
 	 * - Test de la crï¿½ation du formulaire complï¿½mentaire.
 	 */
 	public static Map<IndVoeu, Boolean> getMapValidForm(final Individu ind,
 			final Map<Integer, RegimeInscription> mapRegimeInscription,
 			final ParameterService parameterService) {
 		Map<IndVoeu, Boolean> mapValidForm = new HashMap<IndVoeu, Boolean>();
 		
 		for (IndVoeu voeu : ind.getVoeux()) {
 			VersionEtpOpi vet = voeu.getLinkTrtCmiCamp().getTraitementCmi().getVersionEtpOpi();
 			RegimeInscription ri =  mapRegimeInscription.get(
 					voeu.getLinkTrtCmiCamp().getCampagne().getCodeRI());
 			if (parameterService.isExitFormulaireEtp(vet, String.valueOf(ri.getCode()))) {
 				if (parameterService.isExitFormulaireInd(ind, vet)) {
 					mapValidForm.put(voeu, true);
 				} else {
 					mapValidForm.put(voeu, false);
 				}
 			}
 		}
 		
 		return mapValidForm;
 	}
 }
