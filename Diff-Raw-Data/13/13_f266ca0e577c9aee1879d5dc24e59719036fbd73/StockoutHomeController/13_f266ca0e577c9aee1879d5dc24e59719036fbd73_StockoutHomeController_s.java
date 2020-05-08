 /*
  *  This file is part of OpenGov.
  *
  *  OpenGov is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  OpenGov is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with OpenGov.  If not, see <http://www.gnu.org/licenses/>.
  */
 package za.org.opengov.stockout.web.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.codehaus.jackson.map.util.JSONPObject;
 import org.codehaus.jackson.map.util.JSONWrappedObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.MatrixVariable;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestHeader;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import za.org.opengov.stockout.entity.Facility;
 import za.org.opengov.stockout.entity.Stockout;
 import za.org.opengov.stockout.entity.medical.Medicine;
 import za.org.opengov.stockout.entity.medical.MedicineClass;
 import za.org.opengov.stockout.service.FacilityService;
 import za.org.opengov.stockout.service.StockoutService;
 import za.org.opengov.stockout.service.domain.LocationHeirarchy;
 import za.org.opengov.stockout.service.medical.MedicineClassService;
 import za.org.opengov.stockout.service.medical.MedicineService;
 import za.org.opengov.stockout.web.domain.MapMarker;
 import za.org.opengov.stockout.web.domain.GraphData;
 import za.org.opengov.stockout.web.domain.StockoutResult;
 import za.org.opengov.ussd.controller.cm.CMUssdResponse;
 
 
 @Transactional
 @Controller
 @RequestMapping("sows")
 public class StockoutHomeController {
 	
 	private static final Logger LOG = LoggerFactory.getLogger(StockoutHomeController.class);
 	
 	/*public stockoutResult[] getList(Model model){
 		ArrayList<stockoutResult> stockoutNames = new ArrayList<stockoutResult>();
 		
 	    return result;
 	}*/
 	
 	@Autowired
 	private FacilityService facilityService;
 	
 	@Autowired
 	private MedicineClassService medicineClassService;
 	
 	@Autowired
 	private StockoutService stockoutService;
 	
 	@Autowired
 	private MedicineService medicineService;
 	
 	/**Load up initial data to populate dropdown boxes and forms on the home page**/
 	@RequestMapping(value = "/stockouthome",method = RequestMethod.GET)
 	public String getHomePage(Model model){
 		
 		LOG.debug("Stockout web Application Front-End");
 		List<String> provinces = facilityService.listAllProvinces();
 		List<Long> provinceStockouts = new ArrayList<Long>();
 		List<Stockout> allStockouts = stockoutService.getStockoutsForProvince("Western Cape");
 		
 		
 		List<MedicineClass> medicines = medicineClassService.getAll();
 		model.addAttribute("stockouts", allStockouts);
 		model.addAttribute("locations",provinceStockouts);
 		model.addAttribute("provinces", provinces);
 		model.addAttribute("medicines", medicines);
 		
 		
 		return("Stockout Home");
 	}
 	
 	/**loads up list of districts for dynamic selection of province
 	 * called client side(ajax) hence response body is needed**/
 	@RequestMapping(value = "/getdistricts", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody List<String> getDistricts(@RequestParam(value="province") String province){			
 		return(facilityService.listAllDistrictsForProvince(province));		
 	}
 	
 	/**loads up list of towns for dynamic selection of districts
 	 * called client side(ajax) hence response body is needed**/
 	@RequestMapping(value = "/gettowns", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody List<String> getTowns(@RequestParam(value="district") String district){			
 		return(facilityService.listAllTownsForDistrict(district));		
 	}
 	
 	
 	/**Main request to load up all data to be used in graphs on the home page
 	 * loads data for location bar graph,medicine pie chart and google maps markers
 	 * this request is called from client-side(ajax javascript) therefore requires response body
 	 * rather than model attribute*/
 	@RequestMapping(value="/getgraphdata", method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody  GraphData getGraphData(@RequestParam(value="province") String province,
 			@RequestParam(value="district") String district,
 			@RequestParam(value="town") String town,
 			@RequestParam(value="medicineCat") String medicineCat){
 		
 		GraphData graphs = new GraphData();
 		List<String> locations = new ArrayList<String>();
 		List<Long> data = new ArrayList<Long>();
 		List<Long> medData = new ArrayList<Long>();
 		List<String> names = new ArrayList<String>();
 		List<Stockout> allStockouts = new ArrayList<Stockout>();
 		List<StockoutResult> stockResults = new ArrayList<StockoutResult>();
 		List<MapMarker> markers = new ArrayList<MapMarker>();
 		List<Facility> facilities = new ArrayList<Facility>();
 		List<String> allLocations = new ArrayList<String>();
 		
 		if (province.equals("all")){
 			locations = facilityService.listAllProvinces();
 				for (String loc : locations){
 					data.add(facilityService.totalStockoutsForProvince(loc));
 					allStockouts.addAll(stockoutService.getStockoutsForProvince(loc));
 				
 				
 			}
 				
 		}
 
 		else if (district.equals("all")){
 			allLocations = facilityService.listAllDistrictsForProvince(province);
			
 			for (String loc : allLocations){
				if(facilityService.totalStockoutsForDistrict(loc)>0){
					data.add(facilityService.totalStockoutsForDistrict(loc));
 					allStockouts.addAll(stockoutService.getStockoutsForDistrict(loc));
 					locations.add(loc);
				}
 			}
 		} 
 		
 		else if (town.equals("all")) {
 			allLocations = facilityService.listAllTownsForDistrict(district);
 			
 			for (String loc : allLocations){
 				if(facilityService.totalStockoutsForTown(loc)>0){
 					allStockouts.addAll(stockoutService.getStockoutsForTown(loc));
 					data.add(facilityService.totalStockoutsForTown(loc));
 					locations.add(loc);
 				}
 			}
 			
 		} else
 		{
 			facilities= facilityService.listAllFacilitiesForTown(town);
 			allStockouts.addAll(stockoutService.getStockoutsForTown(town));
 			for (Facility fac : facilities){
 				locations.add(fac.getLocalName());
 				long numberOfStockouts = (long) fac.getStockouts().size();
 				data.add(numberOfStockouts);
 			}
 			
 			
 		}
 		
 		List<MedicineClass> medicineClasses = medicineClassService.getAll();
 		
 		if (medicineCat.equals("all")){
 			
 			for (MedicineClass medClass : medicineClasses){
 				names.add(medClass.getUid());
 				long noStockouts = (long)stockoutService.getStockoutsForMedicineClass(medClass).size(); 
 				medData.add(noStockouts);
 				
 				System.out.println(medClass.getUid() + " " + noStockouts);
 			}
 			
 
 			
 			for(Stockout stockout: allStockouts){
 				
 				StockoutResult result = new StockoutResult(stockout);
 				
 				if(stockout.getFacility().getLatitudeDecimalDegress()!= null 
 						&& stockout.getFacility().getLongitudeDecimalDegrees() != null){
 					markers.add(new MapMarker(stockout.getFacility()));
 				}
 				stockResults.add(result);
 			}
 			
 			
 		} 
 		else {
 			Set<Medicine> medicines = medicineClassService.get(medicineCat).getMedicines();
 			for (Medicine med : medicines){
 				
 				long numberOfStockouts = (long)stockoutService.getStockoutsForMedicine(med).size();
 				if (numberOfStockouts>0){
 					names.add(med.getName());
 					medData.add(numberOfStockouts);
 				}
 			}
 			
 			for(Stockout stockout: allStockouts){
 				if (stockout.getProduct().getMedicine().getMedicineClass().getUid().equals(medicineCat)){
 					
 					StockoutResult result = new StockoutResult(stockout);
 					
 					if(stockout.getFacility().getLatitudeDecimalDegress()!= null 
 							&& stockout.getFacility().getLongitudeDecimalDegrees() != null){
 						markers.add(new MapMarker(stockout.getFacility()));
 					}
 					stockResults.add(result);
 				}
 			}
 			
 		}
 		
 		
 		graphs.setLocations(locations);
 		graphs.setLocationStockouts(data);
 		graphs.setMedicines(names);
 		graphs.setMedicineStockouts(medData);
 		graphs.setAllStockouts(stockResults);
 		graphs.setMarkers(markers);
 		
 		return(graphs);
 		
 	}
 	
 }
