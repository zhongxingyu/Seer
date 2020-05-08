 /*
  * WebimDao.java
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package webim.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import webim.WebimEndpoint;
 import webim.WebimGroup;
 
 public class WebimDao {
 	
 	public WebimDao() {
 	}
 
 	public List<WebimEndpoint> getBuddiesByIds(long[] ids) {
 		return new ArrayList<WebimEndpoint>();
 	}
 
 	/*
 	 * Get group info
 	 */
 	public WebimGroup getGroup(long gid) {
 		// TODO Auto-generated method stub
 		return new WebimGroup("1", "group1");
 	}
 
 	/*
 	 * Get groups of the user from database.
 	 */
 	public List<WebimGroup> getGroups(long uid, int limit) {
 		// TODO Auto-generated method stub
 		List<WebimGroup> groups = new ArrayList<WebimGroup>();
 		WebimGroup g = new WebimGroup("group1", "group1");
		g.setPic_url("/Webim/static/images/group.gif");
 		groups.add(g);
 		return groups;
 	}
 
 	/*
 	 * Read from database.
 	 */
 	public List<WebimEndpoint> getBuddiesByUid(long uid, int limit) {
 		// TODO Auto-generated method stub
 		List<WebimEndpoint> buddies = new ArrayList<WebimEndpoint>();
 		WebimEndpoint e = new WebimEndpoint("1", "user1");
 		e.setPic_url("https://1.gravatar.com/avatar/136e370cbf1cf500cbbf791e56dac614?d=https%3A%2F%2Fidenticons.github.com%2F577292a0aa8cb84aa3e6f06fee6f711c.png&s=50");
 		buddies.add(e);
 		return buddies;
 	}
 
 }
