 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springframework.data.keyvalue.riak.mapreduce.filter;
 
 import java.util.Set;
 
 import org.springframework.data.keyvalue.riak.mapreduce.filter.predicate.RiakBetweenPredicate;
 import org.springframework.data.keyvalue.riak.mapreduce.filter.predicate.RiakLogicalPredicate;
 import org.springframework.data.keyvalue.riak.mapreduce.filter.predicate.RiakSimilarToPredicate;
 import org.springframework.data.keyvalue.riak.mapreduce.filter.predicate.RiakSimplePredicate;
 
 /**
  * @author Andrew Berman
  * 
  */
 public abstract class RiakKeyFilterRestrictions {
 
 	public static RiakSimplePredicate gt(Object value) {
 		return new RiakSimplePredicate("greater_than", value);
 	}
 
 	public static RiakSimplePredicate lt(Object value) {
 		return new RiakSimplePredicate("less_than", value);
 	}
 
 	public static RiakSimplePredicate gte(Object value) {
 		return new RiakSimplePredicate("greater_than_eq", value);
 	}
 
 	public static RiakSimplePredicate lte(Object value) {
 		return new RiakSimplePredicate("less_than_eq", value);
 	}
 
 	public static RiakBetweenPredicate between(Object min, Object max,
 			boolean inclusive) {
 		return new RiakBetweenPredicate(min, max, inclusive);
 	}
 
 	public static RiakBetweenPredicate between(Object min, Object max) {
 		return new RiakBetweenPredicate(min, max, null);
 	}
 
 	public static RiakSimplePredicate matches(String value) {
 		return new RiakSimplePredicate("matches", value);
 	}
 
 	public static RiakSimplePredicate neq(Object value) {
 		return new RiakSimplePredicate("neq", value);
 	}
 
 	public static RiakSimplePredicate eq(Object value) {
 		return new RiakSimplePredicate("eq", value);
 	}
 
	public static RiakSimplePredicate isMember(Set<Object> set) {
 		return new RiakSimplePredicate("set_member", set);
 	}
 
 	public static RiakSimilarToPredicate similarTo(Object value,
 			int levenshteinDistance) {
 		return new RiakSimilarToPredicate(value, levenshteinDistance);
 	}
 
 	public static RiakSimplePredicate startsWith(Object value) {
 		return new RiakSimplePredicate("starts_with", value);
 	}
 
 	public static RiakSimplePredicate endsWith(Object value) {
 		return new RiakSimplePredicate("ends_with", value);
 	}
 
 	public static RiakLogicalPredicate and(
 			RiakSimplePredicate left,
 			RiakSimplePredicate right) {
 		return new RiakLogicalPredicate("and", left, right);
 	}
 
 	public static RiakLogicalPredicate or(
 			RiakSimplePredicate left,
 			RiakSimplePredicate right) {
 		return new RiakLogicalPredicate("or", left, right);
 	}
 
 	public static RiakLogicalPredicate not(
 			RiakSimplePredicate filter) {
 		return new RiakLogicalPredicate("not", filter);
 	}
 }
