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
 
import static org.junit.Assert.*;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.qi4j.chronos.domain.model.account.Account;
 import org.qi4j.chronos.domain.model.account.AccountFactory;
 import org.qi4j.chronos.domain.model.account.AccountId;
 import org.qi4j.chronos.domain.model.account.AccountRepository;
 import org.qi4j.entity.UnitOfWork;
 import org.qi4j.entity.UnitOfWorkCompletionException;
 import org.qi4j.query.Query;
 import org.qi4j.service.ServiceFinder;
 import org.qi4j.service.ServiceReference;
 
 /**
  * @author edward.yakop@gmail.com
  * @since 0.5
  */
 public final class AccountRepositoryTest extends AbstractAccountTest
 {
     @Test
     @Ignore
     public final void findTest()
         throws UnitOfWorkCompletionException
     {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
 
         ServiceFinder serviceFinder = moduleInstance.serviceFinder();
         ServiceReference<AccountRepository> repositoryRef = serviceFinder.findService( AccountRepository.class );
         AccountRepository accountRepository = repositoryRef.get();
         ServiceReference<AccountFactory> factoryRef = serviceFinder.findService( AccountFactory.class );
         AccountFactory accountFactory = factoryRef.get();
 
         try
         {
             Query<Account> accountQuery = accountRepository.findAll();
             assertEquals( 0, accountQuery.count() );
 
             Account account = accountFactory.newAccount( "My Account" );
             assertNotNull( account );
             AccountId accountId = account.accountId();
             assertNotNull( accountId );
            String accountName = account.accountDetail().name();
             assertEquals( "My Account", accountName );
 
             uow.completeAndContinue();
 
             Account account1 = accountRepository.find( accountId );
             assertNotNull( account1 );
             accountId.sameValueAs( account1.accountId() );
            assertEquals( "My Account", account1.accountDetail().name() );
         }
         finally
         {
             factoryRef.releaseService();
             repositoryRef.releaseService();
             uow.discard();
         }
     }
 }
