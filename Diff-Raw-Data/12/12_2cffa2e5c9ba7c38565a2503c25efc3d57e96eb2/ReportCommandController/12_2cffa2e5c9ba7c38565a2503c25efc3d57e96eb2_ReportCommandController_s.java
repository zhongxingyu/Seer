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
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import za.org.opengov.stockout.entity.Facility;
 import za.org.opengov.stockout.entity.Stockout;
 import za.org.opengov.stockout.entity.StockoutReport;
 import za.org.opengov.stockout.entity.Subject;
 import za.org.opengov.stockout.entity.medical.Medicine;
 import za.org.opengov.stockout.entity.medical.MedicineClass;
 import za.org.opengov.stockout.entity.medical.Product;
 import za.org.opengov.stockout.service.FacilityService;
 import za.org.opengov.stockout.service.StockoutReportService;
 import za.org.opengov.stockout.service.medical.MedicineClassService;
 import za.org.opengov.stockout.service.medical.ProductService;
 import za.org.opengov.stockout.web.domain.PublicStockoutReport;
 
 /** Controls all request for the report page**/
 @Controller
 @RequestMapping(value="sows")
 public class ReportCommandController {
 
 	private static final Logger LOG = LoggerFactory.getLogger(ReportCommandController.class);
 	
 	@Autowired
 	private FacilityService facilityService;
 	
 	@Autowired
 	private MedicineClassService medicineClassService;
 	
 	@Autowired
 	private StockoutReportService stockoutReportService;
 	
 	@Autowired
 	private ProductService productService;
 	
 	/** Constructs the Reports Page with all the neccessary Model attributes to pre-load
 	 * various form elements data**/
 	@RequestMapping(value="/reportstockouts",method=RequestMethod.GET)
 	public String getReportPage(Model model){
 		
 		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
 		   
 		Date date = new Date();
 		
 		List<String> provinces = facilityService.listAllProvinces();
 		List<MedicineClass> medicineClasses = medicineClassService.getMedicineClassesEagerFetch();
 		model.addAttribute("medicineCategories", medicineClasses);
 		model.addAttribute("date",dateFormat.format(date));
 		model.addAttribute("provinces",provinces);
 		
 		return("Report_Page");
 	}
 	
 	/**handles client-side requests for facility data(usually from ajax javascript)**/
 	@RequestMapping(value = "/getfacility", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody List<String> getFacilty(@RequestParam(value="province") String province){
 		List<Facility> allFacilities = facilityService.listAllFacilitiesForProvince(province);
 		List<String> facilityNames = new ArrayList<String>();
 		for(Facility fac : allFacilities){
 			facilityNames.add(fac.getLocalName() + " " + fac.getFacilityType().getReadable());		
 		}
 		
 		return(facilityNames);		
 	}
 	
 	/**handles client-side requests for medicine data(usually from ajax javascript)
 	 * returns a list of product names for populating autocomplete form**/
 	@RequestMapping(value = "/getmedicines", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody List<String> getMedicines(@RequestParam(value="medicineClassIndex") int medicineClassIndex){
 		
 		List<String> productDescription = new ArrayList<String>();
 		List<MedicineClass> medicineClasses = medicineClassService.getMedicineClassesEagerFetch();
 		Set<Medicine> medicineNames = medicineClasses.get(medicineClassIndex).getMedicines();
 		
 		for(Medicine med : medicineNames){
 			for (Product prod: med.getProducts()){
 				
 				productDescription.add(prod.getName() + " " + prod.getDescription());
 				
 			}
 		}
 		
 		return(productDescription);		
 	}
 	
 	@ModelAttribute("publicStockoutReport")
     private PublicStockoutReport getPublicStockoutReport() {
         return new PublicStockoutReport();
     }
 
 	/**Post request that handles the processing of form object(publicStockoutReport)
 	 * repeats data for /reportStockouts request if form detects errors
 	 * valid annotation ensures fields in publicStockoutReport have met requirements before being submitted.
 	 * stockoutreport is processed and submitted from form data.
 	 * success message is stored as a redirected attribute and sent to reportStockout request
 	 * upon completion of stockout report submission*/
 	@RequestMapping(value="/processform", method = RequestMethod.POST)
     public String reportStockout(@Valid @ModelAttribute PublicStockoutReport publicStockoutReport, BindingResult result, RedirectAttributes redirectAttrs, Model model) {
         
 		if (result.hasErrors()) {
 			
			DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");   
 			Date date = new Date();			
 			List<String> provinces = facilityService.listAllProvinces();
 			List<MedicineClass> medicineClasses = medicineClassService.getMedicineClassesEagerFetch();
 			model.addAttribute("medicineCategories", medicineClasses);
 			model.addAttribute("date",dateFormat.format(date));
 			model.addAttribute("provinces",provinces);
             return "Report_Page";
         }
 
 		System.out.println(publicStockoutReport.getName());
 		System.out.println(publicStockoutReport.getDesignation());
 		System.out.println(publicStockoutReport.getDateOfOccurence());
 		System.out.println(publicStockoutReport.getCellNumber());
 		System.out.println(publicStockoutReport.getEmailAddress());
 		System.out.println(publicStockoutReport.getReasonForOccurrence());
 		System.out.println(publicStockoutReport.getSelectedMedicines());
 		System.out.println(publicStockoutReport.getFacilityName());
 		
 		Subject reporter = new Subject();
 		reporter.setContactNumber(publicStockoutReport.getCellNumber());
 		reporter.setDesignation(publicStockoutReport.getDesignation());
 		reporter.setEmail(publicStockoutReport.getEmailAddress());
 		
 		String[] names = publicStockoutReport.getName().split(" ");
 		
 		for(int k=0;k<names.length-1;k++){
 			reporter.setName(names[k] + " ");
 		}
 		
 		if(names.length>1){
 			reporter.setSurname(names[names.length-1]);
 		}
 		
 
 		Facility facility = facilityService.getClosestMatch(publicStockoutReport.getFacilityName());
 		
 		String[] medicine = publicStockoutReport.getSelectedMedicines().split(","); 
 		
 		for(String med : medicine){
 			
 			Product product = productService.getClosestMatch(med);
 			stockoutReportService.submitStockoutReport(product.getUid(),facility.getUid(),
 					reporter, publicStockoutReport.getReasonForOccurrence(), true);
 		}
 		
 
         redirectAttrs.addFlashAttribute("message","Stockout Reported");
         redirectAttrs.addFlashAttribute("Name",publicStockoutReport.getName());
         redirectAttrs.addFlashAttribute("Medicine",publicStockoutReport.getSelectedMedicines());
         
 
        return "redirect:/reportstockouts";
     }
 	
 	
 	
 }
