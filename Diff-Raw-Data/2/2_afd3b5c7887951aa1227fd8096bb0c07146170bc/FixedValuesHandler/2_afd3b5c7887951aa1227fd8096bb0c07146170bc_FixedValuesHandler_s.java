 package eionet.meta.savers;
 
 import java.sql.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.util.*;
 
 import eionet.meta.*;
 import eionet.util.Log4jLoggerImpl;
 import eionet.util.LogServiceIF;
 import eionet.util.sql.INParameters;
 import eionet.util.sql.SQL;
 
 import com.tee.util.*;
 
 public class FixedValuesHandler extends BaseHandler{
 	
 	private static final String DEFAULT_OWNER_TYPE = "elem";
 
     private String mode = null;
     private String ownerID = null;
 	private String ownerType = DEFAULT_OWNER_TYPE;
     
     private String lastInsertID = null;
 	private boolean versioning = true;    
     private HashSet prhbDatatypes = new HashSet();
     
     private boolean allowed = true;
 	private boolean allowanceChecked = false;
     
     public FixedValuesHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
         this(conn, new Parameters(req), ctx);
     }
 
     public FixedValuesHandler(Connection conn, Parameters req, ServletContext ctx){
     	
         this.conn = conn;
         this.req  = req;
         this.ctx  = ctx;
         
         mode = req.getParameter("mode");
         ownerID = req.getParameter("delem_id");
 		String _ownerType = req.getParameter("parent_type");
 		if (!Util.nullString(_ownerType))
 			ownerType = _ownerType;
 		
         if (ctx!=null){
 	        String _versioning = ctx.getInitParameter("versioning");
 	        if (_versioning!=null && _versioning.equalsIgnoreCase("false"))
 	            setVersioning(false);
     	}
             
         // set the prohibited datatypes of parent element
         prhbDatatypes.add("BOOLEAN");
     }
 
     public FixedValuesHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
         this(conn, req, ctx);
         this.mode = mode;
     }
     
     public void setVersioning(boolean f){
         this.versioning = f;
     }
     
     public String getOwnerID(){
     	return ownerID;
     }
     
     /*
      *  (non-Javadoc)
      * @see eionet.meta.savers.BaseHandler#execute_()
      */
     public void execute_() throws Exception {
     	
     	if (!allowanceChecked)
 			 checkAllowance();
     	if (!allowed)
     		return; 
 
         if (mode.equalsIgnoreCase("add"))
             insert();
         else if (mode.equalsIgnoreCase("edit"))
             update();
         else
             delete();
     }
 
     private void insert() throws Exception {
 
         String[] newValues = req.getParameterValues("new_value");
         for (int i=0; newValues!=null && i<newValues.length; i++)
             insertValue(newValues[i]);
     }
 
     /**
      * 
      * @param value
      * @throws Exception
      */
     private void insertValue(String value) throws Exception {
 
     	INParameters inParams = new INParameters();
     	LinkedHashMap map = new LinkedHashMap();
     	
         map.put("OWNER_ID", inParams.add(ownerID, Types.INTEGER));
 		map.put("OWNER_TYPE", inParams.add(ownerType));
 		map.put("VALUE", inParams.add(value));
 
         String isDefault = req.getParameter("is_default");
         if (isDefault!=null && isDefault.equalsIgnoreCase("true"))
             map.put("IS_DEFAULT", "'Y'");
         
 		String definition = req.getParameter("definition");
 		if (definition!=null)
 			map.put("DEFINITION", inParams.add(definition));
 		
 		String shortDesc = req.getParameter("short_desc");
 		if (shortDesc!=null)
 			map.put("SHORT_DESC", inParams.add(shortDesc));
 
 		PreparedStatement stmt = null;
 		try{
 			stmt = SQL.preparedStatement(SQL.insertStatement("FXV", map), inParams, conn);
 			stmt.executeUpdate();
 			setLastInsertID();
 		}
 		finally{
 			SQL.close(stmt);
 		}
     }
 
     /**
      * 
      * @throws SQLException
      */
 	private void update() throws SQLException {
 		
 		String fxvID = req.getParameter("fxv_id");
 		if (Util.nullString(fxvID))
 			return;
 
 		String isDefault = req.getParameter("is_default");
 		String definition = req.getParameter("definition");
 		String shortDesc = req.getParameter("short_desc");
 		
     	INParameters inParams = new INParameters();
     	LinkedHashMap map = new LinkedHashMap();
 		if (isDefault!=null)
 			map.put("IS_DEFAULT", isDefault.equals("true") ? "'Y'" : "'N'");
 		if (definition!=null)
 			map.put("DEFINITION", inParams.add(definition));
 		if (definition!=null)
 			map.put("SHORT_DESC", inParams.add(shortDesc));
 
 		if (map.size()==0)
 			return;
 
 		PreparedStatement stmt = null;
 		try{
 			StringBuffer buf = new StringBuffer(SQL.updateStatement("FXV", map));
 			buf.append(" where FXV_ID=").append(inParams.add(fxvID, Types.INTEGER));
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			stmt.executeUpdate();
 			lastInsertID = fxvID;
 		}
 		finally{
 			SQL.close(stmt);
 		}
 	}
 
 	/**
 	 * 
 	 * @throws Exception
 	 */
     private void delete() throws Exception {
 
         String[] fxvID = req.getParameterValues("del_id");
         if (fxvID == null || fxvID.length == 0) return;
 
         for (int i=0; i<fxvID.length; i++){
             deleteValue(fxvID[i]);
         }
     }
 
     /**
      * 
      * @param id
      * @throws SQLException
      */
     private void deleteValue(String id) throws SQLException {
     	
     	INParameters inParams = new INParameters();
         StringBuffer buf = new StringBuffer("delete from FXV where FXV_ID=").
         append(inParams.add(id, Types.INTEGER));
         
         PreparedStatement stmt = null;
         try{
         	stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
         	stmt.executeUpdate();
         }
         finally{
         	SQL.close(stmt);
         }
     }
 
     /**
      * 
      * @throws Exception
      */
     private void checkAllowance() throws Exception {
     	
 		allowanceChecked = true;
 		
 		// check if legal mode
 		if (mode==null || (!mode.equalsIgnoreCase("add") &&
 						   !mode.equalsIgnoreCase("edit") &&
 						   !mode.equalsIgnoreCase("delete")) &&
 						   !mode.equalsIgnoreCase("edit_positions"))
 			throw new Exception("FixedValuesHandler mode unspecified!");
 
 		// check if owner id specified
 		if (ownerID == null && !mode.equals("delete"))
 			throw new Exception("FixedValuesHandler delem_id unspecified!");
 
 		// legalize owner type 
 		if (ownerType.equals("CH1") || ownerType.equals("CH2"))
 			ownerType = "elem";
 
 		if (!ownerType.equals("elem") && !ownerType.equals("attr"))
 			throw new Exception("FixedValuesHandler: unknown parent type!");
     	
     	// for owners with type!="elem" fixed values always allowed
 		if (!ownerType.equals("elem"))
 			return;
 		
 		// get the element's datatype and check if fxvalues are allowed
 		DDSearchEngine eng = new DDSearchEngine(conn);
 		DataElement elm = eng.getDataElement(ownerID);
 		String dtype = elm==null ? "" :
 								   elm.getAttributeValueByShortName("Datatype");
 		dtype = dtype==null ? "" : dtype.toUpperCase();
		if (prhbDatatypes.contains(dtype.toUpperCase()))
 			allowed = false;
     }
     
     /**
      * 
      * @throws SQLException
      */
     private void setLastInsertID() throws SQLException {
         
         String qry = "SELECT LAST_INSERT_ID()";
 
         ResultSet rs = null;
         Statement stmt = null;
         try{
         	stmt = conn.createStatement();
         	rs = stmt.executeQuery(qry);
             rs.clearWarnings();
             if (rs.next())
                 lastInsertID = rs.getString(1);
         }
         finally{
         	SQL.close(rs);
         	SQL.close(stmt);
         }
     }
 
     /**
      * 
      * @return
      */
     public String getLastInsertID(){
         return lastInsertID;
     }
 
     /**
      * 
      * @return
      */
     public boolean isAllowed(){
     	return allowed;
     }
 }
