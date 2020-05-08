 /**
  * Widget Related packages:
  * - AppWidgetProvider
  * - WidgetUpdater
  * - WidgetConfig
  * - WidgetUpdateService
  */
 package ru.elifantiev.fga.widget;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 /**
  * App Widget Provider. Calls Widget Updater
  * 
  * @author zoxa
  */
 public class FuckingWidget extends AppWidgetProvider
 {
     // onEnable have no idea what to do
 
     // Update current widget using db data
     @Override
     public void onUpdate( final Context context, final AppWidgetManager appWidgetManager,
             final int[] appWidgetIds )
     {
         final int N = appWidgetIds.length;
         for ( int i = 0; i < N; i++ )
         {
             WidgetUpdater wUpdater = new WidgetUpdater( context, appWidgetManager, appWidgetIds[i] );
             new Thread( wUpdater ).start();
         }
 
         super.onUpdate( context, appWidgetManager, appWidgetIds );
     }
 
     /**
      * Deleting one or more widgets (not all)
      */
     @Override
     public void onDeleted( Context context, int[] appWidgetIds )
     {
         SQLiteDatabase db = new DBHelper( context ).getWritableDatabase();
 
         final int N = appWidgetIds.length;
         for ( int i = 0; i < N; i++ )
         {
             db.delete( DBHelper.WIDGET_TABLE, DBHelper.WIDGET_ID + " = " + appWidgetIds[i], null );
 
             // remove alert manager for this widget
             PendingIntent pendingWidgetUpdate = WidgetConfig.getPendingItent( context,
                     appWidgetIds[i] );
 
             // set Alarm Manager to call pending Intent
             AlarmManager alarmManager = (AlarmManager) context
                     .getSystemService( Context.ALARM_SERVICE );
             alarmManager.cancel( pendingWidgetUpdate );
         }
         db.close();
 
         super.onDeleted( context, appWidgetIds );
     }
 
     /**
      * All widgets are gone
      */
     @Override
     public void onDisabled( Context context )
     {
         SQLiteDatabase db = new DBHelper( context ).getWritableDatabase();
 
         // get all widget ids
         Cursor cur = db.query( DBHelper.WIDGET_TABLE, new String[] { DBHelper.WIDGET_ID }, null,
                 null, null, null, null );
 
         cur.moveToFirst();
         if ( cur.getCount() > 0 )
         {
             do
             {
                 int widgetId = cur.getInt( cur.getColumnIndex( DBHelper.WIDGET_ID ) );
 
                 PendingIntent pendingWidgetUpdate = WidgetConfig
                         .getPendingItent( context, widgetId );
 
                // stopping AlarmManager
                 AlarmManager alarmManager = (AlarmManager) context
                         .getSystemService( Context.ALARM_SERVICE );
                 alarmManager.cancel( pendingWidgetUpdate );
             }
             while ( cur.moveToNext() );
         }
         cur.close();
 
         // emptying table
         db.delete( DBHelper.WIDGET_TABLE, null, null );
         db.close();
 
         super.onDisabled( context );
     }

    /**
     * Add onReceive method to call onDelete method. This fixes bug in Android 1.5
     */
    public void onReceive( Context context, Intent intent )
    {
        final String action = intent.getAction();
        if ( AppWidgetManager.ACTION_APPWIDGET_DELETED.equals( action ) )
        {
            final int appWidgetId = intent.getExtras().getInt( AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID );
            if ( appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID )
            {
                this.onDeleted( context, new int[] { appWidgetId } );
            }
        }
        else
        {
            super.onReceive( context, intent );
        }
    }

 }
