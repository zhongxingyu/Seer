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
 package org.jboss.tools.jst.jsp.contentassist;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.fieldassist.IContentProposal;
 import org.eclipse.jface.fieldassist.IContentProposalProvider;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jst.jsp.ui.internal.contentassist.JSPContentAssistProcessor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.wst.sse.ui.internal.contentassist.IRelevanceCompletionProposal;
 import org.eclipse.wst.sse.ui.internal.util.Sorter;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.jboss.tools.common.el.core.model.ELInstance;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.model.ELModel;
 import org.jboss.tools.common.el.core.model.ELUtil;
 import org.jboss.tools.common.el.core.parser.ELParser;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.el.core.resolver.ELResolverFactoryManager;
 import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.jst.jsp.contentassist.AbstractXMLContentAssistProcessor.TextRegion;
 import org.jboss.tools.jst.jsp.jspeditor.JSPMultiPageEditor;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.jsp.outline.ValueHelper;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.Constants;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.w3c.dom.Node;
 
 /**
  * 
  * @author Viacheslav Kabanovich
  *
  */
 public class JSPDialogContentProposalProvider implements IContentProposalProvider {
 	static int EL_MODE = 0;
 	static int ATTR_MODE = 1;
 	int mode = EL_MODE;
 	
 	Properties context;
 	String attributeName;
 	String nodeName;
 	int offset = 0;
 	JspContentAssistProcessor processor;
 	IPageContext pageContext = null;
 
 	public JSPDialogContentProposalProvider() {		
 	}
 
 	public void setAttrMode() {
 		mode = ATTR_MODE;
 	}
 	
 	public void setContext(Properties context) {
 		this.context = context;
         attributeName = Constants.EMPTY + context.getProperty("attributeName"); //$NON-NLS-1$
         nodeName = Constants.EMPTY + context.getProperty("nodeName"); //$NON-NLS-1$
         Node node = (Node)context.get("node"); //$NON-NLS-1$
         if (node instanceof IDOMElement) {
         	offset = ((IDOMElement)node).getStartOffset() + ("" + nodeName).length(); //approximation, attribute may be not defined //$NON-NLS-1$
         } else if(context.get("offset") != null) { //$NON-NLS-1$
         	offset = ((Integer)context.get("offset")).intValue(); //$NON-NLS-1$
         }
         ValueHelper valueHelper = (ValueHelper)context.get("valueHelper"); //$NON-NLS-1$
         if(valueHelper == null) {
         	valueHelper = new ValueHelper();
         }
         pageContext = (IPageContext)context.get("pageContext"); //$NON-NLS-1$
         processor = (JspContentAssistProcessor)context.get("processor"); //$NON-NLS-1$
         if(processor == null) {
         	processor = valueHelper.createContentAssistProcessor();
         	context.put("processor", processor); //$NON-NLS-1$
         }
         if(pageContext == null) {
         	pageContext = valueHelper.createPageContext(processor, offset);
         }
         context.put("pageContext", pageContext); //$NON-NLS-1$
         context.put("kbQuery", createKbQuery(Type.ATTRIBUTE_VALUE, "", "", offset, false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	}
 
 	public IContentProposal[] getProposals(String contents, int position) {
 		if(mode == ATTR_MODE) {
 			return getAttrProposals(contents, position);
 		} else {
 			return getELProposals(contents, position);
 		}
 	}
 
 	public IContentProposal[] getAttrProposals(String contents, int position) {
 		List<IContentProposal> result = new ArrayList<IContentProposal>();
 		TextRegion p = getELPrefix(contents, position);
 		if (p == null || !p.isELStarted()) {
 			KbQuery kbQuery = createKbQuery(Type.ATTRIBUTE_VALUE, contents.substring(0, position), contents, position, false);
 			TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, pageContext);
 			if(proposals != null) for (TextProposal textProposal: proposals) {
 				String displayString = textProposal.getReplacementString();
 				int cursorPosition = /*replacementOffset + */ textProposal.getReplacementString().length();
 
 				Image image = textProposal.getImage();
 				String relacementString = textProposal.getReplacementString();
 				if(textProposal.getStart() >= 0 && textProposal.getEnd() >= 0) {
 					int b = textProposal.getStart();
 					int e = textProposal.getEnd();
 					String prefix = contents.substring(0, b);
 					String tail = contents.substring(e);
 					relacementString = prefix + relacementString + tail;
 				}
 				IContentProposal proposal = //new ContentProposal(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, contextInformation, additionalProposalInfo, relevance);
 					new ContentProposal(relacementString, cursorPosition, displayString, displayString);
 				result.add(proposal);
 			}
 			IContentProposal proposal = new ContentProposal(contents.substring(0, position) + "#{}" + contents.substring(position), position, "#{}", JstUIMessages.JSPDialogContentProposalProvider_NewELExpression); //$NON-NLS-1$ //$NON-NLS-2$
 			result.add(proposal);
 		}
 		return toSortedUniqueArray(result);
 	}
 
 	public IContentProposal[] getELProposals(String contents, int position) {
 		List<IContentProposal> result = new ArrayList<IContentProposal>();
 		TextRegion prefix = getELPrefix(contents, position);
 		if (prefix == null || !prefix.isELStarted()) {
 //			IContentProposal proposal = new ContentProposal("#{}", 0, "#{}", JstUIMessages.JSPDialogContentProposalProvider_NewELExpression); //$NON-NLS-1$ //$NON-NLS-2$
 //			result.add(proposal);
 			return toSortedUniqueArray(result);
 		}
 		String matchString = "#{" + prefix.getText(); //$NON-NLS-1$
 		String query = matchString;
 		if (query == null)
 			query = ""; //$NON-NLS-1$
 		String stringQuery = matchString;
 
 		int beginChangeOffset = prefix.getStartOffset() + prefix.getOffset();
 
 		KbQuery kbQuery = createKbQuery(Type.ATTRIBUTE_VALUE, query, stringQuery, position, true);
 		TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, pageContext);
 
 		if(proposals != null) for (TextProposal textProposal: proposals) {
 			int replacementOffset = beginChangeOffset;
 			int replacementLength = prefix.getLength();
 			String displayString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 			int cursorPosition = /*replacementOffset + */ textProposal.getReplacementString().length();
 
 			if(!prefix.isELClosed()) {
 				textProposal.setReplacementString(textProposal.getReplacementString() + "}"); //$NON-NLS-1$
 			}
 
 			Image image = textProposal.getImage();
 
 //			IContextInformation contextInformation = null;
 //			String additionalProposalInfo = textProposal.getContextInfo();
 //			int relevance = textProposal.getRelevance() + 10000;
 
 			IContentProposal proposal = //new ContentProposal(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, contextInformation, additionalProposalInfo, relevance);
 				new ContentProposal(textProposal.getReplacementString(), cursorPosition, displayString, displayString);
 			result.add(proposal);
 		}
 
 		if (prefix.isELStarted() && !prefix.isELClosed()) {
 			IContentProposal proposal = new ContentProposal("}", 0, "}", JstUIMessages.JSPDialogContentProposalProvider_CloseELExpression); //$NON-NLS-1$ //$NON-NLS-2$
 			result.add(proposal);
 		}
 		return toSortedUniqueArray(result);
 	}
 
 	public IContentProposal[] makeUnique(IContentProposal[] proposals) {
 		HashSet<String> present = new HashSet<String>();
 		HashSet<String> info = new HashSet<String>();
 		ArrayList<IContentProposal> unique= new ArrayList<IContentProposal>();
 
 		for (int i = 0; proposals != null && i < proposals.length; i++) {
 			String infoUnquoted = proposals[i].getContent();
 			if (infoUnquoted != null) {
 				if (infoUnquoted.startsWith("\""))
 					infoUnquoted = infoUnquoted.substring(1);
 				if (infoUnquoted.endsWith("\""))
 					infoUnquoted = infoUnquoted.substring(0, infoUnquoted.length() - 1);
 				infoUnquoted = infoUnquoted.trim();
 			}
 			if (!present.contains(proposals[i].getLabel())) {
 				present.add(proposals[i].getLabel());
 				if (infoUnquoted != null && infoUnquoted.length() > 0) {
 					if (!info.contains(infoUnquoted)) {
 						info.add(infoUnquoted);
 					} else {
 						// Do not add proposals with the same info
 						continue;
 					}
 				}
 				unique.add(proposals[i]);
 			}
 		}
 
 		present.clear();
 		return unique.toArray(new IContentProposal[unique.size()]);
 	}
 
 	IContentProposal[] toSortedUniqueArray(List<IContentProposal> result) {
 		IContentProposal[] resultArray = result.toArray(new IContentProposal[0]);
 		if(resultArray.length < 2) return resultArray;
 		Object[] sorted = createSorter().sort(resultArray);
 		System.arraycopy(sorted, 0, resultArray, 0, sorted.length);
 		resultArray = makeUnique(resultArray);
 		return resultArray;
 	}
 
 	protected Sorter createSorter() {
 		return new Sorter() {
 			public boolean compare(Object proposal1, Object proposal2) {
 
 				int pr1 = Integer.MIN_VALUE;
 				int pr2 = Integer.MIN_VALUE;
 				
 				IContentProposal p1 = (IContentProposal)proposal1;
 				IContentProposal p2 = (IContentProposal)proposal2;
 				
 				if (pr1 == pr2) {
 					String str1 = (p1.getLabel() == null ? "" : p1.getLabel()); //$NON-NLS-1$
 					String str2 = (p2.getLabel() == null ? "" : p2.getLabel()); //$NON-NLS-1$
 					return str2.compareTo(str1) > 0;
 				}
 
 				return (pr1 > pr2);
 			}
 		};
 	}
 	
 
 	class ContentProposal implements IContentProposal {
 		String content;
 		int pos;
 		String description = ""; //$NON-NLS-1$
 		String label;
 	
 		public ContentProposal(String content, int pos, String label, String description) {
 			this.content = content;
 			this.pos = pos;
 			this.label = label;
 			this.description = description;
 		}
 
 		public String getContent() {
 			return content;
 		}
 
 		public int getCursorPosition() {
 			return pos;
 		}
 
 		public String getDescription() {
 			return description;
 		}
 
 		public String getLabel() {
 			return label;
 		}
 		
 	}
 
 	protected TextRegion getELPrefix(String text, int pos) {
 			int inValueOffset = pos;
 			if (text.length() < inValueOffset) { // probably, the attribute value ends before the document position
 				return null;
 			}
 			if (inValueOffset<0) {
 				return null;
 			}
 			
 			String matchString = text.substring(0, inValueOffset);
 			
 			ELParser p = ELParserUtil.getJbossFactory().createParser();
 			ELModel model = p.parse(text);
 			
 			ELInstance is = ELUtil.findInstance(model, inValueOffset);// ELInstance
 			ELInvocationExpression ie = ELUtil.findExpression(model, inValueOffset);// ELExpression
 			
 			boolean isELStarted = (model != null && is != null && (model.toString().startsWith("#{") ||  //$NON-NLS-1$
 					model.toString().startsWith("${"))); //$NON-NLS-1$
 			boolean isELClosed = (model != null && is != null && model.toString().endsWith("}")); //$NON-NLS-1$
 			
 //			boolean insideEL = startOffset + model.toString().length() 
 			TextRegion tr = new TextRegion(0,  ie == null ? inValueOffset : ie.getStartPosition(), ie == null ? 0 : inValueOffset - ie.getStartPosition(), ie == null ? "" : ie.getText(), isELStarted, isELClosed); //$NON-NLS-1$
 			
 			return tr;
 	}
 
 	protected ELResolver[] getELResolvers(IResource resource) {
 		ELResolverFactoryManager elrfm = ELResolverFactoryManager.getInstance();
 		return elrfm.getResolvers(resource);
 	}
 
 	protected KbQuery createKbQuery(Type type, String query, String text, int pos, boolean addAttr) {
 		KbQuery kbQuery = new KbQuery();
 
 		String[] parentTags = processor.getParentTags(false);
 		parentTags = add(parentTags, nodeName);
 		if(addAttr) {
 			parentTags = add(parentTags, attributeName);
 		}
 		kbQuery.setPrefix(getPrefix());
 		kbQuery.setUri(processor.getUri(getPrefix()));
 		kbQuery.setParentTags(parentTags);
 		kbQuery.setParent(attributeName);
 		kbQuery.setMask(true); 
 		kbQuery.setType(type);
 //		kbQuery.setOffset(pos);
 		kbQuery.setOffset(offset);
 		kbQuery.setValue(query); 
 		kbQuery.setStringQuery(query);
		kbQuery.setText(text);
 		
 		return kbQuery;
 	}
 
 	private String getPrefix() {
 		if(nodeName == null) return null;
 		int i = nodeName.indexOf(':');
 		return i < 0 ? null : nodeName.substring(0, i);
 	}
 
 	protected String[] getParentTags(JspContentAssistProcessor processor) {
 		String[] result = processor.getParentTags(true);
 		String[] result1 = add(result, attributeName);
 		return result1;
 	}
 
 	private String[] add(String[] result, String v) {
 		String[] result1 = new String[result.length + 1];
 		System.arraycopy(result, 0, result1, 0, result.length);
 		result1[result.length] = v;
 		return result1;
 	}
 
 }
