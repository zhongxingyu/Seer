 package org.jboss.pressgang.ccms.rest.v1.entities.contentspec;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSTranslatedNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTCSNodeCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.join.RESTCSRelatedNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.base.RESTBaseCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums.RESTCSNodeTypeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.join.RESTCSRelatedNodeV1;
 
 public class RESTCSNodeV1 extends RESTBaseCSNodeV1<RESTCSNodeV1, RESTCSNodeCollectionV1, RESTCSNodeCollectionItemV1> {
     public static final String TRANSLATED_NODES_NAME = "translatedNodes_OTM";
 
     protected RESTCSRelatedNodeCollectionV1 relatedToNodes = null;
     protected RESTCSRelatedNodeCollectionV1 relatedFromNodes = null;
     protected RESTCSNodeCollectionV1 children = null;
     private RESTCSTranslatedNodeCollectionV1 translatedNodes_OTM = null;
     private RESTCSNodeCollectionV1 revisions = null;
 
     public RESTCSNodeV1() {
 
     }
 
     public RESTCSNodeV1(final RESTCSRelatedNodeV1 relatedNode) {
         if (relatedNode != null) {
             relatedNode.cloneInto(this, true);
         }
     }
 
     @Override
     public RESTCSNodeCollectionV1 getRevisions() {
         return revisions;
     }
 
     @Override
     public void setRevisions(RESTCSNodeCollectionV1 revisions) {
         this.revisions = revisions;
     }
 
     @Override
     public RESTCSNodeV1 clone(boolean deepCopy) {
         final RESTCSNodeV1 retValue = new RESTCSNodeV1();
 
         this.cloneInto(retValue, deepCopy);
 
         return retValue;
     }
 
     public void cloneInto(final RESTCSNodeV1 clone, final boolean deepCopy) {
         super.cloneInto(clone, deepCopy);
 
         if (deepCopy) {
             if (this.relatedFromNodes != null) {
                 clone.relatedFromNodes = new RESTCSRelatedNodeCollectionV1();
                 relatedFromNodes.cloneInto(clone.relatedFromNodes, deepCopy);
             }
 
             if (this.relatedToNodes != null) {
                 clone.relatedToNodes = new RESTCSRelatedNodeCollectionV1();
                 relatedToNodes.cloneInto(clone.relatedToNodes, deepCopy);
             }
 
             if (this.children != null) {
                 clone.children = new RESTCSNodeCollectionV1();
                 children.cloneInto(clone.children, deepCopy);
             }
 
             if (translatedNodes_OTM != null) {
                 clone.translatedNodes_OTM = new RESTCSTranslatedNodeCollectionV1();
                 this.translatedNodes_OTM.cloneInto(clone.translatedNodes_OTM, deepCopy);
             }
 
             if (this.revisions != null) {
                 clone.revisions = new RESTCSNodeCollectionV1();
                 this.revisions.cloneInto(clone.revisions, deepCopy);
             }
         } else {
             clone.relatedFromNodes = this.relatedFromNodes;
             clone.relatedToNodes = this.relatedToNodes;
             clone.children = this.children;
             clone.translatedNodes_OTM = this.translatedNodes_OTM;
             clone.revisions = this.revisions;
         }
     }
 
     public void explicitSetTitle(final String title) {
         this.title = title;
         this.setParameterToConfigured(TITLE_NAME);
     }
 
     public void explicitSetTargetId(final String targetId) {
         this.targetId = targetId;
         setParameterToConfigured(TARGET_ID_NAME);
     }
 
     public void explicitSetAdditionalText(final String additionalText) {
         this.additionalText = additionalText;
         this.setParameterToConfigured(ADDITIONAL_TEXT_NAME);
     }
 
     public void explicitSetContentSpec(final RESTContentSpecV1 contentSpec) {
         this.contentSpec = contentSpec;
         this.setParameterToConfigured(CONTENT_SPEC_NAME);
     }
 
     public void explicitSetParent(final RESTCSNodeV1 parent) {
         this.parent = parent;
         this.setParameterToConfigured(PARENT_NAME);
     }
 
     public void explicitSetNodeType(final RESTCSNodeTypeV1 nodeType) {
         this.nodeType = nodeType;
         this.setParameterToConfigured(NODE_TYPE_NAME);
     }
 
    public void explicitSetEnitityId(final Integer entityId) {
         this.entityId = entityId;
         this.setParameterToConfigured(ENTITY_ID_NAME);
     }
 
     public void explicitSetEntityRevision(final Integer entityRevision) {
         this.entityRevision = entityRevision;
         this.setParameterToConfigured(ENTITY_REVISION_NAME);
     }
 
     public void explicitSetCondition(final String condition) {
         this.condition = condition;
         this.setParameterToConfigured(CONDITION_NAME);
     }
 
     public void explicitSetNextNodeId(final Integer nextNodeId) {
         this.nextNodeId = nextNodeId;
         this.setParameterToConfigured(NEXT_NODE_NAME);
     }
 
     public void explicitSetPreviousNodeId(final Integer previousNodeId) {
         this.previousNodeId = previousNodeId;
         this.setParameterToConfigured(PREVIOUS_NODE_NAME);
     }
 
     public void explicitSetProperties(final RESTAssignedPropertyTagCollectionV1 properties) {
         this.properties = properties;
         this.setParameterToConfigured(PROPERTIES_NAME);
     }
 
     public RESTCSTranslatedNodeCollectionV1 getTranslatedNodes_OTM() {
         return translatedNodes_OTM;
     }
 
     public void setTranslatedNodes_OTM(final RESTCSTranslatedNodeCollectionV1 translatedNodes) {
         this.translatedNodes_OTM = translatedNodes;
     }
 
     public void explicitSetTranslatedNodes_OTM(final RESTCSTranslatedNodeCollectionV1 translatedNodes) {
         this.translatedNodes_OTM = translatedNodes;
         this.setParameterToConfigured(TRANSLATED_NODES_NAME);
     }
 
     public RESTCSRelatedNodeCollectionV1 getRelatedToNodes() {
         return relatedToNodes;
     }
 
     public void setRelatedToNodes(final RESTCSRelatedNodeCollectionV1 relatedToNodes) {
         this.relatedToNodes = relatedToNodes;
     }
 
     public void explicitSetRelatedToNodes(final RESTCSRelatedNodeCollectionV1 relatedToNodes) {
         this.relatedToNodes = relatedToNodes;
         this.setParameterToConfigured(RELATED_TO_NAME);
     }
 
     public RESTCSRelatedNodeCollectionV1 getRelatedFromNodes() {
         return relatedFromNodes;
     }
 
     public void setRelatedFromNodes(final RESTCSRelatedNodeCollectionV1 relatedFromNodes) {
         this.relatedFromNodes = relatedFromNodes;
     }
 
     public void explicitSetRelatedFromNodes(final RESTCSRelatedNodeCollectionV1 relatedFromNodes) {
         this.relatedFromNodes = relatedFromNodes;
         this.setParameterToConfigured(RELATED_FROM_NAME);
     }
 
     public RESTCSNodeCollectionV1 getChildren_OTM() {
         return children;
     }
 
     public void setChildren_OTM(final RESTCSNodeCollectionV1 children) {
         this.children = children;
     }
 
     public void explicitSetChildren_OTM(final RESTCSNodeCollectionV1 children) {
         this.children = children;
         this.setParameterToConfigured(CHILDREN_NAME);
     }
 }
