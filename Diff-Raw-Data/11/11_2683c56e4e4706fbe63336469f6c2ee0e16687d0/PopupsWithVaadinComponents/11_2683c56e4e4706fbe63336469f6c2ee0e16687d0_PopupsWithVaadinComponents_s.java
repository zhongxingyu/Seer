 package org.vaadin.vol.demo;
 
 import org.vaadin.vol.OpenLayersMap;
 import org.vaadin.vol.OpenStreetMapLayer;
 import org.vaadin.vol.Popup;
 import org.vaadin.vol.Popup.PopupStyle;
 
 import com.vaadin.ui.Button;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.Button.ClickEvent;
 
 public class PopupsWithVaadinComponents extends AbstractVOLTest {
 
     @Override
     public String getDescription() {
         return "Reloads should work with Popups.";
     }
 
     @Override
     Component getMap() {
         final OpenLayersMap map = new OpenLayersMap();
         map.setSizeFull();
         map.addLayer(new OpenStreetMapLayer());
 
         final Popup popup = new Popup("FOOBAR");
         popup.setPopupStyle(PopupStyle.FRAMED_CLOUD);
         popup.setClosable(false);
         map.addPopup(popup);
         
         Popup popup2 = new Popup();
         CssLayout cssLayout = new CssLayout();
         popup2.setLon(30);
         popup2.addComponent(cssLayout);
         cssLayout.addComponent(new CheckBox("Checkbox"));
         cssLayout.addComponent(new TextField("TextField"));
         Button c = new Button("Vaadin button");
         c.addListener(new Button.ClickListener() {
            @Override
             public void buttonClick(ClickEvent event) {
                 getWindow().showNotification("Use any vaadin stuff here like in a window");
             }
         });
         
         cssLayout.addComponent(c);
         map.addPopup(popup2);
  
         popup2 = new Popup();
         popup2.setLat(30);
         
         Table table = new Table();
         table.setWidth("200px");
         table.setHeight("100px");
         table.addContainerProperty("foo", String.class, "jep");
         table.addContainerProperty("bar", String.class, "joo");
         for(int i=0; i < 100 ; i++) {
             table.addItem();
         }
         popup2.setContent(table);
         
         map.addPopup(popup2);
  
 
         return map;
     }
 
 }
