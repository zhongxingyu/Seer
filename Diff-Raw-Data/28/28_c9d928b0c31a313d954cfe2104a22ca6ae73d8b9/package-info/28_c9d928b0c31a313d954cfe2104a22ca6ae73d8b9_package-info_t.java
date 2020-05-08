 /**
  * Simple embeddable socket/http server implementation. It is not a complete web server
 * implementation, but serves most purposes. It is easy to extend/use - just start it up and mount
 * "listeners".
  * 
  * You can use it as a plain socket server (i.e. ftp or something) OR, just mount the LightCmdGET
  * and it becomes an http server with SOA support.
  * 
 * Features:
 * <li>bind java code to URL
 * <li>parse URL into map of values and viceversa
 * <li>handles multithreaded request processing
 * <li>handles simple authentication/authorization
 * 
  * The basic serving logic is in LightCmdGET: it can either serve a file from URL (classpath, disk)
  * or call a SOA method/service.
  * 
  * See examples in ./test See ../lightsoa for the SOA support. See ../assets for SOA asset support.
  * 
  * <p>
  * Testing code is a lot easier when you can start the server inside the test code rather than as a
  * separate process, see the tests for the light server.
  * 
  * @version $Id: package-info.java,v 1.1 2007-10-02 11:54:36 razvanc Exp $
  */
 package com.razie.pub.http;
 
