 package it.xtremesoftware.tracking;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import it.xtremesoftware.tracking.R;
 import it.xtremesoftware.tracking.TrackActivity;
 import it.xtremesoftware.tracking.Entities.Gara;
 import it.xtremesoftware.tracking.Util.MyApplication;
 
 public class LoginActivity extends Activity {
 	private static final String LOG_TITLE= "LoginActivity";
	//PIPPO PAPPA PEPPE 
 	private static Handler mHandler;
 	
 	TextView txtLoginError;
 	EditText txtCodice;
 	Button btnAccedi;
 	
 	SharedPreferences settings;
 
 	
 	//*****************************************************
 	//HANDLER CASE
 	//*****************************************************
 	public static final int FINISH_LOGIN = 0 ;
 	public static final int START_LOGIN = 1 ;
 	public static final int ABORT_LOGIN = 2 ;
     //*****************************************************
   	//FINE HANDLER CASE
   	//*****************************************************
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
         
         //chiamo funzione che applica Font Roboto alla activity.
         changeFonts((ViewGroup) findViewById(R.id.rootViewLogin));
          
         setMyHandler();
         
         //riferimento ai setting delle shared Preferences
         settings = getSharedPreferences("GaraInfo", 0);
         
         //fornisco al Session Manager il mio Handler
         MyApplication.getSessionManager().setLoginHandler(mHandler);
         
         txtLoginError= (TextView) findViewById(R.id.txtLoginError);
         txtCodice= (EditText) findViewById(R.id.Codice);
 
         btnAccedi=(Button) findViewById(R.id.Login);
         btnAccedi.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	if (txtCodice.getText().toString().equals(""))
             	{
             		txtLoginError.setVisibility(View.VISIBLE);
             		txtLoginError.setText(R.string.errorCodeNonInserito);
             	}
             	else
             	{
             		if (MyApplication.VerificaConnessione())
             		{
             			MyApplication.getSessionManager().Login(txtCodice.getText().toString());
             		}
             		else
             		{
             			txtLoginError.setVisibility(View.VISIBLE);
                 		txtLoginError.setText(R.string.MsgAvvisoConnessione);
             		}
             	}
             }
         });
         
     }
     
     //Setto il mio Handler e ne definisco le azioni
     private void setMyHandler()
 	{
         mHandler = new Handler() {
             @Override
             public void handleMessage(Message msg) 
             {
             	//Log.d(LOG_TITLE," HandleMessage Ricevuto: " + msg.what);
                 switch (msg.what) 
                 {
 	                case FINISH_LOGIN:
 	                	//Log.d(LOG_TITLE,"HANDLE: FINISH LOGIN");
 	                	if (MyApplication.getSessionManager().getGara().getEsitoLogin())
 	                	{
 	                		txtLoginError.setVisibility(View.INVISIBLE);
 	                		
 	                		//salvo il Codice applicazione nei Setting
 	                		SharedPreferences.Editor editor = settings.edit();
 	                		editor.putString("CodiceGara",MyApplication.getSessionManager().getGara().getCodiceAttivazione());
 	                		editor.commit();
 	                		
 	                		GoToMapActivity();
 	                	}
 	                	else
 	                	{
 	                		txtLoginError.setVisibility(View.VISIBLE);
 	                		if (MyApplication.getSessionManager().getGara().getStatus().equals(Gara.STATE_TOSTART))
 	                		{
 	                			txtLoginError.setText(getResources().getString(R.string.loginGaraToStart));
 	                			txtLoginError.setText(String.format(getResources().getString(R.string.loginGaraToStart), MyApplication.getSessionManager().getGara().getInizio().toLocaleString()));
 	                		}
 	                		else if (MyApplication.getSessionManager().getGara().getStatus().equals(Gara.STATE_ENDED))
 	                		{
 	                			txtLoginError.setText(getResources().getString(R.string.loginGaraEnded));
 	                			txtLoginError.setText(String.format(getResources().getString(R.string.loginGaraEnded), MyApplication.getSessionManager().getGara().getInizio().toLocaleString()));
 	                		}
 	                		else if (MyApplication.getSessionManager().getGara().getStatus().equals(Gara.STATE_LOGINFAILED))
 	                		{
 	                			txtLoginError.setText(getResources().getString(R.string.loginFailed));
 	                		}
 	                	}
 	                    break;
 	                case START_LOGIN:
 	                	//Log.d(LOG_TITLE,"HANDLE: START LOGIN");
 	                    break;
 	                case ABORT_LOGIN:
 	                	//Log.d(LOG_TITLE,"HANDLE: ABORT LOGIN");
 	                	txtLoginError.setVisibility(View.VISIBLE);
                 		txtLoginError.setText(getResources().getString(R.string.ErroreNoXml));
                         break;
 	               
                 }
             }
 	    };
 	}
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	//NON HO MENU
         return true;
     }
     
     @Override
 	 protected void onResume() {
 	    super.onResume();
 	    
 	    //Leggo dalle shared preferences l'ultimo codice utilizzato e lo ripristino
 	    txtCodice.setText(settings.getString("CodiceGara", "").toString());
     }
     
     
     
     private void GoToMapActivity()
   	{
     	Intent intent=new Intent(MyApplication.getAppContext(),TrackActivity.class);
 	    startActivity(intent);
   	}
     
     protected void changeFonts(ViewGroup root) {
         
         Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
         
         for(int i = 0; i <root.getChildCount(); i++) {
                 View v = root.getChildAt(i);
                 if(v instanceof TextView ) {
                         ((TextView)v).setTypeface(tf);
                 } else if(v instanceof Button) {
                         ((Button)v).setTypeface(tf);
                 } else if(v instanceof EditText) {
                         ((EditText)v).setTypeface(tf);
                 } else if(v instanceof ViewGroup) {
                         changeFonts((ViewGroup)v);
                 }
         }
         
     }
     
 }
