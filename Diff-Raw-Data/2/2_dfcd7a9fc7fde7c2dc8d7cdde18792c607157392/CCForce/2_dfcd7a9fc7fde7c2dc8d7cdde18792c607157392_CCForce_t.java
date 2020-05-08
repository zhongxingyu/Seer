 package org.concord.waba.extra.probware.probs;
 import org.concord.waba.extra.event.DataListener;
 import org.concord.waba.extra.event.DataEvent;
 import extra.util.DataDesc;
 import org.concord.waba.extra.probware.*;
 import extra.util.*;
 
 public class CCForce extends CCProb{
 float  			[]forceData = new float[CCInterfaceManager.BUF_SIZE/2];
 public final static String [] portNames = {"A", "B"};
 public final static String [] numbChannels = {"1", "2"};
 public final static String [] channelNames = {"0", "1"};
 public final static String [] propNames = {"Port", "Num Channels","Channel #"};
 public int curChannel = 0;
 float	A = 0.01734f;
 float B = -25.31f;
 	private boolean 	fromConstructor = true;
 
 	CCForce(){
 		this("unknown");
 	}
 	CCForce(String name){
 	    	activeChannels = 2;
 		setName(name);
 		dDesc.setChPerSample(1);
 		dDesc.setDt(0.0f);
 		dEvent.setDataDesc(dDesc);
 		dEvent.setDataOffset(0);
 		dEvent.setData(forceData);
 
 		properties = new PropObject[4];
 		properties[0] = new PropObject(samplingModeString,samplingModes); 
 		properties[1] = new PropObject(propNames[0], portNames);
 		properties[2] = new PropObject(propNames[1], numbChannels);
 		properties[3] = new PropObject(propNames[2], channelNames);
 		setPropertyValue(0,samplingModes[CCProb.SAMPLING_10BIT_MODE]);
 		setPropertyValue(1,portNames[0]);
 		setPropertyValue(2,numbChannels[0]);
 		setPropertyValue(3,channelNames[0]);
 		
 		calibrationDesc = new CalibrationDesc();
 		calibrationDesc.addCalibrationParam(new CalibrationParam(0,A));
 		calibrationDesc.addCalibrationParam(new CalibrationParam(1,B));
 
 		fromConstructor = false;
 	}
 	public void setDataDescParam(int chPerSample,float dt){
 		dDesc.setDt(dt);
 		dDesc.setChPerSample(chPerSample);
 	}
 	protected boolean setPValue(PropObject p,String value){
 		if(p == null || value == null) return false;
 		String nameProperty = p.getName();
 		if(nameProperty == null) return false;
 		if(!fromConstructor && (nameProperty.equals(samplingModeString))){
 			if(value.equals(samplingModes[SAMPLING_DIG_MODE]))
 				return true;
 			else
 				return super.setPValue(p,value);
 		}
 		if(nameProperty.equals(propNames[0])){
 			if(value.equals("A")){
 				interfacePort = INTERFACE_PORT_A;
 			} else if(value.equals("B")){
 				interfacePort = INTERFACE_PORT_B;
 			}
 		} else if(nameProperty.equals(propNames[1])){
 			if(value.equals("1")){
 				activeChannels = 1;
 			} else if(value.equals("2")){
 				activeChannels = 2;
 			}		
 		} else if(nameProperty.equals(propNames[2])){
 			if(value.equals("0")){
 				curChannel = 0;
 			} else if(value.equals("1")){
 				curChannel = 1;
 			}
 		}
 		return  super.setPValue(p,value);
 	}
 
 	int chPerSample = 2;
 	int channelOffset = 0;
     	public boolean idle(DataEvent e){
 		dEvent.type = e.type;
 		if(calibrationListener == null){
		    notifyDataListeners(dEvent);
 		}
 	    	return true;
 	}
     	public boolean startSampling(DataEvent e){
 		dEvent.type = e.type;
 		dDesc.setDt(e.getDataDesc().getDt());
 		chPerSample = e.getDataDesc().getChPerSample();
 		if(calibrationListener != null){
 			if(activeChannels == 2)
 				dDesc.setChPerSample(3);
 			else
 				dDesc.setChPerSample(2);
 		}else{
 			dDesc.setChPerSample(1);
 		}
 		channelOffset = curChannel;
 		if(curChannel > activeChannels - 1) channelOffset = activeChannels - 1;
 		if(calibrationListener == null){
 			notifyDataListeners(e);
 		}
 	    	return true;
     	}
 
 	public boolean dataArrived(DataEvent e){
 		dEvent.type = e.type;
 		dEvent.time = e.time;
 		if(calibrationListener != null){
 			dEvent.numbSamples = 1;
 			forceData[0] = A*e.data[e.dataOffset+channelOffset]+B;
 			if(activeChannels == 2){
 				forceData[1] = e.data[e.dataOffset];
 				forceData[2] = e.data[e.dataOffset+1];
 			}else{
 				forceData[1] = e.data[e.dataOffset+channelOffset];
 				forceData[2]  = 0f;
 			}
 		}else{
 			dEvent.time = e.time;
 			dEvent.numbSamples = e.numbSamples;
 			dEvent.pTimes = e.pTimes;
 			dEvent.numPTimes = e.numPTimes;
 			int ndata = dEvent.numbSamples*e.dataDesc.chPerSample;
 			int dOff = e.dataOffset;
 			float data [] = e.data;
 			int currPos = 0;
 			for(int i = 0; i < ndata; i+= chPerSample){
 				forceData[currPos++] = A*data[dOff + i+channelOffset]+B;
 			}
 		}
 		notifyDataListeners(dEvent);
 		return true;
 	}
 	public void  calibrationDone(float []row1,float []row2,float []calibrated){
 		if(row1 == null || calibrated == null) return;
 		float x1 = row1[0];
 		float x2 = row1[1];
 		float y1 = calibrated[0];
 		float y2 = calibrated[1];
 		A = (y2 - y1)/(x2 - x1);
 		B = y2 - A*x2;
 		if(calibrationDesc != null){
 			CalibrationParam p = calibrationDesc.getCalibrationParam(0);
 			if(p != null) p.setValue(A);
 			p = calibrationDesc.getCalibrationParam(1);
 			if(p != null) p.setValue(B);
 		}
 		//		System.out.println("A "+A);
 		// System.out.println("B "+B);
 	}
 	public void calibrationDescReady(){
 		if(calibrationDesc == null) return;
 		CalibrationParam p = calibrationDesc.getCalibrationParam(0);
 		if(p == null || !p.isValid()) return;
 		A = p.getValue();
 		p = calibrationDesc.getCalibrationParam(1);
 		if(p == null || !p.isValid()) return;
 		B = p.getValue();
 	}
 }
