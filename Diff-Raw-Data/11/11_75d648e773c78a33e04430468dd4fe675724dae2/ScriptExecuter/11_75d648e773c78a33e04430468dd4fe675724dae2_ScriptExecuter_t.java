 package org.projectx.icetool;
 
 import java.io.InputStreamReader;
 
 import android.os.AsyncTask;
 import android.widget.TextView;
 
 
 public class ScriptExecuter extends AsyncTask<String, String, Integer> {
 	static final int    OUTPUT_BUFSIZE = 32;
	static final String CMD_ICETOOL="icetool";
	static final String CMD_SU="su";
 	static final String CMD_C="-c";
 	private TextView    consoleView  = null;
 			
     protected Integer doInBackground(String...cmds) {
         int count = cmds.length;
         for (int i = 0; i < count; i++) {
             executeCommand(cmds[i]);
         }
         return Integer.valueOf(count);
     }
 
     protected void onProgressUpdate(String... inputChars) {
         for (String c : inputChars) {
            consoleView.append(c);
         }
     }
 
     protected void onPostExecute(Integer result) {
     	consoleView.append("== Finished, return value is " + result.toString() + " ==\n");
     }	
 
 	private Integer executeCommand(String cmd) {
		String[] str={CMD_SU,CMD_C,CMD_ICETOOL,cmd};
		consoleView = ICETool.getInstance().getConsoleView();
 		Process   p = null;
 		int       rc = -1;
 		
 		if (consoleView == null)
 			return rc;
 		
 		try {
 			publishProgress("==== Starting execution: " + cmd + " ====\n");			
 			p = Runtime.getRuntime().exec(str);
 			// We avoid BufferedReader, as we want single characters
 			// such those on wget command
 			InputStreamReader r = new InputStreamReader(p.getInputStream());
 			char[] b = new char[OUTPUT_BUFSIZE];
 			while (r.read(b,0,OUTPUT_BUFSIZE) != -1) {
 				//b[OUTPUT_BUFSIZE] = '\0';
 				publishProgress(new String(b));
 			}
			rc = p.exitValue();			
 		} catch (Exception e) {			
 			publishProgress(e.toString() + "\n");
 		}
 		return rc;	
 	}    
 }
