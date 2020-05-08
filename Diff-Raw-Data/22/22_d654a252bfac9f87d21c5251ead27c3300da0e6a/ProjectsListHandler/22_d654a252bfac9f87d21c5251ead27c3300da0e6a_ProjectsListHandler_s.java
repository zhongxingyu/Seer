 package org.imaginationforpeople.android.handler;
 
 import java.util.List;
 
 import org.imaginationforpeople.android.model.I4pProjectTranslation;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.widget.ArrayAdapter;
 
 public class ProjectsListHandler extends BaseHandler implements OnClickListener {
 	private Activity activity;
 	private ArrayAdapter<I4pProjectTranslation> adapter;
 	
 	private ProgressDialog progress;
 	
 	public ProjectsListHandler(Activity ac, ArrayAdapter<I4pProjectTranslation> ad) {
 		activity = ac;
 		adapter = ad;
 	}
 	
 	@Override
 	protected void onStart(int arg, Object obj) {
 		progress = new ProgressDialog(activity);
 		progress.setMessage("Loading projects list");
 		progress.show();
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onSuccess(int arg, Object obj) {
 		List<I4pProjectTranslation> projects = (List<I4pProjectTranslation>) obj;
 		for(I4pProjectTranslation project : projects) {
 			adapter.add(project);
 		}
 		
 		progress.dismiss();
 	}
 	
 	@Override
 	protected void onError(int arg, Object obj) {
 		progress.dismiss();
 		
 		AlertDialog alert = new AlertDialog.Builder(activity).create();
		alert.setTitle("Error");
		alert.setMessage("An error has occured");
		alert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", this);
 		alert.show();
 	}
 
 	public void onClick(DialogInterface arg0, int arg1) {
 		activity.finish();
 	}
 }
