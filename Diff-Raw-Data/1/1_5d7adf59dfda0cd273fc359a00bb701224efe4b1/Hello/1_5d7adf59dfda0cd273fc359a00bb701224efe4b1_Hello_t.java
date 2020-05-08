 public class Hello {
     public static void main(String ... arg) {
         System.out.println("Hello world");
         System.out.println("Hello new commit");
     }
 
     public void immediateFix() {
 
     }
 
     public void newFunction() {
 
     }
 }
 
 class MegaFeature {
     public boolean isMega = true;
     public boolean isFeature = true;
    public int someFactor = 100;
 
     MegaFeature(boolean mega, boolean feature) {
         isMega = mega;
         isFeature = feature;
     }
 
     public boolean isMega() {
         return isMega;
     }
 
     public void setMega(boolean mega) {
         isMega = mega;
     }
 
     public boolean isFeature() {
         return isFeature;
     }
 
     public void setFeature(boolean feature) {
         isFeature = feature;
     }
 }
