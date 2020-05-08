 package plugins.adufour.vars.gui.swing;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 import plugins.adufour.vars.lang.Var;
 
 public class Label<T> extends SwingVarEditor<T>
 {
     public Label(Var<T> variable)
     {
         super(variable);
     }

     @Override
     protected JComponent createEditorComponent()
     {
         return new JLabel("");
     }

     @Override
     protected void activateListeners()
     {
     }

     @Override
     protected void deactivateListeners()
     {
     }

     @Override
     public JLabel getEditorComponent()
     {
         return (JLabel) super.getEditorComponent();
     }
     
     @Override
     protected void updateInterfaceValue()
     {
         getEditorComponent().setText(variable.getValueAsString(true));
     }
 }
