 package plugins.adufour.ezplug;
 
import icy.main.Icy;
 import icy.sequence.Sequence;
 
 import javax.swing.JComboBox;
 
 import plugins.adufour.vars.gui.SequenceChooser;
import plugins.adufour.vars.gui.VarEditor;
 import plugins.adufour.vars.lang.VarSequence;
 
 /**
  * Class defining a sequence type variable which displays as a combo box where the user may choose among all open
  * sequences
  * 
  * @author Alexandre Dufour
  */
 public class EzVarSequence extends EzVar<Sequence>
 {
     /**
      * Constructs a new variable that handles image data. <br>
      * 
      * @param varName
      *            the parameter name
      */
     public EzVarSequence(String varName)
     {
         super(new VarSequence(varName, null), null);
     }
     
     /**
      * Legacy method
      */
     public Sequence getValue()
     {
     	return super.getValue();
     }
     
     /**
      * Force the sequence chooser to the "no sequence" selection
      */
     public void setNoSequenceSelection()
     {
     	((JComboBox)((SequenceChooser)getVarEditor()).editorComponent).setSelectedIndex(0);
     }
    
    @Override
    protected VarEditor<Sequence> getVarEditor()
    {
    	VarEditor<Sequence> editor = super.getVarEditor();
    	
    	// Initialize with the currently active sequence (if any)
    	variable.setValue(Icy.getMainInterface().getFocusedSequence());
    	
    	return editor;
    }
 }
