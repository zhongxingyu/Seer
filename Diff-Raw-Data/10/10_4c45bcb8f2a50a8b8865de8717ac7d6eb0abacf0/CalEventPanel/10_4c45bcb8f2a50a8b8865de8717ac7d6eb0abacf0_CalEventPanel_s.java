 package client.gui.view.calendarWindowHelper;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import client.gui.GBC;
 import client.gui.GBC.Align;
 import client.gui.view.CalendarWindow;
 import client.model.EventConfigModel.ViewType;
 
 import chronos.DateManagement;
 import chronos.Singleton;
 
 import events.CalEvent;
 
 public class CalEventPanel extends JPanel {
 
 	private CalEvent calEvent;
 	private CalendarWindow view;
 	private Color color;
 
 	public CalEventPanel(CalEvent event, CalendarWindow view, Color personColor) {
 		super();
 		setLayout(new GridBagLayout());
 		color = personColor;
 		setBackground(color);
 		setPreferredSize(new Dimension(130, 50));
 		setMinimumSize(new Dimension(130, 50));
 		setMaximumSize(new Dimension(130, 50));
 		addMouseListener(new CalPanelMouseAdapter());
 		setBorder(BorderFactory.createLineBorder(Color.white, 2));
 
 		JLabel title = new JLabel(event.getTitle());
 		title.setMaximumSize(new Dimension(130, 20));
 		add(title, new GBC(0, 0, Align.FULL_WIDTH).setWeight(1, 0));
 
 		JLabel startDate = new JLabel(DateManagement.getFormattedDate(event.getStart()) + " - 13:37");
 		add(startDate, new GBC(0, 1, Align.FULL_WIDTH_BOTTOM).setWeight(1, 0));
 
 		this.calEvent = event;
 		this.view = view;
 	}
 
 	private class CalPanelMouseAdapter extends MouseAdapter {
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			super.mouseClicked(e);
 			view.getFrame().getEventModel().setCalEvent(calEvent);
 			if (calEvent.getCreator().getUsername().toLowerCase().equals(Singleton.getInstance().getSelf().getUsername().toLowerCase()))
 				view.getFrame().getEventModel().setView(ViewType.UPDATE);
 			else
 				view.getFrame().getEventModel().setView(ViewType.OTHER);
 
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			super.mouseEntered(e);
			setBackground(Color.pink);
 
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			super.mouseExited(e);
 			setBackground(color);
 		}
 	}
 }
