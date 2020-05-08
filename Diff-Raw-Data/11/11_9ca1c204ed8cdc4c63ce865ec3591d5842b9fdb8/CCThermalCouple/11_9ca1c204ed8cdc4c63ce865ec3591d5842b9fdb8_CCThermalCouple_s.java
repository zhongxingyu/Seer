 package org.concord.waba.extra.probware.probs;
 import org.concord.waba.extra.event.DataListener;
 import org.concord.waba.extra.event.DataEvent;
 import extra.util.*;
 import org.concord.waba.extra.probware.*;
 
 
 public class CCThermalCouple extends CCProb{
 float  			[]tempData = new float[3];
 float  			dtChannel = 0.0f;
 public final static int		CELSIUS_TEMP_OUT = 1;
 public final static int		FAHRENHEIT_TEMP_OUT = 2;
 public final static int		KELVIN_TEMP_OUT = 3;
 public final static int		DEFAULT_TEMP_OUT = CELSIUS_TEMP_OUT;
 int				outputMode = DEFAULT_TEMP_OUT;
 public final static String	tempModeString = "Output Mode";
 public final static String	[]tempModes =  {defaultModeName,"C","F","K"};
 float AC = 17.084f;
 float BC = -0.25863f;
 float CC = 0.011012f;
 float DC = 10f;
 float EC = -50f;
 float FC = 0.0f;
 	private boolean fromConstructor = true;
 	CCThermalCouple(){
 		this("unknown");
 	}
 	CCThermalCouple(String name){
 		activeChannels = 2;
 		setName(name);
 		dDesc.setChPerSample(2);
 		dDesc.setDt(0.0f);
 		dEvent.setDataDesc(dDesc);
 		dEvent.setDataOffset(0);
 		dEvent.setNumbSamples(1);
 		dEvent.setData(tempData);
 		properties = new PropObject[2];
 //		samplingModes[1] = null;
 
 		properties[0] = new PropObject(samplingModeString,samplingModes); 
 		properties[1] = new PropObject(tempModeString,tempModes); 
 		setPropertyValue(0,samplingModes[CCProb.SAMPLING_24BIT_MODE]);
 		setPropertyValue(1,tempModes[CELSIUS_TEMP_OUT]);
 		
 		outputMode = DEFAULT_TEMP_OUT;
 		calibrationDesc = new CalibrationDesc();
 		calibrationDesc.addCalibrationParam(new CalibrationParam(0,FC));
 //		calibrationDesc.addCalibrationParam(new CalibrationParam(1,EC));
 
 		fromConstructor = false;
 		unit = CCUnit.UNIT_CODE_CELSIUS;
 	}
 	public int	getActiveChannels(){return 2;}
 	public void setPropertyValue(String nameProperty,String value){
 		if(!fromConstructor && (nameProperty.equals(samplingModeString))){
 			return;
 		}
 		super.setPropertyValue(nameProperty,value);
 		if(nameProperty == null || value == null) return;
 		if(nameProperty.equals(tempModeString)){
 			outputMode = DEFAULT_TEMP_OUT;
 			for(int i = 1; i < tempModes.length;i++){
 				if(tempModes[i].equals(value)){
 					outputMode = i;
 					break;
 				}
 			}
 			
 			switch(outputMode){
 				case FAHRENHEIT_TEMP_OUT:
 					unit = CCUnit.UNIT_CODE_FAHRENHEIT;
 					break;
 				case KELVIN_TEMP_OUT:
 					unit = CCUnit.UNIT_CODE_KELVIN;
 					break;
 				default:
 				case CELSIUS_TEMP_OUT:
 					unit = CCUnit.UNIT_CODE_CELSIUS;
 					break;
 			}
 		}
 	}
 	public void setPropertyValue(int index,String value){
 		if(!fromConstructor && (index == 0)){
 			return;
 		}
 		super.setPropertyValue(index,value);
 		if(index == 1){
 			outputMode = DEFAULT_TEMP_OUT;
 			for(int i = 1; i < tempModes.length;i++){
 				if(tempModes[i].equals(value)){
 					outputMode = i;
 					break;
 				}
 			}
 			switch(outputMode){
 				case FAHRENHEIT_TEMP_OUT:
 					unit = CCUnit.UNIT_CODE_FAHRENHEIT;
 					break;
 				case KELVIN_TEMP_OUT:
 					unit = CCUnit.UNIT_CODE_KELVIN;
 					break;
 				default:
 				case CELSIUS_TEMP_OUT:
 					unit = CCUnit.UNIT_CODE_CELSIUS;
 					break;
 			}
 		}
 	}
 	
 	public void setDataDescParam(int chPerSample,float dt){
 		dDesc.setDt(dt);
 		dDesc.setChPerSample(chPerSample);
 		dtChannel = dt / (float)chPerSample;
 	}
 	public boolean transform(DataEvent e){
		if(e.getType() != DataEvent.DATA_RECEIVED){
			notifyDataListeners(dEvent);
			return true;
		}
 		float t0 = e.getTime();
 		float[] data = e.getData();
 		int nOffset = e.getDataOffset();
 		dDesc.setDt(e.getDataDesc().getDt());
 		dDesc.setChPerSample(e.getDataDesc().getChPerSample());
 		int ndata = e.getNumbSamples()*dDesc.getChPerSample();
 		dtChannel = dDesc.getDt() / (float)dDesc.getChPerSample();
 		int  	chPerSample = dDesc.getChPerSample();
 		if(ndata < chPerSample) return false;
 		if(calibrationListener != null){
 			dDesc.setChPerSample(3);
 		}else{
 			dDesc.setChPerSample(1);
 		}
 		dEvent.setNumbSamples(1);
 		
 		for(int i = 0; i < ndata; i+=chPerSample){
 			dEvent.setTime(t0 + dtChannel*(float)i);
 			float ch1 = data[nOffset+i];
 			float ch2 = data[nOffset+i+1];
 			float lastColdJunct = (ch2 / DC) + EC;
 			float mV = ch1;
 			float mV2 = mV * mV;
 			float mV3 = mV2 * mV;
 			tempData[0] = mV * AC + mV2 * BC + mV3 * CC + lastColdJunct;
 			tempData[0] += FC;
 			switch(outputMode){
 				case FAHRENHEIT_TEMP_OUT:
 					tempData[0] = tempData[0]*1.8f + 32f;
 					break;
 				case KELVIN_TEMP_OUT:
 					tempData[0] += 273.15f;
 					break;
 				default:
 					break;
 			}
 			if(calibrationListener != null){
 				tempData[1]  = ch1;
 				tempData[2]  = ch2;
 			}
 			notifyDataListeners(dEvent);
 		}
 		return true;
 	}
 	public void  calibrationDone(float []row1,float []row2,float []calibrated){
 		if(row1 == null || calibrated == null) return;
 		float ch1 = row1[0];
 		float ch2 = row2[0];
 		float lastColdJunct = (ch2 / DC) + EC;
 		float mV = ch1;
 		float mV2 = mV * mV;
 		float mV3 = mV2 * mV;
 		float trueValue = mV * AC + mV2 * BC + mV3 * CC + lastColdJunct;
 		float userValue = calibrated[0];
 		switch(outputMode){
 			case FAHRENHEIT_TEMP_OUT:
 				userValue = (userValue - 32f)/1.8f;
 				break;
 			case KELVIN_TEMP_OUT:
 				userValue -= 273.15f;
 				break;
 			default:
 				break;
 		}
 		FC = userValue - trueValue;
 	}
 }
