 /*
  * A re-implementation of the classic C=64 game 'Thrust'.
  *
  * @author "Colin Casey (colin.casey@org.com)"
  * @module "COMP 20050, COMP 30050"
  * @creation_date "April 2008"
  * @last_updated_date "April 2008"
  * @keywords "C=64", "Thrust", "game"
  */
 
 package thrust.entities.about;
 
 /**
  * @author Colin Casey (colin.casey@org.com)
  * @version 15 April 2008
  */
 public class KKFuelable implements Fuelable {
 
   /** The maximum amount of fuel that can be contained. */
  static final int MAXIMUM_FUEL;
   /** The amount of fuel that is contained. */
  private int my_fuel;
 
   public int fuel() {
     return my_fuel;
   }
 
   public int maximum_fuel() {
    return MAXIMUM_FUEL;
   }
 
   public void set_fuel_content(final int the_fuel_content) {
     my_fuel = the_fuel_content;
   }
 
   public void change_fuel_content(final int the_fuel_change) {
     my_fuel = my_fuel + the_fuel_change;
   }
 }
