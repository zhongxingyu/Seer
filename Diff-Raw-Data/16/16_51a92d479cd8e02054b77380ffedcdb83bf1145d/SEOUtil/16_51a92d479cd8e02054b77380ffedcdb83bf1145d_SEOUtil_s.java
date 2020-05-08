 package org.karmaexchange.util;
 
 import java.io.IOException;
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
               
              webClient.waitForBackgroundJavaScript(10000);
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
     
    public static String fetchFBOGTags(HttpServletRequest request)
     {
     	StringBuffer output = new StringBuffer();
     	output.append("<meta property=\"og:type\" content=\""+request.getParameter("ogtype")+"\"/>");
    	output.append("<meta property=\"og:url\" content=\""+URLDecoder.decode(request.getParameter("ogurl"))+"\"/>");
    	output.append("<meta property=\"og:title\" content=\""+URLDecoder.decode(request.getParameter("ogtitle"))+"\"/>");
    	output.append("<meta property=\"og:image\" content=\""+URLDecoder.decode(request.getParameter("ogimage"))+"\"/>");
    	output.append("<script> window.location=\""+URLDecoder.decode(request.getParameter("ogurl"))+"\"</script>");
     	return output.toString();
     	
     }
 	
 
 }
 
