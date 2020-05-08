 package net.buddat.mineserv.options;
 
 /**
  * Wrapper class for Boolean options
  * 
  * @author Budda
  */
 public class BooleanOption extends Option {
 
 	/** The boolean value of the option. */
 	private boolean value;
 	
 	/**
 	 * Instantiates a new boolean option.
 	 *
 	 * @param key the key
 	 * @param value the value
 	 */
 	public BooleanOption(String key, boolean value) {
 		this(key, value, null);
 	}
 	
 	/**
 	 * Instantiates a new boolean option.
 	 *
 	 * @param key the key
 	 * @param value the value
 	 * @param comment the comment to be stored in the config file
 	 */
 	public BooleanOption(String key, boolean value, String comment) {
 		setKey(key);
 		setValue(value);
 		setComment(comment);
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.buddat.mineserv.options.Option#setValue(java.lang.Object)
 	 */
 	@Override
 	public void setValue(Object newValue) {
		this.value = (Boolean) newValue;
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.buddat.mineserv.options.Option#getValue()
 	 */
 	@Override
 	public Boolean getValue() {
 		return value;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.buddat.mineserv.options.Option#toString()
 	 */
 	@Override
 	public String toString() {
 		return (getComment() == null ? "" : Options.COMMENT_CHAR + getComment() + "\r\n") + getKey() + Options.DELIMITER + getValue();
 	}
 
 }
