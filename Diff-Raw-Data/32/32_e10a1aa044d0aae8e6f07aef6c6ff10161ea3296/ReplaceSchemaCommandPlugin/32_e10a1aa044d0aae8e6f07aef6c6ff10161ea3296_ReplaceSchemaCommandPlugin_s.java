 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dbflute.maven.plugin;
 
 import java.io.IOException;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.seasar.dbflute.maven.plugin.command.CommandExecutor;
 import org.seasar.dbflute.maven.plugin.util.LogUtil;
 
 /**
  * ReplaceSchemaCommandPlugin provides replace-schema goal to run replace-schema.[sh|bat].
  * 
  * @goal replace-schema
  * 
  * @author shinsuke
  *
  */
 public class ReplaceSchemaCommandPlugin extends CommandPlugin {
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         LogUtil.init(getLog());

        System.out
                .println("Database will be initialized. Are you ready? (y or n)");
         int input;
        try {
            input = System.in.read();
        } catch (IOException e) {
            throw new MojoExecutionException("I/O error.", e);
         }
         if (input == 'y' || input == 'Y') {
             CommandExecutor creator = new CommandExecutor(this);
             creator.environment.put("answer", "y");
             creator.execute("replace-schema");
         }
     }
 
 }
