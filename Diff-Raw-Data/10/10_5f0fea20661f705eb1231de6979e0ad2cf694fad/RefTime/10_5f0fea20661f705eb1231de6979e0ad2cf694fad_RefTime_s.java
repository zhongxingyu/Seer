 /**
  * 
  */
 package rfctool;
 
 /**
  * RFCTool - RefTimes
  * @version 1.0
  * @author <a href="mailto:Dessimat0r@ntlworld.com">Chris Dennett</a>
  */
 public enum RefTime {
 	_30_S("30 secs", 30 * 1000),
 	_1_M("1 min", 60 * 1000),
 	_5_M("5 mins", (60 * 5) * 1000);
 
 	protected final long   timeMS;
 	protected final String text;
 
 	/**
 	 * @param text 
 	 * @param timeMS 
 	 */
 	private RefTime(String text, long timeMS) {
 		this.timeMS = timeMS;
 		this.text   = text;
 	}
 
 	/**
 	 * @return the text
 	 */
 	public String getText() {
 		return text;
 	}
 
 	/**
 	 * @return the timeMS
 	 */
 	public long getTimeMS() {
 		return timeMS;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Enum#toString()
 	 */
 	@Override
 	public String toString() {
 		return text;
 	}
}
