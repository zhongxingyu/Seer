 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.cml.vjetv.reporter.impl;
 
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticProblem;
 import org.eclipse.vjet.dsf.jstojava.cml.vjetv.util.FileOperator;
 
 /**
  * This reporter used by build system.
  * 
  * @author <a href="mailto:liama@ebay.com">liama</a>
  * @since JDK 1.5
  */
 public class BuildReporter extends BaseReporter {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.vjet.dsf.jstojava.cml.vjetv.reporter.impl.BaseReporter#
 	 * printProblem(java.lang.StringBuffer,
 	 * org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticProblem)
 	 */
 	@Override
 	protected void printProblem(StringBuffer message,
 			VjoSemanticProblem vjoSemanticProblem, String sources) {
 		StringBuffer sb = new StringBuffer(vjoSemanticProblem.type() + ":Line:"
 				+ vjoSemanticProblem.getSourceLineNumber() + ": "
 				+ vjoSemanticProblem.getMessage() + "; Code: ");
 		message.append(sb.toString());
 		final int point = sb.toString().length();
 		int start = vjoSemanticProblem.getSourceStart();
 		int end = vjoSemanticProblem.getSourceEnd();
 
 		if (start < sources.length() && start>-1) {
 
 			int newStart = FileOperator.getNewStringPosition(sources, start);
 			String line = FileOperator.getSourceLineFromFile(sources, start,
 					end + 1);
 			String trimLine = line.trim();
 			message.append(trimLine + "\n");
 			for (int i = 0; i < point + start - newStart
 					- (line.length() - trimLine.length()) + 1; i++) {
 				message.append(" ");
 			}
 			message.append("^\n");
 		}else{
			message.append("No message available\n");
 		}
 
 		// message.append("=====================================" +
 		// "============================================" +
 		// "============================================\n");
 	}
 }
