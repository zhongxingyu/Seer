 package com.nbcedu.function.schoolmaster2.biz.impl;
 
 import static org.apache.commons.lang.xwork.StringUtils.isNotBlank;
 
 import java.util.List;
 
 import org.apache.commons.lang.xwork.StringUtils;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Restrictions;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.nbcedu.function.schoolmaster2.biz.SM2MasterSubBiz;
 import com.nbcedu.function.schoolmaster2.core.pager.PagerModel;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Subject;
 import com.nbcedu.function.schoolmaster2.vo.MasterSubSearchVO;
 import com.nbcedu.function.schoolmaster2.vo.StepVo;
 
 public class SM2MasterSubBizImpl extends SM2SubjectBizImpl implements SM2MasterSubBiz{
 
 	
 	private StringBuilder findByMasterHql = new StringBuilder("");
 	{
 		findByMasterHql.append("FROM TSm2Subject sub WHERE sub.moduleId =? ");
 		findByMasterHql.append("AND sub.id in (SELECT subId FROM SM2SubjectMaster m WHERE m.userUid = ?) ");
 		findByMasterHql.append("ORDER BY sub.createTime DESC");
 	}
 	
 	@Override
 	public PagerModel findByMaster(final String modId, final String masterUid) {
 //		SQLQuery q = (SQLQuery) this.sm2SubjectDao.createSqlQuery(this.sm2SubjectDao.getNamedQuery("subject_master_module").getQueryString());
 //		q.addScalar("content", Hibernate.STRING);
 //		q.setString("uid", masterUid);
 //		q.setString("moduleId", modId);
 //		q.setFirstResult(SystemContext.getOffset());
 //		q.setMaxResults(SystemContext.getPagesize());
 //		
 //		List<TSm2Subject> resultSet = q.list();
 //		List<TSm2Subject> result = Lists.transform(resultSet, new Function<TSm2Subject, TSm2Subject>() {
 //			@Override
 //			public TSm2Subject apply(TSm2Subject input) {
 //				
 //				return null;
 //			}
 //		});
 //		
 //		Query countQ = this.sm2SubjectDao.getNamedQuery("subject_master_module_count");
 //		countQ.setString("uid", masterUid);
 //		countQ.setString("moduleId", modId);
 //		Object count = countQ.uniqueResult();
 //		
 //		PagerModel pm = new PagerModel();
 //		pm.setDatas(result);
 //		pm.setTotal(count==null?0:Integer.valueOf(count.toString()));
 //		pm.setTotalPageNo(
 //				pm.getTotal()%SystemContext.getPagesize()==0?
 //				pm.getTotal()/SystemContext.getPagesize():
 //				pm.getTotal()/SystemContext.getPagesize()+1
 //			);
 //		return pm;
 		
 		
		return this.sm2SubjectDao.searchPaginated(findByMasterHql.toString(), new Object[]{masterUid,modId});
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<TSm2Subject> findByMasterAndCount(String modId,
 			String masterUid, Integer size) {
 		Query query = null;
 		if(StringUtils.isNotBlank(masterUid)){
 			query = this.sm2SubjectDao.createQuery(this.findByMasterHql.toString(), 
					new Object[]{masterUid,modId});
 		}else{
 			query = this.sm2SubjectDao.createQuery(
 					"FROM TSm2Subject sub WHERE sub.moduleId =? ",new Object[]{modId});
 		}
 		if(size!=null&&size>0){
 			query.setMaxResults(size);
 		}
 		return query.list();
 	}
 	
 	@Override
 	public List<StepVo> findAllSteps(String subId) {
 		String hql = "SELECT s.id,s.name FROM TSm2Step s WHERE s.subjectId=? ORDER BY s.lastUpdateTime DESC";
 		List<Object[]> resulSet = this.sm2SubjectDao.findByHQL(hql,new Object[]{subId});
 		
 		return Lists.transform(resulSet, new Function<Object[], StepVo>() {
 			@Override
 			public StepVo apply(Object[] input) {
 				StepVo vo = new StepVo();
 				vo.setId(input[0].toString());
 				vo.setName(input[1].toString());
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
 		if(vo.getFlag()!=null){
 			cri.add(Restrictions.eq("flag", vo.getFlag()));
 		}
 		if(vo.getStart()!=null){
 			cri.add(Restrictions.ge("createTime", vo.getStart()));
 		}
 		if(vo.getEnd()!=null){
 			cri.add(Restrictions.le("createTime", vo.getEnd()));
 		}
 		if(isNotBlank(vo.getReceiverUid())){
 			cri.createAlias("checkUsers","checkUsers");
 			cri.add(Restrictions.eq("checkUsers.userUid",vo.getReceiverUid()));
 		}
 		
 		PagerModel pm =  this.sm2SubjectDao.searchPaginated(cri);
 		if(pm!=null&&pm.getDatas()!=null&&pm.getDatas().size()>0){
 			if(!(pm.getDatas().get(0) instanceof TSm2Subject)){
 				pm.setDatas(
 						Lists.transform(pm.getDatas(),
 								new Function<Object[], TSm2Subject>() {
 				@Override
 				public TSm2Subject apply(Object[] input) {
 					return (TSm2Subject)input[1];
 				}
 				}));
 			}
 		}
 		return pm;
 	}
 }
