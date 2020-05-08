 /*
  * Copyright (c) 2008, Muhd Kamil Mohd Baki. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.qi4j.chronos.domain.service.authentication.assembly;
 
 import org.qi4j.chronos.domain.model.account.Account;
 import org.qi4j.chronos.domain.model.customer.Customer;
 import org.qi4j.chronos.domain.model.user.Login;
 import org.qi4j.chronos.domain.model.user.User;
 import org.qi4j.chronos.domain.model.user.admin.Admin;
 import org.qi4j.chronos.domain.model.user.admin.AdminRepository;
 import org.qi4j.chronos.domain.model.user.contactPerson.ContactPerson;
 import org.qi4j.chronos.domain.model.user.staff.Staff;
 import org.qi4j.chronos.domain.service.authentication.AuthenticationService;
 import org.qi4j.chronos.domain.service.authentication.UserAuthenticationFailException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.common.Optional;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.ServiceComposite;
 
 /**
 * TODO user encode password
  */
 @Mixins( AuthenticationServiceComposite.AuthenticationServiceMixin.class )
 interface AuthenticationServiceComposite extends AuthenticationService, ServiceComposite
 {
     class AuthenticationServiceMixin
         implements AuthenticationService
     {
         @Structure private UnitOfWorkFactory uowf;
         @Service private AdminRepository adminRepository;
 
         public final User authenticate( @Optional Account anAccount, String aUserName, final String aPassword )
             throws UserAuthenticationFailException
         {
             UnitOfWork uow = uowf.currentUnitOfWork();
             if( anAccount == null )
             {
                 return authenticateAdminUser( uow, aUserName, aPassword );
             }
             else
             {
                 return authenticateNonAdminUser( uow, anAccount, aUserName, aPassword );
 
             }
         }
 
         private User authenticateNonAdminUser( UnitOfWork uow, Account anAccount, String aUserName, String aPassword )
             throws UserAuthenticationFailException
         {
             Account referencedAccount = uow.dereference( anAccount );
 
             // is user a staff
             Query<Staff> staffs = referencedAccount.staffs();
             User user = authenticateUsersFrom( aUserName, aPassword, staffs );
             if( user != null )
             {
                 return user;
             }
 
             // is user a customer
             Query<Customer> customers = referencedAccount.customers();
             for( Customer customer : customers )
             {
                 Query<ContactPerson> contactPersons = customer.contactPersons();
                 user = authenticateUsersFrom( aUserName, aPassword, contactPersons );
                 if( user != null )
                 {
                     return user;
                 }
             }
 
             throw new UserAuthenticationFailException( "Invalid password or username" );
         }
 
         private User authenticateUsersFrom( String aUserName, String aPassword, Iterable<? extends User> candidates )
         {
             for( User user : candidates )
             {
                 if( isUserCredentialsMatch( aUserName, aPassword, user ) )
                 {
                     return user;
                 }
             }
 
             return null;
         }
 
         private boolean isUserCredentialsMatch( String aUserName, String aPassword, User user )
         {
             Login userLogin = user.login();
             return aUserName.equals( userLogin.loginName() ) && userLogin.authenticate( aPassword );
         }
 
         private Admin authenticateAdminUser( UnitOfWork uow, String aUserName, String aPassword )
             throws UserAuthenticationFailException
         {
             Admin admin = adminRepository.findByLoginName( aUserName );
             if( admin != null )
             {
                 Login userLogin = admin.login();
                 if( userLogin.authenticate( aPassword ) )
                 {
                     return admin;
                 }
             }
 
             throw new UserAuthenticationFailException( "Invalid user or password" );
         }
     }
 }
