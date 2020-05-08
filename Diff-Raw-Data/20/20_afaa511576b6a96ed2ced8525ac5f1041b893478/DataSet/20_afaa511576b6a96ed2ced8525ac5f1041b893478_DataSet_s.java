 package com.k_int.euinside.client.module.europeana;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Class to define a Europeana data set
  */
 public class DataSet extends EuropeanaItemResult {
 	private static Log log = LogFactory.getLog(DataSet.class);
 	private String status;
 	private Long publishedRecords;
 	private Long deletedRecords;
 
 	public DataSet() {
 	}
 	
 	@Override
 	protected Log getLogger() {
 		return(log);
 	}
 	
 	/**
 	 * Retrieves the status
 	 * 
 	 * @return The status of the data set
 	 */
 	public String getStatus() {
 		return(status);
 	}
 
 	/**
 	 * Sets the status
 	 * @param status The status of the data set
 	 */
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	/**
 	 * Retrieves the number of accepted records
 	 * 
 	 * @return The number of accepted records in the data set
 	 */
 	public Long getPublishedRecords() {
 		return(publishedRecords);
 	}
 
 	/**
 	 * Sets the number of accepted records
 	 * 
	 * @param accepted The number of accepted records in the data set
 	 */
 	public void setPublishedRecords(Long publishedRecords) {
 		this.publishedRecords = publishedRecords;
 	}
 
 	/**
 	 * Retrieves the number of rejected records
 	 * 
 	 * @return The number of rejected records in the data set
 	 */
 	public Long getDeletedRecords() {
 		return(deletedRecords);
 	}
 
 	/**
 	 * Sets the number of rejected records
 	 * 
	 * @param rejected The number of rejected records in the data set
 	 */
 	public void setDeletedRecords(String deletedRecords) {
 		try {
 			this.deletedRecords = Long.parseLong(deletedRecords);
 		} catch (NumberFormatException e) {
 			// Not a valid number so we will ignore for now
 		}
 	}
 
 	/**
 	 * Formats the members of this class in a simple to view way
 	 * 
 	 * @return The formatted string
 	 */
 	public String toString() {
 		String result = "Class: DataSet\n";
 		result += super.toString();
 		result += "Status: " + status + "\n"; 
 		result += "Published Records: " + publishedRecords.toString() + "\n"; 
 		result += "Deleted Records: " + ((deletedRecords == null) ? "" : deletedRecords.toString()) + "\n"; 
 		return(result);
 	}
 }
