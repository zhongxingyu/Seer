 package com.dajudge.serinstream;
 
 /**
  * A singleton class to configure serialization used by
 * {@link SerializableInputStream}. The default configuration serializes into
  * memory.
  * 
  * @author Alex Stockinger
  */
 public class SerializableInputStreamConfiguration {
 	private static SerializableInputStreamConfiguration INSTANCE = createDefaultConfiguration();
 
 	private SerializationTempStore tempStore;
 
 	/**
 	 * Creates the default configuration.
 	 * 
 	 * @return the default configuration.
 	 */
 	private static SerializableInputStreamConfiguration createDefaultConfiguration() {
 		final SerializableInputStreamConfiguration ret = new SerializableInputStreamConfiguration();
 		ret.setSerializationTempStore(new MemorySerializationTempStore());
 		return ret;
 	}
 
 	/**
 	 * Sets the serialization temp store. The temp store is used for persisting
 	 * serialization data while the stream is retrieved. By default the main
 	 * memory is used to cache the deserialized data.
 	 * 
 	 * @param tempStore
 	 *            the temp store the be used for deserialization.
 	 */
 	public void setSerializationTempStore(final SerializationTempStore tempStore) {
 		this.tempStore = tempStore;
 	}
 
 	public SerializationTempStore getSerializationTempStore() {
 		return tempStore;
 	}
 
 	/**
 	 * Retrieves the singleton instance.
 	 * 
 	 * @return the singleton instance.
 	 */
 	public static SerializableInputStreamConfiguration getInstance() {
 		return INSTANCE;
 	}
 }
