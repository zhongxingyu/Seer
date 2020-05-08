 package com.readmill.api;
 
 import org.apache.http.HttpHost;
 
 @SuppressWarnings("UnusedDeclaration")
 public class Environment {
   private final HttpHost mApiHost, mWebHost;
 
   /**
    * Creates an environment for a Api Wrapper.
   *
   * @param apiHost   Host to send requests to
    * @param loginHost Host where users can log in to the service
   * @param useSSL    use secure connections or not
    */
   public Environment(String apiHost, String loginHost, boolean useSSL) {
     this(apiHost, -1, loginHost, -1, useSSL);
   }
 
   /**
    * The Readmill LIVE API environment.
    * Sends requests to https://api.readmill.com.
    * Authorizes on https://readmill.com.
    */
   public static final Environment Live = new Environment("api.readmill.com", "m.readmill.com", true);
 
   /**
    * Creates an environment for a Api Wrapper.
   *
    * @param apiHost Host to send requests to
    * @param apiPort Port of api host
    * @param webHost Host where users can log in to the service
    * @param webPort Port of login host
   * @param useSSL  use secure connections or not
    */
   public Environment(String apiHost, int apiPort, String webHost, int webPort, boolean useSSL) {
     String scheme = useSSL ? "https" : "http";
     mApiHost = new HttpHost(apiHost, apiPort, scheme);
     mWebHost = new HttpHost(webHost, webPort, scheme);
   }
 
   /**
    * Gets the api host for this environment
    *
    * @return The api host
    */
   public HttpHost getApiHost() {
     return mApiHost;
   }
 
   /**
    * Gets the web host for this environment
    *
    * @return The web host
    */
   public HttpHost getWebHost() {
     return mWebHost;
   }
 
   /**
    * Gets the URL to the api host.
    *
    * @return The url as a string for the api host
    */
   public String getApiUrl() {
     return mApiHost.toString();
   }
 
   /**
    * Gets the URL to the web host.
    *
    * @return The url as a string to the web host
    */
   public String getWebUrl() {
     return mWebHost.toString();
   }
 
   @Override
   public String toString() {
     return "Readmill Server Environment" +
         " with API server: " + getApiUrl() +
         " and Web server: " + getWebUrl();
   }
 }
