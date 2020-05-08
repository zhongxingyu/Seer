 package com.octo.red.controller;
 
import java.util.List;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
import com.octo.red.newsql.model.InventoryRecord;
import com.octo.red.newsql.services.InventoryService;
 
 @Controller
 public class InventoryController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
 	
 	@Autowired
 	private InventoryService inventoryService;
 	
 	@RequestMapping(value = "/inventory", method = RequestMethod.GET)
 	public @ResponseBody List<InventoryRecord> getInventory(@RequestParam("storeId") long storeId) {
 		
		//List(produit, nombre, cat�gorie, sous-total par cat�gorie)
 		
 		logger.info("Request received [store={}] on /inventory", storeId);
 		
 		List<InventoryRecord> result = inventoryService.list(storeId);
 		
 		return result;
 	}
 
 }
