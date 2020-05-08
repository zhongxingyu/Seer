 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.ti;
 
 import java.util.StringTokenizer;
 
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder;
 import org.eclipse.dltk.javascript.typeinfo.ITypeInfoContext;
 
 /**
  * Implements support for javadocs tags .
  * 
  * @see http://jsdoc.sourceforge.net/
  * @see http://code.google.com/p/jsdoc-toolkit/wiki/TagType
  * @see http://code.google.com/p/jsdoc-toolkit/wiki/TagParam
  */
 public class JSDocSupport implements IModelBuilder {
 
 	public void processMethod(ITypeInfoContext context,
 			FunctionStatement statement, IMethod method) {
 		if (statement.getDocumentation() == null) {
 			return;
 		}
 		final String comment = statement.getDocumentation().getText();
 		if (method.getType() == null) {
 			parseType(context, method, comment);
 		}
 		parseParams(context, method, comment);
 
 		parseDeprecation(method, comment);
 	}
 
 	private static final String DEPRECATED = "@deprecated";
 
 	private void parseDeprecation(IMethod method, String comment) {
 		int index = comment.indexOf(DEPRECATED);
 		if (index != -1) {
 			method.setDeprecated(true);
 		}
 	}
 
 	private static final String PARAM_TAG = "@param";
 
 	private void parseParams(ITypeInfoContext context, IMethod method,
 			String comment) {
 		int index = comment.indexOf(PARAM_TAG);
 		while (index != -1) {
 			int endLineIndex = comment.indexOf("\n", index);
 			if (endLineIndex == -1) {
 				endLineIndex = comment.length();
 			}
			String parameterString = comment.substring(index
					+ PARAM_TAG.length(), endLineIndex);
			StringTokenizer st = new StringTokenizer(parameterString);
 			String type = null;
 			while (st.hasMoreTokens()) {
 				final String token = st.nextToken();
 				if (token.startsWith("{") && token.endsWith("}")) {
 					type = token.substring(1, token.length() - 1);
				} else {
 					final IParameter parameter = method.getParameter(token);
 					if (parameter != null) {
						if (type != null && parameter.getType() == null)
 						parameter.setType(context.getType(type));
						if (parameterString.indexOf("optional") != -1) {
 							parameter.setOptional(true);
 						}
 					}
 					break;
 				}
 			}
 			index = comment.indexOf(PARAM_TAG, endLineIndex);
 		}
 	}
 
 	private static final String TYPE_TAG = "@type";
 
 	private void parseType(ITypeInfoContext context, IMethod method, String comment) {
 		int index = comment.indexOf(TYPE_TAG);
 		if (index != -1) {
 			int endLineIndex = comment.indexOf("\n", index);
 			if (endLineIndex == -1) {
 				endLineIndex = comment.length();
 			}
 			StringTokenizer st = new StringTokenizer(comment.substring(index
 					+ TYPE_TAG.length(), endLineIndex));
 			if (st.hasMoreTokens()) {
 				final String typeToken = st.nextToken();
 				method.setType(context.getType(typeToken));
 			}
 		}
 	}
 }
