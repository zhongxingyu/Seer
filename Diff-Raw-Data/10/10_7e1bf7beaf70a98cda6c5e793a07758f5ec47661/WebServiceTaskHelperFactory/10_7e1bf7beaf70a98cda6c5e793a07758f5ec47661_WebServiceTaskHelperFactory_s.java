 /**
  * 
  */
 package ru.altruix.commons.android;
 
import co.altruix.ccp.android.impl.IWebServiceTaskHelperFactory;
import co.altruix.ccp.android.impl.fragments.WebServiceTaskHelper;

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
