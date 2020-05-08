 /*
  * Copyright ©2011 Brice Arnould
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.vleu.par.android.sync;
 
 import java.lang.ref.SoftReference;
 
 import net.jcip.annotations.GuardedBy;
 import net.jcip.annotations.ThreadSafe;
 import net.vleu.par.android.Config;
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 
 /**
  * Android Service that handles sync. It simply instantiates a SyncAdapter and
  * returns its IBinder.
  */
 @ThreadSafe
 public class SyncService extends Service {
     /**
      * A cached {@link SyncAdapter}. It is important that the same
      * {@link SyncAdapter} is reused if one is still alive, so that it can
      * reject multiple synchronization requests happening at once.
      */
     @GuardedBy(value = "SyncService.class")
     private static SoftReference<SyncAdapter> cachedSyncAdapter = null;
     private static final String TAG = Config.makeLogTag(SyncService.class);
 
     private SyncAdapter getOrInstantiateSyncAdapter() {
        SyncAdapter result;
         synchronized (SyncService.class) {
            if (cachedSyncAdapter == null) {
                 if (Log.isLoggable(TAG, Log.DEBUG))
                     Log.d(TAG, "Instanciating a new SyncAdapter");
                 result = new SyncAdapter(getApplicationContext());
                 cachedSyncAdapter = new SoftReference<SyncAdapter>(result);
                return result;
             }
            else
                return cachedSyncAdapter.get();
         }
     }
 
     @Override
     public IBinder onBind(final Intent intent) {
         return getOrInstantiateSyncAdapter().getSyncAdapterBinder();
     }
 }
