 package com.github.croesch.partimana.view;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.DefaultTableModel;
 
 import net.miginfocom.swing.MigLayout;
 
 import com.github.croesch.partimana.actions.ActionObserver;
 import com.github.croesch.partimana.actions.UserAction;
 import com.github.croesch.partimana.i18n.Text;
 import com.github.croesch.partimana.types.Camp;
 import com.github.croesch.partimana.view.api.ICampListView;
 
 /**
  * Implementation of the table that views the table of camps.
  * 
  * @author croesch
  * @since Date: Jun 8, 2011
  */
 class CampListView extends JPanel implements ICampListView {
 
   /** generated */
   private static final long serialVersionUID = -96888415800702415L;
 
   /** the table to display the different camps */
   private final JTable table;
 
   /**
    * Constructs a new {@link CampListView} that is able to visualise a table of camps. The observer will be notified
    * about selection changes.
    * 
    * @since Date: Sep 27, 2011
    * @param o the {@link ActionObserver} that listens for the selection change event.
    */
   public CampListView(final ActionObserver o) {
     setLayout(new MigLayout("fill"));
 
     this.table = new CampTable(o);
 
     add(new JScrollPane(this.table), "grow");
   }
 
   @Override
   public void update(final List<Camp> camps) {
     while (this.table.getRowCount() > 0) {
       ((DefaultTableModel) this.table.getModel()).removeRow(0);
     }
     for (final Camp c : camps) {
       ((DefaultTableModel) this.table.getModel()).addRow(new Object[] { c.getId(), c.getLocation(), c.getName() });
     }
   }
 
   @Override
   public long getSelectedCampId() {
     if (this.table.getSelectedRowCount() == 0) {
       return 0;
     }
     return Long.parseLong(this.table.getValueAt(this.table.getSelectedRow(), 0).toString());
   }
 
   /**
    * Table that displays a set of camps.
    * 
    * @author croesch
    * @since Date: Sep 27, 2011
    */
   private static class CampTable extends JTable {
 
     /** generated */
     private static final long serialVersionUID = -6694984381634664552L;
 
     /**
      * Creates a new table of camps.
      * 
      * @since Date: Sep 27, 2011
      * @param o the {@link ActionObserver} that'll be notified on selection changes.
      */
     public CampTable(final ActionObserver o) {
       final DefaultTableModel tm = new DefaultTableModel();
       tm.setColumnIdentifiers(new Object[] { Text.CAMP_ID.text(), Text.CAMP_LOCATION.text(), Text.CAMP_NAME.text() });
       setModel(tm);
       setAutoCreateRowSorter(true);
       setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       addMouseListener(new TableMouseListener(o));
     }
 
     @Override
     public boolean isCellEditable(final int rowIndex, final int colIndex) {
       return false;
     }
   }
 
   /**
    * Mouse listener for the table that will fire an action to a given observer that a row has been selected.
    * 
    * @author croesch
    * @since Date: Sep 27, 2011
    */
   private static class TableMouseListener extends MouseAdapter {
 
     /** the observer that should be notified about selection events */
     private final ActionObserver observer;
 
     /**
      * Creates new mouse listener for the table of camps. Will fire the action if double click happened on the table.
      * 
      * @since Date: Sep 27, 2011
      * @param o the {@link ActionObserver} to fire the action to.
      */
     public TableMouseListener(final ActionObserver o) {
       this.observer = o;
     }
 
     @Override
     public void mouseClicked(final MouseEvent e) {
       if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
         this.observer.performAction(UserAction.CAMP_SELECTED);
       }
       super.mouseClicked(e);
     }
   }
 }
