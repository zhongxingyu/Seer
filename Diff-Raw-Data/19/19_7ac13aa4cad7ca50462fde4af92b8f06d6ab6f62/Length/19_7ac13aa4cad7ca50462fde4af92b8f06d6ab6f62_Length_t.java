 package net.pizey.jclu;
 
import java.math.BigDecimal;

 /** 
  * A tuple of a real and a unit to represent a length. 
  * @author timp
  * @since 2013-08-13
  */
 public class Length {
 
   private Double real;
   private LengthUnit lengthUnit;
 
   public Length(double real, String unitSymbol) {
    this.setReal(new Double(real));
     this.setLengthUnit(LengthUnit.fromSymbol(unitSymbol));
   }
 
   public Double getReal() {
     return real;
   }
 
   public void setReal(double real) {
     this.real = new Double(real);
   }
 
   public String getLengthUnitSymbol() {
     return lengthUnit.getSymbol();
   }
 
   public LengthUnit getLengthUnit() {
     return lengthUnit;
   }
 
   public void setLengthUnit(LengthUnit lengthUnit) {
     this.lengthUnit = lengthUnit;
   }
   
  public static Double round(double value) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
    return bd.doubleValue();
  }

   private static String prettyDouble(Double d){
    String out = round(d).toString();
    return out.replaceFirst("\\.0$", "");
   }
  
   @Override
   public String toString() { 
     return prettyDouble(getReal()) + " " + getLengthUnitSymbol(); 
   }
   
   /** Convert Length of given unit. */
   public Length toUnit(String symbol){
     // autoboxed
     return new Length(
         (getReal() * getLengthUnit().getMetres()) / LengthUnit.fromSymbol(symbol).getMetres(), 
         symbol);
   }
   
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result
         + lengthUnit.hashCode();
     result = prime * result + real.hashCode();
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj)
       return true;
     if (obj == null)
       return false;
     if (getClass() != obj.getClass())
       return false;
     Length other = (Length) obj;
     if (!lengthUnit.equals(other.lengthUnit))
       return false;
     if (!real.equals(other.real))
       return false;
     return true;
   }
 
 }
