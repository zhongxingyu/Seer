 /*
  * XComboBox.java
  *
  * Created on June 26, 2010, 1:37 PM
  * @author jaycverg
  */
 
 package com.rameses.rcp.control;
 
 import com.rameses.common.PropertyResolver;
 import com.rameses.rcp.common.PropertySupport;
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.support.ComboBoxEditorSupport;
 import com.rameses.rcp.support.ThemeUI;
 import com.rameses.rcp.ui.ActiveControl;
 import com.rameses.rcp.ui.ControlProperty;
 import com.rameses.rcp.ui.UIInput;
 import com.rameses.rcp.ui.Validatable;
 import com.rameses.rcp.util.ActionMessage;
 import com.rameses.rcp.util.UIControlUtil;
 import com.rameses.rcp.util.UIInputUtil;
 import com.rameses.common.ExpressionResolver;
 import com.rameses.rcp.control.table.ExprBeanSupport;
 import com.rameses.rcp.support.FontSupport;
 import com.rameses.util.ValueUtil;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.ItemEvent;
 import java.awt.event.KeyEvent;
 import java.beans.Beans;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.InputVerifier;
 import javax.swing.JComboBox;
 import javax.swing.JComboBox.KeySelectionManager;
 import javax.swing.JComponent;
 import javax.swing.JTable;
