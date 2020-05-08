 package uw.cse403.nonogramfun;
 
 import android.os.Bundle;
 
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class MainActivity extends Activity {
	private TextView myText = null;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        
 
        myText = new TextView(null);
         
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     
     /** Called when the user clicks the Access button 
      * @throws SQLException */
    public void accessServer(View view) throws SQLException {
         // Do something in response to button
     	Connection con = DriverManager.getConnection("jdbc:mysql://82.197.130.17", 
     			"1361466_00e6", "uwcse403nanogram");
     	PreparedStatement statement = con.prepareStatement("SELECT first_name FROM developers WHERE id = 1");
     	ResultSet result = statement.executeQuery();
    	while (result.next()) {
    		
    	}
     }
     
 }
