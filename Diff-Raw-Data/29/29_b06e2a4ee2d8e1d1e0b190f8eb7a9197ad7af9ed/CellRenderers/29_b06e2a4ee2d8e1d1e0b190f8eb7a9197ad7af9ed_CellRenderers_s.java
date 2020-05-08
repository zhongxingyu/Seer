 /*
  * CellRenderers.java
  *
  * Created on June 13, 2013, 1:16 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.rcp.control.table;
 
 import com.rameses.rcp.common.AbstractListDataProvider;
 import com.rameses.rcp.common.CheckBoxColumnHandler;
 import com.rameses.rcp.common.Column;
 import com.rameses.rcp.common.ComboBoxColumnHandler;
 import com.rameses.rcp.common.DateColumnHandler;
 import com.rameses.rcp.common.DecimalColumnHandler;
 import com.rameses.rcp.common.IntegerColumnHandler;
 import com.rameses.rcp.common.LookupColumnHandler;
 import com.rameses.rcp.common.OpenerColumnHandler;
 import com.rameses.rcp.constant.TextCase;
 import com.rameses.rcp.support.ColorUtil;
 import com.rameses.rcp.support.ComponentSupport;
 import com.rameses.rcp.util.UIControlUtil;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Insets;
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.table.TableCellRenderer;
 
 /**
  *
  * @author wflores
  */
 public class CellRenderers 
 {
     private static Map<String,Class> renderers;
     
     static 
     {
         renderers = new HashMap();
         renderers.put("text", TextRenderer.class); 
         renderers.put("string", TextRenderer.class); 
         renderers.put("boolean", CheckBoxRenderer.class); 
         renderers.put("checkbox", CheckBoxRenderer.class); 
         renderers.put("combo", ComboBoxRenderer.class); 
         renderers.put("combobox", ComboBoxRenderer.class); 
         renderers.put("date", DateRenderer.class); 
         renderers.put("decimal", DecimalRenderer.class); 
         renderers.put("double", DecimalRenderer.class); 
         renderers.put("integer", IntegerRenderer.class); 
         renderers.put("lookup", LookupRenderer.class); 
         renderers.put("opener", OpenerRenderer.class); 
     }
     
     public static AbstractRenderer getRendererFor(Column oColumn) 
     {
         Column.TypeHandler handler = oColumn.getTypeHandler();
         if (handler == null) 
             handler = ColumnHandlerUtil.newInstance().createTypeHandler(oColumn);
         
         return null;
     }
     
     
     // <editor-fold defaultstate="collapsed" desc="  Context (class)  ">
     
     public static class Context 
     {
         private JTable table;
         private Object value;
         private int rowIndex;
         private int columnIndex;
         
         private TableControl tableControl;
         private TableControlModel tableControlModel;
         
         Context(JTable table, Object value, int rowIndex, int columnIndex) 
         {
             this.table = table;
             this.value = value;
             this.rowIndex = rowIndex;
             this.columnIndex = columnIndex;        
             
             this.tableControl = (TableControl) table;
             this.tableControlModel = (TableControlModel) this.tableControl.getModel(); 
         }
         
         public JTable getTable() { return table; }
         public Object getValue() { return value; }
         public int getRowIndex() { return rowIndex; } 
         public int getColumnIndex() { return columnIndex; }
         
         public TableControl getTableControl() { return tableControl; }
         public TableControlModel getTableControlModel() { return tableControlModel; } 
         
         public AbstractListDataProvider getDataProvider() {
             return tableControl.getDataProvider(); 
         }
         
         public Object getItemData() {
             return getItemData(this.rowIndex); 
         }
         
         public Object getItemData(int rowIndex) {
             return getDataProvider().getListItemData(rowIndex); 
         }
         
         public Column getColumn() {
             return getColumn(this.columnIndex); 
         }
         
         public Column getColumn(int index) {
             return getTableControlModel().getColumn(index); 
         }
         
         public Object createExpressionBean() {
             return createExpressionBean(getItemData()); 
         }
         
         public Object createExpressionBean(Object bean) { 
             return getTableControl().createExpressionBean(bean);
         } 
     }
     
     // </editor-fold>
         
     // <editor-fold defaultstate="collapsed" desc="  AbstractRenderer (class)  ">
     
     public static abstract class AbstractRenderer implements TableCellRenderer 
     {
         private Insets CELL_MARGIN = TableUtil.CELL_MARGIN;
         private Color FOCUS_BG = TableUtil.FOCUS_BG; 
         private ComponentSupport componentSupport; 
         private CellRenderers.Context ctx;
         
         protected ComponentSupport getComponentSupport() 
         {
             if (componentSupport == null) 
                 componentSupport = new ComponentSupport();
 
             return componentSupport;
         }
         
         protected CellRenderers.Context getContext() { return ctx; }        
         protected TableControl getTableControl() { return ctx.getTableControl(); } 
         protected TableControlModel getTableControlModel() { return ctx.getTableControlModel(); } 
                 
         public abstract JComponent getComponent(JTable table, int rowIndex, int columnIndex);
         
         public abstract void refresh(JTable table, Object value, boolean selected, boolean focus, int rowIndex, int columnIndex);
         
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) 
         {
             ctx = new CellRenderers.Context(table, value, rowIndex, columnIndex); 
             
             TableControl tc = ctx.getTableControl();
             TableControlModel tcm = ctx.getTableControlModel(); 
             
             JComponent comp = getComponent(table, rowIndex, columnIndex);
             getComponentSupport().setEmptyBorder(comp, CELL_MARGIN);             
             comp.setFont(table.getFont());
             
             if (isSelected) 
             {
                 comp.setBackground(table.getSelectionBackground());
                 comp.setForeground(table.getSelectionForeground());
                 comp.setOpaque(true);                
                 if (hasFocus) 
                 {
                     comp.setBackground(FOCUS_BG);
                     comp.setForeground(table.getForeground());
                 }
             } 
             else 
             {
                 comp.setForeground(table.getForeground());
                 comp.setOpaque(false);
                 
                 if ( (rowIndex % 2 == 0) ) 
                 {
                     if (tc.getEvenBackground() != null) 
                     {
                         comp.setBackground(tc.getEvenBackground());
                         comp.setOpaque(true);
                     }
                     
                     if (tc.getEvenForeground() != null)
                         comp.setForeground(tc.getEvenForeground());
                 } 
                 else 
                 {
                     if (tc.getOddBackground() != null) 
                     {
                         comp.setBackground(tc.getOddBackground());
                         comp.setOpaque(true);
                     }
                     
                     if (tc.getOddForeground() != null) 
                         comp.setForeground(tc.getOddForeground());
                 }
             }
             
             AbstractListDataProvider ldp = ctx.getDataProvider();
             String errmsg = ldp.getMessageSupport().getErrorMessage(rowIndex);
             if (errmsg != null) 
             {
                 if (!hasFocus) 
                 {
                     comp.setBackground( tc.getErrorBackground() );
                     comp.setForeground( tc.getErrorForeground() );
                     comp.setOpaque(true);
                 }
             }
             
             if ( !table.isEnabled() ) 
             {
                 Color c = comp.getBackground();
                 comp.setBackground(ColorUtil.brighter(c, 5));
                 
                 c = comp.getForeground();
                 comp.setForeground(ColorUtil.brighter(c, 5));
             }
             
             //border support
             Border inner = getComponentSupport().createEmptyBorder(CELL_MARGIN); 
             Border border = BorderFactory.createEmptyBorder(1,1,1,1);
             if (hasFocus) 
             {
                 if (isSelected) 
                     border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
                 if (border == null) 
                     border = UIManager.getBorder("Table.focusCellHighlightBorder");
             }
             comp.setBorder(BorderFactory.createCompoundBorder(border, inner));
             
             refresh(table, value, isSelected, hasFocus, rowIndex, columnIndex);
             return comp;
         }
     }
     
     // </editor-fold>    
     
     // <editor-fold defaultstate="collapsed" desc="  AbstractNumberRenderer (class)  ">
     
     public abstract static class AbstractNumberRenderer extends AbstractRenderer 
     {        
         private JLabel label;
         
         public AbstractNumberRenderer() 
         {
             label = new JLabel();
             label.setHorizontalAlignment(SwingConstants.RIGHT);
         }
         
         public JComponent getComponent(JTable table, int rowIndex, int columnIndex) {
             return label;
         }
         
         protected abstract String getFormattedValue(Column c, Object value);
         
         public void refresh(JTable table, Object value, boolean selected, boolean focus, int rowIndex, int columnIndex) 
         {
             Column c = getContext().getColumn(); 
             String result = getFormattedValue(c, value);
             label.setText((result == null ? "" : result));
             
             String alignment = c.getAlignment();
             if (alignment != null) 
                 getComponentSupport().alignText(label, alignment);
         }
         
         protected String formatValue (Number value, String format, String defaultFormat) 
         {
             if (value == null) return null;
             if ("".equals(format)) return value.toString(); 
             
             DecimalFormat formatter = null;
             if ( format != null)
                 formatter = new DecimalFormat(format);
             else
                 formatter = new DecimalFormat(defaultFormat);
             
             return formatter.format(value);
         }
     }
     
     // </editor-fold>        
     
     // <editor-fold defaultstate="collapsed" desc="  TextRenderer (class)  ">
     
     public static class TextRenderer extends AbstractRenderer 
     {        
         private JLabel label;
         
         public TextRenderer() 
         {
             label = new JLabel();
             label.setVerticalAlignment(SwingConstants.CENTER);
         }
 
         public JComponent getComponent(JTable table, int rowIndex, int columnIndex) {
             return label;
         }
         
         protected Object resolveValue(CellRenderers.Context ctx) { 
             return ctx.getValue();
         }        
         
         public void refresh(JTable table, Object value, boolean selected, boolean focus, int rowIndex, int columnIndex) 
         {
             Object columnValue = resolveValue(getContext());
             Column oColumn = getContext().getColumn();
             
             TextCase oTextCase = oColumn.getTextCase();
             if (oTextCase != null && columnValue != null) 
                 label.setText(oTextCase.convert(columnValue.toString())); 
 
             label.setHorizontalAlignment( SwingConstants.LEFT );
             if ( columnValue != null && oColumn.isHtmlDisplay() )
                 columnValue = "<html>" + columnValue + "</html>";
 
             label.setText((columnValue == null ? "" : columnValue.toString()));
 
             //set alignment if it is specified in the Column model
             if ( oColumn.getAlignment() != null ) 
                 getComponentSupport().alignText(label, oColumn.getAlignment());             
         }
     }
     
     // </editor-fold>    
     
     // <editor-fold defaultstate="collapsed" desc="  CheckBoxRenderer (class)  ">
     
     public static class CheckBoxRenderer extends AbstractRenderer 
     {        
         private JCheckBox component;
         private JLabel empty;
         
         public CheckBoxRenderer() 
         {
             component = new JCheckBox();
             component.setHorizontalAlignment(SwingConstants.CENTER);
             component.setBorderPainted(true);
             
             //empty renderer when row object is null
             empty = new JLabel("");
         }
         
         public JComponent getComponent(JTable table, int rowIndex, int colIndex) 
         {
             return (getContext().getItemData() == null? empty: component);
         }
         
         public void refresh(JTable table, Object value, boolean selected, boolean focus, int rowIndex, int columnIndex) 
         {
             Object itemData = getContext().getItemData();
             if (itemData == null) return;
             
             Column oColumn = getContext().getColumn();
             component.setSelected(resolveValue(oColumn, value));
         }
 
         private boolean resolveValue(Column oColumn, Object value) 
         {
             Object checkValue = null;
             if (oColumn.getTypeHandler() instanceof CheckBoxColumnHandler)
                 checkValue = ((CheckBoxColumnHandler) oColumn.getTypeHandler()).getCheckValue();
             else 
                 checkValue = oColumn.getCheckValue(); 
 
             boolean selected = false;             
             if (value == null) selected = false;
             else if (value.equals(checkValue+"")) selected = true;
             else if ("true".equals(value+"")) selected = true;
             else if ("yes".equals(value+"")) selected = true;
             else if ("t".equals(value+"")) selected = true;
             else if ("y".equals(value+"")) selected = true;
             else if ("1".equals(value+"")) selected = true;
 
             return selected;
         }       
     }
     
     // </editor-fold>    
     
     // <editor-fold defaultstate="collapsed" desc="  ComboBoxRenderer (class)  ">    
     
     public static class ComboBoxRenderer extends TextRenderer 
     {
         protected Object resolveValue(CellRenderers.Context ctx) 
         {
             String expression = null;
             Column oColumn = ctx.getColumn(); 
             if (oColumn.getTypeHandler() instanceof ComboBoxColumnHandler) 
                 expression = ((ComboBoxColumnHandler) oColumn.getTypeHandler()).getExpression(); 
             else 
                 expression = oColumn.getExpression(); 
                 
             Object cellValue = ctx.getValue();
            if (expression != null) 
             {
                 try 
                 {
                     Object exprBean = ctx.createExpressionBean();
                     cellValue = UIControlUtil.evaluateExpr(exprBean, expression); 
                 } 
                 catch(Exception e) {;}
             }
             return cellValue; 
         } 
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  DateRenderer (class)  ">    
     
     public static class DateRenderer extends TextRenderer 
     {
         private SimpleDateFormat outputFormatter;
 
         protected Object resolveValue(CellRenderers.Context ctx) 
         {
             String format = null;
             Column oColumn = ctx.getColumn();
             if (oColumn.getTypeHandler() instanceof DateColumnHandler) 
                 format = ((DateColumnHandler) oColumn.getTypeHandler()).getOutputFormat(); 
             else 
                 format = oColumn.getFormat();
 
             Object cellValue = ctx.getValue();             
             if (format != null && cellValue instanceof Date) 
             {
                 try 
                 {
                     if (outputFormatter == null) 
                         outputFormatter = new SimpleDateFormat(format); 
 
                     cellValue = outputFormatter.format((Date) cellValue); 
                 } 
                 catch(Exception ex) {;}
             }
             return cellValue; 
         }  
     }
     
     // </editor-fold>    
     
     // <editor-fold defaultstate="collapsed" desc="  DecimalRenderer (class)  ">
     
     public static class DecimalRenderer extends AbstractNumberRenderer 
     { 
         protected String getFormattedValue(Column c, Object value)
         {
             Number num = null;
             if (value == null) { 
                 /* do nothing */ 
             } 
             else if (value instanceof BigDecimal) {
                 num = (BigDecimal) value;
             } 
             else {
                 try {
                     num = new BigDecimal(value.toString());
                 } catch(Exception e) {}
             }
 
             if (num == null) return null;   
 
             String format = null;
             if (c.getTypeHandler() instanceof DecimalColumnHandler) 
                 format = ((DecimalColumnHandler) c.getTypeHandler()).getFormat(); 
             else 
                 format = c.getFormat();
 
             if (format == null || format.length() == 0) return num.toString();
 
             return formatValue(num, format, "#,##0.00");
         }
     }
     
     // </editor-fold>        
     
     // <editor-fold defaultstate="collapsed" desc="  IntegerRenderer (class)  ">
     
     public static class IntegerRenderer extends AbstractNumberRenderer 
     { 
         protected String getFormattedValue(Column c, Object value)
         {
             Number num = null;
             if (value == null) { 
                 /* do nothing */ 
             } 
             else if (value instanceof Integer) {
                 num = (Integer) value;
             } 
             else 
             {
                 try {
                     num = new Integer(value.toString());
                 } catch(Exception e) {}
             }
 
             if (num == null) return null;
 
             String format = null; 
             if (c.getTypeHandler() instanceof IntegerColumnHandler) 
                 format = ((IntegerColumnHandler) c.getTypeHandler()).getFormat();
             else
                 format = c.getFormat();
 
             if (format == null || format.length() == 0) 
                 return num.toString();
             else 
                 return formatValue(num, c.getFormat(), "0");
         }
     }
     
     // </editor-fold>            
     
     // <editor-fold defaultstate="collapsed" desc="  LookupRenderer (class)  ">
     
     public static class LookupRenderer extends TextRenderer 
     {
         protected Object resolveValue(CellRenderers.Context ctx) 
         {
             String expression = null;
             Column oColumn = ctx.getColumn();
             if (oColumn.getTypeHandler() instanceof LookupColumnHandler) 
                 expression = ((LookupColumnHandler) oColumn.getTypeHandler()).getExpression(); 
             else
                 expression = oColumn.getExpression();
             
             Object cellValue = ctx.getValue(); 
             if (expression != null) 
             {
                 try 
                 {
                     Object exprBean = getContext().createExpressionBean();
                     cellValue = UIControlUtil.evaluateExpr(exprBean, expression); 
                 } 
                 catch(Exception e) {;}
             }
             return cellValue; 
         }
     }    
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  OpenerRenderer (class)  ">
     
     public static class OpenerRenderer extends TextRenderer 
     {
         protected Object resolveValue(CellRenderers.Context ctx) 
         {
             String expression = null;
             Column oColumn = ctx.getColumn();
             if (oColumn.getTypeHandler() instanceof OpenerColumnHandler) 
                 expression = ((OpenerColumnHandler) oColumn.getTypeHandler()).getExpression(); 
             else
                 expression = oColumn.getExpression();
             
             Object cellValue = ctx.getValue(); 
             if (expression != null) 
             {
                 try 
                 {
                     Object exprBean = getContext().createExpressionBean();
                     cellValue = UIControlUtil.evaluateExpr(exprBean, expression); 
                 } 
                 catch(Exception e) {;}
             }
             return cellValue; 
         }
     }    
     
     // </editor-fold>    
 }
