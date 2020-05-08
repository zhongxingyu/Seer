 /*
  * XActionBar.java
  *
  * Created on July 23, 2010, 1:21 PM
  * @author jaycverg
  */
 
 package com.rameses.rcp.control;
 
 import com.rameses.rcp.common.Action;
 import com.rameses.rcp.framework.ActionProvider;
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.util.ControlSupport;
 import com.rameses.rcp.ui.UIComposite;
 import com.rameses.rcp.ui.UIControl;
 import com.rameses.rcp.util.UIControlUtil;
 import com.rameses.common.ExpressionResolver;
 import com.rameses.common.PropertyResolver;
 import com.rameses.rcp.constant.UIConstants;
 import com.rameses.rcp.framework.UIController;
 import com.rameses.util.ValueUtil;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.beans.Beans;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 
 public class XActionBar extends JPanel implements UIComposite {
     
     private Binding binding;
     private String[] depends;
     private boolean useToolBar;
     private boolean dynamic;
     private int spacing = 2;
     private int index;
     private Insets padding = new Insets(0,0,0,0);
     
     private String orientation = UIConstants.HORIZONTAL;
     private String orientationHAlignment = UIConstants.LEFT;
     private String orientationVAlignment = UIConstants.TOP;
     
     //XButton target
     private String target;
     
     private List<XButton> buttons = new ArrayList();
     private JComponent toolbarComponent;
     
     //flag
     private boolean dirty;
     
    private XButton buttonTemplate;
    
     
     public XActionBar() {
         super.setLayout(new OuterLayout());
         setUseToolBar(true);
        
        buttonTemplate = new XButton();
     }
     
     public void setLayout(LayoutManager mgr) {;}
     
     public void refresh() {
         buildToolbar();
     }
     
    public void load() {}
     
     public void reload() {
         buildButtons();
     }
     
     public int compareTo(Object o) {
         return UIControlUtil.compare(this, o);
     }
     
    public XButton getButtonTemplate() {
        return buttonTemplate;
    }
    
    public void setButtonTemplate(XButton buttonTemplate) {
        this.buttonTemplate = buttonTemplate;
    }
    
     //<editor-fold defaultstate="collapsed" desc="  helper methods  ">
     private void buildButtons() {
         buttons.clear();
         List<Action> actions = new ArrayList();
         
         //--get actions defined from the code bean
         Object value = null;
         try {
             value = UIControlUtil.getBeanValue(this);
         } catch(Exception e) {;}
         
         if (value == null) {
             //do nothing
         } else if (value.getClass().isArray()) {
             for (Action aa: (Action[]) value) {
                 actions.add(aa);
             }
         } else if (value instanceof Collection) {
             actions.addAll((Collection) value);
         }
         
         //--get actions defined from the action provider
         ActionProvider actionProvider = ClientContext.getCurrentContext().getActionProvider();
         if (actionProvider != null) {
             UIController controller = binding.getController();
             List<Action> aa = actionProvider.getActionsByType(getName(), controller);
             if (aa != null) actions.addAll(aa);
         }
         
         if (actions.size() == 0) return;
         
         Collections.sort(actions);
         for (Action action: actions) {
             //check permission
             String permission = action.getPermission();
             if (permission != null && binding.getController().getName() != null)
                 permission = binding.getController().getName() + "." + permission;
             
             boolean allowed = ControlSupport.isPermitted(permission);
             if (!allowed) continue;
             
             XButton btn = createButton(action);
             buttons.add(btn);
         }
         
         //set dirty flag to true
         dirty = true;
     }
     
     private XButton createButton(Action action) {
         XButton btn = new XButton();
         btn.setName(action.getName());
         btn.setText(action.getCaption());
         btn.setIndex(action.getIndex());
         btn.setUpdate(action.isUpdate());
         btn.setImmediate(action.isImmediate());
         btn.setMnemonic(action.getMnemonic());
         btn.setToolTipText(action.getTooltip());
         btn.setIcon(ControlSupport.getImageIcon(action.getIcon()));
         btn.putClientProperty("visibleWhen", action.getVisibleWhen());
         btn.setBinding(binding);
         
         Map props = new HashMap(action.getProperties());
         try {
             btn.setAccelerator(props.remove("shortcut")+"");
         } catch(Exception ign){;}
         
         if ( props.get("target") != null ) {
             btn.setTarget(props.remove("target")+"");
         }
         
         if ( props.get("default") != null ) {
             String dfb = props.remove("default")+"";
             if ( dfb.equals("true")) {
                 btn.putClientProperty("default.button", true);
             }
         }
         
         //map out other properties
         if ( !props.isEmpty() ) {
             PropertyResolver res = ClientContext.getCurrentContext().getPropertyResolver();
             for(Object entry : props.entrySet()) {
                 Map.Entry me = (Map.Entry) entry;
                 try {
                     res.setProperty( btn, (String) me.getKey(), me.getValue());
                 }catch(Exception e){;}
             }
         }
         
         Map params = action.getParams();
         if (params != null && params.size() > 0) {
             btn.getParams().putAll(params);
         }
         
         if ( !action.getClass().getName().equals(Action.class.getName()) ) {
             btn.putClientProperty(Action.class.getName(), action);
         }
         
         return btn;
     }
     
     private void buildToolbar() {
         if ( dirty ) {
             toolbarComponent.removeAll();
         }
         
         ExpressionResolver expResolver = ClientContext.getCurrentContext().getExpressionResolver();
         for (XButton btn: buttons) {
             String expression = (String) btn.getClientProperty("visibleWhen");
             if (!ValueUtil.isEmpty(expression)) {
                 Object visible = expResolver.evaluate(binding.getBean(), expression);
                 btn.setVisible( !"false".equals(visible+"") );
             }
             
             if ( dirty ) toolbarComponent.add(btn);
         }
         
         SwingUtilities.updateComponentTreeUI(this);
         dirty = false;
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  Getters/Setters  ">
     public Insets getPadding() { return padding; }
     public void setPadding(Insets padding) { this.padding = padding; }
     
     public int getSpacing() { return spacing; }
     public void setSpacing(int spacing) { this.spacing = spacing; }
     
     public List<? extends UIControl> getControls() { return buttons; }
     
     public String[] getDepends() { return depends; }
     public void setDepends(String[] depends) { this.depends = depends; }
     
     public int getIndex() { return index; }
     public void setIndex(int index) { this.index = index; }
     
     public Binding getBinding() { return binding; }
     public void setBinding(Binding binding) { this.binding = binding; }
     
     public boolean isUseToolBar() { return useToolBar; }
     public void setUseToolBar(boolean useToolBar) {
         this.useToolBar = useToolBar;
         
         super.removeAll();
         if (useToolBar) {
             JToolBar tlb = new JToolBar();
             tlb.setFloatable(false);
             tlb.setRollover(true);
             toolbarComponent = tlb;
         } else {
             toolbarComponent = new JPanel();
         }
         
         toolbarComponent.setLayout(new InnerLayout());
         toolbarComponent.setName("toolbar");
         toolbarComponent.setOpaque(false);
         add(toolbarComponent);
         
         if (Beans.isDesignTime())
             toolbarComponent.add(new JButton(XActionBar.class.getSimpleName()));
     }
     
     public boolean isDynamic() { return dynamic; }
     public void setDynamic(boolean dynamic) { this.dynamic = dynamic; }
     
     public int getHorizontalAlignment() { return 0; }
     public void setHorizontalAlignment(int horizontalAlignment) {}
     
     
     public String getTarget() { return target; }
     public void setTarget(String target) { this.target = target; }
     
     
     public String getOrientation() { return orientation; }
     public void setOrientation(String orientation) {
         if ( orientation != null )
             this.orientation = orientation.toUpperCase();
         else
             this.orientation = UIConstants.HORIZONTAL;
     }
     
     public String getOrientationHAlignment() { return orientationHAlignment; }
     public void setOrientationHAlignment(String alignment) {
         if ( alignment != null )
             this.orientationHAlignment = alignment.toUpperCase();
         else
             this.orientationHAlignment = UIConstants.LEFT;
     }
     
     public String getOrientationVAlignment() { return orientationVAlignment; }
     public void setOrientationVAlignment(String alignment) {
         if ( alignment != null )
             this.orientationVAlignment = alignment.toUpperCase();
         else
             this.orientationVAlignment = UIConstants.TOP;
     }
     
     public boolean focusFirstInput() {
         return false;
     }
     
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc=" OuterLayout (Class) ">
     private class OuterLayout implements LayoutManager {
         public void addLayoutComponent(String name, Component comp) {;}
         public void removeLayoutComponent(Component comp) {;}
         
         public Dimension getLayoutSize(Container parent) {
             synchronized (parent.getTreeLock()) {
                 int w=0, h=0;
                 if (toolbarComponent != null) {
                     Insets margin = parent.getInsets();
                     Dimension dim = toolbarComponent.getPreferredSize();
                     w = (margin.left + dim.width + margin.right);
                     h = (margin.top + dim.height + margin.bottom);
                     
                     Insets pads = getPadding();
                     if (pads != null) {
                         w += (pads.left + pads.right);
                         h += (pads.top + pads.bottom);
                     }
                 }
                 return new Dimension(w, h);
             }
         }
         
         public Dimension preferredLayoutSize(Container parent) {
             return getLayoutSize(parent);
         }
         
         public Dimension minimumLayoutSize(Container parent) {
             return getLayoutSize(parent);
         }
         
         public void layoutContainer(Container parent) {
             synchronized (parent.getTreeLock()) {
                 if (toolbarComponent != null) {
                     Insets margin = parent.getInsets();
                     int x = margin.left;
                     int y = margin.top;
                     int w = parent.getWidth() - (margin.left + margin.right);
                     int h = parent.getHeight() - (margin.top + margin.bottom);
                     
                     Insets pads = getPadding();
                     if (pads != null) {
                         x += pads.left;
                         y += pads.top;
                         w -= (pads.left + pads.right);
                         h -= (pads.top + pads.bottom);
                     }
                     
                     toolbarComponent.setBounds(x, y, w, h);
                 }
             }
         }
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc=" InnerLayout (Class) ">
     private class InnerLayout implements LayoutManager {
         
         private FlowLayout flowLayout;
         
         public void addLayoutComponent(String name, Component comp) {;}
         public void removeLayoutComponent(Component comp) {;}
         
         public Dimension getLayoutSize(Container parent) {
             if ( ValueUtil.isEqual(orientation, UIConstants.FLOW) ) {
                 initFlowLayout();
                 return flowLayout.preferredLayoutSize(parent);
             }
             
             synchronized (parent.getTreeLock()) {
                 int w=0, h=0;
                 
                 Component[] comps = parent.getComponents();
                 for (int i=0; i<comps.length; i++) {
                     if (!comps[i].isVisible()) continue;
                     
                     Dimension dim = comps[i].getPreferredSize();
                     if ( UIConstants.VERTICAL.equals(orientation) ) {
                         w = Math.max(w, dim.width);
                         h += (dim.height + getSpacing());
                     } else {
                         w += (dim.width + getSpacing());
                         h = Math.max(h, dim.height);
                     }
                 }
                 return new Dimension(w,h);
             }
         }
         
         public Dimension preferredLayoutSize(Container parent) {
             return getLayoutSize(parent);
         }
         
         public Dimension minimumLayoutSize(Container parent) {
             return getLayoutSize(parent);
         }
         
         public void layoutContainer(Container parent) {
             if ( ValueUtil.isEqual(orientation, UIConstants.FLOW) ) {
                 initFlowLayout();
                 flowLayout.layoutContainer(parent);
             } else {
                 doCustomLayout(parent);
             }
         }
         
         private void initFlowLayout() {
             if ( flowLayout == null ) flowLayout = new FlowLayout();
             
             flowLayout.setHgap(getSpacing());
             flowLayout.setVgap(getSpacing());
             
             String halign = getOrientationHAlignment();
             if ( ValueUtil.isEqual(halign, UIConstants.CENTER) )
                 flowLayout.setAlignment(FlowLayout.CENTER);
             else if ( ValueUtil.isEqual(halign, UIConstants.RIGHT) )
                 flowLayout.setAlignment(FlowLayout.RIGHT);
             else
                 flowLayout.setAlignment(FlowLayout.LEFT);
         }
         
         private void doCustomLayout(Container parent) {
             synchronized (parent.getTreeLock()) {
                 Insets margin = parent.getInsets();
                 String halign = getOrientationHAlignment();
                 String valign = getOrientationVAlignment();
                 
                 int x = margin.left;
                 int y = margin.top;
                 int w = parent.getWidth() - (margin.left + margin.right);
                 int h = parent.getHeight() - (margin.top + margin.bottom);
                 
                 if ( UIConstants.HORIZONTAL.equals(orientation) && UIConstants.RIGHT.equals(halign) )
                     x = w - margin.right;
                 else if ( UIConstants.VERTICAL.equals(orientation) && UIConstants.BOTTOM.equals(valign) )
                     y = h - margin.bottom;
                 
                 Component[] comps = parent.getComponents();
                 for (int i=0; i<comps.length; i++) {
                     Component comp = null;
                     if ( (UIConstants.HORIZONTAL.equals(orientation) && UIConstants.RIGHT.equals(halign)) || (UIConstants.VERTICAL.equals(orientation) && UIConstants.BOTTOM.equals(valign)) )
                         comp = comps[comps.length - i - 1];
                     else
                         comp = comps[i];
                     
                     if (!comp.isVisible()) continue;
                     
                     Dimension dim = comp.getPreferredSize();
                     if ( UIConstants.VERTICAL.equals(orientation) ) {
                         if ( UIConstants.BOTTOM.equals(valign) )
                             y -= dim.height + ((i > 0)? getSpacing():0);
                         
                         comp.setBounds(x, y, w, dim.height);
                         
                         if ( !UIConstants.BOTTOM.equals(valign) )
                             y += dim.height + getSpacing();
                         
                     } else {
                         if ( UIConstants.RIGHT.equals(halign) )
                             x -= dim.width + ((i > 0)? getSpacing():0);
                         
                         comp.setBounds(x, y, dim.width, h);
                         
                         if ( !UIConstants.RIGHT.equals(halign) )
                             x += dim.width + getSpacing();
                     }
                 }
             }
         }
     }
     //</editor-fold>
     
 }
