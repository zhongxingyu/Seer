 package com.rockontrol.yaogan.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.rockontrol.yaogan.dao.IOrganizationDao;
 import com.rockontrol.yaogan.dao.IUserDao;
 import com.rockontrol.yaogan.model.Organization;
 import com.rockontrol.yaogan.model.User;
 
 @Service
 public class ProvisionServiceImpl implements IProvisionService {
 
    @Autowired
    protected IOrganizationDao orgDao;
 
    @Autowired
    protected IUserDao userDao;
 
    @Transactional
    @Override
    public void createOrg(Organization org) {
       orgDao.save(org);
    }
 
    @Transactional
    @Override
    public void updateOrg(Long orgId, Organization org) {
       org.setId(orgId);
       orgDao.update(org);
    }
 
    @Transactional
    @Override
    public void deleteOrg(Long orgId) {
       orgDao.deleteById(orgId);
    }
 
    @Transactional
    @Override
    public void createUser(Long orgId, User user) {
       user.setOrgId(orgId);
      user.setPassword("password");
       userDao.save(user);
    }
 
    @Transactional
    @Override
    public void updateUser(Long orgId, Long userId, User user) {
       user.setOrgId(orgId);
       user.setId(userId);
      user.setPassword("password");
       userDao.update(user);
    }
 
    @Transactional
    @Override
    public void deleteUser(Long orgId, Long userId) {
       userDao.deleteById(userId);
    }
 
 }
