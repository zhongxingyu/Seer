 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main.dao.qqapi.impl;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.domain.Pos;
 import com.chinarewards.qqgbvpn.domain.status.PosDeliveryStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosInitializationStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosOperationStatus;
 import com.chinarewards.qqgbvpn.main.dao.qqapi.PosDao;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 /**
  * @author cream
  * 
  */
 public class PosDaoImpl implements PosDao {
 
 	Logger logger = LoggerFactory.getLogger(PosDaoImpl.class);
 
 	@Inject
 	Provider<EntityManager> em;
 
 	public EntityManager getEm() {
 		return em.get();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Pos> fetchAll() {
 		return getEm().createQuery("FROM Pos").getResultList();
 	}
 
 	@Override
 	public Pos fetchPos(String posId, PosDeliveryStatus dstatus,
 			PosInitializationStatus istatus, PosOperationStatus ostatus) {
 		logger.trace("posId:{}, dstatus:{}, istatus:{}, ostatus:{}",
 				new Object[] { posId, dstatus, istatus, ostatus });
 
 		StringBuffer hql = new StringBuffer("FROM Pos p ");
 		hql.append(" WHERE p.posId = :posId");
 		if (dstatus != null) {
 			hql.append(" AND p.dstatus = :dstatus");
 		}
 		if (istatus != null) {
 			hql.append(" AND p.istatus = :istatus");
 		}
 		if (ostatus != null) {
 			hql.append(" AND p.ostatus = :ostatus");
 		}
 
 		logger.debug("hql:{}", hql);
 		Query query = getEm().createQuery(hql.toString());
 
 		query.setParameter("posId", posId);
 		if (dstatus != null) {
 			query.setParameter("dstatus", dstatus);
 		}
 		if (istatus != null) {
 			query.setParameter("istatus", istatus);
 		}
 		if (ostatus != null) {
 			query.setParameter("ostatus", ostatus);
 		}
 		return (Pos) query.getSingleResult();
 	}
 
 	@Override
 	public Pos createPos(Pos pos) {
 		getEm().persist(pos);
 
 		return pos;
 	}
 
 	@Override
 	public void merge(Pos pos) {
 		getEm().merge(pos);
		getEm().flush();
 	}
 
 }
