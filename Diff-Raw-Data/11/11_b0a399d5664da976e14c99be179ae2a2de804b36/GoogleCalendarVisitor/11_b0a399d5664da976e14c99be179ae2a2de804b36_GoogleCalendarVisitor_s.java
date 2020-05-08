 package com.raccoonfink.cruisemonkey.server;
 
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.util.Assert;
 
 import com.raccoonfink.cruisemonkey.dao.CalendarVisitor;
 import com.raccoonfink.cruisemonkey.dao.EventDao;
 import com.raccoonfink.cruisemonkey.dao.UserDao;
 import com.raccoonfink.cruisemonkey.model.Event;
 import com.raccoonfink.cruisemonkey.model.User;
 
 public class GoogleCalendarVisitor implements CalendarVisitor, InitializingBean {
     private User m_importUser;
 
     @Autowired
     private UserDao m_userDao;
 
     @Autowired
     private EventDao m_eventDao;
 
     private String m_user;
 
     private Date m_lastUpdated;
 	private Event m_currentEvent;
 
     public GoogleCalendarVisitor() {
     }
 
     public String getUser() {
     	return m_user;
     }
     
     public void setUser(final String user) {
     	m_user = user;
     }
 
     @Override
 	public void afterPropertiesSet() throws Exception {
 		Assert.notNull(m_userDao);
 		Assert.notNull(m_eventDao);
 		Assert.notNull(m_user);
 		m_importUser = m_userDao.get(m_user);
 	}
 
 
 	@Override
 	public void visitCalendarStart() {
 		m_lastUpdated = new Date();
 	}
 
 	@Override
 	public void visitEventStart() {
 		m_currentEvent = new Event();
 		m_currentEvent.setIsPublic(true);
 		m_currentEvent.setCreatedBy(m_importUser.getUsername());
 		m_currentEvent.setCreatedDate(m_lastUpdated);
 	}
 
 	@Override
 	public void visitEventDateTimeStart(final Date date) {
 		m_currentEvent.setStartDate(date);
 	}
 
 	@Override
 	public void visitEventDateTimeEnd(final Date date) {
 		m_currentEvent.setEndDate(date);
 	}
 
 	@Override
 	public void visitEventId(final String value) {
 		m_currentEvent.setId(value);
 	}
 
 	@Override
 	public void visitEventDescription(final String value) {
 		m_currentEvent.setDescription(value.trim());
 	}
 
 	@Override
 	public void visitEventLocation(final String value) {
 		m_currentEvent.setLocation(value.trim());
 	}
 
 	@Override
 	public void visitEventSummary(String value) {
 		m_currentEvent.setSummary(value.trim());
 	}
 
 	@Override
 	public void visitEventEnd() {
 		String summaryString = m_currentEvent.getSummary();
 		final String locationString = m_currentEvent.getLocation();
 
 		if (summaryString != null && locationString != null) {
 			String removeThis = " - " + locationString;
 			if (summaryString.endsWith(removeThis)) {
 				summaryString = summaryString.substring(0, summaryString.length() - removeThis.length());
 			}
 			removeThis = locationString + " - ";
 			if (summaryString.startsWith(removeThis)) {
 				summaryString = summaryString.substring(removeThis.length(), summaryString.length());
 			}
 			m_currentEvent.setSummary(summaryString.trim());
 		}
 
 		final Event existingEvent = m_eventDao.get(m_currentEvent.getId());
 
 		if (existingEvent == null) {
 			m_eventDao.save(m_currentEvent);
 		} else {
 			System.err.println("found existing event: " + existingEvent);
 			existingEvent.setSummary(m_currentEvent.getSummary());
 			existingEvent.setDescription(m_currentEvent.getDescription());
 			existingEvent.setLocation(m_currentEvent.getLocation());
 			existingEvent.setStartDate(m_currentEvent.getStartDate());
 			existingEvent.setEndDate(m_currentEvent.getEndDate());
 			existingEvent.setCreatedDate(m_lastUpdated);
 			m_eventDao.save(m_currentEvent);
 		}
 	}
 
 	@Override
 	public void visitCalendarEnd() {
     	final List<Event> events = m_eventDao.findAllAsList();
 
     	int count = 0;
         for (final Event event : events) {
        	if ("official".equals(event.getCreatedBy())) {
         		if (event.getCreatedDate().getTime() < m_lastUpdated.getTime()) {
             		m_eventDao.delete(event);
         		} else {
         			count++;
         		}
         	}
         }
 
         System.out.println("added/refreshed " + count + " events");
     }
     
 }
