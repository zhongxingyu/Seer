 package org.fao.fi.vme.figis.component;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.fao.fi.figis.dao.FigisDao;
 import org.fao.fi.figis.domain.RefVme;
 import org.fao.fi.vme.dao.VmeDao;
 import org.fao.fi.vme.dao.config.FigisDataBaseProducer;
 import org.fao.fi.vme.dao.config.VmeDataBaseProducer;
 import org.fao.fi.vme.domain.Vme;
 import org.jglue.cdiunit.ActivatedAlternatives;
 import org.jglue.cdiunit.CdiRunner;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import static org.junit.Assert.assertEquals;
 
 @RunWith(CdiRunner.class)
 @ActivatedAlternatives({ VmeDataBaseProducer.class, FigisDataBaseProducer.class })
 public class VmeRefSyncTest {
 
 	@Inject
 	VmeRefSync vmeRefSync;
 
 	@Inject
 	VmeDao vmeDao;
 
 	@Inject
 	FigisDao figisDao;
 
 	/**
 	 * 
 	 */
 	@Test
 	public void testSync() {
 		assertEquals(0, figisDao.loadRefVmes().size());
 
 		int id = 234324;
 		Vme vme = new Vme();
 		vme.setId(id);
 		vmeDao.persist(vme);
 		vmeRefSync.sync();
 		assertEquals(1, figisDao.loadRefVmes().size());
 		vmeRefSync.sync();
 		List<RefVme> list = figisDao.loadRefVmes();
 		assertEquals(1, list.size());
		// RefVme found = list.get(0);
 		// assertEquals(1, found.getObservationList().size());
 
 	}
 }
