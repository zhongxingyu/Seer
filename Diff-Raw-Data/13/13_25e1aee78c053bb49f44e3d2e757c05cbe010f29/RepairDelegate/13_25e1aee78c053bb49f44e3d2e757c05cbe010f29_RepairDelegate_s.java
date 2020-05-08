 /*
  * This file is part of the pgrid project.
  *
  * Copyright (c) 2012. Vourlakis Nikolas. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pgrid.service.repair.internal;
 
 import org.omg.CORBA.ORB;
 import org.omg.CORBA.SystemException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pgrid.entity.Host;
 import pgrid.entity.PGridPath;
 import pgrid.entity.routingtable.RoutingTable;
 import pgrid.service.CommunicationException;
 import pgrid.service.corba.PeerReference;
 import pgrid.service.corba.repair.*;
 import pgrid.service.repair.spi.FixNodeAlgorithm;
 import pgrid.service.repair.spi.ReplaceStrategy;
 import pgrid.utilities.ArgumentCheck;
 import pgrid.utilities.Deserializer;
 import pgrid.utilities.Serializer;
 
 import java.util.*;
 
 /**
  * XXX: [REFACTOR] fixNode code is partial duplicated by fixSubtree, DRY violation!
  * This class is a monstrosity!!!!!!!!!!
  *
  * @author Vourlakis Nikolas <nvourlakis@gmail.com>
  */
 public class RepairDelegate {
 
     private static final Logger logger_ = LoggerFactory.getLogger(RepairDelegate.class);
 
     private final RepairIssueRegistry registry_;
     private final RoutingTable routingTable_;
     private final ORB orb_;
     private FixNodeAlgorithm fix_;
     private ReplaceStrategy replace_;
     private int maxRef_;
 
     public RepairDelegate(ORB orb, RoutingTable routingTable, RepairIssueRegistry registry) {
         ArgumentCheck.checkNotNull(orb, "Cannot initialize a RepairDelegate object with a null ORB value.");
         ArgumentCheck.checkNotNull(routingTable, "Cannot initialize a RepairDelegate object with a null RoutingTable value.");
         ArgumentCheck.checkNotNull(registry, "Cannot initialize a RepairDelegate object with a null RepairIssueRegistry value.");
 
         orb_ = orb;
         routingTable_ = routingTable;
         registry_ = registry;
     }
 
     /**
      * Sets the number of maximum references constant that the routing table
      * can store in a single level.
      *
      * @param maxRef routing table level maximum references constant.
      */
     public void setMaxRef(int maxRef) {
         if (maxRef < 0) {
             throw new IllegalArgumentException("A negative maxRef was given.");
         }
         maxRef_ = maxRef;
     }
 
     /**
      * Sets the repair algorithm to be used.
      *
      * @param algorithm a fully initialized object of the repair algorithm.
      */
     public void setFixNodeAlgorithm(FixNodeAlgorithm algorithm) {
         ArgumentCheck.checkNotNull(algorithm, "Tried to pass a null FixNodeAlgorithm object at RepairDelegate.");
         fix_ = algorithm;
     }
 
     /**
      * Sets the replace strategy to be used.
      *
      * @param replace a fully initialized object of the replace strategy.
      */
     public void setReplaceStrategy(ReplaceStrategy replace) {
         ArgumentCheck.checkNotNull(replace, "Tried to pass a null ReplaceStrategy object at RepairDelegate.");
         replace_ = replace;
     }
 
     /**
      * TODO: Write documentation.
      *
      * @param footpath
      * @param failedHost
      */
     public void fixNode(String footpath, Host failedHost) {
         logger_.info("Fixing node {}", failedHost.getHostPath());
         validateService();
 
         if (registry_.containsHost(failedHost.getUUID())) {
             logger_.info("The localhost has already seen the issue and run the algorithm.");
             return;
         }
 
         if (!routingTable_.contains(failedHost)) {
             logger_.debug("The failed peer is either " +
                     "(a) solved and the replacer is already in the routing table or " +
                     "(b) solved but the replacer is missing cause of refMax constant.");
             return;
         }
 
         logger_.debug("It is not solved and the local peer must execute the algorithm");
         routingTable_.removeReference(failedHost);
         if (!registry_.containsHost(failedHost.getUUID())) {
             logger_.debug("Localhost is storing the issue in the registry");
             registry_.newIssue(failedHost);
         }
 
         List<Host> continuation = fix_.execute(routingTable_, new PGridPath(footpath));
         PGridPath failedHostPath = failedHost.getHostPath();
         if (continuation.size() == 0) {
             logger_.debug("The localhost doesn't know anyone that can fix the problem");
             // XXX: wait ????
             String pathToCheck = failedHostPath.subPath(0, failedHostPath.length() - 1);
             if (registry_.isCompleteSubtree(new PGridPath(pathToCheck))) {
                 logger_.debug("Generalizing to path {}", pathToCheck);
                 List<Host> hosts = registry_.commonPrefixIssues(pathToCheck);
                 fixSubtree(algorithmPathExecution(new PGridPath(pathToCheck)).toString(),
                         pathToCheck, hosts.toArray(new Host[hosts.size()]));
             } // XXX: else solve by fixNode (?)
         } else if (continuation.size() == 1) {
             Host host = continuation.get(0);
             if (routingTable_.getLocalhost().compareTo(host) == 0) { // or conjugate (?)
                 logger_.debug("The localhost prepares to fix the issue.");
                 List<Host> updatedHosts = new ArrayList<Host>(1);
                 updatedHosts.add(routingTable_.getLocalhost());
 
                 if (!failedHostPath.isConjugateTo(routingTable_.getLocalhost().getHostPath())) {
                     logger_.debug("Informing the localhost's conjugate for the issue");
                     Host conjugate = routingTable_.getLevelArray(routingTable_.levelNumber() - 1)[0];
                     try {
                         RepairHandle repairHandle = getRemoteHandle(conjugate);
                         PeerReference updatedConjugateRef = repairHandle.replace(failedHostPath.toString(),
                                 new RepairIssue[]{registry_.getIssue(failedHostPath.toString())});// TODO: Test rpc to conjugate
                         conjugate = Deserializer.deserializeHost(updatedConjugateRef);
                         updatedHosts.add(conjugate);
                         routingTable_.updateReference(conjugate);
                     } catch (CommunicationException e) {
                         logger_.debug("{}:{} cannot be reached.", conjugate, conjugate.getPort());
                         fixNode(algorithmPathExecution(conjugate.getHostPath()).toString(), conjugate);
                     }
                 }
 
                 logger_.info("Replacing peer with path {}", failedHostPath);
                 replace_.execute(routingTable_, failedHostPath);
                 registry_.getIssue(failedHostPath.toString()).issueState = IssueState.SOLVED;
                 routingTable_.refresh(maxRef_);
                 // broadcast the solution!!!!
                 pushSolution(failedHostPath.toString(), updatedHosts.toArray(new Host[updatedHosts.size()]), failedHost);
             } else {
                 logger_.debug("The localhost must forward a request to {}:{}", host, host.getPort());
                 try {
                     RepairHandle repairHandle = getRemoteHandle(host);
                     repairHandle.fixNode(footpath, registry_.getIssue(failedHostPath.toString())); // TODO: Test rpc to host
                 } catch (CommunicationException e) {
                     logger_.debug("{}:{} is not reachable.", host, host.getPort());
                     fixNode(algorithmPathExecution(host.getHostPath()).toString(), host);
                 }
             }
         } else if (continuation.size() > 1) {
             logger_.debug("The localhost must forward a request for a remote algorithm execution.");
             Host selectedHost = continuation.get(0);
             int maxCommon = selectedHost.getHostPath().commonPrefix(failedHostPath).length();
             for (Host host : continuation) {
                 int len = host.getHostPath().commonPrefix(failedHostPath).length();
                 if (maxCommon < len) {
                     selectedHost = host;
                     maxCommon = len;
                 }
             }
             logger_.debug("Selected {}:{} [{}] to continue", new Object[]{
                     selectedHost, selectedHost.getPort(), selectedHost.getHostPath()});
             try {
                 RepairHandle repairHandle = getRemoteHandle(selectedHost);
                 repairHandle.fixNode(footpath, registry_.getIssue(failedHostPath.toString())); // TODO: Test rpc to host
             } catch (CommunicationException e) {
                 if (selectedHost.getHostPath().isConjugateTo(failedHostPath)) {
                     String commonPrefix = failedHostPath.commonPrefix(selectedHost.getHostPath());
                     fixSubtree(algorithmPathExecution(
                             new PGridPath(commonPrefix)).toString(),
                             commonPrefix,
                             failedHost, selectedHost);
                 } else {
                     fixNode(algorithmPathExecution(selectedHost.getHostPath()).toString(), selectedHost);
                 }
             }
         }
     }
 
 
     /**
      * TODO: Write documentation.
      *
      * @param footpath
      * @param prefix
      * @param failedHosts
      */
     public void fixSubtree(String footpath, String prefix, Host... failedHosts) {
         logger_.info("Fixing subtree {}", prefix);
 
 
         for (Host failedHost : failedHosts) {
             routingTable_.removeReference(failedHost);
             if (!registry_.containsHost(failedHost.getUUID())) {
                 registry_.newIssue(failedHost);
             }
         }
 
         List<Host> continuation = fix_.execute(routingTable_, new PGridPath(footpath));
         PGridPath prefixPath = new PGridPath(prefix);
 
         if (continuation.size() == 0) {
             logger_.debug("The localhost doesn't know anyone that can fix the problem");
             // wait till timeout then keep generalizing till replacement
         } else if (continuation.size() == 1) {
             Host host = continuation.get(0);
             if (routingTable_.getLocalhost().compareTo(host) == 0) {
                 logger_.debug("The localhost prepares to fix the subtree issue.");
                 List<Host> updatedHosts = new ArrayList<Host>(1);
                 updatedHosts.add(routingTable_.getLocalhost());
 
                 logger_.debug("Informing the localhost's conjugate for the issue");
                 Host[] conjugateLevel = routingTable_.getLevelArray(routingTable_.levelNumber() - 1);
                 if (conjugateLevel.length > 0) {
                     Host conjugate = conjugateLevel[0];
                     try {
                         RepairHandle repairHandle = getRemoteHandle(conjugate);
                         RepairIssue[] repairIssues = new RepairIssue[failedHosts.length];
                         for (int i = 0; i < failedHosts.length; i++) {
                             repairIssues[i] = registry_.getIssue(failedHosts[i].getUUID());
                         }
                         PeerReference updatedConjugateRef = repairHandle.replace(prefix, repairIssues);// TODO: Test rpc to host
                         conjugate = Deserializer.deserializeHost(updatedConjugateRef);
                         updatedHosts.add(conjugate);
                         routingTable_.updateReference(conjugate);
                     } catch (CommunicationException e) {
                         logger_.debug("{}:{} cannot be reached.", conjugate, conjugate.getPort());
                         fixNode(algorithmPathExecution(conjugate.getHostPath()).toString(), conjugate);
                     }
                 }
 
                 logger_.info("Replacing subtree with prefix {}", prefix);
                 replace_.execute(routingTable_, prefixPath);
                 // XXX: transform all affected issues to SOLVED
                 routingTable_.refresh(maxRef_);
                 // broadcast
                 pushSolution(prefix, updatedHosts.toArray(new Host[updatedHosts.size()]), failedHosts);
             } else {
                 logger_.debug("The localhost must forward a request to {}:{}", host, host.getPort());
                 try {
                     RepairHandle repairHandle = getRemoteHandle(host);
                     RepairIssue[] repairIssues = new RepairIssue[failedHosts.length];
                     for (int i = 0; i < failedHosts.length; i++) {
                         repairIssues[i] = registry_.getIssue(failedHosts[i].getUUID());
                     }
                     repairHandle.fixSubtree(footpath, prefix, repairIssues); // TODO: Test rpc to host
                 } catch (CommunicationException e) {
                     logger_.debug("{}:{} is not reachable.", host, host.getPort());
                     if (host.getHostPath().isConjugateTo(prefixPath)) {
                         String commonPrefix = prefixPath.commonPrefix(host.getHostPath());
                         failedHosts = Arrays.copyOf(failedHosts, failedHosts.length + 1);
                         failedHosts[failedHosts.length - 1] = host;
                         fixSubtree(algorithmPathExecution(
                                 new PGridPath(commonPrefix)).toString(),
                                 commonPrefix,
                                 failedHosts);
                     } else {
                         fixNode(algorithmPathExecution(host.getHostPath()).toString(), host);
                     }
                 }
             }
         } else if (continuation.size() > 1) {
             logger_.debug("The localhost must forward a request for a remote algorithm execution.");
             Host selectedHost = continuation.get(0);
             int maxCommon = selectedHost.getHostPath().commonPrefix(prefixPath).length();
             for (Host host : continuation) {
                 int len = host.getHostPath().commonPrefix(prefixPath).length();
                 if (maxCommon < len) {
                     selectedHost = host;
                     maxCommon = len;
                 }
             }
             logger_.debug("Selected {}:{} [{}] to continue", new Object[]{
                     selectedHost, selectedHost.getPort(), selectedHost.getHostPath()});
             try {
                 RepairHandle repairHandle = getRemoteHandle(selectedHost);
                 RepairIssue[] repairIssues = new RepairIssue[failedHosts.length];
                 for (int i = 0; i < failedHosts.length; i++) {
                     repairIssues[i] = registry_.getIssue(failedHosts[i].getUUID());
                 }
                 repairHandle.fixSubtree(footpath, prefix, repairIssues); // TODO: Test rpc to host
             } catch (CommunicationException e) {
                 if (selectedHost.getHostPath().isConjugateTo(prefixPath)) {
                     String commonPrefix = prefixPath.commonPrefix(selectedHost.getHostPath());
                     failedHosts = Arrays.copyOf(failedHosts, failedHosts.length + 1);
                     failedHosts[failedHosts.length - 1] = selectedHost;
                     fixSubtree(algorithmPathExecution(
                             new PGridPath(commonPrefix)).toString(),
                             commonPrefix,
                             failedHosts);
                 } else {
                     fixNode(algorithmPathExecution(selectedHost.getHostPath()).toString(), selectedHost);
                 }
             }
         } else {
             logger_.debug("The failed peer is either " +
                     "(a) solved and the replacer is already in the routing table or " +
                     "(b) solved but the replacer is missing cause of refMax constant.");
         }
     }
 
     /**
      * A number of hosts known to have failed associated with a certain path,
      * will be replaced. This method is called in response to an rpc.
      *
      * @param failedPath  the failed path of a single host or a complete
      *                    subtree.
      * @param failedHosts all the hosts known to have failed.
      * @return the host needed to sent it back to the remote caller. He needs
      *         the updated version of the local host to form the Solution
      *         object and start broadcasting it.
      */
     public Host replace(String failedPath, Host... failedHosts) {
         for (Host failedHost : failedHosts) {
             if (routingTable_.contains(failedHost)) {
                 routingTable_.removeReference(failedHost);
             }
         }
 
         replace_.execute(routingTable_, new PGridPath(failedPath));
         routingTable_.refresh(maxRef_);
         for (Host failedHost : failedHosts) {
            registry_.getIssue(failedHost.getHostPath().toString()).issueState = IssueState.SOLVED;
         }
         return routingTable_.getLocalhost();
     }
 
     /**
      * This method will be called only by the host that will execute first the
      * replace algorithm. That host is the initiator of the broadcast solution
      * protocol.
      * In the Solution there is an array that holds all the UUIDs of the hosts
      * that have already received the solution. Before this host start
      * broadcasting, it will first add all his neighbours according to its
      * routing table.
      * <p/>
      * In case of a communication exception, the host will do nothing. The
      * broadcast protocol is already complicated. If a new repair session
      * starts before the current solution is broadcasted then things get messy.
      * <p/>
      * TODO: [Important] Needs evaluation!!!!!!
      *
      * @param failedPath   that is now fixed.
      * @param updatedHosts all the hosts that altered their paths to solve the
      *                     issue.
      * @param failedHosts  all the hosts that have failed.
      */
     public void pushSolution(String failedPath, Host[] updatedHosts, Host... failedHosts) {
         if (routingTable_.uniqueHostsNumber() == 0) {
             return;
         }
 
         logger_.info("Broadcasting the solution for path {}.", failedPath);
 
         PeerReference[] updatedHostsRef = new PeerReference[updatedHosts.length];
         for (int i = 0; i < updatedHosts.length; i++) {
             updatedHostsRef[i] = Serializer.serializeHost(updatedHosts[i]);
         }
 
         PeerReference[] failedHostsRef = new PeerReference[failedHosts.length];
         for (int i = 0; i < updatedHosts.length; i++) {
             failedHostsRef[i] = Serializer.serializeHost(failedHosts[i]);
             if (registry_.containsHost(failedHosts[i].getUUID())) {
                 registry_.removeIssue(failedHosts[i].getUUID());
             }
         }
 
         RepairSolution repairSolution = new RepairSolution();
         repairSolution.pushCount = 0;
         repairSolution.failedHosts = failedHostsRef;
         repairSolution.updatedHosts = updatedHostsRef;
         repairSolution.failedPath = failedPath;
 
         List<String> uuidSentList = new ArrayList<String>();
         for (PeerReference reference : updatedHostsRef) {
             uuidSentList.add(reference.uuid);
         }
 
         Collection<Host> allHosts = routingTable_.getAllHosts();
         for (Host host : allHosts) {
             uuidSentList.add(host.getUUID().toString());
         }
         repairSolution.uuidSent = uuidSentList.toArray(new String[uuidSentList.size()]);
 
         for (Host host : allHosts) {
             if (!uuidSentList.contains(host.getUUID().toString())) {
                 try {
                     RepairHandle repairHandle = getRemoteHandle(host);
                     repairHandle.pushSolution(repairSolution);
                 } catch (CommunicationException e) {
                     // Ignore the failure. The broadcasting becomes really
                     // complicated if a new repair session will start. What
                     // will happen with all the host that already got the
                     // solution?
                     logger_.debug("{}:{} is not reachable.", host, host.getPort());
                 }
             }
         }
     }
 
     /**
      * This method will be called in response to an rpc from a remote host.
      * All the hosts that failed must be removed from the routing table and
      * the issue registry. Then it will update the routing table given the
      * updated hosts from the solution. Finally, it will continue the solution
      * broadcasting.
      * <p/>
      * In the Solution there is an array that holds all the UUIDs of the hosts
      * that have already received the solution. Before this host start
      * broadcasting, it will first add all his neighbours according to its
      * routing table.
      * <p/>
      * In case of a communication exception, the host will do nothing. The
      * broadcast protocol is already complicated. If a new repair session
      * starts before the current solution is broadcasted then things get messy.
      * <p/>
      * TODO: [Important] Needs evaluation!!!!!!
      *
      * @param solution for a specific repair issue.
      */
     public void onReceivePushSolution(RepairSolution solution) {
         logger_.info("Solution for path {} received.", solution.failedPath);
 
         List<String> oldUuidSentList = new ArrayList<String>(solution.uuidSent.length);
         Collections.addAll(oldUuidSentList, solution.uuidSent);
 
         List<String> newUuidSentList = new ArrayList<String>(solution.uuidSent.length);
         Collections.addAll(newUuidSentList, solution.uuidSent);
 
         for (int i = 0; i < solution.failedHosts.length; i++) {
             Host host = Deserializer.deserializeHost(solution.failedHosts[i]);
             if (routingTable_.contains(host)) {
                 routingTable_.removeReference(host);
             }
             if (registry_.containsHost(host.getUUID())) {
                 registry_.removeIssue(host.getUUID());
             }
         }
 
         for (int i = 0; i < solution.updatedHosts.length; i++) {
             Host host = Deserializer.deserializeHost(solution.updatedHosts[i]);
             // At the moment only update only the existing hosts. An
             // improvement is to add all the updated hosts in the routing table
             // and then RoutingTable.refresh(...) will take care of everything
             // else.
             if (routingTable_.contains(host)) {
                 routingTable_.updateReference(host);
             }
         }
 
         routingTable_.refresh(maxRef_);
 
         for (Host host : routingTable_.getAllHosts()) {
             if (!newUuidSentList.contains(host.getUUID().toString())) {
                 newUuidSentList.add(host.getUUID().toString());
             }
         }
 
         solution.uuidSent = newUuidSentList.toArray(new String[newUuidSentList.size()]);
         solution.pushCount++;
 
         for (Host host : routingTable_.getAllHosts()) {
             if (!oldUuidSentList.contains(host.getUUID().toString())) {
                 try {
                     RepairHandle repairHandle = getRemoteHandle(host);
                     repairHandle.pushSolution(solution);
                 } catch (CommunicationException e) {
                     // Ignore the failure. The broadcasting becomes really
                     // complicated if a new repair session will start. What
                     // will happen with all the host that already got the
                     // solution?
                     logger_.debug("{}:{} is not reachable.", host, host.getPort());
                 }
             }
         }
     }
 
     /**
      * Checks if the object is correctly initialized with the correct
      * algorithm objects.
      */
     private void validateService() {
         ArgumentCheck.checkNotNull(replace_, "Repair service is not in a valid state. No ReplaceStrategy was found.");
         ArgumentCheck.checkNotNull(fix_, "Repair service is not in a valid state. No FixNodeAlgorithm was found.");
     }
 
     /**
      * Given a host, it will return a corba handle of the repair interface stub
      * so the local host can execute rpc.
      *
      * @param host that the local host wants to communicate with.
      * @return the repair interface stub.
      * @throws CommunicationException if the remote host cannot be reached.
      */
     private RepairHandle getRemoteHandle(Host host) throws CommunicationException {
         String[] repairHandleID = RepairHandleHelper.id().split(":");
         String corbaloc = "corbaloc:iiop:[" +
                 host.getAddress().getHostAddress() + "]:" + host.getPort()
                 + "/" + repairHandleID[1];
         logger_.debug("CORBALOC: {}", corbaloc);
         org.omg.CORBA.Object object = orb_.string_to_object(corbaloc);
 
         RepairHandle handle;
         try {
             handle = RepairHandleHelper.narrow(object);
         } catch (SystemException e) {
             throw new CommunicationException(e.getCause());
         }
         return handle;
     }
 
     /**
      * Given a failed, this method computes the path to pass to the
      * {@link FixNodeAlgorithm#execute(pgrid.entity.routingtable.RoutingTable, pgrid.entity.PGridPath)}.
      * It is essential for the algorithm.
      *
      * @param failedHostPath the failed path of a single host or a complete
      *                       subtree.
      * @return the path that the algorithm will start searching from.
      */
     public PGridPath algorithmPathExecution(PGridPath failedHostPath) {
         String root = failedHostPath.subPath(0, failedHostPath.length() - 1);
         char lastChar = failedHostPath.value(failedHostPath.length() - 1);
         PGridPath initialPath = new PGridPath(root);
         initialPath.revertAndAppend(lastChar);
         return initialPath;
     }
 }
