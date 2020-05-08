 package com.rameses.rcp.control;
 
 import com.rameses.rcp.common.LookupOpenerSupport;
 import com.rameses.rcp.common.PropertySupport;
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.ui.ActiveControl;
 import com.rameses.rcp.util.ControlSupport;
 import com.rameses.rcp.common.Opener;
 import com.rameses.rcp.framework.UIController;
 import com.rameses.rcp.framework.UIControllerContext;
 import com.rameses.rcp.framework.UIControllerPanel;
 import com.rameses.rcp.ui.BindingConnector;
 import com.rameses.rcp.ui.ControlProperty;
 import com.rameses.rcp.ui.UIControl;
 import com.rameses.rcp.ui.UISubControl;
 import com.rameses.rcp.util.UIControlUtil;
 import com.rameses.util.ValueUtil;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.beans.Beans;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 
 /**
  *
  * @author jaycverg
  */
 public class XSubFormPanel extends JPanel implements UISubControl, ActiveControl 
 {    
     private String handler;
     private String[] depends;
     private String visibleWhen;
     private int index;
     private boolean dynamic;
     
     private boolean multiForm;
     private JPanel multiPanel;
     
     /** this can be set when you want to add openers
      *  directly to this component
      */
     private List<Opener> openers;
     private List<Binding> subBindings = new ArrayList();
     private Object handlerObj;
     
     protected Binding binding;
     protected BindingConnector bindingConnector = new  BindingConnector(this);
     protected List<SubFormContext> subFormItems = new ArrayList();
     protected List<Opener> currentOpeners = new ArrayList();
     protected ControlProperty property = new ControlProperty();
     
     
     public XSubFormPanel() 
     {
         super.setLayout(new BorderLayout());
         setOpaque(false);
         
         if ( Beans.isDesignTime() ) 
         {
             setPreferredSize( new Dimension(40,20) );
             setBackground( Color.decode("#a0a0a0") );
             setOpaque(true); 
         }
         
         addAncestorListener(new AncestorListener() {
             public void ancestorMoved(AncestorEvent event) {}
             public void ancestorAdded(AncestorEvent event) {
                 if (binding != null)
                     bindingConnector.setParentBinding(binding);
             }
             public void ancestorRemoved(AncestorEvent event) {
                 bindingConnector.setParentBinding(null);
             }
         });
     }
     
     public XSubFormPanel(Opener o) {
         this();
         getOpeners().add(o);
         multiForm = false;
     }
     
     public XSubFormPanel(List<Opener> o) {
         this();
         this.openers = o;
         multiForm = true;
     }
     
     public List<Opener> getOpeners() {
         if ( openers == null ) openers = new ArrayList();
         return openers;
     }
     
     public void setLayout(LayoutManager mgr) {;}
     
     public Component add(Component comp) {
         if ( multiForm ) {
             return multiPanel.add( comp );
         } else {
             return super.add( comp );
         }
     }
     
     public void refresh() {
         if ( dynamic ) {
             buildForm();
         }
         
         String sval = getVisibleWhen(); 
         if (sval != null && sval.length() > 0) {
             try { 
                 boolean bool = UIControlUtil.evaluateExprBoolean(getBinding().getBean(), sval);
                 setVisible(bool); 
             } catch(Throwable t) {;} 
         }
     }
     
     public void load() {
         if ( !dynamic ) {
             buildForm();
         }
         bindingConnector.setParentBinding(binding);
     }
     
     // <editor-fold defaultstate="collapsed" desc="  helper methods  ">
     
     protected void buildForm() 
     {
         handlerObj = null;
         
         //this is usually set by XTabbedPane or
         //other controls that used XSubForm internally
        if ( getOpeners().size() > 0 ) 
        {
             handlerObj = getOpeners();
         } 
        else if ( !ValueUtil.isEmpty(getHandler()) ) 
        {
             String shandler = getHandler();
             if ( shandler.matches(".+:.+") ) {
                 handlerObj = LookupOpenerSupport.lookupOpener(shandler, new HashMap()); 
             } else {
                 handlerObj = UIControlUtil.getBeanValue(getBinding().getBean(), shandler); 
             }
         }
         
        multiForm = true; //reset, check based on passed value        
         List<Opener> openers = new ArrayList();        
         if ( handlerObj == null ) {
             //do nothing
        } 
        else if ( handlerObj instanceof Collection ) {
             for (Object o: (Collection) handlerObj) {
                 openers.add( (Opener)o );
             }            
        } 
        else if ( handlerObj.getClass().isArray() ) {
             for (Object o: (Object[]) handlerObj) {
                 openers.add( (Opener)o );
             }            
        } 
        else if ( handlerObj instanceof Opener ) {
             openers.add( (Opener)handlerObj );
            multiForm = false;
         }
         
         //-- display support
         Set<Binding> connectorBindings = bindingConnector.getSubBindings();
         connectorBindings.clear();
         
         if ( openers.size() == 0 ) {
             removeAll();
             subFormItems.clear();
             SwingUtilities.updateComponentTreeUI(this);
             return;
         }
         
         if ( !multiForm && currentOpeners.size() > 0 && openers.get(0) == currentOpeners.get(0) && !subFormItems.isEmpty() ) {
             SubFormContext sfc = subFormItems.get(0);
             sfc.renderView();
             
             //register new subBindings
             connectorBindings.addAll(getSubBindings());
         } else {
             removeAll();
             subFormItems.clear();
             currentOpeners.clear();
             currentOpeners.addAll( openers );
             
             SwingUtilities.updateComponentTreeUI(this);
             
             //check if is a multi form
             if ( multiForm ) {
                 multiPanel = new JPanel();
                 multiPanel.setOpaque(false);
                 multiPanel.setLayout( new BoxLayout(multiPanel, BoxLayout.Y_AXIS) );
                 super.add(multiPanel, BorderLayout.NORTH);
             }
             
             for (Opener opener : openers) {
                 addOpener(opener);
             }
             
             //register new subBindings
             connectorBindings.addAll(getSubBindings());
         }
     }
     
     private void addOpener(Opener opener) {
         Map openerParams = opener.getParams();
         if (openerParams == null) {
             openerParams = new HashMap();
             opener.setParams(openerParams); 
         }
         Object userObj = getClientProperty(UIControl.KEY_USER_OBJECT); 
         if (userObj instanceof Map) {
             Object props = ((Map)userObj).get("properties"); 
             if (props instanceof Map) {
                 openerParams.putAll((Map)props);    
             }
         }
         
         UIController caller = binding.getController();
         opener.setCaller( caller );
         opener = ControlSupport.initOpener( opener, caller );
         UIController controller = opener.getController();
         
         if ( controller == null )
             throw new IllegalStateException("Cannot find controller " + opener.getName());
         
         UIControllerContext uic = new UIControllerContext( controller );
         if ( !ValueUtil.isEmpty(opener.getOutcome()) ) {
             uic.setCurrentView(opener.getOutcome());
         }
         SubFormContext sfc = new SubFormContext( uic );
         subFormItems.add( sfc );
         add( sfc );
     }
     //</editor-fold>
     
     public int compareTo(Object o) {
         return UIControlUtil.compare(this, o);
     }
     
     public boolean focusFirstInput() {
         for( Binding b: getSubBindings() ) {
             if ( b.focusFirstInput() ) return true;
         }
         return false;
     }
     
     public Object getHandlerObject() { return handlerObj; }
     public void setHandlerObject(Object handlerObj) 
     { 
         this.handlerObj = handlerObj; 
         
         if (handlerObj instanceof Opener) 
             getOpeners().add((Opener) handlerObj); 
     } 
     
     // <editor-fold defaultstate="collapsed" desc="  Getters/Setters  ">
     
     public Dimension getPreferredSize() {
         if ( getComponentCount() > 0 ) {
             return getComponent(0).getPreferredSize();
         } else {
             return super.getPreferredSize();
         }
     }
     
     public String getHandler() {
         return handler;
     }
     
     public void setHandler(String handler) {
         this.handler = handler;
     }
     
     public String[] getDepends() {
         return depends;
     }
     
     public void setDepends(String[] depends) {
         this.depends = depends;
     }
     
     public int getIndex() {
         return index;
     }
     
     public void setIndex(int index) {
         this.index = index;
     }
     
     public void setBinding(Binding binding) {
         this.binding = binding;
     }
     
     public Binding getBinding() {
         return binding;
     }
     
     public List<Binding> getSubBindings() {
         List<Binding> list = new ArrayList();
         
         for (SubFormContext sfc: subFormItems) {
             list.add( sfc.getCurrentController().getCurrentView().getBinding() );
         }
         
         return list;
     }
     
     public boolean isDynamic() {
         return dynamic;
     }
     
     public void setDynamic(boolean dynamic) {
         this.dynamic = dynamic;
     }
     
     public String getCaption() {
         if ( property.getCaption() == null ) {
             if ( !ValueUtil.isEmpty(getName()))
                 return getName();
             else
                 return handler;
         }
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
     
     public Insets getCellPadding() {
         return property.getCellPadding();
     }
     
     public void setCellPadding(Insets padding) {
         property.setCellPadding(padding);
     }
     
     public ControlProperty getControlProperty() {
         return property;
     }
 
     public void setPropertyInfo(PropertySupport.PropertyInfo info) {
     }
     
     public String getVisibleWhen() { return visibleWhen; } 
     public void setVisibleWhen(String visibleWhen) {
         this.visibleWhen = visibleWhen; 
     }
     
     // </editor-fold>
         
     // <editor-fold defaultstate="collapsed" desc="  SubFormContext (class)  ">
     
     protected class SubFormContext extends UIControllerPanel {
         
         SubFormContext(UIControllerContext controller) {
             super(controller);
             setOpaque(false);
             setName(XSubFormPanel.this.getName());
         }
         
         public void renderView() {
             super.renderView();
             
             Set<Binding> connectorBindings = bindingConnector.getSubBindings();
             connectorBindings.clear();
             connectorBindings.addAll(getSubBindings());
         }
         
     }
     
     // </editor-fold>     
 }
