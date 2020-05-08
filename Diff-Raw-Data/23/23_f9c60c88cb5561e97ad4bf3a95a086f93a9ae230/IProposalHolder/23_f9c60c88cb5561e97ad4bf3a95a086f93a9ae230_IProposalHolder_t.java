 /**
  * 
  */
 package org.eclipse.dlkt.javascript.dom.support;
 
 /**
 * This class is a proposal holder around a function object.
 * So that you can give it a help text and parameter names.
 * 
 * If wrapped in such an object then it is assumed that the
 * object is a function object.
 * 
  * @author jcompagner
  *
  */
 public interface IProposalHolder
 {
 	public char[][] getParameterNames();
 	
 	public Object getProposalInfo();
 	
 	public Object getObject();
 }
