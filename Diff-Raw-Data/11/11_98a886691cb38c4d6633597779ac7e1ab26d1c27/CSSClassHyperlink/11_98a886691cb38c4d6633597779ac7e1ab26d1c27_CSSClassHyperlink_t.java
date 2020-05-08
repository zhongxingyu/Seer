 /*******************************************************************************
  * Copyright (c) 2011-2012 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.text.ext.hyperlink;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.ui.internal.util.Sorter;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.text.ext.ExtensionsPlugin;
 import org.jboss.tools.common.text.ext.hyperlink.AbstractHyperlink;
 import org.jboss.tools.common.text.ext.hyperlink.xpl.Messages;
 import org.jboss.tools.common.text.ext.util.StructuredModelWrapper;
 import org.jboss.tools.common.text.ext.util.StructuredSelectionHelper;
 import org.jboss.tools.common.text.ext.util.Utils;
 import org.jboss.tools.jst.text.ext.JSTExtensionsPlugin;
 import org.jboss.tools.jst.web.kb.ICSSContainerSupport;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.PageContextFactory.CSSStyleSheetDescriptor;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.css.CSSMediaRule;
 import org.w3c.dom.css.CSSRule;
 import org.w3c.dom.css.CSSRuleList;
 import org.w3c.dom.css.CSSStyleRule;
 
 /**
  * @author Jeremy
  */
 @SuppressWarnings("restriction")
 public class CSSClassHyperlink extends AbstractHyperlink {
 	CSSRuleDescriptorSorter SORTER = new CSSRuleDescriptorSorter();
 
 	public static final String[] STYLE_TAGS = new String[] { "style", "link" }; //$NON-NLS-1$//$NON-NLS-2$
 	public static final String LINK_TAG = "link"; //$NON-NLS-1$
 	public static final String HREF_ATTRIBUTE = "href"; //$NON-NLS-1$
 	public static final String CONTEXT_PATH_EXPRESSION = "^\\s*(\\#|\\$)\\{facesContext.externalContext.requestContextPath\\}"; //$NON-NLS-1$
 	
 	@Override
 	protected void doHyperlink(IRegion region) {
 		ICSSContainerSupport cssContainerSupport = null;
 		ELContext context = PageContextFactory.createPageContext(getDocument());
 		if (!(context instanceof ICSSContainerSupport)) {
 			openFileFailed();
 			return;
 		}
 		cssContainerSupport = (ICSSContainerSupport)context;
 		List<CSSStyleSheetDescriptor> descrs = cssContainerSupport.getCSSStyleSheetDescriptors();
 
 		List<CSSRuleDescriptor> bestMatchDescriptors = new ArrayList<CSSRuleDescriptor>();
 		
 		for (int i = (descrs == null) ? -1 : descrs.size() - 1; descrs != null && i >= 0; i--) {
 			CSSStyleSheetDescriptor descr = descrs.get(i);
 			CSSRuleList rules = descr.sheet.getCssRules();
 			for (int r = 0; rules != null && r < rules.getLength(); r++) {
 				CSSRuleDescriptor match = getMatchedRuleDescriptor(rules.item(r), getStyleName(region));
 				if (match != null) {
 					match.stylesheetDescriptor = descr;
 					bestMatchDescriptors.add(match);
 				}
 			}
 		}
 		
 		CSSRuleDescriptor[] sorted = orderCSSRuleDescriptors(
 				bestMatchDescriptors.toArray(new CSSRuleDescriptor[bestMatchDescriptors.size()]));
 		
 		for (CSSRuleDescriptor d : sorted) {
 			CSSStyleSheetDescriptor descr = d.stylesheetDescriptor;
 			IFile file = findFileForCSSStyleSheet(descr.getFilePath());
 			if (file != null) {
 				int startOffset = 0;
 				if (descr.sheet.getOwnerNode() != null) {
 					Node node = descr.sheet.getOwnerNode().getFirstChild();
 					if (node instanceof IndexedRegion) {
 						startOffset = ((IndexedRegion)node).getStartOffset();
 					}
 				}
 				showRegion(
 						file, 
 						new Region(startOffset + ((IndexedRegion)d.rule).getStartOffset(), ((IndexedRegion)d.rule).getLength()));
 				return;
 			}
 		}
 
 		openFileFailed();
 	}
 
 	protected CSSRuleDescriptor[] orderCSSRuleDescriptors(CSSRuleDescriptor[] descriptors) {
 		Object[] sorted = SORTER.sort(descriptors);
 		CSSRuleDescriptor[] sortedDescriptors = new CSSRuleDescriptor[sorted.length];
 		System.arraycopy(sorted, 0, sortedDescriptors, 0, sorted.length);
 		return sortedDescriptors;
 	}
 	
 	class CSSRuleDescriptorSorter extends Sorter {
 		
 		public boolean compare(Object descr1, Object descr2) {
 			CSSAxis[] a1 = ((CSSRuleDescriptor)descr1).axis;
 			CSSAxis[] a2 = ((CSSRuleDescriptor)descr2).axis;
 
 
 			if (a1.length == a2.length) {
 				for (int i = a1.length - 1; i >= 0; i--) {
 					if (a1[i].className != CSSAxis.CSS_ANY && 
 							!a1[i].className.equalsIgnoreCase(a2[i].className))
 						return true;
 	
 					if (a1[i].attrName != CSSAxis.CSS_ANY && 
 							!a1[i].attrName.equalsIgnoreCase(a2[i].attrName))
 						return true;
 	
 					if (a1[i].nodeName != CSSAxis.CSS_ANY && 
 							!a1[i].nodeName.equalsIgnoreCase(a2[i].nodeName))
 						return true;
 				}
 	
 				return false;
 			}
 			return (a1.length > a2.length);
 		}
 	}
 	
 	/*
 	 * Finds a file representing the specified stylesheet
 	 * 
 	 * Three kinds of filePath values are tested:
 	 * - workspace related full path (Comes for a stylesheet defined in STYLE tag. Actually it is a path to the page where the STYLE tag is used)
 	 * - full file path to the CSS-file within the project
 	 * - relative file path
 	 * 
 	 * @param filePath
 	 * @return
 	 */
 	private IFile findFileForCSSStyleSheet(String filePath) {
 		// First try to find a file by WS-related path (because it's the most longest path)
 		IFile file = ResourcesPlugin.getWorkspace().getRoot()
 		.getFileForLocation(
 				ResourcesPlugin.getWorkspace().getRoot().
 					getLocation().append(filePath));
 		if (file != null)
 			return file;
 		
 		// Thought if project is imported as a link from some out-of-workspace location and the first segment of path is a project 
 		IPath path = new Path(filePath);
 		
 		if (path.isAbsolute()) {
 			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
 			for (int i = 0; i < projects.length; i++) {
 				IProject project = projects[i];
 				IPath projectLocation = project.getLocation();
 				if (projectLocation != null && projectLocation.lastSegment().equals(path.segment(0))) {
 					IPath projectRelatedPath = projectLocation.append(path.removeFirstSegments(1));
 					file = ResourcesPlugin.getWorkspace().getRoot()
 							.getFileForLocation(projectRelatedPath);
 					if (file != null)
 						return file;
 				}
 			}
 		}
 
 		return PageContextFactory.getFileFromProject(filePath, getFile());
 	}
 	
 	/**
 	 * 
 	 * @param cssRule
 	 * @param styleName
 	 * @return
 	 */
 	protected CSSRuleDescriptor getMatchedRuleDescriptor(CSSRule cssRule, String styleName) {
 		if (cssRule instanceof CSSMediaRule) {
 			CSSMediaRule cssMediaRule = (CSSMediaRule)cssRule;
 			CSSRuleList rules = cssMediaRule.getCssRules();
 			for (int i = 0; rules != null && i < rules.getLength(); i++) {
 				CSSRule rule = rules.item(i);
 				CSSRuleDescriptor descr = getMatchedRuleDescriptor(rule, styleName);
 				if (descr != null) {
 					return descr;
 				}
 			}
 			return null;
 		}
 
 		if (!(cssRule instanceof CSSStyleRule))
 			return null;
 		
 		// get selector text
 		String selectorText = ((CSSStyleRule) cssRule).getSelectorText();
 
 		if (selectorText != null) {
 			String styles[] = selectorText.trim().toLowerCase().split(","); //$NON-NLS-1$
 			for (String styleText : styles) {
 				String[] styleWords = styleText.trim().split(" ");  //$NON-NLS-1$
 				CSSAxis[] styleAxis = new CSSAxis[styleWords == null ? 0 : styleWords.length];
 				
 				if (styleWords != null) {
 					for (int i = styleWords.length - 1; i >= 0; i--) {
 						String word = styleWords[i];
 						styleAxis[i] = getCSSAxis(word);
 					}
 				}
 				
 				if (matchesRule(getHyperlinkRegion(), styleAxis)) {
 					return new CSSRuleDescriptor(cssRule, styleAxis);
 				}
 			}
 		}
 		return null;
 	}
 
 	boolean matchesRule(IRegion styleNameRegion, CSSAxis[] cssAxis) {
 		if (cssAxis == null || cssAxis.length == 0)
 			return false;
 		
 		String classNameFromTheRegion = getStyleName(styleNameRegion);
 		
 		StructuredModelWrapper smw = new StructuredModelWrapper();
 		try {
 			smw.init(getDocument());
 			Document xmlDocument = smw.getDocument();
 			if (xmlDocument == null) return false;
 			Node node = Utils.findNodeForOffset(xmlDocument, styleNameRegion.getOffset());
 			
 			if (node instanceof Attr) {
 				Attr attr = (Attr)node;
 				node = attr.getOwnerElement();
 			}
 			
 			for (int i = cssAxis.length - 1; i >= 0; i--) {
 				CSSAxis currentAxis = cssAxis[i];
 				
 				do {
 					boolean classFound = currentAxis.className == CSSAxis.CSS_ANY;
 					boolean nodeFound = currentAxis.nodeName == CSSAxis.CSS_ANY;
 					boolean attrFound = currentAxis.attrName == CSSAxis.CSS_ANY;
 
 					if (!classFound) {
 						// For the top node's class name we have to use word from the styleNameRegion 
 						String nodeClasses = (i == cssAxis.length - 1) ? classNameFromTheRegion : getNodeAttributeValue(node, "class"); //$NON-NLS-1$
 						String[] classes = nodeClasses == null ? null : nodeClasses.split(" "); //$NON-NLS-1$
 						if (classes != null) {
 							for (String cssClass : classes) {
 								if (currentAxis.className.equalsIgnoreCase(cssClass)) {
 									classFound = true;
 									break;
 								}
 							}
 						}
 					}
 					
 					if (!nodeFound) {
 						nodeFound = currentAxis.nodeName.equalsIgnoreCase(node.getNodeName());
 					}
 					
 					if (!attrFound) {
 						attrFound = currentAxis.attrValue.equalsIgnoreCase(getNodeAttributeValue(node, currentAxis.attrName));
 					}
 
 					if (classFound && nodeFound && attrFound) {
 						// Proceed with next Axis
 						break;
 					}
 					
 					// proceed with parent node
 					node = node.getParentNode();
 				} while (node instanceof Element);
 
 				
 				// All three parts of the axis were found - proceed with next axis
 				// But do re-check that we still can proceed
 				if (!(node instanceof Element)) {
 					return false;
 				}
 			}
 			
 			return true;
 		} finally {
 			smw.dispose();
 		}		
 	}
 	
 	String getNodeAttributeValue(Node node, String attrName) {
 		NamedNodeMap attrs = node.getAttributes();
 		if (attrs == null)
 			return null;
 		Node attr = attrs.getNamedItem(attrName);
 		if (attr == null)
 			return null;
 		return ((Attr)attr).getValue();
 	}
 
 	class CSSRuleDescriptor {
 		CSSRule rule;
 		CSSAxis[] axis;
 		CSSStyleSheetDescriptor stylesheetDescriptor;
 		
 		CSSRuleDescriptor (CSSRule rule, CSSAxis[] axis) {
 			this.rule = rule;
 			this.axis = axis;
 		}
 	}
 	
 	class CSSAxis {
 		static final String CSS_ANY = "*"; //$NON-NLS-1$
 		String className;
 		String nodeName;
 		String attrName;
 		String attrValue;
 		
 		public CSSAxis(String className, String nodeName, String attrName, String attrValue) {
 			this.className = (className == null ? CSS_ANY : className);
 			this.nodeName = (nodeName == null ? CSS_ANY : nodeName);
 			this.attrName = (attrName == null ? CSS_ANY : attrName);
 			this.attrValue = (attrValue == null ? CSS_ANY : attrValue);
 			if (attrName == null || attrValue == null) {
 				this.attrName = this.attrValue = CSS_ANY;
 			}
 		}
 	}
 	
 	CSSAxis getCSSAxis(String rule) {
 		String restOfRule = rule;
 		
 		// There may be CSS class name at the end
 		String className = null;
 		int lastDot = rule.lastIndexOf('.');
 		if (lastDot != -1) {
 			className = rule.substring(lastDot + 1).trim();
 			if (!checkWord(className)) {
 				className = null;
 			}
 			restOfRule = restOfRule.substring(0, lastDot);
 		}
 		
 		// there may be a node name at the beginning
 		String nodeName = null;
 		if (restOfRule.length() > 0) {
 			StringBuilder sb = new StringBuilder();
 			for (char ch : restOfRule.toCharArray()) {
				if (!Character.isJavaIdentifierPart(ch) 
						&& ch != '-' && ch != '_')
 					break;
 				sb.append(ch);
 			}
 			if (sb.length() > 0) {
 				nodeName = sb.toString();
 				restOfRule = restOfRule.substring(sb.length());
 			}
 		}
 		
 		// there may be node attribute/value inside square brackets
 		String attrName = null;
 		String attrValue = null;
 		if (restOfRule.startsWith("[") &&  restOfRule.endsWith("]")) {//$NON-NLS-1$ //$NON-NLS-2$
 			restOfRule = restOfRule.substring(1, restOfRule.length() - 1).trim();
 			
 			// Get attr name
 			StringBuilder sb = new StringBuilder();
 			for (char ch : restOfRule.toCharArray()) {
				if (!Character.isJavaIdentifierPart(ch) 
						&& ch != '-' && ch != '_')
 					break;
 				sb.append(ch);
 			}
 			if (sb.length() > 0) {
 				attrName = sb.toString();
 				restOfRule = restOfRule.substring(sb.length()).trim();
 			}
 			// There should be an equal sign between attr name and value
 			if (restOfRule.startsWith("=")) { //$NON-NLS-1$
 				restOfRule = restOfRule.substring(1).trim();
 				attrValue = Utils.trimQuotes(restOfRule).trim();
 				if (attrValue.trim().length() == 0) {
 					attrValue = null;
 				}
 			}
 		}
 		
 		return new CSSAxis(className, nodeName, attrName, attrValue);
 	}
 
 	boolean checkWord(String word) {
 		for (char ch : word.toCharArray()) {
			if (!Character.isJavaIdentifierPart(ch)  
					&& ch != '-' && ch != '_') {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param styleRegion
 	 */
 	protected void showRegion(IFile file, IRegion region) {
 
 		IWorkbenchPage workbenchPage = ExtensionsPlugin.getDefault()
 				.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		IEditorPart part = null;
 		if (file != null) {
 			try {
 				part = IDE.openEditor(workbenchPage, file, true);
 			} catch (PartInitException e) {
 				e.printStackTrace();
 			}
 		}
 		if (part == null) {
 			openFileFailed();
 			return;
 		}
 		StructuredSelectionHelper.setSelectionAndReveal(part,
 				region);
 	}
 	
 	/**
 	 * 
 	 * @param region
 	 * @return
 	 */
 	protected String getStyleName(IRegion region) {
 		if(region != null) {
 			try {
 				return getDocument().get(region.getOffset(), region.getLength()).trim();
 			} catch (BadLocationException e) {
 				JSTExtensionsPlugin.getPluginLog().logError(e);
 			}
 		}
 		return null;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IHyperlink#getHyperlinkText()
 	 */
 	public String getHyperlinkText() {
 		String styleName = getStyleName(getHyperlinkRegion());
 		if (styleName == null)
 			return MessageFormat.format(Messages.OpenA, Messages.CSSStyle);
 
 		return MessageFormat.format(Messages.OpenCSSStyle, styleName);
 	}
 
 	@Override
 	protected String findAndReplaceElVariable(String fileName) {
 		if (fileName != null)
 			fileName = fileName.replaceFirst(CONTEXT_PATH_EXPRESSION,""); //$NON-NLS-1$
 		
 		return super.findAndReplaceElVariable(fileName);
 	}
 }
