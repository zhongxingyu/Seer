package com.cse135;
 
 import java.io.*;
 import java.util.*;
 
 
 public class support{
     public Vector getCountries(String path){
         countries c = new countries();
         return c.getVector(path);
     }
 
     public Vector getUniversities(String path){
         universities u = new universities();
         return u.getVector(path);
     }
 
 
     //the logic for majors and specs are exactly
     //the same as for countries so just reuse the class
     public Vector getMajors(String path){
 	        countries m = new countries();
 	        return m.getVector(path);
     }
 
     public Vector getSpecializations(String path){
 	        countries s = new countries();
 	        return s.getVector(path);
     }
 }
 
 class countries{
     Vector c = new Vector();
 
     public Vector getVector(String path){
         if(c.isEmpty())
             this.initVector(path);
         return c;
     }
 
     void initVector(String path){
         try{
             BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
             String line = "";
             while((line = br.readLine()) != null) {
                 c.add(line);
             }
         }
         catch(Exception e){
             System.out.println(e);
         }
     }
 }
 
 class universities{
     Vector v = new Vector();
 
     public Vector getVector(String path){
         if(v.isEmpty())
             this.initVector(path);
         return v;
     }
 
     void initVector(String path){
         this.initStates(path);
     }
 
     void initStates(String path){
         try{
             BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
             String line = "";
             Vector tuple = new Vector();
             Vector univ = new Vector();
             while((line = br.readLine()) != null) {
                 //if we hit a blank line
                 if(line.trim().length() == 0){
                     if(!univ.isEmpty()){
                         tuple.add(univ);
                         v.add(tuple);
                         
                         tuple = new Vector();
                         univ = new Vector();
 
                         //is there a next state?
                         if((line = br.readLine()) != null)                            
                             tuple.add(line.trim());
                     }
                     //else this is the first entry, just add the state to the tuple
                     else if(tuple.isEmpty() && (line = br.readLine()) != null){                     
                          tuple.add(line.trim());
                     }
                 }
                 else{
                     univ.add(line.trim());
                 }
             }
         }
         catch(Exception e){
             System.out.println(e);
         }
     }
 }
