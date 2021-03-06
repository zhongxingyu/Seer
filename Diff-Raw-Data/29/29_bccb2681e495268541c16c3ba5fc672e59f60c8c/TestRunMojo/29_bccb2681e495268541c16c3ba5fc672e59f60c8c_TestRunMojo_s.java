 /**
  * Copyright (C) 2012 https://github.com/yelbota/haxe-maven-plugin
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
  */
 package com.yelbota.plugins.haxe;
 
 import com.yelbota.plugins.haxe.components.nativeProgram.NativeProgram;
 import com.yelbota.plugins.haxe.components.nativeProgram.NativeProgramException;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 
 /**
  * Run tests with `neko`.
  */
 @Mojo(name = "testRun", defaultPhase = LifecyclePhase.TEST)
 public final class TestRunMojo extends AbstractHaxeMojo {
 
     @Component(hint = "neko")
     private NativeProgram neko;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException
     {
         super.execute();
 
         try
         {
            neko.execute(project.getBuild().getFinalName() + "-test.n");
         }
         catch (NativeProgramException e)
         {
             throw new MojoFailureException("Test failed", e);
         }
     }
 }
