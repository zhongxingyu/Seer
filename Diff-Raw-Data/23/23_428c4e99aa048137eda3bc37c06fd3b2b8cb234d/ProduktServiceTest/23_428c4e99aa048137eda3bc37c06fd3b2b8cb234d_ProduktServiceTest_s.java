 package de.shop.artikelverwaltung.service;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.Locale;
 
 import javax.inject.Inject;
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 import org.jboss.arquillian.junit.Arquillian;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.runner.RunWith;
 
 import de.shop.artikelverwaltung.domain.Produkt;
 import de.shop.util.AbstractResourceTest;
 import de.shop.util.Log;
 
 @RunWith(Arquillian.class)
 public class ProduktServiceTest extends AbstractResourceTest {
 	
 	@Inject
 	private ProduktService ps;
 	
 	private static final Long PRODUKT_ID = Long.valueOf("500");
 	
 	private static final String BEZEICHNUNG = "Pulli";
 	private static final String FARBE = "Blau";
 	private static final String GROESSE = "42";
 	private static final int VORRAT = 5;
 	private static final Float PRODUKT_PREIS = Float.valueOf(50);
 //	private static final Long PRODUKT_ID_NEW = Long.valueOf("525");
 	
 	@Rule
 	public ExpectedException thrown = ExpectedException.none();
 	
 	private Produkt createTestProdukt() {
 		Produkt produkt = new Produkt(BEZEICHNUNG, PRODUKT_PREIS, FARBE, GROESSE, VORRAT);
 		
 		return produkt;
 	
 	}
 	
 	@Test
 	public void findProduktById() {
 		// Given
 		Locale locale = Locale.GERMAN;
 		// When
 		Produkt produkt = ps.findProduktById(PRODUKT_ID, locale);
 		// Then
 		assertThat(produkt.getId(), is(PRODUKT_ID));
 	}
 	
 	@Log
 	@Test
 	public void createTest() throws RollbackException,
 		HeuristicMixedException, HeuristicRollbackException, SystemException, NotSupportedException {
 		
 		// Given
 		
 		Produkt produkt = createTestProdukt();
 		Locale locale = Locale.GERMAN;
 		
 		// When
 		Produkt p = ps.createProdukt(produkt, locale);
 		
 		// Then
 		Long id = p.getId();
 		Produkt pro = ps.findProduktById(id, locale);
 		assertThat(pro.getId(),   is(p.getId()));
 
 	}
 	
 	@Test
 	public void updateTest() {
 		// Given
 		Locale locale = Locale.GERMAN;
 		Long l = Long.valueOf("501");
 		Produkt produkt = ps.findProduktById(l, locale);
 		String bez = "Pollunder";
 		produkt.setBezeichnung(bez);
 		
 		// When				
 		ps.updateProdukt(produkt, locale);	
 		Produkt updatedProdukt = ps.findProduktById(l, locale);
 		
 		// Then
 		assertThat(updatedProdukt.getBezeichnung(), is(bez));
 	}
 	
 	
 
 	@Test
 	public void updateTestFail() {
 		// Given
 		Locale locale = Locale.GERMAN;
 		Long l = Long.valueOf("508");
 		Produkt produkt = ps.findProduktById(l, locale);
 		produkt.setBezeichnung("12polizei");
 		
 		// When		
 		thrown.expect(InvalidProduktBezeichnungException.class);	
 		ps.updateProdukt(produkt, locale);
 		
 		// Then
 		thrown.expect(InvalidProduktBezeichnungException.class);
 	}
 	
 	
 }
