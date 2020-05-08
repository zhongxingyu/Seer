 package gov.nih.nci.cadsr.persist.concept;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 import gov.nih.nci.cadsr.persist.common.BaseVO;
 import gov.nih.nci.cadsr.persist.common.DBManager;
 import gov.nih.nci.cadsr.persist.exception.DBException;
 
 public class Component_Concepts_Ext_Mgr extends DBManager{
 	
 	private Logger logger = Logger.getLogger(this.getClass());
 	
 	/**
 	 * Inserts a single row of Component Concept and returns primary key cc_IDSEQ
 	 * 
 	 * @param ComponentConceptVO
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	public String insert(BaseVO vO, Connection conn) throws DBException {
 		ComponentConceptsVO ccVO = (ComponentConceptsVO) vO;
 		PreparedStatement statement = null;
 		String primaryKey = null;
 		// generate cc_IDSEQ(primary key) 	
 		ccVO.setCc_IDSEQ(this.generatePrimaryKey(conn));
 		try {
 			String sql ="insert into component_concepts_view_ext( cc_idseq, condr_idseq, con_idseq, display_order, primary_flag_ind, concept_value) values(?,?,?,?,?,?) ";
 			int column = 0;
 			statement = conn.prepareStatement(sql);
 		    statement.setString(++column, ccVO.getCc_IDSEQ());
 			statement.setString(++column, ccVO.getCondr_IDSEQ());
 			statement.setString(++column, ccVO.getCon_IDSEQ());
 			statement.setInt(++column, ccVO.getDisplay_order());
 			statement.setString(++column, ccVO.getPrimary_flag_ind());
 			statement.setString(++column, ccVO.getConcept_Value());
 				
 			int count = statement.executeUpdate();
 			if (count == 0) {
 				throw new Exception("Unable to insert the record");
 			} else {
 				primaryKey = ccVO.getCc_IDSEQ();
 				if (logger.isDebugEnabled()) {
 					logger.debug("Inserted Component Concepts");
 					logger.debug("cc_IDSEQ(primary key )-----> " + primaryKey);
 				}
 			}
 
 		} catch (Exception e) {
 			logger.error("Error inserting Component Concepts " + e);
 			errorList.add("Error inserting Component Concepts ");
 			throw new DBException(errorList);
 		} finally {
 			statement = SQLHelper.closePreparedStatement(statement);
 		}
 		return primaryKey;
 	}
 	
 	public Vector<ConVO> getConceptsByCondrIdseq(String condrIdseq, Connection conn) throws DBException{
 		Vector<ConVO> conceptsList = new Vector();
 		PreparedStatement statement = null;
 		ResultSet rs =null;
 		try {
 			String sql ="select con_idseq , concept_value from  component_concepts_view_ext where condr_idseq = ? order by display_order desc";
 			statement = conn.prepareStatement(sql);
 		    statement.setString(1, condrIdseq);
 		    rs = statement.executeQuery();
 			while (rs.next()) {
 				ConVO conVO = new ConVO();
 				conVO.setConIDSEQ(rs.getString("CON_IDSEQ"));
 				conVO.setConcept_value(rs.getString("CONCEPT_VALUE"));
 				conceptsList.add(conVO);
 			}
 		} catch (Exception e) {
 			logger.error("Error in getConceptsByCondrIdseq() in Component_Concepts_Ext_Mgr" + e);
 			//errorList.add("Error in getConceptsByCondrIdseq() ");
 			throw new DBException(errorList);
 		} finally {
 			statement = SQLHelper.closePreparedStatement(statement);
 		}
 		return conceptsList;
 	}
 
 }
