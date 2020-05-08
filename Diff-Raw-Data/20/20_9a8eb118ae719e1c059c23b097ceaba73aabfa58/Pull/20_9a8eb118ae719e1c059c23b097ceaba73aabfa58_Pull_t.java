 package org.pm.webdav;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.tools.ant.Task;
 
 public class Pull extends Task {
 	private String user;
 	private String password;
 	private String url;
 	private String file;
 	private String outFile;
 	private boolean verbose = false;
 	private boolean overwrite = false;
 	
 	public void setUser(String user) {
 		this.user = user;
 	}
 	
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	public void setUrl(String url) {
 		this.url = url;
 	}
 	
 	public void setFile(String file) {
 		this.file = file;
		
		//Issue 3 - outFile property now an optional element
		if (this.outFile == null)
			this.outFile = file;
 	}
 
 	public void setOutFile(String outFile) {
 		this.outFile = outFile;
 	}
 	
 	public void setVerbose(boolean verbose) {
 		this.verbose = verbose;
 	}
 	
 	public void setOverwrite(boolean overwrite) {
 		this.overwrite = overwrite;
 	}
 
 	public void execute() {
 		try {
     		HttpClient client = new HttpClient();
     		Credentials creds = new UsernamePasswordCredentials(user, password);
     		client.getState().setCredentials(AuthScope.ANY, creds);
    		log("Outfile = " + outFile + ": file = " + file);
     		File f = new File(outFile);
     		long startTime = System.currentTimeMillis();
     		
     		if (this.overwrite || !f.exists()) {
     			if (verbose) {
     				if (f.exists()) log("Overwriting " + f.getAbsolutePath());
 					log("Downloading " + url + "/" + file + " to " + f.getAbsolutePath());
     			}
     			
     			//Download the file
     			GetMethod method = new GetMethod(url + "/" + file);
         		client.executeMethod(method);
         		
         		//200 OK => No issues
         		if (method.getStatusCode() != 200)
         			throw new Exception(method.getStatusCode() + " " + method.getStatusText());
         		
 	    		InputStream is = method.getResponseBodyAsStream();
 	    		OutputStream out = new FileOutputStream(f);
 	    		byte buf[] = new byte[1024];
 	    		int len;
 	    		
 	    		while((len = is.read(buf))>0) {
 	    		    out.write(buf,0,len);
 	    			out.flush();
 	    		}
 	    		out.close();
 	    		
 	    		if (verbose) {
 		    		long endTime = System.currentTimeMillis();
 		    		long elapsed = ((endTime - startTime) / 1000);
 		    		log (file + " took " + elapsed + " seconds to complete");
 	    		}
     		} else
     			log ("Skipping " + file);
 
     	} catch (Exception e) {
 	    	e.printStackTrace();
 	    } 
 	}
 
 }
