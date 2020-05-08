 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.pagedesigner.jsp.core.internal.metadata;
 
 import org.eclipse.jst.jsf.common.metadata.internal.AbstractTagLibDomainContentModelMetaDataTranslator;
 import org.eclipse.jst.jsf.common.metadata.internal.IMetaDataModelMergeAssistant;
 import org.eclipse.jst.jsf.common.metadata.internal.IMetaDataSourceModelProvider;
 import org.eclipse.jst.jsf.common.metadata.internal.IMetaDataTranslator;
 import org.eclipse.wst.html.core.internal.contentmodel.JSPCMDocument;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
 
 /**
  * Translates the JSP CMDocument to standard metadata model entities and traits
  */
 public class JSPContentModelMetaDataTranslator extends AbstractTagLibDomainContentModelMetaDataTranslator implements IMetaDataTranslator {
 
 	public boolean canTranslate(IMetaDataSourceModelProvider modelProvider) {		
 		if (modelProvider != null &&
 				modelProvider.getSourceModel() instanceof JSPCMDocument)
 			return true;
 		return false;
 	}
 	
 	public void translate(final IMetaDataModelMergeAssistant assistant) {
 		setAssistant(assistant);
 		CMDocument doc = getSourceModel();
 		if (doc instanceof JSPCMDocument){
 			doTranslate(doc);			
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.AbstractTagLibDomainContentModelMetaDataTranslator#getURIDescription()
 	 */
 	protected String getURIDescription() {		
 		return "JSP Tags";
 	}
 
 	@Override
 	protected String getURIDisplayLabel() {		
 		return "JSP";
 	}
 
	@Override
	protected String getTagNodeName(CMElementDeclaration tag) {
		//strip "jsp:"
		String tagName = tag.getNodeName();
		if (tagName.indexOf(":")>0){
			tagName = tagName.substring(tagName.indexOf(":")+1);
		}
		return tagName;
	}

 }
