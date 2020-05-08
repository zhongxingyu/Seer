 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.services.ws;
 
 /**
  * This interface is a base interface for Bug services.
  * 
  * use PublicWSProvider2, this interface maintained for compatibility.
  * 
  * @author ken
  * 
  */
 public interface PublicWSProvider {
 	public static final int GET = 1;
 
 	public static final int PUT = 2;
 
 	public static final int POST = 3;
 
 	public static final int DELETE = 4;
 
 	public static final String PACKAGE_ID = "com.buglabs.service.ws";
 
 	/**
 	 * @param operation
 	 *            HTTP operation. See IPublicServiceProvider.GET, etc.
	 * @return The description of what the service requires and provides, or null of service does not support the passed operation.
 	 */
 	public PublicWSDefinition discover(int operation);
 
 	/**
 	 * Execute a service. This is a proxy to a native OSGi style service.
 	 * 
 	 * @param operation
 	 *            PublicWSProvider.GET, .PUT, .POST, .DELETE
 	 * @param input
 	 * @return
 	 */
 	public IWSResponse execute(int operation, String input);
 
 	/**
 	 * @return Name that this service uses.
 	 */
 	public String getPublicName();
 
 	/**
 	 * @return A brief description of the service.
 	 */
 	public String getDescription();
 }
