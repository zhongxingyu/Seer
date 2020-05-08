 package com.excilys.computerdatabase.controller;
 
 import com.excilys.computerdatabase.form.ComputerForm;
 import com.excilys.computerdatabase.model.Computer;
 import com.excilys.computerdatabase.queryresults.ComputerAndCompanies;
 import com.excilys.computerdatabase.queryresults.ComputersAndTotalNumber;
 import com.excilys.computerdatabase.service.ComputerDatabaseService;
 import com.excilys.computerdatabase.utils.C;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: gplassard
  * Date: 05/06/13
  * Time: 14:38
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/")
 public class ComputerController {
 
     @Autowired
     private ComputerDatabaseService computerDatabaseService;
 
 
     @RequestMapping("/computers")
     public String index(ModelMap model,
                                @ModelAttribute("alertMessage") String alertMessage,
                                @RequestParam(value="s",defaultValue = "2") int sortedColumnNumber,
                                @RequestParam(value="f",defaultValue = "") String research,
                                @RequestParam(value="p",defaultValue = "1") int currentPage) {
 
         if (Math.abs(sortedColumnNumber) < 2 || Math.abs(sortedColumnNumber) > 5) {
             sortedColumnNumber = 2;
         }
 
         int firstComputerIndice = (currentPage - 1) * C.COMPUTERS_PER_PAGE;
         int lastComputerIndice = firstComputerIndice + C.COMPUTERS_PER_PAGE;
 
         ComputersAndTotalNumber queryResult = computerDatabaseService.listOfComputers(research, sortedColumnNumber, firstComputerIndice, lastComputerIndice);
         int numberOfMatchingComputers = queryResult.getNumberOfMatchingComputers();
         List<Computer> computers = queryResult.getMatchingComputers();
         int maxPage = (int) Math.ceil((1.0 * numberOfMatchingComputers) / C.COMPUTERS_PER_PAGE);
        if (lastComputerIndice > numberOfMatchingComputers){
            lastComputerIndice = numberOfMatchingComputers;
        }
 
         model.addAttribute("alertMessage", alertMessage);
         model.addAttribute("sorting", sortedColumnNumber);
         model.addAttribute("research", research);
         model.addAttribute("currentPage", currentPage);
         model.addAttribute("maxPage", maxPage);
         model.addAttribute("firstComputerIndice", firstComputerIndice + 1);
         model.addAttribute("lastComputerIndice", lastComputerIndice);
         model.addAttribute("totalComputersFound", numberOfMatchingComputers);
         model.addAttribute("computers", computers);
         return "computers";
     }
 
     @RequestMapping(value = "/computers/edit", method = RequestMethod.GET)
     public String editComputer(ModelMap model, @RequestParam(value="id", defaultValue = "0") long id){
         ComputerAndCompanies queryResult = computerDatabaseService.computerByIdAndCompanies(id);
         ComputerForm form = new ComputerForm(queryResult.getComputer());
 
         model.addAttribute("mode", "edit");
         model.addAttribute("fieldValues", form.getFieldValues());
         model.addAttribute("companies", queryResult.getCompanies());
         return "computer";
     }
 
     @RequestMapping(value = "/computers/edit", method = RequestMethod.POST)
     public String saveEdit(ModelMap model, final RedirectAttributes redirectAttributes,
                                             @RequestParam(value="id", defaultValue = "0") long id,
                                             @ModelAttribute(value="name") String name,
                                             @ModelAttribute(value="introduced") String introduced,
                                             @ModelAttribute(value="discontinued") String discontinued,
                                             @ModelAttribute(value="company") String companyId){
 
         ComputerForm form = new ComputerForm(name,introduced,discontinued,companyId);
 
         if (form.isValid()) {
             Computer computer = form.getComputer();
             computer.setId(id);
             boolean succesfull = computerDatabaseService.updateComputerAndSetCompany(computer, form.getCompanyId());
             if (succesfull) {
                 redirectAttributes.addFlashAttribute("alertMessage", "Computer " + computer.getName() + " modified successfully");
             } else {
                 redirectAttributes.addFlashAttribute("alertMessage", "There has been a problem while updating " + computer.getName());
             }
             return "redirect:../computers";
         } else {
             model.addAttribute("mode", "edit");
             model.addAttribute("companies", computerDatabaseService.allCompanies());
             model.addAttribute("errorMessages", form.getErrorMessages());
             model.addAttribute("fieldValues", form.getFieldValues());
             return "computer";
         }
     }
 
     @RequestMapping(value ="/computers/new", method = RequestMethod.GET)
     public String newComputer(ModelMap model){
         model.addAttribute("mode", "new");
         model.addAttribute("companies", computerDatabaseService.allCompanies());
         return "computer";
     }
 
     @RequestMapping(value ="/computers/new", method = RequestMethod.POST)
     public String saveNewComputer(ModelMap model,final RedirectAttributes redirectAttributes,
                                   @ModelAttribute(value="name") String name,
                                   @ModelAttribute(value="introduced") String introduced,
                                   @ModelAttribute(value="discontinued") String discontinued,
                                   @ModelAttribute(value="company") String companyId){
 
         ComputerForm form = new ComputerForm(name,introduced,discontinued,companyId);
 
         if (form.isValid()) {
             redirectAttributes.addFlashAttribute("alertMessage", "Computer " + form.getComputer().getName() + " added successfully");
             computerDatabaseService.createComputerAndSetCompany(form.getComputer(), form.getCompanyId());
             return "redirect:../computers";
         } else {
             model.addAttribute("mode", "new");
             model.addAttribute("companies", computerDatabaseService.allCompanies());
             model.addAttribute("errorMessages", form.getErrorMessages());
             model.addAttribute("fieldValues", form.getFieldValues());
             return "computer";
         }
     }
 
     @RequestMapping(value ="/computers/delete")
     public String deleteComputer(final RedirectAttributes redirectAttributes,@RequestParam(value="id", defaultValue = "0") long computerId){
         boolean succesfull = computerDatabaseService.deleteComputerById(computerId);
         if (succesfull) {
             redirectAttributes.addFlashAttribute("alertMessage", "Computer deleted successfully");
         } else {
             redirectAttributes.addFlashAttribute("alertMessage", "There has been a problem while deleting the computer");
         }
         return "redirect:../computers";
     }
 }
