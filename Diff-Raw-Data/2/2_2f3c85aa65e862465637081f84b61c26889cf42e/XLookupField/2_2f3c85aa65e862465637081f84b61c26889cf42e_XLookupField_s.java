 package com.rameses.rcp.control;
 
 import com.rameses.platform.interfaces.Platform;
 import com.rameses.rcp.common.LookupHandler;
 import com.rameses.rcp.common.LookupModel;
 import com.rameses.rcp.common.LookupSelector;
 import com.rameses.rcp.common.MsgBox;
 import com.rameses.rcp.common.Opener;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.util.ControlSupport;
 import com.rameses.rcp.framework.UIController;
 import com.rameses.rcp.framework.UIControllerContext;
 import com.rameses.rcp.framework.UIControllerPanel;
 import com.rameses.rcp.util.UIControlUtil;
 import com.rameses.rcp.util.UIInputUtil;
 import com.rameses.util.ValueUtil;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author jaycverg
  */
 
 public class XLookupField extends AbstractIconedTextField implements LookupSelector {
     
     private String handler;
     private Object handlerObject;
     private LookupModel lookupModel;
     private Object selectedValue;
     private UIController lookupController;
     private String expression;
     private boolean transerFocusOnSelect = true;
     private boolean dynamic;
     
     private XLookupSupport support = new XLookupSupport();
     
     
     public XLookupField() {
         super("com/rameses/rcp/icons/search.png");
         setOrientation( super.ICON_ON_RIGHT );
         addFocusListener( support );
         addKeyListener( support );
     }
     
     public void actionPerformed(){
         fireLookup();
     }
     
     public void validateInput() {
         actionMessage.clearMessages();
         property.setErrorMessage(null);
         if ( isRequired() && ValueUtil.isEmpty( getValue() ) ) {
             actionMessage.addMessage("1001", "{0} is required.", new Object[] {getCaption()});
             property.setErrorMessage(actionMessage.toString());
         }
     }
     
     //<editor-fold defaultstate="collapsed" desc="  refresh/load  ">
     public void refresh() {
         Object value = UIControlUtil.getBeanValue(this);
         if ( value != null ) {
             selectedValue = value;
             if ( !ValueUtil.isEmpty(expression) ) {
                 value = UIControlUtil.evaluateExpr(value, expression);
             }
         }
         
         setValue(value);
     }
     
     public void load() {
         super.load();
         setInputVerifier(null);
         
         if( !dynamic ) loadHandler();
     }
     
     private void loadHandler(){
         Object o = null;
         if( !ValueUtil.isEmpty(handler) ) {
             if ( handler.matches(".+:.+") ) //handler is a module:workunit name
                 o = new Opener(handler);
             else
                 o = UIControlUtil.getBeanValue(this, handler);
         } else if ( handlerObject != null ) {
             if( handlerObject instanceof LookupHandler )
                 o = ((LookupHandler) handlerObject).getHandler();
             else
                 o = handlerObject;
         }
         
         if( o == null ) return;
         
         if( o instanceof LookupModel ) {
             lookupModel = (LookupModel) o;
             
         } else {
             Opener opener = null;
             
             if( o instanceof Opener ) {
                 opener = (Opener)o;
             }
             //check if instanceof String, then load the opener.
             else {
                 opener = new Opener(handler);
             }
             
             if(opener == null)
                 throw new IllegalStateException("Lookup Handler must reference an Opener object");
             
             opener = ControlSupport.initOpener( opener, getBinding().getController() );
             lookupController = opener.getController();
             if( lookupController == null ) {
                 throw new IllegalStateException("Lookup Controller must be valid");
             }
             
             if( !(lookupController.getCodeBean() instanceof LookupModel) )
                throw new IllegalStateException("Lookup Handler code bean must be an instanceof LookupListModel");
             
             
             lookupController.setTitle( opener.getCaption() );
             lookupController.setId( opener.getId() );
             lookupController.setName( opener.getName() );
             lookupModel = (LookupModel) lookupController.getCodeBean();
         }
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  lookup dialog support  ">
     private void fireLookup() {
         try {
             if( dynamic ) loadHandler();
             
             lookupModel.setSelector(this);
             boolean show = lookupModel.show( getText() );
             if( show ) {
                 UIController c = lookupController;
                 if ( c == null ) return; //should use a default lookup handler
                 
                 UIControllerContext uic = new UIControllerContext(c);
                 Platform platform = ClientContext.getCurrentContext().getPlatform();
                 String conId = uic.getId();
                 if ( conId == null ) conId = getName() + handler;
                 if ( platform.isWindowExists(conId) ) return;
                 
                 UIControllerPanel lookupPanel = new UIControllerPanel( uic );
                 
                 Map props = new HashMap();
                 props.put("id", conId);
                 props.put("title", uic .getTitle());
                 
                 platform.showPopup(this, lookupPanel, props);
             }
         } catch(Exception e) {
             MsgBox.err(e);
         }
     }
     
     public void select(Object o) {
         selectedValue = o;
         UIInputUtil.updateBeanValue(this);
         this.refresh();
         support.setDirty(false);
         
         if ( transerFocusOnSelect )
             this.transferFocus();
         else
             this.requestFocus();
     }
     
     public void cancelSelection() {
         Object value = UIControlUtil.getBeanValue(this);
         this.refresh();
         support.setDirty(false);
         this.requestFocus();
         selectedValue = value;
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  Getters/Setters  ">
     public Object getValue() {
         return selectedValue;
     }
     
     public void setValue(Object value) {
         if ( value instanceof KeyEvent ) {
             setText( ((KeyEvent) value).getKeyChar()+"" );
             support.setDirty(true);
         } else {
             if ( value != null )
                 setText(value.toString());
             else
                 setText("");
         }
     }
     
     public String getHandler() {
         return handler;
     }
     
     public void setHandler(String handler) {
         this.handler = handler;
     }
     
     public Object getHandlerObject() {
         return handlerObject;
     }
     
     public void setHandlerObject(Object handlerObject) {
         this.handlerObject = handlerObject;
     }
     
     public String getExpression() {
         return expression;
     }
     
     public void setExpression(String expression) {
         this.expression = expression;
     }
     
     public boolean isTranserFocusOnSelect() {
         return transerFocusOnSelect;
     }
     
     public void setTranserFocusOnSelect(boolean transerFocusOnSelect) {
         this.transerFocusOnSelect = transerFocusOnSelect;
     }
     //</editor-fold>
     
     public boolean isDynamic() {
         return dynamic;
     }
 
     public void setDynamic(boolean dynamic) {
         this.dynamic = dynamic;
     }
     
     
     //<editor-fold defaultstate="collapsed" desc="  XLookupSupport (class)  ">
     private class XLookupSupport implements FocusListener, KeyListener {
         
         private boolean dirty;
         
         public void focusGained(FocusEvent e) {}
         
         public void focusLost(FocusEvent e) {
             if ( dirty && !e.isTemporary() ) {
                 if ( ValueUtil.isEmpty(getText()) ) {
                     setText("");
                     selectedValue = null;
                     UIInputUtil.updateBeanValue(XLookupField.this);
                 } else {
                     refresh();
                 }
                 dirty = false;
             }
         }
         
         public void keyTyped(KeyEvent e) {
             dirty = true;
         }
         
         public void keyPressed(KeyEvent e) {}
         public void keyReleased(KeyEvent e) {}
         
         public void setDirty(boolean dirty) {
             this.dirty = dirty;
         }
         
     }
     //</editor-fold>
 
 }
