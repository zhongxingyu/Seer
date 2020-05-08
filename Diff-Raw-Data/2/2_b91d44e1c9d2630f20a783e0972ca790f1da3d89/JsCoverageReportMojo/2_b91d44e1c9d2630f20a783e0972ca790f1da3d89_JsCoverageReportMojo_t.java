 package com.thoughtworks.green;
 
 /*
  * Copyright 2001-2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.File;
 
 import jstestdriver.coveage.report.CoveageReportAnalysis;
 import jstestdriver.coveage.report.DefaultFileHelper;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * 
  * @goal test
  * 
  * @phase process-sources
  */
 public class JsCoverageReportMojo extends AbstractMojo {
 	/**
 	 * out put directory.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private File outputDirectory;
 	/**
 	 * coverage file path
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String coverageFile;
 	/**
 	 * limit default 0.
 	 * 
 	 * @parameter
 	 */
 	private int limit = 0;
 	/**
 	 * excludes js file.
 	 * 
 	 * @parameter
 	 */
 	private String[] excludes;
 	private String fileName = "coverage.data.js";;
 
 	public void execute() throws MojoExecutionException {
 		String outPutFile;
 		try {
 			System.out.print("JsCoverageReport maven execute:.");
 
			outPutFile = new File(outputDirectory, fileName).getAbsolutePath();
 			CoveageReportAnalysis coveageReportAnalysis = new CoveageReportAnalysis(
 					new DefaultFileHelper());
 			coveageReportAnalysis.execute(coverageFile, outPutFile, excludes,
 					limit);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new MojoExecutionException(e.getMessage());
 		}
 		System.out.print(String.format("%s already finished(%s).", fileName,
 				outPutFile));
 
 	}
 }
