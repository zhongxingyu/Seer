 package fr.kage.samples.shopping.controller;
 
 import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 import static org.springframework.web.bind.annotation.RequestMethod.PUT;
 
 import java.util.List;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import fr.kage.samples.shopping.dao.ShoppingService;
 import fr.kage.samples.shopping.model.Element;
 
 @Controller
 public class ShoppingController {
 	
 	private ShoppingService shoppingService;
 
 	public ShoppingService getShoppingService() {
 		return shoppingService;
 	}
 
 	public void setShoppingService(ShoppingService shoppingService) {
 		this.shoppingService = shoppingService;
 	}
 
 	@RequestMapping(value="/element", method=GET)
 	public @ResponseBody List<Element> list() {
 		return shoppingService.listElements();
 	}
 	
 	@RequestMapping(value="/element", method=POST)
 	public void add(@RequestBody Element element) {
 		shoppingService.addElement(element);
 	}
 	
 	@RequestMapping(value="/element/{id}", method=PUT)
 	public void edit(@PathVariable long id, @RequestBody Element element) {
 		shoppingService.updateElement(id, element);
 	}
 	
 	@RequestMapping(value="/element/{id}", method=DELETE)
 	public void delete(@PathVariable long id) {
 		shoppingService.deleteElement(id);
 	}
 	
 }
