 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sdata.services.col;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.jcr.Node;
 import javax.jcr.RepositoryException;
 
 import org.sakaiproject.jcr.support.api.JCRNodeFactoryService;
 import org.sakaiproject.jcr.support.api.JCRNodeFactoryServiceException;
 import org.sakaiproject.sdata.tool.JCRNodeMap;
 import org.sakaiproject.sdata.tool.api.SDataException;
 import org.sakaiproject.sdata.tool.api.SecurityAssertion;
 import org.sakaiproject.sdata.tool.api.ServiceDefinition;
 import org.sakaiproject.sdata.tool.util.ResourceDefinitionImpl;
 
 /**
  * @author ieb
  */
 public class ColBean implements ServiceDefinition
 {
 
 	private Map<String, Object> m = new HashMap<String, Object>();
 
 	/**
 	 * @param uris
 	 * @param method
 	 * @param function
 	 * @param depth
 	 * @param inbasePath
 	 * @param assertion
 	 * @param jcrNodeFactory
 	 * @throws SDataException
 	 */
 	public ColBean(String[] uris, int depth, String inbasePath,
 			SecurityAssertion assertion, JCRNodeFactoryService jcrNodeFactory)
 			throws SDataException
 	{
 		int inversion = -1;
		Map<String, Object> items = new HashMap<String, Object>();
 		for (String uri : uris)
 		{
 
 			try
 			{
 				ResourceDefinitionImpl rp = new ResourceDefinitionImpl("GET", null,
 						depth, inbasePath, uri, inversion, assertion);
 				String repoPath = rp.getRepositoryPath();
 				Node n = jcrNodeFactory.getNode(repoPath);
 
 				if (n == null)
 				{
					items.put(uri, "404 "+repoPath+" "+uri);
 				}
 				else
 				{
					items.put(uri, new JCRNodeMap(n, depth, rp));
 				}
 			}
 			catch (RepositoryException re)
 			{
				items.put(uri, "Error " + re.getMessage());
 			}
 			catch (JCRNodeFactoryServiceException e)
 			{
				items.put(uri, "Error " + e.getMessage());
 			}
			m.put("items", items);
 
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.ServiceDefinition#getResponseMap()
 	 */
 	public Map<String, Object> getResponseMap()
 	{
 		return m;
 	}
 
 }
