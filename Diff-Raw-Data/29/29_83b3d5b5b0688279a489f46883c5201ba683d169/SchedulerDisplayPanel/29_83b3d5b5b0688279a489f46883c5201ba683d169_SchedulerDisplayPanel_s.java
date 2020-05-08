 package edu.rpi.rocs.client.ui.scheduler;
 
 import java.util.ArrayList;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.rpi.rocs.client.objectmodel.Schedule;
 import edu.rpi.rocs.client.objectmodel.Section;
 
 public class SchedulerDisplayPanel extends VerticalPanel {
 	ArrayList<Schedule> m_schedules=null;
 	static SchedulerDisplayPanel theInstance=null;
 	Schedule m_current;
 	ScheduleViewWidget m_view;
 	FlowPanel m_summary;
 	RandomColorGenerator m_genMap = new RandomColorGenerator();
 	HorizontalPanel crn_pane = new HorizontalPanel();
 	HorizontalPanel content = new HorizontalPanel();
 	VerticalPanel rightbar = new VerticalPanel();
 	SectionInfoPanel infoPanel = new SectionInfoPanel();
 	Label crnsLbl = new Label("Schedule CRNs:");
 
 	private void setActiveSchedule(Schedule s) {
 		m_current = s;
 		updateSchedulePanel();
 	}
 
 	class ScheduleClickHandler implements ClickHandler {
 		Schedule m_schedule;
 
 		ScheduleClickHandler(Schedule s) {
 			m_schedule = s;
 		}
 
 		public void onClick(ClickEvent arg0) {
 			for(Section s : m_schedule.getSections()) {
 				Log.debug("Section " + s.getNumber() + " of course " + s.getParent().getDept() + "-" + s.getParent().getNum());
 			}
 			setActiveSchedule(m_schedule);
 		}
 	}
 
 	private SchedulerDisplayPanel() {
 		m_current = null;
 		m_view = null;
 		m_summary = new FlowPanel();
 		m_summary.addStyleName("rocs-style");
 		m_summary.addStyleName("schedule-list");
 		
 		rightbar.add(m_summary);
		rightbar.add(infoPanel);
 		
 		content.add(rightbar);
 		
 		crn_pane.add(crnsLbl);
 		crn_pane.addStyleName("crn-pane");
 		
 		add(content);
 		add(crn_pane);
 	}
 
 	public static SchedulerDisplayPanel getInstance() {
 		if(theInstance==null) theInstance = new SchedulerDisplayPanel();
 		return theInstance;
 	}
 	
 	public SectionInfoPanel getInfoPanel() {
 		return infoPanel;
 	}
 
 	public void setSchedules(ArrayList<Schedule> schedules) {
 		m_schedules = schedules;
 		if(m_schedules.size()>0) {
 			m_current = m_schedules.get(0);
 			m_summary.clear();
 			for(Schedule s : schedules) {
 				ScheduleMiniViewWidget w = new ScheduleMiniViewWidget(s, m_genMap);
 				m_summary.add(w);
 				w.addClickHandler(new ScheduleClickHandler(s));
 			}
 		}
 		else {
 			m_current = null;
 		}
 		updateSchedulePanel();
 	}
 
 	private void updateSchedulePanel() {
 		if(m_view!=null) {
 			content.remove(m_view);
 			crn_pane.clear();
 			crn_pane.add(crnsLbl);
 		}
 		if(m_current==null) {
 			Window.alert("Unable to compute at least one valid schedule.");
 			infoPanel.clear();
 			return;
 		}
 		m_view = new ScheduleViewWidget(m_current, m_genMap);
 		content.insert(m_view, 0);
 		if(m_current!=null) {
 			for(Section s : m_current.getSections()) {
 				Label x = new Label(Integer.toString(s.getCrn()));
 				crn_pane.add(x);
 			}
 		}
 	}
 }
