 /*
  * Copyright 2012 C24 Technologies.
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
  * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL C24 TECHNOLOGIES BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  * 
  */
 package biz.c24.retaildemo.ui.web.controller;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import biz.c24.retaildemo.service.ReceiptFlow;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import biz.c24.retaildemo.model.Product;
 import biz.c24.retaildemo.service.ReceiptsStatsService;
 
 
 @Controller
 @RequestMapping("/")
 public class MainController {
 
 	public static String VIEW = "home";
 	
 	@Autowired(required=true)
 	ReceiptsStatsService service;
 
     @Autowired(required=false)
     ReceiptFlow liveReceiptFlow = null;
 	
 	@RequestMapping(value = "/home.html", method = RequestMethod.GET)
 	public String getFilterDetails(ModelMap model) {
 		model.put("productsByCategory", getProductFilter());
 		model.put("customerProfiles", service.getCustomerProfiles());
 		return VIEW;
 	}
 	
 	@RequestMapping(value = "/startLiveReceipts.html")
 	@ResponseBody()
 	public String startLiveReceipts() {
 		if (liveReceiptFlow != null) {
             liveReceiptFlow.startLiveReceiptFlow();
             return "Live receipt feed online";
        }
         return "Live receipt flow unavailable";
 	}
 	
 	@RequestMapping(value = "/stopLiveReceipts.html")
 	@ResponseBody()
 	public String stopLiveReceipts() {
         if (liveReceiptFlow != null) {
             liveReceiptFlow.stopLiveReceiptFlow();
             return "Live receipt feed offline";
         }
         return "Live receipt flow unavailable";
 	}
 	
 	private Map<String, ? extends Collection<Product>> getProductFilter() {
 		
 		Map<String, LinkedList<Product>> res = new HashMap<String, LinkedList<Product>>();
 		for(Product prod : service.getProducts()) {
 			LinkedList<Product> category = res.get(prod.getCategory());
 			if(category == null) {
 				category = new LinkedList<Product>();
 				res.put(prod.getCategory(), category);
 			}
 			category.add(prod);
 		}
 		
 		for(String category : res.keySet()) {
 			Collections.sort(res.get(category), new Comparator<Product>() {
 
 				public int compare(Product o1, Product o2) {
 					return o1.getDescription().compareTo(o2.getDescription());
 				}
 				
 			} );
 		}
 		return res;
 		
 	}
 	
 }
