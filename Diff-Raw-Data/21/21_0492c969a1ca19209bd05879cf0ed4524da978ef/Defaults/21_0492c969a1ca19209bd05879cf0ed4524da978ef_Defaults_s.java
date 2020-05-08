 package de.ptb.epics.eve.data.scandescription.defaults;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.log4j.Logger;
 
 /**
  * Collection of default values containing a list of axes and channel defaults.
  * 
  * @author Marcus Michalsky
  * @since 1.8
  */
 @XmlRootElement(name="defaults", namespace="http://www.ptb.de/epics/SCML")
 public class Defaults {
 	private static final Logger LOGGER = Logger.getLogger(Defaults.class
 			.getName());
 	
 	private List<DefaultsAxis> axes;
 	private List<DefaultsChannel> channels;
 	
 	/** */
 	public Defaults() {
 		this.axes = new ArrayList<DefaultsAxis>();
 		this.channels = new ArrayList<DefaultsChannel>();
 	}
 	
 	/**
 	 * @return the axes
 	 */
 	public List<DefaultsAxis> getAxes() {
 		return axes;
 	}
 	/**
 	 * @param axes the axes to set
 	 */
 	@XmlElementWrapper(name="axes")
 	@XmlElement(name="axis")
 	public void setAxes(List<DefaultsAxis> axes) {
 		this.axes = axes;
 	}
 	/**
 	 * @return the channels
 	 */
 	public List<DefaultsChannel> getChannels() {
 		return channels;
 	}
 	
 	/**
 	 * @param channels the channels to set
 	 */
 	@XmlElementWrapper(name="channels")
 	@XmlElement(name="channel")
 	public void setChannels(List<DefaultsChannel> channels) {
 		this.channels = channels;
 	}
 	
 	/**
 	 * Adds the given axis to the defaults. If the axis previously existed, it 
 	 * is removed before adding the new one.
 	 * 
 	 * @param axis the axis to be added/updated
 	 */
 	public void updateAxis(DefaultsAxis axis) {
 		LOGGER.debug("updating axis: " + axis.getId());
 		DefaultsAxis found = null;
 		for (DefaultsAxis defaultsAxis : this.axes) {
 			if (defaultsAxis.getId().equals(axis.getId())) {
 				found = defaultsAxis;
 			}
 		}
 		if (found != null) {
 			this.axes.remove(found);
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("axis already exists -> deleting old one");
 			}
 		}
 		this.axes.add(axis);
 	}
 	
 	/**
 	 * Adds the given channel to the defaults. If the channel previously 
 	 * existed, it is removed before adding the new one.
 	 * 
 	 * @param channel the channel to be added/updated
 	 */
 	public void updateChannel(DefaultsChannel channel) {
 		LOGGER.debug("updating channel: " + channel.getId());
 		DefaultsChannel found = null;
 		for (DefaultsChannel defaultsChannel : this.channels) {
 			if (defaultsChannel.getId().equals(channel.getId())) {
 				found = defaultsChannel;
 			}
 		}
 		if (found != null) {
 			this.channels.remove(found);
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("channel already exists -> deleting old one");
 			}
 		}
 		this.channels.add(channel);
 	}
 	
 	/**
 	 * Sorts the lists returned by {@link #getAxes()} and {@link #getChannels()} 
 	 * lexicographically by id.
 	 */
 	public void sort() {
 		Collections.sort(this.axes);
 		Collections.sort(this.channels);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		StringBuffer buff = new StringBuffer();
 		buff.append("Axes: ");
 		String axesString = "";
 		for (DefaultsAxis axis : this.axes) {
 			axesString += axis.toString() + ", ";
 		}
 		if (!axesString.isEmpty()) {
 			buff.append(axesString.substring(0, axesString.length()-2));
 		} else {
 			buff.append("none");
 		}
 		buff.append("\n");
 		buff.append("Channels: ");
 		String channelsString = "";
 		for (DefaultsChannel channel : this.channels) {
 			channelsString += channel.toString() + ", ";
 		}
 		if (!channelsString.isEmpty()) {
 			buff.append(channelsString.substring(0, channelsString.length()-2));
 		} else {
 			buff.append("none");
 		}
 		return buff.toString();
 	}
 }
