 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package recipe.core;
 
 import recipe.core.Ingredient;
 import java.util.ArrayList;
 
 /**
  *
  * @author Michael
  */
 public class Recipe {
     public ArrayList<Ingredient> Ingredients;
     public ArrayList<String> Steps;
     public int ID;
     public String Title;
     public float Yield;
     private int step;
     
     public Recipe(int id, String title, float yield) {
         this.ID = id;
         this.Title = title;
         this.Yield = yield;
         this.step = 0;
     }
     
     @Override
     public String toString() {
        return Title + "         yields: "+Float.toString(Yield) + " serving(s)";
     }
 
     /**
      * @return the step
      */
     public int getStep() {
         return step;
     }
 
     /**
      * @param step the step to set
      */
     public void setStep(int step) {
         if (step >= 0 && step < this.Steps.size())
             this.step = step;
     }
     
     public String currentStep() {
         return Steps.get(step);
     }
     
 }
