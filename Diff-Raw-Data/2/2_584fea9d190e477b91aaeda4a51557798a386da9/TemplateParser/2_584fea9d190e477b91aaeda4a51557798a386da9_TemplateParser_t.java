 /*
  * utils - TemplateParser.java - Copyright © 2011 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.util.template;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import net.pterodactylus.util.template.ConditionalPart.AndCondition;
 import net.pterodactylus.util.template.ConditionalPart.Condition;
 import net.pterodactylus.util.template.ConditionalPart.DataCondition;
 import net.pterodactylus.util.template.ConditionalPart.DataTextCondition;
 import net.pterodactylus.util.template.ConditionalPart.NotCondition;
 import net.pterodactylus.util.template.ConditionalPart.NullDataCondition;
 import net.pterodactylus.util.template.ConditionalPart.OrCondition;
 
 /**
  * Parser for {@link Template}s.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class TemplateParser {
 
 	/**
 	 * Parses the input of the template if it wasn’t already parsed.
 	 *
 	 * @param input
 	 *            The input to parse
 	 * @return The parsed template
 	 * @throws TemplateException
 	 *             if the template can not be parsed
 	 */
 	public static Template parse(Reader input) throws TemplateException {
 		Template template = new Template();
 		template.add(extractParts(input));
 		return template;
 	}
 
 	//
 	// PRIVATE METHODS
 	//
 
 	/**
 	 * Parses the template and creates {@link Part}s of the input.
 	 *
 	 * @param input
 	 *            The input to parse
	 * @return The list of parts created from the input
 	 * @throws TemplateException
 	 *             if the template can not be parsed correctly
 	 */
 	private static Part extractParts(Reader input) throws TemplateException {
 		BufferedReader bufferedInputReader;
 		if (input instanceof BufferedReader) {
 			bufferedInputReader = (BufferedReader) input;
 		} else {
 			bufferedInputReader = new BufferedReader(input);
 		}
 		Stack<String> commandStack = new Stack<String>();
 		Stack<ContainerPart> partsStack = new Stack<ContainerPart>();
 		Stack<String> lastCollectionName = new Stack<String>();
 		Stack<String> lastLoopName = new Stack<String>();
 		Stack<Condition> lastCondition = new Stack<Condition>();
 		Stack<List<Condition>> lastConditions = new Stack<List<Condition>>();
 		Stack<String> lastIfCommand = new Stack<String>();
 		ContainerPart parts = new ContainerPart(1, 1);
 		StringBuilder currentTextPart = new StringBuilder();
 		boolean gotLeftAngleBracket = false;
 		boolean inAngleBracket = false;
 		boolean inSingleQuotes = false;
 		boolean inDoubleQuotes = false;
 		int line = 1;
 		int column = 1;
 		int startOfTagLine = 1;
 		int startOfTagColumn = 1;
 		while (true) {
 			int nextCharacter;
 			try {
 				nextCharacter = bufferedInputReader.read();
 				++column;
 			} catch (IOException ioe1) {
 				throw new TemplateException(line, column, "Can not read template.", ioe1);
 			}
 			if (nextCharacter == -1) {
 				break;
 			}
 			if (nextCharacter == 10) {
 				++line;
 				column = 1;
 			}
 			if (inAngleBracket) {
 				if (inSingleQuotes) {
 					if (nextCharacter == '\'') {
 						inSingleQuotes = false;
 					}
 					currentTextPart.append((char) nextCharacter);
 				} else if (inDoubleQuotes) {
 					if (nextCharacter == '"') {
 						inDoubleQuotes = false;
 					}
 					currentTextPart.append((char) nextCharacter);
 				} else if (nextCharacter == '\'') {
 					inSingleQuotes = true;
 					currentTextPart.append((char) nextCharacter);
 				} else if (nextCharacter == '"') {
 					inDoubleQuotes = true;
 					currentTextPart.append((char) nextCharacter);
 				} else if (nextCharacter == '>') {
 					inAngleBracket = false;
 					String tagContent = currentTextPart.toString().trim();
 					currentTextPart.setLength(0);
 					Iterator<String> tokens = parseTag(tagContent).iterator();
 					if (!tokens.hasNext()) {
 						throw new TemplateException(startOfTagLine, startOfTagColumn, "empty tag found");
 					}
 					String function = tokens.next();
 					if (function.startsWith("/")) {
 						String lastFunction = commandStack.pop();
 						if (!("/" + lastFunction).equals(function)) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "unbalanced template, /" + lastFunction + " expected, " + function + " found");
 						}
 						if (lastFunction.equals("foreach")) {
 							ContainerPart innerParts = parts;
 							parts = partsStack.pop();
 							lastCollectionName.pop();
 							lastLoopName.pop();
 							parts.add(innerParts);
 						} else if (lastFunction.equals("first") || lastFunction.equals("notfirst") || lastFunction.equals("last") || lastFunction.equals("notlast") || lastFunction.equals("odd") || lastFunction.equals("even")) {
 							ContainerPart innerParts = parts;
 							parts = partsStack.pop();
 							parts.add(innerParts);
 						} else if (lastFunction.equals("if")) {
 							ContainerPart innerParts = parts;
 							parts = partsStack.pop();
 							lastCondition.pop();
 							lastConditions.pop();
 							parts.add(innerParts);
 							lastIfCommand.pop();
 						}
 					} else if (function.equals("foreach")) {
 						if (!tokens.hasNext()) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "foreach requires at least one parameter");
 						}
 						String collectionName = tokens.next();
 						String itemName = null;
 						if (tokens.hasNext()) {
 							itemName = tokens.next();
 						}
 						String loopName = "loop";
 						if (tokens.hasNext()) {
 							loopName = tokens.next();
 						}
 						Filters filters;
 						if (loopName == null) {
 							loopName = "loop";
 							filters = parseFilters(startOfTagLine, startOfTagColumn, tokens, true);
 						} else {
 							filters = parseFilters(startOfTagLine, startOfTagColumn, tokens);
 						}
 						partsStack.push(parts);
 						parts = new LoopPart(startOfTagLine, startOfTagColumn, collectionName, itemName, loopName, filters);
 						commandStack.push("foreach");
 						lastCollectionName.push(collectionName);
 						lastLoopName.push(loopName);
 					} else if (function.equals("foreachelse")) {
 						if (!"foreach".equals(commandStack.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "foreachelse is only allowed in foreach");
 						}
 						partsStack.peek().add(parts);
 						parts = new EmptyLoopPart(startOfTagLine, startOfTagColumn, lastCollectionName.peek());
 					} else if (function.equals("first")) {
 						if (!"foreach".equals(commandStack.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "first is only allowed in foreach");
 						}
 						partsStack.push(parts);
 						final String loopName = lastLoopName.peek();
 						parts = new ConditionalPart(startOfTagLine, startOfTagColumn, new ConditionalPart.DataCondition(loopName + ".first"));
 						commandStack.push("first");
 					} else if (function.equals("notfirst")) {
 						if (!"foreach".equals(commandStack.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "notfirst is only allowed in foreach");
 						}
 						partsStack.push(parts);
 						final String loopName = lastLoopName.peek();
 						parts = new ConditionalPart(startOfTagLine, startOfTagColumn, new ConditionalPart.DataCondition(loopName + ".first", true));
 						commandStack.push("notfirst");
 					} else if (function.equals("last")) {
 						if (!"foreach".equals(commandStack.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "last is only allowed in foreach");
 						}
 						partsStack.push(parts);
 						final String loopName = lastLoopName.peek();
 						parts = new ConditionalPart(startOfTagLine, startOfTagColumn, new ConditionalPart.DataCondition(loopName + ".last"));
 						commandStack.push("last");
 					} else if (function.equals("notlast")) {
 						if (!"foreach".equals(commandStack.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "notlast is only allowed in foreach");
 						}
 						partsStack.push(parts);
 						final String loopName = lastLoopName.peek();
 						parts = new ConditionalPart(startOfTagLine, startOfTagColumn, new ConditionalPart.DataCondition(loopName + ".last", true));
 						commandStack.push("notlast");
 					} else if (function.equals("odd")) {
 						if (!"foreach".equals(commandStack.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "odd is only allowed in foreach");
 						}
 						partsStack.push(parts);
 						final String loopName = lastLoopName.peek();
 						parts = new ConditionalPart(startOfTagLine, startOfTagColumn, new ConditionalPart.DataCondition(loopName + ".odd"));
 						commandStack.push("odd");
 					} else if (function.equals("even")) {
 						if (!"foreach".equals(commandStack.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "even is only allowed in foreach");
 						}
 						partsStack.push(parts);
 						final String loopName = lastLoopName.peek();
 						parts = new ConditionalPart(startOfTagLine, startOfTagColumn, new ConditionalPart.DataCondition(loopName + ".even"));
 						commandStack.push("even");
 					} else if (function.equals("if") || function.equals("ifnull")) {
 						if (!tokens.hasNext()) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "if requires one or two parameters");
 						}
 						String itemName = tokens.next();
 						boolean checkForNull = function.equals("ifnull");
 						boolean invert = false;
 						if (itemName.equals("!")) {
 							invert = true;
 							if (!tokens.hasNext()) {
 								throw new TemplateException(startOfTagLine, startOfTagColumn, "if ! requires one parameter");
 							}
 							itemName = tokens.next();
 						} else {
 							if (itemName.startsWith("!")) {
 								invert = true;
 								itemName = itemName.substring(1);
 							}
 						}
 						boolean directText = false;
 						if (itemName.startsWith("=")) {
 							if (checkForNull) {
 								throw new TemplateException(startOfTagLine, startOfTagColumn, "direct text ('=') with ifnull is not allowed");
 							}
 							itemName = itemName.substring(1);
 							directText = true;
 						}
 						Filters filters = parseFilters(startOfTagLine, startOfTagColumn, tokens);
 						partsStack.push(parts);
 						Condition condition = checkForNull ? new NullDataCondition(itemName, invert) : (directText ? new DataTextCondition(itemName, filters, invert) : new DataCondition(itemName, filters, invert));
 						parts = new ConditionalPart(startOfTagLine, startOfTagColumn, condition);
 						commandStack.push("if");
 						lastCondition.push(condition);
 						lastConditions.push(new ArrayList<Condition>(Arrays.asList(condition)));
 						lastIfCommand.push("if");
 					} else if (function.equals("else")) {
 						if (!"if".equals(commandStack.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "else is only allowed in if");
 						}
 						if (!"if".equals(lastIfCommand.peek()) && !"elseif".equals(lastIfCommand.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "else may only follow if or elseif");
 						}
 						partsStack.peek().add(parts);
 						Condition condition = new NotCondition(new OrCondition(lastConditions.peek()));
 						parts = new ConditionalPart(startOfTagLine, startOfTagColumn, condition);
 						lastIfCommand.pop();
 						lastIfCommand.push("else");
 					} else if (function.equals("elseif") || function.equals("elseifnull")) {
 						if (!"if".equals(commandStack.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "elseif is only allowed in if");
 						}
 						if (!"if".equals(lastIfCommand.peek()) && !"elseif".equals(lastIfCommand.peek())) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "elseif is only allowed after if or elseif");
 						}
 						if (!tokens.hasNext()) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "elseif requires one or two parameters");
 						}
 						String itemName = tokens.next();
 						boolean checkForNull = function.equals("elseifnull");
 						boolean invert = false;
 						if (itemName.equals("!")) {
 							invert = true;
 							if (!tokens.hasNext()) {
 								throw new TemplateException(startOfTagLine, startOfTagColumn, "if ! requires one parameter");
 							}
 							itemName = tokens.next();
 						} else {
 							if (itemName.startsWith("!")) {
 								invert = true;
 								itemName = itemName.substring(1);
 							}
 						}
 						Filters filters = parseFilters(startOfTagLine, startOfTagColumn, tokens);
 						partsStack.peek().add(parts);
 						Condition condition = new AndCondition(new NotCondition(lastCondition.pop()), checkForNull ? new NullDataCondition(itemName, invert) : new DataCondition(itemName, filters, invert));
 						parts = new ConditionalPart(startOfTagLine, startOfTagColumn, condition);
 						lastCondition.push(condition);
 						lastConditions.peek().add(condition);
 						lastIfCommand.pop();
 						lastIfCommand.push("elseif");
 					} else if (function.equals("include")) {
 						if (!tokens.hasNext()) {
 							throw new TemplateException(startOfTagLine, startOfTagColumn, "include requires one parameter");
 						}
 						String templateName = tokens.next();
 						Map<String, String> parameters = parseParameters(startOfTagLine, startOfTagColumn, tokens);
 						parts.add(new TemplatePart(startOfTagLine, startOfTagColumn, templateName, parameters));
 					} else {
 						boolean directText = false;
 						boolean plugin = false;
 						String itemName = function;
 						if (function.equals("=")) {
 							if (!tokens.hasNext()) {
 								throw new TemplateException(startOfTagLine, startOfTagColumn, "empty tag found");
 							}
 							itemName = tokens.next();
 							directText = true;
 						} else if (function.equals(":")) {
 							if (!tokens.hasNext()) {
 								throw new TemplateException(startOfTagLine, startOfTagColumn, "missing plugin name");
 							}
 							itemName = tokens.next();
 							plugin = true;
 						} else if (function.startsWith(":")) {
 							itemName = function.substring(1);
 							plugin = true;
 						} else if (function.startsWith("=")) {
 							itemName = function.substring(1);
 							directText = true;
 						}
 						if (plugin) {
 							Map<String, String> pluginParameters = null;
 							pluginParameters = parseParameters(startOfTagLine, startOfTagColumn, tokens);
 							parts.add(new PluginPart(startOfTagLine, startOfTagColumn, itemName, pluginParameters));
 						} else {
 							Filters filterDefinitions = parseFilters(startOfTagLine, startOfTagColumn, tokens);
 							if (directText) {
 								parts.add(new FilteredTextPart(startOfTagLine, startOfTagColumn, itemName, filterDefinitions));
 							} else {
 								parts.add(new FilteredPart(startOfTagLine, startOfTagColumn, itemName, filterDefinitions));
 							}
 						}
 					}
 				} else {
 					currentTextPart.append((char) nextCharacter);
 				}
 				continue;
 			}
 			if (gotLeftAngleBracket) {
 				if (nextCharacter == '%') {
 					startOfTagLine = line;
 					startOfTagColumn = column;
 					inAngleBracket = true;
 					if (currentTextPart.length() > 0) {
 						parts.add(new TextPart(startOfTagLine, startOfTagColumn, currentTextPart.toString()));
 						currentTextPart.setLength(0);
 					}
 				} else {
 					currentTextPart.append('<').append((char) nextCharacter);
 				}
 				gotLeftAngleBracket = false;
 				continue;
 			}
 			if (nextCharacter == '<') {
 				gotLeftAngleBracket = true;
 				continue;
 			}
 			currentTextPart.append((char) nextCharacter);
 		}
 		if (currentTextPart.length() > 0) {
 			parts.add(new TextPart(startOfTagLine, startOfTagColumn, currentTextPart.toString()));
 		}
 		if (!partsStack.isEmpty()) {
 			throw new TemplateException(line, column, "Unbalanced template.");
 		}
 		return parts;
 	}
 
 	/**
 	 * Parses filters from the rest of the tokens.
 	 *
 	 * @param line
 	 *            The line number of the tag
 	 * @param column
 	 *            The column number of the tag
 	 * @param tokens
 	 *            The tokens to parse
 	 * @return The parsed filters
 	 */
 	private static Filters parseFilters(int line, int column, Iterator<String> tokens) {
 		return parseFilters(line, column, tokens, false);
 	}
 
 	/**
 	 * Parses filters from the rest of the tokens.
 	 *
 	 * @param line
 	 *            The line number of the tag
 	 * @param column
 	 *            The column number of the tag
 	 * @param tokens
 	 *            The tokens to parse
 	 * @param pipeTokenPresent
 	 *            {@code true} to assume that the “|” separator token has
 	 *            already been parsed
 	 * @return The parsed filters
 	 */
 	private static Filters parseFilters(int line, int column, Iterator<String> tokens, boolean pipeTokenPresent) {
 		Filters filterDefinitions = new Filters();
 		if (!pipeTokenPresent && (tokens.hasNext() && (tokens.next() != null))) {
 			throw new TemplateException(line, column, "expected \"|\" token");
 		}
 		while (tokens.hasNext()) {
 			String filterName = tokens.next();
 			if (filterName == null) {
 				throw new TemplateException(line, column, "missing filter name");
 			}
 			Map<String, String> filterParameters = parseParameters(line, column, tokens);
 			filterDefinitions.add(new FilterDefinition(filterName, filterParameters));
 		}
 		return filterDefinitions;
 	}
 
 	/**
 	 * Parses parameters from the given tokens.
 	 *
 	 * @param line
 	 *            The line number of the tag
 	 * @param column
 	 *            The column number of the tag
 	 * @param tokens
 	 *            The tokens to parse the parameters from
 	 * @return The parsed parameters
 	 * @throws TemplateException
 	 *             if an invalid parameter declaration is found
 	 */
 	private static Map<String, String> parseParameters(int line, int column, Iterator<String> tokens) throws TemplateException {
 		Map<String, String> parameters = new HashMap<String, String>();
 		while (tokens.hasNext()) {
 			String parameterToken = tokens.next();
 			if (parameterToken == null) {
 				break;
 			}
 			int equals = parameterToken.indexOf('=');
 			if (equals == -1) {
 				throw new TemplateException(line, column, "found parameter without \"=\" sign");
 			}
 			String key = parameterToken.substring(0, equals).trim();
 			String value = parameterToken.substring(equals + 1);
 			parameters.put(key, value);
 		}
 		return parameters;
 	}
 
 	/**
 	 * Parses the content of a tag into words, obeying syntactical rules about
 	 * separators and quotes. Separators are parsed as {@code null}.
 	 *
 	 * @param tagContent
 	 *            The content of the tag to parse
 	 * @return The parsed words
 	 */
 	static List<String> parseTag(String tagContent) {
 		List<String> expressions = new ArrayList<String>();
 		boolean inSingleQuotes = false;
 		boolean inDoubleQuotes = false;
 		boolean inBackslash = false;
 		StringBuilder currentExpression = new StringBuilder();
 		for (char c : tagContent.toCharArray()) {
 			if (inSingleQuotes) {
 				if (c == '\'') {
 					inSingleQuotes = false;
 				} else {
 					currentExpression.append(c);
 				}
 			} else if (inBackslash) {
 				currentExpression.append(c);
 				inBackslash = false;
 			} else if (inDoubleQuotes) {
 				if (c == '"') {
 					inDoubleQuotes = false;
 				} else if (c == '\\') {
 					inBackslash = true;
 				} else {
 					currentExpression.append(c);
 				}
 			} else {
 				if (c == '\'') {
 					inSingleQuotes = true;
 				} else if (c == '"') {
 					inDoubleQuotes = true;
 				} else if (c == '\\') {
 					inBackslash = true;
 				} else if (c == '|') {
 					if (currentExpression.toString().trim().length() > 0) {
 						expressions.add(currentExpression.toString());
 						currentExpression.setLength(0);
 					}
 					expressions.add(null);
 				} else {
 					if (c == ' ') {
 						if (currentExpression.length() > 0) {
 							expressions.add(currentExpression.toString());
 							currentExpression.setLength(0);
 						}
 					} else {
 						currentExpression.append(c);
 					}
 				}
 			}
 		}
 		if (currentExpression.length() > 0) {
 			expressions.add(currentExpression.toString());
 		}
 		return expressions;
 	}
 
 	/**
 	 * Wrapper for the name of a {@link Filter} and its parameters.
 	 *
 	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 	 */
 	public static class FilterDefinition {
 
 		/** The name of the filter. */
 		private final String name;
 
 		/** The parameters of the filter. */
 		private final Map<String, String> parameters;
 
 		/**
 		 * Creates a new filter definition.
 		 *
 		 * @param name
 		 *            The name of the filter
 		 * @param parameters
 		 *            The parameters of the filter
 		 */
 		public FilterDefinition(String name, Map<String, String> parameters) {
 			this.name = name;
 			this.parameters = parameters;
 		}
 
 		/**
 		 * Returns the name of the filter.
 		 *
 		 * @return The name of the filter
 		 */
 		public String getName() {
 			return name;
 		}
 
 		/**
 		 * Returns the parameters of the filter.
 		 *
 		 * @return The parameters of the filter
 		 */
 		public Map<String, String> getParameters() {
 			return parameters;
 		}
 
 	}
 
 	/**
 	 * Convenience class that wraps around a {@link List} of
 	 * {@link FilterDefinition}s and can filter an object through all the
 	 * filters it contains.
 	 *
 	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 	 */
 	public static class Filters extends ArrayList<FilterDefinition> {
 
 		/**
 		 * Filters the given object through all filters.
 		 *
 		 * @param line
 		 *            The line of the tag
 		 * @param column
 		 *            The column of the tag
 		 * @param templateContext
 		 *            The template context
 		 * @param data
 		 *            The data to filter
 		 * @return The filtered data
 		 */
 		public Object filter(int line, int column, TemplateContext templateContext, Object data) {
 			Object output = data;
 			for (FilterDefinition filterDefinition : this) {
 				Filter filter = templateContext.getFilter(filterDefinition.getName());
 				if (filter == null) {
 					throw new TemplateException(line, column, "Filter “" + filterDefinition.getName() + "” not found.");
 				}
 				try {
 					output = filter.format(templateContext, output, filterDefinition.getParameters());
 				} catch (Exception e1) {
 					throw new TemplateException(line, column, "Error while applying filter.", e1);
 				}
 			}
 			return output;
 		}
 
 	}
 
 }
