 package cs.ut.domain.rest.controller;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import cs.ut.domain.HireRequestStatus;
 import cs.ut.domain.Plant;
 import cs.ut.domain.rest.PlantResource;
 import cs.ut.domain.rest.PlantResourceAssembler;
 import cs.ut.domain.rest.PlantResourceList;
 import cs.ut.repository.PlantRepository;
 
 @Controller
 @RequestMapping("/rest/plant")
 public class PlantRestController {
 	
 	@Autowired
 	PlantRepository repository;
 	
 	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
 	public ResponseEntity<PlantResource> getPlant(@PathVariable("id") Long id){
 		Plant p = Plant.findPlant(id);
 		PlantResourceAssembler assembler = new PlantResourceAssembler();
 		PlantResource res = assembler.getPlantResource(p);
 		ResponseEntity<PlantResource> response = new ResponseEntity<>(res, HttpStatus.OK);
 		return response;
 	}
 	
 	
 	@RequestMapping(method = RequestMethod.GET, value = "/")
 	public ResponseEntity<PlantResourceList> getPlantList(@RequestParam(required = false, value = "startDate") String startDateString, @RequestParam(required = false, value = "endDate") String endDateString){
 		List<Plant> plantList;
 		
 		if(startDateString != null && endDateString != null){
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
 			
 			Date startD = null;
 			Date endD = null;
 			try {
 				startD = formatter.parse(startDateString);
 				endD = formatter.parse(endDateString);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 	    	Date start = new DateTime(startD).toDateMidnight().toDate();
 	    	Date end = new DateTime(endD).toDateMidnight().toDate();
 			plantList = repository.findByDateRange(start, end, HireRequestStatus.PENDING_CONFIRMATION);
 		} else{
 			plantList = repository.findAll();
 		}
 		
 		PlantResourceAssembler assembler = new PlantResourceAssembler();
 		PlantResourceList resList = assembler.getPlantResourceList(plantList);
 		ResponseEntity<PlantResourceList> response = new ResponseEntity<>(resList, HttpStatus.OK);
 		return response;
 	}
 	
 }
