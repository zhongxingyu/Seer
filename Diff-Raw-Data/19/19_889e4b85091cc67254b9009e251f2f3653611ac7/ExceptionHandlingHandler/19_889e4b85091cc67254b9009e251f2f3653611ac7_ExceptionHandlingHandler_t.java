 package daniel.web.http.server.util;
 
import daniel.logging.Logger;
 import daniel.web.html.Element;
 import daniel.web.html.ParagraphBuilder;
 import daniel.web.html.Tag;
 import daniel.web.html.TitleBuilder;
 import daniel.web.http.HttpRequest;
 import daniel.web.http.HttpResponse;
 import daniel.web.http.HttpStatus;
 import daniel.web.http.server.Handler;
 
 public final class ExceptionHandlingHandler implements Handler {
  private static final Logger logger = Logger.forClass(ExceptionHandlingHandler.class);

   private final Handler delegate;
 
   public ExceptionHandlingHandler(Handler delegate) {
     this.delegate = delegate;
   }
 
   @Override
   public HttpResponse handle(HttpRequest request) {
     try {
       return delegate.handle(request);
     } catch (Exception e) {
      logger.error(e, "Error handling requeset:\n", request);
 
       Element title = new TitleBuilder()
           .addEscapedText("Internal Server Error")
           .build();
       Element head = new Element(Tag.HEAD, title);
 
       Element body = new Element(Tag.BODY,
           new Element.Builder(Tag.H1).addEscapedText("Oops.").build(),
           new ParagraphBuilder().addEscapedText("The server encountered an error.").build()
       );
 
       Element html = new Element.Builder(Tag.HTML)
           .setAttribute("xmlns", "http://www.w3.org/1999/xhtml")
           .setAttribute("xml:lang", "en")
           .addChild(head)
           .addChild(body)
           .build();
 
       return HttpResponseFactory.xhtmlResponse(HttpStatus.INTERNAL_SERVER_ERROR, html);
     }
   }
 }
