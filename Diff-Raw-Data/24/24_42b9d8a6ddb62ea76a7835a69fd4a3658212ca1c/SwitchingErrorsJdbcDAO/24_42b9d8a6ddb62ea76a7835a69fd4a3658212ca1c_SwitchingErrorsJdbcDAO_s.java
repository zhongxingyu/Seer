 package com.argility.centralpages.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.jdbc.core.RowMapper;
 
 import com.argility.centralpages.dao.mapper.SwAuditRowMapper;
 import com.argility.centralpages.dao.mapper.SwitchingErrorsRowMapper;
 import com.argility.centralpages.data.ActionTypeCountBean;
 import com.argility.centralpages.data.BranchCountsBean;
 import com.argility.centralpages.data.SwAudit;
 import com.argility.centralpages.data.SwitchingErrors;
 
 public class SwitchingErrorsJdbcDAO extends AbstractDAO implements
 		SwitchingErrorsDAO {
 
 	public String actTypTotalsSQL = "select act_typ, "+ 
 		"act_desc, "+
 		"count(1) from switching_errors "+ 
 		"join sw_audit using (sw_aud_id) "+
 		"join action_typ using (act_typ) "+
 		"group by act_typ, act_desc order by count desc";
 	
 	public String swErrorActTypCountSQL = "select foo.* " +
 			"	FROM (select act_typ, " +
 			"				act_desc, " +
 			"				switching_errors.error, " +
 			"				count(1) FROM switching_errors " +
 			"				JOIN sw_audit using (sw_aud_id) join action_typ using (act_typ) " +
 			"				GROUP by error, act_typ, act_desc) as foo " +
 			"	where count > 50 order by count desc";
 	
 	public SwitchingErrorsJdbcDAO() {
 	}
 
 	public List<ActionTypeCountBean> getActionTypeTotals() {
 		
 		List<ActionTypeCountBean> list = new ArrayList<ActionTypeCountBean>();
 		
		list = getJdbcTemplate().query(actTypTotalsSQL, new RowMapper<ActionTypeCountBean>() {
 
 			public ActionTypeCountBean mapRow(ResultSet rs, int i) throws SQLException {
 				ActionTypeCountBean bean = new ActionTypeCountBean();
 				
 				bean.setActTyp(rs.getInt("act_typ"));
 				bean.setActionDesc(rs.getString("act_desc"));
 				bean.setCount(rs.getInt("count"));
 				
 				return bean;
 			}
 			
 		});
 		
 		
 		log.info("Size is " + list.size());
 		ActionTypeCountBean b = list.get(1);
 		log.info("action " + b.getActionDesc());
 		
 		return list;
 	}
 
 	public List<SwitchingErrors> getSwitchingErrorsByType(Integer actTyp) {
 		log.info("getSwitchingErrorsByType("+actTyp+")");
 		String sql = SwitchingErrorsRowMapper.SELECT_COL_SQL + " WHERE act_typ = ?";
 		
 		return getJdbcTemplate().query(sql,
 				new Object[] {actTyp},
 				new SwitchingErrorsRowMapper<SwitchingErrors>());
 	}
 
 	public List<SwitchingErrors> getSwitchingErrorsFromBranch(String brCde) {
 		log.info("getSwitchingErrorsFromBranch("+brCde+")");
 		String sql = SwitchingErrorsRowMapper.SELECT_COL_SQL + " WHERE switching_errors.br_cde = ?";
 		
 		return getJdbcTemplate().query(sql,
 				new Object[] {brCde},
 				new SwitchingErrorsRowMapper<SwitchingErrors>());
 	}
 
 	public List<SwitchingErrors> getSwitchingErrorsToBranch(String brCde) {
 		log.info("getSwitchingErrorsToBranch("+brCde+")");
 		String sql = SwitchingErrorsRowMapper.SELECT_COL_SQL + " WHERE switching_errors.obo_br_cde = ?";
 		
 		return getJdbcTemplate().query(sql,
 				new Object[] {brCde},
 				new SwitchingErrorsRowMapper<SwitchingErrors>());
 	}
 
 	public List<ActionTypeCountBean> getActionTypeTotalsWithError() {
 		List<ActionTypeCountBean> list = new ArrayList<ActionTypeCountBean>();
 		
		list = getJdbcTemplate().query(swErrorActTypCountSQL, new RowMapper<ActionTypeCountBean>() {
 
 			public ActionTypeCountBean mapRow(ResultSet rs, int i) throws SQLException {
 				ActionTypeCountBean bean = new ActionTypeCountBean();
 				
 				bean.setActTyp(rs.getInt("act_typ"));
 				bean.setActionDesc(rs.getString("act_desc"));
 				bean.setCount(rs.getInt("count"));
 				bean.setError(rs.getString("error"));
 				
 				return bean;
 			}
 			
 		});
 		
 		log.info("Size is " + list.size());
 		
 		return list;
 
 	}
 
 	public List<SwitchingErrors> getSwitchingErrorsByError(String error) {
 		log.info("getSwitchingErrorsByError("+error+")");
 		String sql = SwitchingErrorsRowMapper.SELECT_COL_SQL + " WHERE switching_errors.error ilike ?";
 		
 		return getJdbcTemplate().query(sql,
 				new Object[] {"%" + error + "%"},
 				new SwitchingErrorsRowMapper<SwitchingErrors>());
 	}
 
 	public List<SwitchingErrors> getSwitchingErrorsByAudit(Integer audit) {
 		log.info("getSwitchingErrorsByAudit("+audit+")");
 		String sql = SwitchingErrorsRowMapper.SELECT_COL_SQL + " WHERE switching_errors.aud_id = ?";
 		
 		return getJdbcTemplate().query(sql,
 				new Object[] {audit},
 				new SwitchingErrorsRowMapper<SwitchingErrors>());
 	}
 
 	public List<SwitchingErrors> getSwitchingErrorsBySwAudit(Integer swAudit) {
 		log.info("getSwitchingErrorsBySwAudit("+swAudit+")");
 		String sql = SwitchingErrorsRowMapper.SELECT_COL_SQL + " WHERE switching_errors.sw_aud_id = ?";
 		
 		return getJdbcTemplate().query(sql,
 				new Object[] {swAudit},
 				new SwitchingErrorsRowMapper<SwitchingErrors>());
 	}
 
 	public List<BranchCountsBean> getTotalsBySendingBranch() {
 		
 		String sql = "SELECT switching_errors.br_cde, " +
 				"count(1) FROM switching_errors " +
 				"group by br_cde order by count desc;";
 		
 		List<BranchCountsBean> list = new ArrayList<BranchCountsBean>();
 		
 		list = getJdbcTemplate().query(sql, new RowMapper<BranchCountsBean>() {
 
 			public BranchCountsBean mapRow(ResultSet rs, int i) throws SQLException {
 				BranchCountsBean bean = new BranchCountsBean();
 				
 				bean.setBrCde(rs.getString("br_cde"));
 				bean.setCount(rs.getInt("count"));
 				
 				return bean;
 			}
 			
 		});
 		
 		log.info("Size is " + list.size());
 		
 		return list;
 	}
 	
 	public List<BranchCountsBean> getTotalsByReceivingBranch() {
 		
 		String sql = "SELECT switching_errors.obo_br_cde, " +
 				"count(1) FROM switching_errors " +
 				"group by obo_br_cde order by count desc;";
 		
 		List<BranchCountsBean> list = new ArrayList<BranchCountsBean>();
 		
 		list = getJdbcTemplate().query(sql, new RowMapper<BranchCountsBean>() {
 
 			public BranchCountsBean mapRow(ResultSet rs, int i) throws SQLException {
 				BranchCountsBean bean = new BranchCountsBean();
 				
 				bean.setBrCde(rs.getString("obo_br_cde"));
 				bean.setCount(rs.getInt("count"));
 				
 				return bean;
 			}
 			
 		});
 		
 		log.info("Size is " + list.size());
 		
 		return list;
 	}
 
 	public SwAudit getSwAudit(Integer swAudId) {
 		log.info("getSwAudit("+swAudId+")");
 		String sql = SwAuditRowMapper.SELECT_COLUMNS_SQL + " WHERE sw_aud_id = ?";
 		
 		return getJdbcTemplate().queryForObject(sql,
 				new Object[] {swAudId},
 				new SwAuditRowMapper<SwAudit>());
 	}
 }
