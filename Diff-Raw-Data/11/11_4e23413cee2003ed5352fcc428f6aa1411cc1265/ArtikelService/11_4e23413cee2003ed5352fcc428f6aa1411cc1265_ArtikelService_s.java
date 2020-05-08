 package de.shop.artikelverwaltung.service;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.util.Collection;
 import java.util.Locale;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 import javax.validation.groups.Default;
 
 import org.jboss.logging.Logger;
 
 import com.google.common.base.Strings;
 
 import de.shop.artikelverwaltung.domain.Artikel;
 import de.shop.util.Log;
 import de.shop.util.ValidatorProvider;
 
 @Log
 public class ArtikelService implements Serializable {
 	private static final long serialVersionUID = -5105686816948437276L;
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
 
 	@Inject
 	private ValidatorProvider validatorProvider;
 	
 	@PersistenceContext
 	private transient EntityManager em;
 	
 	@PostConstruct
 	private void postConstruct() {
 		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
 	}
 	
 	@PreDestroy
 	private void preDestroy() {
 		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
 	}
 	
 	public Artikel findArtikelById(Long id, Locale locale) {
		final Artikel artikel = em.find(Artikel.class, id);
 		return artikel;
 	}
 	
 
 		public Collection<Artikel> findAllArtikel() {
 			
 			final Collection<Artikel> artikel = em.createNamedQuery(Artikel.FIND_ALL_ARTIKEL, Artikel.class)
 												.getResultList();
 			return artikel;
 		}
 		
 		public Artikel findArtikelByName(String name, Locale locale) {
 			if (Strings.isNullOrEmpty(name)) {
 				return null;
 			}
 			final Artikel artikel = em.createNamedQuery(Artikel.FIND_ARTIKEL_BY_NAME, Artikel.class)
 													.setParameter(Artikel.PARAM_NAME, "%" + name + "%")
 													.getSingleResult();		
 			return artikel;
 		}
 		
 		public Artikel createArtikel(Artikel artikel, Locale locale) {
 			if (artikel == null) {
 				return null;
 			}
 			
 			// Werden alle Constraints beim Einfuegen gewahrt?B
 			validateArtikel(artikel, locale, Default.class);			
 
 			try {
 				em.createNamedQuery(Artikel.FIND_ARTIKEL_BY_NAME, Artikel.class)
 				.setParameter(Artikel.PARAM_NAME, artikel.getName())
 				.getSingleResult();
 				throw new ArtikelNameExistsException(artikel.getName());
 			}
 			catch (NoResultException e) {
 				//Noch kein Artikel mit diesem Namen
 				LOGGER.trace("Name existiert noch nicht.");
 			}
 
 			em.persist(artikel);
 			return artikel;
 		}
 		
 		private void validateArtikel(Artikel artikel, Locale locale, Class<?>... groups) {
 			// Werden alle Constraints beim Einfuegen gewahrt?
 			final Validator validator = validatorProvider.getValidator(locale);
 			
 			final Set<ConstraintViolation<Artikel>> violations = validator.validate(artikel, groups);
 			if (!violations.isEmpty()) {
 				throw new InvalidArtikelException(artikel, violations);
 			}
 		}
 
 		public Artikel updateArtikel(Artikel artikel, Locale locale) {
 			if (artikel == null) {
 				return null;
 			}
 			//Werden alle Constraints beim Einfuegen gewahrt?
 			validateArtikel(artikel, locale, Default.class);		
 			
 			em.detach(artikel);
 			
 			// Gibt es ein anderes Objekt mit gleichem Namen?
 			final Artikel tmp = findArtikelByName(artikel.getName(), locale);
 			if (tmp != null) {
 				em.detach(tmp);
 				if (tmp.getId().longValue() != artikel.getId().longValue()) {
 					// anderes Objekt mit gleichem Attributwert fuer name
 					throw new ArtikelNameExistsException(artikel.getName());
 				}
 			}
 			
 			em.merge(artikel);
 			return artikel;
 		}
 
 	}
