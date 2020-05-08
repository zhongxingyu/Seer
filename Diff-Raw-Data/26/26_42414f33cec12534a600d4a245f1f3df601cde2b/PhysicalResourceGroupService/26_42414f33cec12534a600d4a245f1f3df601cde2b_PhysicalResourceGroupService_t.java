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
 package nl.surfnet.bod.service;
 
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.UserGroup;
 import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.google.common.base.Function;
 
 @Service
 @Transactional
 public class PhysicalResourceGroupService {
 
   @Autowired
   private PhysicalResourceGroupRepo physicalResourceGroupRepo;
 
   public long count() {
     return physicalResourceGroupRepo.count();
   }
 
   public void delete(final PhysicalResourceGroup physicalResourceGroup) {
     physicalResourceGroupRepo.delete(physicalResourceGroup);
   }
 
   public PhysicalResourceGroup find(final Long id) {
     return physicalResourceGroupRepo.findOne(id);
   }
 
   public List<PhysicalResourceGroup> findAll() {
     return physicalResourceGroupRepo.findAll();
   }
 
   public List<PhysicalResourceGroup> findEntries(final int firstResult, final int maxResults) {
     return physicalResourceGroupRepo.findAll(new PageRequest(firstResult / maxResults, maxResults)).getContent();
   }
 
   public void save(final PhysicalResourceGroup physicalResourceGroup) {
     physicalResourceGroupRepo.save(physicalResourceGroup);
   }
 
   public PhysicalResourceGroup update(final PhysicalResourceGroup physicalResourceGroup) {
     return physicalResourceGroupRepo.save(physicalResourceGroup);
   }
 
   public Collection<PhysicalResourceGroup> findAllForAdminGroups(Collection<UserGroup> groups) {
     if (groups.isEmpty()) {
       return Collections.emptyList();
     }
 
    Collection<String> groupIds = newArrayList(transform(groups, new Function<UserGroup, String>() {
       @Override
       public String apply(UserGroup group) {
         return group.getId();
       }
    }));
 
     return physicalResourceGroupRepo.findByAdminGroupIn(groupIds);
   }
 
   public Collection<PhysicalResourceGroup> findAllForUser(RichUserDetails user) {
     Collection<UserGroup> groups = user.getUserGroups();
     return findAllForAdminGroups(groups);
   }
 
   public PhysicalResourceGroup findByName(String name) {
     return physicalResourceGroupRepo.findByName(name);
   }
 
   protected void setPhysicalResourceGroupRepo(PhysicalResourceGroupRepo physicalResourceGroupRepo) {
     this.physicalResourceGroupRepo = physicalResourceGroupRepo;
   }
 }
