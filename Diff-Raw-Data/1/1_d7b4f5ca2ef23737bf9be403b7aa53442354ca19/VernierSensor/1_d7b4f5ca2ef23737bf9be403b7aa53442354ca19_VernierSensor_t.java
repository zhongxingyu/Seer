 /**
  * 
  */
 package org.concord.sensor.vernier;
 
 import org.concord.sensor.SensorRequest;
 import org.concord.sensor.device.DeviceService;
 import org.concord.sensor.device.impl.SensorConfigImpl;
 import org.concord.sensor.impl.LinearCalibration;
 import org.concord.sensor.impl.Range;
 import org.concord.sensor.impl.SensorCalibration;
 
 public class VernierSensor extends SensorConfigImpl
 {
 	public final static int CHANNEL_TYPE_ANALOG = 0;
 	public final static int CHANNEL_TYPE_DIGITAL = 1;
 	
 	public final static byte kProbeTypeNoProbe = 0;
 	public final static byte kProbeTypeTime = 1;
 	public final static byte kProbeTypeAnalog5V = 2;
 	public final static byte kProbeTypeAnalog10V = 3;
 	public final static byte kProbeTypeHeatPulser = 4;
 	public final static byte kProbeTypeAnalogOut =5;
 	public final static byte kProbeTypeMD = 6;
 	public final static byte kProbeTypePhotoGate = 7;
 	public final static byte kProbeTypeDigitalCount = 10;
 	public final static byte kProbeTypeRotary = 11;
 	public final static byte kProbeTypeDigitalOut = 12;
 	public final static byte kProbeTypeLabquestAudio = 13;
 	
 	/**
      * 
      */
     private final VernierSensorDevice device;
 
 	SensorCalibration calibrationEquation;
 
 	private int channelType;
 	
 	/**
 	 * This corresponds to the OperationType filed in the SensorDDSRec structure.
 	 * Its values are the constants starting with "kProbeType"
 	 * in the verniersensormap.xml file there is a "Type" attribute on each sensor
 	 * that defines this property.
 	 */
 	byte vernierProbeType = kProbeTypeAnalog5V;
 	private SensorCalibration postCalibrationEquation;	
 	
 	/**
      * @param device
 	 * @param channelNumber 
      */
     public VernierSensor(VernierSensorDevice device, DeviceService devService, 
     		int channelNumber, int channelType)
     {
         this.device = device;
         setPort(channelNumber);
         this.channelType = channelType; 
     }
 
 	public void setCalibration(SensorCalibration calibration)
 	{
 		calibrationEquation = calibration;
 	}
 	   	
 	public SensorCalibration getCalibration()
 	{
 		return calibrationEquation;
 	}
 	
 	public void setPostCalibration(SensorCalibration calibration)
 	{
 		postCalibrationEquation = calibration;
 	}
 	
 	public SensorCalibration getPostCalibration()
 	{
 		return postCalibrationEquation;
 	}
 	
 	public float doPostCalibration(float input)
 	{
 		SensorCalibration postCalibration = getPostCalibration();
 		if(postCalibration != null){
 			return postCalibration.calibrate(input);
 		}
 		
 		return input;
 	}
 	
 	/**
 	 * @param sensorId
 	 * @return
 	 */
 	public int setupSensor(int sensorId, SensorRequest request)
 	{
 		if(channelType == CHANNEL_TYPE_DIGITAL){
 			
 			// This is a motion sensor or a GoMotion
 			if(sensorId == 2 || sensorId == 69) {
 				setConfirmed(true);
 
 				// it is digital sensor
 				setUnit("m");
 				setType(QUANTITY_DISTANCE);
 
 				vernierProbeType = kProbeTypeMD;
 				setStepSize(0.01f);
 			}
 			
 		} else if(sensorId >= 20){
 			// This is a smart sensor which means it has 
 			// calibration information stored in the sensor itself
 			
 			setConfirmed(true);
 
 			// TODO get the information from the auto id sensor
 			// sprintf(sensConfig->name, ddsRec.SensorLongName);
 			// state->calibrationFunct = NULL;
 			Range valueRange = null;
 			
 			switch(sensorId){
 			case SensorID.BAROMETER:
 				setUnit("kPa");
 				setType(QUANTITY_GAS_PRESSURE);
 				setName("Barometer");
 				// for pressure this is required so it can tell the diff
 				// between barometer and regular pressure
 				setStepSize(0.01f); 
 				valueRange = new Range(81.0f, 106.0f);
 				setValueRange(valueRange);
 				break;
 
 			case SensorID.GAS_PRESSURE:
 				setUnit("kPa");
 				setType(QUANTITY_GAS_PRESSURE);
 				setName("Biology Gas Pressure");
 				// for pressure this is required so it can tell the diff
 				// between barometer and regular pressure
 				setStepSize(0.05f); 
 				break;
 
 			case SensorID.DUAL_R_FORCE_10:
 				setUnit("N");
 				setType(QUANTITY_FORCE);
 				setName("Dual Range Force 10 N");
 				setStepSize(0.01f);
 				valueRange = new Range(-10f, 10f);
 				setValueRange(valueRange);
 				break;
 
 			case SensorID.DUAL_R_FORCE_50:
 				setUnit("N");
 				setType(QUANTITY_FORCE);
 				setName("Dual Range Force 50 N");
 				setStepSize(0.05f);
 				valueRange = new Range(-50f, 50f);
 				setValueRange(valueRange);
 				break;
 
 			case SensorID.SMART_LIGHT_1:
 			case SensorID.SMART_LIGHT_2:
 			case SensorID.SMART_LIGHT_3:
 				setUnit("lux");
 				setType(QUANTITY_LIGHT);
 				setName("Illuminance");				
 				// we keep this artificially low so we don't restrict 
 				// malformed requests which claim to require small step sizes
 				setStepSize(0.01f);
 				break;
 
 			case SensorID.MAGNETIC_FIELD_HIGH:
 			case SensorID.MAGNETIC_FIELD_LOW:
 				// turns out on the new sensors the default unit is mT not G
 				// on older sensors I heard second hand that the default is G on the small range
 				
 				// So it isn't clear what to do here. On some devices we can query the datasheet
 				// then we can select the correct calibration page, but on the LabPro that doesn't
 				// seem possible.  On the lab pro the best option seems to be processing the
 				// the sensor description xml that Vernier provides.
 				// Also the next issue is the concept of a request unit should each device be responsible
 				// for converting to the request unit?
 				setUnit("mT");
 				setType(QUANTITY_MAGNETIC_FIELD);
 				setName("Magnetic Field");
 				// FIXME this should be different for the different sensors. 
 				setStepSize(0.0032f);
 				break;
 
 			case SensorID.GO_TEMP:
 				setUnit("degC");
 				setType(QUANTITY_TEMPERATURE_WAND);
 				setName("Temperature");
 				setStepSize(0.01f);
 				break;
 			case SensorID.GO_MOTION:
 				setUnit("m");
 				setType(QUANTITY_DISTANCE);
 				setName("Position");
 				setStepSize(0.01f);
 				break;
 
 			case SensorID.SMART_HUMIDITY:
 				setUnit("%RH");
 				setType(QUANTITY_RELATIVE_HUMIDITY);
 				setName("Relative Humidity");
 				// This is higher than the others
 				// but we are not currently paying attention to step size
 				// for humidity sensors @see AbstractSensorDevice#scoreStepSize
 				setStepSize(0.1f);
 				break;
 
 			case SensorID.IR_TEMP:
 				setUnit("degC");
 				setType(QUANTITY_TEMPERATURE_WAND);
 				setName("IR Temperature Sensor");
 				setStepSize(0.01f);
 				break;
 
 			case SensorID.PH:
 				setUnit("pH");
 				setType(QUANTITY_PH);
 				setName("pH");
 				setStepSize(0.0077f);
 				break;	
 
 			case SensorID.UVA_INTENSITY:
 				setUnit("mW/m^2");
 				setType(QUANTITY_UVA_INTENSITY);
 				setName("UVA Intensity");
 				setStepSize(5f);
 				setValueRange(new Range(0f, 20000f));
 				break;
 
 			case SensorID.UVB_INTENSITY:
 				setUnit("mW/m^2");
 				setType(QUANTITY_UVB_INTENSITY);
 				setName("UVB Intensity");
 				setStepSize(0.25f);
 				setValueRange(new Range(0f, 1000f));				
 				break;
 
 			case SensorID.SALINITY:
 				setUnit("ppt");
 				setType(QUANTITY_SALINITY);
 				setName("Salinity");
 				// This is just a bit higher than the others so it might
 				// cause problems, but again we aren't paying attention
 				// to the step size right now @see AbstractSensorDevice#scoreStepSize
 				setStepSize(0.02f);
 				break;			
 
 			case SensorID.CO2_GAS_LOW:
 			case SensorID.CO2_GAS_HIGH:
 				setUnit("ppm");
 				setType(QUANTITY_CO2_GAS);
 				setName("CO2");
 				// This is higher than the others
 				// but we are not currently paying attention to step size
 				// for co2 sensors @see AbstractSensorDevice#scoreStepSize				
 				setStepSize(4.0f);
 				break;
 
 			case SensorID.SOUND_LEVEL:
 				setUnit("dB");
 				setType(QUANTITY_SOUND_INTENSITY);
 				setName("Sound Level");
 				setStepSize(0.2f);
 				break;
 
 			case SensorID.BLOOD_PRESSURE:
 				setUnit("mm Hg");
 				setType(QUANTITY_BLOOD_PRESSURE);
 				setName("Cuff Pressure");
 				setStepSize(0.11222f); 									
 				break;			
 
 			case SensorID.COLORIMETER:
 				setType(QUANTITY_COLORIMETER);
 				setName("Absorbance");
 				
 				// I doubt this is the right step size
 				setStepSize(0.057f);
 
 				// the built in calibration done by the LabPro, and LabQuest, and GoIO sdk 
 				// should return %T, so we need to do this postCalibration to turn that into
 				// absorbance
 
 				setPostCalibration(new SensorCalibration(){ public float calibrate(float input) {
 					return (float)Math.log10(100f/input);
 				}});
 				break;
 
 			case SensorID.HAND_DYNAMOMETER:
 				setUnit("N");
 				setType(QUANTITY_HAND_DYNAMOMETER);
 				setName("Hand Dynamometer");
 				setStepSize(0.35f);
 				break;
 
 			case SensorID.HIGH_CURRENT:
 				setUnit("A");
 				setType(QUANTITY_CURRENT);
 				setName("High Current Sensor");
 				setStepSize(0.005f); // 4.9 mA
 				setValueRange(new Range(-10f,10f));
 				break;
 
 			case SensorID.DISSOLVED_OXYGEN:
 				setUnit("mg/L");
 				setType(QUANTITY_DISSOLVED_OXYGEN);
 				setName("Dissolved Oxygen");
 				setStepSize(0.00654f); 									
 				break;
 				
 			case SensorID.OXYGEN_GAS_CK:
 				setUnit("%");
 				setType(QUANTITY_OXYGEN_GAS);
 				setName("Oxygen Gas");
 				setStepSize(0.01f);
 				break;
 				
 			case SensorID.SPIROMETER:
 				setUnit("L/s");
 				setType(QUANTITY_LUNG_AIR_FLOW);
 				setName("Flow Rate");
 				setStepSize(0.01437f); 									
 				break;
 
 			case SensorID.CONDUCTIVITY_200: 
 			case SensorID.CONDUCTIVITY_2000:
 			case SensorID.CONDUCTIVITY_20000:
 				setUnit("uS/cm");
 				setType(QUANTITY_CONDUCTIVITY);
 				setName("Conductivity");
 				switch(sensorId){
 				case SensorID.CONDUCTIVITY_200: 
 					setStepSize(0.1f);
 					break;
 				case SensorID.CONDUCTIVITY_2000:
 					setStepSize(1f);
 					break;
 				case SensorID.CONDUCTIVITY_20000:
 					setStepSize(10f);
 					break;
 				}
				break;
 
 			default:
 				setType(QUANTITY_UNKNOWN);
 				break;				
 			}	
 
 		} else if(sensorId != 0) {
 			// These are the "not smart" sensors.  They have an id, but they don't store the calibration on the
 			// device.  
 			setConfirmed(true);
 
 			// do a lookup from our list of known sensors and calibrations
 			this.device.log("  current attached sensor: " + sensorId);
 
 			switch(sensorId){
 			case SensorID.TEMPERATURE_C:
 				setUnit("degC");
 				setName("Temperature");
 				setType(QUANTITY_TEMPERATURE);			
 				
 				// we keep this artificially low so we don't restrict 
 				// malformed requests which claim to require small step sizes
 				setStepSize(0.01f); 
 				setCalibration(temperatureCalibration);
 				break;
 			case SensorID.THEROCOUPLE:
 				setUnit("degC");
 				setName("Temperature");
 				setType(QUANTITY_TEMPERATURE);			
 				// we keep this artificially low so we don't restrict 
 				// malformed requests which claim to require small step sizes
 				setStepSize(0.01f); 
 				setCalibration(temperatureCalibration);
 				break;
 			case SensorID.LIGHT:
 				setUnit("lux");
 				setName("Illuminance");
 				setType(QUANTITY_LIGHT);			
 				
 				// This is higher than the others
 				// but we are not currently paying attention to step size
 				// for light sensors @see AbstractSensorDevice#scoreStepSize				
 				setStepSize(2f);
 				setCalibration(lightCalibration);
 				break;			
 			case SensorID.TI_VOLTAGE:			
 			case SensorID.VOLTAGE:
 			case SensorID.CV_VOLTAGE:
 				setUnit("V");
 				setName("Voltage");
 				setType(QUANTITY_VOLTAGE);
 
 				setStepSize(0.01f);
 				switch(sensorId){
 				case SensorID.TI_VOLTAGE:
 					setCalibration(tiVoltageCalibration);
 					vernierProbeType = kProbeTypeAnalog10V;
 					break;		
 				case SensorID.VOLTAGE:
 					setCalibration(rawVoltageCalibration);
 					break;
 				case SensorID.CV_VOLTAGE:
 					setCalibration(differentialVoltageCalibration);
 					break;
 				}
 				break;
 			case SensorID.CO2_GAS:
 				setUnit("ppm");
 				setName("CO2 Gas");
 				setType(QUANTITY_CO2_GAS);			
 
 				// This is higher than the others
 				// but we are not currently paying attention to step size
 				// for co2 sensors @see AbstractSensorDevice#scoreStepSize				
 				setStepSize(4.0f);
 				setCalibration(co2GasCalibration);			
 				break;
 			case SensorID.OXYGEN_GAS:
 				setUnit("%");
 				setName("Oxygen Gas");
 				setType(QUANTITY_OXYGEN_GAS);			
 
 				// This is higher than the others
 				// but we are not currently paying attention to step size
 				// for oxygen sensors @see AbstractSensorDevice#scoreStepSize				
 				setStepSize(0.01f); 
 				setCalibration(oxygenGasCalibration);			
 				break;
 			case SensorID.EKG:
 				setUnit("mV");
 				setName("EKG");
 				setType(QUANTITY_EKG);
 				setStepSize(0.002f); // FIXME: this is a hack we should be able calc this
 
 				// the ekg sensor just returns mV and the software has to convert 
 				// it to a heart rate					
 				setCalibration(rawVoltageCalibration);
 				break;
 			case SensorID.CV_CURRENT:
 				setUnit("A");
 				setType(QUANTITY_CURRENT);
 				setStepSize(0.0003f); // this is assuming 12bit resolution which is on GoLink, LabPro, LabQuest: 0.31 mA 
 				setValueRange(new Range(-0.6f, 0.6f));
 				setCalibration(new LinearCalibration(
 						0.625f,  // k0
 						-0.25f   // k1
 						));
 				break;
 			case SensorID.CURRENT:
 			case SensorID.RESISTANCE:
 			case SensorID.LONG_TEMP:
 			case SensorID.CO2:
 			case SensorID.OXYGEN:
 			case SensorID.TEMPERATURE_F:
 				this.device.log("Sensor type is not supported yet: " + sensorId);
 				setType(QUANTITY_UNKNOWN);
 				break;
 				
 			case SensorID.HEART_RATE:
 				setUnit("v");
 				setName("Heart Rate Signal");
 				setType(QUANTITY_HEART_RATE_SIGNAL);
 				setStepSize(0.002f);
 				setCalibration(rawVoltageCalibration);			
 				break;
 				
 			default:
 				this.device.log("Unknown sensor id: " + sensorId);
 				setType(QUANTITY_UNKNOWN);
 			}
 
 			// TODO Auto-generated method stub
 			return 0;
 		} else {
 			// These are for sensors we can't auto id.
 			// They will not work in the current design
 			// FIXME this code doesn't interact correctly with the AbstractSensorDevice autoid code
 			//   it is never called with a non null request the code below will never be executed.
 			
 			setConfirmed(false);
 			
 			// This is not an auto id sensor
 			// as long as there is only one sensor that matches 
 			// the requested quantity type.  If not then
 			// we are going to have problems.  The api breaks
 			// down here.  Lets cross our fingers and hope we don't
 			// have to deal with that.
 			if(request == null) {
 				// we need a request to determine what calibration
 				// to use.
 				return 0;
 			}
 			switch(request.getType()){
 			case QUANTITY_RELATIVE_HUMIDITY:
 				setUnit("%RH");
 				setName("Relative Humidity");
 				setType(QUANTITY_RELATIVE_HUMIDITY);
 				setStepSize(0.04f);
 				setCalibration(relativeHumidityCalibration);
 				break;
 			case QUANTITY_FORCE:
 				// if we are here it means they are using
 				// a student force sensor
 				setUnit("N");
 				setName("Force");
 				setType(QUANTITY_FORCE);
 				setStepSize(0.02f);
 				setCalibration(studentForceCalibration);
 				break;
 			}
 		}
 		
 
 
 		return 0;
 	}
 	
 	public byte getVernierProbeType()
 	{
 		return vernierProbeType;
 	}
 	
 	public void setVernierProbeType(byte type) {
 		vernierProbeType = type;
 	}
 
 	@Override
 	public void setType(int type){
 		super.setType(type);
 		if(type == QUANTITY_RAW_VOLTAGE_1 ||
 				type == QUANTITY_RAW_DATA_1){
 			// setup sensor to report 0-5V
 			setVernierProbeType(kProbeTypeAnalog5V);
 			setCalibration(rawVoltageCalibration);
 		} else if(type == QUANTITY_RAW_VOLTAGE_2 ||
 				type == QUANTITY_RAW_DATA_2){
 			// setup sensor to report +/-10V
 			setVernierProbeType(kProbeTypeAnalog10V);			
 			setCalibration(rawVoltageCalibration);
 		}
 	}
 	
 	/**
 	 * Special calibration function for simply return the data which is 
 	 * passed in
 	 */
 	public final static SensorCalibration rawVoltageCalibration = 
 		new LinearCalibration(
 				0f,  // k0  
 				1f   // k1 - return the same value passed in
 				);
 
 	/**
 	 * Special calibration function for flagging raw data
 	 * it should actually never be called.  So it returns a value
 	 * which is hopefully noticably weird
 	 */
 	public final static SensorCalibration rawDataCalibration = 
 		new LinearCalibration(
 				0.12345f,  // k0  - return a constant value
 				0f         // k1
 				);
 
 	public final static SensorCalibration temperatureCalibration =
 		new SensorCalibration(){
 		/*
 		 * First get the R of the sensor: V0 = measured voltage Vres = reference
 		 * voltage 5V Rknown = resistance of Vres V1 = voltage we measured
 		 * Rsensor = V0*Rknown/(Vres-V0) this equation comes from the standard
 		 * voltage division equation.
 		 * 
 		 * Now with the resistance the equation for the temp in degC is: 
 		 * <pre>
 		 * T(degC) = 1/(K0 + K1*ln(1000*R) + K2*ln(1000*R)^3) - 273.15 
 		 * K0 = 1.02119E-3 
 		 * K1 = 2.22468E-4 
 		 * K2 = 1.33342E-7 
 		 * </pre>
 		 */
 		public final static float TEMP_K0 = 1.02119E-3f;
 		public final static float TEMP_K1 = 2.22468E-4f;
 		public final static float TEMP_K2 = 1.33342E-7f;
 		public float calibrate(float voltage)
 		{
 			float R = voltage*15/(5-voltage);
 
 			float lnR = (float)Math.log(1000*R);
 
 			return 1.0f /(TEMP_K0 + TEMP_K1*lnR + TEMP_K2*lnR*lnR*lnR) - 273.15f;
 		}
 	};
 	
 	/**
 	 * Light Light Calibration
 	 */
 	public final static SensorCalibration lightCalibration =
 		new SensorCalibration(){
 		/*
 		 * From the vernier light sensor booklet.
 		 */ 
 		//public final static float ILLUM_B0 = 5.0E-3f;  // most sensitive switch position
 		public final static float ILLUM_B1 = 4.5E-4f;  // middle switch position
 		//public final static float ILLUM_B2 = 2.0E-5f;  // least sensitive (outdoor) position
 		public float calibrate(float voltage)
 		{
 			// The only sensor I have doesn't have a switch
 			// so I'm going to guess it is in the middle position
 			return voltage/ILLUM_B1;
 		}
 	};
 
 	/**
 	 * Relative Humidity Calibration
 	 */
 	public final static SensorCalibration relativeHumidityCalibration =
 		new LinearCalibration(
 				-23.8f,   // k0
 				32.9f);   // k1
 	
 	/**
 	 * Student Force
 	 */
 	public final static SensorCalibration studentForceCalibration =
 		new LinearCalibration(
 				9.8f,   // k0
 				-9.8f); // k1
 	
 	public final static SensorCalibration tiVoltageCalibration =
 		rawVoltageCalibration;
 
 	/**
 	 * Differental Voltage
 	 */
 	public final static SensorCalibration differentialVoltageCalibration =
 		new LinearCalibration(
 				6.25f,  // k0
 				-2.5f); // k1
 
 	/**
 	 * CO2 Gas Calibration
 	 * this ithe ppm calibration
 	 */
 	public final static SensorCalibration co2GasCalibration =
 		new LinearCalibration(
 				0f,     // k0
 				2000f   // k1
 				); 
 	
 	/**
 	 * Oxygen Gas Calibration
 	 * this is the % calibration
 	 */
 	public final static SensorCalibration oxygenGasCalibration =
 		new LinearCalibration(
 				0f,      // k0
 				6.769f   // k1
 				);
 }
