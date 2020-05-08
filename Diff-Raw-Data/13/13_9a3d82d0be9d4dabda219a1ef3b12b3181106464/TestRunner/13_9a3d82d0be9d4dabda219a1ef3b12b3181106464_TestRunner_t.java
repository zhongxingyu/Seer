 /*
  * Copyright 2009 Google Inc.
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
 package com.google.jstestdriver.idea;
 
 import com.google.inject.Module;
 import com.google.jstestdriver.*;
 
 import java.io.*;
 import java.io.FileReader;
 import java.net.ConnectException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.Arrays;
 import java.util.List;
 
 import static java.net.InetAddress.getLocalHost;
 
 /**
  * Run JSTD in its own process, and stream messages via a socket to a server that lives in the IDEA process,
  * which will update the UI with our results.
  *
  * @author alexeagle@google.com (Alex Eagle)
  */
 public class TestRunner {
   private final String serverURL;
   private final String settingsFile;
   private final File baseDirectory;
   private final ObjectOutputStream out;
   private static final int TIMEOUT_MILLIS = 2 * 1000; // 2 seconds
   public static final int RETRIES = 5;
 
   public TestRunner(String serverURL, String settingsFile, File baseDirectory,
                     ObjectOutputStream out) {
     this.serverURL = serverURL;
     this.settingsFile = settingsFile;
     this.baseDirectory = baseDirectory;
     this.out = out;
   }
 
   public void execute() throws InterruptedException {
     ResponseStreamFactory responseStreamFactory = createResponseStreamFactory();
     final ActionRunner dryRunRunner =
         makeActionBuilder(responseStreamFactory)
             .dryRunFor(Arrays.asList("all")).build();
     final ActionRunner testRunner =
         makeActionBuilder(responseStreamFactory)
             .addAllTests().build();
     //TODO(alexeagle): support client-side reset action
     final ActionRunner resetRunner =
         makeActionBuilder(responseStreamFactory)
             .resetBrowsers().build();
 
     dryRunRunner.runActions();
     testRunner.runActions();
   }
 
   private ResponseStreamFactory createResponseStreamFactory() {
     return new ResponseStreamFactory() {
       public ResponseStream getRunTestsActionResponseStream(String s) {
         return new ResponseStream() {
           public void stream(Response response) {
             for (TestResult testResult : new TestResultGenerator().getTestResults(response)) {
               try {
                 out.writeObject(TestResultProtocolMessage.fromTestResult(testResult));
               } catch (IOException e) {
                 throw new RuntimeException(e);
               }
             }
           }
 
           public void finish() {
           }
         };
       }
 
       public ResponseStream getDryRunActionResponseStream() {
         return new ResponseStream() {
           public void stream(Response response) {
             if (response.getResponseType() == Response.ResponseType.FILE_LOAD_RESULT) {
               // TODO process it?
 //              new Gson().fromJson(response.getResponse(), LoadedFiles.class);
               return; // for now, don't send back to IDEA
             }
             BrowserInfo browser = response.getBrowser();
             DryRunInfo dryRunInfo = DryRunInfo.fromJson(response);
             for (String testName : dryRunInfo.getTestNames()) {
               try {
                 out.writeObject(TestResultProtocolMessage.fromDryRun(testName, browser));
               } catch (IOException e) {
                 throw new RuntimeException(e);
               }
             }
           }
 
           public void finish() {
           }
         };
       }
 
       public ResponseStream getEvalActionResponseStream() {
         return null;
       }
 
       public ResponseStream getResetActionResponseStream() {
         return null;
       }
     };
   }
 
   private IDEPluginActionBuilder makeActionBuilder(ResponseStreamFactory responseStreamFactory) {
     try {
       FileReader fileReader = new FileReader(settingsFile);
       ConfigurationParser configurationParser = new ConfigurationParser(baseDirectory, fileReader,
           new DefaultPathRewriter());
       IDEPluginActionBuilder builder =
           new IDEPluginActionBuilder(configurationParser, serverURL, responseStreamFactory, baseDirectory);
       for (Module module : new PluginLoader().load(parsePluginsFromConfFile())) {
         builder.install(module);
       }
       return builder;
     } catch (FileNotFoundException e) {
       throw new RuntimeException("Failed to read settings file " + settingsFile, e);
     }
   }
 
   private List<Plugin> parsePluginsFromConfFile() throws FileNotFoundException {
     ConfigurationParser parser =
         new ConfigurationParser(baseDirectory, new FileReader(settingsFile),
             new DefaultPathRewriter());
     parser.parse();
     return parser.getPlugins();
   }
 
   public static void main(String[] args) throws Exception {
     String serverURL = args[0];
     String settingsFile = args[1];
     int port = Integer.parseInt(args[2]);
     try {
       Socket socket = connectToServer(port);
       ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
       new TestRunner(serverURL, settingsFile, new File(""), outputStream).execute();
     } catch (RuntimeException ex) {
       if (ex.getCause() != null && ex.getCause() instanceof ConnectException) {
         System.err.println("\nCould not connect to a JSTD server running at " + serverURL + "\n" +
             "Check that the server is running.");
       } else {
         System.err.println("JSTestDriver crashed!");
         throw ex;
       }
     }
   }
 
   private static Socket connectToServer(int port) throws IOException {
     int retries = RETRIES;
     Socket socket = null;
     do {
       try {
         socket = new Socket();
         socket.connect(new InetSocketAddress(getLocalHost(), port), TIMEOUT_MILLIS);
         break;
       } catch (SocketException e) {
         retries--;
       }
     } while (retries > 0);
     return socket;
   }
 
 }
