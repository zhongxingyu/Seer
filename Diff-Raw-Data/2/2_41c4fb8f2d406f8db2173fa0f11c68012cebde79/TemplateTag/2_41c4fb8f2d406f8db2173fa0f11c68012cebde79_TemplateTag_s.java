 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.parser.jflex;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicType;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 
 /**
  * <code>TemplateTag</code> parses Mediawiki template syntax, which allows
  * programmatic structures to be embedded in wiki syntax.
  */
 public class TemplateTag implements JFlexParserTag {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(TemplateTag.class.getName());
 	protected static final String TEMPLATE_INCLUSION = "template-inclusion";
 	private static Pattern PARAM_NAME_VALUE_PATTERN = Pattern.compile("[\\s]*([A-Za-z0-9_\\ \\-]+)[\\s]*\\=([\\s\\S]*)");
 
 	/**
 	 * Once the template call has been parsed and the template values have been
 	 * determined, parse the template body and apply those template values.
 	 * Parameters may be embedded or have default values, so there is some
 	 * voodoo magic that happens here to first parse any embedded values, and
 	 * to apply default values when no template value has been set.
 	 */
 	private String applyParameter(ParserInput parserInput, String param, Map<String, String> parameterValues) throws ParserException {
 		String content = param.substring("{{{".length(), param.length() - "}}}".length());
 		// re-parse in case of embedded templates or params
 		content = this.parseTemplateBody(parserInput, content, parameterValues);
 		String name = this.parseParamName(content);
 		String defaultValue = this.parseParamDefaultValue(parserInput, content);
 		String value = parameterValues.get(name);
 		if (value == null && defaultValue == null) {
 			return param;
 		}
 		return (value == null) ? defaultValue : value;
 	}
 
 	/**
 	 * Parse a call to a Mediawiki template of the form "{{template|param1|param2}}"
 	 * and return the resulting template output.
 	 */
 	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
 		// validate and extract the template content
 		if (StringUtils.isBlank(raw)) {
 			throw new ParserException("Empty template text");
 		}
 		if (!raw.startsWith("{{") || !raw.endsWith("}}")) {
 			throw new ParserException ("Invalid template text: " + raw);
 		}
 		try {
 			return this.parseTemplateOutput(lexer.getParserInput(), lexer.getParserOutput(), lexer.getMode(), raw, true);
 		} catch (DataAccessException e) {
 			throw new ParserException("Data access exception while parsing: " + raw, e);
 		}
 	}
 
 	/**
 	 * Parses the template content and returns the parsed output.  If there is no result (such
 	 * as when a template does not exist) this method will either return an edit link to the
 	 * template topic page, or if allowTemplateEdit is <code>false</code> it will return
 	 * <code>null</code> (used with substitutions, where an edit link should not be shown).
 	 */
 	private String parseTemplateOutput(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw, boolean allowTemplateEdit) throws DataAccessException, ParserException {
 		String templateContent = raw.substring("{{".length(), raw.length() - "}}".length());
 		// parse for nested templates, signatures, etc.
 		parserInput.incrementTemplateDepth();
 		templateContent = JFlexParserUtil.parseFragment(parserInput, templateContent, mode);
 		parserInput.decrementTemplateDepth();
 		// update the raw value to handle cases such as a signature in the template content
 		raw = "{{" + templateContent + "}}";
 		// check for substitution ("{{subst:Template}}")
 		String subst = this.parseSubstitution(parserInput, parserOutput, mode, raw, templateContent);
 		if (subst != null) {
 			return subst;
 		}
 		// check for magic word or parser function
 		String[] parserFunctionInfo = ParserFunctionUtil.parseParserFunctionInfo(templateContent);
 		if (MagicWordUtil.isMagicWord(templateContent) || parserFunctionInfo != null) {
 			if (mode <= JFlexParser.MODE_MINIMAL) {
 				return raw;
 			}
 			if (MagicWordUtil.isMagicWord(templateContent)) {
 				return MagicWordUtil.processMagicWord(parserInput, templateContent);
 			} else {
 				return ParserFunctionUtil.processParserFunction(parserInput, parserFunctionInfo[0], parserFunctionInfo[1]);
 			}
 		}
 		// extract the template name
 		String name = this.parseTemplateName(parserInput.getVirtualWiki(), templateContent);
 		boolean inclusion = false;
 		if (name.startsWith(Namespace.SEPARATOR)) {
 			name = name.substring(1);
 			inclusion = true;
 		}
 		// get the parsed template body
 		parserInput.incrementTemplateDepth();
 		Topic templateTopic = WikiBase.getDataHandler().lookupTopic(parserInput.getVirtualWiki(), name, false, null);
 		this.processTemplateMetadata(parserInput, parserOutput, templateTopic, raw, name);
 		String result = null;
 		if (mode <= JFlexParser.MODE_MINIMAL) {
 			result = raw;
 		} else {
 			// make sure template was not redirected
 			if (templateTopic != null && templateTopic.getTopicType() == TopicType.REDIRECT) {
 				templateTopic = WikiUtil.findRedirectedTopic(templateTopic, 0);
 				name = templateTopic.getName();
 			}
 			if (templateTopic != null && templateTopic.getTopicType() == TopicType.REDIRECT) {
 				// redirection target does not exist
 				templateTopic = null;
 			}
 			if (inclusion) {
 				result = this.processTemplateInclusion(parserInput, parserOutput, mode, templateTopic, raw, name);
 			} else if (templateTopic == null) {
 				result = ((allowTemplateEdit) ? "[[" + name + "]]" : null);
 			} else {
 				result = this.processTemplateContent(parserInput, parserOutput, templateTopic, templateContent, name);
 			}
 		}
 		parserInput.decrementTemplateDepth();
 		return result;
 	}
 
 	/**
 	 * Given template parameter content of the form "name" or "name|default",
 	 * return the default value if it exists.
 	 */
 	private String parseParamDefaultValue(ParserInput parserInput, String raw) throws ParserException {
 		List<String> tokens = JFlexParserUtil.tokenizeParamString(raw);
 		if (tokens.size() < 2) {
 			return null;
 		}
 		// table elements mess up default processing, so just return anything after
 		// the first parameter to avoid having to implement special table logic
 		String param1 = tokens.get(0);
 		String value = raw.substring(param1.length() + 1);
 		return JFlexParserUtil.parseFragment(parserInput, value, JFlexParser.MODE_PREPROCESS);
 	}
 
 	/**
 	 * Given template parameter content of the form "name" or "name|default",
 	 * return the parameter name.
 	 */
 	private String parseParamName(String raw) throws ParserException {
 		int pos = raw.indexOf('|');
 		String name = ((pos != -1) ? raw.substring(0, pos) : raw).trim();
 		if (StringUtils.isBlank(name)) {
 			// FIXME - no need for an exception
 			throw new ParserException("No parameter name specified");
 		}
 		return name;
 	}
 
 	/**
 	 * Determine if template content is of the form "subst:XXX".  If it is,
 	 * process it, otherwise return <code>null</code>.
 	 */
 	private String parseSubstitution(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw, String templateContent) throws DataAccessException, ParserException {
 		// is it a substitution?
 		templateContent = templateContent.trim();
 		if (!templateContent.startsWith("subst:") || templateContent.length() <= "subst:".length()) {
 			return null;
 		}
 		// get the substitution content
 		String substContent = templateContent.trim().substring("subst:".length()).trim();
 		if (substContent.length() == 0) {
 			return null;
 		}
 		// re-parse the substitution value.  make sure it is parsed in at least MODE_PREPROCESS
 		// so that values are properly replaced prior to saving.
 		String output = this.parseTemplateOutput(parserInput, parserOutput, JFlexParser.MODE_PREPROCESS, "{{" + substContent + "}}", false);
 		return (output == null) ? raw : output;
 	}
 
 	/**
 	 * After template parameter values have been set, process the template body
 	 * and replace parameters with parameter values or defaults, processing any
 	 * embedded parameters or templates.
 	 */
 	private String parseTemplateBody(ParserInput parserInput, String content, Map<String, String> parameterValues) throws ParserException {
 		StringBuilder output = new StringBuilder();
 		int pos = 0;
 		while (pos < content.length()) {
 			String substring = content.substring(pos);
 			if (substring.startsWith("{{{")) {
 				// special case for cases like "{{{{{1}}}}}" where the parameter itself is a template reference
 				while (content.substring(pos + 1).startsWith("{{{")) {
 					output.append(content.charAt(pos));
 					pos++;
 				}
 				int endPos = Utilities.findMatchingEndTag(content, pos, "{{{", "}}}");
 				if (endPos != -1) {
 					// handle cases such as {{{1|{{PAGENAME}}}}} where endPos will be two positions too early
 					if (content.substring(pos + 3, endPos).indexOf("{{") != -1 && content.length() > (endPos + 2) && content.substring(endPos, endPos + 2).equals("}}")) {
 						endPos += 2;
 					}
 					String param = content.substring(pos, endPos);
 					output.append(this.applyParameter(parserInput, param, parameterValues));
 				}
 				pos = endPos;
 			} else {
 				output.append(content.charAt(pos));
 				pos++;
 			}
 		}
		return JFlexParserUtil.parseFragment(parserInput, output.toString(), JFlexParser.MODE_PREPROCESS);
 	}
 
 	/**
 	 * Given a template call of the form "{{template|param|param}}", return
 	 * the template name.
 	 */
 	private String parseTemplateName(String virtualWiki, String raw) throws ParserException {
 		String name = raw;
 		int pos = raw.indexOf('|');
 		if (pos != -1) {
 			name = name.substring(0, pos);
 		}
 		name = Utilities.decodeTopicName(name.trim(), true);
 		if (StringUtils.isBlank(name)) {
 			// FIXME - no need for an exception
 			throw new ParserException("No template name specified");
 		}
 		if (name.startsWith(Namespace.SEPARATOR)) {
 			if (name.length() == 1) {
 				// FIXME - no need for an exception
 				throw new ParserException("No template name specified");
 			}
 		} else if (!name.startsWith(Namespace.TEMPLATE.getLabel(virtualWiki) + Namespace.SEPARATOR)) {
 			name = Namespace.TEMPLATE.getLabel(virtualWiki) + Namespace.SEPARATOR + StringUtils.capitalize(name);
 		}
 		return name;
 	}
 
 	/**
 	 * Given a template call of the form "{{name|param=value|param=value}}"
 	 * parse the parameter names and values.
 	 */
 	private Map<String, String> parseTemplateParameterValues(ParserInput parserInput, String templateContent) throws ParserException {
 		Map<String, String> parameterValues = new HashMap<String, String>();
 		List<String> tokens = JFlexParserUtil.tokenizeParamString(templateContent);
 		if (tokens.isEmpty()) {
 			throw new ParserException("No template name found in " + templateContent);
 		}
 		int count = -1;
 		for (String token : tokens) {
 			count++;
 			if (count == 0) {
 				// first token is template name
 				continue;
 			}
 			String[] nameValue = this.tokenizeNameValue(token);
 			String name = (StringUtils.isBlank(nameValue[0]) ? Integer.toString(count) : nameValue[0].trim());
 			String value = (nameValue[1] == null) ? null : nameValue[1].trim();
 			parameterValues.put(name, value);
 		}
 		return parameterValues;
 	}
 
 	/**
 	 * Given a template call of the form "{{name|param|param}}" return the
 	 * parsed output.
 	 */
 	private String processTemplateContent(ParserInput parserInput, ParserOutput parserOutput, Topic templateTopic, String templateContent, String name) throws ParserException {
 		// set template parameter values
 		Map<String, String> parameterValues = this.parseTemplateParameterValues(parserInput, templateContent);
 		return this.parseTemplateBody(parserInput, templateTopic.getTopicContent(), parameterValues);
 	}
 
 	/**
 	 * Given a template call of the form "{{:name}}" parse the template
 	 * inclusion.
 	 */
 	private String processTemplateInclusion(ParserInput parserInput, ParserOutput parserOutput, int mode, Topic templateTopic, String raw, String name) throws ParserException {
 		if (templateTopic == null) {
 			return "[[" + name + "]]";
 		}
 		// FIXME - disable section editing
 		parserInput.getTempParams().put(TEMPLATE_INCLUSION, "true");
 		return (StringUtils.isBlank(templateTopic.getTopicContent())) ? templateTopic.getTopicContent() : JFlexParserUtil.parseFragment(parserInput, templateTopic.getTopicContent(), mode);
 	}
 
 	/**
 	 * Process template values, setting link and other metadata output values.
 	 */
 	private void processTemplateMetadata(ParserInput parserInput, ParserOutput parserOutput, Topic templateTopic, String raw, String name) {
 		name = (templateTopic != null) ? templateTopic.getName() : name;
 		parserOutput.addLink(name);
 		parserOutput.addTemplate(name);
 	}
 
 	/**
 	 *
 	 */
 	private String[] tokenizeNameValue(String content) {
 		String[] results = new String[2];
 		results[0] = null;
 		results[1] = content;
 		Matcher m = PARAM_NAME_VALUE_PATTERN.matcher(content);
 		if (m.matches()) {
 			results[0] = m.group(1);
 			results[1] = m.group(2);
 		}
 		return results;
 	}
 }
