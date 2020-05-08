 package com.globant.gaetraining.addsincgae.controllers;
 
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.globant.gaetraining.addsincgae.model.Customer;
 import com.globant.gaetraining.addsincgae.model.User;
 import com.globant.gaetraining.addsincgae.services.CustomerService;
 import com.globant.gaetraining.addsincgae.services.UserService;
 import com.google.appengine.api.blobstore.BlobInfo;
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 
 @Controller
 public class CustomerController {
 
 	@Autowired
 	private CustomerService customerService;
 	
 	@Autowired
 	private UserService userService;
 	
 	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 
 	@RequestMapping(value = "/customers", method = RequestMethod.GET)
 	public String getCustomers(Map<String, Object> model) {
 
 		List<Customer> customers = customerService.getCustomers();
 		
 		model.put("customers", customers);
 		
 		return "CustomerList";
 	}
	
	@RequestMapping(value="/customers/{customerId}/logo", method = RequestMethod.GET)
	public void serveLogo(HttpServletResponse response, @PathVariable Long customerId, Model model) {
		Customer customer = customerService.getCustomer(customerId);
		try{
		blobstoreService.serve(customer.getLogo(),response);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
 
 	@RequestMapping(value = "/customers/{customerId}", method = RequestMethod.GET, produces = "text/html")
 	public String editCustomer(@PathVariable Long customerId, Model model) {
 	
 		Customer customer = customerService.getCustomer(customerId);
 		String[] ownersString = null, representativesString = null;
 		if(customer.getOwners()!=null) ownersString = new String[customer.getOwners().size()];
 		if(customer.getRepresentative()!=null) representativesString = new String[customer.getRepresentative().size()];
 		int i=0;
 		if(ownersString!=null){
 			for(Key usr : customer.getOwners()){
 				ownersString[i++]=KeyFactory.keyToString(usr);
 			}
 		}
 		i=0;
 		if(representativesString!=null){
 			for(Key usr : customer.getRepresentative()){
 				representativesString[i++]=KeyFactory.keyToString(usr);
 			}		
 		}
 		ListsHelper lists = new ListsHelper();
 		lists.setOwnersString(ownersString);
 		lists.setRepresentativesString(representativesString);
 		
 		model.addAttribute("lists",lists);
 		model.addAttribute("customer", customer);
 		model.addAttribute("users", userService.getUsers().toArray());
 		
 		return "EditCustomer";
 	}
 	
 	@RequestMapping(value = "/customers/{customerId}", method = RequestMethod.POST)
 	public String saveCustomer(HttpServletRequest request, @PathVariable Long customerId, @ModelAttribute("lists") ListsHelper lists,@ModelAttribute("customer") Customer customer, Model model) {
 	 
 		customer.setKey(KeyFactory.createKey("Customer", customerId));
 		Map<String, List<BlobInfo>> blobs = blobstoreService.getBlobInfos(request);
         List<BlobInfo> blobInfos = blobs.get("logo");
         if(blobInfos!=null&&blobInfos.size()>0){
         	customer.setLogo(blobInfos.get(0).getBlobKey());
 		}
 		//TODO: Support for a list of files, optional
 		
 		String[] owners = lists.getOwnersString();
 		if(owners!=null && owners.length>0){
 			customer.setOwners(new ArrayList<Key>());
 			for(String keyUsr : owners){
 				customer.getOwners().add(KeyFactory.stringToKey(keyUsr));
 			}
 		}
 		
 		String[] representatives = lists.getRepresentativesString();
 		if(representatives!=null && representatives.length>0){
 			customer.setRepresentative(new ArrayList<Key>());
 			for(String keyUsr : representatives){
 				customer.getRepresentative().add(KeyFactory.stringToKey(keyUsr));
 			}
 		}
 		
 		this.customerService.addCustomer(customer);
 	  
 		List<Customer> customers = customerService.getCustomers();
 		
 		
 		model.addAttribute("customers", customers);
 	  
 		return "CustomerList";
 	}
 
 	
 	@RequestMapping(value="/customers", method = RequestMethod.POST)
 	public String addCustomer(HttpServletRequest request, ModelMap model){
 		String name = request.getParameter("name");
 		String legalName = request.getParameter("legalName");
 		String description = request.getParameter("description");
 		String employeeAmount = request.getParameter("employeesAmount");
 		
 		
 		Customer customer = new Customer();
 		customer.setName(name);
 		customer.setLegalName(legalName);
 		customer.setDescription(description);
 		customer.setEmployeesAmount(employeeAmount == null ? 0 : Integer.parseInt(employeeAmount));
 		
 		//TODO Find representatives and owners properly 
 	    List<Key> users = Lists.transform(userService.getUsers(), new Function<User, Key>() {
 	      @Override
 	      @Nullable
 	      public Key apply(@Nullable User user) {
 	        return user.getKey();
 	      }
 	      
 	    });
 	    customer.setOwners(users);
 	    customer.setRepresentative(users);
 customerService.addCustomer(customer);
 		
 		return "redirect:/customers";
 	}
 	
 	@RequestMapping(value="/addCustomer", method = RequestMethod.GET)
 	public String add(){
 		return "AddCustomer";
 	}
 	
 	public static class ListsHelper{
 		private String[] ownersString;
 		private String[] representativesString;
 		public String[] getOwnersString() {
 			return ownersString;
 		}
 		public String[] getRepresentativesString() {
 			return representativesString;
 		}
 		public void setOwnersString(String[] ownersString) {
 			this.ownersString = ownersString;
 		}
 		public void setRepresentativesString(String[] representativesString) {
 			this.representativesString = representativesString;
 		}
 		
 	}
 }
