 /**
  * 
  */
 package com.hashin.project.dao;
 
 import java.lang.reflect.Array;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.core.BatchPreparedStatementSetter;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.PreparedStatementCreator;
 import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
 import org.springframework.jdbc.core.namedparam.SqlParameterSource;
 import org.springframework.jdbc.support.GeneratedKeyHolder;
 import org.springframework.jdbc.support.KeyHolder;
 
 import com.hashin.project.bean.ConstituenciesBean;
 import com.hashin.project.bean.ElectionStatesBean;
 import com.hashin.project.bean.ElectionsBean;
 import com.hashin.project.bean.ElectionsCandidatesBean;
 import com.hashin.project.bean.ElectionsConstsBean;
 import com.hashin.project.bean.ElectionsResultsBean;
 import com.hashin.project.util.CandidatesRowMapper;
 import com.hashin.project.util.ConstituencyMapper;
 import com.hashin.project.util.ElectionDetailRowMapper;
 import com.hashin.project.util.ElectionsCandidatesRowMapper;
 import com.hashin.project.util.ElectionsConstsMapper;
 import com.hashin.project.util.ElectionsResultsConstsRowMapper;
 import com.hashin.project.util.ElectionsStatesMapper;
 
 /**
  * @author jintu.jacob@gmail.com Oct 29, 2013 ElectionManagementDAOImpl
  */
