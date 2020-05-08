 package com.ryanberdeen.routes.builder;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import com.ryanberdeen.routes.Route;
 
 public class RouteBuilder implements RouteOptions, Cloneable {
 	private static final String NAME = "name";
 	private static final String NAME_PREFIX = "namePrefix";
 	private static final String PATTERN = "pattern";
 	private static final String METHODS = "methods";
 	private static final String EXCLUDED_METHODS = "excludedMethods";
 
 	@Deprecated
 	public HashMap<String, String> parameterValues;
 	private HashMap<String, String> defaultStaticParameterValues;
 	private HashMap<String, String> parameterRegexes;
 
 	private HashMap<String, String> options;
 
 	private String name;
 	private String namePrefix;
 
 	private PathPatternBuilder pathPatternBuilder;
 
 	private HashSet<String> methods;
 	private HashSet<String> excludedMethods;
 
 	public RouteBuilder() {
 		parameterValues = new HashMap<String, String>();
 		defaultStaticParameterValues = new HashMap<String, String>();
 		parameterRegexes = new HashMap<String, String>();
 		options = new HashMap<String, String>();
 
 		name = null;
 		namePrefix = "";
 
 		pathPatternBuilder = new PathPatternBuilder();
 
 		methods = new HashSet<String>();
 		excludedMethods = new HashSet<String>();
 	}
 
 	RouteBuilder(RouteBuilder that) {
 		parameterValues = new HashMap<String, String>(that.parameterValues);
 		defaultStaticParameterValues = new HashMap<String, String>(that.defaultStaticParameterValues);
 		parameterRegexes = new HashMap<String, String>(that.parameterRegexes);
 		options = new HashMap<String, String>(that.options);
 
 		name = that.name;
 		namePrefix = that.namePrefix;
 
 		pathPatternBuilder = new PathPatternBuilder(that.pathPatternBuilder);
 
 		methods = new HashSet<String>(that.methods);
 		excludedMethods = new HashSet<String>(that.excludedMethods);
 	}
 
 	Route createRoute() {
 		Route route = new Route(pathPatternBuilder, parameterValues, parameterRegexes);
 		route.setDefaultStaticParameters(defaultStaticParameterValues);
 		route.setName(getName());
 		route.setMethods(getMethods());
 		route.setExcludedMethods(getExcludedMethods());
 		return route;
 	}
 
 	public PathPatternBuilder getPathPatternBuilder() {
 		return pathPatternBuilder;
 	}
 
 	public RouteBuilder setPathPatternBuilder(PathPatternBuilder pathPatternBuilder) {
 		this.pathPatternBuilder = pathPatternBuilder;
 		return this;
 	}
 
 	HashSet<String> getMethods() {
 		return methods;
 	}
 
 	HashSet<String> getExcludedMethods() {
 		return excludedMethods;
 	}
 
 	public RouteBuilder setOption(String optionName, String value) {
 		if (NAME.equals(optionName)) {
 			setName(value);
 		}
 		else if (NAME_PREFIX.equals(optionName)) {
 			namePrefix = optionName;
 		}
 		else if (PATTERN.equals(optionName)) {
 			append(value);
 		}
 		else if (METHODS.equals(optionName)) {
 			methods = parseMethodString(value);
 		}
 		else if (EXCLUDED_METHODS.equals(optionName)) {
 			excludedMethods = parseMethodString(value);
 		}
 		else {
 			options.put(optionName, value);
 		}
 
 		return this;
 	}
 
 	public String getOption(String name) {
 		return options.get(name);
 	}
 
 	private static HashSet<String> parseMethodString(String value) {
 		if (value == null || value.equals("any")) {
 			return null;
 		}
 		String[] methodsArray = value.split(",");
 		HashSet<String> methods = new HashSet<String>(methodsArray.length);
 		for (String method : methodsArray) {
 			methods.add(method.toUpperCase());
 		}
 
 		return methods;
 	}
 
 	public RouteBuilder setName(String name) {
 		this.name = name;
 		return this;
 	}
 
 	private String getName() {
 		String result = name;
 		if (name != null) {
 			result = namePrefix + result;
 		}
 
 		return result;
 	}
 
 	public RouteBuilder append(String pattern) {
 		pathPatternBuilder.append(pattern);
 		return this;
 	}
 
 	public RouteBuilder setParameterValue(String name, String value) {
 		parameterValues.put(name, value);
 		return this;
 	}
 
 	public RouteBuilder setDefaultStaticParameterValue(String name, String value) {
 		defaultStaticParameterValues.put(name, value);
 		return this;
 	}
 
 	public RouteBuilder setParameterRegex(String name, String regex) {
 		parameterRegexes.put(name, regex);
 		return this;
 	}
 
 	public RouteBuilder apply(Map<String, String> applyParameters) {
		pathPatternBuilder = pathPatternBuilder.apply(applyParameters, parameterValues);
 		parameterValues.putAll(applyParameters);
 		return this;
 	}
 }
