 // 
 // MobeelizerSyncCallback.java
 // 
 // Copyright (C) 2012 Mobeelizer Ltd. All Rights Reserved.
 //
 // Mobeelizer SDK is free software; you can redistribute it and/or modify it 
 // under the terms of the GNU Affero General Public License as published by 
 // the Free Software Foundation; either version 3 of the License, or (at your
 // option) any later version.
 //
 // This program is distributed in the hope that it will be useful, but WITHOUT
 // ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 // for more details.
 //
 // You should have received a copy of the GNU Affero General Public License 
 // along with this program; if not, write to the Free Software Foundation, Inc., 
 // 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 // 
 
 package com.mobeelizer.java;
 
 import com.mobeelizer.java.api.MobeelizerErrors;
 import com.mobeelizer.java.api.MobeelizerFile;
 import com.mobeelizer.java.api.MobeelizerOperationError;
 
 /**
  * Callback used to notify when the synchronization is finished.
  * 
  * @since 1.0
  */
 public interface MobeelizerSyncCallback {
 
     /**
      * Method invoked when the synchronization is finished with success. Use confirmCallback to confirm synchronization.
      * 
      * @param entities
      *            new entities
      * @param files
      *            new files
      * @param deletedFiles
      *            guids of the deleted files
      * @param confirmCallback
      *            callback to confirm synchronization
      * @since 1.0
      */
     void onSyncFinishedWithSuccess(final Iterable<Object> entities, final Iterable<MobeelizerFile> files,
             final Iterable<String> deletedFiles, final MobeelizerConfirmSyncCallback confirmCallback);
 
     /**
      * Method invoked when the synchronization is finished with error.
      * 
      * @param error
      *            error
      * @since 1.0
      */
     void onSyncFinishedWithError(final MobeelizerOperationError error);
 
     /**
      * Method invoked when the synchronization is finished with validation errors.
      * 
     * @param databaseError
     *            validation errors
      * @since 1.0
      */
     void onSyncFinishedWithError(final MobeelizerErrors databaseError);
 
 }
