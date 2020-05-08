 	/**
  * 
  */
 package gov.nih.nci.cadsr.persist.evs;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 import gov.nih.nci.cadsr.cdecurate.tool.ConBean;
 import gov.nih.nci.cadsr.persist.common.BaseVO;
 import gov.nih.nci.cadsr.persist.common.DBConstants;
 import gov.nih.nci.cadsr.persist.exception.DBException;
 import gov.nih.nci.cadsr.persist.common.DBManager;
 import gov.nih.nci.cadsr.persist.concept.Component_Concepts_Ext_Mgr;
 import gov.nih.nci.cadsr.persist.concept.ConVO;
 import gov.nih.nci.cadsr.persist.concept.Concepts_Ext_Mgr;
 import gov.nih.nci.cadsr.persist.de.DeErrorCodes;
 
 /**
  * @author hveerla
  *
  */
 public abstract class Evs_Mgr extends DBManager {
 
 private Logger logger = Logger.getLogger(this.getClass());
 	
  	
 /**
 	 * Inserts a single row of Evs Object(Object Class, Property, Representation) and returns primary key IDSEQ
 	 * 
 	 * @param sql
 	 * @param EVSBean
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	public String insert(String sql, BaseVO vO, Connection conn) throws DBException {
 		EvsVO evsVO = (EvsVO) vO;
 		PreparedStatement statement = null;
 		String primaryKey = null;
 		this.initialize(evsVO, conn);
 		// generate IDSEQ(primary key) 	
 		evsVO.setIDSEQ(this.generatePrimaryKey(conn));
 		evsVO.setDeleted_ind(DBConstants.RECORD_DELETED_NO);
 		evsVO.setBegin_date(new java.sql.Timestamp(new java.util.Date().getTime()));
 		
 		try {
 			int column = 0;
 			statement = conn.prepareStatement(sql);
 		    statement.setString(++column, evsVO.getIDSEQ());
 			statement.setString(++column, evsVO.getPrefferred_name());
 			statement.setString(++column, evsVO.getLong_name());
 			statement.setString(++column, evsVO.getPrefferred_def());
 			statement.setString(++column, evsVO.getConte_IDSEQ());
 			statement.setDouble(++column, evsVO.getVersion());
 			statement.setString(++column, evsVO.getAsl_name());
 			statement.setString(++column, evsVO.getLastest_version_ind());
 			statement.setTimestamp(++column, evsVO.getBegin_date());
 			statement.setString(++column, evsVO.getDefinition_source());
 			statement.setString(++column, evsVO.getOrigin());
 			statement.setString(++column, evsVO.getCreated_by());
 			statement.setString(++column, evsVO.getDeleted_ind());
 			statement.setString(++column, evsVO.getCondr_IDSEQ());
 					
 			int count = statement.executeUpdate();
 			if (count == 0) {
 				throw new Exception("Unable to insert the record");
 			} else {
 				primaryKey = evsVO.getIDSEQ();
 				if (logger.isDebugEnabled()) {
 					logger.debug("Inserted EVS Object");
 					logger.debug("IDSEQ(primary key )-----> " + primaryKey);
 				}
 			}
 
 		} catch (Exception e) {
 			logger.error("Error inserting EVS object " + e);
 			errorList.add("Error inserting EVS object ");
 			throw new DBException(errorList);
 		} finally {
 			statement = SQLHelper.closePreparedStatement(statement);
 		}
 		return primaryKey;
 	}
 	
 	/*
 	 * 
 	 */
 	public ArrayList<ResultVO> isCondrExists(ArrayList<ConBean> list, String sql, Connection conn) throws DBException{
 		Statement statement = null;
 		ResultSet rs = null;
 		ArrayList resultList = new ArrayList();
 		try {
 			StringBuffer sqlBuff = new StringBuffer();
 			sqlBuff.append("WITH conlist AS ( ");
 			if (list != null){
 				for(int i=0; i<list.size(); i++){
 					ConBean con = (ConBean)list.get(i);
 					sqlBuff.append("select '" + con.getCon_IDSEQ() + "' as con_idseq, '");
 					sqlBuff.append(con.getConcept_value() + "' as concept_value, ");
 					sqlBuff.append(con.getDisplay_order() + " as display_order from dual ");
 					if ((i+1) != list.size()){
 						sqlBuff.append("union ");
 					}
 				}
 			}
 			sqlBuff.append(") ");
 			sqlBuff.append(sql);
 			statement = conn.createStatement();
 			rs = statement.executeQuery(sqlBuff.toString());
 			while (rs.next()) {
 				ResultVO  vo = new ResultVO();
 				vo.setCondr_IDSEQ(rs.getString("CONDR_IDSEQ"));
 				vo.setIDSEQ(rs.getString("IDSEQ"));
 				vo.setLong_name(rs.getString("LONG_NAME"));
 				vo.setPublicId(rs.getString("PUBLIC_ID"));
 				vo.setVersion(rs.getString("VERSION"));
 				vo.setAsl_name(rs.getString("ASL_NAME"));
 				vo.setContext(rs.getString("CONTEXT"));
 
 				resultList.add(vo);
 	 	   }
 		} catch (Exception e) {
 			logger.error("Error in isCondrExists method in EVS_Mgr " + e);
 			//errorList.add();
 			throw new DBException(errorList);
 		} finally {
			rs = SQLHelper.closeResultSet(rs);
 			statement = SQLHelper.closeStatement(statement);
     	}
 		return resultList;
 	}
 		
 	public void initialize(EvsVO vo, Connection conn) throws DBException{
 		Component_Concepts_Ext_Mgr  ccmgr = new Component_Concepts_Ext_Mgr();
 		Concepts_Ext_Mgr conMgr = new Concepts_Ext_Mgr();
 		vo.setVersion(1);
 		vo.setAsl_name(DBConstants.ASL_NAME_RELEASED);
 		vo.setLastest_version_ind(DBConstants.LATEST_VERSION_IND_YES);
 		Vector<ConVO> conceptsList = ccmgr.getConceptsByCondrIdseq(vo.getCondr_IDSEQ(), conn);
 		String longName = "";
 		String preferredName = "";
 		String prefferredDef ="";
 		String defSource ="";
 		String origin = "";
 		for(int i=0; i<conceptsList.size(); i++){
 			ConVO conVO = conceptsList.get(i);
 			EvsVO conceptVO = conMgr.getConceptByIdseq(conVO.getConIDSEQ(), conn);
 			String lName = "";
 			String pName = "";
 			String pDef	= "";
 			
 			if (conVO.getConcept_value() != null && !conVO.getConcept_value().equals("")) {
 				lName = conceptVO.getLong_name() + "::"	+ conVO.getConcept_value();
 				pName = conceptVO.getPrefferred_name() + "::" + conVO.getConcept_value();
 				pDef = conceptVO.getPrefferred_def() + "::"	+ conVO.getConcept_value();
 			} else {
 				lName = conceptVO.getLong_name();
 				pName = conceptVO.getPrefferred_name();
 				pDef = conceptVO.getPrefferred_def();
      		}
 		
 			if (lName != null && !lName.equals("")) {
 				if (longName.equals("")) {
 					longName = lName;
 				} else {
 					longName = longName + " " + lName;
 				}
 			}
 			if (pName != null && !pName.equals("")) {
 				if (preferredName.equals("")) {
 					preferredName = pName;
 				} else {
 					preferredName = preferredName + ":" + pName;
 				}
 			}
 			if (pDef != null & !pDef.equals("")) {
 				if (prefferredDef.equals("")) {
 					prefferredDef = pDef;
 				} else {
 					prefferredDef = prefferredDef + ":" + pDef;
 				}
 			}
 			if (conceptVO.getDefinition_source() != null && !conceptVO.getDefinition_source().equals("")) {
 				if (defSource.equals("")) {
 					defSource = conceptVO.getDefinition_source();
 				} else {
 					defSource = defSource + ":"	+ conceptVO.getDefinition_source();
 				}
 			}
 			if (conceptVO.getOrigin() != null && !conceptVO.getOrigin().equals("")) {
 				if (origin.equals("")) {
 					origin = conceptVO.getOrigin();
 				} else {
 					origin = origin + ":" + conceptVO.getOrigin();
 				}
 			}
 		}	
 		if (preferredName != null && preferredName.length()>30){
 			preferredName = null;
 		}
 		if (longName != null && longName.length()>255){
 		    longName = longName.substring(0, 255);
 		}
 		if (prefferredDef != null && prefferredDef.length()>2000){
 		    prefferredDef =prefferredDef.substring(0,2000);
 		}
 		if (defSource != null && defSource.length()>2000){
 		    defSource = defSource.substring(0,2000);
 		}
 		if (origin != null && origin.length()>240){
 		    origin = origin.substring(0,240);
 		} 
 			
 		vo.setLong_name(longName);
 		vo.setPrefferred_name(preferredName);
 		vo.setPrefferred_def(prefferredDef);
 		vo.setDefinition_source(defSource);
 		vo.setOrigin(origin);
 	  }
 	public abstract ArrayList<ResultVO> isCondrExists(ArrayList<ConBean> list, Connection conn) throws DBException;
 	public abstract String insert(BaseVO vO, Connection conn) throws DBException;
 }
