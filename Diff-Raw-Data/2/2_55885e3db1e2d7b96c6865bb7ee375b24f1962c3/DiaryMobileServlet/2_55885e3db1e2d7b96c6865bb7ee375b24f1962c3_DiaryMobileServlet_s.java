 package com.zotyo.diary.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.handler.MessageContext;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.context.support.SpringBeanAutowiringSupport;
 
 import com.zotyo.diary.client.Day;
 import com.zotyo.diary.client.Event;
 import com.zotyo.diary.client.Diary;
 import com.zotyo.diary.client.DiaryImplService;
 import com.zotyo.photos.pojo.Photo;
 import com.zotyo.photos.service.PhotoService;
 
 public class DiaryMobileServlet extends HttpServlet {
 	
 	private static Logger logger = Logger.getLogger(DiaryMobileServlet.class); 
 	
 	private Diary diary;
 	private DatatypeFactory df;
 	private String keyword;
 	private DiaryCache diaryCache;
 	
 	@Autowired
 	private DiaryHelper diaryHelper;
 	
 	@Autowired
 	private PhotoService photoService;
 	
 	public void init() throws ServletException {
 		try {
             SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
 			
 			InputStream inputStream = ClassLoader.getSystemResourceAsStream("diary.properties");
 			Properties props = new Properties();
 			props.load(inputStream);
 			
 			keyword = props.getProperty("keyword");
 			
 			URL wsdlURL = new URL(props.getProperty("wsdlURL"));
 			DiaryImplService diaryService = new DiaryImplService(wsdlURL, new QName("http://ws.diary.zotyo.com/", "DiaryImplService")); 
 			diary = diaryService.getDiaryImplPort();
             df = DatatypeFactory.newInstance();
             diaryCache = DiaryCache.getInstance();
             
 		} catch(IOException ioex) {
 			ioex.printStackTrace();
 		} catch (DatatypeConfigurationException dce) {
             throw new IllegalStateException("Exception while obtaining DatatypeFactory instance", dce);
 		}
 	}
 	
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) 
 											throws ServletException, IOException {
 		
 		String command = request.getParameter("cmd");
 		if ("addday".equals(command)) {
 			request.setAttribute("jspPage", "/mobile/addday.jsp");		}
 		if ("addevent".equals(command)) {
 			request.setAttribute("jspPage", "/mobile/addevent.jsp");
 		}
 		if ("alldays".equals(command)) {
 			List<Day> days = new ArrayList<Day>();
 			String yearString = request.getParameter("year");
 			String monthString = request.getParameter("month");
 			if (yearString != null && yearString.length() > 0 && monthString != null && monthString.length() > 0) {
 				int year = Integer.parseInt(yearString);
 				int month = Integer.parseInt(monthString);
 				days = diary.getDaysForAMonth(year, month);
 				request.setAttribute("year", year);
 				request.setAttribute("month", month);
 				request.setAttribute("alldays", days);
 					
 				RequestDispatcher rd = getServletContext().getRequestDispatcher("/mobile/alldays.jsp");
 	    		rd.forward(request, response);
 				return;
 			}
 			else {
 				GregorianCalendar theDayCal = new GregorianCalendar();
 				theDayCal.setTime(new Date());
 				days = diary.getDaysForAMonth(theDayCal.get(Calendar.YEAR), theDayCal.get(Calendar.MONTH));
 				request.setAttribute("year", theDayCal.get(Calendar.YEAR));
 				request.setAttribute("month", theDayCal.get(Calendar.MONTH));
 				request.setAttribute("filtersNeeded", true);
 			}
 			request.setAttribute("alldays", days);
 			request.setAttribute("jspPage", "/mobile/alldays.jsp");
 		}
 		
 		if ("getDays".equals(command)) {
 			String key = request.getParameter("key");
 			List<Integer> days = diaryCache.getEventDays(key, diary);
 			StringBuilder sb = new StringBuilder();
 			sb.append("[");
 			for (int i = 0; i < days.size(); i++) {
 				sb.append(days.get(i));
 				if (i != days.size() - 1) {
 					sb.append(",");	
 				}
 			}
 			sb.append("]");
 			request.setAttribute("days", sb.toString());
 			request.setAttribute("jspPage", "/mobile/calendardays.jsp");
 		}
 
 		if ("terms".equals(command)) {
 			String term = URLDecoder.decode(request.getParameter("term"), "UTF-8");	
 			List<String> result = diary.searchTerms(term);
 			
 			response.setContentType("application/json; charset=UTF-8");
 			PrintWriter out = response.getWriter();
 			ObjectMapper mapper = new ObjectMapper();
 			mapper.writeValue(out, result);
 			return;
 		}
 
 		if (command == null || command.length() == 0) {
 			List<Event> events = diary.getAllEvents();
 			List<Event> latests = new ArrayList<Event>();
 			for (int i=0; i<5; i++) {
 				latests.add(events.get(i));
 			}
 			request.setAttribute("latests", latests);
 			request.setAttribute("jspPage", "/mobile/latests.jsp");	
 		}
 
 		RequestDispatcher rd = getServletContext().getRequestDispatcher("/mobile/diary.jsp");
 	    rd.forward(request, response);
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) 
 											throws ServletException, IOException {
 		String command = request.getParameter("cmd");
 		if ("search".equals(command)) {
 			String searchTerm = request.getParameter("searchTerm");	
 			List<Event> result = diary.searchEvents(searchTerm);
 			request.setAttribute("result", result);
 			request.setAttribute("searchTerm", searchTerm);
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/mobile/result.jsp");
 			rd.forward(request, response);
 			
 			return;
 		}
 		
 		String key = request.getParameter("keyword");
 		if (!diaryHelper.md5(key).equals(keyword)) {
 			response.sendRedirect("/mobile/naplo");
 			return;
 		}
 		String theDay = request.getParameter("theDay");
 		String descriptionOfTheDay = request.getParameter("descriptionOfTheDay");
 		String duration = request.getParameter("duration");
 		String initialEvent = request.getParameter("initialEvent");
 		String startDate = request.getParameter("startDate");
 		
 		if ("add_day".equals(command)) {
 			if (theDay != null && theDay.length() > 0) {
 				Day day = new Day();
 				GregorianCalendar theDayCal = diaryHelper.getDayCal(theDay);
 				day.setTheDay(df.newXMLGregorianCalendar(theDayCal));
 				day.setDescriptionOfTheDay(descriptionOfTheDay);
 				
 				Event event = new Event();
 				event.setDescription(initialEvent);
 				event.setDuration(diaryHelper.getDuration(duration));
 				GregorianCalendar startDateCal = diaryHelper.getStartDateCal(startDate);
 				event.setStartTime(df.newXMLGregorianCalendar(startDateCal));
 				day.getEventsOfTheDay().add(event);
 				((BindingProvider)diary).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
 					    Collections.singletonMap("keyword",Collections.singletonList(key)));
 				diary.addDay(day);
 				diaryCache.purgeKey(theDayCal.get(Calendar.YEAR) + "-" + theDayCal.get(Calendar.MONTH));
 			}
 		} else if ("add_event".equals(command)) {
 			GregorianCalendar theDayCal = diaryHelper.getDayCal(theDay);
 			Event event = new Event();
 			event.setDescription(initialEvent);
 			event.setDuration(diaryHelper.getDuration(duration));
 			GregorianCalendar startDateCal = diaryHelper.getStartDateCal(startDate);
 			event.setStartTime(df.newXMLGregorianCalendar(startDateCal));
 			((BindingProvider)diary).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
 				    Collections.singletonMap("keyword",Collections.singletonList(key)));
 			diary.addEvent(df.newXMLGregorianCalendar(theDayCal), event);
 		}
 		
		response.sendRedirect("/mobile/naplo");
 	}
 
 }
