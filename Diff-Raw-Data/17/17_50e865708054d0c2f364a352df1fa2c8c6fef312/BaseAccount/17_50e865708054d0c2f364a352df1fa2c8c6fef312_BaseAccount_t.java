 /*
  * Copyright 2006-2007 Luca Garulli (luca.garulli--at--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.romaframework.module.users.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.romaframework.aspect.authentication.AuthenticationAspect;
 import org.romaframework.aspect.core.annotation.AnnotationConstants;
 import org.romaframework.aspect.session.SessionAccount;
 import org.romaframework.aspect.view.annotation.ViewField;
 import org.romaframework.core.Roma;
 
 /**
  * Class that represents an account
  * 
  * @author Luca Garulli (luca.garulli--at--assetdata.it)
  */
public class BaseAccount extends AbstractAccount implements SessionAccount, Cloneable {
 
 	protected BaseProfile				profile;
 	protected Date							signedOn;
 	protected Date							lastModified;
 	protected Date							lastPasswordUpdate;
 
 	@ViewField(visible = AnnotationConstants.FALSE)
 	protected String						userIdentification;
 
 	protected String						notes;
 	protected BaseAccountStatus	status;
 
 	protected Boolean						changePasswordNextLogin;
 	protected String						email;
 	protected Set<BaseGroup>		groups;
 
 	@ViewField(render = "password")
 	protected String						password;
 
 	@ViewField(visible = AnnotationConstants.FALSE)
 	protected List<String>			oldPasswords;
 
 	public BaseAccount() {
 	}
 
 
 	public BaseAccount(String name, String password, BaseProfile iProfile) {
 		this(name, password, iProfile, null);
 	}
 
 	public BaseAccount(String name, String password, BaseProfile iProfile, BaseAccountStatus iStatus) {
 		this.name = name;
 		setPassword(password);
 		status = iStatus;
 		profile = iProfile;
 		signedOn = new Date();
 		lastModified = signedOn;
 	}
 
 
 	@Override
 	public String toString() {
 		return name;
 	}
 
 	public BaseProfile getProfile() {
 		return profile;
 	}
 
 	public void setProfile(Object iProfile) {
 		profile = (BaseProfile) iProfile;
 	}
 
 	public void setProfile(BaseProfile iProfile) {
 		profile = iProfile;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String iPassword) {
 		if (iPassword != null && iPassword.equals(password))
 			// you are not changing password
 			return;
 
 		try {
 			password = Roma.aspect(AuthenticationAspect.class).encryptPassword(iPassword);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Date getLastModified() {
 		return lastModified;
 	}
 
 	public void setLastModified(Date lastModified) {
 		this.lastModified = lastModified;
 	}
 
 	@ViewField(visible = AnnotationConstants.FALSE)
 	public Date getLastPasswordUpdate() {
 		return lastPasswordUpdate;
 	}
 
 	public void setLastPasswordUpdate(Date date) {
 		this.lastPasswordUpdate = date;
 	}
 
 	public Date getSignedOn() {
 		return signedOn;
 	}
 
 	public void setSignedOn(Date signedOn) {
 		this.signedOn = signedOn;
 	}
 
 	public BaseAccountStatus getStatus() {
 		return status;
 	}
 
 	public void setStatus(BaseAccountStatus status) {
 		this.status = status;
 	}
 
 	public String getNotes() {
 		return notes;
 	}
 
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 
 	public Boolean isChangePasswordNextLogin() {
 		return changePasswordNextLogin;
 	}
 
 	public void setChangePasswordNextLogin(Boolean changePasswordAtNextLogin) {
 		changePasswordNextLogin = changePasswordAtNextLogin;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	@ViewField(visible = AnnotationConstants.FALSE)
 	public BaseGroup getDefaultGroup() {
 		if (groups != null && groups.size() == 1)
 			return groups.iterator().next();
 		return null;
 	}
 
 	/**
 	 * @return the groups
 	 */
 	public Set<BaseGroup> getGroups() {
 		return groups;
 	}
 
 	/**
 	 * @param groups
 	 *          the groups to set
 	 */
 	public void setGroups(Set<BaseGroup> groups) {
 		this.groups = groups;
 	}
 
 	public boolean addGroup(BaseGroup iGroup) {
 		if (groups.contains(iGroup))
 			return false;
 
 		groups.add(iGroup);
 		return true;
 	}
 
 	public String getUserIdentification() {
 		return userIdentification;
 	}
 
 	public void setUserIdentification(String userIdentification) {
 		this.userIdentification = userIdentification;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		BaseAccount other = (BaseAccount) obj;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 	public List<String> getOldPasswords() {
 		return oldPasswords;
 	}
 
 	public void setOldPasswords(List<String> oldPasswords) {
 		this.oldPasswords = oldPasswords;
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException {
 		return super.clone();
 	}
 
 }
