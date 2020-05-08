 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.lucene;
 
 import com.google.common.base.Preconditions;
 
 import de.cosmocode.patterns.Builder;
 
 /**
  * This class is a Builder for a {@link QueryModifier}.
  * An object of this class is mutable, but the resulting {@link QueryModifier} 
  * of the {@link #end()} method is immutable.
  * 
  * @author Oliver Lorenz
  */
 public final class ModifierBuilder implements Builder<QueryModifier> {
     
     private TermModifier tm;
     private boolean s;
     private boolean d;
     private boolean wc;
     private Double fuzzy;
     
     
     public ModifierBuilder() {
         this.tm = TermModifier.NONE;
     }
     
     
     /**
      * Set the {@link TermModifier}.
      * @param termModifier the {@link TermModifier} to set.
      * @return this
      * @throws NullPointerException if termModifier is null
      */
     public ModifierBuilder setTermModifier(final TermModifier termModifier) {
         this.tm = Preconditions.checkNotNull(termModifier, QueryModifier.ERR_TERMMOD_NULL);
         return this;
     }
     
     /**
      * Set split.
      * <br> If true, then all added fields and arguments to a {@link LuceneQuery} are
      * - additionally to being added normally - split at blanks and each fragment is added to the query.
      * @param isSplit whether or not 
      * @return this
      */
     public ModifierBuilder setSplit(final boolean isSplit) {
         this.s = isSplit;
         return this;
     }
     
     /**
      * Set disjunct.
      * <br> If disjunct is true then multiple values added to a field on a {@link LuceneQuery}
      * are added in disjunction (or-clause, one of the values must be found).
      * <br> Otherwise multiple values in a field on a {@link LuceneQuery}
      * are added in conjunction (and-clause, all values must be found).
      * @param disjunct if true then values are disjunct (or-clause), otherwise conjunct (and-clause)
      * @return this
      */
     public ModifierBuilder setDisjunct(final boolean disjunct) {
         this.d = disjunct;
         return this;
     }
     
     /**
      * Set wildCarded.
      * <br> If true then values added to a {@link LuceneQuery} are searched wildcarded.
      * That means: If the user searches for "abc", then the Query is built so that
      * Lucene searches for everything that starts with "abc", or is "abc" itself.
      * <br> In Lucene's Query Language this is: abc* "abc"
      * <br> Otherwise values are added normally.
      * @param wildCarded whether values should be wildcarded or not
      * @return this
      */
     public ModifierBuilder setWildcarded(final boolean wildCarded) {
         this.wc = wildCarded;
         return this;
     }
     
     /**
      * Set fuzzyness to given value.
      * @param fuzzyness the fuzzyness to set
      * @return this
      * @throws IllegalArgumentException if fuzzyness < 0 || fuzzyness >= 1
      */
     public ModifierBuilder setFuzzyness(final Double fuzzyness) {
         Preconditions.checkArgument(
             fuzzyness == null || (fuzzyness >= 0 && fuzzyness < 1),
             QueryModifier.ERR_FUZZYNESS_INVALID
         );
         this.fuzzy = fuzzyness;
         return this;
     }
     
     
     /**
      * <p> This is a shortcut for {@code setTermModifier(TermModifier.REQUIRED)}.
      * </p>
     * TODO add consequences for this call
      * @return this
      * @see TermModifier#REQUIRED
      * @see #setTermModifier(TermModifier)
      */
     public ModifierBuilder required() {
         this.setTermModifier(TermModifier.REQUIRED);
         return this;
     }
     
     /**
      * <p> This is a synonym for {@link #prohibited()}.
      * </p> 
      * @return this
      * @see #prohibited()
      */
     public ModifierBuilder excluded() {
         this.setTermModifier(TermModifier.PROHIBITED);
         return this;
     }
 
     /**
      * <p> This is a shortcut for {@code setTermModifier(TermModifier.PROHIBITED)}.
      * </p> 
     * TODO add consequences 
      * @return this
      * @see TermModifier#PROHIBITED
      */
     public ModifierBuilder prohibited() {
         this.setTermModifier(TermModifier.PROHIBITED);
         return this;
     }
     
     /**
      * Set wildCarded to true.
      * <br> Values added to a {@link LuceneQuery} are searched wildcarded.
      * That means: If the user searches for "abc", then the Query is built so that
      * Lucene searches for everything that starts with "abc", or is "abc" itself.
      * <br> In Lucene's Query Language this is: abc* "abc"
      * @return this
      */
     public ModifierBuilder wildcarded() {
         this.wc = true;
         return this;
     }
     
     /**
      * Set wildCarded to false.
      * <br> Values added to a {@link LuceneQuery} are searched normally, without wildcards.
      * @return this
      */
     public ModifierBuilder notWildcarded() {
         this.wc = false;
         return this;
     }
     
     /**
      * Set split to true. 
      * <br> That means that all added fields and arguments to the {@link LuceneQuery} are 
      * - additionally to being added normally - split at blanks and each fragment is added to the query.
      * @return this
      */
     public ModifierBuilder doSplit() {
         this.s = true;
         return this;
     }
     
     /**
      * Set split to false.
      * <br> All added fields and arguments to a {@link LuceneQuery} are added normally.
      * @return this
      */
     public ModifierBuilder dontSplit() {
         this.s = false;
         return this;
     }
     
     /**
      * Set disjunct to true.
      * <br> This means: multiple values added to a field on a {@link LuceneQuery}
      * are added in disjunction (or-clause, one of the values must be found).
      * @return this
      */
     public ModifierBuilder disjunct() {
         this.d = true;
         return this;
     }
     
     /**
      * Set disjunct to false.
      * <br> This means: Multiple values in a field on a {@link LuceneQuery}
      * are added in conjunction (and-clause, all values must be found).
      * @return this
      */
     public ModifierBuilder conjunct() {
         this.d = false;
         return this;
     }
     
     /**
      * Disables fuzzyness (the default state of a freshly initialized {@link ModifierBuilder}).
      * @return this
      */
     public ModifierBuilder noFuzzyness() {
         this.fuzzy = null;
         return this;
     }
     
     /**
      * Builds the {@link QueryModifier} that this {@link ModifierBuilder} represents.
      * @return the finished {@link QueryModifier}
      */
     public QueryModifier end() {
         return build();
     }
     
     @Override
     public QueryModifier build() {
         return new QueryModifier(tm, s, d, wc, fuzzy);
     }
 }
