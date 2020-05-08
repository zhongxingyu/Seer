 package uk.me.grambo.syncro;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.*;
 import android.database.*;
 import android.database.sqlite.*;
 import android.content.*;
 import android.view.*;
 import android.app.Dialog;
 import android.util.Log;
 
 
 public class ServerBrowser extends Activity
 {	
 	private static final int DIALOG_ADDSERVER = 0;
 	
 	
 	private Dialog m_oAddServerDialog;
 	
 	private SQLiteDatabase m_oDB;
 	private SQLiteStatement m_oAddServerStatement;
 	
     String[] m_aServers;
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Button oFindServer = (Button)findViewById(R.id.findserver);
         oFindServer.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Perform action on click
             	Intent i = new Intent(ServerBrowser.this,FindServer.class);
             	ServerBrowser.this.startActivity(i);
             	//Toast.makeText(getApplicationContext(), "Hello!",
                 //        Toast.LENGTH_SHORT).show();
                   }
         });
         
         ListView oListView = (ListView)findViewById(R.id.serverlist);
         DBHelper oHelper = new DBHelper( this );
         SQLiteDatabase m_oDB = oHelper.getReadableDatabase();
         Cursor oResults = m_oDB.rawQuery("SELECT * From servers", null);
         int nResults = oResults.getCount();
         if( nResults > 0 ) {
         	m_aServers = new String[nResults];
         	oResults.moveToFirst();
         	int n = 0;
         	do {
         		m_aServers[n] = oResults.getString(0);
         		n++;
         	} while( oResults.moveToNext() );
         	oListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, m_aServers ));
         }
         oResults.close();
         
         Button oManualAdd = (Button)findViewById(R.id.addserverbutton);
         oManualAdd.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View inView) {
         		ServerBrowser.this.showDialog(DIALOG_ADDSERVER);
         	}
         });
         
         m_oAddServerStatement = m_oDB.compileStatement("INSERT INTO servers(Name,IP) VALUES(?,?)");
     }
     
     protected Dialog onCreateDialog(int innID) {
     	Log.d("Syncro","onCreateDialog entry");
     	Dialog oOutDialog = null;
     	switch( innID ) {
     		case DIALOG_ADDSERVER:
     			if( m_oAddServerDialog != null )
     				Log.e("Syncro", "Added Addserverdialog more than once!");
     			m_oAddServerDialog = new Dialog( this ); 
     			oOutDialog = m_oAddServerDialog;
     			Log.d("Syncro","onCreateDialog created dialog");
     			oOutDialog.setContentView(R.layout.dialog_addserver);
     			Log.d("Syncro","onCreateDialog assigned content");
     			oOutDialog.setTitle("Enter Server Address...");
     			
     			Button oButton = (Button)oOutDialog.findViewById(R.id.dialog_addserver_ok);
     			oButton.setOnClickListener( new View.OnClickListener() {
     				public void onClick(View v) {
     					ServerBrowser.this.AddServer();
     				};
     			});
     			
     			break;
     		default:
     			break;
     	}
     	Log.d("Syncro","onCreateDialog exit");
     	return oOutDialog;
     }
     
     public void AddServer() {
     	EditText oServerNameEditText = (EditText)m_oAddServerDialog.findViewById(R.id.dialog_addserver_servername);
     	EditText oPortEditText = (EditText)m_oAddServerDialog.findViewById(R.id.dialog_addserver_port);
     	
     	String sServerName = oServerNameEditText.getText().toString();
     	int nPort = Integer.parseInt(oPortEditText.getText().toString());
         
         m_oAddServerStatement.bindString(1,sServerName);
         m_oAddServerStatement.bindLong(2,nPort);
        int nResult = m_oAddServerStatement.executeInsert();
         if( nResult == -1 )
         	Log.e("Syncro", "execute failed");
         else
         	Log.d("Syncro", "execute success");
     }
 }
