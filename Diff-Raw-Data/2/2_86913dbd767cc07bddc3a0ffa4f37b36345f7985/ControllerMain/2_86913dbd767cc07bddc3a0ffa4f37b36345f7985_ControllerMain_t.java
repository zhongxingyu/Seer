 package footballmanager;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JComboBox;
 
 
 class ControllerMain implements ActionListener, Controller{
 
 	//-------------------------------------VARIABLES----------------------------------------//
 	private Model model;
 	
 	private ViewMain view;
 	
 	
 	//-------------------------------------CONSTRUCTOR--------------------------------------//
 	
 	protected ControllerMain(Model model, ViewMain view){
 		this.model = model;
 		this.view = view;
 	}
 
 	
 	//-------------------------------------METHODS-------------------------------------------//
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource().equals(view.getMp_nextDayButton())){
 			this.model.newDay();
 		} else if(e.getSource().equals(view.getMp_eventComboBox())){
			if(((JComboBox)(e.getSource())).getSelectedItem().equals("Training")) this.model.addEvent(((Integer)this.view.getMp_scheduledDateSpinner().getValue()).intValue(), new EventTraining());
 		}
 	}
 	
 	
 }
