 package miklos.martin.learningandroid;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.sql.SQLDataException;
 import java.sql.SQLException;
 
 import miklos.martin.learningandroid.model.AwesomePerson;
 
 /**
  * SQLite example
  */
 public class SQLiteExample extends Activity implements View.OnClickListener, View.OnKeyListener {
 
     EditText name, awesomeness, id;
     Button insert, load, update, loadEntry, delete;
 
     AwesomePerson editedRow;
 
     @Override
     protected void onCreate ( Bundle savedInstanceState ) {
         super.onCreate( savedInstanceState );
         setContentView( R.layout.sqliteexample );
         initialize();
     }
 
     private void initialize () {
 
         name = (EditText) findViewById( R.id.etSQLName );
         awesomeness = (EditText) findViewById( R.id.etSQLAwesomeness );
         id = (EditText) findViewById( R.id.etSQLId );
 
         insert = (Button) findViewById( R.id.bInsert );
         load = (Button) findViewById( R.id.bLoad );
         update = (Button) findViewById( R.id.bUpdate );
         loadEntry = (Button) findViewById( R.id.bLoadEntry );
         delete = (Button) findViewById( R.id.bDelete );
 
         insert.setOnClickListener( this );
         load.setOnClickListener( this );
         update.setOnClickListener( this );
         loadEntry.setOnClickListener( this );
         delete.setOnClickListener( this );
 
         id.setOnKeyListener( this );
     }
 
     @Override
     public void onClick ( View view ) {
 
         AwesomeOrNot aon;
 
         switch ( view.getId() ) {
             case R.id.bInsert:
 
                 boolean success = true;
 
                 try {
                     String personName = name.getText().toString();
                     int personAwesomeness = Integer.parseInt( awesomeness.getText().toString() );
 
                     aon = new AwesomeOrNot( SQLiteExample.this );
                     aon.open();
 
                     aon.createNew( personName, personAwesomeness );
 
                     aon.close();
                 } catch ( Exception e ) {
                     e.printStackTrace();
                     success = false;
                 }
 
                 if ( success ) {
                     Dialog dialog = new Dialog( this );
                     dialog.setTitle( "Saved model!" );
                     TextView text = new TextView( this );
                     text.setText( "Successfully saved that nonsense you wrote." );
 
                     dialog.setContentView( text );
                     dialog.show();
                 }
 
                 break;
 
             case R.id.bLoad:
                 Intent i = new Intent( SQLiteExample.this, SQLView.class );
                 startActivity( i );
                 break;
 
             case R.id.bUpdate:
                 aon = new AwesomeOrNot( this );
 
                 try {
                     aon.open();
                    int result = aon.update( editedRow );
                    Toast t = Toast.makeText( this, String.valueOf( result ), Toast.LENGTH_LONG );
                    t.show();
                     aon.close();
                 } catch ( SQLException e ) {
                     e.printStackTrace();
                 }
                 break;
 
             case R.id.bLoadEntry:
                 long rowId = Long.parseLong( id.getText().toString() );
                 aon = new AwesomeOrNot( this );
 
                 try {
                     aon.open();
                     editedRow = aon.find( rowId );
                     name.setText( editedRow.getName() );
                     awesomeness.setText( String.valueOf( editedRow.getAwesomeness() ) );
                     aon.close();
                 } catch ( SQLDataException e ) {
                     Toast toast = Toast.makeText( this, "Entity not found!", Toast.LENGTH_LONG );
                     toast.show();
                     e.printStackTrace();
                 } catch ( SQLException e ) {
                     e.printStackTrace();
                 }
 
                 break;
 
             case R.id.bDelete:
                 aon = new AwesomeOrNot( this );
                 try {
                     aon.open();
                     aon.delete( editedRow );
                     aon.close();
                 } catch ( SQLException e ) {
                     e.printStackTrace();
                 }
 
                 break;
         }
     }
 
     private void setEnabledState( boolean enabled ) {
         update.setEnabled( enabled );
         loadEntry.setEnabled( enabled );
         delete.setEnabled( enabled );
     }
 
     @Override
     public boolean onKey ( View view, int i, KeyEvent keyEvent ) {
         setEnabledState( id.getText().length() > 0 );
         return false;
     }
 }
