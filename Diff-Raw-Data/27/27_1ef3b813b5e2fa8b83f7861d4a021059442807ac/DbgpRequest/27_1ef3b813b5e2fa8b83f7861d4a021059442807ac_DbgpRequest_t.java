 package org.eclipse.dltk.dbgp.internal;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.dltk.dbgp.internal.utils.Base64Helper;
 
 public class DbgpRequest {
 	private Map options;
 
 	private String command;
 
 	private String data;
 
 	public DbgpRequest(String command) {
 		this.command = command;
 		this.options = new HashMap();
 	}
 
 	public String getCommand() {
 		return command;
 	}
 
 	public void addOption(String optionNmae, int optionValue) {
 		addOption(optionNmae, new Integer(optionValue));
 	}
 
 	public void addOption(String optionName, Object optionValue) {
		if (optionValue == null) {			
			throw new IllegalArgumentException();
		}

 		options.put(optionName, optionValue.toString());
 	}
 
 	public String getOption(String optionName) {
 		return (String) options.get(optionName);
 	}
 
 	public boolean hasOption(String optionName) {
 		return options.containsKey(optionName);
 	}
 
 	public int optionCount() {
 		return options.size();
 	}
 
 	public void setData(String data) {
 		this.data = data;
 	}
 
 	public String getData() {
 		return this.data;
 	}
 
 	public String toString() {
 		StringBuffer sb = new StringBuffer(command);
 
 		Iterator it = options.entrySet().iterator();
 
 		while (it.hasNext()) {
 			Map.Entry entry = (Map.Entry) it.next();
 
 			sb.append(' ');
 			sb.append(entry.getKey());
 			sb.append(' ');
 			sb.append(entry.getValue());
 		}
 
 		if (data != null) {
 			sb.append(" -- ");
 			sb.append(Base64Helper.encodeString(data));
 		}
 
 		return sb.toString();
 	}
 
 	public boolean equals(Object o) {
 		if (o instanceof DbgpRequest) {
 			DbgpRequest request = (DbgpRequest) o;
 
 			return command.equals(request.command)
 					&& options.equals(request.options)
 					&& (data != null ? data.equals(request.data) : true);
 		}
 
 		return false;
 	}
 }
