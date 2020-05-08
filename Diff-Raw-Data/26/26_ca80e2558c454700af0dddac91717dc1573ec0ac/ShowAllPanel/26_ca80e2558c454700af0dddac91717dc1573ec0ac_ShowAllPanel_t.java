 package edu.wustl.cab2b.client.ui.mainframe.showall;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.JScrollPane;
 
 import org.jdesktop.swingx.LinkRenderer;
 import org.jdesktop.swingx.action.LinkAction;
 import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
 import org.jdesktop.swingx.decorator.HighlighterPipeline;
 
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTable;
 
 /**
  * Abstract panel class for Showing details in tabular format
  * @author deepak_shingan
  */
 abstract public class ShowAllPanel extends Cab2bPanel {
 
     private Cab2bTable table;
 
     private LinkAction linkAction;
 
     private Object[] tableHeader;
 
     private Object[][] data;
 
     /**
      * Constructor
      * @param action
      * @param tableHeader
      * @param data
      */
     public ShowAllPanel(Object[] tableHeader, Object[][] data) {
         this.tableHeader = tableHeader;
         this.data = data;
         linkAction = new ShowAllLinkActionClass();
         initGUI();
     }
 
     /**
      * GUI initialisation method
      */
     private void initGUI() {
         setBorder(BorderFactory.createLineBorder(new Color(200, 200, 220)));
         if (data != null && tableHeader != null) {
            table = new Cab2bTable(false, data, tableHeader);
             table.getColumn(1).setCellRenderer(new LinkRenderer(linkAction));
             table.getColumn(1).setCellEditor(new LinkRenderer(linkAction));
         } else
             table = new Cab2bTable();
         HighlighterPipeline highlighters = new HighlighterPipeline();
         highlighters.addHighlighter(new AlternateRowHighlighter());
         table.setHighlighters(highlighters);
         highlighters.updateUI();
 
         table.setBorder(null);
         table.setShowGrid(false);
         table.setRowHeightEnabled(true);
 
         JScrollPane jScrollPane = new JScrollPane();
         jScrollPane.getViewport().add(table);
         this.add("br hfill vfill", jScrollPane);
         this.updateUI();
     }
 
     /**
      * @author deepak_shingan
      *
      */
     private class ShowAllLinkActionClass extends LinkAction {
         public void actionPerformed(ActionEvent e) {
             setVisited(true);
             linkActionPerformed();
         }
     }
 
     /**
      * Action method for each hyperlink click on the table
      */
     abstract public void linkActionPerformed();
 
     /**
      * Returns Cab2bTable associated with panel
      * @return the table
      */
     public Cab2bTable getTable() {
         return table;
     }
 }
