 package eionet.meta;
 
 import eionet.meta.savers.*;
 
 import java.util.*;
 import java.sql.*;
 import java.io.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.ServletContext;
 
 import com.tee.util.Util;
 import com.tee.xmlserver.AppUserIF;
 
 public class MrProper {
     
     public static final String FUNCTIONS_PAR = "functs";
     public static final String DST_NAME   = "dsname";
 	public static final String DST_IDFIER = "idfier";
 	public static final String VISUALS_PATH = "vispath";
     
     public static final String RLS_DST = "rls_dst";
     public static final String ORPHAN_ELM = "orphan_elm";
     public static final String ORPHAN_TBL = "orphan_tbl";
     public static final String RMV_MULT_VERS = "rmv_mult_vers";
     public static final String RLS_NOWC = "rls_nowc";
 	public static final String RMV_WC_NORIG = "rmv_wc_noorig";
 	public static final String CLN_VISUALS = "cln_visuals";
 	public static final String RMV_OBJECTS = "rmv_objects";
     
     /** */
     private Connection conn = null;
     private ServletContext ctx = null;
     private AppUserIF user = null;
     private Vector response = new Vector();
     
     private Hashtable funNames = null;
     private boolean wasExc = false;
     
     /**
     *
     */
     public MrProper(Connection conn){
         this.conn = conn;
         
         funNames = new Hashtable();
         funNames.put(RLS_DST, "Releasing the dataset");
         funNames.put(ORPHAN_ELM, "Deleting elements without parent tables");
         funNames.put(ORPHAN_TBL, "Deleting tables without parent datasets");
         funNames.put(RMV_MULT_VERS, "Removing multiple versions");
         funNames.put(RLS_NOWC, "Releasing locked objects");
 		funNames.put(RMV_WC_NORIG, "Removing working copies with no originals");
 		funNames.put(CLN_VISUALS, "Deleting uploaded images no longer used");
 		funNames.put(RMV_OBJECTS, "Removing objects by selected criteria");
     }
     
     /**
     *
     */
     public void setContext(ServletContext ctx){
         this.ctx = ctx;
     }
     
     /**
     *
     */
     public void setUser(AppUserIF user){
         this.user = user;
     }
     
     /**
     *
     */
     public void execute(HttpServletRequest req){
         execute(new Parameters(req));
     }
     
     /**
     *
     */
     public void execute(Parameters pars){
     	
         // check if user is authentic
         if (user==null || !user.isAuthentic()){
             response.add("Unauthorized user!");
             return;
         }
         
         /* check the user permissions
         try{
             AccessControlListIF acl = getAcl(DDuser.ACL_SERVICE_NAME );
             boolean isOK =
                 acl.checkPermission(userName, DDuser.ACL_CLEANUP_PRM);
             if (!isOK)
                 throw new Exception("User " + userName +
                                     " does not have this permission!");
         }
         catch (Exception e){
             response.add(e.getMessage());
             return;
         }*/
         
         // start execution
         String[] functs = pars.getParameterValues(FUNCTIONS_PAR);
         if (functs==null || functs.length==0){
             response.add("No functions specified!");
             return;
         }
         
         for (int i=0; i<functs.length; i++){
             
             String fun = functs[i];
             
             try{
 				if (fun.equals(RMV_OBJECTS))
 					removeObj(pars);
 				if (fun.equals(RMV_WC_NORIG))
 					removeHangingWCs();
 				if (fun.equals(RLS_NOWC))
 					releaseNonWC();
 				if (fun.equals(RMV_MULT_VERS))
 					multipleVersions();
 				if (fun.equals(ORPHAN_ELM))
 					orphanElements();
 				if (fun.equals(ORPHAN_TBL))
 					orphanTables();
                 if (fun.equals(RLS_DST))
                     releaseDataset(pars.getParameter(DST_IDFIER));
 				//if (fun.equals(CLN_VISUALS))
 				//	cleanVisuals(pars.getParameter(VISUALS_PATH));
             }
             catch (Exception e){
 				wasExc = true;
 				log(e.toString());
                 response.add((String)funNames.get(fun) +
                                 " failed: <b>" + e.toString() + "</b>");
                 continue;
             }
             
             response.add((String)funNames.get(fun) + " was <b>OK!</b>");
         }
     }
 
 	/**
 	 * 
 	 */
 	private void removeObj(Parameters pars) throws Exception {
 		String objType = pars.getParameter("rm_obj_type");
 		if (objType==null)
 			return;
 		else if (objType.equals("dst"))
 			removeDst(pars);
 		else if (objType.equals("tbl"))
 			removeTbl(pars);
 		else if (objType.equals("elm"))
 			removeElm(pars);
 		else
 			return;
 	}
 
 	/**
 	 * 
 	 */
 	private void removeDst(Parameters pars) throws Exception {
 		
 		Vector v = new Vector();
 		StringBuffer buf = new StringBuffer();
 		String rmCrit = pars.getParameter("rm_crit");
 		if (rmCrit==null)
 			return;
 		else if (rmCrit.equals("lid")){
 			String idfier = pars.getParameter("rm_idfier");
 			if (idfier!=null){
 				buf.append("select DATASET_ID from DATASET where "); 
 				buf.append("IDENTIFIER=").append(Util.strLiteral(idfier));
 			}
 		}
 		else if (rmCrit.equals("id")){
 			String id = pars.getParameter("rm_id");
 			if (id!=null){
 				StringTokenizer st = new StringTokenizer(id);
 				while (st.hasMoreTokens())
 					v.add(st.nextToken());
 			}
 		}
 		
 		if (buf.length()>0){
 			ResultSet rs = conn.createStatement().executeQuery(buf.toString());
 			while (rs.next())
 				v.add(rs.getString(1));
 		}
 		
 		if (v.size()==0) return;
 		
 		Parameters ps = new Parameters();
 		ps.addParameterValue("mode", "delete");
 		ps.addParameterValue("complete", "true");
 		for (int i=0; i<v.size(); i++)
 			ps.addParameterValue("ds_id", (String)v.get(i));
 		
 		DatasetHandler handler = new DatasetHandler(conn, ps, ctx);
 		handler.setUser(user);
 		handler.setVersioning(false);
 		handler.execute();
 	}
 
 	/**
 	 * 
 	 */
 	private void removeTbl(Parameters pars) throws Exception {
 
 		Vector v = new Vector();
 		StringBuffer buf = new StringBuffer();
 		String rmCrit = pars.getParameter("rm_crit");
 		if (rmCrit==null)
 			return;
 		else if (rmCrit.equals("lid")){
 			String idfier = pars.getParameter("rm_idfier");
 			String ns = pars.getParameter("rm_ns");
 			if (idfier!=null && ns!=null){
 				buf.append("select TABLE_ID from DS_TABLE where "); 
 				buf.append("IDENTIFIER=").append(Util.strLiteral(idfier));
 				buf.append("and PARENT_NS=").append(ns);
 			}
 		}
 		else if (rmCrit.equals("id")){
 			String id = pars.getParameter("rm_id");
 			if (id!=null){
 				StringTokenizer st = new StringTokenizer(id);
 				while (st.hasMoreTokens())
 					v.add(st.nextToken());
 			}
 		}
 		
 		if (buf.length()>0){
 			ResultSet rs = conn.createStatement().executeQuery(buf.toString());
 			while (rs.next())
 				v.add(rs.getString(1));
 		}
 		
 		if (v.size()==0) return;
 		
 		Parameters ps = new Parameters();
 		ps.addParameterValue("mode", "delete");
 		for (int i=0; i<v.size(); i++)
 			ps.addParameterValue("del_id", (String)v.get(i));
 		
 		DsTableHandler handler = new DsTableHandler(conn, ps, ctx);
 		handler.setUser(user);
 		handler.setVersioning(false);
 		handler.execute();
 	}
 
 	/**
 	 * 
 	 */
 	private void removeElm(Parameters pars) throws Exception {
 
 		Vector v = new Vector();
 		StringBuffer buf = new StringBuffer();
 		String rmCrit = pars.getParameter("rm_crit");
 		if (rmCrit==null)
 			return;
 		else if (rmCrit.equals("lid")){
 			String idfier = pars.getParameter("rm_idfier");
 			String ns = pars.getParameter("rm_ns");
 			if (idfier!=null){
 				buf.append("select DATAELEM_ID from DATAELEM where "). 
 				append("IDENTIFIER=").append(Util.strLiteral(idfier));
 			}
 			
 			if (!Util.nullString(ns)) buf.append(" and PARENT_NS=").append(ns);
 		}
 		else if (rmCrit.equals("id")){
 			String id = pars.getParameter("rm_id");
 			if (id!=null){
 				StringTokenizer st = new StringTokenizer(id);
 				while (st.hasMoreTokens())
 					v.add(st.nextToken());
 			}
 		}
 		
 		if (buf.length()>0){
 			ResultSet rs = conn.createStatement().executeQuery(buf.toString());
 			while (rs.next())
 				v.add(rs.getString(1));
 		}
 		
 		if (v.size()==0) return;
 		
 		Parameters ps = new Parameters();
 		ps.addParameterValue("mode", "delete");
 		for (int i=0; i<v.size(); i++)
 			ps.addParameterValue("delem_id", (String)v.get(i));
 		
 		DataElementHandler handler = new DataElementHandler(conn, ps, ctx);
 		handler.setUser(user);
 		handler.setVersioning(false);
 		handler.execute();
 	}
 
 	/**
 	*
 	*/
 	public boolean wasException(){
 		return this.wasExc;
 	}
     
     /**
     *
     */
     private void releaseDataset(String idifier) throws Exception{
         
         if (Util.nullString(idifier))
             throw new Exception("Dataset identifier not given!");
         
         String q = "select distinct CORRESP_NS from DATASET where " +
                    "IDENTIFIER=" + Util.strLiteral(idifier);
         
         String ns = null;
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(q);
         if (rs.next())
             ns = rs.getString(1);
         
         if (ns!=null)
             stmt.execute("update NAMESPACE set WORKING_USER=NULL " +
                          "where NAMESPACE_ID=" + ns);
         stmt.close();
     }
 
 	/**
 	*
 	*/
 	private void cleanVisuals(String visualsPath) throws Exception{
 		
 		if (Util.nullString(visualsPath))
 			throw new Exception("Path to uploaded image files not given!");
         
         File dir = new File(visualsPath);
         if (!dir.exists() || !dir.isDirectory())
         	return;
 
 		String q1 =
 		"select count(*) from DATASET where VISUAL=? or DETAILED_VISUAL=?";
 		String q2 =
 		"select count(*) from ATTRIBUTE, M_ATTRIBUTE where " +
 		"ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID and " +
 		"M_ATTRIBUTE.DISP_TYPE='image' and VALUE=?";
 		 
 		PreparedStatement pstmt1 = conn.prepareStatement(q1);
 		PreparedStatement pstmt2 = conn.prepareStatement(q2);
 		
         File[] files = dir.listFiles();
         for (int i=0; files!=null && i<files.length; i++){
         	String fileName = files[i].getName();
         	if (Util.nullString(fileName))
         		continue;
 
 			boolean delete = true;
 			        	
         	pstmt1.setString(1, fileName);
 			pstmt1.setString(2, fileName);
 			ResultSet rs = pstmt1.executeQuery();
 			if (rs.next() && rs.getInt(1)>0)
 				delete = false;
 			
 			pstmt2.setString(1, fileName);
 			rs = pstmt2.executeQuery();
 			if (rs.next() && rs.getInt(1)>0)
 				delete = false;
 			
 			if (delete){
 				files[i].delete();
 			}
         }
         
 		pstmt1.close();
 		pstmt2.close();
 	}
     
     /**
     *
     */
     private void orphanElements() throws Exception{
     	
 		// There might be elements that have relations
 		// both to existing tables and non-existing tables.
 		// So first find the tables that are present in TBL2ELEM,
 		// but actually do not exist any more.
 		// Then remove all TBL2ELEM rows with such tables.
 		// And then delete all elements that do not seem to have a
 		// parent table by join through TBL2ELEM->DS_TABLE.
 
 		// find & delete related, yet non-existing tables
 		String q =
 		"select distinct TBL2ELEM.TABLE_ID from TBL2ELEM " +
 		"left outer join DS_TABLE " +
 		"on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID " +
 		"where DS_TABLE.SHORT_NAME is null";
 
 		Vector v = new Vector();
 		Statement stmt = conn.createStatement();
 		ResultSet rs = stmt.executeQuery(q);
 		while (rs.next())
 			v.add(rs.getString(1));
 		
 		for (int i=0; i<v.size(); i++)
 			stmt.executeQuery("delete from TBL2ELEM where TABLE_ID=" +
 															(String)v.get(i));
        
         // get the elements
         q =
         "select distinct DATAELEM.DATAELEM_ID from DATAELEM " +
         "left outer join TBL2ELEM " +
         "on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID " +
         "left outer join DS_TABLE " +
         "on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID " +
        "where DATAELEM.PARENT_NS is not null and DS_TABLE.SHORT_NAME is null";
         
         v = new Vector();
         rs = stmt.executeQuery(q);
         while (rs.next())
             v.add(rs.getString(1));
         
         if (v.size()==0) return;
         
         // delete the found elements
         Parameters params = new Parameters();
         params.addParameterValue("mode", "delete");
         for (int i=0; i<v.size(); i++)
 			params.addParameterValue("delem_id", (String)v.get(i));
 			
         DataElementHandler delemHandler =
 								new DataElementHandler(conn, params, ctx);
 		delemHandler.setUser(user);
         delemHandler.setVersioning(false);
         delemHandler.setSuperUser(true);
 		delemHandler.execute();
         
         // close statement
         stmt.close();
     }
     
     private void orphanTables() throws Exception{
     	
     	// There might be tables that have relations
     	// both to existing datasets and non-existing datasets.
     	// So first find the datasets that are present in DST2TBL,
     	// but actually do not exist any more.
     	// Then remove all DST2TBL rows with such datasets
     	// And then delete all tables that do not seem to have a
     	// parent dataset by join through DST2TBL->DATASET.
         
         
 		// find & delete related, yet non-existing datasets		
 		String q =
 		"select distinct DST2TBL.DATASET_ID from DST2TBL " +
 		"left outer join DATASET " +
 		"on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +
 		"where DATASET.SHORT_NAME is null";
 		
 		Vector v = new Vector();
 		Statement stmt = conn.createStatement();
 		ResultSet rs = stmt.executeQuery(q);
 		while (rs.next())
 			v.add(rs.getString(1));
 		
 		for (int i=0; i<v.size(); i++)
 			stmt.executeUpdate("delete from DST2TBL where DATASET_ID=" +
 															(String)v.get(i));
 		
         // get orphan tables
         q =
         "select distinct DS_TABLE.TABLE_ID from DS_TABLE " +
         "left outer join DST2TBL " +
         "on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID " +
         "left outer join DATASET " +
         "on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +
         "where DATASET.SHORT_NAME is null";
         
         v = new Vector();
         rs = stmt.executeQuery(q);
         while (rs.next())
             v.add(rs.getString(1));
         
         if (v.size()==0) return;
         
         // delete the found tables
         Parameters params = new Parameters();
         params.addParameterValue("mode", "delete");
         for (int i=0; i<v.size(); i++)
 			params.addParameterValue("del_id", (String)v.get(i));
 			
         DsTableHandler dsTableHandler =
 								new DsTableHandler(conn, params, ctx);
 		dsTableHandler.setUser(user);
         dsTableHandler.setVersioning(false);
         dsTableHandler.setSuperUser(true);
 		dsTableHandler.execute();
         
         // close statement
         stmt.close();
     }
     
     /**
     *
     */
     private void multipleVersions() throws Exception{
     	
 		//data elements
     	StringBuffer buf = new StringBuffer().
     	append("select * from DATAELEM order by DATE desc");
 		
 		Vector odd = new Vector();
 		HashSet all = new HashSet();
 		
 		Statement stmt = conn.createStatement();
 		ResultSet rs = stmt.executeQuery(buf.toString());
 		while (rs.next()){
 			
 			if (rs.getString("WORKING_COPY").equals("Y"))
 				continue;
 			
 			String parentNs = rs.getString("PARENT_NS");
 			if (parentNs==null) parentNs="";
 			
 			Hashtable hash = new Hashtable();			
 			hash.put("SHORT_NAME", rs.getString("SHORT_NAME"));
 			hash.put("PARENT_NS", parentNs);
 			hash.put("VERSION", rs.getString("VERSION"));
 			if (all.contains(hash))
 				odd.add(rs.getString("DATAELEM_ID"));
 			else
 				all.add(hash);
 		}
 		
 		Parameters pars = new Parameters();
 		pars.addParameterValue("mode", "delete");
 		pars.addParameterValue("complete", "true");
 		for (int i=0; i<odd.size(); i++)
 			pars.addParameterValue("delem_id", (String)odd.get(i));
 
 		DataElementHandler elmH = new DataElementHandler(conn, pars, ctx);
 		elmH.setUser(user);
 		elmH.setVersioning(false);
 		elmH.execute();
 
 		// tables
 		buf = new StringBuffer().
 		append("select * from DS_TABLE order by DATE desc");
 		
 		odd = new Vector();
 		all = new HashSet();
 		
 		rs = stmt.executeQuery(buf.toString());
 		while (rs.next()){
 			
 			if (rs.getString("WORKING_COPY").equals("Y"))
 				continue;
 
 			String parentNs = rs.getString("PARENT_NS");
 			if (parentNs==null) parentNs="";
 			
 			Hashtable hash = new Hashtable();
 			hash.put("SHORT_NAME", rs.getString("SHORT_NAME"));
 			hash.put("PARENT_NS", parentNs);
 			hash.put("VERSION", rs.getString("VERSION"));
 			if (all.contains(hash))
 				odd.add(rs.getString("TABLE_ID"));
 			else
 				all.add(hash);
 		}
 		
 		pars = new Parameters();
 		pars.addParameterValue("mode", "delete");
 		pars.addParameterValue("complete", "true");
 					
 		for (int i=0; i<odd.size(); i++)
 			pars.addParameterValue("del_id", (String)odd.get(i));
 
 		DsTableHandler tblH = new DsTableHandler(conn, pars, ctx);
 		tblH.setUser(user);
 		tblH.setVersioning(false);
 		tblH.execute();
 
 		// datasets
 		buf = new StringBuffer().
 		append("select * from DATASET order by DATE desc");
 		
 		odd = new Vector();
 		all = new HashSet();
 		
 		rs = stmt.executeQuery(buf.toString());
 		while (rs.next()){
 			
 			if (rs.getString("WORKING_COPY").equals("Y"))
 				continue;
 			
 			Hashtable hash = new Hashtable();
 			hash.put("SHORT_NAME", rs.getString("SHORT_NAME"));
 			hash.put("VERSION", rs.getString("VERSION"));
 			if (all.contains(hash))
 				odd.add(rs.getString("DATASET_ID"));
 			else
 				all.add(hash);
 		}
 		
 		pars = new Parameters();
 		pars.addParameterValue("mode", "delete");
 		pars.addParameterValue("complete", "true");
 		
 		for (int i=0; i<odd.size(); i++)
 			pars.addParameterValue("ds_id", (String)odd.get(i));
 
 		DatasetHandler dstH = new DatasetHandler(conn, pars, ctx);
 		dstH.setUser(user);
 		dstH.setVersioning(false);
 		dstH.execute();
     }
     
     /**
     *
     */
     private void releaseNonWC() throws Exception{
         releaseNonWC("DATAELEM");
         releaseNonWC("DS_TABLE");
         releaseNonWC("DATASET");
     }
     
     /**
     *
     */
     private void releaseNonWC(String tblName) throws Exception{
         
         // get the locked non-wcs
         StringBuffer buf = new StringBuffer();
 		buf.append("select distinct SHORT_NAME, VERSION");
         if (!tblName.equals("DATASET"))
             buf.append(", PARENT_NS");
         buf.append(" from ");
         buf.append(tblName);
         buf.append(" where WORKING_USER is not null");
         
         Vector v = new Vector();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(buf.toString());
         while (rs.next()){
         	Hashtable hash = new Hashtable();
             hash.put("SHORT_NAME", rs.getString("SHORT_NAME"));
 			hash.put("VERSION", rs.getString("VERSION"));
 			String pns = rs.getString("PARENT_NS");
 			if (pns!=null && !tblName.equals("DATASET")) hash.put("PARENT_NS", pns);
             v.add(hash);
         }
         
         
         // get the wcs
         buf = new StringBuffer();
 		buf.append("select distinct IDENTIFIER, VERSION");
         if (!tblName.equals("DATASET"))
             buf.append(", PARENT_NS");
         buf.append(" from ");
         buf.append(tblName);
         buf.append(" where WORKING_COPY='Y'");
         
         HashSet wcs = new HashSet();
         rs = stmt.executeQuery(buf.toString());
         while (rs.next()){
 			Hashtable hash = new Hashtable();
 			hash.put("IDENTIFIER", rs.getString("IDENTIFIER"));
 			hash.put("VERSION", rs.getString("VERSION"));
 			String pns = rs.getString("PARENT_NS");
 			if (pns!=null && !tblName.equals("DATASET")) hash.put("PARENT_NS", pns);
 			wcs.add(hash);
         }
         
         // loop over locked objects, delete those not present in WC hash 
         for (int i=0; i<v.size(); i++){
             
             Hashtable hash = (Hashtable)v.get(i);
             
             if (wcs.contains(hash)) // if has a WC then skip
             	continue;
             	
             buf = new StringBuffer();
             buf.append("update ");
 			buf.append(tblName);
 			buf.append(" set WORKING_USER=NULL where IDENTIFIER=");
 			buf.append(Util.strLiteral((String)hash.get("IDENTIFIER")));
 			buf.append(" and VERSION=");
 			buf.append((String)hash.get("VERSION"));
 			
 			String pns = (String)hash.get("PARENT_NS");
 			if (pns!=null && !tblName.equals("DATASET"))buf.append(" and PARENT_NS=").append(pns);
 			
             stmt.executeUpdate(buf.toString());
         }
         
         stmt.close();
     }
     
 	/*
 	 * 
 	 */
 	private void removeHangingElmWCs(boolean common) throws Exception{
 		
 		// prepare the statement for retreiving the count of non-WCs for given logical ID
 		StringBuffer buf = new StringBuffer().
 		append("select count(*) from DATAELEM where WORKING_COPY='N' and ").
 		append("WORKING_USER is not null and IDENTIFIER=? and VERSION=? and DATE<?");
 		if (!common)
 			buf.append(" and PARENT_NS=?");
 		else
 			buf.append(" and PARENT_NS is null");
 		PreparedStatement pstmt= conn.prepareStatement(buf.toString());
 		
 		// execute the statement for finding all all working copies
 		Vector hangingWcs = new Vector();
 		buf = new StringBuffer("select * from DATAELEM where WORKING_COPY='Y'");
 		if (!common)
 			buf.append(" and PARENT_NS is not null");
 		else
 			buf.append(" and PARENT_NS is null");
 		Statement stmt = conn.createStatement();
 		ResultSet rs = stmt. executeQuery(buf.toString());
 		while (rs.next()){
 			// execute the prepared statement
 			pstmt.setString(1, rs.getString("IDENTIFIER"));
 			pstmt.setInt(2, rs.getInt("VERSION"));
 			pstmt.setLong(3, rs.getLong("DATE"));
 			if (!common) pstmt.setInt(4, rs.getInt("PARENT_NS"));
 			
 			ResultSet rs2 = pstmt.executeQuery();
 
 			// if no original found, add WC ID to hash
 			if (!rs2.next() || rs2.getInt(1)==0)
 				hangingWcs.add(rs.getString("DATAELEM_ID"));
 		}
 
 		for (int i=0; i<hangingWcs.size(); i++){
 	
 			String id = (String)hangingWcs.get(i);
 	
 			Parameters pars = new Parameters();
 			pars.addParameterValue("mode", "delete");
 			pars.addParameterValue("complete", "true");
 			pars.addParameterValue("delem_id", id);
 
 			DataElementHandler h = new DataElementHandler(conn, pars, ctx);
 			h.setUser(user);
 			h.setVersioning(false);
 			h.execute();
 		}
 	}
     
     /*
      * 
      */
     private void removeHangingWCs() throws Exception{
     	
 		removeHangingElmWCs(true); // handles common elements 
 		removeHangingElmWCs(false); // hanldes non-common elements
     	
 		// tables
 		StringBuffer buf = new StringBuffer().
 		append("select count(*) from DS_TABLE where WORKING_COPY='N' and ").
 		//append("WORKING_USER=").append(Util.strLiteral(user.getUserName())).
 		append("WORKING_USER is not null and ").
 		append("IDENTIFIER=? and PARENT_NS=? and VERSION=? and ").
 		append("DATE<?");
 
 		PreparedStatement pstmt= conn.prepareStatement(buf.toString());
 
 		Vector hangingWcs = new Vector();
 		 
 		Statement stmt = conn.createStatement();
 		ResultSet rs = stmt.executeQuery("select * from DS_TABLE where WORKING_COPY='Y'");
     	
 		while (rs.next()){
 			// execute prep statement with qry for original
 			pstmt.setString(1, rs.getString("IDENTIFIER"));
 			pstmt.setInt(2, rs.getInt("PARENT_NS"));
 			pstmt.setInt(3, rs.getInt("VERSION"));
 			pstmt.setLong(4, rs.getLong("DATE"));
 			ResultSet rs2 = pstmt.executeQuery();
 
 			// if no original found, add WC ID to hash
 			if (!rs2.next() || rs2.getInt(1)==0)
 				hangingWcs.add(rs.getString("TABLE_ID"));
 		}
     	
 		for (int i=0; i<hangingWcs.size(); i++){
     		
 			String id = (String)hangingWcs.get(i);
 			
 			Parameters pars = new Parameters();
 			pars.addParameterValue("mode", "delete");
 			pars.addParameterValue("complete", "true");
 			pars.addParameterValue("del_id", id);
 
 			DsTableHandler h = new DsTableHandler(conn, pars, ctx);
 			h.setUser(user);
 			h.setVersioning(false);
 			//h.execute();
 		}
 
 		// datasets
 		buf = new StringBuffer().
 		append("select count(*) from DATASET where WORKING_COPY='N' and ").
 		//append("WORKING_USER=").append(Util.strLiteral(user.getUserName())).
 		append("WORKING_USER is not null and ").
 		append("IDENTIFIER=? and VERSION=? and ").
 		append("DATE<?");
 
 		pstmt= conn.prepareStatement(buf.toString());
 
 		hangingWcs = new Vector();
 		 
 		stmt = conn.createStatement();
 		rs = stmt.executeQuery("select * from DATASET where WORKING_COPY='Y'");
     	
 		while (rs.next()){
 			// execute prep statement with qry for original
 			pstmt.setString(1, rs.getString("IDENTIFIER"));
 			pstmt.setInt(2, rs.getInt("VERSION"));
 			pstmt.setLong(3, rs.getLong("DATE"));
 			ResultSet rs2 = pstmt.executeQuery();
 
 			// if no original found, add WC ID to hash
 			if (!rs2.next() || rs2.getInt(1)==0)
 				hangingWcs.add(rs.getString("DATASET_ID"));
 		}
     	
 		for (int i=0; i<hangingWcs.size(); i++){
     		
 			String id = (String)hangingWcs.get(i);
 			
 			Parameters pars = new Parameters();
 			pars.addParameterValue("mode", "delete");
 			pars.addParameterValue("complete", "true");
 			pars.addParameterValue("ds_id", id);
 
 			DatasetHandler h = new DatasetHandler(conn, pars, ctx);
 			h.setUser(user);
 			h.setVersioning(false);
 			//h.execute();
 		}
     }
     
     /**
     *
     */
     public Vector getResponse(){
         return response;
     }
 
 	/**
 	*
 	*/
 	public void log(String msg){
 		if (ctx!=null)
 			ctx.log(msg);
 	}
 
     /**
     *
     */
     public static void main(String[] args){
     	
         MrProper mrProper = null;
         
         try{
             Class.forName("org.gjt.mm.mysql.Driver");
             Connection conn = DriverManager.getConnection(
 			"jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
 			//"jdbc:mysql://192.168.1.6:3306/DataDict", "dduser", "xxx");
 
 			AppUserIF testUser = new TestUser();
 			testUser.authenticate("jaanus", "jaanus");
 
             mrProper = new MrProper(conn);    
             mrProper.setUser(testUser);
             
             Parameters pars = new Parameters();
 			pars.addParameterValue(FUNCTIONS_PAR, MrProper.RLS_NOWC);
             //pars.addParameterValue(FUNCTIONS_PAR, RMV_MULT_VERS);
 			/*pars.addParameterValue("rm_obj_type", "dst");
 			pars.addParameterValue("rm_crit", "id");
 			pars.addParameterValue("rm_id", "1428 1222 1219 1220 1249 1429");*/
             
             mrProper.execute(pars);
             
             System.out.println(mrProper.getResponse());
         }
         catch (Exception e){
             System.out.println(e.toString());
         }
         finally{
         }
     }
 }
