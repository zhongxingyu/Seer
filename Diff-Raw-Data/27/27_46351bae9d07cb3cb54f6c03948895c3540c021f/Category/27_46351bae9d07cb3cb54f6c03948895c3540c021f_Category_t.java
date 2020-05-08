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
 
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 import org.jboss.pressgang.ccms.model.constants.Constants;
 import org.jboss.pressgang.ccms.model.sort.TagIDComparator;
 import org.jboss.pressgang.ccms.model.sort.TagToCategorySortingComparator;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "Category", uniqueConstraints = @UniqueConstraint(columnNames = {"CategoryName"}))
 public class Category extends AuditedEntity implements java.io.Serializable, Comparable<Category> {
     public static final String SELECT_ALL_QUERY = "select category from Category category";
     private static final long serialVersionUID = -8650833773254246211L;
 
     private Integer categoryId;
     private String categoryName;
     private String categoryDescription;
     private Set<TagToCategory> tagToCategories = new HashSet<TagToCategory>(0);
     private Integer categorySort;
     private boolean mutuallyExclusive;
 
     public Category() {
     }
 
     public Category(String categoryName) {
         this.categoryName = categoryName;
     }
 
     public Category(final String categoryName, final String categoryDescription, final Set<TagToCategory> tagToCategories) {
         this.categoryName = categoryName;
         this.categoryDescription = categoryDescription;
         this.tagToCategories = tagToCategories;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TagToCategory> getTagToCategories() {
         return tagToCategories;
     }
 
     public void setTagToCategories(Set<TagToCategory> tagToCategories) {
         this.tagToCategories = tagToCategories;
     }
 
     @Transient
     public List<TagToCategory> getTagToCategoriesArray() {
         final List<TagToCategory> retValue = new ArrayList<TagToCategory>();
 
         for (final TagToCategory tagToCategory : tagToCategories) {
             retValue.add(tagToCategory);
         }
 
         return retValue;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "CategoryID", unique = true, nullable = false)
     public Integer getCategoryId() {
         return categoryId;
     }
 
     public void setCategoryId(Integer categoryId) {
         this.categoryId = categoryId;
     }
 
     @Column(name = "CategoryName", nullable = false, length = 255)
     @NotNull(message = "{category.name.notBlank}")
     @NotBlank(message = "{category.name.notBlank}")
     @Size(max = 255)
     public String getCategoryName() {
         return categoryName;
     }
 
     public void setCategoryName(String categoryName) {
         this.categoryName = categoryName;
     }
 
     @Column(name = "CategoryDescription", columnDefinition = "TEXT")
     @Size(max = 65535)
     public String getCategoryDescription() {
         return categoryDescription;
     }
 
     public void setCategoryDescription(String categoryDescription) {
         this.categoryDescription = categoryDescription;
     }
 
     @Column(name = "CategorySort")
     public Integer getCategorySort() {
         return categorySort;
     }
 
     public void setCategorySort(Integer categorySort) {
         this.categorySort = categorySort;
     }
 
     @Column(name = "MutuallyExclusive", nullable = false, columnDefinition = "BIT", length = 1)
     @NotNull
     public boolean isMutuallyExclusive() {
         return mutuallyExclusive;
     }
 
     public void setMutuallyExclusive(boolean mutuallyExclusive) {
         this.mutuallyExclusive = mutuallyExclusive;
     }
 
     @Override
     public int compareTo(Category o) {
         if (o == null) return 1;
 
         if (o.getCategorySort() == null && getCategorySort() == null) return 0;
 
         if (o.getCategorySort() == null) return 1;
 
         if (getCategorySort() == null) return -1;
 
         return getCategorySort().compareTo(o.getCategorySort());
     }
 
     @Transient
     public List<Tag> getTags() {
         final List<Tag> retValue = new ArrayList<Tag>();
         for (final TagToCategory tag : tagToCategories)
             retValue.add(tag.getTag());
 
         Collections.sort(retValue, new TagIDComparator());
 
         return retValue;
     }
 
     @Transient
     public List<Tag> getTagsInProject(final Project project) {
         final List<Tag> retValue = new ArrayList<Tag>();
 
         for (final TagToCategory tagToCategory : tagToCategories) {
             final Tag tag = tagToCategory.getTag();
             if (tag.isInProject(project)) retValue.add(tag);
         }
 
         return retValue;
     }
 
     @Transient
     public String getTagsList() {
         String tagsList = "";
 
         final List<TagToCategory> tags = new ArrayList<TagToCategory>(getTagToCategories());
         Collections.sort(tags, new TagToCategorySortingComparator());
 
         for (final TagToCategory tagToCatgeory : tags) {
             if (tagsList.length() != 0) tagsList += ", ";
             tagsList += tagToCatgeory.getTag().getTagName();
         }
         return tagsList;
     }
 
     public boolean removeTag(final Tag childTag) {
         final List<TagToCategory> children = filter(having(on(TagToCategory.class).getTag(), equalTo(childTag)), getTagToCategories());
         for (final TagToCategory child : children) {
             getTagToCategories().remove(child);
             childTag.getTagToCategories().remove(child);
         }
 
         return children.size() != 0;
     }
 
     public boolean addTag(final Tag childTag, final Integer sort) {
         final List<TagToCategory> children = filter(having(on(TagToCategory.class).getTag(), equalTo(childTag)), getTagToCategories());
 
         /* If this tag is not mapped at all, add it */
         if (children.size() == 0) {
             final TagToCategory tagToCategory = new TagToCategory(childTag, this);
             tagToCategory.setSorting(sort);
             getTagToCategories().add(tagToCategory);
             childTag.getTagToCategories().add(tagToCategory);
             return true;
         }
 
         return false;
     }
 
     public boolean addTag(final Tag childTag) {
         return addTag(childTag, null);
     }
 
     @PreRemove
     private void preRemove() {
         for (final TagToCategory tagToCategory : tagToCategories)
             tagToCategory.getTag().getTagToCategories().remove(tagToCategory);
 
         tagToCategories.clear();
     }
 
     @Override
     @Transient
     public Integer getId() {
         return categoryId;
     }
 
     @Transient
     public void addTag(final TagToCategory tagToCategory) {
        tagToCategory.setCategory(this);
         tagToCategory.getTag().getTagToCategories().add(tagToCategory);
         tagToCategories.add(tagToCategory);
     }
 
     @Transient
     public void removeTag(final TagToCategory tagToCategory) {
         tagToCategory.getTag().getTagToCategories().remove(tagToCategory);
         tagToCategories.remove(tagToCategory);
     }
 }
