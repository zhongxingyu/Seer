 package me.guillsowns.docgym.webforms;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import me.guillsowns.docgym.domain.ScheduleEntry;
 
 import org.primefaces.event.DateSelectEvent;
 import org.primefaces.event.ScheduleEntryMoveEvent;
 import org.primefaces.event.ScheduleEntryResizeEvent;
 import org.primefaces.event.ScheduleEntrySelectEvent;
 import org.primefaces.model.DefaultScheduleEvent;
 import org.primefaces.model.DefaultScheduleModel;
 import org.primefaces.model.ScheduleEvent;
 import org.primefaces.model.ScheduleModel;
 import org.springframework.context.annotation.Scope;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Component;
 
 @Component
 @Scope("session")
 public class ScheduleForm implements Serializable
 {
 	private static final long	serialVersionUID	= 1L;
 
 	class CustomScheduleEvent extends DefaultScheduleEvent implements Serializable
 	{
 		private static final long	serialVersionUID	= 1L;
 		private ScheduleEntry		dbEntry;
 
 		public CustomScheduleEvent()
 		{
 			super();
 		}
 
 		public CustomScheduleEvent(String title, Date start, Date end, ScheduleEntry entry)
 		{
 			super(title, start, end);
 			this.setDbEntry(entry);
 		}
 
 		public CustomScheduleEvent(String title, Date start, Date end)
 		{
 			super(title, start, end);
 		}
 
 		public ScheduleEntry getDbEntry()
 		{
 			return dbEntry;
 		}
 
 		public void setDbEntry(ScheduleEntry dbEntry)
 		{
 			this.dbEntry = dbEntry;
 		}
 	}
 
 	private ScheduleModel		eventModel;
 	private CustomScheduleEvent	event	= new CustomScheduleEvent();
 
 	public ScheduleForm()
 	{
 		eventModel = new DefaultScheduleModel();
 
 		List<ScheduleEntry> entries = ScheduleEntry.findAllScheduleEntrys();
 
 		boolean admin = isAdmin();
 
 		for (ScheduleEntry entry : entries)
 		{
 			CustomScheduleEvent dbEvent = new CustomScheduleEvent(entry.getTitle(), entry.getTimeStart(),
 							entry.getTimeEnd(), entry);
 			if (admin)
 			{
 				dbEvent.setEditable(true);
 			}
 			else
 			{
 				dbEvent.setEditable(false);
 			}
 			eventModel.addEvent(dbEvent);
 		}
 	}
 

 	public boolean isAdmin()
 	{
 		if (SecurityContextHolder.getContext().getAuthentication() != null)
 		{
 			for (GrantedAuthority g : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
 			{
 				if (g.getAuthority().equals("ROLE_ADMIN"))
 				{
 					return true;
 				}
 			}
 
 		}
 
 		return false;
 	}
 
 	public void addEvent(ActionEvent actionEvent)
 	{
 		if (event.getId() == null)
 		{
 			ScheduleEntry entry = new ScheduleEntry();
 			entry.setTitle(event.getTitle());
 			entry.setTimeStart(event.getStartDate());
 			entry.setTimeEnd(event.getEndDate());
 
 			entry.persist();
 			entry.flush();
 
 			event.setDbEntry(entry);
 			eventModel.addEvent(event);
 
 			// System.out.println("added new event : " + event.getTitle() +
 			// " @ "
 			// + event.getId());
 
 		}
 		else
 		{
 			eventModel.updateEvent(event);
 			updateDbFromEvent(event);
 
 			// System.out.println("updated event : " + event.getTitle() + " @ "
 			// + event.getId());
 		}
 
 		event = new CustomScheduleEvent();
 	}
 
 	public void deleteEvent(ActionEvent actionEvent)
 	{
 		if (event.getId() == null)
 		{
 		}
 		else
 		{
 			if (!eventModel.deleteEvent(event))
 			{
 				// System.out.println("delete event: event not found!");
 			}
 			event.getDbEntry().remove();
 			event.getDbEntry().flush();
 
 			// System.out.println("deleted event : " + event.getTitle() + " @ "
 			// + event.getId());
 			//
 			// System.out.println("Left with " +
 			// Integer.toString(eventModel.getEventCount() ) + " events.");
 		}
 
 		event = new CustomScheduleEvent();
 	}
 
 	private void updateDbFromEvent(CustomScheduleEvent event)
 	{
 		String title = event.getTitle();
 		Date timeStart = event.getStartDate();
 		Date timeEnd = event.getEndDate();
 
 		event.getDbEntry().setTitle(title);
 		event.getDbEntry().setTimeStart(timeStart);
 		event.getDbEntry().setTimeEnd(timeEnd);
 		event.getDbEntry().merge();
 		event.getDbEntry().flush();
 	}
 
 	public void onEventSelect(ScheduleEntrySelectEvent selectEvent)
 	{
 		event = (CustomScheduleEvent) selectEvent.getScheduleEvent();
 	}
 
 	public void onDateSelect(DateSelectEvent selectEvent)
 	{
 		Calendar dateEnd = Calendar.getInstance();
 		dateEnd.setTimeInMillis(selectEvent.getDate().getTime());
 		dateEnd.add(Calendar.HOUR_OF_DAY, 1);
 
 		event = new CustomScheduleEvent("", selectEvent.getDate(), dateEnd.getTime());
 	}
 
 	public void onEventMove(ScheduleEntryMoveEvent event)
 	{
 		updateDbFromEvent((CustomScheduleEvent) event.getScheduleEvent());
 	}
 
 	public void onEventResize(ScheduleEntryResizeEvent event)
 	{
 		updateDbFromEvent((CustomScheduleEvent) event.getScheduleEvent());
 	}
 
 	private void addMessage(FacesMessage message)
 	{
 		FacesContext.getCurrentInstance().addMessage(null, message);
 	}
 
 	public ScheduleModel getEventModel()
 	{
 		boolean admin = isAdmin();
 
 		for (ScheduleEvent e : eventModel.getEvents())
 		{
 			if (admin)
 			{
 				((CustomScheduleEvent) e).setEditable(true);
 			}
 			else
 			{
 				((CustomScheduleEvent) e).setEditable(false);
 			}
 
 		}
 		return eventModel;
 	}
 
 	public void setEventModel(ScheduleModel eventModel)
 	{
 		this.eventModel = eventModel;
 	}
 
 	public ScheduleEvent getEvent()
 	{
 		return event;
 	}
 
 	public void setEvent(ScheduleEvent event)
 	{
 		this.event = (CustomScheduleEvent) event;
 	}
 
 }
