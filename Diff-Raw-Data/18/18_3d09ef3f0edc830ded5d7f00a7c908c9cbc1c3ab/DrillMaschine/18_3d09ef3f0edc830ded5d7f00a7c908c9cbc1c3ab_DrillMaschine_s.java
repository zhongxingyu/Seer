 @AuthorClass(author="Jakub Zarzycki")
 public class DrillMaschine extends Maschine{
   private final int saeschare;
 
   public DrillMaschine(int anz){
    this.saeschare = new Integer(anz);
   }
 
   @Override
   public int getSaeschere(){ return this.saeschare;}  
 
 }
