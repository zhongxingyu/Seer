 package com.horeca;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class ChangePasswordActivity extends MyActivity {
 	private Button button = null;
 	private EditText ancien_mdp = null;
 	private EditText nouveau_mdp = null;
 	private EditText confirmation_mdp = null;
 	private TextView password_error = null;
 	private TextView password_error2 = null;
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_changepassword);
 	    ancien_mdp = (EditText) findViewById(R.id.ancien_mdp);
 		nouveau_mdp = (EditText) findViewById(R.id.nouveau_mdp);
 		confirmation_mdp = (EditText) findViewById(R.id.password_confirmation);
 		password_error=(TextView) findViewById(R.id.password_err);
		password_error2=(TextView) findViewById(R.id.password_err2);
 	    button = (Button) findViewById(R.id.change_pass_button);
 	    button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View arg0) {
             	String ancienmdp = ancien_mdp.getText().toString();
             	String nouveaumdp = nouveau_mdp.getText().toString();
             	String confirmmdp = confirmation_mdp.getText().toString();
             	MySqliteHelper sqliteHelper = new MySqliteHelper(ChangePasswordActivity.this);
         		SQLiteDatabase db = sqliteHelper.getReadableDatabase();
             	int err = User.getCurrentUser().updatePassword(db, ancienmdp, nouveaumdp, confirmmdp);
             	if (err == User.INVALID_PASSWORD) {
         			password_error.setVisibility(View.GONE);
        			password_error2.setVisibility(View.VISIBLE);
         		} else if (err == User.PASSWORDS_DONT_MATCH) {
         			password_error.setVisibility(View.VISIBLE);
         			password_error2.setVisibility(View.GONE);
         		} else {
         			password_error.setVisibility(View.GONE);
         			password_error2.setVisibility(View.VISIBLE);
         			finish();
         		}
             	
         		db.close();
             }
        });
 	}
 }
