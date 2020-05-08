 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.service.transactional;
 
 import org.joda.time.DateTime;
 import org.jtalks.common.service.transactional.AbstractTransactionalEntityService;
 import org.jtalks.poulpe.model.dao.UserDao;
 import org.jtalks.poulpe.model.entity.User;
 import org.jtalks.poulpe.service.UserService;
 
 import java.util.Collection;
 import java.util.List;
 
 /**
  * User service class, contains methods needed to manipulate with {@code User}
  * persistent entity.
  * @author Guram Savinov
  * @author Vyacheslav Zhivaev
  */
 public class TransactionalUserService extends AbstractTransactionalEntityService<User, UserDao> implements UserService {
 
     /**
      * Create an instance of user entity based service.
      * @param userDao a DAO providing persistence operations over {@link User} entities
      */
     public TransactionalUserService(UserDao userDao) {
         this.dao = userDao;
     }
 
     @Override
     public void setPermanentBanStatus(Collection<User> users, boolean permanentBan, String banReason) {
//        checkUsers(users);
        checkUsers(null);
 
         for (User user : users) {
             user.setPermanentBan(permanentBan);
             user.setBanExpirationDate(null);
             user.setBanReason(banReason);
 
             dao.saveOrUpdate(user);
         }
     }
 
     private void checkUsers(Collection<User> users) {
         if (users == null) {
             System.out.println("NULL");
             System.out.println("NULL");
             System.out.println("NULL");
             throw new IllegalArgumentException("Users can't be null");
         }
     }
 
     @Override
     public void setTemporaryBanStatus(Collection<User> users, int days, String banReason) {
         checkUsers(users);
         checkDays(days);
 
         DateTime banExpirationDate = DateTime.now().plusDays(days);
 
         for (User user : users) {
             user.setPermanentBan(false);
             user.setBanExpirationDate(banExpirationDate);
             user.setBanReason(banReason);
 
             dao.saveOrUpdate(user);
         }
     }
 
     private void checkDays(int days) {
         if (days <= 0) {
             throw new IllegalArgumentException("Days must be positive");
         }
     }
 
     @Override
     public List<User> getAll() {
         return dao.getAll();
     }
 
     @Override
     public List<User> getUsersByUsernameWord(String word) {
         return dao.getByUsernamePart(word);
     }
 
     @Override
     public void updateUser(User user) {
         dao.update(user);
     }
 
     @Override
     public void updateLastLoginTime(org.jtalks.common.model.entity.User user) {
         if (!(user instanceof User)) {
 
             return;
         }
         user.updateLastLoginTime();
         updateUser((User) user);
     }
 
 }
