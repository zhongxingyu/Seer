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
 
 import javax.annotation.Nullable;
 import javax.persistence.*;
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 @JsonIgnoreProperties({ "physicalResourceGroup" })
 public class PhysicalPort {
 
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Long id;
 
   @Version
   private Integer version;
 
   @NotEmpty
   @Column(unique = true, nullable = false)
   private String name;
 
   @Nullable
   private String displayName;
   
   @Nullable
   @Column(unique = true, nullable = false)
   private String networkElementPk;
 
   @ManyToOne
   private PhysicalResourceGroup physicalResourceGroup;
 
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
 
   public String getName() {
     return this.name;
   }
 
   public void setName(final String name) {
     this.name = name;
   }
 
   public PhysicalResourceGroup getPhysicalResourceGroup() {
     return this.physicalResourceGroup;
   }
 
   public void setPhysicalResourceGroup(final PhysicalResourceGroup physicalResourceGroup) {
     this.physicalResourceGroup = physicalResourceGroup;
   }
 
   public String getNetworkElementPk() {
     return networkElementPk;
   }
 
   public void setNetworkElementPk(String networkElementPk) {
     this.networkElementPk = networkElementPk;
   }
 
   public void setDisplayName(final String displayName) {
     this.displayName = displayName;
   }
 
   public String getDisplayName() {
     return displayName;
   }
 
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PhysicalPort [id=").append(id).append(", version=").append(version).append(", name=").append(name)
        .append(", displayName=").append(displayName).append(", networkElementPk=").append(networkElementPk)
        .append(", physicalResourceGroup=").append(physicalResourceGroup).append("]");
    return builder.toString();
  }

 }
