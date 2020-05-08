 package com.example.haw_app;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 import com.example.haw_app.veranstaltungsplan.implementations.Veranstaltungsplan;
 import com.example.haw_app.veranstaltungsplan.implementations.VeranstaltungsplanTask;
 
 public class VeranstaltungsplanActivity extends Activity {
 	public Veranstaltungsplan vp = Veranstaltungsplan.getInstance();
 	private ProgressDialog mDialog;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		vp.setvpAnzeigeActivity(this);
 		setContentView(R.layout.vp_menu);
 	}
 	
 	public void vpAktualisieren(Veranstaltungsplan vpnew){
 		vp = vpnew;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		builder.setMessage("Veranstaltungsplan.txt wurde aktualisiert!").setNeutralButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 					}
 				});
 		AlertDialog alertDialog = builder.create();
 		alertDialog.show();
 		mDialog.dismiss();
 	}
 	
 	public void anzeigenClick(View view) {
 		startActivity(new Intent(VeranstaltungsplanActivity.this,VeranstaltungsplanAnzeigenActivity.class));
 	}
 
 	public void exportierenClick(View view) {
 		startActivity(new Intent(VeranstaltungsplanActivity.this,VeranstaltungsplanExportierenActivity.class));
 	}
 	public void aktualisierenClick(View view) {
 		mDialog = ProgressDialog.show(VeranstaltungsplanActivity.this, "In progress", "Loading");
 		new VeranstaltungsplanTask().execute(this);
 		 
 	}
 
 }
