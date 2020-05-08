 /**
  *
  * Copyright (c) 2013, Linagora
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  *
  */
 package controllers;
 
 import java.util.List;
 import java.util.UUID;
 
 import org.ow2.play.governance.api.PatternRegistry;
 import org.ow2.play.governance.api.SimplePatternService;
 import org.ow2.play.governance.api.bean.Pattern;
 
 import play.mvc.With;
 import utils.Locator;
 
 /**
  * Manage platform patterns
  * 
  * @author chamerling
  * 
  */
 @With(Secure.class)
 public class PatternsController extends PlayController {
 	
 	/**
 	 * Create pattern page, provide deployment stuff from form
 	 */
 	public static void deploy() {
 		render();
 	}
 	
 	/**
 	 * Deploy from a pattern given as parameter
 	 * 
 	 * @param pattern
 	 */
 	public static void deployFrom(String pattern) {
 		flash.put("pattern", pattern);
 		flash.keep();
 		renderTemplate("PatternsController/deploy.html", pattern);
 	}
 	
 	/**
 	 * Form action: Deploy a pattern to the engine
 	 * @param pattern
 	 */
 	public static void deployToRuntime(String pattern) {
 		if (pattern == null) {
 			handleException("Pattern can not be null", new Exception(
 					"Null parameter"));
 		}
 		
 		SimplePatternService client = null;
 		String id = UUID.randomUUID().toString();
 		try {
 			client = Locator.getSimplePatternService(getNode());
 			String result = client.deploy(id, pattern);
 			flash.success("Pattern has been deployed to runtime %s",
 					result);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			handleException(null, e);
 		}
 		flash.success("Pattern '%s' has been deployed", id);
 		deploy();
 	}
 
 	/**
 	 * List the patterns registered in the registry; This is an historical
 	 * registry and does not handle currently deployed patterns
 	 */
 	public static void list() {
 		PatternRegistry client = null;
 		try {
 			client = Locator.getPatternRegistry(getNode());
 		} catch (Exception e) {
 			handleException(null, e);
 		}
 
 		List<Pattern> list = null;
 		try {
 			list = client.all();
 		} catch (Exception e) {
 			handleException("Unable to get patterns", e);
 		}
 		render(list);
 	}
 	
 	/**
 	 * List the deployed patterns. The client retrieve patterns from the DCEP
 	 * service through the governance.
 	 */
 	public static void listDeployed() {
 		SimplePatternService client = null;
 		try {
 			client = Locator.getSimplePatternService(getNode());
 		} catch (Exception e) {
 			handleException(null, e);
 		}
 
 		List<Pattern> list = null;
 		try {
 			list = client.getPatterns();
 		} catch (Exception e) {
 			handleException("Unable to get patterns from runtime", e);
 		}
 		render(list);
 	}
 	
 	/**
 	 * 
 	 * @param id
 	 * @param data
 	 */
 	public static void download(String name, String pattern) {
 		renderText(pattern);
 	}
 	
 	/**
 	 * Undeploy a pattern from the runtime (DCEP)
 	 * 
 	 * @param id
 	 */
 	public static void undeployPatternFromRuntime(String id) {
		SimplePatternService client = null;
		try {
			client = Locator.getSimplePatternService(getNode());
		} catch (Exception e) {
			handleException(null, e);
		}

		try {
			String result = client.undeploy(id);
			flash.success("Pattern has been undeployed from runtime %s",
					result);
		} catch (Exception e) {
			handleException("Unable to undeploy pattern from runtime", e);
		}
		listDeployed();
 	}
 }
