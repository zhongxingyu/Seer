 package net.mms_projects.copyit.android.tasks;
 
 import net.mms_projects.copy_it.R;
 import net.mms_projects.copyit.AndroidClipboardUtils;
 import net.mms_projects.copyit.ClipboardUtils;
 import net.mms_projects.copyit.api.ServerApi;
 import net.mms_projects.copyit.api.endpoints.ClipboardContentEndpoint;
 import android.content.Context;
 import android.widget.Toast;
 
 public class PasteItTask extends ServerApiUiTask<Void, Void, String> {
 	public PasteItTask(Context context, ServerApi api) {
 		super(context, api);
 
 		this.setProgressDialigMessage(context.getResources().getString(
 				R.string.text_content_pulling));
		this.setUseProgressDialog(true);
 	}
 
 	@Override
 	protected String doInBackgroundWithException(Void... params)
 			throws Exception {
 		return new ClipboardContentEndpoint(api).get();
 	}
 
 	@Override
 	protected void onPostExecute(String content) {
 		try {
 			this.doExceptionCheck();
 
 			ClipboardUtils clipboard = new AndroidClipboardUtils(this.context);
 
 			Toast.makeText(
 					this.context,
 					this.context.getResources().getString(
 							R.string.text_content_pulled, content),
 					Toast.LENGTH_LONG).show();
 			clipboard.setText(content);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 			Toast.makeText(
 					this.context,
 					this.context.getResources().getString(
 							R.string.error_general, e.getLocalizedMessage()),
 					Toast.LENGTH_LONG).show();
 		}
 
 		super.onPostExecute(content);
 	}
 }
