 package de.shop.kundenverwaltung.service;
 
 
 import static de.shop.util.Constants.KEINE_ID;
 import static java.util.logging.Level.FINER;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.logging.Logger;
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 import javax.validation.groups.Default;
 import de.shop.auth.service.jboss.AuthService;
 import de.shop.kundenverwaltung.domain.Adresse;
 import de.shop.kundenverwaltung.domain.Kunde;
 import de.shop.kundenverwaltung.domain.Zahlungsinformation;
 import de.shop.util.IdGroup;
 import de.shop.util.Log;
 import de.shop.util.ValidatorProvider;
 
 @Log
 public class KundeService implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4360325837484294309L;
 
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
 	
 	public enum FetchType {
 		NUR_KUNDE,
 		MIT_ZAHLUNGSINFORMATION,
 		MIT_ADRESSE,
 		MIT_ADRESSE_UND_ZAHLUNGSINFORMATION
 	}
 	
 	public enum OrderType {
 		KEINE,
 		ID
 	}
 	
 	@PersistenceContext
 	private transient EntityManager em;
 	
 	@Inject
 	@NeuerKunde
 	private transient Event<Kunde> event;
 	
 	@Inject
 	private ValidatorProvider validationProvider;
 	
 	@Inject
 	private AuthService authService;
 	
 	
 	@PostConstruct
 	private void postConstruct() {
 		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
 	}
 	
 	@PreDestroy
 	private void preDestroy() {
 		LOGGER.log(FINER, "CDI-faehiges Bean {0} wird geloescht", this);
 	}
 	
 	
 	public Kunde findKundeById(Long id, FetchType fetch, Locale locale) {
 		validateKundeId(id, locale);
 		
 		Kunde kunde = null;
 		try {
 			switch (fetch) {
 				case NUR_KUNDE:
 					kunde = em.find(Kunde.class, id);
 					break;
 				
 				case MIT_ZAHLUNGSINFORMATION:
 					kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_ID_FETCH_ZAHLUNGSINFORMATION , Kunde.class)
 							  .setParameter(Kunde.PARAM_ID, id)
 							  .getSingleResult();
 					break;
 					
 				case MIT_ADRESSE:
 					kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_ID_FETCH_ADRESSE ,
 							Kunde.class)
 							  .setParameter(Kunde.PARAM_ID , id)
 							  .getSingleResult();
 					break;
 				case MIT_ADRESSE_UND_ZAHLUNGSINFORMATION:
 						kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_ID_FETCH_ADRESSE_UND_ZAHLUNGSINFORMATION ,
 													Kunde.class)
 								.setParameter(Kunde.PARAM_ID ,  id)
 								.getSingleResult();
 					 
 				default:
 					kunde = em.find(Kunde.class ,  id);
 					break;
 			}
 		}
 		catch (NoResultException e) {
 			return null;
 		}
 
 		return kunde;
 	}
 	
 	
 	public List<Kunde> findKundenByNachname(String nachname ,  FetchType fetch ,  Locale locale) {
 		validateKundeNachname(nachname ,  locale);
 		
 		List<Kunde> kunden = null;
 		try {
 			switch (fetch) {
 				case NUR_KUNDE:
 					kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME ,  Kunde.class)
 							  .setParameter(Kunde.PARAM_NACHNAME ,  nachname)
 							  .getResultList();
 					break;
 				
 				case MIT_ZAHLUNGSINFORMATION:
 					kunden = em.createNamedQuery(
 								Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_ZAHLUNGSINFORMATION ,  Kunde.class)
 							  .setParameter(Kunde.PARAM_NACHNAME ,  nachname)
 							  .getResultList();
 					break;	
 					
 				case MIT_ADRESSE:
 					kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_ADRESSE , 
 							Kunde.class)
 							  .setParameter(Kunde.PARAM_NACHNAME ,  nachname)
 							  .getResultList();
 					break;
 				case MIT_ADRESSE_UND_ZAHLUNGSINFORMATION:
 						kunden = em.createNamedQuery(
 									Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_ADRESSE_UND_ZAHLUNGSINFORMATION, Kunde.class)
 								.setParameter(Kunde.PARAM_NACHNAME ,  nachname)
 								.getResultList();
 					 
 				default:
 					kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME ,  Kunde.class)
 					  .setParameter(Kunde.PARAM_NACHNAME ,  nachname)
 					  .getResultList();
 					break;
 			}
 		}
 		catch (NoResultException e) {
 			return null;
 		}
 
 		return kunden;
 	}
 	
 	public Kunde findKundeByEmail(String email ,  FetchType fetch ,  Locale locale) {
 		validateKundeEmail(email ,  locale);
 		
 		Kunde kunde = null;
 		try {
 			switch (fetch) {
 				case NUR_KUNDE:
 					kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_EMAIL ,  Kunde.class)
 							  .setParameter(Kunde.PARAM_EMAIL ,  email)
 							  .getSingleResult();
 					break;
 				
 				case MIT_ZAHLUNGSINFORMATION:
 					kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_EMAIL_FETCH_ZAHLUNGSINFORMATION ,  Kunde.class)
 							  .setParameter(Kunde.PARAM_EMAIL ,  email)
 							  .getSingleResult();
 					break;	
 					
 				case MIT_ADRESSE:
 					kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_EMAIL_FETCH_ADRESSE,
 							Kunde.class)
 							  .setParameter(Kunde.PARAM_EMAIL ,  email)
 							  .getSingleResult();
 					break;
 				case MIT_ADRESSE_UND_ZAHLUNGSINFORMATION:
 						kunde = em.createNamedQuery(
 									Kunde.FIND_KUNDE_BY_EMAIL_FETCH_ADRESSE_UND_ZAHLUNGSINFORMATION , Kunde.class)
 								.setParameter(Kunde.PARAM_EMAIL ,  email)
 								.getSingleResult();
 					 
 				default:
 					kunde = em.find(Kunde.class ,  email);
 					break;
 			}
 		}
 		catch (NoResultException e) {
 			return null;
 		}
 
 		return kunde;
 	}
 	
 	
 	
 	
 	
 	public List<Kunde> findAllKunden(FetchType fetch ,  OrderType order) {
 		List<Kunde> kunden = null;
 		switch (fetch) {
 			case NUR_KUNDE:
 				kunden = OrderType.ID.equals(order)
 				         ? em.createNamedQuery(Kunde.FIND_KUNDEN_ORDER_BY_ID ,  Kunde.class)
 				             .getResultList()
 				         : em.createNamedQuery(Kunde.FIND_KUNDEN ,  Kunde.class)
 				             .getResultList();
 				break;
 			
 			case MIT_ZAHLUNGSINFORMATION:
 				break;
 				
 			case MIT_ADRESSE:
 				break;
 			case MIT_ADRESSE_UND_ZAHLUNGSINFORMATION:
 				break;
 				
 		
 			default:
 				kunden = OrderType.ID.equals(order)
 		                 ? em.createNamedQuery(Kunde.FIND_KUNDEN_ORDER_BY_ID, Kunde.class)
 		                	 .getResultList()
 		                 : em.createNamedQuery(Kunde.FIND_KUNDEN, Kunde.class)
 		                     .getResultList();
 				break;
 		}
 
 		return kunden;
 	}
 	
 	public Kunde createKunde(Kunde kunde , Zahlungsinformation zahlungsinformation ,
 							  Adresse lieferadresse, Adresse rechnungsadresse, Locale locale) {
 		if (kunde == null)
 			return kunde;
 		
 		try {
 			em.createNamedQuery(Kunde.FIND_KUNDE_BY_EMAIL, Kunde.class)
 			  .setParameter(Kunde.PARAM_EMAIL, kunde.getEmail())
 			  .getSingleResult();
 			throw new EmailExistsException(kunde.getEmail());
 		}
 		
 		catch (NoResultException e) {
 			LOGGER.finest("Email-Adresse existiert noch nicht");
 			
 		}
 		
 		kunde.setKundeId(KEINE_ID);
 		kunde.setZahlungsinformation(zahlungsinformation);
 		zahlungsinformation.setZahlId(KEINE_ID);
 		kunde.setLieferadresse(lieferadresse);
 		lieferadresse.setAdresseId(KEINE_ID);
 		kunde.setRechnungsadresse(rechnungsadresse);
 		rechnungsadresse.setAdresseId(KEINE_ID);
 		
 		passwordVerschluesseln(kunde);
 		
 		em.persist(kunde);
 		event.fire(kunde);
 		
 		return kunde;
 	}
 	
 	public Kunde updateKunde(Kunde kunde, Locale locale) {
 		if (kunde == null) {
 			return null;
 		}
 
 		
 		validateKunde(kunde, locale, IdGroup.class);
 		
 		// kunde vom EntityManager trennen, weil anschliessend z.B. nach Id und Email gesucht wird
 				em.detach(kunde);
 		
 		try {
 			final Kunde vorhandenerKunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_EMAIL,
 					Kunde.class)
 					                                 .setParameter(Kunde.PARAM_EMAIL, kunde.getEmail())
 					                                 .getSingleResult();
 			
 			em.detach(vorhandenerKunde);
 			if (vorhandenerKunde.getKundeId().longValue() != kunde.getKundeId().longValue()) {
 				throw new EmailExistsException(kunde.getEmail());
 			}
 		}
 		catch (NoResultException e) {
 			LOGGER.finest("Neue Email-Adresse");
 		}
 
 		em.merge(kunde);
 		kunde.setPasswortWdh(kunde.getPasswort());
 		return kunde;
 	}
 	
 	private void passwordVerschluesseln(Kunde kunde) {
 		LOGGER.finest("passwordVerschluesseln BEGINN:");
 
 		final String unverschluesselt = kunde.getPasswort();
 		final String verschluesselt = authService.verschluesseln(unverschluesselt);
 		kunde.setPasswort(verschluesselt);
 
 		LOGGER.finest("passwordVerschluesseln ENDE:");
 	}
 	
 	
 	private void validateKunde(Kunde kunde, Locale locale, Class<?>... groups) {
 		
 		final Validator validator = validationProvider.getValidator(locale);		
 		final Set<ConstraintViolation<Kunde>> violations = validator.validate(kunde, groups);
 		if (!violations.isEmpty()) {
 			throw new InvalidKundeException(kunde, violations);
 		}
 	}
 
 	private void validateKundeId(Long kundeId, Locale locale) {
 		final Validator validator = validationProvider.getValidator(locale);
 		final Set<ConstraintViolation<Kunde>> violations = validator.validateValue(Kunde.class,
 				                                                                           "kundeid",
 				                                                                           kundeId,
 				                                                                           Default.class);
 		if (!violations.isEmpty())
 			throw new InvalidKundeIdException(kundeId, violations);
 	}
 	
 	private void validateKundeNachname(String nachname, Locale locale) {
 		final Validator validator = validationProvider.getValidator(locale);
 		final Set<ConstraintViolation<Kunde>> violations = validator.validateValue(Kunde.class,
 				                                                                           "nachname",
 				                                                                           nachname,
 				                                                                           IdGroup.class);
 		if (!violations.isEmpty())
 			throw new InvalidNachnameException(nachname, violations);
 	}
 	
 	
 	
 	private void validateKundeEmail(String email, Locale locale) {
 		final Validator validator = validationProvider.getValidator(locale);
 		final Set<ConstraintViolation<Kunde>> violations = validator.validateValue(Kunde.class,
 				                                                                           "email",
 				                                                                           email,
 				                                                                           Default.class);
 		if (!violations.isEmpty())
 			throw new InvalidEmailException(email, violations);
 	}
 
 	public List<String> findNachnamenByPrefix(String nachnamePrefix) {
 		final List<String> nachnamen = em.createNamedQuery(Kunde.FIND_NACHNAMEN_BY_PREFIX,
                 String.class)
                 						.setParameter(Kunde.PARAM_KUNDE_NACHNAME_PREFIX, nachnamePrefix + '%')
                 						.getResultList();
 		return nachnamen;
 	}
 
 	public List<Long> findIdsByPrefix(String idPrefix) {
 		final List<Long> ids = em.createNamedQuery(Kunde.FIND_IDS_BY_PREFIX, Long.class)
 								.setParameter(Kunde.PARAM_KUNDE_ID_PREFIX, idPrefix + '%')
 								.getResultList();
 		return ids;
 	}
 
 	public List<Kunde> findKundenByIdPrefix(Long id) {
 		if (id == null) {
 			return Collections.emptyList();
 		}
 		
 		final List<Kunde> kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_ID_PREFIX,
 				                                               Kunde.class)
 				                             .setParameter(Kunde.PARAM_KUNDE_ID_PREFIX, id.toString() + '%')
 				                             .getResultList();
 		return kunden;
 	
 	}
 
 	public Kunde findKundeByUserName(String userName) {
 		Kunde kunde;
 		try {
 			kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_USERNAME,Kunde.class)
 					  .setParameter(Kunde.PARAM_KUNDE_USERNAME, userName)
 					  .getSingleResult();
 		}
 		catch (NoResultException e) {
 			return null;
 		}
 		
 		return kunde;
 	}
 	
 }
