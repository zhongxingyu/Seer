 /*******************************************************************************
  * Copyright (c) 2009, 2010 Dejan Spasic
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.phpsrc.eclipse.pti.tools.phpmd.model;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 
 public class ViolationResource implements IViolation {
 	private IResource resource;
 	private String packageName;
 	private String className;
 	private String functionName;
 	private String methodName;
 
 	private int beginline;
 	private int endline;
 
 	private int priority;
 
 	private String description;
 	private URL externalInfoURL;
 
 	private String rule;
 	private String ruleSet;
 
 	public ViolationResource(IResource resource) {
 		this.resource = resource;
 	}
 
 	public IResource getResource() {
 		return resource;
 	}
 
 	public String getFileName() {
 		return resource.getLocation().lastSegment();
 	}
 
 	public String getPackageName() {
 		return packageName;
 	}
 
 	public void setPackageName(String packageName) {
 		this.packageName = packageName;
 	}
 
 	public String getClassName() {
 		return className;
 	}
 
 	public void setClassName(String className) {
 		this.className = className;
 	}
 
 	public String getFunctionName() {
 		return functionName;
 	}
 
 	public void setFunctionName(String functionName) {
 		this.functionName = functionName;
 	}
 
 	public String getMethodName() {
 		return methodName;
 	}
 
 	public void setMethodName(String methodName) {
 		this.methodName = methodName;
 	}
 
 	public int getBeginline() {
 		return beginline;
 	}
 
 	public String getDescription() {
 		return new String(description);
 	}
 
 	public int getEndline() {
 		return endline;
 	}
 
 	public URL getExternalInfoURL() {
 		return externalInfoURL;
 	}
 
 	public int getPriority() {
 		return priority;
 	}
 
 	public String getRule() {
 		return rule;
 	}
 
 	public String getRuleSet() {
 		return ruleSet;
 	}
 
 	public void setBeginline(final int line) {
 		beginline = line;
 	}
 
 	public void setDescription(final String description) {
 		this.description = description;
 	}
 
 	public void setEndline(final int line) {
 		endline = line;
 	}
 
 	public void setExternalInfoURL(final String url) throws MalformedURLException {
 		setExternalInfoURL(new URL(url));
 	}
 
 	public void setExternalInfoURL(final URL url) {
 		externalInfoURL = url;
 	}
 
 	public void setPriority(final int priority) {
 		this.priority = priority;
 	}
 
 	public void setRule(final String rule) {
 		this.rule = rule;
 	}
 
 	public void setRuleSet(String ruleSet) {
 		this.ruleSet = ruleSet;
 	}
 
 	public String getInfo() {
 		if (null == resource)
 			return "";
 		return resource.getFullPath().toString();
 	}
 
	public static ViolationResource loadViolationByInfo(String info) {
		IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(info));
		if (null == res)
			return null;
		return new ViolationResource(res);
	}

 	public Object getAdapter(Class adapter) {
 		if (adapter.isInstance(resource))
 			return resource;
 		return Platform.getAdapterManager().getAdapter(this, adapter);
 	}
 
 	@Override
 	public String toString() {
 		return "Violation [priority=" + priority + ", rule=" + rule + ", ruleSet=" + ruleSet + ", filenam)="
 				+ getFileName() + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + beginline;
 		result = prime * result + endline;
 		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
 		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ViolationResource other = (ViolationResource) obj;
 		if (beginline != other.beginline)
 			return false;
 		if (endline != other.endline)
 			return false;
 		if (resource == null) {
 			if (other.resource != null)
 				return false;
 		} else if (!resource.equals(other.resource))
 			return false;
 		if (rule == null) {
 			if (other.rule != null)
 				return false;
 		} else if (!rule.equals(other.rule))
 			return false;
 		return true;
 	}
 }
