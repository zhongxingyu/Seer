 package br.com.sysmap.crux.showcase.client.controller;
 
 import br.com.sysmap.crux.core.client.controller.Controller;
 
 @Controller("noJavaSourcesController")
public class NoJavaSourcesController extends BaseSourcesController implements NoJavaSourcesControllerCrossDoc {
 
 	@Override
 	protected boolean hasControllerSource()
 	{
 		return false;
 	}
 }
