 package org.learnnavi.app;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.AsyncTask;
 
 public class DownloadUpdate extends AsyncTask<URL, Integer, File> implements OnClickListener {
 	private Kelutral mContext;
 	private String mError;
 	private int mTotalProgress;
 	ProgressDialog mProgress;
 	
 	public DownloadUpdate(Kelutral context)
 	{
 		mContext = context;
 	}
 	
 	// Initialize the progress dialog
 	@Override
 	protected void onPreExecute()
 	{
 		if (!isCancelled())
 		{
 			mProgress = new ProgressDialog(mContext);
 			mProgress.setCancelable(true);
 			mProgress.setMax(1000);
 			mProgress.setTitle(R.string.DownloadingUpdate);
 			mProgress.setButton(ProgressDialog.BUTTON_NEGATIVE, mContext.getText(android.R.string.cancel), this);
 			mProgress.setIndeterminate(false);
 			mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			mProgress.show();
 		}
 	}
 
 	@Override
 	protected File doInBackground(URL... params) {
 		File outfile = null;
 		try
 		{
 			// Connect to the URL
 			int curprogress = -1;
 			URLConnection connection = params[0].openConnection();
 			connection.setDoInput(true);
 			connection.connect();
 			int totallen = connection.getContentLength();
 			int totread = 0;
 
 			mTotalProgress = totallen;
 
 			// Open the IO streams
 			InputStream i = connection.getInputStream();
 			outfile = new File("/data/data/org.learnnavi.app", "dbupdate.sqlite");
 			FileOutputStream f = new FileOutputStream(outfile);
 
 			// Small buffer, for a small file
 			byte[] buffer = new byte[1024];
 			int read;
 			// Make sure it wasn't cancelled
 			while (!isCancelled() && (read = i.read(buffer)) > 0)
 			{
 				totread += read;
 				f.write(buffer, 0, read);
 				if (totallen != 0)
 				{
 					// Don't go updating the progress excessively
 					int progress = (int)((long)totread * 1000 / totallen);
 					if (progress != curprogress)
 					{
 						curprogress = progress;
 						publishProgress(totread);
 					}
 				}
 			}
 			f.close();
 
 			// If it's cancelled, delete the file
 			if (isCancelled())
 			{
 				outfile.delete();
 				return null;
 			}
 
 			// Return the shiny new file
 			return outfile;
 		}
 		catch (IOException ex)
 		{
 			// On exception, treat it like a cancel
 			if (outfile != null && outfile.exists())
 				outfile.delete();
 			mError = ex.getLocalizedMessage();
 		}
 		// Return null like it was cancelled
 		return null;
 	}
 	
 	@Override
 	protected void onProgressUpdate(Integer... values)
 	{
 		// No need to update progress if it was cancelled
 		// The progress dialog is already closed by then
 		if (!isCancelled())
 		{
 			if (mTotalProgress != 0)
 			{
 				mProgress.setMax(mTotalProgress);
 				mTotalProgress = 0;
 			}
 			mProgress.setProgress(values[0]);
 		}
 	}
 	
 	@Override
 	protected void onPostExecute(File result)
 	{
 		if (mProgress != null)
 		{
 			mProgress.hide();
 			mProgress = null;
 		}
 		if (!isCancelled())
 		{
 			if (result == null || !result.exists())
 			{
 				if (mContext != null)
 				{
 					// Report the error if the file was not downloaded
 					if (mError != null)
 					{
 						AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
 						alert.setCancelable(true);
 						alert.setMessage(mError);
 						alert.setNeutralButton(android.R.string.ok, null);
 						alert.create().show();
 					}
 					mContext.downloadComplete(false);
 				}
 			}
 			else
 			{
 				// Copy the file over the live DB
				result.renameTo(new File("/data/data/org.learnnavi.app/databases/", "database.sqlite"));
 				if (mContext != null)
 				{
 					// Force the activity to reload the DB and update the version string
 					mContext.downloadComplete(true);
 				}
 			}
 		}
 		else if (mContext != null)
 			mContext.downloadComplete(false);
 	}
 	
 	public void unParent()
 	{
 		if (mProgress != null)
 			mProgress.hide();
 		mContext = null;
 	}
 	
 	public void reParent(Kelutral context)
 	{
 		mContext = context;
 		if (mProgress != null)
 		{
 			mProgress.setOwnerActivity(context);
 			mProgress.show();
 		}
 		else if (mError != null)
 			// This will just show the error
 			onPostExecute(null);
 		else
 			// There is an edge case where it wasn't cancelled, no error but no file return
 			// However that shouldn't happen, and the resulting call should be harmless anyway
 			mContext.downloadComplete(!isCancelled());
 	}
 
 	// Clicked the cancel button of the progress dialog
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		cancel(false);
 	}
 }
