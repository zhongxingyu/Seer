 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.html.core.tests.parser;
 
 import org.eclipse.wst.sse.core.events.RegionChangedEvent;
 import org.eclipse.wst.sse.core.events.StructuredDocumentEvent;
 import org.eclipse.wst.sse.core.text.IStructuredDocument;
 import org.eclipse.wst.xml.core.document.XMLModel;
 import org.eclipse.wst.xml.core.internal.document.XMLModelImpl;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 public class UpdaterTestRegionChanged extends ModelTest {
 	/**
 	 * Constructor for UpdaterTestRegionChanged.
 	 * 
 	 * @param name
 	 */
 	public UpdaterTestRegionChanged(String name) {
 		super(name);
 	}
 
 	public UpdaterTestRegionChanged() {
 		super();
 	}
 
 	public static void main(java.lang.String[] args) {
 		new UpdaterTestRegionChanged().testModel();
 	}
 
 	public void testModel() {
		XMLModel model = createXMLModel();
 		try {
 			Document document = model.getDocument();
 			IStructuredDocument structuredDocument = model.getStructuredDocument();
 
 			structuredDocument.setText(this, "<a b= c></a>");
 
 			Node before = document.getFirstChild();
 
 			StructuredDocumentEvent fmEvent = structuredDocument.replaceText(null, 5, 1, "");
 			if (fmEvent instanceof RegionChangedEvent) {
 				outputWriter.writeln("ok: RegionChangedEvent");
 			}
 			else {
 				outputWriter.writeln("not ok: " + fmEvent.getClass().getName());
 			}
 
 			Node after = document.getFirstChild();
 
 			if (before != after) {
 				outputWriter.writeln("not ok: Node replaced");
 			}
 			else {
 				outputWriter.writeln("ok: Node not replaced");
 			}
 
 			saveAndCompareTestResults();
 		}
 		finally {
 			model.releaseFromEdit();
 		}
 
 
 	}
 }
