 /*******************************************************************************
  *  Copyright 2007 Ketan Padegaonkar http://ketan.padegaonkar.name
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
  ******************************************************************************/
 package net.sourceforge.jcctray.model;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 
 /**
  * @author Ketan Padegaonkar
  */
 public class CCNet extends HTTPCruise implements ICruise {
 
 	protected void configureMethod(HttpMethod method, DashBoardProject project) {
 		PostMethod post = (PostMethod) method;
 		post.addParameter("forcebuild", "true");
 		post.addParameter("forceBuildServer", "local");
 		post.addParameter("ForceBuild", "Force");
 		post.addParameter("forceBuildProject", project.getName());
 	}
 
 	protected String forceBuildURL(DashBoardProject project) {
 		return project.getHost().getHostName() + "/ViewFarmReport.aspx";
 	}
 
 	public String formatDate(String date) {
 		try {
			String theDate = date.replaceAll("\\.\\d{3,}+", "");
 			theDate = theDate.replaceAll(":", "");
 			Date parse = new SimpleDateFormat("yyyy-MM-dd'T'HHmmssZ").parse(theDate);
 			return new SimpleDateFormat("h:mm:ss a, dd MMM").format(parse);
 		} catch (Exception e) {
 			getLog().error("Could not parse date: " + date);
 		}
 		return date;
 	}
 
 	public String getName() {
 		return "CruiseControl.NET";
 	}
 
 	protected String getSuccessMessage(DashBoardProject project) {
 		return "Build successfully forced for " + project.getName();
 	}
 
 	protected String getXmlReportURL(Host host) {
 		return host.getHostName() + "/XmlStatusReport.aspx";
 	}
 
 	protected HttpMethod httpMethod(DashBoardProject project) {
 		HttpMethod method = new PostMethod(forceBuildURL(project));
 		configureMethod(method, project);
 		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
 		return method;
 	}
 }
