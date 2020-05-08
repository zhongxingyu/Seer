 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.detector.areadetector.v17;
 
 import gda.observable.Observable;
 
 
 /**
  * This maps to the subset of PV that are shared across all plugins. All the plugins that share this have an object
  * contained within themselves.
  */
 /**
  *
  */
 public interface NDPluginBase {
 	static final String PortName_RBV = "PortName_RBV";
 
 	static final String PluginType_RBV = "PluginType_RBV";
 
 	static final String NDArrayPort = "NDArrayPort";
 
 	static final String NDArrayPort_RBV = "NDArrayPort_RBV";
 
 	static final String NDArrayAddress = "NDArrayAddress";
 
 	static final String NDArrayAddress_RBV = "NDArrayAddress_RBV";
 
 	static final String EnableCallbacks = "EnableCallbacks";
 
 	static final String EnableCallbacks_RBV = "EnableCallbacks_RBV";
 
 	static final String MinCallbackTime = "MinCallbackTime";
 
 	static final String MinCallbackTime_RBV = "MinCallbackTime_RBV";
 
 	static final String BlockingCallbacks = "BlockingCallbacks";
 
 	static final String BlockingCallbacks_RBV = "BlockingCallbacks_RBV";
 
 	static final String ArrayCounter = "ArrayCounter";
 
 	static final String ArrayCounter_RBV = "ArrayCounter_RBV";
 
 	static final String ArrayRate_RBV = "ArrayRate_RBV";
 
 	static final String DroppedArrays = "DroppedArrays";
 
 	static final String DroppedArrays_RBV = "DroppedArrays_RBV";
 
 	static final String NDimensions_RBV = "NDimensions_RBV";
 
 	static final String ArraySize0_RBV = "ArraySize0_RBV";
 
 	static final String ArraySize1_RBV = "ArraySize1_RBV";
 
 	static final String ArraySize2_RBV = "ArraySize2_RBV";
 
 	static final String DataType_RBV = "DataType_RBV";
 
 	static final String ColorMode_RBV = "ColorMode_RBV";
 
 	static final String BayerPattern_RBV = "BayerPattern_RBV";
 
 	static final String UniqueId_RBV = "UniqueId_RBV";
 
 	static final String TimeStamp_RBV = "TimeStamp_RBV";
 
 	static final String NDAttributesFile = "NDAttributesFile";
 
 	
 	/**
 	 *
 	 */
 	String getPortName_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	String getPluginType_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	String getNDArrayPort() throws Exception;
 
 	/**
 	 *
 	 */
 	void setNDArrayPort(String ndarrayport) throws Exception;
 
 	/**
 	 *
 	 */
 	String getNDArrayPort_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	int getNDArrayAddress() throws Exception;
 
 	/**
 	 *
 	 */
 	void setNDArrayAddress(int ndarrayaddress) throws Exception;
 
 	/**
 	 *
 	 */
 	int getNDArrayAddress_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	boolean isCallbackEnabled() throws Exception;
 
 	/**
 	 *
 	 */
 	boolean isCallbacksEnabled_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	double getMinCallbackTime() throws Exception;
 
 	/**
 	 *
 	 */
 	void setMinCallbackTime(double mincallbacktime) throws Exception;
 
 	/**
 	 *
 	 */
 	double getMinCallbackTime_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	short getBlockingCallbacks() throws Exception;
 
 	/**
 	 *
 	 */
 	void setBlockingCallbacks(int blockingcallbacks) throws Exception;
 
 	/**
 	 *
 	 */
 	short getBlockingCallbacks_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	int getArrayCounter() throws Exception;
 
 	/**
 	 *
 	 */
 	void setArrayCounter(int arraycounter) throws Exception;
 
 	/**
 	 *
 	 */
 	int getArrayCounter_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	double getArrayRate_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	int getDroppedArrays() throws Exception;
 
 	/**
 	 *
 	 */
 	void setDroppedArrays(int droppedarrays) throws Exception;
 
 	/**
 	 *
 	 */
 	int getDroppedArrays_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	int getNDimensions_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	int getArraySize0_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	int getArraySize1_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	int getArraySize2_RBV() throws Exception;
 
 	public static final short Int8=0;
 	public static final short UInt8=1;
 	public static final short Int16=2;
 	public static final short UInt16=3;
 	public static final short Int32=4;
 	public static final short UInt32=5;
 	public static final short Float32=6;
 	public static final short Float64=7;
 	//must be in the order above
 	enum DataType{ INT8, UINT8, INT16, UINT16, INT32, UINT32, FLOAT32, FLOAT64 }
 	/**
 	 * BL16I-EA-PILAT-01:image1:DataType_RBV
      * Data type:      DBR_CTRL_ENUM (native: DBF_ENUM)
      * Element count:  1
      * Value:          Int32 (4) 
      * Status:         NO_ALARM
      * Severity:       NO_ALARM
      * Enums:          ( 8)
      *                 [ 0] Int8
      *                 [ 1] UInt8
      *                 [ 2] Int16
      *                 [ 3] UInt16
      *                 [ 4] Int32
      *                 [ 5] UInt32
      *                 [ 6] Float32
      *                 [ 7] Float64
      * 
 	 */
 	short getDataType_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	short getColorMode_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	short getBayerPattern_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	int getUniqueId_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	double getTimeStamp_RBV() throws Exception;
 
 	/**
 	 *
 	 */
 	String getNDAttributesFile() throws Exception;
 
 	/**
 	 *
 	 */
 	void setNDAttributesFile(String ndattributesfile) throws Exception;
 
 	/**
 	 * @throws Exception
 	 */
 	void disableCallbacks() throws Exception;
 
 	/**
 	 * @throws Exception
 	 */
 	void enableCallbacks() throws Exception;
 
 	/**
 	 * 
 	 */
 	void reset() throws Exception;
 
 	/**
 	 * @return initial array port
 	 */
 	String getInitialArrayPort();
 
 	/**
 	 * @return initial array address.
 	 */
 	Integer getInitialArrayAddress();
 	
 	Observable<Integer> createArrayCounterObservable() throws Exception;
 	
 	Observable<Boolean> createConnectionStateObservable() throws Exception;
 
 	Observable<String> createEnableObservable() throws Exception;
 
	Observable<Double> createMinCallbackTimeObservable() throws Exception;

 }
