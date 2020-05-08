 package org.cloudlet.web.core.client.mobile;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.InputElement;
 import com.google.gwt.dom.client.UListElement;
 import com.google.gwt.event.logical.shared.HasSelectionHandlers;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Widget;
 
 import org.cloudlet.web.core.client.CompositeView;
 import org.cloudlet.web.core.client.EventGenerator;
 import org.cloudlet.web.core.client.EventGenerator.Function;
 
 public class HeaderView extends CompositeView implements SelectionHandler<Integer>, HasSelectionHandlers<Integer> {
 
   interface Binder extends UiBinder<Widget, HeaderView> {
   }
 
   interface HeaderViewStyle extends CssResource {
     String inputting();
 
     String navactive();
   }
 
   private static Binder binder = GWT.create(Binder.class);
 
   @UiField
   DivElement logoElm;
   @UiField
   UListElement navUlElm;
   @UiField
   InputElement searchTextElm;
 
   @UiField
   HeaderViewStyle style;
 
  private Integer oldInteger;
 
   @Override
   public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
     return addHandler(handler, SelectionEvent.getType());
   }
 
   @Override
   public void onSelection(SelectionEvent<Integer> event) {
     Integer newIndex = event.getSelectedItem();
     setSelection(newIndex, true);
    setSelection(oldInteger, false);
    oldInteger = newIndex;
     switch (newIndex) {
       case 1:
         // Window.alert("index");
         break;
       case 3:
         // Window.alert("rank");
         break;
       case 5:
         // Window.alert("bargain");
         break;
       case 7:
         // Window.alert("category");
         break;
       case 9:
         // Window.alert("client");
         break;
       case 11:
         // Window.alert("bbs");
         break;
       default:
         break;
     }
   }
 
   @Override
   protected Widget initView() {
     return binder.createAndBindUi(this);
   }
 
   @Override
   protected void start() {
     final Element aElm = searchTextElm.getParentElement();
 
     EventGenerator.onClick(logoElm, new Function() {
       @Override
       public void f(Event e) {
         Window.alert("index!");
       }
     });
 
     EventGenerator.onKeyUp(searchTextElm, new Function() {
       @Override
       public void f(Event e) {
        String value = searchTextElm.getValue();
        if (value.length() == 0) {
          aElm.removeClassName(style.inputting());
          return;
        }
        if (value.length() > 0) {
           aElm.addClassName(style.inputting());
         }
       }
     });
     EventGenerator.onFocus(searchTextElm, new Function() {
       @Override
       public void f(Event e) {
         aElm.addClassName(style.inputting());
       }
     });
     EventGenerator.onBlur(searchTextElm, new Function() {
       @Override
       public void f(Event e) {
         if (searchTextElm.getValue().length() > 0) {
           return;
         }
         aElm.removeClassName(style.inputting());
       }
     });
 
     addSelectionHandler(this);
 
     for (int i = 1; i < navUlElm.getChildCount(); i += 2) {
       Element elm = navUlElm.getChild(i).<Element> cast();
       bindLiEvent(elm, i);
     }
 
   }
 
   private void bindLiEvent(final Element elm, final int i) {
     EventGenerator.onClick(elm, new Function() {
       @Override
       public void f(Event e) {
         SelectionEvent.fire(HeaderView.this, new Integer(i));
       }
     });
   }
 
   private void setSelection(Integer i, boolean isAdd) {
    if (i == null) {
       return;
     }
     Element elm = navUlElm.getChild(i).<Element> cast();
     if (isAdd) {
       elm.addClassName(style.navactive());
     } else {
       elm.removeClassName(style.navactive());
     }
   }
 }
