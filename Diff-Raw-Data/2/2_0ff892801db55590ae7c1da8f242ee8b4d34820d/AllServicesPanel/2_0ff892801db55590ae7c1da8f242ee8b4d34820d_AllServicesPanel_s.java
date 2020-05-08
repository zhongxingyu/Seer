 /**
  * 
  */
 package edu.wustl.cab2b.client.ui.mysettings;
 
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.DIALOG_CLOSE_EVENT;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.SEARCH_EVENT;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.SERVICE_SELECT_EVENT;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Vector;
 
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import org.jdesktop.swingx.JXTitledPanel;
 import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
 
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.wustl.cab2b.client.serviceinstances.ServiceInstanceConfigurator;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTitledPanel;
 import edu.wustl.cab2b.client.ui.pagination.JPagination;
 import edu.wustl.cab2b.client.ui.pagination.NumericPager;
 import edu.wustl.cab2b.client.ui.pagination.PageElement;
 import edu.wustl.cab2b.client.ui.pagination.PageElementImpl;
 
 /**
  * @author atul_jawale
  *  This class is used to show list of all the available services (i.e. loaded models)
  */
 
 public class AllServicesPanel extends Cab2bPanel implements ActionListener {
 
     private static final long serialVersionUID = -1709041822051570137L;
 
     private final Collection<EntityGroupInterface> allServices = new ArrayList<EntityGroupInterface>();
 
     private JXTitledPanel titledSearchResultsPanel;
 
     /**
      * This is the action listener will fire the property change event on allservicepanel 
      * object and is handled in the rightpanel.
      * @param event
      */
     public void actionPerformed(ActionEvent event) {
         final String serviceName = event.getActionCommand();
         firePropertyChange(SERVICE_SELECT_EVENT, this, serviceName);
     }
 
     /**
      * 
      */
     public AllServicesPanel() {
         super(new BorderLayout());
         allServices.addAll(new ServiceInstanceConfigurator().getMetadataEntityGroups());
         initGUI();
     }
 
     /**
      * This constructor is used in the third step of the search data wizard
      * and passed the collection of the involved entities in the query. 
      * @param selectedServices
      */
     public AllServicesPanel(Collection<EntityGroupInterface> selectedServices) {
         super(new BorderLayout());
         this.allServices.addAll(selectedServices);
         initGUI();
     }
 
     /**
      * This method is used to initialize the UI.
      * Will fetch all the loaded models from the database and then create the panel to display services in 
      * Pagination.   
      */
     private void initGUI() {
 
         final SearchPanel searchPanel = new SearchPanel();
         Cab2bPanel resultPanel = null;
 
         final Collection<EntityGroupInterface> filteredServices = new ArrayList<EntityGroupInterface>();
         filteredServices.addAll(allServices);
 
         /*check if allServices has some services then */
         if (allServices.size() > 0) {
             resultPanel = generateContentPanel(filteredServices);
         } else {
             Cab2bLabel noResultLabel = new Cab2bLabel("No instance found.");
             noResultLabel.setForeground(Color.blue);
 
             resultPanel = new Cab2bPanel();
             resultPanel.add(noResultLabel);
             searchPanel.setEnable(false);
         }
 
         /* Now creating the title */
         titledSearchResultsPanel = new Cab2bTitledPanel("Available Services(" + filteredServices.size() + ")");
         GradientPaint gp = new GradientPaint(new Point2D.Double(.05d, 0), new Color(185, 211, 238),
                 new Point2D.Double(.95d, 0), Color.WHITE);
         titledSearchResultsPanel.setTitlePainter(new BasicGradientPainter(gp));
         titledSearchResultsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
         titledSearchResultsPanel.setTitleFont(new Font("SansSerif", Font.BOLD, 11));
         titledSearchResultsPanel.setTitleForeground(Color.BLACK);
         titledSearchResultsPanel.setContentContainer(resultPanel);
 
         /* topPanel contains the SERVICE URLs Label and search Panel*/
         final Cab2bLabel serviceURL = new Cab2bLabel("Service URLs");
         Font font = serviceURL.getFont();
         serviceURL.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));
         serviceURL.setForeground(new Color(185, 211, 238));
 
         searchPanel.addPropertyChangeListener(SEARCH_EVENT, new PropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent event) {
                 final String searchString = (String) event.getNewValue();
                 searchResult(searchString, filteredServices);
 
                 final Cab2bPanel contentPanel = generateContentPanel(filteredServices);
                 titledSearchResultsPanel.setContentContainer(contentPanel);
 
                 final String title = "Searched Services (" + filteredServices.size() + ")";
                 titledSearchResultsPanel.setTitle(title);
                 revalidate();
             }
         });
 
         final JPanel topPanel = new JPanel(new BorderLayout());
         topPanel.add(serviceURL, BorderLayout.NORTH);
         topPanel.add(searchPanel, BorderLayout.CENTER);
         topPanel.setBackground(Color.white);
         FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
         flowLayout.setHgap(10);
         Cab2bPanel buttonPanel = new Cab2bPanel();
         buttonPanel.setBackground(null);
         buttonPanel.setLayout(flowLayout);
         Cab2bButton closeButton = new Cab2bButton("Close");
         closeButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 firePropertyChange(DIALOG_CLOSE_EVENT, true, false);
             }
         });
         buttonPanel.add(closeButton);
 
         /* Now adding all the components to this panel*/
         add(topPanel, BorderLayout.NORTH);
         add(titledSearchResultsPanel, BorderLayout.CENTER);
         this.add(buttonPanel, BorderLayout.SOUTH);
     }
 
     /**
      * This method actually generates main content panel which displays the service names to the 
      * user    
      * @param resultPanel
      */
     private Cab2bPanel generateContentPanel(Collection<EntityGroupInterface> filteredServices) {
         final Cab2bPanel resultPanel = new Cab2bPanel();
         final Vector<PageElement> pageElementCollection = populatePageElements(filteredServices, null, null);
 
         /* Initialize the pagination component. */
         final NumericPager numericPager = new NumericPager(pageElementCollection, 10);
         final JPagination resultsPage = new JPagination(pageElementCollection, numericPager, this, true);
         resultsPage.setSelectableEnabled(false);
         resultsPage.setGroupActionEnabled(false);
         resultsPage.addPageElementActionListener(this);
 
         resultPanel.add("hfill vfill ", resultsPage);
         return resultPanel;
     }
 
     /**
      * This method will search the user search string in every service's name and will
      * add the service inside the filterdServices collection .  
      * @param searchString
      * 
      */
     private void searchResult(String searchString, Collection<EntityGroupInterface> filteredServices) {
         filteredServices.clear();
         if (null != allServices) {
             if ("ShowAll".equals(searchString)) {
                 filteredServices.addAll(allServices);
             }
             searchString = searchString.toLowerCase();
             for (EntityGroupInterface metadata : allServices) {
                 String serviceName = metadata.getLongName().toLowerCase();
                 if (serviceName.contains(searchString)) {
                     filteredServices.add(metadata);
                 }
             }
         }
     }
 
     private Vector<PageElement> populatePageElements(Collection<EntityGroupInterface> filteredServices,
                                                      String serviceName, String status) {
         final Vector<PageElement> pageElementCollection = new Vector<PageElement>();
         for (EntityGroupInterface entityGroup : filteredServices) {
             // Create an instance of the PageElement. Initialize with the appropriate data
             final StringBuilder displayString = new StringBuilder(entityGroup.getLongName());
             final String version = entityGroup.getVersion();
             if (null != version && !version.isEmpty()) {
                 displayString.append(" v" + version);
             }
            final PageElement pageElement = new PageElementImpl();
             pageElement.setDisplayName(displayString.toString());
             pageElement.setDescription(entityGroup.getDescription());
             pageElement.setUserObject(entityGroup);
 
             if (null != status && displayString.toString().equals(serviceName)) {
                 pageElement.setExtraDisplayText(status);
             }
             pageElementCollection.add(pageElement);
         }
         return pageElementCollection;
     }
 
     /**
      * @param serviceName
      * @param status
      */
     public void refreshPanel(String serviceName, String status) {
         Cab2bPanel resultPanel = new Cab2bPanel();
         Collection<EntityGroupInterface> filteredServices = new ArrayList<EntityGroupInterface>();
         filteredServices.addAll(allServices);
         final Vector<PageElement> pageElementCollection = populatePageElements(filteredServices, serviceName,
                                                                                status);
 
         /* Initialize the pagination component. */
         final NumericPager numericPager = new NumericPager(pageElementCollection, 10);
         final JPagination resultsPage = new JPagination(pageElementCollection, numericPager, this, true);
         resultsPage.setSelectableEnabled(false);
         resultsPage.setGroupActionEnabled(false);
         resultsPage.addPageElementActionListener(this);
         resultPanel.add("hfill vfill ", resultsPage);
         titledSearchResultsPanel.setContentContainer(resultPanel);
         titledSearchResultsPanel.revalidate();
     }
 }
