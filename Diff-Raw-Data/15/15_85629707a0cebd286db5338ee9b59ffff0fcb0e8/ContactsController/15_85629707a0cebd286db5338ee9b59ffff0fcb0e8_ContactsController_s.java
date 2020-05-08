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
 
 package nl.enovation.addressbook.cqrs.webui.contacts;
 
 import java.util.List;
 
 import javax.validation.Valid;
 
 import nl.enovation.addressbook.cqrs.command.AbstractContactCrudCommand;
 import nl.enovation.addressbook.cqrs.command.CreateContactCommand;
 import nl.enovation.addressbook.cqrs.command.RemoveContactCommand;
 import nl.enovation.addressbook.cqrs.command.UpdateContactCommand;
 import nl.enovation.addressbook.cqrs.query.ContactEntry;
 import nl.enovation.addressbook.cqrs.query.repositories.ContactQueryRepositoryImpl;
 
 import org.axonframework.commandhandling.CommandBus;
 import org.axonframework.domain.AggregateIdentifier;
 import org.axonframework.domain.StringAggregateIdentifier;
 import org.axonframework.domain.UUIDAggregateIdentifier;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * @author Jettro Coenradie, Yorick Holkamp
  */
 @Controller
 @RequestMapping("/contacts")
 public class ContactsController {
     private final static Logger logger = LoggerFactory.getLogger(ContactsController.class);
 
     @Autowired
     private ContactQueryRepositoryImpl contactRepositoryImpl;
 
     private CommandBus commandBus;
 
     @Autowired
    public ContactsController(ContactQueryRepositoryImpl contactRepository, CommandBus commandBus) {
         contactRepositoryImpl = contactRepository;
         this.commandBus = commandBus;
     }
 
     @RequestMapping(value = "{identifier}", method = RequestMethod.GET)
     public String details(@PathVariable String identifier, Model model) {
         ContactEntry contactEntry = contactRepositoryImpl.findOne(identifier);
         model.addAttribute("identifier", identifier);
         model.addAttribute("contact", contactEntry);
         return "contacts/details";
     }
 
     @RequestMapping(value = "{identifier}/delete", method = RequestMethod.POST)
     public String formDelete(@ModelAttribute("contact") ContactEntry contact, BindingResult bindingResult) {
         if (!bindingResult.hasErrors()) {
             AggregateIdentifier identifier = new StringAggregateIdentifier(contact.getIdentifier());
             RemoveContactCommand command = new RemoveContactCommand(identifier);
 
             logger.debug("Dispatching command with name : {}", command.toString());
             commandBus.dispatch(command);
 
             return "redirect:/contacts";
         }
         return "contacts/delete";
     }
 
     @RequestMapping(value = "{identifier}/delete", method = RequestMethod.GET)
     public String formDelete(@PathVariable String identifier, Model model) {
         ContactEntry contactEntry = contactRepositoryImpl.findOne(identifier);
         model.addAttribute("contact", contactEntry);
         return "contacts/delete";
     }
 
     @RequestMapping(value = "{identifier}/edit", method = RequestMethod.GET)
     public String formEdit(@PathVariable String identifier, Model model) {
         ContactEntry contact = contactRepositoryImpl.findOne(identifier);
         if (contact == null) {
             throw new RuntimeException("contactRepositoryImpl with ID " + identifier + " could not be found.");
         }
         model.addAttribute("contact", contact);
         return "contacts/edit";
     }
 
     @RequestMapping(value = "{identifier}/edit", method = RequestMethod.POST)
     public String formEditSubmit(@ModelAttribute("contact") @Valid ContactEntry contact, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             return "contacts/edit";
         }
 
         AggregateIdentifier identifier = new StringAggregateIdentifier(contact.getIdentifier());
         AbstractContactCrudCommand command = new UpdateContactCommand(identifier, contact);
 
         logger.debug("Dispatching command with name : {}", command.toString());
         commandBus.dispatch(command);
 
         return "redirect:/contacts";
     }
 
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String formNew(Model model) {
         ContactEntry attributeValue = new ContactEntry();
         model.addAttribute("contact", attributeValue);
         return "contacts/new";
     }
 
     @RequestMapping(value = "new", method = RequestMethod.POST)
     public String formNewSubmit(@Valid ContactEntry contact, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             return "contacts/new";
         }
 
         AbstractContactCrudCommand command = new CreateContactCommand(new UUIDAggregateIdentifier(), contact);
 
         logger.debug("Dispatching command with name : {}", command.toString());
         commandBus.dispatch(command);
 
         return "redirect:/contacts";
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String list(Model model) {
         SearchForm searchForm = new SearchForm();
         model.addAttribute("contacts", contactRepositoryImpl.findAll(ContactEntry.class));
         model.addAttribute("searchForm", searchForm);
         return "contacts/list";
     }
 
     @RequestMapping(value = "search", method = RequestMethod.POST)
     public String search(@ModelAttribute("searchForm") SearchForm searchForm, Model model, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             return "contacts/list";
         }
         List<ContactEntry> contacts = contactRepositoryImpl.searchForNames(searchForm.getSearchValue());
         model.addAttribute("contacts", contacts);
         return "contacts/list";
     }
 }
