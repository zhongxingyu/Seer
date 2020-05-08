 package se.chalmers.dat255.sleepfighter.model;
 
 import se.chalmers.dat255.sleepfighter.utils.message.Message;
 import se.chalmers.dat255.sleepfighter.utils.message.MessageBus;
 
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 /**
  * The snooze configuration for an alarm.
  */
 @DatabaseTable(tableName = "snooze_config")
 public class SnoozeConfig implements IdProvider {
 
 	/**
 	 * Field constants for ChangeEvent.
 	 */
 	public static enum Field {
 		SNOOZE_ENABLED, SNOOZE_TIME
 	}
 
 	/**
 	 * An message bus message for a change in SnoozeConfig.
 	 */
 	public static class ChangeEvent implements Message {
 
 		private SnoozeConfig snoozeConfig;
 		private Field field;
 		private Object oldValue;
 
 		public ChangeEvent(SnoozeConfig snoozeConfig, Field field,
 				Object oldValue) {
 			this.snoozeConfig = snoozeConfig;
 			this.field = field;
 			this.oldValue = oldValue;
 		}
 
 		public SnoozeConfig getSnoozeConfig() {
 			return snoozeConfig;
 		}
 
 		public Field getField() {
 			return field;
 		}
 
 		public Object getOldValue() {
 			return oldValue;
 		}
 	}
 
 	public static final String ID_COLUMN = "id";
 
 	@DatabaseField(generatedId = true, columnName = ID_COLUMN)
 	private int id;
 
 	@DatabaseField
 	private int snoozeTime;
 
 	@DatabaseField
 	private boolean snoozeEnabled;
 
 	private MessageBus<Message> bus;
 
 	/**
 	 * Empty constructor, for database use only.
 	 */
 	public SnoozeConfig() {}
 
 	/**
 	 * Constructs a SnoozeConfig.
 	 * 
 	 * @param snoozeEnabled
 	 *            whether snooze is enabled or not
 	 * @param snoozeTime
 	 *            the time to snooze, in minutes
 	 */
 	public SnoozeConfig(boolean snoozeEnabled, int snoozeTime) {
 		this.snoozeEnabled = snoozeEnabled;
 		this.snoozeTime = snoozeTime;
 	}
 
	public SnoozeConfig(SnoozeConfig snoozeConfig) {
		this.snoozeEnabled = snoozeConfig.isSnoozeEnabled();
		this.snoozeTime = snoozeConfig.getSnoozeTime();
	}

 	@Override
 	public int getId() {
 		return this.id;
 	}
 
 	/**
 	 * Gets the configured snooze time, in minutes.
 	 * 
 	 * @return the snooze time in minutes
 	 */
 	public int getSnoozeTime() {
 		return this.snoozeTime;
 	}
 
 	/**
 	 * Sets the snooze time, in minutes
 	 * 
 	 * @param snoozeTime
 	 *            the snooze time in minutes
 	 */
 	public void setSnoozeTime(int snoozeTime) {
 		if (this.snoozeTime == snoozeTime) {
 			return;
 		}
 		int old = snoozeTime;
 		this.snoozeTime = snoozeTime;
 		publish(new ChangeEvent(this, Field.SNOOZE_TIME, old));
 	}
 
 	/**
 	 * Checks if snooze is enabled
 	 * 
 	 * @return if snooze is enabled
 	 */
 	public boolean isSnoozeEnabled() {
 		return snoozeEnabled;
 	}
 
 	/**
 	 * Set whether snooze is enabled or not.
 	 * 
 	 * @param snoozeEnabled
 	 *            whether snooze is enabled or not
 	 */
 	public void setSnoozeEnabled(boolean snoozeEnabled) {
 		if (this.snoozeEnabled == snoozeEnabled) {
 			return;
 		}
 		boolean old = this.snoozeEnabled;
 		this.snoozeEnabled = snoozeEnabled;
 		publish(new ChangeEvent(this, Field.SNOOZE_ENABLED, old));
 	}
 
 	/**
 	 * Sets the message bus.
 	 * 
 	 * @param bus
 	 *            the message bus
 	 */
 	public void setMessageBus(MessageBus<Message> bus) {
 		this.bus = bus;
 	}
 
 	/**
 	 * Publish an event to the message bus.
 	 * 
 	 * @param event
 	 *            the ChangeEvent
 	 */
 	private void publish(ChangeEvent event) {
 		if (this.bus == null) {
 			return;
 		}
 		this.bus.publish(event);
 	}
 }