public class ElectionManagementDAOImpl implements ElectionManagementDAO
 {
     private static final Logger logger = Logger
 	    .getLogger(ElectionManagementDAOImpl.class);
     private JdbcTemplate jdbcTemplate;
 
     @Resource(name = "dataSource")
     public void setDataSource(DataSource dataSource)
     {
 	this.jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     public Long addNewElection(final ElectionsBean election)
     {
 	 logger.debug(">>_____________/addNewElection | DAO -> " + election.toString());
 	 Long ele_id = null;
 		try {
 			KeyHolder keyHolder = new GeneratedKeyHolder();
 			jdbcTemplate.update(new PreparedStatementCreator() {
 				public PreparedStatement createPreparedStatement(
 						Connection connection) throws SQLException {
 					PreparedStatement ps = connection.prepareStatement(
 							SQLConstants.INSERT_NEW_ELECTION,
 							new String[] { "ele_id" });
 					
 					ps.setString(1, election.getElectTitle());
 					ps.setString(2, election.getElectStartDate());
 					ps.setString(3, election.getElectEndDate());
 					ps.setString(4, election.getElectDescription());
 					ps.setString(5, "N");
 					ps.setString(6, "N");
 					
 					return ps;
 				}
 			}, keyHolder);
 
 			ele_id = keyHolder.getKey().longValue();
 			logger.debug(">>_____________/addNewElection | DAO -> Generated Election Id : " + ele_id);
 			return ele_id;
 		} catch (Exception e) {
 		    	logger.debug(">>_____________/addNewElection | DAO -> Exception, return null"); 
 			return null;
 		}
 
     }
 
     @Override
     public List<ElectionsConstsBean> getElectionsListByConst(String constId)
     {
 	Object[] parameters = new Object[] { constId };
 	List<ElectionsConstsBean> electionList = jdbcTemplate.query(
 		SQLConstants.GET_ELECTIONS_BY_CONST_ID, parameters,
 		new ElectionsConstsMapper());
 	return electionList;
     }
 
     @Override
     public List<ElectionsCandidatesBean> getCandidatesListByUnitId(
 	    int unitElectionId)
     {
 	List<ElectionsCandidatesBean> candidateList = null;
 	Object[] parameters = new Object[] { unitElectionId };
 	candidateList = jdbcTemplate.query(
 		SQLConstants.GET_CANDIDATES_BY_UNIT_ELE_ID, parameters,
 		new CandidatesRowMapper());
 	return candidateList;
     }
 
     @Override
     public int increamentVoteCountByCandidate(String candidateId)
     {
 	int numRows = 0;
 	Object[] parameters = new Object[] { candidateId };
 	numRows = jdbcTemplate.update(
 		SQLConstants.INCREMENT_VOTECOUNT_BY_CANDIDATE_ID, parameters);
 
 	// non zero return ==> update is successfully executed.
 	// exception/zero on return ==> update failed
 	return numRows;
     }
 
     
     @Override
     public List<ConstituenciesBean> getAllConstituencies()
     {
 	List<ConstituenciesBean> constList = null;
 	constList = jdbcTemplate.query(SQLConstants.GET_ALL_CONSTS,
 		new ConstituencyMapper());
 	logger.debug("_______________________Fetched the constituecies list >> "
 		+ constList.size());
 	return constList;
     }
 
     @Override
     public List<ElectionsBean> searchElections(String titleToSearch)
     {
 	Object[] parameters = new Object[] {
 		"%" + titleToSearch + "%"  };
 
 	List<ElectionsBean> electionList = jdbcTemplate.query(
 		SQLConstants.SEARCH_ELECTIONS_BY_ALL_CRITERIA, parameters,
 		new ElectionDetailRowMapper());
 	return electionList;
     }
 
     @Override
     public ElectionsBean getElectionDetail(String  electId)
     {
 	Object[] parameters = new Object[] { electId };
 	List<ElectionsBean> electionList = jdbcTemplate.query(
 		SQLConstants.GET_ELECTION_DETAIL_BY_ID, parameters,
 		new ElectionDetailRowMapper());
 
 	if (electionList.size() > 0)
 	{
 	    return electionList.get(0);
 	}
 	return null;
     }
     
     
     @Override
     public ElectionsBean getElectionDetail(String eleTitle, String eleDesc,
 		String eleStartDt)
     {
 	Object[] parameters = new Object[] { 
 			eleTitle,
 			eleDesc,
 			eleStartDt
 		};
 	List<ElectionsBean> electionList = jdbcTemplate.query(
 		SQLConstants.GET_LAST_ELECTION_BASIC_ADDED, parameters,
 		new ElectionDetailRowMapper());
 
 	if (electionList.size() > 0)
 	{
 	    return electionList.get(0);
 	}
 	return null;
     }
     
     
 
     @Override
     public List<ElectionStatesBean> getStatesListByEleId(String electId)
     {
 	Object[] parameters = new Object[] { electId };
 	List<ElectionStatesBean> statesList =  jdbcTemplate.query(
 		SQLConstants.GET_STATES_BY_ELECTION_ID, parameters,
 		new ElectionsStatesMapper());
 	
 	if(statesList.size() > 0 ){
 	    return statesList;
 	}
 
 	return null;
     }
 
 	@Override
 	public List<ElectionStatesBean> getAllStatesForMenu() 
 	{
 	    	logger.debug(">>_____________/getAllStatesForMenu |DAO -> in getAllStatesFormenu:: ");
 		List<ElectionStatesBean> statesList =  jdbcTemplate.query(
 			SQLConstants.GET_ALL_STATES, new ElectionsStatesMapper());
 		
 		if(statesList.size() > 0 ){
 		    logger.debug(">>_____________/getAllStatesForMenu |DAO -> states fetched :: "+ statesList.size());
 		    return statesList;
 		}
 
 		logger.debug(">>_____________/getAllStatesForMenu |DAO -> No States found ! ");
 		return null;
 	}
 
 	@Override
 	public int enrollVotersForElection(String electId)
 	{
 	    Object[] parameters = new Object[] { electId };
 	    int numRows = jdbcTemplate.update(SQLConstants.BATCH_RUN_INSERT_VOTERS_TO_VOTINGSTAT,
 			parameters);
 	    return numRows;
 	 
 	}
 
 	@Override
 	public Boolean getVoterEnrollStatusByElection(String electId)
 	{
 	    Boolean enrlmntStatus = false;
 	    int enrldCount = 0;
 	    Object[] parameters = new Object[] {   electId  	};
 	    
 	    enrldCount =  jdbcTemplate.queryForInt(SQLConstants.GET_ELECTIONS_VOTER_ENRLMNT_STATUS, parameters);
 	    if(enrldCount != 0 ){
 		enrlmntStatus = true;
 	    }
 	    return enrlmntStatus;
 	}
 
 	@Override
 	public int updateEnrlmntStatusForElection(String electId)
 	{
 	    Object[] parameters = new Object[] { 
 		    "Y",
 		    electId 
 	    	};
 	    int numRows = jdbcTemplate.update(SQLConstants.UPDATE_VOTER_ENRLMNT_STAT_FOR_ELE,
 			parameters);
 	    return numRows;
 	}
 
 	@Override
 	public int deleteElectionInElections(String electId)
 	{
 	    //Not deleting changing the status elections:vtr_enrlmnt_stat to "N"
 	    //key change testing is still pending;
 	    Object[] parameters = new Object[] { 
 		    "Y",
 		    electId 
 	    	};
 	    int numRows = jdbcTemplate.update(SQLConstants.DELETE_ELE_IN_ELECTIONS,
 			parameters);
 	    return numRows;
 	}
 	
 	
 	@Override
 	public int createUnitConstituencyElections(final String electId,
 		final List<ConstituenciesBean> constsList)
 	{
 	    
 	    logger.debug(">>_____________/createUnitElections |DAO -> input params :: "
 		    	+ "eleid: " +electId + "states List Size: " + constsList.size());
 	    
 	    int[] rowNum = jdbcTemplate.batchUpdate(
 		    SQLConstants.BATCH_CREATE_UNIT_ELECTIONS_BY_CONSTS, 
 		    new BatchPreparedStatementSetter() 
 		    {
 			public void setValues(PreparedStatement ps, int i) throws SQLException {
 				ConstituenciesBean constituency =  constsList.get(i);
 				ps.setString(1, electId );
 				ps.setString(2, constituency.getConstId());
 			}
 		 
 			public int getBatchSize() {
 				return constsList.size();
 			}
 		    } );
 	    
 	    logger.debug(">>_____________createUnitElections() |DAO -> update impact: "
 	    	+ ""+ Arrays.toString(rowNum));
 	    return rowNum[rowNum.length - 1];   
 	}
 	
 
 /*	@Override
 	public int createUnitConstituencyElections(String electId,
 		List<ElectionStatesBean> stateList)
 	{
 	    
 	    logger.debug(">>_____________/addNewElection |DAO -> input params :: "
 		    	+ "eleid: " +electId + "states List Size: " + stateList.size());
 	    
 	    List<String> paramList = new ArrayList<String>();
 	    Object[] parameters = new Object[] {};
 	    int i = 0;
 	    
 	    paramList.add(electId);	//in first position.add(electId);	
 	    parameters[i++] = electId;
 	    
 	   
 	    
 	    String qMarks = "";
 	    int itr = 0;
 	    for (ElectionStatesBean state: stateList) 
 	    {
 		 qMarks += " ? ";
 		 
 		 paramList.add(state.getStateId());
 		 parameters[i++] = state.getStateId(); 
 		 if(itr != stateList.size()-1){
 		     qMarks += "," ;
 		 }
 		 itr++;
 	    }
 	    
 	    String SQL = "INSERT INTO elections_consts (ele_id, const_id) SELECT ? , const_id FROM "
 			+ "(select CON.const_id from constituencies CON, elections_states STA "
 			+ "where CON.const_state = STA.st_id and STA.st_id in("+ qMarks + ")) as test";
 
 	    logger.debug(">>_____________/addNewElection |DAO -> Query constructed :: "+ SQL);
 	    logger.debug(">>_____________/addNewElection |DAO -> Parameters :: "+ Arrays.toString(parameters));	    
 	    
 	    int numRows = jdbcTemplate.update(SQL,paramList);
 	    
 	    logger.debug(">>_____________/addNewElection |DAO -> number Rows:: "+ numRows);
 	    return numRows;
 	}
 */
 	@Override
 	public List<ConstituenciesBean> searchConstsByName(String constName)
 	{
 	    	constName = "%" + constName + "%";
 	    	logger.debug("___________ /searchConsts/DAO"+ constName);
 	    	Object[] parameters = new Object[] { constName };
 		List<ConstituenciesBean> constList =  jdbcTemplate.query(
 			SQLConstants.GET_CONSTS_BY_NAME, parameters,
 			new ConstituencyMapper());
 		
 		if(constList.size() > 0 ){
 		    return constList;
 		}
 		return null;
 
 	}
 
 	@Override
 	public List<ElectionsCandidatesBean> searchCandidateByNameConst (
 		String candName, String constId)throws Exception
 	{
 	    	
 	    	logger.debug("___________ /searchCandidateByNameConst/DAO:"+ candName +"|"+ constId+"|");
 	    	Object[] parameters = new Object[] { 
 	    		"%"+ candName + "%", 
 	    		"%"+ constId + "%"
 	    	     };
 	    	
 	    	
 		List<ElectionsCandidatesBean> candList =  jdbcTemplate.query(
 			SQLConstants.GET_CANDIDATE_LIST_BY_NAME_CONST, parameters,
 			new ElectionsCandidatesRowMapper());
 		
 		if(candList.size() > 0 ){
 		    return candList;
 		}
 		return null;	
 	}
 
 	@Override
 	public int deleteCandidate(String candId)
 	{
 	    Object[] parameters = new Object[] { candId };
 	    int numRows = jdbcTemplate.update(SQLConstants.DELETE_CANDIDATE_BY_CANDID, parameters);
 	    return numRows;	
 	}
 
 	@Override
 	public Long addCandidateToBaseTable(final String candName, final String candBio)
 	{
 		Long candidateId = null;
 		try {
 			KeyHolder keyHolder = new GeneratedKeyHolder();
 			jdbcTemplate.update(new PreparedStatementCreator() {
 				public PreparedStatement createPreparedStatement(
 						Connection connection) throws SQLException {
 					PreparedStatement ps = connection.prepareStatement(
 							SQLConstants.INSERT_NEW_CANDIDATE_IN_BASE,
 							new String[] { "cand_id" });
 					ps.setString(1, candName);
 					ps.setString(2, "logopath");
 					ps.setString(3, candBio);
 					ps.setString(4, "N" );
 
 					return ps;
 				}
 			}, keyHolder);
 
 			candidateId = keyHolder.getKey().longValue();
 			logger.debug("___________________Autogenerated key >>  "+ candidateId);
 			return candidateId;
 		} catch (Exception e) {
 			logger.debug("________________DAO Exception User already enrolled >> ");
 			return null;
 		}
 	
 	}
 
 	@Override
 	public Long addCandidateToEleCandidates(final String newCandId, final String unitEleId)
 	{
 	    Long ele_cand_id = null;
 		try {
 			KeyHolder keyHolder = new GeneratedKeyHolder();
 			jdbcTemplate.update(new PreparedStatementCreator() {
 				public PreparedStatement createPreparedStatement(
 						Connection connection) throws SQLException {
 					PreparedStatement ps = connection.prepareStatement(
 							SQLConstants.INSERT_NEW_CANDIDATE_IN_ELECTION_CANDS,
 							new String[] { "ele_cand_id" });
 					ps.setString(1, unitEleId);
 					ps.setString(2, newCandId);
 					
 					return ps;
 				}
 			}, keyHolder);
 
 			ele_cand_id = keyHolder.getKey().longValue();
 			logger.debug("___________________Autogenerated key >>  "+ ele_cand_id);
 			return ele_cand_id;
 		} catch (Exception e) {
 			logger.debug("________________DAO Exception User already enrolled >> ");
 			return null;
 		}
 	}
 
 	@Override
 	public int addCandidateToEleResults(String eleCandId, String unitEleId)
 	{
 	    Object[] parameters = new Object[] { 
 		    unitEleId,
 		    eleCandId,
 		    0
 		};
 	    int numRows = jdbcTemplate.update(SQLConstants.INSERT_CANDIDATE_IN_ELE_RESULTS,
 			parameters);
 	    return numRows;
 	}
 
 	@Override
 	public List<ConstituenciesBean> getConstsByStatesId(
 		List<ElectionStatesBean> stateList)
 	{
 	    logger.debug("__________DAO:getConstsByStatesId|incoming params>> "+ stateList.toString());
 	    List<ConstituenciesBean> constList = null;
 	    List<String> paramList = new ArrayList<String>();
 	    StringBuilder qMarks = new StringBuilder("");
 	    int i = 0;
 
 	    for(ElectionStatesBean state: stateList){
 		 qMarks.append("?");
 		 if(i != stateList.size()-1)
 		 {
 		     qMarks.append(", ") ;
 		 }
 		 //System.out.println(state.getStateId());
 		 paramList.add(state.getStateId());
 		 i++;
 	    }
 	    
 	    String SQL  = " select CON.const_id, CON.const_name, ST.st_name "
 	    	+ "from constituencies CON, elections_states ST "
 	    	+ "where CON.const_state = ST.st_id "
 	    	+ "and ST.st_id in (" + qMarks + ") ";
 
 	    logger.debug("__________DAO:getConstsByStatesId/Query >> "+ SQL);
 	    
 	    constList =  jdbcTemplate.query(SQL, paramList.toArray(), new ConstituencyMapper());
 	    
 	    logger.debug("__________DAO:getConstsByStatesId/ Result>> "+ constList.toString());
 	    return constList;
 	}
 
 	@Override
 	public int getTotalVoteCountByConst(String unitEleId )
 	{
 	    Object[] parameters = new Object[] { unitEleId };	    
 	    int totalVoteCount = 0;
 	    totalVoteCount =  jdbcTemplate.queryForInt(SQLConstants.GET_TOTAL_VOTECOUNT_BY_CONSTITUENCY, parameters);
 	    return totalVoteCount;
 	}
 
 	@Override
 	public List<ElectionsResultsBean> getElectionResultsDetailByConst(String unitEleId){
 	    
 	    List<ElectionsResultsBean> resultList = null;
 	    Object[] parameters = new Object[] { unitEleId };
 	    resultList = jdbcTemplate.query(
 			SQLConstants.GET_ELECTIONS_RESULTS_BY_CONSTITUENCY, parameters,
 			new ElectionsResultsConstsRowMapper());
 	    return resultList;
 		
 	}
 	 
 
 
 
 }
