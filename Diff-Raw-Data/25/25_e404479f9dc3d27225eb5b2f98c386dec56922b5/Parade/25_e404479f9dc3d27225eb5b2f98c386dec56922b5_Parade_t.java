 package org.makumba.parade.model;
 
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import org.makumba.parade.init.ParadeProperties;
 import org.makumba.parade.model.managers.AntManager;
 import org.makumba.parade.model.managers.CVSManager;
 import org.makumba.parade.model.managers.FileManager;
 import org.makumba.parade.model.managers.MakumbaManager;
 import org.makumba.parade.model.managers.RowStoreManager;
 import org.makumba.parade.model.managers.TrackerManager;
 import org.makumba.parade.model.managers.WebappManager;
 import org.makumba.parade.view.managers.RowStoreViewManager;
 
 public class Parade {
 	
 	private Long id;
 	
 	private String paradeBase = new String();
 	
 	private Map rows = new HashMap();
 
 	private static Logger logger = Logger.getLogger(Parade.class.getName());
 	
 	
 	/*
 	 * 1. Calls RowStoreManager.paradeRefresh()
 	 * 2. Calls for each row:
 	 * - rowRefresh()
 	 * - directoryRefresh()
 	 */
 	public void refresh() {
 		
 		this.paradeBase = (String) ParadeProperties.getProperty("parade.paradeBase");
 		
 		RowStoreManager rowMgr = new RowStoreManager();
 		FileManager fileMgr = new FileManager();
 		CVSManager CVSMgr = new CVSManager();
 		AntManager antMgr = new AntManager();
 		WebappManager webappMgr = new WebappManager();
 		MakumbaManager makMgr = new MakumbaManager();
 		
 		/* TODO: read in config class/file which managers are parade managers
 		 * and launch paradeRefresh() for all of them
 		 */
 		rowMgr.paradeRefresh(this);
 		
 		
 		/* TODO: read in config class/file which managers are row managers
 		 * and and launch rowRefresh(row)) for all of them
 		 */
 		
 		Iterator i = rows.keySet().iterator();
 		while(i.hasNext()) {
 			
 			Row r = (Row) rows.get((String) i.next());
 			
 			fileMgr.rowRefresh(r);
 			CVSMgr.rowRefresh(r);
 			antMgr.rowRefresh(r);
			webappMgr.rowRefresh(r);
 			makMgr.rowRefresh(r);
 		}
 	}
 	
 	
 	public void addRow(Row r) {
 		r.setParade(this);
 		rows.put(r.getRowname(),r);
 	}
 	
 			
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Map getRows() {
 		return rows;
 	}
 
 	public void setRows(Map rows) {
 		this.rows = rows;
 	}
 	
 	public Parade() {
 		
 	}
 
 
 	public String getParadeBase() {
 		return paradeBase;
 	}
 
 
 	public void setParadeBase(String paradeBase) {
 		this.paradeBase = paradeBase;
 	}
 
 }
