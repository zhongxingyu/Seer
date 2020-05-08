 package com.researchmobile.coretel.view;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.researchmobile.coretel.entity.RespuestaWS;
 import com.researchmobile.coretel.entity.User;
 import com.researchmobile.coretel.entity.Usuario;
 import com.researchmobile.coretel.utility.Mensaje;
 import com.researchmobile.coretel.utility.NotifyManager;
 import com.researchmobile.coretel.ws.RequestWS;
 
 public class Perfil extends Activity implements OnClickListener, OnKeyListener{
 	
 	private LinearLayout edicionLayout;
 	private TextView nombreTextView;
 	private TextView emailText;
 	private TextView usuarioTextView;
 	private TextView telefonoTextView;
 	private EditText clavenuevaEditText;
 	private EditText clavenueva1EditText;
 	private EditText clavenueva2EditText;
 	private Button cambiaButton;
 	private Button guardarButton;
 	private Button editarButton;
 	private ToggleButton compartirUbicacion;
 	private Usuario usuario;
 	private User user;
 	private Mensaje mensaje;
 	private ProgressDialog pd = null;
 	private RespuestaWS respuestaWS;
 	private RespuestaWS respuesta2Geo;
 	private boolean cambioHecho;
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.perfil);
 		
 		setRespuestaWS(new RespuestaWS());
 		
 		Bundle bundle = (Bundle)getIntent().getExtras();
 		setUsuario((Usuario)bundle.get("usuario"));
 		setMensaje(new Mensaje());
 		setNombreTextView((TextView)findViewById(R.id.perfil_nombre_textview));
 		setEmailText((TextView)findViewById(R.id.perfil_email_textview));
 		setUsuarioTextView((TextView)findViewById(R.id.perfil_usuario_textview));
 		setTelefonoTextView((TextView)findViewById(R.id.perfil_telefono_textview));
 		setClavenuevaEditText((EditText)findViewById(R.id.perfil_claveactual_edittext));
 		setClavenueva1EditText((EditText)findViewById(R.id.perfil_clavenueva1_edittext));
 		setClavenueva2EditText((EditText)findViewById(R.id.perfil_clavenueva2_edittext));
 		
 		getClavenuevaEditText().setOnKeyListener(this);
 		getClavenueva1EditText().setOnKeyListener(this);
 		getClavenueva2EditText().setOnKeyListener(this);
 		setCambiaButton((Button)findViewById(R.id.perfil_cambiar_button));
 		setGuardarButton((Button)findViewById(R.id.perfil_guardar_button));
 		setEditarButton((Button)findViewById(R.id.perfil_editar_button));
 		setCompartirUbicacion((ToggleButton)findViewById(R.id.compartir_ubicacion_toggleButton));
 		if(User.isCompartirGeoposicion()){
 		 Log.v("pio", "se est compartiendo la ubicacin");
 		    getCompartirUbicacion().setChecked(true);
 		}else{
 			getCompartirUbicacion().setChecked(false);
 		}
 		getNombreTextView().setText(getUsuario().getNombre());
 		getEmailText().setText(getUsuario().getEmail());
 		getUsuarioTextView().setText(User.getUsername());
 		getTelefonoTextView().setText(getUsuario().getTelefono());
 		getCambiaButton().setOnClickListener(this);
 		getGuardarButton().setOnClickListener(this);
 		getEditarButton().setOnClickListener(this);
 		getCompartirUbicacion().setOnClickListener(this);
 		setEdicionLayout((LinearLayout)findViewById(R.id.perfil_layout_edit));
 		getEdicionLayout().setVisibility(View.INVISIBLE);
 	}
 	
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		 if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)
          {
              if (v == getClavenuevaEditText()){
             	 getClavenueva1EditText().requestFocus();
              }else if (v == getClavenueva1EditText()){
             	 getClavenueva2EditText().requestFocus();
              }else if (v == getClavenueva2EditText()){
             	 ConfirmarCambio(Perfil.this);
              }
              return true;
          }
          if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
          {
              //TODO: When the enter key is pressed
              return true;
          }
          return false;
 
 	}
 
 	private void ConfirmarCambio(Context ctx) {
 		new AlertDialog.Builder(ctx)
         .setIcon(ctx.getResources().getDrawable(R.drawable.alert))
         .setTitle("CAMBIO DE CLAVE")
         .setMessage("Esta de seguro de realizar el cambio?")
         .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 	new GuardarAsync().execute("");
                 }
         })
         .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 	getMensaje().VerMensaje(Perfil.this, "Cambio Cancelado");
                 }
         })
         .show();
 
 		
 	}
 
 	protected void LimpiarCampos() {
 		getClavenuevaEditText().setText("");
 		getClavenueva1EditText().setText("");
 		getClavenueva2EditText().setText("");
 	}
 
 	@Override
 	public void onClick(View view) {
 		if (view == getGuardarButton()){
 			ConfirmarCambio(Perfil.this);
 		}else if (view == getCambiaButton()){
 			if(getEdicionLayout().getVisibility() == View.INVISIBLE){
 				getEdicionLayout().setVisibility(View.VISIBLE);
 				getCambiaButton().setText("Cancelar");
 			}else{
 				getEdicionLayout().setVisibility(View.INVISIBLE);
 				getCambiaButton().setText("Cambiar Clave");
 				getClavenuevaEditText().setText("");
 				getClavenueva1EditText().setText("");
 				getClavenueva2EditText().setText("");
 			}
 		}else if (view == getEditarButton()){
 			editarActivity();
 		}else if (view == getCompartirUbicacion()){
 			accionesUbicacion();
 		}
 	}
 	
 	private void editarActivity(){
 		Intent intent = new Intent(Perfil.this, EditarPerfil.class);
 		intent.putExtra("usuario", getUsuario());
 		startActivity(intent);
 	}
 	
 	// Clase para ejecutar en Background
 	class GuardarAsync extends AsyncTask<String, Integer, Integer> {
 
 		// Metodo que prepara lo que usara en background, Prepara el progress
 		@Override
 		protected void onPreExecute() {
 			pd = ProgressDialog.show(Perfil.this, "ENVIANDO DATOS",
 					"ESPERE UN MOMENTO");
 			pd.setCancelable(false);
 		}
 
 		// Metodo con las instrucciones que se realizan en background
 		@Override
 		protected Integer doInBackground(String... urlString) {
 			try {
 				CambiarClave();
 
 			} catch (Exception exception) {
 
 			}
 			return null;
 		}
 
 		// Metodo con las instrucciones al finalizar lo ejectuado en background
 		protected void onPostExecute(Integer resultado) {
 			pd.dismiss();
 			if (getRespuestaWS() != null){
 				getMensaje().VerMensaje(Perfil.this, getRespuestaWS().getMensaje());
 				if (getRespuestaWS().isResultado()){
 					finish();
 				}
 			}
 			
 		}
 	}
 	
 	public void accionesUbicacion(){
 		if(getCompartirUbicacion().isChecked() && !User.isCompartirGeoposicion()){
 			User.setCompartirGeoposicion(true);
 	    	
 	    	 HandlerThread hilo;
 	    	 hilo=new HandlerThread("hilo_geoposicion");
 	         hilo.start();
 	         new Handler(hilo.getLooper()).post(
 	             new Runnable() {
 	                 @Override
 	                 public void run()
 	                 {
 	                     while(User.isCompartirGeoposicion())
 	                     {
 	                       
 	                      try
 	                      {
 	                         //Aqui lo que quieres hacer
 	                    	  Log.v("pio", "Se ejecut el hilo");
 	                    	  RequestWS request2 = new RequestWS();
 	                          setRespuesta2Geo(request2.EnviarGeoposicion(User.getUserId(), User.getLatitudActual(), User.getLongitudActual()));
 	                          Log.v("geo", "se envio la siguiente geoposicion " + User.getLatitudActual() + " " + User.getLongitudActual());
 	                    	                      	  
 	                    	 /*INICIA LA NOTIFICACIN
 	                    	  NotifyManager notify = new NotifyManager();
 	          			    notify.playNotification(getApplicationContext(),
 	          			      Mapa.class, "Se envio tu geoposicion"
 	          			      , "Notificacin", R.drawable.arrow_right); 
 	          			    TERMINA LA NOTIFICACIN*/
 	          			    
 	                         Thread.sleep(1000 * 60);
 	                      }catch (Exception e) {
 	                         // TODO Auto-generated catch block
 	                         e.printStackTrace();
 	                         try {
 	                             //Por si ocurre algun problema para que no se ejecute sin parar y se sobrecarga
 	                             Thread.sleep(1000 *60);
 	                         } catch (InterruptedException e1) {
 	                             // TODO Auto-generated catch block
 	                             e1.printStackTrace();
 	                         }
 	                     }
 	                      
 	                     }
 	                 }
 	             });
 	        
 		}
 		if(!getCompartirUbicacion().isChecked())
 		{
 			User.setCompartirGeoposicion(false);
 			System.out.println("Ya no se compartira la ubicacin");
 		}
 	}
 
 	private boolean CambiarClave() {
 		try{
			String clavereal = User.getPassword();
 			String claveactual = getClavenuevaEditText().getText().toString();
 			String clavenueva1 = getClavenueva1EditText().getText().toString();
 			String clavenueva2 = getClavenueva2EditText().getText().toString();
 			if (CamposLlenos(claveactual, clavenueva1, clavenueva2)){
 				if (clavereal.equalsIgnoreCase(claveactual)){
 					if (clavenueva1.equalsIgnoreCase(clavenueva2)){
 						RequestWS request = new RequestWS();
 						setRespuestaWS(request.CambiarClave(claveactual, clavenueva1));
 						return true;
 					}else{
 						getRespuestaWS().setResultado(false);
						getRespuestaWS().setMensaje("Las claves nuevas no coinsiden");
 						return false;
 					}
 				}else{
 					getRespuestaWS().setResultado(false);
					getRespuestaWS().setMensaje("La clave actual no es correcta");
 					return false;
 				}
 			}else{
 				getRespuestaWS().setMensaje("Debe llenar todos los campos");
 				getRespuestaWS().setResultado(false);
 				return false;
 			}
 		}catch(Exception exception){
 			return false;
 		}
 		
 	}
 
 	private boolean CamposLlenos(String claveactual, String clavenueva1, String clavenueva2) {
 		if (!claveactual.equalsIgnoreCase("") && !clavenueva1.equalsIgnoreCase("") && !clavenueva2.equalsIgnoreCase("")){
 			Log.e("pio", "no estan vacios");
 			return true;
 		}
 		return false;
 	}
 
 //	Metodo que mantiene los valores al girar la pantalla
 	@Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
     }
 
 
 	public LinearLayout getEdicionLayout() {
 		return edicionLayout;
 	}
 
 	public void setEdicionLayout(LinearLayout edicionLayout) {
 		this.edicionLayout = edicionLayout;
 	}
 
 	public TextView getNombreTextView() {
 		return nombreTextView;
 	}
 
 	public void setNombreTextView(TextView nombreTextView) {
 		this.nombreTextView = nombreTextView;
 	}
 
 	public TextView getEmailText() {
 		return emailText;
 	}
 
 	public void setEmailText(TextView emailText) {
 		this.emailText = emailText;
 	}
 
 	public EditText getClavenuevaEditText() {
 		return clavenuevaEditText;
 	}
 
 	public void setClavenuevaEditText(EditText clavenuevaEditText) {
 		this.clavenuevaEditText = clavenuevaEditText;
 	}
 
 	public EditText getClavenueva1EditText() {
 		return clavenueva1EditText;
 	}
 
 	public void setClavenueva1EditText(EditText clavenueva1EditText) {
 		this.clavenueva1EditText = clavenueva1EditText;
 	}
 
 	public EditText getClavenueva2EditText() {
 		return clavenueva2EditText;
 	}
 
 	public void setClavenueva2EditText(EditText clavenueva2EditText) {
 		this.clavenueva2EditText = clavenueva2EditText;
 	}
 
 	public Button getCambiaButton() {
 		return cambiaButton;
 	}
 
 	public void setCambiaButton(Button cambiaButton) {
 		this.cambiaButton = cambiaButton;
 	}
 
 	public Button getGuardarButton() {
 		return guardarButton;
 	}
 
 	public void setGuardarButton(Button guardarButton) {
 		this.guardarButton = guardarButton;
 	}
 
 	public Usuario getUsuario() {
 		return usuario;
 	}
 
 	public void setUsuario(Usuario usuario) {
 		this.usuario = usuario;
 	}
 
 	public Mensaje getMensaje() {
 		return mensaje;
 	}
 
 	public void setMensaje(Mensaje mensaje) {
 		this.mensaje = mensaje;
 	}
 
 	public boolean isCambioHecho() {
 		return cambioHecho;
 	}
 
 	public void setCambioHecho(boolean cambioHecho) {
 		this.cambioHecho = cambioHecho;
 	}
 
 	public RespuestaWS getRespuestaWS() {
 		return respuestaWS;
 	}
 
 	public void setRespuestaWS(RespuestaWS respuestaWS) {
 		this.respuestaWS = respuestaWS;
 	}
 
 	public TextView getUsuarioTextView() {
 		return usuarioTextView;
 	}
 
 	public void setUsuarioTextView(TextView usuarioTextView) {
 		this.usuarioTextView = usuarioTextView;
 	}
 
 	public TextView getTelefonoTextView() {
 		return telefonoTextView;
 	}
 
 	public void setTelefonoTextView(TextView telefonoTextView) {
 		this.telefonoTextView = telefonoTextView;
 	}
 
 	public Button getEditarButton() {
 		return editarButton;
 	}
 
 	public void setEditarButton(Button editarButton) {
 		this.editarButton = editarButton;
 	}
 
 	public ToggleButton getCompartirUbicacion() {
 		return compartirUbicacion;
 	}
 
 	public void setCompartirUbicacion(ToggleButton compartirUbicacion) {
 		this.compartirUbicacion = compartirUbicacion;
 	}
 
 	public RespuestaWS getRespuesta2Geo() {
 		return respuesta2Geo;
 	}
 
 	public void setRespuesta2Geo(RespuestaWS respuesta2Geo) {
 		this.respuesta2Geo = respuesta2Geo;
 	}
 	
 	
 }
