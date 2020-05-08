 package edu.wisc.myrotsens;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.util.Log;
 import android.widget.TextView;
 
 public class RotVectMSensor extends MSensor {
 	private float[] curRotMatrix; 
 	private float[] prevRotMatrix;
 	private float[] angleChange;
 	
 	public RotVectMSensor(Context context, Sensor sensor, TextView textViewXVal,
 			TextView textViewYVal, TextView textViewZVal,
 			TextView textViewAccuVal) {
 		super(context, sensor, textViewXVal, textViewYVal, textViewZVal,
 				textViewAccuVal);
 		this.curRotMatrix=new float[9];
 		this.prevRotMatrix=new float[9];
 		//initialize rotation matrix
 		this.prevRotMatrix[0]=1;
 		this.prevRotMatrix[4]=1;
 		this.prevRotMatrix[8]=1;
 		this.angleChange=new float[3];
 	}
 	
 	/**
 	 * convert rotation vector to rotation matrix
 	 */
 	public void rotVect2Mat(){
 		SensorManager.getRotationMatrixFromVector(this.curRotMatrix,this.getSample());
 	}
 	
 	/**
 	 * get the angle changes from current rotation matrix and previous one
 	 */
 	public void rotMat2AngChange(){
 		//this following method returns angle in z,x,y
 		SensorManager.getAngleChange(this.angleChange, this.curRotMatrix, this.prevRotMatrix);
		Log.i(this.toString(), "current rotation matrix"+this.curRotMatrix.toString());
		Log.i(this.toString(), "previous rotation matrix"+this.prevRotMatrix.toString());
 		
 		//update previous matrix
 		System.arraycopy(this.curRotMatrix, 0, this.prevRotMatrix, 0, this.curRotMatrix.length);
 		//change the angle order into x,y,z
 		//get z value
 		float tmp=this.angleChange[0];
 		this.angleChange[0]=this.angleChange[1];
 		this.angleChange[1]=this.angleChange[2];
 		this.angleChange[2]=tmp;
		Log.i(this.toString(), "angle change x "+this.angleChange[0]);
		Log.i(this.toString(), "angle change y "+this.angleChange[1]);
		Log.i(this.toString(), "angle change z "+this.angleChange[2]);
 	}
 	
 	/**
 	 * display angle changes onto screen in degrees
 	 * @param data
 	 */
 	@Override
 	public void updateSampleDisplay(){
 		this.updateSampleDisplay(rad2Deg(this.angleChange));		
 	}
 	
 	@Override
 	/**
 	 * write angle changes (in radius instead of sampled values directly from
 	 * rotation vector sensor
 	 */
 	public void writeToFile(){
 		this.writeToFile(this.angleChange);
 	}
 
 	private float[] rad2Deg(float[] rad) {
 		float[] deg=new float[3];
 		for (int i=0;i<=2;i++)
		deg[i]=(float) Math.toDegrees(rad[i]);
 		return deg;
 	}
 }
