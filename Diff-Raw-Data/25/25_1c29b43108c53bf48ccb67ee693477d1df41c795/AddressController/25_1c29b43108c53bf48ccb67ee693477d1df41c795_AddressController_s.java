 package org.bitbucket.controller;
 
 import java.security.Principal;
 import java.util.List;
 
 import org.bitbucket.dao.AddressDao;
 import org.bitbucket.dao.PersonDao;
 import org.bitbucket.model.Address;
 import org.bitbucket.model.Parcel;
 import org.bitbucket.model.Person;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @Transactional(propagation = Propagation.REQUIRED)
 @RequestMapping(value = "/address")
 public class AddressController {
 
 	@Autowired
 	private PersonDao personDao;
 
 	@Autowired
 	private AddressDao addressDao;

 	@RequestMapping(value = "/list", method = RequestMethod.GET)
 	public String printUser(ModelMap model, Principal principal) {
 
 		Person person = personDao.findByName(principal.getName());
 
 		List<Address> listAddress = addressDao.findByIdPerson(person.getId());
 
 		model.addAttribute("listAddress", listAddress);
 
 		return "address/address_list";
 	}
 
 	@RequestMapping(value = "/add", method = RequestMethod.GET)
 	public String viewFormNew(ModelMap model, Principal principal) {
 		Address address = new Address();
 		model.addAttribute("address", address);
 		return "address/address_add";
 	}
 
 	@RequestMapping(value = "/add", method = RequestMethod.POST)
 	public String submitFormNew(ModelMap model, Principal principal, Address address) {
 		Person person = personDao.findByName(principal.getName());
 		address.setPerson(person);
 		addressDao.save(address);
 		return "redirect:/address/list.htm";
 	}
 }
