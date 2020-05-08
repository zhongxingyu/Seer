 package com.argility.centralpages.ui.view;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.argility.centralpages.CentralpagesApplication;
 import com.argility.centralpages.dao.StatsProdDAO;
 import com.argility.centralpages.data.StatsProd;
 import com.argility.centralpages.data.StatsProdContainer;
 import com.argility.centralpages.ui.StatsProdForm;
 import com.argility.centralpages.ui.StatsProdTable;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.ui.VerticalSplitPanel;
 
 @SuppressWarnings("serial")
 public class StatsProdView extends VerticalSplitPanel implements
 	Property.ValueChangeListener{
 
 	protected transient Logger log = Logger.getLogger(this.getClass().getName());
 	
 	protected CentralpagesApplication app;
 	protected StatsProdDAO dao;
 	protected StatsProdTable table;
 	protected StatsProdForm form;
 	
 	public StatsProdView(CentralpagesApplication app) {
 		this.app = app;
 		dao = (StatsProdDAO)app.getSpringContext().getBean("statsProdDAO");
 		addStyleName("view");
 	}
 	
 	public void wireCentralProcessCrashedData () {
 		
 		setStatsTable(dao.getCentralProcCrashedList());
 		
 		table.setVisibleColumns(new String[] {"brCde","central","lastReplicated","xoutReceived", "replDiff", "replProcess", "triadProcess"});
 		table.setColumnHeaders(new String[] {"Branch","Central","Last Replicated","Xout Received", "Replication diff", "Process", "Triad Process"});
 		table.setSortContainerPropertyId("lastReplicated");
		table.removeGeneratedColumn("brCde");
 		
 		wireTable(true);
 
 	}
 	
 	private void wireTable(boolean fullSize) {
 		setFirstComponent(table);
 		if(fullSize)setSplitPosition(100);
 	}
 	
 	private StatsProdTable setStatsTable(List<StatsProd> list) {
 		StatsProdContainer cont = new StatsProdContainer(StatsProd.class,list);
 		table = new StatsProdTable(cont);
 		
 		table.addBrTotalCountFooter(cont);
 		table.addListener((Property.ValueChangeListener) this);
 		
 		return table;
 	}
 	
 	public void wireXoutReceivedNotProcessedData () {
 		setStatsTable(dao.getXoutReceivedNotProcessedList());
 		
 		table.setVisibleColumns(new String[] {"brCde","central","lastReplicated","xoutReceived", "replDiff"});
 		table.setColumnHeaders(new String[] {"Branch","Central","Last Replicated","Xout Received", "Replication Diff"});
 		table.setSortContainerPropertyId("lastReplicated");
		table.removeGeneratedColumn("brCde");
 		
 		wireTable(true);	
 	}
 	
 	public void wireNotReplicatedForDaysData () {
 		setStatsTable(dao.getNotReplicatedForDays());
 
 		table.setVisibleColumns(new String[] { "brCde", "central", "lastReplicated",
 				"xoutReceived", "replDiff" });
 		table.setColumnHeaders(new String[] { "Branch", "Central", "Last Replicated",
 				"Xout Received", "Repl Difference" });
 		table.setSortContainerPropertyId("lastReplicated");
		table.removeGeneratedColumn("brCde");
 		
 		wireTable(true);
 	}
 	
 	public void wireAllData() {
 		setStatsTable(dao.getAllStatsProd());
 		
 		table.setVisibleColumns(StatsProdContainer.NATURAL_COL_ORDER);
 		table.setColumnHeaders(StatsProdContainer.COL_HEADERS_ENGLISH);
 
 		table.setSortContainerPropertyId("brCde");
 
 		wireTable(true);
 	}
 	
 	public void wireImportSwitchingFailedData() {
 		
 		setStatsTable(dao.getSwImportFailedList());
 		
 		table.setVisibleColumns(new String[] {"brCde","central","lastSwLoad","swDiff", "swCrashAudId"});
 		table.setColumnHeaders(new String[] {"Branch","Central","Last Switching Load","Transactions Outstanding", "Crash audit id"});
 		table.setSortContainerPropertyId("lastSwLoad");
 		
 		//table.addGeneratedColumn("swCrashAudId", new CrashedAuditColGenerator());
 		
 		wireTable(true);
 	}
 	
 	public void wireHighSwitchingVolumesToImportData() {
 		setStatsTable(dao.getSwManyToImportList());
 		
 		table.setVisibleColumns(new String[] {"brCde","central","lastSwLoad","swDiff", "swCrashed"});
 		table.setColumnHeaders(new String[] {"Branch","Central","Last Switching load","Transactions Outstanding", "Import Crashed"});
 		table.setSortContainerPropertyId("swDiff");
 		table.setSortAscending(false);
 
 		wireTable(true);
 	}
 	
 	public void wireSwitchingNotImportedForDaysData() {
 		setStatsTable(dao.getSwNotImportedForDaysList());
 		
 		table.setVisibleColumns(new String[] {"brCde","central","lastReplicated","xoutReceived", "lastSwLoad", "swDiff"});
 		table.setColumnHeaders(new String[] {"Branch","Central","Last Replicated","Xout Received", "Last Switching Load", "Switching Trans Outstanding"});
 		table.setSortContainerPropertyId("lastSwLoad");
 		
 		wireTable(true);
 	}
 	
 	public void wireReplicatedAndNotImportedData() {
 		setStatsTable(dao.getReplicateAndNotImportedList());
 		
 		table.setVisibleColumns(new String[] {"brCde","central","lastReplicated","xoutReceived", "lastSwLoad", "swDiff"});
 		table.setColumnHeaders(new String[] {"Branch","Central","Last Replicated","Xout Received", "Last Switching Load", "Switching Trans Outstanding"});
 		table.setSortContainerPropertyId("lastSwLoad");
		table.removeGeneratedColumn("brCde");
 
 		wireTable(true);
 	}
 	
 	public void valueChange(ValueChangeEvent event) {
 		
 		Property property = event.getProperty();
 		if (property == table) {
 			Item item = table.getItem(table.getValue());
 			
 			if (item == null) {
 				setSplitPosition(100);
 			} else {
 				form = new StatsProdForm(item);
 				setSecondComponent(form);
 				setSplitPosition(40);
 			}
 		}
 	}
 
 }
