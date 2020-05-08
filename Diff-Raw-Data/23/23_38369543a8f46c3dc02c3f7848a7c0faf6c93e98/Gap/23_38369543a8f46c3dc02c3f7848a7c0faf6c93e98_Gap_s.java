 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.annotators;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class Gap {
     /** The set of invalid answers. */
     private Set<String> invalidAnswers = new HashSet<String>();
 
     /** The set of valid answers. */
     private Set<String> validAnswers = new HashSet<String>();
 
     /**
      * Returns the union of all valid and invalid answers for this gap.
      *
     * @return valid and invalid answers
      */
     public Set<String> getAll() {
        Set<String> all = new HashSet<String>();
         all.addAll(getInvalidAnswers());
         all.addAll(getValidAnswers());
         return all;
     }
 
     /**
     * @return the invalidAnswers
      */
     public Set<String> getInvalidAnswers() {
         return invalidAnswers;
     }
 
     /**
     * @param invalidAnswers the invalidAnswers to set
      */
     public void setInvalidAnswers(final Set<String> invalidAnswers) {
         this.invalidAnswers = invalidAnswers;
     }
 
     /**
     * @return the validAnswers
      */
     public Set<String> getValidAnswers() {
         return validAnswers;
     }
 
     /**
     * @param validAnswers the validAnswers to set
      */
     public void setValidAnswers(final Set<String> validAnswers) {
         this.validAnswers = validAnswers;
     }
 }
 
