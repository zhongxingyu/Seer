 /**
  * Copyright 2011 DALLAS Alexandros
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package gr.gsis.wsnp;
 
 import java.math.BigDecimal;
 import javax.xml.ws.Holder;
 
 public class WSNPProxy {
 	
 	protected RgWsBasStoixN service;
 	protected String endpoint;
 	
 	public WSNPProxy(String endpoint) {
 		this.endpoint = endpoint;
 		service = new RgWsBasStoixN_Service().getRgWsBasStoixNSoapHttpPort();
 		setEndpoint(this.endpoint);
 	}
 
 	public String rgWsBasStoixNVersionInfo() {
 		return service.rgWsBasStoixNVersionInfo();
 	}
 	
 	public WSNPResponse rgWsBasStoixN(String afm) {
 		// pErrorRecOut
 		Holder<GenWsErrorRtUser> pErrorRecOut = new Holder<GenWsErrorRtUser>();
 		GenWsErrorRtUser genWsErrorRtUser = new GenWsErrorRtUser();
 		pErrorRecOut.value = genWsErrorRtUser;
 		
 		// pBasStoixNRecOut
 		Holder<RgWsBasStoixNRtUser> pBasStoixNRecOut = new Holder<RgWsBasStoixNRtUser>();
 		RgWsBasStoixNRtUser rgWsBasStoixNRtUser = new RgWsBasStoixNRtUser();
 		rgWsBasStoixNRtUser.setRegistDate("2011-01-01");
 		rgWsBasStoixNRtUser.setStopDate("2011-01-01");
 		rgWsBasStoixNRtUser.setDeactivationFlag("1");
 		rgWsBasStoixNRtUser.setFacActivity(BigDecimal.ZERO);
 		pBasStoixNRecOut.value = rgWsBasStoixNRtUser;
 		
 		// pCallSeqIdOut
 		Holder<BigDecimal> pCallSeqIdOut = new Holder<BigDecimal>();
 		pCallSeqIdOut.value = BigDecimal.ZERO;
 		
 		// Fetch the response
 		service.rgWsBasStoixN(afm, pBasStoixNRecOut, pCallSeqIdOut, pErrorRecOut);
 		WSNPResponse response = new WSNPResponse();
 		
 		response.pErrorRecOut = pErrorRecOut.value;
 		response.pBasStoixNRecOut = pBasStoixNRecOut.value;
 		response.pCallSeqIdOut = pCallSeqIdOut.value;
 		
 		return response;
 	}
 	
 	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
 		((javax.xml.ws.BindingProvider)service).getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
 	}
 	
 	public String getEndpoint() {
 		return this.endpoint;
 	}
 }
