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
	public void learnFinish() {
		
		Toast.makeText(this.context, "Test", 0).show();
 		((Activity)this.context).finish();
 	}

}
