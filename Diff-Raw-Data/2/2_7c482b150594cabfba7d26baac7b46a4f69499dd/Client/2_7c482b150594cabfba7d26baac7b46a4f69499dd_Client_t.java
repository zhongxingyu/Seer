 package http;
 
 import java.net.URL;
 
 /**
  * A simple {@code HTTP} client API that can be used to make request to any {@code HTTP 1.0/1.1} server.
  *
  * The most commonly used types have been defined statically within this class so in most cases only one import is
  * required to use the API.
  *
  * {@code import static http.Client.*}
  *
  * This will provide access to all the static request methods
  * ({@link #GET(String)}, {@link #POST(String)}, {@link #PUT(String)}...) as well as the  ({@link Request} and
  * {@link Response} objects.
  *
  * The simplest way to use the client is through the static methods, these use a singleton instance of the client. This
  * singleton isn't instantiated until one of the static methods are used.
  *
  * Usage:
  * <code>System.out.println(GET("http://yoursite.com"))}</code>
  *
  * <code>
  *     Request request = new Request("http://yourservice.com/post");
  *     request.addHeader(new ContentType("application/x-www-form-urlencoded"));
  *     request.setBody(new Parameter("some", "thing"));
  *     POST(request);
  * </code>
  *
  * The singleton client can be configured with the {@link Configuration} instance returned or set by the
  * {@link #SET_CONFIGURATION} or {@link #GET_CONFIGURATION} methods respectively. It also automatically picks up the uses the
  * JVM SSL and proxy properties.
  * Proxy:
  * <code>
  *     http.proxyHost
  *     http.proxyPort
  *     http.nonProxyHosts
  *     https.proxyHost
  *     https.proxyPort
  *     https.nonProxyHosts
  *     socksProxyHost
  *     socksProxyPort
  * </code>
  *
  * SSL:
  * <code>
  *     javax.net.ssl.trustStore
  *     javax.net.ssl.trustStoreType
  *     javax.net.ssl.trustStorePassword
  *     javax.net.ssl.keyStore
  *     javax.net.ssl.keyStoreType
  *     javax.net.ssl.keyStorePassword
  * </code>
  *
  * If multiple client configuration profiles are required a new instance of the {@code Client} class can be created for
  * each and configured individually using the instance {@link #getConfiguration} and {@link #setConfiguration} methods. The
  * instance HTTP request methods would then be used ({@link #get(String), #post(String), #put(String)...}).
  *
  * Usage:
  * <code>System.out.println(new Client().get("http://yoursite.com"))}</code>
  *
  * <code>
  *     Request request = new Request("http://yourservice.com/post");
  *     request.addHeader(new ContentType("application/x-www-form-urlencoded"));
 *     request.setBody(new Parameter("some", "thing"));
  *     new Client().post(request);
  * </code>
  *
  * @author Karl Bennett
  */
 public class Client {
 
     private static class SingletonHolder {
         public static final Client INSTANCE = new Client();
     }
 
     private static Client getInstance() {
         return SingletonHolder.INSTANCE;
     }
 
 
     /**
      * Static method that behaves exactly the same as the {@link #options(String)} method except that it is configure
      * with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response OPTIONS(String url) {
 
         return getInstance().options(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #options(java.net.URL)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response OPTIONS(URL url) {
 
         return getInstance().options(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #options(http.Request)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public static Response OPTIONS(Request request) {
 
         return getInstance().options(request);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #get(String)} method except that it is configure
      * with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response GET(String url) {
 
         return getInstance().get(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #get(java.net.URL)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response GET(URL url) {
 
         return getInstance().get(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #get(http.Request)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public static Response GET(Request request) {
 
         return getInstance().get(request);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #head(String)} method except that it is configure
      * with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response HEAD(String url) {
 
         return getInstance().head(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #head(java.net.URL)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response HEAD(URL url) {
 
         return getInstance().head(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #head(http.Request)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public static Response HEAD(Request request) {
 
         return getInstance().head(request);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #post(String)}  method except that it is configure
      * with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response POST(String url) {
 
         return getInstance().post(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #post(java.net.URL)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response POST(URL url) {
 
         return getInstance().post(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #post(http.Request)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public static Response POST(Request request) {
 
         return getInstance().post(request);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #put(String)}  method except that it is configure
      * with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response PUT(String url) {
 
         return getInstance().put(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #put(java.net.URL)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response PUT(URL url) {
 
         return getInstance().put(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #put(http.Request)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public static Response PUT(Request request) {
 
         return getInstance().put(request);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #delete(String)}  method except that it is configure
      * with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response DELETE(String url) {
 
         return getInstance().delete(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #delete(java.net.URL)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response DELETE(URL url) {
 
         return getInstance().delete(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #delete(http.Request)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public static Response DELETE(Request request) {
 
         return getInstance().delete(request);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #trace(String)}  method except that it is configure
      * with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response TRACE(String url) {
 
         return getInstance().trace(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #trace(java.net.URL)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response TRACE(URL url) {
 
         return getInstance().trace(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #trace(http.Request)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public static Response TRACE(Request request) {
 
         return getInstance().trace(request);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #connect(String)}  method except that it is configure
      * with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response CONNECT(String url) {
 
         return getInstance().connect(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #connect(java.net.URL)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public static Response CONNECT(URL url) {
 
         return getInstance().connect(url);
     }
 
     /**
      * Static method that behaves exactly the same as the {@link #connect(http.Request)} method except that it is
      * configure with the application global {@link #GET_CONFIGURATION} method.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public static Response CONNECT(Request request) {
 
         return getInstance().connect(request);
     }
 
 
     /**
      * Static method that behaves the same as the {@link #getConfiguration} method except that it returns a
      * {@link Configuration} instance that can be used to apply configuration for the application global static request
      * methods e.g. {@link #GET(java.net.URL) GET}, {@link #GET(java.net.URL) POST}, {@link #GET(java.net.URL) PUT}...
      *
      * @return the current static global {@code Configuration} instance.
      */
     public static Configuration SET_CONFIGURATION() {
 
         return getInstance().getConfiguration();
     }
 
     /**
      * Static method that behaves the same as the {@link #getConfiguration} method except that it returns a
      * {@link Configuration} instance that can be used to apply configuration for the application global static request
      * methods e.g. {@link #GET(java.net.URL) GET}, {@link #GET(java.net.URL) POST}, {@link #GET(java.net.URL) PUT}...
      *
      * @param configuration a new {@code Configuration} instance.
      */
     public static void GET_CONFIGURATION(Configuration configuration) {
 
         getInstance().setConfiguration(configuration);
     }
 
 
     public Client() {
     }
 
     /**
      * Sends an {@code OPTIONS} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response options(String url) {
 
         return options(new Request(url));
     }
 
     /**
      * Sends an {@code OPTIONS} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response options(URL url) {
 
         return options(new Request(url));
     }
 
     /**
      * Sends an {@code OPTIONS} request to the {@code HTTP} server defined within the provided {@link Request}.
      *
      * Any {@link Header}'s, {@link Parameter}'s, and body that have been populated in the request will be sent.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public Response options(Request request) {
 
         return null;
     }
 
     /**
      * Sends a {@code GET} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response get(String url) {
 
         return get(new Request(url));
     }
 
     /**
      * Sends an {@code GET} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response get(URL url) {
 
         return get(new Request(url));
     }
 
     /**
      * Sends an {@code GET} request to the {@code HTTP} server defined within the provided {@link Request}.
      *
      * Any {@link Header}'s, {@link Parameter}'s, and body that have been populated in the request will be sent.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public Response get(Request request) {
 
         return null;
     }
 
     /**
      * Sends a {@code HEAD} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response head(String url) {
 
         return head(new Request(url));
     }
 
     /**
      * Sends an {@code HEAD} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response head(URL url) {
 
         return head(new Request(url));
     }
 
     /**
      * Sends an {@code HEAD} request to the {@code HTTP} server defined within the provided {@link Request}.
      *
      * Any {@link Header}'s, {@link Parameter}'s, and body that have been populated in the request will be sent.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public Response head(Request request) {
 
         return null;
     }
 
     /**
      * Sends a {@code POST} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response post(String url) {
 
         return post(new Request(url));
     }
 
     /**
      * Sends an {@code POST} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response post(URL url) {
 
         return post(new Request(url));
     }
 
     /**
      * Sends an {@code POST} request to the {@code HTTP} server defined within the provided {@link Request}.
      *
      * Any {@link Header}'s, {@link Parameter}'s, and body that have been populated in the request will be sent.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public Response post(Request request) {
 
         return null;
     }
 
     /**
      * Sends a {@code PUT} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response put(String url) {
 
         return put(new Request(url));
     }
 
     /**
      * Sends an {@code PUT} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response put(URL url) {
 
         return put(new Request(url));
     }
 
     /**
      * Sends an {@code PUT} request to the {@code HTTP} server defined within the provided {@link Request}.
      *
      * Any {@link Header}'s, {@link Parameter}'s, and body that have been populated in the request will be sent.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public Response put(Request request) {
 
         return null;
     }
 
     /**
      * Sends a {@code DELETE} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response delete(String url) {
 
         return delete(new Request(url));
     }
 
     /**
      * Sends an {@code DELETE} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response delete(URL url) {
 
         return delete(new Request(url));
     }
 
     /**
      * Sends an {@code DELETE} request to the {@code HTTP} server defined within the provided {@link Request}.
      *
      * Any {@link Header}'s, {@link Parameter}'s, and body that have been populated in the request will be sent.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public Response delete(Request request) {
 
         return null;
     }
 
     /**
      * Sends a {@code TRACE} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response trace(String url) {
 
         return trace(new Request(url));
     }
 
     /**
      * Sends an {@code TRACE} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response trace(URL url) {
 
         return trace(new Request(url));
     }
 
     /**
      * Sends an {@code TRACE} request to the {@code HTTP} server defined within the provided {@link Request}.
      *
      * Any {@link Header}'s, {@link Parameter}'s, and body that have been populated in the request will be sent.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public Response trace(Request request) {
 
         return null;
     }
 
     /**
      * Sends a {@code CONNECT} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response connect(String url) {
 
         return connect(new Request(url));
     }
 
     /**
      * Sends an {@code CONNECT} request to the {@code HTTP} server at the provided {@code URL}.
      *
      * @param url the {@code URL} for the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      */
     public Response connect(URL url) {
 
         return connect(new Request(url));
     }
 
     /**
      * Sends an {@code CONNECT} request to the {@code HTTP} server defined within the provided {@link Request}.
      *
      * Any {@link Header}'s, {@link Parameter}'s, and body that have been populated in the request will be sent.
      *
      * @param request the {@code Request} that will be sent to the {@code HTTP} server.
      * @return the {@link Response} sent back by the {@code HTTP} server.
      * @throws IllegalStateException if a converter hasn't been registered that can convert the request body's type into
      * an {@link java.io.InputStream}.
      */
     public Response connect(Request request) {
 
         return null;
     }
 
 
     /**
      * Returns the current {@link Configuration} instance for this {@link Client}.
      * This can be used to configure all the different aspects of the client.
      *
      * @return the current {@code Configuration} instance.
      */
     public Configuration getConfiguration() {
 
         return null;
     }
 
     /**
      * Overrides the clients {@link Configuration} object. This will override all the previous client configuration.
      *
      * @param configuration a new {@code Configuration} instance.
      */
     public void setConfiguration(Configuration configuration) {
 
     }
 }
