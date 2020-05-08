 package net.mms_projects.copyit.android.tasks;
 
 import net.mms_projects.copy_it.R;
 import net.mms_projects.copyit.api.ServerApi;
 import net.mms_projects.copyit.api.endpoints.ClipboardContentEndpoint;
 import android.content.Context;
 import android.widget.Toast;
 
 public class CopyItTask extends ServerApiUiTask<String, Void, Boolean> {
 	public CopyItTask(Context context, ServerApi api) {
 		super(context, api);
 		
 		this.setProgressDialigMessage(context.getResources().getString(
 				R.string.text_content_pushing));
		this.setUseProgressDialog(true);
 	}
 
 	private String content;
 
 	@Override
 	protected Boolean doInBackgroundWithException(String... params)
 			throws Exception {
 		this.content = params[0];
 
 		return new ClipboardContentEndpoint(api).update(this.content);
 	}
 
 	@Override
 	protected void onPostExecute(Boolean result) {
 		try {
 			this.doExceptionCheck();
 
 			if (result) {
 				Toast.makeText(
 						this.context,
 						this.context.getResources().getString(
 								R.string.text_content_pushed, this.content),
 						Toast.LENGTH_LONG).show();
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 			Toast.makeText(
 					this.context,
 					this.context.getResources().getString(
 							R.string.error_general, e.getLocalizedMessage()),
 					Toast.LENGTH_LONG).show();
 		}
 
 		super.onPostExecute(result);
 	}
 }
