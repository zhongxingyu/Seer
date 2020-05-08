 /*
  * SearchResultsView.java
  *
  * Created on Feb 23, 2013, 3:59:05 PM
  */
 package Views.SearchResults;
 
 import Main.LocaleHandler;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.Timer;
 import se.chalmers.ait.dat215.project.Product;
 import se.chalmers.ait.dat215.project.ProductCategory;
 
 /**
  *
  * @author Peter
  */
 public class SearchResultsView extends javax.swing.JPanel {
 
     /**
      * The delay in milis between loading productViews if 
      * they also have to be created
      */
     public static final int LOAD_DELAY_CREATE = 50;
     /**
      * The delay in milis between loading productViews if 
      * they have already been created
      */
     public static final int LOAD_DELAY_SAVED = 15;
     /**
      * Default height for a category filter row. In px(?)
      */
     public static final int CATEGORY_FILTER_ROW_HEIGHT = 30;
     /**
      * List holding timers started for loading products, so they
      * can be canceled if new search.
      */
     private List<Timer> loadingTimers = new LinkedList<Timer>();
     /**
      * The controller for this view
      */
     private SearchResultsController srController;
     /**
      * A boolean for keeping track of if the filterPanel is open or closed.
      */
     private boolean filterPanelShown = false;
     /**
      * A list of SearchResultItemViews that has already been initialized. Used 
      * when possible instead of creating a new one.
      */
     private List<SearchResultItemView> resultItems = new LinkedList<SearchResultItemView>();
     /**
      * A label shown if there are no search results.
      */
     private JLabel noResulsLabel = new JLabel("Inga resultat");
 
     /** Creates a new form SearchResultsView */
     public SearchResultsView() {
         initComponents();
         searchResultsItemsScroll.getVerticalScrollBar().setUnitIncrement(20);
 
         srController = new SearchResultsController(this);
 
         // The filter container must be dynamically set to sizes.
         filterByContainer.setVisible(filterPanelShown);
         filterByContainer.setMinimumSize(new Dimension(200, 100));
         filterByContainer.setPreferredSize(new Dimension(200, 100));
 
         // Add and hide the "No search results" label.
         searchResultItemsContainer.add(noResulsLabel);
         noResulsLabel.setVisible(false);
     }
 
     /**
      * Sets the header of the search result view.
      * 
      * @param text 
      */
     public void setHeader(String text) {
         headerLabel.setText(text);
     }
 
     /**
      * Sets the list of results items to the products provided and redraws
      * the panel.
      * 
      * @param products 
      */
     public void setResultItems(List<Product> products) {
         // First we need to stop all running load timers
         for (Timer t : loadingTimers) {
             t.stop();
         }
 
         // Hide all itemviews we don't need
         if (products.size() < resultItems.size()) {
             for (int i = products.size(); i < resultItems.size(); i++) {
                 resultItems.get(i).setVisible(false);
             }
         }
 
         // Show or hide "no results" label
         noResulsLabel.setVisible(products.isEmpty());
 
         // Generate the list of results
         if (!products.isEmpty()) {
             int i = 0;
 
             // Then recreate the list
             loadingTimers = new LinkedList<Timer>();
            
            int delay = 0;
            
             // Then go trough each product and lay them out.
             for (final Product p : products) {
                 // Use a timer to load products over time instead
                 // of all at once. NO LAG! YEEES!
                 Timer t;
 
                 if (i < resultItems.size()) {
                     // If we have a created resultItem, use it instead as 
                     // creating new ones are very slow.
                     final SearchResultItemView rsiv = resultItems.get(i);
                    delay += LOAD_DELAY_SAVED;
                    t = new Timer(delay, new ActionListener() {
 
                         public void actionPerformed(ActionEvent ae) {
                             rsiv.setProduct(p);
                             rsiv.setVisible(true);
                         }
                     });
 
                 } else {
                     // If we don't have saved ones, create and save a new one.
                    delay += LOAD_DELAY_CREATE;
                    t = new Timer(delay, new ActionListener() {
 
                         public void actionPerformed(ActionEvent ae) {
                             SearchResultItemView rsiv = new SearchResultItemView();
                             resultItems.add(rsiv);
                             searchResultItemsContainer.add(rsiv);
                             rsiv.setProduct(p);
                             rsiv.setVisible(true);
                         }
                     });
                 }
 
                 // Start the timer for showing the product
                 t.setRepeats(false);
                 t.start();
 
                 // And add it to a list so we can stop it if search
                 // changes
                 loadingTimers.add(t);
 
                 i++;
             }
         }
 
         // Scroll to top in the scroll pane
         searchResultsItemsScroll.getVerticalScrollBar().setValue(0);
 
     }
 
     /**
      * Sets the sortBy ComboBox to the default value
      */
     public void resetSortBy() {
         sortByComboBox.setSelectedIndex(0);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         sortByContainer = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
         sortByComboBox = new javax.swing.JComboBox();
         searchResultsItemsScroll = new javax.swing.JScrollPane();
         searchResultItemsContainer = new javax.swing.JPanel();
         filterByContainer = new javax.swing.JPanel();
         toggleCategoriFilterButton = new javax.swing.JButton();
         headerLabel = new javax.swing.JLabel();
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(Main.MainApp.class).getContext().getResourceMap(SearchResultsView.class);
         setBackground(resourceMap.getColor("Form.background")); // NOI18N
         setMaximumSize(new java.awt.Dimension(750, 32767));
         setMinimumSize(new java.awt.Dimension(600, 200));
         setName("Form"); // NOI18N
         setPreferredSize(new java.awt.Dimension(750, 300));
 
         sortByContainer.setName("sortByContainer"); // NOI18N
         sortByContainer.setOpaque(false);
         sortByContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
 
         jLabel1.setLabelFor(sortByComboBox);
         jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
         jLabel1.setName("jLabel1"); // NOI18N
         sortByContainer.add(jLabel1);
 
         filler1.setName("filler1"); // NOI18N
         sortByContainer.add(filler1);
 
         sortByComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Namn A-Z", "Namn Z-A", "Pris Lågt-Högt", "Pris Högt-Lågt" }));
         sortByComboBox.setName("sortByComboBox"); // NOI18N
         sortByComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 sortByComboBoxActionPerformed(evt);
             }
         });
         sortByContainer.add(sortByComboBox);
 
         searchResultsItemsScroll.setBackground(resourceMap.getColor("searchResultsItemsScroll.background")); // NOI18N
         searchResultsItemsScroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         searchResultsItemsScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         searchResultsItemsScroll.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         searchResultsItemsScroll.setName("searchResultsItemsScroll"); // NOI18N
         searchResultsItemsScroll.setOpaque(false);
 
         searchResultItemsContainer.setBackground(resourceMap.getColor("searchResultItemsContainer.background")); // NOI18N
         searchResultItemsContainer.setName("searchResultItemsContainer"); // NOI18N
         searchResultItemsContainer.setLayout(new javax.swing.BoxLayout(searchResultItemsContainer, javax.swing.BoxLayout.PAGE_AXIS));
         searchResultsItemsScroll.setViewportView(searchResultItemsContainer);
 
         filterByContainer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         filterByContainer.setMinimumSize(new java.awt.Dimension(0, 0));
         filterByContainer.setName("filterByContainer"); // NOI18N
         filterByContainer.setOpaque(false);
         filterByContainer.setLayout(new java.awt.GridLayout(0, 4));
 
         toggleCategoriFilterButton.setText(resourceMap.getString("toggleCategoriFilterButton.text")); // NOI18N
         toggleCategoriFilterButton.setName("toggleCategoriFilterButton"); // NOI18N
         toggleCategoriFilterButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 toggleCategoriFilterButtonActionPerformed(evt);
             }
         });
 
         headerLabel.setFont(resourceMap.getFont("headerLabel.font")); // NOI18N
         headerLabel.setText(resourceMap.getString("headerLabel.text")); // NOI18N
         headerLabel.setName("headerLabel"); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(searchResultsItemsScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
                     .addComponent(filterByContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
                     .addComponent(sortByContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
                     .addComponent(toggleCategoriFilterButton)
                     .addComponent(headerLabel))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(headerLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(sortByContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(toggleCategoriFilterButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(filterByContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(searchResultsItemsScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
 private void sortByComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByComboBoxActionPerformed
     // When the sort by drop down is changed
     srController.onSortByChanged();
 }//GEN-LAST:event_sortByComboBoxActionPerformed
 
 private void toggleCategoriFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleCategoriFilterButtonActionPerformed
     // When one of the filter checkboxes are clicked
     filterPanelShown = !filterPanelShown;
     filterByContainer.setVisible(filterPanelShown);
 
     toggleCategoriFilterButton.setText(filterPanelShown ? "Dölj Filter" : "Visa Filter");
 
     filterByContainer.validate();
     filterByContainer.repaint();
 }//GEN-LAST:event_toggleCategoriFilterButtonActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.Box.Filler filler1;
     private javax.swing.JPanel filterByContainer;
     private javax.swing.JLabel headerLabel;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JPanel searchResultItemsContainer;
     private javax.swing.JScrollPane searchResultsItemsScroll;
     private javax.swing.JComboBox sortByComboBox;
     private javax.swing.JPanel sortByContainer;
     private javax.swing.JButton toggleCategoriFilterButton;
     // End of variables declaration//GEN-END:variables
 
     /**
      * Get the selected sort by index.
      * @return 
      */
     public int getSortBySelectedIndex() {
         return sortByComboBox.getSelectedIndex();
     }
 
     /**
      * Updates the list of available categories to filter by
      * 
      * @param resultCategories 
      */
     void setCategoryFilters(Set<ProductCategory> productCategories) {
         filterByContainer.removeAll();
 
         // Calculate size depending on number of categories and number of columns
         // in the grid layout.
         int numRows = productCategories.size();
         numRows /= ((GridLayout) filterByContainer.getLayout()).getColumns();
         numRows += 1;
 
         filterByContainer.setPreferredSize(new Dimension(500,
                 SearchResultsView.CATEGORY_FILTER_ROW_HEIGHT * numRows));
 
         // If we have no categories, show a label saying so.
         if (productCategories.isEmpty()) {
             filterByContainer.add(new JLabel("Inga kategorier."));
         }
 
         // Add a checkbox for each category.
         for (ProductCategory pc : productCategories) {
             String categoryName = LocaleHandler.INSTANCE.getProductCategoryName(pc);
             JCheckBox cb = new JCheckBox(categoryName, false);
             cb.setOpaque(false);
 
             // Add an event listener for when this checkbox is clicked
             final ProductCategory category = pc;
             final SearchResultsController src = this.srController;
             cb.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     JCheckBox cb = (JCheckBox) e.getSource();
                     src.onFilterChanged(category, cb.isSelected());
                 }
             });
 
             filterByContainer.add(cb);
         }
 
         // Have to refresh both the container and the main panel
         filterByContainer.validate();
         filterByContainer.repaint();
         validate();
         repaint();
     }
 }
