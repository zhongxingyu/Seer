 package de.shop.Kundenverwaltung.domain;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Set;
 
 import de.shop.Kundenverwaltung.domain.Kunde;
 
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceException;
 import javax.persistence.TemporalType;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.*;
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 
 import org.jboss.arquillian.junit.Arquillian;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import de.shop.Util.AbstractDomainTest;
 
 
 @RunWith(Arquillian.class)
 public class KundeTest extends AbstractDomainTest {
 	private static final String NACHNAME_VORHANDEN = "Alpha";
 	private static final String NACHNAME_NICHT_VORHANDEN = "Nicht";
 	private static final Long ID_VORHANDEN = Long.valueOf(101);
 	private static final String EMAIL_VORHANDEN = "k1@hska.de";
 	private static final String EMAIL_NICHT_VORHANDEN = "Nicht";
 	private static final int TAG = 31;
 	private static final int MONAT = Calendar.JANUARY;
 	private static final int JAHR = 2001;
 	private static final Date ERSTELLT_VORHANDEN = new GregorianCalendar(JAHR, MONAT, TAG).getTime();
 	
 	private static final String NACHNAME_NEU = "Test";
 	private static final String VORNAME_NEU = "Theo";
 	private static final String EMAIL_NEU = "theo@test.de";
 	private static final String PLZ_NEU = "11111";
 	private static final String ORT_NEU = "Testort";
 	private static final String STRASSE_NEU = "Testweg";
 	private static final String HAUSNR_NEU = "1";
 	private static final String LAND_NEU = "Deutschland";
 	
 	
 	@Test
 	public void validate() {
 		assertThat(true, is(true));
 	}
 	
 	@Test
 	public void findKundeByIdVorhanden() {
 		// Given
 		final Long id = ID_VORHANDEN;
 		
 		// When
 		//final AbstractKunde kunde = getEntityManager().find(AbstractKunde.class, id);
 		
 		Kunde kunde = getEntityManager().createNamedQuery(Kunde.KUNDE_BY_KNR, Kunde.class)
 							.setParameter(Kunde.PARAM_KUNDENNUMMER, id)
 							.getSingleResult();
 				
 				
 		// Then
 		assertThat(kunde.getKundenNr(), is(id));
 	}
 	
 	
 	@Test
 	public void findKundeByEmailVorhanden() {
 		// Given
 		final String email = EMAIL_VORHANDEN;
 		
 		// When
 		Kunde kunde = getEntityManager().createNamedQuery(Kunde.KUNDE_BY_EMAIL, Kunde.class)
 								.setParameter(Kunde.PARAM_EMAIL, email)
 								.getSingleResult();
 				
 				
                                                                        
 		// Then
 		assertThat(kunde.getEmail(), is(email));
 	}
 	
 	@Test
 	public void findKundeByEmailNichtVorhanden() {
 		// Given
 		final String email = EMAIL_NICHT_VORHANDEN;
 		
 		// When / Then
 		thrown.expect(NoResultException.class);
 		getEntityManager().createNamedQuery(Kunde.KUNDE_BY_EMAIL, Kunde.class)
 					.setParameter(Kunde.PARAM_EMAIL, email)
 					.getSingleResult();
                                                        
 	}
 
 	@Test
 	public void findKundenByNachnameVorhanden() {
 		// Given
 		final String nachname = NACHNAME_VORHANDEN;
 		
 		// When
 		List <Kunde> kunden = getEntityManager().createNamedQuery(Kunde.KUNDE_BY_NACHNAME, Kunde.class)
 						.setParameter(Kunde.PARAM_NACHNAME, nachname)
 						.getResultList();
 		
 		// Then
 		assertThat(kunden.isEmpty(), is(false));
 		for(Kunde k : kunden){
 			assertThat(k.getNachname(), is(nachname));
 		}
 		
 	}
 	@Test
 	public void findKundenByNachnameNichtVorhanden() {
 		// Given
 		final String nachname = NACHNAME_NICHT_VORHANDEN;
 		
 		// When
 		List<Kunde> kunden = getEntityManager().createNamedQuery(Kunde.KUNDE_BY_NACHNAME, Kunde.class)
 						.setParameter(Kunde.PARAM_NACHNAME, nachname)
 						.getResultList();
 		
 		// Then
 		assertThat(kunden.isEmpty(), is(true));
 	}
 	
 	@Test
	@Ignore
 	public void findKundenByDateVorhanden() {
 		// Given
 		final Date seit = ERSTELLT_VORHANDEN;
 		
 		// When
 		
 		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
 		CriteriaQuery<Kunde> criteriaQuery =
 		builder.createQuery(Kunde.class);
 		Root<Kunde> k = criteriaQuery.from(Kunde.class);
 		Path<Date> DatePath = k.get(Kunde_.erstelltAm);
 		// k.get("nachname")
 		Predicate pred = builder.equal(DatePath, seit);
 		criteriaQuery.where(pred);
 		TypedQuery<Kunde>query = getEntityManager().createQuery(criteriaQuery);
 		List<Kunde> kunden = query.getResultList();
 					
 		// Then
 		assertThat(kunden.isEmpty(), is(false));
 		for (Kunde ku: kunden) {
 			assertThat(ku.getErstelltAm(), is(seit));
 		}
 	
 	}
 
 	@Test
 	public void createPrivatkunde() {
 		// Given
 		Kunde kunde = new Kunde();
 		kunde.setNachname(NACHNAME_NEU);
 		kunde.setVorname(VORNAME_NEU);
 		kunde.setEmail(EMAIL_NEU);
 		kunde.setErstelltAm(ERSTELLT_VORHANDEN);
 		
 		final Adresse adresse = new Adresse();
 		adresse.setPlz(PLZ_NEU);
 		adresse.setOrt(ORT_NEU);
 		adresse.setStrasse(STRASSE_NEU);
 		adresse.setHausNr(HAUSNR_NEU);
 		adresse.setLand(LAND_NEU);
 		adresse.setKunde(kunde);
 		kunde.setAdresse(adresse);
 		
 		// When
 		try {
 			getEntityManager().persist(kunde);         // abspeichern einschl. Adresse
 		}
 		catch (ConstraintViolationException e) {
 			// Es gibt Verletzungen bzgl. Bean Validation: auf der Console ausgeben
 			final Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
 			for (ConstraintViolation<?> v : violations) {
 				System.err.println("!!! FEHLERMELDUNG>>> " + v.getMessage());
 				System.err.println("!!! ATTRIBUT>>> " + v.getPropertyPath());
 				System.err.println("!!! ATTRIBUTWERT>>> " + v.getInvalidValue());
 			}
 			
 			throw new RuntimeException(e);
 		}
 		
 		// Then
 		
 		// Den abgespeicherten Kunden ueber eine Named Query ermitteln
 		List<Kunde> kunden = getEntityManager().createNamedQuery(Kunde.KUNDE_BY_NACHNAME, Kunde.class)
									.setParameter(Kunde.PARAM_NACHNAME, NACHNAME_NEU)
 									.getResultList();
 		
 		// Ueberpruefung des ausgelesenen Objekts
 		assertThat(kunden.size(), is(1));
 		kunde = (Kunde) kunden.get(0);
 		assertThat(kunde.getKundenNr().longValue() > 0, is(true));
 		assertThat(kunde.getNachname(), is(NACHNAME_NEU));
 	}
 	
 	@Ignore("Beispiel fuer einen unvollstaendigen Test")
 	@Test
 	public void notYetImplemented() {
 		fail();
 	}
 	
 	@Test
 	public void createKundeOhneAdresse() throws HeuristicMixedException, HeuristicRollbackException, SystemException {
 		// Given
 		final String nachname = NACHNAME_NEU;
 		final String email = EMAIL_NEU;
 		
 		// When
 		final Kunde kunde = new Kunde();
 		kunde.setNachname(nachname);
 		kunde.setEmail(email);
 		getEntityManager().persist(kunde);
 		
 		// Then
 		try {
 			getUserTransaction().commit();
 		}
 		catch (RollbackException e) {
 			final PersistenceException cause = (PersistenceException) e.getCause();
 			final ConstraintViolationException cause2 = (ConstraintViolationException) cause.getCause();
 			final Set<ConstraintViolation<?>> violations = cause2.getConstraintViolations();
 			for (ConstraintViolation<?> v : violations) {
 				final String msg = v.getMessage();
 				if (msg.contains("Ein Kunde muss eine Adresse haben")) {
 					return;
 				}
 			}
 		}
 
 	}
 	
 }
