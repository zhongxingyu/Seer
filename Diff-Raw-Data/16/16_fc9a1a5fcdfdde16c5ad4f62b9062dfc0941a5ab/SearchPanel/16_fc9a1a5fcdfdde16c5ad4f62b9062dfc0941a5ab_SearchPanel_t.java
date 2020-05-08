 package org.mule.galaxy.web.client;
 
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.SearchPredicate;
 
 public class SearchPanel
     extends Composite
 {
     private FlowPanel panel;
     private Set rows;
     private Collection artifactIndexes;
     private Map artifactPropertyMap;
     private Button searchButton;
     private RegistryPanel registryPanel;
     private Button clearButton;
     private InlineFlowPanel buttonPanel;
     private Hyperlink freeformQueryLink;
     private TextArea freeformQueryArea;
 
     public SearchPanel(RegistryPanel rp) {
         registryPanel = rp;
         rows = new HashSet();
         
         panel = new FlowPanel();
         panel.setStyleName("search-panel");
         
         registryPanel.getRegistryService().getIndexes(new AbstractCallback(registryPanel) {
 
             public void onSuccess(Object o) {
                 initArtifactIndices((Collection) o);
             }
             
         });
         registryPanel.getRegistryService().getPropertyList(new AbstractCallback(registryPanel) {
             
             public void onSuccess(Object o) {
                 initArtifactProperties((Map) o);
             }
         });
 
         freeformQueryArea = new TextArea();
         freeformQueryArea.setCharacterWidth(83);
         freeformQueryArea.setVisibleLines(7);
         freeformQueryLink = new Hyperlink("Use Freeform Query", "customQuery");
         freeformQueryLink.addClickListener(new ClickListener() {
             public void onClick(Widget sender) {
                 showHideFreeformQuery();
             }
         });
         
         searchButton = new Button("Search", new ClickListener() {
            public void onClick(Widget sender) {
                registryPanel.refreshArtifacts();
            }
         });
         
         clearButton = new Button("Clear", new ClickListener() {
             public void onClick(Widget sender) {
                clear();
                 panel.clear();
                 freeformQueryArea.setText("");
                 
                 addPredicate();
             }
          });
         
         addPredicate();
         
         initWidget(panel);
     }


    public void clear() {
        rows.clear();
    }
     public void initArtifactIndices(Collection c) {
         artifactIndexes = c;
         for (Iterator itr = rows.iterator(); itr.hasNext();) {
             SearchPanelRow row = (SearchPanelRow) itr.next();
             row.addPropertySet("Indexes", artifactIndexes);
         }
     }
     
     public void initArtifactProperties(Map map) {
         artifactPropertyMap = map;
         for (Iterator itr = rows.iterator(); itr.hasNext();) {
             SearchPanelRow row = (SearchPanelRow) itr.next();
             row.addPropertySet("Properties", artifactPropertyMap);
         }
     }
     
     public void addPredicate() {
         SearchPanelRow pred = new SearchPanelRow(this);
         if (artifactIndexes != null)
             pred.addPropertySet("Indexes", artifactIndexes);
         if (artifactPropertyMap != null)
             pred.addPropertySet("Properties", artifactPropertyMap);
         
         // Add the search button if we're adding our first row
         if (rows.size() == 0) {
             buttonPanel = new InlineFlowPanel();
             buttonPanel.setStyleName("search-button-panel");
             buttonPanel.add(freeformQueryLink);
             buttonPanel.add(searchButton);
             buttonPanel.add(clearButton);
             panel.add(buttonPanel);
         }
         
         panel.insert(pred, panel.getWidgetIndex(buttonPanel));
         rows.add(pred);
     }
     
     public void removePredicate(SearchPanelRow pred) {
         panel.remove(pred);
         rows.remove(pred);
         
         // Add a new predicate if we're removing our last row
         if (rows.size() == 0) {
             addPredicate();
         }
     }
     
     public void showHideFreeformQuery() {
         if (panel.remove(freeformQueryArea)) {
             freeformQueryArea.setText("");
             freeformQueryLink.setText("Use Freeform Query");
             
             // Clear the panel because addPredicate will add everything back
             panel.clear();
             addPredicate();
         }
         else {
             panel.insert(freeformQueryArea, 0);
             freeformQueryArea.setText("Add a custom query...");
             freeformQueryArea.selectAll();
             freeformQueryLink.setText("Use Structured Query");
             
             // Remove all the structured query rows
             for (Iterator iter=rows.iterator(); iter.hasNext();)
                 panel.remove((Widget) iter.next());
             rows.clear();
         }
     }
 
     public Set getPredicates()
     {
         Set predicates = new HashSet();
         
         for (Iterator itr = rows.iterator(); itr.hasNext();) {
             SearchPanelRow row = (SearchPanelRow) itr.next();
             SearchPredicate pred = row.getPredicate();
             if (pred != null)
                 predicates.add(pred);
         }
         
         return predicates;
     }
 
     public String getFreeformQuery()
     {
         return freeformQueryArea.getText();
     }
 }
