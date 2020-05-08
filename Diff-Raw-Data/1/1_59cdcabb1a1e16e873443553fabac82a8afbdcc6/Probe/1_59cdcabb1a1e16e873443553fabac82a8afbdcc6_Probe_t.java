 package org.concord.waba.extra.probware.probs;
 
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.probware.*;
 import org.concord.waba.extra.ui.ExtraMainWindow;
 import org.concord.waba.extra.ui.CalibrationDialog;
 import extra.util.*;
 
 public abstract class CCProb implements Transform{
 public 		waba.util.Vector 	dataListeners = null;
 public 		waba.util.Vector 	probListeners = null;
 String		name = null;
 PropObject		[]properties = null;
 CalibrationDesc	calibrationDesc;
 public static final String defaultModeName = "Default";
 
 public final static String samplingModeString = "Sampling";
 public String	[]samplingModes =  {"Slow","Fast","Digital"};
 
 public final static int		SAMPLING_24BIT_MODE = 0;
 public final static int		SAMPLING_10BIT_MODE = 1;
 public final static int		SAMPLING_DIG_MODE = 2;
 
 
 public final static int		INTERFACE_PORT_A	= 0;
 public final static int		INTERFACE_PORT_B	= 1;
 
 public int unit = CCUnit.UNIT_CODE_UNKNOWN;
 
 public final static int		CALIBRATION_PROB_START 	= 10000;
 public final static int		CALIBRATION_PROB_END 		= 10001;
 public final static int		PROPERTIES_PROB_START 	= 10002;
 public final static int		PROPERTIES_PROB_END 		= 10003;
 
 public DataDesc		dDesc = new DataDesc();
 public DataEvent	dEvent = new DataEvent();
 public ProbEvent	pEvent = new ProbEvent();
 
 protected	int interfacePort = INTERFACE_PORT_A;
 public	int interfaceType = CCInterfaceManager.INTERFACE_2;
 protected int 	activeChannels = 1;
 
 protected	int	probeType = ProbFactory.Prob_Undefine;
 
 DataListener calibrationListener = null;
 	protected CCProb(){
 		this("unknown");
 	}
 	
 	protected CCProb(String name){
 		setName(name);
 		calibrationDesc = null;
 		pEvent.setProb(this);
 	}
 	
 	
 	public int 	getInterfaceType(){return interfaceType;}
 	public void setInterfaceType(int interfaceType){this.interfaceType =  interfaceType;}
 	
 	public int 	getInterfacePort(){return interfacePort;}
 	public void setInterfacePort(int interfacePort){this.interfacePort =  interfacePort;}
 	
 
 	public boolean needCalibration(){return ((calibrationDesc != null) && (calibrationDesc.countAvailableParams() > 0));}
 	public CalibrationDesc getCalibrationDesc(){return calibrationDesc;}
 	public void setCalibrationDesc(CalibrationDesc calibrationDesc){this.calibrationDesc = calibrationDesc;}
 
 	public int	getActiveChannels(){return activeChannels;}
 	public int	getActiveCalibrationChannels(){return getActiveChannels();}
 	public void	setActiveChannels(int activeChannels){this.activeChannels = activeChannels;}
 
 	public void setCalibrationListener(DataListener calibrationListener){
 		this.calibrationListener = calibrationListener;
 	}
 
 	public int getProbeType(){return probeType;}
 
 	public void clearCalibrationListener(){
 		setCalibrationListener(null);
 	}
 
 	public void addProbListener(ProbListener l){
 		if(probListeners == null) probListeners = new waba.util.Vector();
 		if(probListeners.find(l) < 0) probListeners.add(l);
 	}
 	public void removeProbListener(ProbListener l){
 		int index = probListeners.find(l);
 		if(index >= 0) probListeners.del(index);
 	}
 	public void notifyProbListeners(ProbEvent e){
 		if(probListeners == null) return;
 		for(int i = 0; i < probListeners.getCount(); i++){
 			ProbListener l = (ProbListener)probListeners.get(i);
 			l.probChanged(e);
 		}
 	}
 	
 	public void addDataListener(DataListener l){
 		if(dataListeners == null) dataListeners = new waba.util.Vector();
 		if(dataListeners.find(l) < 0) dataListeners.add(l);
 	}
 	public void removeDataListener(DataListener l){
		if(dataListeners == null) return;
 		int index = dataListeners.find(l);
 		if(index >= 0) dataListeners.del(index);
 	}
 
 	public void notifyDataListenersEvent(DataEvent e){
 		if(calibrationListener != null){
 			calibrationListener.dataStreamEvent(e);
 		}else{
 			if(dataListeners == null) return;
 			for(int i = 0; i < dataListeners.getCount(); i++){
 				DataListener l = (DataListener)dataListeners.get(i);
 				l.dataStreamEvent(e);
 			}
 		}
 	}
 
 	public void notifyDataListenersReceived(DataEvent e){
 		if(calibrationListener != null){
 			calibrationListener.dataReceived(e);
 		}else{
 			if(dataListeners == null) return;
 			for(int i = 0; i < dataListeners.getCount(); i++){
 				DataListener l = (DataListener)dataListeners.get(i);
 				l.dataReceived(e);
 			}
 		}
 	}
 
 	public boolean startSampling(DataEvent e){
 		if(calibrationListener == null){
 			notifyDataListenersEvent(e);
 		}
 		return true;
 	}
 
 	public boolean dataArrived(DataEvent e){
 		notifyDataListenersReceived(e);
 		return true;
 	}
 
      public boolean idle(DataEvent e){
 		notifyDataListenersEvent(e);
 		return true;
      }
    	
 
 	public abstract void setDataDescParam(int chPerSample,float dt);
     
     public DataDesc getDataDesc()
     {
 	return dDesc;
     }
 
 	public void setName(String name){this.name = name;}
 	public String getName(){return name;}
 	
 	public PropObject getProperty(String nameProperty){
 		if(nameProperty == null) return null;
 		if(countProperties() < 1) return null;
 		for(int i = 0; i < countProperties(); i++){
 			PropObject p = properties[i];
 			if(p == null) continue;
 			if(nameProperty.equals(p.getName())){
 				return p;
 			}
 		}
 		return null;
 	}
 	public PropObject getProperty(int index){
 		if(index < 0 || index >= countProperties()) return null;
 		return properties[index];
 	}
 	public int countProperties(){
 		if(properties == null) return 0;
 		return properties.length;
 	}
 	
 	protected boolean setPValue(PropObject p,String value){
 		if(p == null || value == null) return false;
 		p.setValue(value);
 		pEvent.setInfo(p);
 		notifyProbListeners(pEvent);
 		return true;
 	}
 	
 	public boolean setPropertyValue(String nameProperty,String value){
 		return setPValue(getProperty(nameProperty),value);
 	}
 	public boolean setPropertyValue(int index,String value){
 		return setPValue(getProperty(index),value);
 	}
 	
 	public String getPropertyValue(String nameProperty){
 		PropObject p = getProperty(nameProperty);
 		if(p == null) return null;
 		return p.getValue();
 	}
 	public String getPropertyValue(int index){
 		PropObject p = getProperty(index);
 		if(p == null) return null;
 		return p.getValue();
 	}
 	public float getPropertyValueAsFloat(String nameProperty){
 		PropObject p = getProperty(nameProperty);
 		if(p == null) return 0.0f;
 		p.createFValue();
 		return p.getFValue();
 	}
 	
 	public PropObject[]	getProperties(){return properties;}
 	
 	
 	public void  calibrationDone(float []row1,float []row2,float []calibrated){}
 	
 	public void calibrateMe(ExtraMainWindow owner,DialogListener l,int interfaceType){
 		CalibrationDialog cDialog = new CalibrationDialog(owner,l,"Calibration: "+getName(),this,interfaceType);
 		cDialog.setRect(5,5,160,160);
 		cDialog.show();		
 	}
 	public int getUnit(){return unit;}
 	public boolean setUnit(int unit){this.unit = unit;return true;}
 
 	public String getLabel(){return name;}
 
 	public void writeExternal(extra.io.DataStream out){
 		out.writeInt(CALIBRATION_PROB_START);
 		out.writeBoolean(calibrationDesc != null);
 		if(calibrationDesc != null){
 			calibrationDesc.writeExternal(out);
 		}
 		out.writeInt(CALIBRATION_PROB_END);
 		out.writeInt(PROPERTIES_PROB_START);
 		out.writeBoolean(properties != null);
 		if(properties != null){
 			out.writeInt(countProperties());
 			for(int i = 0; i < countProperties(); i++){
 				PropObject p = properties[i];
 				out.writeBoolean(p != null);
 				if(p == null){
 					continue;
 				}
 				p.writeExternal(out);
 			}
 		}
 		out.writeInt(PROPERTIES_PROB_END);
 		writeInternal(out);
 	}
 	protected void writeInternal(extra.io.DataStream out){
 	}
 	protected void readInternal(extra.io.DataStream in){
 	}
 	
 	public void readExternal(extra.io.DataStream in){
 		int temp = in.readInt();
 		if(temp != CALIBRATION_PROB_START) return;
 		if(in.readBoolean()){
 			if(calibrationDesc == null) calibrationDesc = new CalibrationDesc();
 			calibrationDesc.readExternal(in);
 			calibrationDescReady();
 		}
 		in.readInt();//CALIBRATION_PROB_END
 		temp = in.readInt();
 		if(temp != PROPERTIES_PROB_START) return;	
 		if(in.readBoolean()){
 			temp = in.readInt();
 			if(temp < 1) return;
 			properties = new PropObject[temp];
 			for(int i = 0; i < temp; i++){
 				if(in.readBoolean()) properties[i] = new PropObject(in);
 				setPropertyValue(i,properties[i].getValue());
 			}
 		}
 		temp = in.readInt();//PROPERTIES_PROB_END
 		readInternal(in);
 	}
 	public void calibrationDescReady(){}
 	
 }
