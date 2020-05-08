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
 
 import java.lang.reflect.Array;
 import java.util.ArrayDeque;
 import java.util.Collection;
 import java.util.Deque;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.common.base.Preconditions;
 
 /**
  * <p>
  * A default implementation of a LuceneQuery.
  * This implementation is not threadsafe.
  * </p>
  * 
  * @since 1.0
  * @author Oliver Lorenz
  */
 public final class DefaultLuceneQuery extends AbstractLuceneQuery implements LuceneQuery {
     
     
     private final StringBuilder queryArguments;
     
     private final Deque<Integer> positionStack = new ArrayDeque<Integer>(8);
     
     public DefaultLuceneQuery() {
         this.queryArguments = new StringBuilder();
     }
 
     @Override
     public String getQuery() {
         Preconditions.checkState(this.queryArguments.length() > 0, ERR_EMPTY_QUERY);
         return this.queryArguments.toString();
     }
     
 
     /* ---------------------------
      *     addArgument-methods
      */
     
     /**
      * search for input wildcarded (wildcard is appended at the end). 
      * the original input is added, too, because e.g. adidas* doesn't match "adidas" on text-fields
      * @param value the String value to add wildcarded
      */
     private void addWildcarded(final String value) {
         queryArguments.
             append("(").
             append("\"").append(LuceneHelper.escapeQuotes(value)).append("\"").
             append(" ").append(LuceneHelper.escapeAll(value)).append("*").
             append(")");
     }
     
     private void addFuzzy(final String value, final double fuzzyness) {
         queryArguments.
             append(LuceneHelper.escapeAll(value)).
             append("~").append(fuzzyness);
     }
     
     private void addWildcardedFuzzy(final String value, final double fuzzyness) {
         queryArguments.
             append("(").
             append("\"").append(LuceneHelper.escapeQuotes(value)).append("\"").
             append(" ").append(LuceneHelper.escapeAll(value)).append("*").
             append(" ").append(LuceneHelper.escapeAll(value)).
             append("~").append(fuzzyness).
             append(")");
     }
     
     private void addSplitted(final String value, final QueryModifier modifier) {
         final QueryModifier subModifier = modifier.getMultiValueModifier().copy().dontSplit().end();
         queryArguments.append(" (");
         for (final String token : value.split(" ")) {
             this.addArgument(token, subModifier);
         }
         queryArguments.append(")^0.5 ");
     }
     
     
     @Override
     public DefaultLuceneQuery addArgument(final String value, final QueryModifier modifier) {
         if (StringUtils.isBlank(value)) {
             setLastSuccessful(false);
             return this;
         }
         
         queryArguments.append(modifier.getTermPrefix());
         queryArguments.append("(");
         
         if (modifier.isWildcarded() && modifier.isFuzzyEnabled()) {
             addWildcardedFuzzy(value, modifier.getFuzzyness());
         } else if (modifier.isWildcarded()) {
             addWildcarded(value);
         } else if (modifier.isFuzzyEnabled()) {
             addFuzzy(value, modifier.getFuzzyness());
         } else {
             queryArguments.append(LuceneHelper.escapeAll(value));
         }
         
         if (modifier.isSplit() && (value.contains(" "))) {
             addSplitted(value, modifier);
         }
         
         queryArguments.append(") ");
         
         setLastSuccessful(true);
         
         return this;
     }
     
 
     /* ---------------------------
      *     addArgumentAs...-methods
      */
     
     private void beforeIteration(final QueryModifier modifier) {
         positionStack.push(queryArguments.length());
         queryArguments.append(modifier.getTermPrefix());
         queryArguments.append("(");
     }
     
     private void afterIteration(final QueryModifier modifier) {
         // get previous position here to ensure that positionStack.poll() is executed
         final int previousPosition = positionStack.poll();
         
         if (queryArguments.charAt(queryArguments.length() - 1) == '(') {
             // if the just opened bracket is still the last character, then revert it
             queryArguments.setLength(previousPosition);
             setLastSuccessful(false);
         } else {
             queryArguments.append(") ");
             setLastSuccessful(true);
         }
     }
     
     @Override
     public DefaultLuceneQuery addArgumentAsCollection(final Collection<?> values, final QueryModifier modifier) {
         
         if (values == null || values.size() == 0) {
             setLastSuccessful(false);
             return this;
         }
         
         beforeIteration(modifier);
 
         // add items
         final QueryModifier valueModifier = modifier.getMultiValueModifier();
         for (Object val : values) {
             addArgument(val, valueModifier);
         }
         
         afterIteration(modifier);
         
         return this;
     }
     
     @Override
     public <K> DefaultLuceneQuery addArgumentAsArray(final K[] values, final QueryModifier modifier) {
         
         // quick return
         if (values == null || values.length == 0) {
             setLastSuccessful(false);
             return this;
         }
 
         beforeIteration(modifier);
         
         // add items
         final QueryModifier valueModifier = modifier.getMultiValueModifier();
         for (K val : values) {
             addArgument(val, valueModifier);
         }
         
         afterIteration(modifier);
         
         return this;
     }
     
     @Override
     protected DefaultLuceneQuery addArgumentAsArray(Object values, final QueryModifier modifier) {
         
         if (values == null || !values.getClass().isArray() || Array.getLength(values) == 0) {
             setLastSuccessful(false);
             return this;
         }
         
         final int arrayLength = Array.getLength(values);
         
         beforeIteration(modifier);
         
         // add all items
         final QueryModifier valueModifier = modifier.getMultiValueModifier();
         for (int i = 0; i < arrayLength; i++) {
             addArgument(Array.get(values, i), valueModifier);
         }
         
         afterIteration(modifier);
         
         return this;
     }
     
     
     /*
      * addRange
      */
     
     @Override
     public DefaultLuceneQuery addRange(double from, double to, QueryModifier mod) {
         if (from >= to) {
             setLastSuccessful(false);
             return this;
         }
         
         return addRange(Double.toString(from), Double.toString(to), mod);
     }
     
     @Override
     public DefaultLuceneQuery addRange(int from, int to, QueryModifier mod) {
         if (from >= to) {
             setLastSuccessful(false);
             return this;
         }
         
         return addRange(Integer.toString(from), Integer.toString(to), mod);
     }
     
     @Override
     public DefaultLuceneQuery addRange(String from, String to, QueryModifier mod) {
        // TODO sanity checks on from and to
         // TODO take the given QueryModifier into consideration (e.g. wildcarded)
        queryArguments.append("[").append(from).append(" ").append(to).append("]");
         
         return this;
     }
     
     
     /*
      * addSubquery
      */
     
     @Override
     public DefaultLuceneQuery addSubquery(final LuceneQuery value, final QueryModifier modifier) {
         if (value == null) {
             setLastSuccessful(false);
             return this;
         }
 
         final CharSequence subQuery = value.getQuery();
         if (subQuery.length() == 0) {
             setLastSuccessful(false);
             return this;
         }
         
         queryArguments.append(modifier.getTermPrefix());
         queryArguments.append("(").append(subQuery).append(") ");
         setLastSuccessful(true);
         
         return this;
     }
     
     
     /*---------------------------
      *     addUnescaped-methods
      */
     
     @Override
     public DefaultLuceneQuery addUnescapedField(
         final String key, final CharSequence value, final boolean mandatory) {
         
         if (key == null || value == null || value.length() == 0) {
             setLastSuccessful(false);
             return this;
         }
         
         if (mandatory) queryArguments.append("+");
         queryArguments.append(key).append(":(").append(value).append(") ");
         setLastSuccessful(true);
         
         return this;
     }
     
     @Override
     public DefaultLuceneQuery addUnescaped(final CharSequence value, final boolean mandatory) {
         if (value == null || value.length() == 0) {
             setLastSuccessful(false);
             return this;
         }
         
         if (mandatory) queryArguments.append("+");
         queryArguments.append(value).append(" ");
         setLastSuccessful(true);
         
         return this;
     }
     
 
     /*---------------------------
      *  helper methods
      */
     
     @Override
     public DefaultLuceneQuery startField(final String fieldName, final boolean mandatory) {
         if (StringUtils.isBlank(fieldName)) {
             setLastSuccessful(false);
             return this;
         }
 
         positionStack.push(queryArguments.length() - 1);
         if (mandatory) queryArguments.append("+");
         queryArguments.append(fieldName).append(":(");
         setLastSuccessful(true);
         
         return this;
     }
     
     @Override
     public DefaultLuceneQuery startField(final String fieldName, final QueryModifier modifier) {
         if (StringUtils.isBlank(fieldName)) {
             setLastSuccessful(false);
             return this;
         }
         
         positionStack.push(queryArguments.length());
         queryArguments.append(modifier.getTermPrefix());
         queryArguments.append(fieldName).append(":(");
         setLastSuccessful(true);
         
         return this;
     }
     
     @Override
     public DefaultLuceneQuery endField() {
         // get previous position here to ensure that positionStack.poll() is executed
         final int previousPosition = positionStack.poll();
         
         if (queryArguments.charAt(queryArguments.length() - 1) == '(') {
             // revert to position before startField(), if the field was ended right after it was started
             queryArguments.setLength(previousPosition);
             setLastSuccessful(false);
         } else {
             queryArguments.append(") ");
             setLastSuccessful(true);
         }
         
         return this;
     }
     
     @Override
     public DefaultLuceneQuery addBoost(final double boostFactor) {
         if (boostFactor <= 0.0 || boostFactor >= 10000000.0)
             throw new IllegalArgumentException(ERR_BOOST_OUT_OF_BOUNDS);
         
         // only add boost factor if != 1 (optimization) and last action was successful
         if (boostFactor != 1.0 && lastSuccessful()) {
             final double rounded = ((int) (boostFactor * 100.0)) / 100.0;
             this.queryArguments.append("^").append(rounded).append(" ");
         }
 
         return this;
     }
 
 }
