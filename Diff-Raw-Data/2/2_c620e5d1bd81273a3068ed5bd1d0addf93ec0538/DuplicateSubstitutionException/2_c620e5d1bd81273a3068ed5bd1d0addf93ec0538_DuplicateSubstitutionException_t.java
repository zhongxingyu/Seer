 /*
     jaiml - java AIML library
     Copyright (C) 2004, 2009  Kim Sullivan
 
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package aiml.substitutions;
 
 /**
  * This exception is thrown whenever a substitution with an identical pattern is
  * added to an instance of Substitutions.
  * 
  * @author Kim Sullivan
  * 
  */
 public class DuplicateSubstitutionException extends Exception {
 
   public DuplicateSubstitutionException(String pattern, String replacement) {
     super(
         String.format(
            "Substitution map already contains the replacement \"%s\" for the pattern \"%s\"",
             replacement, pattern));
   }
 
 }
