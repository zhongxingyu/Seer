 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans;
 
 import org.mule.ibeans.api.client.Call;
 import org.mule.ibeans.api.client.ExceptionListenerAware;
 import org.mule.ibeans.api.client.Template;
 import org.mule.ibeans.api.client.params.UriParam;
 
 import java.net.UnknownHostException;
 
 /**
  * A test bean that uses an exception listener rather than declaring exceptions on all the method calls
  */
 
 public interface TestUriIBean extends ExceptionListenerAware
 {
     @UriParam("do_something_uri")
     public static final String DO_SOMETHING_URI = "http://doesnotexist.bom?param1=";
 
     @Template("{do_something_uri}{foo}")
     public String doSomething(@UriParam("foo") String foo);
 
     @Template("{do_something_uri}{foo}&param2={bar}")
     public String doSomethingElse(@UriParam("foo") String foo, @UriParam("bar") String bar) throws UnknownHostException;
 
    @Call(uri = "{do_something_uri}")
     public String doSomethingNoParams() throws Exception;
 }
