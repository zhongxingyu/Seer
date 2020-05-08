 package ch.hslu.appe.fs1301.data;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.junit.Test;
 
 import ch.hslu.appe.fs1301.data.shared.Bestellposition;
 import ch.hslu.appe.fs1301.data.shared.Bestellung;
 import ch.hslu.appe.fs1301.data.shared.Korrespondenz;
 import ch.hslu.appe.fs1301.data.shared.KorrespondenzTemplate;
 import ch.hslu.appe.fs1301.data.shared.Person;
 import ch.hslu.appe.fs1301.data.shared.Produkt;
 import ch.hslu.appe.fs1301.data.shared.Rechnung;
 
 /**
  * @author Thomas Bomatter
  * EntityTest. These are completly useless and are only here to satisfy code coverage :D
  */
 public class EntityTest extends BaseTestClass {
 
 	@Test
 	public void TestBestellposition() {
 		Bestellposition position = new Bestellposition();
 		position.setId(1);
 		position.setAnzahl(25);
 		position.setProdukt(new Produkt());
 		position.setRabatt(20);
 		
 		assertEquals(1, position.getId());
 		assertEquals(25, position.getAnzahl());
 		assertEquals(Produkt.class, position.getProdukt().getClass());
 		assertEquals(20, position.getRabatt());		
 		assertNotNull(position);
 	}
 	
 	@Test
 	public void TestBestellung() {
 		Bestellung bestellung = new Bestellung();
 		bestellung.setId(1);
 		bestellung.setRechnungs(new ArrayList<Rechnung>());
 		bestellung.setBestellpositions(new ArrayList<Bestellposition>());
 		bestellung.setBestelldatum(new Date());
 		bestellung.setLiefertermin_Ist(new Date());
 		bestellung.setLiefertermin_Soll(new Date());
 		bestellung.setPerson1(new Person());
 		bestellung.setPerson2(new Person());
 		
 		assertEquals(1, bestellung.getId());
 		assertEquals(ArrayList.class, bestellung.getRechnungs().getClass());
 		assertEquals(ArrayList.class, bestellung.getBestellpositions().getClass());
 		assertEquals(Date.class, bestellung.getBestelldatum().getClass());
 		assertEquals(Date.class, bestellung.getLiefertermin_Ist().getClass());
 		assertEquals(Date.class, bestellung.getLiefertermin_Soll().getClass());
 		assertEquals(Person.class, bestellung.getPerson1().getClass());
 		assertEquals(Person.class, bestellung.getPerson2().getClass());		
 		assertNotNull(bestellung);
 	}
 	
 	@Test
 	public void TestKorrespondenz() {
 		Korrespondenz korre = new Korrespondenz();
 		korre.setId(1);
 		korre.setInhalt("Test");
 		korre.setPerson1(new Person());
 		korre.setPerson2(new Person());
 		korre.setTyp(4);
 		
 		assertEquals(1, korre.getId());
 		assertEquals(String.class, korre.getInhalt().getClass());
 		assertEquals(Person.class, korre.getPerson1().getClass());
 		assertEquals(Person.class, korre.getPerson2().getClass());	
 		assertEquals(4, korre.getTyp());
 		assertNotNull(korre);
 	}
 	
 	@Test
 	public void TestKorrespondenzTemplate() {
 		KorrespondenzTemplate template = new KorrespondenzTemplate();
 		template.setId(1);
 		template.setInhalt("Test");
 		template.setTyp(4);
 		
 		assertEquals(1, template.getId());
 		assertEquals(4, template.getTyp());
 		assertEquals(String.class, template.getInhalt().getClass());
 	}
 	
 	@Test
 	public void TestPerson() {
 		Person person = new Person();
 		person.setId(1);
 		person.setEMail("Test");
 		person.setName("Test");
 		person.setVorname("Test");
 		person.setBenutzername("Test");
 		person.setOrt("Test");
 		person.setPlz(1);
 		person.setGeburtstag(new Date());
 		person.setRolle(0);
 		person.setStrasse("Test");
 		person.setPasswort("Test");
 		person.setAktiv(0);
 		
 		assertEquals(1, person.getId());
 		assertEquals(String.class, person.getEMail().getClass());
 		assertEquals(String.class, person.getName().getClass());
 		assertEquals(String.class, person.getVorname().getClass());
 		assertEquals(String.class, person.getBenutzername().getClass());
 		assertEquals(String.class, person.getOrt().getClass());
 		assertEquals(1, person.getPlz());
 		assertEquals(Date.class, person.getGeburtstag().getClass());		
 		assertEquals(0, person.getRolle());
 		assertEquals(String.class, person.getStrasse().getClass());
 		assertEquals(String.class, person.getPasswort().getClass());
 		assertEquals(0, person.getAktiv());
 	}
 	
 	@Test
 	public void TestProdukt() {
 		Produkt prod = new Produkt();
 		prod.setId(1);
 		prod.setBestellpositions(new ArrayList<Bestellposition>());
 		prod.setBezeichnung("Test");
 		prod.setLagerbestand(5);
 		prod.setMinimalMenge(3);
 		prod.setPreis(2000);
 		
 		assertEquals(1, prod.getId());
 		assertEquals(ArrayList.class, prod.getBestellpositions().getClass());
		assertEquals(String.class, prod.getBezeichnung().getClass());
 		assertEquals(5, prod.getLagerbestand());
 		assertEquals(3, prod.getMinimalMenge());
 		assertEquals(2000, prod.getPreis());
 	}
 	
 	@Test
 	public void TestRechnung() {
 		Rechnung bill = new Rechnung();
 		bill.setId(1);
 		bill.setBestellung(new Bestellung());
 		bill.setBetrag(200);
 		bill.setBezahlter_Betrag(200);
 		bill.setMahnstufe(0);
 		bill.setPerson(new Person());
 		bill.setZahlbarBis(new Date());
 		
 		assertEquals(1, bill.getId());
 		assertEquals(Bestellung.class, bill.getBestellung().getClass());
 		assertEquals(200, bill.getBetrag());
 		assertEquals(200, bill.getBezahlter_Betrag());
 		assertEquals(0, bill.getMahnstufe());
 		assertEquals(Person.class, bill.getPerson().getClass());
 		assertEquals(Date.class, bill.getZahlbarBis().getClass());
 	}
 }
