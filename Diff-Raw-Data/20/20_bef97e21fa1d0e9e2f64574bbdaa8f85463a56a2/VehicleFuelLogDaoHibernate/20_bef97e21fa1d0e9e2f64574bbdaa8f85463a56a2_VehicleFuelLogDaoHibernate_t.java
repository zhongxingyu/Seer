 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.solairis.yourcarslife.data.dao.impl;
 
 import com.solairis.yourcarslife.data.dao.VehicleFuelLogDao;
 import com.solairis.yourcarslife.data.exception.VehicleLogDaoException;
 import com.solairis.yourcarslife.data.domain.VehicleFuelLog;
 import com.solairis.yourcarslife.data.input.VehicleFuelLogInputData;
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projection;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 /**
  *
  * @author josh
  */
 public class VehicleFuelLogDaoHibernate implements VehicleFuelLogDao {
 
 	private SessionFactory sessionFactory;
 	private final Logger logger = Logger.getLogger(this.getClass());
 
 	@Override
 	public VehicleFuelLog getVehicleFuelLog(long vehicleFuelLogId) throws VehicleLogDaoException {
 
 		VehicleFuelLog vehicleFuelLog = null;
 
 		try {
 			Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(VehicleFuelLog.class);
 			criteria.add(Restrictions.eq("vehicleFuelLogId", vehicleFuelLogId));
			vehicleFuelLog = (VehicleFuelLog) criteria.uniqueResult();
 		} catch (HibernateException e) {
 			throw new VehicleLogDaoException(e);
 		}
 
 		return vehicleFuelLog;
 	}
 
 	@Override
 	public List<VehicleFuelLog> getVehicleFuelLogs(VehicleFuelLogInputData inputData) throws VehicleLogDaoException {
 		List<VehicleFuelLog> vehicleFuelLogs = null;
 
 		try {
 			Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(VehicleFuelLog.class);
 
 			if (inputData.getVehicleId() > 0) {
 				criteria.add(Restrictions.eq("vehicleId", inputData.getVehicleId()));
 			}
 			if (inputData.getMaxRecords() > 0) {
 				criteria.setMaxResults(inputData.getMaxRecords());
 			}
 
 			criteria.setFirstResult(inputData.getStartRecord());
 			criteria.addOrder(Order.desc("odometer"));
 
 			vehicleFuelLogs = criteria.list();
 		} catch (HibernateException e) {
 			throw new VehicleLogDaoException(e);
 		}
 
 		return vehicleFuelLogs;
 	}
 
 	@Override
 	public VehicleFuelLog saveVehicleFuelLog(VehicleFuelLog vehicleLog) throws VehicleLogDaoException {
 		Session session = this.sessionFactory.getCurrentSession();
 		session.save(vehicleLog);
 		return this.getVehicleFuelLog(vehicleLog.getVehicleFuelLogId());
 	}
 
 	@Override
 	public int getVehicleFuelLogCount(VehicleFuelLogInputData inputData) throws VehicleLogDaoException {
 		int count = 0;
 		try {
 			Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(VehicleFuelLog.class);
			if (inputData.getVehicleId() > 0) {
				criteria.add(Restrictions.eq("vehicleId", inputData.getVehicleId()));
			}
 			criteria.setProjection(Projections.rowCount());
			count = ((Integer) criteria.uniqueResult()).intValue();
 		} catch (HibernateException e) {
			throw new VehicleLogDaoException(e);
 		}
 		return count;
 	}
 
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 }
