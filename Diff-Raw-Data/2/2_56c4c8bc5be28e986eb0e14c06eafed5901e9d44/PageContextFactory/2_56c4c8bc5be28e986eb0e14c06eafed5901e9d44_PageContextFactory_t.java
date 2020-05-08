 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.internal.resources.ICoreConstants;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.content.IContentType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.internal.ui.text.FastJavaPartitionScanner;
 import org.eclipse.jdt.ui.text.IJavaPartitions;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.Token;
 import org.eclipse.jst.jsp.core.internal.contentmodel.TaglibController;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TaglibTracker;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.wst.common.componentcore.internal.ComponentResource;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.css.core.internal.provisional.adapters.IModelProvideAdapter;
 import org.eclipse.wst.css.core.internal.provisional.adapters.IStyleSheetAdapter;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSModel;
 import org.eclipse.wst.html.core.internal.htmlcss.LinkElementAdapter;
 import org.eclipse.wst.html.core.internal.htmlcss.URLModelProvider;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.jboss.tools.common.el.core.ELReference;
 import org.jboss.tools.common.el.core.GlobalELReferenceList;
 import org.jboss.tools.common.el.core.model.ELExpression;
 import org.jboss.tools.common.el.core.model.ELInstance;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.model.ELModel;
 import org.jboss.tools.common.el.core.parser.ELParser;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.parser.SyntaxError;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELContextImpl;
 import org.jboss.tools.common.el.core.resolver.ELResolverFactoryManager;
 import org.jboss.tools.common.el.core.resolver.ElVarSearcher;
 import org.jboss.tools.common.el.core.resolver.Var;
 import org.jboss.tools.common.resref.core.ResourceReference;
 import org.jboss.tools.common.text.ext.util.Utils;
 import org.jboss.tools.common.util.EclipseUIUtil;
 import org.jboss.tools.common.util.FileUtil;
 import org.jboss.tools.jst.web.kb.el.KbELReference;
 import org.jboss.tools.jst.web.kb.include.IncludeContextBuilder;
 import org.jboss.tools.jst.web.kb.internal.FaceletPageContextImpl;
 import org.jboss.tools.jst.web.kb.internal.JspContextImpl;
 import org.jboss.tools.jst.web.kb.internal.ResourceBundle;
 import org.jboss.tools.jst.web.kb.internal.XmlContextImpl;
 import org.jboss.tools.jst.web.kb.internal.taglib.NameSpace;
 import org.jboss.tools.jst.web.kb.taglib.CustomTagLibManager;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.jboss.tools.jst.web.kb.taglib.TagLibraryManager;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.css.CSSStyleSheet;
 
 /**
  * 
  * @author Alexey Kazakov
  */
 @SuppressWarnings("restriction")
 public class PageContextFactory implements IResourceChangeListener {
 	private static PageContextFactory fInstance;
 	private static final String XHTML_TAG_LIB_URI = "http://www.w3.org/1999/xhtml"; //$NON-NLS-1$
 	public static final String XML_PAGE_CONTEXT_TYPE = "XML_PAGE_CONTEXT_TYPE"; //$NON-NLS-1$
 	public static final String JSP_PAGE_CONTEXT_TYPE = "JSP_PAGE_CONTEXT_TYPE"; //$NON-NLS-1$
 	public static final String FACELETS_PAGE_CONTEXT_TYPE = "FACELETS_PAGE_CONTEXT_TYPE"; //$NON-NLS-1$
 	private static final String JAVA_PROPERTIES_CONTENT_TYPE = "org.eclipse.jdt.core.javaProperties"; //$NON-NLS-1$
 
 	public static final PageContextFactory getInstance() {
 		if (fInstance != null)
 			return fInstance;
 		return (fInstance = new PageContextFactory());
 	}
 
 	private PageContextFactory() {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		if (workspace != null) workspace.addResourceChangeListener(this);
 	}
 
 	/*
 	 * The cache to store the created contexts
 	 * The key is IFile.getFullPath().toString() of the resource of the context 
 	 */
 	private Map<IFile, ELContext> cache = new HashMap<IFile, ELContext>();
 
 	private ELContext getSavedContext(IFile resource) {
 		synchronized (cache) {
 			return cache.get(resource);
 		}
 	}
 
 	private void saveConvext(ELContext context) {
 		if (context != null && context.getResource() != null) {
 			synchronized (cache) {
 				cache.put(context.getResource(), context);
 			}
 		}
 	}
 
 	private ELContext removeSavedContext(IFile resource) {
 		ELContext removedContext = null;
 
 		synchronized (cache) {
 			removedContext = cache.remove(resource);
 		}
 		return removedContext;
 	}
 
 	/**
 	 * Creates a page context for the specified context file
 	 *
 	 * @param file
 	 * @param contentType
 	 * @return
 	 */
 	public static ELContext createPageContext(IFile file) {
 		return createPageContext(file, null);
 	}
 	/**
 	 * Creates a page context for the specified context type
 	 *
 	 * @param file
 	 * @param contentType
 	 * @return
 	 */
 	public static ELContext createPageContext(IFile file, String contextType) {
 		return getInstance().createPageContext(file, null, contextType);
 	}
 
 	/**
 	 * Cleans up the context for the file specified
 	 * 
 	 * @param file
 	 */
 	public void cleanUp(IFile file) {
 		if (file == null)
 			return;
 		synchronized (cache) {
 			ELContext removedContext = removeSavedContext(file);
 			if (removedContext == null || removedContext.getResource() == null)
 				return;
 
 			// Remove all the contexts that are parent to the removed context
 			ELContext[] contexts = cache.values().toArray(new ELContext[0]);
 			if (contexts != null) {
 				for (ELContext context : contexts) {
 					if (isDependencyContext(context, file)) {
 						removeSavedContext(file);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Cleans up the contexts for the project specified
 	 * 
 	 * @param file
 	 */
 	public void cleanUp(IProject project) {
 		if (project == null)
 			return;
 		synchronized (cache) {
 			// Remove all the contexts that are parent to the removed context
 			IFile[] files = cache.keySet().toArray(new IFile[0]);
 			if (files != null) {
 				for (IFile file : files) {
 					if (project.equals(file.getProject())) {
 						cleanUp(file);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Cleans up the contexts for the resource change delta
 	 * 
 	 * @param file
 	 */
 	public void cleanUp(IResourceDelta delta) {
 		synchronized (cache) {
 			if(cache.size() == 0) return;
 			if(!checkDelta(delta)) return;
 			processDelta(delta);
 		}
 	}
 
 	private ELContext createPropertiesContext(IFile file) {
 		ELContextImpl context = new ELContextImpl();
 		context.setResource(file);
 		context.setElResolvers(ELResolverFactoryManager.getInstance().getResolvers(file));
 		String content = FileUtil.getContentFromEditorOrFile(file);
 		int startEl = content.indexOf("#{"); //$NON-NLS-1$
 		if(startEl<0)
 			startEl = content.indexOf("${"); //$NON-NLS-1$
 		if(startEl>-1) {
 			ELParser parser = ELParserUtil.getJbossFactory().createParser();
 			ELModel model = parser.parse(content);
 			List<SyntaxError> errors = model.getSyntaxErrors();
 			for (ELInstance instance : model.getInstances()) {
 				for(ELInvocationExpression ie : instance.getExpression().getInvocations()){
 					ELReference elReference = new KbELReference();
 					elReference.setResource(file);
 					elReference.setEl(new ELExpression[]{ie});
 					elReference.setLength(ie.getLength());
 					elReference.setStartPosition(0);
 					List<SyntaxError> elErrors = new ArrayList<SyntaxError>();
 					for (SyntaxError error : errors) {
 						if(error.getPosition()>=ie.getStartPosition() && error.getPosition()<=ie.getEndPosition()) {
 							elErrors.add(error);
 						}
 					}
 					elReference.setSyntaxErrors(elErrors);
 					context.addELReference(elReference);
 				}
 			}
 		}
 		return context;
 	}
 
 	private ELContext createJavaContext(IFile file) {
 		ELContextImpl context = new ELContextImpl();
 		context.setResource(file);
 		context.setElResolvers(ELResolverFactoryManager.getInstance().getResolvers(file));
 		String content = FileUtil.getContentFromEditorOrFile(file);
 		FastJavaPartitionScanner scaner = new FastJavaPartitionScanner();
 		Document document = new Document(content);
 		scaner.setRange(document, 0, document.getLength());
 		IToken token = scaner.nextToken();
 		while(token!=null && token!=Token.EOF) {
 			if(IJavaPartitions.JAVA_STRING.equals(token.getData())) {
 				int length = scaner.getTokenLength();
 				int offset = scaner.getTokenOffset();
 				String value = null;
 				try {
 					value = document.get(offset, length);
 				} catch (BadLocationException e) {
 					WebKbPlugin.getDefault().logError(e);
 					return null;
 				}
 				if(value.indexOf('{')>-1) {
 					int startEl = value.indexOf("#{"); //$NON-NLS-1$
 					if(startEl==-1) {
 						startEl = value.indexOf("${"); //$NON-NLS-1$
 					}
 					if(startEl>-1) {
 						ELParser parser = ELParserUtil.getJbossFactory().createParser();
 						ELModel model = parser.parse(value);
 						List<ELInstance> is = model.getInstances();
 
 						ELReference elReference = new KbELReference();
 						elReference.setResource(file);
 						elReference.setEl(is);
 						elReference.setLength(value.length());
 						elReference.setStartPosition(offset);
 						elReference.setSyntaxErrors(model.getSyntaxErrors());
 						context.addELReference(elReference);
 					}
 				}
 			}
 			token = scaner.nextToken();
 		}
 
 		return context;
 	}
 
 //	long ctm = 0;
 
 	/**
 	 * Creates a page context for the specified context type
 	 *
 	 * @param file
 	 * @param contentType
 	 * @param parents List of parent contexts
 	 * @return
 	 */
 	private ELContext createPageContext(IFile file, List<String> parents, String defaultContextType) {
 		boolean isContextCachingAllowed = !EclipseUIUtil.isOpenInActiveEditor(file) && file != null;
 		ELContext context = isContextCachingAllowed ? getSavedContext(file) : null;
 		if (context != null) {
 			return context;
 		}
 		if (file == null) 
 			return createContextInstanceOfType(defaultContextType);
 		
 		IContentType type = IDE.getContentType(file);
 		String typeId = (type == null ? null : type.getId());
 		
 		if(JavaCore.JAVA_SOURCE_CONTENT_TYPE.equalsIgnoreCase(typeId)) {
 			context = createJavaContext(file);
 		} else if(JAVA_PROPERTIES_CONTENT_TYPE.equalsIgnoreCase(typeId)) {
 			context = createPropertiesContext(file);
 		} else {
 	//		ctm = System.currentTimeMillis();
 	//		System.out.println("Create Context : " + file.getFullPath().toString() + ", Totals: " + cache.size());
 			IModelManager manager = StructuredModelManager.getModelManager();
 			if(manager == null) {
 				// this may happen if plug-in org.eclipse.wst.sse.core 
 				// is stopping or un-installed, that is Eclipse is shutting down.
 				// there is no need to report it, just stop validation.
 				return context;
 			}
 			
 			IStructuredModel model = null;
 			try {
 				model = manager.getModelForRead(file);
 				if (model instanceof IDOMModel) {
 					IDOMModel domModel = (IDOMModel) model;
 					IDOMDocument document = domModel.getDocument();
 
 					context = defaultContextType == null ? 
 							createPageContextInstance(domModel.getContentTypeIdentifier()) :
 										createContextInstanceOfType(defaultContextType);
 					if (context == null)
 						return null;
 
 					context.setResource(file);
 					context.setElResolvers(ELResolverFactoryManager.getInstance().getResolvers(file));
 
 					if (context instanceof JspContextImpl && !(context instanceof FaceletPageContextImpl)) {
 						// Fill JSP namespaces defined in TLDCMDocumentManager 
 						fillJSPNameSpaces((JspContextImpl)context);
 					}
 
 					// The subsequently called functions may use the file and document
 					// already stored in context for their needs
 					fillContextForChildNodes(document, context, parents);
 				}
 			} catch (CoreException e) {
 				WebKbPlugin.getDefault().logError(e);
 	        } catch (IOException e) {
 				WebKbPlugin.getDefault().logError(e);
 			} finally {
 				if (model != null) {
 					model.releaseFromRead();
 				}
 			}
 		}
 
 		if (context != null && isContextCachingAllowed) {
 				saveConvext(context);
 		}
 
 		return context;
 	}
 
 	private ELContext createPageContextInstance(String contentType) {
 		String contextType = IncludeContextBuilder.getContextType(contentType);
 		return createContextInstanceOfType(contextType);
 	}
 
 	private ELContext createContextInstanceOfType(String contextType) {
 		if (JSP_PAGE_CONTEXT_TYPE.equals(contextType)) {
 			return new JspContextImpl();
 		} else if (FACELETS_PAGE_CONTEXT_TYPE.equals(contextType)) {
 			return new FaceletPageContextImpl();
 		}
 		return new XmlContextImpl();
 	}
 	/**
 	 * Sets up the context with namespaces and according libraries from the TagLibraryManager
 	 * 
 	 * @param node
 	 * @param context
 	 */
 	@SuppressWarnings({ "unchecked" })
 	private void fillJSPNameSpaces(JspContextImpl context) {
 		TLDCMDocumentManager manager = TaglibController.getTLDCMDocumentManager(context.getDocument());
 		List trackers = (manager == null? null : manager.getCMDocumentTrackers(context.getDocument().getLength() - 1));
 		for (int i = 0; trackers != null && i < trackers.size(); i++) {
 			TaglibTracker tt = (TaglibTracker)trackers.get(i);
 			final String prefix = tt.getPrefix() == null ? null : tt.getPrefix().trim();
 			final String uri = tt.getURI() == null ? null : tt.getURI().trim();
 			if (prefix != null && prefix.length() > 0 &&
 					uri != null && uri.length() > 0) {
 					
 				IRegion region = new Region(0, context.getDocument().getLength());
 				INameSpace nameSpace = new NameSpace(
 						uri, prefix,
 						TagLibraryManager.getLibraries(
 								context.getResource().getProject(), uri));
 				context.addNameSpace(region, nameSpace);
 			}
 		}
 	}
 
 	private void fillContextForChildNodes(IDOMNode parent, ELContext context, List<String> parents) {
 		NodeList children = parent.getChildNodes();
 		for(int i = 0; children != null && i < children.getLength(); i++) {
 			Node child = children.item(i);
 			if (child instanceof IDOMNode) {
 				fillContextForNode((IDOMNode)child, context, parents);
 				fillContextForChildNodes((IDOMNode)child, context, parents);
 			}
 		}
 	}
 
 	private void fillContextForNode(IDOMNode node, ELContext context, List<String> parents) {
 		if (!(context instanceof FaceletPageContextImpl) && !(node instanceof IDOMElement)) {
 			// There is no any useful info for JSP in text nodes
 			return;
 		}
 
 		if (context instanceof XmlContextImpl) {
 			XmlContextImpl xmlContext = (XmlContextImpl)context;
 			fillElReferencesForNode(node, xmlContext);
 			if (node instanceof IDOMElement) {
 				fillXMLNamespacesForNode((IDOMElement)node, xmlContext);
 			}
 		}
 
 		if ((context instanceof JspContextImpl || 
 				context instanceof FaceletPageContextImpl) &&
 				node instanceof IDOMElement) {
 			fillVarsForNode((IDOMElement)node, (ELContextImpl)context);
 		}
 
 		if (context instanceof FaceletPageContextImpl) {
 			// Insert here the initialization code for FaceletPage context elements which may exist in Text nodes
 		}
 
 		if (context instanceof JspContextImpl && node instanceof IDOMElement) {
 			fillResourceBundlesForNode((IDOMElement)node,  (JspContextImpl)context);
 		}
 
 		// There could be some context type to be initialized somehow that is different from JSP or FaceletPage context  
 		// Insert its on-node initialization code here
 
 		// The only Elements may have include/CSS Stylesheet links and other additional info
 		if (context instanceof IPageContext && node instanceof IDOMElement) {
 			fillAdditionalInfoForNode((IDOMElement)node, (IPageContext)context, parents);
 		}
 	}
 
 	private void fillVarsForNode (IDOMElement node, ELContextImpl context) {
 		Var var = ElVarSearcher.findVar(node, ELParserUtil.getJbossFactory());
 		if (var != null) {
 			int start = ((IndexedRegion) node).getStartOffset();
 			int length = ((IndexedRegion) node).getLength();
 
 			start = node.getStartOffset();
 			length = (node.hasEndTag() ? node
 					.getEndStructuredDocumentRegion()
 					.getEnd() : ((IDOMNode) node.getOwnerDocument()).getEndOffset() - 1 - start);
 
 			context.addVar(new Region(start, length), var);
 		}
 	}
 
 	private void fillElReferencesForNode(IDOMNode node, XmlContextImpl context) {
 		if(Node.ELEMENT_NODE == node.getNodeType() || Node.TEXT_NODE == node.getNodeType()) {
 			IStructuredDocumentRegion regionNode = node.getFirstStructuredDocumentRegion();
			if (regionNode == null)
				return;
 			ITextRegionList regions = regionNode.getRegions();
 			for(int i=0; i<regions.size(); i++) {
 				ITextRegion region = regions.get(i);
 				if(region.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE || region.getType() == DOMRegionContext.XML_CONTENT) {
 					String text = regionNode.getFullText(region);
 					if(text.indexOf("{")>-1) { //$NON-NLS-1$
 						int offset = regionNode.getStartOffset() + region.getStart();
 						int startEl = text.indexOf("#{"); //$NON-NLS-1$
 						if(startEl==-1) {
 							startEl = text.indexOf("${"); //$NON-NLS-1$
 						}
 						if(startEl>-1) {
 							ELParser parser = ELParserUtil.getJbossFactory().createParser();
 							ELModel model = parser.parse(text);
 							List<ELInstance> is = model.getInstances();
 
 							ELReference elReference = new KbELReference();
 							elReference.setResource(context.getResource());
 							elReference.setEl(is);
 							elReference.setLength(text.length());
 							elReference.setStartPosition(offset);
 
 							elReference.setSyntaxErrors(model.getSyntaxErrors());
 
 							context.addELReference(elReference);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void fillAdditionalInfoForNode(IDOMElement node, IPageContext context, List<String> parents) {
 		String prefix = node.getPrefix() == null ? "" : node.getPrefix(); //$NON-NLS-1$
 		String tagName = node.getLocalName();
 		Map<String, List<INameSpace>> nsMap = context.getNameSpaces(node.getStartOffset());
 		String[] uris = getUrisByPrefix(nsMap, prefix);
 
 		if (uris != null) {
 			for (String uri : uris) {
 				if (context instanceof IIncludedContextSupport) {
 					String[] includeAttributes = IncludeContextBuilder.getIncludeAttributes(uri, tagName);
 					if (includeAttributes != null) {
 						List<String> newParentList = parents == null ? new ArrayList<String>() : new ArrayList<String>(parents);
 						newParentList.add(context.getResource().getFullPath().toString());
 
 						for (String attr : includeAttributes) {
 							String fileName = node.getAttribute(attr);
 							if (fileName == null || fileName.trim().length() == 0)
 								continue;
 
 							IFile file = getFileFromProject(fileName, context.getResource());
 							if (file == null)
 								continue;
 
 							// Fix for JBIDE-5083 >>>
 							if (!checkCycling(parents, file))
 								continue;
 							// Fix for JBIDE-5083 <<<
 
 							ELContext includedContext = createPageContext(file, newParentList, null);
 							if (includedContext != null)
 								((IIncludedContextSupport)context).addIncludedContext(includedContext);
 						}
 					}
 				}
 				if (context instanceof ICSSContainerSupport) {
 					if(IncludeContextBuilder.isCSSStyleSheetContainer(uri, tagName)) {
 						fillCSSStyleSheetFromElement(node, (ICSSContainerSupport)context);
 					} else {
 						String[] cssAttributes = IncludeContextBuilder.getCSSStyleSheetAttributes(uri, tagName);
 						if (cssAttributes != null) {
 							for (String attr : cssAttributes) {
 								fillCSSStyleSheetFromAttribute(node, attr, (ICSSContainerSupport)context);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private boolean checkCycling(List<String> parents, IFile resource) {
 		String resourceId = resource.getFullPath().toString();
 		if (parents != null) {
 			for (String parentId : parents) {
 				if (resourceId.equals(parentId))
 					return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Sets up the context with namespaces and according libraries for the node
 	 * For the Facelet Context the methods adds an additional special namespace for
 	 * CustomTagLibManager.FACELETS_UI_TAG_LIB_URI when CustomTagLibManager.FACELETS_UI_TAG_LIB_URI 
 	 * is found in xmlns attributes
 	 * 
 	 * @param node
 	 * @param context
 	 */
 	private void fillXMLNamespacesForNode(Element node, XmlContextImpl context) {
 		NamedNodeMap attrs = node.getAttributes();
 		boolean mainNnIsRedefined = false;
 		for (int j = 0; attrs != null && j < attrs.getLength(); j++) {
 			Attr a = (Attr) attrs.item(j);
 			String name = a.getName();
 
 			if (!name.startsWith("xmlns:") && !name.equals("xmlns")) //$NON-NLS-1$ //$NON-NLS-2$
 				continue;
 
 			String prefix = name.startsWith("xmlns:") ? name.substring("xmlns:".length()) : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			String uri = a.getValue();
 
 			prefix = prefix == null ? null : prefix.trim();
 			uri = uri == null ? null : uri.trim();
 			if (XHTML_TAG_LIB_URI.equalsIgnoreCase(uri))
 				continue;
 
 			if (prefix != null // prefix may be empty
 					&& uri != null && uri.length() > 0) {
 
 				int start = ((IndexedRegion) node).getStartOffset();
 				int length = ((IndexedRegion) node).getLength();
 
 				IDOMElement domElement = (node instanceof IDOMElement ? (IDOMElement) node
 						: null);
 				if (domElement != null) {
 					start = domElement.getStartOffset();
 					length = (domElement.hasEndTag() ? domElement
 							.getEndStructuredDocumentRegion()
 							.getEnd() : ((IDOMNode) node.getOwnerDocument()).getEndOffset() - 1 - start);
 				}
 
 				Region region = new Region(start, length);
 				INameSpace nameSpace = new NameSpace(
 						uri, prefix,
 						TagLibraryManager.getLibraries(
 								context.getResource().getProject(), uri));
 
 				context.addNameSpace(region, nameSpace);
 				if (prefix.length() == 0)
 					mainNnIsRedefined = true;
 
 				if (context instanceof FaceletPageContextImpl && 
 						CustomTagLibManager.FACELETS_UI_TAG_LIB_URI.equals(uri) &&
 						!mainNnIsRedefined) {
 					nameSpace = new NameSpace(
 							CustomTagLibManager.FACELETS_HTML_TAG_LIB_URI, "", //$NON-NLS-1$
 							TagLibraryManager.getLibraries(
 									context.getResource().getProject(), 
 									CustomTagLibManager.FACELETS_HTML_TAG_LIB_URI));
 					context.addNameSpace(region, nameSpace);
 				}
 			}
 		}
 	}
 
 	private void fillResourceBundlesForNode(IDOMElement node, JspContextImpl context) {
 		String name = node.getNodeName();
 		if (name == null) return;
 		if (!name.endsWith("loadBundle")) return; //$NON-NLS-1$
 		if (name.indexOf(':') == -1) return;
 		String prefix = name.substring(0, name.indexOf(':'));
 
 		Map<String, List<INameSpace>> ns = context.getNameSpaces(node.getStartOffset());
 		if (!containsPrefix(ns, prefix)) return;
 
 		NamedNodeMap attributes = node.getAttributes();
 		if (attributes == null) return;
 		String basename = (attributes.getNamedItem("basename") == null ? null : attributes.getNamedItem("basename").getNodeValue()); //$NON-NLS-1$ //$NON-NLS-2$
 		String var = (attributes.getNamedItem("var") == null ? null : attributes.getNamedItem("var").getNodeValue()); //$NON-NLS-1$ //$NON-NLS-2$
 		if (basename == null || basename.length() == 0 ||
 			var == null || var.length() == 0) 
 			return;
 
 		context.addResourceBundle(new ResourceBundle(basename, var));
 	}
 
 	private void fillCSSStyleSheetFromAttribute(IDOMElement node,
 			String attribute, ICSSContainerSupport context) {
 		CSSStyleSheetDescriptor descr = getSheetForTagAttribute(node, attribute);
 		if (descr != null)
 			context.addCSSStyleSheetDescriptor(descr);
 	}
 
 	private void fillCSSStyleSheetFromElement(IDOMElement node,
 			ICSSContainerSupport context) {
 		CSSStyleSheet sheet = getSheetForTag(node);
 		if (sheet != null)
 			context.addCSSStyleSheetDescriptor(new CSSStyleSheetDescriptor(context.getResource().getFullPath().toString(), sheet));
 	}
 
 	public static class CSSStyleSheetDescriptor {
 		public CSSStyleSheet sheet;
 		public String source;
 
 		CSSStyleSheetDescriptor (String source, CSSStyleSheet sheet) {
 			this.source = source;
 			this.sheet = sheet;
 		}
 	}
 
 	/**
 	 * 
 	 * @param stylesContainer
 	 * @return
 	 */
 	private CSSStyleSheetDescriptor getSheetForTagAttribute(final Node stylesContainer, String attribute) {
 		INodeNotifier notifier = (INodeNotifier) stylesContainer;
 		CSSStyleSheet sheet = null;
 		String source = null;
 
 		synchronized (notifier) {
 			IStyleSheetAdapter adapter = (IStyleSheetAdapter) notifier.getAdapterFor(IStyleSheetAdapter.class);
 
 			if (!(adapter instanceof ExtendedLinkElementAdapter)) {
 				notifier.removeAdapter(adapter);
 				adapter = new ExtendedLinkElementAdapter(
 						(Element) stylesContainer, attribute);
 				notifier.addAdapter(adapter);
 			}
 		
 			if (adapter != null) {
 				sheet = (CSSStyleSheet) adapter.getSheet();
 				source = ((ExtendedLinkElementAdapter)adapter).getSource();
 			}
 			
 		}
 
 		return sheet == null || source == null ? null : new CSSStyleSheetDescriptor(source, sheet);
 	}
 
 	/**
 	 * 
 	 * @param stylesContainer
 	 * @return
 	 */
 	private CSSStyleSheet getSheetForTag(final Node stylesContainer) {
 		INodeNotifier notifier = (INodeNotifier) stylesContainer;
 		CSSStyleSheet sheet = null;
 
 		synchronized (notifier) {
 			IStyleSheetAdapter adapter = (IStyleSheetAdapter) notifier.getAdapterFor(IStyleSheetAdapter.class);
 
 			if (adapter != null) {
 				sheet = (CSSStyleSheet) adapter.getSheet();
 			}
 		}
 
 		return sheet;
 	}
 
 	private boolean containsPrefix(Map<String, List<INameSpace>> ns, String prefix) {
 		for (List<INameSpace> n: ns.values()) {
 			for (INameSpace nameSpace : n) {
 				if(prefix.equals(nameSpace.getPrefix())) return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Searches the namespace map and returns all the URIs for the specified prefix
 	 *  
 	 * @param nsMap
 	 * @param prefix
 	 * @return
 	 */
 	public static String[] getUrisByPrefix(Map<String, List<INameSpace>> nsMap, String prefix) {
 		if(nsMap == null || nsMap.isEmpty())
 			return null;
 		Set<String> uris = new HashSet<String>();
 		for (List<INameSpace> nsList : nsMap.values()) {
 			for (INameSpace ns : nsList) {
 				if (prefix.equals(ns.getPrefix())) {
 					uris.add(ns.getURI());
 				}
 			}
 		}
 
 		return uris.isEmpty() ? new String[] {prefix} : (String[])uris.toArray(new String[uris.size()]);
 	}
 
 	/**
 	 * Searches the file with the name specified
 	 * 
 	 * @param fileName
 	 * @param documentFile
 	 * @return
 	 */
 	public static IFile getFileFromProject(String fileName, IFile documentFile) {
 		if(documentFile == null || !documentFile.isAccessible()) return null;
 
 		fileName = findAndReplaceElVariable(fileName);
 
 		IProject project = documentFile.getProject();
 		String name = Utils.trimFilePath(fileName);
 		IPath currentPath = documentFile.getLocation()
 				.removeLastSegments(1);
 		IResource member = null;
 		StructureEdit se = StructureEdit.getStructureEditForRead(project);
 		if (se == null) {
 			return null;
 		}
 		WorkbenchComponent[] modules = se.getWorkbenchModules();
 		for (int i = 0; i < modules.length; i++) {
 			if (name.startsWith("/")) { //$NON-NLS-1$
 				member = findFileByAbsolutePath(project, modules[i], name);
 			} else {
 				member = findFileByRelativePath(project, modules[i],
 						currentPath, name);
 				if (member == null && name.length() > 0) {
 					// in some cases path having no leading "/" is
 					// nevertheless absolute
 					member = findFileByAbsolutePath(project, modules[i],
 							"/" + name); //$NON-NLS-1$
 				}
 			}
 			if (member != null && (member instanceof IFile)) {
 				if (((IFile) member).exists())
 					return (IFile) member;
 			}
 		}
 		return null;
 	}
 
 	private static IFile findFileByRelativePath(IProject project,
 			WorkbenchComponent module, IPath basePath, String path) {
 
 		if (path == null || path.trim().length() == 0)
 			return null;
 
 		path = findAndReplaceElVariable(path);
 
 		ComponentResource[] resources = module.findResourcesBySourcePath(
 				new Path("/"), 0); //$NON-NLS-1$
 		IPath projectPath = project.getLocation();
 		IFile member = null;
 
 		for (int i = 0; resources != null && i < resources.length; i++) {
 			IPath runtimePath = resources[i].getRuntimePath();
 			IPath sourcePath = resources[i].getSourcePath();
 
 			// Look in source environment
 			IPath webRootPath = projectPath.append(sourcePath);
 			IPath relativePath = Utils.getRelativePath(webRootPath,
 					basePath);
 			IPath filePath = relativePath.append(path);
 			member = project.getFolder(sourcePath).getFile(filePath);
 			if (member.exists()) {
 				return member;
 			}
 
 			// Look in runtime environment
 			if (runtimePath.segmentCount() >= ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH - 1) {
 				webRootPath = projectPath.append(runtimePath);
 				relativePath = Utils.getRelativePath(webRootPath, basePath);
 				filePath = relativePath.append(path);
 				member = project.getFolder(runtimePath).getFile(filePath);
 				if (member.exists()) {
 					return member;
 				}
 			}
 		}
 		return null;
 	}
 
 	private static IFile findFileByAbsolutePath(IProject project,
 		WorkbenchComponent module, String path) {
 		ComponentResource[] resources = module.findResourcesBySourcePath(
 				new Path("/"), 0); //$NON-NLS-1$
 
 		path = findAndReplaceElVariable(path);
 
 		IFile member = null;
 
 		for (int i = 0; resources != null && i < resources.length; i++) {
 			IPath runtimePath = resources[i].getRuntimePath();
 			IPath sourcePath = resources[i].getSourcePath();
 
 			// Look in source environment
 			member = project.getFolder(sourcePath).getFile(path);
 			if(member.exists()) {
 					return member;
 			} 
 
 			// Look in runtime environment
 			if (runtimePath.segmentCount() >= ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH - 1) {
 				member = project.getFolder(runtimePath).getFile(path);
 					if (member.exists()) {
 						return member;
 				}
 			}
 		}
 		return null;
 	}
 
 	private static final String DOLLAR_PREFIX = "${"; //$NON-NLS-1$
 
     private static final String SUFFIX = "}"; //$NON-NLS-1$
 
     private static final String SHARP_PREFIX = "#{"; //$NON-NLS-1$
 
 	public static final String CONTEXT_PATH_EXPRESSION = "^\\s*(\\#|\\$)\\{facesContext.externalContext.requestContextPath\\}"; //$NON-NLS-1$
 
 	// partly copied from org.jboss.tools.vpe.editor.util.ElService
 	private static String findAndReplaceElVariable(String fileName){
 		if (fileName != null)
 			fileName = fileName.replaceFirst(CONTEXT_PATH_EXPRESSION, ""); //$NON-NLS-1$
 
 		final IPath workspacePath = Platform.getLocation();
 
         final ResourceReference[] gResources = GlobalELReferenceList.getInstance().getAllResources(workspacePath);
 		String result = fileName;
 
 		ResourceReference[] sortedReferences = sortReferencesByScope(gResources);
 
 		for (ResourceReference rf : sortedReferences) {
 			final String dollarEl = DOLLAR_PREFIX + rf.getLocation() + SUFFIX;
 			final String sharpEl = SHARP_PREFIX + rf.getLocation() + SUFFIX;
 
 			if (fileName.contains(dollarEl)) {
 				result = result.replace(dollarEl, rf.getProperties());
 			}
 			if (fileName.contains(sharpEl)) {
 				result = result.replace(sharpEl, rf.getProperties());
 			}
 		}
 		return result;
 	}
 
 	// copied from org.jboss.tools.vpe.editor.util.ElService
 	private static ResourceReference[] sortReferencesByScope(ResourceReference[] references) {
 		ResourceReference[] sortedReferences = references.clone();
 
         Arrays.sort(sortedReferences, new Comparator<ResourceReference>() {
 			public int compare(ResourceReference r1, ResourceReference r2) {
 				return r1.getScope() - r2.getScope();
 			}
         });
 
 		return sortedReferences;
 	}
 
 	public static class ExtendedLinkElementAdapter extends LinkElementAdapter {
 
 		private Element element;
 		private String hrefAttrName;
 		private String source = null;
 
 		public ExtendedLinkElementAdapter(Element element, String hrefAttrName) {
 			this.element = element;
 			this.hrefAttrName = hrefAttrName;
 		}
 
 		@Override
 		public Element getElement() {
 			return element;
 		}
 
 		public String getSource() {
 			return source;
 		}
 
 		@Override
 		protected boolean isValidAttribute() {
 			if (super.isValidAttribute())
 				return true;
 
 			String href = getElement().getAttribute(hrefAttrName);
 			if (href == null || href.length() == 0)
 				return false;
 			return true;
 		}
 
 		/**
 		 */
 		public ICSSModel getModel() {
 			// Fix for JBIDE-5079 >>>
 			if (super.isValidAttribute()) {
 				source = getSourceFromAttribute("href"); //$NON-NLS-1$
 			} else if (isValidAttribute()) {
 				source = getSourceFromAttribute(hrefAttrName);
 			} else {
 				return null;
 			}
 			ICSSModel model = retrieveModel();
 			setModel(model);
 //			System.out.println("get CSS: " + source + " ==>> " + (model == null ? "FAILED" : " SUCCESSFULL"));
 			return model;
 		}
 
 		private String getSourceFromAttribute(String hrefAttributeName) {
 			String hrefExtracted = findAndReplaceElVariable(element
 					.getAttribute(hrefAttrName));
 
 			return hrefExtracted;
 		}
 
 		/**
 		 */
 		private ICSSModel retrieveModel() {
 			if (!isValidAttribute() || source == null) {
 				return null;
 			}
 
 			// null,attr check is done in isValidAttribute()
 			IDOMModel baseModel = ((IDOMNode) element).getModel();
 			if (baseModel == null)
 				return null;
 			Object id = baseModel.getId();
 			if (!(id instanceof String))
 				return null;
 			// String base = (String)id;
 
 			// get ModelProvideAdapter
 			IModelProvideAdapter adapter = (IModelProvideAdapter) ((INodeNotifier) getElement())
 					.getAdapterFor(IModelProvideAdapter.class);
 
 			URLModelProvider provider = new URLModelProvider();
 			try {
 				IStructuredModel newModel = provider.getModelForRead(baseModel,
 						source);
 				if (newModel == null)
 					return null;
 				if (!(newModel instanceof ICSSModel)) {
 					newModel.releaseFromRead();
 					return null;
 				}
 
 				// notify adapter
 				if (adapter != null)
 					adapter.modelProvided(newModel);
 
 				return (ICSSModel) newModel;
 			} catch (UnsupportedEncodingException e) {
 				WebKbPlugin.getDefault().logError(e);
 			} catch (IOException e) {
 				WebKbPlugin.getDefault().logError(e);
 			}
 
 			return null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 		if(event == null || event.getDelta() == null) return;
 		cleanUp(event.getDelta());
 	}
 
 	private boolean checkDelta(IResourceDelta delta) {
 		IResource resource = delta.getResource();
 		if(resource == null) return false;
 		if(resource instanceof IWorkspaceRoot) {
 			IResourceDelta[] d = delta.getAffectedChildren();
 			return (d.length > 0 && checkDelta(d[0]));
 		}
 		return true;
 	}
 
 	private void processDelta(IResourceDelta delta) {
 		if(delta == null) return;
 		int kind = delta.getKind();
 		IResource resource = delta.getResource();
 
 		if(resource instanceof IProject &&
 				kind == IResourceDelta.REMOVED) {
 			cleanUp((IProject)resource);
 		} else if (resource instanceof IFile && (
 			kind == IResourceDelta.CHANGED || 
 			kind == IResourceDelta.ADDED ||
 			kind == IResourceDelta.REMOVED ||
 			kind == IResourceDelta.CONTENT)) {
 			cleanUp((IFile)resource);
 		}
 
 		IResourceDelta[] cs = delta.getAffectedChildren();
 		for (int i = 0; cs != null && i < cs.length; i++) {
 			processDelta(cs[i]);
 		}
 		return;
 	}
 
 	private boolean isDependencyContext(ELContext context, IFile resource) {
 		if (resource.equals(context.getResource())) {
 			return true;
 		}
 
 		if(context instanceof IIncludedContextSupport) {
 			List<ELContext> includedContexts = ((IIncludedContextSupport)context).getIncludedContexts();
 			if (includedContexts != null) {
 				for (ELContext includedContext : includedContexts) {
 					if (isDependencyContext(includedContext, resource))
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		if (workspace != null) workspace.removeResourceChangeListener(this);
 
 		synchronized (cache) {
 			// Remove all the contexts that are parent to the removed context
 			ELContext[] contexts = cache.values().toArray(new ELContext[0]);
 			if (contexts != null) {
 				for (ELContext context : contexts) {
 					removeSavedContext(context.getResource());
 				}
 			}
 		}
 	}
 }
