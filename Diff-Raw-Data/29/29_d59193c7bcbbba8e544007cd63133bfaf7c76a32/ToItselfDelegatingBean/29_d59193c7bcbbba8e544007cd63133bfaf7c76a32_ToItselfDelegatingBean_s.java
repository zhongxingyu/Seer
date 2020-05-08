 package com.cellent.spring.utils.junit_spring.support;
 
 /**
  * Class which delegates in a public method to itself.
  * 
  * @author bjoern
  */
 public class ToItselfDelegatingBean {
 
 	/**
 	 * Delegate Method which I want to overwrite with a Mockito Spy. It is qualified public so it can be partial mocked in
 	 * Mockito. Unless you are able to use package-private, this is the only possibility for doing that.
 	 * 
 	 * @return A String.
 	 */
	public final String myDelegatingMethod() {
 		throw new UnsupportedOperationException("Please override this Method with a Mockito Spy");
 	}
 
 	/**
 	 * Method which delegates simply to {@link #myDelegatingMethod()}.
 	 * 
 	 * @return Whatever {@link #myDelegatingMethod()} returns.
 	 */
 	public String getResultString() {
 		return myDelegatingMethod();
 	}
 
 }
