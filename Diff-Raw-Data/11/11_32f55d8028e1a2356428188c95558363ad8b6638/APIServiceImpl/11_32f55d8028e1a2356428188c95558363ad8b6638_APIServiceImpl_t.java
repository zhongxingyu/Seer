 package net.canadensys.api.narwhal.service.impl;
 
 import java.util.List;
 
 import net.canadensys.api.narwhal.model.CoordinateAPIResponse;
 import net.canadensys.api.narwhal.model.DateAPIResponse;
 import net.canadensys.api.narwhal.service.APIService;
 import net.canadensys.processor.ProcessingResult;
 import net.canadensys.processor.datetime.DateProcessor;
 import net.canadensys.processor.geography.CoordinatePairProcessor;
 import net.canadensys.processor.geography.DegreeMinuteToDecimalProcessor;
 import net.canadensys.processor.geography.LatLongProcessorHelper;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.stereotype.Service;
 
 /**
  * narwhal-api service layer implementation
  * @author canadensys
  *
  */
 @Service("apiService")
 public class APIServiceImpl implements APIService{
 	//Create processors
 	private CoordinatePairProcessor coordinatePairProcessor = new CoordinatePairProcessor();
 	private DegreeMinuteToDecimalProcessor degreeMinuteToDecimalProcessor = new DegreeMinuteToDecimalProcessor();
 	private DateProcessor dateProcessor = new DateProcessor();
 	
 	@Override
 	public CoordinateAPIResponse processCoordinates(List<String> rawCoordinates, List<String> idList, List<String> fallbackList) {
 		//TODO validate that rawCoordinates and idList have the same size
 		Double[] output = new Double[2];
 		CoordinateAPIResponse apiResponse = new CoordinateAPIResponse();
 		ProcessingResult pr = new ProcessingResult();
 		
 		String[] latLong = null;
 		boolean idFound = false;
 		Double decimalLat, decimalLong;
 		
 		int idx = 0;
 		String id;
 		String rawCoordinate;
 		for(String currCoordinate : rawCoordinates){
 			decimalLat = null;
 			decimalLong = null;
 			rawCoordinate = currCoordinate;
 			//extract lat/long from the pair
 			latLong = coordinatePairProcessor.process(currCoordinate, pr);
 			
 			//if we can't extract it from the rawCoordinates, try the fallback list.
 			if(latLong == null){
 				//clear the error since we are trying another process(...) call
 				pr.clear();
 				latLong = coordinatePairProcessor.process(fallbackList.get(idx), pr);
 				//ignore the id
 				id = null;
				//if currCoordinate equals the entire string provided, the fallback will be null
				if(fallbackList.get(idx) != null){
					rawCoordinate = fallbackList.get(idx);
				}
 			}
 			else{
 				id = idList.get(idx);
 			}
 			
 			if(latLong != null){
 				output = degreeMinuteToDecimalProcessor.process(latLong[LatLongProcessorHelper.LATITUDE_IDX],latLong[LatLongProcessorHelper.LONGITUDE_IDX],pr);				
 				if(output[0] != null && output[1] != null){
 					decimalLat = output[LatLongProcessorHelper.LATITUDE_IDX];
 					decimalLong = output[LatLongProcessorHelper.LONGITUDE_IDX];
 				}
 			}	
 			
 			if(!idFound && (StringUtils.isNotBlank(id))){
 				idFound = true;
 			}
 			apiResponse.addProcessedCoordinate(id, rawCoordinate, decimalLat, decimalLong, pr.getErrorString());
 			
 			//we want to reuse the same object
 			pr.clear();
 			idx++;
 		}
 		apiResponse.setIdProvided(idFound);
 		return apiResponse;
 	}
 	
 	@Override
 	public CoordinateAPIResponse processCoordinates(List<String> rawCoordinates, List<String> idList) {
 		//TODO validate that rawCoordinates and idList have the same size
 		Double[] output = new Double[2];
 		CoordinateAPIResponse apiResponse = new CoordinateAPIResponse();
 		ProcessingResult pr = new ProcessingResult();
 		
 		String[] latLong = null;
 		Double decimalLat, decimalLong;
 		
 		int idx = 0;
 		for(String currCoordinate : rawCoordinates){
 			decimalLat = null;
 			decimalLong = null;
 			//extract lat/long from the pair
 			latLong = coordinatePairProcessor.process(currCoordinate, pr);
 
 			if(latLong != null){
 				output = degreeMinuteToDecimalProcessor.process(latLong[LatLongProcessorHelper.LATITUDE_IDX],latLong[LatLongProcessorHelper.LONGITUDE_IDX],pr);				
 				if(output[0] != null && output[1] != null){
 					decimalLat = output[LatLongProcessorHelper.LATITUDE_IDX];
 					decimalLong = output[LatLongProcessorHelper.LONGITUDE_IDX];
 				}
 			}	
 			
 			apiResponse.addProcessedCoordinate(idList.get(idx), currCoordinate, decimalLat, decimalLong, pr.getErrorString());
 			
 			//we want to reuse the same object
 			pr.clear();
 			idx++;
 		}
 		return apiResponse;
 	}
 	
 	@Override
 	public CoordinateAPIResponse processCoordinates(List<String> rawCoordinates) {
 		Double[] output = new Double[2];
 		CoordinateAPIResponse apiResponse = new CoordinateAPIResponse();
 		ProcessingResult pr = new ProcessingResult();
 		
 		String[] latLong = null;
 		Double decimalLat, decimalLong;
 		
 		for(String currCoordinate : rawCoordinates){
 			decimalLat = null;
 			decimalLong = null;
 			//extract lat/long from the pair
 			latLong = coordinatePairProcessor.process(currCoordinate, pr);
 			
 			if(latLong != null){
 				output = degreeMinuteToDecimalProcessor.process(latLong[LatLongProcessorHelper.LATITUDE_IDX],latLong[LatLongProcessorHelper.LONGITUDE_IDX],pr);				
 				if(output[0] != null && output[1] != null){
 					decimalLat = output[LatLongProcessorHelper.LATITUDE_IDX];
 					decimalLong = output[LatLongProcessorHelper.LONGITUDE_IDX];
 				}
 			}
 			apiResponse.addProcessedCoordinate(null, currCoordinate, decimalLat, decimalLong, pr.getErrorString());
 			//we want to reuse the same object
 			pr.clear();
 		}
 		return apiResponse;
 	}
 	
 	public DateAPIResponse processDates(List<String> rawDates, List<String> idList){
 		DateAPIResponse apiResponse = new DateAPIResponse();
 		ProcessingResult pr = new ProcessingResult();
 		int idx = 0;
 		
 		for(String dateText : rawDates){
 			Integer[] dateParts = dateProcessor.process(dateText,pr);
 			apiResponse.addProcessedDate(idList.get(idx),dateText, dateParts[DateProcessor.YEAR_IDX], dateParts[DateProcessor.MONTH_IDX], dateParts[DateProcessor.DAY_IDX], pr.getErrorString());
 			idx++;
 			//we want to reuse the same object
 			pr.clear();
 		}
 		return apiResponse;
 	}
 }
