 /*******************************************************************************
  * Copyright (c) 2011 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jsf.text.ext.hyperlink;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.ui.IEditorPart;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.filesystems.impl.FileAnyImpl;
 import org.jboss.tools.common.model.util.FindObjectHelper;
 import org.jboss.tools.common.text.ext.hyperlink.AbstractHyperlink;
 import org.jboss.tools.jsf.text.ext.JSFTextExtMessages;
 import org.jboss.tools.jst.web.kb.internal.taglib.AbstractComponent;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 
 public class TLDTagHyperlink extends AbstractHyperlink {
 	private AbstractComponent tag;
 	private XModelObject xmodelObject;
 	private String xmodelObjectName = null;
 	private IFile file = null;
 	private IRegion region;
 	
 	public TLDTagHyperlink(AbstractComponent tag, IRegion region){
 		this.tag = tag;
 		this.region = region;
 		
 		IFile file = getFile(tag);
 		
 		if(file != null && file.getFullPath() != null && file.getFullPath().toString().endsWith(".jar")) {
 			xmodelObject = getXModelObject(tag);
 			if(xmodelObject != null)
				xmodelObjectName = getFileName(xmodelObject);
 		}
 	}
 	
 	public static IFile getFile(AbstractComponent tag){
 		ITagLibrary tagLib = tag.getTagLib();
 		IResource r = tagLib.getResource();
 		if(r instanceof IFile)
 			return (IFile)r;
 		
 		return null;
 	}
 	
 	public static XModelObject getXModelObject(AbstractComponent tag){
 		Object id = tag.getId();
 		if(id instanceof XModelObject)
 			return (XModelObject)id;
 		
 		return null;
 	}
 	
 	public static String getFileName(XModelObject xmodelObject){
 		FileAnyImpl fai = null;
 		XModelObject f = xmodelObject;
 		while(f != null && f.getFileType() != XModelObject.FILE) f = f.getParent();
 		if(f instanceof FileAnyImpl) fai = (FileAnyImpl)f;
 		return FileAnyImpl.toFileName(fai);
 	}
 	
 	public AbstractComponent getComponent(){
 		return tag;
 	}
 	
 	protected IRegion doGetHyperlinkRegion(int offset) {
 		return region;
 	}
 
 	@Override
 	protected void doHyperlink(IRegion region) {
 		if(xmodelObjectName != null){
 			int q = FindObjectHelper.findModelObject(xmodelObject, FindObjectHelper.IN_EDITOR_ONLY);
 			if(q == 1) {
 				openFileFailed();
 			}
 		} else if(file != null){
 			IEditorPart part = null;
 			part = openFileInEditor(file);
 			if(part == null) {
 				openFileFailed();
 			}
 		}else
 			openFileFailed();
 	}
 
 	@Override
 	public String getHyperlinkText() {
 		if(xmodelObjectName != null)
 			return NLS.bind(JSFTextExtMessages.Open, xmodelObjectName);
 		else if(file != null)
 			return NLS.bind(JSFTextExtMessages.Open, file.getName());
 		else
 			return NLS.bind(JSFTextExtMessages.Open, "");
 	}
 
 }
