 package plugins.adufour.vars.gui.swing;
 
 import icy.gui.component.sequence.SequencePreviewPanel;
 import icy.main.Icy;
 import icy.sequence.Sequence;
 
 import java.awt.Component;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 import plugins.adufour.vars.lang.Var;
 import plugins.adufour.vars.lang.VarMutable;
 
 public class SequenceViewer extends SwingVarEditor<Sequence>
 {
     public SequenceViewer(Var<Sequence> variable)
     {
         super(variable);
     }
     
    /**
     * Constructs a new SequenceViewer using a mutable variable of type sequence.
     * 
     * @param variable
     *            the mutable variable containing the {@link Sequence} to display
     * @throws ClassCastException
     *             if the given variable is not of type {@link Sequence}
     * @see Var#getType()
     */
    @SuppressWarnings("unchecked")
     public SequenceViewer(final VarMutable variable)
     {
        super(variable);
        if (!variable.getType().isAssignableFrom(Sequence.class)) throw new ClassCastException("Variable " + variable.getName() + " is not a Sequence");
     }
     
     @Override
     protected JComponent createEditorComponent()
     {
         SequencePreviewPanel editor = new SequencePreviewPanel(true);
         
         // make every component opaque
         for (JPanel panel : new JPanel[] { editor.getMainPanel(), editor.getTPanel(), editor.getZPanel() })
         {
             editor.setOpaque(false);
             panel.setOpaque(false);
             for (Component c : panel.getComponents())
                 if (c instanceof JComponent) ((JComponent) c).setOpaque(false);
         }
         
         return editor;
     }
     
     private final MouseAdapter mouseAdapter = new MouseAdapter()
                                             {
                                                 @Override
                                                 public void mouseClicked(MouseEvent e)
                                                 {
                                                     if (e.getClickCount() == 2)
                                                     {
                                                         Sequence s = getVariable().getValue();
                                                         if (s != null) Icy.getMainInterface().addSequence(s);
                                                     }
                                                 }
                                             };
     
     @Override
     protected void activateListeners()
     {
         getEditorComponent().addMouseListener(mouseAdapter);
     }
     
     @Override
     protected void deactivateListeners()
     {
         getEditorComponent().removeMouseListener(mouseAdapter);
     }
     
     @Override
     public SequencePreviewPanel getEditorComponent()
     {
         return (SequencePreviewPanel) super.getEditorComponent();
     }
     
     @Override
     protected void updateInterfaceValue()
     {
        Sequence s = variable.getValue();
        getEditorComponent().setModel(s);
     }
     
     @Override
     public boolean isComponentResizeable()
     {
         return true;
     }
     
     @Override
     public double getComponentVerticalResizeFactor()
     {
         return 1.0;
     }
 }
