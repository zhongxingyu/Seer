 // Copyright 2007-2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.server;
 
 import org.joe_e.array.ByteArray;
 import org.joe_e.charset.UTF8;
 import org.waterken.net.http.HTTPD;
 import org.waterken.project.Project;
 import org.waterken.remote.http.VatInitializer;
 import org.waterken.uri.Header;
 import org.waterken.uri.Hostname;
 
 /**
  * Command line program to create a new database.
  */
 /* package */ final class
 Spawn {
     private Spawn() {}
     
     /**
      * @param args  command line arguments
      */
     static public void
     main(final String[] args) throws Exception {
 
         // extract the arguments
         if (args.length < 2) {
             System.err.println("Creates a new persistent object folder.");
             System.err.println("use: java -jar spawn.jar " +
                 "<project-name> <maker-typename> <database-label>");
             System.exit(-1);
             return;
         }
         final String project = args[0];
         final String typename = args[1];
         final String label = 2 < args.length ? args[2] : null;
         
         /*
          * The command line arguments are assumed to be syntactically valid. 
          */
         final StringBuilder json = new StringBuilder();
         json.append("[ ");
         for (int i = 3; i < args.length; i += 1) {
         	if (i != 3) { json.append(", "); }
         	final String arg = args[i];
         	if ("null".equals(arg) || "false".equals(arg)|| "true".equals(arg)){
         		json.append(arg);
         	} else if (arg.startsWith("@")) {
         		json.append("{ \"@\" : \"");
         		json.append(arg.substring(1));
         		json.append("\" }");
         	} else if (arg.length() != 0 &&
         			   "1234567890-".indexOf(arg.charAt(0)) != -1) {
         		json.append(arg);
         	} else if (arg.startsWith("\"")) {
         		json.append(arg);
         	} else {
         		json.append("\"");
         		json.append(arg);
         		json.append("\"");
         	}
         }
         json.append(" ]");
         final ByteArray body = ByteArray.array(UTF8.encode(json.toString()));
 
         // load configured values
         final String vatURIPathPrefix= Settings.config.read("vatURIPathPrefix");
 
         // determine the local address
         final String here;
         final String host = Proxy.credentials.getHostname();
         if (Header.equivalent("localhost", host)) {
         	final HTTPD http = Settings.config.read("http");
             final int portN = http.port;
             final String port = 80 == portN ? "" : ":" + portN;
             here = "http://localhost" + port + "/" + vatURIPathPrefix;
         } else {
             Hostname.vet(host);
             final HTTPD https = Settings.config.read("https");
             final int portN = https.port;
             final String port = 443 == portN ? "" : ":" + portN;
             here = "https://" + host + port + "/" + vatURIPathPrefix;
         }
         
         // create the database
         final ClassLoader code = Project.connect(project);
         final Class<?> maker = code.loadClass(typename);
        final String r = VatInitializer.create(Settings.db(""), project,
                                               here, label, maker, body);
        System.out.println(r);
     }
 }
