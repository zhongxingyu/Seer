 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fr.univnantes.atal.poubatal;
 
 import java.util.List;
 
 /**
  *
  * @author sildar
  */
 public class CollectePoint {
     
     private String streetName;
     private String numberDescription;
     private String zoneName;
     private String city;
     private CollectableDays yellowBin;
     private CollectableDays blueBin;
     
     public CollectePoint(String csvRow){
         
         String[] columns = csvRow.split(",");
         
        streetName = columns[1];
        city = columns[2];
        numberDescription = columns[9];
        zoneName = columns[13];
         
        yellowBin = new CollectableDays(columns[11]);
        blueBin = new CollectableDays(columns[10]);
         
     }
     
     public String toString(){
         String res = "";
         
         res += streetName + " -> ";
         res += blueBin.toString();
         
         return res;
     }
     
 }
