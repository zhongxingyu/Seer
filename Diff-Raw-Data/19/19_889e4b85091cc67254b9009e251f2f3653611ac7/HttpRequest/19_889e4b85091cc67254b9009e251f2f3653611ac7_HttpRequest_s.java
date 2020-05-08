 package daniel.web.http;
 
 import daniel.data.collection.Collection;
 import daniel.data.dictionary.KeyValuePair;
 import daniel.data.multidictionary.sequential.ImmutableArrayMultidictionary;
 import daniel.data.multidictionary.sequential.ImmutableSequentialMultidictionary;
 import daniel.data.multidictionary.sequential.SequentialMultidictionary;
 import daniel.data.option.Option;
 import daniel.data.sequence.ImmutableSequence;
 import daniel.data.stack.DynamicArray;
 import daniel.data.stack.MutableStack;
 import daniel.data.util.Check;
 import daniel.parsing.ParseResult;
 import daniel.web.http.multipart.Part;
 import daniel.web.http.parsing.CookieHeaderParser;
 import daniel.web.http.parsing.MultipartParser;
 import daniel.web.http.parsing.TokenOrQuotedStringParser;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.nio.charset.StandardCharsets;
 
 public final class HttpRequest {
   public static final class Builder {
     private Option<RequestMethod> method = Option.none();
     private Option<String> resource = Option.none();
     private Option<HttpVersion> httpVersion = Option.none();
     private final MutableStack<KeyValuePair<String, String>> headers;
     private Option<byte[]> body = Option.none();
 
     public Builder() {
       headers = DynamicArray.create();
     }
 
     public Builder setMethod(RequestMethod method) {
       this.method = Option.some(method);
       return this;
     }
 
     public Builder setResource(String resource) {
       this.resource = Option.some(resource);
       return this;
     }
 
     public Builder setHttpVersion(HttpVersion httpVersion) {
       this.httpVersion = Option.some(httpVersion);
       return this;
     }
 
     public Builder addHeader(KeyValuePair<String, String> header) {
       return addHeader(header.getKey(), header.getValue());
     }
 
     public Builder addHeader(RequestHeaderName name, String value) {
       return addHeader(name.getStandardName(), value);
     }
 
     public Builder addHeader(String name, String value) {
       for (RequestHeaderName header : RequestHeaderName.values())
          if (header.getStandardName().equalsIgnoreCase(name))
            name = header.getStandardName();
       headers.pushBack(new KeyValuePair<>(name, value));
       return this;
     }
 
     public Builder setBody(byte[] body) {
       this.body = Option.some(body);
       return this;
     }
 
     public HttpRequest build() {
       return new HttpRequest(this);
     }
   }
 
   private final RequestMethod method;
   private final String resource;
   private final HttpVersion httpVersion;
   private final ImmutableSequentialMultidictionary<String, String> headers;
   private final Option<byte[]> body;
 
   private Option<ImmutableSequence<Part>> memParts = Option.none();
 
   private HttpRequest(Builder builder) {
     this.method = builder.method.getOrThrow("No request method was set.");
     this.resource = builder.resource.getOrThrow("No request resource was set.");
     this.httpVersion = builder.httpVersion.getOrThrow("No HTTP version was set.");
     this.headers = ImmutableArrayMultidictionary.copyOf(builder.headers);
     this.body = builder.body;
   }
 
   public RequestMethod getMethod() {
     return method;
   }
 
   public String getResource() {
     return resource;
   }
 
   public HttpVersion getHttpVersion() {
     return httpVersion;
   }
 
   public SequentialMultidictionary<String, String> getHeaders() {
     return headers;
   }
 
   public SequentialMultidictionary<String, String> getCookies() {
     MutableStack<KeyValuePair<String, String>> cookies = DynamicArray.create();
     for (String cookieList : headers.getValues(RequestHeaderName.COOKIE.getStandardName())) {
       byte[] cookieListBytes = cookieList.getBytes(StandardCharsets.US_ASCII);
       ParseResult<SequentialMultidictionary<String, String>> resCookieList =
           CookieHeaderParser.singleton.tryParse(cookieListBytes, 0)
               .getOrThrow("Failed to parse cookie list.");
       Check.that(resCookieList.getRem() == cookieListBytes.length,
           "Failed to parse entire cookie list: \"%s\".", cookieList);
       for (KeyValuePair<String, String> cookie : resCookieList.getValue())
         cookies.pushBack(cookie);
     }
     return ImmutableArrayMultidictionary.copyOf(cookies);
   }
 
   public SequentialMultidictionary<String, String> getUrlencodedPostData() {
     String contentType = headers.getValues(RequestHeaderName.CONTENT_TYPE.getStandardName())
         .tryGetOnlyElement().getOrThrow("Expected exactly one content type.");
     Check.that(contentType.equals("application/x-www-form-urlencoded"),
         "Expected content type of \"%s\", but found \"%s\".",
         "application/x-www-form-urlencoded", contentType);
 
     String data = new String(body.getOrThrow("No POST data was sent."), StandardCharsets.US_ASCII);
     MutableStack<KeyValuePair<String, String>> keyValuePairs = DynamicArray.create();
     for (String param : data.split("&")) {
       String[] pair = param.split("=");
       Check.that(pair.length > 0);
       Check.that(pair.length <= 2);
       try {
         String key = URLDecoder.decode(pair[0], "UTF-8");
         String value = pair.length == 2
             ? URLDecoder.decode(pair[1], "UTF-8")
             : "";
         keyValuePairs.pushBack(new KeyValuePair<>(key, value));
       } catch (UnsupportedEncodingException e) {
         throw new AssertionError("UTF-8 should be supported universally.");
       }
     }
     return ImmutableArrayMultidictionary.copyOf(keyValuePairs);
   }
 
   public ImmutableSequence<Part> getParts() {
     if (memParts.isDefined())
       return memParts.getOrThrow();
 
     String contentType = headers.getValues(RequestHeaderName.CONTENT_TYPE.getStandardName())
         .tryGetOnlyElement().getOrThrow("Expected exactly one content type.");
     if (!contentType.startsWith("multipart/form-data"))
       throw new RuntimeException("Expected content type of multipart/form-data");
 
     // TODO: This could possibly fail if the content type is something like
     // Content-Type: multipart/form-data; foo="boundary="; boundary=[boundary]
     int pBoundary = contentType.indexOf("boundary=");
     if (pBoundary == -1)
       throw new RuntimeException("Expected boundary in content type.");
     pBoundary += "boundary=".length();
     String boundary = TokenOrQuotedStringParser.singleton
         .tryParse(contentType.substring(pBoundary).getBytes(StandardCharsets.US_ASCII), 0)
         .getOrThrow("Expected token or quoted string after \"boundary=\".").getValue();
 
     ImmutableSequence<Part> result = new MultipartParser(boundary)
         .tryParse(body.getOrThrow("No body was sent."), 0)
         .getOrThrow("Multipart parsing failed")
         .getValue().toImmutable();
     memParts = Option.some(result);
     return result;
   }
 
   public String getHost() {
     Collection<String> hosts = headers.getValues(RequestHeaderName.HOST.getStandardName());
     return hosts.tryGetOnlyElement().getOrThrow(
         "Expected exactly one Host header; received %s.", headers);
   }
 
   public Option<byte[]> getBody() {
     return body;
   }
 
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder();
     sb.append(String.format("%s %s HTTP/%s", method, resource, httpVersion));
     for (KeyValuePair<String, String> header : headers)
       sb.append(String.format("%s: %s\r\n", header.getKey(), header.getValue()));
     return sb.toString();
   }
 }
