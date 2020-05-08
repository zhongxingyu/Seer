 
 package edu.common.dynamicextensions.domain;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PathInterface;
 
 /**
  *
  * @author mandar_shidhore
  * @hibernate.joined-subclass table="DYEXTN_CATEGORY_ENTITY"
  * @hibernate.joined-subclass-key column="IDENTIFIER"
  *
  */
 public class CategoryEntity extends AbstractEntity implements CategoryEntityInterface
 {
 
 	/**
 	 * Serial Version UID
 	 */
 	private static final long serialVersionUID = 6534523890L;
 
 	/**
 	 *
 	 */
 	protected Integer numberOfEntries;
 
 	/**
 	 *
 	 */
 	protected Collection<CategoryEntityInterface> childCategories = new HashSet<CategoryEntityInterface>();
 
 	/**
 	 * 
 	 */
 	protected CategoryEntityInterface parentCategoryEntity;
 	
 	/**
 	 * 
 	 */
 	protected CategoryEntityInterface treeParentCategoryEntity;
 
 	/**
 	 * 
 	 */
 	//protected Collection<CategoryEntityInterface> parentCategoryEntityCollection = new HashSet<CategoryEntityInterface>();
 	/**
 	 *
 	 */
 	protected Collection<CategoryAttributeInterface> categoryAttributeCollection = new HashSet<CategoryAttributeInterface>();
 
 	/**
 	 *
 	 */
 	protected EntityInterface entity;
 
 	/**
 	 *
 	 */
 	protected Collection<PathInterface> pathCollection = new HashSet<PathInterface>();
 
 	/**
 	 *
 	 */
 	protected Collection<CategoryInterface> categoryCollection = new HashSet<CategoryInterface>();
 
 	/**
 	 * 
 	 */
 	protected Collection<CategoryAssociationInterface> CategoryAssociationCollection = new HashSet<CategoryAssociationInterface>();
 	/**
 	 * 
 	 */
	protected Boolean isCreateTable = new Boolean(true);
 	/**
 	 *
 	 *
 	 */
 	public CategoryEntity()
 	{
 		super();
 
 	}
 
 	/**
 	 * This method returns the number of entries.
 	 * @hibernate.property name="numberOfEntries" type="integer" column="NUMBER_OF_ENTRIES"
 	 * @return the maximum cardinality.
 	 */
 	public Integer getNumberOfEntries()
 	{
 		return numberOfEntries;
 	}
 
 	/**
 	 * @param noOfEntries the noOfEntries to set
 	 */
 	public void setNumberOfEntries(Integer numberOfEntries)
 	{
 		this.numberOfEntries = numberOfEntries;
 	}
 
 	/**
 	 * @hibernate.set name="categoryAttributeCollection" table="DYEXTN_CATEGORY_ATTRIBUTE"
 	 * cascade="all-delete-orphan" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="CATEGORY_ENTITY_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.CategoryAttribute"
 	 * @return the categoryAttributeCollection
 	 */
 
 	public Collection<CategoryAttributeInterface> getCategoryAttributeCollection()
 	{
 		return categoryAttributeCollection;
 	}
 
 	/**
 	 * @param categoryAttributeCollection the categoryAttributeCollection to set
 	 */
 	public void setCategoryAttributeCollection(Collection<CategoryAttributeInterface> categoryAttributeCollection)
 	{
 		this.categoryAttributeCollection = categoryAttributeCollection;
 	}
 
 	/**
 	 * @hibernate.set name="childCategories" table="DYEXTN_CATEGORY_ENTITY"
 	 * cascade="none" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="PARENT_CATEGORY_ENTITY_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.CategoryEntity"
 	 * @return the childCategories
 	 */
 	public Collection<CategoryEntityInterface> getChildCategories()
 	{
 		return childCategories;
 	}
 
 	/**
 	 * @param childCategories the childCategories to set
 	 */
 	public void setChildCategories(Collection<CategoryEntityInterface> childCategories)
 	{
 		this.childCategories = childCategories;
 	}
 
 	/**
 	 * 
 	 * @param categoryEntityInterface
 	 */
 	public void addChildCategory(CategoryEntityInterface categoryEntityInterface)
 	{
 		if (this.childCategories == null)
 		{
 			childCategories = new HashSet<CategoryEntityInterface>();
 		}
 		childCategories.add(categoryEntityInterface);
 	}
 
 	/**
 	 * @hibernate.many-to-one class="edu.common.dynamicextensions.domain.Entity" column="ENTITY_ID" cascade="save-update"
 	 * @return the entity
 	 */
 	public EntityInterface getEntity()
 	{
 		return entity;
 	}
 
 	/**
 	 * @param entity the entity to set
 	 */
 	public void setEntity(EntityInterface entity)
 	{
 		this.entity = entity;
 	}
 
 	/**
 	 * @hibernate.set name="pathCollection" table="DYEXTN_PATH"
 	 * cascade="save-update" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="CATEGORY_ENTITY_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.Path"
 	 * @return the pathCollection
 	 */
 	private Collection<PathInterface> getPathCollection()
 	{
 		return pathCollection;
 	}
 
 	/**
 	 *
 	 * @param pathCollection
 	 */
 	private void setPathCollection(Collection<PathInterface> pathCollection)
 	{
 		this.pathCollection = pathCollection;
 	}
 
 	public PathInterface getPath()
 	{
 		PathInterface path = null;
 		if (pathCollection != null && !pathCollection.isEmpty())
 		{
 			Iterator pathIterator = pathCollection.iterator();
 			path = (Path) pathIterator.next();
 		}
 		return path;
 	}
 
 	public void setPath(PathInterface path)
 	{
 		if (pathCollection == null)
 		{
 			pathCollection = new HashSet<PathInterface>();
 		}
 		else
 		{
 			pathCollection.clear();
 		}
 		this.pathCollection.add(path);
 	}
 
 	/**
 	 *
 	 */
 	public void addCategoryAttribute(CategoryAttributeInterface categoryAttributeInterface)
 	{
 		categoryAttributeCollection.add(categoryAttributeInterface);
 	}
 
 	/**
 	 *
 	 */
 	public void removeCategoryAttribute(CategoryAttributeInterface categoryAttributeInterface)
 	{
 
 	}
 
 	/**
 	 *
 	 */
 	public void addPath(PathInterface pathInterface)
 	{
 
 	}
 
 	/**
 	 *
 	 */
 	public void removePath(PathInterface pathInterface)
 	{
 
 	}
 
 	/**
 	 *
 	 */
 	public void removeAllPaths()
 	{
 
 	}
 
 	public void removeAllCategoryAttributes()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * @hibernate.set name="categoryCollection" table="DYEXTN_CATEGORY"
 	 * cascade="none" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="CATEGORY_ENTITY_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.Category"
 	 * @return the categoryCollection
 	 */
 	public Collection<CategoryInterface> getCategoryCollection()
 	{
 		return categoryCollection;
 	}
 
 	/**
 	 *
 	 * @param categoryCollection
 	 */
 	public void setCategoryCollection(Collection<CategoryInterface> categoryCollection)
 	{
 		this.categoryCollection = categoryCollection;
 	}
 
 	/**
 	 *
 	 */
 	public CategoryInterface getCategory()
 	{
 		CategoryInterface categoryInterface = null;
 		if (categoryCollection != null && !categoryCollection.isEmpty())
 		{
 			Iterator categoryCollectionIterator = categoryCollection.iterator();
 			categoryInterface = (CategoryInterface) categoryCollectionIterator.next();
 		}
 		return categoryInterface;
 	}
 
 	/**
 	 *
 	 */
 	public void setCategory(CategoryInterface categoryInterface)
 	{
 		if (categoryCollection == null)
 		{
 			categoryCollection = new HashSet<CategoryInterface>();
 		}
 		else
 		{
 			categoryCollection.clear();
 		}
 		this.categoryCollection.add(categoryInterface);
 	}
 
 	/**
 	 * @hibernate.set name="categoryAssociationCollection" table="DYEXTN_CATEGORY_ASSOCIATION"
 	 * cascade="all-delete-orphan" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="CATEGORY_ENTITY_ID"
 	 * @hibernate.cache usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.CategoryAssociation"
 	 * @return the categoryAssociationCollection
 	 */
 	public Collection<CategoryAssociationInterface> getCategoryAssociationCollection()
 	{
 		return CategoryAssociationCollection;
 	}
 
 	/**
 	 * @param categoryAssociationCollection the categoryAssociationCollection to set
 	 */
 	public void setCategoryAssociationCollection(Collection<CategoryAssociationInterface> categoryAssociationCollection)
 	{
 		CategoryAssociationCollection = categoryAssociationCollection;
 	}
 
 	/**
 	 * @hibernate.many-to-one column="OWN_PARENT_CATEGORY_ENTITY_ID" cascade="all" class="edu.common.dynamicextensions.domain.CategoryEntity"
 	 * @return the parentCategoryEntity
 	 */
 	public CategoryEntityInterface getParentCategoryEntity()
 	{
 		return parentCategoryEntity;
 	}
 
 	/**
 	 * @param parentCategoryEntity the parentCategoryEntity to set
 	 */
 	public void setParentCategoryEntity(CategoryEntityInterface parentCategoryEntity)
 	{
 		this.parentCategoryEntity = parentCategoryEntity;
 	}
 
 	/**
 	 * Fetch self plus all parents' category attributes for a category entity.
 	 * @return allCategoryAttributesCollection
 	 */
 	public Collection<CategoryAttributeInterface> getAllCategoryAttributes()
 	{
 		Collection<CategoryAttributeInterface> allCategoryAttributesCollection = new HashSet<CategoryAttributeInterface>();
 		allCategoryAttributesCollection.addAll(getCategoryAttributeCollection());
 		CategoryEntityInterface parent = this.parentCategoryEntity;
 		while (parent != null)
 		{
 			allCategoryAttributesCollection.addAll(parent.getCategoryAttributeCollection());
 			parent = parent.getParentCategoryEntity();
 		}
 		return allCategoryAttributesCollection;
 	}
 
 	/* (non-Javadoc)
 	 * Incase of CategoryEntity this method will alwas return false, 
 	 * since category entity cannot be abstract. 
 	 * @see edu.common.dynamicextensions.domaininterface.AbstractEntityInterface#isAbstract()
 	 */
 	public boolean isAbstract()
 	{
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * Clears the categoryAttributeCollection
 	 * @see edu.common.dynamicextensions.domaininterface.AbstractEntityInterface#removeAllAttributes()
 	 */
 	public void removeAllAttributes()
 	{
 		if (categoryAttributeCollection != null)
 		{
 			categoryAttributeCollection.clear();
 		}
 	}
 
 	/**
 	 * 
 	 * @param attributeName
 	 * @return
 	 */
 	public CategoryAssociationInterface getAssociationByName(String attributeName)
 	{
 		CategoryAssociationInterface association = null;
 		for (CategoryAssociationInterface attr : this.getCategoryAssociationCollection())
 		{
 			if (attr.getName().trim().equals(attributeName))
 			{
 				association = attr;
 			}
 		}
 
 		return association;
 	}
 	
 	/**
 	 * This method returns the attribute for the given corresponding identifier.
 	 * @param id identifier of the desired AbstractAttribute.
 	 * @return the matched instance of AbstractAttribute.
 	 */
 	public CategoryAttributeInterface getAttributeByIdentifier(Long id)
 	{
 		CategoryAttributeInterface categoryAttribute = null;
 		Collection<CategoryAttributeInterface> attributeCollection = getCategoryAttributeCollection();
 
 		for (CategoryAttributeInterface categoryAttributeInterface : attributeCollection)
 		{
 			if (categoryAttributeInterface.getId() != null && categoryAttributeInterface.getId().equals(id))
 			{
 				categoryAttribute = categoryAttributeInterface;
 				break;
 			}
 		}
 		return categoryAttribute;
 	}
 	/**
 	 * This method returns the attribute for the given corresponding identifier.
 	 * @param id identifier of the desired AbstractAttribute.
 	 * @return the matched instance of AbstractAttribute.
 	 */
 	public CategoryAssociationInterface getAssociationByIdentifier(Long id)
 	{
 		CategoryAssociationInterface association = null;
 		Collection<CategoryAssociationInterface> associationCollection = getCategoryAssociationCollection();
 
 		for (CategoryAssociationInterface categoryAssociationInterface : associationCollection)
 		{
 			if (categoryAssociationInterface.getId() != null && categoryAssociationInterface.getId().equals(id))
 			{
 				association = categoryAssociationInterface;
 				break;
 			}
 		}
 		return association;
 	}
 	/**
 	 *
 	 * @param attributeName
 	 * @return
 	 */
 	public CategoryAttributeInterface getAttributeByName(String attributeName)
 	{
 		CategoryAttributeInterface attribute = null;
 
 		for (CategoryAttributeInterface attr: this.getCategoryAttributeCollection())
 		{
 			if (attr.getName().equals(attributeName))
 			{
 				attribute = attr;
 			}
 		}
 
 		return attribute;
 	}
 
 	/**
      * @hibernate.many-to-one column="TREE_PARENT_CATEGORY_ENTITY_ID" cascade="all" class="edu.common.dynamicextensions.domain.CategoryEntity"
 	 * @return the parentCategoryEntity
      */
 	public CategoryEntityInterface getTreeParentCategoryEntity()
 	{
 		return treeParentCategoryEntity;
 	}
 
 	/**
 	 * @param treeParentCategoryEntity the treeParentCategoryEntity to set
 	 */
 	public void setTreeParentCategoryEntity(CategoryEntityInterface treeParentCategoryEntity)
 	{
 		this.treeParentCategoryEntity = treeParentCategoryEntity;
 	}
 	/**
 	 * This method returns the create table
 	 * @hibernate.property name="isCreateTable" type="boolean" column="IS_CREATETABLE"
 	 * @return the isCreateTable 
 	 */
 	public Boolean isCreateTable()
 	{
 		return isCreateTable;
 	}
 	/**
 	 * @param isTableCreated The isTableCreated to set.
 	 */
 	public void setCreateTable(Boolean isTableCreated)
 	{
 		this.isCreateTable = isTableCreated;
 	}
 
 }
