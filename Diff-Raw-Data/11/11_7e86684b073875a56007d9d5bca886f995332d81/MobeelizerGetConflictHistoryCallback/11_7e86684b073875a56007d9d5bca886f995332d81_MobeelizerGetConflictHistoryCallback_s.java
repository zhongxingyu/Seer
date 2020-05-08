 package com.mobeelizer.java;
 
 import java.util.Map;
 import com.mobeelizer.java.api.MobeelizerEntityVersion;
 import com.mobeelizer.java.api.MobeelizerOperationError;
 
 /**
  * Callback used to notify when the getting conflict history is finished.
  * 
  * @since 1.0
  */
 public interface MobeelizerGetConflictHistoryCallback  {
 	
 	/**
      * Method invoked when the synchronization is finished with success. 
      * 
     * @param entities
      *            entity versions
      * @since 1.7
      */
 	void onFinishedWithSuccess(final  Iterable<MobeelizerEntityVersion> versions); 
 
 	/**
      * Method invoked when getting conflict history finished with error.
      * 
      * @param error
      *            error
      * @since 1.7
      */
     void onFinishedWithError(final MobeelizerOperationError error);
 
 	
 }
