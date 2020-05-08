 package eionet.meta;
 
 import java.sql.*;
 
 import javax.servlet.*;
 import java.util.*;
 
 import eionet.util.Log4jLoggerImpl;
 import eionet.util.LogServiceIF;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 import eionet.util.sql.INParameters;
 import eionet.util.sql.SQL;
 
 import com.tee.util.*;
 
 public class DDSearchEngine {
 	
 	public final static String ORDER_BY_M_ATTR_NAME  = "SHORT_NAME";
 	public final static String ORDER_BY_M_ATTR_DISP_ORDER  = "DISP_ORDER";
 	private final static String ELEMENT_TYPE  = "elm";
 	
 	private Connection conn = null;
 	private ServletContext ctx = null;
 	private String sessionID = "";
 	
 	private String rodObligUrl = "http://rod.eionet.eu.int/obligations/";
 	private String predIdentifier = "http://purl.org/dc/elements/1.1/identifier";
 	private String predTitle = "http://purl.org/dc/elements/1.1/title";
 	
 	private DDUser user = null;
 	
 	private static LogServiceIF logger = new Log4jLoggerImpl();
 	
 	/**
 	 * 
 	 * @param conn
 	 */
 	public DDSearchEngine(Connection conn){
 		this.conn = conn;
 		
 		String s = Props.getProperty(PropsIF.OUTSERV_ROD_OBLIG_URL);
 		if (s!=null && s.length()>0) rodObligUrl = s;
 		
 		s = Props.getProperty(PropsIF.OUTSERV_PRED_IDENTIFIER);
 		if (s!=null && s.length()>0) predIdentifier = s;
 		
 		s = Props.getProperty(PropsIF.OUTSERV_PRED_TITLE);
 		if (s!=null && s.length()>0) predTitle = s;
 	}
 	
 	public DDSearchEngine(Connection conn, String sessionID){
 		this(conn);
 		this.sessionID = sessionID;
 	}
 	
 	public DDSearchEngine(Connection conn, String sessionID, ServletContext ctx){
 		this(conn, sessionID);
 		this.ctx = ctx;
 	}
 	
 	public void setUser(DDUser user){
 		this.user = user;
 	}
 	
 	public DDUser getUser(){
 		return this.user;
 	}
 	
 	/**
 	 *
 	 */
 	public Vector getDataElements() throws SQLException {
 		return getDataElements(null, null, null, null);
 	}
 	
 	/**
 	 *
 	 */
 	public Vector getDataElements(Vector params,
 			String type,
 			String datasetIdf,
 			String short_name) throws SQLException {
 		return getDataElements(params, type, datasetIdf, short_name, null);
 	}
 	
 	/**
 	 * Get data elements by table id.
 	 * 5 inputs
 	 */
 	public Vector getDataElements(Vector unUsed1,
 			String unUsed2,
 			String unUsed3,
 			String unUsed4,
 			String tableID) throws SQLException {
 		
 		// make sure we have the tableID
 		if (Util.nullString(tableID))
 			throw new SQLException("getDataElements(): tableID is missing!");
 		
 		// build the monster query.
 		INParameters inPrms = new INParameters();
 		StringBuffer monsterQry = new StringBuffer().
 		append("select ").
 		append("distinct DATAELEM.*, TBL2ELEM.TABLE_ID, TBL2ELEM.POSITION, ").
 		append("DS_TABLE.TABLE_ID, DS_TABLE.IDENTIFIER, ").
 		append("DS_TABLE.SHORT_NAME, DS_TABLE.VERSION, ").
 		append("DATASET.DATASET_ID, DATASET.IDENTIFIER, DATASET.SHORT_NAME, ").
 		append("DATASET.VERSION, DATASET.REG_STATUS ").
 		append("from TBL2ELEM ").
 		append("left outer join DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID ").
 		append("left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID ").
 		append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ").
 		append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ").
 		append("where ").
 		append("TBL2ELEM.TABLE_ID=").append(inPrms.add(tableID, Types.INTEGER)).append(" and DATASET.DELETED is null and ").
 		append("DATAELEM.WORKING_COPY='N'"). // JH200505 - don't want working copies coming up here
 		append(" order by TBL2ELEM.POSITION asc");
 		
 		// log the monster query
 		logger.debug(monsterQry.toString());
 		
 		// prepare the statement for dynamic attributes
 		ResultSet attrsRs=null;
 		PreparedStatement attrsStmt=null;
 		StringBuffer attrsQry = new StringBuffer().
 		append("select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE ").
 		append("where ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID and ").
 		append("ATTRIBUTE.PARENT_TYPE='E' and ATTRIBUTE.DATAELEM_ID=?");
 		attrsStmt = conn.prepareStatement(attrsQry.toString());
 		
 		// finally execute the monster query
 		Vector result = new Vector();
 		PreparedStatement elemsStmt = null;
 		ResultSet elemsRs = null;
 		
 		int counter = 0;
 		
 		try{
 			elemsStmt = SQL.preparedStatement(monsterQry.toString(), inPrms, conn);
 			elemsRs = elemsStmt.executeQuery();
 			
 			
 			// process ResultSet
 			String curElmIdf = null;
 			while (elemsRs.next()){
 				
 				counter++;
 				
 				String elmIdf = elemsRs.getString("DATAELEM.IDENTIFIER");
 				if (elmIdf==null) continue;
 				
 				// the following if block skips non-latest ELEMENTS
 				if (curElmIdf!=null && elmIdf.equals(curElmIdf))
 					continue;
 				else
 					curElmIdf = elmIdf;
 				
 				// construct the element
 				int elmID = elemsRs.getInt("DATAELEM.DATAELEM_ID");
 				DataElement elm = new DataElement(String.valueOf(elmID),
 						elemsRs.getString("DATAELEM.SHORT_NAME"), elemsRs.getString("DATAELEM.TYPE"));
 				
 				elm.setIdentifier(elmIdf);
 				elm.setVersion(elemsRs.getString("DATAELEM.VERSION"));
 				elm.setWorkingCopy(elemsRs.getString("DATAELEM.WORKING_COPY"));
 				elm.setWorkingUser(elemsRs.getString("DATAELEM.WORKING_USER"));
 				elm.setTopNs(elemsRs.getString("DATAELEM.TOP_NS"));
 				elm.setGIS(elemsRs.getString("DATAELEM.GIS"));
 				elm.setRodParam(elemsRs.getBoolean("DATAELEM.IS_ROD_PARAM"));
 				elm.setTableID(elemsRs.getString("TBL2ELEM.TABLE_ID"));
 				elm.setPositionInTable(elemsRs.getString("TBL2ELEM.POSITION"));
 				elm.setDatasetID(elemsRs.getString("DATASET.DATASET_ID"));
 				elm.setDstShortName(elemsRs.getString("DATASET.SHORT_NAME"));
 				elm.setTblShortName(elemsRs.getString("DS_TABLE.SHORT_NAME"));
 				elm.setTblIdentifier(elemsRs.getString("DS_TABLE.IDENTIFIER"));
 				elm.setDstIdentifier(elemsRs.getString("DATASET.IDENTIFIER"));
 				elm.setNamespace(new Namespace(
 						elemsRs.getString("DATAELEM.PARENT_NS"), "", "", "", ""));
 				elm.setCheckedoutCopyID(elemsRs.getString("DATAELEM.CHECKEDOUT_COPY_ID"));
 				elm.setDate(elemsRs.getString("DATAELEM.DATE"));
 				
 				// execute the statement prepared for dynamic attributes
 				attrsStmt.setInt(1, elmID);
 				attrsRs = attrsStmt.executeQuery();
 				while (attrsRs.next()){
 					String attrID = attrsRs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID");
 					DElemAttribute attr = elm.getAttributeById(attrID);
 					if (attr==null){
 						attr = new DElemAttribute(attrID,
 								attrsRs.getString("M_ATTRIBUTE.NAME"),
 								attrsRs.getString("M_ATTRIBUTE.SHORT_NAME"),
 								DElemAttribute.TYPE_SIMPLE,
 								attrsRs.getString("ATTRIBUTE.VALUE"),
 								attrsRs.getString("M_ATTRIBUTE.DEFINITION"),
 								attrsRs.getString("M_ATTRIBUTE.OBLIGATION"),
 								attrsRs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
 						elm.addAttribute(attr);
 					}
 					else
 						attr.addValue(attrsRs.getString("ATTRIBUTE.VALUE"));
 				}
 				
 				// add the element to the result Vector
 				result.add(elm);
 			}
 		}
 		finally{
 			try{
 				if (elemsRs != null) elemsRs.close();
 				if (attrsRs != null) attrsRs.close();
 				if (elemsStmt != null) elemsStmt.close();
 				if (attrsStmt != null) attrsStmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Get data elements by table id and dataset id
 	 * 6 inputs
 	 */
 	public Vector getDataElements(Vector params,
 			String type,
 			String datasetIdf,
 			String short_name,
 			String tableID,
 			String datasetID) throws SQLException {
 		
 		return getDataElements(params, type, datasetIdf, short_name,
 				tableID, datasetID, false);
 	}
 	
 	/**
 	 * Get data elements with control over working copies
 	 * 7 inputs
 	 */
 	public Vector getDataElements(Vector params,
 			String type,
 			String datasetIdf,
 			String short_name,
 			String tableID,
 			String datasetID,
 			boolean wrkCopies) throws SQLException {
 		
 		return getDataElements(params, type, datasetIdf, short_name,
 				tableID, datasetID, wrkCopies, null);
 	}
 	
 	/**
 	 * Get data elements, control over working copies & params oper
 	 * 8 inputs
 	 */
 	public Vector getDataElements(Vector params,
 			String type,
 			String datasetIdf,
 			String short_name,
 			String tableID,
 			String datasetID,
 			boolean wrkCopies,
 			String oper) throws SQLException {
 		return getDataElements(params, type, datasetIdf,
 				short_name, null, tableID, datasetID, wrkCopies, null);
 	}
 	
 	/**
 	 * Get data elements, control over working copies & params oper
 	 * 9 inputs
 	 */
 	public Vector getDataElements(Vector params,
 			String type,
 			String datasetIdf,
 			String short_name,
 			String idfier,
 			String tableID,
 			String datasetID,
 			boolean wrkCopies,
 			String oper) throws SQLException {
 		
 		return getDataElements(
 				params, type, datasetIdf, short_name, idfier, tableID, datasetID, wrkCopies, false, oper);
 	}
 	/**
 	 * Get data elements, control over working copies, historic versions & params oper
 	 * 10 inputs
 	 */
 	public Vector getDataElements(Vector params,
 			String type,
 			String datasetIdf,
 			String short_name,
 			String idfier,
 			String tableID,
 			String datasetID,
 			boolean wrkCopies,
 			boolean isIncludeHistoricVersions,
 			String oper) throws SQLException {
 		
 		// set up the IN parameters for the upcoming PreparedStatement
 		INParameters inPrms = new INParameters();
 		
 		// oper defines the search precision. If it's missing, set it to substring search
 		if (oper==null)
 			oper=" like ";
 		
 		// build the "from" part of the SQL query
 		StringBuffer tables = new StringBuffer("DATAELEM, TBL2ELEM, DS_TABLE, DST2TBL, DATASET");
 		
 		// start building the "where" part of the SQL query 
 		StringBuffer constraints = new StringBuffer();
 		
 		// join the "from tables"
 		constraints.append("DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID and ").
 		append("TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID and ").
 		append("DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID and ").
 		append("DST2TBL.DATASET_ID=DATASET.DATASET_ID and ");
 		
 		// we look only in non-deleted datasets
 		constraints.append("DATASET.DELETED is null");
 		// if not looking in a concrete dataset copy, take into account the working copies parameter
 		if (datasetID==null){
 			if (wrkCopies)
 				constraints.append(" and DATASET.WORKING_COPY='Y'");
 			else
 				constraints.append(" and DATASET.WORKING_COPY='N'");
 		}
 		// we look only for non-common elements here, so DATAELEM.PARENT_NS must NOT be null
 		constraints.append(" and DATAELEM.PARENT_NS is not null");
 		
 		// set the element type (CH1 or CH2)
 		if (type!=null && type.length()!=0){
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("DATAELEM.TYPE=").append(inPrms.add(type));
 		}
 		
 		// set the element short name
 		if (short_name!=null && short_name.length()!=0){
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("DATAELEM.SHORT_NAME");
 			// short name is not fulltext-indexed, so force "match" to "like"
 			if (oper.trim().equalsIgnoreCase("match"))
 				oper=" like ";
 			constraints.append(oper);
 			if (oper.trim().equalsIgnoreCase("like"))
 				constraints.append(inPrms.add("%" + short_name + "%"));
 			else
 				constraints.append(inPrms.add(short_name));
 		}
 		
 		// set the element identifier
 		if (idfier!=null && idfier.length()!=0){
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("DATAELEM.IDENTIFIER");
 			// identifier is not fulltext-indexed, so force "match" to "like"
 			if (oper.trim().equalsIgnoreCase("match"))
 				oper=" like ";
 			constraints.append(oper);
 			if (oper.trim().equalsIgnoreCase("like"))
 				constraints.append(inPrms.add("%" + idfier + "%"));
 			else
 				constraints.append(inPrms.add(idfier));
 		}
 		
 		// see if looking for elements of a concrete table 
 		if (tableID!=null && tableID.length()!=0){
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("TBL2ELEM.TABLE_ID=").append(inPrms.add(tableID, Types.INTEGER));
 		}
 		
 		// see if looking for elements of a concrete dataset copy
 		if (datasetID!=null && datasetID.length()!=0){
 			if (constraints.length()!=0) constraints.append(" and ");
 			if (datasetID.equals("-1"))
 				constraints.append("DATASET.DATASET_ID IS NULL");
 			else
 				constraints.append("DATASET.DATASET_ID=").append(inPrms.add(datasetID, Types.INTEGER));
 		}
 		
 		// see if looking for elements of datasets with a concrete identifier
 		if (datasetIdf!=null && datasetIdf.length()!=0){
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("DATASET.IDENTIFIER=").append(inPrms.add(datasetIdf));
 		}
 		
 		// the loop for processing dynamic search parameters
 		for (int i=0; params!=null && i<params.size(); i++){
 			
 			String index = String.valueOf(i+1);
 			DDSearchParameter param = (DDSearchParameter)params.get(i);
 			String attrID  = param.getAttrID();
 			Vector attrValues = param.getAttrValues();
 			String valueOper = param.getValueOper();
 			String idOper = param.getIdOper();
 			// Deal with the "from" part. For each dynamic attribute,
 			// create an alias to the ATTRIBUTE table
 			tables.append(", ATTRIBUTE as ATTR" + index);
 			
 			// deal with the where part
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("ATTR").append(index).append(".M_ATTRIBUTE_ID").
 			append(idOper).append(inPrms.add(attrID, Types.INTEGER)).append(" and ");
 			
 			// concatenate the searched values with "or"
 			if (attrValues!=null && attrValues.size()!=0){
 				constraints.append("(");
 				for (int j=0; j<attrValues.size(); j++){
 					if (j>0) constraints.append(" or ");
 					if (valueOper!= null && valueOper.trim().equalsIgnoreCase("MATCH")){
 						constraints.append("match(ATTR").append(index).
 						append(".VALUE) against(").append(inPrms.add(attrValues.get(j), Types.VARCHAR)).append(")");
 					}
 					else
 						constraints.append("ATTR").append(index).append(".VALUE").
 						append(valueOper).append(inPrms.add(attrValues.get(j), Types.VARCHAR));
 				}
 				constraints.append(")");
 			}
 			// join alias'ed ATTRIBUTE tables with DATAELEM
 			constraints.append(" and ").
 			append("ATTR").append(index).append(".DATAELEM_ID=DATAELEM.DATAELEM_ID and ").
 			append("ATTR").append(index).append(".PARENT_TYPE='E'");
 		}
 		// end of dynamic parameters loop
 		
 		// compile the query
 		StringBuffer monsterQry = new StringBuffer().
 		append("select distinct DATAELEM.*, TBL2ELEM.TABLE_ID, TBL2ELEM.POSITION, ").
 		append("DS_TABLE.TABLE_ID, DS_TABLE.IDENTIFIER, ").
 		append("DS_TABLE.SHORT_NAME, DS_TABLE.VERSION, ").
 		append("DATASET.DATASET_ID, DATASET.IDENTIFIER, DATASET.SHORT_NAME, DATASET.WORKING_USER, ").
 		append("DATASET.VERSION, DATASET.REG_STATUS from ").append(tables.toString());
 		if (constraints.length()!=0)
 			monsterQry.append(" where ").append(constraints.toString());
 		if (tableID!=null && tableID.length()!=0)
 			monsterQry.append(" order by TBL2ELEM.POSITION");
 		else
 			monsterQry.append(" order by ").
 			append("DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc, ").
 			append("DS_TABLE.IDENTIFIER asc, DS_TABLE.TABLE_ID desc, ").
 			append("DATAELEM.IDENTIFIER asc, DATAELEM.DATAELEM_ID desc");
 		
 		logger.debug(monsterQry.toString());
 		
 		// see if dynamic attributes of elements should be fetched and if so,
 		// prepare the relevant statement
 		ResultSet attrsRs=null;
 		PreparedStatement attrsStmt=null;
 		boolean getAttributes = Util.nullString(tableID) ? false : true;
 		if (getAttributes){
 			StringBuffer attrsQry = new StringBuffer().
 			append("select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE ").
 			append("where ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID and ").
 			append("ATTRIBUTE.PARENT_TYPE='E' and ATTRIBUTE.DATAELEM_ID=?");
 			attrsStmt = conn.prepareStatement(attrsQry.toString());
 		}
 		
 		// finally execute the monster query
 		Vector result = new Vector();
 		PreparedStatement elemsStmt = null;
 		ResultSet elemsRs = null;
 		try{
 			elemsStmt = SQL.preparedStatement(monsterQry.toString(), inPrms, conn);
 			elemsRs = elemsStmt.executeQuery();
 			
 			String curDstID = null;
 			String curDstIdf = null;
 			String curTblID = null;
 			String curTblIdf = null;
 			String curElmIdf = null;
 			
 			// process ResultSet
 			while (elemsRs.next()){
 				
 				String dstID  = elemsRs.getString("DATASET.DATASET_ID");
 				String dstIdf = elemsRs.getString("DATASET.IDENTIFIER");
 				if (dstID==null || dstIdf==null) continue;
 				
 				// the following if block skips elements from non-latest DATASETS
 				if (curDstIdf==null || !curDstIdf.equals(dstIdf)){
 					curDstID  = dstID;
 					curDstIdf = dstIdf;
 				}
 				else if (!isIncludeHistoricVersions){
 					if (!curDstID.equals(dstID))
 						continue;
 				}
 				
 				String tblID  = elemsRs.getString("DS_TABLE.TABLE_ID");
 				String tblIdf = elemsRs.getString("DS_TABLE.IDENTIFIER");
 				// skip non-existing tables, ie trash from some erroneous situation
 				if (tblID==null || tblIdf==null)
 					continue;
 				
 				int elmID = elemsRs.getInt("DATAELEM.DATAELEM_ID");
 				String elmIdf = elemsRs.getString("DATAELEM.IDENTIFIER");
 				// skip non-existing elements, ie trash from some erroneous situation
 				if (elmIdf==null)
 					continue;
 				
 				// construct the element object
 				DataElement elm = new DataElement(String.valueOf(elmID),
 						elemsRs.getString("DATAELEM.SHORT_NAME"), elemsRs.getString("DATAELEM.TYPE"));
 				elm.setIdentifier(elemsRs.getString("DATAELEM.IDENTIFIER"));
 				elm.setVersion(elemsRs.getString("DATAELEM.VERSION"));
 				elm.setWorkingCopy(elemsRs.getString("DATAELEM.WORKING_COPY"));
 				elm.setWorkingUser(elemsRs.getString("DATAELEM.WORKING_USER"));
 				elm.setTopNs(elemsRs.getString("DATAELEM.TOP_NS"));
 				elm.setGIS(elemsRs.getString("DATAELEM.GIS"));
 				elm.setRodParam(elemsRs.getBoolean("DATAELEM.IS_ROD_PARAM"));
 				elm.setDate(elemsRs.getString("DATAELEM.DATE"));
 				elm.setTableID(elemsRs.getString("TBL2ELEM.TABLE_ID"));
 				elm.setPositionInTable(elemsRs.getString("TBL2ELEM.POSITION"));
 				elm.setDatasetID(elemsRs.getString("DATASET.DATASET_ID"));
 				elm.setDstShortName(elemsRs.getString("DATASET.SHORT_NAME"));
 				elm.setTblShortName(elemsRs.getString("DS_TABLE.SHORT_NAME"));
 				elm.setDstIdentifier(elemsRs.getString("DATASET.IDENTIFIER"));
 				elm.setTblIdentifier(elemsRs.getString("DS_TABLE.IDENTIFIER"));
 				elm.setNamespace(new Namespace(
 						elemsRs.getString("DATAELEM.PARENT_NS"), "", "", "", ""));
 				elm.setCheckedoutCopyID(elemsRs.getString("DATAELEM.CHECKEDOUT_COPY_ID"));
 				elm.setDstWorkingUser(elemsRs.getString("DATASET.WORKING_USER"));
 				elm.setDstStatus(elemsRs.getString("DATASET.REG_STATUS"));
 				
 				// if attributes should be fetched, execute the relevant statement
 				if (getAttributes){
 					attrsStmt.setInt(1, elmID);
 					attrsRs = attrsStmt.executeQuery();
 					while (attrsRs.next()){
 						String attrID = attrsRs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID");
 						DElemAttribute attr = elm.getAttributeById(attrID);
 						if (attr==null){
 							attr = new DElemAttribute(attrID,
 									attrsRs.getString("M_ATTRIBUTE.NAME"),
 									attrsRs.getString("M_ATTRIBUTE.SHORT_NAME"),
 									DElemAttribute.TYPE_SIMPLE,
 									attrsRs.getString("ATTRIBUTE.VALUE"),
 									attrsRs.getString("M_ATTRIBUTE.DEFINITION"),
 									attrsRs.getString("M_ATTRIBUTE.OBLIGATION"),
 									attrsRs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
 							elm.addAttribute(attr);
 						}
 						else
 							attr.addValue(attrsRs.getString("ATTRIBUTE.VALUE"));
 					}
 				} 
 				
 				// add the element object to the result Vector
 				result.add(elm);
 			}
 		}
 		finally{
 			try{
 				if (elemsRs != null) elemsRs.close();
 				if (attrsRs != null) attrsRs.close();
 				if (elemsStmt != null) elemsStmt.close();
 				if (attrsStmt != null) attrsStmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param params
 	 * @param type
 	 * @param short_name
 	 * @param idfier
 	 * @param wrkCopies
 	 * @param oper
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getCommonElements(Vector params,
 			String type,
 			String short_name,
 			String idfier,
 			boolean wrkCopies,
 			String oper) throws SQLException {
 		return getCommonElements(params, type, short_name, idfier, wrkCopies, false, oper);
 	}
 
 	/**
 	 * 
 	 * @param params
 	 * @param type
 	 * @param short_name
 	 * @param idfier
 	 * @param wrkCopies
 	 * @param isIncludeHistoricVersions
 	 * @param oper
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getCommonElements(Vector params,
 			String type,
 			String short_name,
 			String idfier,
 			boolean wrkCopies,
 			boolean isIncludeHistoricVersions,
 			String oper) throws SQLException {
 		
 		return getCommonElements(params, type, short_name, idfier, null, wrkCopies, false, oper);
 	}
 	
 	/**
 	 * 
 	 * @param params
 	 * @param type
 	 * @param short_name
 	 * @param idfier
 	 * @param regStatus
 	 * @param wrkCopies
 	 * @param isIncludeHistoricVersions
 	 * @param oper
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getCommonElements(Vector params,
 			String type,
 			String short_name,
 			String idfier,
 			String regStatus,
 			boolean wrkCopies,
 			boolean isIncludeHistoricVersions,
 			String oper) throws SQLException {
 		
 		Vector result = new Vector();
 		
 		// set up the IN parameters for the up-coming query
 		INParameters inParams = new INParameters();
 		
 		// oper defines the search precision. If it's missing, set it to substring search
 		if (oper==null) oper=" like ";
 		
 		// set up the "from" part of the SQL query
 		StringBuffer tables = new StringBuffer("DATAELEM");
 		
 		// start building the "where" part of the SQL query 
 		StringBuffer constraints = new StringBuffer();
 		
 		// we look only for common elements here, so DATAELEM.PARENT_NS must be null
 		constraints.append("DATAELEM.PARENT_NS is null");
 		
 		// set the registration status
 		if (regStatus!=null && regStatus.trim().length()>0){
 			if (constraints.length()>0)
 				constraints.append(" and ");
 			constraints.append("DATAELEM.REG_STATUS=").append(inParams.add(regStatus));
 		}
 		
 		// set the element type (CH1 or CH2)
 		if (type!=null && type.length()!=0){
 			if (constraints.length()!=0) constraints.append(" and ");
 			constraints.append("DATAELEM.TYPE=").append(inParams.add(type));
 		}
 		
 		// set the element short name
 		if (short_name!=null && short_name.length()!=0){
 			
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("DATAELEM.SHORT_NAME");
 			
 			// SHORT_NAME is not fulltext indexed, so force "match" to "like"
 			if (oper.trim().equalsIgnoreCase("match"))
 				oper=" like ";
 			constraints.append(oper);
 			
 			if (oper.trim().equalsIgnoreCase("like"))
 				constraints.append(inParams.add("%" + short_name + "%"));
 			else
 				constraints.append(inParams.add(short_name));
 		}
 		
 		// set the element identifier
 		if (idfier!=null && idfier.length()!=0){
 			
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("DATAELEM.IDENTIFIER");
 			
 			// IDENTIFIER is not fulltext indexed, so force "match" to "like"
 			if (oper.trim().equalsIgnoreCase("match"))
 				oper=" like ";
 			constraints.append(oper);
 			
 			if (oper.trim().equalsIgnoreCase("like"))
 				constraints.append(inParams.add("%" + idfier + "%"));
 			else
 				constraints.append(inParams.add(idfier));
 		}
 		
 		// the loop for processing dynamic search parameters
 		for (int i=0; params!=null && i<params.size(); i++){
 			
 			String index = String.valueOf(i+1);
 			DDSearchParameter param = (DDSearchParameter)params.get(i);
 			
 			String attrID  = param.getAttrID();
 			Vector attrValues = param.getAttrValues();
 			String valueOper = param.getValueOper();
 			String idOper = param.getIdOper();
 			
 			// Deal with the "from" part. For each dynamic attribute,
 			// create an alias to the ATTRIBUTE table
 			tables.append(", ATTRIBUTE as ATTR" + index);
 			
 			// deal with the where part
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			
 			constraints.append("ATTR").append(index).append(".M_ATTRIBUTE_ID").
 			append(idOper).append(inParams.add(attrID, Types.INTEGER)).append(" and ");
 			
 			// concatenate the searched values with "or"
 			if (attrValues!=null && attrValues.size()!=0){
 				constraints.append("(");
 				for (int j=0; j<attrValues.size(); j++){
 					if (j>0) constraints.append(" or ");
 					if (valueOper!= null && valueOper.trim().equalsIgnoreCase("MATCH")){
 						constraints.append("match(ATTR").append(index).
 						append(".VALUE) against(").append(inParams.add(attrValues.get(j))).append(")");
 					}
 					else
 						constraints.append("ATTR").append(index).append(".VALUE").
 						append(valueOper).append(inParams.add(attrValues.get(j)));
 				}
 				constraints.append(")");
 			}
 			
 			// join alias'ed ATTRIBUTE tables with DATAELEM
 			constraints.append(" and ").
 			append("ATTR").append(index).append(".DATAELEM_ID=DATAELEM.DATAELEM_ID and ").
 			append("ATTR").append(index).append(".PARENT_TYPE='E'");
 		}
 		// end of dynamic parameters loop
 		
 		// if the user is missing, override the wrkCopies argument
 		if (user==null)
 			wrkCopies = false;
 		if (constraints.length()!=0)
 			constraints.append(" and ");
 		if (wrkCopies){
 			constraints.append("DATAELEM.WORKING_COPY='Y' and DATAELEM.WORKING_USER=").
 			append(inParams.add(user.getUserName()));
 		}
 		else
 			constraints.append("DATAELEM.WORKING_COPY='N'");
 		
 		// now build the monster query.
 		// first the "select from" part
 		StringBuffer monsterQry = new StringBuffer().
 		append("select distinct DATAELEM.* from ").append(tables.toString());
 		
 		// then the "where part"
 		if (constraints.length()!=0)
 			monsterQry.append(" where ").append(constraints.toString());
 		
 		// finally the "order by"
 		monsterQry.append(" order by DATAELEM.IDENTIFIER asc, DATAELEM.DATAELEM_ID desc");
 		
 		// log the monster query
 		logger.debug(monsterQry.toString());
 		
 		// prepare the statement for fecthing the elements' dynamic attributes
 		ResultSet attrsRs=null;
 		PreparedStatement attrsStmt=null;
 		StringBuffer attrsQry = new StringBuffer().
 		append("select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE ").
 		append("where ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID and ").
 		append("ATTRIBUTE.PARENT_TYPE='E' and ATTRIBUTE.DATAELEM_ID=?");
 		attrsStmt = conn.prepareStatement(attrsQry.toString());
 		
 		// finally execute the monster query
 		PreparedStatement elemsStmt = null;
 		ResultSet elemsRs = null;
 		try{
 			elemsStmt = SQL.preparedStatement(monsterQry.toString(), inParams, conn);
 			elemsRs = elemsStmt.executeQuery();
 			
 			String curElmIdf = null;
 			
 			// process ResultSet
 			while (elemsRs.next()){
 				
 				String elmIdf = elemsRs.getString("DATAELEM.IDENTIFIER");
 				if (elmIdf==null) continue;
 				
 				// the following if block skips non-latest
 				if (curElmIdf!=null && elmIdf.equals(curElmIdf)){
 					if (!isIncludeHistoricVersions)
 						continue;
 				}
 				else
 					curElmIdf = elmIdf;
 				
 				// construct the element
 				int elmID = elemsRs.getInt("DATAELEM.DATAELEM_ID");
 				
 				DataElement elm = new DataElement( String.valueOf(elmID),
 						elemsRs.getString("DATAELEM.SHORT_NAME"), elemsRs.getString("DATAELEM.TYPE"));
 				
 				elm.setIdentifier(elemsRs.getString("DATAELEM.IDENTIFIER"));
 				elm.setVersion(elemsRs.getString("DATAELEM.VERSION"));
 				elm.setStatus(elemsRs.getString("DATAELEM.REG_STATUS"));
 				elm.setWorkingCopy(elemsRs.getString("DATAELEM.WORKING_COPY"));
 				elm.setWorkingUser(elemsRs.getString("DATAELEM.WORKING_USER"));
 				elm.setGIS(elemsRs.getString("DATAELEM.GIS"));
 				elm.setRodParam(elemsRs.getBoolean("DATAELEM.IS_ROD_PARAM"));
 				elm.setDate(elemsRs.getString("DATAELEM.DATE"));
 				
 				// fetche the element's dynamic attributes
 				attrsStmt.setInt(1, elmID);
 				attrsRs = attrsStmt.executeQuery();
 				while (attrsRs.next()){
 					String attrID = attrsRs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID");
 					DElemAttribute attr = elm.getAttributeById(attrID);
 					if (attr==null){
 						attr = new DElemAttribute(attrID,
 								attrsRs.getString("M_ATTRIBUTE.NAME"),
 								attrsRs.getString("M_ATTRIBUTE.SHORT_NAME"),
 								DElemAttribute.TYPE_SIMPLE,
 								attrsRs.getString("ATTRIBUTE.VALUE"),
 								attrsRs.getString("M_ATTRIBUTE.DEFINITION"),
 								attrsRs.getString("M_ATTRIBUTE.OBLIGATION"),
 								attrsRs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
 						elm.addAttribute(attr);
 					}
 					else
 						attr.addValue(attrsRs.getString("ATTRIBUTE.VALUE"));
 				}
 				
 				// add the element to the result Vector
 				result.add(elm);
 			}
 		}
 		finally{
 			try{
 				if (elemsRs != null) elemsRs.close();
 				if (attrsRs != null) attrsRs.close();
 				if (elemsStmt != null) elemsStmt.close();
 				if (attrsStmt != null) attrsStmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param idf
 	 * @param statuses
 	 * @return
 	 * @throws SQLException
 	 */
 	public String getLatestElmID(String idf, Vector statuses) throws SQLException{
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf = new StringBuffer();
 		buf.append("select DATAELEM_ID from DATAELEM where WORKING_COPY='N'");
 		buf.append(" and PARENT_NS is null and IDENTIFIER=").append(inParams.add(idf));
 		if (statuses!=null && statuses.size()>0){
 			buf.append(" and (");
 			for (int i=0; i<statuses.size(); i++){
 				if (i>0) buf.append(" or ");
 				buf.append("REG_STATUS=").append(inParams.add((String)statuses.get(i)));
 			}
 			buf.append(")");
 		}
 		buf.append(" order by DATAELEM_ID desc limit 0,1");
 		
 		String elmID = null;
 		ResultSet rs = null;
 		PreparedStatement stmt = null;		
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs.next())
 				return rs.getString(1);
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) rs.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param idf
 	 * @param statuses
 	 * @return
 	 * @throws SQLException
 	 */
 	public DataElement getLatestElm(String idf, Vector statuses) throws SQLException {
 		String latestID = getLatestElmID(idf, statuses);
 		return latestID==null ? null : getDataElement(latestID);
 	}
 	
 	/**
 	 * 
 	 * @param idf
 	 * @return
 	 * @throws SQLException
 	 */
 	public DataElement getLatestElm(String idf) throws SQLException {
 		return getLatestElm(idf);
 	}
 	
 	/*
 	 * idf - identifier
 	 * pns - parent ns
 	 */
 	public DsTable getLatestTbl(String idf, String pns) throws SQLException {
 		
 		VersionManager verMan = new VersionManager(conn, user);
 		DsTable tbl = new DsTable(null, null, null);
 		tbl.setIdentifier(idf);
 		if (!Util.nullString(pns))
 			tbl.setParentNs(pns);
 		
 		String latestID = verMan.getLatestTblID(tbl);
 		if (Util.nullString(latestID))
 			return null;
 		else
 			return getDatasetTable(latestID);
 	}
 	
 	/**
 	 * 
 	 * @param idf
 	 * @param statuses
 	 * @return
 	 * @throws SQLException
 	 */
 	public String getLatestDstID(String idf, Vector statuses) throws SQLException{
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf = new StringBuffer();
 		buf.append("select DATASET_ID from DATASET where WORKING_COPY='N'");
 		buf.append(" and DELETED is null and IDENTIFIER=").append(inParams.add(idf));
 		if (statuses!=null && statuses.size()>0){
 			buf.append(" and (");
 			for (int i=0; i<statuses.size(); i++){
 				if (i>0) buf.append(" or ");
 				buf.append("REG_STATUS=").append(inParams.add((String)statuses.get(i)));
 			}
 			buf.append(")");
 		}
 		buf.append(" order by DATASET_ID desc limit 0,1");
 		
 		String dstID = null;
 		ResultSet rs = null;
 		PreparedStatement stmt = null;
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs.next())
 				return rs.getString(1);
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) rs.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param idf
 	 * @param statuses
 	 * @return
 	 * @throws SQLException
 	 */
 	public Dataset getLatestDst(String idf, Vector statuses) throws SQLException{
 		String latestID = getLatestDstID(idf, statuses);
 		return latestID==null ? null : getDataset(latestID);
 	}
 	
 	/**
 	 * 
 	 * @param idf
 	 * @return
 	 * @throws SQLException
 	 */
 	public Dataset getLatestDst(String idf) throws SQLException {
 		return getLatestDst(idf, null);
 	}
 	
 	/*
 	 * 
 	 */
 	public DataElement getDataElement(String elmID) throws SQLException {
 		return getDataElement(elmID, null);
 	}
 	
 	/*
 	 * 
 	 */
 	public DataElement getDataElement(String elmID, String tblID)
 	throws SQLException {
 		return getDataElement(elmID, tblID, true);
 	}
 	
 	/**
 	 * 
 	 * @param elmID
 	 * @param tblID
 	 * @param inheritAttrs
 	 * @return
 	 * @throws SQLException
 	 */
 	public DataElement getDataElement(String elmID, String tblID, boolean inheritAttrs) throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer qry = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		DataElement elm = null;
 		try{
 			// first find out if this is a common element
 			qry = new StringBuffer("select * from DATAELEM where DATAELEM_ID=").append(inParams.add(elmID, Types.INTEGER));
 			stmt = SQL.preparedStatement(qry.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (!rs.next())
 				return null;
 			boolean elmCommon = Util.nullString(rs.getString("PARENT_NS"));
 			
 			// Build the query which takes into account the tblID.
 			// If the latter is null then take the table which is latest in history,
 			// otherwise take exactly the table wanted by tblID
 			inParams = new INParameters();
 			qry = new StringBuffer("select DATAELEM.*");
 			if (!elmCommon){
 				qry.append(", TBL2ELEM.POSITION, DS_TABLE.TABLE_ID, DS_TABLE.IDENTIFIER, ").
 				append("DS_TABLE.SHORT_NAME, DS_TABLE.VERSION, DATASET.DATASET_ID, ").
 				append("DATASET.IDENTIFIER, DATASET.SHORT_NAME, DATASET.VERSION");
 			}
 			qry.append(" from DATAELEM");
 			if (!elmCommon){
 				qry.
 				append(" left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID ").
 				append("left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID ").
 				append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ").
 				append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID");
 			}
 			qry.append(" where DATAELEM.DATAELEM_ID=").append(inParams.add(elmID, Types.INTEGER));
 			if (!elmCommon && !Util.nullString(tblID))
 				qry.append(" and DS_TABLE.TABLE_ID=").append(inParams.add(tblID, Types.INTEGER));
 			
 			qry.append(" order by ").
 			append("DATAELEM.DATAELEM_ID desc");
 			if (!elmCommon)
 				qry.append(", DS_TABLE.TABLE_ID desc, DATASET.DATASET_ID desc");
 			
 			logger.debug(qry.toString());
 			
 			// execute the query
 			stmt = SQL.preparedStatement(qry.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs.next()){
 				elm = new DataElement(rs.getString("DATAELEM.DATAELEM_ID"),
 						rs.getString("DATAELEM.SHORT_NAME"),
 						rs.getString("DATAELEM.TYPE"));
 				
 				elm.setIdentifier(rs.getString("DATAELEM.IDENTIFIER"));
 				elm.setVersion(rs.getString("DATAELEM.VERSION"));
 				elm.setStatus(rs.getString("DATAELEM.REG_STATUS"));
 				elm.setTopNs(rs.getString("DATAELEM.TOP_NS"));
 				elm.setGIS(rs.getString("DATAELEM.GIS"));
 				elm.setRodParam(rs.getBoolean("DATAELEM.IS_ROD_PARAM"));
 				elm.setWorkingCopy(rs.getString("DATAELEM.WORKING_COPY"));
 				elm.setWorkingUser(rs.getString("DATAELEM.WORKING_USER"));
 				elm.setUser(rs.getString("DATAELEM.USER"));
 				elm.setCheckedoutCopyID(rs.getString("DATAELEM.CHECKEDOUT_COPY_ID"));
 				elm.setDate(rs.getString("DATAELEM.DATE"));
 				
 				elm.setNamespace(
 						new Namespace(rs.getString("DATAELEM.PARENT_NS"), null, null, null, null));
 				
 				if (!elmCommon){
 					elm.setTableID(rs.getString("DS_TABLE.TABLE_ID"));
 					elm.setDatasetID(rs.getString("DATASET.DATASET_ID"));
 					elm.setDstShortName(rs.getString("DATASET.SHORT_NAME"));
 					elm.setTblShortName(rs.getString("DS_TABLE.SHORT_NAME"));
 					elm.setDstIdentifier(rs.getString("DATASET.IDENTIFIER"));
 					elm.setTblIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));
 					elm.setPositionInTable(rs.getString("TBL2ELEM.POSITION"));
 				}
 				
 				Vector attributes = !elmCommon && inheritAttrs ?
 						getSimpleAttributes(elmID, "E", elm.getTableID(), elm.getDatasetID()) :
 							getSimpleAttributes(elmID, "E");
 						
 						elm.setAttributes(attributes);
 			}
 			else
 				return null;
 		}
 		finally {
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return elm;
 	}
 	
 	public Vector getDElemAttributes() throws SQLException {
 		return getDElemAttributes(null,null,null);
 	}
 	
 	public Vector getDElemAttributes(String type) throws SQLException {
 		return getDElemAttributes(null,type,null);
 	}
 	
 	public Vector getDElemAttributes(String attr_id, String type) throws SQLException {
 		return getDElemAttributes(attr_id, type, null);
 	}
 	public Vector getDElemAttributes(String attr_id, String type, String orderBy) throws SQLException {
 		return getDElemAttributes(attr_id, type, orderBy, null);
 	}
 	
 	public Vector getDElemAttributes(String attr_id, String type, String orderBy, String inheritable) throws SQLException {
 		
 		if (type==null)
 			type = DElemAttribute.TYPE_SIMPLE;
 		
 		INParameters inParams = new INParameters();
 		StringBuffer qry=new StringBuffer();
 		if (type.equals(DElemAttribute.TYPE_SIMPLE)){
 			qry.append("select distinct M_ATTRIBUTE_ID as ID, M_ATTRIBUTE.* from M_ATTRIBUTE");
 			if (attr_id != null)
 				qry.append(" where M_ATTRIBUTE_ID=");
 		}
 		else{
 			qry.append("select distinct M_COMPLEX_ATTR_ID as ID, M_COMPLEX_ATTR.* from M_COMPLEX_ATTR");
 			if (attr_id != null)
 				qry.append(" where M_COMPLEX_ATTR_ID=");
 		}
 		if (attr_id != null)
 			qry.append(inParams.add(attr_id, Types.INTEGER));
 		
 		if (inheritable!=null){
 			if (attr_id!=null)
 				qry.append(" AND ");
 			if (attr_id==null)
 				qry.append(" WHERE ");
 			qry.append("INHERIT=").append(inParams.add(inheritable));
 		}
 		
 		if (orderBy == null)
 			orderBy = ORDER_BY_M_ATTR_NAME;
 		qry.append(" order by ");
 		qry.append(orderBy);
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Vector v = new Vector();
 		
 		try{
 			stmt = SQL.preparedStatement(qry.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			while (rs.next()){
 				DElemAttribute attr = new DElemAttribute(rs.getString("ID"),
 						rs.getString("NAME"),
 						rs.getString("SHORT_NAME"),
 						type,
 						null,
 						rs.getString("DEFINITION"),
 						rs.getString("OBLIGATION"));
 				
 				Namespace ns = getNamespace(rs.getString("NAMESPACE_ID"));
 				if (ns!=null)
 					attr.setNamespace(ns);
 				
 				if (type.equals(DElemAttribute.TYPE_SIMPLE)){
 					attr.setDisplayProps(rs.getString("DISP_TYPE"),
 							rs.getInt("DISP_ORDER"),
 							rs.getInt("DISP_WHEN"),
 							rs.getString("DISP_WIDTH"),
 							rs.getString("DISP_HEIGHT"),
 							rs.getString("DISP_MULTIPLE"));
 				}
 				else{
 					attr.setDisplayProps(null,
 							rs.getInt("DISP_ORDER"),
 							rs.getInt("DISP_WHEN"),
 							null,
 							null,
 							null);
 					attr.setHarvesterID(rs.getString("HARVESTER_ID"));
 				}
 				
 				attr.setInheritable(rs.getString("INHERIT"));
 				
 				v.add(attr);
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return v;
 	}
 	
 	public Vector getFixedValues(String delem_id) throws SQLException {
 		return getFixedValues(delem_id, "elem");
 	}
 	
 	/**
 	 * 
 	 * @param delem_id
 	 * @param parent_type
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getFixedValues(String delem_id, String parent_type) throws SQLException {
 
 		INParameters inParams = new INParameters();
 		StringBuffer buf = new StringBuffer();
 		buf.append("select * from FXV where OWNER_ID=").append(inParams.add(delem_id, Types.INTEGER)).
 		append(" and OWNER_TYPE=").append(inParams.add(parent_type)).append(" order by VALUE asc");
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Vector v = new Vector();
 		
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			while (rs.next()){
 				
 				FixedValue fxv = new FixedValue(rs.getString("FXV_ID"),
 						rs.getString("OWNER_ID"),
 						rs.getString("VALUE"));
 				
 				String isDefault = rs.getString("IS_DEFAULT");
 				if (isDefault!=null && isDefault.equalsIgnoreCase("Y"))
 					fxv.setDefault();
 				
 				fxv.setDefinition(rs.getString("DEFINITION"));
 				fxv.setShortDesc(rs.getString("SHORT_DESC"));
 				
 				v.add(fxv);
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param attr_id
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getAttrFields(String attr_id) throws SQLException{
 		return getAttrFields(attr_id, null);
 	}
 	
 	/**
 	 * 
 	 * @param attr_id
 	 * @param priority
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getAttrFields(String attr_id, String priority) throws SQLException{
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf = new StringBuffer();
 		buf.append("select * from M_COMPLEX_ATTR_FIELD ");
 		buf.append("where M_COMPLEX_ATTR_ID=");
 		buf.append(inParams.add(attr_id, Types.INTEGER));
 		if (priority != null)
 			buf.append(" and PRIORITY=").append(inParams.add(priority));
 		buf.append(" order by POSITION");
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Vector v = new Vector();
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			while (rs.next()){
 				
 				Hashtable hash = new Hashtable();
 				hash.put("id", rs.getString("M_COMPLEX_ATTR_FIELD_ID"));
 				hash.put("name", rs.getString("NAME"));
 				hash.put("definition", rs.getString("DEFINITION"));
 				hash.put("position", rs.getString("POSITION"));
 				hash.put("priority", rs.getString("PRIORITY"));
 				
 				String harvAttrFldName = rs.getString("HARV_ATTR_FLD_NAME");
 				if (harvAttrFldName!=null)
 					hash.put("harv_fld", harvAttrFldName);
 				
 				v.add(hash);
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param field_id
 	 * @return
 	 * @throws SQLException
 	 */
 	public Hashtable getAttrField(String field_id) throws SQLException{
 		
 		INParameters inParams = new INParameters();
 		StringBuffer buf = new StringBuffer();
 		buf.append("select * from M_COMPLEX_ATTR_FIELD ");
 		buf.append("where M_COMPLEX_ATTR_FIELD_ID=");
 		buf.append(inParams.add(field_id, Types.INTEGER));
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Hashtable hash = new Hashtable();
 		
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			while (rs.next()){
 				
 				hash.put("id", rs.getString("M_COMPLEX_ATTR_FIELD_ID"));
 				hash.put("name", rs.getString("NAME"));
 				hash.put("definition", rs.getString("DEFINITION"));
 				hash.put("position", rs.getString("POSITION"));
 				hash.put("priority", rs.getString("PRIORITY"));
 				
 				String harvAttrFldName = rs.getString("HARV_ATTR_FLD_NAME");
 				if (harvAttrFldName!=null)
 					hash.put("harv_fld", harvAttrFldName);
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return hash;
 	}
 	
 	/**
 	 * 
 	 * @param attr_id
 	 * @param parent_id
 	 * @param parent_type
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getComplexAttribute(String attr_id, String parent_id, String parent_type) throws SQLException {
 		return getComplexAttributes(parent_id, parent_type, attr_id);
 	}
 	
 	/**
 	 * 
 	 * @param parent_id
 	 * @param parent_type
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getComplexAttributes(String parent_id, String parent_type) throws SQLException {
 		return getComplexAttributes(parent_id, parent_type, null);
 	}
 	
 	/**
 	 * 
 	 * @param parent_id
 	 * @param parent_type
 	 * @param attr_id
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getComplexAttributes(String parent_id, String parent_type, String attr_id) throws SQLException {
 		return getComplexAttributes(parent_id, parent_type, attr_id, null, null);
 	}
 	
 	/**
 	 * 
 	 * @param parent_id
 	 * @param parent_type
 	 * @param attr_id
 	 * @param inheritTblID
 	 * @param inheritDstID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getComplexAttributes(String parent_id,
 			String parent_type,
 			String attr_id,
 			String inheritTblID,
 			String inheritDstID) throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		StringBuffer buf = new StringBuffer();
 		buf.append("select ");
 		buf.append("M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID as ATTR_ID, ");
 		buf.append("M_COMPLEX_ATTR.SHORT_NAME as ATTR_NAME, ");
 		buf.append("M_COMPLEX_ATTR.INHERIT as INHERIT, ");
 		buf.append("M_COMPLEX_ATTR.HARVESTER_ID as HARVESTER_ID, ");
 		buf.append("M_COMPLEX_ATTR.OBLIGATION as OBLIGATION, ");
 		buf.append("NAMESPACE.SHORT_NAME as NS, ");
 		buf.append("COMPLEX_ATTR_ROW.POSITION as ROW_POS, ");
 		buf.append("COMPLEX_ATTR_ROW.ROW_ID as ROW_ID, ");
 		buf.append("COMPLEX_ATTR_ROW.HARV_ATTR_ID as HARV_ATTR_ID, ");
 		buf.append("COMPLEX_ATTR_ROW.PARENT_TYPE as PARENT_TYPE, ");
 		buf.append("COMPLEX_ATTR_FIELD.M_COMPLEX_ATTR_FIELD_ID as FIELD_ID, ");
 		buf.append("COMPLEX_ATTR_FIELD.VALUE as FIELD_VALUE, ");
 		buf.append("HARV_ATTR_FIELD.FLD_NAME, ");
 		buf.append("HARV_ATTR_FIELD.FLD_VALUE ");
 		buf.append("from ");
 		buf.append("COMPLEX_ATTR_ROW ");
 		buf.append("left outer join COMPLEX_ATTR_FIELD on ");
 		buf.append("COMPLEX_ATTR_ROW.ROW_ID=COMPLEX_ATTR_FIELD.ROW_ID ");
 		
 		buf.append("left outer join HARV_ATTR on ");
 		buf.append("COMPLEX_ATTR_ROW.HARV_ATTR_ID=");
 		buf.append("HARV_ATTR.LOGICAL_ID ");
 		
 		buf.append("left outer join HARV_ATTR_FIELD on ");
 		buf.append("HARV_ATTR.MD5KEY=HARV_ATTR_FIELD.HARV_ATTR_MD5, ");
 		
 		buf.append("M_COMPLEX_ATTR left outer join NAMESPACE ");
 		buf.append("on M_COMPLEX_ATTR.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID ");        
 		buf.append("where ");
 		buf.append("((COMPLEX_ATTR_ROW.PARENT_ID=");
 		buf.append(inParams.add(parent_id, Types.INTEGER));
 		buf.append(" and COMPLEX_ATTR_ROW.PARENT_TYPE=");
 		buf.append(inParams.add(parent_type));
 		buf.append(")");
 		
 		// EK291003 search inhrted attributes from table and/or dataset level
 		if (!Util.nullString(inheritTblID)){
 			buf.append(" or (COMPLEX_ATTR_ROW.PARENT_ID=");
 			buf.append(inParams.add(inheritTblID));
 			buf.append(" and COMPLEX_ATTR_ROW.PARENT_TYPE='T' and M_COMPLEX_ATTR.INHERIT!='0')");
 		}
 		if (!Util.nullString(inheritDstID)){
 			buf.append(" or (COMPLEX_ATTR_ROW.PARENT_ID=");
 			buf.append(inParams.add(inheritDstID));
 			buf.append(" and COMPLEX_ATTR_ROW.PARENT_TYPE='DS' and M_COMPLEX_ATTR.INHERIT!='0')");
 		}
 		buf.append(")");
 		
 		// set attribute id if looking for a concrete attribute
 		if (attr_id != null){
 			buf.append(" and COMPLEX_ATTR_ROW.M_COMPLEX_ATTR_ID=");
 			buf.append(inParams.add(attr_id, Types.INTEGER));
 		}
 		
 		buf.append(" and COMPLEX_ATTR_ROW.M_COMPLEX_ATTR_ID=");
 		buf.append("M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID ");
 		buf.append("order by ATTR_ID, PARENT_TYPE, ROW_POS");
 		
 		logger.debug(buf.toString());
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Vector v = new Vector();
 		DElemAttribute attr = null;
 		Hashtable rowHash = null;
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			Hashtable attrs  = new Hashtable();
 			Hashtable fields = new Hashtable();
 			
 			Hashtable harvFieldsHash = null;
 			
 			int prvRow = -1;
 			String prvType = "";
 			while (rs.next()){
 				
 				String attrID = rs.getString("ATTR_ID");
 				String parentType = rs.getString("COMPLEX_ATTR_ROW.PARENT_TYPE");
 				String inherited = parentType.equals(parent_type)? null:parentType;
 				if (attrs.containsKey(attrID))
 					attr = (DElemAttribute)attrs.get(attrID);
 				else{
 					if (attr != null){    // this is true, when there are multiple attr_ids and we have found one already
 						
 						addRowHash(attr, rowHash);
 						v.add(attr);
 						rowHash = null;
 						prvRow = -1;
 						prvType="";
 					}
 					
 					harvFieldsHash = null;
 					attr = new DElemAttribute(attrID,
 							null,
 							rs.getString("ATTR_NAME"),
 							DElemAttribute.TYPE_COMPLEX,
 							null,null,rs.getString("OBLIGATION"));
 					attr.setInheritable(rs.getString("INHERIT"));
 					attr.setHarvesterID(rs.getString("HARVESTER_ID"));
 					
 					Namespace ns = new Namespace(null, rs.getString("NS"), null, null, null);
 					attr.setNamespace(ns);
 					
 					attrs.put(attrID, attr);
 				}
 				
 				int row = rs.getInt("ROW_POS");
 				if (row != prvRow || !parentType.equals(prvType)){
 					if (prvRow != -1 && !prvType.equals(""))
 						addRowHash(attr, rowHash);
 					
 					rowHash = new Hashtable();
 					rowHash.put("rowid", rs.getString("ROW_ID"));
 					rowHash.put("position", rs.getString("ROW_POS"));
 					if (inherited!=null)
 						rowHash.put("inherited", inherited);
 				}
 				
 				String fldID = rs.getString("FIELD_ID");
 				String fldValue = rs.getString("FIELD_VALUE");
 				
 				//JH111103
 				String harvAttrID = rs.getString("HARV_ATTR_ID");
 				if (harvAttrID!=null){
 					
 					rowHash.put("harv_attr_id", harvAttrID);
 					
 					String harvAttrFldName = rs.getString("FLD_NAME");
 					if (harvAttrFldName!=null){						
 						if (harvFieldsHash==null){
 							harvFieldsHash =
 								getHarvestedAttrFieldsHash(attr.getID());
 						}
 						
 						fldID = (String)harvFieldsHash.get(harvAttrFldName);
 						if (fldID!=null)
 							fldValue = rs.getString("FLD_VALUE");
 					}
 				}
 				
 				if (fldID!=null && fldValue!=null)
 					rowHash.put(fldID, fldValue);
 				
 				prvRow = row;
 				prvType = parentType;
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		if (attr != null){
 			addRowHash(attr, rowHash);
 			v.add(attr);
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param attr
 	 * @param rowHash
 	 */
 	private void addRowHash(DElemAttribute attr, Hashtable rowHash){
 		String inherited=null;
 		if (rowHash.containsKey("inherited"))
 			inherited = (String)rowHash.get("inherited");
 		
 		if (inherited!=null){
 			if (attr.getInheritable().equals("1")){    //inheritance type 1 - show all values from upper levels
 				attr.addRow(rowHash);
 				attr.addInheritedValue(rowHash);
 				attr.setInheritedLevel(inherited);
 			}
 			else{//inheritance type 2 - show values from upper levels or if current level has values then onlycurrent level
 				if (attr.getInheritedLevel()==null){
 					attr.addInheritedValue(rowHash);
 					attr.setInheritedLevel(inherited);
 				}
 				else{
 					if (attr.getInheritedLevel().equals("DS") && inherited.equals("T"))
 						attr.clearInherited();  // element should inherit table values if exists and then dataset values
 					if (attr.getInheritedLevel().equals(inherited) || inherited.equals("T")){
 						attr.addInheritedValue(rowHash);
 						attr.setInheritedLevel(inherited);
 					}
 				}
 			}
 		}
 		else{  //get values from original level
 			attr.addRow(rowHash);
 			attr.addOriginalValue(rowHash);
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @param attr_id
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getComplexAttributeValues(String attr_id) throws SQLException {
 		
 		if (attr_id==null)
 			return null;
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf = new StringBuffer();
 		buf.append("select ");
 		buf.append("M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID as ATTR_ID, ");
 		buf.append("M_COMPLEX_ATTR.SHORT_NAME as ATTR_NAME, ");
 		buf.append("NAMESPACE.SHORT_NAME as NS, ");
 		buf.append("COMPLEX_ATTR_ROW.POSITION as ROW_POS, ");
 		buf.append("COMPLEX_ATTR_ROW.ROW_ID as ROW_ID, ");
 		buf.append("COMPLEX_ATTR_FIELD.M_COMPLEX_ATTR_FIELD_ID as FIELD_ID, ");
 		buf.append("COMPLEX_ATTR_FIELD.VALUE as FIELD_VALUE ");
 		buf.append("from ");
 		buf.append("COMPLEX_ATTR_ROW, ");
 		buf.append("M_COMPLEX_ATTR left outer join NAMESPACE ");
 		buf.append("on M_COMPLEX_ATTR.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID, ");
 		buf.append("COMPLEX_ATTR_FIELD ");
 		buf.append("where ");
 		buf.append("COMPLEX_ATTR_ROW.M_COMPLEX_ATTR_ID=M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID and ");
 		buf.append("COMPLEX_ATTR_FIELD.ROW_ID=COMPLEX_ATTR_ROW.ROW_ID ");
 		
 		if (attr_id != null){
 			buf.append(" and COMPLEX_ATTR_ROW.M_COMPLEX_ATTR_ID=");
 			buf.append(inParams.add(attr_id, Types.INTEGER));
 		}
 		buf.append(" order by ROW_ID");
 		
 		logger.debug(buf.toString());
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Vector v = new Vector();
 		Hashtable rowHash = null;
 		boolean hasVal=false;
 		boolean ok=true;
 		String row="";
 		
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			String prvRow = "-1";
 			while (ok){
 				ok = rs.next();
 				if (ok) row = rs.getString("ROW_ID");
 				
 				if (!row.equals(prvRow) || !ok){
 					if (!prvRow.equals("-1")){
 						for (int i=0; i<v.size();i++){
 							Hashtable h = (Hashtable)v.get(i);
 							if (h.equals(rowHash)){
 								hasVal=true;
 								break;
 							}
 						}
 						if (!hasVal)
 							v.add(rowHash);
 						hasVal=false;
 					}
 					
 					if (!ok) break;
 					rowHash = new Hashtable();
 				}
 				rowHash.put(rs.getString("FIELD_ID"), rs.getString("FIELD_VALUE"));
 				prvRow = row;
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param attr_id
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getSimpleAttributeValues(String attr_id) throws SQLException {
 		
 		if (attr_id==null)
 			return null;
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf = new StringBuffer();
 		buf.append("select distinct value from ATTRIBUTE where M_ATTRIBUTE_ID=");
 		buf.append(inParams.add(attr_id, Types.INTEGER));
 		buf.append(" order by VALUE");
 		
 		logger.debug(buf.toString());
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Vector v = new Vector();		
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			while (rs.next()){
 				
 				String value = rs.getString("value");
 				if (value==null) continue;
 				if (!value.equals("")){
 					v.add(value);
 				}
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getNamespaces() throws SQLException {
 		return getNamespaces(null);
 	}
 	
 	/**
 	 * 
 	 * @param id
 	 * @return
 	 * @throws SQLException
 	 */
 	public Namespace getNamespace(String id) throws SQLException {
 		Vector v = getNamespaces(id);
 		if (v != null && v.size()!=0) return (Namespace)v.get(0);
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param id
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getNamespaces(String id) throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf = new StringBuffer("select * from NAMESPACE");
 		if (id != null && id.length()!=0)
 			buf.append(" where NAMESPACE_ID=").append(inParams.add(id, Types.INTEGER));
 		buf.append(" order by SHORT_NAME");
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Vector v = new Vector();
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			while (rs.next()){
 				Namespace namespace = new Namespace(rs.getString("NAMESPACE_ID"),
 						rs.getString("SHORT_NAME"), rs.getString("FULL_NAME"), null, rs.getString("DEFINITION"));
 				namespace.setWorkingUser(rs.getString("WORKING_USER"));
 				v.add(namespace);
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return v;
 	}
 
 	/**
 	 * 
 	 * @param datasetID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Dataset getDeletedDataset(String datasetID) throws SQLException {
 		Vector v = getDatasets(datasetID, false, true);
 		if (v==null || v.size()==0)
 			return null;
 		else
 			return (Dataset)v.get(0);
 	}
 	
 	/**
 	 * 
 	 * @param datasetID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Dataset getDataset(String datasetID) throws SQLException {
 		
 		Vector v = getDatasets(datasetID, false, false);
 		if (v==null || v.size()==0)
 			return null;
 		else
 			return (Dataset)v.get(0);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasets() throws SQLException {
 		return getDatasets(null, false, false);
 	}
 	
 	/**
 	 * 
 	 * @param wrkCopies
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasets(boolean wrkCopies) throws SQLException {
 		return getDatasets(null, wrkCopies, false);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDeletedDatasets() throws SQLException {
 		if (user==null || !user.isAuthentic())
 			throw new SQLException("User not authorized");
 		return getDatasets(null, false, true);
 	}
 	
 	/**
 	 * 
 	 * @param datasetID
 	 * @param wrkCopies
 	 * @param deleted
 	 * @return
 	 * @throws SQLException
 	 */
 	private Vector getDatasets(String datasetID, boolean wrkCopies, boolean deleted) throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf  = new StringBuffer();
 		buf.append("select distinct DATASET.* ");
 		buf.append("from DATASET ");
 		buf.append("where CORRESP_NS is not null");
 		if (datasetID!=null && datasetID.length()!=0)
 			buf.append(" and DATASET.DATASET_ID=").append(inParams.add(datasetID, Types.INTEGER));
 		
 		// JH141003
 		// if datasetID specified, ignore the DELETED flag, otherwise follow it
 		if (!Util.nullString(datasetID))
 			buf.append(" ");
 		else if (deleted && user!=null)
 			buf.append(" and DATASET.DELETED=").append(inParams.add(user.getUserName())).append(" ");
 		else
 			buf.append(" and DATASET.DELETED is null ");
 		
 		// prune out the working copies
 		// (the business logic at edit view will lead the user eventually
 		// to the working copy anyway)
 		// But only in case if the ID is not exolicitly specified
 		if (Util.nullString(datasetID)){
 			if (wrkCopies && (user==null || !user.isAuthentic()))
 				wrkCopies = false;
 			if (buf.length()!=0)
 				buf.append(" and ");
 			if (!wrkCopies)
 				buf.append("DATASET.WORKING_COPY='N'");
 			else
 				buf.append("DATASET.WORKING_COPY='Y' and DATASET.WORKING_USER=").append(inParams.add(user.getUserName()));
 		}
 		buf.append(" order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc");
 		
 		logger.debug(buf.toString());
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Vector v = new Vector();
 		
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			Dataset ds = null;
 			while (rs.next()){
 				
 				String idf = rs.getString("IDENTIFIER");
 				
 				// make sure we get the latest version of the dataset
 				// JH101003 - unless were in 'restore' mode
 				if (!deleted && ds!=null && idf.equals(ds.getIdentifier()))
 					continue;
 				
 				ds = new Dataset(rs.getString("DATASET_ID"),
 						rs.getString("SHORT_NAME"),
 						rs.getString("VERSION"));
 				
 				ds.setWorkingCopy(rs.getString("WORKING_COPY"));
 				ds.setStatus(rs.getString("REG_STATUS"));
 				ds.setVisual(rs.getString("VISUAL"));
 				ds.setDetailedVisual(rs.getString("DETAILED_VISUAL"));
 				ds.setNamespaceID(rs.getString("CORRESP_NS"));
 				ds.setDisplayCreateLinks(rs.getInt("DISP_CREATE_LINKS"));
 				ds.setIdentifier(rs.getString("IDENTIFIER"));
 				ds.setCheckedoutCopyID(rs.getString("CHECKEDOUT_COPY_ID"));
 				ds.setWorkingUser(rs.getString("WORKING_USER"));
 				ds.setDate(rs.getString("DATE"));
 				
 				v.add(ds);
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return v;
 	}
 
 	/**
 	 * 
 	 * @param params
 	 * @param short_name
 	 * @param version
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasets(Vector params, String short_name, String version) throws SQLException {
 		return getDatasets(params, short_name, version, null);
 	}
 	
 	/**
 	 * 
 	 * @param params
 	 * @param short_name
 	 * @param version
 	 * @param oper
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasets(Vector params, String short_name, String version, String oper) throws SQLException {
 		return getDatasets(params, short_name, version, oper, false);
 	}
 	
 	/**
 	 * 
 	 * @param params
 	 * @param short_name
 	 * @param version
 	 * @param oper
 	 * @param wrkCopies
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasets(Vector params,
 			String short_name,
 			String version,
 			String oper,
 			boolean wrkCopies) throws SQLException {
 		return getDatasets(params, short_name, null, version, oper, wrkCopies);
 	}
 	
 	/**
 	 * get datasets by params, control oper & working copies
 	 */
 	public Vector getDatasets(Vector params,
 			String short_name,
 			String idfier,
 			String version,
 			String oper,
 			boolean wrkCopies) throws SQLException {
 		
 		return getDatasets(params, short_name, idfier, version, oper, wrkCopies, null);
 	}
 	
 	/**
 	 * get datasets by params, control oper & working copies
 	 */
 	public Vector getDatasets(Vector params,
 			String short_name,
 			String idfier,
 			String version,
 			String oper,
 			boolean wrkCopies,
 			HashSet statuses) throws SQLException {
 		return getDatasets(params, short_name, idfier, version, oper, wrkCopies, false, statuses);
 	}
 	
 	/**
 	 * Get datasets by params, control oper, working copies & historic versions.
 	 */
 	public Vector getDatasets(Vector params,
 			String short_name,
 			String idfier,
 			String version,
 			String oper,
 			boolean wrkCopies,
 			boolean isIncludeHistoricVersions,
 			HashSet statuses) throws SQLException {
 		
 		// get the id of simple attribute "Name"
 		Statement stmt1 = conn.createStatement();
 		ResultSet rs = stmt1.executeQuery("select M_ATTRIBUTE_ID from " +
 		"M_ATTRIBUTE where SHORT_NAME='Name'");
 		String nameID = rs.next() ? rs.getString(1) : null;
 		rs.close();
 		stmt1.close();
 		
 		INParameters inParams = new INParameters();
 		
 		// prepare different parts of the query for getting datasets
 		if (oper==null)
 			oper=" like ";
 		
 		StringBuffer tables = new StringBuffer("DATASET");
 		StringBuffer constraints = new StringBuffer("DATASET.DELETED is null");
 		
 		// short name into constraints
 		if (short_name!=null && short_name.length()!=0){
 			constraints.append(" and DATASET.SHORT_NAME");
 			// overwrite 'match' operator with 'like', because short name is not fulltext-indexed
 			if (oper.trim().equalsIgnoreCase("match"))
 				oper=" like ";
 			constraints.append(oper);
 			if (oper.trim().equalsIgnoreCase("like"))
 				constraints.append(inParams.add("%" + short_name + "%"));
 			else
 				constraints.append(inParams.add(short_name));
 		}
 		
 		// identifier into constraints
 		if (idfier!=null && idfier.length()!=0){
 			constraints.append(" and DATASET.IDENTIFIER");
 			// overwrite 'match' operator with 'like', because identifier is not fulltext-indexed 
 			if (oper.trim().equalsIgnoreCase("match"))
 				oper=" like "; // 
 			constraints.append(oper);
 			if (oper.trim().equalsIgnoreCase("like"))
 				constraints.append(inParams.add("%" + idfier + "%"));
 			else
 				constraints.append(inParams.add(idfier));
 		}
 		
 		// version into constraints
 		if (version!=null && version.length()!=0){
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("DATASET.VERSION=").append(inParams.add(version, Types.INTEGER));
 		}
 		
 		// statuses into constraints
 		if (statuses!=null && statuses.size()>0){
 			if (constraints.length()!=0)
 				constraints.append(" and (");
 			int i = 0;
 			for (Iterator iter=statuses.iterator(); iter.hasNext(); i++){
 				if (i>0) constraints.append(" or ");
 				constraints.append("REG_STATUS=");
 				constraints.append(inParams.add((String)iter.next()));
 			}
 			constraints.append(")");
 		}
 		
 		// params into constraints (if no params, we ask for all)
 		for (int i=0; params!=null && i<params.size(); i++){
 			String index = String.valueOf(i+1);
 			DDSearchParameter param = (DDSearchParameter)params.get(i);
 			String attrID  = param.getAttrID();
 			Vector attrValues = param.getAttrValues();
 			String valueOper = param.getValueOper();
 			String idOper = param.getIdOper();
 			
 			tables.append(", ATTRIBUTE as ATTR" + index);
 			
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("ATTR" + index + ".M_ATTRIBUTE_ID" + idOper + inParams.add(attrID, Types.INTEGER));
 			constraints.append(" and ");
 			
 			if (attrValues!=null && attrValues.size()!=0){
 				constraints.append("(");
 				for (int j=0; j<attrValues.size(); j++){
 					if (j>0) constraints.append(" or ");
 					if (valueOper!= null && valueOper.trim().equalsIgnoreCase("MATCH"))
 						constraints.append("match(ATTR" + index + ".VALUE) against(" + inParams.add(attrValues.get(j)) +")");
 					else
 						constraints.append("ATTR" + index + ".VALUE" + valueOper + inParams.add(attrValues.get(j)));
 				}
 				constraints.append(")");
 			}
 			constraints.append(" and ");
 			constraints.append("ATTR" + index + ".DATAELEM_ID=DATASET.DATASET_ID");
 			constraints.append(" and ");
 			constraints.append("ATTR" + index + ".PARENT_TYPE='DS'");
 		}
 		
 		// unless requested otherwise, prune out the working copies (the business logic in
 		// UI will lead the user eventually to the working copy anyway)
 		if (wrkCopies && (user==null || !user.isAuthentic()))
 			wrkCopies = false;
 		if (constraints.length()!=0)
 			constraints.append(" and ");
 		if (!wrkCopies)
 			constraints.append("DATASET.WORKING_COPY='N'");
 		else
 			constraints.append("DATASET.WORKING_COPY='Y' and DATASET.WORKING_USER=" + inParams.add(user.getUserName()));
 		
 		// compile the query from the above-prepared parts
 		StringBuffer buf = new StringBuffer("select DATASET.* from ");
 		buf.append(tables.toString());
 		if (constraints.length()!=0){
 			buf.append(" where ");
 			buf.append(constraints.toString());
 		}
 		buf.append(" order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc");
 		logger.debug(buf.toString());
 		
 		// preprare the statement for getting attributes
 		PreparedStatement ps = null;
 		if (nameID!=null){
 			String s = "select VALUE from ATTRIBUTE where M_ATTRIBUTE_ID=" +
 			nameID + " and PARENT_TYPE='DS' and " +
 			"DATAELEM_ID=?";
 			ps = conn.prepareStatement(s);
 		}
 		
 		// execute the query for datasets
 		PreparedStatement stmt = null;
 		rs = null;
 		ResultSet rs2 = null;
 		Vector v = new Vector();
 		try {
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			Dataset ds = null;
 			while (rs.next()){
 				
 				String regStatus = rs.getString("REG_STATUS");
 				String idf = rs.getString("DATASET.IDENTIFIER");
 				
 				// if history not wanted, make sure we get the latest version of the dataset
 				if (isIncludeHistoricVersions==false){
 					if (ds!=null && idf.equals(ds.getIdentifier()))
 						continue;
 				}
 				
 				ds = new Dataset(rs.getString("DATASET_ID"),
 						rs.getString("SHORT_NAME"),
 						rs.getString("VERSION"));
 				
 				ds.setWorkingCopy(rs.getString("WORKING_COPY"));                
 				ds.setVisual(rs.getString("VISUAL"));
 				ds.setDetailedVisual(rs.getString("DETAILED_VISUAL"));
 				ds.setNamespaceID(rs.getString("CORRESP_NS"));
 				ds.setDisplayCreateLinks(rs.getInt("DISP_CREATE_LINKS"));
 				ds.setDate(rs.getString("DATASET.DATE"));
 				
 				// set the name if nameID was previously successfully found
 				if (nameID!=null){
 					ps.setInt(1, rs.getInt("DATASET.DATASET_ID"));
 					rs2 = ps.executeQuery();
 					if (rs2.next())
 						ds.setName(rs2.getString(1));
 				}
 				
 				ds.setStatus(regStatus);
 				ds.setIdentifier(rs.getString("IDENTIFIER"));
 				ds.setCheckedoutCopyID(rs.getString("CHECKEDOUT_COPY_ID"));
 				ds.setWorkingUser(rs.getString("WORKING_USER"));
 				
 				v.add(ds);
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (rs2 != null) rs2.close();
 				if (stmt != null) stmt.close();
 				if (stmt1 != null) stmt1.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * Returns true if this.user should not see definition in the given status
 	 * @param regStatus
 	 * @return
 	 */
 	public boolean skipByRegStatus(String regStatus) {
 		
 		if (regStatus!=null){
 			if (user==null || !user.isAuthentic()){
 				if (regStatus.equals("Incomplete") ||
 						regStatus.equals("Candidate") ||
 						regStatus.equals("Qualified"))
 					return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @param dsID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasetTables(String dsID) throws SQLException {
 		return getDatasetTables(dsID, false);
 	}
 	
 	/**
 	 * 
 	 * @param dstID
 	 * @param isOrderByFullName
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasetTables(String dstID, boolean isOrderByFullName) throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		
 		// prepare the query
 		StringBuffer buf  = new StringBuffer();
 		buf.append("select distinct DS_TABLE.*");
 		if (isOrderByFullName)
 			buf.append(", ATTRIBUTE.VALUE");
 		buf.append(" from DS_TABLE ");
 		buf.append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ");
 		if (isOrderByFullName){
 			buf.append("left outer join ATTRIBUTE on DS_TABLE.TABLE_ID=ATTRIBUTE.DATAELEM_ID ").
 			append("left outer join M_ATTRIBUTE on ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID ");
 		}
 		buf.append("where DS_TABLE.CORRESP_NS is not null and ").
 		append("DST2TBL.DATASET_ID=").append(inParams.add(dstID, Types.INTEGER));
 		if (isOrderByFullName){
 			buf.append(" and M_ATTRIBUTE.SHORT_NAME='Name' and ").
 			append("ATTRIBUTE.PARENT_TYPE='T'");
 		}
 		buf.append(" order by DS_TABLE.IDENTIFIER,DS_TABLE.TABLE_ID desc");
 		
 		logger.debug(buf.toString());
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Vector v = new Vector();
 		
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			DsTable dsTable = null;
 			while (rs.next()){
 				
 				// make sure you get the latest version
 				String idf = rs.getString("DS_TABLE.IDENTIFIER");
 				if (dsTable!=null){
 					if (idf.equals(dsTable.getIdentifier()))
 						continue;
 				}
 				
 				dsTable = new DsTable(rs.getString("DS_TABLE.TABLE_ID"),
 						dstID,
 						rs.getString("DS_TABLE.SHORT_NAME"));
 				
 				dsTable.setWorkingCopy(rs.getString("DS_TABLE.WORKING_COPY"));
 				dsTable.setWorkingUser(rs.getString("DS_TABLE.WORKING_USER"));
 				dsTable.setVersion(rs.getString("DS_TABLE.VERSION"));
 				dsTable.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
 				dsTable.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));
 				dsTable.setIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));
 				if (isOrderByFullName){
 					dsTable.setName(rs.getString("ATTRIBUTE.VALUE"));
 					dsTable.setCompStr(dsTable.getName());
 				}
 				
 				v.add(dsTable);
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		if (isOrderByFullName)
 			Collections.sort(v);
 		
 		return v;
 	}
 
 	/**
 	 * 
 	 * @param params
 	 * @param short_name
 	 * @param full_name
 	 * @param definition
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasetTables(Vector params, String short_name, String full_name, String definition) throws SQLException {
 		return getDatasetTables(params, short_name, full_name, definition, null);
 	}
 
 	/**
 	 * 
 	 * @param params
 	 * @param short_name
 	 * @param full_name
 	 * @param definition
 	 * @param oper
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasetTables(Vector params,
 			String short_name,								   
 			String full_name,
 			String definition,
 			String oper) throws SQLException {
 		
 		return getDatasetTables(params, short_name, null, full_name, definition, oper);
 	}
 
 	/**
 	 * 
 	 * @param params
 	 * @param short_name
 	 * @param idfier
 	 * @param full_name
 	 * @param definition
 	 * @param oper
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasetTables(Vector params,
 			String short_name,
 			String idfier,
 			String full_name,
 			String definition,
 			String oper) throws SQLException{
 		
 		return getDatasetTables(params, short_name, idfier, full_name, definition, oper, null, true);
 	}
 
 	/**
 	 * 
 	 * @param params
 	 * @param short_name
 	 * @param idfier
 	 * @param full_name
 	 * @param definition
 	 * @param oper
 	 * @param dstStatuses
 	 * @param latestOnly
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDatasetTables(Vector params,
 			String short_name,
 			String idfier,
 			String full_name,
 			String definition,
 			String oper,
 			HashSet dstStatuses,
 			boolean latestOnly) throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		
 		// get the id of simple attribute "Name"
 		Statement stmt1 = conn.createStatement();
 		ResultSet rs = stmt1.executeQuery("select M_ATTRIBUTE_ID from M_ATTRIBUTE where SHORT_NAME='Name'");
 		String nameID = rs.next() ? rs.getString(1) : null;
 		rs.close();
 		stmt1.close();
 		
 		// prepare different parts of the query for tables
 		if (oper==null)
 			oper=" like ";
 		
 		StringBuffer tables = new StringBuffer("DS_TABLE, DST2TBL, DATASET");
 		StringBuffer constraints = new StringBuffer().
 		append("DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID and DST2TBL.DATASET_ID=DATASET.DATASET_ID").
 		append(" and DATASET.DELETED is null and DATASET.WORKING_COPY='N'");
 		
 		// dataset statuses into constraints
 		if (dstStatuses!=null && dstStatuses.size()>0){
 			if (constraints.length()>0)
 				constraints.append(" and ");
 			constraints.append("(");
 			int i = 0;
 			for (Iterator iter=dstStatuses.iterator(); iter.hasNext(); i++){
 				if (i>0)
 					constraints.append(" or ");
 				constraints.append("DATASET.REG_STATUS=").append(inParams.add((String)iter.next()));
 			}
 			constraints.append(")");
 		}
 
 		
 		// short name into constraints
 		if (short_name!=null && short_name.length()!=0){
 			constraints.append(" and DS_TABLE.SHORT_NAME");
 			// overwrite 'match' operator with 'like', because short name is not fulltext-indexed
 			if (oper.trim().equalsIgnoreCase("match"))
 				oper=" like ";
 			constraints.append(oper);
 			if (oper.trim().equalsIgnoreCase("like"))
 				constraints.append(inParams.add("%" + short_name + "%"));
 			else
 				constraints.append(inParams.add(short_name));
 		}
 		
 		// identifier into constraints
 		if (idfier!=null && idfier.length()!=0){
 			constraints.append(" and DS_TABLE.IDENTIFIER");
 			// overwrite 'match' operator with 'like', because identifier is not fulltext-indexed
 			if (oper.trim().equalsIgnoreCase("match"))
 				oper=" like ";
 			constraints.append(oper);
 			if (oper.trim().equalsIgnoreCase("like"))
 				constraints.append(inParams.add("%" + idfier + "%"));
 			else
 				constraints.append(inParams.add(idfier));
 		}
 		
 		// full name into constraints
 		if (full_name!=null && full_name.length()!=0){
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("DS_TABLE.NAME like ").append(inParams.add("%" + full_name + "%"));
 		}
 		// definition into constraints
 		if (definition!=null && definition.length()!=0){
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("DS_TABLE.DEFINITION like ").append(inParams.add("%" + definition + "%"));
 		}
 		
 		// params into constraints (if params==null, we ask for all)
 		for (int i=0; params!=null && i<params.size(); i++){
 			
 			String index = String.valueOf(i+1);
 			DDSearchParameter param = (DDSearchParameter)params.get(i);
 			String attrID  = param.getAttrID();
 			Vector attrValues = param.getAttrValues();
 			String valueOper = param.getValueOper();
 			String idOper = param.getIdOper();
 			String attrName = param.getAttrShortName();
 			
 			tables.append(", ATTRIBUTE as ATTR" + index);
 			
 			if (constraints.length()!=0)
 				constraints.append(" and ");
 			constraints.append("ATTR" + index + ".M_ATTRIBUTE_ID" + idOper + inParams.add(attrID, Types.INTEGER));
 			constraints.append(" and ");
 			
 			if (attrValues!=null && attrValues.size()!=0){
 				constraints.append("(");
 				for (int j=0; j<attrValues.size(); j++){
 					if (j>0) constraints.append(" or ");
 					if (valueOper!= null && valueOper.trim().equalsIgnoreCase("MATCH"))
 						constraints.append("match(ATTR" + index + ".VALUE) against(" + inParams.add(attrValues.get(j)) +")");
 					else
 						constraints.append("ATTR" + index + ".VALUE" + valueOper + inParams.add(attrValues.get(j)));
 				}
 				constraints.append(")");
 			}
 			constraints.append(" and ");
 			constraints.append("ATTR" + index + ".DATAELEM_ID=DS_TABLE.TABLE_ID");
 			constraints.append(" and ");
 			constraints.append("ATTR" + index + ".PARENT_TYPE='T'");
 		}
 		
 		// compile the query from above-prepared parts
 		StringBuffer buf =
 			new StringBuffer("select distinct DS_TABLE.*, DATASET.* from ");
 		buf.append(tables.toString());
 		if (constraints.length()!=0){
 			buf.append(" where ");
 			buf.append(constraints.toString());
 		}		
 		buf.append(" order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc, " +
 		"DS_TABLE.IDENTIFIER asc, DS_TABLE.TABLE_ID desc");
 		
 		logger.debug(buf.toString());
 		
 		// preprare the statement for getting attributes
 		PreparedStatement attrsPstmt = null;
 		if (nameID!=null){
 			String s = "select VALUE from ATTRIBUTE where M_ATTRIBUTE_ID=" +
 			nameID + " and PARENT_TYPE='T' and " +
 			"DATAELEM_ID=?";
 			attrsPstmt = conn.prepareStatement(s);
 		}
 		
 		// execute the query for tables
 		PreparedStatement stmt = null;
 		ResultSet rs2 = null;
 		Vector v = new Vector();
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			String curDstID  = null;
 			String curDstIdf = null;
 			while (rs.next()){
 				
 				String dstID  = rs.getString("DATASET.DATASET_ID");
 				String dstIdf = rs.getString("DATASET.IDENTIFIER");
 				if (dstID==null && dstIdf==null)
 					continue;
 				
 				if (latestOnly){
 					// the following if-else block skips tables in non-latest DATASETS
 					if (curDstIdf==null || !curDstIdf.equals(dstIdf)){
 						curDstIdf = dstIdf;
 						curDstID = dstID;
 					}
 					else if (!dstID.equals(curDstID))
 						continue;
 				}
 				
 				// skip tables that do not actually exist (ie trash from some erroneous situation)
 				String tblIdf = rs.getString("DS_TABLE.IDENTIFIER");
 				if (tblIdf==null)
 					continue;
 				
 				// skip this dataset if this.user should not see a dataset in given status
 				String dstStatus = rs.getString("DATASET.REG_STATUS");
 				
 				// construct the table object
 				DsTable tbl = new DsTable(rs.getString("DS_TABLE.TABLE_ID"),
 						rs.getString("DATASET.DATASET_ID"),
 						rs.getString("DS_TABLE.SHORT_NAME"));
 				tbl.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
 				tbl.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));
 				tbl.setDatasetName(rs.getString("DATASET.SHORT_NAME"));
 				tbl.setDstIdentifier(dstIdf);
 				tbl.setIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));
 				tbl.setDstStatus(rs.getString("DATASET.REG_STATUS"));
 				tbl.setDstWorkingUser(rs.getString("DATASET.WORKING_USER"));
 				tbl.setDstDate(rs.getString("DATASET.DATE"));
 				
 				// set the name if nameID was previously successfully found
 				if (nameID!=null){
 					attrsPstmt.setInt(1, rs.getInt("DS_TABLE.TABLE_ID"));
 					rs2 = attrsPstmt.executeQuery();
 					if (rs2.next())
 						tbl.setName(rs2.getString(1));
 				}
 				// set comparation string
 				tbl.setCompStr(tbl.getName());
 				// add this table object into result set
 				v.add(tbl);
 			}
 		}
 		finally{
 			try{
 				if (rs2 != null) rs2.close();
 				if (rs != null) rs.close();
 				if (stmt1 != null) stmt1.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		Collections.sort(v);
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param tableID
 	 * @return
 	 * @throws SQLException
 	 */
 	public DsTable getDatasetTable(String tableID) throws SQLException{
 		return getDatasetTable(tableID, null);
 	}
 	
 	/**
 	 * 
 	 * @param tableID
 	 * @param dstID
 	 * @return
 	 * @throws SQLException
 	 */
 	public DsTable getDatasetTable(String tableID, String dstID) throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf  = new StringBuffer();
 		buf.append("select distinct DS_TABLE.*, DATASET.* ");
 		buf.append("from DS_TABLE ");
 		
 		// JH140803
 		// there's now a many-to-many relation btw DS_TABLE & DATASET
 		buf.append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ");
 		buf.append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ");
 		buf.append("where DS_TABLE.CORRESP_NS is not null and DS_TABLE.TABLE_ID=");
 		buf.append(inParams.add(tableID, Types.INTEGER));
 		
 		if (Util.nullString(dstID))
 			buf.append(" and DATASET.DELETED is null");
 		else
 			buf.append(" and DATASET.DATASET_ID=").append(inParams.add(dstID, Types.INTEGER));
 		
 		buf.append(" order by DATASET.DATASET_ID desc");
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		DsTable dsTable = null;
 		
 		try {
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			if (rs.next()){
 				
 				dsTable = new DsTable(rs.getString("DS_TABLE.TABLE_ID"),
 						//rs.getString("DATASET_ID"),
 						rs.getString("DATASET.DATASET_ID"),
 						rs.getString("DS_TABLE.SHORT_NAME"));
 				
 				dsTable.setWorkingCopy(rs.getString("DS_TABLE.WORKING_COPY"));
 				dsTable.setVersion(rs.getString("DS_TABLE.VERSION"));
 				
 				dsTable.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
 				dsTable.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));
 				
 				dsTable.setDatasetName(rs.getString("DATASET.SHORT_NAME"));
 				dsTable.setDstIdentifier(rs.getString("DATASET.IDENTIFIER"));
 				dsTable.setDstStatus(rs.getString("DATASET.REG_STATUS"));
 				dsTable.setIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return dsTable;
 	}
 	
 	/**
 	 * 
 	 * @param parentID
 	 * @param parentType
 	 * @param attrType
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getAttributes(String parentID, String parentType, String attrType) throws SQLException {
 		return getAttributes(parentID, parentType, attrType, null, null);
 	}
 	
 	/**
 	 * 
 	 * @param parentID
 	 * @param parentType
 	 * @param attrType
 	 * @param inheritTblID
 	 * @param inheritDsID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getAttributes(String parentID, String parentType, String attrType, String inheritTblID, String inheritDsID) throws SQLException {
 		if (attrType.equals(DElemAttribute.TYPE_SIMPLE))
 			return getSimpleAttributes(parentID, parentType, inheritTblID, inheritDsID);
 		else
 			return getComplexAttributes(parentID, parentType, null, inheritTblID, inheritDsID);
 	}
 	
 	/**
 	 * 
 	 * @param parentID
 	 * @param parentType
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getSimpleAttributes(String parentID, String parentType) throws SQLException {
 		return getSimpleAttributes(parentID, parentType, null, null);
 	}
 	
 	/**
 	 * 
 	 * @param parentID
 	 * @param parentType
 	 * @param inheritTblID
 	 * @param inheritDstID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getSimpleAttributes(String parentID, String parentType, String inheritTblID, String inheritDstID) throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer qry = new StringBuffer("select M_ATTRIBUTE.*, NAMESPACE.*, ATTRIBUTE.VALUE, ATTRIBUTE.PARENT_TYPE from ");
 		qry.append("M_ATTRIBUTE left outer join NAMESPACE on ");
 		qry.append("M_ATTRIBUTE.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID ");
 		qry.append("left outer join ATTRIBUTE on M_ATTRIBUTE.M_ATTRIBUTE_ID=ATTRIBUTE.M_ATTRIBUTE_ID ");
 		qry.append("where (ATTRIBUTE.DATAELEM_ID=");
 		qry.append(inParams.add(parentID, Types.INTEGER));
 		qry.append(" and ATTRIBUTE.PARENT_TYPE=").append(inParams.add(parentType)).append(")");
 		
 		//Ek  291003 search inhrted attributes from table and/or dataset level
 		if (!Util.nullString(inheritTblID)){
 			qry.append(" or (ATTRIBUTE.DATAELEM_ID=");
 			qry.append(inParams.add(inheritTblID, Types.INTEGER));
 			qry.append(" and ATTRIBUTE.PARENT_TYPE='T' and M_ATTRIBUTE.INHERIT!='0')");
 		}
 		if (!Util.nullString(inheritDstID)){
 			qry.append(" or (ATTRIBUTE.DATAELEM_ID=");
 			qry.append(inParams.add(inheritDstID, Types.INTEGER));
 			qry.append(" and ATTRIBUTE.PARENT_TYPE='DS' and M_ATTRIBUTE.INHERIT!='0')");
 		}
 		
 		logger.debug(qry.toString());
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Vector v = new Vector();		
 		try{
 			stmt = SQL.preparedStatement(qry.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			while (rs.next()){
 				String parent_type = rs.getString("ATTRIBUTE.PARENT_TYPE");
 				String value = rs.getString("ATTRIBUTE.VALUE");
 				String inherited = parent_type.equals(parentType)? null:parent_type;
 				
 				DElemAttribute attr = getAttributeById(v, rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"));
 				if (attr==null){
 					attr =
 						new DElemAttribute(rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"),
 								rs.getString("M_ATTRIBUTE.NAME"),
 								rs.getString("M_ATTRIBUTE.SHORT_NAME"),
 								DElemAttribute.TYPE_SIMPLE,
 								null,
 								rs.getString("M_ATTRIBUTE.DEFINITION"),
 								rs.getString("M_ATTRIBUTE.OBLIGATION"),
 								rs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
 					attr.setInheritable(rs.getString("INHERIT"));                    
 					attr.setDisplayType(
 							rs.getString("M_ATTRIBUTE.DISP_TYPE"));
 					
 					// value will be set afterwards
 					Namespace ns = new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"),
 							rs.getString("NAMESPACE.SHORT_NAME"),
 							rs.getString("NAMESPACE.FULL_NAME"),
 							null,
 							rs.getString("NAMESPACE.DEFINITION"));
 					attr.setNamespace(ns);
 					
 					v.add(attr);
 				}
 				
 				if (inherited!=null){
 					if (attr.getInheritable().equals("1")){    //inheritance type 1 - show all values from upper levels
 						attr.setValue(value);
 						attr.setInheritedValue(value);
 						attr.setInheritedLevel(inherited);
 					}
 					else{//inheritance type 2 - show values from upper levels or if current level has values then onlycurrent level
 						if (attr.getInheritedLevel()==null){
 							attr.setInheritedValue(value);
 							attr.setInheritedLevel(inherited);
 						}
 						else{
 							if (attr.getInheritedLevel().equals("DS") && inherited.equals("T")){
 								attr.clearInherited();
 							}
 							if (attr.getInheritedLevel().equals(inherited) || inherited.equals("T")){
 								attr.setInheritedValue(value);
 								attr.setInheritedLevel(inherited);
 							}
 						}
 					}
 				}
 				else{  //get values from original level
 					attr.setValue(value);
 					attr.setOriginalValue(value);
 				}
 			}
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return v;
 		
 	}
 
 	/**
 	 * 
 	 * @param fxv_id
 	 * @return
 	 * @throws SQLException
 	 */
 	public FixedValue getFixedValue(String fxv_id) throws SQLException {
 		
 		if (fxv_id == null || fxv_id.length()==0){
 			FixedValue fxv = new FixedValue();
 			Vector attributes = getDElemAttributes();
 			for (int i=0; i<attributes.size(); i++)
 				fxv.addAttribute(attributes.get(i));
 			return fxv;
 		}
 		
 		INParameters inParams = new INParameters();
 		String qry = "select * from FXV where FXV_ID=" + inParams.add(fxv_id, Types.INTEGER);
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		FixedValue fxv = null;
 		try{
 			stmt = SQL.preparedStatement(qry, inParams, conn);
 			rs = stmt.executeQuery();
 			
 			if (rs.next()){
 				fxv = new FixedValue(rs.getString("FXV_ID"),
 						rs.getString("OWNER_ID"),
 						rs.getString("VALUE"));
 				
 				String isDefault = rs.getString("IS_DEFAULT");
 				if (isDefault!=null && isDefault.equalsIgnoreCase("Y"))
 					fxv.setDefault();
 				
 				fxv.setDefinition(rs.getString("DEFINITION"));
 				fxv.setShortDesc(rs.getString("SHORT_DESC"));
 			}
 			else return null;
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return fxv;
 	}
 	
 	/**
 	 * 
 	 * @param v
 	 * @param id
 	 * @return
 	 */
 	private DElemAttribute getAttributeById(Vector v, String id){
 		
 		if (v == null) return null;
 		if (id == null || id.length() == 0) return null;
 		
 		for (int i=0; i<v.size(); i++){
 			DElemAttribute attribute = (DElemAttribute)v.get(i);
 			if (attribute.getID().equalsIgnoreCase(id))
 				return attribute;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Get the last insert ID from database.
 	 * 
 	 * @return
 	 * @throws SQLException
 	 */
 	public String getLastInsertID() throws SQLException {
 		
 		String qry = "SELECT LAST_INSERT_ID()";
 		String id = null;
 		
 		Statement stmt = conn.createStatement();
 		ResultSet rs = stmt.executeQuery(qry);        
 		rs.clearWarnings();
 		if (rs.next())
 			id = rs.getString(1);
 		
 		stmt.close();
 		return id;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws SQLException 
 	 */
 	public Vector getDstOtherVersions(String idfier, String ofID) throws SQLException{
 		
 		Vector v = getDatasets(null, null, idfier, null, "=", false, true, null);
 		for (int i=0; v!=null && i<v.size(); i++){
 			if (((Dataset)v.get(i)).getID().equals(ofID)){
 				v.remove(i);
 				break;
 			}
 		}
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param idfier
 	 * @param ofID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getElmOtherVersions(String idfier, String ofID) throws SQLException{
 		Vector v = getCommonElements(null, null, null, idfier, false, true, "=");
 		for (int i=0; v!=null && i<v.size(); i++){
 			if (((DataElement)v.get(i)).getID().equals(ofID)){
 				v.remove(i);
 				break;
 			}
 		}
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param id
 	 * @param type
 	 * @return
 	 * @throws Exception
 	 */
 	public boolean isWorkingCopy(String id, String type) throws Exception{
 		
 		if (type==null)
 			throw new Exception("Type not specified!");
 		
 		String tblName = "";
 		if (type.equals("elm"))
 			tblName = "DATAELEM";
 		else if (type.equals("tbl"))
 			tblName = "DS_TABLE";
 		else if (type.equals("dst"))
 			tblName = "DATASET";
 		else
 			throw new Exception("Unknown type!");
 		
 		String idField = type.equals("tbl") ? "TABLE_ID" : tblName+"_ID";
 		
 		INParameters inParams = new INParameters();
 		String q = "select WORKING_COPY from " + tblName + " where " + idField + "=" + inParams.add(id, Types.INTEGER);
 		
 		ResultSet rs = null;
 		PreparedStatement stmt = null;
 		try{
 			stmt = SQL.preparedStatement(q, inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs.next()){
 				if (rs.getString(1).equals("Y"))
 					return true;
 			}
 			else throw new Exception("Could not find such an object!");
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean hasUserWorkingCopies(){
 
 		if (user==null || !user.isAuthentic())
 			return false;
 
 		INParameters inParams = new INParameters();
 		StringBuffer constraints = new StringBuffer();
 		constraints.append("WHERE WORKING_COPY='Y' and WORKING_USER=").append(inParams.add(user.getUserName()));
 		
 		ResultSet rs = null;
 		PreparedStatement stmt = null;
 		try{
 			stmt = SQL.preparedStatement("SELECT * FROM DATAELEM " + constraints.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs.next())
 				return true;
 			
 			rs.close();
 			stmt.close();
 			
 			stmt = SQL.preparedStatement("SELECT * FROM DATASET " + constraints.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs.next())
 				return true;
 		}
 		catch (SQLException sqle) {
 			logger.fatal(sqle.toString(), sqle);
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @param attrID
 	 * @param attrType
 	 * @return
 	 * @throws SQLException 
 	 */
 	public int getAttributeUseCount(String attrID, String attrType) throws SQLException{
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer sql = new StringBuffer();
 		if (attrType.equals(DElemAttribute.TYPE_COMPLEX))
 			sql.append("select count(distinct PARENT_TYPE, PARENT_ID) from COMPLEX_ATTR_ROW where M_COMPLEX_ATTR_ID=").
 			append(inParams.add(attrID, Types.INTEGER));
 		else
 			sql.append("select count(distinct PARENT_TYPE, DATAELEM_ID) from ATTRIBUTE where M_ATTRIBUTE_ID=").
 			append(inParams.add(attrID, Types.INTEGER));
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = SQL.preparedStatement(sql.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs.next())
 				return rs.getInt(1);
 		}
 		finally{
 			try{
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException sqle) {}
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * 
 	 * @param relID
 	 * @return
 	 * @throws SQLException
 	 */    
 	public Hashtable getFKRelation(String relID) throws SQLException{
 		
 		if (Util.nullString(relID))
 			return null;
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf = new StringBuffer("select ").
 		append("A_ELM.SHORT_NAME as A_NAME, A_TBL.SHORT_NAME as A_TABLE, ").
 		append("B_ELM.SHORT_NAME as B_NAME, B_TBL.SHORT_NAME as B_TABLE, ").
 		append("FK_RELATION.* ").
 		append("from FK_RELATION ").
 		append("left outer join DATAELEM as A_ELM ").
 		append("on FK_RELATION.A_ID=A_ELM.DATAELEM_ID ").
 		append("left outer join DATAELEM as B_ELM on ").
 		append("FK_RELATION.B_ID=B_ELM.DATAELEM_ID ").
 		append("left outer join DS_TABLE as A_TBL ").
 		append("on A_ELM.PARENT_NS=A_TBL.CORRESP_NS ").
 		append("left outer join DS_TABLE as B_TBL ").
 		append("on B_ELM.PARENT_NS=B_TBL.CORRESP_NS ").
 		append("where REL_ID=").
 		append(inParams.add(relID, Types.INTEGER));
 		
 		logger.debug(buf.toString());
 		
 		Hashtable hash = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs.next()){
 				
 				hash = new Hashtable();
 				hash.put("a_id", rs.getString("A_ID"));
 				hash.put("a_name", rs.getString("A_NAME"));
 				hash.put("a_tbl", rs.getString("A_TABLE"));
 				hash.put("a_cardin", rs.getString("A_CARDIN"));
 				
 				hash.put("b_id", rs.getString("B_ID"));
 				hash.put("b_name", rs.getString("B_NAME"));
 				hash.put("b_tbl", rs.getString("B_TABLE"));
 				hash.put("b_cardin", rs.getString("B_CARDIN"));
 				hash.put("cardin", rs.getString("A_CARDIN") + " to " + rs.getString("B_CARDIN"));
 				
 				hash.put("definition", rs.getString("FK_RELATION.DEFINITION"));
 			}
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return hash;
 	}
 	
 	/**
 	 * 
 	 * @param elmID
 	 * @return
 	 * @throws SQLException
 	 */    
 	public Vector getFKRelationsElm(String elmID) throws SQLException{
 		return getFKRelationsElm(elmID, null);
 	}
 	
 	/**
 	 * 
 	 * @param elmID
 	 * @return
 	 * @throws SQLException
 	 */    
 	public Vector getFKRelationsElm(String elmID, String dstID) throws SQLException{
 		
 		Vector v = new Vector();
 		ResultSet rs = null;
 		PreparedStatement stmt = null;
 		try{
 			for (int i=1; i<=2; i++){
 				
 				String side = i==1 ? "A_ID" : "B_ID";
 				String contraSide = i==1 ? "B_ID" : "A_ID";
 				
 				INParameters inParams = new INParameters();
 				StringBuffer buf = new StringBuffer("select distinct ");
 				buf.append("DATAELEM.SHORT_NAME, ");
 				buf.append("DATAELEM.DATAELEM_ID, ");
 				buf.append("DS_TABLE.SHORT_NAME, ");
 				buf.append("DST2TBL.DATASET_ID, ");
 				buf.append("FK_RELATION.* ");
 				buf.append("from FK_RELATION ");
 				buf.append("left outer join DATAELEM on FK_RELATION.");
 				buf.append(contraSide);
 				buf.append("=DATAELEM.DATAELEM_ID ");
 				buf.append("left outer join TBL2ELEM on ");
 				buf.append("DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID ");
 				buf.append("left outer join DS_TABLE on ");
 				buf.append("TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID ");
 				buf.append("left outer join DST2TBL on ");
 				buf.append("DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ");
 				if (!Util.nullString(elmID)){
 					buf.append(" where ");
 					buf.append(side);
 					buf.append("=");
 					buf.append(inParams.add(elmID, Types.INTEGER));
 				}
 	
 				// close rs and stmt if they were opened in the loop's previous step
 				try{
 					if (rs!=null) rs.close();
 					if (stmt!=null) stmt.close();
 				}
 				catch (SQLException e){}
 	
 				HashSet added = new HashSet();
 				stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 				rs = stmt.executeQuery();
 				while (rs.next()){
 					
 					if (dstID!=null){
 						String ds = rs.getString("DST2TBL.DATASET_ID");
 						if (!dstID.equals(ds))
 							continue;
 					}
 					
 					Hashtable hash = new Hashtable();				
 					String id = rs.getString("DATAELEM.DATAELEM_ID");
 					if (id==null)
 						continue;
 					if (added.contains(id))
 						continue;
 					else
 						added.add(id);		
 					hash.put("elm_id", id);
 					hash.put("elm_name", rs.getString("DATAELEM.SHORT_NAME"));
 					hash.put("tbl_name", rs.getString("DS_TABLE.SHORT_NAME"));
 					hash.put("rel_id", rs.getString("REL_ID"));
 					
 					hash.put("a_cardin", rs.getString("A_CARDIN"));
 					hash.put("b_cardin", rs.getString("B_CARDIN"));
 					hash.put("definition", rs.getString("DEFINITION"));
 					hash.put("cardin", rs.getString("A_CARDIN") + " to " +
 							rs.getString("B_CARDIN"));
 					
 					v.add(hash);
 				}
 			}
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return v;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getHarvesters() throws SQLException {
 		
 		String q = "select distinct HARVESTER_ID from HARV_ATTR";
 
 		Vector v = new Vector();
 		Statement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(q);
 			while (rs.next())
 				v.add(rs.getString("HARVESTER_ID"));
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param attrID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getHarvesterFieldsByAttr(String attrID) throws SQLException {
 		return getHarvesterFieldsByAttr(attrID, true);
 	}
 	
 	/**
 	 * 
 	 * @param attrID
 	 * @param all
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getHarvesterFieldsByAttr(String attrID, boolean all) throws SQLException {
 		return getHarvesterFieldsByAttr(attrID, all, null);
 	}
 	
 	/**
 	 * 
 	 * @param attrID
 	 * @param all
 	 * @param includeFields
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getHarvesterFieldsByAttr(String attrID, boolean all, HashSet includeFields) throws SQLException {
 		
 		String q = null;		
 		Vector v = new Vector();
 		HashSet taken = new HashSet();
 		
 		// fields must be returned in the following order
 		Vector order = new Vector();
 		order.add("ID");
 		order.add("NAME");
 		order.add("COUNTRY");
 		order.add("URL");
 		order.add("ADDRESS");
 		order.add("PHONE");
 		order.add("DESCRIPTION");
 		
 		INParameters inParams = new INParameters();
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		
 		try{
 			if (!all){
 				q = "select distinct HARV_ATTR_FLD_NAME from " +
 				"M_COMPLEX_ATTR_FIELD where HARV_ATTR_FLD_NAME is not null " +
 				"and M_COMPLEX_ATTR_ID=" + inParams.add(attrID, Types.INTEGER);
 				stmt = SQL.preparedStatement(q, inParams, conn);
 				rs = stmt.executeQuery();
 				while (rs.next())
 					taken.add(rs.getString(1));
 			}
 			
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 			
 			inParams = new INParameters();
 			q =
 				"select distinct HARV_ATTR_FIELD.FLD_NAME " +
 				"from " +
 				"M_COMPLEX_ATTR " +
 				"left outer join HARV_ATTR on " +
 				"M_COMPLEX_ATTR.HARVESTER_ID=HARV_ATTR.HARVESTER_ID " +
 				"left outer join HARV_ATTR_FIELD on " +
 				"HARV_ATTR.MD5KEY=HARV_ATTR_FIELD.HARV_ATTR_MD5 " +
 				"where M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID=" + inParams.add(attrID, Types.INTEGER) +
 				" and M_COMPLEX_ATTR.HARVESTER_ID is not null " +
 				"order by HARV_ATTR_FIELD.FLD_NAME asc";
 			
 			stmt = SQL.preparedStatement(q, inParams, conn);
 			rs = stmt.executeQuery();
 			while (rs.next()){
 				String fldName = rs.getString("FLD_NAME");
 				if (fldName!=null){
 					if ((includeFields!=null && includeFields.contains(fldName)) || !taken.contains(fldName)){
 						
 						// ordering logic
 						String uc = new String(fldName);
 						int pos = order.indexOf(uc.toUpperCase());
 						if (pos == -1)
 							v.add(fldName);
 						else{
 							if (pos > v.size()-1)
 								v.setSize(pos+1);
 							v.add(pos, fldName);
 						}
 					}
 				}
 			}
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		// ordering might have left some null objects in there
 		for (int i=0; i<v.size(); i++)
 			if (v.get(i)==null) v.remove(i--);
 		
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param attrID
 	 * @param parentID
 	 * @param unUsed
 	 * @return
 	 * @throws SQLException
 	 */
 	public HashSet getHarvestedAttrIDs(String attrID, String parentID, String unUsed) throws SQLException{
 		
 		INParameters inParams = new INParameters();		
 		String q =  "select distinct HARV_ATTR_ID from COMPLEX_ATTR_ROW where M_COMPLEX_ATTR_ID=" +
 					inParams.add(attrID, Types.INTEGER) + " and PARENT_ID=" + inParams.add(parentID, Types.INTEGER) + " and PARENT_TYPE='DS'";
 		
 		HashSet hashSet = new HashSet();
 		ResultSet rs = null;
 		PreparedStatement stmt = null;
 		try{
 			stmt = SQL.preparedStatement(q, inParams, conn);
 			rs = stmt.executeQuery();
 			while (rs.next()){
 				String id = rs.getString(1);
 				if (id!=null) 
 					hashSet.add(id);
 			}
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return hashSet;
 	}
 	
 	/**
 	 * 
 	 */
 	public Vector getHarvestedAttrs(String attrID) throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		String q =
 			"select distinct HARVESTED " +
 			"from " +
 			"M_COMPLEX_ATTR " +
 			"left outer join HARV_ATTR on " +
 			"M_COMPLEX_ATTR.HARVESTER_ID=HARV_ATTR.HARVESTER_ID " +
 			"where M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID=" + inParams.add(attrID, Types.INTEGER) +
 			" and M_COMPLEX_ATTR.HARVESTER_ID is not null " +
 			"order by HARVESTED desc";
 		
 		Vector vv = new Vector();
 		String lastHarvested = null;
 		
 		ResultSet rs = null;
 		PreparedStatement stmt = null;
 		try{
 			stmt = SQL.preparedStatement(q, inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs.next())
 				lastHarvested = rs.getString(1);
 			
 			if (Util.nullString(lastHarvested))
 				return vv;
 			
 			inParams = new INParameters();
 			q =
 				"select distinct MD5KEY, LOGICAL_ID " +
 				"from " +
 				"M_COMPLEX_ATTR " +
 				"left outer join HARV_ATTR on " +
 				"M_COMPLEX_ATTR.HARVESTER_ID=HARV_ATTR.HARVESTER_ID " +
 				"where M_COMPLEX_ATTR.M_COMPLEX_ATTR_ID=" + inParams.add(attrID, Types.INTEGER) +
 				" and M_COMPLEX_ATTR.HARVESTER_ID is not null " +
 				" and HARVESTED=" + inParams.add(lastHarvested, Types.BIGINT);
 	
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 	
 			Hashtable harvAttrs = new Hashtable();
 			stmt = SQL.preparedStatement(q, inParams, conn);
 			rs = stmt.executeQuery();
 			while (rs.next())
 				harvAttrs.put(rs.getString("MD5KEY"), rs.getString("LOGICAL_ID"));
 	
 			inParams = new INParameters();
 			q = 
 				"select distinct FLD_NAME, FLD_VALUE from HARV_ATTR_FIELD " +
 				"where HARV_ATTR_MD5=?";
 	
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 	
 			stmt = conn.prepareStatement(q);
 			Enumeration enumer = harvAttrs.keys();
 			while (enumer.hasMoreElements()){
 				String md5key = (String)enumer.nextElement();
 				String harvAttrID = (String)harvAttrs.get(md5key);
 				
 				Hashtable hash = new Hashtable();
 				hash.put("harv_attr_id", harvAttrID);
 				
 				stmt.setString(1, md5key);
 				rs = stmt.executeQuery();
 				while (rs.next()){
 					hash.put(rs.getString("FLD_NAME"), rs.getString("FLD_VALUE"));
 				}
 				
 				vv.add(hash);				
 			}
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		// order the results by the NAME field
 		for (int i=1; i<vv.size(); i++){
 			Hashtable ih = (Hashtable)vv.get(i);
 			String iname = (String)ih.get("NAME");
 			if (iname==null) continue;
 			for (int j=0; j<i; j++){
 				Hashtable jh = (Hashtable)vv.get(j);
 				String jname = (String)jh.get("NAME");
 				if (jname==null) continue;
 				if (iname.compareToIgnoreCase(jname)<0){
 					vv.remove(i);
 					vv.add(j, ih);
 					break;
 				}
 			}
 		}
 		
 		return vv;	
 	}
 	
 	/**
 	 * 
 	 * @param attrID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Hashtable getHarvestedAttrFieldsHash(String attrID) throws SQLException{
 		Hashtable hash = new Hashtable();
 		Vector v = getAttrFields(attrID);
 		for (int i=0; v!=null && i<v.size(); i++){
 			Hashtable fld = (Hashtable)v.get(i);
 			String harvAttrFldName = (String)fld.get("harv_fld");
 			if (harvAttrFldName!=null)
 				hash.put(harvAttrFldName, fld.get("id")); 
 		}
 		
 		return hash;
 	}
 
 	/**
 	 * 
 	 * @param tblID
 	 * @return
 	 * @throws SQLException
 	 */
 	public boolean hasGIS(String tblID) throws SQLException{
 		
 		if (tblID==null)
 			return false;
 		
 		INParameters inParams = new INParameters();
 		StringBuffer buf = new StringBuffer("select count(*) ").
 		append("from DATAELEM left outer join TBL2ELEM on ").
 		append("DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID ").
 		append("where DATAELEM.GIS is not null and TBL2ELEM.TABLE_ID=").
 		append(inParams.add(tblID, Types.INTEGER));
 		
 		ResultSet rs = null;
 		PreparedStatement stmt = null;
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs.next() && rs.getInt(1)>0)
 				return true;
 			else
 				return false;
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 	}
 
 	/**
 	 * 
 	 * @param ownerID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDocs(String ownerID) throws SQLException{
 		return getDocs(ownerID, "dst");
 	}
 
 	/**
 	 * 
 	 * @param ownerID
 	 * @param ownerType
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getDocs(String ownerID, String ownerType) throws SQLException{
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf = new StringBuffer("select * from DOC where ").
 		append("OWNER_ID=").append(inParams.add(ownerID, Types.INTEGER)).
 		append(" and OWNER_TYPE=").append(inParams.add(ownerType)).
 		append(" order by TITLE asc");
 		
 		Vector v = new Vector();
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			while (rs.next()){
 				String file = rs.getString("ABS_PATH");
 				Hashtable hash = new Hashtable();
 				hash.put("md5", rs.getString("MD5_PATH"));
 				hash.put("file", file);
 				hash.put("icon", eionet.util.Util.getIcon(file));
 				hash.put("title", rs.getString("TITLE"));
 				v.add(hash);
 			}
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param shortName
 	 * @param attrType
 	 * @return
 	 */
 	public String getAttrHelpByShortName(String shortName, String attrType){
 		if (shortName==null) return "";
 		if (attrType==null)
 			return getSimpleAttrHelpByShortName(shortName);
 		else if (attrType.equals(DElemAttribute.TYPE_SIMPLE))
 			return getSimpleAttrHelpByShortName(shortName);
 		else if (attrType.equals(DElemAttribute.TYPE_COMPLEX))
 			return getComplexAttrHelpByShortName(shortName);
 		else
 			return getSimpleAttrHelpByShortName(shortName);
 	}
 	
 	/**
 	 * 
 	 * @param attrID
 	 * @param attrType
 	 * @return
 	 */
 	public String getAttrHelp(String attrID, String attrType){
 		if (attrID==null) return "";
 		if (attrType==null)
 			return getSimpleAttrHelp(attrID);
 		else if (attrType.equals(DElemAttribute.TYPE_SIMPLE))
 			return getSimpleAttrHelp(attrID);
 		else if (attrType.equals(DElemAttribute.TYPE_COMPLEX))
 			return getComplexAttrHelp(attrID);
 		else
 			return getSimpleAttrHelp(attrID);
 	}
 	
 	/**
 	 * 
 	 * @param attrID
 	 * @return
 	 */
 	public String getSimpleAttrHelp(String attrID){
 		return getSimpleAttrHelp("M_ATTRIBUTE_ID", attrID);
 	}
 	
 	/**
 	 * 
 	 * @param shortName
 	 * @return
 	 */
 	public String getSimpleAttrHelpByShortName(String shortName){
 		return getSimpleAttrHelp("SHORT_NAME", Util.strLiteral(shortName));
 	}
 	
 	/**
 	 * 
 	 * @param field
 	 * @param value
 	 * @return
 	 */
 	public String getSimpleAttrHelp(String field, String value){
 		
 		StringBuffer retBuf = new StringBuffer();
 		INParameters inParams = new INParameters();
 		
 		StringBuffer qryBuf = new StringBuffer("select * from M_ATTRIBUTE where ");
 		qryBuf.append(field).append("=").append(inParams.add(value, Types.VARCHAR));
 		
 		if (field!=null && value!=null){
 			
 			PreparedStatement stmt = null;
 			ResultSet rs = null;
 			try{
 				stmt = SQL.preparedStatement(qryBuf.toString(), inParams, conn);
 				rs = stmt.executeQuery();
 				if (rs.next()){
 					retBuf.append("<br/><b>").append(rs.getString("SHORT_NAME")).
 					append("</b><br/><br/>").append(rs.getString("DEFINITION")); 
 				}
 			}
 			catch (SQLException e){
 			}
 			finally{
 				try{
 					if (rs!=null) rs.close();
 					if (stmt!=null) stmt.close();
 				}
 				catch (SQLException e){}
 			}
 		}
 		
 		return retBuf.toString();
 	}
 	
 	/**
 	 * 
 	 * @param attrID
 	 * @return
 	 */
 	public String getComplexAttrHelp(String attrID){
 		return getComplexAttrHelp("M_COMPLEX_ATTR_ID", attrID);
 	}
 	
 	/**
 	 * 
 	 * @param shortName
 	 * @return
 	 */
 	public String getComplexAttrHelpByShortName(String shortName){
 		return getComplexAttrHelp("SHORT_NAME", Util.strLiteral(shortName));
 	}
 	
 	/**
 	 * 
 	 * @param field
 	 * @param value
 	 * @return
 	 */
 	public String getComplexAttrHelp(String field, String value){
 		
 		StringBuffer retBuf = new StringBuffer();
 		INParameters inParams = new INParameters();
 		
 		StringBuffer qryBuf = new StringBuffer("select * from M_COMPLEX_ATTR where ");
 		qryBuf.append(field).append("=").append(inParams.add(value, Types.VARCHAR));
 		
 		if (field!=null && value!=null){
 			PreparedStatement stmt = null;
 			ResultSet rs = null;
 			try{
 				stmt = SQL.preparedStatement(qryBuf.toString(), inParams, conn);
 				rs = stmt.executeQuery();
 				if (rs.next()){
 					retBuf.append("<br/><b>").append(rs.getString("SHORT_NAME")).
 					append("</b><br/><br/>").append(rs.getString("DEFINITION")); 
 				}
 			}
 			catch (SQLException e){
 			}
 			finally{
 				try{
 					if (rs!=null) rs.close();
 					if (stmt!=null) stmt.close();
 				}
 				catch (SQLException e){}
 			}
 		}
 		
 		return retBuf.toString();
 	}
 	
 	/**
 	 * 
 	 * @param objID
 	 * @param objType
 	 * @param article
 	 * @return
 	 * @throws SQLException
 	 */
 	public String getCacheFileName(String objID, String objType, String article) throws SQLException{
 		
 		if (objID==null || objType==null || article==null)
 			throw new SQLException("getCacheFileName(): objID or objType or article is null");
 		
 		INParameters inParams = new INParameters();
 		StringBuffer buf = new StringBuffer("select FILENAME from CACHE where ").
 		append("OBJ_ID=").append(inParams.add(objID, Types.INTEGER)).
 		append(" and OBJ_TYPE=").append(inParams.add(objType)).
 		append(" and ARTICLE=").append(inParams.add(article));
 		
 		String fileName = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			fileName = rs.next() ? rs.getString(1) : null;
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		return fileName;
 	}
 	
 	/**
 	 * 
 	 * @param objID
 	 * @param objType
 	 * @return
 	 * @throws SQLException
 	 */
 	public Hashtable getCache(String objID, String objType) throws SQLException{
 		
 		if (objID==null || objType==null)
 			throw new SQLException("getCache(): objID or objType or article is null");
 		
 		INParameters inParams = new INParameters();
 		StringBuffer buf = new StringBuffer("select * from CACHE where ").
 		append("OBJ_ID=").append(inParams.add(objID, Types.INTEGER)).
 		append(" and OBJ_TYPE=").append(inParams.add(objType));
 		
 		Hashtable result = new Hashtable();
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			while (rs.next()){
 				String filename = rs.getString("FILENAME");
 				String article = rs.getString("ARTICLE");
 				Long created = new Long(rs.getLong("CREATED"));
 				if (Util.nullString(filename))
 					continue;
 				
 				Hashtable hash = new Hashtable();
 				hash.put("filename", filename);
 				hash.put("created", created);
 				
 				result.put(rs.getString("ARTICLE"), hash);
 			}
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 			
 		
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param dstID
 	 * @return
 	 * @throws Exception
 	 */
 	public Vector getRodLinks(String dstID) throws Exception{
 		
 		if (Util.nullString(dstID))
 			throw new Exception("getRodLinks(): dstID missing!");
 		
 		Vector v = new Vector();
 		
 		INParameters inParams = new INParameters();
 		StringBuffer buf =
 			new StringBuffer("select distinct ROD_ACTIVITIES.* from DST2ROD, ROD_ACTIVITIES where ").
 			append("DST2ROD.ACTIVITY_ID=ROD_ACTIVITIES.ACTIVITY_ID and DST2ROD.DATASET_ID=").
 			append(inParams.add(dstID, Types.INTEGER));
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			while (rs.next()){
 				Hashtable hash = new Hashtable();
 				String raID = rs.getString("ACTIVITY_ID");
 				hash.put("ra-id", raID);
 				hash.put("ra-title", rs.getString("ACTIVITY_TITLE"));
 				hash.put("li-id", rs.getString("LEGINSTR_ID"));
 				hash.put("li-title", rs.getString("LEGINSTR_TITLE"));
 				
 				String raURL = Props.getProperty(PropsIF.INSERV_ROD_RA_URLPATTERN);
 				int i = raURL.indexOf(PropsIF.INSERV_ROD_RA_IDPATTERN);
 				if (i==-1) throw new Exception("Invalid property " + PropsIF.INSERV_ROD_RA_URLPATTERN);
 				raURL = new StringBuffer(raURL).
 				replace(i, i + PropsIF.INSERV_ROD_RA_IDPATTERN.length(), raID).toString();
 				
 				hash.put("ra-url", raURL);
 				
 				v.add(hash);
 			}
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException sqle){}
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * 
 	 * @param raID
 	 * @return
 	 * @throws Exception
 	 */
 	public Vector getParametersByActivityID(String raID) throws Exception{
 		
 		if (Util.nullString(raID))
 			throw new Exception("getParametersByActivityID(): activity ID missing!");
 		
 		Vector result = new Vector();
 		
 		INParameters inParams = new INParameters();
 		StringBuffer qryOfDatasets = new StringBuffer().
 		append("select distinct DATASET.DATASET_ID, DATASET.SHORT_NAME, DATASET.IDENTIFIER, ").
 		append("DATASET.VERSION from DST2ROD, DATASET where DST2ROD.ACTIVITY_ID=").append(inParams.add(raID, Types.INTEGER)).
 		append(" and DST2ROD.DATASET_ID=DATASET.DATASET_ID and DATASET.DELETED is null ").
 		append("order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc");
 		
 		StringBuffer qryOfParameters = new StringBuffer().
 		append("select distinct DATAELEM.DATAELEM_ID, DATAELEM.TYPE, DATAELEM.SHORT_NAME, ").
 		append("DS_TABLE.SHORT_NAME from DST2TBL ").
 		append("left outer join DS_TABLE on DST2TBL.TABLE_ID=DS_TABLE.TABLE_ID ").
 		append("left outer join TBL2ELEM on DST2TBL.TABLE_ID=TBL2ELEM.TABLE_ID ").
 		append("left outer join DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID ").
 		append("where DST2TBL.DATASET_ID=? and DS_TABLE.TABLE_ID is not null and ").
 		append("DATAELEM.DATAELEM_ID is not null and DATAELEM.IS_ROD_PARAM='true' ").
 		append("order by DS_TABLE.SHORT_NAME, DATAELEM.SHORT_NAME");
 		
 		ResultSet rsOfDatasets = null;
 		PreparedStatement stmtOfDatasets = null;
 		ResultSet rsOfParams = null;
 		PreparedStatement stmtOfParams = null;
 		try{
 			stmtOfParams = conn.prepareStatement(qryOfParameters.toString());
 			stmtOfDatasets = SQL.preparedStatement(qryOfDatasets.toString(), inParams, conn);
 			rsOfDatasets = stmtOfDatasets.executeQuery();
 			String curDstIdf = null;
 			while (rsOfDatasets.next()){
 				String dstIdf = rsOfDatasets.getString("DATASET.IDENTIFIER");
 				if (curDstIdf!=null && curDstIdf.equals(dstIdf))
 					continue;
 				curDstIdf = dstIdf;
 				
 				String dstName = rsOfDatasets.getString("DATASET.SHORT_NAME");
 				int dstID = rsOfDatasets.getInt("DATASET.DATASET_ID");
 				stmtOfParams.setInt(1,dstID);
 				rsOfParams = stmtOfParams.executeQuery();
 				while(rsOfParams.next()){
 					Hashtable hash = new Hashtable();
 					hash.put("elm-name", rsOfParams.getString("DATAELEM.SHORT_NAME"));
 					hash.put("tbl-name", rsOfParams.getString("DS_TABLE.SHORT_NAME"));
 					hash.put("dst-name", dstName);
 					
 					String elmID = rsOfParams.getString("DATAELEM.DATAELEM_ID");
 					String elmUrl = Props.getProperty(PropsIF.OUTSERV_ELM_URLPATTERN);
 					int i = elmUrl.indexOf(PropsIF.OUTSERV_ELM_IDPATTERN);
 					if (i==-1) throw new Exception(
 							"Invalid property " + PropsIF.OUTSERV_ELM_URLPATTERN);
 					elmUrl = new StringBuffer(elmUrl).
 					replace(i, i + PropsIF.OUTSERV_ELM_IDPATTERN.length(), elmID).toString();
 					
 					hash.put("elm-url", elmUrl);
 					
 					result.add(hash);
 				}
 			}
 		}
 		finally{
 			try{
 				if (stmtOfDatasets!=null) stmtOfDatasets.close();
 				if (stmtOfParams!=null) stmtOfParams.close();
 				if (rsOfParams!=null) rsOfParams.close();
 				if (rsOfDatasets!=null) rsOfDatasets.close();
 				
 			}
 			catch (SQLException sqle){}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * This one returns the IDs and titles of all ogligations that have a released dataset definition present in DD.
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	public Vector getObligationsWithDatasets() throws Exception{
 		
 		Vector obligations = new Vector();
 		
 		Vector datasets = getDatasets(null, null, null, null, null, false);
 		Vector releasedDatasets = new Vector();
 		for (int i=0; datasets!=null && i<datasets.size(); i++){
 			Dataset dst = (Dataset)datasets.get(i);
 			String status = dst.getStatus();
 			if (status==null || !status.equals("Released")) continue;
 			
 			// see from ROD links
 			Vector rodLinks = getRodLinks(dst.getID());
 			for (int j=0; rodLinks!=null && j<rodLinks.size(); j++){
 				Hashtable rodLink = (Hashtable)rodLinks.get(j);
 				String obligID = (String)rodLink.get("ra-id");
 				String obligTitle = (String)rodLink.get("ra-title");
 				if (obligTitle==null) obligTitle = "";
 				if (obligID!=null && obligID.length()>0){
 					
 					obligID = rodObligUrl + obligID;
 					
 					Hashtable hash = new Hashtable();
 					hash.put(predIdentifier, obligID.trim());
 					hash.put(predTitle, obligTitle.trim());
 					
 					if (!obligations.contains(hash))
 						obligations.add(hash);
 				}
 			}
 			
 			// see from the ROD complex attribute
 			if (obligations.size()==0){
 				Vector v = getComplexAttrValueRowHashes("ROD", dst.getID(), "DS");
 				for (int k=0; v!=null && k<v.size(); k++){
 					Hashtable valueRowHash = (Hashtable)v.get(k);
 					
 					String obligID = null;
 					String obligUrl = (String)valueRowHash.get("url");
 					if (obligUrl!=null && obligUrl.length()>0)
 						obligID = eionet.util.Util.getObligationID(obligUrl);
 					
 					String obligTitle = (String)valueRowHash.get("name");
 					if (obligTitle==null) obligTitle = "";
 					if (obligID!=null && obligID.length()>0){
 						
 						obligID = rodObligUrl + obligID;
 						
 						Hashtable hash = new Hashtable();
 						hash.put(predIdentifier, obligID.trim());
 						hash.put(predTitle, obligTitle.trim());
 						
 						if (!obligations.contains(hash))
 							obligations.add(hash);
 					}
 				}
 			}
 		}
 		
 		return obligations;
 	}
 
 	/**
 	 * 
 	 * @param attrShortName
 	 * @param parentID
 	 * @param parentType
 	 * @return
 	 * @throws Exception
 	 */
 	public Vector getComplexAttrValueRowHashes(String attrShortName,
 			String parentID, String parentType) throws Exception{
 		
 		Vector result = new Vector();
 		
 		Vector complexAttrs = getComplexAttributes(parentID, parentType);
 		for (int i=0; complexAttrs!=null && i<complexAttrs.size(); i++){
 			DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
 			String attrID = attr.getID();
 			String sname = attr.getShortName();
 			if (sname==null || !sname.equalsIgnoreCase(attrShortName)) continue;
 			
 			Vector attrFields = getAttrFields(attrID);
 			Vector rows = attr.getRows();
 			for (int j=0; rows!=null && j<rows.size(); j++){
 				Hashtable rowHash = (Hashtable)rows.get(j);
 				Hashtable resultHash = new Hashtable();
 				for (int t=0; t<attrFields.size(); t++){
 					
 					Hashtable fieldHash = (Hashtable)attrFields.get(t);
 					String fieldID = (String)fieldHash.get("id");
 					String fieldValue = null;
 					if (fieldID!=null) fieldValue = (String)rowHash.get(fieldID);
 					String fieldName = (String)fieldHash.get("name");
 					
 					if (fieldName==null  || fieldName.length()==0) continue;
 					if (fieldValue==null || fieldValue.trim().length()==0) continue;
 					
 					resultHash.put(fieldName, fieldValue); 
 				}
 				
 				if (resultHash.size()>0) result.add(resultHash);
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param elmID
 	 * @return
 	 * @throws SQLException
 	 */
 	public Vector getReferringTables(String elmID) throws SQLException{
 		
 		// JH110705 - first get the owners of datasets (we need to display them)
 		StringBuffer qry = new StringBuffer("select * from ACLS where PARENT_NAME='/datasets'");
 		
 		Hashtable owners = new Hashtable();
 		Statement stmt1 = null;
 		ResultSet rs = null;
 		try{
 			stmt1 = conn.createStatement();
 			rs = stmt1.executeQuery(qry.toString());
 			while (rs.next()){
 				String idf = rs.getString("ACL_NAME");
 				String own = rs.getString("OWNER");
 				if (idf!=null && own!=null) owners.put(idf, own);
 			}
 		}
 		finally{
 			try{
 				if (stmt1!=null) stmt1.close();
 				if (rs!=null) rs.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		INParameters inParams = new INParameters();
 		
 		// and now get the referring tables
 		qry = new StringBuffer("select DS_TABLE.*, ").
 		append("DATASET.DATASET_ID,DATASET.IDENTIFIER,DATASET.SHORT_NAME,DATASET.REG_STATUS ").
 		append("from TBL2ELEM ").
 		append("left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID ").
 		append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ").
 		append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ").
 		append("where TBL2ELEM.DATAELEM_ID=").append(inParams.add(elmID, Types.INTEGER)).append(" and ").
 		append("DS_TABLE.TABLE_ID is not null and ").
 		append("DATASET.DELETED is null ").
 		append("order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc, ").
 		append("DS_TABLE.IDENTIFIER asc, DS_TABLE.TABLE_ID desc");
 		
 		Vector result = new Vector();
 		PreparedStatement stmt = null;
 		rs = null;
 		try{
 			stmt = SQL.preparedStatement(qry.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			
 			String curDstID  = null;
 			String curDstIdf = null;
 			String curTblIdf = null;
 			while (rs.next()){
 				
 				String dstID  = rs.getString("DATASET.DATASET_ID");
 				String dstIdf = rs.getString("DATASET.IDENTIFIER");
 				if (dstID==null && dstIdf==null)
 					continue;
 				
 				// the following if block skips tables in non-latest DATASETS
 				if (curDstIdf==null || !curDstIdf.equals(dstIdf)){
 					curDstIdf = dstIdf;
 					curDstID = dstID;
 				}
 				else if (!dstID.equals(curDstID))
 					continue;
 				
 				String tblIdf = rs.getString("DS_TABLE.IDENTIFIER");
 				if (tblIdf==null) continue;
 				
 				// the following if block skips non-latest TABLES
 				if (curTblIdf!=null && tblIdf.equals(curTblIdf))
 					continue;
 				else
 					curTblIdf = tblIdf;
 				
 				// see if the table should be skipped by DATASET.REG_STATUS
 				String dstStatus = rs.getString("DATASET.REG_STATUS");
 				if (skipByRegStatus(dstStatus)) continue;
 				
 				// start constructing the table
 				DsTable tbl = new DsTable(rs.getString("DS_TABLE.TABLE_ID"),
 						rs.getString("DATASET.DATASET_ID"),
 						rs.getString("DS_TABLE.SHORT_NAME"));
 				
 				tbl.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
 				tbl.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));
 				tbl.setIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));
 				
 				tbl.setDatasetName(rs.getString("DATASET.SHORT_NAME"));
 				tbl.setDstIdentifier(dstIdf);
 				if (dstIdf!=null) tbl.setOwner((String)owners.get(dstIdf));
 				
 				tbl.setCompStr(tbl.getShortName());
 				result.add(tbl);
 			}
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 				if (stmt1!=null) stmt1.close();
 			}
 			catch (SQLException e){}
 		}
 		
 		Collections.sort(result);
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param dstID
 	 * @return
 	 * @throws SQLException
 	 */
 	public String getDatasetIdentifier(String dstID) throws SQLException {
 		
 		INParameters inParams = new INParameters();
 		StringBuffer buf = new StringBuffer("select IDENTIFIER from DATASET where DATASET_ID=");
 		buf.append(inParams.add(dstID, Types.INTEGER));
 		
 		ResultSet rs = null;
 		PreparedStatement stmt = null;
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs!=null && rs.next())
 				return rs.getString(1);
 			else
 				return null;
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param elmID
 	 * @return
 	 */
 	public String getElmOwner(String elmIdfier) throws SQLException{
 		
 		INParameters inParams = new INParameters();
 		StringBuffer buf = new StringBuffer("select OWNER from ACLS where PARENT_NAME='/elements' ");
 		buf.append(" and ACL_NAME=").append(inParams.add(elmIdfier));
 		
 		ResultSet rs = null;
 		PreparedStatement stmt = null;
 		try{
 			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
 			rs = stmt.executeQuery();
 			if (rs!=null && rs.next())
 				return rs.getString(1);
 			else
 				return null;
 		}
 		finally{
 			try{
 				if (rs!=null) rs.close();
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 	}
 }
