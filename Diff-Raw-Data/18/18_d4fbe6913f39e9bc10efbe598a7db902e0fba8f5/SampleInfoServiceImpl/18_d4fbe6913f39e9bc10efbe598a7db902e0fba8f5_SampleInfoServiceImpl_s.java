 package org.gwaspi.dao.sql;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import org.gwaspi.constants.cDBGWASpi;
 import org.gwaspi.constants.cDBSamples;
 import org.gwaspi.dao.SampleInfoService;
 import org.gwaspi.database.DbManager;
 import org.gwaspi.global.ServiceLocator;
 import org.gwaspi.model.SampleInfo;
 import org.gwaspi.model.SampleKey;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class SampleInfoServiceImpl implements SampleInfoService {
 
 	private static final Logger log = LoggerFactory.getLogger(SampleInfoServiceImpl.class);
 
 	public SampleInfoServiceImpl() {
 	}
 
 	@Override
 	public String createSamplesInfoTable() {
 		boolean result = false;
 		try {
 			DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
 			// CREATE SAMPLE table in given SCHEMA
 			db.createTable(cDBGWASpi.SCH_SAMPLES,
 					cDBSamples.T_SAMPLES_INFO,
 					cDBSamples.T_CREATE_SAMPLES_INFO);
 			result = true;
 		} catch (Exception ex) {
 			log.error("Error creating Sample table", ex);
 		}
 		return (result) ? "1" : "0";
 	}
 
 	@Override
 	public List<String> selectSampleIDList(Object poolId) {
 		try {
 			DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
 			String sql = "SELECT " + cDBSamples.f_SAMPLE_ID + " FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_POOL_ID + "='" + poolId + "' ORDER BY order_id  WITH RR";
 			List<Map<String, Object>> rs = dBManager.executeSelectStatement(sql);
 
 			List<String> sampleIds = new ArrayList<String>(rs.size());
 			for (Map<String, Object> id : rs) {
 				// PREVENT PHANTOM-DB READS EXCEPTIONS - CAUTION!!
 				if (!id.isEmpty()) {
 					sampleIds.add(id.values().iterator().next().toString());
 				}
 			}
 
 			return sampleIds;
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	@Override
 	public List<SampleInfo> getAllSampleInfoFromDB() throws IOException {
 		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
 
 		String sql = "SELECT * FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " ORDER BY order_id  WITH RR";
 		List<Map<String, Object>> rs = dBManager.executeSelectStatement(sql);
 
 		return parseSampleInfos(rs);
 	}
 
 	@Override
 	public List<SampleInfo> getAllSampleInfoFromDBByPoolID(Object poolId) throws IOException {
 		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
 
 		String sql = "SELECT * FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_POOL_ID + "='" + poolId + "'" + " ORDER BY order_id  WITH RR";
 		List<Map<String, Object>> rs = dBManager.executeSelectStatement(sql);
 
 		return parseSampleInfos(rs);
 	}
 
 	@Override
 	public List<SampleInfo> getCurrentSampleInfoFromDB(SampleKey key, Object poolId) throws IOException {
 
 		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
 
 		String sql = "SELECT * FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_SAMPLE_ID + "='" + key.getSampleId() + "' AND " + cDBSamples.f_FAMILY_ID + "='" + key.getFamilyId() + "' AND " + cDBSamples.f_POOL_ID + "='" + poolId + "'  WITH RR";
 		List<Map<String, Object>> rs = dBManager.executeSelectStatement(sql);
 
 		return parseSampleInfos(rs);
 	}
 
 	@Override
 	public void deleteSamplesByPoolId(Object poolId) throws IOException {
 		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
 
 		String sql = "DELETE FROM " + cDBGWASpi.SCH_SAMPLES + "." + cDBSamples.T_SAMPLES_INFO + " WHERE " + cDBSamples.f_POOL_ID + "='" + poolId + "'";
 		dBManager.executeStatement(sql);
 	}
 
 	private static List<SampleInfo> parseSampleInfos(List<Map<String, Object>> rs) {
 
 		List<SampleInfo> sampleInfos = new ArrayList<SampleInfo>(rs.size());
 		int id = 1;
 		for (Map<String, Object> sampleProperties : rs) {
 			// PREVENT PHANTOM-DB READS EXCEPTIONS - CAUTION!!
 			if (sampleProperties.size() == cDBSamples.T_CREATE_SAMPLES_INFO.length) {
 				sampleInfos.add(parseSampleInfo(id, sampleProperties));
 				id++;
 			} else {
 				log.warn(
 						"Bad SampleInfo read from DB; properties size {} instead of {}",
 						sampleProperties.size(),
 						cDBSamples.T_CREATE_SAMPLES_INFO.length);
 			}
 		}
 
 		return sampleInfos;
 	}
 
 	private static SampleInfo parseSampleInfo(int id, Map<String, Object> properties) {
 
 		// TODO this can be improved, performance wise (or maybe better replace with JPA directy!)
 		Object familyId = properties.get(cDBSamples.f_FAMILY_ID);
 		Object sampleId = properties.get(cDBSamples.f_SAMPLE_ID);
 		Object fatherId = properties.get(cDBSamples.f_FATHER_ID);
 		Object motherId = properties.get(cDBSamples.f_MOTHER_ID);
 		Object sexStr = properties.get(cDBSamples.f_SEX);
 		Object affectionStr = properties.get(cDBSamples.f_AFFECTION);
 		Object ageStr = properties.get(cDBSamples.f_AGE);
 		Object category = properties.get(cDBSamples.f_CATEGORY);
 		Object disease = properties.get(cDBSamples.f_DISEASE);
 		Object population = properties.get(cDBSamples.f_POPULATION);
 		Object filter = properties.get(cDBSamples.f_FILTER);
 		Object poolId = properties.get(cDBSamples.f_POOL_ID);
 		Object approvedStr = properties.get(cDBSamples.f_APPROVED);
 		Object statusStr = properties.get(cDBSamples.f_STATUS_ID_FK);
 
 		SampleInfo.Sex sex = SampleInfo.Sex.parse(sexStr.toString());
 		SampleInfo.Affection affection = SampleInfo.Affection.parse(affectionStr.toString());
 		int age = Integer.parseInt(ageStr.toString());
 		int approved = (approvedStr == null) ? 0 : Integer.parseInt(approvedStr.toString());
 		int status = (statusStr == null) ? 0 : Integer.parseInt(statusStr.toString());
 
 		SampleInfo sampleInfo = new SampleInfo(
 				id,
 				sampleId.toString(),
 				familyId.toString(),
 				fatherId.toString(),
 				motherId.toString(),
 				sex,
 				affection,
 				category.toString(),
 				disease.toString(),
 				population.toString(),
 				age,
 				(filter == null) ? "" : filter.toString(),
 				(poolId == null) ? "" : poolId.toString(),
 				approved,
 				status
 				);
 
 		return sampleInfo;
 	}
 
 	@Override
 	public List<String> insertSampleInfos(Integer studyId, Collection<SampleInfo> sampleInfos) throws IOException {
 		// Retrieving Samplelist from DB
 		List<String> samplesAllreadyInDBAL = new ArrayList<String>(0);
 		DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
 		try {
 			samplesAllreadyInDBAL = selectSampleIDList(studyId);
 		} catch (Exception ex) {
 			log.error(null, ex);
 		}
 
 		List<String> result = new ArrayList<String>();
 		if (!sampleInfos.isEmpty()) {
 			// FIRST UPDATE SAMPLES ALLREADY IN DB
 			updateSamplesByHashMap(db, studyId, sampleInfos, samplesAllreadyInDBAL);
 
 			// NEXT INSERT ANY NEW SAMPLES
 			result = insertSamplesByHashMap(db, studyId, sampleInfos, samplesAllreadyInDBAL);
 		}
 		return result;
 	}
 
 	private static List<String> insertSamplesByHashMap(DbManager db, Integer studyId, Collection<SampleInfo> sampleInfos, List<String> samplesAllreadyInDBAL) throws IOException {
 
 		String processStartTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
 
 		List<String> result = new ArrayList<String>();
 		for (SampleInfo sampleInfo : sampleInfos) {
 			String sampleId = sampleInfo.getSampleId();
 			result.add(sampleId);
 
 			// Standardizing affection to CHAR(1), having 1=Unaffected, 2=Affected
 			// HACK TODO someday this should allow other affection values
 			String affection = sampleInfo.getAffectionStr();
 			if (!affection.equals("1") && !affection.equals("2")) {
 				affection = "0";
 			}
 
 			if (!samplesAllreadyInDBAL.contains(sampleId)) {
 				db.insertValuesInTable(cDBGWASpi.SCH_SAMPLES,
 						cDBSamples.T_SAMPLES_INFO,
 						cDBSamples.F_INSERT_SAMPLES_ALLINFO,
 						new Object[]{
 							sampleId, // SampleID (max 32 chars, null=unknown)
 							sampleInfo.getFamilyId(), // FamilyID (max 32 chars, null=unknown)
 							sampleInfo.getFatherId(), // FatherID (max 32 chars, null=unknown)
 							sampleInfo.getMotherId(), // MotherID (max 32 chars, null=unknown)
 							sampleInfo.getSexStr(), // Sex (1=male,2=female,0=unknown)
 							affection, // Affection (1=unaffected,2=affected,0=unknown)
 							sampleInfo.getCategory(), // category
 							sampleInfo.getDisease(), // disease
 							sampleInfo.getPopulation(), // population
 							sampleInfo.getAge(), // age
 							studyId});  // POOL ID
 			}
 		}
 
 		log.info("Samples stored in DB: {}", result.size());
 
 		if (result.size() > 0) {
 			// LOG OPERATION IN STUDY HISTORY
 			StringBuilder operation = new StringBuilder("Start Time: ");
 			operation.append(processStartTime);
 			operation.append("\n");
 			operation.append("Inserted ").append(result.size()).append(" Samples from info file.\n");
 			operation.append("End Time:").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append("\n");
 			org.gwaspi.global.Utils.logBlockInStudyDesc(operation.toString(), studyId);
 		}
 
 		return result;
 	}
 
 	private static int updateSamplesByHashMap(DbManager db, Integer studyId, Collection<SampleInfo> sampleInfos, List<String> samplesAllreadyInDBAL) throws IOException {
 
 		String processStartTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
 
 		int result = 0;
 
 		for (SampleInfo sampleInfo : sampleInfos) {
 			// Standardizing affection to CHAR(1), having 1=Unaffected, 2=Affected
 			// HACK TODO someday this should allow other affection values
 			String affection = sampleInfo.getAffectionStr();
 			if (!affection.equals("1") && !affection.equals("2")) {
 				affection = "0";
 			}
 
 			if (samplesAllreadyInDBAL.contains(sampleInfo.getSampleId())) {
				db.updateTable(cDBGWASpi.SCH_SAMPLES,
 						cDBSamples.T_SAMPLES_INFO,
 						cDBSamples.F_UPDATE_SAMPLES_ALLINFO,
						new Object[]{
							sampleInfo.getFamilyId(), // FamilyID (max 32 chars, null=unknown)
 							sampleInfo.getFatherId(), // FatherID (max 32 chars, null=unknown)
 							sampleInfo.getMotherId(), // MotherID (max 32 chars, null=unknown)
 							sampleInfo.getSexStr(), // Sex (1=male,2=female,0=unknown)
 							affection, // Affection (1=unaffected,2=affected,0=unknown)
 							sampleInfo.getCategory(), // category
 							sampleInfo.getDisease(), // disease
 							sampleInfo.getPopulation(), // population
 							sampleInfo.getAge(), // age
 							studyId.toString(),
 							100}, // status_id_fk = 100 (OK)
						new String[]{cDBSamples.f_SAMPLE_ID, cDBSamples.f_POOL_ID},
						new String[]{sampleInfo.getSampleId(), studyId.toString()});
 				result++;
 			}
 
 		}
 
 		log.info("Updated {} samples", result);
 
 		if (result > 0) {
 			// LOG OPERATION IN STUDY HISTORY
 			StringBuilder operation = new StringBuilder("Start Time: ");
 			operation.append(processStartTime);
 			operation.append("\n");
 			operation.append("Updated ").append(result).append(" Samples from info file.\n");
 			operation.append("End Time:").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append("\n");
 			org.gwaspi.global.Utils.logBlockInStudyDesc(operation.toString(), studyId);
 		}
 
 		return result;
 	}
 }
