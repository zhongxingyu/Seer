 package org.mosaic.runner.watcher;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static java.lang.System.currentTimeMillis;
 import static java.nio.file.Files.*;
 
 /**
  * @author arik
  */
 public class WatchedResource implements WatchedResourceHandler {
 
     public static enum UpdateResult {
         NO_LONGER_VALID,
         ILLEGAL_STATE,
         UP_TO_DATE,
         UPDATED,
     }
 
    private static final long MIN_ERROR_COUNT_FOR_QUIET = 5;
 
     private static final long ERROR_QUIET_TIME = 1000 * 30;
 
     private static final long DEFAULT_QUIET_PERIOD = 1000 * 3;
 
     private final Logger logger = LoggerFactory.getLogger( getClass() );
 
     private final Path resource;
 
     private final WatchedResourceHandler resourceHandler;
 
     private int successiveErrorCount = 0;
 
    private long lastSuccessfulCheck;
 
     private long quietPeriod;
 
     public WatchedResource( Path resource, WatchedResourceHandler resourceHandler ) {
         this.quietPeriod = Long.getLong( "watchQuietPeriod", DEFAULT_QUIET_PERIOD );
         this.resource = resource.normalize().toAbsolutePath();
         this.resourceHandler = resourceHandler;
     }
 
     public Path getResource() {
         return resource;
     }
 
     @Override
     public void handleNoLongerExists( Path resource ) {
         try {
             if( !isSameFile( this.resource, resource ) ) {
                 throw new IllegalArgumentException( "Resource must be '" + this.resource + "'" );
             }
             this.resourceHandler.handleNoLongerExists( resource );
         } catch( Exception e ) {
             successiveErrorCount++;
             this.logger.error( "Error while processing resource '{}'", new Object[] { resource, e } );
         }
     }
 
     @Override
     public void handleIllegalFile( Path resource ) {
         try {
             if( !isSameFile( this.resource, resource ) ) {
                 throw new IllegalArgumentException( "Resource must be '" + this.resource + "'" );
             }
             this.resourceHandler.handleIllegalFile( resource );
         } catch( Exception e ) {
             successiveErrorCount++;
             this.logger.error( "Error while processing resource '{}'", resource, e );
         }
     }
 
     @Override
     public Long getLastUpdateTime( Path resource ) {
         try {
             if( !isSameFile( this.resource, resource ) ) {
                 throw new IllegalArgumentException( "Resource must be '" + this.resource + "'" );
             }
             return this.resourceHandler.getLastUpdateTime( resource );
         } catch( Exception e ) {
             successiveErrorCount++;
             this.logger.error( "Error while processing resource '{}'", resource, e );
             return Long.MAX_VALUE;
         }
     }
 
     @Override
     public void handleUpdated( Path resource ) {
         try {
             if( !isSameFile( this.resource, resource ) ) {
                 throw new IllegalArgumentException( "Resource must be '" + this.resource + "'" );
             }
             this.resourceHandler.handleUpdated( resource );
         } catch( Exception e ) {
             successiveErrorCount++;
             this.logger.error( "Error while processing resource '{}'", resource, e );
         }
     }
 
     @Override
     public void handleUpToDate( Path resource ) {
         try {
             if( !isSameFile( this.resource, resource ) ) {
                 throw new IllegalArgumentException( "Resource must be '" + this.resource + "'" );
             }
             this.resourceHandler.handleUpToDate( resource );
         } catch( Exception e ) {
             successiveErrorCount++;
             this.logger.error( "Error while processing resource '{}'", resource, e );
         }
     }
 
     public UpdateResult checkForUpdates() {
         long time = System.currentTimeMillis();
 
         UpdateResult result;
        if( this.successiveErrorCount < MIN_ERROR_COUNT_FOR_QUIET || time - this.lastSuccessfulCheck >= ERROR_QUIET_TIME ) {
 
             // save current error count so we can test if it changed after this current check
             int previousErrorCount = this.successiveErrorCount;
 
             // perform the actual check for updates
             result = checkForUpdatesInternal();
 
             // if error count did not change, reset it since it means we've had a fully successful check
             if( previousErrorCount == this.successiveErrorCount ) {
                 this.successiveErrorCount = 0;
                this.lastSuccessfulCheck = time;
             }
             return result;
 
         } else {
 
             return UpdateResult.UP_TO_DATE;
 
         }
     }
 
     private UpdateResult checkForUpdatesInternal() {
         if( notExists( this.resource ) ) {
 
             // file no longer exists - handle and indicate to caller that the resource should no longer be watched
             handleNoLongerExists( this.resource );
             return UpdateResult.NO_LONGER_VALID;
 
         } else if( isDirectory( this.resource ) || !isRegularFile( this.resource ) || !isReadable( this.resource ) ) {
 
             // resource is unwatchable: points to a directory, cannot be read or a special unknown file type;
             // let the handler handle the situation and indicate this to the caller - note that we should still be
             // watched since such a situation can be fixed (e.g. someone adds the missing file permissions to the resource)
             handleIllegalFile( this.resource );
             return UpdateResult.ILLEGAL_STATE;
 
         }
 
         // get last modification time for our resource; if error, log and return illegal state to caller
         long resourceModificationTime;
         try {
             resourceModificationTime = getLastModifiedTime( this.resource ).toMillis();
         } catch( IOException e ) {
             this.logger.warn( "Could not inspect last modification time for file '{}': {}",
                               new Object[] { this.resource, e.getMessage(), e } );
             return UpdateResult.ILLEGAL_STATE;
         }
 
         // compare last modification time of file against the last update time of our handler, and invoke it accordingly
         Long lastUpdateTime = getLastUpdateTime( this.resource );
         if( lastUpdateTime == null ) {
 
             // this is the first time we're processing the resource - "install" it and indicate to caller that
             // we have been updated
             handleUpdated( this.resource );
             return UpdateResult.UPDATED;
 
         } else if( isResourceUpToDate( resourceModificationTime, lastUpdateTime ) ) {
 
             // resource has not changed since the last time we've processed it - indicate to caller and return
             handleUpToDate( resource );
             return UpdateResult.UP_TO_DATE;
 
         } else {
 
             // resource has been modified - handle it and indicate that to the caller
             handleUpdated( this.resource );
             return UpdateResult.UPDATED;
 
         }
     }
 
     @SuppressWarnings( "RedundantIfStatement" )
     private boolean isResourceUpToDate( long resourceModificationTime, Long lastUpdateTime ) {
         if( resourceModificationTime <= lastUpdateTime ) {
 
             // resource has not changed since the last time we've processed it - indicate to caller and return
             return true;
 
         } else if( currentTimeMillis() - resourceModificationTime < this.quietPeriod ) {
 
             // resource was *JUST* modified - wait a little while to ensure that the process that updated it is finished
             // (e.g. if the file-copy operation that updated it might still be running and writing to the file...)
             return true;
 
         } else {
 
             // resource has changed AFTER the last-update time, and we're no longer in the quiet-period
             return false;
 
         }
     }
 }
