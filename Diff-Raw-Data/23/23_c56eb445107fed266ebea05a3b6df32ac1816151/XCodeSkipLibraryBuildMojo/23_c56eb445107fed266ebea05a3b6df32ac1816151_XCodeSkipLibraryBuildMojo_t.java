 /*
  * #%L
  * xcode-maven-plugin
  * %%
  * Copyright (C) 2012 SAP AG
  * %%
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
  * #L%
  */
 package com.sap.prd.mobile.ios.mios;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
 * Prevents building of libraries or frameworks during Company Build.
  * 
 * The apps must be code signed. In case of signing with multiple certificates, e.g Enterprise and
 * Company, apps must be built twice. The libraries and the frameworks do not need to be signed and
 * must be built once.
  * 
  * 
  * @goal skip-library-build
  * 
  */
 public class XCodeSkipLibraryBuildMojo extends AbstractXCodeMojo
 {
 
   /**
    * If set to <code>true</code> the build aborts with an exception if a lib (in comparison with an
    * app) is built.
    * 
    * @parameter expression="${xcode.forbidLibBuild}"
    * @since 1.2.0
    */
   private boolean forbidLibBuild;
 
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
 
     if (forbidLibBuild)
       throw new MojoExecutionException(
            "xcode-library or xcode-framework detected ("
                   + project.getArtifact()
                  + "). Libraries and Frameworks must not be built during company builds. "
                  + "They are not released separately. At the time when the company build is triggered we expect all libraries and frameworks are already contained in the repository.");
   }
 }
