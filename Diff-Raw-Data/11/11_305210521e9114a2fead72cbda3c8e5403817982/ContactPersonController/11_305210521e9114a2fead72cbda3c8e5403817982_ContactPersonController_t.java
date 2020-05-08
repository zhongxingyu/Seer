 package no.dusken.annonseweb.control;
 
 import no.dusken.annonseweb.models.AnnonsePerson;
 import no.dusken.annonseweb.models.ContactPerson;
import no.dusken.annonseweb.models.Customer;
 import no.dusken.annonseweb.service.ContactPersonService;
import no.dusken.annonseweb.service.CustomerService;
 import no.dusken.common.editor.BindByIdEditor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 
 import javax.validation.Valid;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 
 @Controller
 @RequestMapping("/contact/person")
 public class ContactPersonController {
 
     @Autowired
     private ContactPersonService contactPersonService;
 
     @Autowired
     private AnnonsePersonController annonsePersonController;
 
    @Autowired
    private CustomerService customerService;

     @RequestMapping("")
     public String viewContactsHome(){
         // TODO
         return "contact/home";
     }
 
     @RequestMapping("/active")
     public String allActive(Model model){
         List<ContactPerson> contactPersonList = contactPersonService.findAll();
         for(ContactPerson contactPerson: contactPersonList){
             if(!contactPerson.getActive()) contactPersonList.remove(contactPerson);
         }
         model.addAttribute("contactPersonList", contactPersonList);
         return "contact/person/all";
     }
 
     @RequestMapping("/all")
     public String all(Model model){
         List<ContactPerson> contactPersonList = contactPersonService.findAll();
         model.addAttribute("contactPersonList", contactPersonList);
         return "contact/person/all";
     }
 
     @RequestMapping(value="/{contactPerson}")
     public String viewContactPerson(@PathVariable ContactPerson contactPerson, Model model){
         model.addAttribute("contact", contactPerson);
         return "contact/person/person";
     }
 
     @RequestMapping("/new")
     public String newContactPerson(Model model){
         return viewEdit(new ContactPerson(), model);
     }
 
     @RequestMapping("/edit/{contactPerson}")
     public String viewEdit(@PathVariable ContactPerson contactPerson, Model model) {
         model.addAttribute("contactPerson", contactPerson);
        model.addAttribute("customerList", customerService.findAll());
         return "contact/person/edit";
     }
 
     @RequestMapping(value = "/save",  method = RequestMethod.POST)
     public String storeNewContactPerson(@Valid @ModelAttribute ContactPerson contactPerson) {
         AnnonsePerson usr = annonsePersonController.getLoggedInUser();
         contactPerson.setCreatedDate(Calendar.getInstance());
         contactPerson.setLastContactedUser(usr);
         contactPerson.setCreatedUser(usr);
         contactPerson.setLastContactedTime(Calendar.getInstance());
         contactPersonService.saveAndFlush(contactPerson);
         return "contact/person/" + contactPerson.getId();
     }
 
     @RequestMapping(value = "/save/{pathContactPerson}",  method = RequestMethod.POST)
     public String editContactPerson(@Valid @ModelAttribute ContactPerson contactPerson,
                                     @PathVariable ContactPerson pathContactPerson) {
         pathContactPerson.copyInformationFrom(contactPerson);
         return "contact/person/" + pathContactPerson.getId();
     }
 
     @RequestMapping("/emailList")
     public String viewEmailsForCustomersContactPersons(Model model){
         List<String> emailList = new ArrayList<String>();
         for(ContactPerson contactPerson: contactPersonService.findAll()){
             emailList.add(contactPerson.getEmail());
         }
         model.addAttribute("emailList", emailList);
         return "contact/person/emailList";
     }
 
     @InitBinder
     public void initBinder(WebDataBinder binder){
         binder.registerCustomEditor(ContactPerson.class, new BindByIdEditor(contactPersonService));
        binder.registerCustomEditor(Customer.class, new BindByIdEditor(customerService));
     }
 }
