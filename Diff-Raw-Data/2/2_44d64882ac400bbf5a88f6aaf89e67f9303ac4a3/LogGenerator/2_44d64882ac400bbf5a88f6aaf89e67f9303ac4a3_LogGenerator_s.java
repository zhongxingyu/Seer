 /*
  * Copyright 2011 Matthias van der Vlies
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package core;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 
 import play.jobs.Job;
 import play.libs.F.Promise;
 import play.templates.JavaExtensions;
 
 public class LogGenerator extends Job {
 
 	private BufferedReader bufferedReader;
 	private InputStreamReader inputStreamReader;
 	private FileInputStream fileInputStream;
 	
 	public LogGenerator(final String filePath, boolean skipToEnd) throws Exception {
 		fileInputStream = new FileInputStream (filePath);
 		inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");
 		bufferedReader = new BufferedReader(inputStreamReader);
 		
 		if(skipToEnd) {
 			bufferedReader.skip(fileInputStream.available()); // skip to end
 		}
 	}
 	
 	public String doJobWithResult() throws Exception {
 		while (true) {
 		    final String line = bufferedReader.readLine();
 		    if (line == null) {
 		    	// timeout
		        Thread.sleep(500);
 		    }
 		    else {
 		    	return JavaExtensions.escapeHtml(line) + "<br/>";
 		    }
 		}
 	}
 	
 	public void close() throws IOException {
 		fileInputStream.close();
 	}
 }
