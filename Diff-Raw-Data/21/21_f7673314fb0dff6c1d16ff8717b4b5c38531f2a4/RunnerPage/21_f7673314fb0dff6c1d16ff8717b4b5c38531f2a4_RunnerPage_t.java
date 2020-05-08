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
 
 import static com.google.jstestdriver.server.handlers.pages.SlavePageRequest.LOAD_TYPE;
 import static com.google.jstestdriver.server.handlers.pages.SlavePageRequest.TESTCASE_ID;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.google.jstestdriver.util.HtmlWriter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 
 /**
  * Runner page 
  *
  * @author corbinrsmith@gmail.com (Cory Smith)
  */
 public class RunnerPage implements Page {
   private static final Logger logger = LoggerFactory.getLogger(RunnerPage.class);
   private final TestFileUtil testFileUtil;
   private final Boolean debug;
 
   @Inject
   RunnerPage(TestFileUtil testFileUtil, @Named("debug") Boolean debug) {
     this.testFileUtil = testFileUtil;
     this.debug = debug;
   }
 
   @Override
   public void render(HtmlWriter writer, SlavePageRequest request) throws IOException {
    logger.debug("Debug: " + debug);
     writer.startHead()
       .writeTitle("Console Runner")
       .writeScript("var start = new Date().getTime();")
       .writeExternalScript("/static/lib/json2.js")
       .writeExternalScript("/static/lib/json_sans_eval.js")
       .writeExternalScript("/static/jstestdrivernamespace.js")
       .writeExternalScript("/static/lib/jquery-min.js")
       .writeExternalScript("/static/runner.js")
       .writeScript("jstestdriver.runConfig = {'debug':" + debug + "};")
       .writeScript(
           "jstestdriver.console = new jstestdriver.Console();\n" +
           "jstestdriver.runner = jstestdriver.config.createRunner(\n" +
           "    jstestdriver.config.createExecutor,\n" +
           "    jstestdriver.plugins.createPausingRunTestLoop(\n" +
           "        jstestdriver.TIMEOUT,\n" +
           "        jstestdriver.now,\n" +
           "        jstestdriver.setTimeout));")
       .writeScript("jstestdriver.log('runner loaded in ' + (new Date().getTime() - start));start = new Date().getTime();");
 
     
     if (!"load".equals(request.getParameter(LOAD_TYPE))) {
       testFileUtil.writeTestFiles(writer, request.getParameter(TESTCASE_ID));
     }
 
     
     writer.writeScript("jstestdriver.log('test loaded in ' + (new Date().getTime() - start));jstestdriver.jQuery(window).load(function(){jstestdriver.runner.listen(" +
         "jstestdriver.manualResourceTracker.getResults());});");
     writer.finishHead()
       .startBody()
       .finishBody()
       .flush();
     if (request.getBrowser() != null) {
       logger.debug("finished runner page for " + request.getBrowser().getId());
     }
   }
 }
