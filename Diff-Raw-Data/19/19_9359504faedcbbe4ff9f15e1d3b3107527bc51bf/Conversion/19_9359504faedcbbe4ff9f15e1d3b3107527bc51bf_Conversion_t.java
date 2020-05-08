 package com.edsdev.jconvert.domain;
 
 /**
  * This is the abstract class that represents a conversion itself.
  * 
  * @author Ed Sarrazin Created on Jul 14, 2007 10:10:14 AM
  */
 public abstract class Conversion implements Comparable {
 
     private String fromUnit;
 
     private String fromUnitAbbr;
 
     private String toUnit;
 
     private String toUnitAbbr;
 
     private double fromToOffset = 0;
 
     private int generationAge = 0;
 
     protected Conversion() {
         super();
     }
 
     /**
      * This static mehtod acts as the factory for Conversion objects. From this method you will receive a class that
      * implements the Conversion interface in its own way. The parameters that are passed in are the basic properties
      * that are needed to construct this class. They are one for one with the information that is stored in the dat
      * files.
      * 
      * @param fromUnit String:Unit that will be converted from.
      * @param fromUnitAbbr String:Abbreviation of the unit that will be converted from.
      * @param toUnit String:Unit that will be converted to.
      * @param toUnitAbbr String:Abbreviation of the unit that will be converted to.
      * @param fromToFactor String:Factor used to convert from the designated unit to the designated unit. This can
      *        either be a decimal or fractional representation - assumes that the fraction uses "/" symbol i.e.1/3 not
      *        1\3.
      * @param fromToOffset double value representing the offset to be applied in the conversion. Note that the factor
      *        will be applied first and the offset later.
      * @return Conversion implementation representing the information that was passed in.
      */
     public static Conversion createInstance(String fromUnit, String fromUnitAbbr, String toUnit, String toUnitAbbr,
             String fromToFactor, double fromToOffset) {
         Conversion conversion = null;
 
         int pos = fromToFactor.indexOf("/");
         if (pos > 0) {
             String top = fromToFactor.substring(0, pos);
             String bottom = fromToFactor.substring(pos + 1);
             if (isWholeNumber(top) && isWholeNumber(bottom)) {
                 conversion = new FractionalConversion(fromUnit, fromUnitAbbr, toUnit, toUnitAbbr, fromToFactor,
                     fromToOffset);
             } else {
                 conversion = new DecimalConversion(fromUnit, fromUnitAbbr, toUnit, toUnitAbbr, Double.parseDouble(top)
                         / Double.parseDouble(bottom) + "", fromToOffset);
             }
         } else if (isWholeNumber(fromToFactor)) {
             conversion = new FractionalConversion(fromUnit, fromUnitAbbr, toUnit, toUnitAbbr, fromToFactor,
                 fromToOffset);
         } else {
             conversion = new DecimalConversion(fromUnit, fromUnitAbbr, toUnit, toUnitAbbr, fromToFactor, fromToOffset);
         }
 
         return conversion;
     }
 
     /**
      * Just a helper method to get a conversion that is not functional, but can be used to compare This is primarily to
      * speed up generating conversions to determine if one already exists. Will return a conversion item with the
      * appropriate from and to units, but the conversion factor and offset are bogus.
      * 
      * @param fromUnit
      * @param fromUnitAbbr
      * @param toUnit
      * @param toUnitAbbr
      * @return Conversion instance that does not have valid conversion information.
      */
     public static Conversion createEmptyInstance(String fromUnit, String fromUnitAbbr, String toUnit, String toUnitAbbr) {
         return new DecimalConversion(fromUnit, fromUnitAbbr, toUnit, toUnitAbbr, "1", 0);
     }
 
     protected Conversion(String fromUnit, String fromUnitAbbr, String toUnit, String toUnitAbbr, String fromToFactor,
             double fromToOffset) {
         this();
         this.fromUnit = fromUnit;
         this.fromUnitAbbr = fromUnitAbbr;
         this.toUnit = toUnit;
         this.toUnitAbbr = toUnitAbbr;
         this.fromToOffset = fromToOffset;
     }
 
     /**
      * This method really tells you if the number can be represented as a long. The criteria are that it cannot be so
      * large that a long cannot handle it AND it cannot have decimals unless there are only zeros after the decimal.
      * 
      * @param value String value representation of the number
      * @return true if is whole number(long) or false otherwise.
      */
     protected static boolean isWholeNumber(String value) {
         //quick check - if longer than 19 chars long will not hold
         if (value.length() > 19) {
             return false;
         }
         int pos = value.indexOf(".");
 
         //look after decimal for only zeors like 45.000
         if (pos > 0) {
             for (int i = pos + 1; i < value.length(); i++) {
                 if (!"0".equals(value.subSequence(i, i + 1))) {
                     return false;
                 }
             }
         }
 
         //one last test since anything over 9,223,372,036,854,775,808 will
         //succeed, but will not fit in a long
         try {
             if (pos < 0) {
                 Long.parseLong(value);
             } else {
                 Long.parseLong(value.substring(0, pos));
             }
         } catch (Exception e) {
             return false;
         }
         return true;
     }
 
     /**
      * Simply extracts long out of string, but also handles it when there is decimals - should use isWhole number to
      * make sure, because this will throw an exception if not prefect
      * 
      * @param value String representation of the long
      * @return long representation of the value passed in.
      */
     protected long getLong(String value) {
         int pos = value.indexOf(".");
         if (pos < 0) {
             return Long.parseLong(value);
         }
         return Long.parseLong(value.substring(0, pos));
     }
 
     public int hashCode() {
         return fromUnit.hashCode() + toUnit.hashCode();
     }
 
     public String toString() {
         if (this instanceof FractionalConversion) {
             return this.hashCode() + ": " + fromUnit + ";" + toUnit + ";" + getFromToTopFactor() + "/"
                     + getFromToBottomFactor() + ";" + fromToOffset;
         } else {
             return this.hashCode() + ": " + fromUnit + ";" + toUnit + ";" + getFromToFactor() + ";" + fromToOffset;
         }
     }
 
     public boolean equals(Object obj) {
         if (obj instanceof Conversion) {
             Conversion compare = (Conversion) obj;
             if (!areEqual(compare.getFromUnit(), fromUnit)) {
                 return false;
             }
 //            if (!areEqual(compare.getFromUnitAbbr(), fromUnitAbbr)) {
 //                return false;
 //            }
             if (!areEqual(compare.getToUnit(), toUnit)) {
                 return false;
             }
 //            if (!areEqual(compare.getToUnitAbbr(), toUnitAbbr)) {
 //                return false;
 //            }
             return true;
 
         }
         return false;
     }
 
     private boolean areEqual(Object one, Object two) {
         if (one == null && two == null) {
             return true;
         }
         if (one == null) {
             return false;
         }
         if (two == null) {
             return false;
         }
         return one.equals(two);
     }
 
     /**
      * Converts a value for you are returns the answer.
      * 
      * @param value double value you want to convert
     * @param pFromUnit Unit that you want to convert from
      * @return answer If the fromUnit does not match the classes from unit, then it is assumed that you are converting
      *         the other way ex. System.out.println(conversion.convertValue(17, conversion.getFromUnit()));
      */
     public abstract double convertValue(double value, String pFromUnit);
 
     protected double getRoundedResult(double result) {
         return result;
         //		BigDecimal bigResult = new BigDecimal(result);
         //		String resultString = new Double(result).toString();
         //		int scale = resultString.substring(resultString.indexOf(".") + 1).length() - ( 1 + this.getGenerationAge());
         //		BigDecimal rv = bigResult.setScale(scale, BigDecimal.ROUND_HALF_UP);
         //
         //		return rv.doubleValue();
     }
 
     /**
      * Converts from one unit to another.
      * 
      * @param value double value that you want to convert
      * @param pFromUnit Unit you want to convert from
      * @param pTtoUnit Unit you want to convert to
      * @return Double value as a result. If it does not have the information to convert these units, then null is
      *         returned.
      */
     public Double convertValue(double value, String pFromUnit, String pTtoUnit) {
         if (this.getFromUnit().equals(pFromUnit) && this.getToUnit().equals(pTtoUnit)) {
             return new Double(convertValue(value, pFromUnit));
         }
         if (this.getFromUnit().equals(pTtoUnit) && this.getToUnit().equals(pFromUnit)) {
             return new Double(convertValue(value, pFromUnit));
         }
         return null;
     }
 
     /**
      * Returns the "Partner" conversion unit. If this converstion converts from a to b, and you pass in b, then you will
      * get a. Likewise if you pass in a, you will get b
      * 
      * @param unit String unit whose partner you are looking for.
      * @return String:partner
      */
     public String getConversionPartner(String unit) {
         if (this.getFromUnit().equals(unit)) {
             return this.getToUnit();
         } else if (this.getToUnit().equals(unit)) {
             return this.getFromUnit();
         }
         return null;
     }
 
     /**
      * Multiply this conversion by the passed in conversion
      * 
      * @param byConversion Conversion you are multiplying by
      * @return String representation of the value. I know this is a bit hokey, but it allows me to pass this back into
      *         the create method and generate another conversion if need be. This String number can be a fraction,
      *         decimal or integer.
      */
     public abstract String multiply(Conversion byConversion);
 
     /**
      * Divide this conversion by the passed in conversion
      * 
      * @param byConversion Conversion you are dividing by
      * @return String representation of the value. I know this is a bit hokey, but it allows me to pass this back into
      *         the create method and generate another conversion if need be. This String number can be a fraction,
      *         decimal or integer.
      */
     public abstract String divide(Conversion byConversion);
 
     /**
      * @return double representation of the FromToFacor - may not be applicable to all implementing classes.
      */
     public abstract double getFromToFactor();
 
     /**
      * @param fromToFactor sets the double value of the fromToFactor - may not be applicable to all implementing
      *        classes.
      */
     public abstract void setFromToFactor(double fromToFactor);
 
     /**
      * @return double value of the fromTo Offset for this conversion.
      */
     public double getFromToOffset() {
         return fromToOffset;
     }
 
     /**
      * @param fromToOffset sets the double value for the FromTo offset.
      */
     public void setFromToOffset(double fromToOffset) {
         this.fromToOffset = fromToOffset;
     }
 
     /**
      * @return Stirng FromToUnit value
      */
     public String getFromUnit() {
         return fromUnit;
     }
 
     /**
      * @param fromUnit String fromToUnit value
      */
     public void setFromUnit(String fromUnit) {
         this.fromUnit = fromUnit;
     }
 
     /**
      * @return fromTo unit abbreviation
      */
     public String getFromUnitAbbr() {
         return fromUnitAbbr;
     }
 
     /**
      * @param fromUnitAbbr String fromTo unit abbreviation
      */
     public void setFromUnitAbbr(String fromUnitAbbr) {
         this.fromUnitAbbr = fromUnitAbbr;
     }
 
     /**
      * @return to unit
      */
     public String getToUnit() {
         return toUnit;
     }
 
     /**
      * @param toUnit string to unit
      */
     public void setToUnit(String toUnit) {
         this.toUnit = toUnit;
     }
 
     /**
      * @return to unit abbreviation
      */
     public String getToUnitAbbr() {
         return toUnitAbbr;
     }
 
     /**
      * @param toUnitAbbr String to unit abbreviation
      */
     public void setToUnitAbbr(String toUnitAbbr) {
         this.toUnitAbbr = toUnitAbbr;
     }
 
     /**
      * This represents the general age for a conversion. Targeted to be used to help with significant digits.
      * 
     * @return int value representing the age of this conversion.
      */
     public int getGenerationAge() {
         return generationAge;
     }
 
     /**
      * @param generationAge sets the generation age
      */
     public void setGenerationAge(int generationAge) {
         this.generationAge = generationAge;
     }
     
     public int compareTo(Object obj) {
         if (obj instanceof Conversion) {
             Conversion o = (Conversion)obj;
             int rv =  this.getFromUnit().compareTo(o.getFromUnit());
             if (rv == 0) {
                 return this.getToUnit().compareTo(o.getToUnit());
             } else {
                 return rv;
             }
         }
         return -1;
     }
     
 
     public abstract long getFromToBottomFactor();
 
     public abstract void setFromToBottomFactor(long fromToBottomFactor);
 
     public abstract long getFromToTopFactor();
 
     public abstract void setFromToTopFactor(long fromToTopFactor);
 
     public abstract void setFromToFactorString(String factor);
 
     public abstract String getFromToFactorString();
 
 }
