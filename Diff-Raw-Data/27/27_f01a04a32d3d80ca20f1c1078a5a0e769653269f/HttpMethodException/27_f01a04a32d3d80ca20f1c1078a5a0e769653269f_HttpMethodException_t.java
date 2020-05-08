 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.gooddata.exception;
 
import java.util.Formatter;

 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 
import org.apache.commons.httpclient.HttpMethod;
 
 /**
  * @author jiri.zaloudek
  */
 public class HttpMethodException extends GdcRestApiException {
 
     private HttpMethod guiltyMethod = null;
 
     /**
      * Constructs an instance of <code>HttpMethodException</code> with the specified detail message.
      *
      * @param msg the detail message.
      */
     public HttpMethodException(String msg) {
         super(msg);
     }
 
     /**
      * Returns an error message for <code>HttpMethodException</code>, attempting to
      * use GoodData error message or HTTP status line for an exception that was constructed
      * from a response to HTTP method call.
      */
     public String getMessage() {
         if (guiltyMethod == null)
             return super.getMessage();
 
         String msg = guiltyMethod.getStatusCode() + " " + guiltyMethod.getStatusText();
         String body = null;
         try {
             body = guiltyMethod.getResponseBodyAsString();
         } catch (java.io.IOException ioexception) {
             /* No body? No problem, msg is already set fine. */
         }
         if (body != null) {
             try {
                 JSONObject error = JSONObject.fromObject(body);
                 /* Error structure sometimes lacks the tag... */
                 if (error.has("error"))
                     error = error.getJSONObject("error");
                msg = formatErrorMessage(error);
             } catch (JSONException jsone) {
                 /* Do not worry about the non-standard or broken
                  * error JSON. The msg is already meaningful enough.*/
             }
         }
         return msg;
     }
 
     /**
     * Returns formatted error message if the message contains parameters
     * placeholder (%s)
     *
     * @param error
     * @return
     */
    private static String formatErrorMessage(JSONObject error) {
    	final String orig = error.getString("message");
    	if (!orig.contains("%s")) {
    		return orig;
    	}
	    return new Formatter().format(orig, error.getJSONArray("parameters").toArray()).toString();
	}

	/**
      * Returns the request id for <code>HttpMethodException</code> that was constructed
      * from a response to HTTP method call.
      */
     public String getRequestId() {
         try {
             return guiltyMethod.getResponseHeader("X-GDC-Request").getValue();
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Constructs an instance of <code>HttpMethodException</code> for a response to HTTP method call,
      * interpreting GoodData error structures.
      *
      * @param method    the call
      * @param throwable original exception
      */
     public HttpMethodException(HttpMethod method, Throwable throwable) {
         super(throwable);
         guiltyMethod = method;
     }
 
     /**
      * Constructs an instance of <code>HttpMethodException</code> for a response to HTTP method call,
      * interpreting GoodData error structures.
      *
      * @param method the call
      */
     public HttpMethodException(HttpMethod method) {
         super((String) null);
         guiltyMethod = method;
     }
 
     public HttpMethodException(String msg, Throwable e) {
         super(msg, e);
     }
 }
