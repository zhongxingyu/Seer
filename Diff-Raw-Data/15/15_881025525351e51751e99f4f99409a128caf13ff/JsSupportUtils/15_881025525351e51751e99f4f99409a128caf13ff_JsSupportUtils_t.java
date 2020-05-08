 package com.wideplay.warp.internal.pages;
 
 import com.wideplay.warp.rendering.HtmlWriter;
 import org.apache.commons.io.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
 import java.net.URISyntaxException;
 import java.util.MissingResourceException;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * On: 24/03/2007
  *
  * @author Dhanji R. Prasanna
  * @since 1.0
  */
 class JsSupportUtils {
     //js functions start with FN_
     private static String FN_PAGE_EVENT_PUBLISH;
 
     //load support funcs
     static void loadResources() {
         try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(HtmlWriter.class.getResourceAsStream("warp-events.js")));
            StringBuilder builder = new StringBuilder();
            while (bufferedReader.ready()) {
                builder.append(bufferedReader.readLine());
            }
            
            FN_PAGE_EVENT_PUBLISH = builder.toString();

         } catch (IOException e) {
             throw new MissingResourceException("Missing javascript resources required by Warp (are you copying .js files from src to build?)", HtmlWriter.class.getName(), "warp-events.js");
         } catch (NullPointerException e) {
             throw new MissingResourceException("Missing javascript resources required by Warp (are you copying .js files from src to build?)", HtmlWriter.class.getName(), "warp-events.js");            
         }
     }
 
     static String wrapOnFrameLoadFn(StringBuilder content) {
         //insert content in reverse order at index 0
         content.insert(0, "{");
         content.insert(0, HtmlWriter.ON_FRAME_LOAD_FUNCTION);
         content.insert(0, "function ");
 
         //close func
         content.append("}");
 
         //append support functions to onFrameLoad
         content.append(FN_PAGE_EVENT_PUBLISH);
 
         return content.toString();
     }
     
     static String wrapLinkedScripts(Set<String> linkedScripts) {
         StringBuilder builder = new StringBuilder();
         for (String script : linkedScripts) {
             builder.append("<script type=\"text/javascript\" src=\"");
             builder.append(script);
             builder.append("\"></script>");
         }
 
         return builder.toString();
     }
 }
