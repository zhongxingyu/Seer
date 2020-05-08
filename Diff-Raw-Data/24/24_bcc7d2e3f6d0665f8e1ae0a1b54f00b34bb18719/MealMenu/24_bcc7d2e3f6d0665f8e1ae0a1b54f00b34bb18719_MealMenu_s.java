 package pt.uac.cafeteria.model;
 
 import java.util.Calendar;
 
 /**
  * Represents the Meal Menu.
  * 
  */
 public class MealMenu {
    
     /** Day of the menu */
    private Calendar day;
     
     /** Time of the meal */
    private Meal.Time time;
     
     /** Meat meal */
     private String meat;
     
     /** Fish meal */
     private String fish;
     
     /** Vegetarian meal */
     private String veggie;
     
     /** Soup meal */
     private String soup;
     
     /** Dessert Meal */
     private String dessert;
     
     
     /**
      *  Implements Builder Pattern
      */
     public static class Builder {
         
         private final Calendar day;
         private final Meal.Time time;
         
         private String meat;
         private String fish;
         private String veggie;
         private String soup;
         private String dessert;
         
         public Builder(Calendar day, Meal.Time time) {
             this.day = day;
             this.time = time;
         }
         
         public Builder setMeatCourse(String meat) {
             this.meat = meat;
             return this;
         }
         
         public Builder setFishCourse(String fish) {
             this.fish = fish;
             return this;
         }
         
         public Builder setVeggieCourse(String veggie) {
             this.veggie = veggie;
             return this;
         }
         
         public Builder setSoupAndDessert (String soup, String dessert) {
             this.soup = soup;
             this.dessert = dessert;
             return this;
         }
         
         public MealMenu build() {
             if (this.meat == null && this.fish == null && this.veggie == null) {
                 throw new IllegalStateException("É necessário introduzir no mínimo um tipo de prato.");
             }
             if (this.soup == null || this.dessert == null) {
                 throw new IllegalStateException("É necessário introduzir uma sopa e sobremesa.");
             }
             return new MealMenu(this);
         }
     }
 
     /**
      * Meal Menu Constructor
      * 
      * @param builder 
      */
     private MealMenu(Builder builder) {
         this.day = builder.day;
         this.time = builder.time;
         this.meat = builder.meat;
         this.fish = builder.fish;
         this.veggie = builder.veggie;
         this.soup = builder.soup;
         this.dessert = builder.dessert;
     }
     
     
     /** Returns meat meal*/
     public Meal getMeatMeal() {
         return new Meal(day, time, Meal.Type.MEAT, soup, meat, dessert);
     }
     
     /** Returns fish meal*/
     public Meal getFishMeal() {
        return new Meal(day, time, Meal.Type.MEAT, soup, fish, dessert);
     }
     
     /** Returns vegetarian meal*/
     public Meal getVeggieMeal() {
        return new Meal(day, time, Meal.Type.MEAT, soup, veggie, dessert);
     }
 }