import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 public class XComboBox extends JComboBox implements UIInput, Validatable, ActiveControl 
 {
     protected Binding binding;
     
     private int index;
     private String[] depends;
     private String fontStyle; 
     private String caption;
     private String varName;
     private String items;
     private String itemKey; 
     private String expression; 
     private String disableWhen; 
     private String visibleWhen;
     private String emptyText = "-"; 
     private Object itemsObject; 
     private boolean immediate;
     private boolean dynamic;    
     private boolean allowNull = true;
     private boolean readonly;
     
     private ControlProperty property = new ControlProperty();
     private ActionMessage actionMessage = new ActionMessage();
     private Font sourceFont;
     
     protected ComboBoxModelImpl model;
     private Class fieldType;    
     private boolean updating;    
         
     public XComboBox() {
         initComponents();
     }
     
     // <editor-fold defaultstate="collapsed" desc="  initComponents method  ">
     
     private void initComponents() 
     {
         if ( Beans.isDesignTime() ) 
         {
             model = new ComboBoxModelImpl(new Object[]{"Item 1"});
             super.setModel( model );
         }
 
         setVarName("item"); 
         
         //default font
         Font f = ThemeUI.getFont("XComboBox.font");
         if ( f != null ) setFont( f );
 
         UIManager.put("ComboBox.disabledForeground", getForeground());        
         ComboBoxEditorSupport.install(this);
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  Getters/Setters  ">
     
     public String getVarName() { return varName; }
     public void setVarName(String varName) { this.varName = varName; }
     
     public String getName() { return super.getName(); } 
     public void setName(String name) 
     {
         super.setName(name);
         
         if ( Beans.isDesignTime() ) 
         {
             model.removeAllElements();
             model.addElement(name);
         }
     }
     
     public Object getValue() 
     {
         if ( Beans.isDesignTime() ) return null;        
         if ( super.getSelectedItem() == null ) return null;
         
         Object value = ((ComboItem) super.getSelectedItem()).getValue();
         if ( value != null && !ValueUtil.isEmpty(itemKey) ) 
         {
             PropertyResolver res = PropertyResolver.getInstance();
             value = res.getProperty(value, itemKey);
         }        
         return value;
     }    
     public void setValue(Object value) 
     {
         if ( Beans.isDesignTime() ) return;
         
         if ( value instanceof KeyEvent ) {
             processKeyEventValue((KeyEvent)value);
             return;
         }
         
         ComboItem selObj = (ComboItem) getSelectedItem();
         if (isSelected(selObj, value)) return;
         
         if ( value == null && !isAllowNull() ) {
             ComboItem c = (ComboItem) getItemAt(0);
             model.setSelectedItem( c );
             UIInputUtil.updateBeanValue(this);
             
         } else {
             boolean has_selection = false;
             for (int i=0; i<getItemCount(); i++) {
                 ComboItem ci = (ComboItem) getItemAt(i);
                 if (!isSelected(ci, value)) continue;
 
                 model.setSelectedItem(ci);
                 has_selection = true;
                 break; 
             } 
             
             if (!has_selection && getItemCount() > 0) {
                 ComboItem ci = (ComboItem) getItemAt(0);
                 model.setSelectedItem(ci); 
            }
         }
     }
     
     private void processKeyEventValue(KeyEvent evt) 
     {
         KeySelectionManager ksm = getKeySelectionManager();
         if ( ksm != null ) 
         {
             int idx = ksm.selectionForKey(evt.getKeyChar(), model);
             if( idx >= 0 ) model.setSelectedItem( model.getElementAt(idx) );
         }
     }
     
     protected boolean isSelected(ComboItem ci, Object value) 
     {
         if ( value != null && !ValueUtil.isEmpty(itemKey) ) 
         {
             if ( ci.getValue() == null ) return false;
             
             PropertyResolver res = PropertyResolver.getInstance();
             Object key = res.getProperty(ci.getValue(), itemKey);
             return key != null && value.equals(key);            
         } 
         else 
         {
             ComboItem c = new ComboItem( value );
             return ci.equals(c);
         }
     }
     
     public boolean isNullWhenEmpty() { return true; }
     
     public String[] getDepends() { return depends; }    
     public void setDepends(String[] depends) {
         this.depends = depends;
     }
     
     public int getIndex() { return index; }    
     public void setIndex(int index) {
         this.index = index;
     }
 
     public Binding getBinding() { return binding; }    
     public void setBinding(Binding binding) {
         this.binding = binding;
     }
         
     public String getItems() { return items; }    
     public void setItems(String items) {
         this.items = items;
     }
     
     public String getExpression() { return expression; }    
     public void setExpression(String expression) {
         this.expression = expression;
     }
     
     public String getDisableWhen() { return disableWhen; } 
     public void setDisableWhen(String disableWhen) {
         this.disableWhen = disableWhen;
     }
     
     public String getVisibleWhen() { return visibleWhen; } 
     public void setVisibleWhen(String visibleWhen) {
         this.visibleWhen = visibleWhen;
     }    
     
     public boolean isAllowNull() { return allowNull; }    
     public void setAllowNull(boolean allowNull) {
         this.allowNull = allowNull;
     }
     
     public String getCaption() { 
         return property.getCaption(); 
     } 
     public void setCaption(String caption) {
         property.setCaption(caption);
     }
     
     public char getCaptionMnemonic() { 
         return property.getCaptionMnemonic();
     }    
     public void setCaptionMnemonic(char c) {
         property.setCaptionMnemonic(c);
     }
     
     public boolean isRequired() {
         return property.isRequired();
     }    
     public void setRequired(boolean required) {
         property.setRequired(required);
     }
     
     public int getCaptionWidth() {
         return property.getCaptionWidth();
     }    
     public void setCaptionWidth(int width) {
         property.setCaptionWidth(width);
     }
     
     public boolean isShowCaption() {
         return property.isShowCaption();
     }
     public void setShowCaption(boolean showCaption) {
         property.setShowCaption(showCaption);
     }
     
     public Font getCaptionFont() {
         return property.getCaptionFont();
     }
     public void setCaptionFont(Font f) {
         property.setCaptionFont(f);
     }
     
     public String getCaptionFontStyle() { 
         return property.getCaptionFontStyle();
     } 
     public void setCaptionFontStyle(String captionFontStyle) {
         property.setCaptionFontStyle(captionFontStyle); 
     }      
     
     public String getFontStyle() { return fontStyle; } 
     public void setFontStyle(String fontStyle) {
         this.fontStyle = fontStyle;
         if (sourceFont == null) {
             sourceFont = super.getFont();
         } else {
             super.setFont(sourceFont); 
         } 
         new FontSupport().applyStyles(this, fontStyle);
     }    
     
     public Insets getCellPadding() {
         return property.getCellPadding();
     }
     public void setCellPadding(Insets padding) {
         property.setCellPadding(padding);
     }
     
     public void validateInput() 
     {
         actionMessage.clearMessages();
         property.setErrorMessage(null);
         if ( isRequired() && ValueUtil.isEmpty(getValue()) ) 
         {
             actionMessage.clearMessages();
             actionMessage.addMessage("1001", "{0} is required.", new Object[] {getCaption()});
             property.setErrorMessage(actionMessage.toString());
         }
     }
     
     public ActionMessage getActionMessage() { return actionMessage; }    
     public ControlProperty getControlProperty() { return property; }
     
     public Class getFieldType() { return fieldType; }    
     public void setFieldType(Class fieldType) {
         this.fieldType = fieldType;
     }
 
     public void setRequestFocus(boolean focus) {
         if ( focus ) requestFocus();
     }
     
     public boolean isImmediate() { return immediate; }    
     public void setImmediate(boolean immediate) {
         this.immediate = immediate;
     }
     
     public String getItemKey() { return itemKey; }    
     public void setItemKey(String itemKey) {
         this.itemKey = itemKey;
     }
     
     public String getEmptyText() { return emptyText; }    
     public void setEmptyText(String emptyText) {
         this.emptyText = emptyText;
     }
     
     public boolean isDynamic() { return dynamic; }    
     public void setDynamic(boolean dynamic) {
         this.dynamic = dynamic;
     }
     
     public Object getItemsObject() { return itemsObject; }    
     public void setItemsObject(Object itemsObject) {
         this.itemsObject = itemsObject;
     }
 
     public void setPropertyInfo(PropertySupport.PropertyInfo info) 
     {
         if (info == null) return;
         
         PropertyInfoWrapper pi = new PropertyInfoWrapper(info);
         setExpression(pi.getExpression());
         setItemKey(pi.getItemKey());
         
         Object items = pi.getItems();
         if (items instanceof String) 
         {
             setItems(items.toString()); 
             setItemsObject(null);
         }
         else 
         {
             setItems(null);
             setItemsObject(items);
         }
     }
         
     public boolean isReadonly() { return readonly; }    
     public void setReadonly(boolean readonly) 
     { 
         this.readonly = readonly; 
         super.setEnabled(!readonly);
         super.firePropertyChange("enabled", readonly, !readonly);
         repaint(); 
     }
     
     // </editor-fold>    
     
     // <editor-fold defaultstate="collapsed" desc="  buildList  ">
     
     private Binding getCurrentBinding() {
         if ("true".equals(getClientProperty(JTable.class)+"")) 
         {
             if (getVarName() == null) setVarName("item");
             
             Object o = getClientProperty(Binding.class); 
             if (o instanceof Binding) return (Binding)o;
         } 
         return getBinding(); 
     }
     
     private Collection fetchItems() 
     {        
         Collection list = null;
         try 
         {
             Class type = null; 
             Binding oBinding = getCurrentBinding();            
             Object beanItems = UIControlUtil.getBeanValue(oBinding, getItems());
             if ( beanItems != null ) 
             {
                 type = beanItems.getClass();
                 if ( type.isArray() ) 
                     list = Arrays.asList((Object[]) beanItems);
                 else if ( beanItems instanceof Collection ) 
                     list = (Collection) beanItems;
             } 
             else 
             {
                 if ( fieldType != null )
                     type = fieldType;
                 else
                     type = UIControlUtil.getValueType(this, getName());
                 
                 //if type is null, happens when the source is a Map key and no fieldType supplied
                 //try to use the classtype of the value if it is not null
                 if ( type == null ) 
                 {
                     Object value = UIControlUtil.getBeanValue(this);
                     if ( value != null ) type = value.getClass();
                 }
                 
                 if ( type != null && type.isEnum()) 
                     list = Arrays.asList(type.getEnumConstants());
             }
         } 
         catch(Exception e) {;}
         
         if ( itemsObject != null ) 
         {
             Collection col = null;
             if ( itemsObject instanceof Collection )
                 col = (Collection) itemsObject;
             else if ( itemsObject.getClass().isArray() )
                 col = Arrays.asList((Object[]) itemsObject);
             
             if ( list == null )
                 list = col;
             else
                 list.addAll( col );
         }        
         return list;
     }
     
     private void buildList() 
     {
         updating = true;
         model.removeAllElements(); //clear combo model
         
         if ( allowNull ) addItem(null, emptyText);
         
         Collection list = fetchItems();
         if ( list == null ) return;
         
         ExpressionResolver er = ExpressionResolver.getInstance();
         for ( Object o: list ) 
         {
             Object caption = null;
             if ( !ValueUtil.isEmpty(expression) )    
             {
                 Object exprBean = createExpressionBean(o);
                 caption = UIControlUtil.evaluateExpr(exprBean, expression); 
             } 
             
             if ( caption == null ) caption = o;
             
             addItem(o, caption+"");
         }
        SwingUtilities.updateComponentTreeUI(this);
         updating = false;
     }
     
     private void addItem(Object value, String text) 
     {
         ComboItem cbo = new ComboItem(value, text);
         model.addElement(cbo);
     }
     
     private Object createExpressionBean(Object itemBean) 
     {
         ExprBeanSupport beanSupport = new ExprBeanSupport(binding.getBean());
         beanSupport.setItem(getVarName(), itemBean); 
         return beanSupport.createProxy(); 
     }    
     
     // </editor-fold>    
     
     public void load() {
         model = new ComboBoxModelImpl();        
         super.setModel(model);
         
         if ( !dynamic ) buildList();
         
         if ( !immediate ) {
             //super.addItemListener(this);
         } 
         else 
         {
             super.setInputVerifier(new InputVerifier() {
                 public boolean verify(JComponent input) 
                 {
                     if ( isPopupVisible() ) return true;
                     
                     return UIInputUtil.VERIFIER.verify(input);
                 }
             });
         }
     }   
     
     public void refresh() {
         try {
             if (isEnabled()) setReadonly(isReadonly());
             
             if ( dynamic ) {
                 EventQueue.invokeLater(new Runnable(){
                     public void run() {
                         try {
                             buildList();
                         } catch(Exception e) {
                             if (ClientContext.getCurrentContext().isDebugMode()) 
                                 e.printStackTrace();
                         }
                     }
                 });
             }
             //else {
                 EventQueue.invokeLater(new Runnable() {
                     public void run() {
                         try {
                             Object value = UIControlUtil.getBeanValue(XComboBox.this);
                             setValue(value);
                         } catch(Exception e) {
                             if (ClientContext.getCurrentContext().isDebugMode())
                                 e.printStackTrace();
                         }
                     }
                 }); 
             //} 
         } 
         catch(Exception e) {
             setEnabled(false);
             setFocusable(false);
             
             if ( ClientContext.getCurrentContext().isDebugMode() ) e.printStackTrace();
         }
     }
     
     public int compareTo(Object o) {
         return UIControlUtil.compare(this, o);
     }      
     
     protected void onItemStateChanged(ItemEvent e) {
         if ( e.getStateChange() == ItemEvent.SELECTED && !updating ) {
             try {
                 UIInputUtil.updateBeanValue(this);
             } catch(Throwable ex) {;}
         }
     }
 
     protected final void fireItemStateChanged(ItemEvent e) {
         if (isReadonly()) return;
         
         onItemStateChanged(e);        
         super.fireItemStateChanged(e); 
     }
     
     // <editor-fold defaultstate="collapsed" desc=" ComboBoxModelImpl ">
     
     private class ComboBoxModelImpl extends DefaultComboBoxModel 
     {
         XComboBox root = XComboBox.this;
         
         ComboBoxModelImpl() {
             super();
         }
         
         ComboBoxModelImpl(Object[] items) {
             super(items);
         }        
         
         void fireSelectionChanged() {
             fireContentsChanged(this, -1, -1); 
         }
 
         ItemEvent createItemStateChangedEvent(Object value) {
             return new ItemEvent(
                 root, 
                 ItemEvent.ITEM_STATE_CHANGED, 
                 value, 
                 ItemEvent.SELECTED
             );             
         }
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" ComboItem (class) ">
     
     public class ComboItem {
         private String text;
         private Object value;
         
         public ComboItem(Object v) {
             value = v;
         }
         
         public ComboItem(Object v , String t) {
             text = ValueUtil.isEmpty(t)? "": t;
             value = v;
         }
         
         public String toString() { return text; }        
         public Object getValue() { return value; }
         
         public boolean equals(Object o) {
             if (o == null) return false;
             if (!(o instanceof ComboItem)) return false;
             
             ComboItem ci = (ComboItem)o;
             if (value == null && ci.value == null) 
                 return true;
             else if (value != null && ci.value == null) 
                 return false;
             else if (value == null && ci.value != null) 
                 return false;
             
             return value.equals(ci.value);
         }        
     }
     
     // </editor-fold>    
     
     // <editor-fold defaultstate="collapsed" desc=" PropertyInfoWrapper (Class)  "> 
     
     private class PropertyInfoWrapper 
     {
         private PropertySupport.ComboBoxPropertyInfo property;
         private Map map = new HashMap(); 
         
         PropertyInfoWrapper(PropertySupport.PropertyInfo info) 
         {
             if (info instanceof Map) map = (Map)info;
             if (info instanceof PropertySupport.ComboBoxPropertyInfo)
                 property = (PropertySupport.ComboBoxPropertyInfo) info;
         }
         
         public String getExpression() 
         {
             Object value = map.get("expression");
             if (value == null && property != null)
                 value = property.getExpression();
             
             return (value == null? null: value.toString());
         }
         
         public String getItemKey() 
         {
             Object value = map.get("itemKey");
             if (value == null && property != null)
                 value = property.getItemKey();
             
             return (value == null? null: value.toString());
         }   
         
         public Object getItems() 
         {
             Object value = map.get("items");
             if (value == null && property != null)
                 value = property.getItems();
             
             return value;
         }        
     }
     
     // </editor-fold>        
 }
