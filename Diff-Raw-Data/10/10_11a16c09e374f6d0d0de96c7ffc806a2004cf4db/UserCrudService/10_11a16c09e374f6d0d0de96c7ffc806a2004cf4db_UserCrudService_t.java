 package fr.esiea.esieaddress.service.crud.implementation;
 
 import fr.esiea.esieaddress.dao.ICrudUserDao;
 import fr.esiea.esieaddress.dao.exception.DaoException;
 import fr.esiea.esieaddress.model.user.User;
 import fr.esiea.esieaddress.service.crud.ICrudUserService;
 import fr.esiea.esieaddress.service.exception.InvalidIdException;
 import fr.esiea.esieaddress.service.exception.NotUniqueEmailException;
 import fr.esiea.esieaddress.service.exception.ServiceException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import java.util.Collection;
 
 /**
  * Copyright (c) 2013 ESIEA M. Labusquiere D. Déïs
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 @Service
 public class UserCrudService implements ICrudUserService {
     @Autowired
     @Qualifier("databaseUser")
     private ICrudUserDao dao;
 
     @Override
     public Collection<User> getAll() throws DaoException {
         return dao.getAll();
     }
 
     @Override
     public void remove(String idUser) throws DaoException {
         dao.remove(idUser);
     }
 
     @Override
     public void save(User user) throws DaoException, ServiceException {
         if(checkUniqueEmail(user.getMail()))
             dao.save(user);
         else
             throw new NotUniqueEmailException();
     }
 
     @Override
     public void insert(User user) throws DaoException, ServiceException {
         if(checkUniqueEmail(user.getMail()))
             dao.insert(user);
         else
             throw new NotUniqueEmailException();
 
     }
 
     @Override
     public User getOne(String userId) throws InvalidIdException, DaoException {
         User user = dao.getOne(userId);
         if (null == user)
             throw new InvalidIdException();
         return user;
     }
 
     @Override
     public User getOneByMail(String mail) throws InvalidIdException, DaoException {
         User user = dao.getOneByMail(mail);
         return user;
     }
 
 
     private boolean checkUniqueEmail(String mail) throws ServiceException, DaoException {
         return null == getOneByMail(mail);
     }
 }
