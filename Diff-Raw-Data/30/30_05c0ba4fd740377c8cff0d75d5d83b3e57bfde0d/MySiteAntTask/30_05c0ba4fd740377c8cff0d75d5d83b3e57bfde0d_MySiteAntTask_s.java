 /**
  *
  * mysite - Static Site Generator
  * Copyright (c) 2012, myJerry Developers
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package org.myjerry.mysite.ant;
 
 import java.io.IOException;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.myjerry.mysite.MySite;
 
 /**
  * An ANT task to run the MySite application using Apache ANT.
  * 
  * @author sangupta
  * @since 5 Jan 2012
  */
 public class MySiteAntTask extends Task {
 	
 	private String projectXML = null;
 	
 	private String outputFolder = null;
 	
 	@Override
 	public void execute() throws BuildException {
 		if(isEmpty(this.projectXML)) {
 			throw new BuildException("Project XML file path must be specified.");
 		}
 		
 		if(isEmpty(this.outputFolder)) {
 			throw new BuildException("Site output folder must be specified.");
 		}
 		
 		String[] args = new String[2];
 		args[0] = this.projectXML;
 		args[1] = this.outputFolder;
 		
 		try {
 			MySite.main(args);
 		} catch (IOException e) {
 			throw new BuildException("Unable to build the project", e);
 		}
 	}
 	
 	/**
 	 * Convenience function to test if a string contains anything except whitespaces.
 	 * 
 	 * @param string string to test for.
 	 * @return <code>true</code> if the string is <code>null</code> or empty, <code>false</code> otherwise.
 	 */
 	private boolean isEmpty(String string) {
 		if(string == null || string.trim().length() == 0) {
 			return true;
 		}
 		
 		return false;
 	}
 
 }
