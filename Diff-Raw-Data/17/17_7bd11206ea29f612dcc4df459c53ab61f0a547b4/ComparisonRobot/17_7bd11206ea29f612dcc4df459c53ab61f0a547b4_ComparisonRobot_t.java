 // 
 // Copyright (c) 2003-2005, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
 // All rights reserved. 
 // 
 // Redistribution and use in source and binary forms, with or without modification,  
 // are permitted provided that the following conditions are met: 
 //  
 // * Redistributions of source code must retain the above copyright notice,  
 //	 this list of conditions and the following disclaimer. 
 // * Redistributions in binary form must reproduce the above copyright notice,  
 //	 this list of conditions and the following disclaimer in the documentation  
 //	 and/or other materials provided with the distribution. 
 // * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
 //	 nor the names of its contributors may be used to endorse or promote products  
 //	 derived from this software without specific prior written permission. 
 // 
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
 // AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 // IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 // OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
 // POSSIBILITY OF SUCH DAMAGE. 
 // 
 package net.cyklotron.tools;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.httpclient.HttpClient;
 
 /**
  * 
  * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ComparisonRobot.java,v 1.3 2005-03-30 13:24:49 rafal Exp $
  */
 public class ComparisonRobot
 {
     private File workDir;
     
     private String oldUrl;
     
     private String newUrl;
     
     private List<Replacement> oldPatterns;
     
     private List<Replacement> newPatterns;
     
     private HttpClient httpClient;
 
     public static void main(String[] args)
         throws Exception
     {
         File baseDir = new File(System.getProperty("user.dir"));
         ComparisonRobot robot = new ComparisonRobot(baseDir, args[0]);
         robot.run();
     }
     
     public ComparisonRobot(File baseDir, String configPath)
         throws IOException
     {
         Properties properties = Utils.loadProperties(new File(baseDir, configPath));
         this.workDir = new File(baseDir, properties.getProperty("workdir"));

         this.oldUrl = properties.getProperty("url.old");
         this.newUrl = properties.getProperty("url.new");

         this.oldPatterns = Replacement.parse(new File(baseDir, properties
             .getProperty("patterns.old")));
         this.newPatterns = Replacement.parse(new File(baseDir, properties
             .getProperty("patterns.new")));
        
        oldPatterns.add(new Replacement(properties.getProperty("context.old"), "/context/"));
        newPatterns.add(new Replacement(properties.getProperty("context.new"), "/context/"));

         this.httpClient = new HttpClient();
     }
     
     public void run()
         throws Exception
     {
         System.out.println("Start old application on "+oldUrl+" and press enter when ready");
         keypress();
         run(false);
         System.out.println("Start new application on "+newUrl+" and press enter when ready");
         keypress();
         run(true);
         System.out.println("complete.");
     }
     
     private void write(String s)
     {
         System.out.println(s);
     }
     
     private void keypress()
         throws IOException
     {
         System.in.read();
         while(System.in.available() > 0)
         {
             System.in.read();
         }
     }
     
     private void run(boolean newApp)
         throws Exception
     {
         System.out.println("loading listing");
         String listing = loadListing(newApp);
         List<String> ids = Utils.tokenize(listing, "\n");
         System.out.println("got listing of "+ids.size()+" pages");
         int counter = 0;
         for(String id : ids)
         {
             loadPage(newApp, id);
             counter++;
         }
     }
     
     private String loadListing(boolean newApp)
         throws Exception
     {
         URL url;
         if(newApp)
         {
            url = new URL(newUrl + "/view/structure.PublicNodes?text&action=i18n.SetLocale&locale=pl_PL");
         }
         else
         {
             url = new URL(oldUrl + "/app/cms/view/structure,PublicNodes?text");
         }
         return Utils.loadUrl(url, httpClient);
     }
     
     private static final String OUTPUT_ENCODING = "UTF-8";
     
     private void loadPage(boolean newApp, String x)
         throws Exception
     {
         URL url;
         File outFile;
         List<Replacement> patterns;
         if(newApp)
         {
             url = new URL(newUrl + "?x=" + x);
             outFile = new File(workDir, "/new/" + x + ".html");
             patterns = newPatterns;
         }
         else
         {
            url = new URL(oldUrl + "/app/cms/x/" + x);
             outFile = new File(workDir, "/old/" + x + ".html");
             patterns = oldPatterns;
         }
         if(!outFile.getParentFile().exists())
         {
             outFile.getParentFile().mkdirs();
         }
         String content = Utils.loadUrl(url, httpClient);
         content = Replacement.apply(content, patterns);
         Utils.writeFile(outFile, content, OUTPUT_ENCODING);
     }
 }
