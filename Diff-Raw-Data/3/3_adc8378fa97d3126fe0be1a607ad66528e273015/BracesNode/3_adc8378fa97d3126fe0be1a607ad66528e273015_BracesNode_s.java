 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Vladimir Belov)
  *******************************************************************************/
 
 package org.eclipse.dltk.javascript.formatter.internal.nodes;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.formatter.IFormatterContext;
 import org.eclipse.dltk.formatter.IFormatterDocument;
 import org.eclipse.dltk.formatter.IFormatterNode;
 import org.eclipse.dltk.formatter.IFormatterWriter;
 
 public class BracesNode extends FormatterBlockWithBeginEndNode {
 
 	private IBracesConfiguration configuration;
 
 	public BracesNode(IFormatterDocument document,
 			IBracesConfiguration configuration) {
 		super(document);
 
 		Assert.isNotNull(configuration);
 
 		this.configuration = configuration;
 	}
 
 	@Override
 	public void accept(IFormatterContext context, IFormatterWriter visitor)
 			throws Exception {
 
 		context.setBlankLines(getBlankLinesBefore(context));
 
 		printBeforeOpenBrace(context, visitor);
 
 		if (configuration.isBracesIndenting())
 			context.incIndent();
 
 		// print "{"
 		if (getBegin() != null) {
 			IFormatterNode[] nodes = getBegin();
 			for (int i = 0; i < nodes.length; i++) {
 				nodes[i].accept(context, visitor);
 			}
 		}
 
 		printAfterOpenBrace(context, visitor);
 
 		// print body
 		acceptBody(context, visitor);
 
 		printBeforeCloseBrace(context, visitor);
 
 		// print "}"
 		if (getEnd() != null) {
 			visitor.write(context, getEnd().getStartOffset(), getEnd()
 					.getEndOffset());
 		}
 
 		printAfterCloseBrace(context, visitor);
 
 		if (configuration.isBracesIndenting())
 			context.decIndent();
 
 	}
 
 	private void printBeforeCloseBrace(IFormatterContext context,
 			IFormatterWriter visitor) throws Exception {
 
 		switch (configuration.insertBeforeCloseBrace()) {
 		case IBracesConfiguration.LINE_BREAK:
 			context.setBlankLines(-1);
 			visitor.writeLineBreak(context);
 			break;
 
 		case IBracesConfiguration.ONE_SPACE:
 			visitor.appendToPreviousLine(context, JSLiterals.EMPTY);
 			visitor.writeText(context, JSLiterals.SPACE);
 			visitor.skipNextLineBreaks(context);
 			break;
 		}
 	}
 
 	private void printAfterOpenBrace(IFormatterContext context,
 			IFormatterWriter visitor) throws Exception {
 
 		switch (configuration.insertAfterOpenBrace()) {
 
 		case IBracesConfiguration.LINE_BREAK:
 			context.setBlankLines(-1);
 			visitor.writeLineBreak(context);
 			break;
 
 		case IBracesConfiguration.ONE_SPACE:
 			visitor.appendToPreviousLine(context, JSLiterals.EMPTY);
 			visitor.writeText(context, JSLiterals.SPACE);
 			visitor.skipNextLineBreaks(context);
 			break;
 
 		default:
 			if (configuration.insertBeforeOpenBrace() != IBracesConfiguration.LINE_BREAK) {
 				visitor.appendToPreviousLine(context, JSLiterals.EMPTY);
 				visitor.writeText(context, JSLiterals.SPACE);
 				visitor.skipNextLineBreaks(context);
 			}
 			break;
 
 		}
 	}
 
 	private void printBeforeOpenBrace(IFormatterContext context,
 			IFormatterWriter visitor) throws Exception {
 
 		switch (configuration.insertBeforeOpenBrace()) {
 
 		case IBracesConfiguration.LINE_BREAK:
 			context.setBlankLines(-1);
 			visitor.writeLineBreak(context);
 			break;
 
 		case IBracesConfiguration.ONE_SPACE:
			visitor.appendToPreviousLine(context, JSLiterals.SPACE);
 			visitor.skipNextLineBreaks(context);
 			break;
 
 		}
 	}
 
 	private void printAfterCloseBrace(IFormatterContext context,
 			IFormatterWriter visitor) throws Exception {
 
 		switch (configuration.insertAfterCloseBrace()) {
 
 		case IBracesConfiguration.LINE_BREAK:
 			context.setBlankLines(-1);
 			visitor.writeLineBreak(context);
 			break;
 
 		case IBracesConfiguration.ONE_SPACE:
 			visitor.appendToPreviousLine(context, JSLiterals.SPACE);
 			visitor.skipNextLineBreaks(context);
 			break;
 
 		}
 	}
 
 	protected void acceptNodes(final List<IFormatterNode> nodes,
 			IFormatterContext context, IFormatterWriter visitor)
 			throws Exception {
 
 		if (isIndenting())
 			context.incIndent();
 
 		super.acceptNodes(nodes, context, visitor);
 
 		if (isIndenting())
 			context.decIndent();
 
 	}
 
 	protected boolean isIndenting() {
 		return configuration.isIndenting();
 	}
 
 }
