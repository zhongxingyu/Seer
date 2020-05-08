 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package iddb.core.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 public class Server implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1356474862304251338L;
 
 	private static final Integer DEFAULT_MAX_LEVEL = 2;
 	private static final Integer MAX_LEVEL = 100;
 	
 	private Long key;
 
 	private String uid;
 	private String name;
 	private String adminEmail;
 	private Date created;
 	private Date updated;
 	private int onlinePlayers;
 	private String address;
 	private String displayAddress;
 	private String pluginVersion;
 	private Integer maxLevel;
 	private Boolean dirty;
 	private Integer permission;
 	private Boolean disabled;
 	/* key is long because limitations later in jsp processing */
 	private Map<Long, Integer> permissions;
 	private Long maxBanDuration;
 	
 	/* stats */
 	private Integer totalPlayers;
 	
 	public Integer getTotalPlayers() {
 		return totalPlayers;
 	}
 
 	public void setTotalPlayers(Integer totalPlayers) {
 		this.totalPlayers = totalPlayers;
 	}
 
 	public Integer getPermission(Integer func) {
		Integer n = this.permissions.get(new Long(func));
 		if (n == null) return MAX_LEVEL;
 		return n;
 	}
 	
 	public Map<Long, Integer> getPermissions() {
 		return permissions;
 	}
 
 	public void setPermissions(Map<Long, Integer> permissions) {
 		this.permissions = permissions;
 	}
 
 	public Boolean getDisabled() {
 		return disabled;
 	}
 
 	public void setDisabled(Boolean disabled) {
 		this.disabled = disabled;
 	}
 
 	public Server() {
 	}
 
 	public Long getKey() {
 		return key;
 	}
 
 	public void setKey(Long key) {
 		this.key = key;
 	}
 
 	public String getUid() {
 		return uid;
 	}
 
 	public void setUid(String uid) {
 		this.uid = uid;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getAdminEmail() {
 		return adminEmail;
 	}
 
 	public void setAdminEmail(String admin) {
 		this.adminEmail = admin;
 	}
 
 	public Date getCreated() {
 		return created;
 	}
 
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 
 	public Date getUpdated() {
 		return updated;
 	}
 
 	public void setUpdated(Date updated) {
 		this.updated = updated;
 	}
 
 	public int getOnlinePlayers() {
 		return onlinePlayers;
 	}
 
 	public void setOnlinePlayers(int onlinePlayers) {
 		this.onlinePlayers = onlinePlayers;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 		b.append(name);
 		b.append(" [");
 		b.append(uid);
 		b.append("]");
 		return b.toString();
 	}
 
 	public String getPluginVersion() {
 		return pluginVersion;
 	}
 
 	public void setPluginVersion(String pluginVersion) {
 		this.pluginVersion = pluginVersion;
 	}
 
 	public Integer getMaxLevel() {
 		if (maxLevel == null) {
 			return DEFAULT_MAX_LEVEL;
 		}
 		return maxLevel;
 	}
 
 	public void setMaxLevel(Integer maxLevel) {
 		this.maxLevel = maxLevel;
 	}
 
 	public Boolean getDirty() {
 		if (dirty == null) {
 			return true;
 		}
 		return dirty;
 	}
 
 	public void setDirty(Boolean dirty) {
 		this.dirty = dirty;
 	}
 
 	public Boolean getOffline() {
 		Date today = new Date();
 		if (this.updated != null) {
 			long diff = today.getTime() - this.updated.getTime();
 			return (diff / 86400000 >= 2);
 		}
 		return true;
 	}
 
 	public Integer getRemotePermission() {
 		return permission;
 	}
 
 	public void setRemotePermission(Integer permission) {
 		this.permission = permission;
 	}
 
 	public Integer getAdminLevel() {
 		if (this.permissions == null || this.permissions.size() == 0) return MAX_LEVEL;
 		List<Integer> levels = new ArrayList<Integer>(this.permissions.values());
 		Collections.sort(levels);
 		return levels.get(0);
 	}
 
 	public Long getMaxBanDuration() {
 		return maxBanDuration;
 	}
 
 	public void setMaxBanDuration(Long maxBanDuration) {
 		this.maxBanDuration = maxBanDuration;
 	}
 
 	public String getDisplayAddress() {
 		return displayAddress;
 	}
 
 	public void setDisplayAddress(String displayAddress) {
 		this.displayAddress = displayAddress;
 	}
 	
 }
