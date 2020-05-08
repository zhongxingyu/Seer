 /*
  * Copyright 2010 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.server.handlers.pages;
 
 import java.io.IOException;
 
 import com.google.inject.Inject;
 import com.google.jstestdriver.util.HtmlWriter;
 
 /**
  * Runner page 
  *
  * @author corbinrsmith@gmail.com (Cory Smith)
  */
 public class RunnerPage implements Page {
   private final TestFileUtil testFileUtil;
 
   @Inject
   RunnerPage(TestFileUtil testFileUtil) {
     this.testFileUtil = testFileUtil;
   }
 
   @Override
   public void render(HtmlWriter writer, SlavePageRequest request) throws IOException {
     writer.startHead()
       .writeTitle("Console Runner")
       .writeExternalScript("/static/lib/json2.js")
       .writeExternalScript("/static/lib/json_sans_eval.js")
      .writeExternalScript("/static/jstestdrivernamespace.js")
       .writeExternalScript("/static/lib/jquery-min.js")
       .writeExternalScript("/static/runner.js")
       .writeScript(
           "jstestdriver.console = new jstestdriver.Console();\n" +
           "jstestdriver.runner = jstestdriver.config.createRunner(\n" +
           "    jstestdriver.config.createExecutor,\n" +
           "    jstestdriver.plugins.createPausingRunTestLoop(\n" +
           "        jstestdriver.TIMEOUT,\n" +
           "        jstestdriver.now,\n" +
           "        jstestdriver.setTimeout));");
 
     testFileUtil.writeTestFiles(writer);
 
     writer.writeScript("jstestdriver.jQuery(window).load(function(){jstestdriver.runner.listen(" +
         "jstestdriver.manualResourceTracker.getResults());});");
     writer.finishHead()
       .startBody()
       .finishBody()
       .flush();
   }
 }
