 package com.java.gwt.libertycinema.client;
 
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 
 import com.java.gwt.libertycinema.client.BodyPanel;
 
 
 public class LibertyCinema implements EntryPoint {
 
     private DockLayoutPanel main = new DockLayoutPanel(Unit.EM);
     private BodyPanel body = new BodyPanel();
 
     private FooterBar footer;
     private TopNavBar header;
 
     public void onModuleLoad() {
        // Setup Topbar
        header = new TopNavBar(body);
        main.addNorth(header.getTopBarPanel(), 7);
 
        // Setup footer
        footer = new FooterBar(body);
        main.addSouth(footer.getFooterPanel(), 2);
 
        // Setup main
        main.add(body);
 
        RootLayoutPanel.get().add(main);
     }
 }
