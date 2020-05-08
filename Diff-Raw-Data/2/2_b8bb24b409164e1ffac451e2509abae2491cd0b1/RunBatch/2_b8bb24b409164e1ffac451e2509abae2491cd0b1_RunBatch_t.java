 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.ResourceBundle;
 
 /*
  * RunBatch.java
  * 
  * Created on  Aug 23, 2010 5:38:16 PM
  *
  * Copyright 2003-2010 Tufts University  Licensed under the
  * Educational Community License, Version 2.0 (the "License"); you may
  * not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.osedu.org/licenses/ECL-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS"
  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing
  * permissions and limitations under the License.
  * 
  * 
  */
  
 /**
  * This class 
  * @author akumar03
  *
  */
 public class RunBatch {
 	
 	static ResourceBundle bundle = ResourceBundle.getBundle("fctools");
 	public static final String PIDS_FILE = bundle.getString("pids.file");
 	public static final String ACTION = bundle.getString("action");
 	public static final String ignorePattern = bundle.getString("pids.ignore");
 	
 	PrintStream out = System.out;  // default 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		// TODO Auto-generated method stub
 		RunBatch r = new RunBatch();
 		r.run();
 
 	}
 	
 	public void run() throws Exception {
 		InputStream in = this.getClass().getResourceAsStream(PIDS_FILE);
  
 		BufferedReader seqReader = new BufferedReader(new  InputStreamReader(in,"UTF-8"));
 		String outFileName = bundle.getString("output.file");
 		out = new PrintStream(new FileOutputStream(outFileName));
 		ModifyDatastream m = new ModifyDatastream();
 		m.setOut(out);
 		String line  = new String();
         while((line = seqReader.readLine()) != null) {
          	if(!isIgnorePattern(line)) {
          		if (ACTION.equals("bibliographicCitation"))
         			m.purgeBibliographicCitation(line);        		
         		else if (ACTION.equals("oaipmh"))
         		{
         			m.setObjectId(line);
          		m.addItemId();
         		}
         		else
         		{
         			System.out.println("You should specify which action this batch process should perform.");
         			System.exit(0);
         		}
         		Thread.sleep(Integer.parseInt(bundle.getString("time.sleep")));
         	}
          	System.out.println("At PID "+line);
         }
 	}
 	
 	private boolean isIgnorePattern(String s) {
 		String[] ignore = ignorePattern.split("\\s+");
 		for(int i=0;i<ignore.length;i++)  {
 			if(s.startsWith(ignore[i])){
 				out.println(s+", IGNORED matched ignore pattern: "+ignore[i]);
 				return true;
 			}
 		}
 		return false;
 	
 	}
 	
 }
