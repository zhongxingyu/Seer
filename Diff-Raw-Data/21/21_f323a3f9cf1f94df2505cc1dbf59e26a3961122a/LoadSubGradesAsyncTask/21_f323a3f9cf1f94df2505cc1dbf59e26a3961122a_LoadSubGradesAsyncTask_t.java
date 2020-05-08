 package zipcodeman.glookup.subgrades;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import zipcodeman.glookup.util.SSHExecute;
 
 
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 
 import android.app.Activity;
 //import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 //import android.widget.Toast;
import android.util.Log;
 
 public class LoadSubGradesAsyncTask extends
 		AsyncTask<String, Integer, String[]> {
 
 	public static final int PROG_INIT = 0;
 	public static final int PROG_CONNECT = 1;
 	public static final int PROG_LOGIN = 2;
 	public static final int PROG_EXECUTE = 3;
 	public static final int PROG_READ = 4;
 	public static final int PROG_DONE = 5;
 
 	Activity context;
 	String uname;
 	String pass;
 	String server;
 	String assign;
 	String comment;
 	
 	ProgressDialog p;
 	LoadSubGradesAsyncTask(Activity context){
 		this.context = context;
 		p = new ProgressDialog(context);
 		p.setCancelable(false);
 		p.setMax(PROG_DONE);
 		p.setProgress(PROG_INIT);
 		p.show();
 		p.setMessage(getProgressString(PROG_INIT));
 	}
 	
 	private String getProgressString(int prog){
 		switch(prog){
 		case PROG_INIT:
 			return "Loading...";
 		case PROG_CONNECT:
 			return "Connecting";
 		case PROG_LOGIN:
 			return "Logging in";
 		case PROG_EXECUTE:
 			return "Executing command";
 		case PROG_READ:
 			return "Reading result";
 		case PROG_DONE:
 			return "Done";
 		}
 		return "";
 	}
 	
 	@Override
 	protected String[] doInBackground(String... params) {
 		uname = params[0];
 		pass = params[1];
 		server = params[2];
 		assign = params[3];
 		comment = params[4];
 		
 		publishProgress(PROG_INIT);
 		SSHExecute executor = new SSHExecute(uname, server, pass);
 		String read = executor.RunCommand("glookup -b 1 -s " + assign);
 		if (read == "") return null;
         String[] rows = read.split("\n");
         publishProgress(PROG_DONE);
         return rows;
 	}
 	
 	@Override
 	public void onPostExecute(String[] result){
		try {
			p.dismiss();
		} catch (IllegalArgumentException iae) {
			Log.e("LoadSubGradesAsyncTask", "Could not dismiss progress dialog");
		}
 		SubGradeActivity sga = (SubGradeActivity)this.context;
 		sga.setRows(result);
 		sga.render();
 	}
 
 	
 	@Override
 	public void onProgressUpdate(Integer ... progress){
 		p.setProgress(progress[0]);
 		p.setMessage(this.getProgressString(progress[0]));
 	}
 
 }
