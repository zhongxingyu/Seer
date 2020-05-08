 
 package edu.common.dynamicextensions.domaininterface;
 
 import java.util.Collection;
 
 public interface CategoryEntityInterface extends AbstractEntityInterface
 {
 
 	/**
 	 *
 	 * @return
 	 */
 	Collection<CategoryAttributeInterface> getCategoryAttributeCollection();
 
 	/**
 	 *
 	 * @param categoryAttributeCollection
 	 */
 	void setCategoryAttributeCollection(
 			Collection<CategoryAttributeInterface> categoryAttributeCollection);
 
 	/**
 	 *
 	 * @return
 	 */
 	Collection<CategoryEntityInterface> getChildCategories();
 
 	/**
 	 *
 	 * @param childCategories
 	 */
 	void setChildCategories(Collection<CategoryEntityInterface> childCategories);
 
 	/**
 	 * 
 	 * @param categoryEntityInterface
 	 */
 	void addChildCategory(CategoryEntityInterface categoryEntityInterface);
 
 	/**
 	 *
 	 * @return
 	 */
 	EntityInterface getEntity();
 
 	/**
 	 *
 	 * @param entity
 	 */
 	void setEntity(EntityInterface entity);
 
 	/**
 	 * This method adds an AbstractAttribute to the Entity's Collection of AbstractAttribute.
 	 * @param abstractAttribute AbstractAttribute to be added.
 	 */
 	void addCategoryAttribute(CategoryAttributeInterface categoryAttributeInterface);
 
 	/**
 	 * This method removes an AbstractAttribute from the Entity's Collection of AbstractAttribute.
 	 * @param abstractAttribute an AbstractAttribute to be removed.
 	 */
 	void removeCategoryAttribute(CategoryAttributeInterface categoryAttributeInterface);
 
 	/**
 	 * This method removes all the AssociationInterface from thePath's Collection of Association.
 	 * @param associationInterface an AssociationInterface to be removed.
 	 */
 	void removeAllCategoryAttributes();
 
 	/**
 	 * This method adds an AbstractAttribute to the Entity's Collection of AbstractAttribute.
 	 * @param abstractAttribute AbstractAttribute to be added.
 	 */
 	void addPath(PathInterface pathInterface);
 
 	/**
 	 * This method removes an AbstractAttribute from the Entity's Collection of AbstractAttribute.
 	 * @param abstractAttribute an AbstractAttribute to be removed.
 	 */
 	void removePath(PathInterface pathInterface);
 
 	/**
 	 * This method removes all the AssociationInterface from thePath's Collection of Association.
 	 * @param associationInterface an AssociationInterface to be removed.
 	 */
 	void removeAllPaths();
 
 	/**
 	 *
 	 * @return
 	 */
 	CategoryInterface getCategory();
 
 	/**
 	 *
 	 * @param categoryInterface
 	 */
 	void setCategory(CategoryInterface categoryInterface);
 
 	PathInterface getPath();
 
 	void setPath(PathInterface path);
 
 	Integer getNumberOfEntries();
 
 	void setNumberOfEntries(Integer numberOfEntries);
 
 	Collection<CategoryAssociationInterface> getCategoryAssociationCollection();
 
 	void setCategoryAssociationCollection(
 			Collection<CategoryAssociationInterface> categoryAssociationCollection);
 
 	CategoryEntityInterface getParentCategoryEntity();
 
 	void setParentCategoryEntity(CategoryEntityInterface parentCategoryEntity);
 
 	Collection<CategoryAttributeInterface> getAllCategoryAttributes();
 
 	/**
 	 * @param associationName
 	 * @return
 	 */
 	CategoryAssociationInterface getAssociationByName(String associationName);
 
 	/**
 	 *
 	 * @param attributeName
 	 * @return
 	 */
 	CategoryAttributeInterface getAttributeByName(String attributeName);
 
 	CategoryEntityInterface getTreeParentCategoryEntity();
 
 	void setTreeParentCategoryEntity(CategoryEntityInterface treeParentCategoryEntity);
	
	Boolean isCreateTable();
 
 }
