 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 
 
 /**
  * @author Jonathan
  * A window for editing size classes
  */
 public class SizeClassEditor extends JDialog {
     
     private static final long serialVersionUID = 6909421034651884335L;
     private JTable table;
     private SizeClassTableModel model;
     private ArrayList<SizeClass> oldSizeClasses = null;
     /**
      * The size class editor
      * @param saveListener the listener attached to the save button
      */
     public SizeClassEditor(ActionListener saveListener){
         setTitle("Edit Size Classes");
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 setSizeClasses(oldSizeClasses);
             }
         });
         model = new SizeClassTableModel();
         model.addTableModelListener(new TableModelListener() {
             
             @Override
             public void tableChanged(TableModelEvent arg0) {
                 int filledRows = 0;
                 for (int i = 0; i < model.getRowCount(); i++) {
                     int filledColumns =0;
                     for (int j = 0; j < model.getColumnCount(); j++) {
                         // Check that each column of the row is filled out
                         if(model.getValueAt(i, j) != null) {
                             filledColumns++;
                         }
                     }
                     if(filledColumns == model.getColumnCount())
                         filledRows++;
                 }
                 if(filledRows == model.getRowCount() ) {
                     model.setRowCount(model.getRowCount()+1);
                 }
                 // Remove any empty rows
                 for (int i = 0; i < model.getRowCount() -1; i++) {
                     int empty = 0;
                     for (int j = 0; j < model.getColumnCount(); j++) {
                         // Check for empty rows
                         if(model.getValueAt(i, j) == null) {
                             empty++;
                         }
                     }
                     if(empty == model.getColumnCount())
                         model.removeRow(i);
                 }
                 
             }
         });
         
         table = new JTable(model);
         JPanel panel = new JPanel();
         getContentPane().add(panel);
         panel.setLayout(new BorderLayout());
         
         panel.add(new JScrollPane(table),BorderLayout.CENTER);
         
         JPanel btn_panel = new JPanel();
         panel.add(btn_panel, BorderLayout.SOUTH);
         
         JButton btnSave = new JButton("Save");
         btn_panel.add(btnSave);
         
         JButton btnCancel = new JButton("Cancel");
         btnCancel.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 setSizeClasses(oldSizeClasses);
                 
             }
         });
         btn_panel.add(btnCancel);
         btnSave.addActionListener(saveListener);
         pack();
     }
     
     /**
      * @param c
      * @param okListener
      * @param cancelListener
      * @return
      */
     public static SizeClassEditor createSizeClassEditor(Component c,
             ActionListener okListener) {
                 return new SizeClassEditor(okListener);
         
     }
     /**
      * Add a row from a provided size class
      * @param sc the size class to add
      */
     public void addRow(SizeClass sc) {
         model.addRow(new Object[] {sc.getMin(),sc.getMax(),sc.getMortality(),sc.getGrowShrinkP(),sc.getGrowShrinkPC()});
     }
     /**
      * Adds an empty row to the bottom of the table
      */
     private void addEmptyRow() {
         model.addRow(new Object[]  {});
     }
     /**
      * Set the size classes to display for editing 
      * @param sc the arraylist of sizeclasses to use
      */
     public void setSizeClasses(ArrayList<SizeClass>  sc) {
         oldSizeClasses = getSizeClasses();
         model.setRowCount(0);
         if(sc == null) {
             model.addRow(new Object[] {0,50,0d,0d,0d,0d});
             return;
         }
         
         for (SizeClass sizeClass : sc) {
             addRow(sizeClass);
         }
         addEmptyRow();
     }
     /**
      * @return true if the input is a valid set of size classes
      */
     public boolean validateInput() {
         boolean valid = true;
         for(int i = 0; i < model.getRowCount(); i++) {
             
             // Should probably set background colours for the cells that are invalid
             if(table.getValueAt(i, 0) == null) {
                 continue;
             }
             boolean minMax = (Integer) table.getValueAt(i, 0) < (Integer) table.getValueAt(i, 1);
             if(minMax) {
                 // Set the pair that is invalid to have a red background color
                // table.get
             }
             valid &= minMax;
             if(table.getValueAt(i+1, 0) != null)
                 valid &= (Integer) table.getValueAt(i,1) == (Integer) table.getValueAt(i+1, 0);
             for(int j = 2; j <= 4; j++) 
                 valid &= ((Double) table.getValueAt(i, j)) < 1 && ((Double) table.getValueAt(i, j)) >= 0;
         }
         return valid;
     }
     
     /**
      * @return the sizeclasses held by the editor
      */
     public ArrayList<SizeClass> getSizeClasses() {
          ArrayList<SizeClass> scs = new ArrayList<SizeClass>();
         System.out.println("Gettting size classes");
          for (int i = 0; i < table.getRowCount() -1; i++) {
              if(table.getValueAt(i, 0) == null) {
                  continue;
              }
             scs.add(new SizeClass(
                   (Integer)  table.getValueAt(i, 0),
                   (Integer)  table.getValueAt(i, 1),
                   (Double)  table.getValueAt(i, 2),
                   (Double)  table.getValueAt(i, 3),
                   (Double)  table.getValueAt(i, 4)));
         }
          if(scs.size() == 0) {
              return null;
          }
          return scs;
     }
     /**
      * @author Jonathan
      * The table model for sizeclasstable.
      */
     private class SizeClassTableModel  extends DefaultTableModel{
         private static final long serialVersionUID = 6771833776664184864L;
         private final Object[] columns = new Object[] {"Min","Max","Mortality","growshrinkp","growshrinkp (c)"}; 
         public SizeClassTableModel() {
             for (Object i : columns) {
                 addColumn(i);
             }
         } 
         @Override
         public int getColumnCount() {
             return columns.length;
         }
 
         @Override
         public Class getColumnClass(int c) {
             return getValueAt(0, c).getClass();
         }
         
     }
     
 }
