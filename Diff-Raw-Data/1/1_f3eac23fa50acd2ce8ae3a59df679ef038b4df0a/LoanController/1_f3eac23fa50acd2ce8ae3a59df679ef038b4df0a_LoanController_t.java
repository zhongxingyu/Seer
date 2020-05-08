 package cz.muni.fi.pa165.pujcovnaStroju.web.controller;
 
 import cz.muni.fi.pa165.pujcovnastroju.converter.UserTypeDTOConverter;
 import cz.muni.fi.pa165.pujcovnastroju.dto.LoanDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.MachineDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.SystemUserDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.UserTypeEnumDTO;
 import cz.muni.fi.pa165.pujcovnastroju.entity.LoanStateEnum;
 import cz.muni.fi.pa165.pujcovnastroju.entity.SystemUser;
 import cz.muni.fi.pa165.pujcovnastroju.entity.UserTypeEnum;
 import cz.muni.fi.pa165.pujcovnastroju.service.LoanService;
 import cz.muni.fi.pa165.pujcovnastroju.service.MachineService;
 import cz.muni.fi.pa165.pujcovnastroju.service.SystemUserService;
 
 import java.util.ArrayList;
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
         model.addAttribute("loans", loanService.getAllLoans());
         model.addAttribute("loanStates", LoanStateEnum.class.getEnumConstants());
         model.addAttribute("customers", customerService.getSystemUsersByParams(null, null, UserTypeDTOConverter.entityToDto(UserTypeEnum.CUSTOMER)));
         model.addAttribute("list", "list of loans");
         model.addAttribute("pageTitle", "lang.listLoansTitle");
         DefaultController.addHeaderFooterInfo(model);
         //model.addAttribute("machines",machineService.getMachineDTOsByParams(null, null, null, null, null));
         
         if (storeStatus.equalsIgnoreCase("true")) {
                 model.addAttribute("storeStatus","true");
         }
         if (storeStatus.equalsIgnoreCase("false")) {
                 model.addAttribute("storeStatus","false");
                 model.addAttribute("errorMessage",errorMessage);	
         }
         return new ModelAndView("listLoans", "command", new LoanDTO());
     }
     
     @RequestMapping(value = "/add", method = RequestMethod.POST)
     public String addLoan(@ModelAttribute("loan") LoanDTO loan,
                     BindingResult result, ModelMap model,
 		    @RequestParam(value = "machineList", required = false, defaultValue = "") List<String> machineList) {
         boolean stored = false;
         String errorMsg = null;
         try {
 	    SystemUserDTO customer = customerService.read(loan.getCustomer().getId());
 	    loan.setCustomer(customer);
 	    if (machineList != null) {
 		System.out.println("non-empty list");
 		List<MachineDTO> machines = new ArrayList<>();
 		MachineDTO currentMachine = null;
 		for (String machineStr : machineList) {
 		    System.out.println(machineStr);
 		    currentMachine = machineService.read(Long.parseLong(machineStr));
 		    machines.add(currentMachine);
 		}
 		loan.setMachines(machines);
 	    }
 	    else {
 		System.out.println("empty list");
 	    }
 	    
 //	    MachineDTO machine = machineService.read(1L);
 //	    loan.setMachine(machine);
 //	    loan.setCustomer(customer); 
 //	    List<LoanDTO> loansList = new ArrayList<>();
 //	    loansList.add(loan);
 //	    customer.setLoans(loansList);
 //           
 //            SystemUserDTO customerUpdated = customerService.update(customer);
 //            System.out.println(customerUpdated);
 //            loan.setCustomer(customerUpdated);
 //            System.out.println(customerUpdated);
             stored = loanService.create(loan) != null;
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
     
     @RequestMapping(value = "/new/add", method = RequestMethod.POST)
     public String addNewLoan(@ModelAttribute("loan") LoanDTO loan, 
     		
                     BindingResult result, ModelMap model, @RequestParam(value = "machineList", required = false, defaultValue = "") List<String> machineList) {
     	System.out.println("xxx");
 //    	System.out.println(machineID);
     	return addLoan(loan, result, model, machineList);
     }
     
     
     @RequestMapping("/detail/{id}")
     public String viewLoan(@PathVariable String id, ModelMap model) {
         DefaultController.addHeaderFooterInfo(model);
         model.addAttribute("pageTitle", "lang.detailLoanTitle");
         LoanDTO loan = null;
         boolean found = false;
         try {
             Long loanID = Long.valueOf(id);
             loan = loanService.read(loanID);
             found = true;
         } catch (DataAccessException | NumberFormatException e) {
             // TODO log
         }
         model.addAttribute("loan", loan);
         if (!found) {
             model.addAttribute("id", id);
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
     
     @RequestMapping(value = "/update/{id}")
     public ModelAndView updateLoan(@PathVariable String id, ModelMap model) {
         DefaultController.addHeaderFooterInfo(model);
         model.addAttribute("pageTitle", "lang.updateLoanTitle");
         LoanDTO loan = null;
         boolean found = false;
         try {
             Long loanID = Long.valueOf(id);
             loan = loanService.read(loanID);
             found = true;
         } catch (DataAccessException | NumberFormatException e) {
             // TODO log
         }
         
         // prevent the actual type of the user to show in the list twice
         List<LoanStateEnum> enums = new LinkedList<>();
         for(LoanStateEnum enum1 : LoanStateEnum.class.getEnumConstants()){
             if (!enum1.toString().equals(loan.getLoanState().getTypeLabel())){
                 enums.add(enum1);
             }
         }
         LoanStateEnum[] loanStates = (LoanStateEnum[]) enums.toArray(new LoanStateEnum[enums.size()]);
         
        model.addAttribute("customers", customerService.getSystemUsersByParams(null, null, UserTypeDTOConverter.entityToDto(UserTypeEnum.CUSTOMER)));
         model.addAttribute("loanStates", loanStates);
         model.addAttribute("loan", loan);
         if (!found) {
             model.addAttribute("id", id);
         }
         return new ModelAndView("updateLoan", "command", new LoanDTO());
     }
     
     @RequestMapping(value = "/update/update", method = RequestMethod.POST)
     public String editLoan(@ModelAttribute("loan") LoanDTO loan,
                     BindingResult result, ModelMap model) {
         boolean updated = false;
         String errorMsg = null;
         try {
             updated = loanService.update(loan) != null;
         } catch (DataAccessException e) {
             updated = false;
             errorMsg = e.getMessage();
         }
         model.addAttribute("updateStatus", updated);
         if (errorMsg != null) {
             model.addAttribute("errorMessage", errorMsg);
         }
         return "redirect:/loan/list";
     }
     
     @RequestMapping(value = "/new/{id}")
     public ModelAndView createLoan(@PathVariable String id, ModelMap model) {
         DefaultController.addHeaderFooterInfo(model);
         model.addAttribute("pageTitle", "lang.updateLoanTitle");
         LoanDTO loan = new LoanDTO();
         MachineDTO machine = null;
 //        boolean found = false;
 //        try {
 //            Long loanID = Long.valueOf(id);
 //            loan = loanService.read(loanID);
 //            found = true;
 //        } catch (DataAccessException | NumberFormatException e) {
 //            // TODO log
 //        }
         try {
 	        Long machineID = Long.valueOf(id);
 	        machine = machineService.read(machineID);
     	} catch (DataAccessException | NumberFormatException e) {
         // TODO log
     	}
         
 //        LinkedList<MachineDTO> machines = new LinkedList<>();
 //        machines.add(machine);
         //loan.setMachine(machine);
         model.addAttribute("loanStates", LoanStateEnum.class.getEnumConstants());
         model.addAttribute("customers", customerService.getSystemUsersByParams(null, null, null));
 //        model.addAttribute("loan", loan);
         model.addAttribute("machine", machine);
         System.out.println(machine);
 
         return new ModelAndView("newLoan", "command", loan);
     }
 }
