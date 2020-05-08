 @AuthorClass(author="Jakub Zarzycki")
 public class DuengerStreuer extends Maschine{
   private final double behaelter;
 
   public DuengerStreuer(double vol){
    this.behaelter = new Double(vol);
   }
 
   public double getBehaelter(){ return this.behaelter; }
 }
