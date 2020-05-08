 package com.mymed.model.data.reputation;
 
 import com.mymed.model.data.AbstractMBean;
 
 /**
  * 
  * @author lvanni
  * 
  */
 public class MInteractionBean extends AbstractMBean {
 	/**
 	 * Used for the calculation of the hash code
 	 */
 	private static final int PRIME = 31;
 
 	/** INTERACTION_ID */
 	private String id;
 	/** APPLICATION_ID */
 	private String application;
 	/** USER_ID */
 	private String producer;
 	/** USER_ID */
 	private String consumer;
 	private long start;
 	private long end;
 	private double feedback;
 	private int snooze;
 	/** INTERACTION_LIST_ID */
 	private String complexInteraction;
 
 	/**
 	 * Copy constructor.
 	 * <p>
 	 * Provide a clone of the passed MInteractionBean
 	 * 
 	 * @param toClone
 	 *            the interaction bean to clone
 	 */
 	protected MInteractionBean(final MInteractionBean toClone) {
 		id = toClone.getId();
 		application = toClone.getApplication();
 		producer = toClone.getProducer();
 		consumer = toClone.getConsumer();
 		start = toClone.getStart();
 		end = toClone.getEnd();
 		feedback = toClone.getFeedback();
 		snooze = toClone.getSnooze();
 		complexInteraction = toClone.getComplexInteraction();
 	}
 
 	/**
 	 * Create a new empty MInteractionBean
 	 */
 	public MInteractionBean() {
 		// Empty constructor, needed because of the copy constructor
 	}
 
 	@Override
 	public MInteractionBean clone() {
 		final MInteractionBean interactionBean = new MInteractionBean(this);
 		return interactionBean;
 	}
 
 	@Override
 	public String toString() {
 		return "Interaction:\n" + super.toString();
 	}
 
 	/**
 	 * @return the feedback
 	 */
 	public double getFeedback() {
 		return feedback;
 	}
 
 	/**
 	 * @param feedback
 	 *            the feedback to set
 	 */
 	public void setFeedback(final double feedback) {
 		this.feedback = feedback;
 	}
 
 	/**
 	 * @return the start
 	 */
 	public long getStart() {
 		return start;
 	}
 
 	/**
 	 * @param start
 	 *            the start to set
 	 */
 	public void setStart(final long start) {
 		this.start = start;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(final String id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the application
 	 */
 	public String getApplication() {
 		return application;
 	}
 
 	/**
 	 * @param application
 	 *            the application to set
 	 */
 	public void setApplication(final String application) {
 		this.application = application;
 	}
 
 	/**
 	 * @return the produces
 	 */
 	public String getProducer() {
 		return producer;
 	}
 
 	/**
 	 * @param producer
 	 *            the producer to set
 	 */
 	public void setProducer(final String producer) {
 		this.producer = producer;
 	}
 
 	/**
 	 * @return the consumer
 	 */
 	public String getConsumer() {
 		return consumer;
 	}
 
 	/**
 	 * @param consumer
 	 *            the consumer to set
 	 */
 	public void setConsumer(final String consumer) {
 		this.consumer = consumer;
 	}
 
 	/**
 	 * @return the end
 	 */
 	public long getEnd() {
 		return end;
 	}
 
 	/**
 	 * @param end
 	 *            the end to set
 	 */
 	public void setEnd(final long end) {
 		this.end = end;
 	}
 
 	/**
 	 * @return the snooze
 	 */
 	public int getSnooze() {
 		return snooze;
 	}
 
 	/**
 	 * @param snooze
 	 *            the snooze to set
 	 */
 	public void setSnooze(final int snooze) {
 		this.snooze = snooze;
 	}
 
 	/**
 	 * @return the complexInteraction
 	 */
 	public String getComplexInteraction() {
 		return complexInteraction;
 	}
 
 	/**
 	 * @param complexInteraction
 	 *            the complexInteraction to set
 	 */
 	public void setComplexInteraction(final String complexInteraction) {
 		this.complexInteraction = complexInteraction;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {

 		int result = 1;
 		result = PRIME * result + (application == null ? 0 : application.hashCode());
 		result = PRIME * result + (complexInteraction == null ? 0 : complexInteraction.hashCode());
 		result = PRIME * result + (consumer == null ? 0 : consumer.hashCode());
 		long temp;
 		temp = Double.doubleToLongBits(feedback);
 		result = PRIME * result + (int) (temp ^ temp >>> 32);
 		result = PRIME * result + (id == null ? 0 : id.hashCode());
 		result = PRIME * result + (producer == null ? 0 : producer.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object obj) {
 
 		boolean equal = false;
 
 		if (this == obj) {
 			equal = true;
 		} else if (obj instanceof MInteractionBean) {
 			final MInteractionBean comparable = (MInteractionBean) obj;
 
 			equal = true;
 
 			if (application == null && comparable.getApplication() != null) {
 				equal &= false;
 			} else {
 				equal &= application.equals(comparable.getApplication());
 			}
 
 			if (complexInteraction == null && comparable.getComplexInteraction() != null) {
 				equal &= false;
 			} else {
 				equal &= complexInteraction.equals(comparable.getComplexInteraction());
 			}
 
 			if (consumer == null && comparable.getConsumer() != null) {
 				equal &= false;
 			} else {
 				equal &= consumer.equals(comparable.getConsumer());
 			}
 
 			equal &= feedback == comparable.getFeedback();
 
 			if (id == null && comparable.getId() != null) {
 				equal &= false;
 			} else {
 				equal &= id.equals(comparable.getId());
 			}
 
 			if (producer == null && comparable.getProducer() != null) {
 				equal &= false;
 			} else {
 				equal &= producer.equals(comparable.getProducer());
 			}
 		}
 
 		return equal;
 	}
 }
