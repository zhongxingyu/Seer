 package com.HST.highschooltix;
 
 import java.io.BufferedReader;
 
 
 
 
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import java.util.regex.Pattern;
 
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 
 
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 import android.text.InputFilter;
 import android.text.Spanned;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class Login extends Activity {
 
 	EditText logingEditText;
 	EditText passwordEditText;
 	Button login_Button;
 	Button SignUp_Button;
 	Button forgetpassButton;
 	
 	//CustomHttpClient customHttpClient;
 	String user_name;
 	String  password;
 	String respomce;
 	private String response;
 	String login_Url="http://obscure-depths-9305.herokuapp.com/sessions.json";
 	 boolean net_check;
 	 String u;
 	 String r;
 	 String ur;
 	 private final Handler uiHandler=new Handler();
 	 private boolean isUpdateRequired=false;
 	 RelativeLayout relativeLayout;
 		TextView net;
 		TextView netnot;
 		ImageView canclenetbt;
 		HttpURLConnection connection;
 		 ProgressBar progressBar;
 	TextView messaegTextView;
 	DatabaseCopy dbcopy;
 	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
 			"[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
 	        "\\@" +
 	        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
 	        "(" +
 	            "\\." +
 	            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
 	        ")+"
 
 	      );
 	
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Load_login();
         
     
     if(user_name.equalsIgnoreCase("no")&&password.equalsIgnoreCase("no"))
     {
     	  dbcopy=new DatabaseCopy();
 		  AssetManager am = null;
 	       am=getAssets();
 	       dbcopy.copy(am);
 	  	 
     	
     	setContentView(R.layout.activity_loging);
     	r="/HighSchool/script/high.php";
     	InputFilter filter = null;
         logingEditText=(EditText) findViewById(R.id.loginname);
         passwordEditText=(EditText) findViewById(R.id.loginPassword);
         login_Button=(Button) findViewById(R.id.btnLogin);
         relativeLayout=(RelativeLayout) findViewById(R.id.network_layout);
         //forgetpassButton=(Button) findViewById(R.id.btnLinkToForgetScreen);
         net=(TextView) findViewById(R.id.net);
         netnot=(TextView) findViewById(R.id.netnot);
         canclenetbt=(ImageView) findViewById(R.id.canclenet);
         progressBar =  (ProgressBar) findViewById(R.id.progressBar1);
         net_check=isOnline();
         u="http://111.118.248.140";
         filter = new InputFilter() 
         {
         	public CharSequence filter(CharSequence source, int start, int end,Spanned dest, int dstart, int dend) {
         		//end=11;
         	for (int i = start; i < end; i++) {
         	if (Character.isSpaceChar(source.charAt(i))) {
         	return "";
         	}
         	}
         	return null;
         	}
         	};
         	 logingEditText.setFilters(new InputFilter[] { filter,new InputFilter.LengthFilter(30) });
         	 passwordEditText.setFilters(new InputFilter[] { filter,new InputFilter.LengthFilter(30) });
         	 
           	 ur=u+r;
 //         	forgetpassButton.setOnClickListener(new OnClickListener() 
 //         	{
 // 				
 // 				public void onClick(View v) 
 // 				{
 // 					// TODO Auto-generated method stub
 // 					startActivity(new Intent(Login.this,ForgetScreen.class));
 // 					
 // 				}
 // 			}); 
          	 
         	 
         login_Button.setOnClickListener(new OnClickListener() 
         {
 			
 			public void onClick(View v) 
 			{
 				// TODO Auto-generated method stub
 				 user_name=logingEditText.getText().toString();
 				 password=passwordEditText.getText().toString();
 				if( user_name.length()==0 || password.length()==0)
 				{
 					if(user_name.length()==0)
 					{
 						logingEditText.setError("please enter username......");
 					}
 					if(password.length()==0)
 					{
 						passwordEditText.setError("please enter password......");
 					}
 				}
 				else
 				{
 
 				
 				if(!checkEmail(user_name))
 				{
 			        
 					logingEditText.setError( "Correct Email required" );
 		         }
 				
 				else
 				{
 					 if(net_check)
 				     {
 
 							login_Button.setEnabled(false);
 							boolean c=true;
 							String re = null;
 							try {
 								re=CustomHttpClient.executeHttpGet(ur);
 								System.out.println("re="+re);
 							} catch (Exception e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 							if(re.length()==0)
 							{
 								Toast.makeText(getBaseContext(), "problem in connection....",Toast.LENGTH_SHORT).show();
//								new ShowDialogAsyncTask().execute();
 							}
 							else
 							{
 								new ShowDialogAsyncTask().execute();
 							}
 				     }
 					 else
 					 {
 						 relativeLayout.setVisibility(View.VISIBLE);
 					   basicInitializations();
 						// Toast.makeText(getBaseContext(),"Net not connected",Toast.LENGTH_SHORT).show();
 					 }
 				}
 				}
 	
 			}
 		});
 
         
         canclenetbt.setOnClickListener(new OnClickListener()
         {
   		
   		public void onClick(View v) 
   		{
   			// TODO Auto-generated method stub
   			relativeLayout.setVisibility(View.INVISIBLE);
   			canclenetbt.setVisibility(View.INVISIBLE);
   			netnot.setVisibility(View.INVISIBLE);
   			
   		}
   	});
     }
     else 
     {
 		Intent intent=new Intent(Login.this,MainScreen.class);
 		startActivity(intent);
 		finish();
 	}
        
     
     }
 
     
     public boolean isOnline() 
 	{
 		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	         NetworkInfo ni = cm.getActiveNetworkInfo();
 	         boolean result = false;
 	         if(ni != null )
 	         {
 	             if(  ni.getState() == NetworkInfo.State.CONNECTED )
 	             {
 	                 result = true;
 	             }
 	         }
 
 	         return result;
 
 	        } 
     
     
     public void basicInitializations(){
    	 
     	
     	net.setVisibility(View.VISIBLE);
 
         try{
             new Thread()
             {
                 public void run() 
                 {
                     initializeApp();
                     uiHandler.post( new Runnable()
                     {
                         
                         public void run() 
                         {
                             if(isUpdateRequired)
                             {
                                 //TODO:
                             }else
                             {
                             	
                                 net.setVisibility(View.GONE);
                                 netnot.setVisibility(View.VISIBLE);
        						 canclenetbt.setVisibility(View.VISIBLE);
                                
                              
                             }
                         }
                     } );
                 }
                 public void initializeApp()
                 {
                     // Initialize application data here
                 	for(int i=0;i<6;i++)
                 	{
                 		try 
                 		{
     						Thread.sleep(1000);
     					} catch (InterruptedException e)
     					{
     						// TODO Auto-generated catch block
     						e.printStackTrace();
     					}
                 	}
                 }
         }.start();
         }catch (Exception e) {}
     }
 	
     
     private boolean checkEmail(String email) 
     {
  		return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
  		}
     
     
     
     public String tryLogin(String mUsername, String mPassword)
     {           
         
        OutputStreamWriter request = null;
 
             URL url = null;   
             String response = null;         
             String parameters = "user"+"[email]"+"="+mUsername+"&user"+"[password]"+"="+mPassword;   
 
             //Toast.makeText(this,"Call"+ response, 0).show();    
             try
             {
             	
             	// Toast.makeText(this,"Call again", 0).show();    
                 url = new URL(login_Url);
                 connection = (HttpURLConnection) url.openConnection();
                 connection.setDoOutput(true);
                 connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                 connection.setReadTimeout(10000);
                 connection.setConnectTimeout(15000);
                 connection.setRequestMethod("POST"); 
              
 
                 request = new OutputStreamWriter(connection.getOutputStream());
                 request.write(parameters);
                 request.flush();
                 request.close();            
                 String line = "";               
                 InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                 BufferedReader reader = new BufferedReader(isr);
                 StringBuilder sb = new StringBuilder();
                 while ((line = reader.readLine()) != null)
                 {
                     sb.append(line + "\n");
                 }
                 // Response from server after login process will be stored in response variable.                
                 response = sb.toString();
                 // You can perform UI operations here
                 //Toast.makeText(this,"Message from Server: \n"+ response, 0).show();    
                 System.out.println(response);
                 isr.close();
                 
                 reader.close();
 
             }
             catch(IOException e)
             {
                 System.out.println("Error:  "+e);
                 
                 response="no";
             }
 			return response;
     }
     
     
     private void postData(String res)
 	{
 	
 	    	 try {
 
 			      JSONObject json_data=new JSONObject(res);
 			      String success_string=json_data.get("success").toString();
 			      String auth_token=json_data.get("auth_token").toString();
 
 			      
 			      System.out.println("value of sss: "+success_string);
 			      success_string=success_string.trim();
 			      if(success_string.equalsIgnoreCase("false"))
 			      {
 			    	  Toast.makeText(getBaseContext(),"User ID or Password Miss Match...",Toast.LENGTH_SHORT).show();
 			      }
 			      if(success_string.equalsIgnoreCase("true"))
 			      {
 			    	  passwordEditText.setText("");
 			    	  Save_Login(user_name,password);
 			    	  Toast.makeText(getBaseContext(), "Login Succesful...",Toast.LENGTH_SHORT).show();
 			    	  Intent intent=new Intent(Login.this,MainScreen.class);
 			    	  //intent.putExtra("auth_token",auth_token);
 			    	  Save_auth_token(auth_token);
 			    	  startActivity(intent);
 			    	  finish();
 			      }
 
 	    	 } catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					 System.out.println("Error "+e);
 				}
 		     
 		
 	}
     
     private void Save_auth_token(String auth_token)
 	  {
 		    SharedPreferences sharedPreferences = getSharedPreferences("AUTH", MODE_PRIVATE);
 		    SharedPreferences.Editor editor = sharedPreferences.edit();
 		    editor.putString("auth_token",auth_token);
 		    editor.commit();
 
 	}
     
     private void Save_Login(String user_name,String password)
 	  {
 		    SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
 		    SharedPreferences.Editor editor = sharedPreferences.edit();
 		    editor.putString("username",user_name);
 		    editor.putString("password",password);
 		    
 		
 		
 		    editor.commit();
 
 	}
     
     public void Load_login()
     {
     	try {
     		
     		SharedPreferences preferences=getSharedPreferences("LOGIN", MODE_PRIVATE);
     		user_name=preferences.getString("username", "no");
     		password=preferences.getString("password", "no");
     		
 			
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
     }
     
     @Override
     public void onBackPressed() 
    {
 	
     
 	   super.onBackPressed();
 	   Save_Login("no","no");
        finish();
     }
     
     
     
     
     private class ShowDialogAsyncTask extends AsyncTask<Void, Integer, Void>{
     	 
      int progress_status;
          
         @Override
      protected void onPreExecute() 
         {
       // update the UI immediately after the task is executed
       super.onPreExecute();
        progress_status = 0;
      progressBar.setVisibility(View.VISIBLE);
        
      }
          
      @Override
      protected Void doInBackground(Void... params) {
        
     	 try 
          {
              //Getting data from server
 
     		 response=tryLogin(user_name,password); 
          }
          catch (Exception e) {
              e.printStackTrace();
          }
          return null;
      }
      
      @Override
      protected void onProgressUpdate(Integer... values)
      {
       super.onProgressUpdate(values);
          
      }
       
      @Override
      protected void onPostExecute(Void result) 
      {
       super.onPostExecute(result);
        
       progressBar.setVisibility(View.INVISIBLE);
       if(response.equalsIgnoreCase("no"))
 		 {
 			 Toast.makeText(getBaseContext(),"User ID or Password Miss Match...",Toast.LENGTH_SHORT).show();
 		 }
       postData(response);
 
        login_Button.setEnabled(true);
      }
        }
     
 }
