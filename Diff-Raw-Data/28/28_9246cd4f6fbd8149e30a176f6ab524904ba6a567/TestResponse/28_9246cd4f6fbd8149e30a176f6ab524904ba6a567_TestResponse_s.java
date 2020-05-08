 package me.fumiz.android.test.http;
 
 /**
  * HTTP Response Container
  * User: fumiz
  * Date: 11/09/26
  * Time: 21:11
  */
 public class TestResponse {
     /**
      * MIME type text/plain
      */
     public static final String MIME_TEXTPLAIN = "text/plain";
     /**
      * MIME type text/html
      */
     public static final String MIME_TEXTHTML = "text/html";
     /**
      * MIME type application/json
      */
     public static final String MIME_JSON = "application/json";
 
     /**
      * 200 OK
      */
     public static final String HTTP_OK = "200 OK";
     /**
      * 400 Bad Request
      */
     public static final String HTTP_BAD_REQUEST = "400 Bad Request";
     /**
      * 401 Unauthorized
      */
     public static final String HTTP_UNAUTHORIZED = "401 Unauthorized";
     /**
      * 403 Forbidden
      */
     public static final String HTTP_FORBIDDEN = "403 Forbidden";
     /**
      * 404 Not Found
      */
     public static final String HTTP_NOTFOUND = "404 Not Found";
 
     /**
      * MIME type
      */
     private final String mMimeType;
     /**
      * Response Body
      */
     private final String mBody; // expect Stream
     /**
      * Statsu Code
      */
     private final String mStatusCode;
 
     /**
      * initialize with MIME type and Response Body. Status code is fixed by 200 OK.
      * @param mimeType MIME type
      * @param body Response Body
      */
     public TestResponse(String mimeType, String body) {
         this(HTTP_OK, mimeType, body);
     }
 
     /**
      * initialize with Status code, MIME type and Response Body.
      * @param statusCode Status Code
      * @param mimeType MIME type
      * @param body Response Body
      */
     public TestResponse(String statusCode, String mimeType, String body) {
         mStatusCode = statusCode;
         mMimeType = mimeType;
         mBody = body;
     }
 
     /**
      * Get MIME type
      * @return MIME type string
      */
     public String getMimeType() {
         return mMimeType;
     }
 
     /**
      * Get Response Body
      * @return response body string
      */
     public String getBody() {
         return mBody;
     }
 
     /**
      * Get Status Code
      * @return Status code
      */
     public String getStatusCode() {
         return mStatusCode;
     }
 }
