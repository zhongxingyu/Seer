 package de.ptb.epics.eve.data.scandescription.defaults;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.datatype.Duration;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 import de.ptb.epics.eve.data.DataTypes;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.scandescription.Axis;
 import de.ptb.epics.eve.data.scandescription.Chain;
 import de.ptb.epics.eve.data.scandescription.Channel;
 import de.ptb.epics.eve.data.scandescription.PositionMode;
 import de.ptb.epics.eve.data.scandescription.ScanDescription;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.util.io.*;
 
 /**
  * Tries to read a given XML file (as defined in 
  * de.ptb.epics.eve.resources/cfg/defaults.xsd) for default values.
  * Values for individual devices can later be requested via 
  * {@link #getAxis(String)} and {@link #getChannel(String)}.
  * 
  * @author Marcus Michalsky
  * @since 1.8
  */
 public class DefaultsManager {
 	private static final Logger LOGGER = Logger.getLogger(DefaultsManager.class
 			.getName());
 	
 	private Defaults defaults;
 	private boolean initialized;
 	
 	/** */
 	public DefaultsManager() {
 		this.defaults = null;
 		this.initialized = false;
 	}
 	
 	/**
 	 * @param pathToDefaults the location of the defaults file
 	 * @param schema the schema file
 	 */
 	public void init(File pathToDefaults, File schema) {
 		if (!pathToDefaults.exists()) {
 			LOGGER.debug("no defaults file found.");
 			defaults = null;
 			return;
 		}
 		SchemaFactory schemaFactory = SchemaFactory.newInstance(
 				XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		try {
 			Schema schemaFile = schemaFactory.newSchema(schema);
 			JAXBContext jaxbContext = JAXBContext.newInstance(Defaults.class);
 			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
 			jaxbUnmarshaller.setSchema(schemaFile);
 			this.defaults = (Defaults) jaxbUnmarshaller
 					.unmarshal(pathToDefaults);
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("found defaults: \n" + this.defaults.toString());
 			} else {
 				LOGGER.info("found defaults");
 			}
 			this.initialized = true;
 		} catch(JAXBException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (SAXException e) {
 			LOGGER.error(e.getMessage(), e);
 		}
 	}
 	
 	/**
 	 * Returns whether the manager is initialized.
 	 * 
 	 * @return whether the manager is initialized
 	 */
 	public boolean isInitialized() {
 		return this.initialized;
 	}
 	
 	/**
 	 * ... {@link #isInitialized()} should be checked before.
 	 * 
 	 * @param targetFile the destination
 	 * @param schemaFile the schema file
 	 */
 	public synchronized void save(File targetFile, File schemaFile) {
 		File backup = new File(targetFile.getParent() + "/"
 				+ targetFile.getName() + ".bup");
 		if (targetFile.exists()) {
 			try {
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug("target file already exists, creating backup");
 				}
 				FileUtil.copyFile(targetFile, backup);
 			} catch (IOException e) {
 				LOGGER.error(e.getMessage(), e);
 			}
 		}
		
		this.defaults.sort();
		
 		SchemaFactory schemaFactory = SchemaFactory.newInstance(
 				XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		try {
 			JAXBContext jaxbContext = JAXBContext.newInstance(Defaults.class);
 			Schema schema = schemaFactory.newSchema(schemaFile);
 			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
 			jaxbMarshaller.setSchema(schema);
 			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 			jaxbMarshaller.marshal(this.defaults, targetFile);
 			LOGGER.info("defaults saved");
 		} catch (JAXBException e) {
 			LOGGER.error(e.getMessage(), e);
 			try {
				LOGGER.info("saving defaults failed -> restoring backup.");
 				FileUtil.copyFile(backup, targetFile);
 			} catch (IOException e1) {
 				LOGGER.error("restoring backup has failed.");
 			}
 		} catch (SAXException e) {
 			LOGGER.error(e.getMessage(), e);
 			try {
				LOGGER.info("saving defaults failed -> restoring backup.");
 				FileUtil.copyFile(backup, targetFile);
 			} catch (IOException e1) {
 				LOGGER.error("restoring backup has failed.");
 			}
 		}
 	}
 	
 	/**
 	 * Returns the axis with the given id or <code>null</code> if not found.
 	 * 
 	 * @param id the id of the axis to get defaults for
 	 * @return the axis with the given id or <code>null</code> if not found.
 	 */
 	public DefaultsAxis getAxis(String id) {
 		if (this.defaults == null) {
 			return null;
 		}
 		for (DefaultsAxis axis : this.defaults.getAxes()) {
 			if (axis.getId().equals(id)) {
 				return axis;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the channel with the given id or <code>null</code> if not found.
 	 * 
 	 * @param id the id of the channel to get defaults for
 	 * @return the channel with the given id or <code>null</code> if not found.
 	 */
 	public DefaultsChannel getChannel(String id) {
 		if (this.defaults == null) {
 			return null;
 		}
 		for (DefaultsChannel channel : this.defaults.getChannels()) {
 			if (channel.getId().equals(id)) {
 				return channel;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Transfers the properties from the given default channel to the 
 	 * target channel.
 	 * 
 	 * @param from the source values (defaults)
 	 * @param to the target channel
 	 * @param measuringStation the device definition
 	 */
 	public static void transferDefaults(DefaultsChannel from, Channel to, 
 			IMeasuringStation measuringStation) {
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("transfering defaults for "
 					+ to.getAbstractDevice().getID());
 		}
 		try {
 			to.setAverageCount(from.getAverageCount());
 		} catch (NullPointerException e) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("no default for average count of "
 						+ to.getAbstractDevice().getID());
 			}
 		}
 		try {
 			to.setMaxAttempts(from.getMaxAttempts());
 		} catch (NullPointerException e) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("no default for max attempts of "
 						+ to.getAbstractDevice().getID());
 			}
 		}
 		try {
 			to.setMaxDeviation(from.getMaxDeviation());
 		} catch (NullPointerException e) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("no default for max deviation of "
 						+ to.getAbstractDevice().getID());
 			}
 		}
 		try {
 			to.setMinimum(from.getMinimum());
 		} catch (NullPointerException e) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("no default for minimum of "
 						+ to.getAbstractDevice().getID());
 			}
 		}
 		try {
 			to.setNormalizeChannel(measuringStation.getDetectorChannelById(from
 					.getNormalizeId()));
 		} catch (NullPointerException e) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("no default for normalize channel of "
 						+ to.getAbstractDevice().getID());
 			}
 		}
 		to.setDeferred(from.isDeferred());
 	}
 	
 	/**
 	 * Transfers the properties from the given default axis to the 
 	 * target axis.
 	 * 
 	 * @param from the source values (defaults)
 	 * @param to the target axis
 	 */
 	public static void transferDefaults(DefaultsAxis from, Axis to) {
 		to.setStepfunction(from.getStepfunction());
 		to.setPositionMode(from.getPositionmode());
 		switch (from.getStepfunction()) {
 		case ADD:
 		case MULTIPLY:
 			to.setMainAxis(from.isMainAxis());
 			switch (to.getMotorAxis().getType()) {
 			case DATETIME:
 				if (to.getPositionMode().equals(PositionMode.ABSOLUTE)) {
 					to.setStart((Date)from.getStart());
 					to.setStop((Date)from.getStop());
 					to.setStepwidth((Date)from.getStepwidth());
 				} else {
 					to.setStart((Duration)from.getStart());
 					to.setStop((Duration)from.getStop());
 					to.setStepwidth((Duration)from.getStepwidth());
 				}
 				break;
 			case DOUBLE:
 				to.setStart((Double)from.getStart());
 				to.setStop((Double)from.getStop());
 				to.setStepwidth((Double)from.getStepwidth());
 				break;
 			case INT:
 				to.setStart((Integer)from.getStart());
 				to.setStop((Integer)from.getStop());
 				to.setStepwidth((Integer)from.getStepwidth());
 				break;
 			default:
 				break;
 			}
 			break;
 		case PLUGIN:
 			/*
 			 * plugin modifications have to be made elsewhere
 			 * (not when the device is created, but when a plugin is selected)
 			PluginController controller = to.getPluginController();
 			for (DefaultsAxisPluginParameter param : from.getPlugin()
 					.getParameters()) {
 				if (controller.get(param.getName()) != null) {
 					controller.set(param.getName(), param.getValue());
 				}
 			}
 			*/
 			// TODO
 			break;
 		case FILE:
 			to.setFile(new File(from.getFile()));
 			break;
 		case POSITIONLIST:
 			to.setPositionlist(from.getPositionList());
 			break;
 		}
 	}
 	
 	/**
 	 * Returns a 
 	 * {@link de.ptb.epics.eve.data.scandescription.defaults.DefaultsAxis} for 
 	 * the given {@link de.ptb.epics.eve.data.scandescription.Axis}.
 	 * 
 	 * @param axis the axis to convert
 	 * @return the converted defaults axis
 	 */
 	public static DefaultsAxis getDefaultsAxis(Axis axis) {
 		DefaultsAxis defaultsAxis = new DefaultsAxis();
 		defaultsAxis.setId(axis.getMotorAxis().getID());
 		defaultsAxis.setPositionmode(axis.getPositionMode());
 		defaultsAxis.setStepfunction(axis.getStepfunction());
 		switch(axis.getStepfunction()) {
 		case ADD:
 		case MULTIPLY:
 			defaultsAxis.setMainAxis(axis.isMainAxis());
 			switch(axis.getType()) {
 			case DATETIME:
 				defaultsAxis.setStartStopStepType(DataTypes.DATETIME);
 				defaultsAxis.setStart((Date)axis.getStart());
 				defaultsAxis.setStop((Date)axis.getStop());
 				defaultsAxis.setStepwidth((Date)axis.getStepwidth());
 				break;
 			case DOUBLE:
 				defaultsAxis.setStartStopStepType(DataTypes.DOUBLE);
 				defaultsAxis.setStart((Double)axis.getStart());
 				defaultsAxis.setStop((Double)axis.getStop());
 				defaultsAxis.setStepwidth((Double)axis.getStepwidth());
 				break;
 			case INT:
 				defaultsAxis.setStartStopStepType(DataTypes.INT);
 				defaultsAxis.setStart((Integer)axis.getStart());
 				defaultsAxis.setStop((Integer)axis.getStop());
 				defaultsAxis.setStepwidth((Integer)axis.getStepwidth());
 				break;
 			default:
 				break;
 			}
 			break;
 		case FILE:
 			defaultsAxis.setFile(axis.getFile().getAbsolutePath());
 			break;
 		case PLUGIN:
 			// TODO
 			break;
 		case POSITIONLIST:
 			defaultsAxis.setPositionList(axis.getPositionlist());
 			break;
 		}
 		return defaultsAxis;
 	}
 	
 	/**
 	 * Returns a 
 	 * {@link de.ptb.epics.eve.data.scandescription.defaults.DefaultsChannel} 
 	 * for the given 
 	 * {@link de.ptb.epics.eve.data.scandescription.Channel}.
 	 * 
 	 * @param channel the channel to convert
 	 * @return the converted defaults channel
 	 */
 	public static DefaultsChannel getDefaultsChannel(Channel channel) {
 		DefaultsChannel defaultsChannel = new DefaultsChannel();
 		defaultsChannel.setId(channel.getDetectorChannel().getID());
 		defaultsChannel.setAverageCount(channel.getAverageCount());
 		defaultsChannel.setMaxAttempts(channel.getMaxAttempts());
 		defaultsChannel.setMinimum(channel.getMinimum());
 		defaultsChannel.setMaxDeviation(channel.getMaxDeviation());
 		defaultsChannel.setDeferred(channel.isDeferred());
 		if (channel.getNormalizeChannel() != null) {
 			defaultsChannel.setNormalizeId(channel.getNormalizeChannel().getID());
 		}
 		return defaultsChannel;
 	}
 	
 	/**
 	 * Updates the defaults of channels and axes contained in the given 
 	 * scan description.
 	 * 
 	 * @param scanDescription the scan description containing axes and channels 
 	 * 		that should be updated
 	 */
 	public synchronized void update(ScanDescription scanDescription) {
 		if (!isInitialized()) {
 			this.defaults = new Defaults();
			this.initialized = true;
 		}
 		for (Chain ch : scanDescription.getChains()) {
 			for (ScanModule sm : ch.getScanModules()) {
 				for (Axis axis : sm.getAxes()) {
 					this.defaults.updateAxis(DefaultsManager
 							.getDefaultsAxis(axis));
 				}
 				for (Channel channel : sm.getChannels()) {
 					// this.defaults.updateChannel(DefaultsManager
 						//	.getDefaultsChannel(channel));
 				}
 			}
 		}
 	}
 }
