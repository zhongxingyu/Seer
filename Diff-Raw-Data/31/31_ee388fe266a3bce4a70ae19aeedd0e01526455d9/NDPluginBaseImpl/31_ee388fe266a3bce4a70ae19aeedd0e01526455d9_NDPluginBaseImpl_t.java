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
 import gda.device.detector.areadetector.IPVProvider;
 import gda.device.detector.areadetector.v17.NDPluginBase;
 import gda.epics.LazyPVFactory;
 import gda.epics.connection.EpicsController;
 import gda.epics.interfaces.NDPluginBaseType;
 import gda.factory.FactoryException;
 import gda.observable.Observable;
 import gda.observable.ObservableUtil;
 import gda.observable.Observer;
 import gov.aps.jca.CAException;
 import gov.aps.jca.Channel;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.event.ConnectionEvent;
 import gov.aps.jca.event.ConnectionListener;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 public class NDPluginBaseImpl implements InitializingBean, NDPluginBase {
 
 	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
 
 	private String initialArrayPort;
 
 	private Integer initialArrayAddress;
 
 	private Boolean initialBlockingCallbacks;
 
 	private Integer initialEnableCallbacks;
 
 	private String basePVName;
 
 	private IPVProvider pvProvider;
 
 	private NDPluginBaseType config;
 	private String deviceName;
 	// Setup the logging facilities
 	static final Logger logger = LoggerFactory.getLogger(NDPluginBaseImpl.class);
 
 	public String getDeviceName() {
 		return deviceName;
 	}
 
 	public void setDeviceName(String deviceName) throws FactoryException {
 		this.deviceName = deviceName;
 		initializeConfig();
 	}
 
 	private void initializeConfig() throws FactoryException {
 		if (deviceName != null) {
 			try {
 				config = Configurator.getConfiguration(getDeviceName(), NDPluginBaseType.class);
 			} catch (ConfigurationNotFoundException e) {
 				logger.error("EPICS configuration for device {} not found", getDeviceName());
 				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", e);
 			}
 		}
 	}
 
 	/**
 	 * Map that stores the channel against the PV name
 	 */
 	private Map<String, Channel> channelMap = new HashMap<String, Channel>();
 
 	/**
 	 * List all the PVs
 	 */
 
 	/**
 	*
 	*/
 	@Override
 	public String getPortName_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getPortName_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(PortName_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getPortName_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getPluginType_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getPluginType_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(PluginType_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getPluginType_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getNDArrayPort() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getNDArrayPort().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(NDArrayPort));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getNDArrayPort", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setNDArrayPort(String ndarrayport) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getNDArrayPort().getPv()), ndarrayport);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(NDArrayPort), ndarrayport);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot setNDArrayPort", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getNDArrayPort_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getNDArrayPort_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(NDArrayPort_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getNDArrayPort_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNDArrayAddress() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNDArrayAddress().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NDArrayAddress));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getNDArrayAddress", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setNDArrayAddress(int ndarrayaddress) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getNDArrayAddress().getPv()), ndarrayaddress);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(NDArrayAddress), ndarrayaddress);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot setNDArrayAddress", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNDArrayAddress_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNDArrayAddress_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NDArrayAddress_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getNDArrayAddress_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public boolean isCallbackEnabled() throws Exception {
 		int callback = 0;
 		if (config != null) {
 			callback = EPICS_CONTROLLER.cagetInt(createChannel(config.getEnableCallbacks().getPv()));
 		} else {
 			callback = EPICS_CONTROLLER.cagetInt(getChannel(EnableCallbacks));
 		}
 		if (callback == 1) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void enableCallbacks() throws Exception {
 		if (config != null) {
 			EPICS_CONTROLLER.caputWait(createChannel(config.getEnableCallbacks().getPv()), 1);
 		} else {
 			EPICS_CONTROLLER.caputWait(getChannel(EnableCallbacks), 1);
 		}
 	}
 
 	@Override
 	public void disableCallbacks() throws Exception {
 		if (config != null) {
 			EPICS_CONTROLLER.caputWait(createChannel(config.getEnableCallbacks().getPv()), 0);
 		} else {
 			EPICS_CONTROLLER.caputWait(getChannel(EnableCallbacks), 0);
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public boolean isCallbacksEnabled_RBV() throws Exception {
 		int callback = 0;
 		if (config != null) {
 			callback = EPICS_CONTROLLER.cagetInt(createChannel(config.getEnableCallbacks_RBV().getPv()));
 		} else {
 			callback = EPICS_CONTROLLER.cagetInt(getChannel(EnableCallbacks_RBV));
 		}
 		if (callback == 1) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getMinCallbackTime() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getMinCallbackTime().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(MinCallbackTime));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getMinCallbackTime", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setMinCallbackTime(double mincallbacktime) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getMinCallbackTime().getPv()), mincallbacktime);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(MinCallbackTime), mincallbacktime);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot setMinCallbackTime", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getMinCallbackTime_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getMinCallbackTime_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(MinCallbackTime_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getMinCallbackTime_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getBlockingCallbacks() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getBlockingCallbacks().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(BlockingCallbacks));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getBlockingCallbacks", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setBlockingCallbacks(int blockingcallbacks) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getBlockingCallbacks().getPv()), blockingcallbacks);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(BlockingCallbacks), blockingcallbacks);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot setBlockingCallbacks", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getBlockingCallbacks_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getBlockingCallbacks_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(BlockingCallbacks_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getBlockingCallbacks_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getArrayCounter() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArrayCounter().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(ArrayCounter));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getArrayCounter", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setArrayCounter(int arraycounter) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getArrayCounter().getPv()), arraycounter);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(ArrayCounter), arraycounter);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot setArrayCounter", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getArrayCounter_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArrayCounter_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(ArrayCounter_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getArrayCounter_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getArrayRate_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getArrayRate_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(ArrayRate_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getArrayRate_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getDroppedArrays() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getDroppedArrays().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(DroppedArrays));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getDroppedArrays", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setDroppedArrays(int droppedarrays) throws Exception {
 		if (config != null) {
 			EPICS_CONTROLLER.caputWait(createChannel(config.getDroppedArrays().getPv()), droppedarrays);
 		} else {
 			EPICS_CONTROLLER.caputWait(getChannel(DroppedArrays), droppedarrays);
 		}
 
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getDroppedArrays_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getDroppedArrays_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(DroppedArrays_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getDroppedArrays_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNDimensions_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNDimensions_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NDimensions_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getNDimensions_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getArraySize0_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArraySize0_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySize0_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getArraySize0_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getArraySize1_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArraySize1_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySize1_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getArraySize1_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getArraySize2_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArraySize2_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySize2_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getArraySize2_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getDataType_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDataType_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(DataType_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getDataType_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getColorMode_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getColorMode_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ColorMode_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getColorMode_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getBayerPattern_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getBayerPattern_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(BayerPattern_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getBayerPattern_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getUniqueId_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getUniqueId_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(UniqueId_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getUniqueId_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getTimeStamp_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getTimeStamp_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(TimeStamp_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getTimeStamp_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getNDAttributesFile() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getNDAttributesFile().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(NDAttributesFile));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot getNDAttributesFile", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setNDAttributesFile(String ndattributesfile) throws Exception {
 		try {
 
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getNDAttributesFile().getPv()),
 						(ndattributesfile + '\0').getBytes());
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(NDAttributesFile), (ndattributesfile + '\0').getBytes());
 			}
 
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Cannot setNDAttributesFile", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	 * @return Returns the basePVName.
 	 */
 	public String getBasePVName() {
 		return basePVName;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (deviceName == null && basePVName == null && pvProvider == null) {
 			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
 		}
 	}
 
 	/**
 	 * @param basePVName
 	 *            The basePVName to set.
 	 */
 	public void setBasePVName(String basePVName) {
 		this.basePVName = basePVName;
 	}
 
 	/**
 	 * This method allows to toggle between the method in which the PV is acquired.
 	 * 
 	 * @param pvElementName
 	 * @param args
 	 * @return {@link Channel} to talk to the relevant PV.
 	 * @throws Exception
 	 */
 	private Channel getChannel(String pvElementName, String... args) throws Exception {
 		try {
 			String pvPostFix = null;
 			if (args.length > 0) {
 				// PV element name is different from the pvPostFix
 				pvPostFix = args[0];
 			} else {
 				pvPostFix = pvElementName;
 			}
 
 			String fullPvName;
 			if (pvProvider != null) {
 				fullPvName = pvProvider.getPV(pvElementName);
 			} else {
 				fullPvName = basePVName + pvPostFix;
 			}
 			return createChannel(fullPvName);
 		} catch (Exception exception) {
 			logger.warn("g.d.d.a.v.i.NDPluginBaseImpl -> Problem getting channel", exception);
 			throw exception;
 		}
 	}
 
 	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
 		Channel channel = channelMap.get(fullPvName);
 		if (channel == null) {
 			try {
 				channel = EPICS_CONTROLLER.createChannel(fullPvName);
 			} catch (CAException cae) {
 				logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Problem creating channel", cae);
 				throw cae;
 			} catch (TimeoutException te) {
 				logger.warn("g.d.d.a.v.i.NDPluginBaseImpl-> Problem creating channel", te);
 				throw te;
 
 			}
 			channelMap.put(fullPvName, channel);
 		}
 		return channel;
 	}
 
 	/**
 	 * @return Returns the pvProvider.
 	 */
 	public IPVProvider getPvProvider() {
 		return pvProvider;
 	}
 
 	/**
 	 * @param pvProvider
 	 *            The pvProvider to set.
 	 */
 	public void setPvProvider(IPVProvider pvProvider) {
 		this.pvProvider = pvProvider;
 	}
 
 	@Override
 	public void reset() throws Exception {
 		if (initialArrayAddress != null) {
 			setNDArrayAddress(initialArrayAddress);
 		}
 		if (initialArrayPort != null) {
 			setNDArrayPort(initialArrayPort);
 		}
 
 		if (initialBlockingCallbacks != null){
 			setBlockingCallbacks((short) (initialBlockingCallbacks ?  1 : 0));
 		}
 		
 		if (initialEnableCallbacks != null) {
 			if (initialEnableCallbacks > 0) {
 				enableCallbacks();
 			} else {
 				disableCallbacks();
 			}
 		}
 	}
 
 	/**
 	 * @param initialArrayPort
 	 *            The initialArrayPort to set.
 	 */
 	public void setInitialArrayPort(String initialArrayPort) {
 		this.initialArrayPort = initialArrayPort;
 	}
 
 	/**
 	 * @return Returns the initialArrayPort.
 	 */
 	@Override
 	public String getInitialArrayPort() {
 		return initialArrayPort;
 	}
 
 	/**
 	 * @param initialArrayAddress
 	 *            The initialArrayAddress to set.
 	 */
 	public void setInitialArrayAddress(Integer initialArrayAddress) {
 		this.initialArrayAddress = initialArrayAddress;
 	}
 
 	/**
 	 * @return Returns the initialArrayAddress.
 	 */
 	@Override
 	public Integer getInitialArrayAddress() {
 		return initialArrayAddress;
 	}
 
 	/**
 	 * @param initialBlockingCallbacks
 	 *            The initialBlockingCallbacks to set.
 	 */
 	public void setInitialBlockingCallbacks(Boolean initialBlockingCallbacks) {
 		this.initialBlockingCallbacks = initialBlockingCallbacks;
 	}
 
 	/**
 	 * @return Returns the initialBlockingCallbacks.
 	 */
 	public Boolean getInitialBlockingCallbacks() {
 		return initialBlockingCallbacks;
 	}
 
 	/**
 	 * @param initialEnableCallbacks
 	 *            The initialEnableCallbacks to set.
 	 */
 	public void setInitialEnableCallbacks(int initialEnableCallbacks) {
 		this.initialEnableCallbacks = initialEnableCallbacks;
 	}
 
 	/**
 	 * @return Returns the initialEnableCallbacks.
 	 */
 	public int getInitialEnableCallbacks() {
 		return initialEnableCallbacks;
 	}
 
 	private String getChannelName(String pvElementName, String... args)throws Exception{
 		String pvPostFix = null;
 		if (args.length > 0) {
 			// PV element name is different from the pvPostFix
 			pvPostFix = args[0];
 		} else {
 			pvPostFix = pvElementName;
 		}
 
 		String fullPvName;
 		if (pvProvider != null) {
 			fullPvName = pvProvider.getPV(pvElementName);
 		} else {
 			fullPvName = basePVName + pvPostFix;
 		}
 		return fullPvName;
 	}
 	
 	@Override
 	public Observable<Integer> createArrayCounterObservable() throws Exception {
 		return LazyPVFactory.newReadOnlyIntegerPV(getChannelName(ArrayCounter_RBV));
 	}
 
 	@Override
 	public Observable<Boolean> createConnectionStateObservable() throws Exception {
 		return new ConnectionStateObservable( getChannelName(ArrayCounter_RBV));
 	}
 
 	@Override
 	public Observable<String> createEnableObservable() throws Exception {
 		return LazyPVFactory.newReadOnlyEnumPV(getChannelName(EnableCallbacks_RBV), String.class);
 	}

	@Override
	public Observable<Double> createMinCallbackTimeObservable() throws Exception {
		return LazyPVFactory.newReadOnlyDoublePV(getChannelName(MinCallbackTime_RBV));
	}
 	
 	
 }
 
 class ConnectionStateObservable implements Observable<Boolean>{
 	Channel ch;
 	
 	ObservableUtil<Boolean> delegate= new ObservableUtil<Boolean>();
 
 	@Override
 	public void addObserver(Observer<Boolean> observer) {
 		delegate.addObserver(observer);
 	}
 
 	@Override
 	public void deleteIObserver(Observer<Boolean> observer) {
 		delegate.deleteIObserver(observer);
 	}
 	ConnectionStateObservable(String pv) throws Exception{
 		EpicsController.getInstance().createChannel(pv, new ConnectionListener() {
 			
 			@Override
 			public void connectionChanged(ConnectionEvent arg0) {
 				delegate.notifyIObservers(ConnectionStateObservable.this, arg0.isConnected());
 			}
 		});
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		if( ch != null){
 			EpicsController.getInstance().destroy(ch);
 			ch = null;
 		}
 		super.finalize();
 	}
 	
 }
