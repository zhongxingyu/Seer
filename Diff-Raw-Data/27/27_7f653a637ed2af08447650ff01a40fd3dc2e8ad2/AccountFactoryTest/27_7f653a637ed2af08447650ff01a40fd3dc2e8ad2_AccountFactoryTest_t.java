 /*  Copyright 2008 Edward Yakop.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.qi4j.chronos.domain.model.account.assembly;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
 import org.junit.Test;
 import org.qi4j.chronos.domain.model.account.Account;
 import org.qi4j.chronos.domain.model.account.AccountFactory;
 import org.qi4j.entity.UnitOfWork;
 import org.qi4j.service.ServiceFinder;
 import org.qi4j.service.ServiceReference;
 
 /**
  * @author edward.yakop@gmail.com
  * @since 0.5
  */
 public final class AccountFactoryTest extends AbstractAccountTest
 {
     @Test
     public final void createAccountTest()
     {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
 
         ServiceFinder serviceFinder = moduleInstance.serviceFinder();
         ServiceReference<AccountFactory> accountFactoryRef = serviceFinder.findService( AccountFactory.class );
         AccountFactory accountFactory = accountFactoryRef.get();
         try
         {
             Account account = accountFactory.newAccount( "My Account" );
             assertNotNull( account );
 
            assertEquals( "My Account", account.name() );
         }
         finally
         {
             accountFactoryRef.releaseService();
             uow.discard();
         }
     }
 }
