 /**
  * 
  */
 package org.carnegiesciencecenter.buhl;
 
 import java.util.ArrayList;
 
 /**
  * @author Anh Le
  *
  */
 public class InterSystem extends AbstractDevice {
 
 	public InterStatus getStatus() {
 		return (InterStatus) status;
 	}
 	
 	InterSystem(String name, String ch, ArrayList<DeviceStatus> l) {
 		super(name, ch, new InterStatus(), l);
 		getStatus().currentPage = -1;
 		getStatus().usingSVID = false;
 		type = DeviceTypes.INTERACTIVE;
 	}
 	
 	@Override
 	public void loadConfiguration() {
 		getStatus().usingSVID = 
				(ScriptExplorer.globalConf.getParam(getStatus().deviceName, "SVID").toUpperCase().compareTo("YES") == 0);
 	}
 
 	public void page(int n) {
 		//if (getStatus().currentPage != -1)
 		//	DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime, objName()));
 		getStatus().currentPage = n;
 		getStatus().state = DeviceState.STABLE;
 		this.recordStatus();
 	}
 
 	public void load(String s) {
 		getStatus().fileName = s;
 		getStatus().state = DeviceState.STABLE;
 		this.recordStatus();
 	}
 
 	public void run() {
 		page(0);
 	}
 
 	public String objName() {
 		if (getStatus().usingSVID)
 			return "SVid";
 		else
 			return String.format("%s_%s_%02d", getStatus().deviceName, getStatus().fileName, getStatus().currentPage); 
 	}
 }
