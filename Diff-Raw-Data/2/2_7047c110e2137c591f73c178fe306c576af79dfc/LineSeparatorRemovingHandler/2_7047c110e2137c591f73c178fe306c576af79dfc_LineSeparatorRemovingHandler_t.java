 package daniel.web.http.server.util;
 
 import daniel.data.option.Option;
 import daniel.data.sequence.ImmutableArray;
 import daniel.data.sequence.ImmutableSequence;
 import daniel.data.util.Check;
 import daniel.logging.Logger;
 import daniel.web.http.HttpRequest;
 import daniel.web.http.HttpResponse;
 import daniel.web.http.RequestMethod;
 import daniel.web.http.server.PartialHandler;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 
 public final class LineSeparatorRemovingHandler implements PartialHandler {
   private static final Logger logger = Logger.forClass(LineSeparatorRemovingHandler.class);
 
   public static final LineSeparatorRemovingHandler singleton = new LineSeparatorRemovingHandler();
 
   private static final ImmutableSequence<RequestMethod> methodToRedirect =
       ImmutableArray.create(RequestMethod.GET, RequestMethod.HEAD);
 
   private LineSeparatorRemovingHandler() {}
 
   @Override
   public Option<HttpResponse> tryHandle(HttpRequest request) {
     if (!methodToRedirect.contains(request.getMethod()))
       return Option.none();
 
     String strippedResource = stripPercentEncodedLineSeparatorsFromUrl(request.getResource());
     if (strippedResource.equals(request.getResource()))
       return Option.none();
 
     String location = String.format("http://%s%s", request.getHost(), strippedResource);
     logger.info("Redirecting to %s.", location);
     return Option.some(HttpResponseFactory.permanentRedirect(location));
   }
 
   private static String stripPercentEncodedLineSeparatorsFromUrl(String url) {
     try {
       StringBuilder sb = new StringBuilder();
       for (int i = 0; i < url.length();) {
         if (url.charAt(i) == '%') {
           String percentEncodedBlock = getPercentEncodedBlock(url, i);
           String decodedBlock = URLDecoder.decode(percentEncodedBlock, "UTF-8");
           sb.append(URLEncoder.encode(stripLineSeparators(decodedBlock), "UTF-8"));
           i += percentEncodedBlock.length();
         } else {
           sb.append(url.charAt(i));
           ++i;
         }
       }
       return sb.toString();
     } catch (UnsupportedEncodingException e) {
       logger.error("Eh? UTF-8 not supported?", e);
       return url;
     }
   }
 
   private static String getPercentEncodedBlock(String resource, int index) {
     StringBuilder sb = new StringBuilder();
    while (index < resource.length() && resource.charAt(index) == '%') {
       Check.that(resource.length() >= index + 3,
           "% not follwed by two bytes in \"%s\".", resource);
       sb.append(resource.substring(index, index + 3));
       index += 3;
     }
     return sb.toString();
   }
 
   private static String stripLineSeparators(String input) {
     StringBuilder sb = new StringBuilder();
     for (int offset = 0; offset < input.length();) {
       int codepoint = input.codePointAt(offset);
       int type = Character.getType(codepoint);
       if (type != Character.LINE_SEPARATOR && type != Character.PARAGRAPH_SEPARATOR)
         sb.append(Character.toChars(codepoint));
       offset += Character.charCount(codepoint);
     }
     return sb.toString();
   }
 }
