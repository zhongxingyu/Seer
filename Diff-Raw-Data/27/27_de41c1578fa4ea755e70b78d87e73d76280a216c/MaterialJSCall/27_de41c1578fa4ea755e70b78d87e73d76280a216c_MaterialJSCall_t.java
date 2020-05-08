 package tw.edu.chu.csie.e_learning.ui;
 
 import android.app.Activity;
 import android.content.Context;
 import android.webkit.JavascriptInterface;
 import android.widget.Toast;
 
 public class MaterialJSCall {
 
 	private Context context;
 	
 	public MaterialJSCall(Context context) {
 		this.context = context;
 	}
 	
 	// Annotation is needed for SDK version 17 or above.
 	@JavascriptInterface
	public void learnFinish(String[] ansQID, String[] ansCheck) {
		int i;
		for(i = 0; i<ansQID.length ; i++){
			Toast.makeText(this.context, ansQID[i], Toast.LENGTH_SHORT).show();			
		}
 		((Activity)this.context).finish();
 	}
}
