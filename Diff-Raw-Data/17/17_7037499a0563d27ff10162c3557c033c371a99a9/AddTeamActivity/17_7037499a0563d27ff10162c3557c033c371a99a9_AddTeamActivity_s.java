 package com.example.soccerman;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import library.TeamFunctions;
 import library.UserFunctions;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class AddTeamActivity extends Activity
 {
 	UserFunctions userFunctions;
 	Button btnAddTeam, btnCancel;
 	EditText inputTeamName;
 	TextView addTeamErrorMsg;
 	
 	private static String TEAM_ADD_MESSAGE = "Your team has been successfully created. For players to join this team, get them to use the following code: ";
 	
 	// JSON response names
 	private static String KEY_SUCCESS = "success";
 	private static String TEAM_CODE = "code";
 	private static String TEAM_NAME = "name";
 	
 	@Override
 		public void onCreate(Bundle savedInstanceState) 
 		{
 			super.onCreate(savedInstanceState);
 			
 			userFunctions = new UserFunctions();
 	        if(userFunctions.isUserLoggedIn(getApplicationContext())) 
 	        {
 	        	setContentView(R.layout.add_team);
 	        	
 	        	inputTeamName = (EditText) findViewById(R.id.teamName);
 	        	btnAddTeam = (Button) findViewById(R.id.btnAddTeam);
 	            btnCancel = (Button) findViewById(R.id.btnCancelAddTeam);
 	            addTeamErrorMsg = (TextView) findViewById(R.id.add_team_error);
 	            
 	            btnAddTeam.setOnClickListener(new View.OnClickListener()
 	            {
 	            	public void onClick(View view) 
 	            	{
 	            		String name = inputTeamName.getText().toString();
 	                	String created_by = userFunctions.getLoggedInUserId(getApplicationContext());
 	                	
 	                	TeamFunctions teamFunctions = new TeamFunctions();
 	                	JSONObject json = teamFunctions.addTeam(name, created_by);
 	            		
 	                	// check for add team response
 	                	try
 	                	{
 	                		if (json.getString(KEY_SUCCESS) != null) 
 	                		{
 	                			String res = json.getString(KEY_SUCCESS); 
 	                			if(Integer.parseInt(res) == 1)
 	                			{
 	                				addTeamErrorMsg.setText("");
 	                				
 	                				
 	                				final Dialog dialog = new Dialog(AddTeamActivity.this);
 	                				dialog.setContentView(R.layout.add_team_success);
 	                				
 	                				final TextView addTeamMessage = (TextView) dialog.findViewById(R.id.add_team_post_message);
 	                				final TextView addTeamCode = (TextView) dialog.findViewById(R.id.add_team_code);
 	                				final Button addTeamOk = (Button) dialog.findViewById(R.id.add_team_ok);
 	                				
 	                				JSONObject json_team = json.getJSONObject("team");
 	                				
 	                				dialog.setTitle("Team: " + json_team.getString(TEAM_NAME));
 	                				addTeamMessage.setText(TEAM_ADD_MESSAGE);
 		                			addTeamCode.setText(json_team.getString(TEAM_CODE));
 		                			
 		                			addTeamOk.setOnClickListener(new View.OnClickListener() 
 		                			{
 										@Override
 										public void onClick(View v)
 										{
 											Intent dash = new Intent(AddTeamActivity.this, DashboardActivity.class);
 											dash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 											startActivity(dash);
 										}
 		                			});
 		                			dialog.show();
 		                		}
 		                		else 
 		                		{
 		                			addTeamErrorMsg.setText("Team name aleady taken/can't be blank");
 		                		}
 	                		}
 	                	} 
 	                	catch (JSONException e) 
 	                	{
 	            			e.printStackTrace();
 	            		}
 	            		
 	            	}
 	            });
 	            
 	            btnCancel.setOnClickListener(new View.OnClickListener()
 	            {
 	            	public void onClick(View view) {
 	            		Intent dashboard = new Intent(AddTeamActivity.this, DashboardActivity.class);
 	            		startActivity(dashboard);
 	            		finish();
 	            	}
 	            });
 	            
 	        }
 	        else
 	        {
 	            // user is not logged in show login screen
 	            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
 	            login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	            startActivity(login);
 	            finish();
 	        } 
 		}
 }
