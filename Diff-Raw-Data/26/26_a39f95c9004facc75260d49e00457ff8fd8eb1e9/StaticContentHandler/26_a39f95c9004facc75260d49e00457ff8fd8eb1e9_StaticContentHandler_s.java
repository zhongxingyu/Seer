 package daniel.web.http.server.util;
 
 import daniel.data.dictionary.ImmutableDictionary;
 import daniel.data.dictionary.MutableHashDictionary;
 import daniel.data.option.Option;
 import daniel.data.unit.Duration;
 import daniel.data.unit.Instant;
 import daniel.data.util.Check;
 import daniel.data.util.FilenameUtils;
 import daniel.data.util.IOUtils;
 import daniel.logging.Logger;
 import daniel.web.http.DateUtils;
 import daniel.web.http.HttpRequest;
 import daniel.web.http.HttpResponse;
 import daniel.web.http.HttpStatus;
 import daniel.web.http.HttpVersion;
 import daniel.web.http.RequestHeaderName;
 import daniel.web.http.RequestMethod;
 import daniel.web.http.ResponseHeaderName;
 import daniel.web.http.server.PartialHandler;
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Date;
 
 public final class StaticContentHandler implements PartialHandler {
   private static final Logger logger = Logger.forClass(StaticContentHandler.class);
 
   public static final class Builder {
     private Option<File> contentRoot = Option.none();
     private final MutableHashDictionary<String, String>
         mimeTypesByExtension = MutableHashDictionary.create();
 
     public Builder setContentRoot(File contentRoot) {
       this.contentRoot = Option.some(contentRoot);
       return this;
     }
 
     public Builder addContentType(String mimeType, String extension, String... moreExtensions) {
       mimeTypesByExtension.put(extension, mimeType);
       for (String ext : moreExtensions)
         mimeTypesByExtension.put(ext, mimeType);
       return this;
     }
 
     public Builder addCommonContentTypes() {
       return this
           .addContentType("text/plain", "txt")
           .addContentType("text/html", "html", "htm")
           .addContentType("image/gif", "gif")
           .addContentType("image/png", "png")
           .addContentType("image/jpeg", "jpg", "jpeg")
           .addContentType("image/x-icon", "ico")
           .addContentType("application/zip", "zip")
           .addContentType("application/gzip", "gz")
           .addContentType("text/css", "css")
           .addContentType("application/javascript", "js");
     }
 
     public StaticContentHandler build() {
       return new StaticContentHandler(this);
     }
   }
 
   private final File contentRoot;
   private final ImmutableDictionary<String, String> mimeTypesByExtension;
 
   private StaticContentHandler(Builder builder) {
     contentRoot = builder.contentRoot.getOrThrow("No content root was set.");
     mimeTypesByExtension = builder.mimeTypesByExtension.toImmutable();
     Check.that(contentRoot.isDirectory(), "%s is not a directory.", contentRoot);
   }
 
   @Override
   public Option<HttpResponse> tryHandle(HttpRequest request) {
     Option<String> optExtension = FilenameUtils.getExtension(request.getResource());
     if (optExtension.isEmpty())
       return Option.none();
 
     Option<String> mimeType = mimeTypesByExtension.tryGetValue(optExtension.getOrThrow().toLowerCase());
     if (mimeType.isEmpty())
       return Option.none();
 
     File resourcePath = new File(contentRoot, request.getResource());
     if (!resourcePath.exists() || !isInContentRoot(resourcePath))
       return Option.none();
 
     Instant lastModified = Instant.fromDate(new Date(resourcePath.lastModified()));
     Option<String> optIfModifiedSince = request.getHeaders()
        .getValues(RequestHeaderName.IF_MODIFIED_SINCE.toString())
        .tryGetOnlyElement();
     if (optIfModifiedSince.isDefined()) {
       try {
         Instant ifModifiedSince = DateUtils.parseInstant(optIfModifiedSince.getOrThrow());
         if (lastModified.isBefore(ifModifiedSince))
           return Option.some(new HttpResponse.Builder()
               .setStatus(HttpStatus.NOT_MODIFIED)
               .addHeader(ResponseHeaderName.EXPIRES, DateUtils.formatInstant(lastModified.plus(Duration.fromHours(24))))
               .addHeader(ResponseHeaderName.LAST_MODIFIED, DateUtils.formatInstant(lastModified))
               .build());
       } catch (ParseException e) {
         logger.error(e, "Failed to parse If-Modified-Since header: %s",
             optIfModifiedSince.getOrThrow());
       }
     }
 
     Option<byte[]> content;
     if (request.getMethod() == RequestMethod.HEAD)
       content = Option.none();
     else
       try {
         content = Option.some(IOUtils.readFileToByteArray(resourcePath));
       } catch (IOException e) {
         throw new RuntimeException(e);
       }
 
     Instant expires = Instant.now().plus(Duration.fromHours(24));
 
     HttpResponse.Builder responseBuilder = new HttpResponse.Builder()
        .setHttpVersion(HttpVersion._1_1)
         .setStatus(HttpStatus.OK)
         .addHeader(ResponseHeaderName.EXPIRES, DateUtils.formatInstant(expires))
         .addHeader(ResponseHeaderName.LAST_MODIFIED, DateUtils.formatInstant(lastModified))
         .addHeader(ResponseHeaderName.CONTENT_TYPE, mimeType.getOrThrow());
     if (content.isDefined())
       responseBuilder.setBody(content);
     return Option.some(responseBuilder.build());
   }
 
   private boolean isInContentRoot(File file) {
     try {
       file = file.getCanonicalFile();
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
 
     while (file != null && !file.equals(contentRoot))
       file = file.getParentFile();
     return file != null;
   }
 }
