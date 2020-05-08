 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
 
 package org.sakaiproject.discussion.api;
 
 import org.sakaiproject.exception.IdInvalidException;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.message.api.MessageService;
 
 /**
  * <p>
  * DiscussionService is the extension to MessageService configured for Discussions.
  * </p>
  * <p>
  * MessageChannels are DiscussionMessageChannels, and Messages are DiscussionMessages with DiscussionMessageHeaders.
  * </p>
  * <p>
  * Security in the discussion service, in addition to that defined in the channels, include:
  * <ul>
  * <li>discussion.channel.add</li>
  * </ul>
  * </p>
  * <li>discussion.channel.remove</li>
  * </ul>
  * </p>
  * <p>
  * Usage Events are generated:
  * <ul>
  * <li>discussion.channel.add - discussion channel resource id</li>
  * <li>discussion.channel.remove - discussion channel resource id</li>
  * </ul>
  * </p>
  */
 public interface DiscussionService extends MessageService
 {
 	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
 	static final String APPLICATION_ID = "sakai:discussion";
 
 	/** This string starts the references to resources in this service. */
 	public static final String REFERENCE_ROOT = "/discussion";
 
 	/** Security lock for posting topic messages to a channel. */
 	public static final String SECURE_ADD_TOPIC = "new.topic";
 	
 	/** Security lock for posting category to a channel. */
 	public static final String SECURE_ADD_CATEGORY = "new.category";
 
 	/**
 	 * A (DiscussionChannel) cover for getChannel() to return a specific discussion channel.
 	 * 
 	 * @param ref
 	 *        The channel reference.
 	 * @return the DiscussionChannel that has the specified name.
 	 * @exception IdUnusedException
 	 *            If this name is not defined for a discussion channel.
 	 * @exception PermissionException
 	 *            If the user does not have any permissions to the channel.
 	 */
 	public DiscussionChannel getDiscussionChannel(String ref) throws IdUnusedException, PermissionException;
 
 	/**
 	 * A (DiscussionChannel) cover for addChannel() to add a new discussion channel.
 	 * 
 	 * @param ref
 	 *        The channel reference.
 	 * @return The newly created channel.
 	 * @exception IdUsedException
 	 *            if the id is not unique.
 	 * @exception IdInvalidException
 	 *            if the id is not made up of valid characters.
 	 * @exception PermissionException
 	 *            if the user does not have permission to add a channel.
 	 */
 	public DiscussionChannelEdit addDiscussionChannel(String ref) throws IdUsedException, IdInvalidException, PermissionException;
 }
