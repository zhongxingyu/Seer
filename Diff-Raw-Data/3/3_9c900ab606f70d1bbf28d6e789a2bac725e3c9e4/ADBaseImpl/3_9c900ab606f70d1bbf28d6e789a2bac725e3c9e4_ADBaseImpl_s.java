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
 import gda.configuration.epics.EpicsConfiguration;
 import gda.device.Detector;
 import gda.device.detector.areadetector.AreaDetectorBin;
 import gda.device.detector.areadetector.AreaDetectorROI;
 import gda.device.detector.areadetector.IPVProvider;
 import gda.device.detector.areadetector.impl.AreaDetectorBinImpl;
 import gda.device.detector.areadetector.impl.AreaDetectorROIImpl;
 import gda.device.detector.areadetector.v17.ADBase;
 import gda.epics.LazyPVFactory;
 import gda.epics.PV;
 import gda.epics.connection.EpicsController;
 import gda.epics.interfaces.ADBaseType;
 import gda.factory.FactoryException;
 import gda.observable.Observable;
 import gda.observable.Predicate;
 import gda.scan.ScanBase;
 import gda.util.Sleep;
 import gov.aps.jca.CAException;
 import gov.aps.jca.CAStatus;
 import gov.aps.jca.Channel;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.event.PutEvent;
 import gov.aps.jca.event.PutListener;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 public class ADBaseImpl implements InitializingBean, ADBase {
 
 	public class StartPutListener implements PutListener {
 		@Override
 		public void putCompleted(PutEvent event) {
 			if (event.getStatus() != CAStatus.NORMAL) {
 				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(),
 						event.getStatus());
 				setStatus(Detector.FAULT);
 				return;
 			}
 			logger.info("Acquisition request completed: {} called back.", ((Channel) event.getSource()).getName());
 			setStatus(Detector.IDLE);
 		}
 	}
 
 	// Setup the logging facilities
 	static final Logger logger = LoggerFactory.getLogger(ADBaseImpl.class);
 
 	protected final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
 
 	private String basePVName;
 
 	private IPVProvider pvProvider;
 
 	/**
 	 * Map that stores the channel against the PV name
 	 */
 	private Map<String, Channel> channelMap = new HashMap<String, Channel>();
 
 	private Integer initialMinX;
 
 	private Integer initialMinY;
 
 	private Integer initialSizeX;
 
 	private Integer initialSizeY;
 
 	private String initialDataType;
 
 	private Integer initialBinX;
 
 	private Integer initialBinY;
 
 	private ADBaseType config;
 	private String deviceName;
 
 	private String initialNDAttributesFile;
 
 	private StartPutListener startputlistener = new StartPutListener();
 
 	private volatile int status = Detector.IDLE;
 
 	private Object statusMonitor = new Object();
 
 	private PV<Integer> pvArrayCounter_RBV;
 
 	private PV<Integer> pvDetectorState_RBV;
 
 	/**
 	*
 	*/
 	@Override
 	public String getPortName_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetString(createChannel(config.getPortName_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(PortName_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getPortName_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getManufacturer_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetString(createChannel(config.getManufacturer_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(Manufacturer_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getManufacturer_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getModel_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetString(createChannel(config.getModel_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(Model_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getModel_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getMaxSizeX_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMaxSizeX_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(MaxSizeX_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getMaxSizeX_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getMaxSizeY_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMaxSizeY_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(MaxSizeY_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getMaxSizeY_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getDataType() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDataType().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(DataType));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getDataType", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setDataType(String datatype) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getDataType().getPv()), datatype);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(DataType), datatype);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setDataType", ex);
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
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getDataType_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getColorMode() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getColorMode().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ColorMode));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getColorMode", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setColorMode(int colormode) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getColorMode().getPv()), colormode);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ColorMode), colormode);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setColorMode", ex);
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
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getColorMode_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getBinX() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinX().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(BinX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getBinX", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setBinX(int binx) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getBinX().getPv()), binx);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(BinX), binx);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setBinX", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getBinX_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinX_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(BinX_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getBinX_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getBinY() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinY().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(BinY));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getBinY", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setBinY(int biny) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getBinY().getPv()), biny);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(BinY), biny);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setBinY", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getBinY_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinY_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(BinY_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getBinY_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getMinX() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinX().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(MinX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getMinX", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setMinX(int minx) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getMinX().getPv()), minx);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(MinX), minx);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setMinX", ex);
 			throw ex;
 		}
 	}
 
 	@Override
 	public void setMinXWait(int minx, double timeout) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getMinX().getPv()), minx, timeout);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(MinX), minx, timeout);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setMinX", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getMinX_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinX_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(MinX_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getMinX_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getMinY() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinY().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(MinY));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getMinY", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setMinY(int miny) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getMinY().getPv()), miny);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(MinY), miny);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setMinY", ex);
 			throw ex;
 		}
 	}
 	@Override
 	public void setMinYWait(int miny, double timeout) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getMinY().getPv()), miny, timeout);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(MinY), miny,timeout);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setMinY", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getMinY_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinY_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(MinY_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getMinY_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getSizeX() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getSizeX", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setSizeX(int sizex) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getSizeX().getPv()), sizex);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(SizeX), sizex);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setSizeX", ex);
 			throw ex;
 		}
 	}
 	/**
 	*
 	*/
 	@Override
 	public void setSizeXWait(int sizex, double timeout) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getSizeX().getPv()), sizex, timeout);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(SizeX), sizex, timeout);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setSizeX", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getSizeX_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getSizeX_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getSizeY() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getSizeY", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setSizeY(int sizey) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getSizeY().getPv()), sizey);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(SizeY), sizey);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setSizeY", ex);
 			throw ex;
 		}
 	}
 	/**
 	*
 	*/
 	@Override
 	public void setSizeYWait(int sizey, double timeout) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getSizeY().getPv()), sizey, timeout);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(SizeY), sizey, timeout);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setSizeY", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getSizeY_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getSizeY_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getReverseX() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReverseX().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ReverseX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getReverseX", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setReverseX(int reversex) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getReverseX().getPv()), reversex);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ReverseX), reversex);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setReverseX", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getReverseX_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReverseX_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ReverseX_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getReverseX_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getReverseY() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReverseY().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ReverseY));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getReverseY", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setReverseY(int reversey) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getReverseY().getPv()), reversey);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ReverseY), reversey);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setReverseY", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getReverseY_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReverseY_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ReverseY_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getReverseY_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getArraySizeX_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArraySizeX_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySizeX_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getArraySizeX_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getArraySizeY_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArraySizeY_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySizeY_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getArraySizeY_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getArraySizeZ_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArraySizeZ_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySizeZ_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getArraySizeZ_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getArraySize_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArraySize_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySize_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getArraySize_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getAcquireTime() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getAcquireTime().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(AcquireTime));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getAcquireTime", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setAcquireTime(double acquiretime) throws Exception {
 		logger.debug("Setting Acquire time to {} (for '{}')", acquiretime, Acquire);
 
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getAcquireTime().getPv()), acquiretime);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(AcquireTime), acquiretime);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setAcquireTime", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getAcquireTime_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getAcquireTime_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(AcquireTime_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getAcquireTime_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getAcquirePeriod() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getAcquirePeriod().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(AcquirePeriod));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getAcquirePeriod", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setAcquirePeriod(double acquireperiod) throws Exception {
 		logger.debug("Setting Acquire period to {} (for '{}')", acquireperiod, Acquire);
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getAcquirePeriod().getPv()), acquireperiod);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(AcquirePeriod), acquireperiod);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setAcquirePeriod", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getAcquirePeriod_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getAcquirePeriod_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(AcquirePeriod_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getAcquirePeriod_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getTimeRemaining_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getTimeRemaining_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(TimeRemaining_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getTimeRemaining_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getGain() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getGain().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(Gain));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getGain", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setGain(double gain) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getGain().getPv()), gain);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(Gain), gain);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setGain", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getGain_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getGain_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(Gain_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getGain_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getFrameType() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getFrameType().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(FrameType));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getFrameType", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setFrameType(int frametype) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getFrameType().getPv()), frametype);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(FrameType), frametype);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setFrameType", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getFrameType_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getFrameType_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(FrameType_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getFrameType_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getImageMode() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getImageMode().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ImageMode));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getImageMode", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setImageMode(int imagemode) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getImageMode().getPv()), imagemode);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ImageMode), imagemode);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setImageMode", ex);
 			throw ex;
 		}
 	}
 
 	@Override
 	public void setImageModeWait(ImageMode imagemode) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getImageMode().getPv()), imagemode.ordinal());
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(ImageMode), imagemode.ordinal());
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setImageMode", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getImageMode_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getImageMode_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ImageMode_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getImageMode_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getTriggerMode() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getTriggerMode().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(TriggerMode));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getTriggerMode", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setTriggerMode(int triggermode) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getTriggerMode().getPv()), triggermode);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(TriggerMode), triggermode);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setTriggerMode", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getTriggerMode_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getTriggerMode_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(TriggerMode_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getTriggerMode_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNumExposures() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumExposures().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NumExposures));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getNumExposures", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setNumExposures(int numexposures) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getNumExposures().getPv()), numexposures);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(NumExposures), numexposures);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setNumExposures", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setNumExposures(int numexposures, double timeout) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getNumExposures().getPv()), numexposures, timeout);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(NumExposures), numexposures, timeout);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setNumExposures", ex);
 			throw ex;
 		}
 	}
 	/**
 	*
 	*/
 	@Override
 	public int getNumExposures_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumExposures_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NumExposures_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getNumExposures_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNumExposuresCounter_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumExposuresCounter_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NumExposuresCounter_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getNumExposuresCounter_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNumImages() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumImages().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NumImages));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getNumImages", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setNumImages(int numimages) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getNumImages().getPv()), numimages);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(NumImages), numimages);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setNumImages", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNumImages_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumImages_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NumImages_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getNumImages_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getNumImagesCounter_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumImagesCounter_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(NumImagesCounter_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getNumImagesCounter_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public int getAcquireState() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetInt(createChannel(config.getAcquire().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetInt(getChannel(Acquire));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getAcquireState", ex);
 			throw ex;
 		}
 	}
 
 	@Override
 	public void startAcquiring() throws Exception {
 		logger.debug("Acquisition started: {} called.", Acquire);
 		try {
 			if (getAcquireState() == 1) {
 				return; // if continuing acquiring already started,do not put another call again as EPICS will not
 						// callback at all in this case.
 			}
 			setStatus(Detector.BUSY);
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getAcquire().getPv()), 1, startputlistener);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(Acquire), 1, startputlistener);
 			}
 		} catch (Exception e) {
 			setStatus(Detector.IDLE);
 			logger.error("Exception on start Acquirig", e);
 			throw e;
 		}
 	}
 
 	@Override
 	public void startAcquiringSynchronously() throws Exception {
 		int countBefore = getArrayCounter_RBV();
 		startAcquiring();
 		while (getStatus() != Detector.IDLE && getStatus() != Detector.FAULT) {
 			if( getAcquireState()==0)
 				throw new Exception("Camera is not acquiring but putListener has not been called");
			Sleep.sleep(100);
 		}
 		if (getStatus() == Detector.FAULT) {
 			logger.debug("detector in a fault state");
 		}
 		int countAfter = getArrayCounter_RBV();
 		if( countAfter==countBefore)
 			throw new Exception("Acquire completed but counter did not increment");
 	}
 
 	@Override
 	public void stopAcquiring() throws Exception {
 		try {
 				if (config != null) {
 					EPICS_CONTROLLER.caput(createChannel(config.getAcquire().getPv()), 0);
 				} else {
 					EPICS_CONTROLLER.caput(getChannel(Acquire), 0);
 				}
 		} catch (Exception e) {
 			logger.error("Exception on stop Acquiring", e);
 			throw e;
 		} finally {
 			// If the acquisition state is busy then wait for it to complete.
 			while (getAcquireState() == 1) {
 				logger.info("sleeping for 25");
 				Sleep.sleep(25);
 			}
 			setStatus(Detector.IDLE);
 			logger.info("Stopping detector acquisition");
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getAcquire_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getAcquire_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(Acquire_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getAcquire_RBV", ex);
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
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getArrayCounter", ex);
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
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setArrayCounter", ex);
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
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getArrayCounter_RBV", ex);
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
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getArrayRate_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	 * @throws InterruptedException
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	@Override
 	public short getDetectorState_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDetectorState_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(DetectorState_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getDetectorState_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getArrayCallbacks() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getArrayCallbacks().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ArrayCallbacks));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getArrayCallbacks", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setArrayCallbacks(int arraycallbacks) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caputWait(createChannel(config.getArrayCallbacks().getPv()), arraycallbacks);
 			} else {
 				EPICS_CONTROLLER.caputWait(getChannel(ArrayCallbacks), arraycallbacks);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setArrayCallbacks", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getArrayCallbacks_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getArrayCallbacks_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ArrayCallbacks_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getArrayCallbacks_RBV", ex);
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
 				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getNDAttributesFile().getPv())))
 						.trim();
 			}
 			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(NDAttributesFile))).trim();
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getNDAttributesFile", ex);
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
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setNDAttributesFile", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getStatusMessage_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getStatusMessage_RBV().getPv())))
 						.trim();
 			}
 			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(StatusMessage_RBV))).trim();
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getStatusMessage_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getStringToServer_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return new String(
 						EPICS_CONTROLLER.cagetByteArray(createChannel(config.getStringToServer_RBV().getPv()))).trim();
 			}
 			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(StringToServer_RBV))).trim();
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getStringToServer_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getStringFromServer_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getStringFromServer_RBV()
 						.getPv()))).trim();
 			}
 			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(StringFromServer_RBV))).trim();
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getStringFromServer_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getReadStatus() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReadStatus().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ReadStatus));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getReadStatus", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setReadStatus(int readstatus) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getReadStatus().getPv()), readstatus);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ReadStatus), readstatus);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setReadStatus", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getShutterMode() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShutterMode().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterMode));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterMode", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setShutterMode(int shuttermode) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getShutterMode().getPv()), shuttermode);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ShutterMode), shuttermode);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setShutterMode", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getShutterMode_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShutterMode_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterMode_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterMode_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getShutterControl() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShutterControl().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterControl));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterControl", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setShutterControl(int shuttercontrol) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getShutterControl().getPv()), shuttercontrol);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ShutterControl), shuttercontrol);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setShutterControl", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getShutterControl_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShutterControl_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterControl_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterControl_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getShutterStatus_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShutterStatus_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterStatus_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterStatus_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getShutterOpenDelay() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getShutterOpenDelay().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(ShutterOpenDelay));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterOpenDelay", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setShutterOpenDelay(double shutteropendelay) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getShutterOpenDelay().getPv()), shutteropendelay);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ShutterOpenDelay), shutteropendelay);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setShutterOpenDelay", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getShutterOpenDelay_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getShutterOpenDelay_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(ShutterOpenDelay_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterOpenDelay_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getShutterCloseDelay() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getShutterCloseDelay().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(ShutterCloseDelay));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterCloseDelay", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setShutterCloseDelay(double shutterclosedelay) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getShutterCloseDelay().getPv()), shutterclosedelay);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ShutterCloseDelay), shutterclosedelay);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setShutterCloseDelay", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getShutterCloseDelay_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getShutterCloseDelay_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(ShutterCloseDelay_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterCloseDelay_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getShutterOpenEPICSPV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getShutterOpenEPICSPV().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(ShutterOpenEPICSPV_ELEMENTNAME, ShutterOpenEPICSPV_PVPOSTFIX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterOpenEPICSPV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setShutterOpenEPICSPV(String shutteropenepicspv) throws Exception {
 		logger.warn("Problem with PV Name - ShutterOpenEPICSPV -> ShutterOpenEPICS.OUT");
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getShutterOpenEPICSPV().getPv()), shutteropenepicspv);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ShutterOpenEPICSPV_ELEMENTNAME, ShutterOpenEPICSPV_PVPOSTFIX),
 						shutteropenepicspv);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setShutterOpenEPICSPV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getShutterOpenEPICSCmd() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getShutterOpenEPICSCmd().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(ShutterOpenEPICSCmd_ElEMENTNAME, ShutterOpenEPICSCmd_PVPOSTFIX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterOpenEPICSCmd", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setShutterOpenEPICSCmd(String shutteropenepicscmd) throws Exception {
 		logger.warn("Problem with PV Name - ShutterOpenEPICSCmd -> ShutterOpenEPICSCmd.OCAL");
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getShutterOpenEPICSCmd().getPv()), shutteropenepicscmd);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ShutterOpenEPICSCmd_ElEMENTNAME, ShutterOpenEPICSCmd_PVPOSTFIX),
 						shutteropenepicscmd);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setShutterOpenEPICSCmd", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getShutterCloseEPICSPV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getShutterCloseEPICSPV().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(ShutterCloseEPICSPV_ELEMENTNAME, ShutterCloseEPICSPV_PVPOSTFIX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterCloseEPICSPV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setShutterCloseEPICSPV(String shuttercloseepicspv) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getShutterCloseEPICSPV().getPv()), shuttercloseepicspv);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ShutterCloseEPICSPV_ELEMENTNAME, ShutterCloseEPICSPV_PVPOSTFIX),
 						shuttercloseepicspv);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setShutterCloseEPICSPV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getShutterCloseEPICSCmd() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getShutterCloseEPICSCmd().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(ShutterCloseEPICSCmd_ELEMENTNAME, ShutterCloseEPICSCmd_PVPOSTFIX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterCloseEPICSCmd", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setShutterCloseEPICSCmd(String shuttercloseepicscmd) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getShutterCloseEPICSCmd().getPv()), shuttercloseepicscmd);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ShutterCloseEPICSCmd_ELEMENTNAME, ShutterCloseEPICSCmd_PVPOSTFIX),
 						shuttercloseepicscmd);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setShutterCloseEPICSCmd", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public short getShutterStatusEPICS_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShutterStatusEPICS_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterStatusEPICS_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterStatusEPICS_RBV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getShutterStatusEPICSPV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getShutterStatusEPICSPV().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(ShutterStatusEPICSPV_ELEMENTNAME, ShutterStatusEPICSPV_PVPOSTFIX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterStatusEPICSPV", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getShutterStatusEPICSCloseVal() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getShutterStatusEPICSCloseVal().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(ShutterStatusEPICSCloseVal_ELEMENTNAME,
 					ShutterStatusEPICSCloseVal_PVPOSTFIX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterStatusEPICSCloseVal", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public String getShutterStatusEPICSOpenVal() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.caget(createChannel(config.getShutterStatusEPICSOpenVal().getPv()));
 			}
 			return EPICS_CONTROLLER.caget(getChannel(ShutterStatusEPICSOpenVal_ELEMENTNAME,
 					ShutterStatusEPICSOpenVal_PVPOSTFIX));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getShutterStatusEPICSOpenVal", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getTemperature() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getTemperature().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(Temperature));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getTemperature", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public void setTemperature(double temperature) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getTemperature().getPv()), temperature);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(Temperature), temperature);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setTemperature", ex);
 			throw ex;
 		}
 	}
 
 	/**
 	*
 	*/
 	@Override
 	public double getTemperature_RBV() throws Exception {
 		try {
 			if (config != null) {
 				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getTemperature_RBV().getPv()));
 			}
 			return EPICS_CONTROLLER.cagetDouble(getChannel(Temperature_RBV));
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot getTemperature_RBV", ex);
 			throw ex;
 		}
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (deviceName == null && basePVName == null && pvProvider == null) {
 			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
 		}
 
 		pvArrayCounter_RBV = LazyPVFactory.newIntegerPV((config == null) ? genenerateFullPvName(ArrayCounter_RBV)
 				: config.getArrayCounter_RBV().getPv());
 		
 		pvDetectorState_RBV = LazyPVFactory.newIntegerPV((config == null) ? genenerateFullPvName(DetectorState_RBV)
 				: config.getDetectorState_RBV().getPv());
 	}
 
 	/**
 	 * @return Returns the basePVName.
 	 */
 	public String getBasePVName() {
 		return basePVName;
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
 	protected Channel getChannel(String pvElementName, String... args) throws Exception {
 		try {
 			String fullPvName = genenerateFullPvName(pvElementName, args);
 			return createChannel(fullPvName);
 		} catch (Exception exception) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Problem getting channel", exception);
 			throw exception;
 		}
 	}
 
 	protected String genenerateFullPvName(String pvElementName, String... args) throws Exception {
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
 
 	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
 		Channel channel = channelMap.get(fullPvName);
 		if (channel == null) {
 			try {
 				channel = EPICS_CONTROLLER.createChannel(fullPvName);
 			} catch (CAException cae) {
 				logger.warn("g.d.d.a.v.i.ADBaseImpl-> Problem creating channel", cae);
 				throw cae;
 			} catch (TimeoutException te) {
 				logger.warn("g.d.d.a.v.i.ADBaseImpl-> Problem creating channel", te);
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
 	public int getInitialMinX() {
 		return initialMinX;
 	}
 
 	@Override
 	public int getInitialMinY() {
 		return initialMinY;
 	}
 
 	@Override
 	public int getInitialSizeX() {
 		return initialSizeX;
 	}
 
 	@Override
 	public int getInitialSizeY() {
 		return initialSizeY;
 	}
 
 	/**
 	 * @param initialMinX
 	 *            The initialMinX to set.
 	 */
 	public void setInitialMinX(int initialMinX) {
 		this.initialMinX = initialMinX;
 	}
 
 	/**
 	 * @param initialMinY
 	 *            The initialMinY to set.
 	 */
 	public void setInitialMinY(int initialMinY) {
 		this.initialMinY = initialMinY;
 	}
 
 	/**
 	 * @param initialSizeX
 	 *            The initialSizeX to set.
 	 */
 	public void setInitialSizeX(int initialSizeX) {
 		this.initialSizeX = initialSizeX;
 	}
 
 	/**
 	 * @param initialSizeY
 	 *            The initialSizeY to set.
 	 */
 	public void setInitialSizeY(int initialSizeY) {
 		this.initialSizeY = initialSizeY;
 	}
 
 	@Override
 	public AreaDetectorROI getAreaDetectorROI() throws Exception {
 		return new AreaDetectorROIImpl(getMinX(), getMinY(), getSizeX(), getSizeY());
 	}
 
 	@Override
 	public AreaDetectorBin getBinning() throws Exception {
 		return new AreaDetectorBinImpl(getBinX_RBV(), getBinY_RBV());
 	}
 
 	@Override
 	public String getInitialDataType() {
 		return initialDataType;
 	}
 
 	/**
 	 * @param initialDataType
 	 *            The initialDataType to set.
 	 */
 	public void setInitialDataType(String initialDataType) {
 		this.initialDataType = initialDataType;
 	}
 
 	public String getInitialNDAttributesFile() {
 		return initialNDAttributesFile;
 	}
 
 	/**
 	 * @param initialNDAttributesFile
 	 *            The initialNDAttributesFile to set.
 	 */
 	public void setInitialNDAttributesFile(String initialNDAttributesFile) {
 		this.initialNDAttributesFile = initialNDAttributesFile;
 	}
 
 	@Override
 	public void reset() throws Exception {
 		if (initialDataType != null)
 			setDataType(initialDataType);
 		if ((initialMinX != null) && (initialMinY != null) && (initialSizeX != null) && (initialSizeY != null)) {
 			setAreaDetectorROI(initialMinX, initialMinY, initialSizeX, initialSizeY);
 		}
 		if ((initialBinX != null) && (initialBinY != null)) {
 			setBinning(initialBinX, initialBinY);
 		}
 		if (initialNDAttributesFile != null) {
 			setNDAttributesFile(initialNDAttributesFile);
 		}
 		setStatus(Detector.IDLE);
 	}
 
 	/**
 	 * @param binX
 	 * @param binY
 	 */
 	private void setBinning(Integer binX, Integer binY) throws Exception {
 		setBinX(binX);
 		setBinY(binY);
 	}
 
 	/**
 	 * @param minX
 	 * @param minY
 	 * @param sizeX
 	 * @param sizeY
 	 */
 	private void setAreaDetectorROI(Integer minX, Integer minY, Integer sizeX, Integer sizeY) throws Exception {
 		setMinX(minX);
 		setMinY(minY);
 		setSizeX(sizeX);
 		setSizeY(sizeY);
 	}
 
 	/**
 	 * @param initialBinX
 	 *            The initialBinX to set.
 	 */
 	public void setInitialBinX(Integer initialBinX) {
 		this.initialBinX = initialBinX;
 	}
 
 	/**
 	 * @param initialBinY
 	 *            The initialBinY to set.
 	 */
 	public void setInitialBinY(Integer initialBinY) {
 		this.initialBinY = initialBinY;
 	}
 
 	protected EpicsConfiguration epicsConfiguration;
 
 	/**
 	 * Sets the EpicsConfiguration to use when looking up PV from deviceName.
 	 * 
 	 * @param epicsConfiguration
 	 *            the EpicsConfiguration
 	 */
 	public void setEpicsConfiguration(EpicsConfiguration epicsConfiguration) {
 		this.epicsConfiguration = epicsConfiguration;
 	}
 
 	/**
 	 * @return Returns the deviceName.
 	 */
 	public String getDeviceName() {
 		return deviceName;
 	}
 
 	/**
 	 * @param deviceName
 	 *            The deviceName to set.
 	 * @throws FactoryException
 	 */
 	public void setDeviceName(String deviceName) throws FactoryException {
 		this.deviceName = deviceName;
 		initializeConfig();
 	}
 
 	private void initializeConfig() throws FactoryException {
 		if (deviceName != null) {
 			try {
 				if (epicsConfiguration != null) {
 					config = epicsConfiguration.getConfiguration(getDeviceName(), ADBaseType.class);
 				} else {
 					config = Configurator.getConfiguration(getDeviceName(), ADBaseType.class);
 				}
 			} catch (ConfigurationNotFoundException e) {
 				logger.error("EPICS configuration for device {} not found", getDeviceName());
 				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", e);
 			}
 		}
 	}
 
 	@Override
 	public void setStatus(int status) {
 		synchronized (statusMonitor) {
 			this.status = status;
 			this.statusMonitor.notifyAll();
 		}
 	}
 
 	@Override
 	public int waitWhileStatusBusy() throws InterruptedException {
 		synchronized (statusMonitor) {
 			try{
 				while (status == Detector.BUSY) {
 					statusMonitor.wait(1000);
 					ScanBase.checkForInterrupts();
 				}
 			}  finally{
 				//if interrupted clear the status state as the IOC may have crashed
 				if ( status != 0)
 					setStatus(0);
 			}
 			return status;
 		}
 	}
 
 	@Override
 	public int getStatus() {
 		return status;
 	}
 
 	@Override
 	public void getEPICSStatus() throws Exception {
 		this.status = getAcquireState();
 	}
 
 	class GreaterThanOrEqualTo implements Predicate<Integer> {
 
 		private final int value;
 
 		public GreaterThanOrEqualTo(int value) {
 			this.value = value;
 		}
 
 		@Override
 		public boolean apply(Integer object) {
 			return (object >= value);
 		}
 
 	}
 
 	@Override
 	public void waitForArrayCounterToReach(final int exposureNumber, double timeoutS) throws InterruptedException,
 			Exception, java.util.concurrent.TimeoutException {
 
 		pvArrayCounter_RBV.setValueMonitoring(true);
 		pvArrayCounter_RBV.waitForValue(new Predicate<Integer>() {
 			@Override
 			public boolean apply(Integer object) {
 				return (object >= exposureNumber);
 			}
 		}, timeoutS);
 	}
 
 	@Override
 	public void waitForDetectorStateIDLE(double timeoutS) throws InterruptedException,
 			Exception, java.util.concurrent.TimeoutException {
 
 		pvDetectorState_RBV.setValueMonitoring(true);
 		pvDetectorState_RBV.waitForValue(new Predicate<Integer>() {
 			@Override
 			public boolean apply(Integer object) {
 				return (object == 0);
 			}
 		}, timeoutS);
 	}
 
 	
 	
 	private String getChannelName(String pvElementName, String... args)throws Exception{
 		return genenerateFullPvName(pvElementName, args);
 	}	
 	
 	@Override
 	public Observable<Short> createAcquireStateObservable() throws Exception {
 		return LazyPVFactory.newReadOnlyEnumPV(getChannelName(Acquire_RBV), Short.class);
 	}
 
 	@Override
 	public Observable<Double> createAcquireTimeObservable() throws Exception {
 		return LazyPVFactory.newReadOnlyDoublePV(getChannelName(AcquireTime_RBV));
 	}
 
 	@Override
 	public void setImageMode(ImageMode imagemode) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getImageMode().getPv()), imagemode.ordinal());
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ImageMode), imagemode.ordinal());
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setImageMode", ex);
 			throw ex;
 		}
 
 	}
 	@Override
 	public void setImageModeWait(ImageMode imagemode, double timeout) throws Exception {
 		try {
 			if (config != null) {
 				EPICS_CONTROLLER.caput(createChannel(config.getImageMode().getPv()), imagemode.ordinal(), timeout);
 			} else {
 				EPICS_CONTROLLER.caput(getChannel(ImageMode), imagemode.ordinal(), timeout);
 			}
 		} catch (Exception ex) {
 			logger.warn("g.d.d.a.v.i.ADBaseImpl-> Cannot setImageMode", ex);
 			throw ex;
 		}
 
 	}
 	
 	
 
 }
