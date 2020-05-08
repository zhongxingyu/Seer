 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 
 package org.amplafi.flow.web.services;
 
 
import static com.sworddance.util.CUtilities.isNotEmpty;

 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
import org.amplafi.flow.FlowNotFoundException;
 import org.amplafi.flow.web.BaseFlowService;
import org.amplafi.flow.web.FlowRedirectException;
 import org.amplafi.flow.web.FlowRequest;
 import org.amplafi.flow.web.FlowService;
import org.amplafi.flow.web.FlowWebUtils;
import org.amplafi.json.JSONArray;
 import org.apache.commons.lang.StringUtils;
 import org.apache.tapestry.IRequestCycle;
 import org.apache.tapestry.PageNotFoundException;
 import org.apache.tapestry.PageRedirectException;
 import org.apache.tapestry.RedirectException;
 import org.apache.tapestry.engine.IEngineService;
 import org.apache.tapestry.util.ContentType;
 import org.apache.tapestry.web.WebRequest;
 import org.apache.tapestry.web.WebResponse;
 
 import com.sworddance.util.CUtilities;
 
 
 /**
  * Base implementation for Tapestry services that start / continue flows.
  *
  */
 public abstract class EngineFlowService extends BaseFlowService implements FlowService, IEngineService {
     
 	private WebRequest request;
     private WebResponse response;
 	private String name;
 	private HttpServletRequest httpServletRequest;
     
     @Override
     @SuppressWarnings("unchecked")
     public void service(final IRequestCycle cycle) throws IOException {
    	FlowRequest flowRequest = new FlowRequest() {
 			
 			@Override
 			public String getParameter(String parameterName) {
 				return cycle.getParameter(parameterName);
 			}
 
 			@Override
 			public PrintWriter getWriter() {
 				return EngineFlowService.this.getWriter(cycle);
 			}
 
 			@Override
 			public List<String> getParameterNames() {
 				return request.getParameterNames();
 			}
 
 			@Override
 			public String getReferingUri() {
 				return EngineFlowService.this.getReferingUri();
 			}
 
 			@Override
 			public Iterable<String> getIterableParameter(String parameterName) {
 				//HACK cycle.getParameters returns a glued comma separated string for some reason.
 				String[] parameterValues = httpServletRequest.getParameterValues(parameterName);
 //				String[] parameterValues = cycle.getParameters(parameterName);
 				return CUtilities.isEmpty(parameterValues)? Collections.<String>emptyList() : Arrays.asList(parameterValues);
 			}
 		};
 		try {
 			service(flowRequest);
 		} catch (PageRedirectException e) {
 			throw e;
	     } catch (PageNotFoundException e) {
             throw e;
 		} catch (RedirectException e) {
 			throw e;
		} catch (FlowNotFoundException e) {
			throw new PageNotFoundException(e.getMessage());
		} catch (FlowRedirectException e) {
			if(e.getFlowState() == null){
				throw new PageRedirectException(e.getPage());
			} else {
			    FlowWebUtils.activatePageIfNotNull(null, e.getPage(), e.getFlowState());
			}
 		} catch (Exception e) {
 			getLog().info(getReferingUri(), e);
 		}
 	}
 
 	private String getReferingUri() {
 	    String referingUriStr = httpServletRequest.getHeader("Referer");
 	    if(StringUtils.isNotBlank(referingUriStr)){
 	        URI referingUri;
 	        try {
 	            referingUri = new URI(referingUriStr);
 	            return referingUri.toString();
 	        } catch (URISyntaxException e) {
 	            // ignore bad uri
 	        }
 	    }
 	    return null;
 	}
     
 	protected PrintWriter getWriter(IRequestCycle cycle) {
 	    ContentType contentType = new ContentType(SCRIPT_CONTENT_TYPE);
 	
 	    String encoding = contentType.getParameter("charset");
 	
 	    try {
 	        if (encoding == null) {
 	            encoding = cycle.getEngine().getOutputEncoding();
 	            contentType.setParameter("charset", encoding);
 	        }
 	        return getResponse().getPrintWriter(contentType);
 	    } catch (NullPointerException nullPointerException) {
 	        // can happen if the cycle is not available (called in a headless/ non-tapestry way. )
 	        return null;
 	    } catch (IOException e) {
 	    	throw new IllegalStateException(e);
 	    }
 	    }
 
 	@Override
 	public String getName() {
 	    return name;
 	}	    
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	    
 	public void setResponse(WebResponse response) {
 	    this.response = response;
 	}
 
 	public WebResponse getResponse() {
 	    return response;
 	}
 
 	public WebRequest getRequest() {
 	    return request;
 	}
 
 	public void setRequest(WebRequest request) {
 	    this.request = request;
 	}
 
 	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
 	    this.httpServletRequest = httpServletRequest;
 	}
 
 	public HttpServletRequest getHttpServletRequest() {
 	    return httpServletRequest;
 	}
 }
