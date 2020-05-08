 /*
  * DefaultTextField.java
  *
  * Created on May 8, 2013, 2:47 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.rcp.control.text;
 
 import com.rameses.rcp.support.ThemeUI;
 import java.awt.Color;
 import java.awt.event.FocusEvent;
 import java.awt.event.KeyEvent;
 import java.beans.Beans;
 import javax.swing.InputVerifier;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 /**
  *
  * @author wflores
  */
 public class DefaultTextField extends JTextField 
 {
     private InputVerifierProxy inputVerifierProxy;  
     private Color focusBackground;
     private Color disabledBackground;
     private Color enabledBackground;
     private boolean readonly;
     
     public DefaultTextField() 
     {
         super();
         initDefaults(); 
         resetInputVerifierProxy(); 
         
         focusBackground = ThemeUI.getColor("XTextField.focusBackground");
     }
 
     protected void initDefaults() {}
         
     protected InputVerifier getMainInputVerifier() { return null; } 
     protected InputVerifier getChildInputVerifier() { return null; } 
     
     public Color getFocusBackground() { return focusBackground; } 
     public Color getDisabledBackground() { return disabledBackground; } 
     public Color getEnabledBackground() { return enabledBackground; } 
     
     public Color getBackground() 
     {
         if (Beans.isDesignTime()) return super.getBackground();
         
        Color bgcolor = null;
         boolean enabled = isEnabled(); 
         if (enabled) 
         {
             if (hasFocus()) 
             {
                 Color newColor = getFocusBackground();
                bgcolor = (newColor == null? enabledBackground: newColor);
             }
             else {
                bgcolor = enabledBackground; 
             } 
         } 
         else { 
            bgcolor = disabledBackground;
         } 
        
        return bgcolor == null? new Color(255,255,255): bgcolor;
     } 
     
     public boolean isReadonly() { return readonly; }
     public void setReadonly(boolean readonly) 
     {
         if (!isEnabled()) return;
 
         this.readonly = readonly;
         setEditable(!readonly);
         super.firePropertyChange("editable", readonly, !readonly); 
     }
 
     public void setEnabled(boolean enabled) 
     {
         super.setEnabled(enabled);
         setEditable((enabled? !isReadonly(): enabled));    
     }
     
     protected void resetInputVerifierProxy() 
     {
         inputVerifierProxy = new InputVerifierProxy(getMainInputVerifier()); 
         inputVerifierProxy.setChild(getChildInputVerifier());        
         super.setInputVerifier(inputVerifierProxy); 
         super.putClientProperty(InputVerifierProxy.class, inputVerifierProxy);  
     }
     
     public final InputVerifier getInputVerifier() { return inputVerifierProxy; }    
     public final void setInputVerifier(InputVerifier verifier) { 
         inputVerifierProxy.setEnabled((verifier == null? false: true)); 
     } 
     
     public final InputVerifierProxy getInputVerifierProxy() { return inputVerifierProxy; }
     
     protected final void processFocusEvent(FocusEvent e) 
     {
         if (e.getID() == FocusEvent.FOCUS_GAINED) 
         {
             updateBackground();
             
             resetInputVerifierProxy(); 
             inputVerifierProxy.setEnabled(true); 
             super.setInputVerifier(inputVerifierProxy); 
             
             try { onfocusGained(e); } catch(Exception ex) {;} 
         } 
         else if (e.getID() == FocusEvent.FOCUS_LOST) 
         { 
             if (!e.isTemporary()) updateBackground(); 
             
             try { onfocusLost(e); } catch(Exception ex) {;} 
             
             inputVerifierProxy.setEnabled(false);
         } 
         
         super.processFocusEvent(e); 
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
     
     protected void onfocusGained(FocusEvent e) {
     }
     
     protected void onfocusLost(FocusEvent e) {
     }    
     
     public final void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) 
     {
         if ("enableInputVerifier".equals(propertyName)) 
             inputVerifierProxy.setEnabled(newValue); 
         else if ("detachInputVerifier".equals(propertyName)) 
             super.setInputVerifier((newValue == true? null: inputVerifierProxy));
         
         onpropertyChange(propertyName, oldValue, newValue); 
         super.firePropertyChange(propertyName, oldValue, newValue); 
     } 
         
     protected void onpropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
 
     protected void processKeyEvent(KeyEvent e) 
     {
         onprocessKeyEvent(e); 
         super.processKeyEvent(e); 
     }
     
     protected void onprocessKeyEvent(KeyEvent e){
     }     
 }
