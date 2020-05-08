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
 
 import javax.validation.Valid;
 
 import nl.enovation.addressbook.cqrs.command.CreatePhoneNumberCommand;
 import nl.enovation.addressbook.cqrs.command.RemovePhoneNumberCommand;
 import nl.enovation.addressbook.cqrs.pojo.PhoneNumberEntry;
 import nl.enovation.addressbook.cqrs.query.ContactEntry;
 import nl.enovation.addressbook.cqrs.query.repositories.ContactQueryRepositoryImpl;
 
 import org.axonframework.commandhandling.CommandBus;
 import org.axonframework.domain.StringAggregateIdentifier;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.Assert;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * @author Yorick Holkamp
  */
 @Controller
 @RequestMapping("/contacts")
 public class PhoneNumberController {
     private final static Logger logger = LoggerFactory.getLogger(PhoneNumberController.class);
 
     private ContactQueryRepositoryImpl contactRepositoryImpl;
 
     private CommandBus commandBus;
 
     @Autowired
    public PhoneNumberController(@Qualifier("contactQueryRepository")ContactQueryRepositoryImpl contactRepository, CommandBus commandBus) {
         contactRepositoryImpl = contactRepository;
         this.commandBus = commandBus;
     }
 
     @RequestMapping(value = "{contactIdentifier}/phonenumbers/{identifier}/delete", method = RequestMethod.POST)
     public String formDelete(@PathVariable String contactIdentifier, @ModelAttribute("phoneNumber") @Valid PhoneNumberEntry phoneNumber,
                              BindingResult bindingResult) {
         if (!bindingResult.hasErrors()) {
             RemovePhoneNumberCommand command = new RemovePhoneNumberCommand(new StringAggregateIdentifier(contactIdentifier), phoneNumber.getPhoneNumber());
             logger.debug("Dispatching command with name : {}", command.toString());
             commandBus.dispatch(command);
 
             return "redirect:/contacts/" + contactIdentifier;
         }
 
         return "phonenumbers/delete";
     }
 
     @RequestMapping(value = "{contactIdentifier}/phonenumbers/{identifier}/delete", method = RequestMethod.GET)
     public String formDelete(@PathVariable String contactIdentifier, @PathVariable String identifier, Model model) {
         ContactEntry contactEntry = contactRepositoryImpl.findOne(contactIdentifier);
         model.addAttribute("contact", contactEntry);
         model.addAttribute("phoneNumber", contactEntry.getPhoneNumberEntry(identifier));
 
         return "phonenumbers/delete";
     }
 
     @RequestMapping(value = "{contactIdentifier}/phonenumbers/new", method = RequestMethod.GET)
     public String formNew(@PathVariable String contactIdentifier, Model model) {
         ContactEntry contactEntry = contactRepositoryImpl.findOne(contactIdentifier);
         model.addAttribute("contact", contactEntry);
         model.addAttribute("phoneNumberEntry", new PhoneNumberEntry());
         return "phonenumbers/new";
     }
 
     @RequestMapping(value = "{contactIdentifier}/phonenumbers/new", method = RequestMethod.POST)
     public String formNewSubmit(@PathVariable String contactIdentifier, @Valid PhoneNumberEntry phoneNumber, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             return "phonenumbers/new";
         }
 
         Assert.notNull(contactIdentifier);
 
         CreatePhoneNumberCommand command = new CreatePhoneNumberCommand(new StringAggregateIdentifier(contactIdentifier), phoneNumber);
         logger.debug("Dispatching command with name : {}", command.toString());
         commandBus.dispatch(command);
 
         return "redirect:/contacts/" + contactIdentifier;
     }
 }
