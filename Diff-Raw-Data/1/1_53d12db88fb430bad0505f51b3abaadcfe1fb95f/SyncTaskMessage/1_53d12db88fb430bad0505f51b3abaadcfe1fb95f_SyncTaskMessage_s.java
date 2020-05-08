 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2011 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 
 package com.funambol.client.engine;
 
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.sync.SyncException;
 import com.funambol.util.bus.BusMessage;
 import java.util.Vector;
 
 public class SyncTaskMessage extends BusMessage {
 
     public static final int MESSAGE_BEGIN_SYNC     = 0;
     public static final int MESSAGE_SYNC_STARTED   = 1;
     public static final int MESSAGE_SOURCE_STARTED = 2;
     public static final int MESSAGE_SOURCE_FAILED  = 3;
     public static final int MESSAGE_SOURCE_ENDED   = 4;
     public static final int MESSAGE_SYNC_ENDED     = 5;
     public static final int MESSAGE_END_SYNC       = 6;
 
     public static final int MESSAGE_NO_CREDENTIALS = 7;
     public static final int MESSAGE_NO_SOURCES     = 8;
     public static final int MESSAGE_NO_CONNECTION  = 9;
     public static final int MESSAGE_NO_SIGNAL      = 10;
 
     private int messageCode;
 
     private AppSyncSource appSource;
     private Vector        appSources;
     private SyncException exception;
     private boolean       hadErrors;
 
     public SyncTaskMessage(int code) {
         super(null);
         this.messageCode = code;
     }
 
     public SyncTaskMessage(int code, AppSyncSource source) {
         this(code);
         this.appSource = source;
     }
 
     public SyncTaskMessage(int code, Vector sources) {
         this(code);
         this.appSources = sources;
     }
 
     public SyncTaskMessage(int code, AppSyncSource source, SyncException ex) {
         this(code);
         this.appSource = source;
     }
 
     public SyncTaskMessage(int code, Vector sources, boolean hadErrors) {
         this(code);
         this.appSources = sources;
         this.hadErrors = hadErrors;
     }
 
     public int getMessageCode() {
         return messageCode;
     }
 
     public AppSyncSource getAppSource() {
         return appSource;
     }
 
     public Vector getAppSources() {
         return appSources;
     }
 
     public SyncException getSyncException() {
         return exception;
     }
 
     public boolean getHadErrors() {
         return hadErrors;
     }
 }
