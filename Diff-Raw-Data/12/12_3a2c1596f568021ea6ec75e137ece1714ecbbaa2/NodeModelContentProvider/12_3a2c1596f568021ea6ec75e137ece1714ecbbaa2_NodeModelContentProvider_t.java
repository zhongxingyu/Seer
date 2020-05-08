 /**
  * 
  */
 package org.feature.cluster.model.editor.zest.model;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.zest.core.widgets.Graph;
 import org.feature.cluster.model.cluster.CoreGroup;
 import org.feature.cluster.model.cluster.Group;
 import org.feature.cluster.model.cluster.GroupModel;
 import org.feature.cluster.model.cluster.ViewPoint;
 import org.feature.cluster.model.cluster.ViewPointContainer;
 import org.featuremapper.models.feature.Feature;
 import org.featuremapper.models.feature.FeatureModel;
 import org.featuremapper.models.featuremapping.And;
 import org.featuremapper.models.featuremapping.ElementMapping;
 import org.featuremapper.models.featuremapping.FeatureMappingModel;
 import org.featuremapper.models.featuremapping.FeatureRef;
 import org.featuremapper.models.featuremapping.Mapping;
 import org.featuremapper.models.featuremapping.Term;
 
 /**
  * Transforms the {@link FeatureMappingModel} into a {@link Graph}.
  * 
  * @author Tim Winkelmann
  * 
  */
 public class NodeModelContentProvider {
 
    private CoreGroupNode coreGroupNode;
    private Map<ViewPoint, ViewPointNode> viewPointNodes = new HashMap<ViewPoint, ViewPointNode>();
    private Map<Group, GroupNode> groupdNodes = new HashMap<Group, GroupNode>();
    private Map<Feature, FeatureNode> featureNodes = new HashMap<Feature, FeatureNode>();
    private List<Connection> connections = new LinkedList<Connection>();
 
    /**
     * 
     * @param featureMappingModel
     * @param groupModel
     */
    public NodeModelContentProvider(FeatureMappingModel featureMappingModel, GroupModel groupModel) {
       if (featureMappingModel != null && groupModel != null && groupModel.getCoreGroup() != null) {
          createGroupModelNodes(groupModel);
          createFeatureNodes(featureMappingModel.getFeatureModel().getValue());
          EList<Mapping> mappings = featureMappingModel.getMappings();
          for (Mapping mapping : mappings) {
             if (mapping instanceof ElementMapping) {
                ElementMapping elementMapping = (ElementMapping) mapping;
                EObject element = elementMapping.getElement();
                if (element instanceof Group) {
                   Group group = (Group) element;
                   createConnections(group, elementMapping);
                }
                if (element instanceof ViewPoint) {
                   ViewPoint viewPoint = (ViewPoint) element;
                   createConnections(viewPoint, elementMapping);
                }
                if (element instanceof CoreGroup) {
                   createConnections(elementMapping);
                }
             }
          }
       }
       Collection<FeatureNode> values = featureNodes.values();
       for (FeatureNode featureNode : values) {
          if (featureNode.getConnectedTo().isEmpty()) {
             Connection connection = new Connection(coreGroupNode, featureNode);
             coreGroupNode.getConnectedTo().add(featureNode);
             featureNode.getConnectedTo().add(coreGroupNode);
             connections.add(connection);
          }
       }
       System.out.println("#featureNodes:" + featureNodes.size());
    }
 
    /**
     * create all connections between the group and the features
     * 
     * @param group the group
     * @param elementMapping the mapping containing one or more features
     */
    private void createConnections(Group group, ElementMapping elementMapping) {
       if (elementMapping.getTerm() instanceof FeatureRef) {
          FeatureRef featureRef = (FeatureRef) elementMapping.getTerm();
          GroupNode groupNode = groupdNodes.get(group);
          FeatureNode featureNode = featureNodes.get(featureRef.getFeature());
          Connection connection = new Connection(groupNode, featureNode);
          groupNode.getConnectedTo().add(featureNode);
          featureNode.getConnectedTo().add(groupNode);
          connections.add(connection);
       } else if (elementMapping.getTerm() instanceof And) { // TODO Test for other possible terms
          And and = (And) elementMapping.getTerm();
          EList<Term> terms = and.getTerms();
          for (Term term : terms) {
             if (term instanceof FeatureRef) {
                FeatureRef featureRef = (FeatureRef) term;
                GroupNode groupNode = groupdNodes.get(group);
               if (!(group instanceof CoreGroup)) {
                  FeatureNode featureNode = featureNodes.get(featureRef.getFeature());
                  Connection connection = new Connection(groupNode, featureNode);
                  groupNode.getConnectedTo().add(featureNode);
                  featureNode.getConnectedTo().add(groupNode);
                  connections.add(connection);
               }
             }
          }
       }
    }
 
    /**
     * create all connections between the {@link ViewPoint} and the {@link Feature}
     * 
     * @param viewPoint the {@link ViewPoint}
     * @param elementMapping the mapping containing one or more features
     */
    private void createConnections(ViewPoint viewPoint, ElementMapping elementMapping) {
       if (elementMapping.getTerm() instanceof FeatureRef) {
          FeatureRef featureRef = (FeatureRef) elementMapping.getTerm();
          ViewPointNode viewPointNode = viewPointNodes.get(viewPoint);
          FeatureNode featureNode = featureNodes.get(featureRef.getFeature());
          Connection connection = new Connection(viewPointNode, featureNode);
          viewPointNode.getConnectedTo().add(featureNode);
          featureNode.getConnectedTo().add(viewPointNode);
          connections.add(connection);
       } else if (elementMapping.getTerm() instanceof And) {
          And and = (And) elementMapping.getTerm();
          EList<Term> terms = and.getTerms();
          for (Term term : terms) {
             if (term instanceof FeatureRef) {
                FeatureRef featureRef = (FeatureRef) term;
                ViewPointNode viewPointNode = viewPointNodes.get(viewPoint);
                FeatureNode featureNode = featureNodes.get(featureRef.getFeature());
                Connection connection = new Connection(viewPointNode, featureNode);
                viewPointNode.getConnectedTo().add(featureNode);
                featureNode.getConnectedTo().add(viewPointNode);
                connections.add(connection);
             }
          }
       }
    }
 
    /**
     * create all connections between the default group and the features
     * 
     * @param elementMapping the mapping containing one or more features
     */
    private void createConnections(ElementMapping elementMapping) {
       if (elementMapping.getTerm() instanceof FeatureRef) {
          FeatureRef featureRef = (FeatureRef) elementMapping.getTerm();
          FeatureNode featureNode = featureNodes.get(featureRef.getFeature());
          Connection connection = new Connection(coreGroupNode, featureNode);
          coreGroupNode.getConnectedTo().add(featureNode);
          featureNode.getConnectedTo().add(coreGroupNode);
          connections.add(connection);
       } else if (elementMapping.getTerm() instanceof And) {
          And and = (And) elementMapping.getTerm();
          EList<Term> terms = and.getTerms();
          for (Term term : terms) {
             if (term instanceof FeatureRef) {
                FeatureRef featureRef = (FeatureRef) term;
                FeatureNode featureNode = featureNodes.get(featureRef.getFeature());
                Connection connection = new Connection(coreGroupNode, featureNode);
                coreGroupNode.getConnectedTo().add(featureNode);
                featureNode.getConnectedTo().add(coreGroupNode);
                connections.add(connection);
             }
          }
       }
    }
 
    /**
     * creates feature nodes from the feature model
     * 
     * @param featureModel the feature model
     */
    private void createFeatureNodes(FeatureModel featureModel) {
       EList<Feature> children = featureModel.getAllFeatures();
       for (Feature feature : children) {
          FeatureNode featureNode = new FeatureNode(feature);
          featureNodes.put(feature, featureNode);
          createFeatureNodes(feature.getGroups());
       }
    }
 
    /**
     * creates feature nodes from a feature group
     * 
     * @param groups group of features
     */
    private void createFeatureNodes(EList<org.featuremapper.models.feature.Group> groups) {
       for (org.featuremapper.models.feature.Group group : groups) {
          EList<Feature> childFeatures = group.getChildFeatures();
          for (Feature childFeature : childFeatures) {
             FeatureNode featureNode = new FeatureNode(childFeature);
             featureNodes.put(childFeature, featureNode);
             createFeatureNodes(childFeature.getGroups());
          }
       }
    }
 
    /**
     * 
     * @param defaultGroup
     */
    private void createGroupModelNodes(GroupModel groupModel) {
       this.coreGroupNode = new CoreGroupNode(groupModel.getCoreGroup());
       // create group nodes
       createGroupNodes(groupModel.getCoreGroup().getGroups());
       // create viewPoint nodes
 
       ViewPointContainer viewPointContainer = groupModel.getViewPointContainer();
       if (viewPointContainer != null) {
          createViewPointNodes(viewPointContainer.getViewPoints());
          for (GroupNode groupNode : groupdNodes.values()) {
             EList<ViewPoint> viewPointReference = groupNode.getGroup().getViewPointReference();
             for (ViewPoint viewPoint : viewPointReference) {
                ViewPointNode cn = viewPointNodes.get(viewPoint);
                Connection connection = new Connection(cn, groupNode);
                cn.getConnectedTo().add(groupNode);
                // groupNode.getConnectedTo().add(cn);
                connections.add(connection);
             }
          }
       }
    }
 
    /**
     * 
     * @param viewPoints
     */
    private void createViewPointNodes(EList<ViewPoint> viewPoints) {
       for (ViewPoint viewPoint : viewPoints) {
          ViewPointNode cn = new ViewPointNode(viewPoint);
          viewPointNodes.put(viewPoint, cn);
          Connection connection = new Connection(cn, coreGroupNode);
          cn.getConnectedTo().add(coreGroupNode);
          // defaultGroupNode.getConnectedTo().add(cn);
          connections.add(connection);
       }
    }
 
    /**
     * Creates {@link GroupNode} from the sub {@link Group} of the {@link CoreGroup}.
     * 
     * @param groups list of {@link Group} from the {@link CoreGroup}.
     */
    private void createGroupNodes(EList<Group> groups) {
       for (Group group : groups) {
          GroupNode groupNode = new GroupNode(group);
          groupdNodes.put(group, groupNode);
          Connection connection = new Connection(groupNode, coreGroupNode);
          groupNode.getConnectedTo().add(coreGroupNode);
          // defaultGroupNode.getConnectedTo().add(groupNode);
          connections.add(connection);
          if (!group.getGroups().isEmpty()) {
             createGroupNodes(group.getGroups(), groupNode);
          }
       }
    }
 
    /**
     * Creates group nodes from the sub nodes of a group.
     * 
     * @param groups sub groups of a group
     * @param parentNode the parent group node
     */
    private void createGroupNodes(EList<Group> groups, GroupNode parentNode) {
       for (Group group : groups) {
          GroupNode groupNode = new GroupNode(group);
          groupdNodes.put(group, groupNode);
          Connection connection = new Connection(groupNode, parentNode);
          groupNode.getConnectedTo().add(parentNode);
          // parentNode.getConnectedTo().add(groupNode);
          connections.add(connection);
          if (!group.getGroups().isEmpty()) {
             createGroupNodes(group.getGroups(), groupNode);
          }
       }
    }
 
    /**
     * returns all nodes.
     * 
     * @return all nodes.
     */
    public Collection<Node> getNodes() {
       Collection<Node> nodes = new LinkedList<Node>();
       nodes.add(coreGroupNode);
       nodes.addAll(groupdNodes.values());
       nodes.addAll(viewPointNodes.values());
       nodes.addAll(featureNodes.values());
       return nodes;
    }
 
    /**
     * @return the connections
     */
    public List<Connection> getConnections() {
       return connections;
    }
 
 }
