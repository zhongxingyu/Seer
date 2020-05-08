 package de.shop.kundenverwaltung.service;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.jboss.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 import javax.validation.groups.Default;
 
 import de.shop.kundenverwaltung.domain.Zahlungsinformation;
 import de.shop.util.IdGroup;
 import de.shop.util.Log;
 import de.shop.util.ValidatorProvider;
 
 @Log
 public class ZahlungsinformationService implements Serializable {
 	
 
 	private static final long serialVersionUID = 2025119056807350622L;
 		
 	@PersistenceContext
 	private transient EntityManager em;
 	
 	@Inject
 	private transient Logger logger;
 	
 	@Inject
 	private ValidatorProvider validationProvider;
 	
 	@PostConstruct
 	private void postConstruct() {
 		logger.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
 	}
 	
 	@PreDestroy
 	private void preDestroy() {
 		logger.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
 	}
 	
 	// Alle Zahlungsinformationen finden
 	
	public List <Zahlungsinformation> findAllZahlungsinformation() {
 		
 		List<Zahlungsinformation> zahlungsinformationen;
 		
 		zahlungsinformationen = em.createNamedQuery(Zahlungsinformation.FIND_ALL_ZAHLUNGSINFORMATION,
 													Zahlungsinformation.class) 
 								  .getResultList();
 		
 		return zahlungsinformationen;
 	}
 	
 	public Zahlungsinformation findZahlungsinformationById(Long id, Locale locale) {
 		
 		validateZahlungsinformationId(id, locale);
 		Zahlungsinformation zahlungsinformation = null;
 		
 		zahlungsinformation = em.find(Zahlungsinformation.class, id);
 		
 		return zahlungsinformation;
 	}
 	
 	
 	
 	public Zahlungsinformation createZahlungsinformation(Zahlungsinformation zahlungsinformation, Locale locale) {
 		if	(zahlungsinformation == null) {
 			 return zahlungsinformation;
 		}
 		
 		validateZahlungsinformation(zahlungsinformation, locale, Default.class);
 		
 		try {
 			em.createNamedQuery(Zahlungsinformation.FIND_ZAHLUNGSINFORMATION_BY_KONTONR, Zahlungsinformation.class)
 			.setParameter(Zahlungsinformation.PARAM_ZAHLUNGSINFORMATION_KONTONR, zahlungsinformation.getKontonummer())
 			.getSingleResult();
 		}
 		
 		catch (NoResultException e) {
 			// Noch keine Zahlungsinformation mit dieser Kontonummer
 			logger.debugf("Kontonummer existiert noch nicht");
 		}
 		
 		zahlungsinformation.setZahlId(null);
 		em.persist(zahlungsinformation);
 		return zahlungsinformation;
 	}
 	
 	public Zahlungsinformation updateZahlungsinformation(Zahlungsinformation zahlungsinformation, Locale locale) {
 		
 		if (zahlungsinformation == null) {
 		   return null;
 		}
 		
 		validateZahlungsinformation(zahlungsinformation, locale, Default.class);
 		
 			try {
 				final Zahlungsinformation vorhandeneZahlungsinformation = em.find(Zahlungsinformation.class,
 																				  zahlungsinformation.getZahlId());
 				
 				if (vorhandeneZahlungsinformation.getZahlId().longValue() 
 					 != zahlungsinformation.getZahlId().longValue()) {
 				throw new ZahlIdExistsException(zahlungsinformation.getZahlId());
 					
 				}
 			
 				
 			}
 		
 			catch (NoResultException e) {
 				logger.debugf("ZahlungsinformationID");
 				
 			}
 		
 			em.detach(zahlungsinformation);
 			em.merge(zahlungsinformation);
 			return zahlungsinformation;
 	}
 	
 	
 	
 	private void validateZahlungsinformation(Zahlungsinformation zahlungsinformation,
 											 Locale locale, Class<?>... groups) {
 		// Werden alle Constraints beim Einfuegen gewahrt?
 		final Validator validator = validationProvider.getValidator(locale);
 		
 		final Set<ConstraintViolation<Zahlungsinformation>> violations
 			   = validator.validate(zahlungsinformation, groups);
 		
 		if (!violations.isEmpty()) {
 			throw new ZahlungsinformationValidationException(zahlungsinformation, violations);
 		}		
 	}
 	
 	
 	private void validateZahlungsinformationId(Long zahlId, Locale locale) {
 		final Validator validator = validationProvider.getValidator(locale);
 		final Set<ConstraintViolation<Zahlungsinformation>> violations
 		= validator.validateValue(Zahlungsinformation.class, "zahlId", zahlId, IdGroup.class);
 		if (!violations.isEmpty())
 			throw new InvalidZahlungsinformationIdException(zahlId, violations);
 	}
 	
 
 	 
 	
 }
