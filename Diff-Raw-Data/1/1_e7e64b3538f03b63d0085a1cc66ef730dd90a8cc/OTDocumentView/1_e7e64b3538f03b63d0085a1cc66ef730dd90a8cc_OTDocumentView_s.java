 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on Aug 19, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.concord.otrunk.view.document;
 
 import java.io.StringReader;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.Element;
 import javax.swing.text.html.HTML;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.view.OTFrame;
 import org.concord.framework.otrunk.view.OTJComponentView;
 import org.concord.framework.otrunk.view.OTView;
 import org.concord.framework.otrunk.view.OTViewEntryAware;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.framework.otrunk.view.OTXHTMLView;
 import org.concord.otrunk.OTrunkUtil;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * @author scott
  * 
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Generation - Code and Comments
  */
 public class OTDocumentView extends AbstractOTDocumentView implements
 		ChangeListener, HyperlinkListener, OTXHTMLView, OTViewEntryAware {
 	public static boolean addedCustomLayout = false;
 
 	JTabbedPane tabbedPane = null;
 
 	protected JComponent previewComponent = null;
 
 	protected JEditorPane editorPane = null;
 
 	DocumentBuilderFactory xmlDocumentFactory = null;
 
 	DocumentBuilder xmlDocumentBuilder = null;
 
 	protected JTextArea parsedTextArea = null;
 
 	protected OTDocumentViewConfig viewEntry;
 
 	protected OTObject otObject;
 
 	public final static String XHTML_PREFIX_START =
 	// "<?xml version='1.0' encoding='UTF-8'?>\n" +
 	"<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>\n"
 			+ "\n" + "<head>\n" + "<title>default</title>\n"
 			+ "<style type=\"text/css\">\n";
 
 	public final static String XHTML_PREFIX_END = "</style>" + "</head>\n"
 			+ "<body>\n";
 
 	public final static String XHTML_SUFFIX = "</body>\n" + "</html>";
 
 	protected void setup(OTObject doc) {
 		// Don't call super.setup() to avoid listening to the ot object
 		// inneccesarily
 		pfObject = (OTDocument) doc;
 	}
 
 	public JComponent getComponent(OTObject otObject, boolean editable) {
 		this.otObject = otObject;
 		setup(otObject);
 		initTextAreaModel();
 		
 		if (tabbedPane != null) {
 			tabbedPane.removeChangeListener(this);
 		}
 
 		// need to use the PfCDEditorKit
 		updateFormatedView();
 
 		setReloadOnViewEntryChange(true);
 
 		if (!editable) {
 			return previewComponent;
 		}
 
 		// JScrollPane renderedScrollPane = new JScrollPane(previewComponent);
 		// renderedScrollPane.getViewport().setViewPosition(new Point(0,0));
 
 		if (System.getProperty("otrunk.view.debug", "").equals("true")) {
 			tabbedPane = new JTabbedPane();
 
 			// need to add a listener so we can update the view pane
 			tabbedPane.add("View", previewComponent);
 
 			textArea = new JTextArea(textAreaModel);
 			JScrollPane scrollPane = new JScrollPane(textArea);
 			tabbedPane.add("Edit", scrollPane);
 
 			parsedTextArea.setEnabled(false);
 			scrollPane = new JScrollPane(parsedTextArea);
 			tabbedPane.add("Parsed", scrollPane);
 
 			tabbedPane.addChangeListener(this);
 
 			return tabbedPane;
 		} else {
 			return previewComponent;
 		}
 	}
 
 	public String updateFormatedView() {
 		if (pfObject == null)
 			return null;
 
 		// System.out.println(this+" updateFormatedView");
 
 		String markupLanguage = pfObject.getMarkupLanguage();
 		if (markupLanguage == null) {
 			markupLanguage = System.getProperty("org.concord.portfolio.markup",
 					null);
 		}
 
 		String bodyText = pfObject.getDocumentText();
 		bodyText = substituteIncludables(bodyText);
 
 		// default to html viewer for now
 		// FIXME the handling of the plain markup is to test the new view entry
 		// code
 		// it isn't quite valid because plain text might have html chars in it
 		// so it will get parsed incorrectly.
 		if (markupLanguage == null
 				|| markupLanguage.equals(OTDocument.MARKUP_PFHTML)
 				|| markupLanguage.equals(OTDocument.MARKUP_PLAIN)) {
 			if (editorPane == null) {
 				editorPane = new JEditorPane();
 				OTHTMLFactory kitViewFactory = new OTHTMLFactory(this);
 				OTDocumentEditorKit editorKit = new OTDocumentEditorKit(
 						kitViewFactory);
 				editorPane.setEditorKit(editorKit);
 				editorPane.setEditable(false);
 				editorPane.addHyperlinkListener(this);
 			}
 			bodyText = htmlizeText(bodyText);
 
 			if (viewEntry instanceof OTDocumentViewConfig) {
 				
 				String css = "";
 				if (viewEntry.getCss() != null && viewEntry.getCss().length() > 0){
 					css = css + viewEntry.getCss();
 				}
 				
 				if (viewEntry.getCssBlocks() != null && viewEntry.getCssBlocks().getVector().size() > 0){
 					Vector cssBlocks =  viewEntry.getCssBlocks().getVector(); 
 					for (int i = 0; i < cssBlocks.size(); i++) {
 		                OTCssText cssText = (OTCssText) cssBlocks.get(i);
 		                css = css + " " + cssText.getCssText();
 	                }
 				}
 				
 				String XHTML_PREFIX = XHTML_PREFIX_START + css
 						+ XHTML_PREFIX_END;
 				bodyText = XHTML_PREFIX + bodyText + XHTML_SUFFIX;
 			}
 			// when this text is set it will recreate all the
 			// OTDocumentObjectViews, so we need to clear and
 			// close all the old panels first
 			removeAllSubViews();
 
 			editorPane.setText(bodyText);
 
 			previewComponent = editorPane;
 
 			// we used to set thie caret pos so the view would
 			// scroll to the top, but now we disable scrolling
 			// during the load, and that seems to work better
 			// there is no flicker that way.
 			// editorPane.setCaretPosition(0);
 		} else {
 			System.err.println("xhtml markup not supported");
 		}
 
 		if (parsedTextArea == null) {
 			parsedTextArea = new JTextArea();
 		}
 		parsedTextArea.setText(bodyText);
 		
 		return bodyText;
 	}
 
 	/**
 	 * This method gets the object with idStr and tries to get the view entry
 	 * with the viewIdStr. If the viewIdStr is not null then it gets the view
 	 * for that view entry and the current view mode, and determines if it is a
 	 * {@link OTXHTMLView}. If it is then it gets the html text of that view.
 	 * If the viewIdStr is null then asks the viewFactory for a view of this
 	 * object which implements the OTXHTMLView interface. If such a view exists
 	 * then that is used to get the text.
 	 * 
 	 * The text returned is intended for use by the Matcher.appendReplacement
 	 * method. So $0 means to just leave the text as is. Because of this any
 	 * text needs to be escaped, specifically $ and \ needs to be escaped.
 	 * {@link OTrunkUtil#escapeReplacement(String)}
 	 * 
 	 * So far this is only used by the substitueIncludables method
 	 * 
 	 * @see OTrunkUtil#escapeReplacement(String)
 	 * @see #substituteIncludables(String)
 	 * 
 	 */
 	public String getIncludableReplacement(String idStr, String viewIdStr,
 			String modeStr) {
 		// lookup the object at this id
 		OTObject referencedObject = getReferencedObject(idStr);
 		if (referencedObject == null) {
 			return "$0";
 		}
 
 		OTViewEntry viewEntry = null;
 		if (viewIdStr != null) {
 			OTObject viewEntryTmp = getReferencedObject(viewIdStr);
 			if (viewEntryTmp instanceof OTViewEntry) {
 				viewEntry = (OTViewEntry) viewEntryTmp;
 			} else {
 				System.err.println("viewid reference to a non viewEntry object");
 				System.err.println("  doc: " + pfObject.getGlobalId());
 				System.err.println("  refid: " + idStr);
 				System.err.println("  viewid: " + viewIdStr);
 			}
 
 		}
 
 		OTView view = null;
 
 		String viewMode = getViewMode();
 		if (modeStr != null) {
 			if (modeStr.length() == 0) {
 				viewMode = null;
 			} else {
 				viewMode = modeStr;
 			}
 		}
 
 		if (viewEntry != null) {
 			view = getViewFactory().getView(referencedObject, viewEntry,
 					viewMode);
 		} else {
 			view = getViewFactory().getView(referencedObject,
 					OTXHTMLView.class, viewMode);
 			// if there isnt' a xhtml view and there is a view mode, then see if
 			// that viewMode of the jcomponent view is an xhtmlview.
 			// this logic is really contorted. The mixing of viewmodes and
 			// renderings needs to be well defined.
 			if (view == null && getViewMode() != null) {
 				view = getViewFactory().getView(referencedObject,
 						OTJComponentView.class, viewMode);
 			}
 		}
 
 		if (view instanceof OTXHTMLView) {
 			OTXHTMLView xhtmlView = (OTXHTMLView) view;
 			try {
 				String replacement = xhtmlView.getXHTMLText(referencedObject);
 				if (replacement == null) {
 					// this is an empty embedded object
 					System.err.println("empty embedd obj: " + idStr);
 					return "";
 				}
 				return OTrunkUtil.escapeReplacement(replacement);
 			} catch (Exception e) {
 				System.err
 						.println("Failed to generate xhtml version of embedded object");
 				e.printStackTrace();
 			}
 		}
 
 		return "$0";
 	}
 
 	public String substituteIncludables(String inText) {
 		if (inText == null) {
 			return null;
 		}
 
 		Pattern p = Pattern.compile("<object refid=\"([^\"]*)\"[^>]*>");
 		Pattern pViewId = Pattern.compile("viewid=\"([^\"]*)\"");
 		Pattern pMode = Pattern.compile("mode=\"([^\"]*)\"");
 		Matcher m = p.matcher(inText);
 		StringBuffer parsed = new StringBuffer();
 		while (m.find()) {
 			String id = m.group(1);
 
 			String element = m.group(0);
 			Matcher mViewId = pViewId.matcher(element);
 			String viewIdStr = null;
 			if (mViewId.find()) {
 				viewIdStr = mViewId.group(1);
 			}
 
 			Matcher mMode = pMode.matcher(element);
 			String modeStr = null;
 			if (mMode.find()) {
 				modeStr = mMode.group(1);
 			}
 
 			String replacement = getIncludableReplacement(id, viewIdStr,
 					modeStr);
 
 			try {
 				m.appendReplacement(parsed, replacement);
 			} catch (IllegalArgumentException e) {
 				System.err.println("bad replacement: " + replacement);
 				e.printStackTrace();
 			}
 		}
 		m.appendTail(parsed);
 		return parsed.toString();
 	}
 
 	public String htmlizeText(String inText) {
 		if (inText == null) {
 			return null;
 		}
 
 		inText = inText.replaceAll("<p[ ]*/>", "<p></p>");
 		return inText.replaceAll("<([^>]*)/>", "<$1>");
 
 		/*
 		 * Pattern p = Pattern.compile("<([^>]*)/>"); Matcher m =
 		 * p.matcher(inText); StringBuffer parsed = new StringBuffer();
 		 * while(m.find()) { String tagBody = m.group(1);
 		 *  // We need 6 backslashes because // first the java compiler strips
 		 * off half of them so the sting // becomes: \\\$ // then the replacer
 		 * uses the backslash as a quote, and the $ // character is used to
 		 * reference groups of characters, so it // must be escaped. So the 1st
 		 * two are turned into one, and the // 3rd one escapes the $. So the end
 		 * result is: // \$ // We need this \$ because the replacement below is
 		 * going to // parse the $ otherwise tagBody = tagBody.replaceAll("\\$",
 		 * "\\\\\\$"); try { m.appendReplacement(parsed, "<$1>" + tagBody +
 		 * ">"); } catch (IllegalArgumentException e) { System.err.println("bad
 		 * tag: " + tagBody); e.printStackTrace(); } } m.appendTail(parsed);
 		 * return parsed.toString();
 		 */
 	}
 	
 	// FIXME: Does this do anything anymore? Nothing seems to call it. -SF
 	public Document parseString(String text, String systemId) {
 		try {
 			if (xmlDocumentFactory == null) {
 				xmlDocumentFactory = DocumentBuilderFactory.newInstance();
 				xmlDocumentFactory.setValidating(true);
 				xmlDocumentBuilder = xmlDocumentFactory.newDocumentBuilder();
 
 				// TODO Fix this
 				xmlDocumentBuilder.setErrorHandler(new DefaultHandler());
 			}
 			
 			StringReader stringReader = new StringReader(text);
 			InputSource inputSource = new InputSource(stringReader);
 			inputSource.setSystemId(systemId);
 			return xmlDocumentBuilder.parse(inputSource);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
 	 */
 	public void stateChanged(ChangeEvent event) {
 		// System.out.println(this+" -- TABS stateChanged");
 
 		updateFormatedView();
 	}
 
 	public void hyperlinkUpdate(HyperlinkEvent e) {
 
 		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 			try {
 				String linkTarget = e.getDescription();
 				if (linkTarget.startsWith("http")
 						|| linkTarget.startsWith("file")) {
 					try {
 						// FIXME this should be changed to be a service
 						// so external links can work in both a jnlp
 						// env and a regular application env
 						Class serviceManager = Class
 								.forName("javax.jnlp.ServiceManager");
 						Method lookupMethod = serviceManager.getMethod(
 								"lookup", new Class[] { String.class });
 						Object basicService = lookupMethod.invoke(null,
 								new Object[] { "javax.jnlp.BasicService" });
 						Method showDocument = basicService.getClass()
 								.getMethod("showDocument",
 										new Class[] { URL.class });
 						showDocument.invoke(basicService,
 								new Object[] { new URL(linkTarget) });
 						return;
 					} catch (Exception exp) {
 						System.err.println("Can't open external link.");
 						exp.printStackTrace();
 					}
 				}
 
 				OTObject linkObj = getReferencedObject(linkTarget);
 				if (linkObj == null) {
 					System.err.println("Invalid link: " + e.getDescription());
 					return;
 				}
 
 				Element aElement = e.getSourceElement();
 				AttributeSet attribs = aElement.getAttributes();
 
 				// this is a hack because i don't really know what is going on
 				// here
 				AttributeSet tagAttribs = (AttributeSet) attribs
 						.getAttribute(HTML.Tag.A);
 				String target = (String) tagAttribs
 						.getAttribute(HTML.Attribute.TARGET);
 				String viewEntryId = (String) tagAttribs.getAttribute("viewid");
 				String modeStr = (String) tagAttribs.getAttribute("mode");
 
 				if (target == null) {
 					getViewContainer().setCurrentObject(linkObj);
 
 				} else {
 					// they want to use a frame
 					OTFrame targetFrame = null;
 
 					// get the frame object
 					// modify setCurrentObject to take a frame object
 					// then at the top level view container deal with this
 					// object
 					targetFrame = (OTFrame) getReferencedObject(target);
 
 					OTViewEntry viewEntry = null;
 					if (viewEntryId != null) {
 						viewEntry = (OTViewEntry) getReferencedObject(viewEntryId);
 					}
 
 					if (targetFrame == null) {
 						System.err.println("Invalid link target attrib: "
 								+ target);
 						return;
 					}
 
 					if (modeStr != null && modeStr.length() == 0) {
 						modeStr = null;
 					} else if (modeStr == null) {
 						modeStr = getViewMode();
 					}
 					
 					getFrameManager().putObjectInFrame(linkObj, viewEntry,
 							targetFrame, modeStr);
 				}
 
 			} catch (Throwable t) {
 				t.printStackTrace();
 			}
 		}
 	}
 
 	public String getXHTMLText(OTObject otObject) {
 		if (otObject == null) {
 			throw new IllegalArgumentException("otObject can't be null");
 		}
 		pfObject = (OTDocument) otObject;
 		
 	//	return updateFormatedView().replace(viewEntry.getCss(), "");
 		
 		String bodyText = pfObject.getDocumentText();
 		bodyText = substituteIncludables(bodyText);
 		
 		return bodyText;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.concord.framework.otrunk.view.OTViewConfigAware#setViewConfig(org.concord.framework.otrunk.OTObject)
 	 */
 	public void setViewEntry(OTViewEntry viewEntry) {
 		super.setViewEntry(viewEntry);
 		if (viewEntry instanceof OTDocumentViewConfig) {
 			this.viewEntry = (OTDocumentViewConfig) viewEntry;
 			setViewMode(this.viewEntry.getMode());
 		}
 	}
 
 }
