 package org.jyald.core;
 
 import org.jyald.loggingmodel.*;
 import org.jyald.uicomponents.TabContent;
 import org.jyald.util.*;
 
 
 public class LogcatManager {
 	private IterableArrayList<FilteredLogSlot> slots;
 	private EntryList generalEntries;
 	private TabContent generalLoggerUi;
 	private ProcessObject logcatProcess;
 	
 	public LogcatManager() {
 		
 		slots = new IterableArrayList<FilteredLogSlot>();
 		generalEntries = new EntryList();
 		
 		logcatProcess = new ProcessObject();
 		logcatProcess.setOutputLineReceiver(new ProcessStdoutHandler() {
 
 			@Override
 			public void onOutputLineReceived(String line) {
 				onLineReceived(line);
 			}
 		});
 		
 	}
 	
 	
 	private void onLineReceived(String line) {
 		final BoolContainer isGeneralEntry = new BoolContainer(true);
 		
 		if (line.compareTo("DEVCON") == 0) {
 			//TODO: Notifity user
 			return;
 		}
 		
 		final LogEntry entry = LogEntry.parse(line);
 		
 		slots.iterate(new ArrayListIterateHandler<FilteredLogSlot>() {
 
 			@Override
 			public boolean iterate(FilteredLogSlot item) {
 				
 				try {
 					if (item.tryAdd(entry)) {
 						isGeneralEntry.setBool(false);
						return true;
 					}
 					
 				} catch (Exception e) {}
 				
 				isGeneralEntry.setBool(true);
 				return false;
 			}
 			
 		});
 		
 		if (isGeneralEntry.getBool()) {
 			generalEntries.addEntry(entry);
 			generalLoggerUi.writeLog(entry);
 		}
 		
 		
 	}
 	
 	public boolean start() throws Exception {
 		if (StringHelper.isNullOrEmpty(logcatProcess.getExecutableFile()))
 			throw new Exception("adb is not set!");
 		
 		logcatProcess.start();
 		
 		return logcatProcess.isRunning();
 	}
 	
 	public void stop() {
 		logcatProcess.kill();
 	}
 	
 	public final String getAdb() {
 		return logcatProcess.getExecutableFile();
 	}
 	
 	public void setAdb(String adbFile) {
 		logcatProcess.setExecutableFile(adbFile);
 	}
 	
 	public FilteredLogSlot addSlot(String name, FilterList list) {
 		FilteredLogSlot slot = null;
 		
 		if (StringHelper.isNullOrEmpty(name)) 
 			return null;
 		
 		slot = new FilteredLogSlot(name,list);
 		slots.add(slot);
 		
 		if (slots.getCount() == 1) {
 			generalLoggerUi = slot.getLoggerUi();
 		}
 		
 		return slot;
 	}
 	
 	public void removeSlot(String name) {
 		FilteredLogSlot toRemove = null;
 		
 		for (FilteredLogSlot slot : slots) {
 			if (slot.getSlotName().compareTo(name) == 0) {
 				toRemove = slot;
 				break;
 			}
 		}
 		
 		if (toRemove != null) {
 			try {
 				toRemove.dispose();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			slots.remove(toRemove);
 		}
 	}
 	
 	
 	
 }
