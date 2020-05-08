 package com.example.wecharades.presenter;
 /**
  * TODO: CREATE DIRECTORIES AND SHIT ON FTP.
  */
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.MediaController;
 import android.widget.VideoView;
 
import com.example.wecharades.model.DatabaseException;
 import com.example.wecharades.model.Turn;
 import com.example.wecharades.views.StartActivity;
 import com.example.wecharades.views.VideoUploadActivity;
 
 public class VideoUploadPresenter extends Presenter {
 
 	private VideoUploadActivity activity;
 	private UploadVideo upload;
 	private Turn turn;
 	private String fileName = "PresentVideo.mp4";
 	private String serverPath;
 
 	public VideoUploadPresenter(VideoUploadActivity activity) {
 		super(activity);
 		this.activity = activity;
 		this.turn = (Turn) activity.getIntent().getSerializableExtra("Turn");
 	}
 	
 	/**
 	 * Starts the VideoUpload
 	 * @param context
 	 * @param path
 	 */
 	public void uploadVideo(Context context, String path) {
 		upload = new UploadVideo(context, path);
 		upload.execute();
 	}
 	
 	/**
 	 * Initiate and start the video.
 	 * @param videoView
 	 */
 	public void playVideo(VideoView videoView) {
 		videoView.setVideoURI(CaptureVideo.uriVideo);
 		videoView.setMediaController(new MediaController(activity));
 		videoView.start();
 		videoView.requestFocus();
 	}
 	
 	/**
 	 * Sets the storage location of the videofile for the FTP-server. 
 	 * @return
 	 */
 	private String setServerStorageLocation(){
 		String gameID = turn.getGameId();
 		String turnNumber = String.valueOf(turn.getTurnNumber());
 		String serverPath = "/APP/" + gameID + "/" + turnNumber + "/";
 		return serverPath;
 	}
 	
 	/**
 	 * Lets the player to re-record the video and deletes the current file.
 	 * @param path
 	 */
 	public void reRecord(String path) {
 		File file = new File(path);
 		file.delete();
 		Intent intent = new Intent(activity.getApplicationContext(), CaptureVideo.class);
 		activity.startActivity(intent);
 		activity.finish();	
 	}
 
 	private class UploadVideo extends AsyncTask<Void, Long, Boolean>{
 
 		private ProgressDialog mDialog;
 		Context mContext;
 		private String SAVE_PATH;
 
 		public UploadVideo(Context context, String path){
 			mContext = context;
 			SAVE_PATH = path;
 			mDialog = new ProgressDialog(mContext);
 		}
 		
 		@Override
 		protected void onPreExecute(){
 			mDialog.setTitle("Uploading Charade");
 			mDialog.setMessage("Please Wait");
 			mDialog.show();
 		}
 		
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			FTPClient ftp = null;
 
 			try{
 				ftp = new FTPClient();
 				ftp.connect("ftp.mklcompetencia.se", 21);
 
 				if (ftp.login("mklcompetencia.se", "ypkq4w")){
 					ftp.enterLocalPassiveMode();
 					ftp.setFileType(FTP.BINARY_FILE_TYPE);					
 					ftpCreateDirectoryTree(ftp, setServerStorageLocation());				
 					FileInputStream in = new FileInputStream(new File(SAVE_PATH));
 					boolean result = ftp.storeFile(setServerStorageLocation() + fileName, in);					
 					in.close();
 					if (result) 
 						Log.v("upload result", "succeeded");
 					ftp.logout();
 					ftp.disconnect();
 				}
 			}
 			catch (Exception e){
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result){
 			if(mDialog.isShowing()){
 
 				//TODO: Show a dialog with the text "Successful upload" and click OK to proceed
 				/*mDialog.setButton("Cancel", new OnClickListener() {
 
 	                @Override
 	                public void onClick(DialogInterface dialog, int which) {
 	                    // TODO Auto-generated method stub
 
 	                myDialog.dismiss();
 	                }
 	            }); */
 				mDialog.dismiss();
 				turn.setVideoLink(setServerStorageLocation() + fileName);
				try {
					dc.updateGame(dc.getGame(turn.getGameId()));
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 
 				//Send to startscreen on success
 				Intent intent = new Intent(activity.getApplicationContext(), StartActivity.class);
 				activity.startActivity(intent);
 				activity.finish();
 
 			}
 		}
 		/**
 		* Utility to create an arbitrary directory hierarchy on the remote ftp server 
 		* @param client the FTP-client
 		* @param dirTree  the directory tree only delimited with / chars.  No file name!
 		* @throws Exception
 		*/
 		private void ftpCreateDirectoryTree( FTPClient client, String dirTree ) throws IOException {
 			
 		  boolean dirExists = true;
 		  
 		  //tokenize the string and attempt to change into each directory level.  If you cannot, then start creating.
 		  String[] directories = dirTree.split("/");
 		  for (String dir : directories ) {
 		    if (!dir.isEmpty() ) {
 		      if (dirExists) {
 		        dirExists = client.changeWorkingDirectory(dir);
 		      }
 		      if (!dirExists) {
 		        if (!client.makeDirectory(dir)) {
 		          throw new IOException("Unable to create remote directory '" + dir + "'.  error='" + client.getReplyString()+"'");
 		        }
 		        if (!client.changeWorkingDirectory(dir)) {
 		          throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + client.getReplyString()+"'");
 		        }
 		      }
 		    }
 		  }     
 		}
 	}
 }
