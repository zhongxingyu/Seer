 package org.karmaexchange.util;
 
 import java.io.IOException;
import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URLDecoder;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 public class SEOUtil {
 	
     public static String fetchURL(HttpServletRequest request)
     {
         WebClient webClient = new WebClient();
         webClient.setCssErrorHandler(new SilentCssErrorHandler());
         try {
               String hash = URLDecoder.decode(request.getParameter("_escaped_fragment_"),"utf-8");
               HtmlPage page = webClient.getPage(ServletUtil.getBaseUri(request)+"/#!"+hash);
               
              webClient.waitForBackgroundJavaScript(1000);
               return page.asXml();
         } catch (FailingHttpStatusCodeException e) {
           // TODO Auto-generated catch block
               e.printStackTrace();
         } catch (MalformedURLException e) {
           // TODO Auto-generated catch block
               e.printStackTrace();
         } catch (IOException e) {
           // TODO Auto-generated catch block
               e.printStackTrace();
         }
         finally
         {
         	   webClient.closeAllWindows();
         }
         return "Error occured";
     }
     
    public static String fetchFBOGTags(HttpServletRequest request) throws UnsupportedEncodingException
     {
     	StringBuffer output = new StringBuffer();
     	output.append("<meta property=\"og:type\" content=\""+request.getParameter("ogtype")+"\"/>");
    	output.append("<meta property=\"og:url\" content=\""+URLDecoder.decode(request.getParameter("ogurl"),"utf-8")+"\"/>");
    	output.append("<meta property=\"og:title\" content=\""+URLDecoder.decode(request.getParameter("ogtitle"),"utf-8")+"\"/>");
    	output.append("<meta property=\"og:image\" content=\""+URLDecoder.decode(request.getParameter("ogimage"),"utf-8")+"\"/>");
    	output.append("<script> window.location=\""+URLDecoder.decode(request.getParameter("ogurl"),"utf-8")+"\"</script>");
     	return output.toString();
     	
     }
 	
 
 }
 
