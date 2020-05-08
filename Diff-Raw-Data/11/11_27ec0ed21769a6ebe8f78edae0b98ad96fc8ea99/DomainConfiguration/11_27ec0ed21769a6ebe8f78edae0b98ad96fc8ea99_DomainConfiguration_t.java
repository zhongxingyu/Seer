 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.domain.data;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
import com.google.appengine.api.datastore.Text;

 /**
  * Represents an domain configuration, as the domain name, and the enabled modules.
  * 
  * @author Danilo Queiroz
  */
 @PersistenceCapable
 public class DomainConfiguration implements Serializable, Comparable<DomainConfiguration> {
 
 	/*
 	 * TODO missing data as description, avatar, mail-list address, site url
 	 */
 
 	private static final long serialVersionUID = -1578869999667248765L;
 
 	@PrimaryKey
 	@Persistent
 	private String domainName;
 
 	@Persistent
 	private String name;
 
 	@Persistent
 	private String url;
 
 	@Persistent
 	private String mailList;
 
 	@Persistent
	private Text description;
 
 	@Persistent
 	private String avatarKey;
 
 	@Persistent
 	private HashSet<String> modulesEnabled = new HashSet<String>();
 
 	public DomainConfiguration(String domainName) {
 		this.domainName = domainName;
 		this.name = domainName;
 	}
 
 	/**
 	 * @return the domain name
 	 */
 	public String getDomainName() {
 		return this.domainName;
 	}
 
 	/**
 	 * Sets the public domain name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the public domain name
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * Sets the domain avatar key used on blobstore
 	 * 
 	 * @param avatarKey
 	 */
 	public void setAvatarKey(String avatarKey) {
 		this.avatarKey = avatarKey;
 	}
 
 	/**
 	 * @return the domain avatar key used on blobstore
 	 */
 	public String getAvatarKey() {
 		return this.avatarKey;
 	}
 
 	/**
 	 * @param description
 	 */
 	public void setDescription(String description) {
		this.description = new Text(description);
 	}
 
 	/**
 	 * @return description of domain
 	 */
 	public String getDescription() {
		return this.description.getValue();
 	}
 
 	/**
 	 * @param url
 	 *            the url to set
 	 */
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	/**
 	 * @return the url
 	 */
 	public String getUrl() {
 		return this.url;
 	}
 
 	/**
 	 * @param mailList
 	 *            the mailList to set
 	 */
 	public void setMailList(String mailList) {
 		this.mailList = mailList;
 	}
 
 	/**
 	 * @return the mailList
 	 */
 	public String getMailList() {
 		return this.mailList;
 	}
 
 	/**
 	 * Enables a module for this domain
 	 */
 	public void enableModule(String moduleName) {
 		this.modulesEnabled.add(moduleName);
 	}
 
 	/**
 	 * Disables a module for this domain
 	 */
 	public void disableModule(String moduleName) {
 		this.modulesEnabled.remove(moduleName);
 	}
 
 	/**
 	 * @return a {@link Set} of all enabled modules for this domain.
 	 */
 	public Set<String> getModulesEnabled() {
 		return this.modulesEnabled;
 	}
 
 	/**
 	 * @return <code>true</code> if the module with the given name is enabled, <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean isModuleEnabled(String moduleName) {
 		return this.modulesEnabled.contains(moduleName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return this.domainName.hashCode();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof DomainConfiguration) {
 			DomainConfiguration other = (DomainConfiguration) obj;
 			return this.getDomainName().equals(other.getDomainName());
 		} else {
 			return false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo(DomainConfiguration o) {
 		return this.domainName.compareToIgnoreCase(o.domainName);
 	}
 
 }
