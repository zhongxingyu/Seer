 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.domain;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Version;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 public class PhysicalResourceGroup {
 
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Long id;
 
   @Version
   private Integer version;
 
  @OneToOne(optional = false)
   @JoinColumn(name = "institute_id")
   private Institute institute;
 
   @NotEmpty
   private String adminGroup;
 
   @NotEmpty
   @Email(message = "Not a valid email address")
   private String managerEmail;
 
   @NotNull
   @Column(nullable = false)
   private Boolean active = false;
 
   @OneToMany(mappedBy = "physicalResourceGroup", cascade = CascadeType.REMOVE)
   private Collection<PhysicalPort> physicalPorts;
 
   @SuppressWarnings("unused")
   @OneToMany(mappedBy = "physicalResourceGroup", cascade = CascadeType.REMOVE)
   private Collection<VirtualPortRequestLink> virtualPortRequestLinks;
 
   public Long getId() {
     return this.id;
   }
 
   public void setId(final Long id) {
     this.id = id;
   }
 
   public Integer getVersion() {
     return this.version;
   }
 
   public void setVersion(final Integer version) {
     this.version = version;
   }
 
   /**
    * This class will not have a name, instead it has a one-one relation to an
    * {@link Institute} and we will use the name of an Institute instead.
    * 
    * @return Name of the related institute
    */
   public String getName() {
     return institute.getName();
   }
 
   public String getAdminGroup() {
     return adminGroup;
   }
 
   public void setAdminGroup(String adminGroup) {
     this.adminGroup = adminGroup;
   }
 
   public Collection<PhysicalPort> getPhysicalPorts() {
     return physicalPorts;
   }
 
   public void setPhysicalPorts(List<PhysicalPort> physicalPorts) {
     this.physicalPorts = physicalPorts;
   }
 
   public int getPhysicalPortCount() {
     return physicalPorts.size();
   }
 
   public String getManagerEmail() {
     return managerEmail;
   }
 
   public void setManagerEmail(String managerEmail) {
     this.managerEmail = managerEmail;
   }
 
   public Institute getInstitute() {
     return institute;
   }
 
   public void setInstitute(Institute institute) {
     this.institute = institute;
   }
 
   public void setActive(boolean active) {
     this.active = active;
   }
 
   public boolean isActive() {
     return active;
   }
 
   @Override
   public String toString() {
     return "PhysicalResourceGroup [id=" + id + ", version=" + version + ", institute=" + institute + ", adminGroup="
         + adminGroup + ", managerEmail=" + managerEmail + ", active=" + active + ", physicalPorts=" + physicalPorts
         + ", virtualPortRequestLinks=" + virtualPortRequestLinks + "]";
   }
 }
