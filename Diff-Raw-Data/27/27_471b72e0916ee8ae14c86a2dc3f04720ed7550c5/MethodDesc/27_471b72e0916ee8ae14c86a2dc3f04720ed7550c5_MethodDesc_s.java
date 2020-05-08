 package com.pehrs.spring.api.doc.model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import com.sun.javadoc.AnnotationDesc;
 
 /**
  * A REST Method/Service
  * 
  * @author matti
  *
  */
 public class MethodDesc {
 
 	String name;
 	String requestMappingUrl;
 	String requestMappingMethod;
 	
 	String responseJSONSample = "";
 	String requestJSONSample = "";
 
 	String commentText = "";
 
 	AnnotationDesc[] annotations;
 	
 	List<ParameterDesc> parameters = new ArrayList<ParameterDesc>();
 
 	
 	/**
 	 * @return the annotations
 	 */
 	public AnnotationDesc[] getAnnotations() {
 		return annotations;
 	}
 
 	/**
 	 * @param annotations the annotations to set
 	 */
 	public void setAnnotations(AnnotationDesc[] annotations) {
 		this.annotations = annotations;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getRequestMappingUrl() {
 		return requestMappingUrl;
 	}
 
 	public void setRequestMappingUrl(String requestMappingUrl) {
 		this.requestMappingUrl = requestMappingUrl;
 	}
 
 	public String getRequestMappingMethod() {
 		return requestMappingMethod;
 	}
 
 	public void setRequestMappingMethod(String requestMappingMethod) {
 		this.requestMappingMethod = requestMappingMethod;
 	}
 
 	public String getResponseJSONSample() {
 		return responseJSONSample;
 	}
 
 	public void setResponseJSONSample(String responseJSONSample) {
 		this.responseJSONSample = responseJSONSample;
 	}
 
 	public String getRequestJSONSample() {
 		return requestJSONSample;
 	}
 
 	public void setRequestJSONSample(String requestJSONSample) {
 		this.requestJSONSample = requestJSONSample;
 	}
 
 	public void addParameter(ParameterDesc parameter) {
 		parameters.add(parameter);
 	}
 
 	public List<ParameterDesc> getParameters() {
 		return parameters;
 	}
 
 	public void setParameters(List<ParameterDesc> parameters) {
 		this.parameters = parameters;
 	}
 
 	public Iterator<ParameterDesc> getParametersIterator() {
 		return parameters.iterator();
 	}
 	
 	public Iterator<ParameterDesc> getRequestParameterIterator() {
 		ArrayList<ParameterDesc> list = new ArrayList<ParameterDesc>();
 		
 		for(ParameterDesc parameter: parameters) {
 			if(parameter.isRequestParam()) {
 				list.add(parameter);
 			}
 		}
 		
 		return list.iterator();
 	}
 	public boolean hasRequestParameters() {		
 		for(ParameterDesc parameter: parameters) {
 			if(parameter.isRequestParam()) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public Iterator<ParameterDesc> getPathVariableIterator() {
 		ArrayList<ParameterDesc> list = new ArrayList<ParameterDesc>();
 		
 		for(ParameterDesc parameter: parameters) {
 			if(parameter.isPathVariable()) {
 				list.add(parameter);
 			}
 		}
 		
 		return list.iterator();
 	}
 	public boolean hasPathVariables() {
 		
 		for(ParameterDesc parameter: parameters) {
 			if(parameter.isPathVariable()) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public ParameterDesc getRequestBodyParameter() {
 		for(ParameterDesc parameter: parameters) {
 			if(parameter.isRequestBody()) {
 				return parameter;
 			}
 		}
 		
 		return null;
 	}
 
 	public boolean hasRequestBodyParameter() {
 		for(ParameterDesc parameter: parameters) {
 			if(parameter.isRequestBody()) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public String getCommentText() {
 		return commentText;
 	}
 
 	public void setCommentText(String commentText) {
 		this.commentText = commentText;
 	}
 	
 	public String getCommentTextFirstSentence() {
 		if(commentText==null) {
 			return "";
 		}
 		int index = commentText.indexOf('@');
 		if(index!=-1) {
			return commentText.substring(0, index + 1);
 		}
		return commentText;
 	}
 
 	public String getCommentTextWithNoAnnotations() {
 		if(commentText==null) {
 			return "";
 		}
 		
 		StringBuilder out = new StringBuilder();
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new StringReader(commentText));
 			for(String line = in.readLine(); line!=null;line=in.readLine()) {
 				String trimmedLine = line.trim();
 				if(!trimmedLine.startsWith("@")) {
 					out.append(line).append("\n");
 				}
 			}
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			try {
 				if(in!=null) {
 					in.close();
 				}
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return out.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + Arrays.hashCode(annotations);
 		result = prime * result
 				+ ((commentText == null) ? 0 : commentText.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result
 				+ ((parameters == null) ? 0 : parameters.hashCode());
 		result = prime
 				* result
 				+ ((requestJSONSample == null) ? 0 : requestJSONSample
 						.hashCode());
 		result = prime
 				* result
 				+ ((requestMappingMethod == null) ? 0 : requestMappingMethod
 						.hashCode());
 		result = prime
 				* result
 				+ ((requestMappingUrl == null) ? 0 : requestMappingUrl
 						.hashCode());
 		result = prime
 				* result
 				+ ((responseJSONSample == null) ? 0 : responseJSONSample
 						.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		MethodDesc other = (MethodDesc) obj;
 		if (!Arrays.equals(annotations, other.annotations)) {
 			return false;
 		}
 		if (commentText == null) {
 			if (other.commentText != null) {
 				return false;
 			}
 		} else if (!commentText.equals(other.commentText)) {
 			return false;
 		}
 		if (name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!name.equals(other.name)) {
 			return false;
 		}
 		if (parameters == null) {
 			if (other.parameters != null) {
 				return false;
 			}
 		} else if (!parameters.equals(other.parameters)) {
 			return false;
 		}
 		if (requestJSONSample == null) {
 			if (other.requestJSONSample != null) {
 				return false;
 			}
 		} else if (!requestJSONSample.equals(other.requestJSONSample)) {
 			return false;
 		}
 		if (requestMappingMethod == null) {
 			if (other.requestMappingMethod != null) {
 				return false;
 			}
 		} else if (!requestMappingMethod.equals(other.requestMappingMethod)) {
 			return false;
 		}
 		if (requestMappingUrl == null) {
 			if (other.requestMappingUrl != null) {
 				return false;
 			}
 		} else if (!requestMappingUrl.equals(other.requestMappingUrl)) {
 			return false;
 		}
 		if (responseJSONSample == null) {
 			if (other.responseJSONSample != null) {
 				return false;
 			}
 		} else if (!responseJSONSample.equals(other.responseJSONSample)) {
 			return false;
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "MethodDesc [name=" + name + ", requestMappingUrl="
 				+ requestMappingUrl + ", requestMappingMethod="
 				+ requestMappingMethod + ", responseJSONSample="
 				+ responseJSONSample + ", requestJSONSample="
 				+ requestJSONSample + ", commentText=" + commentText
 				+ ", annotations=" + Arrays.toString(annotations)
 				+ ", parameters=" + parameters + "]";
 	}
 
 	
 	
 }
