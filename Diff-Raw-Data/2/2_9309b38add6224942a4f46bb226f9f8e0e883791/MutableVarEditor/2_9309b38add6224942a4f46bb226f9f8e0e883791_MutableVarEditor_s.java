 package plugins.adufour.vars.gui.swing;
 
 import java.awt.Dimension;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 import plugins.adufour.vars.gui.VarEditor;
 import plugins.adufour.vars.lang.Var;
 import plugins.adufour.vars.lang.VarMutable;
 
 /**
  * Special editor that changes type according to the underlying variable
  * 
  * @author Alexandre Dufour
  * 
  */
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public class MutableVarEditor extends SwingVarEditor<Object>
 {
     public MutableVarEditor(VarMutable variable)
     {
         super(variable);
     }
     
     /**
      * Abstract pointer to the active editor
      */
     private VarEditor varEditor;
     
     @Override
     protected JComponent createEditorComponent()
     {
         // bypass accesses via getEditorComponent()
         return null;
     }
     
     @Override
     public void dispose()
     {
         varEditor = null;
         
         super.dispose();
     }
     
     public JComponent getEditorComponent()
     {
         // deactivate the current editor (if any)
         if (varEditor != null) varEditor.setEnabled(false);
         
         Var ref = variable.getReference();
         
         if (ref != null)
         {
             varEditor = ref.createVarViewer();
         }
         else
         {
             varEditor = variable.createVarViewer();
             if (varEditor instanceof Label) ((Label) varEditor).getEditorComponent().setHorizontalAlignment(JLabel.CENTER);
         }
         
         // activate the new listener
         varEditor.setEnabled(true);
         
         return (JComponent) varEditor.getEditorComponent();
     };
     
     @Override
     public Dimension getPreferredSize()
     {
         return varEditor.getPreferredSize();
     }
     
     @Override
     public boolean isComponentOpaque()
     {
         return false;
     }
     
     @Override
     public boolean isComponentEnabled()
     {
         return true;
     }
     
     @Override
     public void setComponentToolTipText(String s)
     {
         varEditor.setComponentToolTipText(s);
     }
     
     @Override
     protected void activateListeners()
     {
         
     }
     
     @Override
     protected void deactivateListeners()
     {
         
     }
     
     public boolean isComponentResizeable()
     {
         return true;
     }
     
     public double getComponentVerticalResizeFactor()
     {
        return 0.5;
     }
     
     @Override
     public void setEnabled(boolean enabled)
     {
         
     }
     
     @Override
     protected void setEditorEnabled(boolean enabled)
     {
         
     }
     
     @Override
     protected void updateInterfaceValue()
     {
         // this is done internally by each custom editor
     }
 }
