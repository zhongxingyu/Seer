 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.web.ui.action;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.ui.IMarkerResolution;
 import org.eclipse.ui.IMarkerResolutionGenerator2;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.ui.internal.reconcile.TemporaryAnnotation;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.quickfix.IQuickFixGenerator;
 import org.jboss.tools.common.refactoring.MarkerResolutionUtils;
 import org.jboss.tools.jst.web.kb.IKbProject;
 import org.jboss.tools.jst.web.kb.KbProjectFactory;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.internal.XmlContextImpl;
 import org.jboss.tools.jst.web.kb.internal.taglib.TLDLibrary;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 
 /**
  * Shows the Marker Resolutions for Unknown tag JSP Problem Marker
  * 
  * @author Daniel Azarov
  *
  */
 public class JSPProblemMarkerResolutionGenerator implements IMarkerResolutionGenerator2, IQuickFixGenerator {
 	
 	private static final String HTML_VALIDATOR_MARKER="org.eclipse.wst.html.core.validationMarker"; //$NON-NLS-1$
 	private static final String JSP_VALIDATOR_MARKER="org.eclipse.jst.jsp.core.validationMarker"; //$NON-NLS-1$
 	
 	private static final String UNKNOWN_TAG = "Unknown tag"; //$NON-NLS-1$
 	
 	public static HashMap<String, String> libs = new HashMap<String, String>();
 	static{
 		libs.put("s", "http://jboss.com/products/seam/taglib");  //$NON-NLS-1$//$NON-NLS-2$
 		libs.put("ui", "http://java.sun.com/jsf/facelets"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("f", "http://java.sun.com/jsf/core"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("h", "http://java.sun.com/jsf/html"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("rich", "http://richfaces.org/rich"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("a4j", "http://richfaces.org/a4j"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("a", "http://richfaces.org/a4j"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("c", "http://java.sun.com/jstl/core"); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	@Override
 	public IMarkerResolution[] getResolutions(IMarker marker) {
 		try{
 			return isOurCase(marker);
 		}catch(CoreException ex){
 			WebUiPlugin.getPluginLog().logError(ex);
 		}
 		return new IMarkerResolution[]{};
 	}
 	
 	private IJavaCompletionProposal[] isOurCase(Annotation annotation){
 		ArrayList<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
 		if(!(annotation instanceof TemporaryAnnotation)){
 			return new IJavaCompletionProposal[]{};
 		}
 		TemporaryAnnotation ta = (TemporaryAnnotation)annotation;
 		
 		String message = annotation.getText();
 		if(ta.getPosition() == null)
 			return new IJavaCompletionProposal[]{};
 		
 		final int start = ta.getPosition().getOffset();
 		
 		final int end = ta.getPosition().getOffset()+ta.getPosition().getLength();
 		
 		if(!message.startsWith(UNKNOWN_TAG))
 			return new IJavaCompletionProposal[]{};
 		
 		String prefix = getPrifix(message);
 		if(prefix == null)
 			return new IJavaCompletionProposal[]{};
 		
 		String tagName = getTagName(message);
 		if(tagName == null)
 			return new IJavaCompletionProposal[]{};
 		
 		IFile file = MarkerResolutionUtils.getFile();
 		if(file == null)
 			return new IJavaCompletionProposal[]{};
 		
 		ELContext context = PageContextFactory.createPageContext(file);
 		if(context instanceof XmlContextImpl){
 			 Map<String, List<INameSpace>> nameSpaces = ((XmlContextImpl) context).getNameSpaces(start);
 			 Iterator<List<INameSpace>> iterator = nameSpaces.values().iterator();
 			 while(iterator.hasNext()){
 				 List<INameSpace> list = iterator.next();
 				 for(INameSpace ns : list){
 					 if(prefix.equals(ns.getPrefix())){
 						 return new IJavaCompletionProposal[]{};
 					 }
 				 }
 			 }
 		}
 		
 		Object additionalInfo = ta.getAdditionalFixInfo();
 		if(additionalInfo instanceof IDocument){
 			IStructuredModel model = StructuredModelManager.getModelManager().getModelForRead((IStructuredDocument)additionalInfo);
 			IDOMDocument xmlDocument = (model instanceof IDOMModel) ? ((IDOMModel) model).getDocument() : null;
 			
 			IKbProject kbProject = KbProjectFactory.getKbProject(file.getProject(), true);
 			
			List<ITagLibrary> libraries = kbProject.getAllTagLibraries();
 			ArrayList<String> names = new ArrayList<String>();
 			boolean worked = false;
 			for(ITagLibrary l : libraries){
 				if(l instanceof TLDLibrary){
 					((TLDLibrary) l).createDefaultNameSpace();
 				}
 				INameSpace ns = l.getDefaultNameSpace();
 				if(ns != null && ns.getPrefix() != null && ns.getPrefix().equals(prefix)){
 					worked = true;
 					String uri = ns.getURI();
 					String resolutionName = getResolutionName(xmlDocument != null && xmlDocument.isXMLType(), true, prefix, uri);
 					if(resolutionName != null && !names.contains(resolutionName) && l.getComponent(tagName) != null){
 						proposals.add(new AddTLDMarkerResolution(resolutionName, start, end, uri, prefix));
 						names.add(resolutionName);
 					}
 				}
 			}
 			
 			if(proposals.size() == 0 && libs.containsKey(prefix) && !worked){
 				String uri = libs.get(prefix);
 				String resolutionName = getResolutionName(xmlDocument != null && xmlDocument.isXMLType(), true, prefix, uri);
 				if(resolutionName != null){
 					proposals.add(new AddTLDMarkerResolution(resolutionName, start, end, uri, prefix));
 				}
 			}
 		}
 		
 		return proposals.toArray(new IJavaCompletionProposal[]{});
 	}
 	
 	private String getResolutionName(boolean xml, boolean noXML, String prefix, String uri){
 		if(xml){
 			return "xmlns: "+prefix+" = \""+uri+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}else if(noXML){
 			return "<%@ taglib uri = \""+uri+"\" prefix=\""+prefix+"\" %>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 		return null;
 	}
 	
 	private IMarkerResolution[] isOurCase(IMarker marker) throws CoreException{
 		ArrayList<IMarkerResolution> resolutions = new ArrayList<IMarkerResolution>();
 		String message = (String)marker.getAttribute(IMarker.MESSAGE);
 		
 		Integer attribute =  ((Integer)marker.getAttribute(IMarker.CHAR_START));
 		if(attribute == null)
 			return new IMarkerResolution[]{};
 		final int start = attribute.intValue();
 		
 		attribute = ((Integer)marker.getAttribute(IMarker.CHAR_END));
 		if(attribute == null)
 			return new IMarkerResolution[]{};
 		final int end = attribute.intValue();
 		
 		if(!message.startsWith(UNKNOWN_TAG))
 			return new IMarkerResolution[]{};
 		
 		String prefix = getPrifix(message);
 		if(prefix == null)
 			return new IMarkerResolution[]{};
 		
 		String tagName = getTagName(message);
 		if(tagName == null)
 			return new IMarkerResolution[]{};
 		
 		IFile file = (IFile)marker.getResource();
 		
 		ELContext context = PageContextFactory.createPageContext(file);
 		if(context instanceof XmlContextImpl){
 			 Map<String, List<INameSpace>> nameSpaces = ((XmlContextImpl) context).getNameSpaces(start);
 			 Iterator<List<INameSpace>> iterator = nameSpaces.values().iterator();
 			 while(iterator.hasNext()){
 				 List<INameSpace> list = iterator.next();
 				 for(INameSpace ns : list){
 					 if(prefix.equals(ns.getPrefix())){
 						 return new IMarkerResolution[]{};
 					 }
 				 }
 			 }
 		}
 		
 		IKbProject kbProject = KbProjectFactory.getKbProject(file.getProject(), true);
 		
		List<ITagLibrary> libraries = kbProject.getAllTagLibraries();
 		ArrayList<String> names = new ArrayList<String>();
 		boolean worked = false;
 		for(ITagLibrary l : libraries){
 			if(l instanceof TLDLibrary){
 				((TLDLibrary) l).createDefaultNameSpace();
 			}
 			INameSpace ns = l.getDefaultNameSpace();
 			if(ns != null && ns.getPrefix() != null && ns.getPrefix().equals(prefix)){
 				worked = true;
 				String uri = ns.getURI();
 				String resolutionName = getResolutionName(marker.getType().equals(HTML_VALIDATOR_MARKER) || marker.isSubtypeOf(HTML_VALIDATOR_MARKER), marker.getType().equals(JSP_VALIDATOR_MARKER) || marker.isSubtypeOf(JSP_VALIDATOR_MARKER), prefix, uri);
 				if(resolutionName != null && !names.contains(resolutionName) && l.getComponent(tagName) != null){
 					resolutions.add(new AddTLDMarkerResolution(file, resolutionName, start, end, uri, prefix));
 					names.add(resolutionName);
 				}
 			}
 		}
 		
 		if(resolutions.size() == 0 && libs.containsKey(prefix) && !worked){
 			String uri = libs.get(prefix);
 			String resolutionName = getResolutionName(marker.getType().equals(HTML_VALIDATOR_MARKER) || marker.isSubtypeOf(HTML_VALIDATOR_MARKER), marker.getType().equals(JSP_VALIDATOR_MARKER) || marker.isSubtypeOf(JSP_VALIDATOR_MARKER), prefix, uri);
 			if(resolutionName != null){
 				resolutions.add(new AddTLDMarkerResolution(file, resolutionName, start, end, uri, prefix));
 			}
 		}
 		return resolutions.toArray(new IMarkerResolution[]{});
 	}
 	
 	public static String getPrifix(String message){
 		String prefix=""; //$NON-NLS-1$
 		
 		int start = message.indexOf("("); //$NON-NLS-1$
 		if(start < 0)
 			return null;
 		
 		int end = message.indexOf(":", start); //$NON-NLS-1$
 		if(end < 0)
 			return null;
 		
 		prefix = message.substring(start+1, end);
 		
 		return prefix;
 	}
 
 	public static String getTagName(String message){
 		String tagName=""; //$NON-NLS-1$
 		
 		int start = message.indexOf(":"); //$NON-NLS-1$
 		if(start < 0)
 			return null;
 		
 		int end = message.indexOf(")", start); //$NON-NLS-1$
 		if(end < 0)
 			return null;
 		
 		tagName = message.substring(start+1, end);
 		
 		return tagName;
 	}
 
 	@Override
 	public boolean hasResolutions(IMarker marker) {
 		try{
 			String message = (String)marker.getAttribute(IMarker.MESSAGE);
 			return message.startsWith(UNKNOWN_TAG);
 		}catch(CoreException ex){
 			WebUiPlugin.getPluginLog().logError(ex);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean hasProposals(Annotation annotation) {
 		String message = annotation.getText();
 		return message.startsWith(UNKNOWN_TAG);
 	}
 
 	@Override
 	public IJavaCompletionProposal[] getProposals(Annotation annotation) {
 		return isOurCase(annotation); 
 	}
 }
