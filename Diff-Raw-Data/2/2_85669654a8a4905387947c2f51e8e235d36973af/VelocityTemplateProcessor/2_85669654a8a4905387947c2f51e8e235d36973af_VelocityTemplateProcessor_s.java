 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package org.infoglue.deliver.util;
 
 import org.apache.log4j.Logger;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.*;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.io.FileHelper;
 
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 
 import java.util.Map;
 import java.util.Iterator;
 import java.io.*;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 
 /**
  *
  * @author Mattias Bogeblad
  */
 
 public class VelocityTemplateProcessor
 {
     private final static Logger logger = Logger.getLogger(VelocityTemplateProcessor.class.getName());
 
 	/**
 	 * This method takes arguments and renders a template given as a string to the specified outputstream.
 	 * Improve later - cache for example the engine.
 	 */
 	
 	public void renderTemplate(Map params, PrintWriter pw, String templateAsString) throws Exception 
 	{
 	    renderTemplate(params, pw, templateAsString, false);
 	}
 	
 	/**
 	 * This method takes arguments and renders a template given as a string to the specified outputstream.
 	 * Improve later - cache for example the engine.
 	 */
 	
 	public void renderTemplate(Map params, PrintWriter pw, String templateAsString, boolean forceVelocity) throws Exception 
 	{
 		try
 		{
 		    Timer timer = new Timer();
 		    timer.setActive(false);
 			
 		    if(templateAsString.indexOf("<%") > -1 || templateAsString.indexOf("http://java.sun.com/products/jsp/dtd/jspcore_1_0.dtd") > -1)
 		    {
 		    	dispatchJSP(params, pw, templateAsString);
 		    }
 		    else
 		    {
 		        boolean useFreeMarker = false;
 		        String useFreeMarkerString = CmsPropertyHandler.getProperty("useFreeMarker");
 		        if(useFreeMarkerString != null && useFreeMarkerString.equalsIgnoreCase("true"))
 		            useFreeMarker = true;
 		        
		        if(useFreeMarker && !forceVelocity)
 		        {
 		            FreemarkerTemplateProcessor.getProcessor().renderTemplate(params, pw, templateAsString);
 		        }
 		        else
 		        {
 					Velocity.init();
 			
 			        VelocityContext context = new VelocityContext();
 			        Iterator i = params.keySet().iterator();
 			        while(i.hasNext())
 			        {
 			        	String key = (String)i.next();
 			            context.put(key, params.get(key));
 			        }
 			        
 			        Reader reader = new StringReader(templateAsString);
 			        boolean finished = Velocity.evaluate(context, pw, "Generator Error", reader);        
 		        }
 		    }
 
 		    timer.printElapsedTime("End renderTemplate");
 		}
 		catch(Exception e)
 		{
 		    logger.warn("templateAsString:" + templateAsString);
 		    throw e;
 		}
 	}
 
 	/**
 	 * This methods renders a template which is written in JSP. The string is written to disk and then called.
 	 * 
 	 * @param params
 	 * @param pw
 	 * @param templateAsString
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 
 	public void dispatchJSP(Map params, PrintWriter pw, String templateAsString) throws ServletException, IOException, Exception
 	{
 		int hashCode = templateAsString.hashCode();
 
 		String contextRootPath = CmsPropertyHandler.getProperty("contextRootPath");
 		String fileName = contextRootPath + "jsp" + File.separator + "Template_" + hashCode + ".jsp";
 		
 		File template = new File(fileName);
 		if(!template.exists())
 		    FileHelper.writeToFile(template, templateAsString, false);
 		
 		TemplateController templateController = (TemplateController)params.get("templateLogic");
 		DeliveryContext deliveryContext = templateController.getDeliveryContext();
     	RequestDispatcher dispatch = templateController.getHttpServletRequest().getRequestDispatcher("/jsp/Template_" + hashCode + ".jsp");
 		templateController.getHttpServletRequest().setAttribute("org.infoglue.cms.deliver.templateLogic", templateController);
     	CharResponseWrapper wrapper = new CharResponseWrapper(deliveryContext.getHttpServletResponse());
 		dispatch.include(templateController.getHttpServletRequest(), wrapper);
 		String result = wrapper.toString();
 		pw.println(result);
 	}
 	
 }
