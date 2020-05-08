 package org.vaadin.hhe.nanoscrollpanel.gwt.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.json.client.JSONBoolean;
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class GwtNanoScrollPanel extends SimplePanel {
     
     public final Element contentNode = DOM.createDiv();
     
     private final JSONObject scrollerOptions = new JSONObject();
     
     private List<NanoScrollListener> listeners = new ArrayList<NanoScrollListener>();
     
     public GwtNanoScrollPanel() {
         this(GUID.get(15));
     }
     
     public GwtNanoScrollPanel(String id) {
         getElement().setId(id);
         getElement().addClassName("nano");
         // to enable ios native scroll
 //        getElement().getStyle().setProperty("WebkitOverflowScrolling", "touch");
         contentNode.addClassName("content");
         DOM.appendChild(getElement(), contentNode);
         
 //        sinkEvents(Event.KEYEVENTS);
     }
     
     private void setBooleanOption(String key, boolean value) {
         scrollerOptions.put(key, JSONBoolean.getInstance(value));
     }
     
     private void setIntegerOption(String key, int value) {
         scrollerOptions.put(key, new JSONNumber(value));
     }
     
     private void setStringOption(String key, String value) {
         scrollerOptions.put(key, new JSONString(value));
     }
     
     private void setElementOption(String key, Widget widget) {
         scrollerOptions.put(key, JSONParser.parseStrict("$(#"+widget.getElement().getId()+")"));
     }
     
     public void setFlash(boolean isFlash) {
         setBooleanOption(NanoScrollOption.FLASH.toString(), isFlash);
     }
     
     public void setFlashDelay(int flashDelay) {
         setIntegerOption(NanoScrollOption.FLASH_DELAY.toString(), flashDelay);
     }
     
     public void setPreventPageScrolling(boolean isPreventPageScrolling) {
         setBooleanOption(NanoScrollOption.PREVENT_PAGE_SCROLLING.toString(), isPreventPageScrolling);
     }
     
     public void setAlwaysVisible(boolean isAlwaysVisible) {
         setBooleanOption(NanoScrollOption.ALWAYS_VISIBLE.toString(), isAlwaysVisible);
     }
     
     public void setPanelClassName(String className) {
         setStringOption(NanoScrollOption.PANEL_CLASS.toString(), className);
     }
     
     public void setSliderClassName(String className) {
         setStringOption(NanoScrollOption.SLIDER_CLASS.toString(), className);
     }
     
     public void setContentClassName(String className) {
         setStringOption(NanoScrollOption.CONTENT_CLASS.toString(), className);
     }
     
     public void setScrollTop(int offset) {
         setIntegerOption(NanoScrollOption.SCROLL_TOP.toString(), offset);
     }
     
     public void setScrollBottom(int offset) {
         setIntegerOption(NanoScrollOption.SCROLL_BOTTOM.toString(), offset);
     }
     
     public void setScrollTo(Widget widget) {
         setElementOption(NanoScrollOption.SCROLL_TO.toString(), widget);
     }
     
     @Override
     protected Element getContainerElement() {
         return contentNode;
     }
     
     @Override
     public void onLoad() {
        buildScroller(this, getElement().getId(), scrollerOptions.getJavaScriptObject());
     }
     
     @Override
     public void setWidget(Widget w) {
         super.setWidget(w);
         updateScroller(getElement().getId());
     }
     
     @Override
     public void onUnload() {
         destroyScroller(getElement().getId());
     }
     
     public void addListener(NanoScrollListener l) {
         listeners.add(l);
     }
     
     public void removeListener(NanoScrollListener l) {
         listeners.remove(l);
     }
     
     private void onScrollEnd(Event e) {
         NanoScrollEvent nanoEvent = new NanoScrollEvent(e, this);
         for(NanoScrollListener l : listeners) {
             l.onScrollEnd(nanoEvent);
         }
     }
     
     private void onScrollTop(Event e) {
         NanoScrollEvent nanoEvent = new NanoScrollEvent(e, this);
         for(NanoScrollListener l : listeners) {
             l.onScrollTop(nanoEvent);
         }
     }
     
     public void setFocus(boolean focus) {
         if (focus) {
             getContainerElement().focus();
         } else {
             getContainerElement().blur();
         }
     }
     
 //    @Override
 //    public void onBrowserEvent(Event event) {
 //        super.onBrowserEvent(event);
 //        final int type = DOM.eventGetType(event);
 //        Logger l = Logger.getLogger("test");
 //        if (type == Event.ONKEYDOWN) {
 //            l.log(Level.SEVERE, "key down");
 //        } else if( type == Event.ONKEYUP) {
 //            l.log(Level.SEVERE, "key up");
 //        }
 //    }
     
     @Override
     public void setHeight(String height) {
         super.setHeight(height);
         triggerEvent(getElement().getId(), "heightChange");
     }
     
     @Override
     public void setWidth(String width) {
         super.setWidth(width);
         triggerEvent(getElement().getId(), "widthChange");
     }
     
     /*
      * JSNI methods 
      */
     // $wnd.$('#'+id).children('.pane').css("display", "block");
     private native void buildScroller(GwtNanoScrollPanel panel, String id, JavaScriptObject options) /*-{
        $wnd.$('#'+id).bind("scrollend", function(e) {
             panel.@org.vaadin.hhe.nanoscrollpanel.gwt.client.GwtNanoScrollPanel::onScrollEnd(Lcom/google/gwt/user/client/Event;)(e);
         });
        $wnd.$('#'+id).bind("scrolltop", function(e) {
             panel.@org.vaadin.hhe.nanoscrollpanel.gwt.client.GwtNanoScrollPanel::onScrollTop(Lcom/google/gwt/user/client/Event;)(e);
         });
        $wnd.$('#'+id).nanoScroller(options);
         
        $wnd.$('#'+id).find('.content').first().bind('DOMNodeInserted DOMNodeRemoved', function(e) {
            $wnd.$('#'+id).nanoScroller();
         });
        $wnd.$('#'+id).bind('heightChange', function(e) {
            $wnd.$('#'+id).nanoScroller();
         });
     }-*/;
     
     private native void updateScroller(String id) /*-{
         $wnd.$('#'+id).nanoScroller();
     }-*/;
     
     private native void destroyScroller(String id) /*-{
         $wnd.$('#'+id).nanoScroller({
             destroy : true
         });
     }-*/;
     
     private native void triggerEvent(String id, String eventType) /*-{
         $wnd.$('#'+id).trigger(eventType);
     }-*/;
 }
