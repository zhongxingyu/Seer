 /**
  * 
  */
 package org.mule.galaxy.web.client.util;
 
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.event.TabPanelEvent;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.List;
 
 import org.mule.galaxy.web.client.ErrorPanel;
 import org.mule.galaxy.web.client.Showable;
 
 public class ShowableTabListener extends SelectionListener<TabPanelEvent> {
     private TabItem previous;
     private List<String> params;
     private List<String> previousParams;
     private final ErrorPanel errorPanel;
     private final List<String> tabNames;
     private final TabPanel tabPanel;
     private final String urlBase;
     
     public ShowableTabListener(TabPanel tabPanel,
                                ErrorPanel errorPanel, 
                                String urlBase,
                                List<String> params,
                                List<String> tabNames) {
         super();
         this.tabPanel = tabPanel;
         this.errorPanel = errorPanel;
         this.urlBase = urlBase;
         this.params = params;
         this.tabNames = tabNames;
     }
 
     @Override
     public void componentSelected(TabPanelEvent ce) {
         TabItem item = ce.getItem();
         
         // Nasty trickery because GXT triggers tab selection events for every click on the screen
         if (item.equals(previous) && params.equals(previousParams)) {
             return;
         }
         
         if (previous != null) {
             Widget widget = previous.getWidget(0);
             if (widget instanceof Showable) {
                 ((Showable)widget).hidePage();
             }
         }
         
         errorPanel.clearErrorMessage();
         Widget widget = item.getWidget(0);
         if (widget instanceof Showable) {
             ((Showable)widget).showPage(params);
         }
         
         // Update the History token once the tab is selected.
         int tabIndex = tabPanel.indexOf(item);
         if (tabNames != null && tabIndex < tabNames.size()) {
             String name = tabNames.get(tabIndex);
             History.newItem(urlBase + "/" + name, false);
         }
         
         previous = item;
         // Once we've shown a panel, store the previous params. We aren't going to trigger a new tab  
         // selection event again until we get new params.
         previousParams = params;
         item.layout();
     }
 
     /**
      * Update the parameters from a new history event. Call before calling showTab.
      * @param params
      */
     public void setParams(List<String> params) {
         this.params = params;
     }
 
     /**
      * Show a named tab. Handy when you have another page that manages history events and you have a tab
      * as a sub component which needs to be triggered on certain history events.
      * @param tabName
      */
     public void showTab(String tabName) {
         int idx = tabNames.indexOf(tabName);
         if (idx == -1) {
             idx = 0;
         }
         TabItem item = tabPanel.getItem(idx);
         Widget widget = item.getWidget(0);
         tabPanel.setSelection(item);
         if (widget instanceof Showable) {
             ((Showable)widget).showPage(params);
         }
         
         // Once we've shown a panel, store the previous params. We aren't going to trigger a new tab  
         // selection event again until we get new params.
         previousParams = params;
         previous = item;
     }
 }
