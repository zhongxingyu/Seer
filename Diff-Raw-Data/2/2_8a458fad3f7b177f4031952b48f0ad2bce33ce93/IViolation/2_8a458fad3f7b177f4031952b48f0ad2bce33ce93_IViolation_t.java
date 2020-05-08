 package org.phpsrc.eclipse.pti.tools.phpmd.model;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public interface IViolation {
	public final IViolation[] NONE = new IViolation[] {};

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
