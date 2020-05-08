 /*
  * Created on Apr 1, 2006
  */
 package com.alertscape.browser.ui.swing.panel.collection.table;
 
 import java.awt.BorderLayout;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.TableCellRenderer;
 
 import ca.odell.glazedlists.GlazedLists;
 import ca.odell.glazedlists.SortedList;
 import ca.odell.glazedlists.gui.TableFormat;
 import ca.odell.glazedlists.swing.EventTableModel;
 import ca.odell.glazedlists.swing.TableComparatorChooser;
 
 import com.alertscape.browser.ui.swing.panel.collection.AlertCollectionPanel;
 import com.alertscape.browser.ui.swing.panel.collection.table.renderer.DateAlertCellRenderer;
 import com.alertscape.browser.ui.swing.panel.collection.table.renderer.DefaultAlertCellRenderer;
 import com.alertscape.browser.ui.swing.panel.collection.table.renderer.SeverityAlertCellRenderer;
 import com.alertscape.common.model.Alert;
 import com.alertscape.common.model.AlertCollection;
 import com.alertscape.common.model.severity.Severity;
 
 /**
  * @author josh
  * @version $Version: $
  */
 public class AlertCollectionTablePanel extends JPanel implements
     AlertCollectionPanel
 {
   private static final long serialVersionUID = 1L;
 
   private JTable collectionTable;
   private AlertCollection collection;
   private SortedList<Alert> sortedList;
 
   // private EventCollectionTableModel model;
   // private TableRowSorter<EventCollectionTableModel> sorter;
 
   public AlertCollectionTablePanel(AlertCollection collection)
   {
     setCollection(collection);
     init( );
   }
 
   public void init( )
   {
     sortedList = new SortedList<Alert>(getCollection( ).getEventList( ), null);
     //String[] propertyNames = new String[] { "alertId", "type",
 //        "longDescription", "severity", "count", "source", "item",
 //        "itemManager", "itemType", "itemManagerType", "firstOccurence",
 //        "lastOccurence" };
     //String[] columnLabels = new String[] { "Alert ID", "Type", "Description",
 //        "Severity", "Count", "Source", "Item", "Manager", "Item Type",
 //        "Manager Type", "First", "Last" };
         
     String[] propertyNames = new String[] { 
     		"lastOccurence" ,
     		"firstOccurence",
     		"type",
     		"item",
     		"severity",    		
     		"longDescription",
         "acknowledgedBy",
     		"count",
     		"itemManager",
     		"itemType",
     		"itemManagerType",    		    		
     		"compositeAlertId", 
     		"source"};
     String[] columnLabels = new String[] { 
     		"Last Event", 
     		"First Event", 
     		"Type",
     		"Item", 
     		"Severity", 
     		"Description", 
         "Acknowledged By", 
     		"Count", 
     		"Manager", 
     		"Item Type",
     		"Manager Type", 
     		"Alert ID", 
     		"Source" };        
         
    TableFormat<Alert> tf = GlazedLists.tableFormat(Alert.class, propertyNames,
         columnLabels);
    collectionTable = new JTable(new EventTableModel<Alert>(sortedList, tf));
 
    new TableComparatorChooser<Alert>(collectionTable, sortedList, true);
 
     TableCellRenderer defaultRenderer = new DefaultAlertCellRenderer( );
     TableCellRenderer sevRenderer = new SeverityAlertCellRenderer( );
     TableCellRenderer dateRenderer = new DateAlertCellRenderer( );
     
     collectionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
     collectionTable.setDefaultRenderer(Long.class, defaultRenderer);
     collectionTable.setDefaultRenderer(Object.class, defaultRenderer);
     collectionTable.setDefaultRenderer(Date.class, dateRenderer);
     collectionTable.setDefaultRenderer(Severity.class, sevRenderer);
     collectionTable
         .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
     // collectionTable.setRowSelectionAllowed(true);
     setLayout(new BorderLayout( ));
     JScrollPane tableScroller = new JScrollPane(collectionTable);
     add(tableScroller, BorderLayout.CENTER);
   }
 
   //
   // public void oldInit( )
   // {
   // List<EventColumn> columns = new ArrayList<EventColumn>( );
   // EventColumn c;
   // c = new EventColumn("Event ID", "eventId");
   // columns.add(c);
   // c = new EventColumn("Description", "longDescription");
   // columns.add(c);
   // c = new EventColumn("Type", "type");
   // columns.add(c);
   // c = new EventColumn("Severity", "severity");
   // columns.add(c);
   // c = new EventColumn("Count", "count");
   // columns.add(c);
   // c = new EventColumn("Source", "sourceId");
   // columns.add(c);
   // c = new EventColumn("Item", "item");
   // c.setWidth(200);
   // columns.add(c);
   // c = new EventColumn("Manager", "itemManager");
   // c.setWidth(200);
   // columns.add(c);
   // c = new EventColumn("Item Type", "itemType");
   // c.setWidth(200);
   // columns.add(c);
   // c = new EventColumn("Manager Type", "itemManagerType");
   // c.setWidth(200);
   // columns.add(c);
   // c = new EventColumn("First", "firstOccurence");
   // c.setWidth(250);
   // columns.add(c);
   // c = new EventColumn("Last", "lastOccurence");
   // c.setWidth(250);
   // columns.add(c);
   //
   // model = new EventCollectionTableModel(columns);
   // EventCollectionTableColumnModel columnModel = new
   // EventCollectionTableColumnModel(
   // columns);
   // // collectionTable = new JTable(model, columnModel);
   // // collectionTable = new JTable(model);
   // collectionTable = new EventTable(model, columnModel);
   // sorter = new TableRowSorter<EventCollectionTableModel>(model);
   //
   // TableCellRenderer defaultRenderer = new DefaultEventCellRenderer( );
   // TableCellRenderer sevRenderer = new SeverityEventCellRenderer( );
   //
   // collectionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
   // collectionTable.setRowSorter(sorter);
   // collectionTable.setDefaultRenderer(Long.class, defaultRenderer);
   // collectionTable.setDefaultRenderer(Object.class, defaultRenderer);
   // collectionTable.setDefaultRenderer(Date.class, defaultRenderer);
   // collectionTable.setDefaultRenderer(Severity.class, sevRenderer);
   // collectionTable
   // .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
   // // collectionTable.setRowSelectionAllowed(true);
   // setLayout(new BorderLayout( ));
   // JScrollPane tableScroller = new JScrollPane(collectionTable);
   // add(tableScroller, BorderLayout.CENTER);
   // }
 
   /**
    * @return Returns the collection.
    */
   public AlertCollection getCollection( )
   {
     return collection;
   }
 
   public List<Alert> getSelectedAlerts()
   {
   	int[] myrows = collectionTable.getSelectedRows();  	
   	ArrayList<Alert> retval = new ArrayList<Alert>(myrows.length);
   	
   	for (int i = 0; i < myrows.length; i++)
   	{
   		int nextrow = myrows[i];
  		Alert next = collection.getAlertAt(nextrow);
   		retval.add(next);
   	}
   	
   	return retval;
   }
   
   
   /**
    * @param collection
    *          The collection to set.
    */
   private void setCollection(AlertCollection collection)
   {
     this.collection = collection;
   }
 
   // public void setCollection(EventCollection collection)
   // {
   // model.setCollection(collection);
   // }
   //
   // public EventCollection getCollection( )
   // {
   // return model.getCollection( );
   // }
 }
