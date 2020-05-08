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
 import org.eclipse.core.runtime.IAdaptable;
 
 public interface IViolation extends IAdaptable {
 	public final IViolation[] NONE = new IViolation[] {};
 
 	public String getFileName();
 
 	public void setPackageName(final String newPackageName);
 
 	public String getPackageName();
 
 	public void setClassName(final String newClassName);
 
 	public String getClassName();
 
 	public void setFunctionName(final String newFunctionName);
 
 	public String getFunctionName();
 
 	public void setMethodName(final String newMethodName);
 
 	public String getMethodName();
 
 	public void setEndline(final int line);
 
 	public int getEndline();
 
 	public void setBeginline(final int line);
 
 	public int getBeginline();
 
 	public void setRule(final String rule);
 
 	public String getRule();
 
 	public void setRuleSet(final String ruleSet);
 
 	public String getRuleSet();
 
 	public void setPriority(final int priority);
 
 	public int getPriority();
 
 	public void setExternalInfoURL(final String url) throws MalformedURLException;
 
 	public void setExternalInfoURL(final URL url);
 
 	public URL getExternalInfoURL();
 
 	public void setDescription(final String description);
 
 	public String getDescription();
 
 	public IResource getResource();

	public String getInfo();
 }
