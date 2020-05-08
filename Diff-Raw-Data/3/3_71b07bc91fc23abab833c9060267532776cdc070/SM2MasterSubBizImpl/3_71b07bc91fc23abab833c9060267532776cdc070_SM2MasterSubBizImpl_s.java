 package com.nbcedu.function.schoolmaster2.biz.impl;
 
 import static org.apache.commons.lang.xwork.StringUtils.isNotBlank;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Criteria;
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.util.CollectionUtils;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.nbcedu.function.schoolmaster2.biz.SM2MasterSubBiz;
 import com.nbcedu.function.schoolmaster2.core.pager.PagerModel;
 import com.nbcedu.function.schoolmaster2.data.model.SM2SubjectMaster;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Subject;
 import com.nbcedu.function.schoolmaster2.utils.Utils;
 import com.nbcedu.function.schoolmaster2.vo.MasterSubSearchVO;
 import com.nbcedu.function.schoolmaster2.vo.StepVo;
 import com.nbcedu.function.schoolmaster2.vo.SubWeekSearch;
 import com.nbcedu.function.schoolmaster2.vo.SubjectWeekVo;
 import com.nbcedu.function.schoolmaster2.vo.SubjectZanVo;
 
 public class SM2MasterSubBizImpl extends SM2SubjectBizImpl implements SM2MasterSubBiz{
 
 	
 	private StringBuilder findByMasterHql = new StringBuilder("");
 	{
 		findByMasterHql.append("FROM TSm2Subject sub WHERE sub.moduleId =? ");
 		findByMasterHql.append("AND sub.id in (SELECT subId FROM SM2SubjectMaster m WHERE m.userUid = ?) ");
 		findByMasterHql.append("ORDER BY sub.lastUpdateTime DESC");
 	}
 	
 	@Override
 	public PagerModel findByMaster(final String modId, final String masterUid) {
 		return this.sm2SubjectDao.searchPaginated(
 				findByMasterHql.toString(), new Object[]{modId,masterUid});
 	}
 
 	@Override
 	public List<TSm2Subject> findByMasterAndCount(String modId,
 			String masterUid, Integer size) {
 		SQLQuery query = (SQLQuery)this.sm2SubjectDao.createSqlQuery(
 				this.sm2SubjectDao.getNamedQuery("index_find_sub_by_module").getQueryString());
 		query.setString("uid", masterUid);
 		query.setString("moduleId", modId);
 		query.setMaxResults(size);
 		return sqlResult(query);
 	}
 	
 	@Override
 	public PagerModel findByMaster(String modId, String masterUid,Integer flagType) {
 		StringBuilder hql = new StringBuilder("");
 		hql.append("FROM TSm2Subject sub WHERE sub.moduleId =? ");
 		hql.append("AND sub.id in (SELECT subId FROM SM2SubjectMaster m WHERE m.userUid = ? AND m.flag=?) ");
 		hql.append("ORDER BY sub.lastUpdateTime DESC");
 		return this.sm2SubjectDao.searchPaginated(hql.toString(), new Object[]{modId,masterUid,flagType});
 	}
 	
 	@Override
 	public List<StepVo> findAllSteps(String subId) {
 		String hql = "SELECT s.id,s.name,s.createrId FROM TSm2Step s WHERE s.subjectId=? ORDER BY s.lastUpdateTime DESC";
 		List<Object[]> resulSet = this.sm2SubjectDao.findByHQL(hql,new Object[]{subId});
 		
 		return Lists.transform(resulSet, new Function<Object[], StepVo>() {
 			@Override
 			public StepVo apply(Object[] input) {
 				StepVo vo = new StepVo();
 				vo.setId(input[0].toString());
 				vo.setName(input[1].toString());
 				vo.setCreaterId(input[2].toString());
 				return vo;
 			}
 		});
 		
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public PagerModel findBySearchVo(MasterSubSearchVO vo) {
 		Criteria cri = this.sm2SubjectDao.createCriteria();
 		
 		if(isNotBlank(vo.getModuleId())){
 			cri.add(Restrictions.eq("moduleId", vo.getModuleId()));
 		}
 		if(isNotBlank(vo.getCreaterName())){
 			cri.add(Restrictions.like("createrName",vo.getCreaterName(),MatchMode.ANYWHERE));
 		}
 		if(isNotBlank(vo.getDepartId())){
 			cri.add(Restrictions.eq("departmentId", vo.getDepartId()));
 		}
 		if(isNotBlank(vo.getTypeId())){
 			cri.add(Restrictions.eq("typeId", vo.getTypeId()));
 		}
 		
 		if(vo.getStart()!=null){
 			cri.add(Restrictions.ge("createTime", vo.getStart()));
 		}
 		if(vo.getEnd()!=null){
 			cri.add(Restrictions.le("createTime", vo.getEnd()));
 		}
 		if(isNotBlank(vo.getReceiverUid())||(vo.getFlag()!=null&&vo.getFlag()==1)){
 			cri.createAlias("checkUsers","checkUsers");
 			if(isNotBlank(vo.getReceiverUid())){
 				cri.add(Restrictions.eq("checkUsers.userUid",vo.getReceiverUid()));
 			}
 			if(vo.getFlag()!=null&&vo.getFlag()==1){
 				cri.add(Restrictions.eq("checkUsers.flag", vo.getFlag()));
 			}
 		}
 		
 		PagerModel pm =  this.sm2SubjectDao.searchPaginated(cri);
 		if(pm!=null&&pm.getDatas()!=null&&pm.getDatas().size()>0){
 			if(!(pm.getDatas().get(0) instanceof TSm2Subject)){
 				pm.setDatas(
 						Lists.transform(pm.getDatas(),
 								new Function<Object[], TSm2Subject>() {
 				@Override
 				public TSm2Subject apply(Object[] input) {
 					TSm2Subject s = (TSm2Subject)input[1];
 					SM2SubjectMaster m = (SM2SubjectMaster)input[0];
 					s.setFlag(m.getFlag());
 					return s;
 				}
 				}));
 			}
 		}
 		return pm;
 	}
 
 	@Override
 	public List<TSm2Subject> findByMsterModule(String masterUid,
 			Collection<String> moduleId, Integer size) {
 		StringBuilder sql = new StringBuilder("");
 		for (String modId : moduleId) {
 			sql.append("SELECT  ");
 				sql.append("id,");
 				sql.append("createrId,");
 				sql.append("createTime,");
 				sql.append("content,");
 				sql.append("flag,");
 				sql.append("departmentId,");
 				sql.append("title,");
 				sql.append("moduleId,");
 				sql.append("lastUpdateTime,");
 				sql.append("parentId,");
 				sql.append("createrName,");
 				sql.append("progress,");
 				sql.append("departmentName ");
 			sql.append("FROM ");
 				sql.append("t_sm2_subject sub,");
 				sql.append("(SELECT sub_id as subId ");
 				sql.append("FROM t_sm2_subject_master ");
 				sql.append("WHERE user_uid = '");
 				sql.append(masterUid);
 				sql.append("'");
 				sql.append(" AND flag= '1' ");
 				sql.append(") masterSub ");
 			sql.append("WHERE ");
 				sql.append("masterSub.subId = sub.id ");
 				sql.append("AND sub.moduleId='");
 				sql.append(modId);
 				sql.append("' ");
 				
 			sql.append("LIMIT 0,");
 			sql.append(size.toString());
 			sql.append(" UNION ALL ");
 		}
 		sql.delete(sql.lastIndexOf(" UNION ALL "),sql.length());
 		
 		SQLQuery query = (SQLQuery) this.sm2SubjectDao.createSqlQuery(sql.toString());
 		return sqlResult(query);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private List<TSm2Subject> sqlResult(SQLQuery query){
 		query.addScalar("id",Hibernate.STRING);
 		query.addScalar("createrId",Hibernate.STRING);
 		query.addScalar("createTime",Hibernate.TIMESTAMP);
 		query.addScalar("content",Hibernate.STRING);
 		query.addScalar("flag",Hibernate.INTEGER);
 		query.addScalar("departmentId",Hibernate.STRING);
 		query.addScalar("title",Hibernate.STRING);
 		query.addScalar("moduleId",Hibernate.STRING);
 		query.addScalar("lastUpdateTime",Hibernate.TIMESTAMP);
 		query.addScalar("parentId",Hibernate.STRING);
 		query.addScalar("createrName",Hibernate.STRING);
 		query.addScalar("progress",Hibernate.INTEGER);
 		query.addScalar("departmentName",Hibernate.STRING);
 		query.addScalar("masterFlag",Hibernate.STRING);
 		
 		List<Object[]> resultSet = query.list();
 		if(CollectionUtils.isEmpty(resultSet)){
 			return Collections.EMPTY_LIST;
 		}
 		
 		return Lists.transform(resultSet, new Function<Object[], TSm2Subject>() {
 			@Override
 			public TSm2Subject apply(Object[] in) {
 				TSm2Subject result = new TSm2Subject();
 				result.setId(trim(in[0]));
 				result.setCreaterId(trim(in[1]));
 				result.setCreateTime((Date)in[2]);
 				result.setContent(trim(in[3]));
 				result.setFlag(Integer.parseInt(in[4].toString()));
 				result.setDepartmentId(trim(in[5]));
 				result.setTitle(trim(in[6]));
 				result.setModuleId(trim(in[7]));
 				result.setLastUpdateTime((Date)in[8]);
 				result.setParentId(trim(in[9]));
 				result.setCreaterName(trim(in[10]));
 				result.setProgress(Integer.parseInt(in[11].toString()));
 				result.setDepartmentName(trim(in[12]));
 				return result;
 			}
 		});
 	}
 	/**
 	 * 原语句见 config/db/querys.sql
 	 */
 	@Override
 	public List<SubjectWeekVo> findWeekSingle(SubWeekSearch search) {
 		
 		if(search.getPublisher().size()!=1){
 			throw new IllegalArgumentException("按单个人查询时条件不匹配");
 		}
 		
 		StringBuilder sql = new StringBuilder("");
 		
 		sql.append("SELECT ");
 			sql.append("sub.id as id,");
 			sql.append("sub.title as title,");
 			sql.append("sub.createrId as createrId,");
 			sql.append("sub.createrName as createrName,");
 			sql.append("subtype.name as typeName,");
 			sql.append("subtype.id as typeId,");
 			sql.append("submaster.flag as status ");
 
 		sql.append("FROM t_sm2_subject sub,");
 		
 			sql.append("(");
 				sql.append("SELECT id,name ");
 				sql.append("FROM t_sm2_type ");	
 			sql.append(") subtype,");
 			
 			sql.append("(");
 				sql.append("SELECT sub_id ,flag ");
 				sql.append("FROM t_sm2_subject_master ");
 				sql.append("WHERE user_uid = '" + Utils.curUserUid() + "' ");
 				sql.append("AND flag in (");
 				if(search.getStatus().size()>0){//状态
 					for (Integer i : search.getStatus()) {
 						sql.append(i);
 						sql.append(",");
 					}
 					sql.deleteCharAt(sql.length()-1);
 				}
 				sql.append(")");
 			sql.append(") submaster ");
 	
 		sql.append("WHERE ");
 			sql.append("sub.id = submaster.sub_id ");
 			
 			if(search.getUpdateDate()!=null){//选择指定日期				
 				sql.append(
 						"AND (DATE(sub.createTime)='${date}' OR DATE(sub.lastUpdateTime)='${date}') "
 						.replace("${date}", Utils.Dates.dateSdf.format(search.getUpdateDate()))
 						);
 			}
 			
 			if(search.getStart()!=null){//按周查询
 				sql.append(
 						"AND (sub.createTime > '${date}' OR sub.lastUpdateTime > '${date}') "
 						.replace("${date}", Utils.Dates.dateSdf.format(search.getStart()))
 				); 
 			}
 			sql.append("AND sub.typeId = subtype.id ");
 			
 			if(search.getSubType().size() > 0){//类型
 				StringBuilder in = new StringBuilder("");
 				for (String type : search.getSubType()) {
 					in.append("'" + type + "',");
 				}
 				sql.append("AND sub.typeId in (" + in.deleteCharAt(in.length()-1).toString() + ") ");
 			}
 			
 			sql.append("AND sub.createrId = '${uid}' ".replace("${uid}", search.getPublisher().get(0)));
 	
 		sql.append("ORDER BY ");
 			sql.append("sub.createTime DESC , ");
 			sql.append("sub.lastUpdateTime DESC ");
 		
 		return weekSqltoList(sql.toString());
 	}
 	
 	/**
 	 * 原语句见 config/db/querys.sql
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<SubjectWeekVo> findWeek(SubWeekSearch search) {
 		
 		if(search==null){ return Collections.EMPTY_LIST;}
 		
 		if (search.getStatus().size() > 0 && search.getPublisher().size() > 0) {
 		
 			StringBuilder sql = new StringBuilder("");
 			
 			List<List<Integer>> status = new ArrayList<List<Integer>>(2){{
 				add(new ArrayList<Integer>(1));
 				add(new ArrayList<Integer>(3));
 			}};
 			
 			for (Integer sta : search.getStatus()) {
 				switch (sta) {
 					case 1:
 						status.get(0).add(sta);
 						break;
 					default:
 						status.get(1).add(sta);
 						break;
 				}
 			}
 			
 			for (String uid : search.getPublisher()) {
 				for (List<Integer> statu : status) {
 					if(status.size()<=0){
 						continue;
 					}
 					sql.append("SELECT id,title,createrId,createrName,typeName,typeId,status ");
 					sql.append("FROM (");
 					
 						sql.append("SELECT ");
 							sql.append("sub.id as id,");
 							sql.append("sub.title as title,");
 							sql.append("sub.createrId as createrId,");
 							sql.append("sub.createrName as createrName,");
 							sql.append("subtype.name as typeName,");
 							sql.append("subtype.id as typeId,");
 							sql.append("submaster.flag as status ");
 			
 						sql.append("FROM t_sm2_subject sub,");
 						
 							sql.append("(");
 								sql.append("SELECT id,name ");
 								sql.append("FROM t_sm2_type ");	
 							sql.append(") subtype,");
 							
 							sql.append("(");
 								sql.append("SELECT sub_id,flag ");
 								sql.append("FROM t_sm2_subject_master ");
 								sql.append("WHERE user_uid = '" + Utils.curUserUid() + "' ");
 								sql.append("AND flag in (");
 								for (Integer integer : statu) {
 									sql.append(integer);
 									sql.append(",");
 								}
 								sql.deleteCharAt(sql.length()-1);
 								sql.append(")");
 							sql.append(") submaster ");
 					
 						sql.append("WHERE ");
 							sql.append("sub.id = submaster.sub_id ");
 							
 							if(search.getUpdateDate()!=null){//选择指定日期				
 								sql.append(
 										"AND (DATE(sub.createTime)='${date}' OR DATE(sub.lastUpdateTime)='${date}') "
 										.replace("${date}", Utils.Dates.dateSdf.format(search.getUpdateDate()))
 										);
 							}
 							
 							if(search.getStart()!=null){//按周查询
 								sql.append(
 										"AND (sub.createTime > '${date}' OR sub.lastUpdateTime > '${date}') "
 										.replace("${date}", Utils.Dates.dateSdf.format(search.getStart()))
 								); 
 							}
 							sql.append("AND sub.typeId = subtype.id ");
 							
 							if(search.getSubType().size() > 0){//类型
 								StringBuilder in = new StringBuilder("");
 								for (String type : search.getSubType()) {
 									in.append("'" + type + "',");
 								}
 								sql.append("AND sub.typeId in (" + in.deleteCharAt(in.length()-1).toString() + ") ");
 							}
 							
 							sql.append("AND sub.createrId = '${uid}' ".replace("${uid}", uid));
 					
 						sql.append("ORDER BY ");
 							sql.append("sub.createTime DESC , ");
 							sql.append("sub.lastUpdateTime DESC ");
 						sql.append("LIMIT 5 ");
 						
 					sql.append(") " + uid + statu.get(0));
 					sql.append(" UNION ALL ");
 				}
 			}
 			sql.delete(sql.lastIndexOf(" UNION ALL "),sql.length());
 			
 			return weekSqltoList(sql.toString());
 			
 		}else{
 			return Collections.EMPTY_LIST;
 		}
 		
 	}
 	
 	@Override
 	public SubjectZanVo findByProgId(String progId) {
 		Query q = this.sm2SubjectDao.getNamedQuery("find_subtitle_uid_by_progid");
 		q.setString("progId", progId);
 		Object[] result = (Object[]) q.uniqueResult();
 		if(result!=null){
 			return new SubjectZanVo(trim(result[0]),trim(result[1]),trim(result[2]));
 		}
 		return null;
 	}
 	
 	@Override
 	@SuppressWarnings({ "serial", "unchecked" })
 	public Map<String, Integer> findNewCountByModule(String uid) {
 		
 		final List<Object[]> resultSet = 
 			this.sm2SubjectDao.getNamedQuery("subject_count_by_module").
 			setString("uid", uid).list();
 		
 		return new HashMap<String, Integer>(){{
 			if(resultSet!=null&&resultSet.size()>0){
 				for (Object[] result : resultSet) {
 					int count = result[1]==null?
 							0:Integer.parseInt(result[1].toString());
 					put(result[0].toString(),count);
 				}
 			}
 		}};
 	}
 	
 	
 	@Override
 	@SuppressWarnings({ "serial", "unchecked" })
 	public Map<String, Integer> findAttCountByModType(String moduleId,
 			String uid) {
		SQLQuery q = (SQLQuery) this.sm2SubjectDao.getNamedQuery("new_count_by_module_type");
 		q.addScalar("id", Hibernate.STRING);
 		q.addScalar("cout", Hibernate.STRING);
 		final List<Object[]> resultSet = q.list();
 		
 		return new HashMap<String, Integer>(){{
 			if(resultSet!=null&&resultSet.size()>0){
 				for (Object[] result : resultSet) {
 					int count = result[1]==null?
 							0:Integer.parseInt(result[1].toString());
 					put(result[0].toString(),count);
 				}
 			}
 		}};
 	}
 	
 	///////////////////////
 	////////privates///////
 	////////////////////
 	@SuppressWarnings("unchecked")
 	private List<SubjectWeekVo> weekSqltoList(String sql){
 		
 		SQLQuery query = (SQLQuery) this.sm2SubjectDao.createSqlQuery(sql);
 		
 		query.addScalar("id", Hibernate.STRING);
 		query.addScalar("title", Hibernate.STRING);
 		query.addScalar("createrId", Hibernate.STRING);
 		query.addScalar("createrName", Hibernate.STRING);
 		query.addScalar("typeName", Hibernate.STRING);
 		query.addScalar("typeId", Hibernate.STRING);
 		query.addScalar("status", Hibernate.INTEGER);
 		
 		List<Object[]> resultSet = query.list();
 		if(CollectionUtils.isEmpty(resultSet)){
 			return Collections.EMPTY_LIST;
 		}
 		
 		return Lists.transform(resultSet, new Function<Object[], SubjectWeekVo>() {
 			@Override
 			public SubjectWeekVo apply(Object[] in) {
 				SubjectWeekVo result = new SubjectWeekVo();
 				result.setSubId(trim(in[0]));
 				result.setSubTitle(trim(in[1]));
 				result.setCreatorUid(trim(in[2]));
 				result.setCreatorName(trim(in[3]));
 				result.setTypeName(trim(in[4]));
 				result.setTypeId(trim(in[5]));
 				String status = null;
 				status = Integer.parseInt(in[6].toString())==0?"new":"updated";
 				result.setStatus(status);
 				return result;
 			}
 		});
 	}
 	
 	private String trim(Object str){
 		return str==null?"":str.toString();
 	}
 	
 	
 }
