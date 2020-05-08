 import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.PatternPredicate;
 import org.xml.sax.SAXException;
 
 import javax.swing.*;
 import javax.xml.parsers.ParserConfigurationException;
 import java.awt.*;
 import java.io.IOException;
import java.util.regex.Pattern;
 
 /**
  * Implements a checkstyle panel.
  * @author Achim Gosse, achim.gosse+checkstyle-gui@gmail.com
  * @version 1.00
  */
 public class CheckStylePanel extends JPanel {
 
     /**
      * Model of the treeTable of the panel.
      */
     CheckStyleTreeTableModel tableModel;
 
     /**
      * TreeTable of the panel.
      */
     JXTreeTable treeView;
 
 
 
 
     /**
      * Creates a checkstyle panel.
      */
     public CheckStylePanel() {
         super();
         tableModel = new CheckStyleTreeTableModel();
         treeView = new JXTreeTable(tableModel);
         init();
     }
 
 
     /**
      * Used to perform a checkstyle run.
      * Called by the frame.
      * @param preferences   users preferences
      */
     public void performCheckStyleRun(CheckStylePreferences preferences) {
         CheckStyleWrapper wrapper = new CheckStyleWrapper(preferences);
         try {
             tableModel.setRoot(wrapper.runCheck());
             treeView.expandAll();
             treeView.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
         } catch (IOException e) {
             popupErrorDialog(e.getLocalizedMessage(), "IOException");
         } catch (SAXException e) {
             popupErrorDialog(e.getLocalizedMessage(), "SAXException");
         } catch (ParserConfigurationException e) {
             popupErrorDialog(e.getLocalizedMessage(), "ParserConfigException");
         }
     }
 
 
     /**
      * Used to initialize current panel and its components.
      */
     private void init() {
 
         treeView.setSortable(false);
 
        HighlightPredicate warningPredicate = new PatternPredicate(Pattern.compile("warning",
                Pattern.CASE_INSENSITIVE), 1);
        ColorHighlighter infoHighlighter = new ColorHighlighter(warningPredicate,
                Color.YELLOW, null);
        treeView.addHighlighter(infoHighlighter);

        HighlightPredicate errorPredicate = new PatternPredicate(Pattern.compile("error",
                Pattern.CASE_INSENSITIVE), 1);
        ColorHighlighter errorHighlighter = new ColorHighlighter(errorPredicate, Color.RED, null);
        treeView.addHighlighter(errorHighlighter);


         setColumnWidths();
 
         treeView.getTableHeader().setReorderingAllowed(false);
 
         JScrollPane scrollPane = new JScrollPane(
                 treeView,
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         setLayout(new BorderLayout());
         add(scrollPane, BorderLayout.CENTER);
 
         setOpaque(true);
         setVisible(true);
     }
 
 
     /**
      * Sets the column widths of the tree table.
      */
     private void setColumnWidths() {
         int[] widths = {-1, 50, 30, 30, -1};
 
         for (int i = 0; i < treeView.getColumnCount(); i++) {
             if (widths[i] >= 0) {
                 treeView.getColumnExt(i).setMinWidth(widths[i]);
                 treeView.getColumnExt(i).setMaxWidth(widths[i]);
                 treeView.getColumnExt(i).setPreferredWidth(widths[i]);
             }
         }
 
         treeView.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
         treeView.setAutoCreateColumnsFromModel(false);
     }
 
 
     /**
      * Used to display exceptions.
      * @param message       message of exception
      * @param title         title / kind of exception
      */
     private void popupErrorDialog(String message, String title) {
         JOptionPane.showMessageDialog(this,
                 message,
                 title,
                 JOptionPane.ERROR_MESSAGE);
     }
 }
