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
 
 package gda.device.detector.areadetector.v17.impl;
 
 import gda.configuration.epics.ConfigurationNotFoundException;
 import gda.configuration.epics.Configurator;
 import gda.device.DeviceException;
 import gda.device.detector.areadetector.IPVProvider;
 import gda.device.detector.areadetector.v17.ADDriverPilatus;
 import gda.epics.LazyPVFactory;
 import gda.epics.NoCallbackPV;
 import gda.epics.PV;
 import gda.observable.Predicate;
 import gda.epics.ReadOnlyPV;
 import gda.epics.interfaces.ADPilatusType;
 import gda.epics.interfaces.ElementType;
 import gda.factory.FactoryException;
 
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 public class ADDriverPilatusImpl implements ADDriverPilatus, InitializingBean {
 
 	static final Logger logger = LoggerFactory.getLogger(ADDriverPilatusImpl.class);
 
 	private String basePVName;
 
 	private IPVProvider pvProvider;
 
 	private ADPilatusType config;
 
 	private String deviceName;
 
 	// PVs
 
 	private ReadOnlyPV<Boolean> pvArmed;
 
 	private PV<Float> pvDelayTime;
 
 	private ReadOnlyPV<Float> pvDelayTime_RBV;
 
 	private PV<Float> pvThresholdEnergy;
 
 	private ReadOnlyPV<Float> pvThresholdEnergy_RBV;
 
 	private PV<Gain> pvGain;
 
 	private PV<Float> pvImageFileTmot;
 
 	private PV<String> pvBadPixelFile;
 
 	private ReadOnlyPV<Integer> pvNumBadPixels;
 
 	private NoCallbackPV<String> pvFlatFieldFile;
 
 	private PV<Integer> pvMinFlatField;
 
 	private ReadOnlyPV<Integer> pvMinFlatField_RBV;
 
 	private ReadOnlyPV<Boolean> pvFlatFieldValid;
 	
 
 	public void setDeviceName(String deviceName) throws FactoryException {
 		this.deviceName = deviceName;
 		initializeConfig();
 	}
 
 	private void initializeConfig() throws FactoryException {
 		if (deviceName != null) {
 			try {
 				config = Configurator.getConfiguration(getDeviceName(), ADPilatusType.class);
 			} catch (ConfigurationNotFoundException e) {
 				logger.error("EPICS configuration for device {} not found", getDeviceName());
 				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", e);
 			}
 		}
 	}
 
 	public void setPvProvider(IPVProvider pvProvider) {
 		this.pvProvider = pvProvider;
 	}
 
 	public void setBasePVName(String basePVName) {
 		this.basePVName = basePVName;
 	}
 
 	public String getDeviceName() {
 		return deviceName;
 	}
 
 	public IPVProvider getPvProvider() {
 		return pvProvider;
 	}
 
 	public String getBasePVName() {
 		return basePVName;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (deviceName == null && basePVName == null && pvProvider == null) {
 			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
 		}
 		try {
 			createLazyPvs();
 		} catch (Exception e) {
 			throw new FactoryException("Problem configuring PVs", e);
 		}
 	}
 
 	private void createLazyPvs() throws Exception {
 		
 		pvArmed = LazyPVFactory.newReadOnlyBooleanFromIntegerPV((config == null) ?
 				fullname("Armed") : getPvName(config.getArmed()));
 
 		pvDelayTime = LazyPVFactory.newFloatPV((config == null) ?
 				fullname("DelayTime") : getPvName(config.getDelayTime()));
 
 		pvDelayTime_RBV = LazyPVFactory.newReadOnlyFloatPV((config == null) ?
 				fullname("DelayTime_RBV") : getRoPvName(config.getDelayTime_RBV()));
 
 		pvThresholdEnergy = LazyPVFactory.newFloatPV((config == null) ?
 				fullname("ThresholdEnergy") : getPvName(config.getThresholdEnergy()));
 
 		pvThresholdEnergy_RBV = LazyPVFactory.newReadOnlyFloatPV((config == null) ?
 				fullname("ThresholdEnergy_RBV") : getRoPvName(config.getThresholdEnergy_RBV()));
 
 		pvGain = LazyPVFactory.newEnumPV((config == null) ?
				fullname("Gain") : getPvName(config.getGain()), Gain.class);
 
 		pvImageFileTmot = LazyPVFactory.newFloatPV((config == null) ?
 				fullname("ImageFileTmot") : getPvName(config.getImageFileTmot()));
 
 		pvBadPixelFile = LazyPVFactory.newStringFromWaveformPV((config == null) ?
 				fullname("BadPixelFile") : getPvName(config.getBadPixelFile()));
 
 		pvNumBadPixels = LazyPVFactory.newReadOnlyIntegerPV((config == null) ?
 				fullname("NumBadPixels") : getRoPvName(config.getNumBadPixels()));
 
 		pvFlatFieldFile = LazyPVFactory.newStringFromWaveformPV((config == null) ?
 				fullname("FlatFieldFile") : getPvName(config.getFlatFieldFile()));
 
 		pvMinFlatField = LazyPVFactory.newIntegerPV((config == null) ?
 				fullname("MinFlatField") : getPvName(config.getMinFlatField()));
 
 		pvMinFlatField_RBV = LazyPVFactory.newReadOnlyIntegerPV((config == null) ?
 				fullname("MinFlatField_RBV") : getRoPvName(config.getMinFlatField_RBV()));
 
 		pvFlatFieldValid = LazyPVFactory.newReadOnlyBooleanFromIntegerPV((config == null) ?
 				fullname("FlatFieldValid"): getRoPvName(config.getFlatFieldValid()));
 	}
 
 
 	private String getRoPvName(ElementType elementType) {
 		if (elementType.getRo() != true) {
 			logger.error("The pv '{}' was expected to be read-only but is not", elementType.getPv());
 		}
 		return elementType.getPv();
 	}
 
 	private String getPvName(ElementType elementType) {
 		if (elementType.getRo() != false) {
 			logger.error("The pv '{}' was expected to be writable but is not", elementType.getPv());
 		}
 		return elementType.getPv();
 	}
 
 	private String fullname(String pvElementName, String... args) throws Exception {
 		// untried - RobW
 		if (pvProvider == null) {
 			return basePVName + ((args.length > 0) ? args[0] : pvElementName);
 		}
 		return pvProvider.getPV(pvElementName);
 	}
 
 	@Override
 	public void reset() throws DeviceException {
 	}
 
 	@Override
 	public boolean isArmed() throws DeviceException {
 		try {
 			return pvArmed.get();
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 
 	@Override
 	public void waitForArmed(double timeoutS) throws DeviceException {
 		try {
 			pvArmed.setValueMonitoring(true);
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 		logger.info("Waiting for pilatus detector to arm");
 		try {
 			pvArmed.waitForValue(new Predicate<Boolean>() {
 				@Override
 				public boolean apply(Boolean b) {
 					return b;
 				}
 			}, timeoutS);
 		} catch (Exception e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public void setDelayTime(float delayTimeSeconds) throws DeviceException {
 		try {
 			pvDelayTime.putWait(delayTimeSeconds);
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public float getDelayTime_RBV() throws DeviceException {
 		try {
 			return pvDelayTime_RBV.get();
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public void setThresholdEnergy(float thresholdEnergy) throws DeviceException {
 		try {
 			pvThresholdEnergy.putWait(thresholdEnergy);
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public float getThresholdEnergy_RBV() throws DeviceException {
 		try {
 			return pvThresholdEnergy_RBV.get();
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public void setGain(Gain gain) throws DeviceException {
 		try {
 			pvGain.putWait(gain);
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public Gain getGain() throws DeviceException {
 		try {
 			return pvGain.get();
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public void setImageFileTmot(float timeoutSeconds) throws DeviceException {
 		try {
 			pvImageFileTmot.putWait(timeoutSeconds);
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public void setBadPixelFile(String filename) throws DeviceException {
 		try {
 			pvBadPixelFile.putWait(filename);
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public int getNumBadPixels() throws DeviceException {
 		try {
 			return pvNumBadPixels.get();
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public void setFlatFieldFile(String filename) throws DeviceException {
 		try {
 			pvFlatFieldFile.putNoWait(filename);
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 		if (!getFlatFieldValid()) {
 			throw new IllegalArgumentException("The flatfied file '" + filename + "' was not loaded or was not valid");
 		}
 	}
 
 	@Override
 	public void setMinFlatField(int minIntensity) throws DeviceException {
 		try {
 			pvMinFlatField.putWait(minIntensity);
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public int getMinFlatField_RBV() throws DeviceException {
 		try {
 			return pvMinFlatField_RBV.get();
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 	@Override
 	public boolean getFlatFieldValid() throws DeviceException {
 		try {
 			return pvFlatFieldValid.get();
 		} catch (IOException e) {
 			throw new DeviceException(e);
 		}
 	}
 
 
 }
