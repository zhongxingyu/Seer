 package info.jlibrarian.stringutils; /* Original source code (c) 2013 C. Ivan Cooper. Licensed under GPLv3, see file COPYING for terms. */
 
 
 import java.util.Comparator;
 
 /**
  * Represents a comparable version string, also static methods for comparing Strings that are version strings.
  * 
  * compare two "version strings" supporting the special wildcards * and +.
  * the + wildcard is only valid as the last character of the version string
  * 
  * if any wildcards are used, this comparison is symmetric and reflexive, 
  * but it is NOT transitive -
  * e.g. a=b and b=c but maybe not a=c
  * 
  * If no wildcards are used, the comparison is also transitive.
  * 
  * 2.4.blarg.0.3 equals 2.4.bla*.0.*
  * 2.* equals 2.4
  * 2 less than 2.4
  * 2.4+ equals 2.4.0
  * 2.4+ equals 3.0
  * 
  * @author ivan
  */
 public class VersionString implements Comparable<VersionString>,Comparator<String> {
     String version;
 
     public VersionString(String version) {
         this.version = version;
     }
     
     static public int compareVersions(String vA, String vB) {
         if(vA==null && vB==null) {
             return 0;
         } 
         if(vA==null) {
             return -1; // non-null is greater than null
         }
         if(vB==null) {
             return 1;
         }
 
         boolean aPlus,bPlus;
         if(vA.endsWith("+")) {
             aPlus=true;
             vA=vA.substring(0, vA.length()-1);
         } else {
             aPlus=false;
         }
         if(vB.endsWith("+")) {
             bPlus=true;
             vB=vB.substring(0, vB.length()-1);
         } else {
             bPlus=false;
         }
 
         String[] vA_strings=vA.split("\\.");  
         String[] vB_strings=vB.split("\\.");
         int numA,numB;
         
         for(int ix=0; ix<vA_strings.length && ix<vB_strings.length; ix++) {
             try {
                 numA=Integer.parseInt(vA_strings[ix]);
                 numB=Integer.parseInt(vB_strings[ix]);
             } catch (NumberFormatException ex) {
                 numA=WildcardString.compareWildcardString(vA_strings[ix], vB_strings[ix]); 
                 numB=0;
             }
             if(numA!=numB) {
                 if(  ((numA<numB) && (aPlus))
                         || ((numB<numA) && (bPlus)) )
                     return 0;
                 return numA-numB;
             }
             if(vA_strings[ix].indexOf("*")>=0 || vB_strings[ix].indexOf("*")>=0 )
                 return 0; // after a wildcard processed, discard remainder
         }
         numA=vA_strings.length;
         numB=vB_strings.length;
         if(  ((numA<numB) && (aPlus))
             || ((numB<numA) && (bPlus)) )
             return 0;
 
         return vA_strings.length - vB_strings.length;
     }
 
     /**
      * generalizes a wildcard version string.
      * "1.2.foo" becomes "1.2.*"
      * "1.2.*" becomes "1.*"
      * "1.*" becomes "*"
      * "*" becomes "*"
      * "foo" becomes "*"
      * 
      * @param v
      * @return
      */
     static public String generalizeVersion(String v) {
         if (v==null)
             return "*";
         String[] vs=v.split(".");
         int ix=v.length()-1;
         if(ix<=1)
             return "*";
         while(ix>0 && vs[ix].equals("*"))
             ix--; // skip any * fields starting from the end
         String new_v="";
         for(int i=0;i<ix;i++) { // copy all fields up to but not including this
             new_v += vs[ix]+"."; 
         }
         new_v+="*"; // last field is a *
         return new_v;
     }
     
     /**
      * compare two version strings as described in VersionString (violates transitivity of equality)
      * 
      * @param s1
      * @param s2
      * @return result of comparison
      */
     public int compare(String s1, String s2) {
         return compareVersions(s1,s2);
     }
     
     /**
      * compare to other VersionString (violates transitivity of equality)
      * @param arg0
      * @return result of comparison
      */
     public int compareTo(VersionString arg0) {
         if(arg0==null)
             throw new NullPointerException("cannot compare VersionString to null");
         return compareVersions(version,arg0.toString());
     }
     @Override
     public String toString() {
         return version;
     }
     /**
      * Check whether this matches another version string (violates transitivity of equality)
      * @param obj
      * @return result of comparison
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final VersionString other = (VersionString) obj;
         if (this.version != other.version && (this.version == null || !this.version.equals(other.version))) {
             return false;
         }
         return true;
     }
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 31 * hash + (this.version != null ? this.version.hashCode() : 0);
         return hash;
     }
 }
