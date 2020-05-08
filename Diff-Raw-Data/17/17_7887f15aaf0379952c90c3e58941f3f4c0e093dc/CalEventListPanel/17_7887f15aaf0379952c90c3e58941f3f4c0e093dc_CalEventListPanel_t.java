 package client.gui.view.CalendarWindowHelper;
 
 import java.awt.Color;
import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Calendar;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 import client.gui.view.CalendarWindow;
 import client.gui.view.EventConfigWindow;
 
 import chronos.DateManagement;
 
 import events.CalEvent;
 
 public class CalEventListPanel extends JPanel {

 	private CalEvent calEvent;
 	private CalendarWindow view;
 
 	public CalEventListPanel(CalEvent event, CalendarWindow view) {
 		super();
 		String text = DateManagement.getFormattedSimple(event.getStart()) + " " + event.getTitle();
 		JLabel label = new JLabel(text);
		// label.setHorizontalAlignment(SwingConstants.WEST);
		label.setAlignmentY(Component.LEFT_ALIGNMENT);

 		this.setBackground(Color.white);
 		this.setPreferredSize(new Dimension(120, 20));
 		this.setMinimumSize(new Dimension(120, 20));
 		this.setMaximumSize(new Dimension(120, 20));
 		this.addMouseListener(new EventListPanelListener());
 		this.setAlignmentX(LEFT_ALIGNMENT);
 		this.add(label);
 		this.calEvent = event;
 		this.view = view;
 
 	}
 
 	private class EventListPanelListener extends MouseAdapter {
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			super.mouseClicked(e);
			((EventConfigWindow) view.getFrame().getEventConfigWindow()).getModel().setCalEvent(calEvent);
			((EventConfigWindow) view.getFrame().getEventConfigWindow()).setVisible(true);
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			super.mouseEntered(e);
 			setBackground(Color.yellow);
 
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			super.mouseExited(e);
 			setBackground(Color.white);
 		}
 
 	}
 }
