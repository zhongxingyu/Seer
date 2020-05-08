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
 package com.github.fhirschmann.clozegen.lib.generator;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Objects.ToStringHelper;
 import com.google.common.collect.Sets;
 import java.util.Set;
 
 /**
  * A gap object contains valid and invalid answers.
  *
  * <p>
  * A gap provides multiple convenience methods. The following gaps are all equal:
  * <blockquote><pre>
  * {@code
  * Gap gap1 = new Gap.with("in", "of", "at");
  *
  * Gap gap2 = new Gap();
  * gap2.addValidAnswer("in");
  * gap2.addInvalidAnswers("of", "at");
  * }
  * </pre></blockquote>
 * </p>
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class Gap {
     /**
      * The set of invalid answers.
      */
     private Set<String> invalidAnswers;
 
     /**
      * The set of valid answers.
      */
     private Set<String> validAnswers;
 
     /**
      * Constructs a new empty gap.
      */
     public Gap() {
         invalidAnswers = Sets.newHashSet();
         validAnswers = Sets.newHashSet();
     }
 
     /**
      * Constructs a gap prefilled with the given answers.
      *
      * @param invalidAnswers set of invalid answers
      * @param validAnswers set of valid answers
      */
     public Gap(final Set<String> validAnswers, final Set<String> invalidAnswers) {
         this();
         this.validAnswers.addAll(validAnswers);
         this.invalidAnswers.addAll(invalidAnswers);
     }
 
     /**
      * Returns the set of invalid answers.
      *
      * @return set of invalid answers.
      */
     public Set<String> getInvalidAnswers() {
         return invalidAnswers;
     }
 
     /**
      * Adds invalid answers to this gap.
      *
      * @param invalidAnswers the invalid answers to set
      */
     public void addInvalidAnswers(final Set<String> invalidAnswers) {
         this.invalidAnswers.addAll(invalidAnswers);
     }
 
     /**
      * Adds invalid answers to this gap.
      *
      * @param invalidAnswers the invalid answers to set
      */
     public void addInvalidAnswers(final String... invalidAnswers) {
         addInvalidAnswers(Sets.newHashSet(invalidAnswers));
     }
 
     /**
      * Returns the set of valid answers.
      *
      * @return set of valid answers.
      */
     public Set<String> getValidAnswers() {
         return validAnswers;
     }
 
     /**
      * Sets the valid answers.
      *
      * @param validAnswers the valid answers to set
      */
     public void addValidAnswers(final Set<String> validAnswers) {
         this.validAnswers.addAll(validAnswers);
     }
 
     /**
      * Sets the valid answers.
      *
      * @param validAnswers the invalid answers to set
      */
     public void addValidAnswers(final String... validAnswers) {
         addValidAnswers(Sets.newHashSet(validAnswers));
     }
 
     /**
      * Sets the valid answer to a single answer.
      *
      * @param validAnswer the valid answer to set
      */
     public void addValidAnswer(final String validAnswer) {
         addValidAnswers(new String[] {validAnswer});
     }
 
     /**
      * Returns an unmodifiable view of all answers (the union of invalid and valid
      * answers).
      *
      * @return set of invalid and valid answers
      */
     public Set<String> getAllAnswers() {
         return Sets.union(getValidAnswers(), getInvalidAnswers());
     }
 
     /**
      * Convenience method for generating gaps.
      *
      * @param validAnswer the valid answers of this gap
      * @return a new gap based upon the parameters provided
      */
     public static Gap with(final String validAnswer) {
         Gap gap = new Gap();
         gap.addValidAnswer(validAnswer);
 
         return gap;
     }
 
     /**
      * Convenience method for generating gaps.
      *
      * @param validAnswer the valid answers of this gap
      * @param invalidAnswers the invalid answers of this gap
      * @return a new gap based upon the parameters provided
      */
     public static Gap with(final String validAnswer, final String... invalidAnswers) {
         Gap gap = Gap.with(validAnswer);
         gap.addInvalidAnswers(invalidAnswers);
 
         return gap;
     }
 
     @Override
     public String toString() {
         final ToStringHelper str = Objects.toStringHelper(this);
         str.add("valid", getValidAnswers().toString());
         str.add("invalid", getInvalidAnswers().toString());
 
         return str.toString();
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(getInvalidAnswers(), getValidAnswers());
     }
 
     @Override
     public boolean equals(final Object obj) {
         if ((obj == null) || (!getClass().equals(obj.getClass()))) {
             return false;
         }
         final Gap other = (Gap) obj;
 
         return Objects.equal(getInvalidAnswers(), other.getInvalidAnswers())
                 && Objects.equal(getValidAnswers(), other.getValidAnswers());
     }
 }
