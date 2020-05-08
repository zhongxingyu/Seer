 package org.jboss.pressgang.ccms.model;
 
 import static ch.lambdaj.Lambda.filter;
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 import static javax.persistence.GenerationType.IDENTITY;
 import static org.hamcrest.Matchers.equalTo;
 
 import javax.persistence.Cacheable;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.PrePersist;
 import javax.persistence.PreRemove;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.pressgang.ccms.model.base.ParentToPropertyTag;
 import org.jboss.pressgang.ccms.model.constants.Constants;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpecToTag;
 import org.jboss.pressgang.ccms.model.sort.CategoryIDComparator;
 import org.jboss.pressgang.ccms.model.sort.ProjectIDComparator;
 import org.jboss.pressgang.ccms.model.sort.TagIDComparator;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "Tag", uniqueConstraints = @UniqueConstraint(columnNames = {"TagName"}))
 public class Tag extends ParentToPropertyTag<Tag, TagToPropertyTag> implements Serializable {
     public static final String SELECT_ALL_QUERY = "select tag from Tag tag";
     private static final long serialVersionUID = 2841080567638275194L;
 
     private Integer tagId;
     private String tagName;
     private String tagDescription;
     private Set<TopicToTag> topicToTags = new HashSet<TopicToTag>(0);
     private Set<Tag> excludedTags = new HashSet<Tag>(0);
     private Set<TagToCategory> tagToCategories = new HashSet<TagToCategory>(0);
     private Set<TagToTag> childrenTagToTags = new HashSet<TagToTag>(0);
     private Set<TagToTag> parentTagToTags = new HashSet<TagToTag>(0);
     private Set<TagToProject> tagToProjects = new HashSet<TagToProject>(0);
     private Set<TagToPropertyTag> tagToPropertyTags = new HashSet<TagToPropertyTag>(0);
     private Set<ContentSpecToTag> contentSpecToTags = new HashSet<ContentSpecToTag>(0);
 
     public Tag() {
     }
 
     public Tag(final String tagName) {
         this.tagName = tagName;
     }
 
     public Tag(final String tagName, final String tagDescription, final Set<TopicToTag> topicToTags,
             final Set<TagToCategory> tagToCategories) {
         this.tagName = tagName;
         this.tagDescription = tagDescription;
         this.topicToTags = topicToTags;
         this.tagToCategories = tagToCategories;
     }
 
     @Override
     @Transient
     public Integer getId() {
         return tagId;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TagToProject> getTagToProjects() {
         return tagToProjects;
     }
 
     public void setTagToProjects(final Set<TagToProject> tagToProjects) {
         this.tagToProjects = tagToProjects;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "secondaryTag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TagToTag> getParentTagToTags() {
         return parentTagToTags;
     }
 
     public void setParentTagToTags(final Set<TagToTag> parentTagToTags) {
         this.parentTagToTags = parentTagToTags;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "primaryTag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TagToTag> getChildrenTagToTags() {
         return childrenTagToTags;
     }
 
     public void setChildrenTagToTags(final Set<TagToTag> childrenTagToTags) {
         this.childrenTagToTags = childrenTagToTags;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TagToCategory> getTagToCategories() {
         return tagToCategories;
     }
 
     public void setTagToCategories(final Set<TagToCategory> tagToCategories) {
         this.tagToCategories = tagToCategories;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "TagID", unique = true, nullable = false)
     public Integer getTagId() {
         return tagId;
     }
 
     public void setTagId(Integer tagId) {
         this.tagId = tagId;
     }
 
     @Column(name = "TagName", nullable = false, length = 255)
     @NotNull(message = "{tag.name.notBlank}")
     @NotBlank(message = "{tag.name.notBlank}")
     @Size(max = 255)
     public String getTagName() {
         return tagName;
     }
 
     public void setTagName(String tagName) {
         this.tagName = tagName;
     }
 
     @Column(name = "TagDescription", columnDefinition = "TEXT")
     @Size(max = 65535)
     public String getTagDescription() {
         return tagDescription;
     }
 
     public void setTagDescription(String tagDescription) {
         this.tagDescription = tagDescription;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TopicToTag> getTopicToTags() {
         return topicToTags;
     }
 
     public void setTopicToTags(final Set<TopicToTag> topicToTags) {
         this.topicToTags = topicToTags;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<ContentSpecToTag> getContentSpecToTags() {
         return contentSpecToTags;
     }
 
     public void setContentSpecToTags(final Set<ContentSpecToTag> contentSpecToTags) {
         this.contentSpecToTags = contentSpecToTags;
     }
 
     @Transient
     public String getCategoriesList() {
         String categoriesList = "";
         for (final TagToCategory category : getTagToCategories()) {
             if (categoriesList.length() != 0) categoriesList += " ";
             categoriesList += category.getCategory().getCategoryName();
         }
         return categoriesList;
     }
 
     @Transient
     public List<Category> getCategories() {
         final List<Category> retValue = new ArrayList<Category>();
         for (final TagToCategory category : getTagToCategories())
             retValue.add(category.getCategory());
 
         Collections.sort(retValue, new CategoryIDComparator());
 
         return retValue;
     }
 
     @Transient
     public List<Project> getProjects() {
         final List<Project> retValue = new ArrayList<Project>();
         for (final TagToProject mapping : tagToProjects)
             retValue.add(mapping.getProject());
 
         Collections.sort(retValue, new ProjectIDComparator());
 
         return retValue;
     }
 
     @Transient
     public String getProjectsList() {
         String retValue = "";
         for (final TagToProject mapping : tagToProjects) {
             if (retValue.length() != 0) retValue += " ";
             retValue += mapping.getProject().getProjectName();
         }
         return retValue;
     }
 
     @Transient
     public List<Tag> getParentTags() {
         final List<Tag> retValue = new ArrayList<Tag>();
         for (final TagToTag mapping : getParentTagToTags())
             retValue.add(mapping.getPrimaryTag());
 
         Collections.sort(retValue, new TagIDComparator());
 
         return retValue;
     }
 
     @Transient
     public List<Tag> getChildTags() {
         final List<Tag> retValue = new ArrayList<Tag>();
         for (final TagToTag mapping : getParentTagToTags())
             retValue.add(mapping.getSecondaryTag());
 
         Collections.sort(retValue, new TagIDComparator());
 
         return retValue;
     }
 
     @Transient
     public String getChildrenList() {
         String retValue = "";
         for (final TagToTag tag : getChildrenTagToTags()) {
             if (retValue.length() != 0) retValue += ", ";
             retValue += tag.getSecondaryTag().getTagName();
         }
         return retValue;
     }
 
     @Transient
     public String getParentList() {
         String retValue = "";
         for (final TagToTag tag : getParentTagToTags()) {
             if (retValue.length() != 0) retValue += ", ";
             retValue += tag.getPrimaryTag().getTagName();
         }
         return retValue;
     }
 
     @Transient
     public ArrayList<Integer> getCategoriesIDList() {
         ArrayList<Integer> categoriesList = new ArrayList<Integer>();
         for (final TagToCategory category : getTagToCategories())
             categoriesList.add(category.getCategory().getCategoryId());
         return categoriesList;
     }
 
     @Transient
     public ArrayList<Integer> getExclusionTagIDs() {
         ArrayList<Integer> tagList = new ArrayList<Integer>();
         for (final Tag tag : getExcludedTags())
             tagList.add(tag.getTagId());
         return tagList;
     }
 
     @Transient
     public boolean isInCategory(final Integer categoryId) {
         for (final TagToCategory category : getTagToCategories())
             if (categoryId.equals(category.getCategory().getCategoryId())) return true;
 
         return false;
     }
 
     @Transient
     public boolean isInCategory(final Category category) {
         for (final TagToCategory myCategory : getTagToCategories())
             if (myCategory.getCategory().equals(category)) return true;
 
         return false;
     }
 
     @Transient
     public TagToCategory getCategory(final Integer categoryId) {
         for (final TagToCategory category : getTagToCategories())
             if (categoryId.equals(category.getCategory().getCategoryId())) return category;
 
         return null;
     }
 
     @ManyToMany(fetch = FetchType.LAZY)
     @JoinTable(name = "TagExclusion", joinColumns = {@JoinColumn(name = "Tag1ID", nullable = false, updatable = false)},
             inverseJoinColumns = {@JoinColumn(name = "Tag2ID", nullable = false, updatable = false)})
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<Tag> getExcludedTags() {
         return excludedTags;
     }
 
     public void setExcludedTags(Set<Tag> excludedTags) {
         this.excludedTags = excludedTags;
     }
 
     /*
      * @Override public int hashCode() { int result; result = (tagId != null ? tagId.hashCode() : 0); return result; }
      */
 
     @PrePersist
     protected void onCreate() {
         validate();
     }
 
     @PreUpdate
     protected void onUpdate() {
         validate();
     }
 
     protected void validate() {
         // a tag can be a parent, or be a child, but not both
         if (getChildrenTagToTags().size() != 0) getParentTagToTags().clear();
     }
 
     public boolean removeTagRelationship(final Tag childTag) {
         final List<TagToTag> children = filter(having(on(TagToTag.class).getSecondaryTag(), equalTo(childTag)), getChildrenTagToTags());
         for (final TagToTag child : children) {
             getChildrenTagToTags().remove(child);
             childTag.getParentTagToTags().remove(child);
         }
 
         return children.size() != 0;
     }
 
     public boolean addTagRelationship(final Tag childTag) {
         final List<TagToTag> children = filter(having(on(TagToTag.class).getSecondaryTag(), equalTo(childTag)), getChildrenTagToTags());
         if (children.size() == 0) {
             final TagToTag tagToTag = new TagToTag(new TagToTagRelationship(1), this, childTag);
             getChildrenTagToTags().add(tagToTag);
             childTag.getParentTagToTags().add(tagToTag);
             return true;
         }
 
         return false;
     }
 
     public boolean removeProjectRelationship(final Project project) {
         final List<TagToProject> children = filter(having(on(TagToProject.class).getProject(), equalTo(project)), getTagToProjects());
         for (final TagToProject child : children) {
             getTagToProjects().remove(child);
             child.getProject().getTagToProjects().remove(child);
         }
 
         return children.size() != 0;
     }
 
     public boolean addProjectRelationship(final Project project) {
         final List<TagToProject> children = filter(having(on(TagToProject.class).getProject(), equalTo(project)), getTagToProjects());
         if (children.size() == 0) {
             final TagToProject tagToProject = new TagToProject(project, this);
             getTagToProjects().add(tagToProject);
             project.getTagToProjects().add(tagToProject);
             return true;
         }
 
         return false;
     }
 
     @Transient
     /**
      * @param project Is the project to test the tags association with. A null project 
      * 		test that there is no association with any project, or in other words, to test 
      * 		if this is a common tag
      * @return true if this tag has been assigned to the project, and false otherwise
      */
     public boolean isInProject(final Project project) {
         if (tagToProjects.size() == 0 && project == null) return true;
 
         for (final TagToProject tagToProject : tagToProjects) {
             if (tagToProject.getProject().equals(project)) return true;
         }
 
         return false;
     }
 
     @Transient
     public List<Tag> getTags() {
         final List<Tag> retValue = new ArrayList<Tag>();
         for (final TagToTag tag : childrenTagToTags)
             retValue.add(tag.getSecondaryTag());
 
         return retValue;
     }
 
     @PreRemove
     private void preRemove() {
         for (final TopicToTag topicToTag : topicToTags)
             topicToTag.getTopic().getTopicToTags().remove(topicToTag);
 
         for (final Tag tag : excludedTags)
             tag.getExcludedTags().remove(this);
 
         for (final TagToCategory tagToCategory : tagToCategories)
             tagToCategory.getCategory().getTagToCategories().remove(tagToCategory);
 
         for (final TagToProject tagToProject : tagToProjects)
             tagToProject.getProject().getTagToProjects().remove(tagToProject);
 
         for (final TagToPropertyTag tagToPropertyTag : tagToPropertyTags)
             tagToPropertyTag.getPropertyTag().getTagToPropertyTags().remove(tagToPropertyTag);
 
         for (final TagToTag tagToTag : childrenTagToTags)
             tagToTag.getSecondaryTag().getParentTagToTags().remove(tagToTag);
 
         for (final TagToTag tagToTag : parentTagToTags)
             tagToTag.getPrimaryTag().getChildrenTagToTags().remove(tagToTag);
 
         topicToTags.clear();
         excludedTags.clear();
         tagToCategories.clear();
         tagToProjects.clear();
         tagToPropertyTags.clear();
         childrenTagToTags.clear();
         parentTagToTags.clear();
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TagToPropertyTag> getTagToPropertyTags() {
         return tagToPropertyTags;
     }
 
     public void setTagToPropertyTags(Set<TagToPropertyTag> tagToPropertyTags) {
         this.tagToPropertyTags = tagToPropertyTags;
     }
 
     @Override
     public void removePropertyTag(final TagToPropertyTag tagToPropertyTag) {
         tagToPropertyTags.remove(tagToPropertyTag);
         tagToPropertyTag.getPropertyTag().getTagToPropertyTags().remove(tagToPropertyTag);
     }
 
     @Override
     public void removePropertyTag(final PropertyTag propertyTag, final String value) {
         final List<TagToPropertyTag> removeList = new ArrayList<TagToPropertyTag>();
 
         for (final TagToPropertyTag mapping : tagToPropertyTags) {
             final PropertyTag myPropertyTag = mapping.getPropertyTag();
             if (myPropertyTag.equals(propertyTag) && mapping.getValue().equals(value)) {
                 removeList.add(mapping);
             }
         }
 
         for (final TagToPropertyTag mapping : removeList) {
             tagToPropertyTags.remove(mapping);
             mapping.getPropertyTag().getTagToPropertyTags().remove(mapping);
         }
     }
 
     @Override
     public void addPropertyTag(final TagToPropertyTag tagToPropertyTag) {
        tagToPropertyTag.setTag(this);
         tagToPropertyTags.add(tagToPropertyTag);
         tagToPropertyTag.getPropertyTag().getTagToPropertyTags().add(tagToPropertyTag);
     }
 
     @Override
     public void addPropertyTag(final PropertyTag propertyTag, final String value) {
         final TagToPropertyTag mapping = new TagToPropertyTag();
         mapping.setTag(this);
         mapping.setPropertyTag(propertyTag);
         mapping.setValue(value);
 
         tagToPropertyTags.add(mapping);
         propertyTag.getTagToPropertyTags().add(mapping);
     }
 
     @Override
     @Transient
     public Set<TagToPropertyTag> getPropertyTags() {
         return tagToPropertyTags;
     }
 
     @Transient
     public void addCategory(final TagToCategory tagToCategory) {
        tagToCategory.setTag(this);
         tagToCategory.getCategory().getTagToCategories().add(tagToCategory);
         tagToCategories.add(tagToCategory);
     }
 
     @Transient
     public void removeCategory(final TagToCategory tagToCategory) {
         tagToCategory.getCategory().getTagToCategories().remove(tagToCategory);
         tagToCategories.remove(tagToCategory);
     }
 
     @Transient
     public List<TagToCategory> getTagToCategoriesList() {
         final List<TagToCategory> retValue = new ArrayList<TagToCategory>();
 
         for (final TagToCategory tagToCategory : tagToCategories) {
             retValue.add(tagToCategory);
         }
 
         return retValue;
     }
 }
