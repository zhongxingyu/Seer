 package com.raccoonfink.cruisemonkey.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import net.fortuna.ical4j.data.CalendarBuilder;
 import net.fortuna.ical4j.data.ParserException;
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.DateTime;
 import net.fortuna.ical4j.model.Period;
 import net.fortuna.ical4j.model.PeriodList;
 import net.fortuna.ical4j.model.TimeZone;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.component.VTimeZone;
 import net.fortuna.ical4j.model.property.RecurrenceId;
 import net.fortuna.ical4j.model.property.Uid;
 
 import org.apache.commons.io.IOUtils;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.Assert;
 
 import com.raccoonfink.cruisemonkey.dao.CalendarVisitor;
 
 public class CalendarManager implements InitializingBean {
 	private static final int MINUTES = 60;
 
 	private boolean m_startOnInit = false;
 	private URL m_url;
 	private CalendarVisitor m_visitor;
 
 	private ScheduledExecutorService m_executor = Executors.newSingleThreadScheduledExecutor();
 
 	public CalendarManager() {
 	}
 
 	public synchronized URL getUrl() {
 		return m_url;
 	}
 
 	public synchronized void setUrl(final URL url) {
 		m_url = url;
 	}
 	
 	public synchronized CalendarVisitor getVisitor() {
 		return m_visitor;
 	}
 	
 	public synchronized void setVisitor(final CalendarVisitor visitor) {
 		m_visitor = visitor;
 	}
 
 	public synchronized boolean getStartOnInit() {
 		return m_startOnInit;
 	}
 
 	public synchronized void setStartOnInit(final boolean startOnInit) {
 		m_startOnInit = startOnInit;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		Assert.notNull(m_url);
 		Assert.notNull(m_visitor);
 
 		if (m_startOnInit) {
 			System.err.println("Scheduling calendar update every " + MINUTES + " minutes.");
 			getRunnable().run();
 			m_executor.schedule(getRunnable(), MINUTES, TimeUnit.MINUTES);
 		}
 	}
 
 	private Runnable getRunnable() {
 		return new Runnable() {
 			@Override
 			public void run() {
 		        final URL url = getUrl();
 				System.err.println("Updating calendar information from " + url);
 				try {
 					final Calendar calendar = getCalendar(url);
 					CalendarManager.visitCalendar(calendar, getVisitor());
 				} catch (final IOException e) {
 					e.printStackTrace();
 				} catch (final ParserException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void visitCalendar(final Calendar calendar, final CalendarVisitor visitor) {
 		visitor.visitCalendarStart();
 
 		final Set<String> rendered = new HashSet<String>();
 
 		TimeZone timeZone = null;
 
 		final Component timeZoneComponent = calendar.getComponent("VTIMEZONE");
 		if (timeZoneComponent != null && timeZoneComponent.getProperties() != null && timeZoneComponent.getProperties().size() > 0) {
 			timeZone = new TimeZone(new VTimeZone(timeZoneComponent.getProperties()));
 		}
 
 		DateTime from;
 		DateTime to;
 
 		try {
 			from = new DateTime("20130209T000000Z");
 			to   = new DateTime("20130218T000000Z");
 			final Period period = new Period(from, to);
 
 			int count = 0;
 			for (final Component c : (List<Component>)calendar.getComponents("VEVENT")) {
 	        	final VEvent vevent = new VEvent(c.getProperties());
 
 				final PeriodList list = c.calculateRecurrenceSet(period);
 				for (final Period o : (Set<Period>)list) {
 
 					final DateTime startDate = o.getStart();
 					final DateTime endDate = o.getEnd();
 
 					if (timeZone != null) {
 						startDate.setTimeZone(timeZone);
 						endDate.setTimeZone(timeZone);
 					}
 
 					final Uid uid = vevent.getUid();
 					final RecurrenceId rid = vevent.getRecurrenceId();
 
 					String id = uid.getValue() + ":" + startDate;
 					if (list.size() == 1 && rid != null && rid.getValue() != null) {
 						id = uid.getValue() + ":" + rid.getValue();
 					}
 
 					String renderedId = id.replaceAll("T\\d+$", "");
 					if (rendered.contains(renderedId)) {
 						System.err.println("Already handled " + uid + " for the day, skipping.");
 					} else {
 						handleVEvent(id, vevent, visitor, timeZone, new Date(startDate.getTime()), new Date(endDate.getTime()));
 						rendered.add(renderedId);
 						count++;
 					}
 				}
 
 	    		/*
 				*/
 			}
 			
 			System.err.println("count = " + count);
 		} catch (final ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	    visitor.visitCalendarEnd();
 	}
 
 	private static void handleVEvent(final String id, final VEvent vevent, final CalendarVisitor visitor, TimeZone timeZone, final Date startDate, final Date endDate) {
 		visitor.visitEventStart();
 
 		visitor.visitEventId(id);
 
 		visitor.visitEventDateTimeStart(startDate);
 		visitor.visitEventDateTimeEnd(endDate);
 
 		if (vevent.getDescription() != null && vevent.getDescription().getValue() != null) {
 			visitor.visitEventDescription(vevent.getDescription().getValue());
 		}
 		if (vevent.getLocation() != null && vevent.getLocation().getValue() != null) {
 			visitor.visitEventLocation(vevent.getLocation().getValue());
 		}
 		if (vevent.getSummary() != null && vevent.getSummary().getValue() != null) {
 			visitor.visitEventSummary(vevent.getSummary().getValue());
 		}
 
 		visitor.visitEventEnd();
 	}
 
 	private Calendar getCalendar(final URL url) throws IOException, ParserException {
 		final InputStream is = url.openStream();
 		final StringWriter sw = new StringWriter();
 		IOUtils.copy(is, sw);
 		IOUtils.closeQuietly(sw);
 		IOUtils.closeQuietly(is);
 
		System.err.println("calendar for " + url + ":");
		System.err.println(sw.toString());
 
 		final StringReader sr = new StringReader(sw.toString());
 		final CalendarBuilder builder = new CalendarBuilder();
 	    final Calendar calendar = builder.build(sr);
 	    IOUtils.closeQuietly(sr);
 
 		return calendar;
 	}
 
 	public void updateNow() {
 		getRunnable().run();
 	}
 
 	
 }
