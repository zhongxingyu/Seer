 /*
  * Copyright (c) 2007, Lan Boon Ping. All Rights Reserved.
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
 package org.qi4j.chronos.ui.util;
 
 import java.util.ArrayList;
 import java.util.Currency;
 import java.util.List;
 import org.qi4j.chronos.model.PriceRateSchedule;
 import org.qi4j.chronos.model.Staff;
 import org.qi4j.chronos.model.PriceRate;
 import org.qi4j.chronos.model.Account;
 import org.qi4j.chronos.model.ProjectRole;
 import org.qi4j.chronos.model.PriceRateTypeEnum;
 import org.qi4j.chronos.model.ProjectStatusEnum;
 import org.qi4j.chronos.model.TaskStatusEnum;
 import org.qi4j.chronos.model.associations.HasPriceRateSchedules;
 import org.qi4j.chronos.model.associations.HasPriceRates;
 import org.qi4j.chronos.model.associations.HasStaffs;
import org.qi4j.chronos.ui.ChronosWebApp;
 import org.qi4j.chronos.ui.pricerate.PriceRateDelegator;
 import org.qi4j.chronos.ui.projectrole.ProjectRoleDelegator;
 import org.qi4j.chronos.ui.staff.StaffDelegator;
 import org.qi4j.chronos.util.CurrencyUtil;
 import org.qi4j.entity.association.SetAssociation;
 import org.qi4j.library.general.model.GenderType;
 
 public final class ListUtil
 {
     public static List<String> getPriceRateScheduleNameList( HasPriceRateSchedules hasPriceRateSchedules )
     {
         List<String> nameList = new ArrayList<String>();
 
         SetAssociation<PriceRateSchedule> priceRateSchedules = hasPriceRateSchedules.priceRateSchedules();
         for( PriceRateSchedule priceRateScheduleComposite : priceRateSchedules )
         {
             nameList.add( priceRateScheduleComposite.name().get() );
         }
 
         return nameList;
     }
 
 
     public static List<StaffDelegator> getStaffDelegator( HasStaffs hasStaffs )
     {
         List<StaffDelegator> staffDelegatorList = new ArrayList<StaffDelegator>();
 
         SetAssociation<Staff> staffs = hasStaffs.staffs();
         for( Staff staffEntityComposite : staffs )
         {
             staffDelegatorList.add( new StaffDelegator( staffEntityComposite ) );
         }
 
         return staffDelegatorList;
     }
 
 
     public static List<PriceRateDelegator> getPriceRateDelegator( HasPriceRates hasPriceRates )
     {
         List<PriceRateDelegator> priceRateList = new ArrayList<PriceRateDelegator>();
         SetAssociation<PriceRate> priceRates = hasPriceRates.priceRates();
         for( PriceRate priceRateComposite : priceRates )
         {
             priceRateList.add( new PriceRateDelegator( priceRateComposite ) );
         }
 
         return priceRateList;
     }
 
     public static List<ProjectRoleDelegator> getProjectRoleDelegatorList( Account account )
     {
 //        List<ProjectRole> projectRolelists = ChronosWebApp.getServices().getProjectRoleService().findAll( account );
 
         List<ProjectRoleDelegator> resultList = new ArrayList<ProjectRoleDelegator>();
 
         for( ProjectRole projectRole : account.projectRoles() )
         {
             resultList.add( new ProjectRoleDelegator( projectRole ) );
         }
 
         return resultList;
     }
 
     public static List<String> getCurrencyList()
     {
         List<Currency> list = CurrencyUtil.getCurrencyList();
         List<String> resultList = new ArrayList<String>();
 
         for( Currency currency : list )
         {
             resultList.add( currency.getCurrencyCode() );
         }
 
         return resultList;
     }
 
     public static List<String> getPriceRateTypeList()
     {
         PriceRateTypeEnum[] priceRateTypes = PriceRateTypeEnum.values();
 
         List<String> list = new ArrayList<String>();
 
         for( PriceRateTypeEnum priceRateType : priceRateTypes )
         {
             list.add( priceRateType.toString() );
         }
 
         return list;
     }
 
     public static List<String> getGenderTypeList()
     {
         GenderType[] genderTypes = GenderType.values();
         List<String> result = new ArrayList<String>();
 
         for( GenderType genderType : genderTypes )
         {
             result.add( genderType.toString() );
         }
 
         return result;
     }
 
     public static List<String> getProjectStatusList()
     {
         List<String> result = new ArrayList<String>();
 
         for( ProjectStatusEnum projectStatus : ProjectStatusEnum.values() )
         {
             result.add( projectStatus.toString() );
         }
 
         return result;
     }
 
 /*
     public static List<String> getTaskStatusList()
     {
         List<String> resultList = new ArrayList<String>();
 
         for( TaskStatusEnum taskStatus : TaskStatusEnum.values() )
         {
             resultList.add( taskStatus.toString() );
         }
 
         return resultList;
     }
 */
 }
