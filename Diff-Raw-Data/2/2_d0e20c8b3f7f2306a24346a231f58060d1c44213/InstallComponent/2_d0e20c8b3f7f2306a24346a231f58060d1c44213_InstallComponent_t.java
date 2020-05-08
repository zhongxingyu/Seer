 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.web.controller;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
 import org.apache.servicemix.jbi.util.FileUtil;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 public class InstallComponent extends SimpleFormController {
 
     private final AdminCommandsServiceMBean adminCommandsService;
     
     public InstallComponent(AdminCommandsServiceMBean adminCommandsService) {
         this.adminCommandsService = adminCommandsService;
     }
     
     protected void doSubmitAction(Object command) throws Exception {
         // cast the bean
         FileUploadBean bean = (FileUploadBean) command;
         // let's see if there's content there
         byte[] file = bean.getFile();
         if (file == null) {
             // hmm, that's strange, the user did not upload anything
         }
         File f = File.createTempFile("smx-comp", ".zip");
         try {
             FileUtil.copyInputStream(new ByteArrayInputStream(file), new FileOutputStream(f));
            String result = adminCommandsService.installComponent(f.toURL().toString(), null, false);
             System.err.println(result);
         } finally {
             f.delete();
         }
     }
     
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
         // to actually be able to convert Multipart instance to byte[]
         // we have to register a custom editor
         binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
         // now Spring knows how to handle multipart object and convert them
     }
 
 }
