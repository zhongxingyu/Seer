 package edu.wpi.cs.jburge.SEURAT.rationaleData;
 
 import java.sql.*;
 
 
 /**
  * Defines the contents of a RationaleStatus record. These capture errors
  * and warnings discovered in the rationale.
  * @author burgeje
  *
  */
 public class RationaleStatus {
 
 	/**
 	 * The id of our parent rationale element (whose status we are giving)
 	 */
 	private int pid; 
 	/**
 	 * Our status level. Initially set to Information
 	 */
 	private RationaleErrorLevel status = RationaleErrorLevel.INFORMATION;
 	/**
 	 * A description of the problem
 	 */
 	private String description 	= "";
 	/**
 	 * The type of element we are the status of
 	 */
 	private RationaleElementType type = RationaleElementType.RATIONALE;
 	/**
 	 * When the error was reported
 	 */
 	private java.util.Date dateStamp;
 	/**
 	 * What the error is
 	 */
 	private RationaleStatusType stype = null;
 	/**
 	 * Has this error been overriden? Overriden errors will not appear on
 	 * the Rationale Task List
 	 */
 	private boolean override;
 
 	/**
 	 * Rationale status constructor
 	 * @param tstatus - the error level
 	 * @param tdescription - a description of the error
 	 * @param ttype - the type element the error is for
 	 * @param date - when this happened
 	 * @param pid - the ID of the element it is about
 	 * @param stype - the type of error
 	 */
 	public RationaleStatus(RationaleErrorLevel tstatus, String tdescription, RationaleElementType ttype,
 		 java.util.Date date, int pid, RationaleStatusType stype)
 		{
 			super();
 			setDescription(tdescription);
 			setStatus(tstatus);
 			setRationaleType(ttype);
 			this.pid = pid;
 			this.stype = stype;
 			this.override = false;
 		}
 	
 	public boolean equivalentTo(RationaleStatus stat)
 	{
 		//need to compare item, parent, and description (in case name changes)
 		boolean equiv = false;
 		if ((stat.getStatusType() == stype) &&
 			(stat.getParent() == pid) &&
 			(stat.getDescription().compareTo(description) == 0))
 			{
 				equiv = true;
 			}
 		return equiv;
 	}
 	/**
 	 * @return true if error, false otherwise
 	 */
 	public boolean isError() {
 		return (status == RationaleErrorLevel.ERROR);
 	}
 	public boolean isWarning() {
 		return (status == RationaleErrorLevel.WARNING);
 	}
 	public boolean isInformation() {
 		return (status == RationaleErrorLevel.INFORMATION);
 	}
 	
 	public RationaleErrorLevel getStatus() {
 		return status;
 	}
 
 
 	public String getDescription() {
 		return description;
 	}
 
 
 	public RationaleElementType getRationaleType() {
 		return type;
 	}
 
 	public int getParent() {
 		return pid;
 	}
 	
 	public java.util.Date getDate() {
 		return dateStamp;
 	}
 
 	public void setStatus(RationaleErrorLevel stat) {
 		status = stat;
 	}
 
 	public void setOverride(boolean ov) {
 		override = ov;
 	}
 	
 	public boolean getOverride() {
 		return override;
 	}
 
 	public void setDescription(String string) {
 		description = string;
 	}
 
 
 	/**
 	 * Get our description
 	 */
     public String toString()
     {
     	return description;
     }
 	public void setRationaleType(RationaleElementType rat) {
 		type = rat;
 	}
 	
 	public void setStatusType(RationaleStatusType stat) {
 		stype = stat;
 	}
 	
 	public RationaleStatusType getStatusType() {
 		return stype;
 	}
 	
 	/**
 	 * Save the status element to the database
 	 * @param parent - the parent rationale element ID
 	 * @return true if this status is new and wasn't already in the database
 	 */
 	public boolean toDatabase(int parent)
 	{
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		boolean newStatus = true;
 		//find out if this status item is already in the database
 		Statement stmt = null; 
 		ResultSet rs = null; 
 		
 		System.out.println("Saving Status to the database");
 
 		try {
 			 stmt = conn.createStatement(); 
 			 String findQuery = "SELECT parent, ptype, description, status FROM status where parent ='" +
 				this.pid + "' and ptype = '" +
 				this.type + "' and status = '" + 
 				this.stype + "'";
 //***			 System.out.println(findQuery);
 			 rs = stmt.executeQuery(findQuery); 
 
 			if (rs.next())
 			{
 				newStatus = false;
 				String over = "No";
 				if (this.override)
 				{
 					over = "Yes";
 				}
 			   String stat = rs.getString("status");
 			   String updateQuery;
 			   updateQuery = "UPDATE Status " +
 					   "SET Override = '" + over + "' " +
 					   "WHERE status = '" + stat +
 					   "' AND parent = " + this.pid +
 				   " and ptype = '" + this.type + "';";
 			   stmt.execute(updateQuery);
 		
 			}
 
 	
 		else 
 		{
 		
 			//now, we have determined that the status item is new
 			String parentRSt = new Integer(parent).toString();
 			System.out.println("parent");	
 			
 			Timestamp time = new Timestamp((new java.util.Date()).getTime());
 			System.out.println(this.description);
 			System.out.println(this.status.toString());
 			System.out.println(this.type.toString());
 			System.out.println(this.stype.toString());
 			String newStatSt = "INSERT INTO Status "+
 			   "(date, description, type, ptype, parent, status, override) " +
 			   "VALUES ('" +
 			   time.toString() + "', '" +
 			   RationaleDB.escape(this.description) + "', '" +
 			   this.status.toString() + "', '" +
 //			   RationaleDB.escape(this.rationale) + "', '" +
 			   this.type.toString() + "', " +
 			   parentRSt + ", '" +
 				stype.toString() + "', '" +
 				"No')";
 
 			   System.out.println(newStatSt);
 			stmt.execute(newStatSt); 
 			
 		}
 
 		} catch (SQLException ex) {
	   // handle any errors 
	   System.out.println("SQLException (stat to db): " + ex.getMessage()); 
	   System.out.println("SQLState: " + ex.getSQLState()); 
	   System.out.println("VendorError: " + ex.getErrorCode()); 
 	   }
    	   
 	   finally { 
		   // it is a good idea to release
		   // resources in a finally{} block 
		   // in reverse-order of their creation 
		   // if they are no-longer needed 

		   if (rs != null) { 
			   try {
				   rs.close(); 
			   } catch (SQLException sqlEx) { // ignore 
			   } 

			   rs = null; 
		   }
    
		   if (stmt != null) { 
			   try { 
				   stmt.close(); 
			   } catch (SQLException sqlEx) { // ignore
				   } 

			   stmt = null; 
		   } 
 		   }
 
 		return newStatus; 
 	}	
 
 
 }
