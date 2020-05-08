 package de.shop.Auftragsverwaltung.domain;
 
 import static javax.persistence.TemporalType.DATE;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Set;
 
 import de.shop.Artikelverwaltung.domain.Artikel;
 import de.shop.Auftragsverwaltung.domain.Lieferung;
 import de.shop.Kundenverwaltung.domain.Kunde;
 
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceException;
 import javax.persistence.TemporalType;
 import javax.persistence.TypedQuery;
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
 public class LieferungTest extends AbstractDomainTest {
 	private static final Long ID_VORHANDEN = Long.valueOf(455);
 	private static final int TAG = 31;
 	private static final int MONAT = Calendar.JANUARY;
 	private static final int JAHR = 2001;
 	private static final Date SEIT_VORHANDEN = new GregorianCalendar(JAHR, MONAT, TAG).getTime();
 	private static final Date BESTELLDATUM_VORHANDEN = new GregorianCalendar(2011, Calendar.SEPTEMBER, 26).getTime();
 	
 	private static final Date LIEFERUNGSDATUM_NEU = new GregorianCalendar(2012,Calendar.NOVEMBER,31).getTime();
 	private static final Date BESTELLDATUM_NEU = new GregorianCalendar(2012,Calendar.DECEMBER,31).getTime();
 	private static final int POS1ANZ_NEU = 3;
 	private static final Long POS1ARTIKELID_NEU = Long.valueOf(303);
 	private static final Long ID_NEU = Long.valueOf(452);
 
 
 	
 	
 	@Test
 	public void validate() {
 		assertThat(true, is(true));
 	}
 	
 	@Test
 	public void findLieferungByIdVorhanden() {
 		// Given
 		final long id = ID_VORHANDEN;
 		
 		// When
 		final TypedQuery<Lieferung> query = getEntityManager().createNamedQuery(Lieferung.LIEFERUNG_BY_ID,
 				Lieferung.class);
 		query.setParameter(Lieferung.PARAM_ID, id);
 		Lieferung lieferung = query.getSingleResult();
 		
 		// Then
 		assertThat(lieferung.getId(), is(id));
 	}
 	
 	@Test
 	public void findLieferungByIdFetchLieferungspositionen() {
 		// Given
 		final long id = ID_VORHANDEN;
 		
 		// When
 		final TypedQuery<Lieferung> query = getEntityManager().createNamedQuery(Lieferung.LIEFERUNG_BY_ID_LIEFERUNGSPOSITIONEN,
 				Lieferung.class);
 		query.setParameter(Lieferung.PARAM_ID, id);
 		final List<Lieferung> lieferungen = query.getResultList();
 		
 		// Then
 		assertThat(lieferungen.size() > 0, is (true));
 	}
 	
 	@Test
 	public void findLieferungByBestelldatum() {
 		//Given
 		final Date bestelldatum = BESTELLDATUM_VORHANDEN;
 		
 		//When
 		final TypedQuery<Lieferung> query = getEntityManager().createNamedQuery(Lieferung.LIEFERUNG_BY_BESTELLDATUM,
 				Lieferung.class);
 		query.setParameter(Lieferung.PARAM_BESTELLDATUM, bestelldatum);
		Lieferung lieferung = query.getSingleResult();
 		
 		// Then
		assertThat(lieferung.getBestelldatum(), is(bestelldatum));
 	}
 	
 	@Test
 	public void createLieferung() {
 		
 		// Given
 		final int position1Anz = POS1ANZ_NEU;
 		final Long position1ArtikelId = POS1ARTIKELID_NEU;
 		final Long lieferungId = ID_NEU;
 
 		Artikel artikel = getEntityManager().createNamedQuery(Artikel.FIND_Artikel_BY_Artikel_ID, Artikel.class)
 											.setParameter(Artikel.PARAM_ID, position1ArtikelId)
 											.getSingleResult();
 		
 		// When
 		Lieferung lieferung = new Lieferung();
 		lieferung.setId(lieferungId);
 		lieferung.setBestelldatum(BESTELLDATUM_NEU);
 		lieferung.setLieferungsdatum(LIEFERUNGSDATUM_NEU);
 		
 		Lieferungsposition position1 = new Lieferungsposition();
 		position1.setAnzahl(position1Anz);
 		position1.setArtikel(artikel);
 		lieferung.addLieferungsposition(position1);
 		
 		try {
			getEntityManager().persist(lieferung);
 			getEntityManager().persist(position1);
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
 		// Then
 				List<Lieferung> lieferungen = getEntityManager().createNamedQuery(Lieferung.LIEFERUNG_BY_ID, Lieferung.class)
 															.setParameter(Lieferung.PARAM_ID, lieferungId) 
 															.getResultList();
 				
 				// Ueberpruefung des ausgelesenen Objekts
 				assertThat(lieferungen.size(), is(1));
 				
 				/*kunde = (Kunde) kunden.get(0);
 				assertThat(kunde.getKundenNr().longValue() > 0, is(true));
 				assertThat(kunde.getNachname(), is(NACHNAME_NEU));*/
 	}
 
 	@Test
 	public void createLieferungOhneBestelldatum() throws HeuristicMixedException, HeuristicRollbackException,
 	                                                  SystemException {
 		// Given
 		final Date bestelldatum = BESTELLDATUM_NEU;
 		
 		// When
 		final Lieferung lieferung = new Lieferung();
 		lieferung.setBestelldatum(bestelldatum);
 		getEntityManager().persist(lieferung);
 		
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
 				if (msg.contains("Eine Lieferung muss ein Bestelldatum enthalten.")) {
 					return;
 				}
 			}
 			fail("Kein Fehler trotz fehlendem Bestelldatum");
 		}
 		
 	}
 	
 }
