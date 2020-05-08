 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.outline;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.jboss.tools.common.model.project.IPromptingProvider;
 import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.common.model.util.ModelFeatureFactory;
 import org.jboss.tools.jst.jsp.contentassist.FaceletPageContectAssistProcessor;
 import org.jboss.tools.jst.jsp.contentassist.JspContentAssistProcessor;
 import org.jboss.tools.jst.jsp.drop.treeviewer.model.AttributeValueResource;
 import org.jboss.tools.jst.jsp.drop.treeviewer.model.AttributeValueResourceFactory;
 import org.jboss.tools.jst.jsp.drop.treeviewer.model.ModelElement;
 import org.jboss.tools.jst.jsp.drop.treeviewer.model.RootElement;
 import org.jboss.tools.jst.jsp.editor.IVisualContext;
 import org.jboss.tools.jst.jsp.editor.IVisualController;
 import org.jboss.tools.jst.jsp.editor.IVisualEditor;
 import org.jboss.tools.jst.jsp.jspeditor.JSPMultiPageEditor;
 import org.jboss.tools.jst.jsp.jspeditor.JSPTextEditor;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.internal.taglib.CustomProposalType;
 import org.jboss.tools.jst.web.kb.internal.taglib.CustomTagLibAttribute;
 import org.jboss.tools.jst.web.kb.internal.taglib.ExtendedProposalType;
 import org.jboss.tools.jst.web.kb.taglib.IAttribute;
 import org.jboss.tools.jst.web.tld.TaglibData;
 import org.jboss.tools.jst.web.tld.VpeTaglibManager;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 public class ValueHelper {
 	public static final String faceletUri = "http://java.sun.com/jsf/facelets"; //$NON-NLS-1$
 	public static final String JSFCAttributeName = "jsfc"; //$NON-NLS-1$
 	
 	private boolean isFacelets = false;
 	
 	public static IPromptingProvider seamPromptingProvider;
 	
 	static {
 		Object o = ModelFeatureFactory.getInstance().createFeatureInstance("org.jboss.tools.seam.internal.core.el.SeamPromptingProvider"); //$NON-NLS-1$
 		if(o instanceof IPromptingProvider) {
 			seamPromptingProvider = (IPromptingProvider)o;
 		}
 	}
 	 //JBIDE-1983, coused a memmory link
 //	IVisualContext iVisualContext = null;
 	private boolean isVisualContextInitialized = false;
 
 	public ValueHelper() {
 		boolean b = init();
 		if(!b) init2();
 	}
 	
 	public IVisualController getController() {
 		IEditorPart editor = ModelUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(!(editor instanceof JSPMultiPageEditor)) return null;
 		IVisualEditor v = ((JSPMultiPageEditor)editor).getVisualEditor();
 		if(v == null) return null;
 		return v.getController();
 	}
 
 	boolean init() {
 		if(isVisualContextInitialized) return true;
 		IEditorPart editor = ModelUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(!(editor instanceof JSPMultiPageEditor)) return false;
 		JSPTextEditor jspEditor = ((JSPMultiPageEditor)editor).getJspEditor();
 		
 		isVisualContextInitialized = true;
 		
 		if(getIVisualContext() != null) {
 			updateFacelets();
 		}
 		return getIVisualContext() != null;
 	}
 
 	private IVisualContext getIVisualContext(){
 		JSPTextEditor jspEditor = getJSPTextEditor();
 		return (jspEditor == null) ? null : jspEditor.getPageContext();
 	}
 
 	public JspContentAssistProcessor createContentAssistProcessor() {
 		return isFacetets() ? new FaceletPageContectAssistProcessor() : new JspContentAssistProcessor();
 	}
 
 	public IPageContext createPageContext(JspContentAssistProcessor processor, int offset) {
 		ISourceViewer sv = getSourceViewer();
 		if(sv == null) return null;		
         processor.createContext(sv, offset);
         return processor.getContext();
 	}
 
 	protected JSPTextEditor getJSPTextEditor() {
 		IEditorPart editor = ModelUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(!(editor instanceof JSPMultiPageEditor)) return null;
 		return ((JSPMultiPageEditor)editor).getJspEditor();		
 	}
 	
 	public ModelElement getInitalInput(IPageContext pageContext, KbQuery kbQuery) {
 		IAttribute[] as = PageProcessor.getInstance().getAttributes(kbQuery, pageContext);
 		if(as == null || as.length == 0) return new RootElement("root", new ArrayList<AttributeValueResource>()); //$NON-NLS-1$
         List<CustomProposalType> proposals = new ArrayList<CustomProposalType>();
         Set<String> proposalTypes = new HashSet<String>();
 		for (IAttribute a: as) {
 			if(a instanceof CustomTagLibAttribute) {
 				CustomTagLibAttribute ca = (CustomTagLibAttribute)a;
 				CustomProposalType[] ps = ca.getProposals();
 				for (CustomProposalType p: ps) {
 					String n = p.getType();
 					if(n == null || proposalTypes.contains(n)) continue;
 					proposalTypes.add(n);
 					proposals.add(p);
 				}
 			}
 		}
 		boolean hasJSFNature = false;
		IProject project = pageContext.getResource().getProject();
 		try {
 			if(project != null && project.isAccessible()) hasJSFNature = project.hasNature("org.jboss.tools.jsf.jsfnature");
 		} catch (CoreException e) {
 			//ignore
 		}
 		String[] TYPES = {AttributeValueResourceFactory.BEAN_PROPERTY_TYPE, 
 				AttributeValueResourceFactory.BUNDLE_PROPERTY_TYPE, 
 				AttributeValueResourceFactory.JSF_VARIABLES_TYPE, 
 				AttributeValueResourceFactory.BEAN_METHOD_BY_SYGNATURE_TYPE};
 		if(hasJSFNature) {
 			for (String type : TYPES) {
 				if(proposalTypes.contains(type)) continue;
 				ExtendedProposalType pt = new ExtendedProposalType();
 				pt.setType(type);
 				proposals.add(pt);
 			}
 		}
 		
 		List<AttributeValueResource> elements = new ArrayList<AttributeValueResource>();
 		ModelElement root = new RootElement("root", elements); //$NON-NLS-1$
 		for (CustomProposalType p: proposals) {
 			AttributeValueResource resource = AttributeValueResourceFactory.getInstance().createResource(getEditorInput(), pageContext, root, p, p.getType(), kbQuery);
 			resource.setParams(p.getParams());
 			resource.setQuery(kbQuery, this);
 			elements.add(resource);
 		}
 		if(seamPromptingProvider != null && getFile() != null) {
 			Properties p = new Properties();
 			p.put("file", getFile()); //$NON-NLS-1$
 			List list = seamPromptingProvider.getList(null, "seam.is_seam_project", null, p); //$NON-NLS-1$
 			if(list != null) {
 				AttributeValueResource resource = AttributeValueResourceFactory.getInstance().createResource(getEditorInput(), pageContext, root, null, "seamVariables", kbQuery); //$NON-NLS-1$
 				resource.setQuery(kbQuery, this);
 				elements.add(resource);
 			}
 		}
 		return root;
 	}
 
 	public boolean isAvailable(IPageContext pageContext, KbQuery kbQuery) {
 		RootElement root = (RootElement)getInitalInput(pageContext, kbQuery);
 		return (root != null && root.getChildren().length > 0);
 	}
 
 	public IEditorInput getEditorInput() {
 		IEditorPart editor = ModelUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		return editor.getEditorInput();
 	}
 	
 	public IFile getFile() {
 		if(!(getEditorInput() instanceof IFileEditorInput)) return null;
 		return ((IFileEditorInput)getEditorInput()).getFile();
 	}
 
 	public IProject getProject() {
 		if(!(getEditorInput() instanceof IFileEditorInput)) return null;
 		IFile file = ((IFileEditorInput)getEditorInput()).getFile();
 		return file == null ? null : file.getProject();
 	}
 
 	public VpeTaglibManager getTaglibManager() {
 		init();
 		
 		IVisualContext iVisualContext = getIVisualContext();
 		
 		if(iVisualContext!=null && iVisualContext instanceof VpeTaglibManager) {
 		
 			return (VpeTaglibManager)iVisualContext;
 		} else {
 			
 			return null;
 		}
 	}
 	
 	//Support of StructuredTextEditor
 	boolean init2() {
 		if(isVisualContextInitialized) return true;
 		IEditorPart editor = ModelUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(!(editor instanceof StructuredTextEditor)) return false;
 		StructuredTextEditor jspEditor = ((StructuredTextEditor)editor);
 		IDocument document = jspEditor.getDocumentProvider().getDocument(getEditorInput());
 		if(document == null) return false;
 //		installActivePropmtSupport(jspEditor, document);
 		return getIVisualContext() != null;
 	}
 
 	public boolean isFacetets() {
 		return isFacelets;
 	}
 
 	public void updateFacelets() {
 		VpeTaglibManager tldManager = getTaglibManager();
 		if(tldManager == null) return;
 		List<TaglibData> list = tldManager.getTagLibs();
 		if(list == null) return;
 		isFacelets = false;
 		for(int i = 0; i < list.size(); i++) {
 			TaglibData data = list.get(i);
 			isFacelets = isFacelets || (data != null &&  faceletUri.equals(data.getUri()));
 		}
 	}
 	
 	public String getFaceletJsfTag(Element element) {
 		if(!isFacelets) return null;
 		String name = element.getNodeName();
 		if(name.indexOf(':') >= 0) return null;
 		
 		NamedNodeMap attributes = element.getAttributes();
 		Node jsfC = attributes.getNamedItem(JSFCAttributeName);
 		if(jsfC != null && (jsfC instanceof Attr)) {
 			Attr jsfCAttribute = (Attr)jsfC;
 			String jsfTagName = jsfCAttribute.getValue();
 			if(jsfTagName != null && jsfTagName.indexOf(':') > 0) {
 				return jsfTagName;
 			}
 		}
 		return null;
 	}
 
 	public ISourceViewer getSourceViewer() {
 		JSPTextEditor jspEditor = getJSPTextEditor();
 		if(jspEditor != null) return jspEditor.getTextViewer();
 		IEditorPart editor = ModelUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if (editor == null) return null;
 		if(editor instanceof AbstractTextEditor) {
 			try {
 				Method m = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer", new Class[0]); //$NON-NLS-1$
 				m.setAccessible(true);
 				return (ISourceViewer)m.invoke(editor, new Object[0]);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 }
