 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.novelang;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.Comparator;
 
 /**
  * Represents a version number with the "major + minor + fix" scheme, and also the version
  * of the whole application. 
  * By default the version is "SNAPSHOT" and the build process replaces a magic string by the
  * version number of the release.
  * In order to replace the magic string at build time, this class compiles separately so it
  * shouldn't depend on other Novelang classes. 
  * 
  * @author Laurent Caillette
  */
 public final class Version {
 
   private final boolean snapshot ;
   private final int major ;
   private final int minor ;
   private final int fix ;
 
   public Version() {
     snapshot = true ;
     this.major = -999 ;
     this.minor = -999 ;
     this.fix = -999 ;    
   }
 
   public Version( final int major, final int minor, final int fix ) {
     checkArgument( "major number should be >= 0, it is " + major, major >= 0 ) ;
     checkArgument( "minor number should be >= 0, it is " + minor, minor >= 0 ) ;
     checkArgument( "fix number should be >= 0, it is " + fix, fix >= 0 ) ;
     snapshot = false ;
     this.major =  major ;
     this.minor = minor ;
     this.fix = fix ;
   }
 
   /**
    * Replaces {@link com.google.common.base.Preconditions} because when rebuilding the class
    * with modified {@link #PRODUCT_VERSION_AS_STRING} we don't want to carry additional
    * dependencies. 
    */
   private static void checkArgument( final String message, final boolean condition ) {
     if( ! condition ) {
       throw new IllegalArgumentException( message ) ;
     }
   }
 
   public int getMajor() {
     verifyNotSnapshot() ;
     return major ;
   }
 
   public int getMinor() {
     verifyNotSnapshot() ;
     return minor ;
   }
 
   public int getFix() {
     verifyNotSnapshot() ;
     return fix ;
   }
   
   private void verifyNotSnapshot() {
     if( isSnapshot() ) {
       throw new IllegalStateException( "snapshot version" ) ;
     }
   }
 
   /**
    * This very string is replaced by the official version number by the build script.
    */
  private static final String PRODUCT_VERSION_AS_STRING = "${project.version}" ;
 
   /**
    * Current version, reflects changes performed by build script.
    */
   public static final Version CURRENT_PRODUCT_VERSION ;
 
   /**
    * This initialization must happen before parsing the version, otherwise we hit
    * a {@code NullPointerException}.
    */
   private static final Pattern PATTERN = Pattern.compile( "(\\d+)\\.(\\d+)\\.(\\d+)" ) ;
 
   static {
     try {
       CURRENT_PRODUCT_VERSION = parse( PRODUCT_VERSION_AS_STRING ) ;
     } catch ( VersionFormatException e ) {
       throw new RuntimeException( e ) ;
     }
   }
 
   private static final String SNAPSHOT = "SNAPSHOT" ;
 
   public boolean isSnapshot() {
     return snapshot ;
   }
 
   public String getName() {
     return isSnapshot() ? SNAPSHOT : major + "." + minor + "." + fix ;
   }
   
   public static Version parse( final String s ) throws VersionFormatException {
     if( ( "@" + "project.version" + "@" ).equals( s ) || SNAPSHOT.equals( s ) ) {
       return new Version() ;
     } else {
       final Matcher matcher = PATTERN.matcher( s ) ;
       if( matcher.find() ) {
         final int major = Integer.parseInt( matcher.group( 1 ) ) ;
         final int minor = Integer.parseInt( matcher.group( 2 ) ) ;
         final int fix = Integer.parseInt( matcher.group( 3 ) ) ;
         return new Version( major, minor, fix ) ;
       } else {
         throw new VersionFormatException( s ) ;
       }
     }
   }
 
   /**
    * Compares two {@code Version} objects on their numbers; a SNAPSHOT is considered as the
    * greater (most recent). 
    */
   public static final Comparator< Version > COMPARATOR = new Comparator< Version >() {
     
     /**
      * Compares its two arguments for order. Returns a negative integer, zero, or a positive 
      * integer as the first argument is less than, equal to, or greater than the second.
      */
     @Override
     public int compare( final Version version1, final Version version2 ) {
       
       if( version1 == version2 ) {
         return 0 ;
       }
       if( version1 == null ) {
         return -1 ;
       }
       if( version2 == null ) {
         return 1 ;
       }
 
       if( version1.isSnapshot() ) {
         if( version2.isSnapshot() ) {
           return 0 ;
         } else {
           return 1 ;
         }
       } else if( version2.isSnapshot() ) {
         return -1 ;
       }
       
       final int majorDifference = version1.getMajor() - version2.getMajor() ;
       if( majorDifference == 0 ) {
         final int minorDifference = version1.getMinor() - version2.getMinor() ;
         if( minorDifference == 0 ) {
           return version1.getFix() - version2.getFix() ;          
         } else {
           return minorDifference ;
         } 
       } else {
         return majorDifference ;
       }
     }
   } ;
 }
