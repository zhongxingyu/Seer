 package edu.nyu.cs.adb;
 
 /**
  * Describes an operation to be done by the Data Manager
  * <p>Immutable</p>
  * @author dandelarosa
  */
 public final class Operation {
 	private final int transactionID;
 	private final Opcode opcode;
 	private final int variableID;
 	private final int writeValue;
 	
 	/**
 	 * Operation code values
 	 * @author dandelarosa
 	 */
 	public static enum Opcode {
 		BEGIN, 
 		BEGIN_READONLY, 
 		READ, 
 		WRITE,
 		ABORT, 
 		FINISH
 	}
 	
 	/**
 	 * Prevent Instantiation!
 	 */
 	private Operation () {
 		throw new AssertionError();
 	}
 	
 	/**
 	 * Constructs a new Operation from a builder object
 	 * @param builder
 	 */
 	private Operation (Builder builder) {
 		this.opcode = builder.opcode;
 		this.transactionID = builder.transactionID;
 		this.variableID = builder.variableID;
 		this.writeValue = builder.writeValue;
 	}
 	
 	/**
 	 * Builder class for Operation
 	 * @author dandelarosa
 	 */
 	public static class Builder {
 		private int transactionID;
 		private final Opcode opcode;
 		private int variableID;
 		private int writeValue;
 		
 		/**
 		 * Create a builder for a given operation
 		 * @param opcode
 		 */
 		public Builder (Opcode opcode) {
 			this.opcode = opcode;
 		}
 		
 		/**
 		 * Set the transaction ID
 		 * @param transactionID
 		 * @return self
 		 */
 		public Builder setTransactionID (int transactionID) {
 			this.transactionID = transactionID;
 			return this;
 		}
 		
 		/**
 		 * Set the variableID
 		 * @param variableID
 		 * @return self
 		 */
 		public Builder setVariableID (int variableID) {
 			this.variableID = variableID;
 			return this;
 		}
 		
 		/**
 		 * Set the write value
 		 * @param writeValue
 		 * @return self
 		 * @throws UnsupportedOperationException if this not a write operation
 		 */
 		public Builder setWriteValue (int writeValue) {
 			if (this.opcode != Opcode.WRITE) {
 				throw new UnsupportedOperationException("Write value " +
 						"should be defined only for write operations.");
 			}
 			this.writeValue = writeValue;
 			return this;
 		}
 		
 		/**
 		 * Generate a new Operation from the builder
 		 * @return a new instance of Operation
 		 */
 		public Operation build () {
 			return new Operation(this);
 		}
 	}
 
 	/**
 	 * Gets the ID of the transaction that requests the operation
 	 * @return The transaction ID
 	 */
 	public int getTransactionID () { return transactionID; }
 	
 	/**
 	 * Gets the opcode to check whether this operation is a begin, 
 	 * beginReadOnly, read, write, abort, or finish
 	 * @return The opcode
 	 */
 	public Opcode getOperationID () { return opcode; }
 	
 	/**
 	 * If this operation is a read or write, get the ID of the variable the 
 	 * operation is asked to act on
 	 * @return The variable ID
 	 */
	public int getVariableID () { return variableID; }
 	
 	/**
 	 * If the operation is a write operation, get the value it is supposed to 
 	 * write
 	 * @return The write value
 	 * @throws UnsupportedOperationException if this is not a write operation
 	 */
 	public int getWriteValue () {
 		if (this.opcode != Opcode.WRITE) {
 			throw new UnsupportedOperationException("Not a write operation");
 		}
 		return writeValue;
 	}
 }
