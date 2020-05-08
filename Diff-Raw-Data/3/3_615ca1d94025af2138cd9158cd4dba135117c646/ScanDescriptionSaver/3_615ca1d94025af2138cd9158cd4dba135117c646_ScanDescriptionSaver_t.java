 package de.ptb.epics.eve.data.scandescription.processors;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Iterator;
 
 import javax.xml.datatype.Duration;
 
 import org.apache.log4j.Logger;
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 import org.eclipse.swt.graphics.RGB;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 import de.ptb.epics.eve.data.DataTypes;
 import de.ptb.epics.eve.data.PluginDataType;
 import de.ptb.epics.eve.data.ComparisonTypes;
 import de.ptb.epics.eve.data.PlotModes;
 import de.ptb.epics.eve.data.PluginTypes;
 import de.ptb.epics.eve.data.MethodTypes;
 import de.ptb.epics.eve.data.TypeValue;
 import de.ptb.epics.eve.data.EventTypes;
 import de.ptb.epics.eve.data.TransportTypes;
 import de.ptb.epics.eve.data.measuringstation.Detector;
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.Device;
 import de.ptb.epics.eve.data.measuringstation.Function;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.Motor;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.data.measuringstation.Option;
 import de.ptb.epics.eve.data.measuringstation.PlugIn;
 import de.ptb.epics.eve.data.measuringstation.PluginParameter;
 import de.ptb.epics.eve.data.measuringstation.Access;
 import de.ptb.epics.eve.data.measuringstation.Selections;
 import de.ptb.epics.eve.data.measuringstation.Unit;
 import de.ptb.epics.eve.data.measuringstation.event.ScheduleEvent;
 import de.ptb.epics.eve.data.scandescription.Axis;
 import de.ptb.epics.eve.data.scandescription.Chain;
 import de.ptb.epics.eve.data.scandescription.Channel;
 import de.ptb.epics.eve.data.scandescription.ControlEvent;
 import de.ptb.epics.eve.data.scandescription.PauseEvent;
 import de.ptb.epics.eve.data.scandescription.PlotWindow;
 import de.ptb.epics.eve.data.scandescription.PluginController;
 import de.ptb.epics.eve.data.scandescription.Positioning;
 import de.ptb.epics.eve.data.scandescription.Postscan;
 import de.ptb.epics.eve.data.scandescription.Prescan;
 import de.ptb.epics.eve.data.scandescription.ScanDescription;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.data.scandescription.YAxis;
 import de.ptb.epics.eve.data.scandescription.PositionMode;
 
 /**
  * This class saves a scan description to a file.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld (-at-) ptb.de>
  * @author Marcus Michalsky
  * @author Hartmut Scherr
  */
 public class ScanDescriptionSaver implements
 		IScanDescriptionSaver {
 
 	private static Logger logger = Logger
 			.getLogger(ScanDescriptionSaver.class.getName());
 	
 	private IMeasuringStation measuringStation;
 	private ScanDescription scanDescription;
 
 	private String version;
 	
 	/* 
 	 * An output stream to the destination where the scan description should be
 	 * written to.
 	 */
 	private OutputStream destination;
 
 	private ContentHandler contentHandler;
 
 	// The XML attributes.
 	private AttributesImpl atts;
 
 	// TODO: use non-deprecated API
 	private XMLSerializer serializer;
 
 	/**
 	 * This constructor create a new saver.
 	 * 
 	 * @param destination
 	 *            The output stream to the destination.
 	 * @param measuringStation
 	 *            The measuring station description.
 	 * @param scanDescription
 	 *            The scan description.
 	 */
 	public ScanDescriptionSaver(final OutputStream destination,
 			final IMeasuringStation measuringStation,
 			final ScanDescription scanDescription) {
 		if (measuringStation == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'measuringStation' must not be null!");
 		}
 		this.destination = destination;
 		this.measuringStation = measuringStation;
 		this.scanDescription = scanDescription;
 	}
 	
 	/**
 	 * @param version the version to set
 	 */
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public IMeasuringStation getMeasuringStationDescription() {
 		return this.measuringStation;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ScanDescription getScanDescription() {
 		return this.scanDescription;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setMeasuringStationDescription(
 			final IMeasuringStation measuringStation) {
 		if (measuringStation == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'measuringStation' must not be null!");
 		}
 		this.measuringStation = measuringStation;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setScanDescription(final ScanDescription scanDescription) {
 		if (scanDescription == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'scanDescription' must not be null!");
 		}
 		this.scanDescription = scanDescription;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean save() {
 		boolean successful = true;
 		this.atts = new AttributesImpl();
 		OutputFormat outputFormat = new OutputFormat("XML", "UTF-8", true);
 		outputFormat.setIndent(1);
 		outputFormat.setIndenting(true);
 
 		// Die Versuche mit dem Setzen von Elemente welche dann nicht in z.B.
 		// &gt;
 		// umgewandelt werden, hatten keinen Erfolg. Warum auch immer.
 		// String[] noEscape = {"value", "prescan"};
 		// outputFormat.setNonEscapingElements(noEscape);
 		// Auch die Versuche mit serializer.startNonEscaping(); sind
 		// fehlgeschlagen
 		// Hartmut Scherr, 10.11.10
 
 		serializer = new XMLSerializer(this.destination, outputFormat);
 
 		this.contentHandler = null;
 
 		try {
 			this.contentHandler = serializer.asContentHandler();
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 			successful = false;
 		}
 
 		try {
 			this.contentHandler.startDocument();
 
 			this.atts.clear();
 			this.atts.addAttribute("xmlns", "tns", "xmlns:tns", "CDATA",
 					"http://www.ptb.de/epics/SCML");
 			this.atts.addAttribute("xmlns", "xsi", "xmlns:xsi", "CDATA",
 					"http://www.w3.org/2001/XMLSchema-instance");
 			this.atts.addAttribute("xsi", "schemaLocation",
 					"xsi:schemaLocation", "CDATA",
 					"http://www.ptb.de/epics/SCML scml.xsd");
 
 			this.contentHandler.startElement("tns", "scml", "tns:scml", atts);
 
 			if (this.scanDescription.getMeasuringStation().getName() != null
 					&& !this.scanDescription.getMeasuringStation().getName()
 							.isEmpty()) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "", "location", atts);
 				this.contentHandler.characters(this.scanDescription
 						.getMeasuringStation().getName().toCharArray(), 0,
 						this.scanDescription.getMeasuringStation().getName()
 								.toCharArray().length);
 				this.contentHandler.endElement("", "", "location");
 			}
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "", "version", atts);
 			this.contentHandler.characters(this.version.toCharArray(), 0,
 					this.version.length());
 			this.contentHandler.endElement("", "", "version");
 			
 			this.atts.clear();
 			this.contentHandler.startElement("", "", "scan", atts);
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "", "repeatcount", atts);
 			this.contentHandler.characters(
 					("" + this.scanDescription.getRepeatCount()).toCharArray(),
 					0, ("" + this.scanDescription.getRepeatCount()).length());
 			this.contentHandler.endElement("", "", "repeatcount");
 
 			for(Chain chain : this.scanDescription.getChains()) {
 				this.writeChain(chain);
 			}
 
 			successful = this.writeMonitorDevices();
 			
 			this.contentHandler.endElement("", "", "scan");
 			
 			successful = this.writePlugins();
 			successful = this.writeDetectors();
 			successful = this.writeMotors();
 			successful = this.writeDevices();
 			successful = this.writeSelections(this.measuringStation.getSelections());
 
 			this.contentHandler.endElement("tns", "scml", "tns:scml");
 			this.contentHandler.endDocument();
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			successful = false;
 		}
 		return successful;
 	}
 
 	/**
 	 * Returns whether writing was successful.
 	 * 
 	 * @return whether writing was successful
 	 */
 	private boolean writeMonitorDevices() {
 		boolean successful = true;
 		try {
 			this.atts.clear();
 			this.atts.addAttribute("", "type", "type", "CDATA",
 					"" + scanDescription.getMonitorOption().toString());
 			this.contentHandler.startElement("", "monitoroptions",
 					"monitoroptions", this.atts);
 								
 			for (Option o : this.scanDescription.getMonitors()) {
 				successful = this.writeId(o.getID());
 			}
 			
 			this.contentHandler.endElement("", "monitordevices",
 					"monitordevices");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return successful;
 	}
 	
 	/**
 	 * 
 	 * @param id
 	 * @return
 	 */
 	private boolean writeId(String id) {
 		this.atts.clear();
 		try {
 			this.contentHandler.startElement("", "id", "id", this.atts);
 			this.contentHandler.characters(id.toCharArray(), 0, id.length());
 			this.contentHandler.endElement("", "id", "id");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * This method writes all devices of the current measuring station.
 	 * 
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeDevices() {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "devices", "devices",
 					this.atts);
 			for(Device device : this.measuringStation.getDevices()) {
 				this.writeDevice(device);
 			}
 			this.contentHandler.endElement("", "devices", "devices");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a device.
 	 * 
 	 * @param device
 	 *            The device to write.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeDevice(final Device device) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "device", "device", this.atts);
 			this.atts.clear();
 			this.contentHandler.startElement("", "class", "class", this.atts);
 			if (device.getClassName() != null) {
 				this.contentHandler.characters(device.getClassName()
 						.toCharArray(), 0, device.getClassName().length());
 			}
 			this.contentHandler.endElement("", "class", "class");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "name", "name", this.atts);
 			if (device.getName() != null) {
 				this.contentHandler.characters(device.getName().toCharArray(),
 						0, device.getName().length());
 			}
 			this.contentHandler.endElement("", "name", "name");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "id", "id", this.atts);
 			this.contentHandler.characters(device.getID().toCharArray(), 0,
 					device.getID().length());
 			this.contentHandler.endElement("", "id", "id");
 
 			if (device.getValue() != null) {
 				this.writeFunction(device.getValue(), "value");
 			}
 			if (device.getUnit() != null) {
 				this.writeUnit(device.getUnit());
 			}
 
 			if (device.getDisplaygroup() != null) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "displaygroup",
 						"displaygroup", this.atts);
 				this.contentHandler.characters(device.getDisplaygroup()
 						.toString().toCharArray(), 0, device.getDisplaygroup()
 						.toString().length());
 				this.contentHandler.endElement("", "displaygroup",
 						"displaygroup");
 			}
 			this.contentHandler.endElement("", "device", "device");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a motors of a measuring station.
 	 * 
 	 * @return Returns if the write process was was successful.
 	 */
 	private boolean writeMotors() {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "motors", "motors", this.atts);
 			for(Motor motor : this.measuringStation.getMotors()) {
 				this.writeMotor(motor);
 			}
 			this.contentHandler.endElement("", "motors", "motors");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a motor.
 	 * 
 	 * @param motor
 	 *            The motor to write.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeMotor(final Motor motor) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "motor", "motor", this.atts);
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "class", "class", this.atts);
 			if (motor.getClassName() != null) {
 				this.contentHandler.characters(motor.getClassName()
 						.toCharArray(), 0, motor.getClassName().length());
 			}
 			this.contentHandler.endElement("", "class", "class");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "name", "name", this.atts);
 			if (motor.getName() != null) {
 				this.contentHandler.characters(motor.getName().toCharArray(),
 						0, motor.getName().length());
 			}
 			this.contentHandler.endElement("", "name", "name");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "id", "id", this.atts);
 			this.contentHandler.characters(motor.getID().toCharArray(), 0,
 					motor.getID().length());
 			this.contentHandler.endElement("", "id", "id");
 
 			if (motor.getTrigger() != null) {
 				this.writeFunction(motor.getTrigger(), "trigger");
 			}
 			if (motor.getUnit() != null) {
 				this.writeUnit(motor.getUnit());
 			}
 
 			for(MotorAxis ma : motor.getAxes()) {
 				this.writeMotorAxis(ma);
 			}
 
 			for(Option o : motor.getOptions()) {
 				this.writeOption(o);
 			}
 
 			this.contentHandler.endElement("", "motor", "motor");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a motor axis.
 	 * 
 	 * @param axis
 	 *            The motor axis to write.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeMotorAxis(final MotorAxis axis) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "axis", "axis", this.atts);
 			
 			this.atts.clear();
 			this.contentHandler.startElement("", "class", "class", this.atts);
 			if (axis.getClassName() != null) {
 				this.contentHandler.characters(axis.getClassName()
 						.toCharArray(), 0, axis.getClassName().length());
 			}
 			this.contentHandler.endElement("", "class", "class");
 
 			this.contentHandler.startElement("", "name", "name", this.atts);
 			if (axis.getName() != null) {
 				this.contentHandler.characters(axis.getName().toCharArray(), 0,
 						axis.getName().length());
 			}
 			this.contentHandler.endElement("", "name", "name");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "id", "id", this.atts);
 			this.contentHandler.characters(axis.getID().toCharArray(), 0, axis
 					.getID().length());
 			this.contentHandler.endElement("", "id", "id");
 
 			if (axis.getGoto() != null) {
 				this.writeFunction(axis.getGoto(), "goto");
 			}
 			if (axis.getPosition() != null) {
 				this.writeFunction(axis.getPosition(), "position");
 			}
 			if (axis.getStop() != null) {
 				this.writeFunction(axis.getStop(), "stop");
 			}
 			if (axis.getSoftHighLimit() != null) {
 				this.writeFunction(axis.getSoftHighLimit(), "highlimit");
 			}
 			if (axis.getSoftLowLimit() != null) {
 				this.writeFunction(axis.getSoftLowLimit(), "lowlimit");
 			}
 			if (axis.getStatus() != null) {
 				this.writeFunction(axis.getStatus(), "status");
 			}
			if (axis.getMoveDone() != null) {
				this.writeFunction(axis.getMoveDone(), "movedone");
			}
 			if (axis.getTrigger() != null) {
 				this.writeFunction(axis.getTrigger(), "trigger");
 			}
 			if (axis.getUnit() != null) {
 				this.writeUnit(axis.getUnit());
 			}
 			if (axis.getDeadband() != null) {
 				this.writeFunction(axis.getDeadband(), "deadband");
 			}
 			if (axis.getOffset() != null) {
 				this.writeFunction(axis.getOffset(), "offset");
 			}
 			if (axis.getTweakValue() != null) {
 				this.writeFunction(axis.getTweakValue(), "tweakvalue");
 			}
 			if (axis.getTweakForward() != null) {
 				this.writeFunction(axis.getTweakForward(), "tweakforward");
 			}
 			if (axis.getTweakReverse() != null) {
 				this.writeFunction(axis.getTweakReverse(), "tweakreverse");
 			}
 
 			for(Option o : axis.getOptions()) {
 				this.writeOption(o);
 			}
 
 			this.contentHandler.endElement("", "axis", "axis");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes all detectors.
 	 * 
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeDetectors() {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "detectors", "detectors",
 					this.atts);
 			for(Detector detector : this.measuringStation.getDetectors()) {
 				this.writeDetector(detector);
 			}
 			this.contentHandler.endElement("", "detectors", "detectors");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a detector.
 	 * 
 	 * @param detector
 	 *            The detector that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeDetector(final Detector detector) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "detector", "detector",
 					this.atts);
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "class", "class", this.atts);
 			if (detector.getClassName() != null) {
 				this.contentHandler.characters(detector.getClassName()
 						.toCharArray(), 0, detector.getClassName().length());
 			}
 			this.contentHandler.endElement("", "class", "class");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "name", "name", this.atts);
 			if (detector.getName() != null) {
 				this.contentHandler.characters(
 						detector.getName().toCharArray(), 0, detector.getName()
 								.length());
 			}
 			this.contentHandler.endElement("", "name", "name");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "id", "id", this.atts);
 			this.contentHandler.characters(detector.getID().toCharArray(), 0,
 					detector.getID().length());
 			this.contentHandler.endElement("", "id", "id");
 
 			if (detector.getUnit() != null) {
 				this.writeUnit(detector.getUnit());
 			}
 			if (detector.getTrigger() != null) {
 				this.writeFunction(detector.getTrigger(), "trigger");
 			}
 
 			for(DetectorChannel ch : detector.getChannels()) {
 				this.writeDetectorChannel(ch);
 			}
 
 			for(Option o : detector.getOptions()) {
 				this.writeOption(o);
 			}
 
 			this.contentHandler.endElement("", "detector", "detector");
 
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a detector channel
 	 * 
 	 * @param channel
 	 *            The detector channel that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeDetectorChannel(final DetectorChannel channel) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "channel", "channel",
 					this.atts);
 			this.atts.clear();
 
 			this.contentHandler.startElement("", "class", "class", this.atts);
 			if (channel.getClassName() != null) {
 				this.contentHandler.characters(channel.getClassName()
 						.toCharArray(), 0, channel.getClassName().length());
 			}
 			this.contentHandler.endElement("", "class", "class");
 
 			this.contentHandler.startElement("", "name", "name", this.atts);
 			if (channel.getName() != null) {
 				this.contentHandler.characters(channel.getName().toCharArray(),
 						0, channel.getName().length());
 			}
 			this.contentHandler.endElement("", "name", "name");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "id", "id", this.atts);
 			this.contentHandler.characters(channel.getID().toCharArray(), 0,
 					channel.getID().length());
 			this.contentHandler.endElement("", "id", "id");
 
 			this.writeFunction(channel.getRead(), "read");
 			if (channel.getUnit() != null) {
 				this.writeUnit(channel.getUnit());
 			}
 			if (channel.getTrigger() != null) {
 				this.writeFunction(channel.getTrigger(), "trigger");
 			}
 
 			for (Option o : channel.getOptions()) {
 				writeOption(o);
 			}
 
 			this.contentHandler.endElement("", "detector", "detector");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a function.
 	 * 
 	 * @param trigger
 	 *            The function that should be written.
 	 * @param name
 	 *            The name of the function, like start, stop, trigger.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeFunction(final Function trigger, final String name) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", name, name, this.atts);
 			if (trigger.getAccess() != null) {
 				this.writeAccess(trigger.getAccess(), "access");
 			}
 			if (trigger.getValue() != null) {
 				this.writeTypeValue(trigger.getValue(), "value");
 			}
 			this.contentHandler.endElement("", name, name);
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a unit.
 	 * 
 	 * @param unit
 	 *            The unit to write.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeUnit(final Unit unit) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "unit", "unit", this.atts);
 			if (unit.isAccess()) {
 				this.writeAccess(unit.getAccess(), "access");
 			} else {
 				this.atts.clear();
 				this.contentHandler.startElement("", "unitstring",
 						"unitstring", this.atts);
 				this.contentHandler.characters(unit.getValue().toCharArray(),
 						0, unit.getValue().length());
 				this.contentHandler.endElement("", "unitstring", "unitstring");
 			}
 			this.contentHandler.endElement("", "unit", "unit");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a option.
 	 * 
 	 * @param option
 	 *            This option that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeOption(final Option option) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "option", "option", this.atts);
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "name", "name", this.atts);
 			if (option.getName() != null) {
 				this.contentHandler.characters(option.getName().toCharArray(),
 						0, option.getName().length());
 			}
 			this.contentHandler.endElement("", "name", "name");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "id", "id", this.atts);
 			this.contentHandler.characters(option.getID().toCharArray(), 0,
 					option.getID().length());
 			this.contentHandler.endElement("", "id", "id");
 
 			this.writeFunction(option.getValue(), "value");
 
 			if (option.getDisplaygroup() != null) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "displaygroup",
 						"displaygroup", this.atts);
 				this.contentHandler.characters(option.getDisplaygroup()
 						.toString().toCharArray(), 0, option.getDisplaygroup()
 						.toString().length());
 				this.contentHandler.endElement("", "displaygroup",
 						"displaygroup");
 			}
 			this.contentHandler.endElement("", "option", "option");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a type value.
 	 * 
 	 * @param typeValue
 	 *            The TypeValue that should be written.
 	 * @param name
 	 *            A name for the tag.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeTypeValue(final TypeValue typeValue, final String name) {
 		try {
 			this.atts.clear();
 			this.atts.addAttribute("", "", "type", "CDATA",
 					DataTypes.typeToString(typeValue.getType()));
 			this.contentHandler.startElement("", name, name, this.atts);
 			this.contentHandler.characters(
 					((typeValue.getValues() != null) ? typeValue.getValues()
 							: "").toCharArray(), 0,
 					((typeValue.getValues() != null) ? typeValue.getValues()
 							: "").length());
 			this.contentHandler.endElement("", name, name);
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes an access.
 	 * 
 	 * @param access
 	 *            The access that should be written.
 	 * @param name
 	 *            The name for the tag.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeAccess(final Access access, final String name) {
 		try {
 			this.atts.clear();
 			this.atts.addAttribute("", "", "method", "CDATA",
 					MethodTypes.typeToString(access.getMethod()));
 			if (access.getType() != null) {
 				this.atts.addAttribute("", "", "type", "CDATA",
 						DataTypes.typeToString(access.getType()));
 			}
 			if (access.getCount() != 0) {
 				this.atts.addAttribute("", "", "count", "CDATA",
 						"" + access.getCount());
 			}
 			if (access.getTransport() != null) {
 				this.atts.addAttribute("", "", "transport", "CDATA",
 						TransportTypes.typeToString(access.getTransport()));
 			}
 			if (access.getMonitor()) {
 				this.atts.addAttribute("", "", "monitor", "CDATA", "true");
 			}
 			if (access.getTimeout() != 0.0) {
 				this.atts.addAttribute("", "", "timeout", "CDATA",
 						"" + access.getTimeout());
 			}
 			this.contentHandler.startElement("", name, name, this.atts);
 			this.contentHandler.characters(
 					access.getVariableID().toCharArray(), 0, access
 							.getVariableID().length());
 			this.contentHandler.endElement("", name, name);
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes all plug ins.
 	 * 
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writePlugins() {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "plugins", "plugins",
 					this.atts);
 			for(PlugIn plugin : this.measuringStation.getPlugins()) {
 				this.writePlugin(plugin);
 			}
 			this.contentHandler.endElement("", "plugins", "plugins");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a plug in.
 	 * 
 	 * @param plugin
 	 *            The plugin that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writePlugin(final PlugIn plugin) {
 		try {
 			this.atts.clear();
 			this.atts.addAttribute("", "", "type", "CDATA",
 					PluginTypes.typeToString(plugin.getType()));
 			this.contentHandler.startElement("", "plugin", "plugin", this.atts);
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "name", "name", this.atts);
 			this.contentHandler.characters(plugin.getName().toCharArray(), 0,
 					plugin.getName().length());
 			this.contentHandler.endElement("", "name", "name");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "location", "location",
 					this.atts);
 			this.contentHandler.characters(plugin.getLocation().toCharArray(),
 					0, plugin.getLocation().length());
 			this.contentHandler.endElement("", "location", "location");
 
 			for(PluginParameter pluginparam : plugin.getParameters()) {
 				this.writePluginParameter(pluginparam);
 			}
 
 			this.contentHandler.endElement("", "plugin", "plugin");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a plug in parameter
 	 * 
 	 * @param pluginParameter
 	 *            The plug in parameter that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writePluginParameter(final PluginParameter pluginParameter) {
 		try {
 			this.atts.clear();
 			this.atts.addAttribute("", "", "name", "CDATA",
 					pluginParameter.getName());
 			this.atts.addAttribute("", "", "datatype", "CDATA",
 					PluginDataType.typeToString(pluginParameter.getType()));
 			if (pluginParameter.getDefaultValue() != null) {
 				this.atts.addAttribute("", "", "default", "CDATA",
 						pluginParameter.getDefaultValue());
 			}
 			this.atts.addAttribute("", "", "mandatory", "CDATA", ""
 					+ pluginParameter.isMandatory());
 			this.contentHandler.startElement("", "parameter", "parameter",
 					this.atts);
 
 			if (pluginParameter.getValues() != null) {
 				this.contentHandler
 						.characters(pluginParameter.getValues().toCharArray(),
 								0, pluginParameter.getValues().length());
 			}
 			this.contentHandler.endElement("", "parameter", "parameter");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a chain.
 	 * 
 	 * @param chain
 	 *            The chain that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeChain(final Chain chain) {
 		try {
 			this.atts.clear();
 			this.atts.addAttribute("", "id", "id", "CDATA", "" + chain.getId());
 
 			this.contentHandler.startElement("", "chain", "chain", this.atts);
 
 			if (!chain.getComment().isEmpty()) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "comment", "comment",
 						this.atts);
 				this.contentHandler.characters(
 						chain.getComment().toCharArray(), 0, chain.getComment()
 								.length());
 				this.contentHandler.endElement("", "comment", "comment");
 			}
 
 			if (chain.getSaveFilename() != null
 					&& !chain.getSaveFilename().isEmpty()) {
 				this.atts.clear();
 
 				this.contentHandler.startElement("", "savefilename",
 						"savefilename", this.atts);
 				this.contentHandler.characters(chain.getSaveFilename()
 						.toCharArray(), 0, chain.getSaveFilename().length());
 				this.contentHandler.endElement("", "savefilename",
 						"savefilename");
 			}
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "confirmsave", "confirmsave",
 					this.atts);
 			this.contentHandler.characters(
 					("" + chain.isConfirmSave()).toCharArray(), 0,
 					("" + chain.isConfirmSave()).length());
 			this.contentHandler.endElement("", "confirmsave", "confirmsave");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "autonumber", "autonumber",
 					this.atts);
 			this.contentHandler.characters(
 					("" + chain.isAutoNumber()).toCharArray(), 0,
 					("" + chain.isAutoNumber()).length());
 			this.contentHandler.endElement("", "autonumber", "autonumber");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "savescandescription",
 					"savescandescription", this.atts);
 			this.contentHandler.characters(
 					("" + chain.isSaveScanDescription()).toCharArray(), 0,
 					("" + chain.isSaveScanDescription()).length());
 			this.contentHandler.endElement("", "savescandescription",
 					"savescandescription");
 
 			if (chain.getSavePluginController().getPlugin() != null) {
 				this.atts.clear();
 
 				this.writePluginController(chain.getSavePluginController(),
 						"saveplugin");
 			}
 
 			// all events except the default start event are in startevent
 			for (ControlEvent event : chain.getStartEvents()) {
 				this.writeControlEvent(event, "startevent");
 			}
 
 			for (ControlEvent event : chain.getPauseEvents()) {
 				this.writeControlEvent(event, "pauseevent");
 			}
 
 			for (ControlEvent event : chain.getRedoEvents()) {
 				this.writeControlEvent(event, "redoevent");
 			}
 
 			for (ControlEvent event : chain.getBreakEvents()) {
 				this.writeControlEvent(event, "breakevent");
 			}
 
 			for (ControlEvent event : chain.getStopEvents()) {
 				this.writeControlEvent(event, "stopevent");
 			}
 
 			this.atts.clear();
 
 			this.contentHandler.startElement("", "scanmodules", "scanmodules",
 					this.atts);
 			for(ScanModule sm : chain.getScanModules()) {
 				this.writeScanModule(sm);
 			}
 			this.contentHandler.endElement("", "scanmodules", "scanmodules");
 
 			this.contentHandler.endElement("", "chain", "chain");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a scan module.
 	 * 
 	 * @param scanModule
 	 *            The scan module that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeScanModule(final ScanModule scanModule) {
 		try {
 			this.atts.clear();
 			this.atts.addAttribute("", "id", "id", "CDATA",
 					"" + scanModule.getId());
 			this.contentHandler.startElement("", "scanmodule", "scanmodule",
 					this.atts);
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "type", "type", this.atts);
 			this.contentHandler.characters(scanModule.getType().toString().toCharArray(),
 					0, scanModule.getType().toString().length());
 			this.contentHandler.endElement("", "type", "type");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "name", "name", this.atts);
 			this.contentHandler.characters(scanModule.getName().toCharArray(),
 					0, scanModule.getName().length());
 			this.contentHandler.endElement("", "name", "name");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "xpos", "xpos", this.atts);
 			this.contentHandler.characters(
 					("" + scanModule.getX()).toCharArray(), 0,
 					("" + scanModule.getX()).length());
 			this.contentHandler.endElement("", "xpos", "xpos");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "ypos", "ypos", this.atts);
 			this.contentHandler.characters(
 					("" + scanModule.getY()).toCharArray(), 0,
 					("" + scanModule.getY()).length());
 			this.contentHandler.endElement("", "ypos", "ypos");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "parent", "parent", this.atts);
 			if (scanModule.getParent() != null) {
 				if (scanModule.getParent().getParentScanModule() != null) {
 				this.contentHandler.characters(("" + scanModule.getParent()
 						.getParentScanModule().getId()).toCharArray(), 0,
 						("" + scanModule.getParent().getParentScanModule()
 								.getId()).length());
 				} else if (scanModule.getParent().getParentEvent() != null) {
 					this.contentHandler.characters("0".toCharArray(), 0, 1);
 				}
 			} else {
 				this.contentHandler.characters("-1".toCharArray(), 0, 2);
 			}
 			this.contentHandler.endElement("", "parent", "parent");
 
 			this.atts.clear();
 			if (scanModule.getNested() != null
 					&& scanModule.getNested().getChildScanModule() != null) {
 				this.contentHandler.startElement("", "nested", "nested",
 						this.atts);
 				this.contentHandler.characters(("" + scanModule.getNested()
 						.getChildScanModule().getId()).toCharArray(), 0,
 						("" + scanModule.getNested().getChildScanModule()
 								.getId()).length());
 				this.contentHandler.endElement("", "nested", "nested");
 			}
 
 			this.atts.clear();
 			if (scanModule.getAppended() != null
 					&& scanModule.getAppended().getChildScanModule() != null) {
 				this.contentHandler.startElement("", "appended", "appended",
 						this.atts);
 				this.contentHandler.characters(("" + scanModule.getAppended()
 						.getChildScanModule().getId()).toCharArray(), 0,
 						("" + scanModule.getAppended().getChildScanModule()
 								.getId()).length());
 				this.contentHandler.endElement("", "appended", "appended");
 			}
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "storage", "storage",
 					this.atts);
 			this.contentHandler.characters(scanModule.getStorage().toString()
 					.toCharArray(), 0, scanModule.getStorage().toString()
 					.length());
 			this.contentHandler.endElement("", "storage", "storage");
 			
 			this.atts.clear();
 			this.contentHandler.startElement("", "valuecount", "valuecount",
 					this.atts);
 			this.contentHandler.characters(
 					("" + scanModule.getValueCount()).toCharArray(), 0,
 					("" + scanModule.getValueCount()).length());
 			this.contentHandler.endElement("", "valuecount", "valuecount");
 
 			if (scanModule.getSettleTime() != Double.NEGATIVE_INFINITY) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "settletime",
 						"settletime", this.atts);
 				this.contentHandler.characters(
 						("" + scanModule.getSettleTime()).toCharArray(), 0,
 						("" + scanModule.getSettleTime()).length());
 				this.contentHandler.endElement("", "settletime", "settletime");
 			}
 
 			if (scanModule.getTriggerDelay() != Double.NEGATIVE_INFINITY) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "triggerdelay",
 						"triggerdelay", this.atts);
 				this.contentHandler.characters(
 						("" + scanModule.getTriggerDelay()).toCharArray(), 0,
 						("" + scanModule.getTriggerDelay()).length());
 				this.contentHandler.endElement("", "triggerdelay",
 						"triggerdelay");
 			}
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "triggerconfirmaxis",
 					"triggerconfirmaxis", this.atts);
 			this.contentHandler.characters(
 					("" + scanModule.isTriggerConfirmAxis()).toCharArray(), 0,
 					("" + scanModule.isTriggerConfirmAxis()).length());
 			this.contentHandler.endElement("", "triggerconfirmaxis",
 					"triggerconfirmaxis");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "triggerconfirmchannel",
 					"triggerconfirmchannel", this.atts);
 			this.contentHandler.characters(
 					("" + scanModule.isTriggerConfirmChannel()).toCharArray(), 0,
 					("" + scanModule.isTriggerConfirmChannel()).length());
 			this.contentHandler.endElement("", "triggerconfirmchannel",
 					"triggerconfirmchannel");
 			
 			for (ControlEvent event : scanModule.getTriggerEvents()) {
 				this.writeControlEvent(event, "triggerevent");
 			}
 			
 			for (ControlEvent event : scanModule.getPauseEvents()) {
 				this.writeControlEvent(event, "pauseevent");
 			}
 			
 			for (ControlEvent event : scanModule.getRedoEvents()) {
 				this.writeControlEvent(event, "redoevent");
 			}
 			
 			for (ControlEvent event : scanModule.getBreakEvents()) {
 				this.writeControlEvent(event, "breakevent");
 			}
 			
 			Prescan[] prescans = scanModule.getPrescans();
 			for (int i = 0; i < prescans.length; ++i) {
 				this.writePrescan(prescans[i]);
 			}
 
 			Axis[] axis = scanModule.getAxes();
 			for (int i = 0; i < axis.length; ++i) {
 				try {
 					this.writeAxis(axis[i]);
 				} catch (Exception ex) {
 					logger.error(ex.getMessage(), ex);
 				}
 			}
 
 			Channel[] channels = scanModule.getChannels();
 			for (int i = 0; i < channels.length; ++i) {
 				this.writeChannel(channels[i]);
 			}
 
 			Postscan[] postscans = scanModule.getPostscans();
 			for (int i = 0; i < postscans.length; ++i) {
 				this.writePostscan(postscans[i]);
 			}
 
 			Positioning[] positioning = scanModule.getPositionings();
 			for (int i = 0; i < positioning.length; ++i) {
 				this.writePositioning(positioning[i]);
 			}
 
 			PlotWindow[] plotWindows = scanModule.getPlotWindows();
 			for (int i = 0; i < plotWindows.length; ++i) {
 				if (plotWindows[i].getXAxis() != null) {
 					this.writePlotWindow(plotWindows[i]);
 				}
 			}
 
 			this.contentHandler.endElement("", "scanmodule", "scanmodule");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a positioning.
 	 * 
 	 * @param positioning
 	 *            The positioning that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writePositioning(final Positioning positioning) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "positioning", "positioning",
 					this.atts);
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "axis_id", "axis_id",
 					this.atts);
 			this.contentHandler.characters(positioning.getMotorAxis().getID()
 					.toCharArray(), 0, positioning.getMotorAxis().getID()
 					.length());
 			this.contentHandler.endElement("", "axis_id", "axis_id");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "channel_id", "channel_id",
 					this.atts);
 			this.contentHandler.characters(positioning.getDetectorChannel()
 					.getID().toCharArray(), 0, positioning.getDetectorChannel()
 					.getID().length());
 			this.contentHandler.endElement("", "channel_id", "channel_id");
 
 			if (positioning.getNormalization() != null) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "normalize_id",
 						"normalize_id", this.atts);
 				this.contentHandler.characters(positioning.getNormalization()
 						.getID().toCharArray(), 0, positioning
 						.getNormalization().getID().length());
 				this.contentHandler.endElement("", "normalize_id",
 						"normalize_id");
 			}
 
 			this.writePluginController(positioning.getPluginController(),
 					"plugin");
 
 			this.contentHandler.endElement("", "positioning", "positioning");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a plug in controller.
 	 * 
 	 * @param pluginController
 	 *            The plug in controller that should be written.
 	 * @param tagName
 	 *            The name of the tag.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writePluginController(
 			final PluginController pluginController, final String tagName) {
 		try {
 			this.atts.clear();
 			this.atts.addAttribute("", "name", "name", "CDATA",
 					pluginController.getPlugin().getName());
 			this.contentHandler.startElement("", tagName, tagName, this.atts);
 
 			this.atts.clear();
 			this.atts.addAttribute("", "name", "name", "CDATA", "location");
 			this.contentHandler.startElement("", "parameter", "parameter",
 					this.atts);
 			this.contentHandler.characters(pluginController.getPlugin()
 					.getLocation().toCharArray(), 0, pluginController
 					.getPlugin().getLocation().length());
 			this.contentHandler.endElement("", tagName, tagName);
 
 			Iterator<PluginParameter> it = pluginController.getPlugin()
 					.getParameters().iterator();
 
 			while (it.hasNext()) {
 				final PluginParameter actPlugin = it.next();
 				final String parameterName = actPlugin.getName();
 
 				if (actPlugin.isMandatory()) {
 					// Parameter ist ein Pflichtfeld und muß auf jeden Fall
 					// gespeichert werden
 					this.atts.clear();
 					this.atts.addAttribute("", "name", "name", "CDATA",
 							parameterName);
 					this.contentHandler.startElement("", "parameter",
 							"parameter", this.atts);
 					this.contentHandler.characters(
 							pluginController.get(parameterName).toCharArray(),
 							0, pluginController.get(parameterName).length());
 					this.contentHandler.endElement("", tagName, tagName);
 				} else {
 					// Parameter ist freiwilliger Eintrag und wird nur
 					// gespeichert, wenn ein Wert vorhanden ist.
 					if (pluginController.get(parameterName) != null) {
 						// Wert vorhanden, Parameter wird gespeichert
 						this.atts.clear();
 						this.atts.addAttribute("", "name", "name", "CDATA",
 								parameterName);
 						this.contentHandler.startElement("", "parameter",
 								"parameter", this.atts);
 						this.contentHandler.characters(
 								pluginController.get(parameterName)
 										.toCharArray(), 0, pluginController
 										.get(parameterName).length());
 						this.contentHandler.endElement("", tagName, tagName);
 					} else {
 						// kein Mandatory, kein Wert vorhanden, nichts wird
 						// gespeichert
 					}
 				}
 			}
 
 			this.contentHandler.endElement("", "plugin", "plugin");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a plot window.
 	 * 
 	 * @param plotWindow
 	 *            The plot window that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writePlotWindow(final PlotWindow plotWindow) {
 		try {
 			this.atts.clear();
 			this.atts.addAttribute("", "id", "id", "CDATA",
 					"" + plotWindow.getId());
 			this.contentHandler.startElement("", "plot", "plot", this.atts);
 
 			if (!plotWindow.getName().isEmpty()) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "name", "name", this.atts);
 				this.contentHandler.characters(plotWindow.getName()
 						.toCharArray(), 0, plotWindow.getName().length());
 				this.contentHandler.endElement("", "name", "name");
 			}
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "xaxis", "xaxis", this.atts);
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "id", "id", this.atts);
 			this.contentHandler.characters(plotWindow.getXAxis().getID()
 					.toCharArray(), 0, plotWindow.getXAxis().getID().length());
 			this.contentHandler.endElement("", "id", "id");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "mode", "mode", this.atts);
 			this.contentHandler.characters((PlotModes.modeToString(plotWindow
 					.getMode())).toCharArray(), 0, (PlotModes
 					.modeToString(plotWindow.getMode())).length());
 			this.contentHandler.endElement("", "mode", "mode");
 			this.contentHandler.endElement("", "xaxis", "xaxis");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "init", "init", this.atts);
 			this.contentHandler.characters(
 					("" + plotWindow.isInit()).toCharArray(), 0,
 					("" + plotWindow.isInit()).length());
 			this.contentHandler.endElement("", "init", "init");
 
 			for(YAxis yaxis : plotWindow.getYAxes()) {
 				this.writeYAxis(yaxis);
 			}
 
 			this.contentHandler.endElement("", "plot", "plot");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes an y-axis.
 	 * 
 	 * @param yaxis
 	 *            The y-axis that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeYAxis(final YAxis yaxis) {
 		if (yaxis.getDetectorChannel() != null) {
 			try {
 				this.atts.clear();
 				this.contentHandler.startElement("", "yaxis", "yaxis",
 						this.atts);
 				this.atts.clear();
 				this.contentHandler.startElement("", "id", "id", this.atts);
 				this.contentHandler.characters(yaxis.getDetectorChannel()
 						.getID().toCharArray(), 0, yaxis.getDetectorChannel()
 						.getID().length());
 				this.contentHandler.endElement("", "id", "id");
 				this.atts.clear();
 				this.contentHandler.startElement("", "mode", "mode", this.atts);
 				this.contentHandler
 						.characters((PlotModes.modeToString(yaxis.getMode()))
 								.toCharArray(), 0, (PlotModes
 								.modeToString(yaxis.getMode())).length());
 				this.contentHandler.endElement("", "mode", "mode");
 
 				if (yaxis.getNormalizeChannel() != null) {
 					this.atts.clear();
 					this.contentHandler.startElement("", "normalize_id",
 							"normalize_id", this.atts);
 					this.contentHandler.characters(yaxis.getNormalizeChannel()
 							.getID().toCharArray(), 0, yaxis
 							.getNormalizeChannel().getID().length());
 					this.contentHandler.endElement("", "normalize_id",
 							"normalize_id");
 				}
 
 				this.atts.clear();
 				this.contentHandler.startElement("", "linestyle", "linestyle",
 						this.atts);
 				this.contentHandler.characters(yaxis.getLinestyle().toString()
 						.toCharArray(), 0, yaxis.getLinestyle().toString()
 						.length());
 				this.contentHandler.endElement("", "linestyle", "linestyle");
 
 				this.atts.clear();
 				this.contentHandler.startElement("", "color", "color",
 						this.atts);
 
 				RGB color = yaxis.getColor();
 				String red = Integer.toHexString(color.red);
 				String green = Integer.toHexString(color.green);
 				String blue = Integer.toHexString(color.blue);
 				if (red.length() == 1) {
 					red = "0" + red;
 				}
 				if (green.length() == 1) {
 					green = "0" + green;
 				}
 				if (blue.length() == 1) {
 					blue = "0" + blue;
 				}
 
 				String sColor = red + green + blue;
 				this.contentHandler.characters(sColor.toCharArray(), 0,
 						sColor.length());
 				this.contentHandler.endElement("", "color", "color");
 
 				this.atts.clear();
 				this.contentHandler.startElement("", "markstyle", "markstyle",
 						this.atts);
 				try {
 					String encoded = URLEncoder.encode(yaxis.getMarkstyle()
 							.toString(), "UTF-8");
 					this.contentHandler.characters(encoded.toCharArray(), 0,
 							encoded.length());
 				} catch (UnsupportedEncodingException e) {
 					logger.warn(e.getMessage(), e);
 				}
 				this.contentHandler.endElement("", "markstyle", "markstyle");
 
 				this.contentHandler.endElement("", "yaxis", "yaxis");
 			} catch (SAXException e) {
 				logger.error(e.getMessage(), e);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a post scan.
 	 * 
 	 * @param postscan
 	 *            The post scan that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writePostscan(final Postscan postscan) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "postscan", "postscan",
 					this.atts);
 			this.atts.clear();
 			this.contentHandler.startElement("", "id", "id", this.atts);
 			this.contentHandler.characters(postscan.getAbstractDevice().getID()
 					.toCharArray(), 0, postscan.getAbstractDevice().getID()
 					.length());
 			this.contentHandler.endElement("", "id", "id");
 
 			if (postscan.isReset()) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "reset_originalvalue",
 						"reset_originalvalue", this.atts);
 				this.contentHandler.characters(
 						("" + postscan.isReset()).toCharArray(), 0,
 						("" + postscan.isReset()).length());
 				this.contentHandler.endElement("", "reset_originalvalue",
 						"reset_originalvalue");
 			} else {
 				this.atts.clear();
 				this.atts.addAttribute(
 						"",
 						"type",
 						"type",
 						"CDATA",
 						DataTypes.typeToString(((postscan
 								.getAbstractPrePostscanDevice().getValue()
 								.getValue() != null) ? postscan
 								.getAbstractPrePostscanDevice().getValue()
 								.getValue().getType() : postscan
 								.getAbstractPrePostscanDevice().getValue()
 								.getAccess().getType())));
 				this.contentHandler.startElement("", "value", "value",
 						this.atts);
 				if (postscan.getValue() != null) {
 					this.contentHandler.characters(postscan.getValue()
 							.toCharArray(), 0, postscan.getValue().length());
 				}
 				this.contentHandler.endElement("", "value", "value");
 			}
 
 			this.contentHandler.endElement("", "postscan", "postscan");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes channel.
 	 * 
 	 * @param channel
 	 *            The channel that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeChannel(final Channel channel) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "smchannel", "smchannel",
 					this.atts);
 			this.atts.clear();
 			this.contentHandler.startElement("", "channelid", "channelid",
 					this.atts);
 			this.contentHandler.characters(channel.getAbstractDevice().getID()
 					.toCharArray(), 0, channel.getAbstractDevice().getID()
 					.length());
 			this.contentHandler.endElement("", "channelid", "channelid");
 
 			if (channel.getAverageCount() > 1) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "averagecount",
 						"averagecount", this.atts);
 				this.contentHandler.characters(
 						("" + channel.getAverageCount()).toCharArray(), 0,
 						("" + channel.getAverageCount()).length());
 				this.contentHandler.endElement("", "averagecount",
 						"averagecount");
 			}
 
 			if (channel.getMaxDeviation() != Double.NEGATIVE_INFINITY) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "maxdeviation",
 						"maxdeviation", this.atts);
 				this.contentHandler.characters(
 						("" + channel.getMaxDeviation()).toCharArray(), 0,
 						("" + channel.getMaxDeviation()).length());
 				this.contentHandler.endElement("", "maxdeviation",
 						"maxdeviation");
 			}
 			if (channel.getMinimum() != Double.NEGATIVE_INFINITY) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "minimum", "minimum",
 						this.atts);
 				this.contentHandler.characters(
 						("" + channel.getMinimum()).toCharArray(), 0,
 						("" + channel.getMinimum()).length());
 				this.contentHandler.endElement("", "minimum", "minimum");
 			}
 
 			if (channel.getMaxAttempts() != Integer.MIN_VALUE) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "maxattempts",
 						"maxattempts", this.atts);
 				this.contentHandler.characters(
 						("" + channel.getMaxAttempts()).toCharArray(), 0,
 						("" + channel.getMaxAttempts()).length());
 				this.contentHandler
 						.endElement("", "maxattempts", "maxattempts");
 			}
 
 			if (channel.getNormalizeChannel() != null) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "normalize_id", 
 						"normalize_id", this.atts);
 				this.contentHandler.characters(
 						channel.getNormalizeChannel().getID().toCharArray(), 
 						0, 
 						channel.getNormalizeChannel().getID().length());
 				this.contentHandler.endElement("", "normalize_id", 
 						"normalize_id");
 			}
 
 			if (scanDescription.isUsedAsEvent(channel)) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "sendreadyevent",
 						"sendreadyevent", this.atts);
 				String trueString = "true";
 				this.contentHandler.characters(trueString.toCharArray(), 0,
 						trueString.length());
 				this.contentHandler.endElement("", "sendreadyevent",
 						"sendreadyevent");
 			}
 
 			for (ControlEvent event : channel.getRedoEvents()) {
 				this.writeControlEvent(event, "redoevent");
 			}
 
 			if (channel.isDeferred()) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "deferredtrigger", 
 						"deferredtrigger", this.atts);
 				this.contentHandler.characters("true".toCharArray(), 0,
 						"true".length());
 				this.contentHandler.endElement("", "deferredtrigger",
 						"deferredtrigger");
 			}
 			
 			this.contentHandler.endElement("", "smchannel", "smchannel");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes an axis.
 	 * 
 	 * @param axis
 	 *            The axis that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeAxis(final Axis axis) {
 		try {
 			this.atts.clear();
 			this.contentHandler.startElement("", "smaxis", "smaxis", this.atts);
 			this.atts.clear();
 			this.contentHandler.startElement("", "axisid", "axisid", this.atts);
 			this.contentHandler.characters(axis.getAbstractDevice().getID()
 					.toCharArray(), 0, axis.getAbstractDevice().getID()
 					.length());
 			this.contentHandler.endElement("", "axisid", "axisid");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "stepfunction",
 					"stepfunction", this.atts);
 			this.contentHandler.characters(axis.getStepfunction().toString()
 					.toCharArray(), 0, axis.getStepfunction().toString().length());
 			this.contentHandler.endElement("", "stepfunction", "stepfunction");
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "positionmode",
 					"positionmode", this.atts);
 			this.contentHandler.characters(
 					PositionMode.typeToString(axis.getPositionMode())
 							.toCharArray(), 0,
 					PositionMode.typeToString(axis.getPositionMode()).length());
 			this.contentHandler.endElement("", "positionmode", "positionmode");
 			
 			switch (axis.getStepfunction()) {
 			case ADD:
 			case MULTIPLY:
 				this.atts.clear();
 				this.contentHandler.startElement("", "startstopstep",
 						"startstopstep", this.atts);
 				if (axis.getStart() != null) {
 					this.atts.clear();
 					this.atts.addAttribute("", "type", "type", "CDATA",
 							DataTypes.typeToString(axis.getType()));
 					this.contentHandler.startElement("", "start", "start",
 							this.atts);
 					if (axis.getType().equals(DataTypes.DATETIME)) {
 						switch (axis.getPositionMode()) {
 						case ABSOLUTE:
 							String date = new SimpleDateFormat(
 									"yyyy-MM-dd HH:mm:ss.SSS").format(axis
 									.getStart());
 							this.contentHandler.characters(date.toCharArray(), 0,
 									date.length());
 							break;
 						case RELATIVE:
 							String duration = ((Duration)axis.getStart()).
 									toString();
 							this.contentHandler.characters(
 									duration.toCharArray(), 0,
 											duration.length());
 							break;
 						}
 					} else {
 					this.contentHandler.characters(axis.getStart().toString()
 						.toCharArray(), 0, axis.getStart().toString().length());
 					}
 					this.contentHandler.endElement("", "start", "start");
 				}
 				if (axis.getStop() != null) {
 					this.atts.clear();
 					this.atts.addAttribute("", "type", "type", "CDATA",
 							DataTypes.typeToString(axis.getType()));
 					this.contentHandler.startElement("", "stop", "stop",
 							this.atts);
 					if (axis.getType().equals(DataTypes.DATETIME)) {
 						switch (axis.getPositionMode()) {
 						case ABSOLUTE:
 							String date = new SimpleDateFormat(
 									"yyyy-MM-dd HH:mm:ss.SSS").format(axis
 									.getStop());
 							this.contentHandler.characters(date.toCharArray(), 0,
 									date.length());
 							break;
 						case RELATIVE:
 							String duration = ((Duration)axis.getStop()).
 									toString();
 							this.contentHandler.characters(
 									duration.toCharArray(), 0,
 											duration.length());
 							break;
 						}
 					} else {
 						this.contentHandler.characters(
 								axis.getStop().toString().toCharArray(), 0, 
 										axis.getStop().toString().length());
 					}
 					this.contentHandler.endElement("", "stop", "stop");
 				}
 				if (axis.getStepwidth() != null) {
 					this.atts.clear();
 					this.atts.addAttribute("", "type", "type", "CDATA",
 							DataTypes.typeToString(axis.getType()));
 					this.contentHandler.startElement("", "stepwidth",
 							"stepwidth", this.atts);
 					if (axis.getType().equals(DataTypes.DATETIME)) {
 						switch (axis.getPositionMode()) {
 						case ABSOLUTE:
 							String date = new SimpleDateFormat(
 									"HH:mm:ss.SSS").format(axis
 									.getStepwidth());
 							this.contentHandler.characters(date.toCharArray(), 0,
 									date.length());
 							break;
 						case RELATIVE:
 							String duration = ((Duration)axis.getStepwidth()).
 									toString();
 							this.contentHandler.characters(
 									duration.toCharArray(), 0,
 											duration.length());
 							break;
 						}
 					} else {
 					this.contentHandler.characters(axis.getStepwidth()
 							.toString().toCharArray(), 0, axis.getStepwidth()
 							.toString().length());
 					}
 					this.contentHandler
 							.endElement("", "stepwidth", "stepwidth");
 				}
 				this.atts.clear();
 				this.contentHandler.startElement("", "ismainaxis",
 						"ismainaxis", this.atts);
 				this.contentHandler.characters(
 						Boolean.toString(axis.isMainAxis()).toCharArray(), 0,
 						Boolean.toString(axis.isMainAxis()).length());
 				this.contentHandler.endElement("", "ismainaxis", "ismainaxis");
 				this.atts.clear();
 				this.contentHandler.endElement("", "startstopstep",
 						"startstopstep");
 				break;
 			case FILE: 
 				if (axis.getFile() != null) {
 					this.atts.clear();
 					this.contentHandler.startElement("", "stepfilename",
 							"stepfilename", this.atts);
 					this.contentHandler.characters(axis.getFile()
 							.getAbsolutePath().toCharArray(), 0, axis.getFile()
 							.getAbsolutePath().length());
 					this.contentHandler.endElement("", "stepfilename",
 							"stepfilename");
 				}
 				break;
 			case PLUGIN:
 				if (axis.getPluginController() != null) {
 					this.atts.clear();
 					this.writePluginController(
 							axis.getPluginController(), "plugin");
 				}
 				break;
 			case POSITIONLIST:
 				if (axis.getPositionlist() != null) {
 					this.atts.clear();
 					this.contentHandler.startElement("", "positionlist",
 							"positionlist", this.atts);
 					this.contentHandler.characters(axis.getPositionlist()
 							.toCharArray(), 0, axis.getPositionlist().length());
 					this.contentHandler.endElement("", "positionlist",
 							"positionlist");
 				}
 				break;
 			default: 
 				break;
 			}
 			this.contentHandler.endElement("", "smaxis", "smaxis");
 		} catch (final SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes a pre scan.
 	 * 
 	 * @param prescan
 	 *            The prescan that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writePrescan(final Prescan prescan) {
 		this.atts.clear();
 		try {
 			this.contentHandler.startElement("", "prescan", "prescan",
 					this.atts);
 
 			this.atts.clear();
 			this.contentHandler.startElement("", "id", "id", this.atts);
 			this.contentHandler.characters(prescan
 					.getAbstractPrePostscanDevice().getID().toCharArray(), 0,
 					prescan.getAbstractPrePostscanDevice().getID().length());
 			this.contentHandler.endElement("", "id", "id");
 
 			this.atts.clear();
 			this.atts.addAttribute(
 					"",
 					"type",
 					"type",
 					"CDATA",
 					DataTypes.typeToString(((prescan
 							.getAbstractPrePostscanDevice().getValue()
 							.getValue() != null) ? prescan
 							.getAbstractPrePostscanDevice().getValue()
 							.getValue().getType() : prescan
 							.getAbstractPrePostscanDevice().getValue()
 							.getAccess().getType())));
 			this.contentHandler.startElement("value", "value", "value",
 					this.atts);
 			this.contentHandler.characters(prescan.getValue().toCharArray(), 0,
 					prescan.getValue().length());
 			this.contentHandler.endElement("value", "value", "value");
 
 			this.contentHandler.endElement("", "prescan", "prescan");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Returns if the write process was successful.
 	 * 
 	 * @param controlEvent
 	 *            This method writes a control event.
 	 * @param name
 	 *            The name of the tag.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeControlEvent(final ControlEvent controlEvent,
 			final String name) {
 		this.atts.clear();
 		try {
 			this.atts.addAttribute("", "type", "type", "CDATA",
 					EventTypes.typeToString(controlEvent.getEventType()));
 			this.contentHandler.startElement("", name, name, this.atts);
 			this.atts.clear();
 
 			if (controlEvent.getEventType() == EventTypes.MONITOR) {
 				this.contentHandler.startElement("", "id", "id", this.atts);
 				this.contentHandler.characters(controlEvent.getEvent().getId()
 						.toCharArray(), 0, controlEvent.getEvent().getId()
 						.length());
 				this.contentHandler.endElement("", "id", "id");
 
 				this.atts.clear();
 				this.atts.addAttribute(
 						"",
 						"type",
 						"type",
 						"CDATA",
 						""
 								+ DataTypes.typeToString(controlEvent
 										.getLimit().getType()));
 				this.atts.addAttribute(
 						"",
 						"comparison",
 						"comparison",
 						"CDATA",
 						""
 								+ ComparisonTypes.typeToString(controlEvent
 										.getLimit().getComparison()));
 				this.contentHandler.startElement("", "limit", "limit",
 						this.atts);
 				this.contentHandler.characters(controlEvent.getLimit()
 						.getValue().toCharArray(), 0, controlEvent.getLimit()
 						.getValue().length());
 				this.contentHandler.endElement("", "limit", "limit");
 			} else if (controlEvent.getEventType() == EventTypes.DETECTOR) {
 				this.atts.clear();
 				this.contentHandler.startElement("", "id", "id", this.atts);
 				this.contentHandler.characters(controlEvent.getEvent().getId()
 						.toCharArray(), 0, controlEvent.getEvent().getId()
 						.length());
 				this.contentHandler.endElement("", "id", "id");
 			} else if (controlEvent.getEventType() == EventTypes.SCHEDULE) {
 				this.contentHandler.startElement("", "incident", "incident",
 						this.atts);
 				ScheduleEvent scheduleEvent = (ScheduleEvent) controlEvent
 						.getEvent();
 				String incident = scheduleEvent.getScheduleTime().toString();
 				this.contentHandler.characters(incident.toCharArray(), 0,
 						incident.length());
 				this.contentHandler.endElement("", "incident", "incident");
 
 				this.contentHandler.startElement("", "chainid", "chainid",
 						this.atts);
 				String tag = Integer.toString(scheduleEvent.getScanModule()
 						.getChain().getId());
 				this.contentHandler.characters(tag.toCharArray(), 0,
 						tag.length());
 				this.contentHandler.endElement("", "chainid", "chainid");
 
 				this.contentHandler.startElement("", "smid", "smid", this.atts);
 				tag = Integer.toString(scheduleEvent.getScanModule().getId());
 				this.contentHandler.characters(tag.toCharArray(), 0,
 						tag.length());
 				this.contentHandler.endElement("", "smid", "smid");
 			}
 			if (name.equals("pauseevent")) {
 				atts.clear();
 				this.contentHandler.startElement("", "action",
 						"action", this.atts);
 				this.contentHandler.characters(
 						("" + ((PauseEvent) controlEvent).getEventAction()
 								.toString()).toCharArray(), 0,
 						("" + ((PauseEvent) controlEvent).getEventAction()
 								.toString()).length());
 				this.contentHandler.endElement("", "action", "action");
 			}
 			this.contentHandler.endElement("", name, name);
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This method writes the selections.
 	 * 
 	 * @param selections
 	 *            The selections that should be written.
 	 * @return Returns if the write process was successful.
 	 */
 	private boolean writeSelections(final Selections selections) {
 		this.atts.clear();
 		try {
 			this.contentHandler.startElement("", "smselection", "smselection",
 					this.atts);
 			this.atts.clear();
 			final StringBuffer stringBuffer = new StringBuffer();
 			String[] stringArray = selections.getStepfunctions();
 			for (int i = 0; i < stringArray.length; ++i) {
 				stringBuffer.append(stringArray[i]);
 				if (i != stringArray.length - 1) {
 					stringBuffer.append(',');
 				}
 			}
 			stringBuffer.toString().toCharArray();
 			this.atts.clear();
 			this.contentHandler.startElement("", "stepfunction",
 					"stepfunction", this.atts);
 			this.contentHandler.characters(stringBuffer.toString()
 					.toCharArray(), 0, stringBuffer.toString().length());
 			this.contentHandler.endElement("", "stepfunction", "stepfunction");
 
 			stringBuffer.delete(0, stringBuffer.length());
 			stringArray = selections.getSmtypes();
 			for (int i = 0; i < stringArray.length; ++i) {
 				stringBuffer.append(stringArray[i]);
 				if (i != stringArray.length - 1) {
 					stringBuffer.append(',');
 				}
 			}
 			this.atts.clear();
 			this.contentHandler.startElement("", "smtype", "smtype", this.atts);
 			this.contentHandler.characters(stringBuffer.toString()
 					.toCharArray(), 0, stringBuffer.toString().length());
 			this.contentHandler.endElement("", "smtype", "smtype");
 
 			stringBuffer.delete(0, stringBuffer.length());
 
 			this.contentHandler.endElement("", "smselection", "smselection");
 		} catch (SAXException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 }
