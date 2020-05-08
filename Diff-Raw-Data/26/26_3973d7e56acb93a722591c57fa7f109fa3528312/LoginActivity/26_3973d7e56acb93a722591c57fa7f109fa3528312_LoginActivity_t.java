 package com.enonic.android.twitranet.login;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.enonic.android.twitranet.EntryArrayAdapter;
 import com.enonic.android.twitranet.R;
 
 public class LoginActivity extends Activity
 {
 
     @Override
     protected void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState );
         setContentView( R.layout.login_layout );
 
         Button loginButton = (Button) findViewById( R.id.loginButton );
         loginButton.setOnClickListener( new View.OnClickListener() {
 
             public void onClick( View view )
             {
                 Intent callingIntent = getIntent();
                 EditText userNameField = (EditText) findViewById( R.id.username );
                 EditText passwordField = (EditText) findViewById( R.id.password );
                 final String userNameEntered = userNameField.getText().toString();
                 final String passwordEntered = passwordField.getText().toString();
                 callingIntent.putExtra( EntryArrayAdapter.KEY_USERNAME, userNameEntered );
                 callingIntent.putExtra( EntryArrayAdapter.KEY_PASSWORD, passwordEntered );
                 setResult( RESULT_OK, callingIntent );
                 finish();
             }
         } );

        Button clearButton = (Button) findViewById( R.id.clearLoginButton );
        clearButton.setOnClickListener( new View.OnClickListener() {
            public void onClick( View view ) {
                EditText userNameField = (EditText) findViewById( R.id.username );
                EditText passwordField = (EditText) findViewById( R.id.password );
                userNameField.setText( "" );
                passwordField.setText( "" );
            }
        });
     }
 
 
 }
