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
 package org.qi4j.chronos.ui.wicket.bootstrap;
 
 import java.util.Calendar;
 import java.util.Currency;
 import java.util.Date;
 import java.util.Iterator;
 import org.qi4j.chronos.model.Account;
 import org.qi4j.chronos.model.AccountReport;
 import org.qi4j.chronos.model.Address;
 import org.qi4j.chronos.model.Admin;
 import org.qi4j.chronos.model.City;
 import org.qi4j.chronos.model.Comment;
 import org.qi4j.chronos.model.ContactPerson;
 import org.qi4j.chronos.model.Country;
 import org.qi4j.chronos.model.Customer;
 import org.qi4j.chronos.model.LegalCondition;
 import org.qi4j.chronos.model.Login;
 import org.qi4j.chronos.model.Money;
 import org.qi4j.chronos.model.Name;
 import org.qi4j.chronos.model.OngoingWorkEntry;
 import org.qi4j.chronos.model.PriceRate;
 import org.qi4j.chronos.model.PriceRateSchedule;
 import org.qi4j.chronos.model.PriceRateTypeEnum;
 import org.qi4j.chronos.model.Project;
 import org.qi4j.chronos.model.ProjectAssignee;
 import org.qi4j.chronos.model.ProjectRole;
 import org.qi4j.chronos.model.ProjectStatusEnum;
 import org.qi4j.chronos.model.Relationship;
 import org.qi4j.chronos.model.Report;
 import org.qi4j.chronos.model.Staff;
 import org.qi4j.chronos.model.State;
 import org.qi4j.chronos.model.SystemRole;
 import static org.qi4j.chronos.model.SystemRole.ACCOUNT_ADMIN;
 import static org.qi4j.chronos.model.SystemRole.ACCOUNT_DEVELOPER;
 import static org.qi4j.chronos.model.SystemRole.CONTACT_PERSON;
 import static org.qi4j.chronos.model.SystemRole.SYSTEM_ADMIN;
 import org.qi4j.chronos.model.SystemRoleTypeEnum;
 import org.qi4j.chronos.model.Task;
 import org.qi4j.chronos.model.TaskStatusEnum;
 import org.qi4j.chronos.model.TimeRange;
 import org.qi4j.chronos.model.User;
 import org.qi4j.chronos.model.WorkEntry;
 import org.qi4j.chronos.model.composites.AccountEntity;
 import org.qi4j.chronos.model.composites.AddressEntity;
 import org.qi4j.chronos.model.composites.AdminEntity;
 import org.qi4j.chronos.model.composites.CityEntity;
 import org.qi4j.chronos.model.composites.CommentEntity;
 import org.qi4j.chronos.model.composites.ContactEntity;
 import org.qi4j.chronos.model.composites.CountryEntity;
 import org.qi4j.chronos.model.composites.CustomerEntity;
 import org.qi4j.chronos.model.composites.LegalConditionEntity;
 import org.qi4j.chronos.model.composites.LoginEntity;
 import org.qi4j.chronos.model.composites.MoneyEntity;
 import org.qi4j.chronos.model.composites.OngoinWorkEntryEntity;
 import org.qi4j.chronos.model.composites.PriceRateEntity;
 import org.qi4j.chronos.model.composites.PriceRateScheduleEntity;
 import org.qi4j.chronos.model.composites.ProjectAssigneeEntity;
 import org.qi4j.chronos.model.composites.ProjectEntity;
 import org.qi4j.chronos.model.composites.ProjectRoleEntity;
 import org.qi4j.chronos.model.composites.RelationshipEntity;
 import org.qi4j.chronos.model.composites.StaffEntity;
 import org.qi4j.chronos.model.composites.StateEntity;
 import org.qi4j.chronos.model.composites.TaskEntity;
 import org.qi4j.chronos.model.composites.TimeRangeEntity;
 import org.qi4j.chronos.model.composites.WorkEntryEntity;
 import org.qi4j.chronos.util.ReportUtil;
 import org.qi4j.entity.EntityBuilder;
 import org.qi4j.entity.UnitOfWork;
 import org.qi4j.entity.UnitOfWorkCompletionException;
 import org.qi4j.entity.UnitOfWorkFactory;
 import org.qi4j.injection.scope.Structure;
 import org.qi4j.library.general.model.Contact;
 import org.qi4j.library.general.model.GenderType;
 import org.qi4j.query.QueryBuilder;
 import org.qi4j.query.QueryBuilderFactory;
 import static org.qi4j.query.QueryExpressions.eq;
 import static org.qi4j.query.QueryExpressions.or;
 import static org.qi4j.query.QueryExpressions.templateFor;
 
 final class DummyDataInitializer
 {
     private @Structure UnitOfWorkFactory unitOfWorkFactory;
 
     /**
      * Initialize mock-up data.
      */
     public void initializeDummyData()
     {
         initSystemRoles();
         initAccounts();
 
         initAdmin();
         initProjectRolesAndStaff();
 
         initPriceRateSchedule();
         initCustomersAndContactPersons();
         initProjectsTasksAndAssignees();
         initWorkEntries();
 
 //        initReports();
     }
 
     private void initReports()
     {
         UnitOfWork unitOfWork = newUnitOfWork( unitOfWorkFactory );
         Iterable<Account> accountQuery = getAllAccounts( unitOfWork );
 
         Calendar now = Calendar.getInstance();
         now.add( Calendar.DATE, -3 );
         Date endTime = now.getTime();
         now.add( Calendar.DATE, -8 );
         Date startTime = now.getTime();
 
         for( Account account : accountQuery )
         {
             account = unitOfWork.dereference( account );
 
             final AccountReport accountReport = ReportUtil.getAccountReport( unitOfWork, account );
             accountReport.account().set( account );
 
             for( Project project : account.projects() )
             {
                 Report report = ReportUtil.generateReport( unitOfWork, "Report for " + project.name().get(),
                                                            project, startTime, endTime );
                 accountReport.reports().add( report );
             }
         }
 
         complete( unitOfWork );
     }
 
     private Iterable<Account> getAllAccounts( UnitOfWork unitOfWork )
     {
         QueryBuilderFactory queryBuilderFactory = unitOfWork.queryBuilderFactory();
         QueryBuilder<Account> accountQueryBuilder = queryBuilderFactory.newQueryBuilder( Account.class );
         return accountQueryBuilder.newQuery();
     }
 
     /**
      * Creates work entry per projects and tasks.
      */
     private void initWorkEntries()
     {
         UnitOfWork unitOfWork = newUnitOfWork( unitOfWorkFactory );
 
         Iterable<Account> accounts = getAllAccounts( unitOfWork );
 
         for( Account account : accounts )
         {
             Calendar now = Calendar.getInstance();
             Date createdDate = now.getTime();
             Date endTime = now.getTime();
             now.add( Calendar.DATE, -30 );
             Date startTime = now.getTime();
 
             account = unitOfWork.dereference( account );
             for( Project project : account.projects() )
             {
                 ProjectAssignee assignee = project.projectAssignees().iterator().next();
                 WorkEntry projectWorkEntry = newWorkEntry( unitOfWork, "Project work entry", "Description",
                                                            createdDate, startTime, endTime, assignee );
                 Comment projectComment = newComment( unitOfWork, "Project work entry comment.",
                                                      createdDate, assignee.staff().get() );
                 projectWorkEntry.comments().add( projectComment );
                 project.workEntries().add( projectWorkEntry );
 
                 for( int j = 0; j < 1; j++ )
                 {
                     now.add( Calendar.HOUR_OF_DAY, -4 );
                     startTime = now.getTime();
                     now.add( Calendar.HOUR_OF_DAY, 2 );
                     endTime = now.getTime();
                     now.add( Calendar.HOUR_OF_DAY, 2 );
                     createdDate = now.getTime();
                     now.add( Calendar.DATE, 1 );
 
                     for( Task task : project.tasks() )
                     {
                         WorkEntry workEntry = newWorkEntry( unitOfWork, "Work Entry " + j, "Description",
                                                             createdDate, startTime, endTime, assignee );
                         Comment comment = newComment( unitOfWork, "This is a comment.",
                                                       createdDate, task.user().get() );
                         workEntry.comments().add( comment );
                         task.comments().add( newComment( unitOfWork, "This is a comment.",
                                                          createdDate, task.user().get() ) );
                         task.workEntries().add( workEntry );
 
                         OngoingWorkEntry ongoingWorkEntry = newOngoingWorkEntry( unitOfWork, createdDate, assignee );
                         task.onGoingWorkEntries().add( ongoingWorkEntry );
                     }
                 }
             }
         }
 
         complete( unitOfWork );
     }
 
     /**
      * Creates a project, legal condition, task, and project assignee per customer.
      */
     private void initProjectsTasksAndAssignees()
     {
         UnitOfWork unitOfWork = newUnitOfWork( unitOfWorkFactory );
 
         Iterable<Account> accounts = getAllAccounts( unitOfWork );
 
         Calendar now = Calendar.getInstance();
         now.add( Calendar.MONTH, -1 );
         Date startDate = now.getTime();
         now.add( Calendar.MONTH, 2 );
         Date endDate = now.getTime();
 
 
         for( Account account : accounts )
         {
             account = unitOfWork.dereference( account );
 
             final Customer customer = account.customers().iterator().next();
             ContactPerson contactPerson = customer.contactPersons().iterator().next();
             PriceRateSchedule priceRateSchedule = customer.priceRateSchedules().iterator().next();
             Staff staff = account.staffs().iterator().next();
             PriceRate priceRate = priceRateSchedule.priceRates().iterator().next();
             ProjectAssignee projectAssignee = newProjectAssignee( unitOfWork, true, staff, priceRate );
 
            Project project = newProject( unitOfWork, "Chronos Qi4j", "Chronos v0.1", ProjectStatusEnum.ACTIVE );
             project.customer().set( customer );
             project.primaryContactPerson().set( contactPerson );
             project.contactPersons().addAll( customer.contactPersons() );
             project.priceRateSchedule().set( priceRateSchedule );
             project.estimateTime().set( newTimeRange( unitOfWork, startDate, endDate ) );
             project.projectAssignees().add( projectAssignee );
 
             for( int i = 0; i < 7; i++ )
             {
                 Task task = newTask( unitOfWork, "Task " + i,
                                      "Task " + i + " description", startDate, TaskStatusEnum.OPEN );
                 task.user().set( staff );
                 project.tasks().add( task );
             }
 
             for( int i = 0; i < 7; i++ )
             {
                 LegalCondition condition = newLegalCondition( unitOfWork, "Maintenance contract",
                                                               "Maintenance contract 3 years" );
                 project.legalConditions().add( condition );
             }
 
             account.projects().add( project );
         }
 
         complete( unitOfWork );
     }
 
     /**
      * Creates a customer and a contact person for available accounts
      */
     private void initCustomersAndContactPersons()
     {
         UnitOfWork unitOfWork = newUnitOfWork( unitOfWorkFactory );
         Iterable<Account> accounts = getAllAccounts( unitOfWork );
 
         for( Account account : accounts )
         {
             account = unitOfWork.dereference( account );
             final Iterator<PriceRateSchedule> priceRateScheduleIterator = account.priceRateSchedules().iterator();
 
             ContactPerson projectManager = newContactPerson( unitOfWork, "Michael", "Lim", "michael", "michael",
                                                              GenderType.MALE, "Project Manager" );
             Contact mobile = newContact( unitOfWork, "Mobile", "7073247032" );
             projectManager.contacts().add( mobile );
 
             QueryBuilder<SystemRole> systemRoleQB = newSystemRoleQuery( unitOfWork );
 
             SystemRole contactPersonRoleTemplate = templateFor( SystemRole.class );
             SystemRole contactPersonRole = systemRoleQB.where( eq( contactPersonRoleTemplate.name(), CONTACT_PERSON ) ).newQuery().find();
 
             projectManager.systemRoles().add( contactPersonRole );
 
             Customer customer = newCustomer( unitOfWork, "Client A", "clientA",
                                              "line 1", "line 2", "city", "state", "country", "41412" );
             customer.priceRateSchedules().add( priceRateScheduleIterator.next() );
             customer.contactPersons().add( projectManager );
 
             account.customers().add( customer );
 
             ContactPerson projectManager2 = newContactPerson( unitOfWork, "Yada", "Yada", "yada", "yada",
                                                               GenderType.MALE, "Manager" );
             Contact mobile2 = newContact( unitOfWork, "Mobile", "7073247032" );
             projectManager2.contacts().add( mobile2 );
             projectManager2.systemRoles().add( contactPersonRole );
 
             Customer customer2 = newCustomer( unitOfWork, "YadaYada", "YadaYada",
                                               "line 1", "line 2", "city", "state", "country", "41412" );
             customer2.priceRateSchedules().add( priceRateScheduleIterator.next() );
             customer2.contactPersons().add( projectManager2 );
 
             account.customers().add( customer2 );
         }
 
         complete( unitOfWork );
     }
 
     /**
      * Create default project roles and staff for available accounts.
      */
     private void initProjectRolesAndStaff()
     {
         UnitOfWork unitOfWork = newUnitOfWork( unitOfWorkFactory );
 
         Iterable<Account> accounts = getAllAccounts( unitOfWork );
 
         for( Account account : accounts )
         {
             // Creating and adding project roles
             account = unitOfWork.dereference( account );
 
             ProjectRole programmerRole = newProjectRole( unitOfWork, "Programmer" );
             ProjectRole consultantRole = newProjectRole( unitOfWork, "Consultant" );
             ProjectRole projectManagerRole = newProjectRole( unitOfWork, "Project Manager" );
 
             account.projectRoles().add( programmerRole );
             account.projectRoles().add( consultantRole );
             account.projectRoles().add( projectManagerRole );
 
             // Creating and adding staffs
             Staff boss = newUser( unitOfWork, StaffEntity.class, "The", "Boss", GenderType.MALE );
             boss.login().set( newLogin( unitOfWork, "boss", "boss" ) );
             boss.salary().set( newMoney( unitOfWork, 8000L, "USD" ) );
 
             Staff developer = newUser( unitOfWork, StaffEntity.class, "The", "Developer", GenderType.MALE );
             developer.login().set( newLogin( unitOfWork, "developer", "developer" ) );
             developer.salary().set( newMoney( unitOfWork, 2000L, "USD" ) );
 
             QueryBuilder<SystemRole> systemRoleQB = newSystemRoleQuery( unitOfWork );
 
             SystemRole staffRole = templateFor( SystemRole.class );
 
             QueryBuilder<SystemRole> systemRoleQueryBuilder = unitOfWork.queryBuilderFactory().newQueryBuilder( SystemRole.class );
 
             systemRoleQueryBuilder.where( or(
                 eq( staffRole.name(), ACCOUNT_ADMIN ),
                 eq( staffRole.name(), ACCOUNT_DEVELOPER )
             ) );
 
             for( SystemRole role : systemRoleQB.newQuery() )
             {
                 role = unitOfWork.dereference( role );
                 boss.systemRoles().add( role );
                 if( equals( role, ACCOUNT_DEVELOPER ) )
                 {
                     developer.systemRoles().add( role );
                 }
             }
 
             account.staffs().add( boss );
             account.staffs().add( developer );
         }
 
         complete( unitOfWork );
     }
 
     /**
      * Creates default price rate schedule for available accounts.
      */
     private void initPriceRateSchedule()
     {
         UnitOfWork unitOfWork = newUnitOfWork( unitOfWorkFactory );
 
         Iterable<Account> accounts = getAllAccounts( unitOfWork );
 
         for( Account account : accounts )
         {
             account = unitOfWork.dereference( account );
 
             PriceRateSchedule priceRateSchedule = newPriceRateSchedule( unitOfWork, "Default A" );
             priceRateSchedule.currency().set( Currency.getInstance( "USD" ) );
 
             for( ProjectRole projectRole : account.projectRoles() )
             {
                 PriceRate priceRate = newPriceRate( unitOfWork, 3000L, "USD", PriceRateTypeEnum.MONTHLY );
                 priceRate.projectRole().set( projectRole );
                 priceRateSchedule.priceRates().add( priceRate );
             }
             account.priceRateSchedules().add( priceRateSchedule );
 
             PriceRateSchedule priceRateSchedule2 = newPriceRateSchedule( unitOfWork, "Default B" );
             priceRateSchedule2.currency().set( Currency.getInstance( "EUR" ) );
 
             for( ProjectRole projectRole : account.projectRoles() )
             {
                 PriceRate priceRate2 = newPriceRate( unitOfWork, 2000L, "EUR", PriceRateTypeEnum.MONTHLY );
                 priceRate2.projectRole().set( projectRole );
                 priceRateSchedule2.priceRates().add( priceRate2 );
             }
             account.priceRateSchedules().add( priceRateSchedule2 );
         }
 
         complete( unitOfWork );
     }
 
     /**
      * Creates 2 default accounts.
      */
 
     private void initAccounts()
     {
         UnitOfWork unitOfWork = newUnitOfWork( unitOfWorkFactory );
 
         Address address = newAddress( unitOfWork, "AbcMixin Road", "Way Centre", "KL",
                                       "Wilayah Persekutuan", "Malaysia", "12345" );
         Account jayway = newAccount( unitOfWork, "Jayway Malaysia", "Jayway Malaysia Sdn. Bhd." );
         jayway.address().set( address );
 
         address = newAddress( unitOfWork, "AbcMixin Road", "Way Centre", "KL",
                               "Wilayah Persekutuan", "Malaysia", "12345" );
         Account testCorp = newAccount( unitOfWork, "Test Corp", "Test Corporation" );
         testCorp.address().set( address );
 
         complete( unitOfWork );
     }
 
     /**
      * Creates all available system roles and populates the role service.
      */
     private void initSystemRoles()
     {
         UnitOfWork unitOfWork = newUnitOfWork( unitOfWorkFactory );
 
         SystemRole adminRole =
             unitOfWork.newEntityBuilder( SYSTEM_ADMIN, SystemRole.class ).newInstance();
         adminRole.name().set( SYSTEM_ADMIN );
         adminRole.systemRoleType().set( SystemRoleTypeEnum.ADMIN );
 
         SystemRole accountAdmin =
             unitOfWork.newEntityBuilder( ACCOUNT_ADMIN, SystemRole.class ).newInstance();
         accountAdmin.name().set( ACCOUNT_ADMIN );
         accountAdmin.systemRoleType().set( SystemRoleTypeEnum.STAFF );
 
         SystemRole developer =
             unitOfWork.newEntityBuilder( ACCOUNT_DEVELOPER, SystemRole.class ).newInstance();
         developer.name().set( ACCOUNT_DEVELOPER );
         developer.systemRoleType().set( SystemRoleTypeEnum.STAFF );
 
         SystemRole contactPerson =
             unitOfWork.newEntityBuilder( CONTACT_PERSON, SystemRole.class ).newInstance();
         contactPerson.name().set( CONTACT_PERSON );
         contactPerson.systemRoleType().set( SystemRoleTypeEnum.CONTACT_PERSON );
 
         complete( unitOfWork );
     }
 
     /**
      * Creates the default admin user.
      */
     private void initAdmin()
     {
         UnitOfWork unitOfWork = newUnitOfWork( unitOfWorkFactory );
 
         Login adminLogin = newLogin( unitOfWork, "admin", "admin" );
         Admin adminUser = newUser( unitOfWork, AdminEntity.class, "System",
                                    "Administrator", GenderType.MALE );
         adminUser.login().set( adminLogin );
 
         QueryBuilder<SystemRole> queryBuilder = newSystemRoleQuery( unitOfWork );
         for( SystemRole role : queryBuilder.newQuery() )
         {
             adminUser.systemRoles().add( unitOfWork.dereference( role ) );
         }
 
         complete( unitOfWork );
     }
 
     private QueryBuilder<SystemRole> newSystemRoleQuery( UnitOfWork unitOfWork )
     {
         QueryBuilderFactory queryBuilderFactory = unitOfWork.queryBuilderFactory();
 
         return queryBuilderFactory.newQueryBuilder( SystemRole.class );
     }
 
 
     /**
      * Compares a given Name composite with a text. Returns true if text equals name.
      */
     private boolean equals( Name name, String text )
     {
         return text.equals( name.name().get() );
     }
 
     protected static void complete( UnitOfWork unitOfWork )
     {
         try
         {
             unitOfWork.complete();
         }
         catch( UnitOfWorkCompletionException uofwce )
         {
             System.err.println( uofwce.getLocalizedMessage() );
             uofwce.printStackTrace();
 
             unitOfWork.reset();
         }
     }
 
     protected static UnitOfWork newUnitOfWork( UnitOfWorkFactory unitOfWorkFactory )
     {
         return unitOfWorkFactory.newUnitOfWork();
     }
 
     protected static Login newLogin( UnitOfWork unitOfWork, String username, String password )
     {
 
         EntityBuilder<LoginEntity> loginBuilder = unitOfWork.newEntityBuilder( LoginEntity.class );
         loginBuilder.stateOfComposite().name().set( username );
         loginBuilder.stateOfComposite().password().set( password );
         loginBuilder.stateOfComposite().isEnabled().set( true );
 
         return loginBuilder.newInstance();
     }
 
     protected static <T extends User, K extends T> T newUser( UnitOfWork unitOfWork, Class<K> clazz,
                                                               String firstName, String lastName, GenderType gender )
     {
         EntityBuilder<K> userBuilder = unitOfWork.newEntityBuilder( clazz );
         userBuilder.stateOfComposite().firstName().set( firstName );
         userBuilder.stateOfComposite().lastName().set( lastName );
         userBuilder.stateOfComposite().gender().set( gender );
 
         return userBuilder.newInstance();
     }
 
     protected static Account newAccount( UnitOfWork unitOfWork, String name, String reference,
                                          String firstLine, String secondLine, String cityName,
                                          String stateName, String countryName, String zipCode )
     {
         EntityBuilder<AccountEntity> accountBuilder =
             unitOfWork.newEntityBuilder( AccountEntity.class );
         accountBuilder.stateOfComposite().isEnabled().set( true );
         accountBuilder.stateOfComposite().name().set( name );
         accountBuilder.stateOfComposite().reference().set( reference );
         accountBuilder.stateOfComposite().address().set(
             newAddress( unitOfWork, firstLine, secondLine, cityName, stateName,
                         countryName, zipCode ) );
 
         return accountBuilder.newInstance();
     }
 
     protected static Account newAccount( UnitOfWork unitOfWork, String name, String reference )
     {
         EntityBuilder<AccountEntity> accountBuilder =
             unitOfWork.newEntityBuilder( AccountEntity.class );
         accountBuilder.stateOfComposite().isEnabled().set( true );
         accountBuilder.stateOfComposite().name().set( name );
         accountBuilder.stateOfComposite().reference().set( reference );
 
         return accountBuilder.newInstance();
     }
 
     protected static Address newAddress( UnitOfWork unitOfWork, String firstLine,
                                          String secondLine, String cityName,
                                          String stateName, String countryName, String zipCode )
     {
         EntityBuilder<AddressEntity> addressBuilder =
             unitOfWork.newEntityBuilder( AddressEntity.class );
         addressBuilder.stateOfComposite().firstLine().set( firstLine );
         addressBuilder.stateOfComposite().secondLine().set( secondLine );
         addressBuilder.stateOfComposite().zipCode().set( zipCode );
         addressBuilder.stateOfComposite().city().set(
             newCity( unitOfWork, cityName, newState( unitOfWork, stateName ),
                      newCountry( unitOfWork, countryName ) ) );
 
         return addressBuilder.newInstance();
     }
 
     protected static City newCity( UnitOfWork unitOfWork, String cityName, State state, Country country )
     {
         EntityBuilder<CityEntity> cityBuilder = unitOfWork.newEntityBuilder( CityEntity.class );
         cityBuilder.stateOfComposite().name().set( cityName );
         cityBuilder.stateOfComposite().state().set( state );
         cityBuilder.stateOfComposite().country().set( country );
 
         return cityBuilder.newInstance();
     }
 
     protected static State newState( UnitOfWork unitOfWork, String stateName )
     {
         EntityBuilder<StateEntity> stateBuilder = unitOfWork.newEntityBuilder( StateEntity.class );
         stateBuilder.stateOfComposite().name().set( stateName );
 
         return stateBuilder.newInstance();
     }
 
     protected static Country newCountry( UnitOfWork unitOfWork, String countryName )
     {
         EntityBuilder<CountryEntity> countryBuilder =
             unitOfWork.newEntityBuilder( CountryEntity.class );
         countryBuilder.stateOfComposite().name().set( countryName );
 
         return countryBuilder.newInstance();
     }
 
     protected static Money newMoney( UnitOfWork unitOfWork, Long amount, String currencyCode )
     {
         EntityBuilder<MoneyEntity> money = unitOfWork.newEntityBuilder( MoneyEntity.class );
         money.stateOfComposite().amount().set( amount );
         money.stateOfComposite().currency().set( Currency.getInstance( currencyCode ) );
 
         return money.newInstance();
     }
 
     protected static ProjectRole newProjectRole( UnitOfWork unitOfWork, String projectRoleName )
     {
         EntityBuilder<ProjectRoleEntity> projectRoleBuilder =
             unitOfWork.newEntityBuilder( ProjectRoleEntity.class );
         projectRoleBuilder.stateOfComposite().name().set( projectRoleName );
 
         return projectRoleBuilder.newInstance();
     }
 
     protected static PriceRateSchedule newPriceRateSchedule( UnitOfWork unitOfWork, String reference )
     {
         EntityBuilder<PriceRateScheduleEntity> priceRateScheduleBuilder =
             unitOfWork.newEntityBuilder( PriceRateScheduleEntity.class );
         priceRateScheduleBuilder.stateOfComposite().name().set( reference );
 
         return priceRateScheduleBuilder.newInstance();
     }
 
     protected static PriceRate newPriceRate( UnitOfWork unitOfWork, Long amount, String currencyCode,
                                              PriceRateTypeEnum priceRateTypeEnum )
     {
         EntityBuilder<PriceRateEntity> priceRateBuilder =
             unitOfWork.newEntityBuilder( PriceRateEntity.class );
         priceRateBuilder.stateOfComposite().amount().set( amount );
         priceRateBuilder.stateOfComposite().currency().set( Currency.getInstance( currencyCode ) );
         priceRateBuilder.stateOfComposite().priceRateType().set( priceRateTypeEnum );
 
         return priceRateBuilder.newInstance();
     }
 
     protected static Customer newCustomer( UnitOfWork unitOfWork, String customerName,
                                            String reference, String firstLine, String secondLine,
                                            String cityName, String stateName, String countryName, String zipCode )
     {
         EntityBuilder<CustomerEntity> customerBuilder =
             unitOfWork.newEntityBuilder( CustomerEntity.class );
         customerBuilder.stateOfComposite().name().set( customerName );
         customerBuilder.stateOfComposite().reference().set( reference );
         customerBuilder.stateOfComposite().isEnabled().set( true );
         customerBuilder.stateOfComposite().address().set(
             newAddress( unitOfWork, firstLine, secondLine, cityName, stateName, countryName, zipCode ) );
 
         return customerBuilder.newInstance();
     }
 
     protected static ContactPerson newContactPerson( UnitOfWork unitOfWork, String firstName,
                                                      String lastName, String username, String password,
                                                      GenderType genderType, String relationshipName )
     {
         EntityBuilder<ContactPerson> contactPersonBuilder = unitOfWork.newEntityBuilder( ContactPerson.class );
         contactPersonBuilder.stateOfComposite().firstName().set( firstName );
         contactPersonBuilder.stateOfComposite().lastName().set( lastName );
         contactPersonBuilder.stateOfComposite().gender().set( genderType );
         contactPersonBuilder.stateOfComposite().login().set( newLogin( unitOfWork, username, password ) );
         contactPersonBuilder.stateOfComposite().relationship().set( newRelationship( unitOfWork, relationshipName ) );
 
         return contactPersonBuilder.newInstance();
     }
 
     protected static Relationship newRelationship( UnitOfWork unitOfWork, String relationshipName )
     {
         EntityBuilder<RelationshipEntity> relationshipBuilder =
             unitOfWork.newEntityBuilder( RelationshipEntity.class );
         relationshipBuilder.stateOfComposite().relationship().set( relationshipName );
 
         return relationshipBuilder.newInstance();
     }
 
     protected static Contact newContact( UnitOfWork unitOfWork, String contactType, String contactValue )
     {
         EntityBuilder<ContactEntity> contactBuilder =
             unitOfWork.newEntityBuilder( ContactEntity.class );
         contactBuilder.stateOfComposite().contactType().set( contactType );
         contactBuilder.stateOfComposite().contactValue().set( contactValue );
 
         return contactBuilder.newInstance();
     }
 
     protected static Project newProject( UnitOfWork unitOfWork, String projectName,
                                          String formalReference, ProjectStatusEnum projectStatus )
     {
         EntityBuilder<ProjectEntity> projectBuilder =
             unitOfWork.newEntityBuilder( ProjectEntity.class );
         projectBuilder.stateOfComposite().name().set( projectName );
         projectBuilder.stateOfComposite().reference().set( formalReference );
         projectBuilder.stateOfComposite().projectStatus().set( projectStatus );
 
         return projectBuilder.newInstance();
     }
 
     protected static TimeRange newTimeRange( UnitOfWork unitOfWork, Date startDate, Date endDate )
     {
         EntityBuilder<TimeRangeEntity> timeRangeBuilder =
             unitOfWork.newEntityBuilder( TimeRangeEntity.class );
         timeRangeBuilder.stateOfComposite().startTime().set( startDate );
         timeRangeBuilder.stateOfComposite().endTime().set( endDate );
 
         return timeRangeBuilder.newInstance();
     }
 
     protected static ProjectAssignee newProjectAssignee( UnitOfWork unitOfWork, boolean isLead,
                                                          Staff staff, PriceRate priceRate )
     {
         EntityBuilder<ProjectAssigneeEntity> projectAssigneeBuilder =
             unitOfWork.newEntityBuilder( ProjectAssigneeEntity.class );
         projectAssigneeBuilder.stateOfComposite().isLead().set( isLead );
         projectAssigneeBuilder.stateOfComposite().staff().set( staff );
         projectAssigneeBuilder.stateOfComposite().priceRate().set( priceRate );
 
         return projectAssigneeBuilder.newInstance();
     }
 
     protected static LegalCondition newLegalCondition( UnitOfWork unitOfWork, String value, String description )
     {
         EntityBuilder<LegalConditionEntity> legalConditionBuilder =
             unitOfWork.newEntityBuilder( LegalConditionEntity.class );
         legalConditionBuilder.stateOfComposite().name().set( value );
         legalConditionBuilder.stateOfComposite().description().set( description );
 
         return legalConditionBuilder.newInstance();
     }
 
     protected static Task newTask( UnitOfWork unitOfWork, String title, String description,
                                    Date createdDate, TaskStatusEnum taskStatus )
     {
         EntityBuilder<TaskEntity> taskBuilder = unitOfWork.newEntityBuilder( TaskEntity.class );
         taskBuilder.stateOfComposite().title().set( title );
         taskBuilder.stateOfComposite().description().set( description );
         taskBuilder.stateOfComposite().createdDate().set( createdDate );
         taskBuilder.stateOfComposite().taskStatus().set( taskStatus );
 
         return taskBuilder.newInstance();
     }
 
     protected static Comment newComment( UnitOfWork unitOfWork, String comment, Date createdDate, User user )
     {
         EntityBuilder<CommentEntity> commentBuilder =
             unitOfWork.newEntityBuilder( CommentEntity.class );
         commentBuilder.stateOfComposite().text().set( comment );
         commentBuilder.stateOfComposite().createdDate().set( createdDate );
         commentBuilder.stateOfComposite().user().set( user );
 
         return commentBuilder.newInstance();
     }
 
     protected static WorkEntry newWorkEntry( UnitOfWork unitOfWork, String title, String description, Date createdDate,
                                              Date startTime, Date endTime, ProjectAssignee projectAssignee )
     {
         EntityBuilder<WorkEntryEntity> workEntryBuilder =
             unitOfWork.newEntityBuilder( WorkEntryEntity.class );
         workEntryBuilder.stateOfComposite().title().set( title );
         workEntryBuilder.stateOfComposite().description().set( description );
         workEntryBuilder.stateOfComposite().createdDate().set( createdDate );
         workEntryBuilder.stateOfComposite().startTime().set( startTime );
         workEntryBuilder.stateOfComposite().endTime().set( endTime );
         workEntryBuilder.stateOfComposite().projectAssignee().set( projectAssignee );
 
         return workEntryBuilder.newInstance();
     }
 
     protected static OngoingWorkEntry newOngoingWorkEntry( UnitOfWork unitOfWork,
                                                            Date createdDate, ProjectAssignee projectAssignee )
     {
         EntityBuilder<OngoinWorkEntryEntity> ongoingWorkEntryBuilder =
             unitOfWork.newEntityBuilder( OngoinWorkEntryEntity.class );
         ongoingWorkEntryBuilder.stateOfComposite().createdDate().set( createdDate );
         ongoingWorkEntryBuilder.stateOfComposite().projectAssignee().set( projectAssignee );
 
         return ongoingWorkEntryBuilder.newInstance();
     }
 }
