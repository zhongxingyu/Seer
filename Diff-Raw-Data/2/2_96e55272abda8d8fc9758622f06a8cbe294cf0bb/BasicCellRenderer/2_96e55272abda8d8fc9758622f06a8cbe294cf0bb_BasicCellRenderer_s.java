 package org.pillarone.riskanalytics.application.ui.parameterization.view;
 
 import com.ulcjava.base.application.IRendererComponent;
 import com.ulcjava.base.application.ULCLabel;
 import com.ulcjava.base.application.ULCPopupMenu;
 import com.ulcjava.base.application.ULCTableTree;
 import com.ulcjava.base.application.datatype.IDataType;
 import com.ulcjava.base.application.tabletree.DefaultTableTreeCellRenderer;
 import com.ulcjava.base.application.tabletree.ITableTreeCellRenderer;
 import com.ulcjava.base.application.util.Color;
 import org.pillarone.riskanalytics.application.ui.parameterization.model.ParameterizationTableTreeNode;
 
 public class BasicCellRenderer extends DefaultTableTreeCellRenderer implements ITableTreeCellRenderer {
 
     protected int columnIndex;
     private IDataType dataType;
     ULCPopupMenu menu;
 
     public BasicCellRenderer() {
 
     }
 
     public BasicCellRenderer(int columnIndex) {
         this(columnIndex, null);
     }
 
     public BasicCellRenderer(int columnIndex, IDataType dataType) {
         this.columnIndex = columnIndex;
         this.dataType = dataType;
     }
 
 
     public IRendererComponent getTableTreeCellRendererComponent(ULCTableTree ulcTableTree, Object value,
                                                                 boolean selected, boolean hasFocus, boolean expanded,
                                                                 boolean leaf, Object node) {
         if (!selected) {
             if (value != null || ((ParameterizationTableTreeNode) node).isCellEditable(columnIndex)) {
                 setBackground(Color.white);
                 setHorizontalAlignment(ULCLabel.RIGHT);
                 setComponentPopupMenu(menu);
             } else {
                 setBackground(Color.lightGray);
                 setComponentPopupMenu(null);
             }
         }
         setDataType(dataType);
         return super.getTableTreeCellRendererComponent(ulcTableTree, value, selected, hasFocus, expanded, leaf, node);
     }
 
     public void setMenu(ULCPopupMenu menu) {
         this.menu = menu;
     }
 
     protected String typeString() {
        return "org.pillarone.riskanalytics.application.client.UIErrorFeedbackLabel";
     }
 
 }
