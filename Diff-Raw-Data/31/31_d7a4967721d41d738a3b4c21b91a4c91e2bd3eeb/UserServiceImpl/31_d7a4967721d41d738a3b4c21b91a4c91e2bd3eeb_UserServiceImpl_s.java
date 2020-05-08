 package com.financial.pyramid.service.impl;
 
 import com.financial.pyramid.dao.UserDao;
 import com.financial.pyramid.domain.Passport;
 import com.financial.pyramid.domain.User;
 import com.financial.pyramid.domain.type.Role;
 import com.financial.pyramid.service.SettingsService;
 import com.financial.pyramid.service.UserService;
 import com.financial.pyramid.service.beans.AccountDetails;
 import com.financial.pyramid.settings.Setting;
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
 
     @Override
     @Transactional(readOnly = false)
     public void save(User user) {
         userDao.saveOrUpdate(user);
     }
 
     @Override
     @Transactional(readOnly = false)
     @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
     public void delete(Long id) {
         User user = userDao.findById(id);
         User parent = findParent(id);
         if(parent != null && parent.getLeftChild() != null && parent.getLeftChild().equals(user)) {
             parent.setLeftChild(null);
         }
         if(parent != null && parent.getRightChild() != null && parent.getRightChild().equals(user)) {
             parent.setRightChild(null);
         }
         userDao.saveOrUpdate(parent);
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
     @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
     public void update(RegistrationForm form) {
         SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
         User user = findById(Long.parseLong(form.getId()));
         user.setName(form.getName());
         user.setSurname(form.getSurname());
         user.setPatronymic(form.getPatronymic());
         user.setPhoneNumber(form.getPhoneNumber());
         user.setRole(Role.USER);
 
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
         save(user);
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
     public String createPassword(int n) {
         Random rd = new Random();
 
         char lowerChars[] = "abcdefghijklmnopqrstuvwxyz".toCharArray();
         char upperChars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
         char numbers[] = "0123456789".toCharArray();
         char specialChars[] = "~!@#$%^&*()-_=+[{]}|;:<>/?".toCharArray();
 
         List<Character> pwdLst = new ArrayList<Character>();
         for (int g = 0; g < 4; g++) {
             for (int z = 0; z < 1; z++) {
                 if (g == 0) {
                     pwdLst.add(numbers[rd.nextInt(10)]);
                 } else if (g == 1) {
                     pwdLst.add(lowerChars[rd.nextInt(26)]);
                 } else if (g == 2) {
                     pwdLst.add(upperChars[rd.nextInt(26)]);
                 } else if (g == 3) {
                     pwdLst.add(specialChars[rd.nextInt(26)]);
                 }
             }
             if (pwdLst.size() == n) {
                 break;
             }
             if (g + 1 == 4) {
                 g = (int) Math.random() * 5;
 
             }
         }
         StringBuilder password = new StringBuilder();
         Collections.shuffle(pwdLst);
         for (int c = 0; c < pwdLst.size(); c++) {
             password.append(pwdLst.get(c));
         }
         return password.toString();
 
     }
 
     @Override
     public AccountDetails getAccountDetails(User u) {
         User user = findById(u.getId());
         AccountDetails accountDetails = new AccountDetails();
 
         Date activationStartDate = user.getAccount().getDateActivated();
         Date activationEndDate = user.getAccount().getDateExpired();
         Double earningsAmount = user.getAccount().getEarningsSum();
         accountDetails.setEarningsSum(new BigDecimal(earningsAmount).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
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
         return accountDetails;
     }
 
     public User findParent(Long userId) {
         return userDao.findParent(userId);
     }
 }
