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
 import webim.WebimMenu;
 import webim.WebimNotification;
 
 /**
  * WebimDao从数据库中读取好友关系、群组关系。
  * 
  * @author Ery Lee<ery.lee at gmail.com>
  * @since 1.0
  */
 public class WebimDao {

	public WebimDao() {}

 	/**
 	 * 读取用户的好友列表。数据库中读取信息转换为WebimEndpoint对象。
 	 * 
 	 * @param uid
 	 *            用户uid
 	 * @param limit
 	 *            列表数量上线
 	 * @return 好友列表
 	 */
 	public List<WebimEndpoint> getBuddiesByUid(long uid, int limit) {
 		// TODO Auto-generated method stub
 		// TODO: 示例代码，需要替换
 		List<WebimEndpoint> buddies = new ArrayList<WebimEndpoint>();
 		WebimEndpoint e = new WebimEndpoint("1", "user1");
 		e.setPic_url("https://1.gravatar.com/avatar/136e370cbf1cf500cbbf791e56dac614?d=https%3A%2F%2Fidenticons.github.com%2F577292a0aa8cb84aa3e6f06fee6f711c.png&s=50");
 		buddies.add(e);
 
 		e = new WebimEndpoint("2", "user2");
 		e.setPic_url("https://1.gravatar.com/avatar/136e370cbf1cf500cbbf791e56dac614?d=https%3A%2F%2Fidenticons.github.com%2F577292a0aa8cb84aa3e6f06fee6f711c.png&s=50");
 		buddies.add(e);
 
 		return buddies;
 	}
 
 	/**
 	 * 根据好友id数据，读取好友列表。
 	 * 
 	 * @param ids
 	 *            好友id列表
 	 * @return 好友列表。
 	 */
 	public List<WebimEndpoint> getBuddiesByIds(long[] ids) {
 		// TODO: 示例代码，需要替换
 		return new ArrayList<WebimEndpoint>();
 	}
 
 	/**
 	 * 读取用户所属的群组。
 	 * 
 	 * @param uid
 	 *            用户uid
 	 * @param limit
 	 *            数量。
 	 * @return 群组列表。
 	 */
 	public List<WebimGroup> getGroups(long uid, int limit) {
 		// TODO Auto-generated method stub
 		// TODO: 示例代码，需要替换
 		List<WebimGroup> groups = new ArrayList<WebimGroup>();
 		WebimGroup g = new WebimGroup("group1", "group1");
 		g.setPic_url("/Webim/static/images/group.gif");
 		groups.add(g);
 		return groups;
 	}
 
 	/**
 	 * 读取群组对象信息
 	 * 
 	 * @param id
 	 *            群组id
 	 * @return 群组对象
 	 */
 	public WebimGroup getGroup(String id) {
 		// TODO Auto-generated method stub
 		return null; // new WebimGroup("1", "group1");
 	}
 
 	/**
 	 * 读取用户通知
 	 * 
 	 * @param uid
 	 *            用户uid
 	 * @return 用户通知列表
 	 */
 	public List<WebimNotification> getNotifications(long uid) {
 		return new ArrayList<WebimNotification>();
 	}
 
 	/**
 	 * 读取用户菜单
 	 * 
 	 * @param uid
 	 *            用户uid
 	 * 
 	 * @return 用户菜单列表
 	 */
 	public List<WebimMenu> getMenuList(long uid) {
 		return new ArrayList<WebimMenu>();
 	}
 
 }
