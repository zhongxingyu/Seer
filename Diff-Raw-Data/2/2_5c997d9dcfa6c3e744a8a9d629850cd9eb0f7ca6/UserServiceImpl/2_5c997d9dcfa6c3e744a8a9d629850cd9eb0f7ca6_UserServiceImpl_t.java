 package com.financial.pyramid.service.impl;
 
 import com.financial.pyramid.dao.UserDao;
 import com.financial.pyramid.domain.Account;
 import com.financial.pyramid.domain.Passport;
 import com.financial.pyramid.domain.User;
 import com.financial.pyramid.domain.type.Role;
 import com.financial.pyramid.service.AccountService;
 import com.financial.pyramid.service.SettingsService;
 import com.financial.pyramid.service.UserService;
 import com.financial.pyramid.service.beans.AccountDetails;
 import com.financial.pyramid.settings.Setting;
 import com.financial.pyramid.utils.Password;
 import com.financial.pyramid.web.form.AdminRegistrationForm;
 import com.financial.pyramid.web.form.QueryForm;
 import com.financial.pyramid.web.form.RegistrationForm;
 import com.financial.pyramid.web.tree.BinaryTree;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.security.crypto.password.PasswordEncoder;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * User: Danil
  * Date: 29.05.13
  * Time: 22:17
  */
 @Service("userService")
 @Transactional(readOnly = true)
 public class UserServiceImpl implements UserService {
 
     private final static Logger logger = Logger.getLogger(UserService.class);
 
     private final static Integer COUNT_LEVEL_IN_USER_TREE = 4;
 
     @Autowired
     private UserDao userDao;
 
     @Autowired
     SettingsService settingsService;
 
     @Autowired
     AccountService accountService;
 
     @Autowired
     private PasswordEncoder passwordEncoder;
 
     @Override
     @Transactional(readOnly = false)
     public void save(User user) {
         userDao.merge(user);
     }
 
     @Override
     @Transactional(readOnly = false)
     @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
     public void delete(Long id) {
         User user = userDao.findById(id);
         User parent = findParent(id);
         if (parent != null && parent.getLeftChild() != null && parent.getLeftChild().equals(user)) {
             parent.setLeftChild(null);
             userDao.saveOrUpdate(parent);
         }
         if (parent != null && parent.getRightChild() != null && parent.getRightChild().equals(user)) {
             parent.setRightChild(null);
             userDao.saveOrUpdate(parent);
         }
         userDao.delete(user);
     }
 
     @Override
     public User findById(Long id) {
         return userDao.findById(id);
     }
 
     @Override
     public boolean checkEmail(String email) {
         return userDao.isEmail(email);
     }
 
     @Override
     public List<User> list() {
         return userDao.findAll();
     }
 
     @Override
     public List<User> findByQuery(QueryForm form) {
         return userDao.findByQuery(form);
     }
 
     @Override
     @Transactional(readOnly = false)
     @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
     public void update(AdminRegistrationForm form) {
         SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
         User user = findById(form.getId());
         user.setName(form.getName());
         user.setSurname(form.getSurname());
         user.setPatronymic(form.getPatronymic());
         user.setPhoneNumber(form.getPhoneNumber());
         user.setEmail(form.getEmail());
         try {
             Date date = format.parse(form.getDateOfBirth());
             user.setDateOfBirth(date);
         } catch (ParseException e) {
             logger.error("User date of birth is not set");
         }
 
         Passport passport = user.getPassport();
         passport.setSerial(form.getPassportSerial());
         passport.setNumber(form.getPassportNumber());
         passport.setIssuedBy(form.getPassportIssuedBy());
         passport.setRegisteredAddress(form.getRegisteredAddress());
         passport.setResidenceAddress(form.getResidenceAddress());
 
         try {
             Date date = format.parse(form.getPassportDate());
             passport.setDate(date);
         } catch (ParseException e) {
             logger.error("User passport date is not set");
         }
         user.setPassport(passport);
        userDao.saveOrUpdate(user);
     }
 
     @Override
     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
         User user = findByEmail(username);
         if (user != null) {
             return new org.springframework.security.core.userdetails.User(
                     user.getEmail(),
                     user.getPassword(),
                     Arrays.asList(new SimpleGrantedAuthority(user.getRole().name())));
         } else {
             throw new UsernameNotFoundException("User with name " + username + " not found");
         }
     }
 
     @Override
     public User findByEmail(String email) {
         return userDao.findByEmail(email);
     }
 
     @Override
     public BinaryTree getBinaryTree(User u) {
         User user = findById(u.getId());
         Integer levels = Integer.parseInt(settingsService.getProperty(Setting.COUNT_LEVEL_IN_USER_TREE));
         return new BinaryTree(user, levels == null ? COUNT_LEVEL_IN_USER_TREE : levels);
     }
 
     @Override
     public List<User> findUsersByOwner(Long ownerId) {
         return userDao.findByOwner(ownerId);
     }
 
     @Override
     public AccountDetails getAccountDetails(User u) {
         User user = findById(u.getId());
         AccountDetails accountDetails = new AccountDetails();
 
         Date activationStartDate = user.getAccount().getDateActivated();
         Date activationEndDate = user.getAccount().getDateExpired();
 
         accountDetails.setDateExpired(activationEndDate);
         accountDetails.setDateActivated(activationStartDate);
 
         Double balance = user.getAccount().getBalance();
         accountDetails.setBalance(new BigDecimal(balance).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
 
         if (activationStartDate != null) {
             accountDetails.setDaysMonth(Days.daysBetween(new DateTime(activationStartDate),
                     new DateTime(activationEndDate).minusDays(1)).getDays());
         }
 
         if (activationEndDate != null) {
             Calendar calendar = Calendar.getInstance();
             calendar.setTime(new Date());
             calendar.set(Calendar.HOUR, 0);
             calendar.set(Calendar.MINUTE, 0);
             calendar.set(Calendar.SECOND, 0);
             calendar.set(Calendar.MILLISECOND, 0);
             accountDetails.setDaysLeft(Days.daysBetween(new DateTime(calendar.getTime()), new DateTime(activationEndDate)).getDays());
         }
 
         if (accountDetails.getDaysLeft() < 0 && !user.getAccount().isLocked()) {
             this.deactivateUserAccount(user);
         }
 
         accountDetails.setAppPaid(user.getAccount().isAppPaid());
         return accountDetails;
     }
 
     @Override
     public Account getAccount(User u) {
         User user = findById(u.getId());
         return user.getAccount();
     }
 
     @Override
     @Transactional(readOnly = false)
     public void activateUserAccount(User user) {
         Account account = getAccount(user);
         if (account.isLocked()) {
             accountService.activate(account);
         }
     }
 
     @Override
     @Transactional(readOnly = false)
     public void deactivateUserAccount(User user) {
         Account account = getAccount(user);
         if (!account.isLocked()) {
             accountService.deactivate(account);
         }
     }
 
     @Override
     public Long getCountUsersOnLevel(Integer level) {
         return userDao.getCountUsersOnLevel(level);
     }
 
     @Override
     public User findParent(Long userId) {
         return userDao.findParent(userId);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void withdrawFromAccount(User user, Double count) {
         User u = findById(user.getId());
         if(u == null) return;
         u.getAccount().writeOFF(count);
         save(user);
     }
 }
