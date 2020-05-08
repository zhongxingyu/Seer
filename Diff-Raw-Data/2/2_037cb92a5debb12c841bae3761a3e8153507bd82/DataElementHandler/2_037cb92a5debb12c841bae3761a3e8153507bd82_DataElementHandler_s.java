 package eionet.meta.savers;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import com.tee.uit.security.AccessController;
 import com.tee.uit.security.SignOnException;
 import eionet.util.sql.SQLGenerator;
 import com.tee.util.Util;
 
 import eionet.meta.DDSearchEngine;
 import eionet.meta.DDUser;
 import eionet.meta.DElemAttribute;
 import eionet.meta.DataElement;
 import eionet.meta.FixedValue;
 import eionet.meta.VersionManager;
 import eionet.util.SecurityUtil;
 import eionet.util.sql.INParameters;
 import eionet.util.sql.SQL;
 
 /**
  * 
  * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
  *
  */
 public class DataElementHandler extends BaseHandler {
 	
 	/** */
 	public static LinkedHashMap valueDelimiters;
 	
 	/**
 	 * 
 	 */
 	static{
 		valueDelimiters = new LinkedHashMap();
 		valueDelimiters.put("", "None");
 		valueDelimiters.put(" ", "space");
 		valueDelimiters.put(",", ",");
 		valueDelimiters.put(";", ";");
 	}
 
     public static String ATTR_PREFIX = "attr_";
     public static String ATTR_MULT_PREFIX = "attr_mult_";
 
     public static String INHERIT_ATTR_PREFIX = "inherit_";
     public static String INHERIT_COMPLEX_ATTR_PREFIX = "inherit_complex_";
 
     public static String POS_PREFIX = "pos_";
     public static String OLDPOS_PREFIX = "oldpos_";
     public static String DELIM_PREFIX = "delim_";
     public static String OLDDELIM_PREFIX = "olddelim_";
     public static String MANDATORYFLAG_PREFIX = "mndtry_";
     public static String OLDMANDATORYFLAG_PREFIX = "oldmndtry_";
 
     private String mode = null;
     private String elmValuesType = null;
     private String delem_id = null;
     private String[] delem_ids = null;
     private String elmShortName = null;
 	private String elmIdfier = null;
     private String lastInsertID = null;
     
     private String tblNamespaceID = null;
     private String dstNamespaceID = null;
     
     private String tableID = null;
     
     private String schemaPhysPath = null;
     private String schemaUrlPath = null;
     
     private HashSet ch1ProhibitedAttrs = new HashSet();
     
     private String mDatatypeID = null; 
     private String datatypeValue = null;
     
     private DDSearchEngine searchEngine = null;
     
     private boolean checkInResult = false;
     
     boolean versioning = true;    
     boolean superUser = false;
 	private String date = null;
 	private boolean isImportMode = false;
 	private boolean useForce = false;
     
     /** indicates if top namespace needs to be released after an exception*/
     private boolean doCleanup = false;
     
     /**
     for deletion - a HashSet for remembering namespace ids and short_names
     of all working copies, so later we can find originals and deal with them*/
     HashSet originals = new HashSet();
         
     /** for deletion - remember the top namespaces */
     HashSet topns = new HashSet();
     
     /** for storing table ID returned by VersionManager.deleteElm() */
     private String newTblID = null;
 
 	/** for storing restored elm ID returned by Restorer.restoreElm() */
 	private String restoredID = null;
 
 	/** for storing the ID of the next-in-line version of the deleted common element */
 	private String latestCommonElmID = null;
 
 	/** */
 	private String checkedInCopyID = null;
 
 	/** */
 	private HashMap attrShortNamesById = new HashMap();
 	private HashMap attrIdsByShortName = new HashMap();
 	
     /**
     *
     */
     public DataElementHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
         this(conn, new Parameters(req), ctx);
     }
 
     /**
      * 
      * @param conn
      * @param req
      * @param ctx
      */
     public DataElementHandler(Connection conn, Parameters req, ServletContext ctx){
     	
         this.conn = conn;
         this.req = req;
         this.ctx = ctx;
         this.mode = req.getParameter("mode");
         this.elmValuesType = req.getParameter("type");
         this.delem_id = req.getParameter("delem_id");
         this.delem_ids = req.getParameterValues("delem_id");
         this.elmShortName = req.getParameter("delem_name");
 		this.elmIdfier = req.getParameter("idfier");
         this.tableID = req.getParameter("table_id");
         this.dstNamespaceID = req.getParameter("dst_namespace_id");
         this.tblNamespaceID = req.getParameter("tbl_namespace_id");        
         // some code might still supply parent namespace id with "ns"
         if (this.tblNamespaceID==null || this.tblNamespaceID.length()==0)
         	this.tblNamespaceID = req.getParameter("ns");
         
         
         if (ctx!=null){
 	        String _versioning = ctx.getInitParameter("versioning");
 	        if (_versioning!=null && _versioning.equalsIgnoreCase("false"))
 	            setVersioning(false);
         }
         
         // loop over all possible attributes, set certain business rules
         try{
             searchEngine = new DDSearchEngine(conn, "", ctx);
             Vector v = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE);
             for (int i=0; v!=null && i<v.size(); i++){
             	
                 DElemAttribute attr = (DElemAttribute)v.get(i);
                 if (attr.getShortName().equalsIgnoreCase("MinSize")){
                     ch1ProhibitedAttrs.add(attr.getID());
                 }
                 if (attr.getShortName().equalsIgnoreCase("MaxSize")){
                     ch1ProhibitedAttrs.add(attr.getID());
                 }
                 if (attr.getShortName().equalsIgnoreCase("Datatype")){
                     this.mDatatypeID = attr.getID();
                 }
                 
                 this.attrShortNamesById.put(new Integer(attr.getID()), attr.getShortName());
                 this.attrIdsByShortName.put(attr.getShortName(), attr.getID());
             }
         }
         catch (Exception e){}
     }
     
     public DataElementHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
         this(conn, req, ctx);
         this.mode = mode;
     }
     
     public void setUser(DDUser user){
         this.user = user;
     }
     
     public void setVersioning(boolean f){
         this.versioning = f;
     }
     
     public boolean getVersioning(){
         return this.versioning;
     }
     
     public void setSuperUser(boolean su){
         this.superUser = su;
     }
     
     /**
      * 
      * @return
      */
     public String getNewTblID(){
     	return this.newTblID;
     }
     
     /**
     *
     */
     public void cleanup() throws Exception{
         
         if (!doCleanup) return;
         
         INParameters inParams = new INParameters();
         
         PreparedStatement stmt = null;
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("DATAELEM");
         
         // release originals
 		for (Iterator i=originals.iterator(); i.hasNext(); ){
 			
 			String q = "update DATAELEM set WORKING_USER=NULL where IDENTIFIER="; 
 
 			String origID = (String)i.next();
 			int commaPos = origID.indexOf(',');
 			if (commaPos<0){
 				q += inParams.add(origID, Types.VARCHAR);
 			}
 			else {
 				q += inParams.add(origID.substring(0,commaPos), Types.VARCHAR);
 				q += " and PARENT_NS=" + inParams.add(origID.substring(commaPos+1), Types.INTEGER);
 			}
 			stmt = SQL.preparedStatement(q, inParams, conn);
 			stmt.executeQuery();
 		}
 		
         // release the top namespaces
         if (req.getParameter("common")==null){
 	        gen.clear();
 	        gen.setTable("NAMESPACE");
 	        gen.setFieldExpr("WORKING_USER", "NULL");
 	        for (Iterator i=topns.iterator(); i.hasNext(); ){            
 	            stmt.executeUpdate(gen.updateStatement() +
 	                    " where NAMESPACE_ID=" + (String)i.next());
 	        }
         }
         
         stmt.close();
     }
     
     public void execute_() throws Exception {
         
         // initialize this.topNsReleaseNeeded (just in case)
         doCleanup = false;
         
         if (mode==null || (!mode.equalsIgnoreCase("add") &&
                           !mode.equalsIgnoreCase("edit") &&
                           !mode.equalsIgnoreCase("delete") &&
                           !mode.equalsIgnoreCase("copy") &&
                           !mode.equalsIgnoreCase("edit_tblelems")))
             throw new Exception("DataElementHandler mode unspecified!");
 
         if (mode.equalsIgnoreCase("add")){
             if (elmValuesType==null || (!elmValuesType.equalsIgnoreCase("CH1") &&
                             !elmValuesType.equalsIgnoreCase("CH2")))
                 throw new Exception("Element type not specified!");
         }
 
         if (mode.equalsIgnoreCase("add") || mode.equalsIgnoreCase("copy")){
             insert();
             delem_id = getLastInsertID();
         }
         else if (mode.equalsIgnoreCase("edit"))
             update();
         else if (mode.equalsIgnoreCase("edit_tblelems"))
             processTableElems();
         else{
             delete();
             cleanVisuals();
         }
     }
     
     /**
      * 
      * @throws Exception
      */
     private void insert() throws Exception {
     	
     	// init the flag indicating if this is a common element
     	boolean elmCommon = req.getParameter("common")!=null; 
 
         // check missing parameters
 		if (elmIdfier == null){
 			throw new SQLException("Missing request parameter: idfier");
 		}
 		if (!elmCommon && (this.tableID==null || this.tableID.length()==0)){
 			throw new Exception("Missing request parameter: table_id");
 		}
 		
 		// if non-common element, make sure such does not already exist within this table
 		if (!elmCommon && existsInTable()){
 			throw new SQLException("The table already has an element with this Identifier");
 		}
 		
 		// if common element, make sure such does not already exist
 		if (elmCommon && existsCommon()){
 			throw new SQLException("A common element with this Identifier already exists");
 		}
 
         // if making a copy, do the copy, create acl, and return
 		String copyElemID = req.getParameter("copy_elem_id");
 		if (copyElemID != null && copyElemID.length()!=0){
 			if (elmCommon){
 				copyIntoCommon(copyElemID);
 			} else {
 				copyIntoNonCommon(copyElemID);
 			}
 			
 			if (elmCommon && user!=null){
 				createAclForCommonElm();
 			}
 			return;
 		}
 
 		// prepare SQL generator for element insert
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("DATAELEM");
         gen.setField("IDENTIFIER", elmIdfier);
         if (elmShortName!=null){
         	gen.setField("SHORT_NAME", elmShortName);
         }
         if (elmValuesType!=null){
         	gen.setField("TYPE", elmValuesType);
         }
         
         if (!elmCommon){
         	gen.setFieldExpr("PARENT_NS", getTblNamespaceID());
         	gen.setFieldExpr("TOP_NS", getDstNamespaceID());
         }
         
 		// set the element's registration status (relevant for common elements only)
 		if (elmCommon){
 			String regStatus = req.getParameter("reg_status");
 			if (regStatus!=null && regStatus.length()>0){
 				gen.setField("REG_STATUS", regStatus);
 			}
 		}
         
         // set gis type
 		String gisType = req.getParameter("gis");
 		if (gisType!=null && gisType.length()==0 && isImportMode){
 			gisType = null;
 		}
 		if (gisType!=null && !gisType.equals("nogis")){
 			gen.setField("GIS", gisType);
 		}
 
 		// set IS_ROD_PARAM
 		String isRodParam = req.getParameter("is_rod_param");
 		if (isRodParam!=null){
 			if (!isRodParam.equals("true") && !isRodParam.equals("false")){
 				throw new Exception("Invalid value for is_rod_param!");
 			}
 			gen.setField("IS_ROD_PARAM", isRodParam);
 		}
 
         // if not in import mode, treat new common elements as working copies until checked in
 		if (elmCommon && !isImportMode){
             gen.setField("WORKING_COPY", "Y");
             if (user!=null && user.isAuthentic()){
                 gen.setField("WORKING_USER", user.getUserName());
             }
         }
 		if (user!=null){
 			gen.setField("USER", user.getUserName());
 		}
 		if (date==null){
 			date = String.valueOf(System.currentTimeMillis());
 		}
 		gen.setFieldExpr("DATE", date);
 
 		// execute element insert SQL
 		Statement stmt = conn.createStatement();
 		stmt.executeUpdate(gen.insertStatement());
 		setLastInsertID();
 		
 		// if non-common element, create row in TBL2ELEM
 		if (!elmCommon){
 			if (tableID==null || tableID.length()==0){
 				throw new Exception("Missing tableID");
 			}
 			StringBuffer sqlBuf = new StringBuffer("insert into TBL2ELEM (TABLE_ID, DATAELEM_ID, POSITION) select ");
 			sqlBuf.append(tableID).append(", ").append(getLastInsertID());
 			sqlBuf.append(", ifnull(max(POSITION)+1,1) from TBL2ELEM where TABLE_ID=").append(tableID);
 			stmt.executeUpdate(sqlBuf.toString());
 		}
 
 		// process the element's attributes
 		processAttributes();
 		
 		// if this is a boolean element, auto-create the fixed values ("true" and "false")
 		if (elmValuesType!=null && elmValuesType.equals("CH1") && mDatatypeID!=null && datatypeValue.equals("boolean")){
 			autoCreateBooleanFixedValues(stmt, getLastInsertID());
 		}
 		stmt.close();
 		
 		// if common element, create corresponding acl
 		if (elmCommon && user!=null){
 			createAclForCommonElm();
 		}
     }
 
     /**
      * @throws SignOnException 
      *
      */
     private void createAclForCommonElm() throws SignOnException {
 		String aclPath = "/elements/" + elmIdfier;
 		HashMap acls = AccessController.getAcls();
 		if (!acls.containsKey(aclPath)){
 			String aclDesc = "Identifier: " + elmIdfier;
 			try{
 				AccessController.addAcl(aclPath, user.getUserName(), aclDesc);
 			}
 			catch (Exception e){
 				e.printStackTrace();
 			}
 		}
     }
 
     /**
      * @throws SQLException 
      * 
      *
      */
     public static void autoCreateBooleanFixedValues(Statement stmt, String elmID) throws SQLException {
     	
     	String[] values = new String[2];
     	values[0] = "true";
     	values[1] = "false";
     	for (int i=0; i<values.length; i++){
     		SQLGenerator gen = new SQLGenerator();
 	    	gen.setTable("FXV");
 	    	gen.setFieldExpr("OWNER_ID", elmID);
 	    	gen.setField("OWNER_TYPE", "elem");
 	    	gen.setField("VALUE", values[i]);
 	    	gen.setField("DEFINITION", "Value auto-created by DD");
 	    	gen.setField("SHORT_DESC", "Auto-created by DD");
 	    	stmt.executeUpdate(gen.insertStatement());
     	}
 	}
 
 	/**
      * 
      * @throws Exception
      */
     private void update() throws Exception {
     	
 		// init the flag indicating if this is a common element
 		boolean elmCommon = req.getParameter("common")!=null;
 		
 		lastInsertID = delem_id;
 		String checkIn = req.getParameter("check_in");
 		String switchType = req.getParameter("switch_type");
 		
 		// if check-in, do the check-in and exit
 		if (checkIn!=null && checkIn.equalsIgnoreCase("true")){
         	
 			VersionManager verMan = new VersionManager(conn, user);
 			verMan.setContext(ctx);
 			verMan.setServlRequestParams(req);
 			
 			String updVer = req.getParameter("upd_version");
 			if (updVer!=null && updVer.equalsIgnoreCase("true")){
 				verMan.setVersionUpdate(true);
 				setCheckedInCopyID(delem_id);
 			}
 			else
 				setCheckedInCopyID(req.getParameter("checkedout_copy_id"));
 			
 			verMan.checkIn(delem_id, "elm", req.getParameter("reg_status"));
 			return;
 		}
 		if (switchType!=null && switchType.equalsIgnoreCase("true")){
 			
 			String newType = elmValuesType.equals("CH1") ? "CH2" : "CH1";
 			
 			SQLGenerator gen = new SQLGenerator();
 			gen.setTable("DATAELEM");
 			gen.setField("TYPE", newType);
 			conn.createStatement().executeUpdate(gen.updateStatement() + " where DATAELEM_ID=" + delem_id);
 			
 			if (newType.equals("CH1") && !ch1ProhibitedAttrs.isEmpty()){
 				
 				StringBuffer buf = new StringBuffer("delete from ATTRIBUTE where PARENT_TYPE='E' and DATAELEM_ID=");
 				buf.append(delem_id).append(" and M_ATTRIBUTE_ID in (");
 				int i=0;
 				for (Iterator iter = ch1ProhibitedAttrs.iterator(); iter.hasNext();i++){
 					if (i>0)
 						buf.append(",");
 					buf.append(iter.next());
 				}
 				buf.append(")");
 				conn.createStatement().executeUpdate(buf.toString());
 			}
 			
 			return;
 		}
 		
 		// prepare SQL generator for element update
 		SQLGenerator gen = new SQLGenerator();
 		gen.setTable("DATAELEM");
 		if (!Util.nullString(elmShortName)){
 			gen.setField("SHORT_NAME", elmShortName);
 		}
 
 		// if common element, set regisration status
 		if (elmCommon){
 			String elmRegStatus = req.getParameter("reg_status");
 			if (!Util.nullString(elmRegStatus))
 				gen.setField("REG_STATUS", elmRegStatus);
 		}
 		
 		// set IS_ROD_PARAM
 		String isRodParam = req.getParameter("is_rod_param");
 		if (isRodParam!=null){
 			if (!isRodParam.equals("true") && !isRodParam.equals("false")){
 				throw new Exception("Invalid value for is_rod_param!");
 			}
 			gen.setField("IS_ROD_PARAM", isRodParam);
 		}
 		
 		// set the gis type (relevant for common elements only)
 		String gisType = req.getParameter("gis");
 		if (gisType==null || gisType.equals("nogis")){
 			gen.setFieldExpr("GIS", "NULL");
 		} else {
 			gen.setField("GIS", gisType);
 		}
         
 		// execute element update SQL if at least one field was set
 		if (!Util.nullString(gen.getValues())){
 			conn.createStatement().executeUpdate(gen.updateStatement() + " where DATAELEM_ID=" + delem_id);
 		}
         // handle element's attributes
         deleteAttributes();
         processAttributes();
         
         // handle the element's fixed/suggested values
         String rmvValues = req.getParameter("remove_values"); 
         if (rmvValues!=null && rmvValues.equals("true")){
         	conn.createStatement().executeUpdate("delete from FXV where OWNER_TYPE='elem' and OWNER_ID=" + delem_id);
         }
         
         // handle datatype conversion
         handleDatatypeConversion(req.getParameter("datatype_conversion"));
     }
 
     /**
      * 
      * @param conversion
      * @throws SQLException 
      */
     private void handleDatatypeConversion(String conversion) throws Exception {
     	
     	if (eionet.util.Util.voidStr(conversion)){
     		return;
     	}
     	
     	// conversion is a string with pattern "oldtype-newtype", so check that '-' is indeed present
     	int i = conversion.indexOf('-');
     	if (i<=0){
     		throw new Exception("Invalid parameter value: " + conversion);
     	}
     	
     	// extract old and new datatype
     	String oldDatatype = conversion.substring(0,i);
     	String newDatatype = conversion.substring(i+1);
     	
     	// setup search engine object
     	if (searchEngine==null){
     		searchEngine = new DDSearchEngine(conn, "", ctx);
     	}
     	
     	// find ids of attributes whose values must be deleted according to the new datatype's rules
     	HashSet deleteAttrs = new HashSet();
     	Vector v = searchEngine.getDElemAttributes(DElemAttribute.TYPE_SIMPLE);
     	for (i=0; v!=null && i<v.size(); i++){
     		DElemAttribute attr = (DElemAttribute)v.get(i);
     		if (eionet.util.Util.skipAttributeByDatatype(attr.getShortName(), newDatatype)){
     			deleteAttrs.add(attr.getID());
     		}
     	}
     	
     	// delete the values of teh above-found attributes
     	Statement stmt = null;
     	if (deleteAttrs.size()>0){
     		StringBuffer buf = new StringBuffer("delete from ATTRIBUTE where PARENT_TYPE='E' and DATAELEM_ID=");
     		buf.append(delem_id).append(" and (");
     		i=0;
 	    	for (Iterator iter=deleteAttrs.iterator(); iter.hasNext(); i++){
 	    		if (i>0){
 	    			buf.append(" or ");
 	    		}
 	    		buf.append("M_ATTRIBUTE_ID=").append(iter.next());
 	    	}
 	    	buf.append(")");
 
 	    	stmt = conn.createStatement();
 	    	stmt.executeUpdate(buf.toString());
     	}
 
     	// if a fixed values element is converted into boolean datatype, auto-create "true" and "false" values
     	// (it is assumed here that the old non-boolean values have already been removed)
 		if (elmValuesType!=null && elmValuesType.equals("CH1") && newDatatype.equals("boolean")){
 			if (stmt==null){
 				stmt = conn.createStatement();
 			}
 			autoCreateBooleanFixedValues(stmt, delem_id);
 		}
 
 		if (stmt!=null)
 			stmt.close();
 	}
 
 	/**
      * 
      * @throws Exception
      */
     private void delete() throws Exception {
     	
     	Statement stmt = null;
 		try{
 			// create SQL statement object and start transaction
 			stmt = conn.createStatement();
 	    	
 	    	// do the delete
 	    	delete(stmt);
 		}
 		finally{
 			try{
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
     }
 
 	/**
 	 * 
 	 * @throws Exception
 	 */
     private void delete(Statement stmt) throws Exception{
         
     	// this method handles deletion of elements and deletion of just tbl2elm relations
     	// of common elements in a given table
     	
     	// if deletion of tbl2elm relations of common elements in a given table is requested,
     	// do them first
     	String[] linkelms = req.getParameterValues("linkelm_id");
         if (linkelms!=null && linkelms.length!=0){
         	if (tableID==null || tableID.length()==0){
         		throw new Exception("Missing request parameter: table_id");
         	}
         	
         	INParameters inParams = new INParameters();
         	String q = "delete from TBL2ELEM where TABLE_ID="
         		+ inParams.add(tableID, Types.INTEGER)+" and (";
         	
     		for (int i=0; i<linkelms.length; i++){
     			if (i>0){ 
     				q += " or ";
     			}
     			q += "DATAELEM_ID=" + inParams.add(linkelms[i], Types.INTEGER);
     		}
     		q += ")";
     		
     		PreparedStatement preparedStatement = SQL.preparedStatement(q, inParams, conn);
    		preparedStatement.executeQuery();
         }
         
         // if no deletion of elements requested, return
         if (delem_ids==null || delem_ids.length==0)
             return;
         
         // go through the given elements in database, make sure they can be deleted
         // and gather information we need when starting the deletion
         HashSet identifiers = new HashSet();
         HashSet unlockCheckedoutCopies = new HashSet();
         
         INParameters inParams = new INParameters();
         String q = "select * from DATAELEM where ";
         
         //StringBuffer buf = new StringBuffer("select * from DATAELEM where ");
         for (int i=0; i<delem_ids.length; i++){
             if (i>0){
             	q += " or ";
             }
             q += "DATAELEM_ID=";
             q += inParams.add(delem_ids[i], Types.INTEGER);
         }
         
         PreparedStatement preparedStatement = SQL.preparedStatement(q, inParams, conn);
         ResultSet rs = preparedStatement.executeQuery();
         
         while (rs.next()){
         	// skip non-common elements, as this loop is relevant for common elements only
         	String parentNS = rs.getString("PARENT_NS");
         	if (parentNS!=null)
         		continue;
         	
         	String identifier = rs.getString("IDENTIFIER");
         	String workingCopy = rs.getString("WORKING_COPY");
         	String workingUser = rs.getString("WORKING_USER");
         	String regStatus = rs.getString("REG_STATUS");
         	if (workingCopy==null || regStatus==null || identifier==null)
         		throw new NullPointerException();
         	
         	identifiers.add(identifier);
         	if (workingCopy.equals("Y")){
         		if (workingUser==null && useForce==false)
         			throw new Exception("Working copy without a working user");
         		else if (!workingUser.equals(user.getUserName()) && useForce==false)
         			throw new Exception("Cannot delete working copy of another user");
         		else{
         			try{
         				String checkedOutCopyID = rs.getString("CHECKEDOUT_COPY_ID");
         				if (checkedOutCopyID!=null && checkedOutCopyID.length()>0)
         					unlockCheckedoutCopies.add(rs.getString("CHECKEDOUT_COPY_ID"));
         			}
         			catch (NullPointerException npe){}
         		}
         	}
         	else if (workingUser!=null && useForce==false)
         		throw new Exception("Element checked out by another user: " + workingUser);
         	else if (useForce==false){
         		boolean canDelete = false;
         		if (regStatus.equals("Released") || regStatus.equals("Recorded"))
         			canDelete = SecurityUtil.hasPerm(user.getUserName(), "/elements/" + identifier, "er");
 				else
 					canDelete = SecurityUtil.hasPerm(user.getUserName(), "/elements/" + identifier, "u") ||
 								SecurityUtil.hasPerm(user.getUserName(), "/elements/" + identifier, "er");
         		if (!canDelete)
         			throw new Exception("You have no permission to delete this element: " +
         					rs.getString("DATAELEM_ID"));
         	}
         }
         
 		// delete dependencies
 		deleteAttributes();
 		deleteComplexAttributes();
 		deleteFixedValues();
 		deleteFkRelations();
 		deleteTableElem();
 		
 		// delete the elements themselves
 		
 		
 		inParams = new INParameters();
 		q = "delete from DATAELEM where ";
         for (int i=0; i<delem_ids.length; i++){
             if (i>0){
                 q += " or ";
             }
             q += "DATAELEM_ID=";
             q += inParams.add(delem_ids[i], Types.INTEGER);
         }
 	
         preparedStatement = SQL.preparedStatement(q, inParams, conn);
         preparedStatement.executeUpdate();
         
         // remove acls of common elements whose identifiers are not present any more
         removeAcls(stmt, identifiers);
         
         // unlock checked out copies whose working copies were deleted
         if (unlockCheckedoutCopies.size()>0){
 	        int i=0;
 	        StringBuffer buf = new StringBuffer("update DATAELEM set WORKING_USER=NULL where ");
 	        for (Iterator iter=unlockCheckedoutCopies.iterator(); iter.hasNext(); i++){
 	        	if (i>0) buf.append(" or ");
 	        	buf.append("DATAELEM_ID=").append(iter.next());
 	        }
 	        stmt.executeUpdate(buf.toString());
     	}
     }
 
     /**
      * 
      * @throws SQLException
      */
     private void deleteAttributes() throws SQLException {
     	
     	if (delem_ids==null || delem_ids.length==0){
     		return;
     	}
     	
     	StringBuffer buf = new StringBuffer().
     	append("delete from ATTRIBUTE where PARENT_TYPE='E' and DATAELEM_ID in (").
         append(eionet.util.Util.toCSV(delem_ids)).append(")");
         
     	// Skip the deletion of image-attributes if not in complete-delete mode.
     	// That's because image-attributes are handled by image upload servlet instead.
     	// Complete-delete mode is used for example by version manager, and means
     	// that all must be deleted with no exceptions.
         String completeDelete = req.getParameter("complete");
         if (completeDelete==null || !completeDelete.equals("true")){
         	buf.append(" and M_ATTRIBUTE_ID not in ").
         	append("(select M_ATTRIBUTE_ID from M_ATTRIBUTE where DISP_TYPE='image')");
         }
 
         logger.debug(buf.toString());
 
         Statement stmt = null;
         try{
         	stmt = conn.createStatement();
         	stmt.executeUpdate(buf.toString());
         }
         finally{
         	SQL.close(stmt);
         }
     }
     
     private void deleteComplexAttributes() throws SQLException {
 
         for (int i=0; delem_ids!=null && i<delem_ids.length; i++){
             
             Parameters params = new Parameters();
             params.addParameterValue("mode", "delete");
             params.addParameterValue("legal_delete", "true");
             params.addParameterValue("parent_id", delem_ids[i]);
             params.addParameterValue("parent_type", "E");
             
             AttrFieldsHandler attrFieldsHandler =
                                 new AttrFieldsHandler(conn, params, ctx);
             attrFieldsHandler.setVersioning(versioning);
             try{
                 attrFieldsHandler.execute();
             }
             catch (Exception e){
                 throw new SQLException(e.toString());
             }
         }
     }
     
     private void deleteRelations() throws SQLException {
         
     	INParameters inParams = new INParameters();
     	StringBuffer buf = new StringBuffer("delete from RELATION where ");
     	
         for (int i=0; i<delem_ids.length; i++){
             if (i>0) {
             	buf.append(" or ");
             }
             buf.append("PARENT_ID=");
             buf.append(inParams.add(delem_ids[i], Types.INTEGER));
         }
 
         
         PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
         
         logger.debug(buf.toString());
         
         stmt.executeUpdate();
         stmt.close();
     }
 
     private void deleteFixedValues() throws Exception {
         
     	INParameters inParams = new INParameters();
     	String q = "select distinct FXV_ID from FXV where ";
     	q += "OWNER_TYPE='elem' and (";
     	
         for (int i=0; i<delem_ids.length; i++){
             if (i>0){
                 q += " or ";
             }
             q += "OWNER_ID=";
             q += inParams.add(delem_ids[i], Types.INTEGER);
         }
         q += ")";
         
         PreparedStatement stmt = SQL.preparedStatement(q, inParams, conn);
         ResultSet rs = stmt.executeQuery();
 		
         Parameters pars = new Parameters();
         while (rs.next()){
             pars.addParameterValue("del_id", rs.getString("FXV_ID"));
         }
         stmt.close();
         
         pars.addParameterValue("mode", "delete");
         pars.addParameterValue("legal_delete", "true");
         FixedValuesHandler fvHandler = new FixedValuesHandler(conn, pars, ctx);
         fvHandler.setVersioning(versioning);
         fvHandler.execute();
     }
     
     private void deleteFkRelations() throws Exception{
     	
     	INParameters inParams = new INParameters();
     	String q = "delete from FK_RELATION where ";
     	
 		for (int i=0; i<delem_ids.length; i++){
 			if (i>0){
 				q += " or ";
 			}
 			q += "A_ID=";
 			q += inParams.add(delem_ids[i], Types.INTEGER);
 			q += " or B_ID=";
 			q += inParams.add(delem_ids[i], Types.INTEGER);
 		}
 		
         PreparedStatement stmt = SQL.preparedStatement(q, inParams, conn);
         stmt.executeUpdate();
 		stmt.close();
     }
     
     private String getTableElemPos() throws SQLException{
 
     	INParameters inParams = new INParameters();
     	
         StringBuffer buf = new StringBuffer().
 		append("select max(POSITION) from TBL2ELEM where TABLE_ID=").
         append(inParams.add(tableID, Types.INTEGER));
 
         logger.debug(buf.toString());
 
         PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
         ResultSet rs = stmt.executeQuery();
         rs.clearWarnings();
 
         String pos=null;
         if (rs.next()){
             pos = rs.getString(1);
         }
         stmt.close();
         if (pos != null){
             try {
               int i = Integer.parseInt(pos) + 1;
               return Integer.toString(i);
             }
             catch(Exception e){
                 return "1";
             }
         }
 
         return "1";
     }
     private void deleteTableElem() throws SQLException {
 
         if (delem_ids==null || delem_ids.length==0){
             return;
         }
 
         INParameters inParams = new INParameters();
         StringBuffer buf = new StringBuffer("delete from TBL2ELEM where ");
         
         for (int i=0; i<delem_ids.length; i++){
             if (i>0){
                 buf.append(" or ");
             }
             buf.append("DATAELEM_ID=");
             buf.append(inParams.add(delem_ids[i], Types.INTEGER));
         }
 
         logger.debug(buf.toString());
 
         PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
         stmt.executeUpdate();
         
         stmt.close();
     }
     
     /**
      * 
      * @throws Exception
      */
     private void processTableElems() throws Exception {
     	
     	processElmPositions();
     	processValueDelimiters();
     	processMandatoryFlags();
     }
 
     /**
      * @throws SQLException 
      * 
      */
     private void processElmPositions() throws SQLException{
     	
     	HashMap positions = new HashMap();
     	Enumeration parNames = req.getParameterNames();
     	while (parNames.hasMoreElements()){
     		
     		String parName = (String)parNames.nextElement();
     		
     		if (parName.startsWith(OLDPOS_PREFIX)){
     			
     			Integer id = Integer.valueOf(parName.substring(OLDPOS_PREFIX.length()));
     			Integer oldPos = Integer.valueOf(req.getParameter(parName));
     			Integer newPos = Integer.valueOf(req.getParameter(POS_PREFIX + id));
     			
     			if (!oldPos.equals(newPos)){
     				positions.put(id, newPos);
     			}
     		}
     	}
     	
     	if (!positions.isEmpty()){
     		
     		PreparedStatement stmt = null;
     		try{    		
     			stmt = conn.prepareStatement("update TBL2ELEM set POSITION=? where DATAELEM_ID=? and TABLE_ID=?");
     			for (Iterator iter = positions.entrySet().iterator(); iter.hasNext();){
     				
     				Map.Entry entry = (Map.Entry)iter.next();
     				stmt.setInt(1, ((Integer)entry.getValue()).intValue());
     				stmt.setInt(2, ((Integer)entry.getKey()).intValue());
     				stmt.setInt(3, Integer.parseInt(tableID));
     				stmt.addBatch();
     			}
     			stmt.executeBatch();
     		}
     		finally{
     			SQL.close(stmt);
     		}
     	}
     }
     
     /**
      * @throws SQLException 
      * 
      */
     private void processValueDelimiters() throws SQLException{
     	
     	HashMap valueDelims = new HashMap();
     	Enumeration parNames = req.getParameterNames();
     	while (parNames.hasMoreElements()){
     		
     		String parName = (String)parNames.nextElement();
     		
     		if (parName.startsWith(OLDDELIM_PREFIX)){
     			
     			Integer id = Integer.valueOf(parName.substring(OLDDELIM_PREFIX.length()));
     			String oldDelim = req.getParameter(parName);
     			String newDelim = req.getParameter(DELIM_PREFIX + id);
     			if (newDelim.length()==0){
     				newDelim = null;
     			}
     			
     			if (!oldDelim.equals(newDelim)){
     				valueDelims.put(id, newDelim);
     			}
     		}
     	}
     	
     	if (!valueDelims.isEmpty()){
     		
     		PreparedStatement stmt = null;
     		try{    		
     			stmt = conn.prepareStatement(
     				"update TBL2ELEM set MULTIVAL_DELIM=? where DATAELEM_ID=? and TABLE_ID=?");
     			for (Iterator iter = valueDelims.entrySet().iterator(); iter.hasNext();){
     				
     				Map.Entry entry = (Map.Entry)iter.next();
     				stmt.setString(1, (String)entry.getValue());
     				stmt.setInt(2, ((Integer)entry.getKey()).intValue());
     				stmt.setInt(3, Integer.parseInt(tableID));
     				stmt.addBatch();
     			}
     			stmt.executeBatch();
     		}
     		finally{
     			SQL.close(stmt);
     		}
     	}
     }
     
     /**
      * 
      * @throws SQLException
      */
     private void processMandatoryFlags() throws SQLException{
     	
     	HashMap flags = new HashMap();
     	Enumeration parNames = req.getParameterNames();
     	while (parNames.hasMoreElements()){
     		
     		String parName = (String)parNames.nextElement();
     		
     		if (parName.startsWith(OLDMANDATORYFLAG_PREFIX)){
     			
     			Integer id = Integer.valueOf(parName.substring(OLDMANDATORYFLAG_PREFIX.length()));
     			String oldFlag = req.getParameter(parName);
     			String newFlag = req.getParameter(MANDATORYFLAG_PREFIX + id);
     			
     			if (!oldFlag.equals(newFlag)){
     				flags.put(id, newFlag==null ? "" : newFlag);
     			}
     		}
     	}
     	
     	if (!flags.isEmpty()){
     		
     		PreparedStatement stmt = null;
     		try{    		
     			stmt = conn.prepareStatement("update TBL2ELEM set MANDATORY=? where DATAELEM_ID=? and TABLE_ID=?");
     			for (Iterator iter = flags.entrySet().iterator(); iter.hasNext();){
     				
     				Map.Entry entry = (Map.Entry)iter.next();
     				stmt.setBoolean(1, Boolean.valueOf(entry.getValue().equals("T")));
     				stmt.setInt(2, ((Integer)entry.getKey()).intValue());
     				stmt.setInt(3, Integer.parseInt(tableID));
     				stmt.addBatch();
     			}
     			stmt.executeBatch();
     		}
     		finally{
     			SQL.close(stmt);
     		}
     	}
     }
     
     /**
      * 
      * @param elemId
      * @param pos
      * @throws Exception
      */
     private void updateTableElemPos(String elemId, String pos) throws Exception {
     	
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("TBL2ELEM");
         gen.setField("POSITION", pos);
 
         INParameters inParams = new INParameters();
         
         StringBuffer sqlBuf = new StringBuffer(gen.updateStatement());
         sqlBuf.append(" where TABLE_ID=");
         sqlBuf.append(inParams.add(tableID, Types.INTEGER));
         sqlBuf.append(" and DATAELEM_ID=");
         sqlBuf.append(inParams.add(elemId, Types.INTEGER));
 
         logger.debug(sqlBuf.toString());
 
         PreparedStatement stmt = SQL.preparedStatement(sqlBuf.toString(), inParams, conn);
         stmt.executeQuery();
         
         stmt.close();
     }
     
     /**
      * 
      * @throws Exception
      */
     private void validateAttributes() throws Exception {
 
     	validateMinMaxValues();
     }
 
     /**
      * 
      * @throws Exception
      */
 	private void validateMinMaxValues() throws Exception {
 		
 		// TODO this needs a little better handling,
 		// right now it is not, for example, checked whether the entered number
 		// format matches the data element type (i.e. integer, float).
 		// All entered numbers are converted into Double.
 		// The comparison of min and max values does take into account the
 		// decimal precision of the data element if specified.
 		
 		Double minInclValue = getAttrDoubleValueByShortName("MinInclusiveValue");
     	Double minExclValue = getAttrDoubleValueByShortName("MinExclusiveValue");
     	if (minInclValue!=null && minExclValue!=null){
     		throw new Exception("Either MinInclusiveValue or MinExclusiveValue can be specified!");
     	}
 
     	Double maxInclValue = getAttrDoubleValueByShortName("MaxInclusiveValue");
     	Double maxExclValue = getAttrDoubleValueByShortName("MaxExclusiveValue");
     	if (maxInclValue!=null && maxExclValue!=null){
     		throw new Exception("Either MaxInclusiveValue or MaxExclusiveValue can be specified!");
     	}
 
     	boolean atLeastOneMinSpecified = minInclValue!=null || minExclValue!=null;
     	boolean atLeastOneMaxSpecified = maxInclValue!=null || maxExclValue!=null;
     	
     	if (atLeastOneMinSpecified && atLeastOneMaxSpecified){
 
     		boolean ok = true;
     		if (minInclValue!=null){
     			
     			if (maxInclValue!=null){
     				if (!(maxInclValue >= minInclValue)){
     					ok = false;
     				}
     			}
     			else if (maxExclValue!=null){
     				if (!(maxExclValue > minInclValue)){
     					ok = false;
     				}
     			}
     		}
     		else if (minExclValue!=null){
     			
     			if (maxInclValue!=null){
     				if (!(maxInclValue > minExclValue)){
     					ok = false;
     				}
     			}
     			else if (maxExclValue!=null){
     				if (!(maxExclValue > minExclValue)){
     					ok = false;
     				}
     			}
     		}
     		
     		if (ok==false){
     			throw new Exception("The specified min-max values do not fit with each other!");
     		}
     	}
 	}
     
     /**
      * 
      * @param shortName
      * @return
      */
     private String getAttrValueByShortName(String shortName){
     	
     	String attrId = (String)attrIdsByShortName.get(shortName);
     	if (attrId!=null && attrId.trim().length()>0){
     		return req.getParameter(ATTR_PREFIX + attrId);
     	}
     	else{
     		return null;
     	}
     }
 
     /**
      * 
      * @param shortName
      * @return
      * @throws Exception
      */
     private Double getAttrDoubleValueByShortName(String shortName) throws Exception{
     	
     	String stringValue = getAttrValueByShortName(shortName);
     	if (stringValue==null || stringValue.trim().length()==0){
     		return null; 
     	}
     	else{
     		try{
     			return Double.valueOf(stringValue);
     		}
     		catch (NumberFormatException e){
     			throw new Exception("Illegal number format for attribute: " + shortName);
     		}
     	}
     }
     
     /**
      * 
      * @throws Exception
      */
     private void processAttributes() throws Exception {
     	
     	validateAttributes();
     	
     	String attrID=null;
     	Enumeration parNames = req.getParameterNames();
 
     	while (parNames.hasMoreElements()){
 
     		String parName = (String)parNames.nextElement();
     		
     		if (parName.startsWith(ATTR_PREFIX) && !parName.startsWith(ATTR_MULT_PREFIX)){
 
     			String attrValue = req.getParameter(parName);
     			if (attrValue.length()==0){
     				continue;
     			}
     			attrID = parName.substring(ATTR_PREFIX.length());
     			if (req.getParameterValues(INHERIT_ATTR_PREFIX + attrID)!=null){
     				continue;  //some attributes will be inherited from table level
     			}
 
     			insertAttribute(attrID, attrValue);
     		}
     		else if(parName.startsWith(ATTR_MULT_PREFIX)){
 
     			String[] attrValues = req.getParameterValues(parName);
     			if (attrValues == null || attrValues.length == 0){
     				continue;
     			}
 
     			attrID = parName.substring(ATTR_MULT_PREFIX.length());
     			if (req.getParameterValues(INHERIT_ATTR_PREFIX + attrID)!=null){
     				continue;  //some attributes will be inherited from table level
     			}
 
     			for (int i=0; i<attrValues.length; i++){
     				insertAttribute(attrID, attrValues[i]);
     			}
     		}
     		else if (parName.startsWith(INHERIT_ATTR_PREFIX)
     				&& !parName.startsWith(INHERIT_COMPLEX_ATTR_PREFIX)){
 
     			attrID = parName.substring(INHERIT_ATTR_PREFIX.length());
     			if (tableID==null){
     				continue;
     			}
 
     			CopyHandler ch = new CopyHandler(conn, ctx, searchEngine);
     			ch.setUser(user);
     			ch.copyAttribute(lastInsertID, tableID, "E", "T", attrID);
     		}
     		else if (parName.startsWith(INHERIT_COMPLEX_ATTR_PREFIX)){
 
     			attrID = parName.substring(INHERIT_COMPLEX_ATTR_PREFIX.length());
     			if (tableID==null){
     				continue;
     			}
     			CopyHandler ch = new CopyHandler(conn, ctx, searchEngine);
     			ch.setUser(user);
     			ch.copyComplexAttrs(lastInsertID, tableID, "T", "E", attrID);
     		}
     	}
 
     	// if there is a Datatype attribute and its value wasn't specified,
     	// make it a string.
     	if (!Util.nullString(mDatatypeID)){
     		if (datatypeValue==null){
     			insertAttribute(mDatatypeID, "string");
     		}
     	}
     }
     
     private void insertAttribute(String attrId, String value) throws Exception {
     	
         // for CH1 certain attributes are not allowed
         if (elmValuesType!=null && elmValuesType.equals("CH1") && ch1ProhibitedAttrs.contains(attrId))
             return;
 
         // 'Datatype' attribute needs special handling
         if (mDatatypeID!=null && attrId.equals(mDatatypeID)){
             
             // a CH2 cannot be of 'boolean' datatype
             if (elmValuesType!=null && elmValuesType.equals("CH2"))
                 if (value.equalsIgnoreCase("boolean"))
                     throw new Exception("An element of CH2 type cannot be a boolean!");
             
             // make sure that the value matches fixed values for 'Datatype'
             // we can do this in insertAttribute() only, because the problem
             // comes from Import tool only.
             if (searchEngine==null)
             	searchEngine = new DDSearchEngine(conn, "", ctx);
             Vector v = searchEngine.getFixedValues(attrId, "attr");
             boolean hasMatch = false;
             for (int i=0; v!=null && i<v.size(); i++){
                 FixedValue fxv = (FixedValue)v.get(i);
                 if (value.equals(fxv.getValue())){
                     hasMatch = true;
                     break;
                 }
             }
             
             if (!hasMatch)
                 throw new Exception("Unknown datatype for element " + elmIdfier);
                 
             datatypeValue = value;
         }
                     
         
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("ATTRIBUTE");
 
         gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
         gen.setFieldExpr("DATAELEM_ID", lastInsertID);
         gen.setField("VALUE", value);
         gen.setField("PARENT_TYPE", "E");
 
         String sql = gen.insertStatement();
         logger.debug(sql);
 
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(sql);
         stmt.close();
     }
 
     private void updateAttribute(String attrId, String value) throws SQLException {
         
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("ATTRIBUTE");
 
         gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
         gen.setFieldExpr("DATAELEM_ID", delem_id);
         gen.setFieldExpr("PARENT_TYPE", "E");
         gen.setField("VALUE", value);
 
         String sql = gen.updateStatement();
         logger.debug(sql);
 
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(sql);
         stmt.close();
     }
     
     private void setLastInsertID() throws SQLException {
 
         String qry = "SELECT LAST_INSERT_ID()";
 
         logger.debug(qry);
 
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(qry);
         rs.clearWarnings();
         if (rs.next()){
             lastInsertID = rs.getString(1);
         }
         stmt.close();
     }
 
     public String getLastInsertID(){
         return lastInsertID;
     }
 
     /**
      * 
      * @param copyElmID
      * @throws Exception
      */
     private void copyIntoNonCommon(String copyElmID) throws Exception{
 
     	// return if copyElemID is null
         if (copyElmID==null)
         	return;
         
         // copy row in DATAELEM table
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("DATAELEM");
         gen.setField("DATAELEM_ID", "");
         CopyHandler copyHandler = new CopyHandler(conn, null, null);
         lastInsertID = copyHandler.copy(gen, "DATAELEM_ID=" + copyElmID, false);
         if (lastInsertID==null){
         	return;
         }
 
         Statement stmt = null;
     	try {
 			// set Identifier to what user supplied 
 			gen = new SQLGenerator();
 			gen.setTable("DATAELEM");
 			gen.setField("IDENTIFIER", elmIdfier);
 			
 			// set parent -and top namespaces (corresponding to parent table and dataset respectively) 
 			gen.setFieldExpr("PARENT_NS", tblNamespaceID);
 			gen.setFieldExpr("TOP_NS", dstNamespaceID);
 
 			// set defaults
 			gen.setFieldExpr("VERSION", "1");
 			gen.setFieldExpr("NAMESPACE_ID", "1");
 			gen.setFieldExpr("CHECKEDOUT_COPY_ID", "NULL");
 			gen.setField("REG_STATUS", "Incomplete");			
 			gen.setFieldExpr("DATE", date==null ? String.valueOf(System.currentTimeMillis()) : date);
 			gen.setField("USER", user.getUserName());
 			gen.setFieldExpr("WORKING_USER", "NULL");
 			gen.setField("WORKING_COPY", "N");
 
 			// execute SQL
 			StringBuffer sqlBuf = new StringBuffer(gen.updateStatement());
 			sqlBuf.append(" where DATAELEM_ID=").append(lastInsertID);
 			logger.debug(sqlBuf.toString());
 			stmt = conn.createStatement();
 			stmt.executeUpdate(sqlBuf.toString());
 			
 			// copy simple attributes
 			gen.clear();
 			gen.setTable("ATTRIBUTE");
 			gen.setField("DATAELEM_ID", lastInsertID);
 			copyHandler.copy(gen, "DATAELEM_ID=" + copyElmID + " and PARENT_TYPE='E'");
 			
 	        // copy complex attributes
 			copyHandler.copyComplexAttrs(lastInsertID, copyElmID, "E");
 	        
 	        // copy fixed values
 			copyHandler.copyFxv(lastInsertID, copyElmID, "elem");
 			
 			// create table-to-element relation
 			if (tableID==null || tableID.length()==0)
 				throw new Exception("Missing tableID");
 			sqlBuf = new StringBuffer("insert into TBL2ELEM (TABLE_ID, DATAELEM_ID, POSITION) select ");
 			sqlBuf.append(tableID).append(", ").append(lastInsertID);
 			sqlBuf.append(", max(POSITION)+1 from TBL2ELEM where TABLE_ID=").append(tableID);
 			stmt.executeUpdate(sqlBuf.toString());
 		}
     	catch (Exception e) {
 			e.printStackTrace();
 			if (stmt!=null){
 				try{ stmt.close(); } catch (SQLException sqle){}
 			}
 		}
     }
     
 	/**
 	 * 
 	 * @param copyElmID
 	 * @throws Exception
 	 */
     private void copyIntoCommon(String copyElmID) throws Exception{
 
     	// return if copyElemID is null
         if (copyElmID==null){
         	return;
         }
         
         // copy row in DATAELEM table
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("DATAELEM");
         gen.setField("DATAELEM_ID", "");
         CopyHandler copyHandler = new CopyHandler(conn, null, null);
         lastInsertID = copyHandler.copy(gen, "DATAELEM_ID=" + copyElmID, false);
         if (lastInsertID==null){
         	return;
         }
 
         Statement stmt = null;
     	try {
 			// set Identifier to what user supplied 
 			gen = new SQLGenerator();
 			gen.setTable("DATAELEM");
 			gen.setField("IDENTIFIER", elmIdfier);
 			
 			// set defaults
 			gen.setFieldExpr("VERSION", "1");
 			gen.setFieldExpr("NAMESPACE_ID", "1");
 			gen.setFieldExpr("PARENT_NS", "NULL");
 			gen.setFieldExpr("TOP_NS", "NULL");
 			gen.setFieldExpr("CHECKEDOUT_COPY_ID", "NULL");
 			gen.setField("REG_STATUS", "Incomplete");			
 			gen.setFieldExpr("DATE", date==null ? String.valueOf(System.currentTimeMillis()) : date);
 			gen.setField("USER", user.getUserName());
 			gen.setField("WORKING_USER", user.getUserName());
 			gen.setField("WORKING_COPY", "Y");
 			
 			// execute SQL
 			StringBuffer sqlBuf = new StringBuffer(gen.updateStatement());
 			sqlBuf.append(" where DATAELEM_ID=").append(lastInsertID);
 			logger.debug(sqlBuf.toString());
 			stmt = conn.createStatement();
 			stmt.executeUpdate(sqlBuf.toString());
 			
 			// copy simple attributes
 			gen.clear();
 			gen.setTable("ATTRIBUTE");
 			gen.setField("DATAELEM_ID", lastInsertID);
 			copyHandler.copy(gen, "DATAELEM_ID=" + copyElmID + " and PARENT_TYPE='E'");
 			
 	        // copy complex attributes
 			copyHandler.copyComplexAttrs(lastInsertID, copyElmID, "E");
 	        
 	        // copy fixed values
 			copyHandler.copyFxv(lastInsertID, copyElmID, "elem");
 		}
     	catch (Exception e) {
 			e.printStackTrace();
 			throw e;
 		}
     	finally{
 			if (stmt!=null){
 				try{ stmt.close(); } catch (SQLException sqle){}
 			}
     	}
 	}
     
 	/**
 	 * @param elmCommon
 	 * @return
 	 * @throws SQLException
 	 */
 	private boolean existsInTable() throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf = new StringBuffer();
         buf.append("select count(DATAELEM.DATAELEM_ID) from TBL2ELEM ").
         append("left outer join DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID where ").
         append("TBL2ELEM.TABLE_ID=").
         append(inParams.add(tableID, Types.INTEGER)).
         append(" and DATAELEM.DATAELEM_ID is not null and DATAELEM.IDENTIFIER=").
         append(inParams.add(elmIdfier, Types.VARCHAR));
         
         PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
         ResultSet rs = stmt.executeQuery();
         
         if (rs.next()){
             if (rs.getInt(1)>0){
                 return true;
             }
         }
 
         return false;
 	}
 	
 	/**
 	 * 
 	 * @param elmCommon
 	 * @return
 	 * @throws SQLException
 	 */
 	private boolean existsCommon() throws SQLException {
 
 		INParameters inParams = new INParameters();
 		String q = "select count(*) as COUNT from DATAELEM where PARENT_NS is null and IDENTIFIER=";
 		q += inParams.add(elmIdfier, Types.VARCHAR);
 	
 		PreparedStatement stmt = SQL.preparedStatement(q, inParams, conn);
         ResultSet rs = stmt.executeQuery();
 		if (rs.next()){
 			if (rs.getInt("COUNT")>0){
 				return true;
 			}
 		}
 		stmt.close();
 		return false;
 	}
 
     public boolean getCheckInResult(){
         return this.checkInResult;
     }
     
 	public String getRestoredID(){
 		return this.restoredID;
 	}
 
 	private void setLatestCommonElmID(String idf) throws SQLException{
 		
 		VersionManager verMan = new VersionManager(conn, user);
 		DataElement elm = new DataElement();
 		elm.setIdentifier(idf);
 		this.latestCommonElmID = verMan.getLatestElmID(elm);
 	}
 	
 	public String getLatestCommonElmID(){
 		return latestCommonElmID;
 	}
 	
 	/*
 	 * 
 	 */
 	public void setDate(String unixTimestampMillisec){
 		this.date = unixTimestampMillisec;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
     public String getCheckedInCopyID() {
 		return checkedInCopyID;
 	}
 
 	/**
 	 * 
 	 * @param latestDatasetID
 	 */
     private void setCheckedInCopyID(String latestDatasetID) {
 		this.checkedInCopyID = latestDatasetID;
 	}
 
     /**
      * 
      * @param importMode
      */
 	public void setImportMode(boolean importMode){
 		this.isImportMode = importMode;
 	}
 
     /**
      * 
      * @param useForce
      */
     public void setUseForce(boolean useForce) {
 		this.useForce = useForce;
 	}
 
     /**
      * 
      * @param oldID
      * @param newID
      * @param conn
      */
     public static void replaceID(String oldID, String newID, Connection conn) throws SQLException{
     	
     	Statement stmt = null;
     	SQLGenerator gen = null;
     	StringBuffer buf = null;
     	try{
     		stmt = conn.createStatement();
 	    	
     		gen = new SQLGenerator();
         	gen.setTable("DATAELEM");
         	gen.setFieldExpr("DATAELEM_ID", newID);
         	buf = new StringBuffer(gen.updateStatement());
         	buf.append(" where DATAELEM_ID=").append(oldID);
 			stmt.executeUpdate(buf.toString());
 			
 			gen.clear();
 			gen.setTable("ATTRIBUTE");
         	gen.setFieldExpr("DATAELEM_ID", newID);
         	buf = new StringBuffer(gen.updateStatement());
         	buf.append(" where PARENT_TYPE='E' and DATAELEM_ID=").append(oldID);
         	stmt.executeUpdate(buf.toString());
         	
 			gen.clear();
 			gen.setTable("COMPLEX_ATTR_ROW");
         	gen.setFieldExpr("PARENT_ID", newID);
         	buf = new StringBuffer(gen.updateStatement());
         	buf.append(" where PARENT_TYPE='E' and PARENT_ID=").append(oldID);
         	stmt.executeUpdate(buf.toString());
         	
         	gen.clear();
 			gen.setTable("TBL2ELEM");
         	gen.setFieldExpr("DATAELEM_ID", newID);
         	buf = new StringBuffer(gen.updateStatement());
         	buf.append(" where DATAELEM_ID=").append(oldID);
         	stmt.executeUpdate(buf.toString());
         	
         	gen.clear();
 			gen.setTable("FK_RELATION");
         	gen.setFieldExpr("A_ID", newID);
         	buf = new StringBuffer(gen.updateStatement());
         	buf.append(" where A_ID=").append(oldID);
         	stmt.executeUpdate(buf.toString());
         	gen.clear();
 			gen.setTable("FK_RELATION");
         	gen.setFieldExpr("B_ID", newID);
         	buf = new StringBuffer(gen.updateStatement());
         	buf.append(" where B_ID=").append(oldID);
         	stmt.executeUpdate(buf.toString());
         	
         	gen.clear();
 			gen.setTable("FXV");
         	gen.setFieldExpr("OWNER_ID", newID);
         	buf = new StringBuffer(gen.updateStatement());
         	buf.append(" where OWNER_TYPE='elem' and OWNER_ID=").append(oldID);
         	stmt.executeUpdate(buf.toString());
     	}
     	finally{
 			try{
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
     	}
     }
 
     /**
      * Removes ACLs of those common elements not present any more in IDENTIFIER from DATAELEM.
      * NB! Modifies the identifiers HashSet by removing those whose acl is not to be deleted.
      * 
      * @throws SQLException 
      * @throws SignOnException 
      */
     private void removeAcls(Statement stmt, HashSet identifiers) throws SQLException, SignOnException{
     	
     	ResultSet rs = stmt.executeQuery("select distinct IDENTIFIER from DATAELEM");
     	while (rs.next()){
     		String identifier = rs.getString(1);
     		if (identifiers.contains(identifier)){
     			identifiers.remove(identifier);
     		}
     	}
     	
     	if (identifiers.size()==0)
     		return;
     	
     	int i=0;
     	for (Iterator iter = identifiers.iterator(); iter.hasNext(); i++){
     		AccessController.removeAcl("/elements/" + (String)iter.next());
     	}		
     }
 	
 	/**
 	 * @throws Exception 
 	 */
 	private String getTblNamespaceID() throws Exception{
 		
 		if (tblNamespaceID==null){
 			
 			INParameters inParams = new INParameters();
 			String q = "select CORRESP_NS from DS_TABLE where TABLE_ID=";
 			q += inParams.add(tableID, Types.INTEGER);
 
 			PreparedStatement stmt = null;
 			ResultSet rs = null;
 			try{
 				stmt = SQL.preparedStatement(q, inParams, conn);
 		        rs = stmt.executeQuery();
 				tblNamespaceID = rs.next() ? rs.getString(1) : null;
 			}
 			catch (Exception e){
 				e.printStackTrace();
 				try{
 					if (stmt!=null) stmt.close();
 					if (rs!=null) rs.close();
 				}
 				catch (SQLException sqle){}
 			}
 		}
 		
 		if (tblNamespaceID==null){
 			throw new Exception("Failed to obtain table namespace ID which is required");
 		}
 		
 		return tblNamespaceID;
 	}
 	
 	/**
 	 * @throws Exception 
 	 */
 	private String getDstNamespaceID() throws Exception{
 		
 		if (dstNamespaceID==null){
 			
 			INParameters inParams = new INParameters();
 			String q = "select PARENT_NS from DS_TABLE where TABLE_ID=";
 			q += inParams.add(tableID, Types.INTEGER);
 
 			PreparedStatement stmt = null;
 			ResultSet rs = null;
 			try{
 				stmt = SQL.preparedStatement(q, inParams, conn);
 		        rs = stmt.executeQuery();
 				dstNamespaceID = rs.next() ? rs.getString(1) : null;
 			}
 			catch (Exception e){
 				e.printStackTrace();
 				try{
 					if (stmt!=null) stmt.close();
 					if (rs!=null) rs.close();
 				}
 				catch (SQLException sqle){}
 			}
 		}
 		
 		if (dstNamespaceID==null){
 			throw new Exception("Failed to obtain dataset namespace ID which is required");
 		}
 		
 		return dstNamespaceID;
 	}
 }
