 package edu.nyu.cs.adb;
 
 /**
  * A response object sent from the Data Manager to the Transaction Manager 
  * <p>Immutable</p>
  * @author dandelarosa
  */
 public final class Response {
 	private final int siteID;
 	private final int transactionID;
 	private final Status status;
 	private final int readValue;
 	
 	/**
 	 * Status codes
 	 * @author dandelarosa
 	 */
 	public static enum Status {
 		SUCCESS,
 		LOCKED,
 		FAILURE
 	}
 	
 	/**
 	 * Prevent instantiation!
 	 */
 	private Response () {
 		throw new AssertionError();
 	}
 	
 	/**
 	 * Constructor using a builder
 	 * @param builder
 	 */
 	private Response (Builder builder) {
 		this.readValue = builder.readValue;
 		this.siteID = builder.siteID;
 		this.status = builder.status;
 		this.transactionID = builder.transactionID;
 	}
 	
 	/**
 	 * A buider class for Response
 	 * @author dandelarosa
 	 */
 	public static class Builder {
 		private final int siteID;
 		private int transactionID;
 		private final Status status;
 		private int readValue;
 		
 		/**
 		 * Constructor
 		 * @param siteId
 		 * @param status
 		 */
 		public Builder (int siteId, Status status) {
 			this.siteID = siteId;
 			this.status = status;
 			this.transactionID = 0;
 			this.readValue = 0;
 		}
 		
 		/**
 		 * Sets the transaction ID for the builder
 		 * @param transactionID
 		 * @return self
 		 */
 		public Builder setTransactionID (int transactionID) {
 			this.transactionID = transactionID;
 			return this;
 		}
 		
 		/**
 		 * Sets the read value for the builder (if applicable)
 		 * @param readValue
 		 * @return self
 		 */
 		public Builder setReadValue (int readValue) {
 			if (this.status != Status.SUCCESS) {
 				throw new UnsupportedOperationException("Read value should " +
 						"be set only for successful read operations");
 			}
 			this.readValue = readValue;
 			return this;
 		}
 		
 		/**
 		 * Generates a new Response object from the builder
 		 * @return a new instance of Response
 		 */
 		public Response build () {
 			return new Response(this);
 		}
 	}
 	
 	/**
 	 * Reads which site the response is from
 	 * @return The site ID
 	 */
 	public int getSiteID () { return siteID; }
 	
 	/**
 	 * Reads which transaction the site is referring to
 	 * @return The transaction ID
 	 */
 	public int getTransactionID () { return transactionID; }
 	
 	/**
 	 * Reads the status of the transaction
 	 * @return The status ID (later this can be elaborated upon to describe 
 	 * the different status codes: fail, locked, etc.)
 	 */
 	public Status getStatus () { return status; }
 	
 	/**
 	 * Reads the value, usually this in response to a read request
 	 * @return The value returned from the a read operation
 	 */
	public int getReadValue () { return readValue; }
 }
