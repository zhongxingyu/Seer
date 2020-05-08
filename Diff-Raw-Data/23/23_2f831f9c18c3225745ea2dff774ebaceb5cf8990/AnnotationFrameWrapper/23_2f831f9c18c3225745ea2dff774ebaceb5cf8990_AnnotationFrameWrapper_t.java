 package org.mindinformatics.gwt.domeo.client.ui.content;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.mindinformatics.gwt.domeo.client.Domeo;
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.actions.IAnnotationEditListener;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.forms.images.ImageAnnotationFormsPanel;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.forms.multipletargets.MultipleTargetsAnnotationFormsPanel;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.forms.relationships.RelationshipAnnotationFormsPanel;
 import org.mindinformatics.gwt.domeo.client.ui.annotation.forms.text.TextAnnotationFormsPanel;
 import org.mindinformatics.gwt.domeo.client.ui.popup.CurationPopup;
 import org.mindinformatics.gwt.domeo.model.AnnotationFactory;
 import org.mindinformatics.gwt.domeo.model.MAnnotation;
 import org.mindinformatics.gwt.domeo.model.MAnnotationCitationReference;
 import org.mindinformatics.gwt.domeo.model.MAnnotationSet;
 import org.mindinformatics.gwt.domeo.model.MOnlineImage;
 import org.mindinformatics.gwt.domeo.model.buffers.HighlightedTextBuffer;
 import org.mindinformatics.gwt.domeo.model.persistence.AnnotationPersistenceManager;
 import org.mindinformatics.gwt.domeo.model.selectors.MSelector;
 import org.mindinformatics.gwt.domeo.model.selectors.MTextQuoteSelector;
 import org.mindinformatics.gwt.domeo.model.selectors.SelectorUtils;
 import org.mindinformatics.gwt.domeo.plugins.annotation.selection.model.MSelectionAnnotation;
 import org.mindinformatics.gwt.framework.component.preferences.src.BooleanPreference;
 import org.mindinformatics.gwt.framework.component.resources.model.MGenericResource;
 import org.mindinformatics.gwt.framework.component.styles.src.StylesManager;
 import org.mindinformatics.gwt.framework.component.ui.glass.EnhancedGlassPanel;
 import org.mindinformatics.gwt.framework.src.Application;
 import org.mindinformatics.gwt.framework.src.ApplicationResources;
 import org.mindinformatics.gwt.utils.src.HtmlTraversalUtils;
 import org.mindinformatics.gwt.utils.src.HtmlUtils;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.IFrameElement;
 import com.google.gwt.dom.client.Node;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 
 /**
  * 
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class AnnotationFrameWrapper implements IAnnotationEditListener {
 
 	// Components wrapping
 	public static final String PREF_WRAP_TABLES = "Table components wrapping";
 	public static final String PREF_WRAP_IMAGES = "Image components wrapping";
 	public static final String PREF_WRAP_LINKS = "Document components wrapping";
 	
 	public static final Integer MAX_CHARS_AROUND_MATCH = 254;
 
 	public static final String LOG_CATEGORY_TEXT_SELECTION = "TEXT SELECTION";
 	public static final String LOG_CATEGORY_TEXT_BUFFERING = "TEXT BUFFERING";
 	public static final String LOG_CATEGORY_TEXT_BUFFERED = "TEXT BUFFERED";
 	public static final String LOG_CATEGORY_TEXT_HIGHLIGHT = "TEXT HIGHLIGHT";
 	public static final String LOG_CATEGORY_TEXT_HIGHLIGHTED = "TEXT HIGHLIGHTED";
 	public static final String LOG_CATEGORY_TEXT_ANNOTATION = "TEXT ANNOTATION";
 	public static final String LOG_CATEGORY_TEXT_ANNOTATED = "TEXT ANNOTATED";
 	
 	public static final String LOG_CATEGORY_SHOW_ANNOTATION = "SHOW ANNOTATION";
 	public static final String LOG_CATEGORY_EDIT_ANNOTATION = "EDIT ANNOTATION";
 	public static final String LOG_CATEGORY_DELETE_ANNOTATION = "DELETE ANNOTATION";
 	public static final String LOG_CATEGORY_ANNOTATION_UPDATED = "ANNOTATION UPDATED";
 	public static final String LOG_CATEGORY_ANNOTATION_VERSION = "ANNOTATION VERSION";
 	public static final String LOG_CATEGORY_ANNOTATION_DELETED = "ANNOTATION DELETED";
 	public static final String LOG_CATEGORY_COMMENT_ANNOTATION = "COMMENT ANNOTATION";
 	
 	public static final String LOG_CATEGORY_TARGETS_ANNOTATION = "TARGETS ANNOTATION";
 	
 	public static final String LOG_CATEGORY_ANNOTATION_FAILED = "ANNOTATION FAILED";
 	public static final String LOG_CATEGORY_BUFFERING_FAILED = "BUFFERING FAILED";
 	
 	
 	Frame _frame;
 	String currentUrl = "";
 	String _documentTitle;
 	HashMap<String, String> _documentMetadata;
 
 	
 	private IDomeo _domeo;
 	private ApplicationResources _resources;
 	
 	public boolean isSelectionCollapsed;
 	public Object anchorParentElement;
 	public Object focusParentElement;
 	public Object anchorNode;
 	public int anchorOffset;
 	public Object focusNode;
 	public int focusOffset;
 	public int rangeCount;
 	public String matchText;
 	
 	private CurationPopup _curationPopup;
 	
 	private DocumentElementWrapper wrapper;
 	private HandlerRegistration registration;
 	
 	public AnnotationFrameWrapper(IDomeo domeo, Frame frame) {
 		
 		_domeo = domeo;
 		_resources = Application.applicationResources;
 		_frame = frame;
 		
 		if(!HtmlUtils.getUserAgent().contains("msie")) {
 			hookEventsNotIE(frame.getElement(), 
 					((BooleanPreference)domeo.getPreferences().getPreferenceItem(Domeo.class.getName(), PREF_WRAP_TABLES)).getValue(),
 					((BooleanPreference)domeo.getPreferences().getPreferenceItem(Domeo.class.getName(), PREF_WRAP_IMAGES)).getValue(),
 					((BooleanPreference)domeo.getPreferences().getPreferenceItem(Domeo.class.getName(), PREF_WRAP_LINKS)).getValue());
 		} else {
 			hookEventsIE(frame.getElement());
 		}
 	}
 
 	public native void getSelectionText(AnnotationFrameWrapper x, Document frameDocument) /*-{
 		var txt = '';
 		
 		if(frameDocument.getSelection && frameDocument.getSelection().anchorNode!=null){ // Chrome
 			txt = frameDocument.getSelection();
 			// https://developer.mozilla.org/en/DOM/Selection
 			// https://developer.mozilla.org/En/DOM/Text
 	
 			if(txt == null || txt.anchorNode == null) {
 				x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::resetSelection(Ljava/lang/String;)("");
 				return;
 			} 
 	
 			x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::isSelectionCollapsed = txt.isCollapsed;
 			x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::matchText = txt.toString();
 			x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::anchorNode = txt.anchorNode;
 			x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::anchorOffset = txt.anchorOffset;
 			x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::focusNode = txt.focusNode;
 			x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::focusOffset = txt.focusOffset;
 			x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::rangeCount = txt.rangeCount;
 			x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::anchorParentElement = txt.anchorNode.parentNode;
 			x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::focusParentElement = txt.focusNode.parentNode;
 		} 
 		//else if ($doc.selection.createRange) { // Internet Explorer
         //   txt = $doc.selection.createRange();
         //    alert ("Internet Explorer " + range.text);
         //} 
         else if ($wnd.getSelection() && $wnd.getSelection().anchorNode) { // Firefox
 			try {
 				txt =  $doc.getElementById("domeoframe").contentWindow.window.getSelection();
 				if(txt == null || txt.anchorNode == null) {
 					x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::resetSelection(Ljava/lang/String;)("");
 					return;
 				}
 				x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::isSelectionCollapsed = txt.isCollapsed;
 				x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::matchText = txt.toString();
 				x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::anchorNode = txt.anchorNode;
 				x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::anchorOffset = txt.anchorOffset;
 				x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::focusNode = txt.focusNode;
 				x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::focusOffset = txt.focusOffset;
 				x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::rangeCount = txt.rangeCount;
 				x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::anchorParentElement = txt.anchorNode.parentNode;
 				x.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::focusParentElement = txt.focusNode.parentNode;
 			} catch(e) {
 				alert(e);
 			}
 		} 
 	}-*/;
 	
 	public void resetSelection(String dummy) {
 		isSelectionCollapsed = true;
 		matchText = null;
 		anchorNode = null;
 		anchorOffset = 0;
 		focusNode = null;
 		focusOffset = 0;
 		rangeCount = 0;
 		anchorParentElement = null;
 		focusParentElement = null;
 	}
 	
 	public void clearSelection() {
 		IFrameElement iframe = IFrameElement.as(this.getFrame().getElement());
 		final Document frameDocument = iframe.getContentDocument();
 		clrSelections(frameDocument);
 	}
 	
 	public void clearSelection(Document frameDocument) {
 		clrSelections(frameDocument);
 	}
 	
 	public native void clrSelections(Document frameDocument) /*-{
 		if ($wnd.getSelection) {
 			$doc.getElementById($wnd.FRAME_ID).contentWindow.window.getSelection().removeAllRanges();
 		} else if(frameDocument.getSelection) {
 			frameDocument.getSelection().removeAllRanges();
 		} else if(frameDocument.selection){
 			frameDocument.selection.clear();
 		}
 	}-*/;
 	
 	public void resetSelection() {
 		anchorParentElement = null;
 		focusParentElement = null;
 		anchorNode = null;
 		anchorOffset = -1;
 		focusNode = null;
 		focusOffset = -1;
 		rangeCount = -1;
 		matchText = null;
 	}
 	
 	private native int compareNodesOrder(Node node1, Node node2)
 	/*-{
 		return $wnd.compareNodesOrder(node1, node2)
 	}-*/;
 	
 	public void logSelection(String prefix, String matchText, String suffix) {
 		_domeo.getLogger().command(LOG_CATEGORY_TEXT_SELECTION, AnnotationFrameWrapper.class.getName(), 
 				prefix + " $ " + matchText + " $ " + suffix);
 	}
 	
 	public String trimPrefix(String prefix) {
 		if(prefix.length()>MAX_CHARS_AROUND_MATCH)
 			return prefix.substring(prefix.length()-MAX_CHARS_AROUND_MATCH);
 		else return prefix;
 	}
 	
 	public String trimSuffix(String suffix) {
 		if(suffix.length()>MAX_CHARS_AROUND_MATCH)
 			return suffix.substring(0, MAX_CHARS_AROUND_MATCH);
 		else return suffix;
 	}
 	
 	public void registerHighlightHandlers() {
 		try {
 			if (registration != null) {
 				registration.removeHandler();
 				_domeo.getLogger().debug(AnnotationFrameWrapper.class.getName(), 
 					"Removed existing Highlight Handlers");
 			}
 
 			IFrameElement iframe = IFrameElement.as(_frame.getElement());
 			final Document frameDocument = iframe.getContentDocument();
 			wrapper = new DocumentElementWrapper((Element) frameDocument.getDocumentElement());
 			
 			final AnnotationFrameWrapper _this = this;
 
 			MouseUpHandler mouseUpHandler = new MouseUpHandler() {
 				Document doc = frameDocument;
 	
 				public void onMouseUp(MouseUpEvent event) {
 					// Checks if a popup is already displaying
 					if (_curationPopup != null) {
 						_curationPopup.hide();
 						_curationPopup = null;
 					}
 					
 					// From here annotation management
 					getSelectionText(_this, doc);
 			
 /*					if(isSelectionCollapsed) 
 					Window.alert(""+anchorNode);
 */					
 					// If not in annotation mode it displays cards
 					if(isSelectionCollapsed && !_domeo.isManualAnnotationEnabled() && !_domeo.isManualHighlightEnabled() && !_domeo.isManualClipAnnotationEnabled()) {
 						Element linkElement = null;
 						boolean linkflag = false;
 						if(Element.is(event.getNativeEvent().getEventTarget())) {
 							Element currentElement = (Element) Element.as(event.getNativeEvent().getEventTarget());
 							
 							// Management of the links in that click area
 							if(currentElement.getTagName().toLowerCase().equals("a")) {
 								while((currentElement.getAttribute("href")!=null && currentElement.getAttribute("href").length()>4) || 
 										HtmlUtils.getAnnotationIdAttribute(currentElement)!=null) {
 									if(linkflag) {
 										if(HtmlUtils.getAnnotationIdAttribute(currentElement)==null) {
 											break;
 										} else {
 											if(HtmlUtils.getAnnotationIdAttribute(currentElement.getParentElement())!=null) 
 												currentElement = (Element) currentElement.getParentElement();
 											else 
 												break;
 										}
 									} else {
 										if((currentElement.getAttribute("href")!=null && currentElement.getAttribute("href").length()>4)) {
 											linkElement = currentElement;
 											linkflag = true;
 											if(HtmlUtils.getAnnotationIdAttribute(currentElement.getParentElement())!=null) 
 												currentElement = (Element) currentElement.getParentElement();
 											else
 												break;
 										} else if(HtmlUtils.getAnnotationIdAttribute(currentElement)==null) {
 											break;
 										} else {
 											if(HtmlUtils.getAnnotationIdAttribute(currentElement.getParentElement())!=null ||
 												(currentElement.getParentElement().getAttribute("href")!=null && currentElement.getParentElement().getAttribute("href").length()>4)) 
 												currentElement = (Element) currentElement.getParentElement();
 											else 
 												break;
 										} 
 									}
 								}
 							}	
 							
 							// Management of the annotations in that click area
 							ArrayList<String> elementsIds = new ArrayList<String>();
 							ArrayList<MAnnotation> items = new ArrayList<MAnnotation>();
 							ArrayList<Element> spans = new ArrayList<Element>();
 							
 							if(currentElement.getTagName().toLowerCase().equals("img")) {
 								ArrayList<MAnnotation>  anns = _domeo.getAnnotationPersistenceManager().getAllAnnotationsForImageElement(currentElement);
 								for(MAnnotation ann: anns) {
 									elementsIds.add(HtmlUtils.getImageIdAttribute(currentElement));
 									items.add(ann);
 									spans.add(currentElement);
 								}	
 							} else {
 								HtmlUtils.getOnClickAnnotationElements(currentElement, spans, 5);
 								//ArrayList<Node> nodes = new ArrayList<Node>();
 								//HtmlUtils.getOnClickAnnotationElements(currentElement, nodes);
 								for(Element element: spans) {
 									//Element element = (Element) n;
 									String idString = HtmlUtils.getAnnotationIdAttribute(element);
 									if(idString!=null) {
 										if(idString.indexOf(":")>0) idString = idString.substring(0, idString.indexOf(":"));
 										elementsIds.add(HtmlUtils.getAnnotationIdAttribute(element));
 										//spans.add(element);
 										MAnnotation annotation = _domeo.getAnnotationPersistenceManager().getAnnotationByLocalId(Long.parseLong(idString));
 										
 										if(annotation==null) {
 											List<MAnnotation> anns = _domeo.getAnnotationPersistenceManager().getBibliographicSet().getAnnotations();
 											for(MAnnotation ann: anns) {
 												if(ann.getLocalId()==Long.parseLong(idString)) {
 													items.add(ann);
 													break;
 												}
 											}
 										} else {
 											items.add(annotation);
 										}
 									}
 								}
 							}
 							
 							
 							// Calculate room on the right side of the click 
 							int x = event.getClientX();
 							int y = event.getClientY();
 							int dx = Window.getClientWidth()-x;
 							int maxHeight = Window.getClientHeight()-y;
 							
 							// Adjustments for when the box is close to the right border
 							if(dx>400) {
 								y = y + _frame.getAbsoluteTop() + 8;
 							} else {
 								x = x-400;
 								y = y + _frame.getAbsoluteTop() + 8;
 							}
 							
 							_curationPopup = new CurationPopup(_domeo);
 							_curationPopup.initialize();
 							_curationPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
 								public void onClose(CloseEvent<PopupPanel> event) {
 								}
 							});
 							
 							if(linkElement!=null && elementsIds.size()==0) {
 								_curationPopup.showLink(linkElement, x, y);
 							} else if(linkElement==null && elementsIds.size()>0) {
 								_curationPopup.showAnnotation(items, spans, x, y, maxHeight);
 							} else if(linkElement!=null && elementsIds.size()>0) {
 								_curationPopup.show(linkElement, items, spans, x, y, maxHeight);
 							}
 						}					
 						return; 
 					} else if(anchorNode!=null && !_domeo.isManualAnnotationEnabled() && !_domeo.isManualHighlightEnabled() && !_domeo.isManualClipAnnotationEnabled()){		
 						return;
 					}
 
 
 					if (anchorNode==null || focusNode==null) return;
 					
 					if (isSelectionCollapsed) {
 						if(_domeo.isManualAnnotationEnabled()) {
 							// Management of entities for relationships
 							ArrayList<String> elementsIds = new ArrayList<String>();
 							ArrayList<MAnnotation> items = new ArrayList<MAnnotation>();
 							ArrayList<Element> spans = new ArrayList<Element>();
 							if(Element.is(event.getNativeEvent().getEventTarget())) {
 								Element currentElement = (Element) Element.as(event.getNativeEvent().getEventTarget());
 								
 								HtmlUtils.getOnClickAnnotationElements(currentElement, spans, 5);
 								//Window.alert("spans: " + spans.size());
 								//ArrayList<Node> nodes = new ArrayList<Node>();
 								//HtmlUtils.getOnClickAnnotationElements(currentElement, nodes);
 								for(Element element: spans) {
 									//Element element = (Element) n;
 									String idString = HtmlUtils.getAnnotationIdAttribute(element);
 									if(idString!=null) {
 										if(idString.indexOf(":")>0) idString = idString.substring(0, idString.indexOf(":"));
 										elementsIds.add(HtmlUtils.getAnnotationIdAttribute(element));
 										//spans.add(element);
 										MAnnotation annotation = _domeo.getAnnotationPersistenceManager().getAnnotationByLocalId(Long.parseLong(idString));
 										if(annotation==null) { // References
 											List<MAnnotation> anns = _domeo.getAnnotationPersistenceManager().getBibliographicSet().getAnnotations();
 											for(MAnnotation ann: anns) {
 												// TODO Qualifier Annotation 
 												if(ann.getLocalId()==Long.parseLong(idString)) {
 													Window.alert(""+ann.getLocalId());
 													items.add(ann);
 													break;
 												}
 											}
 										} else { // Others
 											
 											items.add(annotation);
 										}
 									}
 								}
 							}
 						
 							// If there are potential subject for creating structured relationships
 							// between annotation item, the method is called
 							if(items.size()>0) {
 								//performAnnotation(items);
 							}
 							
 							resetSelection();
 							return;
 						} /*if(_domeo.isManualClipAnnotationEnabled()) {
 							Window.alert("Add annotation item to clipboard");
 						}*/
 						// This is necessary to allow double click selection for a single word
 						else return;
 					}
 					
 					if (anchorNode == focusNode) {
 						int start = 0, stop = 0;
 						if (anchorOffset > focusOffset) {
 							start = focusOffset;
 							stop = anchorOffset;
 						} else {
 							start = anchorOffset;
 							stop = focusOffset;
 						}
 						
 						String prefix = ((Node) anchorNode).getNodeValue().toString().substring(0, start);
 						String suffix = ((Node) anchorNode).getNodeValue().toString().substring(stop, ((Node) anchorNode).getNodeValue() .toString().length());
 						
 						try {
 						    _domeo.getLogger().debug(this, "Before calibration(1): "+prefix +  matchText + suffix);
 							if(isWeakSelection("Weak selection (1)", prefix, matchText, suffix)) {
 								// Selection enhancement strategy
 								prefix =  selectionPrefixEnhancement((Node)anchorNode, (Node)focusNode) + ((Node) anchorNode).getNodeValue().toString().substring(0, start);
 								if(isWeakSelection("Weak selection (1b)", prefix, matchText, suffix)) {
 									suffix = ((Node) anchorNode).getNodeValue().toString().substring(stop) + selectionPostfixEnhancement((Node)anchorNode, (Node)focusNode);
 								}
 							}
 							_domeo.getLogger().debug(this, "After calibration(1): "+prefix +  matchText + suffix);
 						} catch(Exception e) {
 							_domeo.getLogger().warn(this, "SELECTION ENHANCEMENT", e.getMessage());
 						}
 						
 						prefix = trimPrefix(prefix);
 						suffix = trimSuffix(suffix);
 						logSelection(prefix, matchText, suffix);
 						
 						/*
 						ArrayList<AnnotationTermDTO> items = displayIncludedSpans(
 								matchText, 
 								prefix,
 								suffix,
 								(Element) anchorNode);
 	
 						new AnnotationManagementGlassPanel(ann, resources,
 								new AnnotationManagementPanel(ann, resources,
 										matchText, 
 										prefix,
 										suffix,
 										(Node) anchorNode, items));
 										*/
 						
 						clearSelection(doc);
 						
 						if (matchText.length()>0 && _domeo.isManualClipAnnotationEnabled()) 
 							addToClipboard(matchText, prefix, suffix, (Node) anchorNode);
 						else
 							performAnnotation(matchText, prefix, suffix, (Node) anchorNode);
 
 					} else {
 						int start, stop = 0;
 						String prefix, suffix;
 	
 						List<Node> nodes = new ArrayList<Node>();
 						if (compareNodesOrder((Node) anchorNode, (Node) focusNode) == 1) {
 							//Window.alert(((Element) anchorNode).getInnerHTML() + " - " + ((Element) focusNode).getInnerHTML());
 							start = focusOffset;
 							prefix = ((Node) focusNode).getNodeValue().toString()
 									.substring(0, start);
 							stop = anchorOffset;
 							suffix = (stop >= ((Node) anchorNode).getNodeValue()
 									.toString().length()) ? ""
 									: ((Node) anchorNode)
 											.getNodeValue()
 											.toString()
 											.substring(
 													stop,
 													((Node) anchorNode)
 															.getNodeValue()
 															.toString().length() - 1);
 	
 							nodes.add((Node) focusNode);
 							nodes.add((Node) anchorNode);
 						} else {
 							// Returns if elements like wrapped tables or images
 							if(((Node) anchorNode).getNodeValue()==null) return;
 							
 							start = anchorOffset;
 							prefix = ((Node) anchorNode).getNodeValue().toString()
 									.substring(0, anchorOffset);
 							stop = focusOffset;
 							suffix = (focusOffset >= ((Node) focusNode)
 									.getNodeValue().toString().length()) ? ""
 									: ((Node) focusNode)
 											.getNodeValue()
 											.toString()
 											.substring(
 													focusOffset,
 													((Node) focusNode)
 															.getNodeValue()
 															.toString().length() - 1);
 	
 							nodes.add((Node) anchorNode);
 							nodes.add((Node) focusNode);
 						}
 	
 						prefix = trimPrefix(prefix);
 						suffix = trimSuffix(suffix);
 						logSelection(prefix, matchText, suffix);
 						
 						try {
 						    _domeo.getLogger().warn(this, "Before calibration(2): "+prefix +  matchText + suffix);
 							if(isWeakSelection("Weak selection (2)", prefix, matchText, suffix)) {
 								// Selection enhancement strategy
 								
 								if (compareNodesOrder((Node) anchorNode, (Node) focusNode) == 1) {
 									_domeo.getLogger().debug(this, ((Node) focusNode).getNodeValue().toString());
 									prefix =  selectionPrefixEnhancement((Node)anchorNode, (Node)focusNode) + ((Node) focusNode).getNodeValue().toString().substring(0, start);
 								} else {
 									_domeo.getLogger().debug(this, ((Node) anchorNode).getNodeValue().toString());
 									prefix =  selectionPrefixEnhancement((Node)anchorNode, (Node)focusNode) + ((Node) anchorNode).getNodeValue().toString().substring(0, start);
 								}
 								
 								if(isWeakSelection("Weak selection (2b)", prefix, matchText, suffix)) {
 									if (compareNodesOrder((Node) anchorNode, (Node) focusNode) == 1) {
 										suffix = ((Node) anchorNode).getNodeValue().toString().substring(stop) + selectionPostfixEnhancement((Node)anchorNode, (Node)focusNode);
 									} else {
 										suffix = ((Node) focusNode).getNodeValue().toString().substring(stop) + selectionPostfixEnhancement((Node)anchorNode, (Node)focusNode);
 									}
 								}
 							}
 							 _domeo.getLogger().warn(this, "After calibration(2): "+prefix +  matchText + suffix);
 						} catch(Exception e) {
 							_domeo.getLogger().warn(this, "SELECTION ENHANCEMENT", e.getMessage());
 						}
 	
 						/*
 						ArrayList<AnnotationTermDTO> items = displayIncludedSpans(
 								matchText, prefix, suffix, (Element) HtmlUtils
 										.getCommonParent(nodes), nodes);
 						
 						new AnnotationManagementGlassPanel(ann, resources,
 								new AnnotationManagementPanel(ann, resources,
 										matchText, prefix, suffix, (Node) HtmlUtils
 												.getCommonParent(nodes), items));
 												*/
 						
 						clearSelection(doc);
 						
 						if (matchText.length()>0 && _domeo.isManualClipAnnotationEnabled()) 
 							addToClipboard(matchText, prefix, suffix, (Node) anchorNode);
 						else
 							performAnnotation(matchText, prefix, suffix, (Node) anchorNode);
 					}
 				}
 			};
 			
 			_domeo.getLogger().debug(AnnotationFrameWrapper.class.getName(), 
 				"Registering Highlight Handlers");
 			registration = wrapper.addMouseUpHandler(mouseUpHandler);
 			/*
 			wrapper.addClickHandler(new ClickHandler() {
 	
 				@Override
 				public void onClick(ClickEvent event) {
 					Window.alert("blah");
 				}
 				
 			});
 			*/
 			wrapper.onAttach();
 			wrapper.onLoad();
 			
 			_domeo.getLogger().debug(AnnotationFrameWrapper.class.getName(), 
 				"Registered Highlight Handlers");
 			
 			_domeo.getLogger().info(this, "Content type: " + content(frameDocument));
 						
 		} catch (Exception e) {
 			Window.alert(e.getMessage());
 			_domeo.getLogger().exception(AnnotationFrameWrapper.class.getName(), 
 				"Problems in registering highlight handlers " + e.getMessage());
 		}
 	}
 	
 	public void annotate() {
 		if (isSelectionCollapsed) return;
 		
 		IFrameElement iframe = IFrameElement.as(_frame.getElement());
 		final Document frameDocument = iframe.getContentDocument();
 		Document doc = frameDocument;
 		
 //		if (isSelectionCollapsed) {
 //			if(_domeo.isManualAnnotationEnabled()) {
 //				// Management of entities for relationships
 //				ArrayList<String> elementsIds = new ArrayList<String>();
 //				ArrayList<MAnnotation> items = new ArrayList<MAnnotation>();
 //				ArrayList<Element> spans = new ArrayList<Element>();
 //				if(Element.is(event.getNativeEvent().getEventTarget())) {
 //					Element currentElement = (Element) Element.as(event.getNativeEvent().getEventTarget());
 //					
 //					HtmlUtils.getOnClickAnnotationElements(currentElement, spans, 5);
 //					//Window.alert("spans: " + spans.size());
 //					//ArrayList<Node> nodes = new ArrayList<Node>();
 //					//HtmlUtils.getOnClickAnnotationElements(currentElement, nodes);
 //					for(Element element: spans) {
 //						//Element element = (Element) n;
 //						String idString = HtmlUtils.getAnnotationIdAttribute(element);
 //						if(idString!=null) {
 //							if(idString.indexOf(":")>0) idString = idString.substring(0, idString.indexOf(":"));
 //							elementsIds.add(HtmlUtils.getAnnotationIdAttribute(element));
 //							//spans.add(element);
 //							MAnnotation annotation = _domeo.getAnnotationPersistenceManager().getAnnotationByLocalId(Long.parseLong(idString));
 //							if(annotation==null) { // References
 //								List<MAnnotation> anns = _domeo.getAnnotationPersistenceManager().getBibliographicSet().getAnnotations();
 //								for(MAnnotation ann: anns) {
 //									// TODO Qualifier Annotation 
 //									if(ann.getLocalId()==Long.parseLong(idString)) {
 //										Window.alert(""+ann.getLocalId());
 //										items.add(ann);
 //										break;
 //									}
 //								}
 //							} else { // Others
 //								
 //								items.add(annotation);
 //							}
 //						}
 //					}
 //				}
 //			
 //				// If there are potential subject for creating structured relationships
 //				// between annotation item, the method is called
 //				if(items.size()>0) {
 //					performAnnotation(items);
 //				}
 //				
 //				resetSelection();
 //				return;
 //			} /*if(_domeo.isManualClipAnnotationEnabled()) {
 //				Window.alert("Add annotation item to clipboard");
 //			}*/
 //			// This is necessary to allow double click selection for a single word
 //			else return;
 //		}
 		
 		if (anchorNode == focusNode) {
 			int start = 0, stop = 0;
 			if (anchorOffset > focusOffset) {
 				start = focusOffset;
 				stop = anchorOffset;
 			} else {
 				start = anchorOffset;
 				stop = focusOffset;
 			}
 			
 			String prefix = ((Node) anchorNode).getNodeValue().toString().substring(0, start);
 			String suffix = ((Node) anchorNode).getNodeValue().toString().substring(stop, ((Node) anchorNode).getNodeValue() .toString().length());
 			
 			try {
 			    _domeo.getLogger().debug(this, "Before calibration(1): "+prefix +  matchText + suffix);
 				if(isWeakSelection("Weak selection (1)", prefix, matchText, suffix)) {
 					// Selection enhancement strategy
 					prefix =  selectionPrefixEnhancement((Node)anchorNode, (Node)focusNode) + ((Node) anchorNode).getNodeValue().toString().substring(0, start);
 					if(isWeakSelection("Weak selection (1b)", prefix, matchText, suffix)) {
 						suffix = ((Node) anchorNode).getNodeValue().toString().substring(stop) + selectionPostfixEnhancement((Node)anchorNode, (Node)focusNode);
 					}
 				}
 				_domeo.getLogger().debug(this, "After calibration(1): "+prefix +  matchText + suffix);
 			} catch(Exception e) {
 				_domeo.getLogger().warn(this, "SELECTION ENHANCEMENT", e.getMessage());
 			}
 			
 			prefix = trimPrefix(prefix);
 			suffix = trimSuffix(suffix);
 			logSelection(prefix, matchText, suffix);
 			
 			/*
 			ArrayList<AnnotationTermDTO> items = displayIncludedSpans(
 					matchText, 
 					prefix,
 					suffix,
 					(Element) anchorNode);
 
 			new AnnotationManagementGlassPanel(ann, resources,
 					new AnnotationManagementPanel(ann, resources,
 							matchText, 
 							prefix,
 							suffix,
 							(Node) anchorNode, items));
 							*/
 			
 			
 			clearSelection(doc);
 			
 			if (matchText.length()>0 && _domeo.isManualClipAnnotationEnabled()) 
 				addToClipboard(matchText, prefix, suffix, (Node) anchorNode);
 			else
 				performAnnotation(matchText, prefix, suffix, (Node) anchorNode);
 
 		} else {
 			int start, stop = 0;
 			String prefix, suffix;
 
 			List<Node> nodes = new ArrayList<Node>();
 			if (compareNodesOrder((Node) anchorNode, (Node) focusNode) == 1) {
 				//Window.alert(((Element) anchorNode).getInnerHTML() + " - " + ((Element) focusNode).getInnerHTML());
 				start = focusOffset;
 				prefix = ((Node) focusNode).getNodeValue().toString()
 						.substring(0, start);
 				stop = anchorOffset;
 				suffix = (stop >= ((Node) anchorNode).getNodeValue()
 						.toString().length()) ? ""
 						: ((Node) anchorNode)
 								.getNodeValue()
 								.toString()
 								.substring(
 										stop,
 										((Node) anchorNode)
 												.getNodeValue()
 												.toString().length() - 1);
 
 				nodes.add((Node) focusNode);
 				nodes.add((Node) anchorNode);
 			} else {
 				// Returns if elements like wrapped tables or images
 				if(((Node) anchorNode).getNodeValue()==null) return;
 				
 				start = anchorOffset;
 				prefix = ((Node) anchorNode).getNodeValue().toString()
 						.substring(0, anchorOffset);
 				stop = focusOffset;
 				suffix = (focusOffset >= ((Node) focusNode)
 						.getNodeValue().toString().length()) ? ""
 						: ((Node) focusNode)
 								.getNodeValue()
 								.toString()
 								.substring(
 										focusOffset,
 										((Node) focusNode)
 												.getNodeValue()
 												.toString().length() - 1);
 
 				nodes.add((Node) anchorNode);
 				nodes.add((Node) focusNode);
 			}
 
 			prefix = trimPrefix(prefix);
 			suffix = trimSuffix(suffix);
 			logSelection(prefix, matchText, suffix);
 			
 			try {
 			    _domeo.getLogger().warn(this, "Before calibration(2): "+prefix +  matchText + suffix);
 				if(isWeakSelection("Weak selection (2)", prefix, matchText, suffix)) {
 					// Selection enhancement strategy
 					
 					if (compareNodesOrder((Node) anchorNode, (Node) focusNode) == 1) {
 						_domeo.getLogger().debug(this, ((Node) focusNode).getNodeValue().toString());
 						prefix =  selectionPrefixEnhancement((Node)anchorNode, (Node)focusNode) + ((Node) focusNode).getNodeValue().toString().substring(0, start);
 					} else {
 						_domeo.getLogger().debug(this, ((Node) anchorNode).getNodeValue().toString());
 						prefix =  selectionPrefixEnhancement((Node)anchorNode, (Node)focusNode) + ((Node) anchorNode).getNodeValue().toString().substring(0, start);
 					}
 					
 					if(isWeakSelection("Weak selection (2b)", prefix, matchText, suffix)) {
 						if (compareNodesOrder((Node) anchorNode, (Node) focusNode) == 1) {
 							suffix = ((Node) anchorNode).getNodeValue().toString().substring(stop) + selectionPostfixEnhancement((Node)anchorNode, (Node)focusNode);
 						} else {
 							suffix = ((Node) focusNode).getNodeValue().toString().substring(stop) + selectionPostfixEnhancement((Node)anchorNode, (Node)focusNode);
 						}
 					}
 				}
 				 _domeo.getLogger().warn(this, "After calibration(2): "+prefix +  matchText + suffix);
 			} catch(Exception e) {
 				_domeo.getLogger().warn(this, "SELECTION ENHANCEMENT", e.getMessage());
 			}
 
 			/*
 			ArrayList<AnnotationTermDTO> items = displayIncludedSpans(
 					matchText, prefix, suffix, (Element) HtmlUtils
 							.getCommonParent(nodes), nodes);
 			
 			new AnnotationManagementGlassPanel(ann, resources,
 					new AnnotationManagementPanel(ann, resources,
 							matchText, prefix, suffix, (Node) HtmlUtils
 									.getCommonParent(nodes), items));
 									*/
 			
 			clearSelection(doc);
 			
 			if (matchText.length()>0 && _domeo.isManualClipAnnotationEnabled()) 
 				addToClipboard(matchText, prefix, suffix, (Node) anchorNode);
 			else
 				performAnnotation(matchText, prefix, suffix, (Node) anchorNode);
 		}
 	}
 	
 	public static native String content(Object frame) /*-{
 		var contentType = frame.contentType || frame.mimeType;
 	    return contentType;
 	}-*/;
 	
 	private boolean isWeakSelection(String message, String prefix, String match, String suffix) {
 		if((matchText.length()<20 && prefix.length()+matchText.length()+suffix.length()<50) ||
 		        (prefix.length()==0 && suffix.length()==0)) {
 			_domeo.getLogger().debug(this, "WEAK SELECTION", message + " " + (prefix.length()+suffix.length()));
 			return true;
 		}
 		return false;
 	}
 	
 	private String selectionPostfixEnhancement(Node anchorNode, Node focusNode) {
 		HtmlTraversalUtils htmlTraversalUtils = new HtmlTraversalUtils();
 		if (anchorNode == focusNode) {
 			String newPostfix = htmlTraversalUtils.getTextContentFromNode(anchorNode);
 			_domeo.getLogger().debug(this, "WEAK SELECTION", "Same " + ((newPostfix.trim().length()==0)?"No postfix found": "Postfix found"));
 			return newPostfix;
 		} else {
 			if (compareNodesOrder((Node) anchorNode, (Node) focusNode) == 1) { // Inverted
 				String newPrefix = htmlTraversalUtils.getTextContentFromNode(anchorNode);
 				_domeo.getLogger().debug(this, "WEAK SELECTION", "Different 1 " + ((newPrefix.trim().length()==0)?"No postfix found": "Postfix found"));
 				return newPrefix;
 			} else {
 				String newPrefix = htmlTraversalUtils.getTextContentFromNode(focusNode);
 				_domeo.getLogger().debug(this, "WEAK SELECTION", "Different 2 " + ((newPrefix.trim().length()==0)?"No postfix found": "Postfix found"));
 				return newPrefix;
 			}	
 		}
 	}
 	
 	private String selectionPrefixEnhancement(Node anchorNode, Node focusNode) {
 		HtmlTraversalUtils htmlTraversalUtils = new HtmlTraversalUtils();
 		if (anchorNode == focusNode) {
 			String newPrefix = htmlTraversalUtils.getTextContentToNode(anchorNode);
 			_domeo.getLogger().debug(this, "WEAK SELECTION", "Same " + ((newPrefix.trim().length()==0)?"No prefix found": "Prefix found"));
 			return newPrefix;
 		} else {
 			if (compareNodesOrder((Node) anchorNode, (Node) focusNode) == 1) { // Inverted
 				String newPrefix = htmlTraversalUtils.getTextContentToNode(focusNode);
 				_domeo.getLogger().debug(this, "WEAK SELECTION", "Different 1 " + ((newPrefix.trim().length()==0)?"No prefix found": "Prefix found"));
 				return newPrefix;
 			} else {
 				String newPrefix = htmlTraversalUtils.getTextContentToNode(anchorNode);
 				_domeo.getLogger().debug(this, "WEAK SELECTION", "Different 2 " + ((newPrefix.trim().length()==0)?"No prefix found": "Prefix found"));
 				return newPrefix;
 			}	
 		}
 	}
 	
 	public void updateAnnotationAsNewVersion(MAnnotation annotation, MAnnotationSet set) {
 		
 		if(set!=null) Window.alert("Set: " + set.getLabel());
 		//updateAnnotation(annotation, set);
 		
 		//???
 		if(!_domeo.getAnnotationPersistenceManager().getSetByAnnotationId(annotation.getLocalId()).getLocalId().equals(set.getLocalId())) {
 			Window.alert("Case 1");
 			_domeo.getAnnotationPersistenceManager().updateAnnotationAnnotationSet(annotation, set);
 		} else {
 			Window.alert("Case 2");
 			// Set unchanged?
 			_domeo.getAnnotationPersistenceManager().getSetByAnnotationId(annotation.getLocalId()).setHasChanged(true);
 		}
 		
 		_domeo.getLogger().info(LOG_CATEGORY_ANNOTATION_VERSION, this, "Item updated as new version " + annotation.getLocalId());
 		_domeo.refreshAnnotationComponents();
 	}
 	
 	public void updateAnnotation(MAnnotation annotation, MAnnotationSet set) {
 		if(set==null) { 
 			set= _domeo.getAnnotationPersistenceManager().createNewAnnotationSet();
 		}
 		annotation.setHasChanged(true);
 		annotation.setNewVersion(false);
 		// If new annotation it will be saved as new version
 		if(annotation.getUuid()==null) annotation.setNewVersion(true);
 		if(!_domeo.getAnnotationPersistenceManager().getSetByAnnotationId(annotation.getLocalId()).getLocalId().equals(set.getLocalId())) {
 			_domeo.getAnnotationPersistenceManager().updateAnnotationAnnotationSet(annotation, set);
 		} else {
 			_domeo.getAnnotationPersistenceManager().getSetByAnnotationId(annotation.getLocalId()).setHasChanged(true);
 		}
 		_domeo.getLogger().info(LOG_CATEGORY_ANNOTATION_UPDATED, this, "Item " + annotation.getClass().getName() + "-" + annotation.getLocalId());
 		_domeo.refreshAnnotationComponents();
 	}
 	
 	private void performAnnotation(Long localId, String match, String prefix, String suffix, Node node, String className) {	
 		HtmlUtils.performHighlight(Long.toString(localId), match, prefix, suffix, node, className);
 	}
 	
 	private void performAnnotation(String localId, String match, String prefix, String suffix, Node node, String className) {	
 		HtmlUtils.performHighlight(localId, match, prefix, suffix, node, className);
 	}
 	
 	private void addToClipboard(String matchText, String prefix, String suffix, Node anchorNode) {	
 		performAnnotation(matchText, prefix, suffix, (Node) anchorNode);
 		_domeo.refreshClipboardComponents();
 	}
 	
 	public void performMultipleTargetsAnnotation(ClickHandler closingHandler) {
 		try {
 			_domeo.getLogger().info(LOG_CATEGORY_TEXT_ANNOTATION, this, 
 					"# of targets: " + _domeo.getClipboardManager().getBufferedAnnotation().size());
 			MultipleTargetsAnnotationFormsPanel afp = new MultipleTargetsAnnotationFormsPanel(_domeo, _domeo.getClipboardManager().getBufferedAnnotation());
 			new EnhancedGlassPanel(_domeo, afp, afp.getTitle(), false, false, false, closingHandler);
 		} catch(Exception e) {
 			_domeo.getLogger().exception(LOG_CATEGORY_TARGETS_ANNOTATION, this, 
 					"# of targets: " + _domeo.getClipboardManager().getBufferedAnnotation().size());
 		}
 	}
 	
 	
 	private void performAnnotation(String match, String prefix, String suffix, Node node) {	
 		if(_domeo.isManualAnnotationEnabled()) {
 			try {
 				_domeo.getLogger().info(LOG_CATEGORY_TEXT_ANNOTATION, this, 
 						prefix + " $ " + matchText + " $ " + suffix );
 				TextAnnotationFormsPanel afp = new TextAnnotationFormsPanel(_domeo, match, prefix, suffix, node, null);
 				new EnhancedGlassPanel(_domeo, afp, afp.getTitle(), false, false, false);
 			} catch(Exception e) {
 				_domeo.getLogger().exception(LOG_CATEGORY_TEXT_ANNOTATION, this, 
 						"Annotation could not be completed, problem with annotation forms plugins while annotating: " + 
 						prefix + " $ " + matchText + " $ " + suffix + " $$$$ " + e.getMessage());
 				Window.alert("Annotation could not be completed, problem with annotation forms plugins: " + prefix + matchText + suffix);
 			}
 		} else if(_domeo.isManualClipAnnotationEnabled()) {
 			try {
 				_domeo.getLogger().info(LOG_CATEGORY_TEXT_BUFFERING, this, 
 						prefix + " $ " + matchText + " $ " + suffix );
 				// Buffer annotation creation
 				long start = System.currentTimeMillis();
 				MTextQuoteSelector selector = AnnotationFactory.createPrefixSuffixTextSelector(
 						_domeo.getAgentManager().getUserPerson(), 
 						_domeo.getPersistenceManager().getCurrentResource(), match, prefix, suffix);
 				// TODO Register coordinate of the buffer annotation.
 				MSelectionAnnotation annotation = AnnotationFactory.createTemporary(
 						((AnnotationPersistenceManager)_domeo.getPersistenceManager()).getCurrentSet(), 
 						_domeo.getAgentManager().getUserPerson(), 
 						_domeo.getPersistenceManager().getCurrentResource(), selector);
 				// TODO Add to the annotation buffer
 				boolean success = _domeo.getClipboardManager().addAnnotation(annotation);
 				if(success) {
 					try {
 						HtmlUtils.performHighlight(Long.toString(annotation.getLocalId()), match, prefix, suffix, node, StylesManager.JUST_SELECTED);
 						int y = HtmlUtils.getVerticalPositionOfElementWithId(_frame.getElement(), annotation.getLocalId().toString());
 						
 						annotation.setY(y);
 					
 						_domeo.getLogger().info(LOG_CATEGORY_TEXT_BUFFERED, this, matchText + " in (ms) " + (System.currentTimeMillis()-start));
 						//_domeo.refreshAnnotationComponents();
 					} catch(Exception e) {
 						// TODO Add notification to user
 						_domeo.getLogger().exception(LOG_CATEGORY_TEXT_BUFFERING, this, 
 								prefix + " $ " + matchText + " $ " + suffix + " $$$$ " + e.getMessage());
 						_domeo.getClipboardManager().removeAnnotation(annotation);
 						Window.alert("FIXME: annotation could not be completed: " + prefix + matchText + suffix);
 					}
 				} else {
 					_domeo.getLogger().exception(LOG_CATEGORY_BUFFERING_FAILED, this, "Something went wrong while caching new annotation item.");
 				}
 			} catch(Exception e) {
 				_domeo.getLogger().exception(LOG_CATEGORY_TEXT_BUFFERING, this, 
 						prefix + " $ " + matchText + " $ " + suffix + " $$$$ " + e.getMessage());
 				Window.alert("FIXME: annotation could not be completed: " + prefix + matchText + suffix);
 			}
 		} else if(_domeo.isManualHighlightEnabled()) {		
 			try {
 				_domeo.getLogger().command(LOG_CATEGORY_TEXT_HIGHLIGHT, this, matchText);
 				
 				long start = System.currentTimeMillis();
 				MTextQuoteSelector selector = AnnotationFactory.createPrefixSuffixTextSelector(
 						_domeo.getAgentManager().getUserPerson(), 
 						_domeo.getPersistenceManager().getCurrentResource(), match, prefix, suffix);
 				// TODO Register coordinate of highlight.
 				MAnnotation annotation = AnnotationFactory.createHighlight(
 						((AnnotationPersistenceManager)_domeo.getPersistenceManager()).getCurrentSet(), 
 						_domeo.getAgentManager().getUserPerson(), _domeo.getAgentManager().getSoftware(),
 						_domeo.getPersistenceManager().getCurrentResource(), selector);
 				boolean success = _domeo.getAnnotationPersistenceManager().addAnnotation(annotation, false);
 				
 				if(success) {
 					try {
 						HtmlUtils.performHighlight(Long.toString(annotation.getLocalId()), match, prefix, suffix, node, _domeo.getCssManager().getStrategy().getObjectStyleClass(annotation));
 						//HtmlUtils.performHighlight(Long.toString(annotation.getLocalId()), match, prefix, suffix, node, StylesManager.HIGHLIGHT);
 						int y = HtmlUtils.getVerticalPositionOfElementWithId(_frame.getElement(), annotation.getLocalId().toString());
 						annotation.setY(y);
 						
 						clearSelection();
 						resetSelection("");
 						
 						_domeo.getLogger().info(LOG_CATEGORY_TEXT_HIGHLIGHTED, this, matchText + " in (ms) " + (System.currentTimeMillis()-start));
 						_domeo.refreshAnnotationComponents();
 					} catch(Exception e) {
 						// TODO Add notification to user
 						_domeo.getLogger().exception(LOG_CATEGORY_TEXT_HIGHLIGHT, this, 
 								prefix + " $ " + matchText + " $ " + suffix + " $$$$ " + e.getMessage());
 						Window.alert("FIXME: annotation could not be completed: " + prefix + matchText + suffix);
 						_domeo.getAnnotationPersistenceManager().removeAnnotation(annotation, false);
 					}
 				} else {
 					_domeo.getLogger().exception(LOG_CATEGORY_ANNOTATION_FAILED, this, "Something went wrong while caching new annotation item.");
 				}
 			} catch(Exception e) {
 				_domeo.getLogger().exception(LOG_CATEGORY_TEXT_HIGHLIGHT, this, 
 						prefix + " $ " + matchText + " $ " + suffix + " $$$$ " + e.getMessage());
 				Window.alert("FIXME: annotation could not be completed: " + prefix + matchText + suffix);
 			}
 		}
 	}
 	
 	public void performAnnotation(MAnnotation annotation, HighlightedTextBuffer buffer) {
 		try {
 			long start = System.currentTimeMillis();
 			HtmlUtils.performHighlight(Long.toString(annotation.getLocalId()), buffer.getExact(), 
 					buffer.getPrefix(), buffer.getSuffix(), buffer.getNode(), _domeo.getCssManager().getStrategy().getObjectStyleClass(annotation));
 			int y = HtmlUtils.getVerticalPositionOfElementWithId(_frame.getElement(), annotation.getLocalId().toString());
 			annotation.setY(y);
 			
 			clearSelection();
 			resetSelection("");
 			
 			_domeo.getLogger().info(LOG_CATEGORY_TEXT_ANNOTATED, this, matchText + " in (ms) " + (System.currentTimeMillis()-start));
 			_domeo.refreshAnnotationComponents();
 		}  catch(Exception e) {
 			// TODO Add notification to user
 			_domeo.getLogger().exception(LOG_CATEGORY_TEXT_HIGHLIGHT, this, e.getMessage());
 			_domeo.getAnnotationPersistenceManager().removeAnnotation(annotation, false);
 		}
 	}
 	
 	public void performMultipleTargetsAnnotation(MAnnotation annotation, ArrayList<MAnnotation> targets) {
 		try {
 			long start = System.currentTimeMillis();
 			_domeo.getLogger().debug(AnnotationFrameWrapper.LOG_CATEGORY_TARGETS_ANNOTATION, this, "Performing multiple targets highlight");
 			//MSelector selector = annotation.getSelector();
 			int counter = 0;
 			for(MAnnotation target: targets) {
 				Iterator<com.google.gwt.dom.client.Element> es = HtmlUtils.getElementsByAnnotationId(_frame.getElement(), target.getLocalId()+"").iterator();
 				while(es.hasNext()) {
 					HtmlUtils.flipElement(es.next(), Long.toString(annotation.getLocalId()), ""+target.getSelector().getLocalId(), ""+counter++, "domeo-annotation");
 					if(counter==1) {
 						int y = HtmlUtils.getVerticalPositionOfElementWithId(_frame.getElement(), annotation.getLocalId().toString());
 						annotation.setY(y);
 					}
 				}
 				annotation.addSelector(target.getSelector());
 			}			
			
			clearSelection();
			resetSelection("");
			
 			_domeo.getLogger().info(LOG_CATEGORY_TEXT_ANNOTATED, this, matchText + " in (ms) " + (System.currentTimeMillis()-start));
 			_domeo.getContentPanel().getAnnotationFrameWrapper().clearTemporaryAnnotations();
 			_domeo.refreshAnnotationComponents();
 		}  catch(Exception e) {
 			// TODO Add notification to user
 			_domeo.getLogger().exception(LOG_CATEGORY_TEXT_HIGHLIGHT, this, e.getMessage());
 			_domeo.getAnnotationPersistenceManager().removeAnnotation(annotation, false);
 		}
 	}
 	
 	public void performMultipleTargetsHighlight(ArrayList<MAnnotation> targets) {
 		MAnnotation annotation = null;
 		try {
 			long start = System.currentTimeMillis();
 			_domeo.getLogger().debug(AnnotationFrameWrapper.LOG_CATEGORY_TARGETS_ANNOTATION, this, "Performing multiple targets highlight");
 			//MSelector selector = annotation.getSelector();
 			
 			annotation = AnnotationFactory.createHighlight(
 				((AnnotationPersistenceManager)_domeo.getPersistenceManager()).getCurrentSet(), 
 				_domeo.getAgentManager().getUserPerson(), _domeo.getAgentManager().getSoftware());
 			_domeo.getAnnotationPersistenceManager().addAnnotation(annotation, ((AnnotationPersistenceManager)_domeo.getPersistenceManager()).getCurrentSet());
 			
 			int counter = 0;
 			for(MAnnotation target: targets) {
 				Iterator<com.google.gwt.dom.client.Element> es = HtmlUtils.getElementsByAnnotationId(_frame.getElement(), target.getLocalId()+"").iterator();
 				while(es.hasNext()) {
 					HtmlUtils.flipElement(es.next(), Long.toString(annotation.getLocalId()), ""+target.getSelector().getLocalId(), ""+counter++, "domeo-highlight");
 					if(counter==1) {
 						int y = HtmlUtils.getVerticalPositionOfElementWithId(_frame.getElement(), annotation.getLocalId().toString());
 						annotation.setY(y);
 					}
 				}
 				annotation.addSelector(target.getSelector());
 			}			
			
			clearSelection();
			resetSelection("");
			
 			_domeo.getLogger().info(LOG_CATEGORY_TEXT_ANNOTATED, this, matchText + " in (ms) " + (System.currentTimeMillis()-start));
 			_domeo.getContentPanel().getAnnotationFrameWrapper().clearTemporaryAnnotations();
 			_domeo.refreshAnnotationComponents();
 		}  catch(Exception e) {
 			// TODO Add notification to user
 			_domeo.getLogger().exception(LOG_CATEGORY_TEXT_HIGHLIGHT, this, e.getMessage());
 			_domeo.getAnnotationPersistenceManager().removeAnnotation(annotation, false);
 		}
 	}
 	
 	public void performAnnotation(MAnnotation annotation, MGenericResource resource, Element element) {
 		long start = System.currentTimeMillis();
 		
 		DOM.setStyleAttribute(element, "border", _domeo.getCssManager().getStyle(StylesManager.IMG_ANNOTATED_BORDER));
 		_domeo.getLogger().info(LOG_CATEGORY_TEXT_HIGHLIGHTED, this, matchText + " in (ms) " + (System.currentTimeMillis()-start));
 		_domeo.refreshAnnotationComponents();
 		
 		//_domeo.getImagesCache().add();
 	}
 	
 	public void performAnnotation(ArrayList<MAnnotation> annotations) {
 		try {
 			long start = System.currentTimeMillis();
 			_domeo.getLogger().info(LOG_CATEGORY_TEXT_ANNOTATION, this, "TODO");
 			RelationshipAnnotationFormsPanel afp = new RelationshipAnnotationFormsPanel(_domeo, annotations);
 			new EnhancedGlassPanel(_domeo, afp, afp.getTitle(), false, false, false);
 		}  catch(Exception e) {
 			// TODO Add notification to user
 			_domeo.getLogger().exception(LOG_CATEGORY_TEXT_HIGHLIGHT, this, e.getMessage());
 			
 		}
 	}
 	
 	public void editAnnotation(MAnnotation annotation) {	
 		try {
 			if(SelectorUtils.isOnTextFragment(annotation.getSelectors())) {
 				// Text fragment annotation
 				TextAnnotationFormsPanel afp = new TextAnnotationFormsPanel(_domeo, annotation, null);
 				new EnhancedGlassPanel(_domeo, afp, afp.getTitle(), false, false, false);
 			} else if(SelectorUtils.isOnMultipleTargets(annotation.getSelectors())) {
 				MultipleTargetsAnnotationFormsPanel afp = new MultipleTargetsAnnotationFormsPanel(_domeo, annotation);
 				_domeo.getComponentsManager().addComponent(afp);
 				new EnhancedGlassPanel(_domeo, afp, afp.getTitle(), false, false, false);
 			} else {
 				ImageAnnotationFormsPanel afp = new ImageAnnotationFormsPanel(_domeo, annotation, null);
 				_domeo.getComponentsManager().addComponent(afp);
 				new EnhancedGlassPanel(_domeo, afp, afp.getTitle(), false, false, false);
 			}
 		} catch(Exception e) {
 			_domeo.getLogger().exception(LOG_CATEGORY_EDIT_ANNOTATION, this, "Failed in editing annotation of type " + 
 					annotation.getClass().getName() + "  " + e.getMessage());
 		}
 	}
 	
 	public void showAnnotationLocation(MAnnotation annotation) {
 		if(annotation.getSelector().getTarget() instanceof MOnlineImage) {
 			final Element element = ((MOnlineImage)annotation.getSelector().getTarget()).getImage();
 			final String newStyle = _domeo.getCssManager().getStyle(StylesManager.IMG_EMPHASIZED_BORDER);
 			HtmlUtils.scrollToElement(_frame.getElement(), element);
 			DOM.setStyleAttribute(element, "border", newStyle);
 			Timer timer = new Timer() {
 				public void run() {
 					DOM.setStyleAttribute(element, "border",  _domeo.getCssManager().getStyle(StylesManager.IMG_ANNOTATED_BORDER));
 				}
 			};
 			timer.schedule(1000);
 			
 		} else {
 			Set<com.google.gwt.dom.client.Element> spans = HtmlUtils.getElementsByAnnotationId(
 					_frame.getElement(), annotation.getLocalId().toString());
 			for (com.google.gwt.dom.client.Element span : spans) {
 				HtmlUtils.scrollToElement(_frame.getElement(), span);
 				final com.google.gwt.dom.client.Element _span = span;
 				final String spanClass = span.getClassName();
 				span.setClassName(StylesManager.EMPHASIZED);
 	
 				Timer timer = new Timer() {
 					public void run() {
 						_span.setClassName(spanClass);
 					}
 				};
 				timer.schedule(1000);
 			}
 		}
 	}
 	
 	public void showLocationAnnotationById(String annotationId) {
 		Set<com.google.gwt.dom.client.Element> spans = HtmlUtils.getElementsByAnnotationId(
 				_frame.getElement(), annotationId);
 		for (com.google.gwt.dom.client.Element span : spans) {
 			HtmlUtils.scrollToElement(_frame.getElement(), span);
 			GWT.log(span.getInnerText(), null);
 			final com.google.gwt.dom.client.Element _span = span;
 			final String spanClass = span.getClassName();
 			span.setClassName(StylesManager.EMPHASIZED);
 
 			Timer timer = new Timer() {
 				public void run() {
 					_span.setClassName(spanClass);
 				}
 			};
 			timer.schedule(1000);
 		}
 	}
 	
 	public void removeAnnotationSetAnnotation(MAnnotationSet set, boolean mark) {
 		long start=System.currentTimeMillis();
 		_domeo.getLogger().debug(this, "Starting set with id " + set.getLocalId() + " annotation removal");
 		
 		List<MAnnotation> toRemove = new ArrayList<MAnnotation>();
 		toRemove.addAll(set.getAnnotations());
 		Iterator<MAnnotation> it = toRemove.iterator();
 		while(it.hasNext()) {
 			removeAnnotation(it.next(), mark);
 		}
 		
 		_domeo.getLogger().info(LOG_CATEGORY_ANNOTATION_DELETED, this, "Set " + set.getLocalId() + " annotation removed in(ms):"+(System.currentTimeMillis()-start));
 	}
 	
 	public void removeAnnotation(MAnnotation annotation, boolean mark) {
 		long start=System.currentTimeMillis();
 		_domeo.getLogger().debug(this, "Starting annotation removal " + annotation.getClass().getName() + "-" + annotation.getLocalId());
 		
 		// TODO check if image and if it is remove border.
 		if(annotation.getSelector().getTarget() instanceof MOnlineImage) {
 			if(_domeo.getAnnotationPersistenceManager().getAllAnnotationsForResource(annotation.getSelector().getTarget().getUrl()).size()==1) {
 				DOM.setStyleAttribute(((MOnlineImage)annotation.getSelector().getTarget()).getImage(), "border", "0");
 			}
 		} else if(SelectorUtils.isOnMultipleTargets(annotation.getSelectors())) {
 			for(MSelector selector: annotation.getSelectors()){
 				HtmlUtils.removeSpansWithAnnotationId(_frame.getElement(), annotation.getLocalId()+":"+selector.getLocalId());
 			}
 		} else HtmlUtils.removeSpansWithAnnotationId(_frame.getElement(), annotation.getLocalId());
 		
 		if(annotation instanceof MSelectionAnnotation) {
 			_domeo.getClipboardManager().removeAnnotation((MSelectionAnnotation)annotation);
 			_domeo.refreshClipboardComponents();
 		} else {
 			_domeo.getAnnotationPersistenceManager().removeAnnotation(annotation, mark);
 			_domeo.refreshAnnotationComponents();
 		}
 		
 		_domeo.getLogger().info(LOG_CATEGORY_ANNOTATION_DELETED, this, "Annotation " + annotation.getClass().getName() + "-" + annotation.getLocalId() + " in(ms):"+(System.currentTimeMillis()-start));
 	}
 	
 	public void clearTemporaryAnnotations() {
 		long start=System.currentTimeMillis();
 		_domeo.getLogger().debug(this, "Starting temporary annotations removal #" + _domeo.getClipboardManager().getBufferedAnnotation().size());
 		for(MAnnotation annotation: _domeo.getClipboardManager().getBufferedAnnotation()) {
 			HtmlUtils.removeSpansWithAnnotationId(_frame.getElement(), annotation.getLocalId());
 		}
 		_domeo.getClipboardManager().init();
 		_domeo.refreshClipboardComponents();
 		_domeo.getLogger().info(LOG_CATEGORY_ANNOTATION_DELETED, this, "Annotations removed in(ms):"+(System.currentTimeMillis()-start));
 	}
 
 	public void manageSetVisibility(MAnnotationSet set, boolean isVisible) {
 		long start = System.currentTimeMillis();
 		set.setIsVisible(isVisible);
 		if(isVisible) {
 			if(set.isEmphasized()) modifyDisplayAnnotationSets(set, StylesManager.EMPHASIZED);
 			else displayAnnotationSets(set);
 		} else {
 			if(set.isEmphasized()) modifyDisplayAnnotationSets(set, StylesManager.EMPHASIZED);
 			else modifyDisplayAnnotationSets(set, StylesManager.NEUTRAL);
 		}
 		_domeo.getLogger().info(this, "Changed AnnotationSet visibility in (ms): " + (System.currentTimeMillis()-start));
 	}
 	
 	public void manageSetEmphasis(MAnnotationSet set, boolean isEmphasized) {
 		long start = System.currentTimeMillis();
 		set.setEmphasized(isEmphasized);
 		if(isEmphasized) modifyDisplayAnnotationSets(set, StylesManager.EMPHASIZED);
 		else {
 			if(set.getIsVisible()) resetDisplayAnnotationSets(set);
 			else modifyDisplayAnnotationSets(set, StylesManager.NEUTRAL);
 		}
 		_domeo.getLogger().info(this, "Changed AnnotationSet emphasis in (ms): " + (System.currentTimeMillis()-start));
 	}
 	
 	public void manageSetHighlight(MAnnotationSet set, boolean isHighlighted) {
 		long start = System.currentTimeMillis();
 		set.setEmphasized(isHighlighted);
 		if(isHighlighted) modifyDisplayAnnotationSets(set, StylesManager.LIGHTBLUE_HIGHLIGHT);
 		else {
 			modifyDisplayAnnotationSets(set, StylesManager.NEUTRAL);
 		}
 		_domeo.getLogger().info(this, "Changed AnnotationSet highlight in (ms): " + (System.currentTimeMillis()-start));
 	}
 	
 	public void manageSetHighlightFragments(MAnnotationSet set, boolean isHighlighted) {
 		try {
 			long start = System.currentTimeMillis();
 			//set.setEmphasized(isHighlighted);
 			if(isHighlighted) modifyDisplayAnnotationSetsFragments(set, StylesManager.LIGHTBLUE_HIGHLIGHT);
 			else {
 				modifyDisplayAnnotationSetsFragments(set, StylesManager.NEUTRAL);
 			}
 			_domeo.getLogger().info(this, "Changed AnnotationSet fragments highlight in (ms): " + (System.currentTimeMillis()-start));
 		} catch(Exception e) {
 			Window.alert("Exception " + e.getMessage());
 		}
 	}
 	
 	// TODO to optimize
 	public void displayAnnotationSets(MAnnotationSet annotationSet) {
 		_domeo.getLogger().debug(this, "Displaying AnnotationSet " + annotationSet.getLocalId());
 		IFrameElement iframe = IFrameElement.as(_frame.getElement());
 		final Document frameDocument = iframe.getContentDocument();
 		NodeList<com.google.gwt.dom.client.Element> elements = frameDocument.getElementsByTagName("span");
 		for (int i = 0; i < elements.getLength(); i++) {
 			String id = HtmlUtils.getAnnotationIdAttribute(elements.getItem(i));
 			if (id == null)
 				continue;
 			String className = HtmlUtils
 					.getAnnotationDefaultCssAttribute(elements.getItem(i));
 			for (MAnnotation item : annotationSet.getAnnotations()) {
 				if (id.trim().length() > 0 && id.equals(item.getLocalId().toString())) {
 					elements.getItem(i).setClassName(className);
 				}
 			}
 		}
 	}
 	
 	// TODO to optimize
 	public void modifyDisplayAnnotationSetsFragments(MAnnotationSet annotationSet, String className) {
 		_domeo.getLogger().debug(this, "Modifying display of AnnotationSet fragments " + annotationSet.getLocalId() + " to " + className);
 		IFrameElement iframe = IFrameElement.as(_frame.getElement());
 		final Document frameDocument = iframe.getContentDocument();
 		NodeList<com.google.gwt.dom.client.Element> elements = frameDocument.getElementsByTagName("span");
 		for (int i = 0; i < elements.getLength(); i++) {
 			String id = HtmlUtils.getAnnotationIdAttribute(elements.getItem(i));
 			if (id == null) continue;
 			for (MAnnotation item : annotationSet.getAnnotations()) {
 				if (id.trim().length() > 0 && id.startsWith(item.getLocalId().toString()+":")) {
 					elements.getItem(i).setClassName(className);
 					break;
 				}
 			}
 		}
 	}
 	
 	// TODO to optimize
 	public void modifyDisplayAnnotationSets(MAnnotationSet annotationSet, String className) {
 		_domeo.getLogger().debug(this, "Modifying display of AnnotationSet " + annotationSet.getLocalId() + " to " + className);
 		IFrameElement iframe = IFrameElement.as(_frame.getElement());
 		final Document frameDocument = iframe.getContentDocument();
 		NodeList<com.google.gwt.dom.client.Element> elements = frameDocument.getElementsByTagName("span");
 		for (int i = 0; i < elements.getLength(); i++) {
 			String id = HtmlUtils.getAnnotationIdAttribute(elements.getItem(i));
 			if (id == null)
 				continue;
 			for (MAnnotation item : annotationSet.getAnnotations()) {
 				if (id.trim().length() > 0 && id.equals(item.getLocalId().toString())) {
 					elements.getItem(i).setClassName(className);
 					break;
 				}
 			}
 		}
 	}
 	
 	// TODO to optimize
 	public void modifyDisplayAnnotation(MAnnotationSet annotationSet, Map<Long, Boolean> classNames /*id and classname*/) {
 	    _domeo.getLogger().debug(this, "Modifying display of AnnotationSet elements (show/hide)");
         IFrameElement iframe = IFrameElement.as(_frame.getElement());
         final Document frameDocument = iframe.getContentDocument();
         NodeList<com.google.gwt.dom.client.Element> elements = frameDocument.getElementsByTagName("span");
         for (int i = 0; i < elements.getLength(); i++) {
             String id = HtmlUtils.getAnnotationIdAttribute(elements.getItem(i));
             if (id == null) continue;
             _domeo.getLogger().debug(this, "id: " + id);
             String className = HtmlUtils
                     .getAnnotationDefaultCssAttribute(elements.getItem(i));
             _domeo.getLogger().debug(this, "className: " + className);
             for (MAnnotation item : annotationSet.getAnnotations()) {
                 _domeo.getLogger().debug(this, "local: " + item.getLocalId());
                 if (id.trim().length() > 0 && id.equals(item.getLocalId().toString())) {
                     _domeo.getLogger().debug(this, "map: " + classNames.get(id));
                     if(classNames.containsKey(item.getLocalId())) {
                         _domeo.getLogger().debug(this, "map: " + classNames.get(item.getLocalId()));
                         if(classNames.get(item.getLocalId())) elements.getItem(i).setClassName(className);
                         else elements.getItem(i).setClassName(StylesManager.NEUTRAL);
                     }  
                     break;
                 }
             }
         }
     }
 	
 	// TODO to optimize
 	public void resetDisplayAnnotationSets(MAnnotationSet annotationSet) {
 		_domeo.getLogger().debug(this, "Reset display of AnnotationSet " + annotationSet.getLocalId());
 		IFrameElement iframe = IFrameElement.as(_frame.getElement());
 		final Document frameDocument = iframe.getContentDocument();
 		NodeList<com.google.gwt.dom.client.Element> elements = frameDocument.getElementsByTagName("span");
 		for (int i = 0; i < elements.getLength(); i++) {
 			String id = HtmlUtils.getAnnotationIdAttribute(elements.getItem(i));
 			if (id == null)
 				continue;
 			String className = HtmlUtils.getAnnotationDefaultCssAttribute(elements.getItem(i));
 			for (MAnnotation item : annotationSet.getAnnotations()) {
 				if (id.trim().length() > 0 && id.equals(item.getLocalId().toString())) {
 					elements.getItem(i).setClassName(className);
 					break;
 				}
 			}
 		}
 	}
 	
 	
 	
 	public void createEditTableButton(Element table) {
 		
 		IFrameElement iframe = IFrameElement.as(_frame.getElement());
 		final Document frameDocument = iframe.getContentDocument();
 		
 		Image editIcon = new Image(_resources.runLittleIcon());
 		
 		SimplePanel sp = new SimplePanel();
 		sp.setHeight("18px");
 		sp.setWidth("18px");
 		sp.add(editIcon);
 		DOM.setStyleAttribute(sp.getElement(), "position", "absolute");
 		DOM.setStyleAttribute(sp.getElement(), "top", "100px");
 		DOM.setStyleAttribute(sp.getElement(), "left", "100px");
 		DOM.setStyleAttribute(sp.getElement(), "background-color", "red");
 		frameDocument.appendChild(sp.getElement());
 		
 		
 		/*
 		 * var ST= 'position:absolute'
 									  +'; left:' + ($wnd.findXPosition(tables[i])+tables[i].offsetWidth-18) + 'px'
 									  +'; top:'+ ($wnd.findYPosition(tables[i])) + 'px'
 									  +'; width: 18px'+ 
 									  +'; height: 18px'+ 
 									  +'; background-color: black';
 								
 								var div = iframe.contentDocument.createElement('div');
 								div.setAttribute('style',ST);
 								div.innerHtml = 'dasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafasdasfsafas';
 								iframe.contentDocument.body.appendChild(div);
 		 */
 	}
 	
 // -----------------------------
 //  FRAME URL MANAGEMENT
 // -----------------------------
 	public void setUrl(String url) {
 		currentUrl = url;
 		_frame.setUrl(currentUrl);
 		registerPdfLoadedNotification();
 	}
 	
 	public void setUrl(String url, String realUrl) {
 		currentUrl = realUrl;
 		_domeo.getLogger().debug(this, "Setting up url " + realUrl);
 		_frame.setUrl(url);
 		registerPdfLoadedNotification();
 	}
 	
 	public String getUrl(String dummy) {
 		return currentUrl;
 	}
 	
 	public String getUrl() {
 		return currentUrl;
 	}
 	
 	public Frame getFrame() {
 		
 		return _frame;
 	}
 	
 	public void displayLink(String x, String y, String link, Element element) {
 		//_application.displayBubble(Integer.parseInt(x), Integer.parseInt(y), link, element);
 	}
 	
 	public void annotateImage(String src, String displayUrl, com.google.gwt.dom.client.Element element) {
 		try {
 			if(_domeo.isManualAnnotationEnabled()) {
 				//Window.alert(HtmlUtils.getElementXPath(element));
 				ImageAnnotationFormsPanel afp = new ImageAnnotationFormsPanel(_domeo, src, displayUrl, element.getAttribute("title"), element.getAttribute("imageid"), (Element) element, null);
 				_domeo.getComponentsManager().addComponent(afp);
 				new EnhancedGlassPanel(_domeo, afp, afp.getTitle(), false, false, false);
 			}
 		} catch(Exception e) {
 			Window.alert("AnnotationFrameWrapper::annotateImage " + e.getMessage());
 		}
 	}
 	
 	public native void hookEventsNotIE(Element iframe, boolean wrapTables, boolean wrapImages, boolean wrapLinks) /*-{
 		var foo = this;
 		if (iframe) {
 			iframe.onload = function() {		
 				try {
 					// Fix this, if I can't hook up events no standard notification.
 					if(wrapLinks) {
 						var links = iframe.contentDocument.getElementsByTagName('a');
 						for(var i=0;i<links.length;++i) {
 							links[i].onclick=function(e) { 
 								// TODO 
 								// 1) Determine if the link is also annotated
 								// 2) If not open the page
 								// 3) If yes open the popup with curation + link to the page
 								
 								// window.open(e.target,links[i].href,'');
 								e.stopPropagation();
 								return false;
 							}
 						}
 					}
 
 					if(wrapImages) {	
 						var images = iframe.contentDocument.getElementsByTagName('img');
 						var imagesLength = images.length;
 						for(var i=0;i<imagesLength;++i) {
 							var originalSrc = images[i].getAttribute('src');
 							images[i].setAttribute('imageid', 'domeo_img_'+i);
 							
 							
 							var frameBaseWithProxy = $doc.getElementById("domeoframe").contentWindow.document.location;
 							//alert('frameBaseWithProxy ' + frameBaseWithProxy)
 							if(new String(frameBaseWithProxy).lastIndexOf("proxy/")>0) {
 								frameBaseWithProxy = new String(frameBaseWithProxy).substring(new String(frameBaseWithProxy).lastIndexOf("proxy/")+6);
 							} 
 							//alert('frameBaseWithoutProxy ' + frameBaseWithProxy)
 							
 							// Detect base url
 							var frameBaseUrl = $wnd.getBaseUrl(new String(frameBaseWithProxy));
 							//alert('frameBase ' + frameBaseUrl);
 							
 							var appBaseUrl = $wnd.getHostUrl(new String($doc.location));
 							
 							if(images[i].src!=null && images[i].src.length>0 && images[i].src[0]=='/') {
 								
 							}
 							var src = images[i].src;
 							if(images[i].src.substring(0,appBaseUrl.length) == appBaseUrl) {
 								src = src.replace(appBaseUrl, frameBaseUrl);
 							} 
 							
 							if(originalSrc.substring(0,appBaseUrl.length) == appBaseUrl) {
 								originalSrc = originalSrc.replace(appBaseUrl, frameBaseUrl);
 							} else if(originalSrc.indexOf("/", 0) === 0) {
 								if(frameBaseUrl.charAt(frameBaseUrl.length-1)=="/") {
 									frameBaseUrl = frameBaseUrl.substring(0, frameBaseUrl.length-1);
 								}  
 								originalSrc = frameBaseUrl + originalSrc;
 							}
 							images[i].setAttribute('domeoOriginalSrc', originalSrc);
 							//foo.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::addImage(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/dom/client/Element;)(src, ''+images[i].width, ''+images[i].height, images[i].alt, images[i]);
 							
 							images[i].onclick=(function(e, element) {
 								
 								return function(){
 									try {
 										//var src = (element.src);
 										var src = new String(e);
 										var src2 = element.src;
 										if(src.substring(0,appBaseUrl.length) == appBaseUrl) {
 											if(frameBaseUrl.charAt(frameBaseUrl.length-1)!="/" && appBaseUrl.charAt(appBaseUrl.length-1)=="/")
 												frameBaseUrl = frameBaseUrl + "/";
 											src = src.replace(appBaseUrl, frameBaseUrl);
 										} 
 										//alert('appBase ' + src + ' - ' + src2);
 										if(src2.substring(0,appBaseUrl.length) == appBaseUrl) {
 											if(frameBaseUrl.charAt(frameBaseUrl.length-1)!="/" && appBaseUrl.charAt(appBaseUrl.length-1)=="/")
 												frameBaseUrl = frameBaseUrl + "/";
 											src2 = src2.replace(appBaseUrl, frameBaseUrl);
 										} 										
 										//alert('appBase ' + src + ' - ' + src2);
 										foo.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::annotateImage(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/dom/client/Element;)(src, src2, element);
 										return false;
 									} catch(e) {
 										alert('images[i].onclick: ' +e);
 									}
 								}
 							}) (images[i].src, images[i]);
 							images[i].onmouseover=function(e){
 								try {
 									this.style.cursor='crosshair'
 								} catch(e) {
 									alert('images[i].onmouseover: ' +e);
 								}
 							}
 							images[i].onmouseout=function(e){
 								try {
 									this.style.cursor='default'
 								} catch(e) {
 									alert('images[i].onmouseout: ' +e);
 								}
 							}
 						}
 					}
 					
 					if(wrapTables) {
 						var domeoTableCounter = 0;
 						var tables = iframe.contentDocument.getElementsByTagName('table');
 						for(var i=0;i<tables.length;++i) {
 							//tables[i].className += tables[i].bufferStyle + " " + "domeo-table-normal";
 							//tables[i].bufferStyle = tables[i].className;
 							
 							var img = iframe.contentDocument.createElement('img');
 							img.src = '../images/domeo16x16.png';
 							img.title = 'Process table';
 							img.onclick=function(e){
 								try {
 									alert('process table: ' +'domeoTableEditIcon'+domeoTableCounter);
 									e.stopPropagation();
 									return false;
 								} catch(e) {
 									alert('tables[i].onclick: ' +e);
 								}
 							}
 						
 							//var ST= 'position:absolute'
 							//	  +'; left:' + ($wnd.findXPosition(tables[i])+tables[i].offsetWidth-18) + 'px'
 							//	  +'; top:'+ ($wnd.findYPosition(tables[i])) + 'px'
 							//	  +'; width: 18px'+ 
 							//	  +'; height: 18px'+ 
 							//	  +'; background-color: #ddd';
 								  
 						 	//var heightBuffer = tables[i].offsetWidth;
 							//alert(heightBuffer);
 		//
 							var ST= 'position:relative'
 								  +'; left:' + (tables[i].offsetWidth-8) + 'px'
 								  +'; top:-'+ ((tables[i].offsetHeight)) + 'px'
 								  +'; width: 18px'+ 
 								  +'; height: 18px'+ 
 								  +'; cursor: pointer'+ 
 								  +'; visibility: hidden'+
 								  +'; pointer: hand'+ 
 								  +'; background-color: #ddd';
 									
 							var iconWrapper = iframe.contentDocument.createElement('div');
 							iconWrapper.setAttribute('style',ST);
 							iconWrapper.appendChild(img);
 							iconWrapper.id = 'domeoTableEditIcon'+domeoTableCounter;
 							
 							// Table wrapper
 							var container = iframe.contentDocument.createElement('div');
 							container.className = "domeo-table-normal";
 							container.style.width = tables[i].offsetWidth + 8;
 							container.style.height = tables[i].height + 8;
 							container.domeoTargetOf = 'domeoTableEditIcon'+domeoTableCounter;
 							
 							// Buffers
 							var tableBuffer =  tables[i];
 							var parentBuffer = tables[i].parentNode;
 							
 							// Replace Image with Image wrapper
 							parentBuffer.replaceChild(container, tableBuffer);
 							
 							// Add image and icon to the image wrapper
 							container.appendChild(tableBuffer);
 							container.appendChild(iconWrapper);
 							
 							container.onclick=function(e){
 								try {
 									e.stopPropagation();
 									return false;
 								} catch(e) {
 									alert('tables[i].onclick: ' +e);
 								}
 							}
 							container.onmouseover=function(e){
 								try {
 									this.className = "domeo-table-highlight";
 									var elem = iframe.contentDocument.getElementById(this.domeoTargetOf);
 									elem.style.visibility = 'visible';
 								} catch(e) {
 									alert('tables[i].onmouseover: ' +e);
 								}
 							}
 							container.onmouseout=function(e){
 								try {
 									this.className = "domeo-table-normal";
 									var elem = iframe.contentDocument.getElementById(this.domeoTargetOf);
 									elem.style.visibility = 'hidden';
 								} catch(e) {
 									alert('tables[i].onmouseout: ' +e);
 								}
 							}
 							domeoTableCounter++;
 						}
 					}
 						if(foo.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::getUrl()().length>0)
 						foo.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::documentLoaded()();
 					
 				} catch (e) {
 					alert(e);
 					foo.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::notProxiedDocumentLoaded()();
 				}
 			};
 		}
 	}-*/;
 	
 	public void evaluateImage(String dummy) {
 		//_application.evaluateImage(dummy);
 	}
 	
 	public void notifyException(String message) {
 		_domeo.notifyException(this.getClass().getName(), message);
 	}
 	
 	public void removeDialog() {
 		_domeo.removeDialog();
 	}
 	
 	public native void hookEventsIE(Element iframe) /*-{
 		var foo = this;
 		if (iframe) {
 			iframe.onreadystatechange = function() {
 				try {
 					if(foo.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::getUrl()().length>0)
 						foo.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::documentLoaded()();
 					var links = iframe.contentDocument.getElementsByTagName('a');
 					for(var i=0;i<links.length;++i)links[i].onclick=function(e){ 
 						alert('click to be submitted to the proxy');
 						if (!e) var e = window.event;
 						window.open(e.srcElement,'','');
 						e.cancelBubble = true;
 						if (e.stopPropagation) e.stopPropagation();
 						return false;
 					}
 				} catch (e) {
 					foo.@org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper::notProxiedDocumentLoaded()();				
 				}
 			};
 		}
 	}-*/;
 	
 	public native void registerPdfLoadedNotification() /*-{
 	   $doc.getElementById($wnd.FRAME_ID).contentWindow.window.pdfLoaded = $entry(@org.mindinformatics.gwt.domeo.client.Domeo::notifyDocumentLoadedStageOneStatic());
 	}-*/;
 	
 	// Called by native javascript code
 	public void documentLoaded() {
 		_domeo.notifyDocumentLoadedStageOne();
 	}
 	
 	// Called by native javascript code
 	public void notProxiedDocumentLoaded() {
 		_domeo.notifyNotProxiedDocumentLoaded();
 	}
 		
 	public void retrieveDocumentTitle() {
 		IFrameElement iframe = IFrameElement.as(_frame.getElement());
 		final Document frameDocument = iframe.getContentDocument();
 		_documentTitle = HtmlUtils.getDocumentTitle(frameDocument);
 	}
 	
 	public void retrieveDocumentMetadata() {
 		IFrameElement iframe = IFrameElement.as(_frame.getElement());
 		final Document frameDocument = iframe.getContentDocument();
 		_documentMetadata = HtmlUtils.getDocumentMetadata(frameDocument);
 	}
 	
 	public String getDocumentTitle() {
 		return _documentTitle;
 	}
 	
 	public String getDocumentDescription() {
 		if(_documentMetadata.containsKey("description"))
 			return _documentMetadata.get("description");
 		else return "";
 	}
 	
 	public String[] getDocumentAuthors() {
 		if(_documentMetadata.containsKey("author"))
 			return _documentMetadata.get("author").split(",");
 		else return new String[]{};
 	}
 	
 	public String[] getDocumentKeywords() {
 		if(_documentMetadata.containsKey("keywords"))
 			return _documentMetadata.get("keywords").split(",");
 		else return new String[]{};
 	}
 	
 	public void performReferencesAnnotation(MAnnotationCitationReference annotation, MSelector selector, com.google.gwt.dom.client.Element _element) {
 		try {
 			if(((BooleanPreference)_domeo.getPreferences().getPreferenceItem(Domeo.class.getName(), Domeo.PREF_PERFORM_ANNOTATION_OF_CITATIONS)).getValue()) {
 				long start = System.currentTimeMillis();
 	//			
 	//				_domeo.getLogger().debug(this, "Starting highlighting the references");
 	//				int counter = 0;
 	//				for(MAnnotationCitationReference annotation: citations) {
 	
 						_domeo.getLogger().debug(this.getClass().getName(), SelectorUtils.getPrefix(selector) + "***" + SelectorUtils.getMatch(selector) + "***" + SelectorUtils.getSuffix(selector));
 						this.performAnnotation(annotation.getLocalId()+":"+selector.getLocalId(), SelectorUtils.getMatch(selector), 
 								SelectorUtils.getPrefix(selector), 
 								SelectorUtils.getSuffix(selector), 
 								_element, StylesManager.LIGHTBLUE_HIGHLIGHT);
 		
 	//					counter++;
 	//				}
 	//			}
 				
 				_domeo.getLogger().debug(this.getClass().getName(), 
 						"Highlight of PubMed Central document references info completed in " + (System.currentTimeMillis()-start) + "ms");
 			}
 		} catch(Exception e) {
 			_domeo.getLogger().exception(this, 
 					"Highlight of PubMed Central document not completed correctly " + e.getMessage());
 		}
 	}
 	
 	public void performReferencesAnnotation(ArrayList<MAnnotationCitationReference> citations, List<com.google.gwt.dom.client.Element> _elements) {
 		if(((BooleanPreference)_domeo.getPreferences().getPreferenceItem(Domeo.class.getName(), Domeo.PREF_PERFORM_ANNOTATION_OF_REFERENCES)).getValue()) {
 			long start = System.currentTimeMillis();
 			
 				_domeo.getLogger().debug(this, "Starting highlighting the references");
 				int counter = 0;
 				for(MAnnotationCitationReference annotation: citations) {
 					try {
 						this.performAnnotation(annotation.getLocalId(), SelectorUtils.getMatch(annotation.getReferenceSelector()), 
 								SelectorUtils.getPrefix(annotation.getReferenceSelector()), 
 								SelectorUtils.getSuffix(annotation.getReferenceSelector()), 
 								_elements.get(counter), StylesManager.LIGHTBLUE_HIGHLIGHT);
 						counter++;
 					} catch(Exception e) {
 						_domeo.getLogger().exception(this, 
 								"Highlight of PubMed Central document not completed correctly " + e.getMessage());
 					}
 				}
 
 			_domeo.getLogger().debug(this.getClass().getName(), 
 					"Highlight of PubMed Central document references info completed in " + (System.currentTimeMillis()-start) + "ms");
 		}
 	}
 }
 
 
