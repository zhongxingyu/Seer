 /* Person.java - created on Mar 18, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package org.theeuropeanlibrary.model.common.party;
 
 import java.util.List;
 
 import org.theeuropeanlibrary.model.common.FieldId;
 import org.theeuropeanlibrary.model.common.Identifier;
 import org.theeuropeanlibrary.model.common.time.Period;
 
 /**
  * Class represents a person.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Mar 18, 2011
  */
 public class Person extends Party {
     /**
      * The period from birth until death
      */
     @FieldId(3)
     private Period lifePeriod;
 
     /**
      * A period when the person was active. These are used in library data when the life period is
      * not known
      */
     @FieldId(4)
     private Period flourishingPeriod;
 
     /**
      * The person firstNames (when it is not included in the main part of the name
      */
     @FieldId(5)
     private String firstNames;
 
     /**
      * Numerals like in: Paul IV, John II
      */
     @FieldId(6)
     private String numerals;
 
     /**
      * The person title (King, Emperor, etc)
      */
     @FieldId(7)
     private String title;
 
     /**
      * The person surname
      */
     @FieldId(8)
     private String surname;
 
     /**
      * Creates a new instance of this class.
      */
     public Person() {
         super();
     }
 
     /**
      * Creates a new instance of this class.
      * 
      * @param unstructuredName
      *            name of the person when not parsed
      */
     public Person(String unstructuredName) {
         super(unstructuredName);
         this.firstNames = null;
         this.surname = null;
         lifePeriod = null;
         flourishingPeriod = null;
     }
 
     /**
      * Creates a new instance of this class.
      * 
      * @param surname
      *            The person surname (when it is not included in the main part of the name)
      * @param firstNames
      *            name of the person (excluding surname)
      */
     public Person(String surname, String firstNames) {
         super(firstNames == null ? surname : (surname == null ? firstNames : firstNames + " " +
                                                                              surname));
         this.firstNames = firstNames;
         this.surname = surname;
         lifePeriod = null;
         flourishingPeriod = null;
     }
 
     /**
      * Creates a new instance of this class.
      * 
      * @param firstNames
      *            name of the person (excluding surname)
      * @param surname
      *            The person surname (when it is not included in the main part of the name)
      * @param lifePeriod
      *            The period from birth until death
      * @param flourishingPeriod
      *            A period when the person was active
      * @param identifiers
      */
     public Person(String surname, String firstNames, Period lifePeriod, Period flourishingPeriod,
                   List<Identifier> identifiers) {
 
         super(firstNames == null ? surname : (surname == null ? firstNames : firstNames + " " +
                                                                              surname), identifiers);
         this.firstNames = firstNames;
         this.surname = surname;
         this.lifePeriod = lifePeriod;
         this.flourishingPeriod = flourishingPeriod;
     }
 
     /**
      * @return The period from birth until death
      */
     public Period getLifePeriod() {
         return lifePeriod;
     }
 
     /**
      * @return A period when the person was active
      */
     public Period getFlourishingPeriod() {
         return flourishingPeriod;
     }
 
     /**
      * @param lifePeriod
      *            The period from birth until death
      */
     public void setLifePeriod(Period lifePeriod) {
         this.lifePeriod = lifePeriod;
     }
 
     /**
      * @param flourishingPeriod
      *            A period when the person was active
      */
     public void setFlourishingPeriod(Period flourishingPeriod) {
         this.flourishingPeriod = flourishingPeriod;
     }
 
     /**
      * @return Numerals like in: Paul IV, John II
      */
     public String getNumerals() {
         return numerals;
     }
 
     /**
      * @param numerals
      *            Numerals like in: Paul IV, John II
      */
     public void setNumerals(String numerals) {
         this.numerals = numerals;
     }
 
     /**
      * @return The person title (King, Emperor, etc)
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * @param title
      *            The person title (King, Emperor, etc)
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * @return The person firstNames (when it is not included in the main part of the name
      */
     public String getFirstNames() {
         return firstNames;
     }
 
     /**
      * @param firstNames
      *            The person firstNames (when it is not included in the main part of the name
      */
     public void setFirstNames(String firstNames) {
         this.firstNames = firstNames;
     }
 
     /**
      * @return The surname part of the name
      */
     public String getSurname() {
         return surname;
     }
 
     /**
      * @return a string representation of the party for end user display
      */
     @Override
     public String getDisplay() {
         String ret = "";
         if (partyName != null) {
             ret = partyName;
         } else {
             if (surname != null) {
                 ret = surname;
             }
             if (firstNames != null) {
                 if (ret.isEmpty()) {
                     ret = firstNames;
                 } else {
                     ret += ", " + firstNames;
                 }
             }
             if (numerals != null) {
                 ret += ", " + numerals;
             }
             if (title != null) {
                 ret += ", " + title;
             }
             if (lifePeriod != null) {
                 ret += ", " + lifePeriod.getDisplay();
             }
         }
         return ret;
     }
 
     /**
      * @return a string representation of the person name, in the format "surname, first names" (if
      *         available in this form in the object)
      */
     public String getFullNameInverted() {
         if (firstNames == null && surname == null) {
             return partyName;
         } else {
             if (firstNames == null) {
                 return surname;
             } else {
                if (surname != null) { return surname.trim() + ", " + firstNames.trim(); }
                 return firstNames;
             }
         }
     }
 
     /**
      * @return a string representation of the person name, in the format "first names surname" (if
      *         available in this form in the object)
      */
     public String getFullName() {
         if (firstNames == null && surname == null) {
             return partyName;
         } else {
             if (firstNames == null) {
                 return surname;
             } else {
                 if (surname != null) { return firstNames.trim() + " " + surname.trim(); }
                 return firstNames;
             }
         }
     }
 
     /**
      * Sets the surname to the given value.
      * 
      * @param surname
      *            the surname to set
      */
     public void setSurname(String surname) {
         this.surname = surname;
     }
 
     /**
      * Splits the name by whitespace and returns a String with all the first letters from the names.
      * the output for "Jonh Smith" is "JS"
      * 
      * @return name initials
      */
     public String getNameInitials() {
         String fullname = getFullName();
         String[] split = fullname.split(" ");
         String initials = "";
         for (String n : split) {
             if (n.length() > 0) initials += n.charAt(0);
         }
         return initials;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((partyName == null) ? 0 : partyName.hashCode());
         result = prime * result + ((firstNames == null) ? 0 : firstNames.hashCode());
         result = prime * result + ((flourishingPeriod == null) ? 0 : flourishingPeriod.hashCode());
         result = prime * result + ((lifePeriod == null) ? 0 : lifePeriod.hashCode());
         result = prime * result + ((numerals == null) ? 0 : numerals.hashCode());
         result = prime * result + ((surname == null) ? 0 : surname.hashCode());
         result = prime * result + ((title == null) ? 0 : title.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (!super.equals(obj)) return false;
         if (getClass() != obj.getClass()) return false;
         Person other = (Person)obj;
         if (partyName == null) {
             if (other.partyName != null) return false;
         } else if (!partyName.equals(other.partyName)) return false;
         if (firstNames == null) {
             if (other.firstNames != null) return false;
         } else if (!firstNames.equals(other.firstNames)) return false;
         if (flourishingPeriod == null) {
             if (other.flourishingPeriod != null) return false;
         } else if (!flourishingPeriod.equals(other.flourishingPeriod)) return false;
         if (lifePeriod == null) {
             if (other.lifePeriod != null) return false;
         } else if (!lifePeriod.equals(other.lifePeriod)) return false;
         if (numerals == null) {
             if (other.numerals != null) return false;
         } else if (!numerals.equals(other.numerals)) return false;
         if (surname == null) {
             if (other.surname != null) return false;
         } else if (!surname.equals(other.surname)) return false;
         if (title == null) {
             if (other.title != null) return false;
         } else if (!title.equals(other.title)) return false;
         return true;
     }
 }
