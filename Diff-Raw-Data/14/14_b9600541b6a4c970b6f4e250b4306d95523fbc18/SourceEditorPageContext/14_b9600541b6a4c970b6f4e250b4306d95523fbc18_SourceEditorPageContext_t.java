 /******************************************************************************* 
 
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
 package org.jboss.tools.jst.jsp.jspeditor;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.text.IDocument;
 import org.jboss.tools.jst.jsp.editor.IVisualContext;
 import org.jboss.tools.jst.jsp.util.XmlUtil;
 import org.jboss.tools.jst.web.tld.TaglibData;
 import org.jboss.tools.jst.web.tld.VpeTaglibListener;
 import org.jboss.tools.jst.web.tld.VpeTaglibManager;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * @author Max Areshkau (mareshkau@exadel.com)
  *
  * Class responsible to register TagLib data suitable for the current context 
  */
 public class SourceEditorPageContext implements IVisualContext,VpeTaglibManager {
 
 	/**
 	 * Contains information about taglibs on edited page
 	 */
 	private List<TaglibData> taglibs = null;
 	
 	private IDocument iDocument;
 	/**
 	 * references node
 	 */
 	private Node referenceNode = null;
 	/**
 	 * Reference page context 
 	 * @param pageContext
 	 */
 	private IVisualContext pageContext;
 	
 	public SourceEditorPageContext(JSPMultiPageEditor externalEditor) {
 		if(externalEditor != null && externalEditor.getVisualEditor() != null 
 				&& externalEditor.getVisualEditor().getController() != null) {			
 			setPageContext(externalEditor.getVisualEditor().getController().getPageContext());
 		}
 	}
 
 	public void clearAll() {		
 		setTaglibs(null);
 	}
 	
 	public void dispose() {
 		clearAll();
 		setReferenceNode(null);
 		setPageContext(null);
 		iDocument = null;
 	}
 
 	/**
 	 * Sets current node in scope of which we will be call context assistent
 	 * @param refNode
 	 */
 	public void setReferenceNode(Node refNode) {	
 		if (refNode == null || refNode.equals(getReferenceNode())) {
 			return;
 		}
 		referenceNode = refNode;
 		updateTagLibs();	
 	}
 
 	/**
 	 * This method will be called if we work with jsp pages
 	 * @param iDocument
 	 */
 	public void setDocument(IDocument iDocument, Node refNode) {
 		this.iDocument = iDocument;
 		if(refNode != null) {
 			referenceNode = null; //TODO study when we really need refresh
 			setReferenceNode(refNode);			
 		} else {
			//if we on jsp page we will set taglibs 
			//TODO Max Areshkau Find other possibility to check if we on jsp page
 			List<TaglibData> taglibs = getIncludeTaglibs();
			
			// FIX FOR https://jira.jboss.org/jira/browse/JBIDE-3888 
			// in some cases list of taglibs is not refreshed
			// if(taglibs != null && taglibs.size()>0) {
			if(taglibs != null) {
 				setTaglibs(taglibs);
 			}
 		}
 	}
 
 	public void collectRefNodeTagLibs() {
 		if(getReferenceNode() == null) {
 			return;
 		}
 			
 		if(getReferenceNode().getNodeType()==Node.DOCUMENT_NODE) {
 			NodeList nodes =getReferenceNode().getChildNodes();
 			
 			for(int i=0;i<nodes.getLength();i++) {
 				Node node =nodes.item(i);
 				List<TaglibData> result =XmlUtil.processNode(node, getIncludeTaglibs());
 				if(result!=null&&result.size()>0) {
 					setTaglibs(result);
 					break;
 				}
 			}
 		} else {
 			setTaglibs(XmlUtil.processNode(getReferenceNode(), getIncludeTaglibs()));
 		}
 	}
 	
 	public void updateTagLibs() {
 		collectRefNodeTagLibs();
 	}
 
 	// implements IVisualContext
 	public void refreshBundleValues() {
 		updateTagLibs();
 	}
 
 	// implements IVisualContext
 	public List<TaglibData> getTagLibs() {
 		List<TaglibData> clone = new ArrayList<TaglibData>();
 
 		Iterator<TaglibData> iter = getTaglibs() .iterator();
 		while (iter.hasNext()) {
 			TaglibData taglib = (TaglibData)iter.next();
 			//Max Areshkau we doesn't need double check
 //			if (!taglib.inList(clone)) {
 //					clone.add(taglib.clone());
 //			}
 			clone.add(taglib);
 		}
 
 		return clone;
 	}
 
 	// implements IVisualContext
 	public void addTaglibListener(VpeTaglibListener listener) {
 		//just a stub
 	}
 
 	// implements IVisualContext
 	public void removeTaglibListener(VpeTaglibListener listener) {
 		//just a stub
 	}
 
 	/**
 	 * @return the taglibs
 	 */
 	private List<TaglibData> getTaglibs() {
 		if(taglibs == null) {
 			taglibs = new ArrayList<TaglibData>();
 		}
 		return taglibs;
 	}
 
 	/**
 	 * @param taglibs the taglibs to set
 	 */
 	private void setTaglibs(List<TaglibData> taglibs) {
 		this.taglibs = taglibs;
 	}
 
 	/**
 	 * @return the referenceNode
 	 */
 	public Node getReferenceNode() {
 		return referenceNode;
 	}
 
 	/**
 	 * @return the pageContext
 	 */
 	private IVisualContext getPageContext() {
 		return pageContext;
 	}
 
 	/**
 	 * @param pageContext the pageContext to set
 	 */
 	private void setPageContext(IVisualContext pageContext) {
 		this.pageContext = pageContext;
 	}
 
 	public List<TaglibData> getIncludeTaglibs() {
 		List<TaglibData> result = null;
 		if(getPageContext() != null) {
 			result = getPageContext().getIncludeTaglibs();
 		} else {
 			result = new ArrayList<TaglibData>();
 		}
 		if(iDocument != null) {
 			result = XmlUtil.getTaglibsForJSPDocument(iDocument, result);
 		}
 		return result;
 	}
 		
 }
