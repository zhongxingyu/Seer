 package org.openstack.atlas.service.domain.service.impl;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openstack.atlas.service.domain.common.*;
 import org.openstack.atlas.service.domain.entity.LoadBalancer;
 import org.openstack.atlas.service.domain.entity.LoadBalancerProtocol;
 import org.openstack.atlas.service.domain.entity.LoadBalancerStatus;
 import org.openstack.atlas.service.domain.entity.SessionPersistence;
 import org.openstack.atlas.service.domain.exception.*;
 import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
 import org.openstack.atlas.service.domain.service.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Service
 public class LoadBalancerServiceImpl implements LoadBalancerService {
     private final Log LOG = LogFactory.getLog(LoadBalancerServiceImpl.class);
 
     @Autowired
     protected AccountLimitService accountLimitService;
 
     @Autowired
     protected BlacklistService blacklistService;
 
     @Autowired
     protected HostService hostService;
 
     @Autowired
     protected LoadBalancerRepository loadBalancerRepository;
 
     @Autowired
     protected VirtualIpService virtualIpService;
 
     @Override
     @Transactional
     public final LoadBalancer create(final LoadBalancer loadBalancer) throws PersistenceServiceException {
         validateCreate(loadBalancer);
         addDefaultValuesForCreate(loadBalancer);
         LoadBalancer dbLoadBalancer = loadBalancerRepository.create(loadBalancer);
         dbLoadBalancer.setUserName(loadBalancer.getUserName());
         return dbLoadBalancer;
     }
 
     @Override
     @Transactional
     public LoadBalancer update(final LoadBalancer loadBalancer) throws PersistenceServiceException {
         LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());
 
         loadBalancerRepository.changeStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);
 
         setName(loadBalancer, dbLoadBalancer);
         setAlgorithm(loadBalancer, dbLoadBalancer);
         setPort(loadBalancer, dbLoadBalancer);
         setProtocol(loadBalancer, dbLoadBalancer);
         setConnectionLogging(loadBalancer, dbLoadBalancer);
 
         dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);
         dbLoadBalancer.setUserName(loadBalancer.getUserName());
 
         return dbLoadBalancer;
     }
 
     @Override
     @Transactional
     public void delete(final LoadBalancer lb) throws PersistenceServiceException {
         List<Integer> loadBalancerIds = new ArrayList<Integer>();
         loadBalancerIds.add(lb.getId());
         delete(lb.getAccountId(), loadBalancerIds);
     }
 
     @Override
     @Transactional
     public void delete(final Integer accountId, final List<Integer> loadBalancerIds) throws PersistenceServiceException {
         validateDelete(accountId, loadBalancerIds);
         for(int lbIdToDelete : loadBalancerIds) {
             loadBalancerRepository.changeStatus(lbIdToDelete, accountId, LoadBalancerStatus.PENDING_DELETE);
         }
     }
 
     @Transactional
     public void pseudoDelete(final LoadBalancer lb) throws EntityNotFoundException {
         LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lb.getId(), lb.getAccountId());
         dbLoadBalancer.setStatus(LoadBalancerStatus.DELETED);
         dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);
         virtualIpService.removeAllVipsFromLoadBalancer(dbLoadBalancer);
     }
 
     private void setProtocol(final LoadBalancer loadBalancer, final LoadBalancer dbLoadBalancer) throws BadRequestException {
         boolean portHMTypecheck = true;
         if (loadBalancer.getProtocol() != null && !loadBalancer.getProtocol().equals(dbLoadBalancer.getProtocol())) {
 
             //check for health monitor type and allow update only if protocol matches health monitory type for HTTP and HTTPS
             if (dbLoadBalancer.getHealthMonitor() != null) {
                 if (dbLoadBalancer.getHealthMonitor().getType() != null) {
                     if (dbLoadBalancer.getHealthMonitor().getType().name().equals(LoadBalancerProtocol.HTTP.name())) {
                         //incoming port not HTTP
                         if (!(loadBalancer.getProtocol().name().equals(LoadBalancerProtocol.HTTP.name()))) {
                             portHMTypecheck = false;
                         }
                     } else if (dbLoadBalancer.getHealthMonitor().getType().name().equals(LoadBalancerProtocol.HTTPS.name())) {
                         //incoming port not HTTP
                         if (!(loadBalancer.getProtocol().name().equals(LoadBalancerProtocol.HTTPS.name()))) {
                             portHMTypecheck = false;
                         }
                     }
                 }
             }
 
             if (portHMTypecheck) {
                 /* Notify the Usage Processor on changes of protocol to and from secure protocols */
                 //notifyUsageProcessorOfSslChanges(message, queueLb, dbLoadBalancer);
 
                 if (loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                     LOG.debug("Updating loadbalancer protocol to " + loadBalancer.getProtocol());
                     dbLoadBalancer.setProtocol(loadBalancer.getProtocol());
                 } else {
                     dbLoadBalancer.setSessionPersistence(SessionPersistence.NONE);
                     dbLoadBalancer.setProtocol(loadBalancer.getProtocol());
                 }
             } else {
                 LOG.error("Cannot update port as the loadbalancer has a incompatible Health Monitor type");
                 throw new BadRequestException(ErrorMessages.PORT_HEALTH_MONITOR_INCOMPATIBLE);
             }
         }
     }
 
     private void setAlgorithm(final LoadBalancer loadBalancer, final LoadBalancer dbLoadBalancer) {
         if (loadBalancer.getAlgorithm() != null && !loadBalancer.getAlgorithm().equals(dbLoadBalancer.getAlgorithm())) {
             LOG.debug("Updating loadbalancer algorithm to " + loadBalancer.getAlgorithm());
             dbLoadBalancer.setAlgorithm(loadBalancer.getAlgorithm());
         }
     }
 
     private void setName(final LoadBalancer loadBalancer, final LoadBalancer dbLoadBalancer) {
         if (loadBalancer.getName() != null && !loadBalancer.getName().equals(dbLoadBalancer.getName())) {
             LOG.debug("Updating loadbalancer name to " + loadBalancer.getName());
             dbLoadBalancer.setName(loadBalancer.getName());
         }
     }
 
     private void setPort(final LoadBalancer loadBalancer, final LoadBalancer dbLoadBalancer) throws BadRequestException {
         if (loadBalancer.getPort() != null && !loadBalancer.getPort().equals(dbLoadBalancer.getPort())) {
             LOG.debug("Updating loadbalancer port to " + loadBalancer.getPort());
             if (loadBalancerRepository.canUpdateToNewPort(loadBalancer.getPort(), dbLoadBalancer.getLoadBalancerJoinVipSet())) {
                 loadBalancerRepository.updatePortInJoinTable(loadBalancer);
                 dbLoadBalancer.setPort(loadBalancer.getPort());
             } else {
                 LOG.error("Cannot update load balancer port as it is currently in use by another virtual ip.");
                 throw new BadRequestException(ErrorMessages.PORT_IN_USE);
             }
         }
     }
 
     private void setConnectionLogging(final LoadBalancer loadBalancer, final LoadBalancer dbLoadBalancer) throws UnprocessableEntityException {
         if (loadBalancer.getConnectionLogging() != null && !loadBalancer.getConnectionLogging().equals(dbLoadBalancer.getConnectionLogging())) {
             /*if (loadBalancer.getConnectionLogging()) {
                 if (loadBalancer.getProtocol() != LoadBalancerProtocol.HTTP) {
                     LOG.error("Protocol must be HTTP for connection logging.");
                     throw new UnprocessableEntityException(String.format("Protocol must be HTTP for connection logging."));
                 }
                 LOG.debug("Enabling connection logging on the loadbalancer...");
             } else {
                 LOG.debug("Disabling connection logging on the loadbalancer...");
             }*/
             dbLoadBalancer.setConnectionLogging(loadBalancer.getConnectionLogging());
         }
     }
 
     protected void validateDelete(final Integer accountId, final List<Integer> loadBalancerIds) throws BadRequestException {
         List<Integer> badLbIds = new ArrayList<Integer>();
         List<Integer> badLbStatusIds = new ArrayList<Integer>();
         for (int loadBalancerId : loadBalancerIds) {
             try {
                 LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
                 if(!dbLoadBalancer.getStatus().equals(LoadBalancerStatus.ACTIVE)) {
                     LOG.warn(StringHelper.immutableLoadBalancer(dbLoadBalancer));
                     badLbStatusIds.add(loadBalancerId);
                 }
             } catch (EntityNotFoundException e) {
                 badLbIds.add(loadBalancerId);
             }
         }
         if (!badLbIds.isEmpty()) {
            throw new BadRequestException(ErrorMessages.LBS_NOT_FOUND.getMessage(StringUtilities.DelimitString(badLbIds, ",")));
         }
         if (!badLbStatusIds.isEmpty()) {
            throw new BadRequestException(ErrorMessages.LBS_IMMUTABLE.getMessage(StringUtilities.DelimitString(badLbStatusIds, ",")));
         }
     }
 
     protected void validateCreate(final LoadBalancer loadBalancer) throws BadRequestException, EntityNotFoundException, LimitReachedException {
         Validator.verifyTCPProtocolandPort(loadBalancer);
         Validator.verifyProtocolAndHealthMonitorType(loadBalancer);
         accountLimitService.verifyLoadBalancerLimit(loadBalancer.getAccountId());
         blacklistService.verifyNoBlacklistNodes(loadBalancer.getNodes());
     }
 
     protected void addDefaultValuesForCreate(final LoadBalancer loadBalancer) throws PersistenceServiceException {
         LoadBalancerDefaultBuilder.addDefaultValues(loadBalancer);
         loadBalancer.setHost(hostService.getDefaultActiveHost());
         virtualIpService.assignVIpsToLoadBalancer(loadBalancer);
     }
 }
 
