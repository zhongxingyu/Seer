 package org.geoserver.monitor.web;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
 import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.ResourceModel;
 
 public class OWSSummaryPage extends MonitorBasePage {
 
     public OWSSummaryPage() {
         List tabs = new ArrayList();
         tabs.add(new AbstractTab(new ResourceModel("overview")) {
             @Override
             public Panel getPanel(String panelId) {
                 return new OWSOverviewPanel(panelId, getMonitor(), null);
             }
         });
         tabs.add(new AbstractTab(new ResourceModel("wfs")) {
             @Override
             public Panel getPanel(String panelId) {
                return new OWSDetailsPanel(panelId, getMonitor(), "wfs");
             }
         });
         tabs.add(new AbstractTab(new ResourceModel("wms")) {
             @Override
             public Panel getPanel(String panelId) {
                return new OWSDetailsPanel(panelId, getMonitor(), "wms");
             }
         });
         tabs.add(new AbstractTab(new ResourceModel("wcs")) {
             @Override
             public Panel getPanel(String panelId) {
                return new OWSDetailsPanel(panelId, getMonitor(), "wcs");
             }
         });
         add(new TabbedPanel("charts", tabs));
         
     }
 }
