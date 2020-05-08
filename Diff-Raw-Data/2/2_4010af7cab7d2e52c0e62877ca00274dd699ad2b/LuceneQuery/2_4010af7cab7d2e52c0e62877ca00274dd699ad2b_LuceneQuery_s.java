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
 
 import java.util.Collection;
 
 
 /**
  * <p> The LuceneQuery is a builder for Lucene queries.
  * It provides several methods that all handle proper escaping.
  * </p>
  * <p> An abstract implemententation of this interface,
  * that takes care of the redirects to methods with
  * default values and those with old style "boolean mandatory" signature,
  * is available at {@link AbstractLuceneQuery}.
  * A version that delegates every call is {@link ForwardingLuceneQuery}.
  * </p>
  * <p> Example for the usage:
  * </p>
  * <pre>
  *   import com.google.common.collect.Lists;
  *   import de.cosmocode.lucene.LuceneQuery;
  *   ...
 *   // This example uses the default implemtation, but any other implementation works alike
  *   final LuceneQuery builder = LuceneHelper.newQuery();
  *   builder.setModifier(LuceneQuery.MOD_ID);
  *   builder.addField("test", Lists.newArrayList("test1", "test2"));
  *   builder.addArgument("blubb", LuceneQuery.MOD_TEXT);
  *   builder.addSubquery(LuceneHelper.newQuery().addField("sub", "test^3").addField("array", new int[] {1, 2}));
  *   System.out.println(builder.getQuery());
  *   // prints out: +test:(test1 test2) +(blubb blubb*) +(sub:test\^3 array:(1 2))
  * </pre>
  * 
  * @since 1.0
  * @author Oliver Lorenz
  * 
  * @see AbstractLuceneQuery
  * @see ForwardingLuceneQuery
  */
 public interface LuceneQuery {
     
     /*
      * Some general QueryModifiers.
      */
     
     /**
      * <p> A {@link QueryModifier} that has
      * {@link ModifierBuilder#required()} and
      * {@link ModifierBuilder#disjunct()} set.
      * </p>
      * <p> Can be used to include one or more IDs into the search.
      * </p>
      */
     QueryModifier MOD_ID = QueryModifier.start().required().disjunct().end();
     
     /**
      * <p> A {@link QueryModifier} that has
      * {@link ModifierBuilder#prohibited()} and
      * {@link ModifierBuilder#conjunct()} set.
      * </p>
      * <p> Can be used to exclude one or more IDs from the search.
      * </p>
      */
     QueryModifier MOD_NOT_ID = QueryModifier.start().prohibited().conjunct().end();
     
     /**
      * <p> A {@link QueryModifier} that has
      * {@link ModifierBuilder#required()},
      * {@link ModifierBuilder#conjunct()},
      * {@link ModifierBuilder#wildcarded()} and
      * {@link ModifierBuilder#doSplit()} set.
      * </p>
      * <p> Can be used for required text fields.
      * </p>
      */
     QueryModifier MOD_TEXT = QueryModifier.start().required().conjunct().wildcarded().doSplit().end();
     
     /**
      * <p> A {@link QueryModifier} that has
      * {@link ModifierBuilder#required()},
      * {@link ModifierBuilder#disjunct()},
      * {@link ModifierBuilder#wildcarded()},
      * {@link ModifierBuilder#setFuzzyness(Double)} with 0.7 and
      * {@link ModifierBuilder#doSplit()} set.
      * </p>
      * <p> Can be used for some autocompletion, though the fuzzyness may vary
      * from project to project.
      * </p>
      */
     QueryModifier MOD_AUTOCOMPLETE = 
         QueryModifier.start().required().disjunct().wildcarded().setFuzzyness(0.7).doSplit().end();
     
     
     /*
      * Other default values.
      */
     
     /**
      * The default fuzzyness. It is used by
      * <ul>
      *  <li> {@link LuceneQuery#addFuzzyArgument(String)}</li>
      *  <li> {@link LuceneQuery#addFuzzyArgument(String, boolean)}</li>
      *  <li> {@link LuceneQuery#addFuzzyField(String, String)}</li>
      *  <li> {@link LuceneQuery#addFuzzyField(String, String, boolean)}</li>
      * </ul>
      */
     double DEFAULT_FUZZYNESS = 0.5;
     
     /**
      * Error that is thrown when {@link #getQuery()} is called,
      * but the result would be an empty String.
      */
     String ERR_EMPTY_QUERY = 
         "The resulting query is empty, no previous method call was successful";
     
     /**
      * Error to show that the boost is out of bounds.
      * Valid boost values are: 0 < boostFactor < 10000000
      */
     String ERR_BOOST_OUT_OF_BOUNDS = 
         "boostFactor must be greater than 0 and less than 10.000.000 (10 millions)";
     
     /**
      * Error to indicate that the given QueryModifier is null.
      */
     String ERR_MODIFIER_NULL = 
         "the QueryModifier must not be null, choose QueryModifier.DEFAULT instead";
     
     
     /*
      * Utility methods
      */
 
     /**
      * Returns true if this LuceneQuery appends a wildcard ("*") after each added argument, false otherwise.
      * <br>
      * <br><i>Implementation note</i>: This method should use isWildcarded() of {@link #getModifier()}
      * @return true if this LuceneQuery appends a wildcard ("*") after each added argument, false otherwise
      */
     boolean isWildCarded();
     
     /**
      * The wildcard parameter.<br>
      * Set it to true to append a wildcard ("*") after each added argument,
      * false to turn this behaviour off (and just append each argument as is).
      * <br> <br>
      * <i>Implementation note</i>:
      * To provide a coherent user experience,
      * the implementation should alter the default QueryModifier
      * (i.e. set a new default QueryModifier with wildcarded set to the given value),
      * with {@link #setModifier(QueryModifier)} and {@link #getModifier()}.
      * @param wildCarded true to turn wildcarded behaviour on, false to turn it off.
      * 
      * @see ModifierBuilder#setWildcarded(boolean)
      * @see #setModifier(QueryModifier)
      */
     void setWildCarded(final boolean wildCarded);
     
     /**
      * <p> Sets a default QueryModifier that is used
      * whenever a method is invoked without a QueryModifier parameter.
      * </p>
      * <p> Please note that null as a parameter is not permitted and results in a NullPointerException.
      * If you want to set a default value, use {@link QueryModifier#DEFAULT} instead.
      * </p>
      * 
      * @param mod the default modifier to set
      * @throws NullPointerException if the parameter `mod` is null
      */
     void setModifier(final QueryModifier mod);
     
     /**
      * <p> Gets the default {@link QueryModifier} that is used
      * whenever a method is invoked without a QueryModifier parameter.
      * </p>
      * 
      * @return the default QueryModifier
      */
     QueryModifier getModifier();
     
     /**
      * <p> Returns the query which was built with the add...-methods.
      * It throws an IllegalStateException if no add...-methods were successful
      * and the resulting query is empty.
      * </p>
      * 
      * @return the query which was built with the add...-methods
      * @throws IllegalStateException if no add...-methods were successful so that the query would be empty
      */
     String getQuery() throws IllegalStateException;
     
     /**
      * <p> If the last method call was successful (that means it altered the output of this query),
      * then this method returns true, false otherwise.
      * </p>
      * All method calls but the following alter this state:
      * <ul>
      *   <li> {@link #addBoost(double)} - but the boost uses this feature </li>
      *   <li> {@link #getModifier()} </li>
      *   <li> {@link #getQuery()} </li>
      *   <li> {@link #isWildCarded()} </li>
      *   <li> {@link #lastSuccessful()} </li>
      *   <li> {@link #setModifier(QueryModifier)} </li>
      *   <li> {@link #setWildCarded(boolean)} </li>
      * </ul>
      * @return true if the last method call changed this LuceneQuery, false otherwise
      */
     boolean lastSuccessful();
     
     
     //---------------------------
     //     addFuzzyArgument
     //---------------------------
     
     
     /**
      * Append a fuzzy term. <br>
      * fuzzy searches include terms that are in the levenshtein distance of the searched term.
      * <br><br>
      * This method uses the {@link #getModifier()} and {@link #DEFAULT_FUZZYNESS}.
      * 
      * @param value the value to search for
      * @return this
      */
     LuceneQuery addFuzzyArgument(String value);
 
     
     /**
      * Append a fuzzy term with default fuzzyness of 0.5. <br>
      * fuzzy searches include terms that are in the levenshtein distance of the searched term.
      * <br><br>
      * This method uses the {@link #DEFAULT_FUZZYNESS}.
      * 
      * @see #addFuzzyArgument(String, boolean, double)
      * @param value the value to search for
      * @param mandatory if true then the value must be found, otherwise it is just prioritized in the search results
      * @return this
      */
     LuceneQuery addFuzzyArgument(String value, boolean mandatory);
     
     
     /**
      * Append a fuzzy argument with the given fuzzyness. <br>
      * fuzzy searches include arguments that are in the levenshtein distance of the searched term.
      * 
      * @param value the value to search for
      * @param mandatory if true then the value must be found, otherwise it is just prioritized in the search results
      * @param fuzzyness the fuzzyness; must be between 0 (inclusive) and 1 (exclusive), so that: 0 <= fuzzyness < 1
      * @return this
      */
     LuceneQuery addFuzzyArgument(String value, boolean mandatory, double fuzzyness);
 
     
     
     //---------------------------
     //     addArgument
     //---------------------------
     
 
     
     /**
      * <p> Adds a String term to this LuceneQuery.
      * The default modifier is applied to the value.
      * This method uses {@link #getModifier()} and
      * redirects to {@link #addArgument(String, QueryModifier)}.
      * </p>
      * <p> The parameter `value` can have any value, including null or an empty or blank String,
      * but then this method call has no effect on the final query.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * 
      * @param value the String value to add
      * @return this
      * 
      * @see #addArgument(String, QueryModifier)
      */
     LuceneQuery addArgument(String value);
     
     
     /**
      * <p> Adds a String term to this LuceneQuery.
      * </p>
      * <p> The parameter `value` can have any value, including null or an empty or blank String,
      * but then this method call has no effect on the final query.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> If the second parameter `mandatory` is true, then the value is added as required
      * (i.e. the result contains only documents that match the value).
      * Otherwise the String value is added as a "boost" so that all documents
      * matching it are ordered to the top.
      * </p>
      * 
      * @param value the String value to add
      * @param mandatory if true then the value must be found,
      *                  otherwise it is just prioritized in the search results
      * @return this
      */
     LuceneQuery addArgument(String value, boolean mandatory);
     
     
     /**
      * <p> Adds a String term to this LuceneQuery.
      * The given modifier is applied to the value.
      * </p>
      * <p> The first parameter `value` can have any value,
      * including null or an empty or blank String,
      * but then this method call has no effect on the final query.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * 
      * @param value the String value to add
      * @param modifier the {@link QueryModifier} for the value
      * @return this (for chaining)
      * @throws NullPointerException if the second parameter `modifier` is null
      * 
      * @see QueryModifier
      */
     LuceneQuery addArgument(String value, QueryModifier modifier);
     
     
     /**
      * <p> Add a collection of Terms to this LuceneQuery.
      * </p>
      * <p> The first parameter contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> This method uses {@link #getModifier()} and
      * redirects to {@link #addArgument(Collection, QueryModifier)}.
      * </p>
      * 
      * @param values a collection of search terms
      * @return this
      * 
      * @see #addArgumentAsCollection(Collection)
      */
     LuceneQuery addArgument(Collection<?> values);
     
     
     /**
      * <p> Add a collection of Terms to this LuceneQuery.
      * </p>
      * <p> The first parameter contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> If the second parameter `mandatory` is true, then the array of terms are added as required
      * (i.e. the result contains only documents that match all values).
      * Otherwise the array is added as a "boost" so that all documents matching
      * at least one of the values are ordered to the top.
      * </p>
      * 
      * @param values a collection of search terms
      * @param mandatory if true then the value must be found,
      *                  otherwise it is just prioritized in the search results
      * @return this
      */
     LuceneQuery addArgument(Collection<?> values, boolean mandatory);
     
     
     /**
      * <p> Add a collection of Terms to this LuceneQuery.
      * </p>
      * <p> The first parameter contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * 
      * @param values a collection of search terms
      * @param modifier the {@link QueryModifier} that is applied to the values
      * @return this
      * @throws NullPointerException if the second parameter `modifier` is null
      * 
      * @see #addArgumentAsCollection(Collection, QueryModifier)
      */
     LuceneQuery addArgument(Collection<?> values, QueryModifier modifier);
     
     
     /**
      * <p> Add an array of Terms to this LuceneQuery.
      * This method is equivalent to {@link #addArgumentAsArray(Object[])}.
      * </p>
      * <p> This method uses {@link #getModifier()} and
      * redirects to {@link #addArgumentAsArray(Object[], QueryModifier)}.
      * </p>
      * 
      * @param <K> generic element type
      * @param values an array of search terms
      * @return this
      * 
      * @see #addArgumentAsArray(Object[]) 
      */
     <K> LuceneQuery addArgument(K[] values);
     
     
     /**
      * <p> Add an array of Terms to this LuceneQuery.
      * </p>
      * <p> If the second parameter `mandatory` is true, then the array of terms are added as required
      * (i.e. the result contains only documents that match all values).
      * Otherwise the array is added as a "boost" so that all documents matching
      * at least one of the values are ordered to the top.
      * </p>
      * 
      * @param <K> generic element type
      * @param values an array of search terms
      * @param mandatory if true then the value must be found, otherwise it is just prioritized in the search results
      * @return this
      */
     <K> LuceneQuery addArgument(K[] values, boolean mandatory);
     
     
     /**
      * <p> Add an array of Terms to this LuceneQuery.
      * </p>
      * <p> The first parameter contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * 
      * @param <K> generic element type
      * @param values an array of search terms
      * @param modifier the {@link QueryModifier} that is applied to the values
      * @return this
      * @throws NullPointerException if the second parameter `modifier` is null
      * 
      * @see #addArgumentAsArray(Object[], QueryModifier)
      */
     <K> LuceneQuery addArgument(K[] values, QueryModifier modifier);
     
     /**
      * <p> Add an array of doubles to this LuceneQuery.
      * This method uses the {@link #getModifier()}.
      * </p>
      * 
      * @param values the array of terms to search for
      * @return this
      */
     LuceneQuery addArgument(double[] values);
     
     /**
      * <p> Add an array of doubles to this LuceneQuery,
      * using the given {@link QueryModifier}.
      * </p>
      * 
      * @param values the array of terms to search for
      * @param modifier the {@link QueryModifier} that is applied to the values
      * @return this
      */
     LuceneQuery addArgument(double[] values, QueryModifier modifier);
     
     /**
      * <p> Add an int array to this LuceneQuery.
      * This method uses the {@link #getModifier()}.
      * </p>
      * 
      * @param values the array of terms to search for
      * @return this
      */
     LuceneQuery addArgument(int[] values);
     
     /**
      * <p> Add an int array to this LuceneQuery, using the given QueryModifier.
      * </p>
      * 
      * @param values the array of terms to search for
      * @param modifier the {@link QueryModifier} that is applied to the values
      * @return this
      */
     LuceneQuery addArgument(int[] values, QueryModifier modifier);
     
     
     
     //---------------------------
     //     addArgumentAs...
     //---------------------------
     
     
     /**
      * <p> Add a collection of Terms to this LuceneQuery.
      * </p>
      * <p> The first parameter contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> This method uses {@link #getModifier()} and
      * redirects to {@link #addArgumentAsCollection(Collection, QueryModifier)}.
      * </p>
      * 
      * @param values a collection of search terms
      * @return this
      */
     LuceneQuery addArgumentAsCollection(Collection<?> values);
     
     
     /**
      * <p> Add a collection of Terms to this LuceneQuery.
      * </p>
      * <p> The first parameter contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * 
      * @param values a collection of search terms
      * @param modifier the {@link QueryModifier} that is applied to the values
      * @return this
      * @throws NullPointerException if the second parameter `modifier` is null
      */
     LuceneQuery addArgumentAsCollection(Collection<?> values, QueryModifier modifier);
     
     
     /**
      * <p> Add an array of Terms to this LuceneQuery.
      * </p>
      * <p> The first parameter contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> This method uses {@link #getModifier()} and
      * redirects to {@link #addArgumentAsArray(Object[], QueryModifier)}.
      * </p>
      * 
      * @param <K> generic element type
      * @param values an array of search terms
      * @return this
      * 
      * @see #addArgumentAsArray(Object[]) 
      */
     <K> LuceneQuery addArgumentAsArray(K[] values);
     
     
     /**
      * <p>
      * Add an array of Terms to this LuceneQuery.
      * The given QueryModifier specifies the way the terms are added.
      * </p>
      * 
      * @param <K> generic element type
      * @param values an array of search terms
      * @param modifier the {@link QueryModifier} that is applied to the values
      * @return this
      */
     <K> LuceneQuery addArgumentAsArray(K[] values, QueryModifier modifier);
     
     
     /*
      * addRange
      */
     
     
     /**
      * <p> Adds a range to the query.
      * This method calls {@link #addRange(String, String, QueryModifier)} with the default modifier 
      * ({@link #getModifier()}).
      * </p>
      * 
      * @param from the start of the range
      * @param to the end of the range
      * @return this (for chaining)
      * 
      * @since 1.2
      * @see #addRange(String, String, QueryModifier)
      */
     LuceneQuery addRange(String from, String to);
 
     /**
      * <p> Adds a range to the query, using the given QueryModifier.
      * </p>
      * <p> If the given QueryModifier has wildcarded set to true,
      * then both the from and the to clause is set to wildcarded.
      * This means that for example a call of
      * {@code addRange("a", "b", QueryModifier.start().wildcarded().end()}
      * would return results that start with a or b.
      * </p>
      * <p> <strong> Important: </strong> doSplit() and fuzzyness of the given QueryModifier
      * have no effect on a range query.
      * </p>
      * Examples:
      * <pre>
      * final LuceneQuery query = LuceneHelper.newQuery();
      * query.addRange("a", "b");
      * System.out.println(query.getQuery());   // prints something like [a TO b]
      * </pre>
      * <pre>
      * final LuceneQuery query = LuceneHelper.newQuery();
      * query.addRange("a", "b", LuceneQuery.MOD_TEXT);
      * System.out.println(query.getQuery());   // prints something like [a* TO b*]
      * </pre>
      * 
      * @param from the start of the range
      * @param to the end of the range
      * @param mod the QueryModifier that affects the 
      * @return this (for chaining)
      * 
      * @since 1.2
      */
     LuceneQuery addRange(String from, String to, QueryModifier mod);
     
     /**
      * <p> Adds a numerical range to the query.
      * This method calls {@link #addRange(int, int, QueryModifier)} with the default modifier 
      * ({@link #getModifier()}).
      * </p>
      * 
      * @param from the start of the range
      * @param to the end of the range
      * @return this (for chaining)
      * 
      * @since 1.2
      * @see #addRange(int, int, QueryModifier)
      */
     LuceneQuery addRange(int from, int to);
 
     /**
      * <p> Adds a numerical range to the query, using the given QueryModifier.
      * </p>
      * <p> If the given QueryModifier has wildcarded set to true,
      * then both the from and the to clause is set to wildcarded.
      * This means that for example a call of
      * {@code addRange(1, 3, QueryModifier.start().wildcarded().end()}
      * would return results that start with 1 or 3, so every number that starts with 1,2 or 3.
      * </p>
      * <p> <strong> Important: </strong> doSplit() and fuzzyness of the given QueryModifier
      * have no effect on a range query.
      * </p>
      * Examples:
      * <pre>
      * final LuceneQuery query = LuceneHelper.newQuery();
      * query.addRange(1, 10);
      * System.out.println(query.getQuery());   // prints something like [1 TO 10]
      * </pre>
      * <pre>
      * final LuceneQuery query = LuceneHelper.newQuery();
      * query.addRange(1, 3, LuceneQuery.MOD_TEXT);
      * System.out.println(query.getQuery());   // prints something like [1* TO 3*]
      * </pre>
      * 
      * @param from the start of the range
      * @param to the end of the range
      * @param mod the QueryModifier that affects the 
      * @return this (for chaining)
      * 
      * @since 1.2
      */
     LuceneQuery addRange(int from, int to, QueryModifier mod);
     
     /**
      * <p> Adds a numerical, floating point range to the query.
      * This method calls {@link #addRange(double, double, QueryModifier)} with the default modifier 
      * ({@link #getModifier()}).
      * </p>
      * 
      * @param from the start of the range
      * @param to the end of the range
      * @return this (for chaining)
      * 
      * @since 1.2
      * @see #addRange(double, double, QueryModifier)
      */
     LuceneQuery addRange(double from, double to);
 
     /**
      * <p> Adds a numerical, floating point range to the query, using the given QueryModifier.
      * </p>
      * <p> If the given QueryModifier has wildcarded set to true,
      * then both the from and the to clause is set to wildcarded.
      * This means that for example a call of
      * {@code addRange(2.0, 3.0, QueryModifier.start().wildcarded().end()}
      * would return results that start with 2.0 or 3.0, so every floating point number
      * between 2 (inclusive) and 3.1 (exclusive).
      * </p>
      * <p> <strong> Important: </strong> doSplit() and fuzzyness of the given QueryModifier
      * have no effect on a range query.
      * </p>
      * Example:
      * <pre>
      * final LuceneQuery query = LuceneHelper.newQuery();
      * query.addRange(1.1, 1.9);
      * System.out.println(query.getQuery());   // prints something like [1.1 TO 1.9]
      * </pre>
      * 
      * @param from the start of the range
      * @param to the end of the range
      * @param mod the QueryModifier that affects the 
      * @return this (for chaining)
      * 
      * @since 1.2
      */
     LuceneQuery addRange(double from, double to, QueryModifier mod);
     
     
     //---------------------------
     //     addSubquery
     //---------------------------
     
     
     /**
      * <p> This method adds a LuceneQuery as a sub query to this LuceneQuery.
      * </p>
      * <p> If the parameter (`value`) is null,
      * then this LuceneQuery remains unchanged. No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> This method uses the {@link #getModifier()}
      * and redirects to {@link #addSubquery(LuceneQuery, QueryModifier)} with it.
      * </p>
      * 
      * @param value the SubQuery to add
      * @return this
      */
     LuceneQuery addSubquery(LuceneQuery value);
     
     
     /**
      * <p> This method adds a LuceneQuery as a sub query to this LuceneQuery.
      * </p>
      * <p> If the first parameter (`value`) is null,
      * then this LuceneQuery remains unchanged. No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> If the second parameter is true, then the sub query is added as required
      * (i.e. the result contains only documents that match the sub query).
      * Otherwise the sub query is added as a "boost" so that all documents matching
      * the sub query are ordered to the top.
      * </p>
      * 
      * @param value the subQuery to add
      * @param mandatory if true then the sub query restricts the results, otherwise only boosts them
      * @return this
      */
     LuceneQuery addSubquery(LuceneQuery value, boolean mandatory);
     
     
     /**
      * <p> This method adds a LuceneQuery as a sub query to this LuceneQuery.
      * </p>
      * <p> If the parameter (`value`) is null,
      * then this LuceneQuery remains unchanged. No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> The second parameter `modifier` affects the way the sub query is added.
      * </p>
      * <p>If its {@link QueryModifier#getTermModifier()} is {@link TermModifier#REQUIRED},
      * then the sub query is added as required,
      * that means the result contains only documents that match the sub query.
      * </p>
      * <p> If its {@link QueryModifier#getTermModifier()} is {@link TermModifier#PROHIBITED},
      * then the sub query is added as prohibited,
      * that means the result contains only documents that do NOT match the sub query.
      * </p>
      * <p> Otherwise the sub query is added as a "boost" so that all documents matching
      * the sub query are ordered to the top. The number of the documents is not 
      * </p>
      * 
      * @param value the subQuery to add
      * @param modifier the {@link QueryModifier} that affects the way the sub query is added
      * @return this
      */
     LuceneQuery addSubquery(LuceneQuery value, QueryModifier modifier);
     
     
     
     //---------------------------
     //     addUnescaped-methods
     //---------------------------
     
     
     /**
      * Add a field with an argument unescaped.
      * If the second parameter `value` is null then nothing happens.
      * <b>Attention</b>: Use with care, otherwise you get Exceptions on execution.
      * 
      * @param key the field name
      * @param value the value of the field; 
      * @param mandatory whether the field is mandatory or not
      * @return this
      */
     LuceneQuery addUnescapedField(String key, CharSequence value, boolean mandatory);
     
     
     /**
      * Add an argument unescaped.
      * If the parameter `value` is null then nothing happens.
      * <b>Attention</b>: Use with care, otherwise you get Exceptions on execution.
      * 
      * @param value the argument to add unescaped; omitted if null
      * @param mandatory whether the argument is mandatory or not
      * @return this
      */
     LuceneQuery addUnescaped(CharSequence value, boolean mandatory);
     
     
     
     //---------------------------
     //     addField(String, String, ...)
     //---------------------------
     
     
     /**
      * <p> Add a field with the name `key` to the query.
      * The searched value is given as a String.
      * </p>
      * <p> This method uses {@link #getModifier()} and
      * redirects to {@link #addField(String, String, QueryModifier)}.
      * </p>
      * 
      * @param key the name of the field
      * @param value the (string)-value of the field
      * @return this
      */
     LuceneQuery addField(String key, String value);
     
     
     /**
      * <p> Add a field with the name `key` to the query.
      * The searched value is given as a String.
      * </p>
      * <p> The first parameter, key, must be a valid field name
      * (i.e. it must not contain any special characters of Lucene).
      * </p>
      * <p> The second parameter, value, can be any valid String.
      * Blank or empty String or null value is permitted,
      * but then this method call has no effect on the final query.
      * </p>
      * <p> If the third parameter, `mandatoryKey`, is true,
      * then the field with the given String value is search for as required
      * (i.e. the result contains only documents that have the specified field).
      * Otherwise the field is added as a "boost" so that all documents
      * that have this field are ordered to the top.
      * </p>
      * 
      * @param key the name of the field
      * @param value the (string)-value of the field
      * @param mandatoryKey if true then the field is required, otherwise the field is only boosted
      * @return this
      */
     LuceneQuery addField(String key, String value, boolean mandatoryKey);
     
     
     /**
      * <p> Append a field with a string value, and apply a boost afterwards.
      * </p>
      * <p> The method calls {@link #addField(String, String, boolean)}
      * and then {@link #addBoost(double)}.
      * </p>
      * 
      * @param key the name of the field
      * @param value the (string)-value of the field
      * @param mandatoryKey if true then the field is required, otherwise the field is only boosted
      * @param boostFactor the boost factor to apply to the field
      * @return this
      * 
      * @see #addField(String, String, boolean)
      * @see #addBoost(double)
      */
     LuceneQuery addField(String key, String value, boolean mandatoryKey, double boostFactor);
     
     
     /**
      * <p> Append a field with a string value with the specified QueryModifier.
      * </p>
      * <p> The first parameter, key, must be a valid field name
      * (i.e. it must not contain any special characters of Lucene).
      * </p>
      * <p> The second parameter, value, can be any valid String.
      * Blank or empty String or null value is permitted,
      * but then this method call has no effect on the final query.
      * </p>
      * <p> The third parameter, the {@link QueryModifier} `modifier`, must not be null.
      * A NullPointerException is thrown otherwise.
      * </p> 
      * 
      * @param key the name of the field
      * @param value the value for the field
      * @param modifier the {@link QueryModifier} to apply to the field
      * @return this
      * @throws NullPointerException if the third parameter, modifier, is null
      * 
      * @see QueryModifier
      */
     LuceneQuery addField(String key, String value, QueryModifier modifier);
     
     
     
     //---------------------------
     //     addField(String, Collection, ...)
     //---------------------------
     
     
     /**
      * <p> Append a field with a collection of values.
      * </p>
      * <p> The first parameter, key, must be a valid field name
      * (i.e. it must not contain any special characters of Lucene).
      * </p>
      * <p> If the second parameter, `mandatoryKey`, is true,
      * then the field is search for as required
      * (i.e. the result contains only documents that have the specified field).
      * Otherwise the field is added as a "boost" so that all documents
      * that have this field are ordered to the top.
      * </p>
      * <p> The third parameter `values` contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> If the fourth parameter, `mandatoryValue`, is true,
      * then the field must have all values given in the collection.
      * Otherwise the field must have only one of the values.
      * </p> 
      * 
      * @param key the name of the field
      * @param mandatoryKey if true then the field is required, otherwise the field is only boosted
      * @param values the values (as a collection) for the field
      * @param mandatoryValue if true then field must have all values, otherwise only one of them
      * @return this
      */
     LuceneQuery addField(String key, boolean mandatoryKey, Collection<?> values, boolean mandatoryValue);
     
     
     /**
      * <p> Append a field with a collection of values, and apply a boost afterwards.
      * </p>
      * <p> This method calls {@link #addField(String, boolean, Collection, boolean)} first
      * and then {@link #addBoost(double)}.
      * </p>
      * 
      * @param key the name of the field
      * @param mandatoryKey if true then the field is required, otherwise the field is only boosted
      * @param values the values (as a collection) for the field
      * @param mandatoryValue if true then field must have all values, otherwise only one of them
      * @param boostFactor the boost to apply afterwards.
      * @return this
      * 
      * @see #addField(String, boolean, Collection, boolean)
      * @see #addBoost(double)
      */
     LuceneQuery addField(String key, boolean mandatoryKey, Collection<?> values, 
             boolean mandatoryValue, double boostFactor);
     
 
     /**
      * <p> Add a field with the name `key` to the query.
      * The values to search for are given in a collection.
      * </p>
      * <p> This method calls {@link #addField(String, Collection, QueryModifier)}
      * with {@link #getModifier()}.
      * </p>
      * 
      * @param key the name of the field
      * @param values the values (as a collection) for the field
      * @return this
      * 
      * @see #addField(String, Collection, QueryModifier)
      */
     LuceneQuery addField(String key, Collection<?> values);
     
 
     /**
      * <p> Add a field with the name `key` to the query.
      * The values to search for are given in a collection.
      * </p>
      * <p> The first parameter `key` must be a valid field name
      * (i.e. it must not contain any special characters of Lucene).
      * </p>
      * <p> The second parameter `values` contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> The third parameter, the {@link QueryModifier} `modifier`, must not be null.
      * A NullPointerException is thrown otherwise.
      * </p> 
      * 
      * @param key the name of the field
      * @param values the values (as a collection) for the field
      * @param modifier the {@link QueryModifier} to apply to the field
      * @return this
      * @throws NullPointerException if the third parameter, modifier, is null
      * 
      * @see #addFieldAsCollection(String, Collection, QueryModifier)
      */
     LuceneQuery addField(String key, Collection<?> values, QueryModifier modifier);
 
     
     
     //---------------------------
     //     addField(String, Array, ...)
     //---------------------------
     
 
     
     /**
      * <p> Add a field with the name `key` to the query.
      * The values to search for are given in an array.
      * </p>
      * <p> This method calls {@link #addField(String, Object[], QueryModifier)}
      * with {@link #getModifier()}.
      * </p>
      * 
      * @param <K> generic element type
      * @param key the name of the field
      * @param value the values to be searched in the field
      * @return this
      * 
      * @see #addFieldAsArray(String, Object[])
      */
     <K> LuceneQuery addField(String key, K[] value);
     
     
     /**
      * <p> Add a field with the name `key` to the query.
      * The values to search for are given in an array.
      * </p>
      * <p> The first parameter `key` must be a valid field name
      * (i.e. it must not contain any special characters of Lucene).
      * </p>
      * <p> The second parameter `values` contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> The third parameter, the {@link QueryModifier} `modifier`, must not be null.
      * A NullPointerException is thrown otherwise.
      * </p> 
      * 
      * @param <K> generic element type
      * @param key the name of the field
      * @param value the values to be searched in the field
      * @param modifier the {@link QueryModifier} to apply to the field
      * @return this
      * @throws NullPointerException if the third parameter, modifier, is null
      * 
      * @see #addFieldAsArray(String, Object[], QueryModifier)
      */
     <K> LuceneQuery addField(String key, K[] value, QueryModifier modifier);
     
 
     /*
      * addRangeField, for example: (fieldName):[(a) TO (b)]
      */
     
     
     /**
      * <p> Adds a range for a specific field to this query.
      * This method calls {@link #addRangeField(String, String, String, QueryModifier)}
      * with the default modifier ({@link #getModifier()}).
      * </p>
      * 
      * @param fieldName the name of the field to search in
      * @param from the start of the range
      * @param to the end of the range
      * @return this (for chaining)
      * 
      * @see #addRangeField(String, String, String, QueryModifier)
      * @since 1.2
      */
     LuceneQuery addRangeField(String fieldName, String from, String to);
 
     /**
      * <p> Adds a range for a specific field to this query, using the given QueryModifier.
      * See {@link #addRange(String, String, QueryModifier)} for further documentation.
      * </p>
      * 
      * @param fieldName the name of the field to search in
      * @param from the start of the range
      * @param to the end of the range
      * @param mod the {@link QueryModifier} to apply to the field
      * @return this (for chaining)
      * 
      * @see #addRange(String, String, QueryModifier)
      * @since 1.2
      */
     LuceneQuery addRangeField(String fieldName, String from, String to, QueryModifier mod);
     
     /**
      * <p> Adds a numeric range for a specific field to this query.
      * This method calls {@link #addRangeField(String, int, int, QueryModifier)}
      * with the default modifier ({@link #getModifier()}).
      * </p>
      * 
      * @param fieldName the name of the field to search in
      * @param from the start of the range
      * @param to the end of the range
      * @return this (for chaining)
      * 
      * @see #addRangeField(String, int, int, QueryModifier)
      * @since 1.2
      */
     LuceneQuery addRangeField(String fieldName, int from, int to);
 
     /**
      * <p> Adds a numeric range for a specific field to this query, using the given QueryModifier.
      * See {@link #addRange(int, int, QueryModifier)} for further documentation.
      * </p>
      * 
      * @param fieldName the name of the field to search in
      * @param from the start of the range
      * @param to the end of the range
      * @param mod the {@link QueryModifier} to apply to the field
      * @return this (for chaining)
      * 
      * @see #addRange(int, int, QueryModifier)
      * @since 1.2
      */
     LuceneQuery addRangeField(String fieldName, int from, int to, QueryModifier mod);
     
     /**
      * <p> Adds a numeric, floating point range for a specific field to this query.
      * This method calls {@link #addRangeField(String, double, double, QueryModifier)}
      * with the default modifier ({@link #getModifier()}).
      * </p>
      * 
      * @param fieldName the name of the field to search in
      * @param from the start of the range
      * @param to the end of the range
      * @return this (for chaining)
      * 
      * @see #addRangeField(String, double, double, QueryModifier)
      * @since 1.2
      */
     LuceneQuery addRangeField(String fieldName, double from, double to);
 
     /**
      * <p> Adds a numeric, floating point range for a specific field to this query, using the given QueryModifier.
      * See {@link #addRange(double, double, QueryModifier)} for further documentation.
      * </p>
      * 
      * @param fieldName the name of the field to search in
      * @param from the start of the range
      * @param to the end of the range
      * @param mod the {@link QueryModifier} to apply to the field
      * @return this (for chaining)
      * 
      * @see #addRange(double, double, QueryModifier)
      * @since 1.2
      */
     LuceneQuery addRangeField(String fieldName, double from, double to, QueryModifier mod);
 
     //---------------------------
     //     addFuzzyField
     //---------------------------
     
     
     /**
      * Append a fuzzy search argument with the given fuzzyness for the given field.
      * <br>fuzzy searches include arguments that are in the levenshtein distance of the searched term.
      * <br>Less fuzzyness (closer to 0) means less accuracy, and vice versa
      *   (the closer to 1, solr yields less but accurater results)
      * <br>
      * <br>This method uses {@link #getModifier()} and the {@link #DEFAULT_FUZZYNESS}.
      * 
      * @param key the name of the field
      * @param value the value to search for
      * @return this
      */
     LuceneQuery addFuzzyField(String key, String value);
     
     
     /**
      * Append a fuzzy search argument with default fuzzyness (0.5) for the given field. <br>
      * fuzzy searches include arguments that are in the levenshtein distance of the searched term.
      * <br><br>
      * This method uses the {@link #DEFAULT_FUZZYNESS}.
      * 
      * @param key the name of the field
      * @param value the value to search for
      * @param mandatoryKey if true then the field must contain the given value, 
      *   otherwise it is just prioritized in the search results
      * @return this
      */
     LuceneQuery addFuzzyField(String key, String value, boolean mandatoryKey);
     
     
     /**
      * Append a fuzzy search argument with the given fuzzyness for the given field.
      * <br>fuzzy searches include arguments that are in the levenshtein distance of the searched term.
      * <br>Less fuzzyness (closer to 0) means less accuracy, and vice versa
      *   (the closer to 1, lucene yields less but accurater results)
      * 
      * @param key the name of the field
      * @param value the value to search for
      * @param mandatoryKey if true then the field must contain the given value,
      *   otherwise it is just prioritized in the search results
      * @param fuzzyness the fuzzyness; must be between 0 and 1, so that 0 <= fuzzyness < 1
      * @return this
      */
     LuceneQuery addFuzzyField(String key, String value, boolean mandatoryKey, double fuzzyness);
 
     
 
     //---------------------------
     //     addFieldAs...
     //---------------------------
     
     /**
      * <p> Add a field with the name `key` to the query.
      * The values to search for are given in a collection.
      * </p>
      * <p> This method calls {@link #addFieldAsCollection(String, Collection, QueryModifier)}
      * with {@link #getModifier()}.
      * </p>
      * 
      * @param key the name of the field
      * @param values the values (as a collection) for the field
      * @return this
      * 
      * @see #addFieldAsCollection(String, Collection, QueryModifier)
      */
     LuceneQuery addFieldAsCollection(String key, Collection<?> values);
     
     
     /**
      * <p> Add a field with the name `key` to the query.
      * The values to search for are given in a collection.
      * </p>
      * <p> The second parameter `values` contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> The third parameter, the {@link QueryModifier} `modifier`, must not be null.
      * A NullPointerException is thrown otherwise.
      * </p> 
      * 
      * @param key the name of the field
      * @param values the values (as a collection) for the field
      * @param modifier the {@link QueryModifier} to apply to the field
      * @return this
      * @throws NullPointerException if the third parameter, modifier, is null
      */
     LuceneQuery addFieldAsCollection(String key, Collection<?> values, QueryModifier modifier);
     
     
     /**
      * <p> Append a field with a collection of values, and apply a boost afterwards.
      * </p>
      * <p> This method calls {@link #addField(String, Collection, QueryModifier)} first
      * and then {@link #addBoost(double)}.
      * </p>
      * 
      * @param key the name of the field
      * @param values the values (as a collection) for the field
      * @param modifier the {@link QueryModifier} to apply to the field
      * @param boost the boost to apply on the field afterwards.
      * @return this
      * 
      * @see #addFieldAsCollection(String, Collection, QueryModifier)
      * @see #addBoost(double)
      */
     LuceneQuery addFieldAsCollection(String key, Collection<?> values, QueryModifier modifier, double boost);
     
     
     /**
      * <p> Add a field with the name `key` to the query.
      * The values to search for are given in an array.
      * </p>
      * <p> This method calls {@link #addFieldAsArray(String, Object[], QueryModifier)}
      * with {@link #getModifier()}.
      * </p>
      * 
      * @param <K> generic element type
      * @param key the name of the field
      * @param values the values to be searched in the field
      * @return this
      */
     <K> LuceneQuery addFieldAsArray(String key, K[] values);
     
     
     /**
      * <p> Add a field with the name `key` to the query.
      * The values to search for are given in an array.
      * </p>
      * <p> The first parameter `key` must be a valid field name
      * (i.e. it must not contain any special characters of Lucene).
      * </p>
      * <p> The second parameter `values` contains the values which are added to the final query.
      * It can be null or empty or contain only blank or empty Strings,
      * but then this method call has no effect on the final query.
      * No Exception will be thrown on this invocation.
      * If all other method calls don't change this LuceneQuery,
      * then {@link #getQuery()} will throw an IllegalStateException.
      * </p>
      * <p> The third parameter, the {@link QueryModifier} `modifier`, must not be null.
      * A NullPointerException is thrown otherwise.
      * </p> 
      * 
      * @param <K> generic element type
      * @param key the name of the field
      * @param value the values to be searched in the field
      * @param modifier the {@link QueryModifier} to apply to the field
      * @return this
      * @throws NullPointerException if the third parameter, modifier, is null
      */
     <K> LuceneQuery addFieldAsArray(String key, K[] value, QueryModifier modifier);
     
     
     
     //---------------------------------------
     //    startField, endField, addBoost
     //---------------------------------------
     
     
     /**
      * Starts a field with `key`:(.<br>
      * <b>Attention</b>: Use this method carefully and end all fields with 
      * {@link LuceneQuery#endField()},
      * or otherwise you get Exceptions on execution.
      * @param fieldName the name of the field; omitted if null
      * @param mandatory whether the field is mandatory for execution ("+" is prepended) or not.
      * @return this
      */
     LuceneQuery startField(String fieldName, boolean mandatory);
     
     
     /**
      * Starts a field with `key`:(.<br>
      * <b>Attention</b>: Use this method carefully and end all fields with 
      * {@link LuceneQuery#endField()},
      * or otherwise you get Exceptions on execution.
      * @param fieldName the name of the field; omitted if null
      * @param modifier the modifiers for the field (see QueryModifier for more details)
      * @return this
      */
     LuceneQuery startField(String fieldName, QueryModifier modifier);
     
     
     /**
      * Ends a previously started field. <br>
      * <b>Attention</b>: Use this method carefully and only end fields that have been started with
      * {@link LuceneQuery#startField(String, boolean)},
      * or otherwise you get Solr-Exceptions on execution.
      * @return this
      */
     LuceneQuery endField();
     
     
     /**
      * Add a boost factor to the current element. <br>
      * <b>Attention</b>: Don't use this method directly after calling startField(...), 
      * or otherwise you get Exceptions on execution.
      * @param boostFactor a positive double < 10.000.000 which boosts the previously added element
      * @return this
      */
     LuceneQuery addBoost(double boostFactor);
     
 
 }
