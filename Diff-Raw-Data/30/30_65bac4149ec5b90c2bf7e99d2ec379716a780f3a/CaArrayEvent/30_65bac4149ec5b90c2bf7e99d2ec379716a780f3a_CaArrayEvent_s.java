 package org.geworkbench.events;
 
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;
 import org.geworkbench.engine.config.events.Event;
 
 public class CaArrayEvent extends Event {
 	public static final String EXPERIMENT = "EXP";
 	public static final String BIOASSAY = "BIOASSAY";
 	public static String searchcritia = "selection";
 	private boolean populated = false;
 	private boolean succeed = true;
 	private String infoType;
	private DSDataSet<? extends DSBioObject> dataSet = null;
	private String url;
	private int port;
	private String username;
	private String password;
 	private String errorMessage;
 	
 	private CaArray2Experiment[] experiments;
 
 	public CaArrayEvent(String _url, int _port) {
 		super(null);
 		url = _url;
 		port = _port;
 		infoType = EXPERIMENT;
 	}
 
 	public boolean isPopulated() {
 		return populated;
 	}
 
 	public void setPopulated(boolean populated) {
 		this.populated = populated;
 	}
 
 	public boolean isSucceed() {
 		return succeed;
 	}
 
 	public String getInfoType() {
 		return infoType;
 	}
 
 	public void setInfoType(String infoType) {
 		this.infoType = infoType;
 	}
 
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

 	public String getUrl() {
 		return url;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public void setSucceed(boolean succeed) {
 		this.succeed = succeed;
 	}
 
	public DSDataSet<? extends DSBioObject> getDataSet() {
		return dataSet;
	}

 	public CaArray2Experiment[] getExperiments() {
 		return experiments;
 	}
 
 	public void setExperiments(CaArray2Experiment[] experiments) {
 		this.experiments = experiments;
 	}
 
 	public String getErrorMessage() {
 		return errorMessage;
 	}
 
 	public void setErrorMessage(String errorMessage) {
 		this.errorMessage = errorMessage;
 	}
 
 }
