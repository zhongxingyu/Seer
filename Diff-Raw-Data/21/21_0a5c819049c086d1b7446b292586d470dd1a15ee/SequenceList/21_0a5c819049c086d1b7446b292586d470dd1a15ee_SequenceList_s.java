 package plugins.adufour.vars.gui.swing;
 
 import icy.gui.main.MainEvent;
 import icy.gui.main.MainListener;
 import icy.main.Icy;
 import icy.sequence.Sequence;
 import icy.system.thread.ThreadUtil;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import plugins.adufour.vars.lang.Var;
 
 /**
  * Graphical component representing the list of opened sequence, allowing multiple selected items
  * 
  * @author Alexandre Dufour
  * 
  */
 public class SequenceList extends SwingVarEditor<Sequence[]>
 {
     private ListSelectionListener listener;
     
     private MainListener          mainListener;
     
     public SequenceList(Var<Sequence[]> variable)
     {
         super(variable);
     }
     
     private final class SequenceListModel extends DefaultListModel
     {
         private static final long serialVersionUID = 1L;
         
         @Override
         public int getSize()
         {
             return Icy.getMainInterface().getSequences().size();
         }
         
         @Override
         public Object getElementAt(int index)
         {
             return Icy.getMainInterface().getSequences().get(index);
         }
     }
     
     @Override
     protected JComponent createEditorComponent()
     {
         final JList list = new JList();
         
         list.setModel(new SequenceListModel());
         
         listener = new ListSelectionListener()
         {
             @Override
             public void valueChanged(ListSelectionEvent e)
             {
                 Object[] selection = list.getSelectedValues();
                 
                 Sequence[] sequences = new Sequence[selection.length];
                 
                 for (int i = 0; i < selection.length; i++)
                 {
                     sequences[i] = (Sequence) selection[i];
                 }
                 
                 variable.setValue(sequences);
             }
         };
         
         mainListener = new MainListener()
         {
             @Override
             public void painterAdded(MainEvent event)
             {
             }
             
             @Override
             public void painterRemoved(MainEvent event)
             {
             }
             
             @Override
             public void roiAdded(MainEvent event)
             {
             }
             
             @Override
             public void roiRemoved(MainEvent event)
             {
             }
             
             @Override
             public void sequenceClosed(MainEvent event)
             {
                 list.updateUI();
                 list.repaint();
             }
             
             @Override
             public void sequenceFocused(MainEvent event)
             {
             }
             
             @Override
             public void sequenceOpened(MainEvent event)
             {
                 list.updateUI();
                 list.repaint();
             }
             
             @Override
             public void viewerClosed(MainEvent event)
             {
             }
             
             @Override
             public void viewerFocused(MainEvent event)
             {
             }
             
             @Override
             public void viewerOpened(MainEvent event)
             {
             }
             
             @Override
             public void pluginClosed(MainEvent arg0)
             {
             }
             
             @Override
             public void pluginOpened(MainEvent arg0)
             {
             }
         };
         
         return list;
     }
     
     @Override
     public JList getEditorComponent()
     {
         return (JList) super.getEditorComponent();
     }
     
     @Override
     public boolean isComponentOpaque()
     {
         return true;
     }
     
     @Override
     protected void activateListeners()
     {
         Icy.getMainInterface().addListener(mainListener);
         getEditorComponent().addListSelectionListener(listener);
     }
     
     @Override
     protected void deactivateListeners()
     {
         Icy.getMainInterface().removeListener(mainListener);
         getEditorComponent().removeListSelectionListener(listener);
     }
     
     @Override
     protected void updateInterfaceValue()
     {
         ThreadUtil.invokeLater(new Runnable()
         {
             @Override
             public void run()
             {
                 Sequence[] seqs = variable.getValue();
                 int[] indices = new int[seqs.length];
                 
                 for (int i = 0; i < seqs.length; i++)
                     indices[i] = Icy.getMainInterface().getSequences().indexOf(seqs[i]);
                 
                ((JList) getEditorComponent()).setSelectedIndices(indices);
             }
         });
     }
     
 }
