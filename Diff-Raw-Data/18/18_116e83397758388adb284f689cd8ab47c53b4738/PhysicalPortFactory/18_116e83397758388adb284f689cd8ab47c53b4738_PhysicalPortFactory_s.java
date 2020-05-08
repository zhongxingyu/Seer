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
 package nl.surfnet.bod.support;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 
 public class PhysicalPortFactory {
   
   private Long id = null;
   private String name = "nameDefault " + id;
   private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
   private Integer version;
 
   public PhysicalPort create() {
     PhysicalPort port = new PhysicalPort();
     port.setId(id);
     port.setName(name);
     port.setVersion(version);
     port.setPhysicalResourceGroup(physicalResourceGroup);
 
     return port;
   }
 
   public PhysicalPortFactory setId(Long id) {
     this.id = id;
     return this;
   }
 
   public PhysicalPortFactory setVersion(Integer version) {
     this.version = version;
     return this;
   }
 
   public PhysicalPortFactory setName(String name) {
     this.name = name;
     return this;
   }
 
   public PhysicalPortFactory setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
     this.physicalResourceGroup = physicalResourceGroup;
     return this;
   }
 }
