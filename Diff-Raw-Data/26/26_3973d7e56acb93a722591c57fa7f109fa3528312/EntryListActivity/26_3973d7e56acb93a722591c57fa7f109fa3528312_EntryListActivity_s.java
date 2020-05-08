 package com.enonic.android.twitranet;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 
 import org.jdom.JDOMException;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ListView;
 
 import com.enonic.android.twitranet.login.LoginActivity;
 
 public class EntryListActivity
     extends ListActivity
 {
     private static final int ACTIVITY_LOGIN = 0;
 
     public final String CLASS = EntryListActivity.class.getSimpleName();
 
     public static final boolean DEBUG = true;
 
     private ArrayList<EntryDataHolder> m_options = null;
 
     private EntryArrayAdapter m_adapter;
 
 //    private Runnable entryListActivity;
 
     @Override
     protected void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState );
         setContentView( R.layout.twitranett_layout );
 
         m_options = new ArrayList<EntryDataHolder>();
         if ( DEBUG )
         {
             Log.d( CLASS, "calling CreateEntryAdapter()" );
         }
         this.m_adapter = new EntryArrayAdapter( this, R.layout.twitranett_list_item, m_options );
         ListView listView = getListView();
         listView.setAdapter( this.m_adapter );
         listView.setSmoothScrollbarEnabled( true );
 
         Intent i = new Intent( this, LoginActivity.class );
         startActivityForResult( i, ACTIVITY_LOGIN );
 
 //        if ( DEBUG )
 //        {
 //            Log.d( CLASS, "initiate new viewTest = new Runable" );
 //        }
 //        entryListActivity = new Runnable()
 //        {
 //            public void run()
 //            {
 //                while ( true )
 //                {
 //                    try
 //                    {
 //                        Thread.sleep( 180000 );
 //                    }
 //                    catch ( InterruptedException e )
 //                    {
 //                        // Do nothing
 //                    }
 //                    m_adapter.notifyDataSetChanged();
 //                    if (DEBUG) {
 //                        Log.d(CLASS, "Data changed notification sent.");
 //                    }
 //                }
 //            }
 //        };
 //        if ( DEBUG )
 //        {
 //            Log.d( CLASS, "create new thread with viewTestRunable" );
 //        }
 //        Thread thread = new Thread( null, entryListActivity, "UpdateData" );
 //
 //        if ( DEBUG )
 //        {
 //            Log.d( CLASS, "thread start()" );
 //        }
 //        thread.start();
 
     }
 
 //    private Runnable returnRes = new Runnable()
 //    {
 //        public void run()
 //        {
 //            if ( DEBUG )
 //            {
 //                Log.d( CLASS, "returnRes runable run() start" );
 //            }
 //            if ( m_options != null && m_options.size() > 0 )
 //            {
 //                if ( DEBUG )
 //                {
 //                    Log.d( CLASS, "m_tests got something" );
 //                }
 //                m_adapter.notifyDataSetChanged();
 //                if ( DEBUG )
 //                {
 //                    Log.d( CLASS, "m_adapter.notifydatasetchanged() since m_tests got something" );
 //                }
 //                for ( int i = 0; i < m_options.size(); i++ )
 //                {
 //                    m_adapter.add( m_options.get( i ) );
 //                }
 //            }
 //            m_adapter.notifyDataSetChanged();
 //            if ( DEBUG )
 //            {
 //                Log.d( CLASS, "m_adapter.notifydatasetchanged() after dismiss m_progressdialog" );
 //            }
 //        }
 //    };
 
 
     @Override
     protected void onActivityResult( int requestCode, int resultCode, Intent intent )
     {
         if (requestCode == ACTIVITY_LOGIN) {
 
             super.onActivityResult( requestCode, resultCode, intent );
             this.m_adapter.setUsername( intent.getExtras().getString( EntryArrayAdapter.KEY_USERNAME ) );
             this.m_adapter.setPassword( intent.getExtras().getString( EntryArrayAdapter.KEY_PASSWORD ) );
             try
             {
                 this.m_adapter.refreshDataFromServer();
                 this.m_adapter.notifyDataSetChanged();
             }
             catch ( ParseException e )
             {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             catch ( IOException e )
             {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             catch ( JDOMException e )
             {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
     }
 
 }
