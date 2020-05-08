 package net.mms_projects.copyit.android.tasks;
 
 import net.mms_projects.copy_it.R;
 import net.mms_projects.copyit.api.ServerApi;
 import net.mms_projects.copyit.api.endpoints.ClipboardContentEndpoint;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.Toast;
 
 public class SendToAppTask extends ServerApiUiTask<Void, Void, String> {
 
 	public SendToAppTask(Context context, ServerApi api) {
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
 
 			Intent sendIntent = new Intent();
 			sendIntent.setAction(Intent.ACTION_SEND);
 			sendIntent.putExtra(Intent.EXTRA_TEXT, content);
 			sendIntent.setType("text/plain");
 
 			this.context.startActivity(Intent.createChooser(
 					sendIntent,
 					this.context.getResources().getString(
 							R.string.dialog_title_select_send_app)));
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
