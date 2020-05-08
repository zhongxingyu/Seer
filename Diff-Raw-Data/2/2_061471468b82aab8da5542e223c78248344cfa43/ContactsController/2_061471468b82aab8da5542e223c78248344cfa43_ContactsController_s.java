 /*
  * Copyright (c) 2010. Axon Framework
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package nl.enovation.addressbook.jpa.webui.controllers;
 
 import java.util.List;
 
 import javax.validation.Valid;
 
 import nl.enovation.addressbook.jpa.contacts.Contact;
 import nl.enovation.addressbook.jpa.repositories.ContactRepository;
 import nl.enovation.addressbook.jpa.webui.forms.SearchForm;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * @author Jettro Coenradie
  */
 @Controller
 @RequestMapping("/contacts")
 public class ContactsController {
 
     private ContactRepository contactRepository;
 
     private final static Logger logger = LoggerFactory.getLogger(ContactsController.class);
 
     @RequestMapping(value = "{identifier}", method = RequestMethod.GET)
     public String details(@PathVariable Long identifier, Model model) {
         Contact contact = contactRepository.findOne(identifier);
         logger.debug("Received request for command : {getContact(id)}");
         model.addAttribute("contact", contact);
         return "contacts/details";
     }
 
     @RequestMapping(value = "{identifier}/delete", method = RequestMethod.POST)
     public String formDelete(@ModelAttribute("contact") Contact contact, BindingResult bindingResult) {
         if (!bindingResult.hasErrors()) {
             contactRepository.delete(contact);
             logger.debug("Received request for command : {removeContact(contact)}");
             return "redirect:/contacts";
         }
         return "contacts/delete";
     }
 
     @RequestMapping(value = "{identifier}/delete", method = RequestMethod.GET)
     public String formDelete(@PathVariable Long identifier, Model model) {
         Contact contact = contactRepository.findOne(identifier);
         model.addAttribute("contact", contact);
         return "contacts/delete";
     }
 
     @RequestMapping(value = "{identifier}/edit", method = RequestMethod.GET)
     public String formEdit(@PathVariable Long identifier, Model model) {
         Contact contact = contactRepository.findOne(identifier);
         if (contact == null) {
             throw new RuntimeException("contactRepository with ID " + identifier + " could not be found.");
         }
         model.addAttribute("contact", contact);
         return "contacts/edit";
     }
 
     @RequestMapping(value = "{identifier}/edit", method = RequestMethod.POST)
     public String formEditSubmit(@ModelAttribute("contact") @Valid Contact contact, BindingResult bindingResult) {
         logger.debug("Received form submit for contact with identifier {}", contact.getIdentifier());
         if (bindingResult.hasErrors()) {
             return "contacts/edit";
         }
         contactRepository.save(contact);
         logger.debug("Received request for command : {editContact(contact)}");
         return "redirect:/contacts/" + contact.getIdentifier();
     }
 
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String formNew(Model model) {
         Contact attributeValue = new Contact();
         model.addAttribute("contact", attributeValue);
         return "contacts/new";
     }
 
     @RequestMapping(value = "new", method = RequestMethod.POST)
     public String formNewSubmit(@Valid Contact contact, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             return "contacts/new";
         }
         contactRepository.save(contact);
        return "redirect:/contacts";
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String list(Model model) {
         List<Contact> listContacts = contactRepository.findAll();
         SearchForm value = new SearchForm();
         logger.debug("Received request for command : {getContacts}");
         model.addAttribute("contacts", listContacts);
         model.addAttribute("searchValue", value);
         return "contacts/list";
     }
 
     @RequestMapping(value = "search", method = RequestMethod.POST)
     public String search(@ModelAttribute("searchValue") SearchForm value, Model model, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             return "contacts/list";
         }
         List<Contact> listSearchContacts = contactRepository.findByName(value.getSearchValue());
         model.addAttribute("contacts", listSearchContacts);
         return "contacts/list";
     }
 
     public void setContactRepository(ContactRepository contactRepository) {
         this.contactRepository = contactRepository;
     }
 }
