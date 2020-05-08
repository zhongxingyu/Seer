 package com.edsdev.jconvert.domain;
 
 public class DecimalConversion extends Conversion {
 
     private double fromToFactor = 1;
 
     private DecimalConversion() {
         super();
     }
 
     public DecimalConversion(String fromUnit, String fromUnitAbbr, String toUnit, String toUnitAbbr,
             String fromToFactor, double fromToOffset) {
         super(fromUnit, fromUnitAbbr, toUnit, toUnitAbbr, fromToFactor, fromToOffset);
         setFromToFactorString(fromToFactor);
     }
 
     /**
      * @param value
      *            double value you want to convert
     * @param fromUnit
      *            Unit that you want to convert from
      * @return answer
      * 
      * If the fromUnit does not match the classes from unit, then it is assumed that you are converting the other way
      * ex. System.out.println(conversion.convertValue(17, conversion.getFromUnit()));
      * 
      */
     public double convertValue(double value, String pFromUnit) {
         if (pFromUnit.equals(this.getFromUnit())) {
             return getRoundedResult((value * fromToFactor) + getFromToOffset());
         } else {
             return getRoundedResult((value - getFromToOffset()) / fromToFactor);
         }
     }
 
     public String multiply(Conversion byConversion) {
         String rv = "1";
         if (byConversion instanceof FractionalConversion) {
             FractionalConversion fc = (FractionalConversion) byConversion;
             rv = (this.getFromToFactor() * fc.getFromToTopFactor()) / fc.getFromToBottomFactor() + "";
         } else {
             rv = (this.getFromToFactor() * byConversion.getFromToFactor()) + "";
         }
         return rv;
     }
 
     public String divide(Conversion byConversion) {
         String rv = "1";
         if (byConversion instanceof FractionalConversion) {
             FractionalConversion fc = (FractionalConversion) byConversion;
             rv = (this.getFromToFactor() * fc.getFromToBottomFactor()) / fc.getFromToTopFactor() + "";
         } else {
             rv = (this.getFromToFactor() / byConversion.getFromToFactor()) + "";
         }
         return rv;
     }
 
     public double getFromToFactor() {
         return fromToFactor;
     }
 
     public void setFromToFactor(double fromToFactor) {
         this.fromToFactor = fromToFactor;
     }
 
     /* (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#getFromToBottomFactor()
      */
     public long getFromToBottomFactor() {
         //TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
     }
 
     /* (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#setFromToBottomFactor(long)
      */
     public void setFromToBottomFactor(long fromToBottomFactor) {
         //TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
     }
 
     /* (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#getFromToTopFactor()
      */
     public long getFromToTopFactor() {
         //TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
     }
 
     /* (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#setFromToTopFactor(long)
      */
     public void setFromToTopFactor(long fromToTopFactor) {
         //TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
     }
 
    /* (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#setFromToFactorString(java.lang.String)
      */
     public void setFromToFactorString(String factor) {
         fromToFactor = Double.parseDouble(factor);
     }
 
     /* (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#getFromToFactorString()
      */
     public String getFromToFactorString() {
         return fromToFactor + "";
     }
 
 }
