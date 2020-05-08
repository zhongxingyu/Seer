 package org.concord.ProbeLib.probes;
 
 import org.concord.ProbeLib.*;
 import org.concord.waba.extra.util.*;
 
 public class CCVoltCurrent extends Probe
 {
 	float  			[]data = new float[CCInterfaceManager.BUF_SIZE/2];
 	int  			[]intData = new int[CCInterfaceManager.BUF_SIZE];
 	float  			dtChannel = 0.0f;
 	float				energy = 0.0f;
 	public final static int		CURRENT_OUT 			= 0;
 	public final static int		VOLTAGE_OUT 			= 1;
 	public final static int		POWER_OUT 			= 2;
 	public final static int		ENERGY_OUT 			= 3;
 
 	//	PropObject modeProp = new PropObject("Mode", "Mode", PROP_MODE, modeNames, 1);
 	PropObject rangeProp = new PropObject("Range", "Range", PROP_RANGE, rangeNames);
 	PropObject speedProp = new PropObject("Speed", "Speed", PROP_SPEED, speedNames);
 	PropObject versionProp = new PropObject("Version", "Version", PROP_VERSION, versionNames);
 
 	/*
 	    Voltage=(input(mV)-offset(mV))/sensitivity(mV/Volt)
 		                    max	    min	    standard	maximum	
 	    offset	sensitivity	range	range	deviation	deviation	range
 		1252.7	63.13	    19.76	-19.84	4.5%	     7.4%	    "+/- 20V"
 
 		Current=(input(mV)-offset(mV))/sensitivity(mV/Amp)
 		                    max	    min	    standard	maximum	    maximum
 		offset	sensitivity	range	range	deviation	deviation	range
 		1247.1	620.95	    2.02	-2.01	4.1%	    10.2%       "+/- 2A"
 	 */
 
	float					zeroPointCurrent				= 1252.7f;//	
 	float					zeroPointVoltage				= 1247.1f;//	
 	//	float					currentResolution		= 271f; //       mV(reading)/A
 	//  float					voltageResolution		= 38f; //     mV(reading)/(true)V
 	float					currentResolution		= 620.95f; //       mV(reading)/A
 	float					voltageResolution		= 63.13f; //     mV(reading)/(true)V
 
 	int					outputMode 			= VOLTAGE_OUT;
 	public static String [] modeNames = {"Current", "Voltage","Power","Energy"};
 	public static String [] rangeNames = {"unknown"};
 	public static String [] speedNames = {3 + speedUnit, 200 + speedUnit};
 	public static String [] versionNames = {"1.0", "2.0"};
    
 	int 				curChannel = 0;
 
 	int version = 1;
 	int voltOff = 1;
 	int currentOff = 0;
 
 	CCVoltCurrent(boolean init, String name, int interfaceT){
 		super(init, name, interfaceT);
 		probeType = ProbFactory.Prob_VoltCurrent;
 		activeChannels = 2;
 		quantityNames = modeNames;
 		defQuantityName = "Current";
 
 		dDesc.setChPerSample(1);
 		dDesc.setDt(0.0f);
 		dEvent.setDataDesc(dDesc);
 		dEvent.setDataOffset(0);
 		dEvent.setNumbSamples(1);
 		dEvent.setData(data);
 		dEvent.setIntData(intData);
 
 		// addProperty(modeProp);
 		addProperty(rangeProp);
 		addProperty(speedProp);
 		addProperty(versionProp);
 
 		if(init){		
 			calibrationDesc = new CalibrationDesc();
 			calibrationDesc.addCalibrationParam(new CalibrationParam(0,zeroPointCurrent));
 			calibrationDesc.addCalibrationParam(new CalibrationParam(1,currentResolution));
 			calibrationDesc.addCalibrationParam(new CalibrationParam(2,zeroPointVoltage));
 			calibrationDesc.addCalibrationParam(new CalibrationParam(3,voltageResolution));
 		}
 	}
 
 	DataListener voltListener = null;
 	DataListener powerListener = null;
 	DataListener energyListener = null;
 	public DataListener setModeDataListener(DataListener l, int mode)
 	{
 		DataListener old = null;
 
 		switch(mode){
 		case 1:
 			old = voltListener;
 			voltListener = l;
 			break;
 		case 2:
 			old = powerListener;
 			powerListener = l;
 			break;
 		case 3:
 			old = energyListener;
 			energyListener = l;
 			break;
 		}
 		return old;
 	}
 
 	public int getQuantityUnit(int mode)
 	{
 		int unit = -1;
 		switch(mode){
 		case 1:
 			unit = CCUnit.UNIT_CODE_VOLT;
 			break;
 		case 2:
 			unit = CCUnit.UNIT_CODE_WATT;
 			break;
 		case 3:
 			unit = CCUnit.UNIT_CODE_JOULE;
 			break;
 		}
 		return unit;
 	}
 
 
 	public int getUnit()
 	{
 		//		int outputMode = modeProp.getIndex();
 		int outputMode = CURRENT_OUT;
 
 		switch(outputMode){
 		case CURRENT_OUT:
 			unit = CCUnit.UNIT_CODE_AMPERE;
 			break;
 		case VOLTAGE_OUT:
 			unit = CCUnit.UNIT_CODE_VOLT;
 			break;
 		case POWER_OUT:
 			unit = CCUnit.UNIT_CODE_WATT;
 			break;
 		case ENERGY_OUT:
 			unit = CCUnit.UNIT_CODE_JOULE;
 			break;
 		}
 
 		return unit;
 	}
 
 	public CalibrationDesc getCalibrationDesc()
 	{
 		CalibrationParam cp = calibrationDesc.getCalibrationParam(0);
 		if(cp != null) cp.setAvailable(outputMode == CURRENT_OUT);
 		cp = calibrationDesc.getCalibrationParam(1);
 		if(cp != null) cp.setAvailable(outputMode == CURRENT_OUT);
 		cp = calibrationDesc.getCalibrationParam(2);
 		if(cp != null) cp.setAvailable(outputMode == VOLTAGE_OUT);
 		cp = calibrationDesc.getCalibrationParam(3);
 		if(cp != null) cp.setAvailable(outputMode == VOLTAGE_OUT);
 
 		return calibrationDesc;
 	}
 
 	public int  getActiveCalibrationChannels(){return 1;}
 	public void setDataDescParam(int chPerSample,float dt){
 		dDesc.setDt(dt);
 		dDesc.setChPerSample(chPerSample);
 		dtChannel = dt / (float)chPerSample;
 	}
 
 	public boolean visValueChanged(PropObject po)
 	{
 		/*
 		if(po == modeProp || po == versionProp){
 			int mIndex = modeProp.getVisIndex();
 			int vIndex = versionProp.getVisIndex();
 			   
 			if((mIndex == 0 && vIndex == 0)  ||
 			   (mIndex == 1 && vIndex == 1)){
 				speedProp.setVisPossibleValues(speed1Names);
 			} else {
 				speedProp.setVisPossibleValues(speed2Names);
 			}
 		}
 		*/
 
 		return true;
 	}
 
 	public int getInterfaceMode()
 	{
 		//		outputMode = modeProp.getIndex();
 		outputMode = CURRENT_OUT;
 		int vIndex = versionProp.getIndex();
 		if(vIndex == 0){
 			version = 1;
 			voltOff = 1;
 			currentOff = 0;
 		}else {
 			version = 2;
 			voltOff = 0;
 			currentOff = 1;
 		}
 
 		int speedIndex = speedProp.getIndex();
 		if(speedIndex == 0){
 			interfaceMode = CCInterfaceManager.A2D_24_MODE;
 			activeChannels = 2;
 		} else if(speedIndex == 1){
 			interfaceMode = CCInterfaceManager.A2D_10_MODE;
 			activeChannels = 2;
 		} 
 		return interfaceMode;
 	}
 	
 	public boolean startSampling(DataEvent e)
 	{
 		energy = 0.0f;
 		dEvent.type = e.type;
 		dDesc.setDt(e.getDataDesc().getDt());
 		dEvent.setNumbSamples(1);
 		dDesc.setTuneValue(e.getDataDesc().getTuneValue());
 		if(calibrationListener != null){
 			dDesc.setChPerSample(2);
 		}else{
 			dDesc.setChPerSample(1);
 		}
 		dDesc.setIntChPerSample(2);
 		dtChannel = dDesc.getDt() / (float)dDesc.getChPerSample();
 		return super.startSampling(dEvent);
 	}
 
 	public void notifyDataListenersEvent(DataEvent e)
 	{
 		if(voltListener != null){
 			voltListener.dataStreamEvent(e);
 		}
 		if(powerListener != null){
 			powerListener.dataStreamEvent(e);
 		}
 		if(energyListener != null){
 			energyListener.dataStreamEvent(e);
 		}
 
 		super.notifyDataListenersEvent(e);
 	}
     	
 
     public boolean dataArrived(DataEvent e)
     {
 		dEvent.type 		= e.type;
 		int nOffset 		= e.getDataOffset();
 		int ndata 			= e.getNumbSamples()*e.dataDesc.getChPerSample();
 		float t0 			= e.getTime();
 		int[] dataEvent 	= e.getIntData();
 		if(calibrationListener != null){
 			float v = 0.0f;
 			switch(outputMode){
 			case CURRENT_OUT:
 				v = (float)dataEvent[nOffset + currentOff]*dDesc.tuneValue;
 				data[0] = (v - zeroPointCurrent)/currentResolution;
 				data[1] = v;
 				break;
 			case VOLTAGE_OUT:
 				v = (float)dataEvent[nOffset + voltOff]*dDesc.tuneValue;
 				data[0] = (v - zeroPointVoltage)/voltageResolution;
 				data[1] = v;
 				break;
 			}
 			dEvent.setNumbSamples(1);
 			return true;
 		}else{
 			int  	chPerSample = e.dataDesc.chPerSample;
 			int	dataIndex;
 			dEvent.intTime = e.intTime;
 
 			boolean ret = true;
 			if(dataListeners != null){
 				dataIndex = 0;
 				// this is current
 				for(int i = 0; i < ndata; i+=chPerSample){
 					intData[i] = dataEvent[nOffset+i];
 					if(chPerSample == 2){
 						intData[i+1] = dataEvent[nOffset+i+1];
 					}
 					data[dataIndex] = (intData[i+currentOff]*dDesc.tuneValue - zeroPointCurrent)/currentResolution;
 					dataIndex++;
 				}
 				dEvent.setNumbSamples(dataIndex);
 				ret = super.dataArrived(dEvent);
 			}
 
 			if(voltListener != null){
 				dataIndex = 0;
 				for(int i = 0; i < ndata; i+=chPerSample){
 					intData[i] = dataEvent[nOffset+i];
 					if(chPerSample == 2){
 						intData[i+1] = dataEvent[nOffset+i+1];
 					}
 					data[dataIndex] = (intData[i+voltOff]*dDesc.tuneValue - zeroPointVoltage)/voltageResolution;
 					dataIndex++;
 				}
 				dEvent.setNumbSamples(dataIndex);
 				voltListener.dataReceived(dEvent);
 			}
 
 			if(powerListener != null){
 				dataIndex = 0;
 				for(int i = 0; i < ndata; i+=chPerSample){
 					intData[i] = dataEvent[nOffset+i];
 					if(chPerSample == 2){
 						intData[i+1] = dataEvent[nOffset+i+1];
 					}
 					float		amper = (intData[i+currentOff]*dDesc.tuneValue - zeroPointCurrent)/currentResolution;
 					float		voltage = (intData[i+voltOff]*dDesc.tuneValue - zeroPointVoltage)/voltageResolution;
 					data[dataIndex] = amper*voltage;
 					if(data[dataIndex] < 0f){
 					    data[dataIndex] = -data[dataIndex];
 					}
 					dataIndex++;
 				}
 				dEvent.setNumbSamples(dataIndex);
 				powerListener.dataReceived(dEvent);
 			}
 
 			if(energyListener != null){
 				dataIndex = 0;
 				for(int i = 0; i < ndata; i+=chPerSample){
 					intData[i] = dataEvent[nOffset+i];
 					if(chPerSample == 2){
 						intData[i+1] = dataEvent[nOffset+i+1];
 					}
 					float		amper = (intData[i+currentOff]*dDesc.tuneValue - zeroPointCurrent)/currentResolution;
 					float		voltage = (intData[i+voltOff]*dDesc.tuneValue - zeroPointVoltage)/voltageResolution;
 					data[dataIndex] = amper*voltage;
 					if(data[dataIndex] < 0f){
 					    data[dataIndex] = -data[dataIndex];
 					}
 					energy 	+= data[dataIndex]*dDesc.dt; 
 					data[dataIndex] 	= energy;
 					dataIndex++;
 				}
 				dEvent.setNumbSamples(dataIndex);
 				energyListener.dataReceived(dEvent);
 			}
 
 			return ret;
 		}
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
