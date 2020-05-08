 package de.shop.bestellverwaltung.service;
 
 import static java.util.logging.Level.FINER;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 import javax.validation.groups.Default;
 
 import de.shop.util.NotFoundException;
 import de.shop.util.ValidationService;
 import de.shop.artikelverwaltung.domain.Produkt;
 import de.shop.artikelverwaltung.service.ProduktService;
 import de.shop.bestellverwaltung.domain.Bestellung;
 import de.shop.bestellverwaltung.domain.Bestellposition;
 import de.shop.bestellverwaltung.domain.Lieferung;
 import de.shop.kundenverwaltung.domain.Kunde;
 import de.shop.util.Log;
 
 public class BestellService implements Serializable {
 
 	public enum FetchType {
 		NUR_BESTELLUNG, MIT_KUNDE
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
 
 	@PersistenceContext
 	private transient EntityManager em;
 
 	@Inject
 	private ValidationService validationService;
 
 	@Inject
 	private ProduktService ps;
 
 	@Inject
 	private transient Event<Bestellung> event;
 
 	@PostConstruct
 	private void postConstruct() {
 		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
 	}
 
 	@PreDestroy
 	private void preDestroy() {
 		LOGGER.log(FINER, "CDI-faehiges Bean {0} wird geloescht", this);
 	}
 
 	/*
 	 * Sucht alle Bestellungen und gibt diese in Form einer Liste zurck
 	 */
 	@Log
 	public List<Bestellung> findBestellungen(Locale locale) {
 		List<Bestellung> result = null;
 		result = em.createNamedQuery(Bestellung.FIND_BESTELLUNGEN, Bestellung.class).getResultList();
 
 		return result;
 	}
 
 	/*
 	 * Liefert eine Bestellung mit der entsprechenden ID zurck
 	 */
 	public Bestellung findBestellungById(Long id, Locale locale) {
 		List<Bestellung> result = em.createNamedQuery(Bestellung.FIND_BESTELLUNG_BY_ID, Bestellung.class)
 				.setParameter(Bestellung.PARAM_BESTELLUNG_ID, id).getResultList();
 
 		if (result.size() > 0)
 			return result.get(0);
 		else
 			return null;
 	}
 	
 	/*
 	 * Sucht eine bestimmte Bestellposition
 	 */
 	@Log
 	public Bestellposition findBestellpositionById(Long id, Locale locale) {
 		return em.createNamedQuery(Bestellposition.FIND_BESTELLPOSITION_BY_ID, Bestellposition.class)
 				.setParameter(Bestellposition.PARAM_ID, id).getSingleResult();
 	}
 
 	/*
 	 * Sucht eine bestimmte lieferung
 	 */
 	@Log
 	public Lieferung findLieferungById(Long id, Locale locale) {
 		return em.createNamedQuery(Lieferung.FIND_LIEFERUNG_BY_ID, Lieferung.class)
 				.setParameter(Lieferung.PARAM_LIEFERUNG_ID, id).getSingleResult();
 	}
 
 	/*
 	 * gibt alle Lieferungen zurck
 	 */
 	@Log
 	public List<Lieferung> findLieferungen(Locale locale) {
 		List<Lieferung> result = em.createNamedQuery(Lieferung.FIND_LIEFERUNGEN, Lieferung.class).getResultList();
 
 		if (result.size() > 0)
 			return result;
 		else
 			return null;
 	}
 
 	/*
 	 * Sucht alle Bestellungen eines Kunden und gibt diese in einer Liste
 	 * zurck. Die Anzahl der Listeneintrge entspricht hierbei der IDX fr die
 	 * nchste Bestellung
 	 */
 	@Log
 	public List<Bestellung> findBestellungenByKunde(Kunde kunde, FetchType fetch) {
 		List<Bestellung> result = null;
 		result = em.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_KUNDE_ID, Bestellung.class)
 				.setParameter(Bestellung.PARAM_BESTELLUNG_KUNDE_ID, kunde.getId()).getResultList();
 
 		return result;
 	}
 
 	/*
 	 * sucht eine Bestellung nach einem Datum
 	 */
 	@Log
 	public List<Bestellung> findBestellungByDate(Date date, FetchType fetch) {
 		List<Bestellung> result = null;
 		switch (fetch) {
 		case NUR_BESTELLUNG:
 			result = em.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_DATE, Bestellung.class)
 					.setParameter(Bestellung.PARAM_BESTELLUNG_DATE, date).getResultList();
 			break;
 		case MIT_KUNDE:
 			break;
 		default:
 			break;	
 		}
 
 		return result;
 	}
 
 	public Bestellposition findBestellpositionOfBestellungByProduktId(
 			Bestellung bestellung, Produkt produkt, Locale locale) {
 		Bestellposition result = null;
 
 		result = em.createNamedQuery(Bestellposition.FIND_BESTELLPOSITIONEN_BY_BESTELLUNG_BY_PRODUKT,
 						Bestellposition.class)
 				.setParameter(Bestellposition.PARAM_BESTELLUNG_ID, bestellung.getId())
 				.setParameter(Bestellposition.PARAM_PRODUKT_ID, produkt.getId()).getSingleResult();
 
 		return result;
 	}
 
 	/*
 	 * Sucht alle Bestellpositionen die zu einer Bestellung gehren und gibt sie
 	 * in einer Liste zurck. Die Anzahl der Listeneintrge entspricht der
 	 * hchsten idx dieser Bestellpositionen + 1 weshalb die Anzahl der
 	 * Listeneintrge als neue IDX verwendet wird
 	 */
 	@Log
 	public List<Bestellposition> findBestellpositionenByBestellung(Bestellung bestellung) {
 		List<Bestellposition> result = null;
 
 		result = em.createNamedQuery(Bestellposition.FIND_BESTELLPOSITIONEN_BY_BESTELLUNG_ID,
 						Bestellposition.class)
 				.setParameter(Bestellposition.PARAM_BESTELLUNG_ID, bestellung.getId()).getResultList();
 
 		return result;
 	}
 
 	@Log
 	public Lieferung createLieferung(Lieferung lieferung, Locale locale) {
 		em.persist(lieferung);
 
 		return lieferung;
 	}
 	
 	@Log
 	public Lieferung updateLieferung(Lieferung lieferung, Locale locale) {
 		if (lieferung == null) {
 			LOGGER.finest("Lieferung ist null => Update nicht mglich");
 			return null;
 		}
 			
 		if (lieferung.getId() == null) {
 			LOGGER.finest("ID der Lieferung ist null => Update nicht mglich, noch nicht in der Datenbank vorhanden");
 			return null;
 		}
 		
 		em.merge(lieferung);
 		LOGGER.finest("update der Lieferung erfolgreich");
 		
 		return lieferung;
 	}
 	
 	//Erstellt eine Bestellposition
 	//Eine Bestellposition bentigt immer ein Produkt, eine Lieferung und eine dazugehrige Bestellung
 	@Log
 	public Bestellposition createBestellposition(Bestellposition bestellposition, Locale locale) {
 		Produkt produkt = bestellposition.getProdukt();
 		bestellposition.setEinzelpreis(produkt.getPreis());
 		em.persist(bestellposition);
 
 		return bestellposition;
 	}
 	
 	//Updated eine Bestellposition
 	@Log
 	public Bestellposition updateBestellposition(Bestellposition bestellposition, Locale locale)
 	{
 		if (bestellposition == null) {
 			LOGGER.finest("Bestellposition ist null => Update nicht mglich");
 			return null;
 		}
 			
 		if (bestellposition.getId() == null) {
 			LOGGER.finest("ID der Bestellposition null => Update nicht mglich, noch nicht in der Datenbank vorhanden");
 			return null;
 		}
 		
 		//TODO Preis wird noch nicht gesetzt, bentigt wird NamedQuery zum finden des Produkts einer Bestellposition
 		
 		em.merge(bestellposition);
 		LOGGER.finest("update der Bestellposition erfolgreich");
 		
 		return bestellposition;
 	}
 	
 	//Legt eine Bestellung mit einer oder gegebenenfalls mehreren Bestellpositionen an
 	@Log
 	public Bestellung createBestellung(Bestellung bestellung, Kunde kunde, Locale locale) {
 		if (bestellung == null) {
 			LOGGER.finest("Konnte Bestellung nicht erstellen => Bestellung ist null");
 			throw new IllegalArgumentException("Konnte Bestellung nicht erstellen => Bestellung ist null");
 		}
 		if (kunde == null) {
 			LOGGER.finest("Konnte Bestellung nicht erstellen => Kunde ist null");
 			throw new IllegalArgumentException("Konnte Bestellung nicht erstellen => Kunde ist null");
 		}
 		
 		bestellung.setKunde(kunde);
 		kunde.addBestellung(bestellung);
 		
 		List<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
 		
 		if (bestellpositionen == null || bestellpositionen.isEmpty()) {
 			LOGGER.finest("Bestellung nicht erstellt => bestellpositionen sind null oder leer");
 			throw new IllegalArgumentException("Bestellung nicht erstellt => bestellpositionen sind null oder leer");
 		}
 		
 		float p = (float) 0;
 		Produkt produkt = null;
 		for (Bestellposition bp : bestellpositionen) {
 			produkt = bp.getProdukt();
 			
 			if (produkt == null) {
 				throw new NotFoundException("Produkt nicht auffindbar");
 			}
 			
 			bp.setEinzelpreis(produkt.getPreis());
 			p += bp.getEinzelpreis();
 			bp.setBestellung(bestellung);
 		}
 		bestellung.setGesamtpreis(p);
 		//bestellung = em.find(Bestellung.class, bestellung.getId());
 		em.persist(bestellung);
 		LOGGER.finest("Bestellung wurde erfolgreich erstellt mit ID " + bestellung.getId());
 		
 		return bestellung;
 	}
 	
 	
 	
 	//Erstellt eine Bestellung mit genau einer Bestellposition
 	@Log
 	public Bestellung createBestellung(
 			Kunde kunde, Produkt produkt, int menge, Lieferung lieferung, Locale locale) throws Exception {
 		// Bestellung anlegen
 		Bestellung bestellung = new Bestellung();
 		bestellung.setKunde(kunde);
 		kunde.addBestellung(bestellung);
 
 		// Bestellposition anlegen
 		Bestellposition bestellposition = new Bestellposition(bestellung, lieferung, produkt, menge);
 
 		// bestellung anpassen
 		bestellung.setGesamtpreis(bestellposition.getEinzelpreis());
 
 		validateBestellung(bestellung, locale, Default.class);
 
 		em.persist(bestellung);
 
 		return bestellung;
 	}
 
 	public Bestellung updateBestellung(Bestellung bestellung, Locale locale) {
 		if (bestellung == null) {
 			LOGGER.finest("Bestellung ist null => Update nicht mglich");
 			return null;
 		}
 			
 		if (bestellung.getId() == null) {
 			LOGGER.finest("ID der Bestellung ist null => Update nicht mglich, noch nicht in der Datenbank vorhanden");
 			return null;
 		}
 		
 		List<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
 		
 		if (bestellpositionen == null || bestellpositionen.isEmpty()) {
 			LOGGER.finest("Bestellung nicht erstellt => bestellpositionen sind null oder leer");
 			throw new IllegalArgumentException("Bestellung nicht erstellt => bestellpositionen sind null oder leer");
 		}
 		
 		float p = (float) 0;
 
 		for (Bestellposition bp : bestellpositionen) {
 			p += bp.getEinzelpreis();
 			bp.setBestellung(bestellung);
 		}
 		bestellung.setGesamtpreis(p);
 		
 		em.merge(bestellung);
 		
 		LOGGER.finest("update der Bestellung erfolgreich");
 		
 		return bestellung;
 	}
 
 	// Validation Methods....
 	public void validateBestellung(Bestellung bestellung, Locale locale, Class<?>... groups) {
 		final Validator validator = validationService.getValidator(locale);
 
 		final Set<ConstraintViolation<Bestellung>> violations = validator.validate(bestellung, groups);
 		if (!violations.isEmpty()) {
 			throw new BestellungValidationException(bestellung, violations);
 		}
 	}
 	
 	public void validateLieferung(Lieferung lieferung, Locale locale, Class<?>... groups) {
 		final Validator validator = validationService.getValidator(locale);
 
 		final Set<ConstraintViolation<Lieferung>> violations = validator.validate(lieferung, groups);
 		if (!violations.isEmpty()) {
 			throw new LieferungValidationException(lieferung, violations);
 		}
 	}
 	
 	public void validateBestellposition(Bestellposition bestellposition, Locale locale, Class<?>... groups) {
 		final Validator validator = validationService.getValidator(locale);
 
 		final Set<ConstraintViolation<Bestellposition>> violations = validator.validate(bestellposition, groups);
 		if (!violations.isEmpty()) {
 			throw new BestellpositionValidationException(bestellposition, violations);
 		}
 	}
 }
