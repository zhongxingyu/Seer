 /*
  * TableUtil.java
  *
  * Created on June 26, 2010, 10:53 AM
  * @author jaycverg
  */
 
 package com.rameses.rcp.control.table;
 
 import com.rameses.common.ExpressionResolver;
 import com.rameses.rcp.common.AbstractListModel;
 import com.rameses.rcp.common.Column;
 import com.rameses.rcp.common.StyleRule;
 import com.rameses.rcp.control.XCheckBox;
 import com.rameses.rcp.control.XComboBox;
 import com.rameses.rcp.control.XDateField;
 import com.rameses.rcp.control.XLookupField;
 import com.rameses.rcp.control.XNumberField;
 import com.rameses.rcp.control.XTextField;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.ui.Validatable;
 import com.rameses.rcp.util.ControlSupport;
 import com.rameses.util.ValueUtil;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.FocusListener;
 import java.math.BigDecimal;
 import java.sql.Time;
 import java.sql.Timestamp;
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
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.table.TableCellRenderer;
 
 /**
  *   This class handles TableComponents cell renderer and editor management
  * Default Alignments:
  *  1. decimal/double - right
  *  2. integer/boolean/checkbox/date - center
  *  3. string - left
  */
 
 public final class TableUtil {
     
     public static final Insets CELL_MARGIN = new Insets(1, 5, 1, 5);
     public static final Color FOCUS_BG = new Color(254, 255, 208);
     public static final String HIDE_ON_ENTER = "hide.on.enter";
     
     private static Map<String, Class<? extends JComponent>> editors = new HashMap();
     private static Map<String, Class> numClass = new HashMap();
     private static Map<String, TableCellRenderer> renderers = new HashMap();
     private static TableCellRenderer headerRenderer = new TableHeaderRenderer();
     
     
     //<editor-fold defaultstate="collapsed" desc="  static initializer  ">
     static {
         //map of editors
         editors.put("string", XTextField.class);
         editors.put("boolean", XCheckBox.class);
         editors.put("checkbox", XCheckBox.class);
         editors.put("combo", XComboBox.class);
         editors.put("date", XDateField.class);
         editors.put("double", XNumberField.class);
         editors.put("integer", XNumberField.class);
         editors.put("decimal", XNumberField.class);
         editors.put("lookup", XLookupField.class);
         
         //map of renderers
         TableCellRenderer renderer = new StringRenderer();
         renderers.put("string", renderer);
         renderers.put("integer", renderer);
         renderers.put("decimal", renderer);
         renderers.put("double", renderer);
         renderers.put("date", renderer);
         renderers.put("combo", renderer);
         renderers.put("lookup", renderer);
         
         renderer = new BooleanRenderer();
         renderers.put("boolean", renderer);
         renderers.put("checkbox", renderer);
         
         //number class types
         numClass.put("decimal", BigDecimal.class);
         numClass.put("integer", Integer.class);
         numClass.put("double", Double.class);
     }
     //</editor-fold>
     
     public static JComponent createCellEditor(Column col) {
         String type = col.getType()+"";
         JComponent editor = null;
         try {
             editor = editors.get(type).newInstance();
             customize(editor, col);
             
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return editor;
     }
     
     public static TableCellRenderer getCellRenderer(String type) {
         return renderers.get(type);
     }
     
     public static TableCellRenderer getHeaderRenderer() {
         return headerRenderer;
     }
     
     public static JComponent getTableCornerComponent(Color borderColor) {
         JLabel label = new JLabel(" ");
         Border bb = BorderFactory.createLineBorder(borderColor);
         Border eb = BorderFactory.createEmptyBorder(2,5,2,1);
         label.setBorder( BorderFactory.createCompoundBorder(bb, eb) );
         return label;
     }
     
     //<editor-fold defaultstate="collapsed" desc="  editor customizer method  ">
     private static void customize(JComponent editor, Column col) {
         //add JTable flag to notify that editor is in a JTable component
         editor.putClientProperty(JTable.class, true);
         
         //remove all focus listeners (we don't need it in the table)
         for (FocusListener l: editor.getFocusListeners() ) {
             editor.removeFocusListener(l);
         }
         
         //apply required if editor is Validatable
         if( editor instanceof Validatable ) {
             Validatable v = (Validatable) editor;
             v.setRequired( col.isRequired() );
             v.setCaption( col.getCaption() );
         }
         
         String type = col.getType()+"";
         if ( editor instanceof XCheckBox ) {
             XCheckBox xcb = (XCheckBox) editor;
             xcb.setHorizontalAlignment(SwingConstants.CENTER);
             xcb.setBorderPainted(true);
             
             if( col.getCheckValue() != null )  xcb.setCheckValue( col.getCheckValue() );
             if( col.getUncheckValue() != null )xcb.setUncheckValue( col.getUncheckValue() );
             
         } else if ( editor instanceof XNumberField ) {
             XNumberField xnf = (XNumberField) editor;
             xnf.setFieldType(numClass.get(type));
             if ( !ValueUtil.isEmpty(col.getFormat()) ) {
                 xnf.setPattern( col.getFormat() );
             }
             
         } else if ( editor instanceof XLookupField ) {
             XLookupField xlf = (XLookupField) editor;
             
             if( col.getHandler() instanceof String )
                 xlf.setHandler( col.getHandler().toString() );
             else
                 xlf.setHandlerObject( col.getHandler() );
             
             xlf.setTranserFocusOnSelect(false);
             
         } else if ( editor instanceof XDateField ) {
             XDateField xdf = (XDateField) editor;
             xdf.setHorizontalAlignment(SwingConstants.CENTER);
             if ( !ValueUtil.isEmpty(col.getFormat()) ) {
                 xdf.setInputFormat( col.getFormat() );
                 xdf.setOutputFormat( col.getFormat() );
             }
         }
         
         
         editor.setBackground(FOCUS_BG);
         Font font = (Font) UIManager.get("Table.font");
         editor.setFont(font);
         
         //set alignment if it is specified in the Column model
         if ( col.getAlignment() != null && editor instanceof JTextField ) {
             JTextField jtf = (JTextField) editor;
             if ( "right".equals(col.getAlignment().toLowerCase()) )
                 jtf.setHorizontalAlignment(SwingConstants.RIGHT);
             else if ( "center".equals(col.getAlignment().toLowerCase()))
                 jtf.setHorizontalAlignment(SwingConstants.CENTER);
             else if ( "left".equals(col.getAlignment().toLowerCase()) )
                 jtf.setHorizontalAlignment(SwingConstants.LEFT);
         }
         
         if ( editor instanceof XComboBox ) {
             XComboBox cbox = (XComboBox) editor;
             cbox.setImmediate(true);
             if ( col.getItems() != null ) {
                 cbox.setItems( col.getItems() );
             }
             if ( col.isRequired() ) {
                 cbox.setAllowNull(false);
             }
             if ( col.getFieldType() != null ) {
                 cbox.setFieldType( col.getFieldType() );
             }
             
         } else {
             //border support
             Border inner = BorderFactory.createEmptyBorder(CELL_MARGIN.top, CELL_MARGIN.left, CELL_MARGIN.bottom, CELL_MARGIN.right);
             Border border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
             if (border == null) {
                 border = UIManager.getBorder("Table.focusCellHighlightBorder");
             }
             
             editor.setBorder(BorderFactory.createCompoundBorder(border, inner));
         }
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  AbstractRenderer (class)  ">
     public static abstract class AbstractRenderer implements TableCellRenderer {
         
         public abstract JComponent getComponent(JTable table, int row, int column);
         
         public abstract void refresh(JTable table, Object value, boolean selected, boolean focus, int row, int column);
         
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
             TableControl xtable = (TableControl) table;
             TableControlModel xmodel = (TableControlModel) xtable.getModel();
             JComponent comp = getComponent(table, row, column);
             comp.setBorder(BorderFactory.createEmptyBorder(CELL_MARGIN.top, CELL_MARGIN.left, CELL_MARGIN.bottom, CELL_MARGIN.right));
             comp.setFont(table.getFont());
             
             if (isSelected) {
                 comp.setBackground(table.getSelectionBackground());
                 comp.setForeground(table.getSelectionForeground());
                 comp.setOpaque(true);
                 
                 if (hasFocus) {
                     comp.setBackground(FOCUS_BG);
                     comp.setForeground(table.getForeground());
                 }
                 
             } else {
                 comp.setForeground(table.getForeground());
                 comp.setOpaque(false);
                 
                 if ( (row % 2 == 0) ) {
                     if ( xtable.getEvenBackground() != null ) {
                         comp.setBackground( xtable.getEvenBackground() );
                         comp.setOpaque(true);
                     }
                     if ( xtable.getEvenForeground() != null ) {
                         comp.setForeground( xtable.getEvenForeground() );
                     }
                     
                 } else {
                     if ( xtable.getOddBackground() != null ) {
                         comp.setBackground( xtable.getOddBackground() );
                         comp.setOpaque(true);
                     }
                     if ( xtable.getOddForeground() != null ) {
                         comp.setForeground( xtable.getOddForeground() );
                     }
                 }
             }
             
             AbstractListModel lm = xtable.getListModel();
             ClientContext clientCtx = ClientContext.getCurrentContext();
             ExpressionResolver exprRes = clientCtx.getExpressionResolver();
             Column colModel = xmodel.getColumn(column);
             
 //            try {
 //                StyleRule[] styles = xtable.getBinding().getStyleRules();
 //                if( styles != null && styles.length > 0) {
 //                    comp.setOpaque(true);
//                    
 //                    ListItem listItem = lm.getSelectedItem();
 //                    if( listItem == null ) {
 //                        listItem = lm.getItemList().get(0);
 //                    }
//                    
 //                    Map bean = new HashMap();
 //                    bean.put("row", listItem.getRownum());
 //                    bean.put("column", column);
 //                    bean.put("columnName", colModel.getName());
 //                    bean.put("root", listItem.getRoot());
 //                    bean.put("selected", isSelected);
 //                    bean.put("hasFocus", hasFocus);
 //                    bean.put("item", listItem.getItem());
 //                    applyStyle( xtable.getName(), bean, comp, styles, exprRes );
 //                }
 //            } catch(Exception e){;}

             
             String errmsg = lm.getErrorMessage(row);
             if (errmsg != null) {
                 if (!hasFocus) {
                     comp.setBackground( xtable.getErrorBackground() );
                     comp.setForeground( xtable.getErrorForeground() );
                     comp.setOpaque(true);
                 }
             }
             
             //border support
             Border inner = BorderFactory.createEmptyBorder(CELL_MARGIN.top, CELL_MARGIN.left, CELL_MARGIN.bottom, CELL_MARGIN.right);
             Border border = BorderFactory.createEmptyBorder(1,1,1,1);
             if (hasFocus) {
                 if (isSelected) {
                     border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
                 }
                 if (border == null) {
                     border = UIManager.getBorder("Table.focusCellHighlightBorder");
                 }
             }
             comp.setBorder(BorderFactory.createCompoundBorder(border, inner));
             
             refresh(table, value, isSelected, hasFocus, row, column);
             return comp;
         }
         
         private void applyStyle(String name, Map bean, Component comp, StyleRule[] styles, ExpressionResolver exprRes) {
             if ( styles == null ) return;
             
             if( name == null ) name = "_any_name";
             
             //apply style rules
             for(StyleRule r : styles) {
                 String pattern = r.getPattern();
                 String rule = r.getExpression();
                 
                 //test expression
                 boolean applyStyles = false;
                 if ( rule!=null && name.matches(pattern) ){
                     try {
                         Object o = exprRes.evaluate(bean, rule);
                         applyStyles = Boolean.valueOf(o+"");
                     } catch (Exception ign){
                         System.out.println("STYLE RULE ERROR: " + ign.getMessage());
                     }
                 }
                 
                 if ( applyStyles ) {
                     ControlSupport.setStyles( r.getProperties(), comp );
                 }
             }
         }
         
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  StringRenderer (class)  ">
     public static class StringRenderer extends AbstractRenderer {
         
         private JLabel label;
         
         public StringRenderer() {
             label = new JLabel();
             label.setVerticalAlignment(SwingConstants.CENTER);
         }
         
         public JComponent getComponent(JTable table, int row, int column) {
             return label;
         }
         
         public void refresh(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
             TableControl tc = (TableControl) table;
             Column c = ((TableControlModel) tc.getModel()).getColumn(column);
             String format = c.getFormat();
             String type = c.getType();
             if ( "decimal".equals(type) || "double".equals(type) || value instanceof BigDecimal || value instanceof Double ) {
                 label.setHorizontalAlignment( SwingConstants.RIGHT );
                 label.setText((value == null ? "" : format(value, format, "#,##0.00")));
                 
             } else if ( "integer".equals(type) || value instanceof Number ) {
                 label.setHorizontalAlignment( SwingConstants.CENTER );
                 label.setText((value == null ? "" : format(value, format, "#,##0")));
                 
             } else if ( "date".equals(type) || value instanceof Date ||
                     value instanceof Time || value instanceof Timestamp ) {
                 
                 label.setHorizontalAlignment( SwingConstants.CENTER );
                 SimpleDateFormat formatter = null;
                 if ( format != null )
                     formatter = new SimpleDateFormat(format);
                 else
                     formatter = new SimpleDateFormat("yyyy-MM-dd");
                 
                 label.setText((value == null ? "" : formatter.format(value)));
                 
             } else {
                 label.setHorizontalAlignment( SwingConstants.LEFT );
                 if ( value != null && c.isHtmlDisplay() ) {
                     value = "<html>" + value + "</html>";
                 }
                 label.setText((value == null ? "" : value.toString()));
             }
             
             //set alignment if it is specified in the Column model
             if ( c.getAlignment() != null ) {
                 if ( "right".equals(c.getAlignment().toLowerCase()) )
                     label.setHorizontalAlignment(SwingConstants.RIGHT);
                 else if ( "center".equals(c.getAlignment().toLowerCase()))
                     label.setHorizontalAlignment(SwingConstants.CENTER);
                 else if ( "left".equals(c.getAlignment().toLowerCase()) )
                     label.setHorizontalAlignment(SwingConstants.LEFT);
             }
             
             //set vertical alignment if it is specified in the Column model
             if ( c.getVAlignment() != null ) {
                 if ( "top".equals(c.getVAlignment().toLowerCase()) )
                     label.setVerticalAlignment(SwingConstants.TOP);
                 else if ( "center".equals(c.getVAlignment().toLowerCase()) )
                     label.setVerticalAlignment(SwingConstants.CENTER);
                 else if ( "bottom".equals(c.getVAlignment().toLowerCase()) )
                     label.setVerticalAlignment(SwingConstants.BOTTOM);
             }
         }
         
         private String format(Object value, String format, String defaultFormat) {
             DecimalFormat formatter = null;
             if ( format != null)
                 formatter = new DecimalFormat(format);
             else
                 formatter = new DecimalFormat(defaultFormat);
             
             return formatter.format(value);
         }
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  BooleanRenderer (class)  ">
     public static class BooleanRenderer extends AbstractRenderer {
         
         private JCheckBox component;
         private JLabel empty;
         
         public BooleanRenderer() {
             component = new JCheckBox();
             component.setHorizontalAlignment(SwingConstants.CENTER);
             component.setBorderPainted(true);
             
             //empty renderer when row object is null
             empty = new JLabel("");
         }
         
         public JComponent getComponent(JTable table, int row, int column) {
             AbstractListModel alm = ((TableControl) table).getListModel();
             if ( alm.getItemList().get(row).getItem() == null )
                 return empty;
             
             return component;
         }
         
         public void refresh(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
             AbstractListModel alm = ((TableControl) table).getListModel();
             if ( alm.getItemList().get(row).getItem() == null ) return;
             
             component.setSelected("true".equals(value+""));
         }
     }
     //</editor-fold>
     
 }
