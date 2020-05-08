 package com.sparc.BoozeChoose.Model;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Whitney Champion
  * Date: 8/23/13
  * Time: 10:05 PM
  * Description:
  */
 public class Ingredient {
 
     private String _id;
     private String name;
 
     public String get_id() {
         return _id;
     }
 
     public void set_id(String _id) {
         this._id = _id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
    public boolean equals(Object a){
        Ingredient ingredient = (Ingredient)a;
        return this.getName().equals(ingredient.getName());

    }
 
 }
