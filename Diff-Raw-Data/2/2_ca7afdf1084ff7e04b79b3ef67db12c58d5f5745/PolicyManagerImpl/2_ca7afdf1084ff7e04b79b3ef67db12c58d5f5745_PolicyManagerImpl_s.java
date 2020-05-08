 package org.mule.galaxy.impl.policy;
 
 import static org.mule.galaxy.impl.jcr.JcrUtil.getOrCreate;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.lifecycle.Lifecycle;
 import org.mule.galaxy.lifecycle.Phase;
 import org.mule.galaxy.policy.ArtifactPolicy;
 import org.mule.galaxy.policy.PolicyManager;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 public class PolicyManagerImpl implements PolicyManager, ApplicationContextAware {
     private Map<String, ArtifactPolicy> policies = new HashMap<String, ArtifactPolicy>();
     private JcrTemplate jcrTemplate;
     private String lifecyclesNodeId;
     private String workspaceLifecyclesNodeId;
     private String artifactsLifecyclesNodeId;
     private String artifactsPhasesNodeId;
     private String workspacesPhasesNodeId;
     private String phasesNodeId;
     
     public void initilaize() throws Exception{
         Session session = jcrTemplate.getSessionFactory().getSession();
         Node root = session.getRootNode();
         
         Node activations = getOrCreate(root, "policyActivations");
         lifecyclesNodeId = getOrCreate(activations, "lifecycles").getUUID();
         phasesNodeId = getOrCreate(root, "phases").getUUID();
 
         Node artifacts = getOrCreate(activations, "artifacts");
         artifactsLifecyclesNodeId = getOrCreate(artifacts, "lifecycles").getUUID();
         artifactsPhasesNodeId = getOrCreate(artifacts, "phases").getUUID();
         
         Node workspaces = getOrCreate(activations, "workspaces");
         workspaceLifecyclesNodeId = getOrCreate(workspaces, "lifecycles").getUUID();
         workspacesPhasesNodeId = getOrCreate(workspaces, "phases").getUUID();
         
         session.save();
     }
     
     public void setApplicationContext(ApplicationContext ctx) throws BeansException {
         String[] names = ctx.getBeanNamesForType(ArtifactPolicy.class);
         for (String s : names) {
             ArtifactPolicy p = (ArtifactPolicy) ctx.getBean(s);
             policies.put(p.getId(), p);
         }
     }
 
     public ArtifactPolicy getPolicy(String id) {
         return policies.get(id);
     }
 
     public Collection<ArtifactPolicy> getPolicies() {
         return policies.values();
     }
 
     public void activatePolicy(ArtifactPolicy p, Artifact a, Collection<Phase> phases) {
         activatePolicy(artifactsPhasesNodeId, phases, p.getId(), a.getId());
     }
     
     private void activatePolicy(final String nodeId, 
                                 final Collection<Phase> phases,
                                 final String policyId,
                                 final String... nodes) {
         jcrTemplate.execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = session.getNodeByUUID(nodeId);
                 
                 for (String name : nodes) {
                     node = getOrCreate(node, name);
                 }
                 for (Phase p : phases) {
                     Node lNode = getOrCreate(node, p.getLifecycle().getName());
                     Node pNode = getOrCreate(lNode, p.getName());
                     getOrCreate(pNode, policyId);
                 }
                 
                 session.save();
                 return null;
             }
         });
     }
     public void activatePolicy(ArtifactPolicy p, Artifact a, Lifecycle lifecycle) {
         activatePolicy(artifactsLifecyclesNodeId, a.getId(), lifecycle.getName(), p.getId());
     }
 
     public void activatePolicy(ArtifactPolicy p, Collection<Phase> phases) {
         activatePolicy(phasesNodeId, phases, p.getId());
     }
 
     public void activatePolicy(final ArtifactPolicy p, final Lifecycle lifecycle) {
         activatePolicy(lifecyclesNodeId, lifecycle.getName(), p.getId());
     }
 
     public void activatePolicy(ArtifactPolicy p, Workspace w, Collection<Phase> phases) {
         activatePolicy(workspacesPhasesNodeId, phases, p.getId(), w.getId());
     }
 
     public void activatePolicy(final ArtifactPolicy p, final Workspace w, final Lifecycle lifecycle) {
         activatePolicy(workspaceLifecyclesNodeId, w.getId(), lifecycle.getName(), p.getId());
     }
 
     private void activatePolicy(final String nodeId, final String... nodes) {
         jcrTemplate.execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = session.getNodeByUUID(nodeId);
                 
                 for (String name : nodes) {
                     node = getOrCreate(node, name);
                 }
                 
                 session.save();
                 return null;
             }
         });
     }
 
     public Collection<ArtifactPolicy> getActivePolicies(final Artifact a) {
         final Set<ArtifactPolicy> activePolicies = new HashSet<ArtifactPolicy>();
         jcrTemplate.execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 String lifecycle = a.getPhase().getLifecycle().getName();
                String workspace = a.getWorkspace().getName();
                 
                 addPolicies(activePolicies, session, lifecyclesNodeId, lifecycle);
                 addPolicies(activePolicies, session, workspaceLifecyclesNodeId, 
                             workspace, lifecycle);
                 addPolicies(activePolicies, session, artifactsLifecyclesNodeId, 
                             a.getId(), lifecycle);
                 addPolicies(activePolicies, session, phasesNodeId, 
                             lifecycle, a.getPhase().getName());
                 addPolicies(activePolicies, session, workspacesPhasesNodeId, 
                             workspace, lifecycle, a.getPhase().getName());
                 addPolicies(activePolicies, session, artifactsPhasesNodeId, 
                             a.getId(), lifecycle, a.getPhase().getName());
                 
                 return null;
             }
 
         });
         return activePolicies;
     }
     
     private void addPolicies(final Set<ArtifactPolicy> activePolicies,
                              final Session session,
                              final String rootNodeId,
                              String... nodeIds) throws ItemNotFoundException, RepositoryException {
         try {
             Node node = session.getNodeByUUID(rootNodeId);
             for (String name : nodeIds) {
                 node = node.getNode(name);
             }
             
             for (NodeIterator itr = node.getNodes(); itr.hasNext();) {
                 ArtifactPolicy p = policies.get(itr.nextNode().getName());
                 if (p != null) {
                     activePolicies.add(p);
                 }
             }
         } catch (PathNotFoundException e){
             
         }
     }
     
     public void deactivatePolicy(ArtifactPolicy p, Artifact a, Collection<Phase> phases) {
         deactivatePolicy(artifactsPhasesNodeId, phases, p.getId(), a.getId());
     }
     
     public void deactivatePolicy(ArtifactPolicy p, Artifact a, Lifecycle lifecycle) {
         deactivatePolicy(artifactsLifecyclesNodeId, a.getId(), lifecycle.getName(), p.getId());
     }
 
     public void deactivatePolicy(ArtifactPolicy p, Collection<Phase> phases) {
         deactivatePolicy(phasesNodeId, phases, p.getId());
     }
     
     private void deactivatePolicy(final String nodeId, 
                                   final Collection<Phase> phases,
                                   final String policyId,
                                   final String... nodes) {
         jcrTemplate.execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = session.getNodeByUUID(nodeId);
                 
                 try {
                     for (String name : nodes) {
                         node = node.getNode(name);
                     }
                 } catch (PathNotFoundException e) {
                     return null;
                 }
             
                 for (Phase p : phases) {
                     try {
                         Node lNode = node.getNode(p.getLifecycle().getName());
                         Node pNode = lNode.getNode(p.getName());
                         pNode.getNode(policyId).remove();
                     } catch (PathNotFoundException e) {
                     }
                 }
                 
                 session.save();
                 return null;
             }
         });
     }
     public void deactivatePolicy(ArtifactPolicy p, Lifecycle lifecycle) {
         deactivatePolicy(lifecyclesNodeId, lifecycle.getName(), p.getId());
     }
 
     private void deactivatePolicy(final String nodeId, final String... nodes) {
         jcrTemplate.execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = session.getNodeByUUID(nodeId);
                 try {
                     for (String name : nodes) {
                         node = getOrCreate(node, name);
                     }
                     
                     node.remove();
                 } catch (PathNotFoundException e) {
                     
                 }
                 
                 session.save();
                 return null;
             }
         });
     }
     
     public void deactivatePolicy(ArtifactPolicy p, Workspace w, Collection<Phase> phases) {
         deactivatePolicy(workspacesPhasesNodeId, phases, p.getId(), w.getId());
     }
 
     public void deactivatePolicy(ArtifactPolicy p, Workspace w, Lifecycle lifecycle) {
         deactivatePolicy(workspaceLifecyclesNodeId, w.getId(), lifecycle.getName(), p.getId());
     }
     
     public void setJcrTemplate(JcrTemplate jcrTemplate) {
         this.jcrTemplate = jcrTemplate;
     }
     
 }
