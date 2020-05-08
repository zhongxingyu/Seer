 package org.jboss.pressgang.ccms.model;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
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
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 import org.jboss.pressgang.ccms.model.constants.Constants;
 import org.jboss.pressgang.ccms.model.sort.TagIDComparator;
 import org.jboss.pressgang.ccms.utils.structures.NameIDSortMap;
 
 @Audited
 @Entity
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "Project", uniqueConstraints = @UniqueConstraint(columnNames = {"ProjectName"}))
 public class Project extends AuditedEntity implements java.io.Serializable {
     public static final String SELECT_ALL_QUERY = "select project from Project project";
     private static final long serialVersionUID = 7468160102030564523L;
 
     private Integer projectId;
     private String projectName;
     private String projectDescription;
     private Set<TagToProject> tagToProjects = new HashSet<TagToProject>(0);
 
     public Project() {
     }
 
     public Project(final String projectName) {
         this.projectName = projectName;
     }
 
     public Project(final String projectName, final String projectDescription, final Set<TagToProject> tagToProjects) {
         this.projectName = projectName;
         this.projectDescription = projectDescription;
         this.tagToProjects = tagToProjects;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "ProjectID", unique = true, nullable = false)
     public Integer getProjectId() {
         return projectId;
     }
 
     public void setProjectId(final Integer projectId) {
         this.projectId = projectId;
     }
 
     @Column(name = "ProjectName", nullable = false, length = 255)
     @NotNull(message = "{project.name.notBlank}")
     @NotBlank(message = "{project.name.notBlank}")
     @Size(max = 255)
     public String getProjectName() {
         return projectName;
     }
 
     public void setProjectName(final String projectName) {
         this.projectName = projectName;
     }
 
     @Column(name = "ProjectDescription", columnDefinition = "TEXT")
     @Size(max = 65535)
     public String getProjectDescription() {
         return projectDescription;
     }
 
     public void setProjectDescription(final String projectDescription) {
         this.projectDescription = projectDescription;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "project",
             cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TagToProject> getTagToProjects() {
         return tagToProjects;
     }
 
     public void setTagToProjects(final Set<TagToProject> tagToProjects) {
         this.tagToProjects = tagToProjects;
     }
 
     @Transient
     public String getTagsList() {
         return getTagsList(true);
     }
 
     /**
      * Generates a HTML formatted and categorized list of the tags that are associated with this topic
      *
      * @return A HTML String to display in a table
      */
     @Transient
     public String getTagsList(final boolean brLineBreak) {
         // define the line breaks for html and for tooltips
         final String lineBreak = brLineBreak ? "<br/>" : "\n";
         final String boldStart = brLineBreak ? "<b>" : "";
         final String boldEnd = brLineBreak ? "</b>" : "";
 
         final TreeMap<NameIDSortMap, ArrayList<String>> tags = new TreeMap<NameIDSortMap, ArrayList<String>>();
 
         for (final TagToProject tagToProject : tagToProjects) {
             final Tag tag = tagToProject.getTag();
             final Set<TagToCategory> tagToCategories = tag.getTagToCategories();
 
             if (tagToCategories.size() == 0) {
                NameIDSortMap categoryDetails = new NameIDSortMap("Uncatagorised", -1, 0);
 
                 if (!tags.containsKey(categoryDetails)) tags.put(categoryDetails, new ArrayList<String>());
 
                 tags.get(categoryDetails).add(tag.getTagName());
             } else {
                 for (final TagToCategory category : tagToCategories) {
                     NameIDSortMap categoryDetails = new NameIDSortMap(category.getCategory().getCategoryName(),
                             category.getCategory().getCategoryId(),
                             category.getCategory().getCategorySort() == null ? 0 : category.getCategory().getCategorySort());
 
                     if (!tags.containsKey(categoryDetails)) tags.put(categoryDetails, new ArrayList<String>());
 
                     tags.get(categoryDetails).add(tag.getTagName());
                 }
             }
         }
 
         String tagsList = "";
         for (final NameIDSortMap key : tags.keySet()) {
             // sort alphabetically
             Collections.sort(tags.get(key));
 
             if (tagsList.length() != 0) tagsList += lineBreak;
 
             tagsList += boldStart + key.getName() + boldEnd + ": ";
 
             String thisTagList = "";
 
             for (final String tag : tags.get(key)) {
                 if (thisTagList.length() != 0) thisTagList += ", ";
 
                 thisTagList += tag;
             }
 
             tagsList += thisTagList + " ";
         }
 
         return tagsList;
     }
 
     @Transient
     public List<Tag> getTags() {
         final List<Tag> retValue = new ArrayList<Tag>();
         for (final TagToProject tag : tagToProjects)
             retValue.add(tag.getTag());
 
         Collections.sort(retValue, new TagIDComparator());
 
         return retValue;
     }
 
     @PreRemove
     private void preRemove() {
         for (final TagToProject mapping : getTagToProjects())
             mapping.getTag().getTagToProjects().remove(mapping);
 
         tagToProjects.clear();
     }
 
     @Transient
     public boolean isRelatedTo(final Tag tag) {
         for (final TagToProject mapping : getTagToProjects())
             if (mapping.getTag().equals(tag)) return true;
 
         return false;
     }
 
     public boolean addRelationshipTo(final Tag tag) {
         if (!isRelatedTo(tag)) {
             final TagToProject mapping = new TagToProject(this, tag);
             getTagToProjects().add(mapping);
             tag.getTagToProjects().add(mapping);
             return true;
         }
 
         return false;
     }
 
     public boolean removeRelationshipTo(final Integer relatedTagId) {
         for (final TagToProject mapping : getTagToProjects()) {
             final Tag tag = mapping.getTag();
 
             if (tag.getTagId().equals(relatedTagId)) {
                 /* remove the relationship from this project */
                 getTagToProjects().remove(mapping);
 
                 /* remove the relationship from the tag */
                 tag.getTagToProjects().remove(mapping);
 
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     @Transient
     public Integer getId() {
         return projectId;
     }
 }
