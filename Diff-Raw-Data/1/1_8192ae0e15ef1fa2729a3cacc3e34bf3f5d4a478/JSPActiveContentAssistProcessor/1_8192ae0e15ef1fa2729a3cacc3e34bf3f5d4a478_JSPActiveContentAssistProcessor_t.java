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
 package org.jboss.tools.jst.jsp.contentassist;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.wst.sse.core.utils.StringUtils;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.contentassist.XMLRelevanceConstants;
 import org.eclipse.wst.xml.ui.internal.util.SharedXMLEditorPluginImageHelper;
 import org.jboss.tools.common.el.core.model.ELInstance;
 import org.jboss.tools.common.el.core.model.ELModel;
 import org.jboss.tools.common.el.core.model.ELUtil;
 import org.jboss.tools.common.el.core.parser.ELParser;
 import org.jboss.tools.common.el.core.parser.ELParserFactory;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.kb.KbException;
 import org.jboss.tools.common.kb.KbProposal;
 import org.jboss.tools.common.kb.KbQuery;
 import org.jboss.tools.common.kb.wtp.WtpKbConnector;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.contentassist.xpl.JSPBaseContentAssistProcessor;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * @author Igels
  */
 public class JSPActiveContentAssistProcessor extends JSPBaseContentAssistProcessor {
 
     private WtpKbConnector wtpKbConnector;
     private boolean isFacelets = false;
 
 	public JSPActiveContentAssistProcessor() {
 		super();
 	}
 
 	public void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {
 		if(wtpKbConnector==null) {
 			return;
 		}
 		super.addAttributeValueProposals(contentAssistRequest);
 	}
 	
 	public void addFaceletAttributeValueProposals(
 			ContentAssistRequest contentAssistRequest,String tagName,IDOMNode node,String attributeName, String matchString,String strippedValue, int offset, String currentValue) {
 		boolean faceletJsfTag = false;
 
 		String htmlQuery = null;
 		if(isFacelets && tagName.indexOf(':')<1 && !FaceletsHtmlContentAssistProcessor.JSFCAttributeName.equals(attributeName)) {
 			Element element = (Element)node;
 
 			NamedNodeMap attributes = element.getAttributes();
 			Node jsfC = attributes.getNamedItem(FaceletsHtmlContentAssistProcessor.JSFCAttributeName);
 			if(jsfC!=null && (jsfC instanceof Attr)) {
 				Attr jsfCAttribute = (Attr)jsfC;
 				String jsfTagName = jsfCAttribute.getValue();
 				if(jsfTagName!=null && jsfTagName.indexOf(':')>0) {
 					htmlQuery = new StringBuffer(KbQuery.TAG_SEPARATOR).append(FaceletsHtmlContentAssistProcessor.faceletHtmlPrefixStart + tagName).append(KbQuery.ATTRIBUTE_SEPARATOR).append(attributeName).append(KbQuery.ENUMERATION_SEPARATOR).append(matchString).toString();
 					tagName = jsfTagName;
 					faceletJsfTag = true;
 				}
 			}
 		}
 
 		if(!faceletJsfTag && isFacelets && tagName.indexOf(':')<0) {
 			tagName = FaceletsHtmlContentAssistProcessor.faceletHtmlPrefixStart + tagName;
 		}
 
     	if(!currentValue.startsWith("\"") && !currentValue.startsWith("'")) {
     		// Do not show any value proposals if the attribute value is not started with a quote/double-quote character 
     		return;
     	}
 
 		int elStartPosition = getELStartPosition(matchString);
 		int delta = 0;
 		String elProposalPrefix = "";
 		String elQueryString = null;
 		String queryString = null;
 		String elStartChar = "#";
 		if (elStartPosition == -1) {
 			queryString = matchString;
 			elQueryString = "#{";
 			delta = matchString.length();
 			if(isCharSharp(matchString, offset-1) || isCharDollar(matchString, offset-1)) {
 				elProposalPrefix = "{";  //$NON-NLS-1$
 				queryString = null; // Do not request for ordinar attr-value proposals 
 									// in case of starting EL-expression
 				if (isCharDollar(matchString, offset-1)) 
 					elStartChar = "$";
 			} else {
 				elProposalPrefix = "#{";  //$NON-NLS-1$
 			}
	    	delta = matchString.length() - elQueryString.length();
 			if (matchString.startsWith("\"") || matchString.startsWith("'")) { 
 				queryString = matchString.substring(1);
 			}
 		} else {
 			// // Do not request for ordinar attr-value proposals 
 			// in case of EL-expression 
 			queryString = null;
 			elQueryString = matchString.substring(elStartPosition);
 	    	delta = matchString.length() - elQueryString.length();
 		}
 		
     	// Correct delta if matchString starts with a quote (exclude that quote)
 		if (matchString.startsWith("\"") || matchString.startsWith("'")) {
 			delta--;
 		}
 
 		Collection proposals = null;
 		if (queryString != null) {
 			// Query to request for ordinar proposals
 			try {
 			    String query = new StringBuffer(KbQuery.TAG_SEPARATOR).append(tagName).append(KbQuery.ATTRIBUTE_SEPARATOR).append(attributeName).append(KbQuery.ENUMERATION_SEPARATOR).append(queryString).toString();
 				proposals = wtpKbConnector.getProposals(query);
 				if(proposals.size()==0 && htmlQuery!=null) {
 					proposals = wtpKbConnector.getProposals(htmlQuery);
 				}
 		        if(proposals != null) {
 		            for (Iterator iter = proposals.iterator(); iter.hasNext();) {
 		            	KbProposal kbProposal = cleanFaceletProposal((KbProposal)iter.next());
 		            	kbProposal.postProcess(queryString, offset - delta);
 		            	if (kbProposal.getReplacementString().startsWith("#{"))
 		            		continue; // Do not process EL-proposals here!!!
 		            	
 		                int relevance = kbProposal.getRelevance();
 		                if(relevance==KbProposal.R_NONE) {
 		                    relevance = KbProposal.R_XML_ATTRIBUTE_VALUE;
 		                }
 		                
 		                if(kbProposal.getStart() < 0) {
 		                	StringBuffer replacementStringBuffer = new StringBuffer(kbProposal.getReplacementString());
 		                    int replacementBeginPosition = contentAssistRequest.getReplacementBeginPosition();
 		                	int replacementLength = contentAssistRequest.getReplacementLength();
 		                	int cursorPositionDelta = 0;
 		                	if(currentValue.startsWith("\"") || currentValue.startsWith("'")) {
 		                		replacementBeginPosition = replacementBeginPosition +1;
 		                		replacementLength--;
 		            		} else {
 		            			cursorPositionDelta++;
 		            			replacementStringBuffer.insert(0, "\"");
 		            		}
 		                	if((currentValue.endsWith("\"") || currentValue.endsWith("'")) &&
 		                			strippedValue.length() < currentValue.length() ) {
 		                		replacementLength--;
 		            		}
 		            		
 		                	String replacementString = replacementStringBuffer.toString();
 		                	int cursorPosition = kbProposal.getPosition() + cursorPositionDelta;
 		                	AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(kbProposal.autoActivationContentAssistantAfterApplication(), replacementString,
 		                			replacementBeginPosition, replacementLength, cursorPosition, SharedXMLEditorPluginImageHelper.getImage(SharedXMLEditorPluginImageHelper.IMG_OBJ_ATTRIBUTE),
 		            				kbProposal.getLabel(), null, kbProposal.getContextInfo(), relevance);
 		            		contentAssistRequest.addProposal(proposal);
 
 		                }
 		            }
 		        }
 	        } catch (KbException e) {
 	        	JspEditorPlugin.getPluginLog().logError(e);
 	        }
 		}
 
 		Collection elProposals = null;
 		if (elQueryString != null) {
 			// Query to request for EL-proposals
 			try {
 			    String query = new StringBuffer(KbQuery.TAG_SEPARATOR).append(tagName).append(KbQuery.ATTRIBUTE_SEPARATOR).append(attributeName).append(KbQuery.ENUMERATION_SEPARATOR).append(elQueryString).toString();
 				elProposals = wtpKbConnector.getProposals(query);
 	        } catch (KbException e) {
 	        	JspEditorPlugin.getPluginLog().logError(e);
 	        }
 	        if(elProposals != null) {
 	            for (Iterator iter = elProposals.iterator(); iter.hasNext();) {
 	            	KbProposal kbProposal = cleanFaceletProposal((KbProposal)iter.next());
 	            	kbProposal.postProcess("\"" + elQueryString, offset - delta);
 	            	if (!kbProposal.getReplacementString().startsWith("#{"))
 	            		continue; // Process the only EL-proposals here!!!
 	                int relevance = kbProposal.getRelevance();
 	                if(relevance==KbProposal.R_NONE) {
 	                    relevance = KbProposal.R_JSP_JSF_EL_VARIABLE_ATTRIBUTE_VALUE;
 	                }
 	                
 	                if(kbProposal.getStart() >= 0) {
 	        			String replacementString = kbProposal.getReplacementString().substring(2,kbProposal.getReplacementString().length() - 1);
 	                    int replacementBeginPosition = contentAssistRequest.getReplacementBeginPosition() + kbProposal.getStart() + delta;
 	                    int replacementLength = kbProposal.getEnd() - kbProposal.getStart();
 	                	int cursorPositionDelta = 0;
 	                	
 	                	// Add an EL-starting quotation characters if needed
 	                	if (elStartPosition == -1) {
 	                		replacementString = elProposalPrefix + replacementString;
 	            			cursorPositionDelta += elProposalPrefix.length();
 	                	}
 	  
 	                	if((currentValue.length() > StringUtils.strip(currentValue).length()) && 
 	                			(currentValue.endsWith("\"") || currentValue.endsWith("'")) ) {
 	                		String restOfCurrentValue = currentValue.substring(matchString.length());
 	                		
 	                		if(getELEndPosition(matchString, currentValue) == -1) {
 	                			replacementString += "}";
 	                		}
 	            		} else {
 	                		if(elStartPosition == -1 && !currentValue.endsWith("}")) {
 	                			replacementString += "}";
 	                		}
 	//            			replacementString += ("\"");
 	            		}
 	                	int cursorPosition = kbProposal.getPosition() + cursorPositionDelta;
 	                	String displayString = elProposalPrefix == null || elProposalPrefix.length() == 0 ? 
 	                			kbProposal.getReplacementString().substring(2,kbProposal.getReplacementString().length() - 1) :
 	                			elProposalPrefix + kbProposal.getReplacementString().substring(2,kbProposal.getReplacementString().length() - 1) + "}" ;
 
 	                	if ('#' == displayString.charAt(0) || '$' == displayString.charAt(0))
 	                		displayString = elStartChar + displayString.substring(1);
 
 	                	AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(
 	                			kbProposal.autoActivationContentAssistantAfterApplication(), 
 	                			replacementString,
 	                			replacementBeginPosition, replacementLength, cursorPosition, 
 	                			SharedXMLEditorPluginImageHelper.getImage(SharedXMLEditorPluginImageHelper.IMG_OBJ_ATTRIBUTE),
 	            				displayString, null, kbProposal.getContextInfo(), relevance);
 	            		contentAssistRequest.addProposal(proposal);
 	                }
 	            }
 	        }
 		}
 	}
 
 	private KbProposal cleanFaceletProposal(KbProposal proposal) {
 		if(isFacelets) {
 			proposal.setLabel(removeFaceletsPrefix(proposal.getLabel()));
 			proposal.setReplacementString(removeFaceletsPrefix(proposal.getReplacementString()));
 		}
 		return proposal;
 	}
 
 	private String removeFaceletsPrefix(String tagName) {
 		if(tagName.startsWith(FaceletsHtmlContentAssistProcessor.faceletHtmlPrefixStart)) {
 			return tagName.substring(FaceletsHtmlContentAssistProcessor.faceletHtmlPrefixStart.length());
 		}
 		return tagName;
 	}
 
 	public void init() {
 	    super.init();
 	}
 
 	public void setKbConnector(WtpKbConnector connector) {
 	    this.wtpKbConnector = connector;
 	}
 
     public void setFacelets(boolean isFacelets) {
 		this.isFacelets = isFacelets;
 	}
     
 	/*
 	 * Checks if the EL operand starting characters are present
 	 * @return
 	 */
 	private int getELStartPosition(String matchString) {
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(matchString);
 		ELInstance is = ELUtil.findInstance(model, matchString.length());
 		return is == null ? -1 : is.getStartPosition();
 	}
 
 	/*  Checks if the preceding character is a Sharp-character
 	 */
 	private boolean isCharSharp(String matchString, int offset) {
 		if (matchString == null || offset > matchString.length() || offset < 0) {
 			return false;
 		}
 		return ('#' == matchString.charAt(offset));
 	}
 
 	/*  Checks if the preceding character is a Dollar-character
 	 */
 	private boolean isCharDollar(String matchString, int offset) {
 		if (matchString == null || offset > matchString.length() || offset < 0) {
 			return false;
 		}
 		return ('$' == matchString.charAt(offset));
 	}
 
 	/*
 	 * Checks if the EL operand ending character is present
 	 * @return
 	 */
 	private int getELEndPosition(String matchString, String currentValue) {
 		if (matchString == null || matchString.length() == 0 ||
 				currentValue == null || currentValue.length() == 0 || 
 				currentValue.length() < matchString.length())
 			return -1;
 
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(currentValue);
 		ELInstance is = ELUtil.findInstance(model, matchString.length());
 		if(is == null || is.getCloseInstanceToken() == null) return -1;
 
 		return is.getEndPosition();
 	}
 
 }
