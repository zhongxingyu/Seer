 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.formatter;
 
import java.text.MessageFormat;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.TypedPosition;
 import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
 import org.eclipse.jface.text.formatter.FormattingContextProperties;
 import org.eclipse.jface.text.formatter.IFormattingContext;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.text.edits.MalformedTreeException;
 import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
 
 /**
  * Formatting strategy for java source code.
  * 
  * @since 3.0
  */
 public class ScriptFormattingStrategy extends ContextBasedFormattingStrategy {
 
 	private final String natureId;
 
 	private static class FormatJob {
 		final IDocument document;
 		final TypedPosition partition;
 		final IProject project;
 		final String formatterId;
 
 		/**
 		 * @param document
 		 * @param partition
 		 * @param project
 		 */
 		public FormatJob(IDocument document, TypedPosition partition,
 				IProject project, String formatterId) {
 			this.document = document;
 			this.partition = partition;
 			this.project = project;
 			this.formatterId = formatterId;
 		}
 
 	}
 
 	/** Jobs to be formatted by this strategy */
 	private final LinkedList fJobs = new LinkedList();
 
 	/**
 	 * Creates a new java formatting strategy.
 	 */
 	public ScriptFormattingStrategy(String natureId) {
 		this.natureId = natureId;
 	}
 
 	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#format()
 	 */
 	public void format() {
 		super.format();
 
 		final FormatJob job = (FormatJob) fJobs.removeFirst();
 		final IDocument document = job.document;
 		final TypedPosition partition = job.partition;
 
 		if (document != null && partition != null) {
 			Map partitioners = null;
 			try {
 				int offset = partition.getOffset();
 
 				final IScriptFormatterFactory formatterFactory = selectFormatterFactory(job);
 				if (formatterFactory != null) {
 					final String lineDelimiter = TextUtilities
 							.getDefaultLineDelimiter(document);
 					final Map prefs = getPreferences();
 					final IScriptFormatter formatter = formatterFactory
 							.createFormatter(lineDelimiter, prefs);
 					final int indentationLevel = formatter
 							.detectIndentationLevel(document, offset);
 					final TextEdit edit = formatter.format(document.get(),
 							offset, partition.getLength(), indentationLevel);
 					if (edit != null) {
 						if (edit.getChildrenSize() > 20)
 							partitioners = TextUtilities
 									.removeDocumentPartitioners(document);
 						edit.apply(document);
 					}
 				}
 			} catch (FormatterSyntaxProblemException e) {
				WorkbenchWindow window = (WorkbenchWindow) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow();
				if (window != null && window.getStatusLineManager() != null) {
					window
							.getStatusLineManager()
							.setErrorMessage(
									MessageFormat
											.format(
													FormatterMessages.ScriptFormattingStrategy_unableToFormatSourceContainingSyntaxError,
													new Object[] { e
															.getMessage() }));
				}
				PlatformUI.getWorkbench().getDisplay().beep();
 			} catch (MalformedTreeException e) {
 				DLTKUIPlugin
 						.warn(
 								FormatterMessages.ScriptFormattingStrategy_formattingError,
 								e);
 			} catch (BadLocationException e) {
 				// Can only happen on concurrent document modification
 				DLTKUIPlugin
 						.warn(
 								FormatterMessages.ScriptFormattingStrategy_formattingError,
 								e);
 			} catch (Exception e) {
 				final String msg = NLS
 						.bind(
 								FormatterMessages.ScriptFormattingStrategy_unexpectedFormatterError,
 								e.toString());
 				DLTKUIPlugin.logErrorMessage(msg, e);
 			} finally {
 				if (partitioners != null)
 					TextUtilities.addDocumentPartitioners(document,
 							partitioners);
 			}
 		}
 	}
 
 	protected IScriptFormatterFactory selectFormatterFactory(FormatJob job) {
 		IScriptFormatterFactory factory = (IScriptFormatterFactory) ScriptFormatterManager
 				.getInstance().getContributionById(job.formatterId);
 		if (factory != null) {
 			return factory;
 		}
 		return ScriptFormatterManager.getSelected(natureId, job.project);
 	}
 
 	public void formatterStarts(final IFormattingContext context) {
 		super.formatterStarts(context);
 		final IDocument document = (IDocument) context
 				.getProperty(FormattingContextProperties.CONTEXT_MEDIUM);
 		final TypedPosition partition = (TypedPosition) context
 				.getProperty(FormattingContextProperties.CONTEXT_PARTITION);
 		final IProject project = (IProject) context
 				.getProperty(ScriptFormattingContextProperties.CONTEXT_PROJECT);
 		final String formatterId = (String) context
 				.getProperty(ScriptFormattingContextProperties.CONTEXT_FORMATTER_ID);
 		fJobs.addLast(new FormatJob(document, partition, project, formatterId));
 	}
 
 	public void formatterStops() {
 		super.formatterStops();
 		fJobs.clear();
 	}
 }
