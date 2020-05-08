 package com.wideplay.warp.internal.pages;
 
 import com.wideplay.warp.rendering.HtmlWriter;
 import com.wideplay.warp.util.reflect.InfrastructureError;
 import org.apache.commons.io.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Set;
import java.util.MissingResourceException;
 
 /**
  * Created with IntelliJ IDEA.
  * On: 24/03/2007
  *
  * @author Dhanji R. Prasanna
  * @since 1.0
  */
 class JsSupportUtils {
     //js functions start with FN_
    private static final String FN_PAGE_EVENT_PUBLISH;
 
     private static JsSupportUtils instance;
 
     //load support funcs
    static {
         try {
             FN_PAGE_EVENT_PUBLISH = FileUtils.readFileToString(new File(HtmlWriter.class.getResource("warp-events.js").toURI()), null);
         } catch (IOException e) {
            throw new MissingResourceException("Missing javascript resources required by Warp", JsSupportUtils.class.getName(), "warp-events.js");
         } catch (URISyntaxException e) {
            throw new MissingResourceException("Missing javascript resources required by Warp", JsSupportUtils.class.getName(), "warp-events.js");
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
