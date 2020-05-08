 package info.ilyaraz.passwordgenerator;
 
 import java.security.MessageDigest;
 
 import info.ilyaraz.passwordgenerator.domain.ClueData;
 import info.ilyaraz.passwordgenerator.util.Callback1;
 import info.ilyaraz.passwordgenerator.util.Closure;
 import info.ilyaraz.passwordgenerator.util.Constants;
 import info.ilyaraz.passwordgenerator.util.StringCallback;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContextWrapper;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 
 public class ClueEditor {
 	public static void editClue(String clueId, final Activity context, final Callback1<ClueData> onSuccess, final Closure onFailure) {
 		final SharedPreferences settings = context.getSharedPreferences(Constants.STORAGE_NAMESPACE, 0);
 		
     	LayoutInflater inflater = context.getLayoutInflater();
    	View dialogLayout = inflater.inflate(R.layout.add_clue, null);
     	AlertDialog.Builder builder = new AlertDialog.Builder(context);
     	builder.setView(dialogLayout);
     	    	
     	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {				
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				onSuccess.Run(new ClueData());
 			}
 		});
     	
     	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {				
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				onFailure.Run();
 			}
 		});
     	
     	builder.show();
 	}
 }
