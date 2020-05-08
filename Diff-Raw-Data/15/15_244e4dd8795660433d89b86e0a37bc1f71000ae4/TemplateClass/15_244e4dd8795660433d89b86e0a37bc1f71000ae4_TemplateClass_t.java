 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.company.lessproject;
 
 import java.awt.Color;
 import java.io.Serializable;
 import java.util.Random;
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
 
 /**
  *
  * @author duncan
  */
@Named
 @SessionScoped
 public class TemplateClass implements Serializable {
     
     private String color;
     
     public String getColor(){
         return "@nice-blue: "+ color + ";";
         
     }
     
    public String getColour(){
        return color;
    }
    
     @PostConstruct
     public void init(){
         Random r = new Random();
         Color  c = new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
         color = "#" + Integer.toHexString(c.getRGB()).substring(2).toUpperCase();
     }
 }
