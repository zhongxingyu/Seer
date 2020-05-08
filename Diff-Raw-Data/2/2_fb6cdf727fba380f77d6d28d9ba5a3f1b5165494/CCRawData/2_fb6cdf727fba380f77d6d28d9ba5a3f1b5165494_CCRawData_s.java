 package org.concord.waba.extra.probware.probs;
 import org.concord.waba.extra.event.DataListener;
 import org.concord.waba.extra.event.DataEvent;
 import extra.util.DataDesc;
 import org.concord.waba.extra.probware.*;
 import extra.util.*;
 
 public class CCRawData extends CCProb{
 float  			[]rawData = new float[CCInterfaceManager.BUF_SIZE];
 int  			[]rawIntData = new int[CCInterfaceManager.BUF_SIZE];
 int				firstIndex,secondIndex;
 
 
     String [] portNames = {"A", "B"};
     String [] channelNames = {"0", "1"};
     String [] numbChannels = {"1", "2"};
 
     int curChannel = 0;
 
 	CCRawData(){
 		this("unknown");
 	}
 	CCRawData(String name){
 		activeChannels = 2;
 		setName(name);
 		dDesc.setChPerSample(2);
 		dDesc.setIntChPerSample(2);
 		dDesc.setDt(0.0f);
 		dEvent.setDataDesc(dDesc);
 		dEvent.setDataOffset(0);
 		dEvent.setNumbSamples(1);
 		dEvent.setData(rawData);
 		dEvent.setIntData(rawIntData);
 
 		properties = new PropObject[4];
 		properties[0] = new PropObject("Port", portNames);
 		properties[1] = new PropObject("Num Channels", numbChannels);
 		properties[2] = new PropObject("Channel #", channelNames);
 		properties[3] = new PropObject(samplingModeString,samplingModes); 
 		
 		setPropertyValue(0,samplingModes[CCProb.SAMPLING_10BIT_MODE]);
 
 		unit = CCUnit.UNIT_CODE_VOLT;
 	}
 
 	public void setDataDescParam(int chPerSample,float dt){
 		dDesc.setDt(dt);
 		dDesc.setChPerSample(chPerSample);
 		dDesc.setIntChPerSample(chPerSample);
 	}
 
     public boolean idle(org.concord.waba.extra.event.DataEvent e){
 		dEvent.type = e.type;
 		notifyDataListeners(dEvent);
 		return true;
     }
     public boolean startSampling(org.concord.waba.extra.event.DataEvent e){
 		dEvent.type = e.type;
 		dDesc.setDt(e.getDataDesc().getDt());
 		// Change to Volts
		dDesc.setTuneValue(e.getDataDesc().getTuneValue()/100f);
 		if(activeChannels == 2){
 			dDesc.setChPerSample(2);
 			dDesc.setIntChPerSample(2);
 			firstIndex = (curChannel == 1)?1:0;
 			secondIndex = (curChannel == 1)?0:1;
 		}else{
 			dDesc.setChPerSample(1);
 			dDesc.setIntChPerSample(1);
 			firstIndex = secondIndex = 0;
 		}
 		notifyDataListeners(dEvent);
  		return true;
    }
     public boolean dataArrived(DataEvent e)
     {
 		int nOffset 		= e.getDataOffset();
 		int[] data = e.getIntData();
 		int  	chPerSample = e.dataDesc.chPerSample;
 		int		nSamples	= e.getNumbSamples();
 		int 	ndata 		= nSamples*chPerSample;
 		int 	v = 0,v1 = 0;
 		dEvent.type = e.type;
 		dEvent.intTime = e.intTime;
 		for(int i = 0; i < ndata; i+=chPerSample){
 			if(activeChannels == 1){
 				v = data[nOffset+i];
 				rawIntData[i] = v;
 				rawData[i] = (float)v*dDesc.tuneValue;
 			}else{
 				v = data[nOffset+i+firstIndex];
 				v1 = data[nOffset+i+secondIndex];
 				rawIntData[i] = v;
 				rawIntData[i+1] = v1;
 				rawData[i] = (float)v*dDesc.tuneValue;
 				rawData[i+1] = (float)v1*dDesc.tuneValue;
 
 			}
 		}
 		dEvent.setNumbSamples(nSamples);
 		notifyDataListeners(dEvent);
 		return true;
     }
 
 	protected boolean setPValue(PropObject p,String value){
 		if(p == null || value == null) return false;
 		String nameProperty = p.getName();
 		if(nameProperty == null) return false;
 		if(nameProperty.equals("Port")){
 			if(value.equals("A")){
 				interfacePort = INTERFACE_PORT_A;
 			} else if(value.equals("B")){
 				interfacePort = INTERFACE_PORT_B;
 			}
 		} else if(nameProperty.equals("Num Channels")){
 			if(value.equals("1") && curChannel != 1){
 				activeChannels = 1;
 			} else if(value.equals("2")){
 				activeChannels = 2;
 			}		
 		} else if(nameProperty.equals("Channel #")){
 			if(value.equals("0")){
 				curChannel = 0;
 			} else if(value.equals("1")){
 				curChannel = 1;
 				activeChannels = 2;
 			}
 		}
 		return  super.setPValue(p,value);
 	}
 
 }
