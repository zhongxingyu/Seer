 // Bicycle class
 public class Bicycle {
 
   /* some complicated
    * comment
    * without code
    * in it
    */
 
   public int cadence;
   public int gear;
   public int speed;
   // public float commented;
   /* some commented
    * method:
    public void commented_meth() {}
    */
 
 
   public Bicycle(int startCadence, int startSpeed, int startGear) {
     gear = startGear;
     cadence = startCadence;
     speed = startSpeed;
   }
 
 
 
 
   public void setCadence(int newValue) {
     cadence = newValue;
 
   }
 
   public void setGear(int newValue) {
     gear = newValue;
   }
 
   public void applyBrake(int decrement) {
     speed -= decrement;
   }
 
   public void speedUp(int increment) {
     speed += increment;
   }
 
   private void testMethod(int n) {
     if (n > 3)
       speed *= 2;
     else {
       gear /= 2;
     }
     for (int i = 0; i < n; ++i);
     ;;
   }
 
  public static void Main()
  {
    Bicycle(10, 100, 2);
     setCadence(50);
     setGear(3);
     applyBrake(10);
     speedUp(30);
     testMethod(i, j);
   }
 }
