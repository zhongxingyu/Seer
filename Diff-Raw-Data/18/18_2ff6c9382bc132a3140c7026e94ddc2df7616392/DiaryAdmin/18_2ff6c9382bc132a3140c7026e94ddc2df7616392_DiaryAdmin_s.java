 package com.zotyo.diary.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.context.support.SpringBeanAutowiringSupport;
 
 import com.zotyo.diary.admin.persistence.DiaryAdminDAO;
 import com.zotyo.diary.pojos.Day;
 import com.zotyo.diary.pojos.Event;
 
 public class DiaryAdmin extends HttpServlet {
 	
 	private static Logger logger = Logger.getLogger(DiaryAdmin.class); 
 	
 	private static final String PARAM_PREFIX_CHANGED = "changed_";
 	private static final String PARAM_PREFIX_WORD = "word_";
 	@Autowired
 	private DiaryAdminDAO diaryAdminDAO;
 	
 	@Autowired
 	private DiaryHelper diaryHelper;
 	
 	private String keyword;
 		
 	public void init() throws ServletException {
 		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
 		try {
 			InputStream inputStream = ClassLoader.getSystemResourceAsStream("diary.properties");
 			Properties props = new Properties();
 			props.load(inputStream);
 			
 			keyword = props.getProperty("keyword");
 		} catch(IOException ioex) {
 			ioex.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) 
 											throws ServletException, IOException {
 		request.getSession().setAttribute("redirect_to", "admin");
 		response.sendRedirect("/admin/admin.jsp");
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) 
 											throws ServletException, IOException {
 		String command = request.getParameter("cmd");
 		
 		if ("login".equals(command)) {
 			String password = request.getParameter("password");
 			if (!diaryHelper.md5(password).equals(keyword)) {
 				RequestDispatcher rd = getServletContext().getRequestDispatcher("/admin/login.jsp");
 				rd.forward(request, response);
 				return;
 			}
 			/*
 			List<Day> days = diaryAdminDAO.getAllDays();
 			request.setAttribute("days", days);
 			*/
 			request.getSession().setAttribute("admin", "true");
 
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/admin/admin.jsp");
 			rd.forward(request, response);
 			return;
 		}
 		
 		if ("show".equals(command)) {
 			String dayString = request.getParameter("day");
 			Date theDay = null;
 			SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
 			if (dayString != null && dayString.length() > 0) {
 				try {
 					theDay = format.parse(dayString);
 				} catch (ParseException e) {
 					logger.error(e);
 				}
 			}
 			if (theDay == null) theDay = new Date();
 			
 			Calendar cal=Calendar.getInstance();
 			cal.setTime(theDay);
 			cal.set(Calendar.HOUR_OF_DAY, 12);
 			cal.set(Calendar.MINUTE, 0);
 			cal.set(Calendar.SECOND, 0);
 			
 			Day day = diaryAdminDAO.getDayByDate(cal.getTime());
 			if (day != null) {
 				List<Event> events = diaryAdminDAO.getEventsByDay(day.getId());
 				
 				request.setAttribute("day", day);
 				request.setAttribute("events", events);
 			}
 			request.setAttribute("dayString", dayString);
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/admin/edit.jsp");
 			rd.forward(request, response);
 			return;
 		}
 		
 		if ("update_day".equals(command)) {
 			String id = request.getParameter("id");
 			String descriptionOfTheDay = request.getParameter("descriptionOfTheDay");
 			Day d = new Day();
 			d.setId(Integer.parseInt(id));
 			d.setDescriptionOfTheDay(descriptionOfTheDay);
 			boolean b = diaryAdminDAO.updateDay(d);
 			if (b) {
 				request.setAttribute("msg", "Sikeresen mentve!");
 			}
 			
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/admin/admin.jsp");
 			rd.forward(request, response);
 			return;
 		}
 		
 		if ("update_event".equals(command)) {
 			String id = request.getParameter("id");
 			String startDate = request.getParameter("startDate");
 			String duration = request.getParameter("duration");
 			String description = request.getParameter("description");
 			
 			Event e = new Event();
 			e.setId(Integer.parseInt(id));
 			e.setStartTime(diaryHelper.getStartDateCal(startDate).getTime());
 			e.setDuration(Long.parseLong(duration));
 			e.setDescription(description);
 			
 			boolean b = diaryAdminDAO.updateEvent(e);
 			if (b) {
 				request.setAttribute("msg", "Sikeresen mentve!");
 			}
 
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/admin/admin.jsp");
 			rd.forward(request, response);
 			return;
 		}
 		
 		if ("dbfix".equals(command)) {
 			List<Day> allDays = diaryAdminDAO.getAllDays();
 			List<Event> allEvents = diaryAdminDAO.getAllEvents();
 			Map<String, String> wordsToFix = new HashMap<String, String>();
 			for (Day d : allDays) {
 				collectWordsToFix(d.getDescriptionOfTheDay(), wordsToFix);
 			}
 			for (Event e : allEvents) {
 				collectWordsToFix(e.getDescription(), wordsToFix); 
 			}
 			request.setAttribute("wordsToFix", wordsToFix);
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/admin/fix_db_transfers.jsp");
 			rd.forward(request, response);
 			return;			
 		}
 		if ("edit_words".equals(command)) {
 			Map<String, String> fixedWords = getFixedWords(request);
 			List<Day> allDays = diaryAdminDAO.getAllDays();
 			List<Day> daysToFix = new ArrayList<Day>();
 			for (Day d : allDays) {
 				for (Map.Entry<String, String> entry : fixedWords.entrySet()) {
 					if (d.getDescriptionOfTheDay().indexOf(entry.getKey()) != -1) {
 						d.setDescriptionOfTheDay(d.getDescriptionOfTheDay().replace(entry.getKey(), entry.getValue()));
 						if (!daysToFix.contains(d)) {
 							daysToFix.add(d);
 						}
 					}
 				}
 			}
 			for (Day d : daysToFix) {
 				diaryAdminDAO.updateDay(d);
 			}
 			List<Event> allEvents = diaryAdminDAO.getAllEvents();
 			List<Event> eventsToFix = new ArrayList<Event>();
 			for (Event e :  allEvents) {
 				for (Map.Entry<String, String> entry : fixedWords.entrySet()) {
 					if (e.getDescription().indexOf(entry.getKey()) != -1) {
 						e.setDescription(e.getDescription().replace(entry.getKey(), entry.getValue()));
 						if (!eventsToFix.contains(e)) {
 							eventsToFix.add(e); 
 						}
 					}
 				}
 			}
 			for (Event e : eventsToFix) {
 				diaryAdminDAO.updateEvent(e);
 			}
 			response.sendRedirect("/admin/admin.jsp");
 		}
 	}
 	
 	private Map<String, String> getFixedWords(HttpServletRequest request) {
 		Map<String, String> fixedWords = new HashMap<String, String>();
 		Enumeration en = request.getParameterNames();
 		while (en.hasMoreElements()) {
 			String param = (String)en.nextElement();
 			if (param.startsWith(PARAM_PREFIX_CHANGED)) {
 				if (Boolean.valueOf(request.getParameter(param))) {
 					String key = param.replace(PARAM_PREFIX_CHANGED, "");
 					fixedWords.put(key, request.getParameter(PARAM_PREFIX_WORD + key));
 				}
 			}
 		}
 		return fixedWords;
 	}
 
 	private void collectWordsToFix(String description, Map<String, String> wordsToFix) {
 		StringTokenizer st = new StringTokenizer(description, " .,\"");
 		while (st.hasMoreElements()) {
 			String s = st.nextToken();
 			if (s.indexOf('?') == -1) {
 				continue;
 			}
 			if (!wordsToFix.containsKey(s)) {
 				wordsToFix.put(s, s);
 			}
 		}
 	}
 }
 
