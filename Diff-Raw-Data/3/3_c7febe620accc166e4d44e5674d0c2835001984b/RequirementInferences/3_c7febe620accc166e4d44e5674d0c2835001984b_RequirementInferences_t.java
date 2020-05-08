 /*
  * Created on Jan 19, 2004
  *
  * To change this generated comment go to 
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 package edu.wpi.cs.jburge.SEURAT.inference;
 
 import java.sql.*;
 import java.util.Vector;
 
 import edu.wpi.cs.jburge.SEURAT.rationaleData.*;
 
 /**
  * Performs any necessary inference when a requirement is changed.
  * @author jburge
  */
 public class RequirementInferences {
 	
 	/**
 	 * Empty constructor.
 	 */
 	public RequirementInferences() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 	
 	/**
 	 * Update the requirement status (and any dependent elements). This includes
 	 * checking for requirement violations.
 	 * @param req - the requirement modified
 	 * @return a vector of status that needs to be displayed.
 	 */
 	public Vector<RationaleStatus> updateRequirement(Requirement req)
 	{
 		Vector<RationaleStatus> status;
 		status = new Vector<RationaleStatus>();
 		
 		UpdateManager manager = UpdateManager.getHandle();
 		manager.addUpdate(req.getID(), req.getName(), RationaleElementType.REQUIREMENT);
 		
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		
 		Statement stmt = null; 
 		ResultSet rs = null; 
 		ReqStatus rstat = ReqStatus.UNDECIDED;
 		String findArgQuery = "";
 		//	boolean error = false;
 		try {
 			stmt = conn.createStatement();
 			
 			//look for requirements violations - if the requirement is not retracted or rejected
 			if ((req.getStatus() != ReqStatus.REJECTED) && 
 //					(req.getStatus() != ReqStatus.DEFERRED) &&
 					(req.getStatus() != ReqStatus.RETRACTED) &&
 					(req.getEnabled()))
 			{
 				//reset our status - removed - we should be able to
 				//have satisfied requirements w/o rationale
 //				req.setStatus(ReqStatus.UNDECIDED);
 				//check to see if the requirement was violated
 				findArgQuery = "SELECT * FROM " + RationaleDBUtil.escapeTableName("arguments") 
 				+ " where argtype = 'Requirement' and " +
 				"requirement = " + req.getID() + " and " +
 				"type = 'Violates'";
 //				***			System.out.println(findArgQuery);
 				rs = stmt.executeQuery(findArgQuery);
 				
 				//		Vector arguments;
 				
 				while (rs.next())
 				{	
 					//now, need to find who violates the requirement
 					RationaleElementType element = RationaleElementType.fromString(rs.getString("ptype"));
 					int parent = rs.getInt("parent");
 					
 					if (element == RationaleElementType.ALTERNATIVE)
 					{
 						Alternative alt = new Alternative();
 						alt.fromDatabase(parent);
						//we only report violations for FRs
						if ((req.getType() == ReqType.FR) && (alt.getStatus() == AlternativeStatus.ADOPTED))
 						{
 							req.setStatus(ReqStatus.VIOLATED);
 							rstat = ReqStatus.VIOLATED;	
 							//in this case, put the error on the requirement
 							//we assume that the error on the alternative occurs separately
 							RationaleErrorLevel err = RationaleErrorLevel.ERROR;
 							String problem = "Violated by alt '" + alt.getName() + "'";
 							RationaleStatus stat = new RationaleStatus(err, problem, 
 									RationaleElementType.REQUIREMENT, new java.util.Date(), req.getID(),
 									RationaleStatusType.REQ_VIOLATION);
 							status.add(stat);		
 							
 						}
 					}
 					//can't have an argument that an argument violates a requirement
 					else if (element == RationaleElementType.ARGUMENT)
 					{
 					}
 					//can a requirement violate a requirement?  no
 					else if (element == RationaleElementType.REQUIREMENT)
 					{
 					}
 					//can you have arguments for decisions? not at this point
 					else if (element == RationaleElementType.DECISION)
 					{
 					}
 					
 					
 				} //check violated
 				
 				//check to see if the requirement has been satisfied
 //				if (req.getStatus() == ReqStatus.UNDECIDED)
 				if (rstat == ReqStatus.UNDECIDED)
 				{
 					//check to see if the requirement was satisifed
 					findArgQuery = "SELECT * FROM "+RationaleDBUtil.escapeTableName("arguments")
 					+" where argtype = 'Requirement' and " +
 					"requirement = " + req.getID() + " and " +
 					"type = 'Satisfies'";
 //					***				System.out.println(findArgQuery);
 					rs = stmt.executeQuery(findArgQuery);
 					while (rs.next())
 					{
 						RationaleElementType element = RationaleElementType.fromString(rs.getString("ptype"));
 						int parent = rs.getInt("parent");
 						
 						if (element == RationaleElementType.ALTERNATIVE)
 						{
 							Alternative alt = new Alternative();
 							alt.fromDatabase(parent);
 							if (alt.getStatus() == AlternativeStatus.ADOPTED)
 							{
 								req.setStatus(ReqStatus.SATISFIED);
 								rstat = ReqStatus.SATISFIED;
 							}
 						}
 						
 					}
 				}
 				
 //				if (req.getStatus() == ReqStatus.UNDECIDED)
 				if (rstat == ReqStatus.UNDECIDED)
 				{
 					//check to see if the requirement was addressed
 					findArgQuery = "SELECT * FROM "+RationaleDBUtil.escapeTableName("arguments")
 					+" where argtype = 'Requirement' and " +
 					"requirement = " + req.getID() + " and " +
 					"type = 'Addresses'";
 //					***				System.out.println(findArgQuery);
 					rs = stmt.executeQuery(findArgQuery);
 					while(rs.next())
 					{
 						RationaleElementType element = RationaleElementType.fromString(rs.getString("ptype"));
 						int parent = rs.getInt("parent");
 						if (element == RationaleElementType.ALTERNATIVE)
 						{
 							Alternative alt = new Alternative();
 							alt.fromDatabase(parent);
 							if (alt.getStatus() == AlternativeStatus.ADOPTED)
 							{
 								req.setStatus(ReqStatus.ADDRESSED);
 								rstat= ReqStatus.ADDRESSED;
 							}
 						}
 						
 					}
 				}			
 				
 				//If we didn't come up with a violated, satisfied, or addressed
 				//status, then we only change the original status if it was 
 				//previously violated.
 				if ((rstat == ReqStatus.UNDECIDED) && (req.getStatus() == ReqStatus.VIOLATED))
 				{
 					req.setStatus(rstat);
 				}
 				req.toDatabase(req.getParent(), RationaleElementType.fromString(req.getPtype()));
 			} //not disabled, etc.
 			
 			//if it has been rejected, retracted, or disabled, we need to
 			//recompute arguments
 			else
 			{
 				//check to see if the requirement was violated
 				findArgQuery = "SELECT * FROM "+RationaleDBUtil.escapeTableName("arguments")
 				+" where argtype = 'Requirement' and " +
 				"requirement = " + req.getID();
 //				***			System.out.println(findArgQuery);
 				rs = stmt.executeQuery(findArgQuery);
 				while (rs.next())
 				{
 					//now, need to find who violates the requirement
 					RationaleElementType element = RationaleElementType.fromString(rs.getString("ptype"));
 					int parent = rs.getInt("parent");
 					
 					if (element == RationaleElementType.ALTERNATIVE)
 					{
 						Alternative alt = new Alternative();
 						alt.fromDatabase(parent);
 						AlternativeInferences aInf = new AlternativeInferences();
 						//we can do this safely because the alternative will not
 						//update the requirement status if the requirement is not
 						//enabled, active, etc.
 						status.addAll(aInf.updateAlternative(alt));
 					}
 					
 				}			
 			}
 			
 		} catch (SQLException ex) {
 			RationaleDB.reportError(ex, "RequirementInferences.updateRequirement",
 					findArgQuery);
 		}
 		
 		finally { 
 			RationaleDB.releaseResources(stmt, rs);
 		}
 		return status;
 	}
 	
 	public Vector<Argument> getArguments(Requirement req)
 	{
 		Vector<Argument> args;
 		args= new Vector<Argument>();
 		
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		
 		Statement stmt = null; 
 		ResultSet rs = null; 
 		String findArgQuery = "";
 		//	boolean error = false;
 		try {
 			stmt = conn.createStatement();
 			//check to see if the requirement was violated
 			findArgQuery = "SELECT * FROM "+RationaleDBUtil.escapeTableName("arguments")
 			+" where argtype = 'Requirement' and " +
 			"requirement = " + req.getID();
 //			System.out.println(findArgQuery);
 			rs = stmt.executeQuery(findArgQuery);
 			
 			while (rs.next())
 			{	
 				Argument nextArg = new Argument();
 				String name = RationaleDBUtil.decode(rs.getString("name"));
 				nextArg.fromDatabase(name);
 				args.add(nextArg);
 			}
 			
 		} catch (SQLException ex) {
 			RationaleDB.reportError(ex, "RequirementInferences.getArguments", 
 					findArgQuery);
 		}
 		
 		finally { 
 			RationaleDB.releaseResources(stmt, rs);
 		}
 		return args;
 	}
 	
 	public Vector<Argument> getArguments(Requirement req, ArgType type)
 	{
 		Vector<Argument> args;
 		args= new Vector<Argument>();
 		
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		
 		Statement stmt = null; 
 		ResultSet rs = null; 
 		String findArgQuery = "";
 		//	boolean error = false;
 		try {
 			stmt = conn.createStatement();
 			//check to see if the requirement was violated
 			findArgQuery = "SELECT * FROM "+RationaleDBUtil.escapeTableName("arguments")
 			+" where argtype = 'Requirement' and " +
 			"requirement = " + req.getID() + " and " +
 			"type = '" + type.toString() + "'";
 //			System.out.println(findArgQuery);
 			rs = stmt.executeQuery(findArgQuery);
 			
 			while (rs.next())
 			{	
 				Argument nextArg = new Argument();
 				String name = RationaleDBUtil.decode(rs.getString("name"));
 				nextArg.fromDatabase(name);
 				args.add(nextArg);
 			}
 			
 		} catch (SQLException ex) {
 			RationaleDB.reportError(ex, "RequirementInferences.getArguments", 
 					findArgQuery);
 		}
 		
 		finally { 
 			RationaleDB.releaseResources(stmt, rs);
 		}
 		return args;
 	}
 	
 }
