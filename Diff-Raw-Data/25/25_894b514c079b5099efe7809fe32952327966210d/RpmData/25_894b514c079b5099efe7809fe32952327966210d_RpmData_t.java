 package com.abstracttech.ichiban.data;
 
 public class RpmData extends StatisticData {
 	@Override
 	public void update() {
 		float d = Data.getRpm();

 		if(d>cMax)
 			cMax=d;
		if(d<cMin && d>0)
 			cMin=d;

 		//averaging
 		n++;
 		total+=d;
 	}

	@Override
	public float getMin(){
		if(cMin==Integer.MAX_VALUE)
			return 0;
		else
			return cMin;
	}

	@Override
	public float getMax(){
		if(cMax==Integer.MIN_VALUE)
			return 0;
		else
			return cMax;
	}
 }
