 package com.NbodySimulator;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import android.os.Environment;
 import android.util.Log;
 
 public class SimDataModel {
 	private final static String LOGDIR = Environment.getExternalStorageDirectory().getPath() + "/NbodySimulator/";
 	private String dataFile = LOGDIR + "data.txt";
 	static final int NUM_P = 5;
 	private int num_p;
 	private double dt;
 	private double k;
 	private double[] x3;
 	private double[] v3;
 	private double[] a3;
 	private double[] mass;
 	private String[] color;
 	private String[] shape;
 	private double[] tmp_a3;
 	
 	private float[] footprint;
 	private boolean showFootprint;
 	private int indexFootprint = 0;
 	private int len_footprint;
 	static final int LEN_FOOTPRINT = 1000;
 	private int intervalFootprint;
 	private int[] indexes;
 
 	public SimDataModel(){
 		System.loadLibrary("calculate");
 		x3 = new double[NUM_P*3];
 		v3 = new double[NUM_P*3];
 		a3 = new double[NUM_P*3];
 		mass = new double[NUM_P];
 		color = new String[NUM_P];
 		shape = new String[NUM_P];
 		tmp_a3 = new double[NUM_P*(NUM_P-1)*3];
 		footprint = new float[NUM_P*LEN_FOOTPRINT*2];//only 2D position.
 		indexes = new int[NUM_P*3];
		readData();
 	}
 	public void reset() {
 		dt = 1.0; k = 1.0; num_p = NUM_P; showFootprint=false; len_footprint = +LEN_FOOTPRINT; intervalFootprint = 20;
 		mass[0] = 1.0;
 		mass[1] = 1.0;
 		mass[2] =30.0;
 		mass[3] = 1.0;
 		mass[4] = 1.0;
 		x3[ 0] = 300.0; x3[ 1] = 200.0; x3[ 2] =  10.0;
 		v3[ 0] =   0.5; v3[ 1] =   0.0; v3[ 2] =   0.0;
 		a3[ 0] =   0.0; a3[ 1] =   0.0; a3[ 2] =   0.0;
 		x3[ 3] = 300.0; x3[ 4] = 400.0; x3[ 5] =  10.0;
 		v3[ 3] =  -0.5; v3[ 4] =   0.0; v3[ 5] =   0.0;
 		a3[ 3] =   0.0; a3[ 4] =   0.0; a3[ 5] =   0.0;
 		x3[ 6] = 300.0; x3[ 7] = 300.0; x3[ 8] =  10.0;
 		v3[ 6] =   0.0; v3[ 7] =   0.0; v3[ 8] =   0.0;
 		a3[ 6] =   0.0; a3[ 7] =   0.0; a3[ 8] =   0.0;
 		x3[ 9] = 350.0; x3[10] = 300.0; x3[11] =  10.0;
 		v3[ 9] =   0.0; v3[10] =  -1.0; v3[11] =   0.0;
 		a3[ 9] =   0.0; a3[10] =   0.0; a3[11] =   0.0;
 		x3[12] = 250.0; x3[13] = 300.0; x3[14] =  10.0;
 		v3[12] =   0.0; v3[13] =   1.0; v3[14] =   0.0;
 		a3[12] =   0.0; a3[13] =   0.0; a3[14] =   0.0;
 		color[0] = "red"; color[1] = "blue"; color[2] = "white"; color[3] = "green"; color[4] = "yellow";
 		shape[0] = "circle"; shape[1] = "circle"; shape[2] = "circle"; shape[3] = "circle"; shape[4] = "circle";
 	}
 	public int getNum_p() {
 		return num_p;
 	}
 	public double[] getX3() {
 		return x3;
 	}
 	public String[] getColor() {
 		return color;
 	}
 	public String[] getShape() {
 		return shape;
 	}
 	public double[] getMass() {
 		return mass;
 	}
 	public native void ndk_calcForce(double[] x3, double[] a3, double[] tmp_a3, double[] mass, int[] indexes, int num_p,double k);
 	public native void ndk_integrate(double[] x3, double[] v3, double[] a3, int num_p, double dt);
 	public void calc() {
 		//forward Euler
 		ndk_calcForce(x3,a3,tmp_a3,mass,indexes,num_p,k);
 		ndk_integrate(x3,v3,a3,num_p,dt);
 	}
 	private void calcForce(double[] x3, double[] a3){
 		double tmpF,r2,r;
 		double[] tmpFx = new double[3];
 		int[] indexes = new int[num_p*3];
 		double nega[]={0.0, 0.0, 0.0}, posi[]={0.0, 0.0, 0.0};
 		for(int i=0;i<num_p;++i){
 			for(int j=i+1;j<num_p;++j){
 				r2 = range2(i,j);
 				r = Math.sqrt(r2);
 				//Log.i("debug","range2 "+r2+" range "+r);
 				tmpF = k/r2;
 				tmpFx[0] = tmpF*(x3[i*3+0]-x3[j*3+0])/r;
 				tmpFx[1] = tmpF*(x3[i*3+1]-x3[j*3+1])/r;
 				tmpFx[2] = tmpF*(x3[i*3+2]-x3[j*3+2])/r;
 				tmp_a3[(num_p-1)*3*i + (j-1)*3 + 0] = -1*mass[j]*tmpFx[0];
 				tmp_a3[(num_p-1)*3*i + (j-1)*3 + 1] = -1*mass[j]*tmpFx[1];
 				tmp_a3[(num_p-1)*3*i + (j-1)*3 + 2] = -1*mass[j]*tmpFx[2];
 				tmp_a3[(num_p-1)*3*j + i*3 + 0] = mass[i]*tmpFx[0];
 				tmp_a3[(num_p-1)*3*j + i*3 + 1] = mass[i]*tmpFx[1];
 				tmp_a3[(num_p-1)*3*j + i*3 + 2] = mass[i]*tmpFx[2];
 				/*
 				a3[j*3+0] += mass[i]*tmpF[0];
 				a3[j*3+1] += mass[i]*tmpF[1];
 				a3[j*3+2] += mass[i]*tmpF[2];
 				a3[i*3+0] += -1*mass[j]*tmpF[0];
 				a3[i*3+1] += -1*mass[j]*tmpF[1];
 				a3[i*3+2] += -1*mass[j]*tmpF[2];
 				*/
 			}
 		}
 		sortTmp_a3(indexes);
 		for(int i=0;i<num_p;++i){//TODO: optimize data access
 			for(int j=0;j<indexes[i*3];++j){
 				nega[0] += tmp_a3[i*(num_p-1)*3 + j*3];
 			}
 			for(int j=indexes[i*3];j<num_p-1;++j){
 				posi[0] += tmp_a3[i*(num_p-1)*3 + j*3];
 			}
 			for(int j=0;j<indexes[i*3+1];++j){
 				nega[1] += tmp_a3[i*(num_p-1)*3 + j*3 +1];
 			}
 			for(int j=indexes[i*3+1];j<num_p-1;++j){
 				posi[1] += tmp_a3[i*(num_p-1)*3 + j*3 +1];
 			}
 			for(int j=0;j<indexes[i*3+2];++j){
 				nega[2] += tmp_a3[i*(num_p-1)*3 + j*3 +2];
 			}
 			for(int j=indexes[i*3+2];j<num_p-1;++j){
 				posi[2] += tmp_a3[i*(num_p-1)*3 + j*3 +2];
 			}
 			a3[i*3+0] = posi[0]+nega[0];
 			a3[i*3+1] = posi[1]+nega[1];
 			a3[i*3+2] = posi[2]+nega[2];
 			posi[0] = 0.0; nega[0] = 0.0;
 			posi[1] = 0.0; nega[1] = 0.0;
 			posi[2] = 0.0; nega[2] = 0.0;
 			//Log.i("accel","i: "+i+" "+a3[i*3+0]+" "+a3[i*3+1]+" "+a3[i*3+2]);
 		}
 	}
 	private void sortTmp_a3(int[] indexes) {
 		//sort tmp_a3
 		//return array of index of tmp_a3[]
 		//if(i<ret) tmp_a3[i] < 0, else tmp_a3[i] > 0
 		//asc |tmp_a3[index]|
 		double tmp;
 		for(int i=0;i<num_p*3;++i){
 			indexes[i] = num_p-1;
 		}
 		for(int i=0;i<num_p;++i){
 			//sort tmp_a3[i*(num_p-1)*3 + j*3 +0or1or2] (0<=j<num_p-1)
 			for(int j=0;j<num_p-1-1;++j){//bubble sort loop
 				for(int k=num_p-1-1;k>j;--k){
 					if(tmp_a3[i*(num_p-1)*3 + k*3]<tmp_a3[i*(num_p-1)*3 + (k-1)*3]){
 						tmp = tmp_a3[i*(num_p-1)*3 + k*3];
 						tmp_a3[i*(num_p-1)*3 + k*3] = tmp_a3[i*(num_p-1)*3 + (k-1)*3];
 						tmp_a3[i*(num_p-1)*3 + (k-1)*3] = tmp;
 					}
 					if(tmp_a3[i*(num_p-1)*3 + k*3 +1]<tmp_a3[i*(num_p-1)*3 + (k-1)*3 +1]){
 						tmp = tmp_a3[i*(num_p-1)*3 + k*3 +1];
 						tmp_a3[i*(num_p-1)*3 + k*3 +1] = tmp_a3[i*(num_p-1)*3 + (k-1)*3 +1];
 						tmp_a3[i*(num_p-1)*3 + (k-1)*3 +1] = tmp;
 					}
 					if(tmp_a3[i*(num_p-1)*3 + k*3 +2]<tmp_a3[i*(num_p-1)*3 + (k-1)*3 +2]){
 						tmp = tmp_a3[i*(num_p-1)*3 + k*3 +2];
 						tmp_a3[i*(num_p-1)*3 + k*3 +2] = tmp_a3[i*(num_p-1)*3 + (k-1)*3 +2];
 						tmp_a3[i*(num_p-1)*3 + (k-1)*3 +2] = tmp;
 					}
 				}
 			}
 			for(int j=0;j<num_p-1;++j){//length of tmp_a3[i][]
 				if(indexes[i*3]==num_p-1){
 					if(Double.compare(tmp_a3[i*(num_p-1)*3 + j*3],0.0)>0){
 						indexes[i*3] = j;
 					}
 				}
 				if(indexes[i*3+1]==num_p-1){
 					if(Double.compare(tmp_a3[i*(num_p-1)*3 + j*3 +1],0.0)>0){
 						indexes[i*3+1] = j;
 					}
 				}
 				if(indexes[i*3+2]==num_p-1){
 					if(Double.compare(tmp_a3[i*(num_p-1)*3 + j*3 +2],0.0)>0){
 						indexes[i*3+2] = j;
 					}
 				}
 			}
 		}
 		return;
 	}
 	private double range2(int i, int j) {
 		double[] dx3;
 		dx3 = new double[3];
 		dx3[0] = x3[i*3+0]-x3[j*3+0];
 		dx3[1] = x3[i*3+1]-x3[j*3+1];
 		dx3[2] = x3[i*3+2]-x3[j*3+2];
 		return dx3[0]*dx3[0]+dx3[1]*dx3[1]+dx3[2]+dx3[2];
 	}
 	private void integrate(double[] x3, double[] v3, double[] a3){
 		for(int i=0;i<num_p;++i){
 			x3[i*3+0] += v3[i*3+0]*dt;
 			x3[i*3+1] += v3[i*3+1]*dt;
 			x3[i*3+2] += v3[i*3+2]*dt;
 			v3[i*3+0] += a3[i*3+0]*dt;
 			v3[i*3+1] += a3[i*3+1]*dt;
 			v3[i*3+2] += a3[i*3+2]*dt;
 			a3[i*3+0] = 0.0;
 			a3[i*3+1] = 0.0;
 			a3[i*3+2] = 0.0;
 		}
 	}
 	/*
 	 * dataFile format:
 	 * 1 : dt k num_p
 	 * 2~: mass x v color shape
 	 */
 	public void writeData(){
 		String fileName = dataFile;
 		FileWriter writer = null;
		File dir = new File(LOGDIR);
		if(!dir.exists()){
			dir.mkdirs();
		}
 		File fl = new File(fileName);
 		try {
 			if(!fl.exists()){
 				fl.createNewFile();
 			}
 			writer = new FileWriter(fileName);//over write
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		BufferedWriter bw = new BufferedWriter(writer);

 		String line = null;
 		try {
 			line = ""+dt+" "+k+" "+num_p+" "+len_footprint+" "+intervalFootprint;
 			Log.i("write",line);
 			bw.write(line);
 			bw.newLine();
 
 			for(int i=0;i<num_p;++i){
 				line = mass[i]+" "+x3[i*3+0]+" "+x3[i*3+1]+" "+x3[i*3+2]+" "+v3[i*3+0]+" "+v3[i*3+1]+" "+v3[i*3+2]+" "+color[i]+" "+shape[i];
 				Log.i("write",line);
 				bw.write(line);
 				bw.newLine();
 			}
 			
 			bw.close();writer.close();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 	public void readData(){
 		String fileName = dataFile;
 	    FileReader reader = null;
 		try {
 			reader = new FileReader(fileName);
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 			reset();
 			return;
 		}
 		BufferedReader br = new BufferedReader(reader);
 		String line;
 		String[] strAry;
 		try {
 			line = br.readLine();
 			strAry = line.split(" ");
 			dt = Double.valueOf(strAry[0]);
 			k = Double.valueOf(strAry[1]);
 			num_p = Integer.valueOf(strAry[2]);
 			len_footprint = Integer.valueOf(strAry[3]);
 			intervalFootprint = Integer.valueOf(strAry[4]);
 			for(int i=0;i<num_p;++i){
 				line = br.readLine();
 				strAry = line.split(" ");
 				mass[i] = Double.valueOf(strAry[0]);
 				x3[i*3+0] = Double.valueOf(strAry[1]);
 				x3[i*3+1] = Double.valueOf(strAry[2]);
 				x3[i*3+2] = Double.valueOf(strAry[3]);
 				v3[i*3+0] = Double.valueOf(strAry[4]);
 				v3[i*3+1] = Double.valueOf(strAry[5]);
 				v3[i*3+2] = Double.valueOf(strAry[6]);
 				a3[i*3+0] = 0.0;
 				a3[i*3+1] = 0.0;
 				a3[i*3+2] = 0.0;
 				Log.i("pos",""+x3[i*3+0]+" "+x3[i*3+1]+","+x3[i*3+2]);
 				color[i] = strAry[7].toString();
 				shape[i] = strAry[8].toString();
 			}
 			br.close();reader.close();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} catch (Exception e){
 			e.printStackTrace();
 			reset();
 		}
 	}
 	public int getIntervalFootprint() {
 		return intervalFootprint;
 	}
 	public void toggleShowFootprint() {
 		if(showFootprint){
 			showFootprint=false;
 		}else{
 			showFootprint=true;
 		}
 	}
 	public boolean isShowFootprint() {
 		return showFootprint;
 	}
 	public int getLen_footprint() {
 		return len_footprint;
 	}
 	public float[] getFootprint() {
 		return footprint;
 	}
 	public int getIndexFootprint() {
 		return indexFootprint;
 	}
 	public void inclementIndexFootprint() {
 		++indexFootprint;
 		indexFootprint = indexFootprint % len_footprint;
 	}
 }
