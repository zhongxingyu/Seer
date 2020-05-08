 /*
     Copyright 2013, Strategic Gains, Inc.
 
 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at
 
 		http://www.apache.org/licenses/LICENSE-2.0
 
 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
 */
 package com.strategicgains.restexpress.plugin.swagger;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.restexpress.domain.metadata.RouteMetadata;
import org.restexpress.domain.metadata.UriMetadata;
 
 /**
  * @author toddf
  * @since Nov 21, 2013
  */
 public class ApiOperation
 {
 	private String method;
 	private String nickname;
 	private String type;		// return type
 	private List<ApiParameters> parameters;
 	private String summary;
 	private String notes;
 	private String[] errorResponses;
 
 	public ApiOperation(String method, RouteMetadata route)
     {
 		this.method = method;
 //		this.nickname = route.getName();
		UriMetadata metadata = route.getUri();
 		
		if (metadata.getParameters() == null) return;

 		for (String param : route.getUri().getParameters())
 		{
 			if (parameters == null)
 			{
 				parameters = new ArrayList<ApiParameters>();
 			}
 
 			parameters.add(new ApiParameters("path", param, "string", !param.equals("format")));
 		}
     }
 }
