 /**
  * BazaarLogin, part of Aptoide
  * Copyright (C) 2011 Duarte Silveira
  * duarte.silveira@caixamagica.pt
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 package pt.caixamagica.aptoide.appsbackup;
 
 import pt.caixamagica.aptoide.appsbackup.data.AIDLAptoideServiceData;
 import pt.caixamagica.aptoide.appsbackup.data.AptoideServiceData;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewListIds;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerLoginCreateStatus;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerLoginStatus;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewServerLogin;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 /**
  * BazaarLogin, handles Aptoide's Bazaar login interface (by activity)
  * 
  * @author dsilveira
  *
  */
 public class BazaarLogin extends Activity {
 	
 	private AIDLAptoideServiceData serviceDataCaller = null;
 
 	private boolean serviceDataIsBound = false;
 
 	private ServiceConnection serviceDataConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the object we can use to
 			// interact with the service.  We are communicating with the
 			// service using AIDL, so here we set the remote service interface.
 			serviceDataCaller = AIDLAptoideServiceData.Stub.asInterface(service);
 			serviceDataIsBound = true;
 			
 			Log.v("Aptoide-Login", "Connected to ServiceData");
 			
 			try {
 				serviceDataCaller.callRegisterLoginObserver(serviceDataCallback);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 	        			
 			handleLogin();
 			
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			// This is called when the connection with the service has been
 			// unexpectedly disconnected -- that is, its process crashed.
 			serviceDataIsBound = false;
 			serviceDataCaller = null;
 			
 			Log.v("Aptoide-Login", "Disconnected from ServiceData");
 		}
 	};	
 	
 	private AIDLLogin.Stub serviceDataCallback = new AIDLLogin.Stub() {
 
 		@Override
 		public void repoInserted() throws RemoteException {
 			interfaceTasksHandler.sendEmptyMessage(EnumLoginInterfaceTasks.REPO_INSERTED.ordinal());	
 		}
 	};
 	
 	private Handler interfaceTasksHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
         	EnumLoginInterfaceTasks task = EnumLoginInterfaceTasks.reverseOrdinal(msg.what);
         	switch (task) {
 				case REPO_INSERTED:
 					dialogProgress.dismiss();
 					break;
 					
 				default:
 					break;
 				}
         }
 	};
 	
 	private ProgressDialog dialogProgress;
 	private boolean success;	
 	private LoginState loginState;
 	
 	private ViewServerLogin serverLogin;
 	
 	private boolean afterAction;
 	private ViewListIds actionListIds;
 	private EnumAppsLists actionType;
 	
 	private InvoqueType invoqueType;
 	
 	public static enum LoginState{
 		WAITING,
 		SUCCESS,
 		BAD_LOGIN,
 		REPO_NOT_FROM_USER
 	}
 	
 	public static enum InvoqueType{ 
 		CREDENTIALS_FAILED,
 		NO_CREDENTIALS_SET,
 		OVERRIDE_CREDENTIALS;
 		
 		public static InvoqueType reverseOrdinal(int ordinal){
 			return values()[ordinal];
 		}
 	}
 	
 	private EditText username;
 	private EditText password;
 //	private CheckBox showPass;
 	
 	private EditText repository;
  	private CheckBox privt;
 // 	private TextView priv_username_id;
 // 	private EditText priv_username;
 // 	private TextView priv_password_id;
 // 	private EditText priv_password;
 //	private CheckBox priv_showPass;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.form_login);
     	
 		Bundle extras = getIntent().getExtras();
 		if(extras != null && extras.containsKey("uploads")){
 			afterAction = true;
 			this.actionListIds = (ViewListIds) getIntent().getIntegerArrayListExtra("uploads");
 			this.actionType = EnumAppsLists.BACKUP;
 		}else if(extras != null && extras.containsKey("restores")){
 			afterAction = true;			
 			this.actionListIds = (ViewListIds) getIntent().getIntegerArrayListExtra("restores");
 			this.actionType = EnumAppsLists.RESTORE;
 		}else{
 			afterAction = false;
 		}
 		
 		this.invoqueType = InvoqueType.reverseOrdinal(extras.getInt("InvoqueType"));	
 		loginState = LoginState.WAITING;
 		success = false;
 		
 		if(!serviceDataIsBound){
     		bindService(new Intent(this, AptoideServiceData.class), serviceDataConnection, Context.BIND_AUTO_CREATE);
     	}
 		
 		super.onCreate(savedInstanceState);
 	}
 
 	
 	private void handleLogin(){
 		
  		username = ((EditText)findViewById(R.id.username));
  		password = ((EditText)findViewById(R.id.password));
  //		showPass = (CheckBox) findViewById(R.id.show_password);
  //		showPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
  //            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
  //                if(isChecked) {
  //                    password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
  //                } else {
  //                    password.setInputType(129);
  ////                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
  //                }
  //            }
  //        });
  		
  		repository = ((EditText)findViewById(R.id.repository));
  		privt = (CheckBox) findViewById(R.id.privt_store);
 // 		privt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 // 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 // 				if(isChecked){
 // 					priv_username.setEnabled(true);
 // 					priv_password.setEnabled(true);
 // //					priv_showPass.setEnabled(true);
 // 					priv_username_id.setVisibility(View.VISIBLE);
 // 					priv_username.setVisibility(View.VISIBLE);
 // 					priv_password_id.setVisibility(View.VISIBLE);
 // 					priv_password.setVisibility(View.VISIBLE);
 // //					priv_showPass.setVisibility(View.VISIBLE);
 // 				}else{
 // 					priv_username.setEnabled(false);
 // 					priv_password.setEnabled(false);
 // //					priv_showPass.setEnabled(false);
 // 					priv_username_id.setVisibility(View.GONE);
 // 					priv_username.setVisibility(View.GONE);
 // 					priv_password.setVisibility(View.GONE);
 // 					priv_password_id.setVisibility(View.GONE);
 // //					priv_showPass.setVisibility(View.GONE);
 // 				}
 // 			}
 // 		});
 // 		priv_username_id = (TextView) findViewById(R.id.priv_username_id);
 // 		priv_username = ((EditText)findViewById(R.id.priv_username));
 // 		priv_username.setEnabled(false);
 // 		priv_password_id = (TextView) findViewById(R.id.priv_password_id);
 // 		priv_password = ((EditText)findViewById(R.id.priv_password));
 // 		priv_password.setEnabled(false);
  //		priv_showPass = (CheckBox) findViewById(R.id.priv_show_password);
  //		priv_showPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
  //            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
  //                if(isChecked) {
  //                	priv_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
  //                } else {
  //                	priv_password.setInputType(129);
  ////                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
  //                }
  //            }
  //        });
  //		priv_showPass.setEnabled(false);
  		
  		
  		((Button)findViewById(R.id.login)).setOnClickListener(new View.OnClickListener(){
  			public void onClick(View arg) {
  				success = false;
  				if(username.getText().toString().trim().equals("")){
  					Toast.makeText(BazaarLogin.this, BazaarLogin.this.getString(R.string.no_username), Toast.LENGTH_SHORT).show();
  				}else if(password.getText().toString().trim().equals("")){
  					Toast.makeText(BazaarLogin.this, BazaarLogin.this.getString(R.string.no_password), Toast.LENGTH_SHORT).show();
  				}else if(repository.getText().toString().trim().equals("")){
  					Toast.makeText(BazaarLogin.this, BazaarLogin.this.getString(R.string.no_repository), Toast.LENGTH_SHORT).show();
  				}
 // 				else if(privt.isChecked() && priv_username.getText().toString().trim().equals("")){
 // 					Toast.makeText(Login.this, Login.this.getString(R.string.no_private_repo_username), Toast.LENGTH_SHORT).show();
 // 				}else if(privt.isChecked() && priv_password.getText().toString().trim().equals("")){
 // 					Toast.makeText(Login.this, Login.this.getString(R.string.no_private_repo_password), Toast.LENGTH_SHORT).show();
 // 				}
  				else{
  					try {
 						serviceDataCaller.callClearServerLogin();
 					} catch (RemoteException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
  						
  					dialogProgress = ProgressDialog.show(BazaarLogin.this, BazaarLogin.this.getString(R.string.logging_in), BazaarLogin.this.getString(R.string.please_wait),true);
  					dialogProgress.setIcon(android.R.drawable.ic_menu_info_details);
  					dialogProgress.setCancelable(true);
  					dialogProgress.setOnDismissListener(new OnDismissListener(){
  						public void onDismiss(DialogInterface arg0) {
  								if(success){
  									Log.d("Aptoide-Login", "Logged in");
  									if(afterAction){
  										switch (actionType) {
 											case BACKUP:
 												Intent upload = new Intent(BazaarLogin.this, Upload.class);
 												upload.putIntegerArrayListExtra("uploads", actionListIds);
 												startActivity(upload);
 												break;
 												
 											case RESTORE:
 												for (Integer appHashid : actionListIds) {
 													try {
 														serviceDataCaller.callInstallApp(appHashid);
 													} catch (RemoteException e) {
 														e.printStackTrace();
 													}
 												}
 												break;
 	
 											default:
 												break;
 										}
  									}
  									finish();
  								}else{
  //										switch (Response) {
  //										case bad_login:
  //										Toast.makeText(Login.this, Login.this.getString(R.string.bad_login), Toast.LENGTH_LONG).show();
  //											break;
  //
  //										default:
  //											break;
  //										}
  									
 // 								}
  								
  //								}else{
  //									Toast.makeText(Login.this, LoginDialog.this.getString(R.string.unabletoexecute), Toast.LENGTH_LONG).show();
  								}
  						}
  					});
  					
  					ViewServerLogin serverLogin =  new ViewServerLogin(username.getText().toString(), password.getText().toString());
  					if(serverLogin.getPasshash() == null){
  						serverLogin =  new ViewServerLogin(username.getText().toString(), password.getText().toString());
  					}
  					serverLogin.setRepoName(repository.getText().toString());
  					if(privt.isChecked()){
 // 						serverLogin.setRepoPrivate(priv_username.getText().toString(), priv_password.getText().toString());
  						serverLogin.setRepoPrivate(serverLogin.getUsername(), serverLogin.getPasshash());
  					}
  
  					Log.d("Aptoide-Login", "Logging in, login: "+serverLogin);
  					new LoginTask(BazaarLogin.this, serverLogin).execute();
  					
  				}
  			}
  		});
  		
  
  		((Button)findViewById(R.id.sign_up)).setOnClickListener(new View.OnClickListener(){
  			public void onClick(View arg) {
 // 				success = false;
 // 				if(username.getText().toString().trim().equals("")){
 // 					Toast.makeText(BazaarLogin.this, BazaarLogin.this.getString(R.string.no_username), Toast.LENGTH_SHORT).show();
 // 				}else if(password.getText().toString().trim().equals("")){
 // 					Toast.makeText(BazaarLogin.this, BazaarLogin.this.getString(R.string.no_password), Toast.LENGTH_SHORT).show();
 // 				}else if(repository.getText().toString().trim().equals("")){
 // 					Toast.makeText(BazaarLogin.this, BazaarLogin.this.getString(R.string.no_repository), Toast.LENGTH_SHORT).show();
 // 				}
 //// 				else if(privt.isChecked() && priv_username.getText().toString().trim().equals("")){
 //// 					Toast.makeText(Login.this, Login.this.getString(R.string.no_private_repo_username), Toast.LENGTH_SHORT).show();
 //// 				}else if(privt.isChecked() && priv_password.getText().toString().trim().equals("")){
 //// 					Toast.makeText(Login.this, Login.this.getString(R.string.no_private_repo_password), Toast.LENGTH_SHORT).show();
 //// 				}
 // 				else{
 // 					
 // 					serverLogin =  new ViewServerLogin(username.getText().toString(), password.getText().toString());
 // 					if(serverLogin.getPasshash() == null){
 // 						serverLogin =  new ViewServerLogin(username.getText().toString(), password.getText().toString());
 // 					}
 // 					serverLogin.setRepoName(repository.getText().toString());
 //// 					if(privt.isChecked()){
 //// 						serverLogin.setRepoPrivate(priv_username.getText().toString(), priv_password.getText().toString());
 //// 					}
 // 					
 // //					(new DialogName(Login.this)).show();
 // 					
 // 					ProgressDialog createAccountProgress = ProgressDialog.show(BazaarLogin.this, BazaarLogin.this.getString(R.string.new_account), BazaarLogin.this.getString(R.string.please_wait),true);
 // 					createAccountProgress.setIcon(android.R.drawable.ic_menu_add);
 // 					createAccountProgress.setCancelable(true);
 // 					createAccountProgress.setOnDismissListener(new OnDismissListener(){
 // 						public void onDismiss(DialogInterface arg0) {
 // 								if(success){
 // 									finish();
 // 									Log.d("Aptoide-Login", "New User Created");
 // 								}else{
 // 									
 // 								}
 // 						}
 // 					});
 // 					
 // 					Log.d("Aptoide-Login", "Creating new acocunt with login: "+serverLogin);
 // 					new CreateAccountTask(BazaarLogin.this, createAccountProgress, serverLogin).execute();
 // 				}
  				
 				try {
 					serviceDataCaller.callClearServerLogin();
 				} catch (RemoteException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
  				
  				Intent signUp = new Intent(BazaarLogin.this, BazaarSignUp.class);
  				if(afterAction){
 					switch (actionType) {
 						case BACKUP:
 							signUp.putIntegerArrayListExtra("uploads", actionListIds);
 							break;
 							
 						case RESTORE:
 							signUp.putIntegerArrayListExtra("restores", actionListIds);
 							break;
 
 						default:
 							break;
 					}
 				}
  				startActivity(signUp);
  				finish();
  			}
  		});
  		
  		
  		
  		if(invoqueType.equals(InvoqueType.OVERRIDE_CREDENTIALS)){
  			ViewServerLogin serverLogin = null;
  			try {
  				serverLogin = serviceDataCaller.callGetServerLogin();
  			} catch (RemoteException e) {
  				e.printStackTrace();
  			}
  			if(serverLogin != null){
  				username.setText(serverLogin.getUsername());
 				repository.setText(serverLogin.getRepoName());
 // 				if(serverLogin.isRepoPrivate()){
 // 					privt.setChecked(true);
 // 					priv_username.setText(serverLogin.getPrivUsername());
 // 					priv_password.setText(serverLogin.getPrivPassword());
 // 				}
  			}
  		}
 		
 	}
 
 	
 //	@Override
 //	public boolean onKeyDown(int keyCode, KeyEvent event) {
 //		if (keyCode == KeyEvent.KEYCODE_BACK && !success) {
 //			Log.d("Aptoide-BazaarLogin", "back press ignored");
 //			return true;
 //		}
 //		return super.onKeyDown(keyCode, event);
 //	}
 
 
 	@Override
 	public void finish() {
 		if(serviceDataIsBound){
 			unbindService(serviceDataConnection);
 		}
 		super.finish();
 	}
 	
 	
 	
 	public class LoginTask extends AsyncTask<Void, Void, EnumServerLoginStatus>{
 		
 		private Context context;
 		private ViewServerLogin serverLogin;
 		
 		public LoginTask(Context context, ViewServerLogin serverLogin) {
 			this.context = context;
 			this.serverLogin = serverLogin;
 		}
 		
 		@Override
 		protected EnumServerLoginStatus doInBackground(Void... args) {
 			try {
 				return EnumServerLoginStatus.reverseOrdinal(serviceDataCaller.callServerLogin(serverLogin));
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		
 		protected void onPostExecute(EnumServerLoginStatus status) {
 			if(status!=null){
 
 				if(status.equals(EnumServerLoginStatus.SUCCESS)){
 					success = true;
					dialogProgress.dismiss();
//					dialogProgress.setCancelable(false);
 				}else{
 					success = false;
 					Toast.makeText(BazaarLogin.this, status.toString(BazaarLogin.this), Toast.LENGTH_SHORT).show();
 					dialogProgress.dismiss();
 				}
 				
 			}else{
 				Toast.makeText(BazaarLogin.this, getString(R.string.login_service_unavailable), Toast.LENGTH_SHORT).show();
 				dialogProgress.dismiss();
 			}
 	    }
 		
 	}
 	
 }
