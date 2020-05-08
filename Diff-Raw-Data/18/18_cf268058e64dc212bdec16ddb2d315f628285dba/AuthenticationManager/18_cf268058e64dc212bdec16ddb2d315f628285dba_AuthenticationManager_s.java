 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.controller.core.manager.authentication;
 
 import java.util.Map;
 
import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.data.session.MAuthenticationBean;
 import com.mymed.model.data.user.MUserBean;
 
 /**
  * The manager for the authentication bean
  * 
  * @author lvanni
  * @author Milo Casagrande
  */
 public class AuthenticationManager extends AbstractManager implements IAuthenticationManager {
 
     /**
      * The 'login' field.
      */
     private static final String FIELD_LOGIN = FIELDS.get("field.login");
 
     /**
      * The 'authentication' column family.
      */
     private static final String CF_AUTHENTICATION = COLUMNS.get("column.cf.authentication");
 
     /**
      * Default constructor.
      * 
      * @throws InternalBackEndException
      */
     public AuthenticationManager() throws InternalBackEndException {
         this(new StorageManager());
     }
 
     public AuthenticationManager(final IStorageManager storageManager) throws InternalBackEndException {
         super(storageManager);
     }
 
     /**
      * @throws IOBackEndException
      * @see IAuthenticationManager#create(MUserBean, MAuthenticationBean)
      */
     @Override
     public final MUserBean create(final MUserBean user, final MAuthenticationBean authentication)
                     throws InternalBackEndException, IOBackEndException {
 
         final ProfileManager profileManager = new ProfileManager(storageManager);
         profileManager.create(user);
 
         try {
             read(authentication.getLogin(), authentication.getPassword());
         } catch (final IOBackEndException e) {
             // only if the user does not exist
             if (e.getStatus() == ERROR_NOT_FOUND) {
                 final Map<String, byte[]> authMap = authentication.getAttributeToMap();
                 storageManager.insertSlice(CF_AUTHENTICATION,
                                 com.mymed.utils.MConverter.byteArrayToString(authMap.get(FIELD_LOGIN)), authMap);
                 return user;
             }
         }
 
         throw new IOBackEndException("The login already exist", ERROR_CONFLICT);
     }
 
     /**
      * @see IAuthenticationManager#read(String, String)
      */
     @Override
     public final MUserBean read(final String login, final String password) throws InternalBackEndException,
                     IOBackEndException {
 
         final Map<byte[], byte[]> args = storageManager.selectAll(CF_AUTHENTICATION, login);
         final MAuthenticationBean authentication = (MAuthenticationBean) introspection(MAuthenticationBean.class, args);
 
         if ("".equals(authentication.getLogin())) {
             throw new IOBackEndException("The login does not exist!", ERROR_NOT_FOUND);
         } else if (!authentication.getPassword().equals(password)) {
             throw new IOBackEndException("Wrong password", ERROR_FORBIDDEN);
         }
 
         return new ProfileManager(storageManager).read(authentication.getUser());
     }
 
     /**
      * @throws IOBackEndException
      * @see IAuthenticationManager#update(MAuthenticationBean)
      */
     @Override
     public final void update(final String id, final MAuthenticationBean authentication)
                     throws InternalBackEndException, IOBackEndException {
         // Remove the old Authentication (the login/key can be changed)
         storageManager.removeAll(CF_AUTHENTICATION, id);
         // Insert the new Authentication
         storageManager.insertSlice(CF_AUTHENTICATION, FIELD_LOGIN, authentication.getAttributeToMap());
 
         final Map<String, byte[]> authMap = authentication.getAttributeToMap();
         storageManager.insertSlice(CF_AUTHENTICATION,
                         com.mymed.utils.MConverter.byteArrayToString(authMap.get(FIELD_LOGIN)), authMap);
     }
 
 
 }
