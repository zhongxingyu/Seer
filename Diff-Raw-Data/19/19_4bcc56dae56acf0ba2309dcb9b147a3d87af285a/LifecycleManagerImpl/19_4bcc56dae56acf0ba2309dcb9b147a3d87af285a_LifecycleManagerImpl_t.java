 package org.mule.galaxy.impl.lifecycle;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Property;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.Value;
 import javax.jcr.query.InvalidQueryException;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 
 import org.apache.jackrabbit.util.ISO9075;
 import org.mule.galaxy.ActivityManager;
 import org.mule.galaxy.ArtifactPolicyException;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.Dao;
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.ActivityManager.EventType;
 import org.mule.galaxy.impl.jcr.JcrUtil;
 import org.mule.galaxy.impl.jcr.JcrVersion;
 import org.mule.galaxy.impl.jcr.onm.AbstractDao;
 import org.mule.galaxy.lifecycle.Lifecycle;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.lifecycle.Phase;
 import org.mule.galaxy.lifecycle.PhaseLogEntry;
 import org.mule.galaxy.lifecycle.TransitionException;
 import org.mule.galaxy.policy.ApprovalMessage;
 import org.mule.galaxy.policy.ArtifactPolicy;
 import org.mule.galaxy.policy.PolicyManager;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.util.BundleUtils;
 import org.mule.galaxy.util.Message;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springmodules.jcr.JcrCallback;
 
 
 public class LifecycleManagerImpl extends AbstractDao<Lifecycle> 
     implements LifecycleManager, ApplicationContextAware {
 
     private static final String INITIAL_PHASE = "initial";
     private static final String DEFAULT = "default";
     private static final String NEXT_PHASES = "nextPhases";
 
     private List<ArtifactPolicy> phaseApprovalListeners = new ArrayList<ArtifactPolicy>();
     private Dao<PhaseLogEntry> entryDao;
     private PolicyManager policyManager;
     private ApplicationContext context;
     private ActivityManager activityManager;
     
     public LifecycleManagerImpl() throws Exception {
         super(Lifecycle.class, "lifecycles", false);
     }
 
     public Lifecycle getDefaultLifecycle() {
         return (Lifecycle) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 return build(getDefaultLifecycleNode(session), session);
             }
         });
     }
 
     protected Node getDefaultLifecycleNode(Session session) 
         throws RepositoryException {
         QueryManager qm = getQueryManager(session);
         javax.jcr.query.Query q = qm.createQuery("//(*, galaxy:lifecycle)[@default='true']", javax.jcr.query.Query.XPATH);
         
         NodeIterator nodes = q.execute().getNodes();
         if (nodes.getSize() == 0) {
             throw new RuntimeException("No default lifecycle was found!");
         }
         
         return nodes.nextNode();
     }
 
     public void setDefaultLifecycle(final Lifecycle l) {
         execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node lNode = getDefaultLifecycleNode(session);
                 lNode.setProperty(DEFAULT, false);
                 
                 Node newDefaultNode = getNodeByUUID(l.getId());
                 newDefaultNode.setProperty(DEFAULT, true);
                 
                 session.save();
                 return null;
             }
             
         });
     }
 
     public Lifecycle getLifecycle(Workspace workspace) {
         return getDefaultLifecycle();
     }
 
     public Collection<Lifecycle> getLifecycles() {
         return listAll();
     }
     
     public void save(Lifecycle l) throws DuplicateItemException, NotFoundException {
         // TODO: we should have a validation related exception in the DAO framework
         if (l.getName() == null || "".equals(l.getName())) {
             throw new RuntimeException(new Message("NAME_NOT_NULL", BundleUtils.getBundle(LifecycleManagerImpl.class)).toString());
         }
         
         if (l.getInitialPhase() == null) {
             throw new RuntimeException(new Message("INITIAL_PHASE_NOT_NULL", BundleUtils.getBundle(LifecycleManagerImpl.class)).toString());
         }
         
         super.save(l);
     }
 
     public void delete(final String lifecycleId, 
                        final String fallbackLifecycleId) throws NotFoundException {
         final Lifecycle fallbackLifecycle;
         if (fallbackLifecycleId != null) {
             fallbackLifecycle = getLifecycleById(fallbackLifecycleId);
         } else {
             fallbackLifecycle = null;
         }
         
         if (fallbackLifecycle == null) {
             throw new NotFoundException(fallbackLifecycleId);
         }
         
         if (fallbackLifecycleId.equals(lifecycleId)) {
             throw new IllegalArgumentException("The fallback lifecycle cannot be the same as the lifecycle being deleted.");
         }
         System.out.println("deleting " + lifecycleId + " and falling back to " + fallbackLifecycleId);
         
         final Lifecycle lifecycle = getLifecycleById(lifecycleId);
         
         execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Phase p = fallbackLifecycle.getInitialPhase();
                 
                 // update all the artifacts using this lifecycle
                 NodeIterator nodes = getArtifactVersionsInLifecycle(lifecycle.getId(), session);
                 
                 while (nodes.hasNext()) {
                     Node n = nodes.nextNode();
                     
                     n.setProperty(JcrVersion.LIFECYCLE, fallbackLifecycle.getId());
                     n.setProperty(JcrVersion.PHASE, p.getId());
                 }
 
                 // switch the default lifecycle for workspaces
                 nodes = getWorkspacesInLifecycle(lifecycleId, session);
                 
                 while (nodes.hasNext()) {
                     Node n = nodes.nextNode();
                     
                     n.setProperty(JcrVersion.LIFECYCLE, fallbackLifecycle.getId());
                 }
 
                 // technically we should clean up the policy manager too
                 // but we can do that lazily inside the LM :-)
                 
                 // actually delete the lifecycle
                 doDelete(lifecycleId, session);
                 
                 return false;
             }
             
         });
     }
 
     @Override
     protected void doCreateInitialNodes(Session session, javax.jcr.Node objects) throws RepositoryException {
         if (objects.getNodes().getSize() > 0)
             return;
         
         Node lNode = JcrUtil.getOrCreate(objects, "Default");
         lNode.setProperty(DEFAULT, true);
         
         Node created = addPhaseNode(lNode, "Created", new String[] { "Developed" });
         created.setProperty(INITIAL_PHASE, true);
         
         addPhaseNode(lNode, "Developed", new String[] { "Tested" });
        addPhaseNode(lNode, "Tested", new String[] { "Staged", "Production", "Retired" });
        addPhaseNode(lNode, "Staged", new String[] { "Production", "Retired" });
        addPhaseNode(lNode, "Production", new String[] { "Retired" });
         addPhaseNode(lNode, "Retired", new String[0]);
     }
 
     private Node addPhaseNode(Node lNode, String name, String[] nextPhases) throws RepositoryException {
         Node pNode = lNode.addNode(name);
         pNode.addMixin("mix:referenceable");
         pNode.setProperty(NEXT_PHASES, nextPhases);
         return pNode;
     }
     
     public boolean isTransitionAllowed(ArtifactVersion a, Phase p2) {
         Phase p = a.getPhase();
         Lifecycle l = p2.getLifecycle();
         
         if (p == null || !l.equals(p.getLifecycle())) {
             return l.getInitialPhase().equals(p2);
         } else {
             return p != null && p.getNextPhases() != null && p.getNextPhases().contains(p2);
         }
     }
     
     public void transition(final ArtifactVersion a, 
                            final Phase p, 
                            final User user) throws TransitionException, ArtifactPolicyException {
         if (!isTransitionAllowed(a, p)) {
             throw new TransitionException(p);
         }
         
         executeWithPolicyException(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 JcrVersion ja = (JcrVersion) a;
                 ja.setPhase(p);
                 
                 ArtifactVersion previous = a.getPrevious();
                 
                 boolean approved = true;
                 List<ApprovalMessage> approvals = getPolicyManager().approve(previous, a);
                 for (ApprovalMessage app : approvals) {
                     if (!app.isWarning()) {
                         approved = false;
                         break;
                     }
                 }
                 
                 if (!approved) {
                     throw new RuntimeException(new ArtifactPolicyException(approvals));
                 }
                 
                 PhaseLogEntry entry = new PhaseLogEntry();
                 entry.setUser(user);
                 entry.setPhase(p);
                 entry.setArtifactVersion(a);
                 
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(new Date());
                 entry.setCalendar(cal);
                 
                 try {
                     entryDao.save(entry);
                 } catch (DuplicateItemException e1) {
                     // should never happen
                     throw new RuntimeException(e1);
                 } catch (NotFoundException e1) {
                     // should never happen
                     throw new RuntimeException(e1);
                 }
 
                 getActivityManager().logActivity(user,
                                                  "Artifact " + ja.getParent().getPath() 
                                                      + " (version " + ja.getVersionLabel()
                                                      + ") was transitioned to phase "
                                                      + p.getName() + " in lifecycle "
                                                      + p.getLifecycle().getName(), EventType.INFO);
                                             
                 session.save();
                 return null;
             }
             
         });
     }
 
     protected PolicyManager getPolicyManager() {
         if (policyManager == null) {
             policyManager = (PolicyManager) context.getBean("policyManager");
         }
         return policyManager;
     }
 
     private void executeWithPolicyException(JcrCallback jcrCallback) throws ArtifactPolicyException {
         try {
             execute(jcrCallback);
         } catch (RuntimeException e) {
             if (e.getCause() instanceof ArtifactPolicyException) {
                 throw (ArtifactPolicyException) e.getCause();
             }
             throw e;
         }
     }
     
     public List<ArtifactPolicy> getPhaseApprovalListeners() {
         return phaseApprovalListeners;
     }
 
     public void setPhaseApprovalListeners(List<ArtifactPolicy> phaseApprovalListeners) {
         this.phaseApprovalListeners = phaseApprovalListeners;
     }
 
     public ActivityManager getActivityManager() {
         if (activityManager == null) {
             // workaround because spring sucks at circular dependencies
             activityManager = (ActivityManager) context.getBean("activityManager");
         }
         return activityManager;
     }
 
     public Lifecycle getLifecycle(final String lifecycleName) {
         return (Lifecycle) execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 try {
                     Node node = getObjectsNode(session).getNode(ISO9075.encode(lifecycleName));
                     
                     return build(node, session);
                 } catch (PathNotFoundException e) {
                     return null;
                 }
             }
             
         });
     }
     
 
     public Lifecycle getLifecycleById(final String id) {
         return (Lifecycle) execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 try {
                     Node node = getNodeByUUID(id);
                     
                     return build(node, session);
                 } catch (PathNotFoundException e) {
                     return null;
                 } 
             }
         });
     }
 
     @Override
     public Lifecycle build(Node node, Session session) throws RepositoryException {
         Lifecycle l = new Lifecycle();
         l.setId(node.getUUID());
         l.setName(node.getName());
         l.setPhases(new HashMap<String,Phase>());
         
         for (NodeIterator nodes = node.getNodes(); nodes.hasNext();) {
             Node phaseNode = nodes.nextNode();
             
             Phase phase = new Phase(l);
             phase.setId(phaseNode.getUUID());
             phase.setName(ISO9075.decode(phaseNode.getName()));
             
             l.getPhases().put(phase.getName(), phase);
         }
         
         for (NodeIterator nodes = node.getNodes(); nodes.hasNext();) {
             Node phaseNode = nodes.nextNode();
             
             Phase phase = l.getPhaseById(phaseNode.getUUID());
             
             if (phase == null) {
                 throw new RuntimeException("Null phase for node " + phaseNode.getName());
             }
             
             HashSet<Phase> nextPhases = new HashSet<Phase>();
             try {
                 Property property = phaseNode.getProperty(NEXT_PHASES);
                 
                 for (Value v : property.getValues()) {
                     Phase next = l.getPhase(v.getString());
                     
                     nextPhases.add(next);
                 }
                 
                 phase.setNextPhases(nextPhases);
             } catch (PathNotFoundException e) {
                 
             }
             
             try {
                 Property property = phaseNode.getProperty(INITIAL_PHASE);
                 
                 if (property.getValue().getBoolean()) {
                     l.setInitialPhase(phase);
                 }
             } catch (PathNotFoundException e) {
             
             }
         }
         
         if (l.getInitialPhase() == null) {
             throw new RuntimeException("No initial phase was found for lifecycle " + l.getName());
         }
      
         return l;
     }
 
     @Override
     protected Node findNode(String id, Session session) throws RepositoryException {
         if (id == null) {
             return null;
         }
         return getNodeByUUID(id);
     }
 
     @Override
     protected String generateNodeName(Lifecycle t) {
         return t.getName();
     }
 
     @Override
     protected void persist(Lifecycle l, javax.jcr.Node lNode, Session session) throws Exception {
         for (Phase p : l.getPhases().values()) {
            Node pNode = getChild(lNode, p.getId());
            
            // We're creating a new phase
            if (pNode == null) {
                pNode = JcrUtil.getOrCreate(lNode, p.getName());
                p.setId(pNode.getUUID());
            }
            
            ArrayList<String> nextPhases = new ArrayList<String>();
            for (Phase nextPhase : p.getNextPhases()) {
                nextPhases.add(nextPhase.getName());
            }
            
            if (l.getInitialPhase().equals(p)) {
                pNode.setProperty(INITIAL_PHASE, true);
            } else {
                pNode.setProperty(INITIAL_PHASE, false);
            }
            
            pNode.setProperty(NEXT_PHASES, nextPhases.toArray(new String[nextPhases.size()]));
 
            if (!p.getName().equals(pNode.getName())) {
                session.move(pNode.getPath(), 
                             pNode.getParent().getPath() + "/" + ISO9075.encode(p.getName()));
            }
         }
         for (NodeIterator nodes = lNode.getNodes(); nodes.hasNext();) {
             Node node = nodes.nextNode();
             
             if (l.getPhaseById(node.getUUID()) == null) {
                 NodeIterator artifacts = getArtifactsInPhase(node.getUUID(), session);
                 
                 if (artifacts.getSize() > 0) {
                     // we should probably throw an exception here, but for now
                     // we'll switch people back to the beginning phase.
                     
                     while (artifacts.hasNext()) {
                         Node artifactNode = artifacts.nextNode();
                         
                         artifactNode.setProperty(JcrVersion.PHASE, l.getInitialPhase().getId());
                     }
                 }
                 
                 node.remove();
             }
         }
 
         String newName = l.getName();
         String origName = ISO9075.decode(lNode.getName());
         
         if (!newName.equals(origName)) {
             session.move(lNode.getPath(), 
                          lNode.getParent().getPath() + "/" + ISO9075.encode(newName));
             lNode.setProperty("name", newName);
         }
     }
     
     private Node getChild(Node node, String id) throws RepositoryException {
         if (id == null) return null;
         
         for (NodeIterator nodes = node.getNodes(); nodes.hasNext();) {
             Node n = nodes.nextNode();
             if (id.equals(n.getUUID())) {
                 return n;
             }
         }
         return null;
     }
     
 
     public Phase getPhaseById(final String id) {
         return (Phase) execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node pNode = getNodeByUUID(id);
                 
                 Lifecycle l = build(pNode.getParent(), session);
                 
                 return l.getPhaseById(id);
             }
             
         });
     }
 
     @Override
     protected String getId(Lifecycle t, Node node, Session session) throws RepositoryException {
         return node.getUUID();
     }
 
     @Override
     protected String getNodeType() {
         return "galaxy:lifecycle";
     }
 
     public void setPhaseLogEntryDao(Dao<PhaseLogEntry> entryDao) {
         this.entryDao = entryDao;
     }
 
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.context = applicationContext;
     }
 
     private NodeIterator getArtifactVersionsInLifecycle(final String lifecycleId, Session session)
         throws RepositoryException, InvalidQueryException {
         QueryManager qm = getQueryManager(session);
         javax.jcr.query.Query query = 
             qm.createQuery("//element(*, galaxy:artifactVersion)[@lifecycle = '" + lifecycleId + "']", 
                            javax.jcr.query.Query.XPATH);
         
         QueryResult qr = query.execute();
         
         NodeIterator nodes = qr.getNodes();
         return nodes;
     }
     
 
     private NodeIterator getArtifactsInPhase(final String phaseId, Session session)
         throws RepositoryException, InvalidQueryException {
         QueryManager qm = getQueryManager(session);
         javax.jcr.query.Query query = 
             qm.createQuery("//element(*, galaxy:artifact)[@phaseId = '" + phaseId + "']", 
                            javax.jcr.query.Query.XPATH);
         
         QueryResult qr = query.execute();
         
         NodeIterator nodes = qr.getNodes();
         return nodes;
     }
 
     private NodeIterator getWorkspacesInLifecycle(final String lifecycleName, Session session)
         throws RepositoryException, InvalidQueryException {
         QueryManager qm = getQueryManager(session);
         javax.jcr.query.Query query = 
             qm.createQuery("//element(*, galaxy:workspace)[@lifecycle = '" + lifecycleName + "']", 
                            javax.jcr.query.Query.XPATH);
         
         QueryResult qr = query.execute();
         
         NodeIterator nodes = qr.getNodes();
         return nodes;
     }
     @Override
     protected String getObjectNodeName(Lifecycle t) {
         return t.getName();
     }
     
 }
