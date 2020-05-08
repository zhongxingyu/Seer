 package controllers;
 
 import models.Event;
 import models.EventSet;
 import models.Organizer;
 import views.gui.ExportManager;
 import views.gui.components.JEventBox;
 
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ExportManagerController extends Controller {
 
 	ExportManager exportManager;
 	
 	public ExportManagerController(ExportManager exportManager) {
 		super();
 		this.exportManager = exportManager;
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		String dest = exportManager.destination.getText();
 		if(dest.equals("Pick a destination")){
			// TODO: ADD error dialog
 			return;
 		}
 		EventSet eS = new EventSet();
 		for(JEventBox box : exportManager.checkboxes)
 			if(box.isSelected())
 				eS.add(box.getEvent());
 		if(!eS.isEmpty()){
 			eS.exportEventSet(dest, Organizer.getInstance().getCurrentUser().getUsername()+"_expo");
 		}
 		else{
			// TODO: ADD error dialog
 			return;
 		}
 		exportManager.dispose();
 	}
 
 	public static List<JEventBox> getBoxes() {
 		List<JEventBox> list = new ArrayList<>();
 		EventSet events = Organizer.getInstance().getCurrentUser().getUserProfile().getEvents().all();
 		for(Event e : events)
 			list.add(new JEventBox(e.getTitle(), e));
 		return list;
 	}
 
 }
