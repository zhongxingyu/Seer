 package com.razie.pub.http.test;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Properties;
 
 import com.razie.pub.base.ActionItem;
 import com.razie.pub.base.ActionToInvoke;
 import com.razie.pub.base.ServiceActionToInvoke;
 import com.razie.pub.base.data.HtmlRenderUtils;
 import com.razie.pub.base.data.HttpUtils;
 import com.razie.pub.base.data.HtmlRenderUtils.HtmlTheme;
 import com.razie.pub.draw.DrawStream;
 import com.razie.pub.draw.DrawTable;
 import com.razie.pub.draw.widgets.NavLink;
 import com.razie.pub.http.AuthException;
 import com.razie.pub.http.LightCmdGET;
 import com.razie.pub.http.LightServer;
 import com.razie.pub.http.MyServerSocket;
 import com.razie.pub.lightsoa.HttpSoaBinding;
 import com.razie.pub.lightsoa.SoaMethod;
 import com.razie.pub.lightsoa.SoaStreamable;
 import com.razie.pub.resources.RazIconRes;
 import com.razie.pub.resources.RazIcons;
 
 /**
  * a simple no threads FILE server - streams to browser as files are found...try it on a large
  * folder and put a sleep here to see the effect on slow operations
  * 
  * @author razvanc99
  * 
  */
 public class SimpleFileServer {
 
     @SoaMethod(descr = "browse folder", args = { "folderpath" })
     @SoaStreamable
     public void browse(DrawStream out, String folderpath) {
         folderpath = HttpUtils.fromUrlEncodedString(folderpath);
 
         File[] dirs = new File(folderpath).listFiles();
         if (dirs == null) {
             return;
         }
 
         ActionItem BROWSE = new ActionItem("browse", RazIcons.FOLDER);
         ActionItem SERVE = new ActionItem("serve", RazIcons.FILE);
 
         DrawTable list = new DrawTable(0, 2);
         list.packed = true;
         list.horizAlign = DrawTable.HorizAlign.LEFT;
 
         out.open(list);
 
         for (File f : dirs) {
             if (f.isDirectory()) {
                 BROWSE.label = "DIR  " + f.getAbsolutePath();
                 // the table will write stuff out only when a row is full - gotta clone the
                 // changeables
                 ActionToInvoke ai = new ServiceActionToInvoke("lightsoa/service", BROWSE.clone(),
                         "folderpath", f.getAbsolutePath());
                 ai.drawTiny = true;
 
                 list.write(ai);
                 list.write(new NavLink(ai));
             } else {
                 SERVE.label = "FILE " + f.getAbsolutePath();
                 // this is a trick so explorer recognizes the filename
                 SERVE.name = "serve/" + f.getName();
                 // the table will write stuff out only when a row is full - gotta clone the
                 // changeables
                 ActionToInvoke ai = new ActionToInvoke(SERVE.clone(), "filepath", f.getAbsolutePath());
                 ai.drawTiny = true;
 
                 list.write(ai);
                 list.write(new NavLink(ai));
             }
         }
 
         out.close(list);
     }
 
     @SoaMethod(descr = "die")
     public String die() {
         server.shutdown();
         return "dying...";
     }
 
     static LightServer      server;
     static SimpleFileServer singleton;
     static LightCmdGET      cmdGET = new SimpleFileServerCmd();
 
     public static void main(String[] argv) throws IOException {
         RazIconRes.init();
         start(TestLightServer.PORT);
     }
 
     public static void start(int port) {
         // stuff to set before you start the server
         HtmlRenderUtils.setTheme(new DarkTheme());
 
         singleton = new SimpleFileServer();
 
         server = new SampleNoThreadsServer(port);
         server.registerCmdListener(cmdGET);
 
         HttpSoaBinding soa = new HttpSoaBinding(singleton, "service");
         cmdGET.registerSoa(soa);
 
         // start the server thread...
         server.run();
     }
 
     /**
      * the default cmdGET does not serve files - this one will serve. use the classpathserver to
      * serve stylesheet
      */
     static class SimpleFileServerCmd extends SampleWebServer.SimpleClasspathServer {
         /**
          * will serve any file on the harddrive
          */
         @Override
         protected URL findUrlToServe(MyServerSocket socket, String path, Properties parms)
                 throws MalformedURLException, AuthException {
 
             if (path.equals("/") || path.equals("")) {
                 path = "/classpath/com/razie/pub/hframe/http/test/index-files.html";
             }
 
             if (path.startsWith("/serve")) {
                 String filenm = parms.getProperty("filepath");
 
                 URL url = new File(filenm).toURL();
 
                 return url;
             }
 
             return super.findUrlToServe(socket, path, parms);
         }
     }
 
     /** a simple, css-based theme to be used by the sample server */
     static class DarkTheme extends HtmlTheme {
         static String[] tags = {
                                      "<head><link rel=\"stylesheet\" type=\"text/css\" href=\"/classpath/com/razie/pub/hframe/http/test/style.css\" /></head><body link=\"yellow\" vlink=\"yellow\">",
                                      "</body>", "<html>", "</html>" };
 
         public String get(int what) {
             return tags[what];
         }
     }
 }
