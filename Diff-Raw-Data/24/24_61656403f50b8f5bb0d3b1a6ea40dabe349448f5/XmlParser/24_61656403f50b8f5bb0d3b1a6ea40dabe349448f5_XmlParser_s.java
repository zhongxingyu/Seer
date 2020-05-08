 /**
  * Copyright 2009-2011 The Australian National University
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package au.edu.anu.portal.portlets.basiclti.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 
 import au.edu.anu.portal.portlets.basiclti.models.Page;
import au.edu.anu.portal.portlets.basiclti.models.PageList;
 import au.edu.anu.portal.portlets.basiclti.models.Site;
 import au.edu.anu.portal.portlets.basiclti.models.SiteList;
 import au.edu.anu.portal.portlets.basiclti.models.SitePageList;
 import au.edu.anu.portal.portlets.basiclti.models.Tool;
 
 /**
  * A collection of methods to parse XML into various lists and objects
  * Ideally these should be genericised to a generic parsing algorithm
  * 
  * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
  *
  */
 public class XmlParser {
 
 	private static final Log log = LogFactory.getLog(XmlParser.class);
 	
 	/**
 	 * Parse the given XML into a list of Sites. This comes from getSitesUserCanAccess.
 	 * @param xml
 	 * @return
 	 */
 	public static List<Site> parseListOfSites(String xml){
 		
		log.debug(xml);
 		
 		Serializer serializer = new Persister();
 		List<Site> sites = new ArrayList<Site>();
 		
 		//validate
 		if(!isValid(serializer, SiteList.class, xml)){
 			return sites;
 		}
 		
 		//parse
 		try {
 			SiteList list = serializer.read(SiteList.class, xml);
 			sites = list.getSites();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return sites;
 	}
 	
 	/**
 	 * Parse the given XML into a list of Tools. This comes from getPagesAndToolsForSite.
 	 * @param xml
 	 * @return
 	 */
 	public static List<Tool> parseListOfPages(String xml){
 		
		log.debug(xml);
 		
 		Serializer serializer = new Persister();
 		List<Tool> tools = new ArrayList<Tool>();
 		
 		//validate
 		if(!isValid(serializer, SitePageList.class, xml)){
 			return tools;
 		}
 		
 		//parse
 		try {
 			SitePageList sitePageList = serializer.read(SitePageList.class, xml);
 			List<Page> pages = sitePageList.getPageList().getPages();
 			for(Page p: pages) {
 				tools.addAll(p.getTools());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return tools;
 	}
 	
 	
 	/**
 	 * Check if the given XML validates to the given class type
 	 * @param serializer
 	 * @param clazz
 	 * @param xml
 	 * @return
 	 * @throws Exception 
 	 */
 	private static boolean isValid(Serializer serializer, Class<?> clazz, String xml){
 		
 		if(StringUtils.isBlank(xml)){
 			return false;
 		}
 		
 		try {
 			if(serializer.validate(clazz, xml)) {
 				return true;
 			}
 		} catch (Exception e) {
 			log.error("Unable to validate xml: " + e.getClass().getName() + ": " + e.getMessage());
 		}
 		return false;
 	}
 }
