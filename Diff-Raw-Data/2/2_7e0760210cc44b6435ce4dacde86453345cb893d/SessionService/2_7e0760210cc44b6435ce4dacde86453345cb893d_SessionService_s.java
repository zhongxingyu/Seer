 package org.siraya.rent.user.service;
 
 import org.siraya.rent.pojo.Session;
 import org.siraya.rent.user.dao.ISessionDao;
 import org.siraya.rent.user.dao.IDeviceDao;
 import org.siraya.rent.utils.RentException;
 import org.slf4j.Logger;
 import org.siraya.rent.utils.RentException;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 @Service("sessionService")
 public class SessionService implements ISessionService {
 
 	@Autowired
     private ISessionDao sessionDao;
 
 
 	@Autowired
     private IDeviceDao deviceDao;
 	
 	private static Logger logger = LoggerFactory.getLogger(SessionService.class);
 
 	@Transactional(value = "rentTxManager", propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = java.lang.Throwable.class)
 	public void newSession(Session session) {
 		this.sessionDao.newSession(session);
 		int ret = this.deviceDao.updateLastLoginIp(session);
 		if (ret != 1) {
 			throw new RentException(RentException.RentErrorCode.ErrorNotFound,
					"update device not found");
 		}
 	}
 	public void setSessionDao(ISessionDao sessionDao) {
 		this.sessionDao = sessionDao;
 	}
 
 	public void setDeviceDao(IDeviceDao deviceDao) {
 		this.deviceDao = deviceDao;
 	}
 }
