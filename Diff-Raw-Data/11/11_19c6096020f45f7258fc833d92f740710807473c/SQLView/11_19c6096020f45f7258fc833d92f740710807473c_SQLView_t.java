 package miklos.martin.learningandroid;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.ViewGroup;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import miklos.martin.learningandroid.model.AwesomePerson;
 
 /**
  * Show model from SQLite db
  */
 public class SQLView extends Activity {
 
     TableLayout table;
     AwesomeOrNot model;
 
     @Override
     protected void onCreate ( Bundle savedInstanceState ) {
         super.onCreate( savedInstanceState );
         setContentView( R.layout.sqlview );
 
         initialize();
     }
 
     private void initialize () {
 
         table = (TableLayout) findViewById( R.id.tlResults );
         model = new AwesomeOrNot( this );
         try {
             model.open();
             ArrayList<AwesomePerson> data = model.getData();
             model.close();
 
             for ( AwesomePerson row : data ) {
                 TableRow newRow = new TableRow( SQLView.this );
 
                 TextView name = new TextView( SQLView.this );
                 TextView awesome = new TextView( SQLView.this );
 
                name.setLayoutParams( new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                ) );
                awesome.setLayoutParams( new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                ) );

                 name.setText( row.getName() );
                 if ( row.isAwesome() ) {
                     awesome.setText( "Yes" );
                 } else {
                     awesome.setText( "No" );
                 }
 
                 newRow.addView( name );
                 newRow.addView( awesome );
                 table.addView( newRow );
             }
 
         } catch ( SQLException e ) {
             e.printStackTrace();
         }
     }
 }
