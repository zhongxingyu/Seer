 /**
  * A RoomPanel has a HeadingPanel and a RoomSubpanel. It corresponds to a single room in the data structure. 
  */
 
 package gui.main;
 
 import gui.TimeSlot;
 import gui.main.listeners.NewPractitionerListener;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 
 import backend.DataTransferObjects.PractitionerDto;
 import backend.DataTransferObjects.SchedulePractitionerDto;
 
 //import gui.Constants;
 
 @SuppressWarnings("serial")
 public class RoomPanel extends JPanel {
 	HeadingPanel headingPanel;
 	RoomSubpanel roomSubpanel;
 	SchedulePractitionerDto room;
 	NewPractitionerListener npl = new NewPractitionerListener(this, this.getParent());
 	DayPanel dp;
         MainWindow main;
 	
 	/** Constructs a room panel given a practitioner to be scheduled on a particular day. */
 	public RoomPanel(SchedulePractitionerDto room, DayPanel dp, MainWindow main) {
 		this.dp = dp;
 		this.room = room;
 		this.main = main;
 		roomSubpanel = new RoomSubpanel(room, dp, main);
 		headingPanel = new HeadingPanel(room, this, roomSubpanel);
 		setLayout(new BorderLayout());
 		add(headingPanel, BorderLayout.NORTH);
 		add(roomSubpanel, BorderLayout.CENTER);
 		addMouseListener(npl);
 		//setPreferredSize(new Dimension(Constants.APPT_SUBPANEL_WIDTH,Constants.HEADING_PANEL_HEIGHT));
 	}
 	
 	/**
 	 * Sets the practitioner. When the practitioner is null, it adds a mouse-listener to look for a double click
 	 * event, which opens up the Select Practitioner UI 
 	 * @param p the practitioner to set
 	 */
 	public void setPractitioner(PractitionerDto p) {
 		headingPanel.setText();
 		roomSubpanel.setAppointments();
 		if (p == null) addMouseListener(npl);
 		else removeMouseListener(npl);
 	}
 
 	/** Returns the scheduled practitioner associated with the room panel (i.e., a set of appointments). */
 	public SchedulePractitionerDto getRoom() {
 		return room;
 	}
 
 	/** Returns the day panel associated with the scheduled practitioners for the day. */
 	public DayPanel getDayPanel() {
 		return dp;
 	}
 	
 	public void resetPractitionerHours(SchedulePractitionerDto room) {
 		this.room = room;
 		remove(headingPanel);
 		remove(roomSubpanel);
 		roomSubpanel = new RoomSubpanel(room, dp, main);
 		headingPanel = new HeadingPanel(room, this, roomSubpanel);
 		setLayout(new BorderLayout());
 		add(headingPanel, BorderLayout.NORTH);
 		add(roomSubpanel, BorderLayout.CENTER);
 		addMouseListener(npl);
 		repaint();
 		revalidate();
 	}
 }
