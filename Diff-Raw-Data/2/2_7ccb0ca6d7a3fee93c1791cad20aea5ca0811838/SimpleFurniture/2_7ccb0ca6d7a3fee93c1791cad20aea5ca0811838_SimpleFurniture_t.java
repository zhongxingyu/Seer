 package furntest;
 
 public class SimpleFurniture extends Furniture {
 
     public SimpleFurniture() {
         super.setParameters(new fParameters());
     }
 
     private String Result = "";
 
     public String applyWeight(int w, int x, int y) {
 
         Result = "Applying weight of " + w + "kg to position (x=" + x + ",y=" + y + ") of furniture type: " + super.getParameters().getFurnitureName() + "\n\nSimulated Result:\n";
         if ( (super.getParameters().getSturdiness() + super.getParameters().getFlexability()) < w) {
             Result += "The " + super.getParameters().getFurnitureName() + " broke under the " + w + " kg weight...";
         } else {
             Result += "The " + super.getParameters().getFurnitureName() + " passed the " + w + " kg weight test with flying colours...\n\n";
         }
         return (Result);
     }
 
     public String applyFire(int f, int x, int y) {
         Result = "Applying fire of " + f + "degrees C to position (x=" + x + ",y=" + y + ") of furniture type: " + super.getParameters().getFurnitureName() + "\n\nSimulated Result:\n";
         if ( super.getParameters().getDegreesFlamable() < f) {
            Result += "The " + super.getParameters().getFurnitureName() + " caught fire when fire @ " + f + " degrees C was applied...";
         } else {
             Result += "The " + super.getParameters().getFurnitureName() + " passed the fire test with flying colours...\n\n";
         }
         return (Result);
     }
 }
