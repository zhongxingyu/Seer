 /**
  * 
  */
 package org.fao.fi.vme.batch.reference;
 
 import javax.inject.Inject;
 
 import org.fao.fi.vme.domain.model.Authority;
 import org.fao.fi.vme.domain.model.InformationSourceType;
 import org.fao.fi.vme.domain.model.VmeCriteria;
 import org.fao.fi.vme.domain.model.VmeType;
 import org.vme.dao.ReferenceBatchDao;
 
 /**
  * Batch will which load or update the reference data
  * 
  * 
  * 
  * @author Erik van Ingen
  * 
  */
 public class ReferenceDataHardcodedBatch {
 
 	@Inject
 	private ReferenceBatchDao dao;
 
 	public void run() {
 		createAuthorities();
 		createVmeCriterias();
 		createVmeTypes();
 		createInformationSourceTypes();
 		// createYears();
 	}
 
 	private void createAuthorities() {
 		dao.syncStoreObject(new Authority(20010L, "CCAMLR", "Commission for the Conservation of Antarctic Marine Living Resources"), 20010L);
		dao.syncStoreObject(new Authority(24561L, "GFCM", "General Fishery Commission for the Mediterranean sea"), 24561L);
 		dao.syncStoreObject(new Authority(20220L, "NAFO", "Northwest Atlantic Fisheries Organization"), 20220L);
 		dao.syncStoreObject(new Authority(21580L, "NEAFC", "North East Atlantic Fisheries Commission"), 21580L);
 		dao.syncStoreObject(new Authority(22140L, "SEAFO", "South East Atlantic Fisheries Organisation"), 22140L);
 		dao.syncStoreObject(new Authority(24564L, "NPFC", "North Pacific Fisheries Commission"), 24564L);
 
 		// repAuthority.put((long)90010, new
 		// Authority(22140,"SIODFA","Southern Indian Ocean Deepsea Fishers' Association"),);
 	}
 
 	private void createVmeCriterias() {
 		dao.syncStoreObject(new VmeCriteria(10L, "Uniqueness or rarity"), 10L);
 		dao.syncStoreObject(new VmeCriteria(20L, "Functional significance of the habitat"), 20L);
 		dao.syncStoreObject(new VmeCriteria(30L, "Fragility"), 30L);
 		dao.syncStoreObject(new VmeCriteria(40L, "Life-history traits"), 40L);
 		dao.syncStoreObject(new VmeCriteria(50L, "Structural complexity"), 50L);
 		dao.syncStoreObject(new VmeCriteria(60L, "Unspecified"), 60L);
 	}
 
 	private void createVmeTypes() {
 		dao.syncStoreObject(new VmeType(10L, "VME"), 10L);
 		dao.syncStoreObject(new VmeType(20L, "Risk area"), 20L);
 		dao.syncStoreObject(new VmeType(30L, "Other types of closed/restricted area"), 30L);
 	}
 
 	private void createInformationSourceTypes() {
 		dao.syncStoreObject(new InformationSourceType(1L, "Book", InformationSourceType.IS_NOT_A_MEETING_DOCUMENT), 1L);
 		dao.syncStoreObject(new InformationSourceType(2L, "Meeting documents",
 				InformationSourceType.IS_A_MEETING_DOCUMENT), 2L);
 		dao.syncStoreObject(new InformationSourceType(3L, "Journal", InformationSourceType.IS_NOT_A_MEETING_DOCUMENT),
 				3L);
 		dao.syncStoreObject(new InformationSourceType(4L, "Project", InformationSourceType.IS_NOT_A_MEETING_DOCUMENT),
 				4L);
 		dao.syncStoreObject(
 				new InformationSourceType(6L, "CD-ROM/DVD", InformationSourceType.IS_NOT_A_MEETING_DOCUMENT), 6L);
 		dao.syncStoreObject(new InformationSourceType(99L, "Other", InformationSourceType.IS_NOT_A_MEETING_DOCUMENT),
 				99L);
 	}
 
 	// private void createYears() {
 	// dao.syncStoreObject(new ReferenceYear(2013), 2013);
 	// dao.syncStoreObject(new ReferenceYear(2012), 2012);
 	// dao.syncStoreObject(new ReferenceYear(2011), 2011);
 	// dao.syncStoreObject(new ReferenceYear(2010), 2010);
 	// dao.syncStoreObject(new ReferenceYear(2009), 2009);
 	// dao.syncStoreObject(new ReferenceYear(2008), 2008);
 	// dao.syncStoreObject(new ReferenceYear(2007), 2007);
 	// dao.syncStoreObject(new ReferenceYear(2006), 2006);
 	// }
 }
