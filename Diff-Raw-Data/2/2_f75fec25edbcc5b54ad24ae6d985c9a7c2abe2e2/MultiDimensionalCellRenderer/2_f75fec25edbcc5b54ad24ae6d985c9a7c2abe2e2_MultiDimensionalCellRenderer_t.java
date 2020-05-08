 package org.pillarone.riskanalytics.application.ui.parameterization.view;
 
 import com.ulcjava.base.application.IRendererComponent;
 import com.ulcjava.base.application.ULCTableTree;
 import com.ulcjava.base.application.util.HTMLUtilities;
 import org.pillarone.riskanalytics.application.ui.parameterization.model.MultiDimensionalParameterizationTableTreeNode;
 import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter;
 
 /**
  * Render the cell that displays a MultiDimensionalParameter with a tooltip that allows a
  * preview of the table data. Truncate the tooltip for large tables.
  */
 public class MultiDimensionalCellRenderer extends BasicCellRenderer {
     private static final int DISPLAY_MAX_ROW_COUNT = 10;
     private static final int DISPLAY_MAX_COLUMN_COUNT = 5;
 
     public MultiDimensionalCellRenderer(int columnIndex) {
         super(columnIndex);
     }
 
     @Override
     public IRendererComponent getTableTreeCellRendererComponent(ULCTableTree ulcTableTree, Object value, boolean selected, boolean hasFocus, boolean expanded, boolean leaf, Object node) {
         setToolTipText(createTooltip((MultiDimensionalParameterizationTableTreeNode) node));
         return super.getTableTreeCellRendererComponent(ulcTableTree, value, selected, hasFocus, expanded, leaf, node);
     }
 
     // this would all be so much easier if it was a Groovy class...
 
     private String createTooltip(MultiDimensionalParameterizationTableTreeNode node) {
         String toolTip = null;
         AbstractMultiDimensionalParameter value = node.getMultiDimensionalValue(columnIndex);
         if (value != null) {
             StringBuilder text = new StringBuilder();
             text.append("<table>");
             int rowCount = value.getRowCount();
             int columnCount = value.getColumnCount();
             for (int row = 0; row < rowCount && row < DISPLAY_MAX_ROW_COUNT; row++) {
                 text.append("<tr>");
                for (int col = 1; col <= columnCount; col++) {
                     if (col > DISPLAY_MAX_COLUMN_COUNT) break;
                     text.append("<td>");
                     if (isTitleCell(value, row, col)) {
                         text.append("<b>");
                         if (col == DISPLAY_MAX_COLUMN_COUNT){
                             text.append(""+ (columnCount-DISPLAY_MAX_COLUMN_COUNT) +" more...");
                         } else {
                             text.append(value.getValueAt(row, col));
                         }
                         text.append("</b>");
                     } else {
                         if (col == DISPLAY_MAX_COLUMN_COUNT){
                             text.append("...");
                         } else {
                             text.append(value.getValueAt(row, col));
                         }
                     }
                     text.append("</td>");
                 }
                 text.append("</tr>");
                 if (row + 1 == DISPLAY_MAX_ROW_COUNT && rowCount > DISPLAY_MAX_ROW_COUNT) {
                     int additionalRowCount = rowCount - DISPLAY_MAX_ROW_COUNT;
                     text.append("<tr><td>").append(additionalRowCount).append(" more rows...</td></tr>");
                 }
             }
             text.append("</table>");
 
             if (text.length() > 0) {
                 toolTip = HTMLUtilities.convertToHtml(text.toString());
             }
         }
         return toolTip;
     }
 
     private boolean isTitleCell(AbstractMultiDimensionalParameter value, int row, int col) {
         return value.getTitleColumnCount() > col || value.getTitleRowCount() > row;
     }
 }
