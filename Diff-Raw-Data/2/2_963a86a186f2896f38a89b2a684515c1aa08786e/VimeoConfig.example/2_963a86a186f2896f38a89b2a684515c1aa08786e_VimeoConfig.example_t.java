 package org.vimeoid.connection;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.connection</dd>
  * </dl>
  *
  * <code>VimeoConfig</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Aug 28, 2010 1:46:57 PM 
  *
  */
 public class VimeoConfig {
     
     public static final String VIMEO_SITE_URL = "http://vimeo.com";
     public static final int VIMEO_API_VERSION = 2;
     
     private static final String VIMEO_API_URL = VIMEO_SITE_URL + "/api";
     private static final String VIMEO_REST_API_URL = VIMEO_API_URL + "/rest";
     
     public static final String VIMEO_SIMPLE_API_CALL_PREFIX   = VIMEO_API_URL +
                                                                 "/v" + VIMEO_API_VERSION;
    public static final String VIMEO_ADVANCED_API_ROOT        = VIMEO_REST_API_URL +
                                                                 "/v" + VIMEO_API_VERSION;
     /* public static final String VIMEO_AUTH_API_CALL_PREFIX     = VIMEO_SITE_URL +
                                                                 "/services/auth"; */    
     public static final String VIMEO_OAUTH_API_ROOT           = VIMEO_SITE_URL +
     															"/oauth";
     
     public static final String VIMEO_API_KEY = "<Vimeo API key>";
     public static final String VIMEO_SHARED_SECRET = "<Vimeo shared secret>";
 
 }
