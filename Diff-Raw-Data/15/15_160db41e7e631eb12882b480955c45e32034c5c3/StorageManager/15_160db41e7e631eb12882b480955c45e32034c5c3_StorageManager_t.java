 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core.storage;
 
 import com.flexive.core.Database;
 import com.flexive.core.storage.mySQL.MySQLEnvironmentLoader;
 import com.flexive.core.storage.mySQL.MySQLHierarchicalStorage;
 import com.flexive.core.storage.mySQL.MySQLTreeStorage;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.configuration.DBVendor;
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.structure.TypeStorageMode;
 
 import java.sql.SQLException;
 
 /**
  * Singleton Facade for the various storage implementations
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class StorageManager {
 
 
     /**
      * Get concrete content storage implementation for the given type storage mode
      *
      * @param mode used storage mode
      * @return DataSelector for the given storage mode
      * @throws FxNotFoundException if no implementation was found
      */
     public static ContentStorage getContentStorage(TypeStorageMode mode) throws FxNotFoundException {
         DBVendor vendor;
         try {
             vendor = Database.getDivisionData().getDbVendor();
             switch (mode) {
                 case Hierarchical:
                     switch (vendor) {
                         case MySQL:
                             return MySQLHierarchicalStorage.getInstance();
                         default:
                             throw new FxNotFoundException("ex.db.contentStorage.undefined", vendor, mode);
                     }
                 default:
                     throw new FxNotFoundException("ex.structure.typeStorageMode.notImplemented", mode);
             }
         } catch (SQLException e) {
            throw new FxNotFoundException(e, "ex.db.vendor.notFound", FxContext.get().getDivisionId(), e);
         }
     }
 
     /**
      * Get concrete content storage implementation for the given type storage mode.
      * This variant is only to be used from a division free context like in a timer service or mbean
      *
      * @param data DivisionData
      * @param mode used storage mode
      * @return DataSelector for the given storage mode
      * @throws FxNotFoundException if no implementation was found
      */
     public static ContentStorage getContentStorage(DivisionData data, TypeStorageMode mode) throws FxNotFoundException {
         switch (mode) {
             case Hierarchical:
                 switch (data.getDbVendor()) {
                     case MySQL:
                         return MySQLHierarchicalStorage.getInstance();
                     default:
                         throw new FxNotFoundException("ex.db.contentStorage.undefined", data.getDbVendor(), mode);
                 }
             default:
                 throw new FxNotFoundException("ex.structure.typeStorageMode.notImplemented", mode);
         }
     }
 
     /**
      * Get concrete tree storage implementation for the used database
      *
      * @return TreeStorage
      * @throws FxNotFoundException if no implementation was found
      */
     public static TreeStorage getTreeStorage() throws FxNotFoundException {
         DBVendor vendor;
         try {
             vendor = Database.getDivisionData().getDbVendor();
             switch (vendor) {
                 case MySQL:
                     return MySQLTreeStorage.getInstance();
                 default:
                     throw new FxNotFoundException("ex.db.treeStorage.undefined", vendor);
             }
         } catch (SQLException e) {
            throw new FxNotFoundException(e, "ex.db.vendor.notFound", FxContext.get().getDivisionId(), e);
         }
     }
 
     /**
      * Get a concrete environment loader instance for a division
      *
      * @param dd DivisionData for the requested Division
      * @return EnvironmentLoader instance
      * @throws FxNotFoundException if no implementation was found
      */
     public static EnvironmentLoader getEnvironmentLoader(DivisionData dd) throws FxNotFoundException {
         DBVendor vendor = dd.getDbVendor();
         switch (vendor) {
             case MySQL:
                 return MySQLEnvironmentLoader.getInstance();
             default:
                 throw new FxNotFoundException("ex.db.environmentLoader.undefined", vendor);
         }
     }
 }
