 package daniel.web.http.server.util;
 
 import daniel.web.html.AnchorBuilder;
 import daniel.web.html.Element;
 import daniel.web.html.ParagraphBuilder;
 import daniel.web.html.Tag;
 import daniel.web.html.TitleBuilder;
 import daniel.web.html.Xhtml5Document;
 import daniel.web.http.HttpResponse;
 import daniel.web.http.HttpStatus;
 import daniel.web.http.HttpVersion;
 import daniel.web.http.ResponseHeaderName;
 import daniel.web.http.cookies.CookieManager;
 import java.nio.charset.Charset;
 
 public final class HttpResponseFactory {
   private HttpResponseFactory() {}
 
   public static HttpResponse xhtmlResponse(HttpStatus status, Element html) {
     Xhtml5Document document = new Xhtml5Document(html);
     byte[] documentBytes = document.toString().getBytes(Charset.forName("UTF-8"));
     return new HttpResponse.Builder()
         .setStatus(status)
        .setHttpVersion(HttpVersion._1_1)
         .addHeader(ResponseHeaderName.EXPIRES, "Thu, 19 Nov 1981 08:52:00 GMT")
         .addHeader(ResponseHeaderName.CACHE_CONTROL, "no-store, no-cache, must-revalidate, post-check=0, pre-check=0")
         .addHeader(ResponseHeaderName.CONTENT_TYPE, "application/xhtml+xml; charset=utf-8")
         .addAllCookies(CookieManager.getCookies())
         .setBody(documentBytes)
         .build();
   }
 
   public static HttpResponse permanentRedirect(String location) {
     return redirect(location, HttpStatus.MOVED_PERMANENTLY);
   }
 
   public static HttpResponse redirectToGet(String location) {
     return redirect(location, HttpStatus.SEE_OTHER);
   }
 
   public static HttpResponse redirectSameMethod(String location) {
     return redirect(location, HttpStatus.TEMPORARY_REDIRECT);
   }
 
   private static HttpResponse redirect(String location, HttpStatus status) {
     Element title = new TitleBuilder().addEscapedText(status.toString()).build();
     Element head = new Element.Builder(Tag.HEAD)
         .addChild(title)
         .build();
 
     Element header = new Element.Builder(Tag.H1).addEscapedText(status.toString()).build();
     Element content = new ParagraphBuilder()
             .addEscapedText("This document has been moved ")
             .addChild(new AnchorBuilder()
                 .setHref(location)
                 .addEscapedText("here")
                 .build())
             .addEscapedText(".")
             .build();
     Element body = new Element(Tag.BODY, header, content);
 
     Element html = new Element.Builder(Tag.HTML)
         .setAttribute("xmlns", "http://www.w3.org/1999/xhtml")
         .setAttribute("xml:lang", "en")
         .addChild(head)
         .addChild(body)
         .build();
 
     Xhtml5Document document = new Xhtml5Document(html);
     byte[] documentBytes = document.toString().getBytes(Charset.forName("UTF-8"));
 
     return new HttpResponse.Builder()
         .setStatus(status)
        .setHttpVersion(HttpVersion._1_1)
         .addHeader(ResponseHeaderName.LOCATION, location)
         .addHeader(ResponseHeaderName.CONTENT_TYPE, "application/xhtml+xml; charset=utf-8")
         .addAllCookies(CookieManager.getCookies())
         .setBody(documentBytes)
         .build();
   }
 }
