 /*
 * Copyright (c) 2005, 2009 Sven Efftinge and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sven Efftinge - Initial API and implementation
  *     Artem Tikhomirov - LPG lexer/parser and error reporting
  */
 package org.eclipse.gmf.internal.xpand.util;
 
 import java.io.IOException;
 import java.io.Reader;
 
 import org.eclipse.gmf.internal.xpand.ast.Template;
 import org.eclipse.gmf.internal.xpand.model.XpandResource;
 import org.eclipse.gmf.internal.xpand.parser.XpandLexer;
 import org.eclipse.gmf.internal.xpand.parser.XpandParser;
 import org.eclipse.gmf.internal.xpand.util.ParserException.ErrorLocationInfo;
 
 public class XpandResourceParser {
 
 	// XXX everything except exact lexer and parser instances are the same as n XtendResourceParser
 	public XpandResource parse(final Reader source, final String qualifiedTemplateName) throws IOException, ParserException {
 		Template tpl = null;
 		XpandParser parser = null;
 		XpandLexer scanner = null;
 		final char[] buffer = new StreamConverter().toCharArray(source);
		if (buffer.length > 0 && buffer[0] == '\uFEFF') {
			System.arraycopy(buffer, 1, buffer, 0, buffer.length-1);
		}
 		try {
 			scanner = new XpandLexer(buffer, qualifiedTemplateName);
 			parser = new XpandParser(scanner);
 			scanner.lexer(parser);
 			tpl = parser.parser();
 			// FIXME handle errors if find out how to force generated parser to throw exception instead of consuming it
 		} catch (final Exception e) {
 			ErrorLocationInfo[] errors = extractErrors(scanner, parser);
         	if (errors.length == 0) {
         		throw new IOException("Unexpected exception while parsing:" + e.toString());
         	} else {
         		throw new ParserException(qualifiedTemplateName, errors);
         	}
 		}
 		ErrorLocationInfo[] errors = extractErrors(scanner, parser);
 		if (errors.length > 0) {
 			// TODO: instead of throwing an exception all errors should be added
 			// into the parsed template and processed later. This will allow us
 			// executing partially parsed template.
 			throw new ParserException(qualifiedTemplateName, errors);	
 		}
 		if (tpl != null) {
 			// XXX two choices here - 
 			// (1) pass any name into parse method, do not assume it's fqn and move setFQN outside of this method
 			// (2) assume fqn is passed into parse() as it's now.
 			tpl.setFullyQualifiedName(qualifiedTemplateName);
 			return tpl;
 		}
 		assert false : "no reason not to get template";
 		throw new ParserException(qualifiedTemplateName, errors);
 	}
 
 	// FIXME do it in the parser itself, though keeping errors separate may help
 	// those willing to report them separately
 	private static ErrorLocationInfo[] extractErrors(XpandLexer scanner, XpandParser parser) {
 		ErrorLocationInfo[] e1 = scanner.getErrors();
 		ErrorLocationInfo[] e2 = parser.getErrors();
 		ErrorLocationInfo[] res = new ErrorLocationInfo[e1.length + e2.length];
 		System.arraycopy(e1, 0, res, 0, e1.length);
 		System.arraycopy(e2, 0, res, e1.length, e2.length);
 		return res;
 	}
 }
