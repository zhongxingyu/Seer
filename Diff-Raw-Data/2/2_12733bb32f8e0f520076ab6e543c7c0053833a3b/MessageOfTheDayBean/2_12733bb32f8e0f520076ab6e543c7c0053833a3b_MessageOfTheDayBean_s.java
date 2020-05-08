 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/tfd/trunk/sdata/sdata-tool/impl/src/java/org/sakaiproject/sdata/tool/JCRDumper.java $
  * $Id: JCRDumper.java 45207 2008-02-01 19:01:06Z ian@caret.cam.ac.uk $
  ***********************************************************************************
  *
  * Copyright (c) 2008 The Sakai Foundation.
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
 
 package org.sakaiproject.sdata.services.motd;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.message.api.Message;
 import org.sakaiproject.message.api.MessageService;
 import org.sakaiproject.sdata.tool.api.ServiceDefinition;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.time.api.Time;
 import org.sakaiproject.time.api.TimeService;
 import org.sakaiproject.tool.api.SessionManager;
 
 /**
  * Message of the Day Bean generates the message of the day feed connecting to the underlying 
  * Announcement API
  * 
  * @author
  */
 public class MessageOfTheDayBean implements ServiceDefinition
 {
 
 	private static final Log log = LogFactory.getLog(MessageOfTheDayBean.class);
 
 	private Map<String, Object> map2 = new HashMap<String, Object>();;
 
 	private Map<String, Object> map = new HashMap<String, Object>();
 
 	private List<Map> MyMotds = new ArrayList<Map>();
 
 	/**
 	 * Create a Message of the Dat bean injecting the requred services
 	 * 
 	 * @param sessionManager
 	 * @param messageservice
 	 * @param timeService
 	 * @param siteService
 	 */
 	public MessageOfTheDayBean(SessionManager sessionManager,
 			MessageService messageservice, TimeService timeService,
 			SiteService siteService, HttpServletResponse response)
 	{
 
 		try
 		{
			log.error(messageservice);
 
 			// hardcoded because there does not seem to be a good way to do it
 			String ref = "/announcement/channel/!site/motd"; // messageservice.channelReference("!site",
 			// SiteService.MAIN_CONTAINER);
 			// making up a date that is wayyy in the past
 			Time reallyLongTimeAgo = timeService.newTime(0);
 			if (messageservice == null)
 			{
 				map.put("motdBody", "No Announcement Service Available on this server");
 				map.put("motdUrl", "#");
 				MyMotds.add(map);
 				map2.put("items", MyMotds);
 
 			}
 			else
 			{
 				List<Message> messages = messageservice.getMessages(ref,
 						reallyLongTimeAgo, 1, false, false, false);
 				if (messages.size() <= 0)
 				{
 					map.put("motdBody", "No Message of the day set");
 					map.put("motdUrl", "#");
 					MyMotds.add(map);
 					map2.put("items", MyMotds);
 				}
 				else
 				{
 					Message motd = messages.get(0);
 					map.put("motdBody", motd.getBody());
 					map.put("motdUrl", motd.getUrl());
 					MyMotds.add(map);
 					map2.put("items", MyMotds);
 
 				}
 			}
 		}
 		catch (PermissionException e)
 		{
 			throw new RuntimeException("He's dead Jim! : " + e.getMessage(), e);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.ServiceDefinition#getResponseMap()
 	 */
 	public Map<String, Object> getResponseMap()
 	{
 
 		return map2;
 	}
 
 }
