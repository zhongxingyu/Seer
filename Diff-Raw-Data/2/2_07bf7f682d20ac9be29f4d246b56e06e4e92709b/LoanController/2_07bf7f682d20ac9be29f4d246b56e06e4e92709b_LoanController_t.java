 package cz.muni.fi.pa165.pujcovnaStroju.web.controller;
 
 import cz.muni.fi.pa165.pujcovnaStroju.web.converter.StringToLoanStateEnumDTOConverter;
 import cz.muni.fi.pa165.pujcovnastroju.converter.UserTypeDTOConverter;
 import cz.muni.fi.pa165.pujcovnastroju.dto.LoanDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.LoanStateEnumDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.MachineDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.SystemUserDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.UserTypeEnumDTO;
 import cz.muni.fi.pa165.pujcovnastroju.entity.Loan;
 import cz.muni.fi.pa165.pujcovnastroju.entity.LoanStateEnum;
 import cz.muni.fi.pa165.pujcovnastroju.entity.SystemUser;
 import cz.muni.fi.pa165.pujcovnastroju.entity.UserTypeEnum;
 import cz.muni.fi.pa165.pujcovnastroju.service.LoanService;
 import cz.muni.fi.pa165.pujcovnastroju.service.MachineService;
 import cz.muni.fi.pa165.pujcovnastroju.service.SystemUserService;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author xguttner
  */
 @Controller
 @RequestMapping("/loan")
 public class LoanController {
     private LoanService loanService;
     private SystemUserService customerService;
     private MachineService machineService;
     
     @Autowired
     public LoanController(LoanService loanService, SystemUserService customerService, 
     		MachineService machineService){
         this.loanService = loanService;
         this.customerService = customerService;
         this.machineService = machineService;
     }
     
     @RequestMapping("")
     public String redirectToList(ModelMap model) {
             return "redirect:/loan/list";
     }
 
     @RequestMapping("/")
     public String redirectToListBackslash(ModelMap model) {
             return "redirect:/loan/list";
     }
     
     @RequestMapping(value = "/list")
     public ModelAndView listLoans(ModelMap model,
         @RequestParam(value = "storeStatus", required = false, defaultValue = "") String storeStatus,
         @RequestParam(value = "errorMessage", required = false, defaultValue = "") String errorMessage
         ) {
 	   List<LoanDTO> list = loanService.getAllLoans(); 
         model.addAttribute("loans", list);
 	model.addAttribute("existingLoans", list);
         model.addAttribute("loanStates", LoanStateEnum.class.getEnumConstants());
         model.addAttribute("customers", customerService.getSystemUsersByParams(null, null, UserTypeDTOConverter.entityToDto(UserTypeEnum.CUSTOMER)));
         model.addAttribute("list", "list of loans");
         model.addAttribute("pageTitle", "lang.listLoansTitle");
         DefaultController.addHeaderFooterInfo(model);
         
         if (storeStatus.equalsIgnoreCase("true")) {
                 model.addAttribute("storeStatus","true");
         }
         if (storeStatus.equalsIgnoreCase("false")) {
                 model.addAttribute("storeStatus","false");
                 model.addAttribute("errorMessage",errorMessage);	
         }
         return new ModelAndView("listLoans", "command", new LoanDTO());
     }
     
     @RequestMapping(value = "/updateForm")
     public ModelAndView printLoanAddForm(ModelMap model) {
         model.addAttribute("loanStates", LoanStateEnum.class.getEnumConstants());
         model.addAttribute("customers", customerService.getSystemUsersByParams(null, null, UserTypeDTOConverter.entityToDto(UserTypeEnum.CUSTOMER)));
 	
         return new ModelAndView("updateLoan", "command", new LoanDTO());
     }
     
     @RequestMapping(value = "/updateForm/{id}")
     public ModelAndView printLoanUpdateForm(@ModelAttribute("loan") LoanDTO loan, ModelMap model, @PathVariable String id) {
         model.addAttribute("loanStates", LoanStateEnum.class.getEnumConstants());
         model.addAttribute("customers", customerService.getSystemUsersByParams(null, null, UserTypeDTOConverter.entityToDto(UserTypeEnum.CUSTOMER)));
 	
 	if (id != null) loan = loanService.read(Long.parseLong(id));
 	model.addAttribute("loan", loan);
 	
         return new ModelAndView("updateLoan", "command", loan);
     }
     
     @RequestMapping(value = "/update", method = RequestMethod.POST)
     public String updateLoan(@ModelAttribute("loan") LoanDTO loan,
                     BindingResult result, ModelMap model,
 		    @RequestParam(value = "machineList", required = false, defaultValue = "") List<String> machineList) {
         boolean stored = false;
         String errorMsg = null;
         try {
 	    SystemUserDTO customer = customerService.read(loan.getCustomer().getId());
 	    loan.setCustomer(customer);
 	    if (machineList != null) {
 		
 		List<MachineDTO> machines = new ArrayList<>();
 		MachineDTO currentMachine = null;
 		for (String machineStr : machineList) {
 		    currentMachine = machineService.read(Long.parseLong(machineStr));
 		    machines.add(currentMachine);
 		}
 		
 		loan.setMachines(machines);
 	    }
 	    
             if (loan.getId() != null) stored = loanService.update(loan) != null;
 	    else stored = loanService.create(loan) != null;
             System.out.println(stored);
         } catch (DataAccessException e) {
             stored = false;
             errorMsg = e.getMessage();
         }
         model.addAttribute("storeStatus", stored);
         if (errorMsg != null) {
             model.addAttribute("errorMessage", errorMsg);
         }
         return "redirect:/loan/list";
     }
     
     @RequestMapping("/detail/{id}")
     public String viewLoan(@PathVariable String id, ModelMap model) {
         LoanDTO loan = null;
         boolean found = false;
 	String errorMsg = null;
         try {
             Long loanID = Long.valueOf(id);
             loan = loanService.read(loanID);
             found = true;
         } catch (DataAccessException | NumberFormatException e) {
 	    errorMsg = e.getMessage();
 	}
         model.addAttribute("loan", loan);
         if (!found) {
             model.addAttribute("id", id);
 	    model.addAttribute("errorMessage", errorMsg);
         }
         return "loanDetail";
     }
     
     @RequestMapping(value = "/delete/{id}")
     public String deleteLoan(@PathVariable String id, ModelMap model) {
         boolean deleted = false;
         String errorMsg = null;
         LoanDTO loanDTO = new LoanDTO();
         try {
             Long loanID = Long.valueOf(id);
             loanDTO = loanService.read(loanID);
             loanService.delete(loanDTO.getId());
             deleted = true;
         } catch (DataAccessException | NumberFormatException
                         | NullPointerException e) {
             // TODO log
             deleted = false;
             errorMsg = e.getMessage();
         }
         model.addAttribute("deleteStatus", deleted);
         if (errorMsg != null) {
             model.addAttribute("errorMessage", errorMsg);
         }
         return "redirect:/loan/list";
     }
     
 	@RequestMapping(value = "/filter", method = RequestMethod.GET, params = "submit")
 	public ModelAndView filterLoans(ModelMap model,
 			@RequestParam(value = "loanTime", required = false) Date from,
 			@RequestParam(value = "returnTime", required = false) Date till,
 			@RequestParam(value = "loanState", required = false) String loanStateStr,
 			@RequestParam(value = "customer", required = false) String customerId,
 			@RequestParam(required = false) String label,
 			@RequestParam(required = false) String description,
 			@RequestParam(required = false) String type) {
 		StringToLoanStateEnumDTOConverter converter = new StringToLoanStateEnumDTOConverter();
 		
 		DefaultController.addHeaderFooterInfo(model);
 		
 		SystemUserDTO customer = null;
 		if (customerId != null) {
 			try {
 				customer = customerService.read(Long.parseLong(customerId));
 			} catch (NumberFormatException e) {
 				customer = null;
 			}
 		}
 		LoanStateEnumDTO loanState = null;
 		if (loanStateStr.equals("--no type--")) loanState = null;
 		else loanState = converter.convert(loanStateStr);
 		
 		model.addAttribute("loans", loanService.getLoansByParams(from, till, loanState, customer, null));
 		model.addAttribute("existingLoans", loanService.getAllLoans());
 		model.addAttribute("loanStates", LoanStateEnum.class.getEnumConstants());
 		model.addAttribute("customers", customerService.getSystemUsersByParams(null, null, UserTypeDTOConverter.entityToDto(UserTypeEnum.CUSTOMER)));
 		model.addAttribute("list", "list of loans");
 		return new ModelAndView("listLoans", "command", new LoanDTO());
 	}
 	
 	@RequestMapping(value = "/filter", method = RequestMethod.GET, params = "void")
 	public String voidFilter(ModelMap model) {
		return "redirect:/loan/list";
 	}
 }
