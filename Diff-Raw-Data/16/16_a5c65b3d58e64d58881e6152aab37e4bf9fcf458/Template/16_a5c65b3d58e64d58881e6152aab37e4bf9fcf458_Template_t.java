 /*
  * The MIT License
  *
  * Copyright (c) 2011 Kevin Birch <kevin.birch@gmail.com>. Some rights reserved.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.webguys.maven.plugin.st;
 
 import org.apache.maven.ProjectDependenciesResolver;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.stringtemplate.v4.AutoIndentWriter;
 import org.stringtemplate.v4.ST;
 import org.stringtemplate.v4.misc.ErrorBuffer;
 import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;
 
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

 public class Template
 {
     /**
      * The path to the template file's parent directory.
      *
      * @parameter
      * @required
      */
     private File directory;
 
     /**
      * The name of the template file to render.
      *
      * @parameter
      * @required
      */
     private String name;
 
     /**
      * The path to the output file.
      *
      * @parameter
      * @required
      */
     private File target;
 
     /**
      * The class to invoke to provide data for the template.
      *
      * @parameter
      */
     private Controller controller;
 
     /**
      * The static properties to be provided to the template.
      *
      * @parameter
      */
     private Map<String, String> properties;
 
     public File getDirectory()
     {
         return directory;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void invokeController(ST st, ExecutionEnvironment executionEnvironment, ProjectDependenciesResolver dependenciesResolver, Log log) throws MojoExecutionException
     {
         if(null != this.controller)
         {
             this.controller.invoke(st, executionEnvironment, dependenciesResolver, log);
         }
     }
 
     public void installProperties(ST st)
     {
         if(null != this.properties)
         {
             for(Entry<String, String> entry : this.properties.entrySet())
             {
                 st.add(entry.getKey(), entry.getValue());
             }
         }
     }
 
     public void render(ST st, MavenProject project, Log log) throws MojoExecutionException
     {
         try
         {
             File outputFile = this.prepareOutputFile(project.getBasedir());
             this.prepareCompilerSourceRoot(outputFile, project, log);
             FileWriter fileWriter = new FileWriter(outputFile);
             ErrorBuffer listener = new ErrorBuffer();
             st.write(new AutoIndentWriter(fileWriter), listener);
             fileWriter.flush();
             fileWriter.close();
 
             if(!listener.errors.isEmpty())
             {
                 throw new MojoExecutionException(listener.toString());
             }
         }
         catch(IOException e)
         {
             throw new MojoExecutionException(String.format("Unable to write output file: %s. (%s)", this.target.getAbsolutePath(), e.getMessage()), e);
         }
     }
 
     private File prepareOutputFile(File baseDirectory) throws MojoExecutionException, IOException
     {
         File outputFile = this.target;
         if(!outputFile.isAbsolute())
         {
             outputFile = new File(baseDirectory, outputFile.getPath());
         }
 
         if(!outputFile.exists())
         {
             if(!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs())
             {
                 throw new MojoExecutionException(String.format("Unable to fully create the output directory: %s", this.target.getParentFile()));
             }
             if(!outputFile.createNewFile())
             {
                 throw new MojoExecutionException(String.format("Unable to create the output file: %s", this.target));
             }
         }
 
         return outputFile;
     }
 
     private void prepareCompilerSourceRoot(File file, MavenProject project, Log log)
     {
         String path = file.getPath();
         if(file.getName().endsWith("java") && path.contains("generated-sources"))
         {
             int index = path.indexOf("generated-sources") + 18;
            index = path.indexOf(File.separator, index);
             String sourceRoot = path.substring(0, index);
             log.info("Adding compile source root: " + sourceRoot);
             project.addCompileSourceRoot(sourceRoot);
         }
     }
 }
