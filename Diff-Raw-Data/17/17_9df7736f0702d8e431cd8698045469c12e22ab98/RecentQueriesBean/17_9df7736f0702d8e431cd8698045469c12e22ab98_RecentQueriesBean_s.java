 package edu.wustl.common.beans;
 /**
  * @author chitra_garg
  *used as bean for setting the 
  *parameters needed in recent query ajax call 
  */
 public class RecentQueriesBean
 {
 	
 	/**
 	 * title for query
 	 */
	String queryTitle;
 	
 	/**
 	 * status that is in-progress or Completed 
 	 */
	String status;
 	
 	/**
 	 * count
 	 */
	Long resultCount;
 	
 	/**
 	 * creation date assigned to query
 	 */
	String queryCreationDate;
 	/**
	 * xecution id assigned to query
 	 */
	Long queyExecutionId;
 	
 	/**
 	 * @return query title
 	 */
 	public String getQueryTitle()
 	{
 		return queryTitle;
 	}
 	
 	/**
 	 * @param queryTitle = title for query
 	 */
 	public void setQueryTitle(String queryTitle)
 	{
 		this.queryTitle = queryTitle;
 	}
 	
 	/**
 	 * @return status that is in-progress or Completed 
 	 */
 	public String getStatus()
 	{
 		return status;
 	}
 	
 	/**
 	 * @param status set that is in-progress or Completed 
 	 */
 	public void setStatus(String status)
 	{
 		this.status = status;
 	}
 	
 	/**
 	 * @return count 
 	 */ 
 	public Long getResultCount()
 	{
 		return resultCount;
 	}
 	
 	/**
 	 * @param resultCount=count
 	 */
 	public void setResultCount(Long resultCount)
 	{
 		this.resultCount = resultCount;
 	}
 
 	
 	/**
 	 * @return creation date assigned to query
 	 */
 	public String getQueryCreationDate()
 	{
 		return queryCreationDate;
 	}
 
 	
 	/**
 	 * @param queryCreationDate =creation date assigned to query
 	 */
 	public void setQueryCreationDate(String queryCreationDate)
 	{
 		this.queryCreationDate = queryCreationDate;
 	}
 
 	
 	/**
 	 * @return execution id assigned to query
 	 */
 	public Long getQueyExecutionId()
 	{
 		return queyExecutionId;
 	}
 
 	
 	/**
 	 * @param queyExecutionId=execution id assigned to query
 	 */
 	public void setQueyExecutionId(Long queyExecutionId)
 	{
 		this.queyExecutionId = queyExecutionId;
 	}
 
 }
