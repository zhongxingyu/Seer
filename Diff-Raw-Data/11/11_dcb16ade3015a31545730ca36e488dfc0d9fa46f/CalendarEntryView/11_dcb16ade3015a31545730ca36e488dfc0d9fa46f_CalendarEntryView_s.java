 package no.ntnu.fp.gui;
 
 import java.awt.Color;
 import java.awt.Insets;
 
 import javax.swing.JTextArea;
 
 import no.ntnu.fp.model.CalendarEntry;
 
 /**
  * View that represents an entry on the week sheet
  * @author andrephilipp
  *
  */
 
 public class CalendarEntryView extends JTextArea{
 	final int PADDING = 6; 	
 	
 	private CalendarEntry model;
 
 	public CalendarEntry getModel() {
 		return model;
 	}
 
 	public void setModel(CalendarEntry model) {
 		this.model = model;
 	}
 
 	public CalendarEntryView(CalendarEntry model) {
 		this.model = model;
		setText(model.getDescription());
 		setLineWrap(true);
 		setMargin(new Insets(PADDING, PADDING, PADDING, PADDING));
 		
 		//TODO: BG-COLORS SHOULD REPRESENT CALENDAR 
 		setBackground(Color.getHSBColor(129, 185, 106));
 	}

 }
