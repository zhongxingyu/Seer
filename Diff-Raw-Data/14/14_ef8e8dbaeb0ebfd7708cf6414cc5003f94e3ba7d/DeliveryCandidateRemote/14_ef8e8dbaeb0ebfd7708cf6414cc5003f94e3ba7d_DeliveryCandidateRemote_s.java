 package org.devilry.core.session.dao;
 
 import java.util.List;
 import javax.ejb.Remote;
 
 @Remote
 public interface DeliveryCandidateRemote {
 	
 	/**
 	 * Creates a delivery candidate in the database
 	 * @return the database id of the delivery candidate
 	 */
 	public long create();
 		
 	/**
 	 * Get parent (Delivery) node
 	 * @return
 	 */
	public long getDelivery(long deliveryCandiateId);
 		
 	/**
 	 * Add a file to the delivery candidate with ID deliveryCandiateId
 	 * @param filePath
 	 * @return the ID of the file that was added
 	 */
	public long addFile(long deliveryCandiateId, String filePath);
 		
 	/**
 	 * Get all the files in the delivery candidate.
 	 * @param deliveryCandiateId
 	 * @return
 	 */
	public List<Long> getFiles(long deliveryCandiateId);
 	
 	/**
 	 * Set the status of the delivery
 	 * @param deliveryCandiateId
 	 * @param status
 	 */
	public void setStatus(long deliveryCandiateId, short status);
 	
 	/**
 	 * Get the status of the delivery.
 	 * @param deliveryCandiateId
 	 * @return
 	 */
	public short getStatus(long deliveryCandiateId);
 	
 }
