 package com.paypal.core;
 
 public class Constants {
 	public static final String ENCODING_FORMAT = "UTF8";
 	public static final String EMPTY_STRING = "";
 	public static final String ACCCOUT_PREFIX = "acct";
 
	public static final String PAYLOAD_FORMAT_SOAP = "SOAP";
	public static final String PAYLOAD_FORMAT_NVP = "NV";
 	public static final String SDK_NAME = "";
 	public static final String SDK_VERSION = "";
 
 	// HTTP Header Constants
 	// PayPal Security UserId Header
 	public static final String PAYPAL_SECURITY_USERID_HEADER = "X-PAYPAL-SECURITY-USERID";
 
 	// PayPal Security Password Header
 	public static final String PAYPAL_SECURITY_PASSWORD_HEADER = "X-PAYPAL-SECURITY-PASSWORD";
 
 	// PayPal Security Signature Header
 	public static final String PAYPAL_SECURITY_SIGNATURE_HEADER = "X-PAYPAL-SECURITY-SIGNATURE";
 
 	// PayPal Platform Authorization Header
 	public static final String PAYPAL_AUTHORIZATION_PLATFORM_HEADER = "X-PAYPAL-AUTHORIZATION";
 
 	// PayPal Merchant Authorization Header
 	public static final String PAYPAL_AUTHORIZATION_MERCHANT_HEADER = "X-PP-AUTHORIZATION";
 
 	// PayPal Application ID Header
 	public static final String PAYPAL_APPLICATION_ID_HEADER = "X-PAYPAL-APPLICATION-ID";
 
 	// PayPal Request Data Header
 	public static final String PAYPAL_REQUEST_DATA_FORMAT_HEADER = "X-PAYPAL-REQUEST-DATA-FORMAT";
 
 	// PayPal Request Data Header
 	public static final String PAYPAL_RESPONSE_DATA_FORMAT_HEADER = "X-PAYPAL-RESPONSE-DATA-FORMAT";
 
 	// PayPal Request Source Header
 	public static final String PAYPAL_REQUEST_SOURCE_HEADER = "X-PAYPAL-REQUEST-SOURCE";
 
 	// PayPal Device IP Address Header
 	public static final String PAYPAL_DEVICE_IPADDRESS_HEADER = "X-PAYPAL-DEVICE-IPADDRESS";
 
 	// PayPal Sandbox Email Address for AA Header
 	public static final String PAYPAL_SANDBOX_EMAIL_ADDRESS_HEADER = "X-PAYPAL-SANDBOX-EMAIL-ADDRESS";
 
 	// Constants key defined for configuration options in application properties
 	// End point
 	public static final String END_POINT = "service.EndPoint";
 
 	// Use Google App Engine
 	public static final String GOOGLE_APP_ENGINE = "http.GoogleAppEngine";
 
 	// Use HTTP Proxy
 	public static final String USE_HTTP_PROXY = "http.UseProxy";
 
 	// HTTP Proxy host
 	public static final String HTTP_PROXY_HOST = "http.ProxyHost";
 
 	// HTTP Proxy port
 	public static final String HTTP_PROXY_PORT = "http.ProxyPort";
 
 	// HTTP Proxy username
 	public static final String HTTP_PROXY_USERNAME = "http.ProxyUserName";
 
 	// HTTP Proxy password
 	public static final String HTTP_PROXY_PASSWORD = "http.ProxyPassword";
 
 	// HTTP Connection Timeout
 	public static final String HTTP_CONNECTION_TIMEOUT = "http.ConnectionTimeOut";
 
 	// HTTP Connection Retry
 	public static final String HTTP_CONNECTION_RETRY = "http.Retry";
 
 	// HTTP Read timeout
 	public static final String HTTP_CONNECTION_READ_TIMEOUT = "http.ReadTimeOut";
 
 	// HTTP Max Connections
 	public static final String HTTP_CONNECTION_MAX_CONNECTION = "http.MaxConnection";
 
 	// HTTP Device IP Address Key
 	public static final String DEVICE_IP_ADDRESS = "http.IPAddress";
 
 	// Credential Username suffix
 	public static final String CREDENTIAL_USERNAME_SUFFIX = ".UserName";
 
 	// Credential Password suffix
 	public static final String CREDENTIAL_PASSWORD_SUFFIX = ".Password";
 
 	// Credential Application ID
 	public static final String CREDENTIAL_APPLICATIONID_SUFFIX = ".AppId";
 
 	// Credential Subject
 	public static final String CREDENTIAL_SUBJECT_SUFFIX = ".Subject";
 
 	// Credential Signature
 	public static final String CREDENTIAL_SIGNATURE_SUFFIX = ".Signature";
 
 	// Credential Certificate Path
 	public static final String CREDENTIAL_CERTPATH_SUFFIX = ".CertPath";
 
 	// Credential Certificate Key
 	public static final String CREDENTIAL_CERTKEY_SUFFIX = ".CertKey";
 
 	// Sandbox Email Address Key
 	public static final String SANDBOX_EMAIL_ADDRESS = "sandbox.EmailAddress";
 
 }
