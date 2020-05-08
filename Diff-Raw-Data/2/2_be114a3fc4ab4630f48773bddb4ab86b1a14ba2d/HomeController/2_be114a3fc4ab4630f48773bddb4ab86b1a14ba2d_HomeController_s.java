 package co.silbersoft.anchor.controllers;
 
 import co.silbersoft.anchor.exceptions.GenericDataException;
 import co.silbersoft.anchor.models.FrontPageImage;
 import co.silbersoft.anchor.models.Mail;
 import co.silbersoft.anchor.models.Menu;
 import co.silbersoft.anchor.models.MenuItem;
 import co.silbersoft.anchor.services.MailService;
 import co.silbersoft.anchor.services.MenuService;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.mail.MailException;
 import org.springframework.mobile.device.Device;
 import org.springframework.mobile.device.DeviceHandlerMethodArgumentResolver;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 @Controller
 @RequestMapping(value = "/")
 public class HomeController {
 
     @RequestMapping(value = "", method = RequestMethod.GET)
     public String index(@RequestParam(value = "_escaped_fragment_", required = false) String pageToIndex, Device device, Model model) {
         if (pageToIndex != null) {
             model.addAttribute("page", pageToIndex);
             return "indexer";
         } else if(device.isMobile() || device.isTablet()){
            return "mobile";
         } else {
         	return "index";
         }
     }
 
     @RequestMapping(value = "login", method = RequestMethod.GET)
     public String login(@RequestParam(value = "error", required = false) String error, ModelMap model) {
         if (error != null && error.equals("authFailed")) {
             model.addAttribute("error", "true");
         }
         return "login";
     }
 
     @RequestMapping(value="mail", method=RequestMethod.POST)
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody Map<String,String> sendMail(@RequestBody Mail mail){     	    
         mailService.sendMessage(mail.getFrom(), mail.getMessage());        
         Map m = new HashMap();
         m.put("sent", "true");
         return m;
     }
     
     @RequestMapping(value = "menus", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody
     List<Menu> getAllMenus() {
         return menuService.getAllMenus();
     }
 
     @RequestMapping(value = "menus/{id}", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody
     List<MenuItem> getAllItemsInMenu(@PathVariable Long id) {
         return menuService.getAllByMenu(id);
     }
 
     @RequestMapping(value = "menus/{id}/{catagory}", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody
     List<MenuItem> getItems(@PathVariable Long id, @PathVariable String catagory) {
         return menuService.getAllByMenuAndCatagory(id, catagory);
     }
 
     @RequestMapping(value = "menus/{id}/catagories", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody
     List<String> getCatagories(@PathVariable Long id) {
         return menuService.getCatagoriesForMenu(id);
     }
 
     @RequestMapping(value = "landing/imgs", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody List<FrontPageImage> getImages(Device device, HttpServletRequest request) {    	
     	List<FrontPageImage> imgList = new ArrayList<FrontPageImage>();
     	imgList.add(new FrontPageImage("resources/img/landing/1.jpg", null, 1 ));
     	imgList.add(new FrontPageImage("resources/img/landing/2.jpg", null, 2 ));    	
     	imgList.add(new FrontPageImage("resources/img/landing/3.jpg", null, 3 ));
     	imgList.add(new FrontPageImage("resources/img/landing/4.jpg", "w2", 4 ));
     	imgList.add(new FrontPageImage("resources/img/landing/5.jpg", "w2", 5 ));
     	imgList.add(new FrontPageImage("resources/img/landing/6.jpg", null, 6 ));
     	imgList.add(new FrontPageImage("resources/img/landing/7.jpg", null, 7 ));
     	imgList.add(new FrontPageImage("resources/img/landing/8.jpg", null, 8 ));
     	imgList.add(new FrontPageImage("resources/img/landing/10.jpg", null, 9 ));    	
     	imgList.add(new FrontPageImage("resources/img/landing/11.jpg", null, 10 ));
     	imgList.add(new FrontPageImage("resources/img/landing/9.jpg", null, 11 ));
     	imgList.add(new FrontPageImage("resources/img/landing/13.jpg", "w2", 12 ));
     	imgList.add(new FrontPageImage("resources/img/landing/14.jpg", null, 13 ));
     	imgList.add(new FrontPageImage("resources/img/landing/16.jpg", null, 14 ));
     	imgList.add(new FrontPageImage("resources/img/landing/12.jpg", null, 15));
     	imgList.add(new FrontPageImage("resources/img/landing/15.jpg", null, 16 ));
     	imgList.add(new FrontPageImage("resources/img/landing/17.jpg", "w2", 17 ));
     	imgList.add(new FrontPageImage("resources/img/landing/18.jpg", null, 18 ));
     	imgList.add(new FrontPageImage("resources/img/landing/20.jpg", null, 19 ));
     	imgList.add(new FrontPageImage("resources/img/landing/21.jpg", null, 20 ));
     	imgList.add(new FrontPageImage("resources/img/landing/19.jpg", null, 21 ));    	
     	return imgList;
     }
     
     @PreAuthorize("hasRole('Administrator')")
     @RequestMapping(value = "menus/items/{id}", method = RequestMethod.DELETE)
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody
     Map<String, String> deleteMenuItem(@PathVariable Long id) {
         menuService.deleteMenuItem(id);
         Map<String, String> error = new HashMap();
         error.put("removed", id.toString());
         return error;
     }
 
     @PreAuthorize("hasRole('Administrator')")
     @RequestMapping(value = "menus/items", method = RequestMethod.POST, headers = {"Accept=application/json"})
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody
     Map<String, Long> addMenuItem(@RequestBody MenuItem menuItem) {
         Long id = menuService.createMenuItem(menuItem);
         Map<String, Long> error = new HashMap();
         error.put("added", id);
         return error;
     }
 
     @PreAuthorize("hasRole('Administrator')")
     @RequestMapping(value = "menus/items", method = RequestMethod.PUT, headers = {"Accept=application/json"})
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody
     Map<String, Long> editMenuItem(@RequestBody MenuItem menuItem) {
         menuService.updateMenuItem(menuItem);
         Map<String, Long> status = new HashMap();
         status.put("updated", menuItem.getId());
         return status;
     }
 
     @ExceptionHandler(MailException.class)
     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
     public @ResponseBody Map<String, String> handleMailException(MailException e){
         Map error = new HashMap();
         error.put("error", e.getMessage());
         return error;
     }
     
     @ExceptionHandler(GenericDataException.class)
     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
     public @ResponseBody
     Map<String, String> handleGenericDataException(GenericDataException e) {
         Map<String, String> error = new HashMap();
         error.put("error", e.getMessage());
         return error;
     }
 
     @ExceptionHandler(Exception.class)
     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
     public @ResponseBody
     Map<String, String> handleException(Exception e) {
         Map<String, String> error = new HashMap();
         error.put("error", e.getMessage());
         return error;
     }    
     
     @Autowired MenuService menuService;    
     @Autowired MailService mailService;
 }
