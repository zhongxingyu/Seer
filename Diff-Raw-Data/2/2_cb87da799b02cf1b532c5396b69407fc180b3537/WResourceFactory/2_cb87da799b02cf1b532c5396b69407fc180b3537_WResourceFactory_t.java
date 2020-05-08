 package com.delcyon.capo.webapp.widgets;
 
 import java.io.IOException;
 
 import eu.webtoolkit.jwt.WResource;
 import eu.webtoolkit.jwt.servlet.WebRequest;
 import eu.webtoolkit.jwt.servlet.WebResponse;
 
/** this is just a wrapper class around WResource that less us use java 8 lambdas 
  * 
  * @author jeremiah
  *
  */
 public abstract class WResourceFactory extends WResource
 {
     
     @Override
     protected void handleRequest(WebRequest request, WebResponse response) throws IOException
     {
         handleRequestFunction(request, response);
         
     }
 
 	/**
 	 * method overridden by lambda expression
 	 * @param input
 	 * @return
 	 */
 	protected abstract void handleRequestFunction(WebRequest request, WebResponse response);
 	
 	/**
 	 * factory method to to use a lambda to make a custom WValidator
 	 * {@link WResourceFactory#handleRequestFunction(WebRequest request, WebResponse response) handleRequest} method.
 	 * @param WebRequest request,
 	 * @param WebResponse response	 * 
 	 */
 	public static WResource resource(WResourceInterface resourceInterface)
 	{
 		return new WResourceFactory()
 		{
 			@Override
 			protected void handleRequestFunction(WebRequest request, WebResponse response)
 			{
 			    resourceInterface.handleRequestFunction(request, response);
 			    
 			}
 		};
 	}
 	
 	/**
 	 * matches the handleRequestFunction method from WResource
 	 *
 	 */
 	public interface WResourceInterface
 	{		
 		public abstract void handleRequestFunction(WebRequest request, WebResponse response);
 
 	}
 }
