 package fr.xebia.mockjms;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.jms.Destination;
 import javax.jms.JMSException;
 
 import fr.xebia.mockjms.exceptions.JMSRuntimeException;
 
 public class MessageBuilder {
 
 	private Map<String, Boolean> booleanProperties = new HashMap<String, Boolean>();
 	private Map<String, Byte> bytesProperties = new HashMap<String, Byte>();
 	private Map<String, Double> doubleProperties = new HashMap<String, Double>();
 	private Map<String, Float> floatProperties = new HashMap<String, Float>();
 	private Map<String, Integer> intProperties = new HashMap<String, Integer>();
 	private String text;
 	private String correlationID;
 	private byte[] correlationIDBytes;
 	private int deliveryMode;
 	private Destination destination;
 	private long expiration;
 	private String id;
 	private int priority;
 	private boolean redelivered;
 	private Destination replyTo;
 	private long timestamp;
 	private String type;
 	private long delayedTime;
 
 	public MockTextMessage buildTextMessage() {
 		MockTextMessage textMessage = new MockTextMessage();
 		fill(textMessage);
 		try {
 			textMessage.setText(text);
 		} catch (JMSException e) {
 			throw new JMSRuntimeException(e);
 		}
 		return textMessage;
 	}
 
 	public MockBytesMessage buildBytesMessage() {
 		MockBytesMessage bytesMessage = new MockBytesMessage();
 		fill(bytesMessage);
 		return bytesMessage;
 	}
 
 	private void fill(MockMessage message) {
 		// Fill javax.jmsMessage
 		try {
 			for (Map.Entry<String, Boolean> booleanProperty : booleanProperties
 					.entrySet()) {
 				message.setBooleanProperty(booleanProperty.getKey(),
 						booleanProperty.getValue());
 			}
 			for (Map.Entry<String, Byte> bytesProperty : bytesProperties
 					.entrySet()) {
 				message.setByteProperty(bytesProperty.getKey(),
 						bytesProperty.getValue());
 			}
 			for (Map.Entry<String, Double> doubleProperty : doubleProperties
 					.entrySet()) {
 				message.setDoubleProperty(doubleProperty.getKey(),
 						doubleProperty.getValue());
 			}
 			for (Map.Entry<String, Float> floatProperty : floatProperties
 					.entrySet()) {
 				message.setFloatProperty(floatProperty.getKey(),
 						floatProperty.getValue());
 			}
 			for (Map.Entry<String, Integer> intProperty : intProperties
 					.entrySet()) {
 				message.setIntProperty(intProperty.getKey(),
 						intProperty.getValue());
 			}
 			message.setJMSCorrelationID(correlationID);
 			message.setJMSCorrelationIDAsBytes(correlationIDBytes);
 			message.setJMSDeliveryMode(deliveryMode);
 			message.setJMSDestination(destination);
 			message.setJMSExpiration(expiration);
 			message.setJMSMessageID(id);
 			message.setJMSPriority(priority);
 			message.setJMSRedelivered(redelivered);
 			message.setJMSReplyTo(replyTo);
 			message.setJMSTimestamp(timestamp);
 			message.setJMSType(type);
 
 			// Fill fr.xebia.mockjms.MockMessage
 			message.setDelayedTime(delayedTime);
 
 		} catch (JMSException e) {
 			throw new JMSRuntimeException(e);
 		}
 	}
 
 	public MessageBuilder setBooleanProperties(
 			Map<String, Boolean> booleanProperties) {
 		this.booleanProperties = booleanProperties;
 		return this;
 	}
 
 	public MessageBuilder setBytesProperties(Map<String, Byte> bytesProperties) {
 		this.bytesProperties = bytesProperties;
 		return this;
 	}
 
 	public MessageBuilder setDoubleProperties(
 			Map<String, Double> doubleProperties) {
 		this.doubleProperties = doubleProperties;
 		return this;
 	}
 
 	public MessageBuilder setFloatProperties(Map<String, Float> floatProperties) {
 		this.floatProperties = floatProperties;
 		return this;
 	}
 
 	public MessageBuilder setIntProperties(Map<String, Integer> intProperties) {
 		this.intProperties = intProperties;
 		return this;
 	}
 
 	public MessageBuilder setText(String text) {
 		this.text = text;
 		return this;
 	}
 
 	public MessageBuilder setCorrelationID(String correlationID) {
 		this.correlationID = correlationID;
 		return this;
 	}
 
 	public MessageBuilder setCorrelationIDBytes(byte[] correlationIDBytes) {
 		this.correlationIDBytes = correlationIDBytes;
 		return this;
 	}
 
 	public MessageBuilder setDeliveryMode(int deliveryMode) {
 		this.deliveryMode = deliveryMode;
 		return this;
 	}
 
 	public MessageBuilder setDestination(Destination destination) {
 		this.destination = destination;
 		return this;
 	}
 
 	public MessageBuilder setExpiration(long expiration) {
 		this.expiration = expiration;
 		return this;
 	}
 
 	public MessageBuilder setId(String id) {
 		this.id = id;
 		return this;
 	}
 
 	public MessageBuilder setPriority(int priority) {
 		this.priority = priority;
 		return this;
 	}
 
 	public MessageBuilder setRedelivered(boolean redelivered) {
 		this.redelivered = redelivered;
 		return this;
 	}
 
 	public MessageBuilder setReplyTo(Destination replyTo) {
 		this.replyTo = replyTo;
 		return this;
 	}
 
 	public MessageBuilder setTimestamp(long timestamp) {
 		this.timestamp = timestamp;
 		return this;
 	}
 
 	public MessageBuilder setType(String type) {
 		this.type = type;
 		return this;
 	}
 
 	/**
 	 * Set delayedTime on {@code MockMessage}.
 	 * 
 	 * @param delayedTime
 	 *            delayed time in ms.
	 * @return
 	 */
 	public MessageBuilder setDelayedTimeInMs(long delayedTime) {
 		this.delayedTime = delayedTime;
 		return this;
 	}
 }
