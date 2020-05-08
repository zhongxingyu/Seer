 package org.sukrupa.app.needs;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.sukrupa.bigneeds.BigNeed;
 import org.sukrupa.bigneeds.BigNeedRepository;
 import org.sukrupa.platform.RequiredByFramework;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 
 @Controller
 @RequestMapping("/bigneeds")
 public class BigNeedsController {
 
     private BigNeedRepository bigNeedRepository;
 
     @RequiredByFramework
     public BigNeedsController() {
     }
 
     @Autowired
     public BigNeedsController(BigNeedRepository bigNeedRepository) {
         this.bigNeedRepository = bigNeedRepository;
     }
 
     @RequestMapping
     public String list(Map<String, Object> model) {
         List<BigNeed> bigNeedList = bigNeedRepository.getList();
         int priority = bigNeedList.size() == 0 ? 1 : bigNeedList.get(bigNeedList.size() - 1).getPriority() + 1;
         model.put("priority", priority);
         model.put("bigNeedList", bigNeedList);
         return "bigNeeds/bigNeedsList";
     }
 
     @RequestMapping(value = "create", method = POST)
     public String create(@RequestParam String priority, @RequestParam String itemName, @RequestParam String itemCost, Map<String, Object> model) {
         bigNeedRepository.addOrEditBigNeed(new BigNeed(itemName, Double.parseDouble(itemCost), Integer.parseInt(priority)));
        model.clear();
        return "redirect:/bigneeds";
     }
 
     @RequestMapping(value = "delete", method = POST)
     @Transactional
     public String delete(@RequestParam long itemId, HashMap<String, Object> model) {
         BigNeed bigNeed = bigNeedRepository.getBigNeed(itemId);
         this.bigNeedRepository.delete(bigNeed);
        model.clear();
        return "redirect:/bigneeds";
     }
 
     @RequestMapping(value = "saveeditedneed", method = POST)
     @Transactional
     public String saveEdit(@RequestParam String priority, @RequestParam long itemId, @RequestParam String itemName, @RequestParam String itemCost, HashMap<String, Object> model) {
         try {
             BigNeed bigNeed = bigNeedRepository.getBigNeed(itemId);
             bigNeed.setItemName(itemName);
             bigNeed.setCost(Double.parseDouble(itemCost));
             bigNeed.setPriority(Integer.parseInt(priority));
             bigNeedRepository.editBigNeed(bigNeed);
         } catch (Exception e) {
             return "Error: " + e.toString();
         }
        model.clear();
        return "redirect:/bigneeds";
     }
 }
