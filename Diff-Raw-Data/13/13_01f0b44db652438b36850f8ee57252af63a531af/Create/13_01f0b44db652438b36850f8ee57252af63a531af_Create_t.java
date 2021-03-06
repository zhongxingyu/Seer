 /*******************************************************************************
  * Copyright (c) 2012, Institute for Pervasive Computing, ETH Zurich.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the Institute nor the names of its contributors
  *    may be used to endorse or promote products derived from this software
  *    without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * 
  * This file is part of the Californium (Cf) CoAP framework.
  ******************************************************************************/
 package ch.ethz.inf.vs.californium.examples.plugtest;
 
import ch.ethz.inf.vs.californium.coap.DELETERequest;
 import ch.ethz.inf.vs.californium.coap.GETRequest;
 import ch.ethz.inf.vs.californium.coap.PUTRequest;
 import ch.ethz.inf.vs.californium.coap.registries.CodeRegistry;
 import ch.ethz.inf.vs.californium.coap.registries.MediaTypeRegistry;
 import ch.ethz.inf.vs.californium.endpoint.resources.LocalResource;
 
 /**
  * This resource implements a test of specification for the ETSI IoT CoAP Plugtests, Paris, France, 24 - 25 March 2012.
  * 
  * @author Matthias Kovatsch
  */
 public class Create extends LocalResource {
 	
 	private boolean isCreated = false;
 
 	public Create(String name) {
 		super(name);
 		setTitle("Resource which does not exist yet (to perform atomic PUT)");
 		isHidden(true);
 	}
 	
 	@Override
 	public void performPUT(PUTRequest request) {
 		if (isCreated) {
 			request.respond(CodeRegistry.RESP_PRECONDITION_FAILED);
 		} else {
 			isCreated = true;
 			isHidden(false);
 			request.respond(CodeRegistry.RESP_CREATED);
 		}
 	}
 	
 	@Override
 	public void performGET(GETRequest request) {
 		if (isCreated) {
 			request.respond(CodeRegistry.RESP_CONTENT, "Exists", MediaTypeRegistry.TEXT_PLAIN);
 		} else {
 			request.respond(CodeRegistry.RESP_NOT_FOUND);
 		}
 	}

 	@Override
	public void performDELETE(DELETERequest request) {
 		isCreated = false;
 		isHidden(true);
 		request.respond(CodeRegistry.RESP_DELETED);
 	}
 }
