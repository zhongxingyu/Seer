 package ma.nabil.android.dialog;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.Button;
 
 public class TestDialog extends Activity {
 	/** Called when the activity is first created. */
 	protected ProgressDialog mBusy;
 	final Handler mHandler = new Handler();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Button test = (Button) findViewById(R.id.Button01);
 		test.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				testDialog();
 			}
 		});
 	}
 
 	private void testDialog() {
 		mBusy = ProgressDialog.show(TestDialog.this, "Tlchargement",
 				"En cours...", true, false);
 
 		Thread t = new Thread() {
 			public void run() {
 				try {
 					sleep(6000);
 					mHandler.post(new Runnable() {
 
 						@Override
 						public void run() {
 							mBusy.dismiss();

 						}
 					});
					new AlertDialog.Builder(TestDialog.this)
    				.setTitle("Erreur de tlchargement")
    				.setMessage("Une erreur est survenue lors du tlchargement : Erreur Inconnue")
    				.setIcon(R.drawable.star_big_on)
    				.setNeutralButton("Ok",
    				new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog,
    				int which) {
    				}
    				}).show();
 				} catch (Exception e) {
 					mHandler.post(new Runnable() {
 
 						@Override
 						public void run() {
 							mBusy.dismiss();
 
 						}
 					});
 				}
 
 			}
 		};
 		t.start();
 	}
 }
