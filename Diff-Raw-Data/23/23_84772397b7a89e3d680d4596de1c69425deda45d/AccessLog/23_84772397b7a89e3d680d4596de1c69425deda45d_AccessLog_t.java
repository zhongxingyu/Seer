 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author ne_Sachirou
  */
 public class AccessLog extends Log {
	private static final long serialVersionUID = 1L;
 
 	public AccessLog() {
 		super();
 	}
 
 	/**
 	 * 
 	 * @param uri
 	 * @param memberId
 	 */
 	public AccessLog(String uri, String memberId) {
 		super();
 		Map<String, Object> log = new HashMap<>();
 		log.put("uri", uri);
 		log.put("memberId", memberId);
 		setLog(log);
 	}
 }
