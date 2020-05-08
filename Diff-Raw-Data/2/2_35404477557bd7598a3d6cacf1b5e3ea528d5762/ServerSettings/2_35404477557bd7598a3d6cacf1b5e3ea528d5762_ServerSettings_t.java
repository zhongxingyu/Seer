 package ch.cern.atlas.apvs.client.settings;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.cell.client.TextInputCell;
 
 public class ServerSettings extends AbstractServerSettings {
 
 	private static final long serialVersionUID = -8089892467523033522L;
 	
 	public enum Entry {
 		ptuUrl("PTU URL"),
 		procedureUrl("Procedure URL"),
 		databaseUrl("Database URL"),
 		audioUrl("Audio URL");
 		private String s;
 		private Class<?> c;
 		private Class<?> n;
 		
 		private Entry(String s) {
 			this(s, TextInputCell.class, TextCell.class);
 		}
 		
 		@SuppressWarnings({ "rawtypes" }) 
 		private Entry(String s, Class c, Class n) {
 			this.s = s;
 			this.c = c;
 			this.n = n;
 		}
 		
 		private Class<?> getCellClass() {
 			return c;
 		}
 
 		private Class<?> getNameClass() {
 			return n;
 		}
 		
 		public String toString() {
 			return s;
 		}
 		
 		public static List<String> getKeys() {
 			List<String> r = new ArrayList<String>(values().length);
 			for (Entry k:values()) {
 				r.add(k.toString());
 			}
 			return r;
 		}
 		
 		public static List<Class<?>> getCellClasses() {
 			List<Class<?>> r = new ArrayList<Class<?>>(values().length);
 			for (Entry k:values()) {
 				r.add(k.getCellClass());
 			}
 			return r;
 		}
 		
 		public static List<Class<?>> getNameClasses() {
 			List<Class<?>> r = new ArrayList<Class<?>>(values().length);
 			for (Entry k:values()) {
 				r.add(k.getNameClass());
 			}
 			return r;
 		}	
 	}
 	
 	public ServerSettings() {
 		this(false);
 	}
 
 	public ServerSettings(boolean setDefaults) {
 		if (!setDefaults)
 			return;
 		put(Entry.ptuUrl.toString(), "pcatlaswpss03:10123");
 		put(Entry.procedureUrl.toString(), "http://localhost:8890/apvs-procs/procedures");
 		put(Entry.databaseUrl.toString(), "wpss@//pcatlaswpss03.cern.ch:1521/XE");
		put(Entry.audioUrl.toString(), "manager@pcatlaswpss02.cern.ch");
 	}
 	
 	public String toString() {
 		StringBuffer sb = new StringBuffer("ServerSettings: ");
 		for (Entry k:Entry.values()) {
 			sb.append(k.toString()+"="+get(k.toString())+"; ");
 		}
 		return sb.toString();
 	}
 }
