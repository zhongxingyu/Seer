 package dani.leahele.EkspSysApp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class WriteActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_write);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.write, menu);
 		return true;
 	}
 	
 	public void sendComment(View view){
		System.out.println("!!!!!!! test");
 		TextView comment = (TextView) findViewById(R.id.write_comment);
 		String s = comment.getText().toString();
 		Intent i = new Intent();
 		i.putExtra("dani.leahele.EkspSysApp.comment", s );
 		setResult(RESULT_OK, i);
 		finish();
 	}
 
 }
