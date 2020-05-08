 package repository;
 
 /**
  * @author weiqi kong
  * @author 
  */
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import domain.Attributes;
 import domain.Classes;
 import domain.MatricsObject;
 import domain.MatricsType;
 import domain.Policy;
 
 
 public class PolicyDAO {
 
     /**
      * Add Policy into DB 
      * 			
      * @param policy
      */
     public static Policy addPolicy(Policy policy) {
     	ResultSet rs;
 	try {
 		Connection conn = DbManager.getConnection();		
 	    PreparedStatement pstmt = conn.prepareStatement(
 		    "INSERT into policy(policyName, policyDescription,policyLevel) VALUES(?,?,?);");
 	    pstmt.setString(1, policy.getPolicyName());
 		pstmt.setString(2, policy.getPolicyDescription());
 		pstmt.setInt(3, policy.getPolicyLevel());
 		
 	    // Execute the SQL statement and update database accordingly.
 	    pstmt.executeUpdate();
 	    rs = pstmt.getGeneratedKeys();
 	    if (rs.next()) {
 		int newId = rs.getInt(1);
 		policy.setPolicyID(newId);
 	    }
 	    pstmt.close();
 	    rs.close();
 	    conn.close();
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    return null;
 	}
 	return policy;
     }
     
   
     
     
     
     /**
      * Update  Policy into DB 
      * 			
      * @param policy
      */    
     public static Policy UpdatePolicy(Policy policy) {
     	try {
     		
     		Connection conn = DbManager.getConnection();
     	    PreparedStatement pstmt = conn.prepareStatement(
     		    "UPDATE policy SET policyName=?,policyDescription=?,policyLevel=? WHERE policyID=?;");
     	    pstmt.setString(1, policy.getPolicyName());
 			pstmt.setString(2, policy.getPolicyDescription());
 			pstmt.setInt(3, policy.getPolicyLevel());
 			pstmt.setInt(4, policy.getPolicyID());
 		    // Execute the SQL statement and update database accordingly.
 		    pstmt.executeUpdate();
 			
     	    pstmt.close();
     	    conn.close();
     	} catch (SQLException e) {
     	    e.printStackTrace();
     	    return policy;
     	}
     	return policy;
         }
 
     /**
      * Remove a Policy from DB
      * 
      * @param policyID  
      */
     public static boolean deletePolicy(int policyID) {
 		
 		try {
 			Connection conn = DbManager.getConnection();
 			PreparedStatement pstmt = conn
 					.prepareStatement("DELETE FROM Policy where policyID = ?;");
 			pstmt.setInt(1, policyID );
 
 			// Execute the SQL statement and update database accordingly.
 			pstmt.executeUpdate();
 
 			pstmt.close();
 			conn.close();
 			return true;
 		} catch (SQLException e) {
 			throw new IllegalArgumentException(e.getMessage(), e);
 		}
 	}
     
     public static Policy getPolicy(int diagramID){
     	
     	try {
     		Policy policy = new Policy();
 			Connection conn = DbManager.getConnection();
 			PreparedStatement pstmt = conn
 					.prepareStatement("SELECT p.* FROM policy p INNER JOIN diagramContext dc ON p.policyId = dc.policyId WHERE dc.diagramContextId in (SELECT contextid FROM diagram where diagramId = ?);");
 			pstmt.setInt(1, diagramID );
 
 			// Execute the SQL statement and update database accordingly.
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				policy.setPolicyID(rs.getInt("policyId"));
 				policy.setPolicyName(rs.getString("policyName"));
 				policy.setPolicyLevel(rs.getInt("policyLevel"));
 				policy.setPolicyDescription(rs.getString("policyDescription"));
 				
 			}
 			pstmt.close();
 			ArrayList<MatricsObject> mObjs = new ArrayList<MatricsObject>();
 			PreparedStatement pstmt2 = conn.prepareStatement("SELECT * FROM Metric m WHERE policyId = ?");
 			pstmt2.setInt(1,  policy.getPolicyID());
 			ResultSet rs2 = pstmt2.executeQuery();
 			while (rs2.next()) {
 				
 				MatricsType mType = MatricsType.fromInt(rs2.getInt("metricTypeId"));
 				int mId = rs2.getInt("metricId");
 				int policyId = rs2.getInt("policyId");
 				int matricsWt = rs2.getInt("metricsWeight");
 				
 				if(mType == MatricsType.CLASSES)
 				{
 					Classes mObj = new Classes();
 					mObj.setPolicyId(policyId);
 					mObj.setMatricsType(mType);
 					mObj.setMatricsId(mId);
 					mObj.setMatricsWeight(matricsWt);
 					
 					PreparedStatement pstmt3 = conn.prepareStatement("SELECT * FROM CLASSES m WHERE metricId = ?");
 					pstmt3.setInt(1,  mId);
 					ResultSet rs3 = pstmt3.executeQuery(); 
 					while (rs3.next()) {
 						
 						mObj.setIdealNoOfClasses(rs3.getInt("idealNoOfClasses"));
 						mObj.setMaxNoOfClasses(rs3.getInt("maxNoOfClasses"));
 						mObj.setMinNoOfClasses(rs3.getInt("minNoOfClasses"));
 						
 					}
 					pstmt3.close();
 					mObjs.add(mObj);
 				
 				}
 				if(mType == MatricsType.ATTRIBUTES)
 				{
 					Attributes mObj = new Attributes();
 					mObj.setPolicyId(policyId);
 					mObj.setMatricsType(mType);
 					mObj.setMatricsId(mId);
 					mObj.setMatricsWeight(matricsWt);
 					
 					PreparedStatement pstmt3 = conn.prepareStatement("SELECT * FROM Attributes m WHERE metricId = ?");
 					pstmt3.setInt(1,  mId);
 					ResultSet rs3 = pstmt3.executeQuery(); 
 					while (rs3.next()) {
 						
 						mObj.setIdealNoOfAttributes(rs3.getInt("idealNoOfAttributes"));
 						mObj.setMaxNoOfAttributes(rs3.getInt("maxNoOfAttributes"));
 						mObj.setMinNoOfAttributes(rs3.getInt("minNoOfAttributes"));
 						
 					}
 					pstmt3.close();
 					mObjs.add(mObj);
 				}
 				
 				
 			}
 			pstmt2.close();
 			conn.close();
 			policy.setMatricsObjects(mObjs);
 			return policy;
 		} catch (SQLException e) {
 			throw new IllegalArgumentException(e.getMessage(), e);
 		}
     }
     
     public static ArrayList<Policy> getAllPolicys() throws SQLException {
     	ArrayList<Policy> policys = new ArrayList<Policy>();
     	Connection conn = null;
     	PreparedStatement pstmt = null;
     	ResultSet rs = null;
     	try {
     		conn = DbManager.getConnection();
     	    pstmt = conn.prepareStatement(
     		    "SELECT * FROM policy;");
     	    rs = pstmt.executeQuery();
     	    while (rs.next()) {
     	    	Policy policy = new Policy();
     	    	policy.setPolicyDescription( rs.getString("policyDescription"));
     	    	policy.setPolicyID(rs.getInt("policyId"));
     	    	policy.setPolicyLevel(rs.getInt("policyLevel"));
     	    	policy.setPolicyName(rs.getString("policyName"));
 
 	    		policys.add(policy);
     	    }
     	    return policys;
     	} catch (SQLException e) {
     		e.printStackTrace();
     	} finally {
     		if( rs != null) {rs.close();}
     		if( pstmt != null) {pstmt.close();}
     		if( conn != null) {conn.close();}
     	}
     	return policys;
     }
 }
