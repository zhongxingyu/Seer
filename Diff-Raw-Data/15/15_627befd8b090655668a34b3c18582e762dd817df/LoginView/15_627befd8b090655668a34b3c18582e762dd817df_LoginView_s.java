 package be.uclouvain.sinf1225.gourmet;
 
 import be.uclouvain.sinf1225.gourmet.models.User;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetUtils;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class LoginView extends Activity
 {
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		//If we are here, the user is not yet connected.
 		SharedPreferences sp = getSharedPreferences("gourmetLogin",Context.MODE_PRIVATE);
 		String emailP = sp.getString("email", "");
 		String passwordHashP = sp.getString("passwordHash", "");
 		if(!emailP.equals("") && !passwordHashP.equals(""))
 			User.loginUser(emailP, passwordHashP, true); //return value is not important
 		
 		if(User.isUserConnected())
 		{
 			Intent intent = new Intent(this, CityListView.class);
 			startActivity(intent);
 			finish();
 		}
 		else
 		{
 			sp.edit().putString("email", "").putString("passwordHash", "").commit();
 			
 			setContentView(R.layout.activity_connexion);
 			
 			final EditText name = (EditText)this.findViewById(R.id.loginName);
 			final EditText surname = (EditText)this.findViewById(R.id.loginSurname);
 			final EditText email = (EditText)this.findViewById(R.id.loginEmail);
 			final EditText password = (EditText)this.findViewById(R.id.loginPassword);
 			final ToggleButton inscription = (ToggleButton)this.findViewById(R.id.loginInscription);
 			final Button confirm = (Button)this.findViewById(R.id.loginConfirm);
 			
 			inscription.setOnCheckedChangeListener(new OnCheckedChangeListener()
 			{
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 				{
 					if(isChecked)
 					{
 						name.setVisibility(EditText.VISIBLE);
 						surname.setVisibility(EditText.VISIBLE);
 						confirm.setText("S'inscrire");
 					}
 					else
 					{
 						name.setVisibility(EditText.GONE);
 						surname.setVisibility(EditText.GONE);
 						confirm.setText("Connexion");
 					}
 				}
 			});
 			
 			confirm.setOnClickListener(new OnClickListener()
 			{
 				@Override
 				public void onClick(View arg0)
 				{
 					User.UserManagerReturn ret;
 					if(inscription.isChecked())
						ret = User.registerUser(name.getText().toString(),surname.getText().toString(),email.getText().toString(),password.getText().toString());
 					else
						ret = User.loginUser(email.getText().toString(),password.getText().toString());
 					
 					if(ret == User.UserManagerReturn.LOGIN_OK)
 					{
 						//Update last login values
 						SharedPreferences sp = getSharedPreferences("gourmetLogin",Context.MODE_PRIVATE);
 						sp.edit().putString("email", email.getText().toString()).putString("passwordHash", GourmetUtils.sha1(password.getText().toString())).commit();
 						
 						//Start tab activity
 						Intent intent = new Intent(LoginView.this, CityListView.class);
 						startActivity(intent);
 						finish();
 					}
 					else if(ret == User.UserManagerReturn.LOGIN_ERR)
 					{
 						Toast toast = Toast.makeText(getApplicationContext(), "Email ou mot de passe invalide", Toast.LENGTH_LONG);
 						toast.show();
 					}
 					else if(ret == User.UserManagerReturn.REGISTER_MAIL_ALREADY_TAKEN)
 					{
 						Toast toast = Toast.makeText(getApplicationContext(), "Email dja utilise", Toast.LENGTH_LONG);
 						toast.show();
 					}
 					else if(ret == User.UserManagerReturn.NOT_FULL)
 					{
 						Toast toast = Toast.makeText(getApplicationContext(), "Veuillez remplir tout les champs", Toast.LENGTH_LONG);
 						toast.show();
 					}
 				}
 			});
 		}
 	}
 }
