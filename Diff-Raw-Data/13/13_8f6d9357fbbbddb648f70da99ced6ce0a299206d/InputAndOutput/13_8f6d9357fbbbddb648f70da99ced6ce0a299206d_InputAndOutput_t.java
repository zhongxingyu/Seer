 package com.luugiathuy.apps.remotebluetooth;
 
import java.io.Serializable;
 

public class InputAndOutput implements Serializable
 {
         double[] _InputPoint;
         double[] _AlgorithmLabel;
         double[] _Size;
         double[] _Refer= new double [5];
         
         public InputAndOutput(double[] inputpoint, double[] algorithm_labels)
         {
             _InputPoint = inputpoint;
             _AlgorithmLabel = algorithm_labels;
         }
         public InputAndOutput(double[] inputpoint, double[] algorithm_labels, double [] _Size)
         {
             _InputPoint = inputpoint;
             _AlgorithmLabel = algorithm_labels;
             _Size=_Size;
             Calculate_Refer(inputpoint, _Size);
         }
         
         
         public double[] getinputpoint()
         {
                 return _InputPoint;
         }
         
         public double[] getalgorithmlabels ()
         {
                 return _AlgorithmLabel;
         }
         
         //get_index of the 
         public int getindex(){
         	int len=_AlgorithmLabel.length;
         	for (int i=0;i<len;i++){
         		if (_AlgorithmLabel[i]==1)
         			return i;
         	}
         		return -1;
         }
         /**
          * Calculate refer so we can check if another input suit for this one or not. 
          * (used when we do prediction.)
          * qiao 26, August,2012
          * @param inputpoint
          * @param time
          */
         public void  Calculate_Refer(double [] inputpoint, double[] size)
         {
         	//first I will get the direction vector.
         	
         	_Refer[0]=inputpoint[28];
         	_Refer[1]=inputpoint[29];
         	_Refer[2]=inputpoint[30];
         	
         	//time
         	_Refer[3]=size[0];
         	//accelerate
         	_Refer[4]=size[1];
         }
         
         /**
          * compare time and direction (gyroscope..)
          * 
          * @param newinput
          * @return
          */
         public boolean Compare( double [] newinput ){
         	//time 
         	if( newinput[3]>2*_Refer[3] || newinput[3]<_Refer[3]/1.5){
         		System.out.println("Time erro...newinput: "+newinput[3]+"\nTime erro...refer: "+_Refer[3]);
         		return false;
         	}
         	System.out.println("Time  newinput: "+newinput[3]+"\nTime  refer: "+_Refer[3]);
         	
         	
         	
         	System.out.println("Acc  newinput: "+newinput[4]+"\nAcc  refer: "+_Refer[4]);
         	
         	
         	//angle
         	double gyro_new_pow = Math.sqrt(Math.pow(newinput[0], 2) + Math.pow(newinput[1], 2) + Math.pow(newinput[2], 2));
         	double gyro__Refer_pow = Math.sqrt(Math.pow(_Refer[0], 2) + Math.pow(_Refer[1], 2) + Math.pow(_Refer[2], 2));
         	
         	//calculate the direction angle of movement here. cos ?= a.b/|a||b|
         	double result= (newinput[0]*_Refer[0]+newinput[1]*_Refer[1]+newinput[2]*_Refer[2]) /(gyro_new_pow*gyro__Refer_pow );
         	if (result < 0.95){
         		System.out.println("Angle erro... "+result);
         		for (int i=0;i<3;i++){
             		System.out.println("new "+newinput[i]+ " refer"+_Refer[i]);
             	}
         		return false;
         	}
         	System.out.println("Angle... "+result);
         	for (int i=0;i<3;i++){
         		System.out.println("new "+newinput[i]+ " refer"+_Refer[i]);
         	}
         	return true;
         }
 }
