 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
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
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.persistence.model.request;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.glite.security.voms.admin.persistence.model.VOMSUser;
 
 @Entity
 @Table(name = "requester_info")
 public class RequesterInfo implements Serializable {
 
 	/**
      * 
      */
 	private static final long serialVersionUID = 1L;
 	public static final String MULTIVALUE_COUNT_PREFIX = "num_";
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO, generator="VOMS_REQ_INFO_SEQ")
 	@SequenceGenerator(name="VOMS_REQ_INFO_SEQ", sequenceName="VOMS_REQ_INFO_SEQ")
 	Long id;
 
 	@Column(nullable = false)
 	String certificateSubject;
 
 	@Column(nullable = false)
 	String certificateIssuer;
 
 	String name;
 
 	String surname;
 
 	String institution;
 
 	String address;
 
 	String phoneNumber;
 
 	@Column(nullable = false)
 	String emailAddress;
 
 	@org.hibernate.annotations.CollectionOfElements
 	@JoinTable(name = "requester_personal_info", joinColumns = @JoinColumn(name = "requester_id"))
 	@org.hibernate.annotations.MapKey(columns = @Column(name = "pi_key"))
 	@Column(name = "pi_value")
 	Map<String, String> personalInformation = new HashMap<String, String>();
 
 	Boolean voMember;
 
 	/**
 	 * @return the id
 	 */
 	public Long getId() {
 
 		return id;
 	}
 
 	/**
 	 * @return the certificateSubject
 	 */
 	public String getCertificateSubject() {
 
 		return certificateSubject;
 	}
 
 	/**
	 * @return the certificateIssuer
 	 */
 	public String getCertificateIssuer() {
 
 		return certificateIssuer;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(Long id) {
 
 		this.id = id;
 	}
 
 	/**
 	 * @param certificateSubject
 	 *            the certificateSubject to set
 	 */
 	public void setCertificateSubject(String certificateSubject) {
 
 		this.certificateSubject = certificateSubject;
 	}
 
 	/**
	 * @param certificateIssuer
	 *            the certificateIssuer to set
 	 */
 	public void setCertificateIssuer(String certificateIssuer) {
 
 		this.certificateIssuer = certificateIssuer;
 	}
 
 	/**
 	 * @return the personalInformation
 	 */
 	public Map<String, String> getPersonalInformation() {
 
 		return personalInformation;
 	}
 
 	/**
 	 * @param personalInformation
 	 *            the personalInformation to set
 	 */
 	public void setPersonalInformation(Map<String, String> personalInformation) {
 
 		this.personalInformation = personalInformation;
 	}
 
 	/**
 	 * @return the emailAddress
 	 */
 	public String getEmailAddress() {
 
 		return emailAddress;
 	}
 
 	/**
 	 * @param emailAddress
 	 *            the emailAddress to set
 	 */
 	public void setEmailAddress(String emailAddress) {
 
 		this.emailAddress = emailAddress;
 	}
 
 	public boolean isVoMember() {
 
 		return voMember;
 	}
 
 	/**
 	 * @return the voMember
 	 */
 	public Boolean getVoMember() {
 
 		return voMember;
 	}
 
 	/**
 	 * @param voMember
 	 *            the voMember to set
 	 */
 	public void setVoMember(Boolean voMember) {
 
 		this.voMember = voMember;
 	}
 
 	@Override
 	public boolean equals(Object other) {
 
 		if (this == other)
 			return true;
 
 		if (!(other instanceof RequesterInfo))
 			return false;
 
 		if (other == null)
 			return false;
 
 		RequesterInfo that = (RequesterInfo) other;
 
 		// Implement meaningful checks here
 
 		return new EqualsBuilder().append(certificateSubject,
 				that.certificateSubject).append(certificateIssuer,
 				that.certificateIssuer).append(emailAddress, that.emailAddress)
 				.isEquals();
 
 	}
 
 	public String addInfo(String name, String value) {
 
 		return personalInformation.put(name, value);
 	}
 
 	public String getInfo(String name) {
 
 		return personalInformation.get(name);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getSurname() {
 		return surname;
 	}
 
 	public void setSurname(String surname) {
 		this.surname = surname;
 	}
 
 	public String getInstitution() {
 		return institution;
 	}
 
 	public void setInstitution(String institution) {
 		this.institution = institution;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public String getPhoneNumber() {
 		return phoneNumber;
 	}
 
 	public void setPhoneNumber(String phoneNumber) {
 		this.phoneNumber = phoneNumber;
 	}
 
 	@Override
 	public int hashCode() {
 
 		return new HashCodeBuilder(17, 37).append(certificateSubject).append(
 				certificateIssuer).append(emailAddress).toHashCode();
 
 	}
 
 	@Override
 	public String toString() {
 
 		ToStringBuilder builder = new ToStringBuilder(this);
 		
 		builder.append("certificateSubject", certificateSubject).
			append("certificateIssuer", certificateIssuer).
 			append("emailAddress", emailAddress).
 			append("name", name).append("surname",surname).append("voMember", voMember);
 		
 		return builder.toString();
 			
 	}
 	
 	public List<String> getMultivaluedInfo(String propertyName){
 		List<String> result = new ArrayList<String>();
 		
 		Integer valueCount;
 		
 		if (personalInformation.get(MULTIVALUE_COUNT_PREFIX+propertyName) != null){
 			valueCount = Integer.parseInt(personalInformation.get(MULTIVALUE_COUNT_PREFIX+propertyName));
 			
 			for (int i=0; i < valueCount; i++)
 				result.add(personalInformation.get(propertyName+i));
 			
 			return result;
 		
 		}
 		
 		return Collections.EMPTY_LIST;
 	}
 
 	public static RequesterInfo fromVOUser(VOMSUser user){
 		
 		RequesterInfo ri = new RequesterInfo();
 		
 		ri.setName(user.getName());
 		ri.setSurname(user.getSurname());
 		ri.setAddress(user.getAddress());
 		ri.setInstitution(user.getInstitution());
 		ri.setPhoneNumber(user.getPhoneNumber());
 		ri.setEmailAddress(user.getEmailAddress());
 		ri.setCertificateSubject(user.getDefaultCertificate().getSubjectString());
 		ri.setCertificateIssuer(user.getDefaultCertificate().getCa().getSubjectString());
 		ri.setVoMember(true);
 		
 		return ri;
 		
 		
 	}
 }
