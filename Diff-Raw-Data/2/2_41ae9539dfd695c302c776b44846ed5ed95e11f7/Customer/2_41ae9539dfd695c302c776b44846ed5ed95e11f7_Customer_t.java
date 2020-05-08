package org.easetech.easytest.codegen.example;
 
 /**
  * This is example customer class to check codegen
  * @author polamper
  *
  */
 public class Customer
 {
     private String _name;
     private int    _zip;
 
     public Customer (String name, int zip) {
         _name = name;
         _zip = zip;
     }
     
     public Customer () {
         super();
     	_name = "defaultName";
     	_zip = 12345;
     }
 
     public String getName () {
         return _name;
     }
 
     public int getZip () {
         return _zip;
     }
     
     public void  setName (String name) {
         this._name = name;
     }
 
     public void setZip(int zip) {
         this._zip = zip;
     }
     
     public String fetchNameZipCombined(String combined){
     	return _name.concat(combined).concat(String.valueOf(_zip));
     }
 }
