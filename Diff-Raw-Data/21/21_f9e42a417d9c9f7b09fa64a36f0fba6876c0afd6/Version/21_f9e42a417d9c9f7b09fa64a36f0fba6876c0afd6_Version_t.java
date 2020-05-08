 package de.skuzzle.polly.sdk;
 
 
 /**
  * This class represents a three-part version number. The parts are called
  * VERSION, REVISION and BUILD and are integer fields. A Version has a natural order,
  * which is given by the values of its parts in the order from VERSION to BUILD.  
  * 
  * @author Simon
  * @version 28.07.2011
  * @since Beta 0.9
  * @see #compareTo(Version)
  */
 public class Version implements Comparable<Version> {
 
     /**
      * The highest field part.
      */
     public final static int VERSION = 0;
     
     /**
      * The middle field part.
      */
     public final static int REVISION = 1;
     
     /**
      * The lowest field part.
      */
     public final static int BUILD = 2;
     
     
     
     private int[] fields;
     private String versionString;
     
     
     
     /**
      * Creates a new version from a String. The String must follow exactly this 
      * formatting scheme: It must have three integer parts separated by a '.' (dot).
      * For example {@code 1.3.4} as well as {@code 0.2.1} and {@code 0.0.0} are valid
      * version strings.
      * 
      * @param version The version string to parse.
      * @throws IllegalArgumentException If {@code version} does not follow the formmating
      *      rules.
      */
     public Version(String version) {
        String[] parts = version.split("\\.");
         if (parts.length != 3) {
             throw new IllegalArgumentException("misformatted version string: " + version);
         }
         try {
             this.fields = new int[] {Integer.parseInt(parts[VERSION]), 
                                      Integer.parseInt(parts[REVISION]), 
                                      Integer.parseInt(parts[BUILD])};
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException("misformatted version string: " + version);
         }
 
         this.versionString = version;
     }
     
     
     
     /**
      * Creates a new version from its three integer parts.
      * 
      * @param version The VERSION part.
      * @param rev The REVISION part.
      * @param build The BUILD part.
      */
     public Version(int version, int rev, int build) {
         this.fields = new int[] {version, rev, build};
         this.versionString = version + "." + rev + "." + build;
     }
     
     
     
     /**
      * Returns the value of any version field. Valid field values are: {@link #VERSION},
      * {@link #REVISION} and {@link #BUILD}.
      * @param field The version field.
      * @return The version value for that field.
      * @throws IndexOutOfBoundsException If {@code field} parameter is invalid.
      */
     public int get(int field) {
         if (field < VERSION || field > BUILD) {
             throw new IndexOutOfBoundsException("" + field);
         }
         return this.fields[field];
     }
     
     
     
     /**
      * Return this version as a String.
      * @return A String representation for this Version.
      */
     @Override
     public String toString() {
         return this.versionString;
     }
     
     
     
     /**
      * Considers {@code obj} equals to {@code this} iff all three version parts
      * equals.
      * @return <code>true</code> if {@code obj} is equal to {@code this}.
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         } else if (!(obj instanceof Version)) {
             return false;
         }
         Version other = (Version) obj;
         return other.compareTo(this) == 0;
     }
     
     
     
     /**
      * Compares {@code this} to another Version. The ordering of Versions is defined as
      * follows: The version with the higher value in a higher field is considered greater
      * than the other. They are considered equal if all three version parts matches. The
      * highest field is {@link #VERSION}, the lowest field is {@link #BUILD}.
      * 
      * @param o The version to compare this vesion with.
      * @return An integer representing the order of these Versions in the means of the 
      *      Comparable contract.
      */
     @Override
     public int compareTo(Version o) {
         for (int i = 0; i < this.fields.length; ++i) {
             int diff = this.get(i) - o.get(i);
             if (diff != 0) {
                 return diff;
             }
         }
         return 0;
     }
 }
