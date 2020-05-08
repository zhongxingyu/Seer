 /**
  * Copyright (C) 2013 Alexander Szczuczko
  *
  * This file may be modified and distributed under the terms
  * of the MIT license. See the LICENSE file for details.
  */
 package ca.szc.keratin.core.net;
 
 import org.pmw.tinylog.Logger;
 
 import net.engio.mbassy.IPublicationErrorHandler;
 import net.engio.mbassy.PublicationError;
 
 /**
  * Logs PublicationErrors at error level
  */
 public class BusErrorHandler
     implements IPublicationErrorHandler
 {
 
     @Override
     public void handleError( PublicationError error )
     {
         Logger.error( error.toString() );
        logAllSubCauses( error.getCause() );

     }
 
    private void logAllSubCauses( Throwable cause )
    {
        Throwable subcause = cause.getCause();
        if ( subcause != null )
        {
            Logger.error( subcause, "Subcause of " + cause );
            logAllSubCauses( subcause );
        }
    }
 }
