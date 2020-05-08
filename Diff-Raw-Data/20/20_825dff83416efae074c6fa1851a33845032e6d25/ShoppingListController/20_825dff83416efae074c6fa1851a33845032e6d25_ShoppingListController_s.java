 package com.app.shopandmeals.models.controllers;
 
 
 import java.io.PrintWriter;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import com.app.shopandmeals.models.entity.ListUser;
 import com.app.shopandmeals.models.entity.ShopList;
 import com.app.shopandmeals.models.entity.User;
 import com.app.shopandmeals.models.services.IShoppingListService;
 import com.app.shopandmeals.models.services.IUserService;
 
 
 @Controller
 @RequestMapping(value = "/shopping")
 @SessionAttributes("shoppingList")
 public class ShoppingListController {
 	
 	 @Autowired
 	 private IShoppingListService shoppingListService;
 	 
 	 @Autowired
 	 private IUserService userService;
 
 	 static Logger logger = Logger.getLogger(ShoppingListController.class.getName());
  
 	@RequestMapping(method = RequestMethod.GET, produces = "application/json") 
 	@ResponseBody
     public List<ShopList> getShoppingLists() throws Exception {
       return shoppingListService.findAll();
      
  	}
 
 	@RequestMapping(value = "/user/{id}", method = RequestMethod.GET, produces = "application/json") 
 	@ResponseBody
     public List<ShopList> getShoppingLists(@PathVariable String id) throws Exception {
      return shoppingListService.findAll(id);
      
  	}
 	
 	@RequestMapping(value="/user/new", method = RequestMethod.POST)  
 	@ResponseStatus(HttpStatus.CREATED)
 	@ResponseBody
     public ShopList create(@RequestBody ShopList shopList) {
 		String username = shopList.getUsers()[0];
        return shoppingListService.create(shopList,username);
       
    }
 	
 	@RequestMapping(value = "/list/{id}", method = RequestMethod.GET, produces = "application/json") 
 	@ResponseStatus(HttpStatus.OK)
 	@ResponseBody
 	public ShopList getShoppingList(@PathVariable String id) {       
 	   return shoppingListService.findById(id);
 	}
 	
 	@RequestMapping(value = "/share/{id}", method = RequestMethod.PUT) 
 	@ResponseStatus(HttpStatus.OK)
 	@ResponseBody
 	public ShopList updateListShared(@PathVariable String id, @RequestBody ListUser listUser) {
 		User  u = userService.findById(listUser.getUsername());
 		if (u != null){
 			shoppingListService.updateListShared(id, listUser.getUsername());
 			return shoppingListService.findById(id);
 		}
 		else
 			return null;
 	}
    
    @RequestMapping(value = "/user/{id}", method = RequestMethod.PUT) 
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
      public ShopList update(@RequestBody ShopList shopList,@PathVariable String id) {
 	   
 	   shoppingListService.update(shopList);	  
 	   return shoppingListService.findById(id);
 	   
     }
    
    @RequestMapping(value = "/items/{id}", method = RequestMethod.PUT) 
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
      public ShopList updateItems(@RequestBody ShopList shopList, @PathVariable String id) {
 	   String[] description = shopList.getItems();
 	   shoppingListService.updateItems(id,description);	  
 	   return shoppingListService.findById(id);
 	   
     }
    
    @RequestMapping(value = "/list/{id}", method = RequestMethod.DELETE) 
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<ShopList> remove(@PathVariable String id) {
 	   ShopList shopList = shoppingListService.findById(id);
 	   shoppingListService.delete(shopList);
 	   return shoppingListService.findAll();
    }
    
    @RequestMapping(value = "/lists", method = RequestMethod.GET) 
    public String toLists() {
 		return "lists/lists";
      
   }
    
    
    @ExceptionHandler
    @ResponseStatus( value=HttpStatus.NOT_ACCEPTABLE )
 	public void exception( Throwable t, PrintWriter out )
 	{
 	   logger.error( "Should be a 406" );
 	}	
 }
