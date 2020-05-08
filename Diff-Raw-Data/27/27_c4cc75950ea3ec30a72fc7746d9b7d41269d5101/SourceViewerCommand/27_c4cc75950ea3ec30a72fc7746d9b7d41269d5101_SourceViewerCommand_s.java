 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.commands;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.wst.html.core.internal.format.HTMLFormatProcessorImpl;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.eclipse.wst.sse.ui.internal.provisional.extensions.ISourceEditingTextTools;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.ui.internal.provisional.IDOMSourceEditingTextTools;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * @author mengbo
  */
 public abstract class SourceViewerCommand extends Command {
	protected StructuredTextEditor _editor;
 
 	private Document _document;
 
 	private Logger _log = PDPlugin.getLogger(SourceViewerCommand.class);
 
 	/**
	 * 
 	 */
 	public SourceViewerCommand(String label, StructuredTextEditor editor) {
 		super();
 		_editor = editor;
 		IDOMSourceEditingTextTools tools = getSourceEditingTextTools();
 		_document = tools.getDOMDocument();
 	}
 
 	protected IDOMSourceEditingTextTools getSourceEditingTextTools() {
 		IDOMSourceEditingTextTools tools = (IDOMSourceEditingTextTools) _editor
 				.getAdapter(ISourceEditingTextTools.class);
 		return tools;
 	}
 
 	/**
 	 * preExecute and postExecute is a pair. () SHOULD NOT throw any exception,
 	 * if it throw any exception, it should catch itself and return false to
 	 * indicate not continue.
 	 */
 	protected final boolean preExecute() {
 		int position = 0;
 		int length = 0;
 		ISelection selection = _editor.getTextViewer().getSelection();
 		if (selection instanceof TextSelection) {
 			position = ((TextSelection) selection).getOffset();
 			length = ((TextSelection) selection).getLength();
 		}
 		getModel().beginRecording(this, getLabel(), position, length);
 		getModel().aboutToChangeModel();
 		return true;
 	}
 
 	/**
 	 * if preExecute() return true, then this method will always be called even
 	 * preExecute()/doExecute() and postExecute() fail.
 	 */
 	protected final void postExecute() {
 		getModel().changedModel();
 		getModel().endRecording(this);
 		setSelection();
 	}
 
 	/**
 	 * format the specified node in source code. Utility method that can be
 	 * called by child classes
 	 * 
 	 * @param node
 	 */
 	public final void formatNode(Node node) {
 		new HTMLFormatProcessorImpl().formatNode(node);
 	}
 
 	protected IDOMModel getModel() {
 		Assert.isTrue(_document != null && _document instanceof IDOMNode);
 		return ((IDOMNode) _document).getModel();
 	}
 
 	public final void execute() {
 		boolean ok = preExecute();
 		if (ok) {
 			try {
 				doExecute();
 			} catch (Exception ex) {
 				// "Error in command execution"
 				_log.error("Error.SourceViewerCommand.Execution", ex); //$NON-NLS-1$
 			} finally {
 				postExecute();
 			}
 		}
 	}
 
 	public abstract void doExecute();
 
 	public abstract void setSelection();
 }
