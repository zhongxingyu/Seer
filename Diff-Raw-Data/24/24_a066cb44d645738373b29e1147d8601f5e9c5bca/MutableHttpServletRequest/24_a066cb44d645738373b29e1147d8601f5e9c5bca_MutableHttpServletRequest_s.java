 package net.unicon.shibster.web;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 
 public class MutableHttpServletRequest extends HttpServletRequestWrapper {
 	private Map<String, List<String>> headers = new HashMap<String, List<String>>();
 	private String remoteUser;
 
 	private DateFormat[] dateFormats = {
 			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
 			new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
 			new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
 	};
 
 	public MutableHttpServletRequest(HttpServletRequest request) {
 		super(request);
 	}
 
 	@Override
 	public String getRemoteUser() {
 		return remoteUser;
 	}
 
 	public void setRemoteUser(String remoteUser) {
 		this.remoteUser = remoteUser;
 	}
 
 	@Override
 	public long getDateHeader(String name) {
 		String v = getHeader(name);
 		if (v == null) {
			return -1L;
 		}
 		for (DateFormat df : dateFormats) {
 			try {
 				Date date = df.parse(v);
 				return date.getTime();
 			} catch (ParseException pe) {
 				;
 			}
 		}
 		throw new IllegalArgumentException("Unhandled date format: " + v);
 	}
 
 	@Override
 	public String getHeader(String name) {
		List<String> list = headers.get(name);
 		return (list != null && list.size() > 0) ? list.get(0) : null;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Enumeration getHeaders(String name) {
		List<String> list = headers.get(name);
 		if (list == null) {
 			list = Collections.emptyList();
 		}
 		return Collections.enumeration(list);
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Enumeration getHeaderNames() {
 		return Collections.enumeration(headers.keySet());
 	}
 
 	@Override
 	public int getIntHeader(String name) {
 		String v = getHeader(name);
 		return v != null ? Integer.parseInt(v) : -1;
 	}
 
 	public void addHeader(String name, String value) {
 		List<String> list = headers.get(name);
 		if (list == null) {
 			list = new ArrayList<String>();
 			headers.put(name, list);
 		}
 		list.add(value);
 	}
 }
