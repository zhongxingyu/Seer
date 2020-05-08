 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.mixin;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.ISourceModuleInfoCache;
 import org.eclipse.dltk.core.mixin.IMixinParser;
 import org.eclipse.dltk.core.mixin.IMixinRequestor;
 import org.eclipse.dltk.core.search.SearchDocument;
 import org.eclipse.dltk.core.search.indexing.AbstractIndexer;
 
 public class MixinIndexer extends AbstractIndexer {
 	char[] source;
 	MixinIndexRequestor requestor = new MixinIndexRequestor();
 	ISourceModuleInfoCache.ISourceModuleInfo info;
 	public MixinIndexer(SearchDocument document, char[] source, ISourceModuleInfoCache.ISourceModuleInfo info ) {
 		super(document);
 		this.source = source;
 		this.info = info;
 	}
 	public void indexDocument() {
 		IDLTKLanguageToolkit toolkit = this.document.getToolkit();
 		if (toolkit == null) {
 			toolkit = DLTKLanguageManager.findToolkit(new Path(this.document
 					.getPath()));
 		}
 		if( toolkit == null ) {
 			return;
 		}
 		try {
 			IMixinParser parser = MixinManager.getMixinParser(toolkit.getNatureId());
 			if( parser != null ) {
 				parser.setRequirestor(this.requestor);
 //				System.out.println("Mixins: indexing " + this.document.getPath());
 				parser.parserSourceModule(this.source, false, null, info);
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 	private class MixinIndexRequestor implements IMixinRequestor {
 		public void reportElement(ElementInfo info) {
 			if( info.key.length() > 0 ) {
 				MixinIndexer.this.addMixin(info.key.toCharArray());
 			}
 			else {
				//System.out.println("error");
 			}
 		}	
 	}
 }
