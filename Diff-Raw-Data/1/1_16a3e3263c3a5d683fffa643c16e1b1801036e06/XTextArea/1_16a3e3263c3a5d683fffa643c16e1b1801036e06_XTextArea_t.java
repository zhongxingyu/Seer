 package com.rameses.rcp.control;
 
 import com.rameses.rcp.common.PropertySupport;
 import com.rameses.rcp.common.TextDocumentModel;
 import com.rameses.rcp.constant.TextCase;
 import com.rameses.rcp.constant.TrimSpaceOption;
 import com.rameses.rcp.framework.ActionHandler;
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.support.FontSupport;
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
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.beans.Beans;
 import javax.swing.JTextArea;
 import javax.swing.UIManager;
 import javax.swing.text.BadLocationException;
 
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
     private boolean readonly;    
     private boolean nullWhenEmpty = true;   
     private String[] depends;
     private String fontStyle;
     private ControlProperty property = new ControlProperty();
     private ActionMessage actionMessage = new ActionMessage();
     
     private TextDocument textDocument = new TextDocument();
     private TrimSpaceOption trimSpaceOption = TrimSpaceOption.NONE;
     private ActionHandlerImpl actionHandler = new ActionHandlerImpl();
     
     private String handler;
     private TextDocumentModel handlerObject; 
     
     private String hint;
     private boolean showHint;
         
     public XTextArea() 
     {
         super();
 
         TextEditorSupport.install(this);
         for (FocusListener l : getFocusListeners()) {
             removeFocusListener(l); 
         }
         
         //default font
         Font f = ThemeUI.getFont("XTextArea.font");
         if ( f != null ) setFont( f );
         
         //set default margin
         setMargin(new Insets(2,2,2,2));
         setPreferredSize(new Dimension(100,40));
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
         int oldCaretPos = getCaretPosition();
         try 
         {
             //force to update component's status
             updateBackground();
             
             Object value = UIControlUtil.getBeanValue(this);
             setValue(value);
         } 
         catch(Exception e) 
         {
             setText("");
             
             if (ClientContext.getCurrentContext().isDebugMode()) 
                 e.printStackTrace();
         }
         
         try {
             setCaretPosition(oldCaretPos); 
         } catch(Exception ign){;} 
     }
     
     public void load() 
     {
         setInputVerifier(UIInputUtil.VERIFIER);
         setDocument(textDocument);
         
         String shandler = getHandler();
         if (shandler != null) 
         {
             Object obj = UIControlUtil.getBeanValue(getBinding(), shandler); 
             if (obj instanceof TextDocumentModel) 
             {
                 handlerObject = (TextDocumentModel) obj;
                 handlerObject.setProvider(new DocumentProvider()); 
             }
         }
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
         
         if (Beans.isDesignTime()) super.setText(name);
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
     public void setBinding(Binding binding) 
     { 
         //detached the handler from the old binding
         if (this.binding != null) 
             this.binding.getActionHandlerSupport().remove(actionHandler); 
         
         this.binding = binding; 
         
         if (binding != null) 
             binding.getActionHandlerSupport().add(actionHandler); 
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
     
     public String getCaptionFontStyle() { 
         return property.getCaptionFontStyle();
     } 
     public void setCaptionFontStyle(String captionFontStyle) {
         property.setCaptionFontStyle(captionFontStyle); 
     } 
     
     public String getFontStyle() { return fontStyle; } 
     public void setFontStyle(String fontStyle) {
         this.fontStyle = fontStyle;
         new FontSupport().applyStyles(this, fontStyle);
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
     
     public String getHandler() { return handler; } 
     public void setHandler(String handler) { this.handler = handler; }
 
     public void setPropertyInfo(PropertySupport.PropertyInfo info) {
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  others methods  ">
     
     public Color getFocusBackground() { return focusBackground; } 
     
     public Color getBackground() 
     {
         if (Beans.isDesignTime()) return super.getBackground();
         
         if (enabledBackground == null) 
             enabledBackground = UIManager.getLookAndFeelDefaults().getColor("TextField.background");
         if (disabledBackground == null)
             disabledBackground = UIManager.getLookAndFeelDefaults().getColor("TextField.disabledBackground");
         
         Color preferredColor = null;
         boolean enabled = isEnabled(); 
         if (enabled) 
         {
             if (hasFocus()) 
             {
                 Color newColor = getFocusBackground();
                 preferredColor = (newColor == null? enabledBackground: newColor);
             }
             else {
                 preferredColor = enabledBackground; 
             } 
         } 
         else { 
             preferredColor = disabledBackground;
         } 
         
         return (preferredColor == null? super.getBackground(): preferredColor); 
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
     
     // <editor-fold defaultstate="collapsed" desc="  DocumentProvider (class)  ">
     
     private class DocumentProvider implements TextDocumentModel.Provider 
     {
         private XTextArea root = XTextArea.this; 
         
         public String getText() { 
             return root.getText(); 
         }
 
         public void setText(String text) 
         {
             if (text == null) return;
             
             root.setText(text); 
             root.repaint();
         }
 
         public void insertText(String text) 
         {
             if (text == null) return;
 
             int caretPos = root.getCaretPosition();
             try 
             {
                 int caretCharPos = (text == null? -1: text.indexOf('|'));
                 if (caretCharPos >= 0) 
                 {
                     StringBuffer sb = new StringBuffer(); 
                     sb.append(text.substring(0, caretCharPos));
                     sb.append(' ');
                     sb.append(text.substring(caretCharPos+1));
                     text = sb.toString(); 
                 }
 
                 root.textDocument.insertString(caretPos, text, null);
                 
                 if (caretCharPos >= 0) root.setCaretPosition(caretPos + caretCharPos);
             } 
             catch (BadLocationException ex) {
                 System.out.println("[XTextArea] failed to insert text at position " + caretPos + " caused by " + ex.getMessage());
             }
             finally {
                 repaint();                 
             }
         } 
         
         public void requestFocus() 
         {
             EventQueue.invokeLater(new Runnable() {
                 public void run() 
                 {
                     root.requestFocus();
                     root.grabFocus();
                 }
             }); 
         }
         
         public void load() { root.load(); }
         public void refresh() { root.refresh(); } 
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" ActionHandlerImpl (class) ">   
     
     private class ActionHandlerImpl implements ActionHandler
     {
         XTextArea root = XTextArea.this;
         
         public void onBeforeExecute() {
         }
 
         /*
          *  This method is called once a button is clicked.
          */
         public void onAfterExecute() 
         {
            if (root.isReadonly() || !root.isEnabled() || !root.isEditable()) return;
             if (!root.textDocument.isDirty()) return;
             
             UIInputUtil.updateBeanValue(root); 
         } 
     }
     
     // </editor-fold>    
 }
