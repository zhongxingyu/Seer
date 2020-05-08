 package android.ubication;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONObject;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 //import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * 
  * @author Flavio Corpa Ros
  *
  */
 public class LoginActivity extends Activity {
 	
 	/**
 	 * The default email to populate the email field with.
 	 */
 	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
 	/**
 	 * Keep track of the login task to ensure we can cancel it if requested.
 	 */
 	private UserLoginTask mAuthTask = null;
 
 	// Values for email and password at the time of the login attempt.
 	private String mEmail;
 	private String mPassword;
 
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_login);
 
 		// Set up the login form.
 		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
 		mEmailView = (EditText) findViewById(R.id.email);
 		mEmailView.setText(mEmail);
 
 		mPasswordView = (EditText) findViewById(R.id.password);
 		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView textView, int id,
 							KeyEvent keyEvent) {
 						if (id == R.id.login || id == EditorInfo.IME_NULL) {
 							attemptLogin();
 							return true;
 						}
 						return false;
 					}
 				});
 
 		mLoginFormView = findViewById(R.id.login_form);
 		mLoginStatusView = findViewById(R.id.login_status);
 		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 
 		findViewById(R.id.sign_in_button).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						attemptLogin();
 					}
 				});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		//getMenuInflater().inflate(R.menu.activity_login, menu);
 		return true;
 	}
 	
 	/*@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		if (item.getItemId() == R.id.menu_forgot_password)
 		{
 			//Formulario para recuperar la contrasea
 			return true;
 		}
 		else if (item.getItemId() == R.id.menu_registrarse)
 		{
 			//Forulario para REGISTRARSE
 			Intent i = new Intent(this, Registrarse.class);
 			startActivity(i);
 			return true;
 		}
 		else
 			return super.onMenuItemSelected(featureId, item);
 	}*/
 
 	/**
 	 * Attempts to sign in or register the account specified by the login form.
 	 * If there are form errors (invalid email, missing fields, etc.), the
 	 * errors are presented and no actual login attempt is made.
 	 */
 	public void attemptLogin() 
 	{
 		if (mAuthTask != null) {
 			return;
 		}
 
 		// Reset errors.
 		mEmailView.setError(null);
 		mPasswordView.setError(null);
 
 		// Store values at the time of the login attempt.
 		mEmail = mEmailView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 
 		boolean cancel = false;
 		View focusView = null;
 
 		// Check for a valid password.
 		if (TextUtils.isEmpty(mPassword)) {
 			mPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mPasswordView;
 			cancel = true;
 		} else if (mPassword.length() < 4) {
 			mPasswordView.setError(getString(R.string.error_invalid_password));
 			focusView = mPasswordView;
 			cancel = true;
 		}
 
 		// Check for a valid email address.
 		if (TextUtils.isEmpty(mEmail)) {
 			mEmailView.setError(getString(R.string.error_field_required));
 			focusView = mEmailView;
 			cancel = true;
 		} else if (!mEmail.contains("@")) {
 			mEmailView.setError(getString(R.string.error_invalid_email));
 			focusView = mEmailView;
 			cancel = true;
 		}
 
 		if (cancel) {
 			// There was an error; don't attempt login and focus the first
 			// form field with an error.
 			focusView.requestFocus();
 		} else {
 			// Show a progress spinner, and kick off a background task to
 			// perform the user login attempt.
 			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
 			showProgress(true);
 			mAuthTask = new UserLoginTask();
 			mAuthTask.execute((Void) null);
 		}
 	}
 	
 	/**
 	 * Shows the progress UI and hides the login form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mLoginStatusView.setVisibility(View.VISIBLE);
 			mLoginStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginStatusView.setVisibility(show ? View.VISIBLE
 									: View.GONE);
 						}
 					});
 
 			mLoginFormView.setVisibility(View.VISIBLE);
 			mLoginFormView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginFormView.setVisibility(show ? View.GONE
 									: View.VISIBLE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
 		}
 	}
 
 	/**
 	 * Represents an asynchronous login/registration task used to authenticate
 	 * the user.
 	 */
 	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
 		
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			
 			//URL del Servidor.
 	    	String url = "http://www.energysistem.com/ubication/index.php";
 
 			//Creamos un nuevo objeto HttpClient que ser el encargado de realizar la
 			//comunicacin HTTP con el servidor a partir de los datos que le damos.
 			HttpClient comunicacion = new DefaultHttpClient();
 			
 			//Creamos una peticion POST indicando la URL de llamada al servicio.
 			HttpPost peticion = new HttpPost(url);
 			
 			//Objeto JSON con los datos del Login.
 //		    	JSONObject object = new JSONObject();
 //		        try 
 //		        {
 //		            object.put("action", "login");
 //		            object.put("id", System.currentTimeMillis()); //Tiempo del Sistema en milisecs.
 //		            object.put("email", mEmail);
 //		            object.put("password", mPassword);
 //		            
 //		        } catch (Exception ex) {
 //		    		Log.e("Error", "Error al crear objeto JSON.", ex);
 //		        }
 			
 			try 
 			{
 				String idEnviado = String.valueOf(System.currentTimeMillis());
 				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 			    nameValuePairs.add(new BasicNameValuePair("action", "login"));
 			    nameValuePairs.add(new BasicNameValuePair("id", idEnviado));
 			    nameValuePairs.add(new BasicNameValuePair("email", mEmail));
 			    nameValuePairs.add(new BasicNameValuePair("password", mPassword));
 			    peticion.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 				//Modificamos mediante setHeader el atributo http content-type para indicar
 				//que el formato de los datos que utilizaremos en la comunicacin ser JSON.
 				peticion.setHeader("Accept", "application/json");
 				
 				//Ejecutamos la peticin y obtenemos la respuesta en forma de cadena
 				HttpResponse respuesta = comunicacion.execute(peticion);
 				String respuestaString = EntityUtils.toString(respuesta.getEntity());
 				
 				//Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
 				JSONObject respuestaJSON = new JSONObject(respuestaString);
 				
 				//Si la respuesta del servidor es true
				if (respuestaJSON.get("result").equals("true") && respuestaJSON.get("ack").equals(idEnviado))
 				{	//El Login es correcto
 					Log.e("LogDebug", "true");
 					return true;
 					//Arrancamos el Service
 					//startService(new Intent(this, UbicationService.class));
 				}
 				else
 				{
 					Log.e("LogDebug", "false");
 					return false;
 				}
 			} catch(Exception e) {
 				Log.e("Error", "Error al recibir respuesta del Servidor.", e);
 			}
 			return false;
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success) {
 				finish();
 			} else {
 				mEmailView.setError(getString(R.string.error_incorrect_password));
 				mPasswordView.setError(getString(R.string.error_incorrect_password));
 				mPasswordView.requestFocus();
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 }
