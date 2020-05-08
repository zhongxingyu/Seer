 /* InstantGranularity.java - created on 22 de Mar de 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package org.theeuropeanlibrary.model.common.time;
 
 /**
  * Defines the level of detail to which an instant is defined. When associated with a
  * java.util.Date, it indicated up to which level the date is significant.
  * 
  * @author Nuno Freire (nfreire@gmail.com)
  * @since 22 de Mar de 2011
  */
 public enum InstantGranularity {
     /**
      * The date is only significant in the millennium. Ex: To be used when dates like "19uu" or
      * "19--" are found in MARC data
      */
     MILLENNIUM,
     /**
      * The date is only significant in the millennium and century. Ex: To be used when dates like
      * "19uu" or "19--" are found in MARC data
      */
     CENTURY,
     /**
      * The date is only significant in the millennium, century and decade. Ex: To be used when dates
      * like "195u" or "195-" are found in MARC data Could also be used to represent textual
      * expressions like "20th century"
      */
     DECADE,
     /**
      * Only the specified year is significant Ex: "1951", "1975", "2011"
      */
     YEAR,
     /**
      * The date is significant from millenium to month Ex: "August 2010", "2010-08"
      */
     MONTH,
     /**
      * The date is significant from millenium to the day Ex: "2nd August 2010", "2010-08-02"
      */
     DAY,
     /**
      * The date is significant from millenium to the minute Ex: "2010-08-02 12.30"
      */
     MINUTE,
     /**
      * The date is significant from millenium to the second Ex: "2010-08-02 12.30.30"
      */
     SECOND,
     /**
      * The date is significant from millenium to the milisecond Ex: "2010-08-02 12.30.30:999"
      */
     MILLISECOND,
     /**
      * The date is unknown. None of the parts of the date are significant. Ex: when in MARC data we
      * find "?"
      */
    UNKNOWN;
    
    
    /**
     * @param other
     * @return true if this level of granularity has more detail than the other
     */
    public boolean isMoreDetailedThan(InstantGranularity other) {
        return this!=UNKNOWN && this.ordinal() > other.ordinal();
    }
    
 }
