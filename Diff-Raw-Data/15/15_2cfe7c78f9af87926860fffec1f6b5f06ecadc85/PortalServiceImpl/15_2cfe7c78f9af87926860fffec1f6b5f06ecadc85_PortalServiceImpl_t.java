 package com.ritchey.portal.server;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.ibatis.session.RowBounds;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
 import org.springframework.stereotype.Service;
 
 import com.browsexml.ldap.LdapUserDetails;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.ritchey.portal.client.PortalService;
 import com.ritchey.portal.domain.chapel.Person;
 import com.ritchey.portal.domain.chapel.PersonExample;
 import com.ritchey.portal.domain.chapel.PersonMapper;
 import com.ritchey.portal.domain.chapel.PropertiesExample;
 import com.ritchey.portal.domain.chapel.PropertiesMapper;
 import com.ritchey.portal.domain.chapel.PunchMapper;
 import com.ritchey.portal.domain.powercampus.AcademicCalendar;
 import com.ritchey.portal.domain.powercampus.AcademicCalendarExample;
 import com.ritchey.portal.domain.powercampus.AcademicCalendarMapper;
 import com.ritchey.portal.shared.ListCount;
 import com.ritchey.portal.shared.Log;
 import com.ritchey.portal.shared.SimpleLoginType;
 
 /**
  * The server side implementation of the RPC service.
  */
 @Configurable
 @Service
 @SuppressWarnings("serial")
 public class PortalServiceImpl extends RemoteServiceServlet implements
 		PortalService, BeanFactoryAware {
 	
 	private PropertiesMapper propertiesMapper;
 	private PunchMapper punchMapper;
 	private AcademicCalendarMapper academicCalendarMapper;
 	private PersonMapper personMapper;
 	
 	public PropertiesMapper getPropertiesMapper() {
 		return propertiesMapper;
 	}
 
 	@Autowired
 	public void setPropertiesMapper(PropertiesMapper propertiesMapper) {
 		this.propertiesMapper = propertiesMapper;
 	}
 
 	public PersonMapper getPersonMapper() {
 		return personMapper;
 	}
 
 	@Autowired
 	public void setPersonMapper(PersonMapper personMapper) {
 		this.personMapper = personMapper;
 	}
 
 	@Autowired
 	public void setPunchMapper(PunchMapper punchMapper) {
 		this.punchMapper = punchMapper;
 	}
 	
 	@Autowired
 	public void setAcademicCalendarMapper(AcademicCalendarMapper academicCalendarMapper) {
 		this.academicCalendarMapper = academicCalendarMapper;
 	}
 	
 	static Properties properties = new Properties();
 	static {
 		try {
 			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public PortalServiceImpl() {
 		Log.debug("create PortalService");
 	}
 	
 	public String hashString(String completeString)
 			throws NoSuchAlgorithmException {
 		MessageDigest m = MessageDigest.getInstance("MD5");
 		return byteArrayToString(m.digest(completeString.getBytes()));
 	}
 
 	public static String byteArrayToString(byte[] data) {
 		StringBuilder sb = new StringBuilder();
 		for (byte b : data) {
 			sb.append(String.format("%02X", b));
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * return the earliest date of all the terms currently in session
 	 */
 	@Override
 	public Date getStartTerm() {
 		Date ret = null;
 		try {
 			AcademicCalendarExample calendar = new AcademicCalendarExample();
 			Date now = new Date();
 			ret = new Date(now.getTime());
 			calendar.createCriteria().andStartDateLessThanOrEqualTo(now).andEndDateGreaterThanOrEqualTo(now);
 			List<AcademicCalendar> list = academicCalendarMapper.selectByExample(calendar);
 
 			// Get first (smallest) start date of all terms that are currently active
 			
 			for (AcademicCalendar c: list) {
 				if (ret.after(c.getStartDate()))
 					ret = c.getStartDate();
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	@Override
 	public ListCount<Map> getPresent(String peopleCodeId, Date termStart, int offset, int limit) {
 		try {
 			String campusId = peopleCodeId.replaceAll("[Pp]", "");

 			if (termStart == null) {
 				termStart = getStartTerm();
 			}
 			
 			PersonExample example = new PersonExample();
			example.createCriteria().andPeopleIdEqualTo(campusId);
 			List<Person> x = personMapper.selectByExample(example);
 			Integer exempt = 0;
 			if (x.size() > 0) {
 				exempt = x.get(0).getExempt();
 			}
 			
 			PropertiesExample propertiesExample = new PropertiesExample();
 			propertiesExample.createCriteria().andKeyvalueEqualTo("GOAL_CALC");
 			List<com.ritchey.portal.domain.chapel.Properties> properties = propertiesMapper.selectByExample(propertiesExample);
 			String equation = "0";
 			if (properties.size() > 0) {
 				equation = properties.get(0).getStrvalue();
 			}
 			
 			List<Map> list = punchMapper.selectPresent(campusId, termStart, new RowBounds(offset, limit));
 			Map m = punchMapper.selectPresentCount(campusId, termStart);
 			Map env = new HashMap();
 			ListCount<Map> ret = new ListCount(list, (Integer) m.get("count"), env);
 			ret.getEnv().put("total", m.get("total"));
 			   
 			ScriptEngineManager mgr = new ScriptEngineManager();
 			    ScriptEngine engine = mgr.getEngineByName("JavaScript");
 			    engine.put("exempt", exempt);
 			    Object result = engine.eval(equation);
 			    
 			ret.getEnv().put("goal", result);
 			return ret;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@Override
 	
 	public ListCount<Map> getTardies(String peopleCodeId, Date termStart, int offset, int limit) {
 		if (termStart == null) {
 			termStart = getStartTerm();
 		}
 		try {
 			String campusId = peopleCodeId.replaceAll("[Pp]", "");
 			List<Map> list = punchMapper.selectTardy(campusId, termStart, new RowBounds(offset, limit));
 			Map m = punchMapper.selectTardyCount(campusId, termStart);
 			Map env = new HashMap();
 			ListCount<Map> ret = new ListCount(list, (Integer) m.get("count"), env);
 			ret.getEnv().put("total", m.get("total"));
 			return ret;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public SimpleLoginType chapel( String userName, String password) throws IllegalArgumentException {
 		// Verify that the input is valid.
 		Log.debug("chapel");
 		SimpleLoginType s = null;
 		try {
 			s = authenticate(userName, password);
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		if (!s.getAuthenticated()) {
 			throw new IllegalArgumentException(
 					"Authentication failed for " + userName);
 		}
 		return s;
 
 	}
 	
 	@Override
 	public String getName(String peopleId) {
 		try {
 			Log.debug("getName  " + peopleId);
 			if (peopleId.startsWith("P")) {
 				peopleId = peopleId.substring(1);
 			}
 			PersonExample example = new PersonExample();
 			example.createCriteria().andPeopleIdEqualTo(peopleId);
 			List<Person> persons = personMapper.selectByExample(example);
 			if (persons.size() > 0) {
 				Person p = persons.get(0);
 				String name = p.getFirstname() + " " + p.getLastname();
 				Log.debug("return " + name);
 				return name;
 			}
 			Log.debug("return ''");
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	/**
 	 * Escape an html string. Escaping data received from the client helps to
 	 * prevent cross-site script vulnerabilities.
 	 * 
 	 * @param html
 	 *            the html string to escape
 	 * @return the escaped string
 	 */
 	private String escapeHtml(String html) {
 		if (html == null) {
 			return null;
 		}
 		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
 				.replaceAll(">", "&gt;");
 	}
 
 	public String greetServer(String name) throws IllegalArgumentException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	LdapAuthenticationProvider secondLdapProvider = null;	
 	
 	@Override
 	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
 		Log.debug("SET BEAN FACTORY");
 		secondLdapProvider = (LdapAuthenticationProvider) beanFactory
 				.getBean("secondLdapProvider");
 
 	}
 	
 	public SimpleLoginType authenticate(String user, String password) {
 		//System.err.println("Authenticate " + user + "  password: " + password);
 		
 	      Authentication y = new UsernamePasswordAuthenticationToken(user, password);
 	      if (secondLdapProvider != null) {
 	    	  System.err.println("AUTHENTICATING...");
 			try {
 				y = secondLdapProvider.authenticate(y);
 			} catch (AuthenticationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 			System.err.println("result is " + y.isAuthenticated());
 	      }
 	      System.err.println("principal = " + y.getPrincipal());
 	      System.err.println("authenticated = " + y.isAuthenticated());
 	      LdapUserDetails userDetails = ((LdapUserDetails) y.getPrincipal());
 	      SimpleLoginType s = new SimpleLoginType();
 	      s.setAuthenticated(y.isAuthenticated());
 	      userDetails.copy(s);
 	      s.setAuthenticated(s.getAuthenticated());
 	      System.err.println("peopleId = " + s.getPeopleId());
 	      System.err.println("email = " + s.getEmail());
 	      System.err.println("Name = " + s.getName());
 	      
 	      return s;
 	}
 	
 	@Override
 	public String nelnetServer( String fullName, String peopleId, String email, String payMethod) throws IllegalArgumentException {
 
 		// Escape data from the client to avoid cross-site script
 		// vulnerabilities.
 		if (payMethod == null || payMethod.trim().equals("")) {
 			payMethod = "ach_cc";
 		}
 
 		email = email.toLowerCase();
 		//String fullName = firstName + " " + lastName;
 		Long timestamp = new Date().getTime();
 		System.err.println("timestamp = " + timestamp);
 		//timestamp = 1351105697865L;
  
 		String sharedSecret = properties.getProperty("sharedSecret");
 		Log.debug("shared = " + sharedSecret);
 		String complete = peopleId + fullName + email + payMethod + timestamp
 				+ sharedSecret;
 		String hash = null;
 		try {
 			hash = hashString(complete);
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		String link =  "https://quikpayasp.com/lcu/tuition/payer.do?userId=" + peopleId
 				+ "&fullName=" + fullName + "&email=" + email
 				+ "&paymentMethod=" + payMethod + "&timestamp="
 				+ timestamp + "&hash=" + hash;
 		Log.debug("link = " + link);
 		System.err.println("link = " + link);
 		return link;
 
 	}
 	
 	@Override
 	public String getContext() {
 		Log.debug("in server; properties = " + properties);
 		HttpServletRequest local = this.getThreadLocalRequest();
 		StringBuffer request = new StringBuffer("");
 		if (local != null) local.getRequestURL();
 		String context  = "" + properties.get("application.version");
 		Log.debug("contxt returning = " + context);
 		return context;// this.getThreadLocalRequest().getQueryString();
 	}
 }
