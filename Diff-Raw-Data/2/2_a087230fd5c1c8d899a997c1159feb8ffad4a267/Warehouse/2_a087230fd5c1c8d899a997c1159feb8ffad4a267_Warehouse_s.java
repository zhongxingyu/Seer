 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.soen487.supplychain.warehouse;
 
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.*;
 import org.xml.sax.InputSource;
 
 /**
  *
  * @author root
  */
 @WebService()
 public class Warehouse {
 
    //private static final String INVENTORY_XML = "/root/NetBeansProjects/SupplyChainManagementClient/web/inventory.xml";
     // NEED TO SET PROPER RELATIVE PATH TO inventory.xml
     //private static final String INVENTORY_XML = "../../../../../inventory.xml";
     private static final String INVENTORY_XML = "C:/Java/soen487-retailsupplychain/WarehouseService/src/java/org/soen487/supplychain/warehouse/inventory.xml";
     private static final int REPLENISH_MINIMUM = 50;
     private static final int REPLENISH_AMOUNT = 200;
     private ArrayList<String> namesInCatalog;
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "shipGoods")
     public ItemShippingStatusList shipGoods(@WebParam(name = "itemList")
     org.soen487.supplychain.warehouse.ItemList itemList, @WebParam(name = "info")
     Customer info) {
         System.out.println(System.getProperty("user.dir")); // INDICATE THE WORKING DIR TO HLEP AT RELATIVE PATH SETTING
         File file = new File(INVENTORY_XML);
         try{
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             InputSource is = new InputSource();
             is.setCharacterStream(new FileReader(file));
 
             Document doc = db.parse(is);
 
             NodeList inventory = doc.getElementsByTagName("item");
 
             ItemShippingStatusList statusList = new ItemShippingStatusList();
             // Flag to call the replenish function at the end
             boolean restock = false;
 
             System.out.println("Looping through items");
             // Loop through the items passed in the itemlist
             for(int t=0;t<itemList.size();t++){
                 System.out.println("Item " + t);
                 // Grab the item
                 Item tmp = (Item) itemList.get(t);
                 // Find the item in the inventory
                 for(int i=0;i<inventory.getLength();i++){
                     Element xmlItem = (Element) inventory.item(i);
                     System.out.println(xmlItem.getAttribute("name"));
                     // Check if the unique identifiers match
                     if(tmp.getProductName().equals(xmlItem.getAttribute("name"))){
                         System.out.println("Found the product");
                         int newQuantity = (int) getFloatValue(xmlItem,"quantity") - tmp.getQuantity();
                         if(newQuantity >= 0){
                             // Ship and remove the items from inventory
                             xmlItem.getElementsByTagName("quantity").item(0).setTextContent(Integer.toString(newQuantity));
                             statusList.add(tmp, tmp.getQuantity());
                         }else{
                             // send available stock only
                             statusList.add(tmp, (int) getFloatValue(xmlItem,"quantity"));
                             xmlItem.getElementsByTagName("quantity").item(0).setTextContent("0");
                             
                             restock = true;
                         }
                         break;
                     }
                 }
             }
             // Write the updated content to the xml file
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
             DOMSource source = new DOMSource(doc);
             //StreamResult result = new StreamResult(System.out);
             StreamResult result = new StreamResult(INVENTORY_XML);
             transformer.transform(source, result);
 
             if(restock) replenish();
 
             System.out.println("statusList generated - sending num items: "  + statusList.getItems().size());
             System.out.println("statusList type: " + statusList);
             return statusList;
         } catch(Exception e){
             System.out.println("Error: " + e.getMessage());
         }
         return null;
 
     }
 
     private void replenish(){
         // Performs the replenishing of items in the inventory, if needed
         File file = new File(INVENTORY_XML);
         try{
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             InputSource is = new InputSource();
             is.setCharacterStream(new FileReader(file));
 
             Document doc = db.parse(is);
 
             NodeList inventory = doc.getElementsByTagName("item");
             for(int i=0;i<inventory.getLength();i++){
                 Element xmlItem = (Element) inventory.item(i);
                 if(getFloatValue(xmlItem,"quantity") < REPLENISH_MINIMUM){
                     xmlItem.getElementsByTagName("quantity").item(0).setTextContent(Integer.toString(REPLENISH_AMOUNT));
                 }
             }
 
             // Write the updated content to the xml file
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
             DOMSource source = new DOMSource(doc);
             //StreamResult result = new StreamResult(System.out);
             StreamResult result = new StreamResult(INVENTORY_XML);
             transformer.transform(source, result);
 
         } catch(Exception e){
             System.out.println("Error: " + e.getMessage());
         }
     }
     private static String getTextValue(Element ele, String tagName) {
             String textVal = null;
             NodeList nl = ele.getElementsByTagName(tagName);
             if(nl != null && nl.getLength() > 0) {
                     Element el = (Element)nl.item(0);
                     textVal = el.getFirstChild().getNodeValue();
             }
 
             return textVal;
     }
     private static float getFloatValue(Element ele, String tagName) {
             //in production application you would catch the exception
             return Float.valueOf(getTextValue(ele,tagName)).floatValue();
     }
 
     @WebMethod(operationName= "getNameForCatalog")
    public static List getNameForCatalog(){
          try {
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             InputSource is = new InputSource();
             System.out.println(System.getProperty("user.dir"));
             is.setCharacterStream(new FileReader(INVENTORY_XML));
 
             Document doc = db.parse(is);
             productList current = new productList();
             NodeList nodes = doc.getElementsByTagName("item");
             System.out.println(nodes.getLength());
             // Loop through and print out all of the title elements
             for (int i = 0; i < nodes.getLength(); i++) {
                 Element element = (Element) nodes.item(i);
                 //System.out.println("inside loop"+getTextValue(element,"productType"));
                 if(!getTextValue(element,"productType").equals("")){
                     System.out.println("in here! "+getTextValue(element,"productType"));
                     current.add(getTextValue(element,"productType"));
                     System.out.println(current.getItems());
                     break;
                 }
             }
         } catch (Exception e) {
             System.out.println("An error occurred: " + e.getMessage());
         }       return null;
     }
 
 
 }
