 package org.ukiuni.irc4j;
 
 /*
  * Copyright [2013] [ukiuni]
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 /**
  * User in IRC server.
  * 
  * @author ukiuni
  */
 public class User {
 	private String name;
 	private String realName;
 	private String hostName;
 	private String nickName;
 	private String description;
 	private boolean owner;
 
 	public String getNickName() {
 		return nickName;
 	}
 
 	public void setNickName(String nickName) {
 		this.nickName = nickName;
 	}
 
 	public boolean isOwner() {
 		return owner;
 	}
 
 	public void setOwner(boolean owner) {
 		this.owner = owner;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getHostName() {
 		return hostName;
 	}
 
 	public void setHostName(String hostName) {
 		this.hostName = hostName;
 	}
 
 	public String getFQUN() {
 		return nickName + "!" + name + "@" + hostName;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getRealName() {
 		return realName;
 	}
 
 	public void setRealName(String realName) {
 		this.realName = realName;
 	}
 
 	@Override
 	public int hashCode() {
 		if (null == this.nickName) {
 			return 0;
 		}
 		return this.nickName.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (null == this.nickName) {
 			return false;
 		}
		if (!(obj instanceof User)) {
			return false;
		}
		return this.nickName.equals(((User) obj).getNickName());
 	}
 }
