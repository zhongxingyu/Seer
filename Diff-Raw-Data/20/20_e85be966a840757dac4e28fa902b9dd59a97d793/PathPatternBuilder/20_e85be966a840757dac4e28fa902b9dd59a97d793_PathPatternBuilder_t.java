 package com.ryanberdeen.routes.builder;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.ryanberdeen.routes.PathPattern;
 import com.ryanberdeen.routes.PathPattern.ParameterSegment;
 import com.ryanberdeen.routes.PathPattern.PathSegment;
 import com.ryanberdeen.routes.PathPattern.StaticSegment;
 
 public class PathPatternBuilder implements Cloneable {
 	private static final char PARAMETER_WITHOUT_SLASHES_PREFIX = ':';
 	private static final char PARAMETER_WITH_SLASHES_PREFIX = '*';
 
 	private HashSet<String> parameterNames;
 	private ArrayList<PathSegmentBuilder> pathSegmentBuilders;
 
 	public PathPatternBuilder() {
 		pathSegmentBuilders = new ArrayList<PathSegmentBuilder>();
 		parameterNames = new HashSet<String>();
 	}
 
 	public PathPatternBuilder(PathPatternBuilder that) {
 		pathSegmentBuilders = new ArrayList<PathSegmentBuilder>(that.pathSegmentBuilders);
 		parameterNames = new HashSet<String>(that.parameterNames);
 	}
 
 	public PathPatternBuilder apply(Map<String, String> parameters, Map<String, String> staticParameterValues) {
 		PathPatternBuilder result = new PathPatternBuilder();
 
 		for (PathSegmentBuilder pathSegment : pathSegmentBuilders) {
 			result.append(pathSegment.apply(parameters, staticParameterValues));
 		}
 
 		return result;
 	}
 
 	/** Appends a pattern.
 	 */
 	public PathPatternBuilder append(String pattern) {
 		PathPatternBuilder.parse(this, pattern);
 		return this;
 	}
 
 	/** Appends a path segment.
 	 */
 	public PathPatternBuilder appendStatic(String pathSegment) {
 		return append(new StaticSegmentBuilder(pathSegment, true));
 	}
 
 	/** Appends a parameter.
 	 *  The parameter does not allow slashes.
 	 */
 	public PathPatternBuilder appendParameter(String parameterName) {
 		return appendParameter(parameterName, false);
 	}
 
 	/** Appends a parameter.
 	 */
 	public PathPatternBuilder appendParameter(String parameterName, boolean allowSlashes) {
 		return append(new ParameterSegmentBuilder(parameterName, allowSlashes));
 	}
 
 	/** Appends a PathSegmentBuilder.
 	 */
 	private PathPatternBuilder append(PathSegmentBuilder pathSegment) {
 		pathSegmentBuilders.add(pathSegment.clone());
 		if (pathSegment instanceof ParameterSegmentBuilder) {
 			ParameterSegmentBuilder parameter = (ParameterSegmentBuilder) pathSegment;
 			parameterNames.add(parameter.name);
 		}
 		return this;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public PathPatternBuilder clone() {
 		try {
 			PathPatternBuilder result = (PathPatternBuilder) super.clone();
 
 			result.pathSegmentBuilders = new ArrayList<PathSegmentBuilder>(pathSegmentBuilders.size());
 			for (PathSegmentBuilder parameter : pathSegmentBuilders) {
 				result.pathSegmentBuilders.add(parameter.clone());
 			}
 
 			result.parameterNames = (HashSet<String>) parameterNames.clone();
 
 			return result;
 		}
 		catch (CloneNotSupportedException ex) {
 			throw new Error(ex);
 		}
 	}
 
 	public PathPattern createPathPattern(Set<String> optionalParameterNames, Map<String, String> parameterRegexes) {
 		ArrayList<PathSegment> pathSegments = new ArrayList<PathSegment>();
 		for (PathSegmentBuilder pathSegmentBuidler : pathSegmentBuilders) {
 			if (pathSegmentBuidler instanceof ParameterSegmentBuilder) {
 				ParameterSegmentBuilder parameterSegmentBuilder = (ParameterSegmentBuilder) pathSegmentBuidler;
 				boolean required = true;
 				String regex = null;
 
 				String parameterName = parameterSegmentBuilder.name;
 				if (optionalParameterNames != null && optionalParameterNames.contains(parameterName)) {
 					required = false;
 				}
 
 				if (parameterRegexes != null) {
 					regex = parameterRegexes.get(parameterName);
 				}
 				pathSegments.add(parameterSegmentBuilder.createPathSegment(regex, required));
 			}
 			else {
 				StaticSegmentBuilder staticSegmentBuilder = (StaticSegmentBuilder) pathSegmentBuidler;
 				pathSegments.add(staticSegmentBuilder.createPathSegment());
 			}
 		}
 		return new PathPattern(pathSegments, parameterNames);
 	}
 
 	/** Parses a String into a path pattern.
 	 *
 	 * Parameters whose name is contained in <code>optionalParameterNames</code>
 	 * will not be required for paths to match.
 	 * @param pattern describes the format of matching paths
 	 * @param optionalParameterNames parameter names that are not required
 	 * @param parameterRegexes the regular expressions to use for the parameter values
 	 * @return a {@link PathPatternBuilder} that will match paths to the pattern
 	 */
 	public static PathPatternBuilder parse(String pattern) {
 		PathPatternBuilder builder = new PathPatternBuilder();
 		parse(builder, pattern);
 		return builder;
 	}
 
 	private static void parse(PathPatternBuilder builder, String pattern) {
 		boolean requireNameStart = false;
 		StringBuilder nameBuilder = null;
 		boolean allowSlashes = false;
 		StringBuilder segmentBuilder = new StringBuilder();
 
 		for (int i = 0, len = pattern.length(); i < len; ++i) {
 			char c = pattern.charAt(i);
 			if (nameBuilder != null) {
 				if (isValidNameChar(c)) {
 					nameBuilder.append(c);
 				}
 				else {
 					builder.addParameterName(segmentBuilder, nameBuilder, allowSlashes);
 					segmentBuilder = new StringBuilder();
 					nameBuilder = null;
 
 					//
 					--i;
 				}
 			}
 			else if (c == PARAMETER_WITHOUT_SLASHES_PREFIX) {
 				allowSlashes = false;
 				requireNameStart = true;
 			}
 			else if (c == PARAMETER_WITH_SLASHES_PREFIX) {
 				allowSlashes = true;
 				requireNameStart = true;
 			}
 			else {
 				segmentBuilder.append(c);
 			}
 
 			// peek ahead at the next name character and make sure there can be a valid name
 			if (requireNameStart) {
 				if (i == len - 1) {
 					throw new IllegalArgumentException("Invalid pattern: expecting name, found end of pattern");
 				}
 				else {
 					char d = pattern.charAt(i + 1);
 					if (!isValidNameChar(d)) {
 						throw new IllegalArgumentException("Invalid pattern: expecting name, found '" + pattern.substring(i + 1) + "' at index " + i);
 					}
 					else {
 						requireNameStart = false;
 						nameBuilder = new StringBuilder();
 						nameBuilder.append(d);
 						++i;
 					}
 				}
 			}
 		}
 
 		builder.addParameterName(segmentBuilder, nameBuilder, allowSlashes);
 	}
 
 	/** Tests if the character is a valid name character: [A-Za-z0-9_]
 	 */
 	private static boolean isValidNameChar(char c) {
 		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
 	}
 
 	private void addParameterName(Object segment, Object parameterName, boolean allowSlashes) {
		String segmentString = segment.toString();
		if (segmentString.length() > 0) {
			pathSegmentBuilders.add(new StaticSegmentBuilder(segment.toString(), true));
 		}
 
		if (parameterName != null) {
			String parameterNameString = parameterName.toString();
			if (parameterNameString.length() > 0) {
				parameterNames.add(parameterNameString);
				pathSegmentBuilders.add(new ParameterSegmentBuilder(parameterNameString, allowSlashes));
			}
 		}
 	}
 
 	private interface PathSegmentBuilder extends Cloneable {
 		public PathSegmentBuilder apply(Map<String, String> parameters, Map<String, String> staticParameterValues);
 
 		public PathSegmentBuilder clone();
 	}
 
 	private static class StaticSegmentBuilder implements PathSegmentBuilder {
 		private String value;
 		private boolean required;
 
 		StaticSegmentBuilder(String value, boolean required) {
 			this.value = value;
 			this.required = required;
 		}
 
 		public PathSegmentBuilder apply(Map<String, String> parameters, Map<String, String> staticParameterValues) {
 			return clone();
 		}
 
 		public PathSegment createPathSegment() {
 			return new StaticSegment(value, required);
 		}
 
 		@Override
 		public StaticSegmentBuilder clone() {
 			return new StaticSegmentBuilder(value, required);
 		}
 	}
 
 	private static class ParameterSegmentBuilder implements PathSegmentBuilder {
 		boolean allowSlashes;
 		String name;
 
 		ParameterSegmentBuilder(String name, boolean allowSlashes) {
 			this.name = name;
 			this.allowSlashes = allowSlashes;
 		}
 
 		public PathSegmentBuilder apply(Map<String, String> parameters, Map<String, String> staticParameterValues) {
 			String value = parameters.get(name);
 			if (value != null) {
 				return new StaticSegmentBuilder(value, !value.equals(staticParameterValues.get(name)));
 			}
 			else {
 				return clone();
 			}
 		}
 
 		public PathSegment createPathSegment(String regex, boolean required) {
 			return new ParameterSegment(required, allowSlashes, name, regex);
 		}
 
 		@Override
 		public PathSegmentBuilder clone() {
 			try {
 				return (ParameterSegmentBuilder) super.clone();
 			}
 			catch (CloneNotSupportedException ex) {
 				throw new Error(ex);
 			}
 		}
 	}
 }
