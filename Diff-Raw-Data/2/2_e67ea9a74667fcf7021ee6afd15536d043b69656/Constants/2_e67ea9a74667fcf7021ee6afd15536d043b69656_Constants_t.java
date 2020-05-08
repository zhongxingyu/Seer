 package ikrs.httpd;
 
 /**
  * @author Ikaros Kappler
  * @date 2012-07-16
  * @version 1.0.0
  **/
 
 public class Constants {
 
     public static final byte CR                                                  = 0xD; // 13 decimal
     public static final byte LF                                                  = 0xA; // 10 decimal
     
 
    public static final String VERSION                                           = "1.0.1.alpha";
     public static final String HTTP                                              = "HTTP";
     public static final String SUPPORTED_HTTP_VERSION                            = "1.1";
 
     /* Currently only GET, POST, HEAD, TRACE and OPTIONS are implemented */
     public static final String HTTP_METHOD_GET                                   = "GET";
     public static final String HTTP_METHOD_POST                                  = "POST";
     public static final String HTTP_METHOD_HEAD                                  = "HEAD";
     public static final String HTTP_METHOD_PUT                                   = "PUT";
     public static final String HTTP_METHOD_DELETE                                = "DELETE";
     public static final String HTTP_METHOD_TRACE                                 = "TRACE";
     public static final String HTTP_METHOD_OPTIONS                               = "OPTIONS";
     public static final String HTTP_METHOD_CONNECT                               = "CONNECT";
 
     /* For the LogManager. */
     public static final String NAME_DEFAULT_LOGGER                               = "DEFAULT_HTTP_LOGGER";
 
     /* The http yucca-driven config constants */
     public static final String KEY_HTTPCONFIG                                    = "httpConfig";
 
     public static final String KEY_HTTPCONFIG_SETTINGS                           = "httpSettings";
     public static final String KEY_HTTPCONFIG_SETTINGS_FILE                      = "configFile";
 
     public static final String KEY_HTTPCONFIG_FILEHANDLERS                       = "fileHandlers";
     public static final String KEY_HTTPCONFIG_FILEHANDLERS_FILE                  = "configFile";
 
     
     /* Keys for the additional-settings during resource processing */
     /* (will be temp-stored in the 'additionSettings' map) */
     public static final String AKEY_HTACCESS_ERROR_DOCUMENT_BASE                 = "HTACCESS.ERROR_DOCUMENT.{STATUS_CODE}";
 
     
     /* The ikrs.http config constants */
     public static final String CKEY_HTTPCONFIG_DISABLE_METHOD_BASE               = "DISABLE_METHOD.{HTTP_METHOD}";
     public static final String CKEY_HTTPCONFIG_DOCUMENT_ROOT                     = "DOCUMENT_ROOT";
     public static final String CKEY_HTTPCONFIG_SESSION_TIMEOUT                   = "SESSION_TIMEOUT";
     public static final String CKEY_HTTPCONFIG_ERROR_DOCUMENT_BASE               = "ERROR_DOCUMENT.{STATUS_CODE}";
 
 
     public static final String KEY_AUTHORIZATION_METHOD                          = "Authorization.Method";
     public static final String KEY_AUTHORIZATION_USER                            = "Authorization.User";
     public static final String KEY_AUTHORIZATION_PASS                            = "Authorization.Pass";
     public static final String KEY_AUTHORIZATION_CHALLENGE                       = "Authorization.Challenge";
 
 
     public static final String KEY_AUTHENTICATION_NONCE                          = "Authentication.Nonce";
     public static final String KEY_AUTHENTICATION_DOMAIN                         = "Authentication.Domain";
     public static final String KEY_AUTHENTICATION_ALGORITHM                      = "Authentication.Algorithm";
 
 
     public static final String KEY_HTACCESS_AUTHTYPE                             = "htaccess.AuthType";
     public static final String KEY_HTACCESS_AUTHNAME                             = "htaccess.AuthName";
     public static final String KEY_HTACCESS_CHARSET                              = "htaccess.Charset";
     public static final String KEY_HTACCESS_ADDEDTYPE                            = "htaccess.AddedType";
 
     public static final String KEY_HTPASSWD_ENCRYPTEDLINE                        = "htpasswd.EncryptedLine";
 
     public static final String KEY_SESSIONID                                     = "SESSION_ID";
     public static final String KEY_SESSIONTIMEOUT                                = "SESSION_TIMEOUT";
 
     /* Configuration constants */
     public static final String KEY_DEFAULTCHARACTERSET                           = "DEFAULT_CHARSET";
 
     //public static final String KEY_SERVERNAME                                    = "SERVER_NAME";
     //public static final String KEY_SERVERPORT                                    = "SERVER_PORT";
     public static final String KEY_SOFTWARENAME                                  = "SOFTWARE_NAME";
 
 
     // The session key constants
     //public static final String SKEY_ISALIVE                                      = "IS_ALIVE";
     //public static final String SKEY_LASTACCESSTIME                               = "LAST_ACCESSTIME";
 
     public static final String SKEY_REMOTE_ADDRESS                               = "REMOTE_ADDRESS";
     public static final String SKEY_REMOTE_HOST                                  = "REMOTE_HOST";
     public static final String SKEY_REMOTE_PORT                                  = "REMOTE_PORT";
     /* The REMOTE_IDENT field is optional. See http://graphcomp.com/info/specs/cgi11.html */
     public static final String SKEY_REMOTE_IDENT                                 = "REMOTE_IDENT";  
     public static final String SKEY_REMOTE_USER                                  = "REMOTE_USER";
     public static final String SKEY_LOCAL_ADDRESS                                = "LOCAL_ADDRESS";
     public static final String SKEY_LOCAL_HOST                                   = "LOCAL_HOST";
     public static final String SKEY_LOCAL_PORT                                   = "LOCAL_PORT";
 
     public static final String SESSION_NAME_INTERNAL                             = "SESSION.INTERNAL";
 
     // The environment name constants
     public static final String EKEY_GLOBALCONFIGURATION                          = "GLOBAL_CONFIGURATION";
     public static final String EKEY_FILESYSTEMPRIVILEGUES                        = "FILESYSTEM_PRIVILEGUES";
 
     
 
     // HTTP status codes
     public static final int HTTP_STATUS_INFORMATIONAL_CONTINUE                   = 100;
     public static final int HTTP_STATUS_INFORMATIONAL_SWITCHING_PROTOCOLS        = 101;
 
     public static final int HTTP_STATUS_SUCCESSFUL_OK                            = 200;
     public static final int HTTP_STATUS_SUCCESSFUL_CREATED                       = 201;
     public static final int HTTP_STATUS_SUCCESSFUL_ACCEPTES                      = 202;
     public static final int HTTP_STATUS_SUCCESSFUL_NON_AUTHORATIVE_INFORMATION   = 203;
     public static final int HTTP_STATUS_SUCCESSFUL_NO_CONTENT                    = 204;
     public static final int HTTP_STATUS_SUCCESSFUL_RESET_CONTENT                 = 205;
     public static final int HTTP_STATUS_SUCCESSFUL_PARTIAL_CONTENT               = 206;
     
     public static final int HTTP_STATUS_REDIRECION_MULTIPLE_CHOICES              = 300;
     public static final int HTTP_STATUS_REDIRECION_MOVED_PERMANENTLY             = 301;
     public static final int HTTP_STATUS_REDIRECION_FOUND                         = 302;
     public static final int HTTP_STATUS_REDIRECION_SEE_OTHER                     = 303;
     public static final int HTTP_STATUS_REDIRECION_NOT_MODIFIED                  = 304;
     public static final int HTTP_STATUS_REDIRECION_USE_PROXY                     = 305;
     //public static final int HTTP_STATUS_REDIRECION_                            = 306;   // Not in use any more
     public static final int HTTP_STATUS_REDIRECION_TEMPORARY_REDIRECT            = 307;
 
     public static final int HTTP_STATUS_CLIENTERROR_BAD_REQUEST                  = 400;
     public static final int HTTP_STATUS_CLIENTERROR_UNAUTHORIZED                 = 401;
     public static final int HTTP_STATUS_CLIENTERROR_PAYMENT_REQUIRED             = 402;
     public static final int HTTP_STATUS_CLIENTERROR_FORBIDDEN                    = 403;
     public static final int HTTP_STATUS_CLIENTERROR_NOT_FOUND                    = 404;
     public static final int HTTP_STATUS_CLIENTERROR_METHOD_NOT_ALLOWED           = 405;
     public static final int HTTP_STATUS_CLIENTERROR_NOT_ACCEPTABLE               = 406;
     public static final int HTTP_STATUS_CLIENTERROR_PROXY_AUTHENTICATION_REQUIRED = 407;
     public static final int HTTP_STATUS_CLIENTERROR_REQUEST_TIMED_OUT            = 408;
     public static final int HTTP_STATUS_CLIENTERROR_CONFLICT                     = 409;
     public static final int HTTP_STATUS_CLIENTERROR_GONE                         = 410;
     public static final int HTTP_STATUS_CLIENTERROR_LENGTH_REQUIRED              = 411;
     public static final int HTTP_STATUS_CLIENTERROR_PRECONDITION_FAILED          = 412;
     public static final int HTTP_STATUS_CLIENTERROR_REQUEST_ENTITY_TOO_LARGE     = 413;
     public static final int HTTP_STATUS_CLIENTERROR_REQUEST_URI_TOO_LONG         = 414;
     public static final int HTTP_STATUS_CLIENTERROR_UNSUPPORTED_MEDIA_TYPE       = 415;
     public static final int HTTP_STATUS_CLIENTERROR_REQUEST_RANGE_NOT_SATISFIABLE = 416;
     public static final int HTTP_STATUS_CLIENTERROR_EXPECTATION_FAILED           = 417;
 
     public static final int HTTP_STATUS_SERVERERROR_INTERNAL_SERVER_ERROR        = 500;
     public static final int HTTP_STATUS_SERVERERROR_NOT_IMPLEMENTED              = 501;
     public static final int HTTP_STATUS_SERVERERROR_BAD_GATEWAY                  = 502;
     public static final int HTTP_STATUS_SERVERERROR_SERVICE_UNAVAILABLE          = 503;
     public static final int HTTP_STATUS_SERVERERROR_GATEWAY_TIMEOUT              = 504;
     public static final int HTTP_STATUS_SERVERERROR_HTTP_VERSION_NOT_SUPPORTED   = 505;
 
 
 }
