 package benares98.contacts.web;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import benares98.contacts.dao.ContactDAO;
 import benares98.contacts.domain.Contact;
 
 @Controller
 public class ContactsController{
 	private ContactDAO contactDAO;
 
 	public void setContactDAO(ContactDAO contactDAO) {this.contactDAO = contactDAO;}
 	
 	@RequestMapping(value="**/add.htm")
 	public ModelAndView add(HttpServletRequest request, HttpServletResponse response, Contact contact){
 		System.out.println("shitc");
 		contactDAO.saveContact(contact);
 		ModelMap modelMap = new ModelMap();
 		modelMap.addAttribute("contactList", contactDAO.listNames());
 		modelMap.addAttribute("contact", contact);
 		return new ModelAndView("contactsForm", modelMap);
 	}
 	
 	@RequestMapping(value="**/delete.htm")
 	public ModelAndView delete(@RequestParam(value="delName", required=true) String id){
 		contactDAO.deleteContact(contactDAO.readContact(id));
 		return new ModelAndView("redirect:lists.htm");
 	}
 	
 	/*
 	public ModelAndView save(HttpServletRequest request, HttpServletResponse response, Contact contact){
 		contactDAO.saveContact(contact);
 		ModelAndView modelAndView = list(request, response);
 		modelAndView.getModelMap().addAttribute("contact", contact);
 		return modelAndView;
 	}*/
 	
	@RequestMapping(value="**/edit/{contactName}.htm")
 	public ModelAndView readContact(@PathVariable("contactName") String contactName){
 		ModelMap modelMap = new ModelMap();
 		modelMap.addAttribute("contact", contactDAO.readContact(contactName));
 		modelMap.addAttribute("contactList", contactDAO.listNames());
 		return new ModelAndView("contactsForm", modelMap);
 	}
 	
 	@RequestMapping(value="**/lists.htm")
 	public ModelAndView list(HttpServletRequest request, HttpServletResponse response){
 		System.out.println("sas");
 		ModelMap modelMap = new ModelMap();
 		modelMap.addAttribute("contactList", contactDAO.listNames());
 		modelMap.addAttribute("contact", new Contact());
 		return new ModelAndView("contactsForm", modelMap);
 	}
 }
