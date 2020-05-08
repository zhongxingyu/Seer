 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.exception.error;
 
 /**
  * Gravifon service internal error.<p>
  * Characterized by its {@link #getErrorCode() error code} and associated {@link #getHttpStatusCode() http status code}.
  *
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
 public enum GravifonError {
 
     /**
      * Unexpected error. Indicates that something wrong happened that really shouldn't as unrealistic.
      */
     UNEXPECTED(999, 500),
     /**
      * Internal error. Usually means there's a bug or runtime error.
      */
     INTERNAL(1000, 500),
     /**
      * Basic database operation error. The cause is not specified or unknown.
      */
     DATABASE_OPERATION(1001, 500),
     /**
      * JSON processing operation error. Some data binding error took place.
      */
     JSON_PROCESSING_OPERATION(1002, 500),
     /**
      * User not found error. Database reported that requested user document is not found.
      */
    USER_NOT_FOUND(2000, 404),
     /**
      * User already exists error. User document with particular username already exists in database.
      */
    USER_EXISTS(2001, 409),
     /**
      * Required property error. Property is missing.
      */
    REQUIRED(10001, 400),
     /**
      * Invalid property error. Property value is invalid.
      */
    INVALID(10002, 400),
     /**
      * Unknown property value error. Property value is unknown. Applicable to enum restricted properties.
      */
    UNKNOWN(10003, 400);
     
     /**
      * @see #getErrorCode()
      */
     private int errorCode;
     
     /**
      * @see #getHttpStatusCode()
      */
     private int httpStatusCode;
 
     /**
      * Constructs Gravifon service internal error.
      *
      * @param errorCode internal error code
      * @param httpStatusCode http status code
      */
     private GravifonError(int errorCode, int httpStatusCode) {
         this.errorCode = errorCode;
         this.httpStatusCode = httpStatusCode;
     }
 
     /**
      * Returns internal error code.
      *
      * @return error code
      */
     public int getErrorCode() {
         return errorCode;
     }
 
     /**
      * Returns http status code.
      *
      * @return http status code
      */
     public int getHttpStatusCode() {
         return httpStatusCode;
     }
     
 }
