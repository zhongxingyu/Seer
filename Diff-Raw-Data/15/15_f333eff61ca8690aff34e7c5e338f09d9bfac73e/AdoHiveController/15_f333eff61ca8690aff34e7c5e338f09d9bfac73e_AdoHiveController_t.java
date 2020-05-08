 /*
  * Copyright (C) 2010 The AdoHive Team
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 /**
  *
  */
 package de.unistuttgart.iste.se.adohive.controller;
 
 import java.util.HashMap;
 
 import de.unistuttgart.iste.se.adohive.controller.ansi.AnsiAdoHiveController;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 /**
  * This class provides the sole access to the objects implementing the Manager interfaces.
  * @author Team AdoHive
  *
  */
 public abstract class AdoHiveController {
 
     protected IActivityManager activityManager;
     protected IAssistantManager assistantManager;
     protected ICourseManager courseManager;
     protected IEmploymentManager employmentManager;
     protected IFinancialCategoryManager financialCategoryManager;
     protected IHourlyWageManager hourlyWageManager;
     protected IContractManager contractManager;
     protected IHistoryManager historyManager;
     // each AdohiveController should use a singleton following this pattern:
     protected static AdoHiveController instance;
 
     protected static String connectionString;
     protected static String driver;
     protected static SqlDialects sqlDialect;
 
     protected HashMap<String, String> properties = new HashMap<String, String>();
 
     protected AdoHiveController() {
 
     }
 
     /**
      * @return the activityManager
      */
     public IActivityManager getActivityManager() {
         return activityManager;
     }
 
     /**
      * @return the assistantManager
      */
     public IAssistantManager getAssistantManager() {
         return assistantManager;
     }
 
     /**
      * @return the courseManager
      */
     public ICourseManager getCourseManager() {
         return courseManager;
     }
 
     /**
      * @return the employmentManager
      */
     public IEmploymentManager getEmploymentManager() {
         return employmentManager;
     }
 
     /**
      * @return the financialCategoryManager
      */
     public IFinancialCategoryManager getFinancialCategoryManager() {
         return financialCategoryManager;
     }
 
     /**
      * @return the hourlyWageManager
      */
     public IHourlyWageManager getHourlyWageManager() {
         return hourlyWageManager;
     }
 
     public IContractManager getContractManager() {
         return contractManager;
     }
 
     public IHistoryManager getHistoryManager() {
         return historyManager;
     }
 
     public static void setConnectionString(String con) {
         connectionString = con;
     }
 
     public static void setDriver(String driver) {
         AdoHiveController.driver = driver;
     }
 
     public static void setSqlDialect(SqlDialects dialect) {
         sqlDialect = dialect;
     }
 
     public static AdoHiveController getInstance() throws AdoHiveException {
        if (instance == null) {
            instance = new AnsiAdoHiveController(connectionString, driver);
        }
 
         return instance;
     }
 
     /**
      * calls clear for all managers in the right order to not violate any foreign keys
      * @throws AdoHiveException
      */
     public void clearAll() throws AdoHiveException {
         employmentManager.clear();
         activityManager.clear();
         contractManager.clear();
         courseManager.clear();
         financialCategoryManager.clear();
 
         assistantManager.clear();
         hourlyWageManager.clear();
     }
 }
