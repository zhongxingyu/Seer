 package com.uwusoft.timesheet.commands;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.ui.AbstractSourceProvider;
 import org.eclipse.ui.ISources;
 
 import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.model.TaskEntry;
 
 public class SessionSourceProvider extends AbstractSourceProvider {
 	public final static String SESSION_STATE = "com.uwusoft.timesheet.commands.sourceprovider.active";
 	public final static String BREAK_STATE = "com.uwusoft.timesheet.commands.sourceprovider.break";
 	public final static String ENABLED = "ENABLED";
 	public final static String DISABLED = "DISABLED";
 	private boolean enabled;
 	private boolean breakSet;
 
 	public SessionSourceProvider() {
 		super();
 		LocalStorageService storageService = LocalStorageService.getInstance();
 		TaskEntry lastTask = storageService.getLastTask();
 		enabled = lastTask != null;
		breakSet = enabled && StorageService.BREAK.equals(lastTask == null ? "" : lastTask.getTask().getName());
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 	@SuppressWarnings({ "rawtypes" })
 	@Override
 	public Map getCurrentState() {
 		Map<String, String> map = new HashMap<String, String>(2);
 		String value = enabled ? ENABLED : DISABLED;
 		map.put(SESSION_STATE, value);
 		value = breakSet ? ENABLED : DISABLED;
 		map.put(BREAK_STATE, value);
 		return map;
 	}
 
 	@Override
 	public String[] getProvidedSourceNames() {
 		return new String[] { SESSION_STATE, BREAK_STATE };
 	}
 
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled ;
 		String value = enabled ? ENABLED : DISABLED;
 		fireSourceChanged(ISources.WORKBENCH, SESSION_STATE, value);
 	}
 
 	public void setBreak(boolean breakSet) {
 		this.breakSet = breakSet;
 		String value = breakSet ? ENABLED : DISABLED;
 		fireSourceChanged(ISources.WORKBENCH, BREAK_STATE, value);
 	}
 }
