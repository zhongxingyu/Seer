 /**
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.tools.recess;
 
 import org.apache.commons.io.FileUtils;
 import org.mozilla.javascript.*;
 import org.mozilla.javascript.tools.shell.Global;
 import org.mule.tools.rhinodo.api.ConsoleFactory;
 import org.mule.tools.rhinodo.api.NodeModule;
 import org.mule.tools.rhinodo.api.Runnable;
 import org.mule.tools.rhinodo.impl.JavascriptRunner;
 import org.mule.tools.rhinodo.impl.NodeModuleFactoryImpl;
 import org.mule.tools.rhinodo.impl.NodeModuleImpl;
 import org.mule.tools.rhinodo.rhino.RhinoHelper;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 class Recess implements Runnable {
     private NodeModuleImpl recess;
     private JavascriptRunner javascriptRunner;
     private Map<String, Object> config;
     private Object[] files;
     private final String outputFile;
     private boolean merge;
     private String outputDirectory;
     RhinoHelper rhinoHelper;
     private ConsoleFactory consoleFactory;
     private Set<String> alreadyWritten;
     private Context ctx;
     private Global global;
 
     public Recess(ConsoleFactory consoleFactory, Object[] files, Map<String, Object> config, String outputFile, boolean merge, String destDir, String outputDirectory) {
         this.rhinoHelper = new RhinoHelper();
         this.alreadyWritten = new HashSet<String>();
 
         if (merge && outputFile == null) {
             throw new IllegalArgumentException("outputFile cannot be null");
         }
 
         new File(destDir).mkdirs();
 
         if ( config == null ) {
             throw new RuntimeException("config cannot be null");
         }
         this.outputDirectory = outputDirectory;
         this.files = files;
         this.config = config;
         this.outputFile = outputFile;
         this.merge = merge;
         this.consoleFactory = consoleFactory;
 
         recess = NodeModuleImpl.fromJar(this.getClass(), "META-INF/recess", destDir);
 
         List<? extends NodeModule> nodeModuleList = Arrays.asList(
                 NodeModuleImpl.fromJar(this.getClass(), "META-INF/abbrev", destDir),
                 NodeModuleImpl.fromJar(this.getClass(), "META-INF/colors", destDir),
                 NodeModuleImpl.fromJar(this.getClass(), "META-INF/less", destDir),
                 NodeModuleImpl.fromJar(this.getClass(), "META-INF/nopt", destDir),
                 NodeModuleImpl.fromJar(this.getClass(), "META-INF/underscore", destDir),
                 NodeModuleImpl.fromJar(this.getClass(), "META-INF/watch", destDir),
                 recess);
 
         javascriptRunner = JavascriptRunner.withConsoleFactory(consoleFactory, new NodeModuleFactoryImpl(nodeModuleList), this, destDir);
     }
 
     public void run() {
         javascriptRunner.run();
     }
 
     @Override
     public void executeJavascript(final Context ctx, final Global global) {
         this.ctx = ctx;
         this.global = global;
         global.put("__dirname", global, Context.toString(recess.getPath()));
 
         Function require = (Function)global.get("require", global);
         Object result = require.call(ctx,global,global,new String [] {"recess"});
 
         NativeObject nobj = rhinoHelper.mapToNativeObject(config);
 
         Function recess = (Function) Context.jsToJava(result, Function.class);
         BaseFunction recessCallback = new RecessCallback(ctx, global);
         recess.call(ctx, global, global, new Object[]{ctx.newArray(global, files), nobj, recessCallback});
 
     }
 
     private static class RecessResult {
         private String path;
 
         public String getPath() {
             return path;
         }
 
         public NativeArray getOutput() {
             return output;
         }
 
         public String getFileNameWithExtension() {
             return fileNameWithExtension;
         }
 
         public String getFileNameWithoutExtension() {
             return fileNameWithoutExtension;
         }
 
         private NativeArray output;
         private String fileNameWithExtension;
         private String fileNameWithoutExtension;
 
         private RecessResult() {}
 
         public static RecessResult fromNativeObjectResult(Object result) {
             if (result == null || !(result instanceof NativeObject)) {
                 return null;
             }
 
             NativeObject resultNativeObject = (NativeObject) result;
 
             RecessResult recessResult = new RecessResult();
 
             recessResult.output = (NativeArray) resultNativeObject.get("output", resultNativeObject);
             recessResult.path = (String) resultNativeObject.get("path", resultNativeObject);
 
             recessResult.fileNameWithExtension = recessResult.path.substring(recessResult.path.lastIndexOf(File.separator));
             if ( recessResult.fileNameWithExtension.endsWith(".css") || recessResult.fileNameWithExtension.endsWith(".less")) {
                 recessResult.fileNameWithoutExtension = recessResult.fileNameWithExtension.substring(0, recessResult.fileNameWithExtension.lastIndexOf("."));
             }
 
             return recessResult;
         }
     }
 
     private void writeOutputResult(RecessResult result, String prefix, boolean merge) {
         File outputFile;
         if ( merge ) {
             outputFile = new File(this.outputFile);
         } else {
             outputFile = new File(outputDirectory, prefix + ".css");
         }
 
         if (result.getOutput().size() > 0 && result.getOutput().get(0) != null) {
             try {
                 String path = outputFile.getPath();
                 FileUtils.write(outputFile, result.getOutput().get(0).toString(), alreadyWritten.contains(path));
                 alreadyWritten.add(path);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         } else {
             log(ctx, global, "Not saving result as output was null or invalid");
         }
     }
 
     private void log(Context ctx, Global global, String stringToPrint) {
         Scriptable console = javascriptRunner.getConsole();
         Function function = (Function) console.get("log", console);
         function.call(ctx, global, console, new Object[]{stringToPrint});
     }
 
     private class RecessCallback extends BaseFunction {
         private final Context ctx;
         private final Global global;
 
         public RecessCallback(Context ctx, Global global) {
             this.ctx = ctx;
             this.global = global;
         }
 
         @Override
         public Object call(Context context, Scriptable scriptable, Scriptable thisObject, Object[] objects) {
             Scriptable err = objects.length > 0 ? (Scriptable) objects[0] : null;
             Scriptable result = objects.length > 1 ? (Scriptable) objects[1] : null;
             callback(err, result);
             return Undefined.instance;
         }
 
         private void callback(Scriptable error, Scriptable result) {
             if (error != null && error != Undefined.instance) {
                 if (error instanceof NativeArray) {
                     NativeArray errorAsNativeArray = (NativeArray) error;
                     for (Object o : errorAsNativeArray) {
                         if (o instanceof NativeObject) {
                             NativeObject nativeObject = (NativeObject) o;
                             String stringToPrint = "Error: %s in file: %s line: %d column: %d";
                             stringToPrint = String.format(stringToPrint, nativeObject.get("message"),
                                     nativeObject.get("filename"), ((Double) nativeObject.get("line")).longValue(),
                                     ((Double) nativeObject.get("column")).longValue());
                             Recess.this.log(ctx, global, stringToPrint);
 
                         }
                     }
                 }
                 throw new RuntimeException(error.toString());
             }
 
             if (result instanceof NativeArray) {
                 HashMap<String, RecessResult> recessResults = new HashMap<String, RecessResult>();
                 NativeArray resultAsNativeArray = (NativeArray) result;
                 for (Object o : resultAsNativeArray) {
                     RecessResult recessResult = RecessResult.fromNativeObjectResult(o);
                     if (recessResults.containsKey(recessResult.getFileNameWithoutExtension())) {
                         recessResults.put(recessResult.getFileNameWithExtension(), recessResult);
                         RecessResult oldRecessResult = recessResults.get(recessResult.getFileNameWithoutExtension());
                         recessResults.put(oldRecessResult.getFileNameWithExtension(), oldRecessResult);
                     } else {
                         recessResults.put(recessResult.getFileNameWithoutExtension(), recessResult);
                     }
                 }
 
                 for (Map.Entry<String, RecessResult> filePrefixAndrecessResult : recessResults.entrySet()) {
                     writeOutputResult(filePrefixAndrecessResult.getValue(), filePrefixAndrecessResult.getKey(), merge);
                 }
 
             } else {
                 RecessResult result1 = RecessResult.fromNativeObjectResult(result);
                writeOutputResult(result1, result1.getFileNameWithoutExtension(), merge);
             }
         }
 
     }
 }
