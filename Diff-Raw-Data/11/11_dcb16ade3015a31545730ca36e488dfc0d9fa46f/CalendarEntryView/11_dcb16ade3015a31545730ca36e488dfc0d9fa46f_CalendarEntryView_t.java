 package no.ntnu.fp.gui;
 
 import java.awt.Color;
import java.awt.Font;
 import java.awt.Insets;
 
 import javax.swing.JTextArea;
 
 import no.ntnu.fp.model.CalendarEntry;
import no.ntnu.fp.util.TimeLord;
 
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
		setFont(new Font("Arial", 0, 9));
		setText(model.getDescription() +"\nStart: \n" + TimeLord.formatDate(model.getStartDate()) + 
				"\nEnd: \n" + TimeLord.formatDate(model.getEndDate()));
 		setLineWrap(true);
 		setMargin(new Insets(PADDING, PADDING, PADDING, PADDING));
 		
 		//TODO: BG-COLORS SHOULD REPRESENT CALENDAR 
 		setBackground(Color.getHSBColor(129, 185, 106));
 	}
	
 }
