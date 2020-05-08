 package org.concord.waba.extra.event;
 import extra.util.DataDesc;
 
 public class DataEvent extends waba.ui.Event{
 public static final int DATA_RECEIVED 		= 1000;
 public static final int DATA_COLLECTING 	= 1001;
 public static final int DATA_READY_TO_START = 1002;
 public float 	[]data = null;
 public DataDesc	dataDesc = null;
 public int		numbSamples = 1;
 public int		dataOffset = 0;
 public float	deltaT;
 public int		intTime;
 public int	 	[]intData = null;
 
     public int [] pTimes = new int [10];
     public int numPTimes = 0;
 
 	public DataEvent(){
 		this(0);
 	}
 	public DataEvent(int intTime){
 		this(DATA_RECEIVED,intTime,null,null,null);
 	}
 	public DataEvent(int type,int time){
 		this(type,0,null,null,null);
 	}
 	public DataEvent(int type,int intTime, float[] data,DataDesc dataDesc){
 		this(type,intTime,data,null,dataDesc);
 	}
 	
 	public DataEvent(int type,int intTime, float[] data,int[] intData,DataDesc dataDesc){
 		this.intTime 	= intTime;
 		this.type 		= type;
 		this.data 		= data;
 		this.intData 	= intData;
 		this.dataDesc 	= dataDesc;
 	}
 	
 	public void setData(float[] data){this.data = data;}
 	public void setIntData(int[] intData){this.intData = intData;}
 	public void setDataDesc(DataDesc dataDesc){ this.dataDesc = dataDesc;}
 	public void setType(int type){this.type = type;}
 	public void setIntTime(int intTime){this.intTime = intTime;}
 	public void setNumbSamples(int numbSamples){this.numbSamples = numbSamples;}
 	public void setDataOffset(int dataOffset){this.dataOffset = dataOffset;}
 	
 	public float[] getData(){return data;}
 	public int[] getIntData(){return intData;}
 	public DataDesc getDataDesc(){return dataDesc;}
 	public int getType(){return type;}
 	public int getIntTime(){return intTime;}
 	public int getNumbSamples(){return numbSamples;}
 	public int getDataOffset(){return dataOffset;}
 	public float getDeltaT(){return (dataDesc == null)?0.0f:dataDesc.getDt();}
 	public float getTime(){return (float)getIntTime()*getDeltaT();}//for backward compatibility
 	public void  setTime(float val){
		setIntTime((int)(val/getDeltaT()+0.5));
 	}
 	
 	public void 	setTuneValue(float tuneValue){
 		if(dataDesc != null)	dataDesc.setTuneValue(tuneValue);
 	}
 
 	
 }
