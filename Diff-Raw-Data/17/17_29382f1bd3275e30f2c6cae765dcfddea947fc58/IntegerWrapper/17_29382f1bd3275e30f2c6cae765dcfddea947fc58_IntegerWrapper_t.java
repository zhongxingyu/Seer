 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.controllers;
 
 /**
  *
  * @author Caleb
  */
public class IntegerWrapper {
     private Integer integer;
     
    public IntegerWrapper(){
         integer = new Integer(0);
     }
     
    public IntegerWrapper(Integer integer){
         this.integer = integer;
     }
     
     public void setInteger(Integer integer){
         this.integer = integer;
     }
     
     public Integer getInteger(){
         return this.integer;
     }
 }
