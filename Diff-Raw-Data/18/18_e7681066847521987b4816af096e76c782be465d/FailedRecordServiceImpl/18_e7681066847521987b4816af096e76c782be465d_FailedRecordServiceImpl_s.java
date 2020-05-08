 package eu.europeana.uim.gui.cp.server;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.mongodb.MongoException;
 
 import eu.europeana.corelib.tools.lookuptable.FailedRecord;
 import eu.europeana.corelib.tools.lookuptable.LookupState;
 import eu.europeana.uim.gui.cp.client.services.FailedRecordService;
 import eu.europeana.uim.gui.cp.server.engine.ExpandedOsgiEngine;
 import eu.europeana.uim.gui.cp.shared.validation.FailedRecordDTO;
 import eu.europeana.uim.gui.cp.shared.validation.FailedRecordResultDTO;
 
 public class FailedRecordServiceImpl extends IntegrationServicesProviderServlet
 		implements FailedRecordService {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5292609043023259706L;
 
 	@Override
 	public FailedRecordResultDTO getFailedRecords(String collectionId,
 			int offset, int maxSize) {
 		ExpandedOsgiEngine engine = getEngine();
 		FailedRecordResultDTO result = null;
 		try {
 
 			List<Map<String,String>> failedResults = engine.getDedupService()
 					.getFailedRecords(collectionId);
 
			int max = maxSize;
			if (failedResults != null
					&& failedResults.size() - offset < maxSize) {
				max = failedResults.size() - offset;
			}

			if (max > 0) {
 				List<FailedRecordDTO> failedRecordDTOList = new ArrayList<FailedRecordDTO>();
				for (Map<String,String> failedRecord : failedResults.subList(offset,
						max)) {
 					FailedRecordDTO failedRecordDTO = new FailedRecordDTO();
 					failedRecordDTO.setDate(failedRecord
 							.get("date"));
 					failedRecordDTO.setCollectionId(failedRecord
 							.get("collectionId"));
 					failedRecordDTO.setEuropeanaId(failedRecord
 							.get("europeanaId"));
 					failedRecordDTO.setOriginalId(failedRecord.get("originalId"));
 					failedRecordDTO.setEdm(failedRecord.get("edm"));
 					if (StringUtils.equals(failedRecord.get("lookupState"),
 							LookupState.IDENTICAL.toString())) {
 						failedRecordDTO
 								.setLookupState("The record was encountered twice");
 					} else if (StringUtils.equals(failedRecord.get("lookupState"),
 							LookupState.DUPLICATE_IDENTIFIER_ACROSS_COLLECTIONS.toString())) {
 						failedRecordDTO
 								.setLookupState("The Europeana Identifier of the record was encountered twice for different records among split datasets");
 					} 
 					else if (StringUtils.equals(failedRecord.get("lookupState"),
 							LookupState.DUPLICATE_RECORD_ACROSS_COLLECTIONS.toString())) {
 						failedRecordDTO
 								.setLookupState("The record was encountered twice for different records among split datasets");
 					}
 					else if (StringUtils.equals(failedRecord.get("lookupState"),
 							LookupState.INCOMPATIBLE_XML_CONTENT.toString())) {
 						failedRecordDTO
 								.setLookupState("The imported record has a deprecated XML structure (MINT schema not aligned with internal schema used by UIM).");
 					}
 					else if (StringUtils.equals(failedRecord.get("lookupState"),
 							LookupState.SYSTEM_ERROR.toString())) {
 						failedRecordDTO
 								.setLookupState("A system error occured during the import of this file).");
 					}
 					else {
 						failedRecordDTO
 								.setLookupState("The Europeana Identifier of the record was encountered twice for different records in the same dataset");
 					}
 					failedRecordDTOList.add(failedRecordDTO);
 				}
 
 				result = new FailedRecordResultDTO(failedRecordDTOList,
 						failedResults.size());
 			}
 		} catch (MongoException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 }
