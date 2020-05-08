 package com.abudko.reseller.huuto.mvc.item;
 
 import javax.annotation.Resource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.abudko.reseller.huuto.order.ItemOrder;
 import com.abudko.reseller.huuto.query.html.item.ItemResponse;
 import com.abudko.reseller.huuto.query.service.item.QueryItemService;
 
 @Controller
 public class QueryItemController {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     @Resource
     private QueryItemService queryItemService;
 
     @RequestMapping(value = "/item", method = RequestMethod.GET)
     public String extractItem(@RequestParam String url, Model model) {
         log.info(String.format("Handling item request with parameter %s", url));
 
         ItemResponse response = queryItemService.extractItem(url);
 
         ItemOrder order = createItemOrderFor(response);
         model.addAttribute("itemOrder", order);
 
        return "order/order";
     }
 
     private ItemOrder createItemOrderFor(ItemResponse response) {
         ItemOrder order = new ItemOrder();
         order.setItemResponse(response);
         order.setNewPrice(response.getPrice());
         return order;
     }
 }
