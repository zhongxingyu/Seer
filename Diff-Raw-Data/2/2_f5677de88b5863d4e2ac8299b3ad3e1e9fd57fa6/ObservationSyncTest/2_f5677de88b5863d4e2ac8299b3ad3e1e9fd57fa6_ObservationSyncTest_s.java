 package org.fao.fi.vme.sync2;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.fao.fi.figis.dao.FigisDao;
 import org.fao.fi.figis.domain.Observation;
 import org.fao.fi.figis.domain.ObservationXml;
 import org.fao.fi.figis.domain.RefVme;
 import org.fao.fi.figis.domain.VmeObservation;
 import org.fao.fi.vme.dao.VmeDao;
 import org.fao.fi.vme.dao.config.FigisDataBaseProducer;
 import org.fao.fi.vme.dao.config.VmeDataBaseProducer;
 import org.fao.fi.vme.domain.GeneralMeasures;
 import org.fao.fi.vme.domain.GeoRef;
 import org.fao.fi.vme.domain.History;
 import org.fao.fi.vme.domain.InformationSource;
 import org.fao.fi.vme.domain.SpecificMeasures;
 import org.fao.fi.vme.domain.Vme;
 import org.fao.fi.vme.test.RefVmeMock;
 import org.fao.fi.vme.test.VmeMock;
 import org.jglue.cdiunit.ActivatedAlternatives;
 import org.jglue.cdiunit.CdiRunner;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 @RunWith(CdiRunner.class)
 @ActivatedAlternatives({ VmeDataBaseProducer.class, FigisDataBaseProducer.class })
 public class ObservationSyncTest {
 
 	@Inject
 	ObservationSync observationSync;
 
 	@Inject
 	FigisDao figisDao;
 
 	@Inject
 	VmeDao vmeDao;
 
 	Long id;
 
 	@Before
 	public void generateVme() {
 		Vme vme = VmeMock.generateVme(1);
 
 		for (GeneralMeasures o : vme.getRfmo().getGeneralMeasuresList()) {
 			vmeDao.persist(o);
 		}
 
 		for (History h : vme.getRfmo().getFishingHistoryList()) {
 			vmeDao.persist(h);
 		}
 
 		for (InformationSource informationSource : vme.getRfmo().getInformationSourceList()) {
 			vmeDao.persist(informationSource);
 		}
 
 		vmeDao.persist(vme.getRfmo());
 
 		for (History h : vme.getHistoryList()) {
 			vmeDao.persist(h);
 		}
 		for (GeoRef geoRef : vme.getGeoRefList()) {
 			vmeDao.persist(geoRef);
 		}
 
 		vmeDao.persist(vme);
 
 		RefVme refVme = RefVmeMock.create();
 		refVme.setId(vme.getId());
		// figisDao.persist(refVme);
 		System.out.println("=========================================");
 		System.out.println("=========================================");
 	}
 
 	/**
 	 * 
 	 */
 
 	@Test
 	public void testSync() {
 		assertNrOfObjects(0);
 		observationSync.sync();
 		assertNrOfObjects(1);
 
 		// // test repeatability
 		observationSync.sync();
 		observationSync.sync();
 		assertNrOfObjects(1);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testSyncWithUpdate() {
 		observationSync.sync();
 		assertNrOfObjects(1);
 
 		assertEquals(1, vmeDao.count(SpecificMeasures.class).intValue());
 
 		List<Vme> vmeList = (List<Vme>) vmeDao.loadObjects(Vme.class);
 		for (Vme vme : vmeList) {
 			SpecificMeasures specificMeasures = new SpecificMeasures();
 			specificMeasures.setId(333333333l);
 			specificMeasures.setYear(VmeMock.YEAR + 1);
 			vme.getSpecificMeasureList().add(specificMeasures);
 			vmeDao.merge(vme);
 		}
 		observationSync.sync();
 		assertNrOfObjects(2);
 
 		// test repeatability
 		observationSync.sync();
 		assertNrOfObjects(2);
 	}
 
 	@Test
 	public void testSyncCD_COLLECTION() {
 		assertNrOfObjectsX(0);
 		observationSync.sync();
 		assertNrOfObjectsX(1);
 		List<?> oss = figisDao.loadObjects(Observation.class);
 		for (Object object : oss) {
 			Observation o = (Observation) object;
 			assertNotNull(o.getCollection());
 			assertNotNull(o.getOrder());
 			String id = new String(o.getId() + ":en");
 			System.out.println("======================about to check====" + id);
 
 			ObservationXml xml = (ObservationXml) figisDao.find(ObservationXml.class, id);
 			assertNotNull(xml);
 			assertEquals(o.getId(), xml.getObservation().getId());
 			System.out.println("==========================" + o.getId());
 		}
 
 	}
 
 	private void assertNrOfObjectsX(int i) {
 		System.out.println("assertNrOfObjectsX = " + i);
 		System.out.println(figisDao.count(VmeObservation.class));
 		System.out.println(figisDao.count(Observation.class));
 		System.out.println(figisDao.count(ObservationXml.class));
 	}
 
 	private void assertNrOfObjects(int i) {
 		assertEquals(i, figisDao.count(VmeObservation.class).intValue());
 		assertEquals(i, figisDao.count(Observation.class).intValue());
 		assertEquals(i, figisDao.count(ObservationXml.class).intValue());
 	}
 };
