 package com.rameses.rcp.control;
 
 import com.rameses.rcp.common.PropertySupport;
 import com.rameses.rcp.constant.TextCase;
 import com.rameses.rcp.constant.TrimSpaceOption;
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.support.TextDocument;
 import com.rameses.rcp.support.TextEditorSupport;
 import com.rameses.rcp.support.ThemeUI;
 import com.rameses.rcp.ui.ActiveControl;
 import com.rameses.rcp.ui.ControlProperty;
 import com.rameses.rcp.ui.UIInput;
 import com.rameses.rcp.ui.Validatable;
 import com.rameses.rcp.util.ActionMessage;
 import com.rameses.rcp.util.UIControlUtil;
 import com.rameses.rcp.util.UIInputUtil;
 import com.rameses.util.ValueUtil;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.event.FocusEvent;
 import java.beans.Beans;
 import javax.swing.JTextArea;
 import javax.swing.UIManager;
 
 /**
  *
  * @author Windhel
  */
 
 public class XTextArea extends JTextArea implements UIInput, Validatable, ActiveControl 
 {
     private Color focusBackground;
     private Color disabledBackground;
     private Color enabledBackground;
     
     private Binding binding;
     private int index;
     private boolean nullWhenEmpty = true;
     private String[] depends;
     private ControlProperty property = new ControlProperty();
     private ActionMessage actionMessage = new ActionMessage();
     private boolean readonly;
     
     private TextDocument textDocument = new TextDocument();
     private TrimSpaceOption trimSpaceOption = TrimSpaceOption.NONE;
     
     private String hint;
     private boolean showHint;
     
     
     public XTextArea() 
     {
         super();
         
         TextEditorSupport.install(this);
         
         //default font
         Font f = ThemeUI.getFont("XTextArea.font");
         if ( f != null ) setFont( f );
         
         //set default margin
         setMargin(new Insets(2,2,2,2));
         setPreferredSize(new Dimension(100,40));
        focusBackground = ThemeUI.getColor("XTextField.focusBackground"); 
     }
         
     public void paint(Graphics origGraphics) 
     {
         super.paint(origGraphics);
         
         if ( showHint && getDocument().getLength() == 0 ) 
         {
             Graphics g = origGraphics.create();
             Font f = getFont();
             FontMetrics fm = g.getFontMetrics(f);
             g.setColor(Color.LIGHT_GRAY);
             g.setFont( f );
             
             Insets margin = getInsets();
             int x = margin.left;
             int y = margin.top + fm.getAscent();
             g.drawString(" " + getHint(), x, y);
             g.dispose();
         }
     }
     
     public void refresh() 
     {
         try 
         {
             //force to update component's status
             updateBackground();
             
             Object value = UIControlUtil.getBeanValue(this);
             setValue(value);
             setCaretPosition(0);
         } 
         catch(Exception e) 
         {
             setText("");
             
             if (ClientContext.getCurrentContext().isDebugMode()) 
                 e.printStackTrace();
         }
     }
     
     public void load() 
     {
         setInputVerifier(UIInputUtil.VERIFIER);
         setDocument(textDocument);
     }
     
     public int compareTo(Object o) {
         return UIControlUtil.compare(this, o);
     }
     
     public void validateInput() 
     {
         actionMessage.clearMessages();
         property.setErrorMessage(null);
         if ( isRequired() && ValueUtil.isEmpty(getText()) ) 
         {
             actionMessage.addMessage("", "{0} is required", new Object[]{ getCaption() });
             property.setErrorMessage(actionMessage.toString());
         }
     }
     
     
     // <editor-fold defaultstate="collapsed" desc="  Getters/Setters  "> 
     
     public void setName(String name) 
     {
         super.setName(name);
         super.setText(name);
     }
     
     public Object getValue() 
     {
         String text = getText();
         if ( ValueUtil.isEmpty(text) && nullWhenEmpty ) return null;
         
         if ( trimSpaceOption != null ) text = trimSpaceOption.trim(text);
         
         return text;
     }
     
     public void setValue(Object value) {
         setText(value == null? "" : value.toString());
     }
     
     public boolean isNullWhenEmpty() { return nullWhenEmpty; }    
     public void setNullWhenEmpty(boolean nullWhenEmpty) {
         this.nullWhenEmpty = nullWhenEmpty;
     }
     
     public String[] getDepends() { return depends; }
     public void setDepends(String[] depends) { this.depends = depends; }
     
     public int getIndex() { return index; }    
     public void setIndex(int index) { this.index = index; }
 
     public Binding getBinding() { return binding; }    
     public void setBinding(Binding binding) { this.binding = binding; }
         
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
     
     public boolean isShowCaption() {
         return property.isShowCaption();
     }    
     public void setShowCaption(boolean show) {
         property.setShowCaption(show);
     }
     
     public int getCaptionWidth() {
         return property.getCaptionWidth();
     }    
     public void setCaptionWidth(int width) {
         property.setCaptionWidth(width);
     }
     
     public Font getCaptionFont() {
         return property.getCaptionFont();
     }    
     public void setCaptionFont(Font f) {
         property.setCaptionFont(f);
     }
     
     public Insets getCellPadding() {
         return property.getCellPadding();
     }    
     public void setCellPadding(Insets padding) {
         property.setCellPadding(padding);
     }
     
     public ActionMessage getActionMessage() { return actionMessage; }
     
     public ControlProperty getControlProperty() { return property; }
     
     public boolean isImmediate() { return false; }
     
     public TextCase getTextCase() {
         return textDocument.getTextCase();
     }    
     public void setTextCase(TextCase textCase) {
         textDocument.setTextCase(textCase);
     }
     
     public TrimSpaceOption getTrimSpaceOption() {
         return trimSpaceOption;
     }    
     public void setTrimSpaceOption(TrimSpaceOption option) {
         this.trimSpaceOption = option;
     }
 
     public boolean isReadonly() { return readonly; }    
     public void setReadonly(boolean readonly) 
     {
         if (!isEnabled()) return;
 
         this.readonly = readonly;
         setEditable(!readonly);
         super.firePropertyChange("editable", readonly, !readonly);
     }
         
     public void setRequestFocus(boolean focus) {
         if ( focus ) requestFocus();
     }
     
     public String getHint() { return hint; }
     public void setHint(String hint) 
     {
         this.hint = hint;
         showHint = !ValueUtil.isEmpty(hint);
     }
 
     public void setPropertyInfo(PropertySupport.PropertyInfo info) {
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  others methods  ">
     
     public Color getFocusBackground() { return focusBackground; } 
     
     public Color getBackground() 
     {
         if (Beans.isDesignTime()) return super.getBackground();
         
         boolean enabled = isEnabled(); 
         if (enabled) 
         {
             if (hasFocus()) 
             {
                 Color newColor = getFocusBackground();
                return (newColor == null? enabledBackground: newColor);
             }
             else {
                return enabledBackground; 
             } 
         } 
         else { 
            return disabledBackground;
         } 
     } 
     
     protected void updateBackground() 
     {
         if (enabledBackground == null) 
             enabledBackground = UIManager.getLookAndFeelDefaults().getColor("TextField.background");
         if (disabledBackground == null)
             disabledBackground = UIManager.getLookAndFeelDefaults().getColor("TextField.disabledBackground");
         
         Color newColor = getBackground(); 
         setBackground(newColor); 
         repaint();
     }
     
     protected void processFocusEvent(FocusEvent e) 
     {
         if (e.getID() == FocusEvent.FOCUS_GAINED) 
         {
             updateBackground();
         } 
         
         else if (e.getID() == FocusEvent.FOCUS_LOST) 
         { 
             if (!e.isTemporary()) updateBackground(); 
         } 
         
         super.processFocusEvent(e); 
     } 
     
     // </editor-fold>    
 }
