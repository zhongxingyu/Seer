 /**
  * 
  */
 package ru.altruix.commons.android;
 
 /**
  * @author DP118M
  *
  */
 public class WebServiceTaskHelperFactory implements IWebServiceTaskHelperFactory {
 	@Override
 	public WebServiceTaskHelper create()
 	{
 		return new WebServiceTaskHelper();
 	}
 }
