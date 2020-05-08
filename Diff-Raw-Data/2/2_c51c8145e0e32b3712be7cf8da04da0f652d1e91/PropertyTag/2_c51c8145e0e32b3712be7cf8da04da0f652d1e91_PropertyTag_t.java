 package org.jboss.pressgang.ccms.model;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Cacheable;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.PreRemove;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import javax.validation.constraints.Size;
 import javax.validation.constraints.NotNull;
 
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 import org.jboss.pressgang.ccms.model.contentspec.CSNodeToPropertyTag;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpecToPropertyTag;
 import org.jboss.pressgang.ccms.model.constants.Constants;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "PropertyTag", uniqueConstraints = @UniqueConstraint(columnNames = { "PropertyTagName" }))
 public class PropertyTag extends AuditedEntity<PropertyTag> implements java.io.Serializable {
     private static final long serialVersionUID = -9064491060913710869L;
     public static final String SELECT_ALL_QUERY = "select propertyTag from PropertyTag propertyTag";
 
     private Integer propertyTagId;
     private String propertyTagName;
     private String propertyTagDescription;
     private String propertyTagRegex;
     private boolean propertyTagCanBeNull;
     private Set<TagToPropertyTag> tagToPropertyTags = new HashSet<TagToPropertyTag>(0);
     private Set<TopicToPropertyTag> topicToPropertyTags = new HashSet<TopicToPropertyTag>(0);
     private Set<ContentSpecToPropertyTag> contentSpecToPropertyTags = new HashSet<ContentSpecToPropertyTag>(0);
     private Set<CSNodeToPropertyTag> csNodeToPropertyTags = new HashSet<CSNodeToPropertyTag>(0);
     private Set<PropertyTagToPropertyTagCategory> propertyTagToPropertyTagCategories = new HashSet<PropertyTagToPropertyTagCategory>(
             0);
     private Boolean propertyTagIsUnique;
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "PropertyTagID", unique = true, nullable = false)
     public Integer getPropertyTagId() {
         return propertyTagId;
     }
 
     public void setPropertyTagId(final Integer propertyTagId) {
         this.propertyTagId = propertyTagId;
     }
 
     @Column(name = "PropertyTagName", nullable = false, length = 255)
     @NotNull
     @Size(max = 255)
     public String getPropertyTagName() {
         return propertyTagName;
     }
 
     public void setPropertyTagName(final String propertyTagName) {
         this.propertyTagName = propertyTagName;
     }
 
     @Column(name = "PropertyTagDescription", columnDefinition = "TEXT")
     @Size(max = 65535)
     public String getPropertyTagDescription() {
         return propertyTagDescription;
     }
 
     public void setPropertyTagDescription(final String propertyTagDescription) {
         this.propertyTagDescription = propertyTagDescription;
     }
 
     @Column(name = "PropertyTagRegex", columnDefinition = "TEXT")
     @NotNull
     @Size(max = 65535)
     public String getPropertyTagRegex() {
         return propertyTagRegex;
     }
 
     public void setPropertyTagRegex(final String propertyTagRegex) {
         this.propertyTagRegex = propertyTagRegex;
     }
 
     @Column(name = "PropertyTagCanBeNull", nullable = false, columnDefinition = "BIT", length = 1)
     @NotNull
     public boolean isPropertyTagCanBeNull() {
         return propertyTagCanBeNull;
     }
 
     public void setPropertyTagCanBeNull(final boolean propertyTagCanBeNull) {
         this.propertyTagCanBeNull = propertyTagCanBeNull;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "propertyTag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TagToPropertyTag> getTagToPropertyTags() {
         return tagToPropertyTags;
     }
 
     public void setTagToPropertyTags(final Set<TagToPropertyTag> tagToPropertyTags) {
         this.tagToPropertyTags = tagToPropertyTags;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "propertyTag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TopicToPropertyTag> getTopicToPropertyTags() {
         return topicToPropertyTags;
     }
 
     public void setTopicToPropertyTags(final Set<TopicToPropertyTag> topicToPropertyTags) {
         this.topicToPropertyTags = topicToPropertyTags;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "propertyTag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<ContentSpecToPropertyTag> getContentSpecToPropertyTags() {
         return contentSpecToPropertyTags;
     }
 
     public void setContentSpecToPropertyTags(final Set<ContentSpecToPropertyTag> contentSpecToPropertyTags) {
         this.contentSpecToPropertyTags = contentSpecToPropertyTags;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "propertyTag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<CSNodeToPropertyTag> getCSNodeToPropertyTags() {
         return csNodeToPropertyTags;
     }
 
     public void setCSNodeToPropertyTags(final Set<CSNodeToPropertyTag> csNodeToPropertyTags) {
         this.csNodeToPropertyTags = csNodeToPropertyTags;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "propertyTag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<PropertyTagToPropertyTagCategory> getPropertyTagToPropertyTagCategories() {
         return propertyTagToPropertyTagCategories;
     }
 
     public void setPropertyTagToPropertyTagCategories(
             final Set<PropertyTagToPropertyTagCategory> propertyTagToPropertyTagCategories) {
         this.propertyTagToPropertyTagCategories = propertyTagToPropertyTagCategories;
     }
 
     @SuppressWarnings("unused")
     @PreRemove
     private void preRemove() {
         this.propertyTagToPropertyTagCategories.clear();
         this.tagToPropertyTags.clear();
     }
 
     @Transient
     public boolean isInCategory(final PropertyTagCategory propertyTagCategory) {
         for (final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory : this.propertyTagToPropertyTagCategories)
             if (propertyTagToPropertyTagCategory.getPropertyTagCategory().equals(propertyTagCategory))
                 return true;
 
         return false;
     }
 
     @Transient
     public PropertyTagToPropertyTagCategory getCategory(final Integer categoryId) {
         for (final PropertyTagToPropertyTagCategory category : this.propertyTagToPropertyTagCategories)
             if (categoryId.equals(category.getPropertyTagCategory().getPropertyTagCategoryId()))
                 return category;
 
         return null;
     }
 
     @Override
     @Transient
     public Integer getId() {
         return this.propertyTagId;
     }
 
    @Column(name = "PropertyTagIsUnique", nullable = false, columnDefinition = "BIT", length = 1)
     public Boolean getPropertyTagIsUnique() {
         return propertyTagIsUnique;
     }
 
     public void setPropertyTagIsUnique(final Boolean isUnique) {
         this.propertyTagIsUnique = isUnique;
     }
 
     @Transient
     public void removeTag(final TagToPropertyTag tagToPropertyTag) {
         tagToPropertyTag.getTag().getTagToPropertyTags().remove(tagToPropertyTag);
         this.tagToPropertyTags.remove(tagToPropertyTag);
     }
 
     @Transient
     public void addTag(final TagToPropertyTag tagToPropertyTag) {
         this.tagToPropertyTags.add(tagToPropertyTag);
         tagToPropertyTag.getTag().getTagToPropertyTags().add(tagToPropertyTag);
     }
 
     @Transient
     public void removeTopic(final TopicToPropertyTag topicToPropertyTag) {
         topicToPropertyTag.getTopic().getTopicToPropertyTags().remove(topicToPropertyTag);
         this.topicToPropertyTags.remove(topicToPropertyTag);
     }
 
     @Transient
     public void addTopic(final TopicToPropertyTag topicToPropertyTag) {
         this.topicToPropertyTags.add(topicToPropertyTag);
         topicToPropertyTag.getTopic().getTopicToPropertyTags().add(topicToPropertyTag);
     }
 
     @Transient
     public List<PropertyTagToPropertyTagCategory> getPropertyTagToPropertyTagCategoriesList() {
         final List<PropertyTagToPropertyTagCategory> propertyTagToPropertyTagCategories = new ArrayList<PropertyTagToPropertyTagCategory>();
 
         propertyTagToPropertyTagCategories.addAll(this.propertyTagToPropertyTagCategories);
 
         return propertyTagToPropertyTagCategories;
     }
 
     public boolean removePropertyTagCategory(final PropertyTagCategory propertyTagCategory) {
         for (final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory : propertyTagToPropertyTagCategories) {
             if (propertyTagToPropertyTagCategory.getPropertyTagCategory().equals(propertyTagCategory)) {
                 propertyTagToPropertyTagCategories.remove(propertyTagToPropertyTagCategory);
                 propertyTagCategory.getPropertyTagToPropertyTagCategories().remove(propertyTagToPropertyTagCategory);
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean addPropertyTagCategory(final PropertyTagCategory propertyTagCategory) {
         boolean found = false;
         for (final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory : propertyTagToPropertyTagCategories) {
             if (propertyTagToPropertyTagCategory.getPropertyTag().equals(propertyTagCategory)) {
                 found = true;
                 break;
             }
         }
 
         if (!found) {
             final PropertyTagToPropertyTagCategory propertyTagToPropertyTagCategory = new PropertyTagToPropertyTagCategory();
             propertyTagToPropertyTagCategory.setPropertyTag(this);
             propertyTagToPropertyTagCategory.setPropertyTagCategory(propertyTagCategory);
             propertyTagToPropertyTagCategories.add(propertyTagToPropertyTagCategory);
             propertyTagCategory.getPropertyTagToPropertyTagCategories().add(propertyTagToPropertyTagCategory);
         }
 
         return !found;
     }
 
     @Transient
     public void removeCSNode(final CSNodeToPropertyTag csNodeToPropertyTag) {
         csNodeToPropertyTag.getCSNode().getCSNodeToPropertyTags().remove(csNodeToPropertyTag);
         this.csNodeToPropertyTags.remove(csNodeToPropertyTag);
     }
 
     @Transient
     public void addCSNode(final CSNodeToPropertyTag csNodeToPropertyTag) {
         this.csNodeToPropertyTags.add(csNodeToPropertyTag);
         csNodeToPropertyTag.getCSNode().getCSNodeToPropertyTags().add(csNodeToPropertyTag);
     }
 }
