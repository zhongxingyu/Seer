 package edu.nrao.dss.client;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.google.gwt.core.client.GWT;
 
 public class PeriodExplorer extends Explorer {
 	public PeriodExplorer() {
 		super("/periods", new PeriodType(columnTypes));
 		initLayout(initColumnModel());
 	}
 	
 	private ColumnModel initColumnModel() {
 		configs = new ArrayList<ColumnConfig>();
 		for (ColumnType ct : columnTypes) {
	        configs.add(new PeriodColConfig(ct.getId(), ct.getName(), ct.getLength(), ct.getClasz()));
 		}
 	    return new ColumnModel(configs);
 	}
 	
 	public ColumnConfig getSessionConfig() {
 		return configs.get(0);
 	}
 	
 	private List<ColumnConfig> configs;
 
 	private static final ColumnType[] columnTypes = {
        	new ColumnType("handle",          "Session (Project)", 200, SessionField.class),
         new ColumnType("start",           "Start",             120, String.class), 
         new ColumnType("duration",        "Duration (Hrs)",     90, Double.class),
        	new ColumnType("backup",          "Backup?",            70, Boolean.class),
 	};
 }
