 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.soen487.supplychain.retail;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.xml.ws.WebServiceRef;
import org.soen487.supplychain.warehouse.*;
 
 /**
  *
  * @author jose
  */
 @WebService()
 public class Retail {
     @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8080/WarehouseService/WarehouseService.wsdl")
     private WarehouseService service;
     private List<Integer> warehouse_indexes;
     private ItemShippingStatusList list_stat;
    
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "Retail")
     public String Retail(@WebParam(name = "itemList")
    ItemList itemList, @WebParam(name = "custInfo")
    Customer custInfo) {
         warehouse_indexes = Arrays.asList(1,2,3);
         Collections.shuffle(warehouse_indexes); // random order of warehouse calls
 
         list_stat = warehouse_call(itemList, custInfo, 1);
 
         list_stat.getItems();
 
 //        for(Integer rand : warehouse_indexes){
 //            list_stat = warehouse_call(itemList, custInfo, rand.intValue());
 //
 //            //list_stat.
 //        }
         
 
         return null;
     }
 
     private ItemShippingStatusList warehouse_call(ItemList itemList, Customer custInfo, int rand){
         switch(rand){
             case 1:
                 return shipGoods_W1(itemList, custInfo);
             case 2:
                 return shipGoods_W1(itemList, custInfo);
             case 3:
                 return shipGoods_W1(itemList, custInfo);
             default:
                 return null;
         }
     }
 
 
     private void updateList(){
 
     }
 
    private ItemShippingStatusList shipGoods_W1(ItemList itemList, Customer info) {
        Warehouse port = service.getWarehousePort();
         return port.shipGoods(itemList, info);
     }
 
 }
