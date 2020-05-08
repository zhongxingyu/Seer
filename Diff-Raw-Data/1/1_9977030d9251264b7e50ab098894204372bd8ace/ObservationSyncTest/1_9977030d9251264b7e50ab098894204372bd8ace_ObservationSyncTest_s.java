 package org.fao.fi.vme.sync2;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.fao.fi.figis.dao.FigisDao;
 import org.fao.fi.figis.domain.Observation;
 import org.fao.fi.figis.domain.ObservationXml;
 import org.fao.fi.figis.domain.RefVme;
 import org.fao.fi.figis.domain.VmeObservation;
 import org.fao.fi.figis.domain.test.RefVmeMock;
 import org.fao.fi.vme.dao.VmeDao;
 import org.fao.fi.vme.dao.config.FigisDataBaseProducer;
 import org.fao.fi.vme.dao.config.VmeDataBaseProducer;
 import org.fao.fi.vme.domain.SpecificMeasures;
 import org.fao.fi.vme.domain.Vme;
 import org.fao.fi.vme.domain.test.ValidityPeriodMock;
 import org.fao.fi.vme.domain.test.VmeMock;
 import org.fao.fi.vme.test.FigisDaoTestLogic;
 import org.fao.fi.vme.test.VmeDaoTestLogic;
 import org.jglue.cdiunit.ActivatedAlternatives;
 import org.jglue.cdiunit.CdiRunner;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * 
  * @author Erik van Ingen
  * 
  */
 @RunWith(CdiRunner.class)
 @ActivatedAlternatives({ VmeDataBaseProducer.class, FigisDataBaseProducer.class })
 public class ObservationSyncTest extends FigisDaoTestLogic {
 
 	@Inject
 	ObservationSync observationSync;
 
 	@Inject
 	FigisDao figisDao;
 
 	@Inject
 	VmeDao vmeDao;
 
 	Long id;
 
 	@Before
 	public void generateVme() {
 		VmeDaoTestLogic l = new VmeDaoTestLogic();
 		l.mockAndSaveVme(vmeDao, 1);
 		RefVme refVme = RefVmeMock.create();
 		refVme.setId(VmeMock.VME_ID);
 		figisDao.persist(refVme);
 	}
 
 	/**
 	 * 
 	 */
 
 	@Test
 	public void testSync() {
 		assertNrOfObjects(0);
 		observationSync.sync();
 
 		assertNrOfObjects(ValidityPeriodMock.getNumberOfYearInclusive());
 
 		// // test repeatability
 		observationSync.sync();
 		observationSync.sync();
 		assertNrOfObjects(ValidityPeriodMock.getNumberOfYearInclusive());
 	}
 
 	@Test
 	public void testSyncWithUpdate() {
 		observationSync.sync();
 		assertNrOfObjects(ValidityPeriodMock.getNumberOfYearInclusive());
 
 		assertEquals(1, vmeDao.count(SpecificMeasures.class).intValue());
 
 		@SuppressWarnings("unchecked")
 		List<Vme> vmeList = (List<Vme>) vmeDao.loadObjects(Vme.class);
 		for (Vme vme : vmeList) {
 			SpecificMeasures specificMeasures = new SpecificMeasures();
 			specificMeasures.setId(333333333l);
 			specificMeasures.setYear(VmeMock.YEAR + 1);
 			vme.getSpecificMeasuresList().add(specificMeasures);
 			vmeDao.merge(vme);
 		}
 		observationSync.sync();
 		assertNrOfObjects(ValidityPeriodMock.getNumberOfYearInclusive());
 
 		// test repeatability
 		observationSync.sync();
 		assertNrOfObjects(ValidityPeriodMock.getNumberOfYearInclusive());
 	}
 
 	@Test
 	public void testSyncCD_COLLECTION() {
 		assertNrOfObjects(0);
 		observationSync.sync();
 		assertNrOfObjects(ValidityPeriodMock.getNumberOfYearInclusive());
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
 
 	private void assertNrOfObjects(int i) {
 		assertEquals(i, figisDao.count(VmeObservation.class).intValue());
 		assertEquals(i, figisDao.count(Observation.class).intValue());
 		assertEquals(i, figisDao.count(ObservationXml.class).intValue());
 	}
 };
