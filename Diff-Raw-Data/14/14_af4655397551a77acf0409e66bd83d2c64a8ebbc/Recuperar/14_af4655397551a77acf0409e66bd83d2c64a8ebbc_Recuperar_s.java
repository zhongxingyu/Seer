 package com.researchmobile.coretel.view;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.researchmobile.coretel.entity.RespuestaWS;
 import com.researchmobile.coretel.utility.ConnectState;
 import com.researchmobile.coretel.ws.RequestWS;
 
 public class Recuperar extends Activity implements OnClickListener, OnKeyListener{
 	
 	private Button recuperarButton;
 	private Button volverButton;
 	private EditText emailEditText;
 	private ConnectState connectState = new ConnectState();
 	private RespuestaWS respuesta;
 	private RequestWS request = new RequestWS();
 	private String email = "";
 	
	
 	private ProgressDialog pd = null;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.recuperar_contrasena);
         
         setRespuesta(new RespuestaWS());
         setRecuperarButton((Button)findViewById(R.id.recuperar_recuperar_button));
         setVolverButton((Button)findViewById(R.id.recuperar_volver_button));
         setEmailEditText((EditText)findViewById(R.id.recuperar_email_edittext));
         
         getRecuperarButton().setOnClickListener(this);
         getVolverButton().setOnClickListener(this);
         getEmailEditText().setOnKeyListener(this);
 	}
 	
 	@Override
 	public void onClick(View view) {
 		if (view == getRecuperarButton()){
 			enviarSolicitud();
 		}else if (view == getVolverButton()){
 			finish();
 		}
 		
 	}
 	
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)
         {
             return true;
         }
         if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
         {
             //TODO: When the enter key is pressed
             return true;
         }
 
 		return false;
 	}
 	
 	public void iniciaRecuperar(){
 		setRespuesta(request.recuperaClave(email));
 	}
 	
 	public void enviarSolicitud(){
 		email = getEmailEditText().getText().toString();
 		if(connectState.isConnectedToInternet(this)){
 			if (!email.equalsIgnoreCase("")){
 				new recuperarAsync().execute("");
 			}else{
 				Toast.makeText(getBaseContext(), "debe ingresar su email", Toast.LENGTH_SHORT).show();
 			}
 		}else{
 			Toast.makeText(getBaseContext(), "No cuenta con conexin a internet", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	// Clase para ejecutar en Background
     class recuperarAsync extends AsyncTask<String, Integer, Integer> {
 
           // Metodo que prepara lo que usara en background, Prepara el progress
           @Override
           protected void onPreExecute() {
                 pd = ProgressDialog. show(Recuperar.this, null, "ESPERE UN MOMENTO");
                 pd.setCancelable( false);
          }
 
           // Metodo con las instrucciones que se realizan en background
           @Override
           protected Integer doInBackground(String... urlString) {
                 try {
                 	iniciaRecuperar();
                } catch (Exception exception) {
 
                }
                 return null ;
          }
 
           // Metodo con las instrucciones al finalizar lo ejectuado en background
           protected void onPostExecute(Integer resultado) {
                 pd.dismiss();
                 Toast.makeText(getBaseContext(), getRespuesta().getMensaje(), Toast.LENGTH_SHORT).show();
                 if (getRespuesta().isResultado()){
                 	finish();
                 }
 
          }
     }
 
 
 	public Button getRecuperarButton() {
 		return recuperarButton;
 	}
 
 	public void setRecuperarButton(Button recuperarButton) {
 		this.recuperarButton = recuperarButton;
 	}
 
 	public Button getVolverButton() {
 		return volverButton;
 	}
 
 	public void setVolverButton(Button volverButton) {
 		this.volverButton = volverButton;
 	}
 
 	public EditText getEmailEditText() {
 		return emailEditText;
 	}
 
 	public void setEmailEditText(EditText emailEditText) {
 		this.emailEditText = emailEditText;
 	}
 
 	public RespuestaWS getRespuesta() {
 		return respuesta;
 	}
 
 	public void setRespuesta(RespuestaWS respuesta) {
 		this.respuesta = respuesta;
 	}
 	
 	
 }
