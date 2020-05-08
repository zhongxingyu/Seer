 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.service;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static nl.surfnet.bod.service.VirtualPortPredicatesAndSpecifications.byGroupIdInLastMonthSpec;
 import static nl.surfnet.bod.service.VirtualPortPredicatesAndSpecifications.byPhysicalPortSpec;
 import static nl.surfnet.bod.service.VirtualPortPredicatesAndSpecifications.forManagerSpec;
 import static nl.surfnet.bod.service.VirtualPortPredicatesAndSpecifications.forUserSpec;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.collect.Collections2;
 
 import nl.surfnet.bod.domain.BodRole;
 import nl.surfnet.bod.domain.NsiVersion;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.UniPort;
 import nl.surfnet.bod.domain.UserGroup;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.domain.VirtualPortRequestLink;
 import nl.surfnet.bod.domain.VirtualPortRequestLink.RequestStatus;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.nsi.NsiHelper;
 import nl.surfnet.bod.repo.VirtualPortRepo;
 import nl.surfnet.bod.repo.VirtualPortRequestLinkRepo;
 import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 import nl.surfnet.bod.web.view.VirtualPortView;
 
 import org.joda.time.DateTime;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
 
 @Service
 @Transactional
 public class VirtualPortService extends AbstractFullTextSearchService<VirtualPort> {
 
   @Resource private VirtualPortRepo virtualPortRepo;
   @Resource private VirtualResourceGroupRepo virtualResourceGroupReppo;
   @Resource private VirtualPortRequestLinkRepo virtualPortRequestLinkRepo;
   @Resource private EmailSender emailSender;
   @Resource private ReservationService reservationService;
   @Resource private LogEventService logEventService;
   @Resource private NsiHelper nsiHelper;
 
   @PersistenceContext
   private EntityManager entityManager;
 
   public long count() {
     return virtualPortRepo.count();
   }
 
   public long countForUser(RichUserDetails user) {
     if (user.getUserGroups().isEmpty()) {
       return 0;
     }
     return virtualPortRepo.count(forUserSpec(user));
   }
 
   public long countForManager(BodRole managerRole) {
     checkArgument(managerRole.isManagerRole());
 
     return virtualPortRepo.count(forManagerSpec(managerRole));
   }
 
   public long countForUniPort(final UniPort uniPort) {
     return virtualPortRepo.count(byPhysicalPortSpec(uniPort));
   }
 
 
   public void deleteVirtualPorts(Collection<VirtualPort> virtualPorts, RichUserDetails userDetails) {
     for (VirtualPort virtualPort : virtualPorts) {
       Collection<Reservation> reservations = reservationService.findByVirtualPort(virtualPort);
       reservationService.cancelAndArchiveReservations(new ArrayList<>(reservations), userDetails);
       delete(virtualPort, userDetails);
     }
   }
 
   public void delete(VirtualPort virtualPort, RichUserDetails user) {
     List<Reservation> reservations = reservationService.findBySourcePortOrDestinationPort(virtualPort, virtualPort);
     reservationService.cancelAndArchiveReservations(reservations, user);
 
     logEventService.logDeleteEvent(Security.getUserDetails(),
         getLogLabel(Security.getSelectedRole(), virtualPort), virtualPort);
 
     virtualPort.getVirtualResourceGroup().removeVirtualPort(virtualPort);
     virtualPortRepo.delete(virtualPort);
 
     if (virtualPort.getVirtualResourceGroup().getVirtualPortCount() == 0) {
       virtualResourceGroupReppo.delete(virtualPort.getVirtualResourceGroup());
     }
   }
 
   private String getLogLabel(BodRole selectedRole, VirtualPort virtualPort) {
     if (selectedRole.isUserRole() && StringUtils.hasText(virtualPort.getUserLabel())) {
       return virtualPort.getUserLabel();
     }
 
     return virtualPort.getManagerLabel();
   }
 
   public VirtualPort find(Long id) {
     return virtualPortRepo.findOne(id);
   }
 
   public List<VirtualPort> findAll() {
     return virtualPortRepo.findAll();
   }
 
   public List<VirtualPort> findAllForUser(RichUserDetails user) {
     checkNotNull(user);
 
     if (user.getUserGroups().isEmpty()) {
       return Collections.emptyList();
     }
 
     return virtualPortRepo.findAll(forUserSpec(user));
   }
 
   public List<VirtualPort> findEntries(int firstResult, int maxResults, Sort sort) {
     checkArgument(maxResults > 0);
 
     return virtualPortRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
   }
 
   public List<VirtualPort> findEntriesForUser(RichUserDetails user, int firstResult, int maxResults, Sort sort) {
     checkNotNull(user);
 
     if (user.getUserGroups().isEmpty()) {
       return Collections.emptyList();
     }
 
     return virtualPortRepo.findAll(forUserSpec(user), new PageRequest(firstResult / maxResults, maxResults, sort))
         .getContent();
   }
 
   public List<VirtualPort> findEntriesForManager(BodRole managerRole, int firstResult, int maxResults, Sort sort) {
     checkArgument(maxResults > 0);
     checkArgument(managerRole.isManagerRole());
 
     return virtualPortRepo.findAll(forManagerSpec(managerRole),
         new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
   }
 
   public VirtualPort findByManagerLabel(String label) {
     return virtualPortRepo.findByManagerLabel(label);
   }
 
   public VirtualPort findByUserLabel(String label) {
     return virtualPortRepo.findByUserLabel(label);
   }
 
   public void save(VirtualPort virtualPort) {
     virtualPortRepo.save(virtualPort);
 
     // Log event after creation, so the ID is set by hibernate
     logEventService.logCreateEvent(Security.getUserDetails(), virtualPort);
   }
 
   public VirtualPort update(VirtualPort virtualPort) {
     logEventService.logUpdateEvent(
         Security.getUserDetails(),
         getLogLabel(Security.getSelectedRole(), virtualPort),
         virtualPort);
     return virtualPortRepo.save(virtualPort);
   }
 
   public Collection<VirtualPort> findAllForPhysicalPort(UniPort physicalPort) {
     checkNotNull(physicalPort);
 
     return virtualPortRepo.findAll(byPhysicalPortSpec(physicalPort));
   }
 
   public void requestNewVirtualPort(RichUserDetails user, VirtualResourceGroup vGroup, PhysicalResourceGroup pGroup,
       String userLabel, Long minBandwidth, String message) {
     VirtualPortRequestLink link = new VirtualPortRequestLink();
     link.setVirtualResourceGroup(vGroup);
     link.setPhysicalResourceGroup(pGroup);
     link.setUserLabel(userLabel);
     link.setMinBandwidth(minBandwidth);
     link.setMessage(message);
     link.setRequestorEmail(user.getEmail().get());
     link.setRequestorName(user.getDisplayName());
     link.setRequestorUrn(user.getUsername());
     link.setRequestDateTime(DateTime.now());
 
     virtualPortRequestLinkRepo.save(link);
     emailSender.sendVirtualPortRequestMail(user, link);
 
     // Log event after creation, so the ID is set by hibernate
     logEventService.logCreateEvent(Security.getUserDetails(), link);
   }
 
   public void requestDeleteVirtualPort(RichUserDetails user, String message, VirtualPort virtualPort) {
     VirtualPortRequestLink link = new VirtualPortRequestLink();
     link.setVirtualResourceGroup(virtualPort.getVirtualResourceGroup());
     link.setPhysicalResourceGroup(virtualPort.getPhysicalResourceGroup());
     link.setUserLabel(virtualPort.getUserLabel());
 
     link.setMinBandwidth(0L);
     link.setMessage(message);
     link.setRequestorEmail(user.getEmail().get());
     link.setRequestorName(user.getDisplayName());
     link.setRequestorUrn(user.getUsername());
     link.setRequestDateTime(DateTime.now());
 
     link.setStatus(RequestStatus.DELETE_REQUEST_PENDING);
 
     virtualPortRequestLinkRepo.save(link);
     emailSender.sendVirtualPortRequestMail(user, link);
 
     // Log event after creation, so the ID is set by hibernate
     logEventService.logDeleteEvent(Security.getUserDetails(), "Added delete request for VirtualPort with ID: "
         + virtualPort.getId(), link);
   }
 
   public Collection<VirtualPortRequestLink> findPendingRequests(PhysicalResourceGroup prg) {
    return virtualPortRequestLinkRepo.findByPhysicalResourceGroupAndStatus(prg, RequestStatus.PENDING);
   }
 
   public VirtualPortRequestLink findRequest(String uuid) {
     return virtualPortRequestLinkRepo.findByUuid(uuid);
   }
 
   public void requestLinkApproved(VirtualPortRequestLink link, VirtualPort port) {
     link.setStatus(RequestStatus.APPROVED);
 
     logEventService.logUpdateEvent(Security.getUserDetails(), "Approved request link " + link.getUserLabel(), link);
     virtualPortRequestLinkRepo.save(link);
 
     emailSender.sendVirtualPortRequestApproveMail(link, port);
   }
 
   public VirtualPortRequestLink findRequest(Long id) {
     return virtualPortRequestLinkRepo.findOne(id);
   }
 
   public Collection<VirtualPortRequestLink> findRequestsForLastMonth(Collection<UserGroup> userGroups) {
     Collection<String> groups = Collections2.transform(userGroups, new Function<UserGroup, String>() {
       @Override
       public String apply(UserGroup group) {
         return group.getId();
       }
     });
 
     return virtualPortRequestLinkRepo.findAll(byGroupIdInLastMonthSpec(groups));
   }
 
   public void requestLinkDeclined(VirtualPortRequestLink link, String declineMessage) {
     link.setStatus(RequestStatus.DECLINED);
 
     logEventService.logUpdateEvent(Security.getUserDetails(), "Declined request link " + link.getUserLabel(), link);
     virtualPortRequestLinkRepo.save(link);
 
     emailSender.sendVirtualPortRequestDeclineMail(link, declineMessage);
   }
 
   public VirtualPort findByNsiV1StpId(String stpId) {
     String id = nsiHelper.parseLocalNsiId(stpId, NsiVersion.ONE);
     return findByLocalStpId(id);
   }
 
   public VirtualPort findByNsiV2StpId(String stpId) {
     String id = nsiHelper.parseLocalNsiId(stpId, NsiVersion.TWO);
     return findByLocalStpId(id);
   }
 
   private VirtualPort findByLocalStpId(String id) {
     if (id == null) {
       return null;
     }
     try {
       return find(Long.valueOf(id));
     } catch (NumberFormatException e) {
       return null;
     }
   }
 
   @Override
   protected EntityManager getEntityManager() {
     return entityManager;
   }
 
   public List<Long> findIdsForUserUsingFilter(RichUserDetails userDetails, VirtualPortView filter, Sort sort) {
     BodRole selectedRole = userDetails.getSelectedRole();
 
     if (selectedRole.isManagerRole()) {
       return virtualPortRepo.findIdsWithWhereClause(forManagerSpec(selectedRole), Optional.<Sort> fromNullable(sort));
     } else if (selectedRole.isNocRole()) {
       return virtualPortRepo.findIds(Optional.<Sort> fromNullable(sort));
     } else if (selectedRole.isUserRole()) {
       return virtualPortRepo.findIdsWithWhereClause(forUserSpec(userDetails), Optional.<Sort> fromNullable(sort));
     }
 
     return Collections.emptyList();
   }
 }
