 package org.vaadin.teemu.wizards.client.ui;
 
 import java.util.ArrayList;
 
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.CellPanel;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.client.ApplicationConnection;
 
 public class VWizardProgressBar extends FlowPanel implements
         HasProgressBarItemClickHandler {
 
     /** Set the CSS class name to allow styling. */
     public static final String CLASSNAME = "v-wizardprogressbar";
 
     private static String combinedStylename = "";
 
     /** The client side widget identifier */
     protected String paintableId;
 
     /** Reference to the server connection object. */
     ApplicationConnection client;
 
     private Element barElement;
     private CellPanel captions;
 
     private String id;
     private boolean initialized = false;
 
     private boolean _isHorizontal;
 
     private boolean _hasVerticalSpacing;
 
     private boolean _completed;
 
     private boolean _showProgressIndicatorBar;
 
     private String _linkmode;
 
     private ArrayList<SerializableStep> _steps = null;
 
     private ProgressBarItemClickHandler progressBarItemClickHandler = null;
 
     /**
      * The constructor should first call super() to initialize the component and
      * then handle any initialization relevant to Vaadin.
      */
     public VWizardProgressBar() {
         // This method call of the Paintable interface sets the component
         // style name in DOM tree
 
     }
 
     @Override
     public void setStyleName(String style) {
         String s = getStyleName();
         super.setStyleName(s + " " + combinedStylename + " " + style);
     }
 
     private void init(boolean horizontal, boolean showProgressIndicatorBar) {
         if (horizontal) {
             captions = new HorizontalPanel();
             captions.setWidth("100%");
             combinedStylename = CLASSNAME + " wiz-horiz";
             setStyleName("");
         } else {
             int maxWidth = getOffsetWidth();
             captions = new VerticalPanel();
             // captions.setHeight("100%");
             if (showProgressIndicatorBar) {
                 captions.setWidth((maxWidth - 15) + "px");
             } else {
                 captions.setWidth((maxWidth) + "px");
             }
             combinedStylename = CLASSNAME + " wiz-vertical";
             setStyleName("");
         }
         add(captions);
 
         if (showProgressIndicatorBar) {
             Element barWrapperElement = DOM.createDiv();
             barWrapperElement.setClassName("bar-wrapper");
             getElement().appendChild(barWrapperElement);
 
             barElement = DOM.createDiv();
             barElement.setClassName("bar");
             barWrapperElement.appendChild(barElement);
         }
 
         initialized = true;
     }
 
     /**
      * Called whenever an update is received from the server
      */
     public void update() {
 
         int offsetWidth = getOffsetWidth();
         // BUG(?!) if component is 100% height at server side, no offset height
         // is found the first round trip
         int offsetHeight = getOffsetHeight();
 
         if (!initialized) {
             init(_isHorizontal, _showProgressIndicatorBar);
         }
 
         int numberOfSteps = _steps.size();
         double stepWidth = offsetWidth / (double) numberOfSteps;
         double stepHeight = offsetHeight / (double) numberOfSteps;
         int totalHeight = 0;
         int i = -1;
         for (SerializableStep step : _steps) {
             i++;
 
             String stepId = step.stepid;
 
             ProgressBarItem item;
             if (captions.getWidgetCount() > i) {
                 // get the existing widget for updating
                 item = (ProgressBarItem) captions.getWidget(i);
             } else {
                 // create new widget and add it to the layout
                 item = new ProgressBarItem(i + 1, stepId,
                         progressBarItemClickHandler);
                 captions.add(item);
             }
 
             boolean clickableStep = isLinkStep(_linkmode, step.current,
                     step.completed);
             item.setAsLink(clickableStep);
             item.setCaption(step.caption);
             int captionHeight = item.getCaptionElement().getOffsetHeight();
             totalHeight += captionHeight;
 
             // update the barElement width according to the current step
             if (_showProgressIndicatorBar && !_completed && step.current) {
                 if (_isHorizontal) {
                     barElement.getStyle().setWidth(
                             (i + 1) * stepWidth - stepWidth / 2, Unit.PX);
                 } else {
 
                     if (_hasVerticalSpacing) {
                         barElement.getStyle().setHeight(
                                 (i + 1) * stepHeight - stepHeight
                                         + captionHeight / 2, Unit.PX);
                     } else {
                         barElement.getStyle().setHeight(
                                 totalHeight - captionHeight / 2, Unit.PX);
                     }
                 }
             }
             if (_isHorizontal) {
                 item.setWidth(stepWidth + "px");
             } else {
                 if (_hasVerticalSpacing) {
                     item.setHeight(stepHeight + "px");
                 }
             }
 
             boolean first = (i == 0);
             boolean last = (i == _steps.size() - 1);
             updateStyleNames(step, item, first, last, _linkmode);
         }
 
         if (_showProgressIndicatorBar && _completed) {
             if (_isHorizontal) {
                 barElement.getStyle().setWidth(100, Unit.PCT);
             } else {
                 barElement.getStyle().setHeight(100, Unit.PCT);
             }
         }
     }
 
     private boolean isLinkStep(String linkmode, boolean isCurrentStep,
             boolean isCompletedStep) {
         if (linkmode.equals("none")) {
             return false;
         }
 
         if (linkmode.equals("previous") && !isCurrentStep && isCompletedStep) {
             return true;
         }
         if (linkmode.equals("all") && !isCurrentStep) {
             return true;
         }
 
         return false;
     }
 
     private void updateStyleNames(SerializableStep step, ProgressBarItem item,
             boolean first, boolean last, String linkmode) {
         if (step.completed) {
             item.addStyleName("completed");
         } else {
             item.removeStyleName("completed");
         }
         if (step.current) {
             item.addStyleName("current");
         } else {
             item.removeStyleName("current");
         }
         if (first) {
             item.addStyleName("first");
         } else {
             item.removeStyleName("first");
         }
         if (last) {
             item.addStyleName("last");
         } else {
             item.removeStyleName("last");
         }
 
         if (linkmode.equals("all")) {
             item.addStyleName("all-linkmode");
         } else {
             item.removeStyleName("all-linkmode");
         }
     }
 
     private static class ProgressBarItem extends Widget {
 
         private final int index;
         private Element captionElement;
         private boolean asLink;
 
         public ProgressBarItem(int index, final String stepId,
                 final ProgressBarItemClickHandler handler) {
             Element root = Document.get().createDivElement();
             setElement(root);
             setStyleName("step");
             this.index = index;
             captionElement = Document.get().createDivElement();
             root.appendChild(captionElement);
 
             addDomHandler(new ClickHandler() {
 
                 @Override
                 public void onClick(ClickEvent event) {
 
                     if (asLink) {
                        handler.onProgressBarItemClicked(stepId);
                     } else {
                         System.out.println("Clicked, but not as a link");
                     }
 
                 }
             }, ClickEvent.getType());
         }
 
         public void setCaption(String caption) {
 
             captionElement.setClassName("step-caption");
             captionElement.setInnerHTML("<span>" + index + ".</span> "
                     + caption);
 
         }
 
         protected Element getCaptionElement() {
             return captionElement;
         }
 
         public boolean isAsLink() {
             return asLink;
         }
 
         public void setAsLink(boolean asLink) {
             this.asLink = asLink;
             if (asLink) {
                 addStyleName("link");
             } else {
                 removeStyleName("link");
             }
         }
     }
 
     public void set_isHorizontal(boolean _isHorizontal) {
         this._isHorizontal = _isHorizontal;
     }
 
     public void set_hasVerticalSpacing(boolean _hasVerticalSpacing) {
         this._hasVerticalSpacing = _hasVerticalSpacing;
     }
 
     public void set_completed(boolean _completed) {
         this._completed = _completed;
     }
 
     public void set_showProgressIndicatorBar(boolean _showProgressIndicatorBar) {
         this._showProgressIndicatorBar = _showProgressIndicatorBar;
     }
 
     public void set_linkmode(String _linkmode) {
         this._linkmode = _linkmode;
     }
 
     public void set_steps(ArrayList<SerializableStep> _steps) {
         this._steps = _steps;
     }
 
     @Override
     public void setProgressBarItemClickHandler(
             ProgressBarItemClickHandler handler) {
         progressBarItemClickHandler = handler;
 
     }
 
 }
