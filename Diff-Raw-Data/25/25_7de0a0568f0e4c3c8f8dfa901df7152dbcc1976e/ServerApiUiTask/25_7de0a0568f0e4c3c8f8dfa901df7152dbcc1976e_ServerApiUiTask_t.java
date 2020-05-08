 package net.mms_projects.copyit.android.tasks;
 
 import net.mms_projects.copyit.R;
 import net.mms_projects.copyit.api.ServerApi;
 import android.app.ProgressDialog;
 import android.content.Context;
 
 abstract public class ServerApiUiTask<Params, Progress, Result> extends
 		ServerApiTask<Params, Progress, Result> {
 
 	final protected Context context;

 	protected ProgressDialog progress;
 	protected boolean useProgressDialog = false;
	protected String progressDialogTitle;
	protected String progressDialogMessage;

 	protected Exception exception;
 
 	public ServerApiUiTask(Context context, ServerApi api) {
 		super(api);
 
 		this.context = context;
		this.progressDialogTitle = this.context.getResources().getString(
				R.string.dialog_title_busy);
		this.progressDialogMessage = this.context.getResources().getString(
				R.string.dialog_title_busy);
 	}
 
 	@Override
 	protected void onPreExecute() {
 		if (this.useProgressDialog) {
 			this.progress = ProgressDialog.show(this.context,
 					this.progressDialogTitle, this.progressDialogMessage, true);
 		}
 		
 		super.onPreExecute();
 	}
 
 	public void setUseProgressDialog(boolean useProgressBar) {
 		this.useProgressDialog = useProgressBar;
 	}
	
	public void setProgressDialigTitle(String progressDialogTitle) {
		this.progressDialogTitle = progressDialogTitle;
	}
	
	public void setProgressDialigMessage(String progressDialogMessage) {
		this.progressDialogMessage = progressDialogMessage;
	}
 
 	@Override
 	protected void onPostExecute(Result result) {
 		if (this.useProgressDialog) {
 			this.progress.dismiss();
 		}
 
 		super.onPostExecute(result);
 	}
 
 }
