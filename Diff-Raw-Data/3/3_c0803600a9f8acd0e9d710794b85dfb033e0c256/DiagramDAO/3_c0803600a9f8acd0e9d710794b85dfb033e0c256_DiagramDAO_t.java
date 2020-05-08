 package repository;
 
 /**
  * @author Yidu Liang
  * @author yangchen
  */
 import domain.Diagram;
 import domain.DiagramType;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class DiagramDAO {
 
     /**
      * Add Diagram into DB (diagram name, created time, in edition, owner Id, file path)
      * 			
      * @param Diagram object
      * 			diagramName, inEdition, ownerId, ecoreFilePath
      * @return true if success; false if fail
      */
     public static boolean addDiagram(Diagram diagram) {
 	ResultSet rs;
 	try {
 		Connection conn = DbManager.getConnection();
 	    //String sql = "INSERT INTO diagram (diagramName , createdTime , inEdition , owner_Id , filePath) VALUES (?,NOW(),?,?,?);";
 		//add by Yidu Liang Mar 20 2013
 		
 		String sql = "insert into diagram (projectId, userId, diagramType, diagramName ,filePath, fileType, merged, notationFileName, notationFilePath, diFlieName, diFilePath)"+
 		"VALUES (?, ?, ?, ?, ? ,?, ?, ?, ?, ?,? )";
 		
 		
 	    PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 
 	    //pstmt.setString(1, diagram.getDiagramName());
 	    //pstmt.setBoolean(2, diagram.isInEdition());
 	    //pstmt.setInt(3, diagram.getOwnerId());
 	    //pstmt.setString(4, diagram.getEcoreFilePath());
 	    
 	    //System.out.println("diagram upload SQL test");
 	    //System.out.println(diagram.getProjectId());
 	    //System.out.println(diagram.getUserId());
 	    //System.out.println(diagram.getDiagramType());
 	    //System.out.println(diagram.getMerged());
 		
 		pstmt.setInt(1,diagram.getProjectId()); // this need to be implementing 
 		pstmt.setInt(2,diagram.getUserId());
		pstmt.setString(3,Integer.toString(1)); //Temporary hack to get code to work. Please replace when 
												//diagramType comes into effect @AniketHajirnis
 		pstmt.setString(4,diagram.getDiagramName());    // this need to be implementing 
 		pstmt.setString(5,diagram.getFilePath());    // this need to be implementing 
 		pstmt.setString(6,diagram.getFileType());    // this need to be implementing 
 		pstmt.setInt(7,diagram.getMerged());
 		pstmt.setString(8,diagram.getNotationFileName());   // this need to be implementing 
 		pstmt.setString(9,diagram.getNotationFilePath());   // this need to be implementing 
 		pstmt.setString(10,diagram.getDiFileName());   // this need to be implementing 
 		pstmt.setString(11,diagram.getDiFilepath());     // this need to be implementing 
 		
 	    pstmt.executeUpdate();
 
 	    //Get and set the auto-generated PK
 	    rs = pstmt.getGeneratedKeys();
 	    if (rs.next()) {
 		int newId = rs.getInt(1);
 		diagram.setDiagramId(newId);
 	    }
 
 	    rs.close();
 	    pstmt.close();
 	    conn.close();
 	    return true;
 	} catch (SQLException ex) {
 		System.out.println("Exception"+ex.getMessage());
 	    Logger.getLogger(DiagramDAO.class.getName()).log(Level.SEVERE, null, ex);
 	}
 	return false;
     }
 
     /**
      * Get Diagram ArrayList from DB
      * 
      * @param projectId
      * 			The ID of the project
      * @return Diagram ArrayList
      */
     public static ArrayList<Diagram> getDiagramList(int projectId) {
 	ArrayList<Diagram> searchResult = new ArrayList<>();
 	try {
 	    Connection conn = DbManager.getConnection();
 	    String sql = "SELECT * FROM diagram where projectId = ?;";
 	    PreparedStatement pstmt = conn.prepareStatement(sql);
 
 	    pstmt.setInt(1, projectId);
 
 	    ResultSet rs = pstmt.executeQuery();
 
 	    //Initiate a list to get all returned report objects and set attributes
 	    /*
 		while (rs.next()) {
 		Diagram diagram = new Diagram();
 		diagram.setDiagramId(rs.getInt("diagram_Id"));
 		diagram.setDiagramName(rs.getString("diagramName"));
 		diagram.setCreatedTime(rs.getString("createdTime"));
 		diagram.setInEdition(rs.getBoolean("inEdition"));
 		diagram.setOwnerId(rs.getInt("owner_Id"));
 		diagram.setEcoreFilePath(rs.getString("filePath"));
 		searchResult.add(diagram);
 	    }
 		*/
 		//add by Yidu Liang Mar22 2013  projectId, userId, diagramType, diagramName, filePath, fileType, notationFileName, notationFilePath, diFileName, diFilePath
 		while (rs.next()) {
 		Diagram diagram = new Diagram();
 		diagram.setDiagramId(rs.getInt("diagramId"));
 		diagram.setProjectId(rs.getInt("projectId"));
 		diagram.setUserId(rs.getInt("userId"));
 		//support for enum type
 		diagram.setDiagramType(DiagramType.fromString(rs.getString("diagramType")));
 		
 		diagram.setDiagramName(rs.getString("diagramName"));
 		diagram.setFilePath(rs.getString("filePath"));
 		diagram.setFileType(rs.getString("fileType"));
 		diagram.setNotationFileName(rs.getString("notationFileName"));
 		diagram.setNotationFilePath(rs.getString("notationFilePath"));
 		diagram.setDiFilepath(rs.getString("diFilePath"));
 		diagram.setCreatedTime(rs.getString("createTime"));
 		
 		searchResult.add(diagram);
 	    }
 
 	    rs.close();
 	    pstmt.close();
 	    conn.close();
 	    return searchResult;
 	} catch (SQLException ex) {
 	    Logger.getLogger(DiagramDAO.class.getName()).log(Level.SEVERE, null, ex);
 	}
 	return null;
     }
 
     /**
      * Get Diagram from DB
      * 
      * @param diagram_Id
      * 			The ID of the diagram
      * @return Diagram object
      */
     public static Diagram getDiagram(int diagram_Id) {
 	try {
 	    Connection conn = DbManager.getConnection();
 	    String sql = "SELECT * FROM diagram WHERE diagramId = ?;";
 	    PreparedStatement pstmt = conn.prepareStatement(sql);
 
 	    pstmt.setInt(1, diagram_Id);
 
 	    ResultSet rs = pstmt.executeQuery();
 
 	    if (!rs.next()) {
 		return null;
 	    }
 
 	    Diagram diagram = new Diagram();
 	    diagram.setDiagramId(rs.getInt("diagramId"));
 		diagram.setProjectId(rs.getInt("projectId"));
 		diagram.setUserId(rs.getInt("userId"));
 		//support for enum type
 		diagram.setDiagramType(DiagramType.fromString(rs.getString("diagramType")));
 		diagram.setDiagramName(rs.getString("diagramName"));
 		diagram.setFilePath(rs.getString("filePath"));
 		diagram.setFileType(rs.getString("fileType"));
 		diagram.setNotationFileName(rs.getString("notationFileName"));
 		diagram.setNotationFilePath(rs.getString("notationFilePath"));
 		diagram.setDiFileName(rs.getString("diFlieName")); // TODO diFlieName typo in DB
 		diagram.setDiFilepath(rs.getString("diFilePath"));
 
 	    pstmt.close();
 	    conn.close();
 	    return diagram;
 	} catch (SQLException ex) {
 	    Logger.getLogger(DiagramDAO.class.getName()).log(Level.SEVERE, null, ex);
 	}
 	return null;
     }
 
     /**
      * Update Diagram from DB
      * 
      * @param Diagram object
      * 			projectId, userId, diagramType, diagramName, filePath, fileType, notationFileName, notationFilePath, diFileName, diFilePath
      * @return true if success; false if fail
      */
     public static boolean updateDiagram(Diagram diagram) {
 	try {
 	    Connection conn = DbManager.getConnection();
 	    String sql = "UPDATE diagram SET projectId = ?, userId = ?, diagramType = ?, diagramName = ?, filePath = ?, fileType = ?, notationFileName = ?, notationFilePath= ?, diFileName = ?, diFilePath = ? WHERE diagramId = ?;";
 	    PreparedStatement pstmt = conn.prepareStatement(sql);
 
 	    pstmt.setInt(1,diagram.getProjectId()); // this need to be implementing 
 		pstmt.setInt(2,diagram.getUserId());
 		//converted to an enum type to avoid hard coding
 		pstmt.setString(3,diagram.getDiagramType().toString()); // this need to be implementing 
 		pstmt.setString(4,diagram.getDiagramName());    // this need to be implementing 
 		pstmt.setString(5,diagram.getFilePath());    // this need to be implementing 
 		pstmt.setString(6,diagram.getFileType());    // this need to be implementing 
 		pstmt.setString(7,diagram.getNotationFileName());   // this need to be implementing 
 		pstmt.setString(8,diagram.getNotationFilePath());   // this need to be implementing 
 		pstmt.setString(9,diagram.getDiagramName());   // this need to be implementing 
 		pstmt.setString(10,diagram.getDiFilepath());     // this need to be implementing 
 
 	    pstmt.executeUpdate();
 
 	    pstmt.close();
 	    conn.close();
 	    return true;
 	} catch (SQLException ex) {
 	    Logger.getLogger(DiagramDAO.class.getName()).log(Level.SEVERE, null, ex);
 	}
 	return false;
     }
 
     /**
      * Delete Diagram from DB
      * 
      * @param Diagram object
      * @return true if success; false if fail
      */    
     public static boolean deleteDiagram(Diagram diagram) {
 	try {
 	    Connection conn = DbManager.getConnection();
 	    String sql = "DELETE FROM diagram WHERE diagram_Id = ?;";
 	    PreparedStatement pstmt = conn.prepareStatement(sql);
 
 	    pstmt.setInt(1, diagram.getDiagramId());
 
 	    pstmt.executeUpdate();
 
 	    pstmt.close();
 	    conn.close();
 	    return true;
 	} catch (SQLException ex) {
 	    Logger.getLogger(DiagramDAO.class.getName()).log(Level.SEVERE, null, ex);
 	}
 	return false;
     }
 }
