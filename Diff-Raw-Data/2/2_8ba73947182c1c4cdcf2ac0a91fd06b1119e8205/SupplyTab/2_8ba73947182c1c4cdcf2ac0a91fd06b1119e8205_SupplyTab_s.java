 
 package overwatch.gui.tabs;
 
 import javax.swing.*;
 
 import overwatch.controllers.SupplyLogic;
 import overwatch.core.Gui;
 import overwatch.gui.CheckedFieldValidator;
 import overwatch.gui.GenericPanelButtoned;
 import overwatch.gui.LabelFieldPair;
 
 
 
 
 
 /**
  * Implements the supply management tab.
  * 
  * @author  John Murphy
  * @author  Lee Coakley
  * @version 5
  */
 
 
 
 
 
 public class SupplyTab extends GenericPanelButtoned<Integer>
 {
 	public final LabelFieldPair number;
 	public final LabelFieldPair name;
 	public final LabelFieldPair amount;
 	
 	
 	
 	
 	
 	public SupplyTab()
 	{
 		super( "Supplies" );	
 		
 		number	= addLabelledField( "Number:" );
 		name	= addLabelledField( "Name:"   );
 		amount  = addLabelledField( "Amount:" );	
 		
 		number.field.setEditable(false);
 	}
 	
 	
 	
 	
 	
 	public void addTypeValidator  (CheckedFieldValidator v)	{ name  .field.addValidator(v); }
 	public void addAmountValidator(CheckedFieldValidator v)	{ amount.field.addValidator(v); }
 	public void addNumberValidator(CheckedFieldValidator v)	{ number.field.addValidator(v); }
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Test
 	/////////////////////////////////////////////////////////////////////////
 	
 	public static void main(String[] args)
 	{
 		Gui.setNativeStyle();
 		SupplyTab st = new SupplyTab();
 		
 		JFrame frame = new JFrame();
 		frame.add(st);
 		frame.pack();
 		frame.setVisible(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
		new SupplyLogic(st);
 	}
 
 }
