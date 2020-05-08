 /*!
  * This program is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
  * or from the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
  */
 
 package org.pentaho.mantle.client.dialogs.scheduling;
 
 import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
 import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
 import org.pentaho.mantle.client.messages.Messages;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 
 /**
  * Entry point for display schedule dialog.
  * 
  * It registers $wnd.openReportSchedulingDialog for call dialog from native js.
  */
 public class NewScheduleDialogEntryPoint implements EntryPoint, IResourceBundleLoadCallback {
   @Override
   public void onModuleLoad() {
     ResourceBundle messages = new ResourceBundle();
     Messages.setResourceBundle( messages );
     messages
         .loadBundle( GWT.getModuleBaseURL() + "messages/", "mantleMessages", true, NewScheduleDialogEntryPoint.this ); //$NON-NLS-1$ //$NON-NLS-2$
   }
 
   @Override
   public void bundleLoaded( String bundleName ) {
     setupNativeHooks( this );
   }
 
   public void openScheduleDialog( String reportFile ) {
     IScheduleCallback callback = new IScheduleCallback() {
 
       @Override
       public void okPressed() {
       }
 
       @Override
       public void cancelPressed() {
       }
 
       @Override
       public void scheduleJob() {
       }
     };
     NewScheduleDialog dialog = new NewScheduleDialog( reportFile, callback, false );
    dialog.show();
   }
 
   public void openBackgroundDialog( String reportFile ) {
     new ScheduleOutputLocationDialogExecutor( reportFile ).performOperation();
   }
 
   public native void setupNativeHooks( NewScheduleDialogEntryPoint reportSchedulingEntryPoint )
   /*-{
     $wnd.openReportSchedulingDialog = function(reportFile) {
       reportSchedulingEntryPoint.@org.pentaho.mantle.client.dialogs.scheduling.NewScheduleDialogEntryPoint::openScheduleDialog(Ljava/lang/String;)(reportFile);
     }
     $wnd.openReportBackgroundDialog = function(reportFile) {
       reportSchedulingEntryPoint.@org.pentaho.mantle.client.dialogs.scheduling.NewScheduleDialogEntryPoint::openBackgroundDialog(Ljava/lang/String;)(reportFile);
     }
   }-*/;
 }
