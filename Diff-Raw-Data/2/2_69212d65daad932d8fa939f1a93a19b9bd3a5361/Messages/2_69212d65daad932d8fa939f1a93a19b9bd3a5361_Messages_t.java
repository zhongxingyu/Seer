 /**
  * Copyright (C) 2008 Ivan S. Dubrov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.code.nanorm.internal.util;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.text.MessageFormat;
 
 import com.google.code.nanorm.annotations.Property;
 import com.google.code.nanorm.annotations.ResultMap;
 import com.google.code.nanorm.annotations.ResultMapRef;
 import com.google.code.nanorm.internal.config.StatementConfig;
 import com.google.code.nanorm.internal.config.StatementKey;
 import com.google.code.nanorm.internal.config.SubselectConfig;
 
 /**
  * Different error messages.
  * 
  * @author Ivan Dubrov
  */
 public class Messages {
 	/**
 	 * Generate error message for case when 'before' generated key specified
 	 * without the SQL.
 	 * 
 	 * @param mapper mapper interface
 	 * @param method method
 	 * @return message
 	 */
 	public static String beforeKeyWithoutSQL(Class<?> mapper, Method method) {
 		return "@SelectKey with BEFORE type must have non-empty SQL specified, check "
 				+ location(mapper, method);
 	}
 
 	/**
 	 * Generate error message for case when 'before' generated key specified
 	 * without specifying the property.
 	 * 
 	 * @param mapper mapper interface
 	 * @param method method
 	 * @return message
 	 */
 	public static String beforeKeyWithoutProperty(Class<?> mapper, Method method) {
 		return "@SelectKey with BEFORE type must have non-empty property specified, check "
 				+ location(mapper, method);
 	}
 
 	/**
 	 * Generate error message for case when generated key specified without
 	 * specifying the property and method return type is void.
 	 * 
 	 * @param mapper mapper interface
 	 * @param method method
 	 * @return message
 	 */
 	public static String voidReturnWithoutProperty(Class<?> mapper, Method method) {
 		return "@SelectKey must have non-empty property specified when insert method return value is void, check "
 				+ location(mapper, method);
 	}
 
 	/**
 	 * Generate error message for case when nested result map is not found.
 	 * 
 	 * @param mapper mapper declaring the result map
 	 * @param method method result map is applied to
 	 * @param resultMap result map that declares the property
 	 * @param mapping mapping
 	 * @return message
 	 */
 	public static String nestedMapNotFound(Class<?> mapper, Method method, ResultMap resultMap,
 			Property mapping) {
 		ResultMapRef ref = mapping.nestedMap();
 
 		return MessageFormat.format(
 				"Nested map ''{0}'' not found in ''{1}'' for property ''{2}'' while processing "
 						+ "the result map ''{3}'' declared in {4}", ref.value(), mapper(
 						ref.declaringClass(), mapper).getName(), mapping.value(), resultMap.id(),
 				location(mapper, method));
 	}
 
 	/**
 	 * Generate error message for case when referenced result map is not found.
 	 * 
 	 * @param mapper mapper declaring the result map
 	 * @param method method result map is applied to
 	 * @param ref result map reference
 	 * @return message
 	 */
 	public static String resultMapNotFound(Class<?> mapper, Method method, ResultMapRef ref) {
 		return MessageFormat.format("Result map ''{0}'' used in {1} not found in mapper ''{2}''",
 				ref.value(), location(mapper, method), mapper(ref.declaringClass(), mapper)
 						.getName());
 	}
 
 	/**
 	 * Generate error message for case when mapper is not configured yet when
 	 * result map is referenced from it.
 	 * 
 	 * @param mapper mapper interface
 	 * @param resultMapId resultMapId
 	 * @return message
 	 */
 	public static String notMapped(Class<?> mapper, String resultMapId) {
 		return MessageFormat.format("The mapper ''{0}'' is not configured yet, "
 				+ "while searching for result map ''{1}''.", mapper.getName(), resultMapId);
 	}
 
 	/**
 	 * Generate error message for case when subselect query statement is not
 	 * found.
 	 * 
 	 * @param cfg subselect config
 	 * @return message
 	 */
 	public static String subselectNotFound(SubselectConfig cfg) {
 		StatementKey key = cfg.getSubselectKey();
 		return MessageFormat.format(
 				"Subselect query method ''{0}'' not found in mapper ''{1}'' while processing "
 						+ "property ''{2}'' in result map ''{3}'' of mapper ''{4}''",
 				key.getName(), key.getMapper().getName(), cfg.getPropertyMapping().getProperty(),
 				cfg.getResultMap().id(), cfg.getMapper().getName());
 	}
 
 	/**
 	 * Generate error message for case when subselect query statement does not
 	 * have exactly one parameter.
 	 * 
 	 * @param cfg subselect config
 	 * @return message
 	 */
 	public static String subselectParameterCount(SubselectConfig cfg) {
 		StatementKey key = cfg.getSubselectKey();
 		return MessageFormat.format(
 				"Subselect query method ''{0}'' in mapper ''{1}'' does not have exactly one parameter, used in "
 						+ "property ''{2}'' in result map ''{3}'' of mapper ''{4}''.", key
 						.getName(), key.getMapper().getName(), cfg.getPropertyMapping()
 						.getProperty(), cfg.getResultMap().id(), cfg.getMapper().getName());
 	}
 
 	/**
 	 * Generate error message for case when both column and columnIndex are
 	 * specified.
 	 * 
 	 * @param mapping mapping annotation
 	 * @param mapper mapper interface
 	 * @param resultMap result map
 	 * @return message
 	 */
 	public static String multipleColumn(Property mapping, Class<?> mapper, ResultMap resultMap) {
 		return MessageFormat
 				.format(
 						"Both column (''{0}'') and column index ({1}) were specified for property ''{2}'' in result map ''{3}'' of mapper ''{4}''",
 						mapping.column(), mapping.columnIndex(), mapping.value(), resultMap.id(),
 						mapper.getName());
 	}
 
 	/**
 	 * Generate error message for case when none of column or columnIndex are
 	 * specified for subselect property.
 	 * 
 	 * @param mapping mapping annotation
 	 * @param mapper mapper interface
 	 * @param resultMap result map
 	 * @return message
 	 */
 	public static String subselectNoColumn(Property mapping, Class<?> mapper, ResultMap resultMap) {
 		return MessageFormat
 				.format(
 						"Neither column name nor column index were specified for property with subselect "
 								+ "''{0}'' in result map ''{1}'' of mapper ''{2}''. You must explicitly configure column "
 								+ "or column name for properties with subselect.", mapping.value(),
 						resultMap.id(), mapper.getName());
 	}
 
 	/**
 	 * Generate error message for case when property name is empty.
 	 * 
 	 * @param mapping mapping annotation
 	 * @param mapper mapper interface
 	 * @param resultMap result map
 	 * @return message
 	 */
 	public static String emptyProperty(Property mapping, Class<?> mapper, ResultMap resultMap) {
 		return MessageFormat.format("Empty property found in result map ''{0}'' of mapper ''{1}''",
 				resultMap.id(), mapper.getName());
 	}
 
 	/**
 	 * Generate error message for case when subselectMapper is specified without
 	 * specifying subselect.
 	 * 
 	 * @param mapping mapping annotation
 	 * @param mapper mapper interface
 	 * @param resultMap result map
 	 * @return message
 	 */
 	public static String subselectMapperWithoutSubselect(Property mapping, Class<?> mapper,
 			ResultMap resultMap) {
 		return MessageFormat.format(
 				"subselectMapper ''{0}'' used without specifying subselect itself for "
 						+ "property ''{1}'' in result map ''{2}'' of mapper ''{3}''", mapping
 						.subselectMapper().getName(), mapping.value(), resultMap.id(), mapper
 						.getName());
 	}
 
 	/**
 	 * Generate error message for case when both nested map and subselect map
 	 * are selected.
 	 * 
 	 * @param mapping mapping annotation
 	 * @param mapper mapper interface
 	 * @param resultMap result map
 	 * @return message
 	 */
 	public static String bothSubselectNested(Property mapping, Class<?> mapper, ResultMap resultMap) {
 		return MessageFormat
 				.format(
 						"Both subselect (''{0}'' of mapper ''{1}'') and nested map (''{2}'' of mapper ''{3}'') are specified for "
 								+ "property ''{4}'' in result map ''{5}'' of mapper ''{6}''",
 						mapping.subselect(), mapper(mapping.subselectMapper(), mapper).getName(),
 						mapping.nestedMap().value(), mapper(mapping.nestedMap().declaringClass(),
 								mapper).getName(), mapping.value(), resultMap.id(), mapper
 								.getName());
 	}
 
 	/**
 	 * Generate error message for case when both nested map is specified for the
 	 * property and property itself is in the groupBy list.
 	 * 
 	 * @param mapping mapping annotation
 	 * @param mapper mapper interface
 	 * @param resultMap result map
 	 * @return message
 	 */
 	public static String bothNestedGroupBy(Property mapping, Class<?> mapper, ResultMap resultMap) {
 		return MessageFormat.format(
 				"Property ''{0}'' has nested map (''{1}'' of mapper ''{2}'') and is in the ''groupBy'' list "
 						+ "of the result map ''{3}'' of mapper ''{4}'' at the same time.", mapping
 						.value(), mapping.nestedMap().value(), mapper(
 						mapping.nestedMap().declaringClass(), mapper).getName(), resultMap.id(),
 				mapper.getName());
 	}
 
 	/**
 	 * Generate error message for case when property in groupBy list is not
 	 * explicitly configured.
 	 * 
 	 * @param mapper mapper interface
 	 * @param method method result map applied to
 	 * @param resultMap result map
 	 * @param prop property
 	 * @return message
 	 */
 	public static String groupByPropertyMissing(String prop, Class<?> mapper, Method method,
 			ResultMap resultMap) {
 		return MessageFormat.format(
 				"Property ''{0}'' was specified in the ''groupBy'' list of the the "
 						+ "result map ''{1}'' of {2}, but is not explicitly configured.", prop,
 				resultMap.id(), location(mapper, method));
 	}
 
 	/**
 	 * Generate error message for case when mapper method result type is
 	 * primitive, but no values were got during the query.
 	 * 
 	 * @param mapper mapper interface
 	 * @param method method result map applied to
 	 * @param type result type (primitive)
 	 * 
 	 * @return message
 	 */
 	public static String nullResult(Class<?> mapper, String method, Class<?> type) {
 		return "Cannot convert empty result set to primitive type '" + type.getName()
 				+ "' while executing " + location(mapper, method) + '.';
 	}
 
 	/**
 	 * Generate error message for case when single result was expected, but
 	 * result set contained more than one row.
 	 * 
 	 * @param location mapper interface
 	 * 
 	 * @return message
 	 */
 	public static String singleResultExpected(Object location) {
 		if (location instanceof StatementConfig) {
 			StatementConfig stConfig = (StatementConfig) location;
 			String method = stConfig.getId().getName();
 			Class<?> mapper = stConfig.getId().getMapper();
 			location = location(mapper, method);
 		}
 		return "Single result expected while executing " + location + '.';
 	}
 
 	/**
 	 * Generate error message for case when method result type could not be
 	 * deduced.
 	 * 
 	 * @param method query method
 	 * @return message
 	 */
 	public static String invalidReturnType(Method method) {
 		return "Cannot deduce return type for " + location(method.getDeclaringClass(), method)
 				+ " (return type is void and no DataSink is provided in parameters)";
 	}
 
 	/**
 	 * Generate error message for case when type handler could not be found for
 	 * property.
 	 * 
 	 * @param type property type
 	 * @return message
 	 */
 	public static String typeHandlerNotFound(Type type) {
 		return "Type handler not found for type '" + type + '\'';
 	}
 
 	/**
 	 * Generate error message for case when mutually exclusive annotations are
 	 * used on query method.
 	 * 
 	 * @param mapper mapper interface
 	 * @param method query method annotations are applied to
 	 * @param ann1 first annotation name
 	 * @param ann2 second annotation name
 	 * @return message
 	 */
 	public static String mutuallyExclusive(Class<?> mapper, Method method, String ann1, String ann2) {
 		return MessageFormat.format(
 				"''{0}'' annotation and ''{1}'' annotation used in {2} are mutually exclusive",
 				ann1, ann2, location(mapper, method));
 	}
 	
 	/**
 	 * Generate error message for case when method configuration is missing.
 	 * 
 	 * @param mapper mapper interface
 	 * @param method method
 	 * @return message
 	 */
 	public static String missingConfiguration(Class<?> mapper, String method) {
		return MessageFormat.format("The {0} does not have any configuration.", 
 				location(mapper, method));
 	}
 
 	private static Class<?> mapper(Class<?> override, Class<?> mapper) {
 		return override != Object.class ? override : mapper;
 	}
 
 	private static String location(Class<?> mapper, Method method) {
 		return location(mapper, method.getName());
 	}
 
 	private static String location(Class<?> mapper, String method) {
 		if (method != null) {
 			return "method '" + method + "' of mapper '" + mapper.getName() + '\'';
 		}
 		return "mapper '" + mapper.getName() + '\'';
 	}
 }
