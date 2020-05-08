 package innovationcare.app.antibioticguidelines.ui;
 
 import innovationcare.app.antibioticguidelines.Antibiotic;
 import innovationcare.app.antibioticguidelines.R;
 import innovationcare.app.antibioticguidelines.database.GuidelineDataAccess;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class DisplayAntibioticActivity extends Activity {
 	private final GuidelineDataAccess dao = new GuidelineDataAccess(this);
 	private Antibiotic antibiotic;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_display_antibiotic);
 		
 		Intent intent = getIntent();
 		String id = intent.getStringExtra("id");
 		dao.open();
 		antibiotic = dao.readAntibiotic(id);
 		
 		
 		// Close the database;
 		dao.close();
 		setTitle(antibiotic.getName());
 		LinearLayout layout = (LinearLayout)findViewById(R.id.PDF1);
 		String filename = antibiotic.getName()+".pdf";
 		File tempFile = new File( Environment.getExternalStorageDirectory().getAbsolutePath(), filename );
         if ( tempFile.exists() ) {
         	TextView tv = (TextView)findViewById(R.id.PDF1text);
         	tv.setText(filename);
         }
         else {
         	layout.setVisibility(layout.GONE);
         }
         
 		layout = (LinearLayout)findViewById(R.id.PDF2);
 		filename = antibiotic.getName()+" RDH 2009.pdf";
 		tempFile = new File( Environment.getExternalStorageDirectory().getAbsolutePath(), filename );
         if ( tempFile.exists() ) {
         	TextView tv = (TextView)findViewById(R.id.PDF2text);
         	tv.setText(filename);
         }
         else {
         	layout.setVisibility(layout.GONE);
         }
         
 		layout = (LinearLayout)findViewById(R.id.Link1);
         if ( !antibiotic.getInfoLink1Title().isEmpty() ) {
         	TextView tv = (TextView)findViewById(R.id.Link1text);
         	tv.setText(antibiotic.getInfoLink1Title());
         }
         else {
         	layout.setVisibility(layout.GONE);
         }
         
         layout = (LinearLayout)findViewById(R.id.Link2);
         if ( !antibiotic.getInfoLink2Title().isEmpty() ) {
         	TextView tv = (TextView)findViewById(R.id.Link2text);
         	tv.setText(antibiotic.getInfoLink2Title());
         }
         else {
         	layout.setVisibility(layout.GONE);
         }
 		}
 		
 		public void clickPDF(View view) {
 			if(view.getId() == R.id.PDF1) {
 				String filename = antibiotic.getName() + ".pdf";
 				final File tempFile = new File( Environment.getExternalStorageDirectory().getAbsolutePath(), filename );
 				openPDF( this, Uri.fromFile( tempFile ) );
 			}
 			
 			else {
				String filename = antibiotic.getName() + "RDH.pdf";
 				final File tempFile = new File( Environment.getExternalStorageDirectory().getAbsolutePath(), filename );
 				openPDF( this, Uri.fromFile( tempFile ) );
 			}
 		}
 		
 		public void clickLink(View view) {
 			Intent intent = new Intent(this, DisplayAntibioticLinkActivity.class);
 			if(view.getId() == R.id.Link1) {
 				intent.putExtra("URL", antibiotic.getInfoLink1());
 			}
 			
 			else {
 				intent.putExtra("URL", antibiotic.getInfoLink2());
 			}
 			intent.putExtra("name", antibiotic.getName());
 			startActivity(intent);
 		}
 		
 		public static final void openPDF(Context context, Uri localUri ) {
 	        Intent i = new Intent( Intent.ACTION_VIEW );
 	        i.setDataAndType( localUri,  "application/pdf" );
 	        context.startActivity( i );
 	    }
 
 		@Override
 		public boolean onCreateOptionsMenu(Menu menu) {
 			// Inflate the menu; this adds items to the action bar if it is present.
 			getMenuInflater().inflate(R.menu.main, menu);
 			return true;
 		}
 		
 		@Override
 		public boolean onOptionsItemSelected(MenuItem item) {
 		    // Handle item selection
 		    switch (item.getItemId()) {
 		        case R.id.returnToHomeButton:
 		            Intent intent = new Intent(this, MainActivity.class);
 		            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 		            startActivity(intent);
 		            return true;
 		        default:
 		            return super.onOptionsItemSelected(item);
 		    }
 		}
 
 }
