 package org.jboss.pressgang.ccms.model.contentspec;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import javax.persistence.Cacheable;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.PersistenceException;
 import javax.persistence.PrePersist;
 import javax.persistence.PreRemove;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.AuditReader;
 import org.hibernate.envers.AuditReaderFactory;
 import org.hibernate.envers.Audited;
 import org.hibernate.envers.query.AuditEntity;
 import org.hibernate.envers.query.AuditQuery;
 import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.pressgang.ccms.model.PropertyTag;
 import org.jboss.pressgang.ccms.model.Topic;
 import org.jboss.pressgang.ccms.model.base.ParentToPropertyTag;
 import org.jboss.pressgang.ccms.model.constants.Constants;
 import org.jboss.pressgang.ccms.model.exceptions.CustomConstraintViolationException;
 import org.jboss.pressgang.ccms.model.interfaces.HasCSNodes;
 import org.jboss.pressgang.ccms.model.interfaces.HasTwoWayRelationships;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "ContentSpecNode")
 public class CSNode extends ParentToPropertyTag<CSNode, CSNodeToPropertyTag> implements HasCSNodes, HasTwoWayRelationships<CSNodeToCSNode>,
         Serializable {
 
     private static final long serialVersionUID = -5074781793940947664L;
     public static final String SELECT_ALL_QUERY = "select csNode FROM CSNode AS csNode";
 
     private Integer csNodeId = null;
     private String csNodeTitle = null;
     private String csNodeTargetId = null;
     private String additionalText = null;
     private Integer csNodeType = null;
     private ContentSpec contentSpec = null;
     private CSNode parent = null;
     private CSNode next = null;
     private CSNode previous = null;
     private Integer entityId = null;
     private Integer entityRevision = null;
     private String condition = null;
     private Set<CSNode> children = new HashSet<CSNode>(0);
     private Set<CSNodeToCSNode> relatedFromNodes = new HashSet<CSNodeToCSNode>(0);
     private Set<CSNodeToCSNode> relatedToNodes = new HashSet<CSNodeToCSNode>(0);
     private Set<CSNodeToPropertyTag> csNodeToPropertyTags = new HashSet<CSNodeToPropertyTag>(0);
 
     private Topic topic;
 
     public CSNode() {
     }
 
     @Override
     @Transient
     public Integer getId() {
         return csNodeId;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "ContentSpecNodeID", unique = true, nullable = false)
     public Integer getCSNodeId() {
         return csNodeId;
     }
 
     public void setCSNodeId(Integer csNodeId) {
         this.csNodeId = csNodeId;
     }
 
     @Column(name = "NodeTitle", length = 255)
     @NotNull(message = "{contentspec.node.title.notBlank}")
     @NotBlank(message = "{contentspec.node.title.notBlank}")
     public String getCSNodeTitle() {
         return csNodeTitle;
     }
 
     public void setCSNodeTitle(String csNodeTitle) {
         this.csNodeTitle = csNodeTitle;
     }
 
     @Column(name = "NodeTargetID", length = 255, nullable = true)
     public String getCSNodeTargetId() {
         return csNodeTargetId;
     }
 
     public void setCSNodeTargetId(String csNodeTargetId) {
         this.csNodeTargetId = csNodeTargetId;
     }
 
     @Column(name = "AdditionalText", columnDefinition = "TEXT", nullable = true)
     @Size(max = 65535)
     public String getAdditionalText() {
         return additionalText;
     }
 
     public void setAdditionalText(String csNodeAlternativeTitle) {
         this.additionalText = csNodeAlternativeTitle;
     }
 
     @Column(name = "NodeType", nullable = false, columnDefinition = "TINYINT")
     public Integer getCSNodeType() {
         return csNodeType;
     }
 
     public void setCSNodeType(Integer csNodeType) {
         this.csNodeType = csNodeType;
     }
 
     @JoinColumn(name = "ContentSpecID")
     @ManyToOne(fetch = FetchType.LAZY)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     public ContentSpec getContentSpec() {
         return contentSpec;
     }
 
     public void setContentSpec(ContentSpec contentSpec) {
         this.contentSpec = contentSpec;
 
         for (final CSNode child : children) {
             child.setContentSpec(contentSpec);
         }
     }
 
     @JoinColumn(name = "ParentID")
     @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     public CSNode getParent() {
         return parent;
     }
 
     public void setParent(CSNode parent) {
         this.parent = parent;
     }
 
     @JoinColumn(name = "NextNodeID")
     @OneToOne
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     public CSNode getNext() {
         return next;
     }
 
     public void setNext(CSNode next) {
         this.next = next;
     }
 
     /**
      * Sets the Next Node and cleans up any old references.
      *
      * @param next The next node.
      */
     @Transient
     public void setNextAndClean(CSNode next) {
         setNextInternal(next);
         if (next != null) {
             next.setPreviousInternal(this);
         }
     }
 
     @Transient
     protected void setNextInternal(CSNode next) {
         if (this.next != next) {
             if (this.next != null) {
                 this.next.previous = null;
             }
             this.next = next;
         }
     }
 
     @OneToOne(fetch = FetchType.LAZY, mappedBy = "next")
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     public CSNode getPrevious() {
         return previous;
     }
 
     public void setPrevious(CSNode previous) {
         this.previous = previous;
     }
 
     /**
      * Sets the Previous Node and cleans up any old references.
      *
      * @param previous The previous node.
      */
     @Transient
     public void setPreviousAndClean(CSNode previous) {
         setPreviousInternal(previous);
         if (previous != null) {
             previous.setNextInternal(this);
         }
     }
 
     @Transient
     protected void setPreviousInternal(CSNode previous) {
         if (this.previous != previous) {
             if (this.previous != null) {
                 this.previous.next = null;
             }
             this.previous = previous;
         }
     }
 
     @Column(name = "EntityID")
     public Integer getEntityId() {
         return entityId;
     }
 
     public void setEntityId(Integer entityId) {
         this.entityId = entityId;
     }
 
     @Column(name = "EntityRevision")
     public Integer getEntityRevision() {
         return entityRevision;
     }
 
     public void setEntityRevision(Integer entityRevision) {
         this.entityRevision = entityRevision;
     }
 
     @Column(name = "NodeCondition")
     public String getCondition() {
         return condition;
     }
 
     public void setCondition(String condition) {
         this.condition = condition;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     @Override
     public Set<CSNode> getChildren() {
         return children;
     }
 
     public void setChildren(Set<CSNode> children) {
         this.children = children;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "mainNode", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<CSNodeToCSNode> getRelatedToNodes() {
         return relatedToNodes;
     }
 
     public void setRelatedFromNodes(Set<CSNodeToCSNode> relatedFromNodes) {
         this.relatedFromNodes = relatedFromNodes;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "relatedNode", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<CSNodeToCSNode> getRelatedFromNodes() {
         return relatedFromNodes;
     }
 
     public void setRelatedToNodes(Set<CSNodeToCSNode> relatedToNodes) {
         this.relatedToNodes = relatedToNodes;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "CSNode", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<CSNodeToPropertyTag> getCSNodeToPropertyTags() {
         return csNodeToPropertyTags;
     }
 
     public void setCSNodeToPropertyTags(Set<CSNodeToPropertyTag> csNodeToPropertyTags) {
         this.csNodeToPropertyTags = csNodeToPropertyTags;
     }
 
     @Transient
     @Override
     public List<CSNode> getChildrenList() {
         return new ArrayList<CSNode>(getChildren());
     }
 
     @Transient
     public List<CSNodeToCSNode> getRelatedFromNodesList() {
         return new ArrayList<CSNodeToCSNode>(relatedFromNodes);
     }
 
     @Transient
     public List<CSNodeToCSNode> getRelatedToNodesList() {
         return new ArrayList<CSNodeToCSNode>(relatedToNodes);
     }
 
     @Transient
     @Override
     public void removeChild(final CSNode child) {
         children.remove(child);
         child.setParent(null);
         if (contentSpec != null) {
             contentSpec.removeChild(child);
         }
     }
 
     @Transient
     @Override
     public void addChild(final CSNode child) {
         if (child.getParent() != null && !child.getParent().equals(this)) {
             child.getParent().removeChild(child);
         }
         if (child.getContentSpec() != null) {
             child.getContentSpec().removeChild(child);
         }
         children.add(child);
         child.setParent(this);
         if (contentSpec != null) {
             contentSpec.addChild(child);
         }
     }
 
     @Transient
     public void removeRelationshipTo(final CSNode node, Integer relationshipTypeId) {
         final List<CSNodeToCSNode> removeNodes = new ArrayList<CSNodeToCSNode>();
 
         for (final CSNodeToCSNode nodeToNode : relatedToNodes) {
             if (nodeToNode.getRelatedNode().equals(node) && nodeToNode.getRelationshipType().equals(relationshipTypeId)) {
                 removeNodes.add(nodeToNode);
             }
         }
 
         for (final CSNodeToCSNode removeNode : removeNodes) {
             removeRelationshipTo(removeNode);
         }
     }
 
     @Transient
     public void addRelationshipTo(final CSNode relatedNode, Integer relationshipTypeId) {
         final CSNodeToCSNode nodeToNode = new CSNodeToCSNode();
         nodeToNode.setMainNode(this);
         nodeToNode.setRelatedNode(relatedNode);
         nodeToNode.setRelationshipType(relationshipTypeId);
 
         addRelationshipTo(nodeToNode);
     }
 
     @Transient
     @Override
     public void addRelationshipTo(final CSNodeToCSNode relatedNode) {
         relatedNode.setMainNode(this);
         getRelatedToNodes().add(relatedNode);
         relatedNode.getRelatedNode().getRelatedFromNodes().add(relatedNode);
     }
 
     @Transient
     @Override
     public void removeRelationshipTo(final CSNodeToCSNode relatedNode) {
         getRelatedToNodes().remove(relatedNode);
         relatedNode.getRelatedNode().getRelatedFromNodes().remove(relatedNode);
     }
 
     @Transient
     public void removeRelationshipFrom(final CSNode node, Integer relationshipTypeId) {
         final List<CSNodeToCSNode> removeNodes = new ArrayList<CSNodeToCSNode>();
 
         for (final CSNodeToCSNode nodeFromNode : relatedFromNodes) {
             if (nodeFromNode.getRelatedNode().equals(node) && nodeFromNode.getRelationshipType().equals(relationshipTypeId)) {
                 removeNodes.add(nodeFromNode);
             }
         }
 
         for (final CSNodeToCSNode removeNode : removeNodes) {
             removeRelationshipFrom(removeNode);
         }
     }
 
     @Transient
     public void addRelationshipFrom(final CSNode relatedNode, Integer relationshipTypeId) {
         final CSNodeToCSNode nodeFromNode = new CSNodeToCSNode();
         nodeFromNode.setMainNode(relatedNode);
         nodeFromNode.setRelatedNode(this);
         nodeFromNode.setRelationshipType(relationshipTypeId);
 
         addRelationshipFrom(nodeFromNode);
     }
 
     @Transient
     @Override
     public void addRelationshipFrom(final CSNodeToCSNode relatedNode) {
         relatedNode.setRelatedNode(this);
         getRelatedFromNodes().add(relatedNode);
         relatedNode.getRelatedNode().getRelatedToNodes().add(relatedNode);
     }
 
     @Transient
     @Override
     public void removeRelationshipFrom(final CSNodeToCSNode relatedNode) {
         getRelatedFromNodes().remove(relatedNode);
         relatedNode.getMainNode().getRelatedToNodes().remove(relatedNode);
     }
 
     @Transient
     @Override
     public Set<CSNodeToPropertyTag> getPropertyTags() {
         return csNodeToPropertyTags;
     }
 
     @Override
     public void addPropertyTag(final PropertyTag propertyTag, final String value) {
         final CSNodeToPropertyTag mapping = new CSNodeToPropertyTag();
         mapping.setCSNode(this);
         mapping.setPropertyTag(propertyTag);
         mapping.setValue(value);
 
         csNodeToPropertyTags.add(mapping);
         propertyTag.getCSNodeToPropertyTags().add(mapping);
     }
 
     @Override
     public void addPropertyTag(final CSNodeToPropertyTag mapping) {
         mapping.setCSNode(this);
         csNodeToPropertyTags.add(mapping);
         mapping.getPropertyTag().getCSNodeToPropertyTags().add(mapping);
     }
 
     @Override
     public void removePropertyTag(final PropertyTag propertyTag, final String value) {
         final List<CSNodeToPropertyTag> removeList = new ArrayList<CSNodeToPropertyTag>();
 
         for (final CSNodeToPropertyTag mapping : csNodeToPropertyTags) {
             final PropertyTag myPropertyTag = mapping.getPropertyTag();
             if (myPropertyTag.equals(propertyTag) && mapping.getValue().equals(value)) {
                 removeList.add(mapping);
             }
         }
 
         for (final CSNodeToPropertyTag mapping : removeList) {
             removePropertyTag(mapping);
         }
     }
 
     @Override
     public void removePropertyTag(final CSNodeToPropertyTag mapping) {
         csNodeToPropertyTags.remove(mapping);
         mapping.getPropertyTag().getCSNodeToPropertyTags().remove(mapping);
     }
 
     @Transient
     public Topic getTopic(final EntityManager entityManager) {
         if (!(getCSNodeType().equals(CommonConstants.CS_NODE_TOPIC) || getCSNodeType().equals(CommonConstants.CS_NODE_INNER_TOPIC)))
             return null;
         if (entityId == null)
             return null;
 
         if (topic == null) {
             if (entityRevision == null) {
                 // Find the latest topic
                 topic = entityManager.find(Topic.class, entityId);
             } else {
                 // Find the envers topic
                 final AuditReader reader = AuditReaderFactory.get(entityManager);
                 final AuditQuery query = reader.createQuery().forEntitiesAtRevision(Topic.class, entityRevision).add(
                         AuditEntity.id().eq(entityId));
                 topic = (Topic) query.getSingleResult();
             }
         }
         return topic;
     }
 
     @SuppressWarnings("unchecked")
     @Transient
     public List<TranslatedCSNode> getTranslatedNodes(final EntityManager entityManager, final Number revision) {
         /*
          * We have to do a query here as a @OneToMany won't work with hibernate envers since the TranslatedCSNode entity is
          * audited and we need the latest results. This is because the translated node will never exist for its matching
          * audited node.
          */
         final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
         final CriteriaQuery<TranslatedCSNode> query = criteriaBuilder.createQuery(TranslatedCSNode.class);
         final Root<TranslatedCSNode> root = query.from(TranslatedCSNode.class);
         query.select(root);
 
         final Predicate csNodeIdMatches = criteriaBuilder.equal(root.get("CSNodeId"), csNodeId);
         final Predicate csNodeRevisionMatches = criteriaBuilder.lessThanOrEqualTo(root.get("CSNodeRevision").as(Integer.class),
                 (Integer) revision);
 
         if (revision == null) {
             query.where(csNodeIdMatches);
         } else {
             query.where(criteriaBuilder.and(csNodeIdMatches, csNodeRevisionMatches));
         }
 
         return entityManager.createQuery(query).getResultList();
     }
 
     @Transient
     public String getInheritedCondition() {
         if (getCondition() == null || getCondition().trim().isEmpty()) {
             if (getParent() != null) {
                 return getParent().getInheritedCondition();
             } else if (getContentSpec() != null) {
                 return getContentSpec().getCondition();
             } else {
                 return null;
             }
         } else {
             return getCondition();
         }
     }
 
     @PrePersist
     @PreUpdate
     protected void preSave() {
         validateNode();
 
         // Set the content specs last modified date if one of it's nodes change
         if (contentSpec != null) {
             contentSpec.setLastModified();
         }
     }
 
     @Transient
     protected void validateNode() {
         if (getCSNodeType() == CommonConstants.CS_NODE_META_DATA && getParent() != null) {
             throw new CustomConstraintViolationException("Meta Data nodes are only allowed at the root level.");
         }
 
         if (getCSNodeType() == CommonConstants.CS_NODE_META_DATA && !getChildren().isEmpty()) {
            throw new PersistenceException("Meta Data nodes cannot have children nodes.");
         } else if ((getCSNodeType().equals(CommonConstants.CS_NODE_TOPIC) || getCSNodeType().equals(CommonConstants.CS_NODE_INNER_TOPIC))
                 && !getChildren().isEmpty()) {
             throw new CustomConstraintViolationException("Topic nodes cannot have children nodes.");
         }
     }
 
     @PreRemove
     protected void preRemove() {
         for (final CSNodeToCSNode mapping : new HashSet<CSNodeToCSNode>(relatedFromNodes)) {
             removeRelationshipFrom(mapping);
         }
 
         for (final CSNodeToCSNode mapping : new HashSet<CSNodeToCSNode>(relatedToNodes)) {
             removeRelationshipTo(mapping);
         }
     }
 }
