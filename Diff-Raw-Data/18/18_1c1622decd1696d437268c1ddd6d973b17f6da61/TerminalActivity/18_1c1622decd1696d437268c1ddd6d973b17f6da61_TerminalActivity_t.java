 package com.adamcarruthers.foundry;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
import java.lang.ProcessBuilder;
 
 public class TerminalActivity extends Fragment {
 	
 	private Context mContext;
 	private TextView terminalOutput;
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
 		
 		mContext = getActivity().getApplicationContext();
 		
         View view = inflater.inflate(R.layout.terminal, container, false);
         
         terminalOutput = (TextView) view.findViewById(R.id.terminal_output);
         
         // TODO: do something for real, this is a stupid thing to do.
         new ExecuteInShell().execute("ls /system/");
         return view;
     }
 	
    /*
     * Courtesy of Daniel Huckaby (HandlerExploit)
     */
 	private class ExecuteInShell extends AsyncTask<String, Void, Void> {
 		@Override
 		protected Void doInBackground(String... cmds) {
 			final Process process;
 	        try {
	        	process = new ProcessBuilder("sh").redirectErrorStream(true).start();
 	            BufferedReader stdInput = new BufferedReader(
 	            new InputStreamReader(process.getInputStream()));
 	            BufferedWriter stdOutput = new BufferedWriter(
 	            new OutputStreamWriter(process.getOutputStream()));
 
 	            stdOutput.write(cmds[0] + "; exit\n");
 	            stdOutput.flush();
 	            
 	            Thread thread = new Thread(new Runnable() {
 	          	  public void run() {
 	          		  try {
 	          			  process.waitFor();
 	          		  } catch (InterruptedException e) {
 	          			  e.printStackTrace();
 	          		  }
 	          	  }
 	            });
 	            thread.start();
 	            
 	            final StringBuilder status = new StringBuilder();
 				while (thread.isAlive() || stdInput.ready()) {
 					final String newLine = stdInput.readLine();
 					if (newLine != null) {
 						status.append(newLine + "\n");
 					}
 					getActivity().runOnUiThread(new Runnable() {
 						public void run() {
 							terminalOutput.setText(status.toString());
 						}
 					});
 				}
 	            
 	            stdInput.close();
 	            stdOutput.close();
 	        } catch (Exception e) {
 	     	   e.printStackTrace();
 	        }
 			return null;
 		}
 	}
 }
