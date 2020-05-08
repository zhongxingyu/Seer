 package jcube.core.filter.controller;
 
 import java.util.List;
 
 import jcube.core.controller.invoker.ControllerInvocationManager;
 import jcube.core.server.chain.ChainFilter;
 import jcube.core.server.chain.Filter;
 import jcube.core.server.environ.Environ;
 
 public class ControllerInvoke implements ChainFilter
 {
 	Environ environ;
 
 	@Override
 	public void filter(Filter filterOptions) throws Exception
 	{
 		// Create the controllerInvoker
		ControllerInvocationManager invoker = new ControllerInvocationManager(environ, filterOptions);
 	}
 
 	@Override
 	public boolean stopChaining()
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public List<String> skipChainsByName()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
