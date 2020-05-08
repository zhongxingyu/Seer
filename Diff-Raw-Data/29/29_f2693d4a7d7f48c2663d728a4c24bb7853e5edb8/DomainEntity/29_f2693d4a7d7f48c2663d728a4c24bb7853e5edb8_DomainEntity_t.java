 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008 CEJUG - Ceará Java Users Group
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
  
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 package net.java.dev.cejug.classifieds.server.ejb3.entity;
 
 import java.util.Collection;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 /**
  * A domain is company or a group of people. The domain should be registered in
  * the Cejug-Classifieds system, and a domain has a unique domain name.
  * 
  * @author $Author:felipegaucho $
  * @version $Rev:504 $ ($Date:2008-08-24 11:22:52 +0200 (Sun, 24 Aug 2008) $)
  */
 @Entity
 @Table(name = "DOMAIN")
 public class DomainEntity extends AbstractEntity {
 	@Column(name = "URI", nullable = false, unique = true)
 	private String uri;
 
 	@Column(name = "SHARED_COTA", nullable = false)
 	private Boolean sharedQuota;
 
	public Boolean isSharedQuota() {
 		return sharedQuota;
 	}
 
 	public void setSharedQuota(Boolean sharedQuota) {
 		this.sharedQuota = sharedQuota;
 	}
 
 	@Column(name = "BRAND", nullable = false)
 	private String brand;
 
 	// @OneToMany(mappedBy = "domain")
 	// private Collection<QuotaEntity> quotas;
 
 	@ManyToMany
 	@JoinTable(name = "DOMAIN_CATEGORY", joinColumns = @JoinColumn(name = "DOMAIN_ID", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "ID"))
 	private Collection<CategoryEntity> advertisementCategory;
 
 	public String getBrand() {
 		return brand;
 	}
 
 	public void setBrand(final String brand) {
 		this.brand = brand;
 	}
 
 	public String getUri() {
 		return uri;
 	}
 
 	public void setUri(final String uri) {
 		this.uri = uri;
 	}
 
 	/**
 	 * @return the categories
 	 */
 	public Collection<CategoryEntity> getAdvertisementCategory() {
 		return advertisementCategory;
 	}
 
 	/**
 	 * @param categories
 	 *            the categories to set
 	 */
 	public void setAdvertisementCategory(
 			final Collection<CategoryEntity> categories) {
 		this.advertisementCategory = categories;
 	}
 }
