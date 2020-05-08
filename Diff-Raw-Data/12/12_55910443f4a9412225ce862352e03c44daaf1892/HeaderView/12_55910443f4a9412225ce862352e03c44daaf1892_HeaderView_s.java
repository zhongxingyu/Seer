 package com.jci.client.application.ui;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.query.client.Function;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.jci.client.resource.header.HeaderResource;
 
 import javax.inject.Inject;
 
 import static com.google.gwt.query.client.GQuery.$;
 
 public class HeaderView extends Composite {
     public interface Binder extends UiBinder<HTMLPanel, HeaderView> {
     }
 
     @UiField
    HTMLPanel menu;
 
     private final String activeStyleName;
 
     @Inject
     public HeaderView(Binder binder, HeaderResource headerResource) {
         initWidget(binder.createAndBindUi(this));
 
         activeStyleName = headerResource.style().active();
     }
 
     @Override
     protected void onAttach() {
         $("a", menu).click(new Function() {
             @Override
             public void f(Element e) {
                 $("." + activeStyleName).removeClass(activeStyleName);
                 $(e).addClass(activeStyleName);
             }
         });
     }
 }
