 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.model;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import lombok.*;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.repository.LogRepository;
 
 /**
  * @author ne_Sachirou
  */
 @Data
 public class Log implements Serializable {
	private static final long serialVersionUID = -7881950325931115064L;
 
 	private Map<String, Object> log = new HashMap<>();
 
 	/**
 	 * This MUST NOT throw errors.
 	 */
 	public void write() {
 		try {
 			new LogRepository().insert(this);
 		} catch (DBAccessException e) {
 		}
 	}
 }
