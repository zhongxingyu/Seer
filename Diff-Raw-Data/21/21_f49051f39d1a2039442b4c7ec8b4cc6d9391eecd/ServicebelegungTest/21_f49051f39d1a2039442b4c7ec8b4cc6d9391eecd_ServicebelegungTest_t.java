 package de.hs.lu.service;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.hs.lu.model.ReservierungsService;
 import de.hs.lu.model.ZusatzService;
 import de.hs.lu.orm.dao.ReservierungsServiceDao;
 import de.hs.lu.orm.dao.ZusatzServiceDao;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "/META-INF/spring/applicationContext*.xml")
 public class ServicebelegungTest {	
 	
 	@Autowired
 	private ZusatzServiceDao zsDao;
 	
 	@Autowired
 	private ReservierungsServiceDao rsDao;
 	
 	@Autowired
 	private Servicebelegung servicebelegung;
 	
 	@Autowired
 	private Zimmerbelegung zb;
 	
 	private ZusatzService zs = new ZusatzService();
 	
 	@Before
 	public void setUp() throws Exception {
 		
 		zs.setName("bmw");
 		zs.setAnzahl(5);
 		zsDao.persist(zs);
 				
 		ReservierungsService rs = new ReservierungsService();
 		rs.setStartdatum(100l);
 		rs.setEnddatum(200l);
 		rs.setZusatzService(zs);
 		rsDao.persist(rs);
 		
 		ReservierungsService rs2 = new ReservierungsService();
 		rs2.setStartdatum(50l);
 		rs2.setEnddatum(150l);
 		rs2.setZusatzService(zs);
 		rsDao.persist(rs2);
 		
 		ReservierungsService rs3 = new ReservierungsService();
 		rs3.setStartdatum(150l);
 		rs3.setEnddatum(250l);
 		rs3.setZusatzService(zs);
 		rsDao.persist(rs3);
 		
 		ReservierungsService rs4 = new ReservierungsService();
 		rs4.setStartdatum(50l);
 		rs4.setEnddatum(250l);
 		rs4.setZusatzService(zs);
 		rsDao.persist(rs4);
 		
 		ReservierungsService rs5 = new ReservierungsService();
 		rs5.setStartdatum(50l);
 		rs5.setEnddatum(75l);
 		rs5.setZusatzService(zs);
 		rsDao.persist(rs5);			
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Transactional
 	@Test
 	public void test() {
 	
 		List<Object[]> rsList = rsDao.findReservierungsServiceByStartAndEnd(100l, 200l);
 		
 		for(Object[] row: rsList)
 		{			
 			// wir pruefen ob es nur vier reservierte zusatzservice gibt die in unser datum fallen
 			assertEquals(4l, row[1]);
 		}
 		
 		ReservierungsService rs6 = new ReservierungsService();
 		rs6.setStartdatum(101l);
 		rs6.setEnddatum(199l);
 		rs6.setZusatzService(zs);
 		rsDao.persist(rs6);
 		
 		rsList = rsDao.findReservierungsServiceByStartAndEnd(100l, 200l);		
 		assertEquals(1, rsList.size());
 		
 		for(Object[] row: rsList)
 		{
 			// wir pruefen ob es nur 5 reservierte zusatzservice gibt die in unser datum fallen
 			assertEquals(5l, row[1]);
 		}				
 		
 	}
 	
 	@Transactional
 	@Test
 	public void BelegungTest()
 	{	
 		zb.freieZimmerSuche("blub", 1l, 2l);
 		List<ZusatzService> freie = servicebelegung.freieServiceSuche(100l, 200l);		
 		
 		assertEquals("bmw", freie.get(0).getName());
	
 	}
 	
 	@Transactional
 //	@Test
 	//funktioniert nur bei leerer datenbank
 	public void BelegungTest2()
 	{	
 		zs.setAnzahl(0);
 		zsDao.merge(zs);
 		
 		ZusatzService beamer = new ZusatzService();
 		beamer.setAnzahl(1);
 		zsDao.persist(beamer);
 		
 		ReservierungsService rs_beamer = new ReservierungsService();
 		rs_beamer.setStartdatum(100l);
 		rs_beamer.setEnddatum(200l);
 		rs_beamer.setZusatzService(beamer);
 		rsDao.persist(rs_beamer);
 		
 		List<ZusatzService> freie = servicebelegung.freieServiceSuche(100l, 200l);
 		assertEquals(0, freie.size());
 		
 		freie = servicebelegung.freieServiceSuche(101l, 199l);
 		assertEquals(0, freie.size());
 		
 		
 		freie = servicebelegung.freieServiceSuche(80l, 150l);
 		assertEquals(0, freie.size());
 		
 		freie = servicebelegung.freieServiceSuche(150l, 250l);
 		assertEquals(0, freie.size());
 		
 		freie = servicebelegung.freieServiceSuche(200l, 250l);
 		assertEquals(0, freie.size());
 
 		
 	}
 	
 	
 
 }
