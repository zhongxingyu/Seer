 package com.gemantic.labs.killer.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.gemantic.common.exception.ServiceDaoException;
 import com.gemantic.common.exception.ServiceException;
 import com.gemantic.common.util.PasswordUtils;
 import com.gemantic.dal.dao.Dao;
 import com.gemantic.dal.dao.exception.DaoException;
 import com.gemantic.killer.model.User;
 
 
 @Component
 public class UsersServiceImpl implements UsersService {
 
 	@Autowired
 	private Dao dao;
 
 	private static final Log log = LogFactory.getLog(UsersServiceImpl.class);
 
 	public Dao getDao() {
 		return dao;
 	}
 
 	public void setDao(Dao dao) {
 		this.dao = dao;
 	}
 
 	@Override
 	public Long insert(User users) throws ServiceException, ServiceDaoException {
 
 		if (log.isInfoEnabled()) {
 			log.info(" insert data : " + users);
 		}
 		if (users == null) {
 			return null;
 		}
 
 		long currentTimeMillis = System.currentTimeMillis();
 		if(users.getLoginAt()==null){
 			users.setLoginAt(System.currentTimeMillis());
 		}
 		users.setCreateAt(currentTimeMillis);
 		users.setUpdateAt(currentTimeMillis);
 
 		Long result = null;
 		try {
 			result = (Long) dao.save(users);
 		} catch (DaoException e) {
 			log.error(" insert wrong : " + users);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" insert data success : " + result);
 		}
 		return result;
 	}
 
 	@Override
 	public List<User> insertList(List<User> usersList) throws ServiceException, ServiceDaoException {
 
 		if (log.isInfoEnabled()) {
 			log.info(" insert lists : " + (usersList == null ? "null" : usersList.size()));
 		}
 		List<User> resultList = null;
 
 		if (CollectionUtils.isEmpty(usersList)) {
 			return new ArrayList<User>();
 		}
 
 		long currentTimeMillis = System.currentTimeMillis();
 		for (User users : usersList) {
 			users.setCreateAt(currentTimeMillis);
 			users.setUpdateAt(currentTimeMillis);
 		}
 
 		try {
 			resultList = (List<User>) dao.batchSave(usersList);
 		} catch (DaoException e) {
 			log.error(" insert list wrong : " + usersList);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" insert lists  success : " + (resultList == null ? "null" : resultList.size()));
 		}
 		return resultList;
 
 	}
 
 	@Override
 	public boolean delete(Long id) throws ServiceException, ServiceDaoException {
 
 		if (log.isInfoEnabled()) {
 			log.info(" delete data : " + id);
 		}
 		boolean result = false;
 
 		if (id == null) {
 			return true;
 		}
 
 		try {
 			result = dao.delete(User.class, id);
 		} catch (DaoException e) {
 			log.error(" delete wrong : " + id);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" delete data success : " + id);
 		}
 		return result;
 
 	}
 
 	@Override
 	public boolean update(User users) throws ServiceException, ServiceDaoException {
 
 		log.info(" update data : " + (users == null ? "null" : users.getId()));
 
 		boolean result = false;
 
 		if (users == null) {
 			return true;
 		}
 
 		users.setUpdateAt(System.currentTimeMillis());
 
 		try {
 			result = dao.update(users);
 		} catch (DaoException e) {
 			log.error(" update wrong : " + users);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" update data success : " + users);
 		}
 		return result;
 	}
 
 	@Override
 	public boolean updateList(List<User> usersList) throws ServiceException, ServiceDaoException {
 
 		log.info(" update lists : " + (usersList == null ? "null" : usersList.size()));
 
 		boolean result = false;
 
 		if (CollectionUtils.isEmpty(usersList)) {
 			return true;
 		}
 
 		long currentTimeMillis = System.currentTimeMillis();
 		for (User users : usersList) {
 			users.setUpdateAt(currentTimeMillis);
 		}
 
 		try {
 			result = dao.batchUpdate(usersList);
 		} catch (DaoException e) {
 			log.error(" update list wrong : " + usersList);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" update lists success : " + usersList.size());
 		}
 		return result;
 	}
 
 	@Override
 	public User getObjectById(Long id) throws ServiceException, ServiceDaoException {
 
 			log.info(" get data : " + id);
		
 		User users = null;
 
 		if (id == null) {
 			return users;
 		}
 
 		try {
 			users = (User) dao.get(User.class, id);
 		} catch (DaoException e) {
 			log.error(" get wrong : " + id);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" get data success : " + id);
 		}
 		return users;
 	}
 
 	@Override
 	public List<User> getObjectsByIds(List<Long> ids) throws ServiceException, ServiceDaoException {
 
	
 			log.info(" get lists : " + (ids == null ? "null" : ids));
	
 		List<User> users = null;
 
 		if (CollectionUtils.isEmpty(ids)) {
 			return new ArrayList<User>();
 		}
 
 		try {
 			users = (List<User>) dao.getList(User.class, ids);
 		} catch (DaoException e) {
 			log.error(" get wrong : " + ids);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" get data success : " + (users == null ? "null" : users.size()));
 		}
 		return users;
 	}
 
 	/**
 	 * 
 	 * @param
 	 * @return
 	 * @throws ServiceException
 	 * @throws ServiceDaoException
 	 */
 	@Override
 	public Long getUsersIdByOpenID(String openID) throws ServiceException, ServiceDaoException {
 
 		if (log.isInfoEnabled()) {
 			log.info(" get id by openID  : " + openID);
 		}
 		Long id = null;
 
 		// TODO 参数检查!
 
 		try {
 
 			id = (Long) dao.getMapping("getUsersIdByOpenID", new Object[] { openID });
 		} catch (DaoException e) {
 			log.error(" get id wrong by openID  : " + openID);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" get id success : " + id);
 		}
 		return id;
 
 	}
 
 	/**
 	 * 
 	 * @param
 	 * @return
 	 * @throws ServiceException
 	 * @throws ServiceDaoException
 	 */
 	@Override
 	public Long getUsersIdByEmail(String email) throws ServiceException, ServiceDaoException {
 
 		if (log.isInfoEnabled()) {
 			log.info(" get id by email  : " + email);
 		}
 		Long id = null;
 
 		// TODO 参数检查!
 
 		try {
 
 			id = (Long) dao.getMapping("getUsersIdByEmail", new Object[] { email });
 		} catch (DaoException e) {
 			log.error(" get id wrong by email  : " + email);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" get id success : " + id);
 		}
 		return id;
 
 	}
 
 	@Override
 	public boolean verify(Long uid, String password) throws ServiceException, ServiceDaoException {
 		User u=this.getObjectById(uid);
 		String pass = PasswordUtils.encode(password);
 		return u.getPassword().equals(pass);
 	}
 
 	@Override
 	public Integer getTotalCount() throws ServiceException, ServiceDaoException {
 	
 		List<Long> idList = null;
 
 		// TODO 参数检查!
 
 
 		try {
 			//idList = (List<Long>) dao.excuteSimpleSql("select id from records where version = "+version, Records.class);
 			idList = dao.getIdList("getAllUsers", new Object[] {  },0, Integer.MAX_VALUE, false);
 
 		} catch (DaoException e) {
 			log.error(" get getAllUsers  wrong )  : " );
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" get getAllUsers success : " + (idList == null ? "null" : idList.size()));
 		}
 		return idList.size();
 	}
 
 	@Override
 	public List<Long> getUIdsOrderByMoney(Integer start, Integer limit) throws ServiceException, ServiceDaoException {
 		if (log.isInfoEnabled()) {
 			log.info(" getUIdsOrderByMoney,start,limit  : " + " , " + start + " , " + limit);
 		}
 		List<Long> idList = null;
 
 		// TODO 参数检查!
 
 		if (start == null) {
 			start = 0;
 		}
 
 		if (limit == null) {
 			limit = Integer.MAX_VALUE;
 		}
 
 		try {
 			//idList = (List<Long>) dao.excuteSimpleSql("select id from records where version = "+version, Records.class);
 			idList = dao.getIdList("getUIdsOrderByMoney", new Object[] {  }, start, limit, false);
 
 		} catch (DaoException e) {
 			log.error(" getUIdsOrderByMoney,start,limit)  : "  + " , " + start + " , " + limit);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" get ids success : " + (idList == null ? "null" : idList.size()));
 		}
 		return idList;
 	}
 
 	@Override
 	public List<Long> getUIdsByPunchAtOrderByPunchAt(Long punch, Integer start, Integer size) throws ServiceException, ServiceDaoException {
 		
 		if (log.isInfoEnabled()) {
 			log.info(" getUIdsByPunchAtOrderByPunchAt,start,limit  : " + " , " + start + " , " + size);
 		}
 		List<Long> idList = null;
 
 		// TODO 参数检查!
 
 		if (start == null) {
 			start = 0;
 		}
 
 		if (size == null) {
 			size = Integer.MAX_VALUE;
 		}
 
 		try {
 			//idList = (List<Long>) dao.excuteSimpleSql("select id from records where version = "+version, Records.class);
 			idList = dao.getIdList("getUIdsByPunchAtOrderByPunchAt", new Object[] { punch }, start, size, false);
 
 		} catch (DaoException e) {
 			log.error(" getUIdsByPunchAtOrderByPunchAt,start,limit)  : "  + " , " + start + " , " + size);
 			log.error(e);
 			e.printStackTrace();
 			throw new ServiceDaoException(e);
 		}
 		if (log.isInfoEnabled()) {
 			log.info(" get ids success : " + (idList == null ? "null" : idList.size()));
 		}
 		return idList;
 	}
 
 	
 
 }
