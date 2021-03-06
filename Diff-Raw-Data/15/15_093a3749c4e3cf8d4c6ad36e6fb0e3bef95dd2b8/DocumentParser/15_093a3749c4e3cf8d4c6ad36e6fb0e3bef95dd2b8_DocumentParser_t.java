 /*
  * Copyright (c) 2008 committers of openArchitectureWare and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    committers of openArchitectureWare - initial API and implementation
  */
 
 package org.eclipse.emf.mwe.ui.internal.editor.utils;
 
 import org.eclipse.emf.mwe.ui.internal.editor.editor.WorkflowEditor;
 import org.eclipse.emf.mwe.ui.internal.editor.elements.IWorkflowElement;
 import org.eclipse.emf.mwe.ui.internal.editor.marker.MarkerManager;
 import org.eclipse.emf.mwe.ui.internal.editor.parser.WorkflowContentHandler;
 import org.eclipse.emf.mwe.ui.internal.editor.parser.XMLParser;
 import org.eclipse.jface.text.IDocument;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.LocatorImpl;
 
 /**
  * @author Patrick Schoenbach - Initial API and implementation
 * @version $Revision: 1.11 $
  */
 
 public final class DocumentParser {
 
 	public static final String TAG_POSITIONS = "__tag_positions";
 
 	/**
 	 * Don't allow instantiation.
 	 */
 	private DocumentParser() {
 		throw new UnsupportedOperationException();
 	}
 
 	public static IWorkflowElement parse(final IDocument document) {
 		return parse(document, null);
 	}
 
 	public static IWorkflowElement parse(final IDocument document, final WorkflowEditor editor) {
		if (document == null || editor == null) // don't try to parse erroneous editors
 			return null;
 
 		final String text = document.get();
 		final XMLParser xmlParser = new XMLParser();
 		final WorkflowContentHandler contentHandler = new WorkflowContentHandler();
 		contentHandler.setDocument(document);
 		contentHandler.setPositionCategory(TAG_POSITIONS);
 		contentHandler.setDocumentLocator(new LocatorImpl());
 		xmlParser.setContentHandler(contentHandler);
 		try {
 			xmlParser.parse(text);
 		}
 		catch (final SAXException e) {
 			MarkerManager.createMarkerFromParserException(editor.getInputFile(), document, e);
 		}
 		final IWorkflowElement root = xmlParser.getRootElement();
 		return root;
 	}
 }
