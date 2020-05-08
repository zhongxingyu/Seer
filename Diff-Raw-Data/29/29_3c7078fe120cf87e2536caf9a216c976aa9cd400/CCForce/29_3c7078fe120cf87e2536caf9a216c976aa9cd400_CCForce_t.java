 package org.concord.waba.extra.probware.probs;
 import org.concord.waba.extra.event.DataListener;
 import org.concord.waba.extra.event.DataEvent;
 import extra.util.DataDesc;
 import org.concord.waba.extra.probware.*;
 import org.concord.waba.extra.event.*;
 import extra.util.*;
 
 public class CCForce extends CCProb{
 float  			[]forceData = new float[CCInterfaceManager.BUF_SIZE/2];
 int  			[]forceIntData = new int[CCInterfaceManager.BUF_SIZE];
 
 	public final static String [] modeNames = {"End of Arm", "Middle of Arm"};
 	public final static String [] range1Names = {"+/- 2N", "+/- 20N"};
 	public final static String [] range2Names = {"+/- 20N", "+/- 200N"};
 	public final static String [] speed1Names = {3 + speedUnit, 200 + speedUnit, 400 + speedUnit};
 	public final static String [] speed2Names = {3 + speedUnit, 200 + speedUnit};
 
 	public int curChannel = 1;
 	float	A = 0.01734f;
 	float B = -25.31f;
 
 	PropObject modeProp = new PropObject("Mode", "Mode", PROP_MODE, modeNames);
 	PropObject rangeProp = new PropObject("Range", "Range", PROP_RANGE, range1Names, 1);
 	PropObject speedProp = new PropObject("Speed", "Speed", PROP_SPEED, speed1Names);
 
 	protected CCForce(boolean init, String name, int interfaceT){
 		super(init, name, interfaceT);
 		probeType = ProbFactory.Prob_Force;
 	    activeChannels = 1;
 		defQuantityName = "Force";
 		
 		dDesc.setChPerSample(1);
 		dDesc.setDt(0.0f);
 		dEvent.setDataDesc(dDesc);
 		dEvent.setDataOffset(0);
 		dEvent.setData(forceData);
 		dEvent.setIntData(forceIntData);
 
 		addProperty(modeProp);
 		addProperty(rangeProp);
 		addProperty(speedProp);
 
 		if(init){	
 			calibrationDesc = new CalibrationDesc();
 			calibrationDesc.addCalibrationParam(new CalibrationParam(0,A));
 			calibrationDesc.addCalibrationParam(new CalibrationParam(1,B));
 		}
 		unit = CCUnit.UNIT_CODE_NEWTON;		
 	}
 
 	boolean zeroing = false;
 	int zeroCount = 0;
 	float zeroSum = 0f;
 	int zeroOrigSpeed = 0;
 	public void startZero()
 	{
 		zeroing = true;
 		zeroCount = 0;
 		zeroSum = 0f;
 		zeroOrigSpeed = speedProp.getIndex();
 		speedProp.setIndex(0);
 	}
 
 	public void setDataDescParam(int chPerSample,float dt){
 		dDesc.setDt(dt);
 		dDesc.setChPerSample(chPerSample);
 	}
 
 	public boolean visValueChanged(PropObject po)
 	{
 		int index = po.getVisIndex();
 		if(po == rangeProp){
 			if(index == 0){
 				speedProp.setVisPossibleValues(speed2Names);
 			} else if(index == 1){
 				speedProp.setVisPossibleValues(speed1Names);
 			}
 		} else if(po == modeProp){
 			if(index == 0){
 				rangeProp.setVisPossibleValues(range1Names);
 			} else if(index == 1){
 				rangeProp.setVisPossibleValues(range2Names);
 			}
 		} 
 		return true;
 	}
 
 	public int getInterfaceMode()
 	{
 		int rangeIndex = rangeProp.getIndex();
 		int speedIndex = speedProp.getIndex();
 
 		curChannel = 1-rangeIndex;
 
 		if(speedIndex == 0){
 			interfaceMode = CCInterfaceManager.A2D_24_MODE;
 			activeChannels = 2;
 		} else if(speedIndex == 1){
 			interfaceMode = CCInterfaceManager.A2D_10_MODE;
 			activeChannels = 2;
 		} else if(speedIndex == 2){
 			interfaceMode = CCInterfaceManager.A2D_10_MODE;
 			activeChannels = 1;
 		}
 		return interfaceMode;
 	}
 
 	int chPerSample = 2;
 	int channelOffset = 0;
 
 	public boolean startSampling(DataEvent e){
 		dEvent.type = e.type;
 		dDesc.setDt(e.getDataDesc().getDt());
 		chPerSample = e.getDataDesc().getChPerSample();
 		dDesc.setTuneValue(e.getDataDesc().getTuneValue());
 		if(calibrationListener != null){
 			if(activeChannels == 2)
 				dDesc.setChPerSample(3);
 			else
 				dDesc.setChPerSample(2);
 		}else{
 			dDesc.setChPerSample(1);
 		}
 		dDesc.setIntChPerSample(1);
 		channelOffset = curChannel;
 		if(curChannel > activeChannels - 1) channelOffset = activeChannels - 1;
 
 		if(!zeroing) return super.startSampling(dEvent);
 		return true;
     }
 
 	public final static int ZEROING_DONE = 0;
 	public final static int ZEROING_NUM_POINTS = 4;
 
 	public boolean idle(DataEvent e){
 		if(!zeroing) return super.idle(e);
 		return true;
 	}
 
 	public boolean dataArrived(DataEvent e){
 		dEvent.type = e.type;
 		dEvent.intTime = e.intTime;
		float v = e.getDataDesc().tuneValue;
 		if(zeroing){
			int ndata = e.numbSamples*e.dataDesc.chPerSample;
 			int dOff = e.dataOffset;
 			int data [] = e.intData;

			// notice there is a hack in here to skip the first point
			// it seems to be screwed up some how
 			for(int i = 0; i < ndata; i+= chPerSample){
				if(zeroCount > 0){
					zeroSum += (float)(data[dOff + i+channelOffset]);
				}
 				zeroCount++;
				if(zeroCount > ZEROING_NUM_POINTS + 1){
 					notifyProbListeners(new ProbEvent(this, ZEROING_DONE, null));
					B = -A*v*(zeroSum/(float)(zeroCount-1));
 					if(calibrationDesc != null){
 						CalibrationParam p = calibrationDesc.getCalibrationParam(1);
 						if(p != null) p.setValue(B);
 					}
 					speedProp.setIndex(zeroOrigSpeed);
 					zeroing = false;
					break;
 				}
 			}
 			return true;
 		} else if(calibrationListener != null){
 			dEvent.numbSamples = 1;
 			forceData[0] = A*e.intData[e.dataOffset+channelOffset]*v+B;
 			if(activeChannels == 2){
 				forceData[1] = e.intData[e.dataOffset]*v;
 				forceData[2] = e.intData[e.dataOffset+1]*v;
 			}else{
 				forceData[1] = e.intData[e.dataOffset+channelOffset]*v;
 				forceData[2]  = 0f;
 			}
 		}else{
 			dEvent.intTime = e.intTime;
 			dEvent.numbSamples = e.numbSamples;
 			dEvent.pTimes = e.pTimes;
 			dEvent.numPTimes = e.numPTimes;
 			int ndata = dEvent.numbSamples*e.dataDesc.chPerSample;
 			int dOff = e.dataOffset;
 			int data [] = e.intData;
 			int currPos = 0;
 			for(int i = 0; i < ndata; i+= chPerSample){
 				forceIntData[currPos] = data[dOff + i+channelOffset];
 				forceData[currPos] = A*forceIntData[currPos]*v+B;
 				currPos++;
 			}
 		}
 		return super.dataArrived(dEvent);
 	}
 
 	public void  calibrationDone(float []row1,float []row2,float []calibrated){
 		if(row1 == null || calibrated == null) return;
 		float x1, x2;
 		if(curChannel ==0){
 		    x1 = row1[0];
 		    x2 = row1[1];
 		} else {
 		    x1 = row2[0];
 		    x2 = row2[1];
 		}
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
