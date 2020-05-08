 package infowall.web.controller;
 
 import infowall.domain.process.ItemValueProcess;
 import org.codehaus.jackson.node.ObjectNode;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  *
  */
 @Controller
 public class ItemValueController {
 
     private final ItemValueProcess itemValueProcess;
 
     @Autowired
     public ItemValueController(ItemValueProcess itemValueProcess) {
         this.itemValueProcess = itemValueProcess;
     }
 
     @RequestMapping(value="/item/{dashboardId}/{itemName}/{value}",method = RequestMethod.PUT)
     @ResponseBody
     public String storeSimpleValue(
             @PathVariable String dashboardId,
             @PathVariable String itemName,
             @PathVariable String value){
 
         itemValueProcess.storeSimpleValue(dashboardId,itemName, value);
        return "OK\n";
     }
 
     @RequestMapping(value="/item/{dashboardId}/{itemName}", method = RequestMethod.GET)
     @ResponseBody
     public ObjectNode getValueDate(@PathVariable String dashboardId,@PathVariable String itemName){
         return itemValueProcess.getValue(dashboardId,itemName);
     }
 }
