 package com.sm;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.io.IOUtils;
 
 import com.sm.DepTreeNode.DepType;
 import com.sm.DepTreeNode.IncIterator;
 
 /**
  * Copyright 2012 Valeriy Filatov.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 public class Merger {
 	
 	private String modulesDir;
 
	public void setModulesDir(String dir){
 		this.modulesDir = dir;
 		if(!dir.endsWith(File.separator))
 			this.modulesDir+=File.separator;
 	}
 	
 	public String getModule(String moduleName) throws Exception {
 		DepTreeNode node = new DepTreeNode(DepType.DEP, modulesDir+moduleName);
 		final Set<String> incs = new TreeSet<String>();
 		
 		node.iterate(new IncIterator() {
 			public void onDep(DepTreeNode dep) {
 				incs.add(dep.getFileName());
 			}
 		});
 		
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		for(String inc : incs){
 			IOUtils.copy(new FileInputStream(inc), out);
 			IOUtils.write("\n", out);
 		}
 		
 		String ret = out.toString();
 		if(node.getPostProcessors() != null){
 			for(String postprocessor : node.getPostProcessors()){
 				if("coffeescript".equalsIgnoreCase(postprocessor)){
 					CoffeeScriptCompiler coffeec = new CoffeeScriptCompiler();
 					ret = coffeec.compile(ret);
 				}
 			}
 		}
 		
 		return ret;
 	}
 
 }
