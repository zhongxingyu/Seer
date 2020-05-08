 package com.abstracttech.ichiban.data;
 
 public class RpmData extends StatisticData {
 	@Override
 	public void update() {
 		float d = Data.getRpm();
		
 		if(d>cMax)
 			cMax=d;
		if(d<cMin)
 			cMin=d;
		
 		//averaging
 		n++;
 		total+=d;
 	}
 }
