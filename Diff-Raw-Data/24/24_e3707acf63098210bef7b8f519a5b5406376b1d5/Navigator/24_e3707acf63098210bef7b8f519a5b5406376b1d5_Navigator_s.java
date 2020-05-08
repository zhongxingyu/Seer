 package edu.wpi.cs.jburge.SEURAT.api;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import java.sql.ResultSet;
 
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 import edu.wpi.cs.jburge.SEURAT.rationaleData.Alternative;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.PatternElement;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleDB;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleDBUtil;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleElementType;
 import edu.wpi.cs.jburge.SEURAT.views.RationaleExplorer;
 
 /**
  * This API is used when other programs wishes to navigate to a particular element in SEURAT
  * @author yechen
  *
  */
 public class Navigator {
 
 	public static final int EXPLORER_RATIONALE = 1;
 	public static final int EXPLOERR_TACTIC = 3;
 	public static final int EXPLORER_PATTERN = 2;
 
 	public static void navigateTo(int explorerType, String elementName, RationaleElementType elementType){
 		//Show View
 		switch (explorerType){
 		case EXPLORER_RATIONALE:
 			try {
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("edu.wpi.cs.jburge.SEURAT.views.RationaleExplorer");
 				RationaleExplorer exp = RationaleExplorer.getHandle();
 				exp.showRationaleNode(elementName, elementType);
 			} catch (PartInitException e) {
 				e.printStackTrace();
 			}
 			break;
 
 			//TODO
 		}
 
 
 	}
 
 	public static boolean navigateTo(String xmiID){
 		PatternElement patternElement = new PatternElement();
 		patternElement.fromDatabase(xmiID);
 		if (patternElement.getID() <= 0) {
 			//Is this a package?
 			RationaleDB db = RationaleDB.getHandle();
 			Connection conn = db.getConnection();
 			
 			try{
 				Statement stmt = conn.createStatement();
 				String query = "SELECT * FROM DIAGRAM_ALTERNATIVE WHERE package_xmi_id = '"
 					+ RationaleDBUtil.escape(xmiID) + "'";
 				ResultSet rs = stmt.executeQuery(query);
 				if (rs.next()){
 					Alternative alt = new Alternative();
 					alt.fromDatabase(rs.getInt("alt_id"));
 					if (alt.getID() < 0) return false;
 					navigateTo(EXPLORER_RATIONALE, alt.getName(), RationaleElementType.ALTERNATIVE);
 					return true;
 				}
 			}catch(SQLException e){
 				e.printStackTrace();
 			}
 			return false;
 		}
 		Alternative alt = new Alternative();
 		alt.fromDatabase(patternElement.getAltID());
 		if (alt.getID() < 0) {
 			return false;
 		}
 		navigateTo(EXPLORER_RATIONALE, alt.getName(), RationaleElementType.ALTERNATIVE);
 		return true;
 	}
 }
