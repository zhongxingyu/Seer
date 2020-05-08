 package com.adc.pdfer;
 
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import com.adc.pdfer.stripes.BaseAction;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URLDecoder;
 import net.sourceforge.stripes.action.StreamingResolution;
 import java.util.ArrayList;
 import org.apache.log4j.Logger;
 
 /*
  * reverted to using file output. streaming output was causing errors
  * for larger filestreams perhaps this is due to our implementation,
  * but it seems to be an error in the wkhtmltopdf executable.
  */
 @UrlBinding(value = "/fit")
 public class FitAction extends BaseAction {
 
     private static final Logger log = Logger.getLogger(FitAction.class);
     String tempfile = "/tmp/temp.pdf";
     private String url;
     private String html_head = "<html><head></head><body>";
     private String html_foot = "</body></html>";
     private String html_inner;
     private String file = "report";
     //0.9.9 options
     //private String options = "--ignore-load-errors --disable-javascript";
     //0.10.0rc1 options
     private String options = "--encoding UTF-8";
 
     @DefaultHandler
     public Resolution pdf() {
 
         /*
         JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker("pdfer", "UA-xxxxxx-x");
         FocusPoint focusPoint = new FocusPoint("fit");
         tracker.trackAsynchronously(focusPoint);
          */
         ArrayList<String> cmds = new ArrayList();
         cmds.add("/bin/sh");
         cmds.add("-c");
         Process p = null;
 
         log.debug("URL : " + url);
         //log.debug("html_head : " + html_head);
         log.debug("html_inner : " + html_inner);
         //log.debug("html_foot : " + html_foot);
 
         if(options == null)options = "--encoding UTF-8";
         if(html_head == null)html_head = "<html><head></head><body>";
         if(html_foot == null)html_foot = "</body></html>";
 
         try {
             if(options.contains(";") || options.contains("&&")){
                 throw new RuntimeException("Options may contain hack attempt, check your options for ';' or '&&'");
             }
 
             if (getHtml_inner() != null && getHtml_inner().length() > 0) {
                 //cmds.add("wkhtmltopdf - - " +getOptions()); //issues with streaming output
                 cmds.add("wkhtmltopdf - " + tempfile + " " + getOptions());
             } else if (getUrl() != null && getUrl().length() > 0) {
                 String t = getContext().getRequest().getQueryString();
                 if (t == null) {
                     t = getUrl();
                 }
                 try {
                     t = t.replaceFirst("url=", "");
                    t = URLDecoder.decode(t);
                     t = t.replaceFirst("http://", "");
                 } catch (Exception e) {
                    e.printStackTrace();
                 }
                 //cmds.add("wkhtmltopdf \"" + t + "\" - " +getOptions());
                 cmds.add("wkhtmltopdf \"" + t + "\" " + tempfile + " " + getOptions());
             } else {
                 return new ForwardResolution("/index.html");
             }
 
             String[] cmd = new String[cmds.size()];
             cmd = cmds.toArray(cmd);
 
             for (String s : cmd) {
                 log.debug("CMD : " + s);
             }
 
 
             p = Runtime.getRuntime().exec(cmd);
 
             if (getHtml_inner() != null && getHtml_inner().length() > 0) {
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                 writer.write(getHtml_head());
                 /* hack to filter odd chars generated in eagli_engine reports
                  */
                 writer.write(getHtml_inner()
                         .replaceAll("â", "")
                         .replaceAll("Â","")
                         .replaceAll("â","-")
                         );
                 writer.write(getHtml_foot());
                 writer.flush();
                 writer.close();
             }
 
             InputStream stderr = p.getErrorStream();
 
             String line;
             BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stderr));
             while ((line = brCleanUp.readLine()) != null) {
                 log.debug(line);
             }
             brCleanUp.close();
 
             setFile(getFile().replaceAll(".pdf", ""));
 
             File tmpfile = new File(tempfile);
             InputStream is = new FileInputStream(tmpfile);
             tmpfile.delete();
             return new StreamingResolution("application/pdf", is).setFilename(getFile() + ".pdf");
 
             //return new StreamingResolution("application/pdf", p.getInputStream()).setFilename(file + ".pdf");
 
         } catch (IOException e) {
             //e.printStackTrace();
             log.error(e.getMessage());
             return new ForwardResolution("/error.html");
         }
 
     }
 
 // <editor-fold defaultstate="collapsed" desc="getters and setter">
     /**
      * @return the url
      */
     public String getUrl() {
         return url;
     }
 
     /**
      * @param url the url to set
      */
     public void setUrl(String url) {
         this.url = url;
     }
 
     /**
      * @return the html_head
      */
     public String getHtml_head() {
         return html_head;
     }
 
     /**
      * @param html_head the html_head to set
      */
     public void setHtml_head(String html_head) {
         this.html_head = html_head;
     }
 
     /**
      * @return the html_inner
      */
     public String getHtml_inner() {
         return html_inner;
     }
 
     /**
      * @param html_inner the html_inner to set
      */
     public void setHtml_inner(String html_inner) {
         this.html_inner = html_inner;
     }
 
     /**
      * @return the html_foot
      */
     public String getHtml_foot() {
         return html_foot;
     }
 
     /**
      * @param html_foot the html_foot to set
      */
     public void setHtml_foot(String html_foot) {
         this.html_foot = html_foot;
     }
 
     /**
      * @return the options
      */
     public String getOptions() {
        return UrlDecoder.decode(options);
     }
 
     /**
      * @param options the options to set
      */
     public void setOptions(String options) {
         this.options = options;
     }
 
     /**
      * @return the output
      */
     public String getFile() {
         return file;
     }
 
     /**
      * @param output the output to set
      */
     public void setFile(String file) {
         this.file = file;
     }
     //</editor-fold>
 }
