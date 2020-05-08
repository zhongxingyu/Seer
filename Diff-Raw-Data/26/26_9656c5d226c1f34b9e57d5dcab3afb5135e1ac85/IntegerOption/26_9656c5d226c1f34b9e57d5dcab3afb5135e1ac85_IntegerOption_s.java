 package net.buddat.mineserv.options;
 
 /**
  * Wrapper class for Integer options.
  * 
  * @author Budda
  */
 public class IntegerOption extends Option {
 
 	/** The integer value of the option. */
 	private int value;
 	
 	/**
 	 * Instantiates a new integer option.
 	 *
 	 * @param key the key
 	 * @param value the value
 	 */
 	public IntegerOption(String key, int value) {
 		this(key, value, null);
 	}
 			
 	/**
 	 * Instantiates a new integer option.
 	 *
 	 * @param key the key
 	 * @param value the value
 	 * @param comment the comment to be stored in the config file
 	 */
 	public IntegerOption(String key, int value, String comment) {
 		setKey(key);
 		setValue(value);
 		setComment(comment);
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.buddat.mineserv.options.Option#setValue(java.lang.Object)
 	 */
 	@Override
 	public void setValue(Object newValue) {
		this.value = (Integer) newValue;
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.buddat.mineserv.options.Option#getValue()
 	 */
 	@Override
 	public Integer getValue() {
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
