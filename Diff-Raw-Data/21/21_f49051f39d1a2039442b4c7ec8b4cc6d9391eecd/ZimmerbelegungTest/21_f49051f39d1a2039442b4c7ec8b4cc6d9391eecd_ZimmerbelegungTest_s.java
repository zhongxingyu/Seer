 package de.hs.lu.service;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.hs.lu.model.Reservierung;
 import de.hs.lu.model.Zimmer;
 import de.hs.lu.model.Zimmerkategorie;
 import de.hs.lu.orm.dao.ReservierungDao;
 import de.hs.lu.orm.dao.ZimmerDao;
 import de.hs.lu.orm.dao.ZimmerkategorieDao;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "/META-INF/spring/applicationContext*.xml")
 public class ZimmerbelegungTest {
 	
 	@Autowired
 	private ZimmerDao zimmerDao;
 	
 	@Autowired
 	private ZimmerkategorieDao zimmerkategorieDao;
 	
 	@Autowired
 	private ReservierungDao reservierungDao;
 	
 	@Autowired
 	private Zimmerbelegung zimmerbelegung;
 	
 	private Zimmerkategorie zk = new Zimmerkategorie();
 	
 	private Zimmer z = new Zimmer();
 	
 	private Reservierung r = new Reservierung();
 	
 	@Before
 	public void setUp() throws Exception {
 			
 	}
 
 	
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Transactional
 	@Test
 	public void test() {
 		
 		
 		zk.setZimmertyp("einzelzimmer");		
 		zimmerkategorieDao.persist(zk);		
 
 		z.setZimmerNr(667);
 		z.setZimmerkategorie(zk);
 		zimmerDao.persist(z);
 		
 		r.setStartdatum(100l);
 		r.setEnddatum(200l);
 		r.setZimmer(z);
 		reservierungDao.persist(r);
 		reservierungDao.flush();			
 		
 		Zimmer z_temp = zimmerDao.findZimmerByZimmerNr(667);
 		assertNotNull(z_temp);
 		Reservierung re = z_temp.getReservierungen().iterator().next();
 		assertEquals(re.getStartdatum(), 100l);
 		assertEquals(re.getEnddatum(), 200l);
 		assertNotSame(re.getStartdatum(), 101l);
 		
 		//es gibt 6 möglichkeiten wir versuchen alle abzudecken
 		//bei 2 fällen ist noch ein zimmer frei, ansonsten nicht!
 		
 		assertEquals(z_temp.getId(), zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 50l, 70l).getId());
 		assertEquals(z_temp.getId(), zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 201l, 250l).getId());
 				
 		assertNull(zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 99l, 199l));
 		assertNull(zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 101l, 199l));
 		assertNull(zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 101l, 201l));		
 		assertNull(zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 99l, 201l));	
 		
 		Zimmer z2 = new Zimmer();
 		z2.setZimmerkategorie(zk);
 		z2.setZimmerNr(668);
 		zimmerDao.persist(z2);
 		zimmerDao.flush();
 		
 		
 		//wir haben ein weiteres Zimmer hinzugefügt, deshalb sollte es jetzt immer was frei haben
 		
 		assertNotNull(zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 50l, 70l));
 		assertNotNull(zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 201l, 250l));				
 		assertNotNull(zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 99l, 199l));
 		assertNotNull(zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 101l, 199l));
 		assertNotNull(zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 101l, 201l));		
		assertNotNull(zimmerbelegung.freieZimmerSuche(zk.getZimmertyp(), 99l, 201l));
		
		assertNotNull(zimmerbelegung.freieZimmerSuche("%", 99l, 201l));
			
 	}
 
 }
