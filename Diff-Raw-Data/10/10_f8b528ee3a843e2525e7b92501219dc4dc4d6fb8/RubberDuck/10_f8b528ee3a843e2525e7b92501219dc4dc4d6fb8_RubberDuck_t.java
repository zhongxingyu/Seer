 package ua.patterns;
 
 public class RubberDuck extends Duck {
   @Override
   public void display() {
     System.out.println("I'm rubber duck!");
   }

  @Override
  public void fly() {
    // Do nothing : rubber ducks can't fly :<
  }

  @Override
  public void quack() {
    System.out.println("Squeeek!");
  }
 }
