 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.huahinframework.emanager.rest.service;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wink.common.internal.utils.MediaTypeUtils;
 import org.apache.wink.common.model.multipart.InMultiPart;
 import org.apache.wink.common.model.multipart.InPart;
 import org.huahinframework.emanager.amazonaws.elasticmapreduce.Config;
 import org.huahinframework.emanager.amazonaws.elasticmapreduce.EMRProperties;
 import org.huahinframework.emanager.queue.QueueUtils;
 import org.huahinframework.emanager.rest.response.Response;
 import org.huahinframework.emanager.util.S3Utils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 
 /**
  *
  */
 @Path("/queue")
 public class QueueService {
     private static final Log log = LogFactory.getLog(QueueService.class);
 
     private static final String ARGUMENT_ERROR = "arguments error:[%s]";
 
     private static final int LIST_QUEUE = 0;
     private static final int LIST_RUNNING = 1;
 
     private static final int METHOD_PUT = 0;
     private static final int METHOD_POST = 1;
 
     private static final String CONTENT_DISPOSITION = "Content-Disposition";
     private static final String FORM_DATA_NAME_SCRIPT = "form-data; name=\"SCRIPT\"";
     private static final String FORM_DATA_NAME_MAPPER = "form-data; name=\"MAPPER\"";
     private static final String FORM_DATA_NAME_REDUCER = "form-data; name=\"REDUCER\"";
     private static final String FORM_DATA_NAME_JAR = "form-data; name=\"JAR\"";
     private static final String FORM_DATA_NAME_ARGUMENTS = "form-data; name=\"ARGUMENTS\"";
 
     private static final String STEP_NAME = "STEP_NAME";
     private static final String JAR = "jar";
     private static final String MAIN_CLASS = "mainClass";
     private static final String SCRIPT = "script";
     private static final String INPUT = "input";
     private static final String OUTPUT = "output";
     private static final String MAPPER = "mapper";
     private static final String REDUCER = "reducer";
     private static final String ARGUMENTS = "arguments";
     private static final String DELETE_ON_EXIT = "deleteOnExit";
 
     private AmazonS3 s3;
     private EMRProperties emrProperties;
 
     @Path("/list")
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public JSONArray list() {
         return map2Array(LIST_QUEUE);
     }
 
     @Path("/runnings")
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public JSONArray runnings() {
         return map2Array(LIST_RUNNING);
     }
 
     /**
      * @param type
      * @param map
      * @return JSONArray
      */
     @SuppressWarnings("unchecked")
     private JSONArray map2Array(int type) {
         JSONArray jsonArray = null;
 
         try {
             Map<String, Config> queueMap = null;
             switch (type) {
             case LIST_QUEUE:
                 queueMap = QueueUtils.list();
                 break;
             case LIST_RUNNING:
                 queueMap = QueueUtils.runnings();
                 break;
             default:
                 break;
             }
 
             List<JSONObject> l = new ArrayList<JSONObject>();
             for (Entry<String, Config> entry : queueMap.entrySet()) {
                 Config config = entry.getValue();
                 Map<String, Object> m = new HashMap<String, Object>();
                 m.put(Response.STEP_NAME, config.getName());
                 m.put(Response.TYPE, Config.getJobTypeName(config.getJobType()));
                 m.put(Response.CREATION_DATE, config.getDate().toString());
                 if (!isEmpty(config.getJobFlowId())) {
                     m.put(Response.JOB_FLOW, config.getJobFlowId());
                 }
                 l.add(new JSONObject(m));
             }
 
             jsonArray = new JSONArray(l);
         } catch (Exception e) {
             e.printStackTrace();
             log.error(e);
             Map<String, String> status = new HashMap<String, String>();
             status.put(Response.STATUS, e.getMessage());
             jsonArray = new JSONArray(Arrays.asList(status));
         }
 
         return jsonArray;
     }
 
     @Path("/describe/{" + STEP_NAME + "}")
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public JSONObject describe(@PathParam(STEP_NAME) String stepName) {
         Map<String, Object> status = new HashMap<String, Object>();
 
         try {
             Config config = QueueUtils.get(stepName);
             status.put(Response.STEP_NAME, config.getName());
             status.put(Response.STATUS, Config.getStatusName(config.getStatus()));
             status.put(Response.TYPE, Config.getJobTypeName(config.getJobType()));
             status.put(Response.CREATION_DATE, config.getDate().toString());
             status.put(Response.DELETE_ON_EXIT, config.isDeleteOnExit());
             if (!isEmpty(config.getJobFlowId())) {
                 status.put(Response.JOB_FLOW, config.getJobFlowId());
             }
             if (!isEmpty(config.getRun())) {
                 status.put(Response.SCRIPT, config.getRun());
             }
             if (!isEmpty(config.getMainClass())) {
                 status.put(Response.MAIN_CLASS, config.getMainClass());
             }
             if (config.getArgs() != null && config.getArgs().length >= 1) {
                 status.put(Response.ARGS, config.getArgs());
             }
             if (config.getArgMap() != null && !config.getArgMap().isEmpty()) {
                 for (Entry<String, String> entry : config.getArgMap().entrySet()) {
                     status.put(entry.getKey(), entry.getValue());
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             log.error(e);
             status.put(Response.STATUS, e.getMessage());
         }
 
         return new JSONObject(status);
     }
 
     @Path("/register/hive")
     @PUT
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaTypeUtils.MULTIPART_FORM_DATA)
     public JSONObject registerPutHive(InMultiPart inMP) {
         return registerScripts(METHOD_PUT, Config.JOB_TYPE_HIVE, inMP);
     }
 
     @Path("/register/pig")
     @PUT
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaTypeUtils.MULTIPART_FORM_DATA)
     public JSONObject registerPutPig(InMultiPart inMP) {
         return registerScripts(METHOD_PUT, Config.JOB_TYPE_PIG, inMP);
     }
 
     @Path("/register/hive")
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaTypeUtils.MULTIPART_FORM_DATA)
     public JSONObject registerPostHive(InMultiPart inMP) {
         return registerScripts(METHOD_POST, Config.JOB_TYPE_HIVE, inMP);
     }
 
     @Path("/register/pig")
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaTypeUtils.MULTIPART_FORM_DATA)
     public JSONObject registerPostPig(InMultiPart inMP) {
         return registerScripts(METHOD_POST, Config.JOB_TYPE_PIG, inMP);
     }
 
     /**
      * @param method
      * @param type
      * @param inMP
      * @return
      */
     private JSONObject registerScripts(int method, int type, InMultiPart inMP) {
         Map<String, String> status = new HashMap<String, String>();
         status.put(Response.STATUS, "accepted");
 
         File file = null;
         JSONObject argument = null;
         try {
             while (inMP.hasNext()) {
                 InPart part = inMP.next();
 
                 for (String s : part.getHeaders().get(CONTENT_DISPOSITION)) {
                     if (s.startsWith(FORM_DATA_NAME_SCRIPT)) {
                         file = File.createTempFile("huahin", ".tmp");
                         createTempFile(part.getInputStream(), file);
                         break;
                     } else if (s.startsWith(FORM_DATA_NAME_ARGUMENTS)) {
                         argument = createJSON(part.getInputStream());
                         break;
                     }
                 }
             }
 
             if (method == METHOD_POST) {
                 if (file == null) {
                     deleteFile(file);
                     status.put(Response.STATUS, "arguments error");
                     return new JSONObject(status);
                 }
             }
 
             if (argument == null) {
                 deleteFile(file);
                 status.put(Response.STATUS, "arguments error");
                 return new JSONObject(status);
             }
 
             if (argument.isNull(SCRIPT)) {
                 deleteFile(file);
                 status.put(Response.STATUS, String.format(ARGUMENT_ERROR, "script not found"));
                 return new JSONObject(status);
             }
 
             Config config = new Config();
             config.setJobType(type);
             config.setRun(argument.getString(SCRIPT));
 
             if (argument.has(ARGUMENTS)) {
                 JSONArray array = argument.getJSONArray(ARGUMENTS);
                 config.setArgs(new String[array.length()]);
                 for (int i = 0; i < array.length(); i++) {
                     config.getArgs()[i] = array.getString(i);
                 }
             }
 
             if (method == METHOD_POST) {
                 if (argument.has(DELETE_ON_EXIT)) {
                     config.setDeleteOnExit(argument.getBoolean(DELETE_ON_EXIT));
                 }
 
                 S3Utils.upload(s3, config.getRun(), file);
             }
 
             config = QueueUtils.registerQueue(config);
             status.put(Response.STEP_NAME, config.getName());
         } catch (Exception e) {
             e.printStackTrace();
             log.error(e);
             status.put(Response.STATUS, e.getMessage());
         }
 
         deleteFile(file);
 
         return new JSONObject(status);
     }
 
     @Path("/register/streaming")
     @PUT
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaTypeUtils.MULTIPART_FORM_DATA)
     public JSONObject registerPutStreaming(InMultiPart inMP) {
         return registerStreaming(METHOD_PUT, inMP);
     }
 
     @Path("/register/streaming")
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaTypeUtils.MULTIPART_FORM_DATA)
     public JSONObject registerPostStreaming(InMultiPart inMP) {
         return registerStreaming(METHOD_POST, inMP);
     }
 
     /**
      * @param inMP
      * @return JSONObject
      */
     public JSONObject registerStreaming(int method, InMultiPart inMP) {
         Map<String, String> status = new HashMap<String, String>();
         status.put(Response.STATUS, "accepted");
 
         File mapperFile = null;
         File reducerFile = null;
         JSONObject argument = null;
         try {
             while (inMP.hasNext()) {
                 InPart part = inMP.next();
 
                 for (String s : part.getHeaders().get(CONTENT_DISPOSITION)) {
                     if (s.startsWith(FORM_DATA_NAME_MAPPER)) {
                         mapperFile = File.createTempFile("huahin", ".tmp");
                         createTempFile(part.getInputStream(), mapperFile);
                         break;
                     } else if (s.startsWith(FORM_DATA_NAME_REDUCER)) {
                         reducerFile = File.createTempFile("huahin", ".tmp");
                         createTempFile(part.getInputStream(), reducerFile);
                         break;
                     } else if (s.startsWith(FORM_DATA_NAME_ARGUMENTS)) {
                         argument = createJSON(part.getInputStream());
                         break;
                     }
                 }
             }
 
             if (method == METHOD_POST) {
                 if (mapperFile == null || reducerFile == null) {
                     deleteFile(mapperFile);
                     deleteFile(reducerFile);
                     status.put(Response.STATUS, "arguments error");
                     return new JSONObject(status);
                 }
             }
 
             if (argument == null) {
                 deleteFile(mapperFile);
                 deleteFile(reducerFile);
                 status.put(Response.STATUS, "arguments error");
                 return new JSONObject(status);
             }
 
             if (argument.isNull(INPUT)) {
                 deleteFile(mapperFile);
                 deleteFile(reducerFile);
                 status.put(Response.STATUS, String.format(ARGUMENT_ERROR, INPUT + " not found"));
                 return new JSONObject(status);
             }
             if (argument.isNull(OUTPUT)) {
                 deleteFile(mapperFile);
                 deleteFile(reducerFile);
                 status.put(Response.STATUS, String.format(ARGUMENT_ERROR, OUTPUT + " not found"));
                 return new JSONObject(status);
             }
             if (argument.isNull(MAPPER)) {
                 deleteFile(mapperFile);
                 deleteFile(reducerFile);
                 status.put(Response.STATUS, String.format(ARGUMENT_ERROR, MAPPER + " not found"));
                 return new JSONObject(status);
             }
             if (argument.isNull(REDUCER)) {
                 deleteFile(mapperFile);
                 deleteFile(reducerFile);
                 status.put(Response.STATUS, String.format(ARGUMENT_ERROR, REDUCER + " not found"));
                 return new JSONObject(status);
             }
 
             Map<String, String> argMap = new HashMap<String, String>();
             for (String key : JSONObject.getNames(argument)) {
                 argMap.put(key, argument.getString(key));
             }
 
             Config config = new Config();
             config.setJobType(Config.JOB_TYPE_STREAMING);
             config.setArgMap(argMap);
 
             if (method == METHOD_POST) {
                 if (argument.has(DELETE_ON_EXIT)) {
                     argMap.remove(DELETE_ON_EXIT);
                     config.setDeleteOnExit(argument.getBoolean(DELETE_ON_EXIT));
                 }
 
                 S3Utils.upload(s3, argMap.get(MAPPER), mapperFile);
                 S3Utils.upload(s3, argMap.get(REDUCER), reducerFile);
             }
 
             config = QueueUtils.registerQueue(config);
             status.put(Response.STEP_NAME, config.getName());
         } catch (Exception e) {
             e.printStackTrace();
             log.error(e);
             status.put(Response.STATUS, e.getMessage());
         }
 
         deleteFile(mapperFile);
         deleteFile(reducerFile);
 
         return new JSONObject(status);
     }
 
     @Path("/register/customjar")
     @PUT
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaTypeUtils.MULTIPART_FORM_DATA)
     public JSONObject registerPutCustomJar(InMultiPart inMP) {
         return registerCustomJar(METHOD_PUT, inMP);
     }
 
     @Path("/register/customjar")
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaTypeUtils.MULTIPART_FORM_DATA)
     public JSONObject registerPostCustomJar(InMultiPart inMP) {
         return registerCustomJar(METHOD_POST, inMP);
     }
 
     /**
      * @param method
      * @param inMP
      * @return
      */
     public JSONObject registerCustomJar(int method, InMultiPart inMP) {
         Map<String, String> status = new HashMap<String, String>();
         status.put(Response.STATUS, "accepted");
 
         File file = null;
         JSONObject argument = null;
         try {
             while (inMP.hasNext()) {
                 InPart part = inMP.next();
 
                 for (String s : part.getHeaders().get(CONTENT_DISPOSITION)) {
                     if (s.startsWith(FORM_DATA_NAME_JAR)) {
                         file = File.createTempFile("huahin", ".tmp");
                         createTempFile(part.getInputStream(), file);
                         break;
                     } else if (s.startsWith(FORM_DATA_NAME_ARGUMENTS)) {
                         argument = createJSON(part.getInputStream());
                         break;
                     }
                 }
             }
 
             if (method == METHOD_POST) {
                 if (file == null) {
                     deleteFile(file);
                     status.put(Response.STATUS, "arguments error");
                     return new JSONObject(status);
                 }
             }
 
             if (argument == null) {
                 deleteFile(file);
                 status.put(Response.STATUS, "arguments error");
                 return new JSONObject(status);
             }
 
             if (argument.isNull(JAR)) {
                 deleteFile(file);
                 status.put(Response.STATUS, String.format(ARGUMENT_ERROR, JAR + " not found"));
                 return new JSONObject(status);
             }
 
             Config config = new Config();
             config.setJobType(Config.JOB_TYPE_CUSTOM_JAR);
             config.setRun(argument.getString(JAR));
 
             if (argument.has(MAIN_CLASS)) {
                 config.setMainClass(argument.getString(MAIN_CLASS));
             }
 
             if (argument.has(ARGUMENTS)) {
                 JSONArray array = argument.getJSONArray(ARGUMENTS);
                 config.setArgs(new String[array.length()]);
                 for (int i = 0; i < array.length(); i++) {
                     config.getArgs()[i] = array.getString(i);
                 }
             }
 
             if (method == METHOD_POST) {
                 if (argument.has(DELETE_ON_EXIT)) {
                     config.setDeleteOnExit(argument.getBoolean(DELETE_ON_EXIT));
                 }
 
                 S3Utils.upload(s3, config.getRun(), file);
             }
 
             config = QueueUtils.registerQueue(config);
             status.put(Response.STEP_NAME, config.getName());
         } catch (Exception e) {
             e.printStackTrace();
             log.error(e);
             status.put(Response.STATUS, e.getMessage());
         }
 
         deleteFile(file);
 
         return new JSONObject(status);
     }
 
     /**
      * @return {@link JSONObject}
      */
     @Path("/kill/{" + STEP_NAME + "}")
     @DELETE
     @Produces(MediaType.APPLICATION_JSON)
     public JSONObject kill(@PathParam(STEP_NAME) String stepName) {
         Map<String, String> status = new HashMap<String, String>();
         try {
             QueueUtils.removeQueue(stepName);
             status.put(Response.STATUS, "Killed queue " + stepName);
         } catch (Exception e) {
             e.printStackTrace();
             log.error(e);
             status.put(Response.STATUS, e.getMessage());
         }
 
         return new JSONObject(status);
     }
 
     /**
      * @param in
      * @param file
      * @throws IOException
      */
     private void createTempFile(InputStream in, File file) throws IOException {
         OutputStream out = new FileOutputStream(file);
 
         byte[] buf = new byte[1024];
         int len = 0;
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         out.close();
     }
 
     /**
      * @param in
      * @return JSONObject
      * @throws IOException
      * @throws JSONException
      */
     private JSONObject createJSON(InputStream in)
             throws IOException, JSONException {
         BufferedReader reader =
                 new BufferedReader(new InputStreamReader(in, "UTF-8"));
         StringBuilder sb = new StringBuilder();
         String str;
         while ((str = reader.readLine()) != null) {
             sb.append(str);
         }
 
         return new JSONObject(sb.toString());
     }
 
     /**
      * @param file
      */
     private void deleteFile(File file) {
         if (file != null) {
             file.delete();
         }
     }
 
     /**
      *
      */
     public void init() {
         s3 = new AmazonS3Client(
                 new BasicAWSCredentials(emrProperties.getAccessKey(),
                                         emrProperties.getSecretKey()));
        if (!isEmpty(emrProperties.getEndpoint())) {
            s3.setEndpoint(emrProperties.getEndpoint());
         }
     }
 
     /**
      *
      */
     private boolean isEmpty(String s) {
         return s == null || s.isEmpty();
     }
 
     /**
      * @param emrProperties the emrProperties to set
      */
     public void setEmrProperties(EMRProperties emrProperties) {
         this.emrProperties = emrProperties;
     }
 }
