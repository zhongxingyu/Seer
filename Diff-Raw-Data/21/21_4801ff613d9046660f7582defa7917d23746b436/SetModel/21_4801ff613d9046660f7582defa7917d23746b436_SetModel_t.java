 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.paul.entitytools.Conversion;
 
import java.util.LinkedHashSet;
 import java.util.Set;
 
 /*
  *
  * Document   : SetModel
  * Created on : Nov 15, 2011, 1:32:46 PM
  * Author     : samuraipanzer
  */
 /**
  * SetModel
  * @author samuraipanzer
  */
 public class SetModel {
   private Set set;
 
   public SetModel() {
   }
 
   public SetModel(Set set) {
     this.set = set;
   }
 
   public Set getSet() {
    if(set == null){
      return new LinkedHashSet();
    }

     return set;
   }
 
   public void setSet(Set set) {
     this.set = set;
   }
 
   
 }
