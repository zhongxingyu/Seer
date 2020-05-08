 package org.concord.waba.extra.probware.probs;
 import org.concord.waba.extra.event.DataListener;
 import org.concord.waba.extra.event.DataEvent;
 import extra.util.DataDesc;
 import org.concord.waba.extra.probware.*;
 import extra.util.*;
 
 public class CCVoltCurrent extends CCProb{
 float  			[]data = new float[CCInterfaceManager.BUF_SIZE/2];
 float  			dtChannel = 0.0f;
 float				energy = 0.0f;
 public final static int		CURRENT_OUT 			= 0;
 public final static int		VOLTAGE_OUT 			= 1;
 public final static int		WATT_OUT 			= 2;
 public final static int		ENERGY_OUT 			= 3;
 public final static String [] propNames = {"Port", "Output mode"};
 
 float					zeroPointCurrent				= 1257f;//	
 float					zeroPointVoltage				= 1257f;//	
 float					currentResolution		= 700f; //       mV(reading)/A
 float					voltageResolution		= 650f/20; //     mV(reading)/(true)V
 
 int					outputMode 			= VOLTAGE_OUT;
     String [] portNames = {"A", "B"};
 public static String [] modelNames = {"Current", "Voltage","Watt","Joule"};
 
    
    
    
 	int 				curChannel = 0;
 	private boolean 	fromConstructor = true;
 
 	CCVoltCurrent(){
 		this("unknown");
 	}
 	CCVoltCurrent(String name){
 		activeChannels = 2;
 		setName(name);
 		dDesc.setChPerSample(1);
 		dDesc.setDt(0.0f);
 		dEvent.setDataDesc(dDesc);
 		dEvent.setDataOffset(0);
 		dEvent.setNumbSamples(1);
 		dEvent.setData(data);
 
 		properties = new PropObject[3];
 		properties[0] = new PropObject(samplingModeString,samplingModes); 
 		properties[1] = new PropObject(propNames[0], portNames);
 		properties[2] = new PropObject(propNames[1], modelNames);
 		
 		calibrationDesc = new CalibrationDesc();
 		calibrationDesc.addCalibrationParam(new CalibrationParam(0,zeroPointCurrent));
 		calibrationDesc.addCalibrationParam(new CalibrationParam(1,currentResolution));
 		calibrationDesc.addCalibrationParam(new CalibrationParam(2,zeroPointVoltage));
 		calibrationDesc.addCalibrationParam(new CalibrationParam(3,voltageResolution));
 		setPropertyValue(0,samplingModes[CCProb.SAMPLING_24BIT_MODE]);
 		setPropertyValue(1,portNames[0]);
 		setPropertyValue(2,modelNames[1]);
 		unit = CCUnit.UNIT_CODE_VOLT;
 		fromConstructor = false;
 	}
 
 	public int  getActiveCalibrationChannels(){return 1;}
 	public void setDataDescParam(int chPerSample,float dt){
 		dDesc.setDt(dt);
 		dDesc.setChPerSample(chPerSample);
 		dtChannel = dt / (float)chPerSample;
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
 			for(int i = 0; i < modelNames.length;i++){
 				if(modelNames[i ].equals(value)){
 					outputMode = i ;
 					break;
 				}
 			}
 			switch(outputMode){
 				case CURRENT_OUT:
 					unit = CCUnit.UNIT_CODE_AMPERE;
 					break;
 				case VOLTAGE_OUT:
 					unit = CCUnit.UNIT_CODE_VOLT;
 					break;
 				case WATT_OUT:
 					unit = CCUnit.UNIT_CODE_WATT;
 					break;
 				case ENERGY_OUT:
 					unit = CCUnit.UNIT_CODE_JOULE;
 					break;
 			}
 			CalibrationParam cp = calibrationDesc.getCalibrationParam(0);
 			if(cp != null) cp.setAvailable(outputMode == CURRENT_OUT);
 			cp = calibrationDesc.getCalibrationParam(1);
 			if(cp != null) cp.setAvailable(outputMode == CURRENT_OUT);
 			cp = calibrationDesc.getCalibrationParam(2);
 			if(cp != null) cp.setAvailable(outputMode == VOLTAGE_OUT);
 			cp = calibrationDesc.getCalibrationParam(3);
 			if(cp != null) cp.setAvailable(outputMode == VOLTAGE_OUT);
 		}
 		return  super.setPValue(p,value);
 	}
 
 	public boolean idle(org.concord.waba.extra.event.DataEvent e){
 		return true;
 	}
 	public boolean startSampling(org.concord.waba.extra.event.DataEvent e){
 		energy = 0.0f;
 		dEvent.type = e.type;
 		dDesc.setDt(e.getDataDesc().getDt());
 		dEvent.setNumbSamples(1);
 		if(calibrationListener != null){
 			dDesc.setChPerSample(2);
 		}else{
 			dDesc.setChPerSample(1);
 		}
 		dtChannel = dDesc.getDt() / (float)dDesc.getChPerSample();
 		return true;
 	}
     public boolean dataArrived(DataEvent e)
     {
 	dEvent.type 		= e.type;
 	int nOffset 		= e.getDataOffset();
 	int ndata 			= e.getNumbSamples()*e.dataDesc.getChPerSample();
 	float t0 			= e.getTime();
 	float[] dataEvent 	= e.getData();
 	if(calibrationListener != null){
 		switch(outputMode){
 			case CURRENT_OUT:
 				data[0] = (dataEvent[nOffset] - zeroPointCurrent)/currentResolution;
 				data[1] = dataEvent[nOffset];
 				break;
 			case VOLTAGE_OUT:
 				data[0] = (dataEvent[nOffset+1] - zeroPointVoltage)/voltageResolution;
 				data[1] = dataEvent[nOffset+1];
 				break;
 		}
 		dEvent.setNumbSamples(1);
 	}else{
 		int  	chPerSample = e.dataDesc.getChPerSample();
 		int	dataIndex = 0;
 		dEvent.setTime(t0);
 		for(int i = 0; i < ndata; i+=chPerSample){
 			switch(outputMode){
 				case CURRENT_OUT:
 					data[dataIndex] = (dataEvent[nOffset+i] - zeroPointCurrent)/currentResolution;
 //					System.out.println("amper "+data[0]);
 					break;
 				case VOLTAGE_OUT:
 					data[dataIndex] = (dataEvent[nOffset+i +1] - zeroPointVoltage)/voltageResolution;
 //					System.out.println("voltage "+data[0]);
 					break;
 				case WATT_OUT:
 				case ENERGY_OUT:
 					float		amper = (dataEvent[nOffset+i] - zeroPointCurrent)/currentResolution;
 					float		voltage = (dataEvent[nOffset+i +1] - zeroPointVoltage)/voltageResolution;
					data[dataIndex] = amper*voltage;
					if(data[dataIndex] < 0f){
					    data[dataIndex] = -data[dataIndex];
					}
 					if(outputMode == ENERGY_OUT){
						energy 	+= data[dataIndex]*dDesc.dt; 
						data[dataIndex] 	= energy;
 					}
 					break;
 			}
 			dataIndex++;
 		}
 		dEvent.setNumbSamples(dataIndex);
 	}
 	notifyDataListeners(dEvent);
 	return true;
     }
     
 	protected void writeInternal(extra.io.DataStream out){
 	}
 	protected void readInternal(extra.io.DataStream in){
 	}
 	
 	public void  calibrationDone(float []row1,float []row2,float []calibrated){
 		if(outputMode != CURRENT_OUT && outputMode != VOLTAGE_OUT) return;
 		if(row1 == null  || calibrated == null) return;
 		float zeroPoint = (calibrated[0]*row1[1] - calibrated[1]*row1[0])/(calibrated[0] - calibrated[1]);
 		float resolution = (row1[0] - row1[1])/(calibrated[0] - calibrated[1]);
 		
 		if(outputMode == CURRENT_OUT){
 			zeroPointCurrent 		= zeroPoint;
 			currentResolution 		= resolution;
 			if(calibrationDesc != null){
 				CalibrationParam p = calibrationDesc.getCalibrationParam(0);
 				if(p != null) p.setValue(zeroPointCurrent);
 				p = calibrationDesc.getCalibrationParam(1);
 				if(p != null) p.setValue(currentResolution);
 			}
 		}else if(outputMode == VOLTAGE_OUT){
 			zeroPointVoltage 		= zeroPoint;
 			voltageResolution 		= resolution;
 			if(calibrationDesc != null){
 				CalibrationParam p = calibrationDesc.getCalibrationParam(2);
 				if(p != null) p.setValue(zeroPointVoltage);
 				p = calibrationDesc.getCalibrationParam(3);
 				if(p != null) p.setValue(voltageResolution);
 			}
 		}
 	}
 	public void calibrationDescReady(){
 		if(calibrationDesc == null) return;
 		CalibrationParam p = calibrationDesc.getCalibrationParam(0);
 		if(p != null && p.isValid()){
 			zeroPointCurrent = p.getValue();
 		}
 		p = calibrationDesc.getCalibrationParam(1);
 		if(p != null && p.isValid()){
 			currentResolution = p.getValue();
 		}
 		p = calibrationDesc.getCalibrationParam(2);
 		if(p != null && p.isValid()){
 			zeroPointVoltage = p.getValue();
 		}
 		p = calibrationDesc.getCalibrationParam(3);
 		if(p != null && p.isValid()){
 			voltageResolution = p.getValue();
 		}
 	}
 
 }
