 package com.selenium.training.web;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.*;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Controller
 @RequestMapping("/" + Controller2.ROOT)
 @SessionAttributes({"name", "items", "totals"})
 public class Controller2 {
 
     public static final String ROOT = "2";
 
     @RequestMapping(method = RequestMethod.GET)
     public String hi() {
         return ROOT + "/hi";
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public String myName(@RequestParam("name") String name, ModelMap model) {
         model.put("name", name);
         return ROOT + "/apple";
     }
 
     @RequestMapping(value = "selection")
     public String selection(@RequestParam String fruit, HttpServletRequest request, ModelMap model) {
         if (request.getParameter("yes") != null) {
             model.put("fruit", fruit);
             return ROOT + "/howmuch";
         } else {
             List<FruitItem> items = getItems(model);
             Map<String, Integer> totals = new HashMap<String, Integer>();
             for (FruitItem item : items) {
                 Integer value = totals.get(item.getName());
                 if (value == null) {
                     value = 0;
                 }
                 totals.put(item.getName(), value + item.getAmount());
             }
             model.put("totals", totals);
             return ROOT + "/summary";
         }
     }
 
     @ModelAttribute("items")
     public List<FruitItem> defaultItems() {
         return new ArrayList<FruitItem>();
     }
 
     @RequestMapping(value = "howmuch", method = RequestMethod.POST)
     public String howMuch(@RequestParam("fruit") String fruit, @RequestParam("amount") int amount, ModelMap model) {
         List<FruitItem> items = getItems(model);
         items.add(new FruitItem(fruit, amount));
         model.put("items", items);
         return ROOT + "/apple";
     }
 
     private List<FruitItem> getItems(ModelMap model) {
         return (List<FruitItem>) model.get("items");
     }
 }
