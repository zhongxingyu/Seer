 package org.phpsrc.eclipse.pti.tools.phpmd.model;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public interface IViolation {
 	public final IViolation[] NONE = new IViolation[] {};
 
 	public void setFileName(final String newFileName);
 
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
 }
