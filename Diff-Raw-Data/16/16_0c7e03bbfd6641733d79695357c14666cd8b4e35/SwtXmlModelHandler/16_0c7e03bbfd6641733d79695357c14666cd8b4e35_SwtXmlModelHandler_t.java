 /*******************************************************************************
  * Copyright (c) 2008 Ralf Ebert
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Ralf Ebert - initial API and implementation
  *******************************************************************************/
 package com.swtxml.ide;
 
import java.util.ArrayList;
import java.util.List;

 import org.eclipse.wst.sse.core.internal.document.IDocumentCharsetDetector;
 import org.eclipse.wst.sse.core.internal.document.IDocumentLoader;
 import org.eclipse.wst.sse.core.internal.ltk.modelhandler.AbstractModelHandler;
 import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IModelHandler;
 import org.eclipse.wst.sse.core.internal.provisional.IModelLoader;
 import org.eclipse.wst.xml.core.internal.encoding.XMLDocumentCharsetDetector;
 import org.eclipse.wst.xml.core.internal.encoding.XMLDocumentLoader;
 import org.eclipse.wst.xml.core.internal.modelhandler.XMLModelLoader;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeAdapterFactory;
 
 @SuppressWarnings("restriction")
 public class SwtXmlModelHandler extends AbstractModelHandler implements IModelHandler {
 
 	private static String modelHandlerID = "com.swtxml.ide.SwtXmlModelHandler";
 
 	public static String associatedContentTypeID = Activator.PLUGIN_ID + ".swtxml";
 
 	public SwtXmlModelHandler() {
 		setId(modelHandlerID);
 		setAssociatedContentTypeId(associatedContentTypeID);
 	}
 
 	@Override
 	public IDocumentCharsetDetector getEncodingDetector() {
 		return new XMLDocumentCharsetDetector();
 	}
 
 	public IDocumentLoader getDocumentLoader() {
 		return new XMLDocumentLoader();
 	}
 
 	public IModelLoader getModelLoader() {
		return new XMLModelLoader() {
			@SuppressWarnings("unchecked")
			@Override
			public List getAdapterFactories() {
				ArrayList adapterFactories = new ArrayList(super.getAdapterFactories());
				adapterFactories.add(new JFaceNodeAdapterFactory());
				return adapterFactories;
			}
		};
 	}
 }
