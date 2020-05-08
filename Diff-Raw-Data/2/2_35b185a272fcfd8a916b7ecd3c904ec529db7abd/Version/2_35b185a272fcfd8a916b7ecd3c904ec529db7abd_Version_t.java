 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011-2012, CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons.classes;
 
 import java.util.regex.Pattern;
 
 /**
  * A class that represents a version.
  */
 public class Version {
     private final String version;
     private final String separator;
 
     /**
      * Parses a version from string using default version format (separated with <code>.</code> char).
      * 
      * @param version  the string to parse
      */
     public Version(String version) {
         this(version, ".");
     }
 
     /**
      * Parses a version from string using given separator
      * 
      * @param version    the string to parse
      * @param separator  the separator used in <code>version</code> parameter
      */
     public Version(String version, String separator) {
         this.version = version;
         this.separator = separator;
     }
 
     /**
      * Returns the string that was used to create the version.
      * 
      * @return a string representation of the version
      */
     public String getString() {
         return this.version;
     }
 
     /**
      * Returns a string representation of the version using given <code>separator</code>.
      * 
      * @param separator  the separator to use
      * @return           a string representation of the version
      */
     public String getString(String separator) {
         String[] array = this.getArray();
         if (array == null || array.length == 0) {
             return null;
         }
         StringBuilder builder = new StringBuilder(array[0]);
        for (int i = 1; i < array.length; ++i) {
             builder.append(separator).append(array[i]);
         }
         return builder.toString();
     }
 
     /**
      * Returns an array of string elements of the version.
      * <p>
      * For example <code>new Version("1.5.3").getArray()</code> returns an array of <code>"1"</code>, <code>"5"</code>, <code>"3"</code>.
      * 
      * @return  an array representation of the version
      */
     public String[] getArray() {
         return this.version.split(Pattern.quote(this.separator));
     }
 
     /**
      * Returns the separator that was used to create this version.
      * 
      * @return  the separator
      */
     public String getSeparator() {
         return this.separator;
     }
     
     /**
      * Compares two versions.
      * <p>
      * Note: If common parts of versions are equal and this version is longer than <code>anotherVersion</code>, it means this one is bigger, but if <code>anotherVersion</code> is longer than this one,
      *       it means they are equal.
      * 
      * @param anotherVersion  the version to be compared
      * @return                the value <code>1</code> if this version is greater than the version argument; the value <code>-1<code> if less; the value <code>0</code> if equal
      */
     public int compareTo(Version anotherVersion) {
         String[] splitA = this.getArray();
         String[] splitB = anotherVersion.getArray();
         int len = Math.min(splitA.length, splitB.length);
         for (int i = 0; i < len; ++i) {
             // TODO: Catch NumberFormatException somewhere or add throws declaration.
             int a = Integer.parseInt(splitA[i]);
             int b = Integer.parseInt(splitB[i]);
             if (a > b) {
                 return 1;
             } else if (a < b) {
                 return -1;
             }
         }
         if (splitA.length > splitB.length) {
             return 1;
         }
         return 0;
     }
 
     /**
      * Checks if the version is within given <code>range</code>.
      * 
      * @param range  a range to check the version with
      * @return       <code>true</code> if the version is in range, <code>false</code> if not
      * @see          VersionRange#inVersionRange(Version)
      */
     public boolean inVersionRange(VersionRange range) {
         return range.inVersionRange(this);
     }
     
     /* (non-Javadoc)
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == this) {
             return true;
         }
         if (obj instanceof Version) {
             return ((Version) obj).getString(this.separator).equalsIgnoreCase(this.version);
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return getString(".").hashCode();
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return this.getString();
     }
 }
