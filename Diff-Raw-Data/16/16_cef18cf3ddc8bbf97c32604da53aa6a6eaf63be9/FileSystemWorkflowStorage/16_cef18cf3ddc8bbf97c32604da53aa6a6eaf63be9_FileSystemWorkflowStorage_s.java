 /**
  * Approved for Public Release: 10-4800. Distribution Unlimited.
  * Copyright 2014 The MITRE Corporation,
  * Licensed under the Apache License,
  * Version 2.0 (the "License");
  *
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */
 
 package org.wiredwidgets.cow.server.service.workflow.storage;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.oxm.jaxb.Jaxb2Marshaller;
 import org.wiredwidgets.cow.server.api.model.v2.Process;
 import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
 
 public class FileSystemWorkflowStorage implements IWorkflowStorage {
 
 	private static Logger log = Logger.getLogger(FileSystemWorkflowStorage.class);
 	
     @Value("${workflow.fs.dir}")
     String WORKFLOW_DIR;
     
     @Autowired
     protected Jaxb2Marshaller marshaller;
 	
 	
 	@Override
 	public Process get(String key) {
 		try {
 			InputStream is = getFileAsInputStream(key);
 			StreamSource source = new StreamSource(is);
 			Process process = (Process) marshaller.unmarshal(source);
 			return process;
 		} 
 		catch (IOException e) {
 			log.error("Process: " + key + " was requested", e);
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	
 	
 	@Override
 	public List<ProcessDefinition> getAll() {
 		List<ProcessDefinition> procDefs = new ArrayList<ProcessDefinition>();
 		
 		File wFlowDir = new File(WORKFLOW_DIR);
		for (String filePath : wFlowDir.list()) {
			
			String fileName = wFlowDir.getName();		
			//remove extension from filename
 			int lastDot = fileName.lastIndexOf(".xml");
 			if (lastDot == -1) {
				lastDot = filePath.length();
 			}
			String workflowKey = fileName.substring(0, lastDot);
 			
 			ProcessDefinition pd = new ProcessDefinition();
 			pd.setName(workflowKey);
 			pd.setKey(workflowKey);
 			procDefs.add(pd);
 		}
 		
 		return procDefs;
 	}
 
 	
 	@Override
 	public URI save(Process process) {
 		File file = getFile(process.getKey());
 		StreamResult result = new StreamResult(file);
 		marshaller.marshal(process, result);
 		return file.toURI();
 	}
 
 	
 	@Override
 	public boolean delete(String key) {
 		File f = getFile(key);	
 		return f.delete();
 	}
 
 	private File getFile(String key) {
 		File f = new File(WORKFLOW_DIR + '/' + key + ".xml");
 		return f;
 	}
 	
 	/**
 	 * Easiest way to convert file to UTF-16 for marshaller
 	 * @param key
 	 * @return
 	 */
 	private InputStream getFileAsInputStream(String key) throws IOException{
 		BufferedReader reader = null;
 		try {
 			StringBuilder sb = new StringBuilder();
 			reader = new BufferedReader(new FileReader(getFile(key)));
 			for(String line = reader.readLine(); line != null; line = reader.readLine()) {
 			    sb.append(line);
 			}
 			return new ByteArrayInputStream(sb.toString().getBytes());
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 	}
 		
 	
 }
