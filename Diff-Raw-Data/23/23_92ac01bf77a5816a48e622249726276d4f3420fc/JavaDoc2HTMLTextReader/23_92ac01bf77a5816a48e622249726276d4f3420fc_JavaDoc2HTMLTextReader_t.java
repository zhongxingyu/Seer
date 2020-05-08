 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.scriptdoc;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.corext.SourceRange;
 import org.eclipse.dltk.internal.ui.text.SubstitutionTextReader;
 import org.eclipse.dltk.javascript.core.JSKeywordCategory;
 import org.eclipse.dltk.javascript.core.JSKeywordManager;
 import org.eclipse.dltk.javascript.parser.jsdoc.JSDocTag;
 import org.eclipse.dltk.utils.TextUtils;
 
 public class JavaDoc2HTMLTextReader extends SubstitutionTextReader {
 
 	/*
 	 * Standard doc tag name (value {@value}).
 	 */
 	public static final String TAG_AUTHOR = "@author"; //$NON-NLS-1$
 
 	/**
 	 * Standard inline doc tag name (value {@value} ).
 	 * <p>
 	 * Note that this tag first appeared in J2SE 5.
 	 * </p>
 	 * 
 	 * @since 3.1
 	 */
 	public static final String TAG_CODE = "@code"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_DEPRECATED = "@deprecated"; //$NON-NLS-1$
 
 	/**
 	 * Standard inline doc tag name (value {@value} ).
 	 */
 	public static final String TAG_DOCROOT = "@docRoot"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_EXCEPTION = "@exception"; //$NON-NLS-1$
 
 	/**
 	 * Standard inline doc tag name (value {@value} ).
 	 */
 	public static final String TAG_INHERITDOC = "@inheritDoc"; //$NON-NLS-1$
 
 	/**
 	 * Standard inline doc tag name (value {@value} ).
 	 */
 	public static final String TAG_LINK = "@link"; //$NON-NLS-1$
 
 	/**
 	 * Standard inline doc tag name (value {@value} ).
 	 */
 	public static final String TAG_LINKPLAIN = "@linkplain"; //$NON-NLS-1$
 
 	/**
 	 * Standard inline doc tag name (value {@value} ).
 	 * <p>
 	 * Note that this tag first appeared in J2SE 5.
 	 * </p>
 	 * 
 	 * @since 3.1
 	 */
 	public static final String TAG_LITERAL = "@literal"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_PARAM = "@param"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_RETURN = "@return"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_RETURNS = "@returns"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_SEE = "@see"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_SERIAL = "@serial"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_SERIALDATA = "@serialData"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_SERIALFIELD = "@serialField"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_SINCE = "@since"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_THROWS = "@throws"; //$NON-NLS-1$
 
 	/**
 	 * Standard inline doc tag name (value {@value} ).
 	 */
 	public static final String TAG_VALUE = "@value"; //$NON-NLS-1$
 
 	/**
 	 * Standard doc tag name (value {@value} ).
 	 */
 	public static final String TAG_VERSION = "@version"; //$NON-NLS-1$
 
 	static private class Pair {
 		final String fTag;
 		final String fContent;
 
 		Pair(String tag, String content) {
 			fTag = tag;
 			fContent = content;
 		}
 	}
 
 	private List<String> fParameters;
 	private String fReturn;
 	private List<String> fExceptions;
 	private List<String> fAuthors;
 	private List<String> fSees;
 	private List<String> fSince;
 	private List<Pair> fRest; // list of Pair objects
 
 	public JavaDoc2HTMLTextReader(Reader reader) {
 		super(reader);
 		setSkipWhitespace(false);
 	}
 
 	private int getTag(StringBuffer buffer) throws IOException {
 		int c = nextChar();
 		while (c == '.' || c == '-' || c != -1
 				&& Character.isLetterOrDigit((char) c)) {
 			buffer.append((char) c);
 			c = nextChar();
 		}
 		return c;
 	}
 
 	private int getContent(StringBuffer buffer, char stopChar)
 			throws IOException {
 		int c = nextChar();
 		while (c != -1 && c != stopChar) {
 			buffer.append((char) c);
 			c = nextChar();
 		}
 		return c;
 	}
 
 	private int getContentUntilNextTag(StringBuffer buffer) throws IOException {
 		int c = nextChar();
 		boolean blockStartRead = false;
 		while (c != -1) {
 			if (c == '@') {
 				int index = buffer.length();
 				while (--index >= 0
 						&& Character.isWhitespace(buffer.charAt(index))) {
 					switch (buffer.charAt(index)) {
 					case '\n':
 					case '\r':
 						return c;
 					}
 					if (index <= 0) {
 						return c;
 					}
 				}
 			}
 			if (blockStartRead) {
 				buffer.append(processBlockTag());
 				blockStartRead = false;
 			} else {
 				buffer.append((char) c);
 			}
 
 			c = nextChar();
 			blockStartRead = c == '{';
 		}
 		return c;
 	}
 
 	private String substituteQualification(String qualification) {
 		String result = qualification.replace('#', '.');
 		if (result.startsWith(".")) { //$NON-NLS-1$
 			result = result.substring(1);
 		}
 		return result;
 	}
 
 	static enum TypedDefinition {
 		NONE, AUTO, PARAM
 	}
 
 	private void printDefinitions(StringBuffer buffer, List<String> list,
 			TypedDefinition typed) {
 		Iterator<String> e = list.iterator();
 		while (e.hasNext()) {
 			String s = e.next();
 			printDefinition(buffer, s, typed);
 		}
 	}
 
 	private void printDefinition(StringBuffer buffer, String s,
 			TypedDefinition typed) {
 		buffer.append("<dd>"); //$NON-NLS-1$
 		if (s != null && s.length() != 0) {
 			if (typed == TypedDefinition.NONE) {
 				buffer.append(s);
 			} else if (typed == TypedDefinition.PARAM) {
 				final ISourceRange param = getParamRange(s);
 				if (param != null) {
 					if (param.getOffset() > 0) {
 						buffer.append("<i>");
 						buffer.append(TextUtils.escapeHTML(s.substring(0,
 								param.getOffset())));
 						buffer.append("</i>");
 					}
 					buffer.append("<b>"); //$NON-NLS-1$
 					buffer.append(TextUtils.escapeHTML(s.substring(
 							param.getOffset(),
 							param.getOffset() + param.getLength())));
 					buffer.append("</b>"); //$NON-NLS-1$
 					buffer.append(s.substring(param.getOffset()
 							+ param.getLength()));
 				} else {
 					buffer.append(s);
 				}
 			} else {
 				assert (typed == TypedDefinition.AUTO);
 				int endOfType = skipTypeDefinition(s);
 				if (endOfType > 0) {
 					buffer.append("<i>");
 					buffer.append(TextUtils.escapeHTML(s
 							.substring(0, endOfType)));
 					buffer.append("</i>");
 				}
 				buffer.append(s.substring(endOfType));
 			}
 		}
 		buffer.append("</dd>"); //$NON-NLS-1$
 	}
 
 	private int skipTypeDefinition(String s) {
 		final int length = s.length();
 		int i = 0;
		int braces = 0;
 		// \s*
 		while (i < length && Character.isWhitespace(s.charAt(i)))
 			++i;
 		if (i < length && s.charAt(i) == '{') {
			braces++;
 			++i;
			while (i < length) {
				if (s.charAt(i) == '}') {
					if (--braces == 0)
						break;
				}
				if (s.charAt(i) == '{')
					braces++;
 				++i;
 			}
 			if (i < length) {
 				++i; // skip closing '}'
 			}
 		}
 		return i;
 	}
 
 	private ISourceRange getParamRange(String s) {
 		int i = skipTypeDefinition(s);
 		final int length = s.length();
 		while (i < length && Character.isWhitespace(s.charAt(i)))
 			++i;
 		final int paramStart = i;
 		if (i < length && s.charAt(i) == '<') {
 			++i;
 			// generic type parameter
 			// read <\s*\w*\s*>
 			while (i < length && Character.isWhitespace(s.charAt(i)))
 				++i;
 			while (i < length && Character.isJavaIdentifierPart(s.charAt(i)))
 				++i;
 			while (i < length && s.charAt(i) != '>')
 				++i;
 		} else {
			if (i < length && s.charAt(i) == '[')
				i++; // optional
 			// simply read an identifier
			while (i < length
					&& (Character.isJavaIdentifierPart(s.charAt(i)) || s
							.charAt(i) == '.'))
 				++i;
			if (i < length && s.charAt(i) == ']')
				i++; // optional
 		}
 		if (i > paramStart) {
 			return new SourceRange(paramStart, i - paramStart);
 		}
 
 		return null;
 	}
 
 	private void print(StringBuffer buffer, String tag, List<String> elements,
 			TypedDefinition typed) {
 		if (!elements.isEmpty()) {
 			buffer.append("<dt>"); //$NON-NLS-1$
 			buffer.append(tag);
 			buffer.append("</dt>"); //$NON-NLS-1$
 			printDefinitions(buffer, elements, typed);
 		}
 	}
 
 	private void print(StringBuffer buffer, String tag, String content,
 			TypedDefinition typed) {
 		if (content != null) {
 			buffer.append("<dt>"); //$NON-NLS-1$
 			buffer.append(tag);
 			buffer.append("</dt>"); //$NON-NLS-1$
 			printDefinition(buffer, content, typed);
 		}
 	}
 
 	private void printRest(StringBuffer buffer) {
 		if (!fRest.isEmpty()) {
 			final Set<String> definedTags = new HashSet<String>();
 			Collections.addAll(definedTags, JSDocTag.getTags());
 			ISourceModule module = null; /* TODO identify module? */
 			Collections.addAll(definedTags, JSKeywordManager.getInstance()
 					.getKeywords(JSKeywordCategory.JS_DOC_TAG, module));
 			final List<Pair> unknowTags = new ArrayList<Pair>();
 			for (Pair p : fRest) {
 				buffer.append("<dt>"); //$NON-NLS-1$
 				if (definedTags.contains(p.fTag)) {
 					buffer.append(Character.toUpperCase(p.fTag.charAt(1)))
 							.append(p.fTag.substring(2));
 					if (p.fContent.length() != 0) {
 						buffer.append(":");
 					}
 					buffer.append("</dt>"); //$NON-NLS-1$
 					printDefinition(buffer, p.fContent, TypedDefinition.AUTO);
 				} else {
 					unknowTags.add(p);
 				}
 			}
 			for (Pair p : unknowTags) {
 				buffer.append("<dt>"); //$NON-NLS-1$
 				buffer.append(p.fTag);
 				if (p.fContent.length() != 0) {
 					buffer.append(":");
 				}
 				buffer.append("</dt>"); //$NON-NLS-1$
 				printDefinition(buffer, p.fContent, TypedDefinition.AUTO);
 			}
 		}
 	}
 
 	private String printSimpleTag() {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("<dl>"); //$NON-NLS-1$
 		print(buffer,
 				JavaDocMessages.JavaDoc2HTMLTextReader_parameters_section,
 				fParameters, TypedDefinition.PARAM);
 		print(buffer, JavaDocMessages.JavaDoc2HTMLTextReader_returns_section,
 				fReturn, TypedDefinition.AUTO);
 		print(buffer, JavaDocMessages.JavaDoc2HTMLTextReader_throws_section,
 				fExceptions, TypedDefinition.AUTO);
 		print(buffer, JavaDocMessages.JavaDoc2HTMLTextReader_author_section,
 				fAuthors, TypedDefinition.NONE);
 		print(buffer, JavaDocMessages.JavaDoc2HTMLTextReader_since_section,
 				fSince, TypedDefinition.NONE);
 		print(buffer, JavaDocMessages.JavaDoc2HTMLTextReader_see_section,
 				fSees, TypedDefinition.NONE);
 		printRest(buffer);
 		buffer.append("</dl>"); //$NON-NLS-1$
 
 		return buffer.toString();
 	}
 
 	private void handleTag(String tag, String tagContent) {
 
 		tagContent = tagContent.trim();
 
 		if (TAG_PARAM.equals(tag))
 			fParameters.add(tagContent);
 		else if (TAG_RETURN.equals(tag) || TAG_RETURNS.equals(tag))
 			fReturn = tagContent;
 		else if (TAG_EXCEPTION.equals(tag))
 			fExceptions.add(tagContent);
 		else if (TAG_THROWS.equals(tag))
 			fExceptions.add(tagContent);
 		else if (TAG_AUTHOR.equals(tag))
 			fAuthors.add(substituteQualification(tagContent));
 		else if (TAG_SEE.equals(tag))
 			fSees.add(substituteQualification(tagContent));
 		else if (TAG_SINCE.equals(tag))
 			fSince.add(substituteQualification(tagContent));
 		else {
 			fRest.add(new Pair(tag, tagContent));
 		}
 	}
 
 	/*
 	 * A '@' has been read. Process a javadoc tag
 	 */
 	private String processSimpleTag() throws IOException {
 
 		fParameters = new ArrayList<String>();
 		fExceptions = new ArrayList<String>();
 		fAuthors = new ArrayList<String>();
 		fSees = new ArrayList<String>();
 		fSince = new ArrayList<String>();
 		fRest = new ArrayList<Pair>();
 
 		StringBuffer buffer = new StringBuffer();
 		int c = '@';
 		while (c != -1) {
 
 			buffer.setLength(0);
 			buffer.append((char) c);
 			c = getTag(buffer);
 			String tag = buffer.toString();
 
 			buffer.setLength(0);
 			if (c != -1) {
 				// e.g. @SuppressWarnings(...) case
 				if (!Character.isWhitespace(c)) {
 					buffer.append((char) c);
 				}
 				c = getContentUntilNextTag(buffer);
 			}
 
 			handleTag(tag, buffer.toString());
 		}
 
 		return printSimpleTag();
 	}
 
 	private String printBlockTag(String tag, String tagContent) {
 
 		if (TAG_LINK.equals(tag) || TAG_LINKPLAIN.equals(tag)) {
 
 			char[] contentChars = tagContent.toCharArray();
 			boolean inParentheses = false;
 			int labelStart = 0;
 
 			for (int i = 0; i < contentChars.length; i++) {
 				char nextChar = contentChars[i];
 
 				// tagContent always has a leading space
 				if (i == 0 && Character.isWhitespace(nextChar)) {
 					labelStart = 1;
 					continue;
 				}
 
 				if (nextChar == '(') {
 					inParentheses = true;
 					continue;
 				}
 
 				if (nextChar == ')') {
 					inParentheses = false;
 					continue;
 				}
 
 				// Stop at first whitespace that is not in parentheses
 				if (!inParentheses && Character.isWhitespace(nextChar)) {
 					labelStart = i + 1;
 					break;
 				}
 			}
 			if (TAG_LINK.equals(tag))
 				return "<code>" + substituteQualification(tagContent.substring(labelStart)) + "</code>"; //$NON-NLS-1$//$NON-NLS-2$
 			else
 				return substituteQualification(tagContent.substring(labelStart));
 
 		} else if (TAG_LITERAL.equals(tag)) {
 			return printLiteral(tagContent);
 
 		} else if (TAG_CODE.equals(tag)) {
 			return "<code>" + printLiteral(tagContent) + "</code>"; //$NON-NLS-1$//$NON-NLS-2$
 		}
 
 		// If something went wrong at least replace the {} with the content
 		return substituteQualification(tagContent);
 	}
 
 	private String printLiteral(String tagContent) {
 		int contentStart = 0;
 		for (int i = 0; i < tagContent.length(); i++) {
 			if (!Character.isWhitespace(tagContent.charAt(i))) {
 				contentStart = i;
 				break;
 			}
 		}
 		return TextUtils.escapeHTML(tagContent.substring(contentStart));
 	}
 
 	/*
 	 * A '{' has been read. Process a block tag
 	 */
 	private String processBlockTag() throws IOException {
 
 		int c = nextChar();
 
 		if (c != '@') {
 			StringBuffer buffer = new StringBuffer();
 			buffer.append('{');
 			buffer.append((char) c);
 			return buffer.toString();
 		}
 
 		StringBuffer buffer = new StringBuffer();
 		if (c != -1) {
 
 			buffer.setLength(0);
 			buffer.append((char) c);
 
 			c = getTag(buffer);
 			String tag = buffer.toString();
 
 			buffer.setLength(0);
 			if (c != -1 && c != '}') {
 				buffer.append((char) c);
 				c = getContent(buffer, '}');
 			}
 
 			return printBlockTag(tag, buffer.toString());
 		}
 
 		return null;
 	}
 
 	/*
 	 * @see SubstitutionTextReaderr#computeSubstitution(int)
 	 */
 	protected String computeSubstitution(int c) throws IOException {
 		if (c == '@' && fWasWhiteSpace)
 			return processSimpleTag();
 
 		if (c == '{')
 			return processBlockTag();
 
 		return null;
 	}
 }
