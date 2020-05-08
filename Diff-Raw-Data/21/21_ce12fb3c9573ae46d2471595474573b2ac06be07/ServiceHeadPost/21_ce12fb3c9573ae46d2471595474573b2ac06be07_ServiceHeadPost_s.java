 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.tools.webservice;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.concurrent.Semaphore;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.webui.WebUIException;
 import org.meandre.webui.WebUIFragmentCallback;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 
 /**
  * This class implements a component that using the WebUI accepts post requests
  *
  * @author Xavier Llor&agrave;
  * @author Boris Capitanu
  */
 
 @Component(
 		creator = "Xavier Llora",
 		description = "Service head for a service that gets data via posts",
 		name = "Service Head Post",
 		tags = "WebUI, post, process request",
 		rights = Licenses.UofINCSA,
 		mode = Mode.webui,
 		firingPolicy = FiringPolicy.all,
 		baseURL = "meandre://seasr.org/components/tools/"
 )
 public class ServiceHeadPost extends AbstractExecutableComponent
     implements WebUIFragmentCallback {
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			description = "A map object containing the key elements of the request and the associated values",
 			name = Names.PORT_REQUEST_DATA
 	)
 	protected static final String OUT_REQUEST = Names.PORT_REQUEST_DATA;
 
 	@ComponentOutput(
 			description = "The response to be sent to the Service Tail Post.",
 			name = Names.PORT_RESPONSE_HANDLER
 	)
 	protected static final String OUT_RESPONSE = Names.PORT_RESPONSE_HANDLER;
 
 	@ComponentOutput(
 			description = "The semaphore to signal the response was sent.",
 			name = Names.PORT_SEMAPHORE
 	)
 	protected static final String OUT_SEMAPHORE = Names.PORT_SEMAPHORE;
 
 
     //--------------------------------------------------------------------------------------------
 
 	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	}
 
 	public void executeCallBack(ComponentContext cc) throws Exception {
 	    cc.startWebUIFragment(this);
 
 		console.info("Starting service head for " + cc.getFlowID());
 
 		while (!cc.isFlowAborting())
 			Thread.sleep(1000);
 
 		console.info("Abort request received for " + cc.getFlowID());
 
 		cc.stopWebUIFragment(this);
 	}
 
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
 	// -------------------------------------------------------------------------
 
 	public void emptyRequest(HttpServletResponse response) throws WebUIException {
 		try {
 			console.warning("Empty request received");
 			response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
 		} catch (IOException e) {
 			throw new WebUIException(e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException {
 		console.info("Request received from " + request.getRemoteHost()
 				+ " (" + request.getRemoteAddr() + ":" + request.getRemotePort() + ")"
 				+ ((request.getRemoteUser() != null) ? " [" + request.getRemoteUser() + "]" : ""));
 
 		Map<String,byte[]> map = new Hashtable<String,byte[]>();
 		Enumeration mapRequest = request.getParameterNames();
 		while ( mapRequest.hasMoreElements() ) {
 			String sName = mapRequest.nextElement().toString();
 			String [] sa = request.getParameterValues(sName);
 			String sAcc = "";
 			for ( String s:sa ) sAcc+=s;
			map.put(sName, sAcc.getBytes());
 		}
 
 		try {
 			Semaphore sem = new Semaphore(1, true);
 			sem.acquire();
 			componentContext.pushDataComponentToOutput(OUT_REQUEST, BasicDataTypesTools.mapToByteMap(map));
 			componentContext.pushDataComponentToOutput(OUT_RESPONSE, response);
 			componentContext.pushDataComponentToOutput(OUT_SEMAPHORE, sem);
 			sem.acquire();
 			sem.release();
 		} catch (InterruptedException e) {
 			throw new WebUIException(e);
 		} catch (ComponentContextException e) {
 			throw new WebUIException(e);
 		}
 	}
 }
