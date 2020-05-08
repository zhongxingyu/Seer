 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  *
 * $Id: CategoryController.java,v 1.12 2005/09/01 14:02:32 mattias Exp $
  */
 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.management.Category;
 import org.infoglue.cms.entities.management.CategoryVO;
 import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 
 
 /**
  * The CategoryController manages all actions related to persistence
  * and querying for Categories.
  *
  * TODO: When we convert have Hibernate manage all of these relationships, it will pull it
  * TODO: all back with one query and be a helluva lot faster than this pasic implementation
  *
  * @author Frank Febbraro (frank@phase2technology.com)
  */
 public class CategoryController extends BaseController
 {
 	private static final CategoryController instance = new CategoryController();
 	private static final ContentCategoryController contentCategoryStore = ContentCategoryController.getController();
 
 	private static final String findByParent = new StringBuffer("SELECT c ")
 			.append("FROM org.infoglue.cms.entities.management.impl.simple.CategoryImpl c ")
 			.append("WHERE c.parentId = $1 ")
 			.append("ORDER BY c.name ASC").toString();
 
 	private static final String findActiveByParent = new StringBuffer("SELECT c ")
 			.append("FROM org.infoglue.cms.entities.management.impl.simple.CategoryImpl c ")
 			.append("WHERE c.parentId = $1 ")
 			.append("AND c.active = $2 ")
 			.append("ORDER BY c.name ASC").toString();
 
 	private static final String findRootCategories = new StringBuffer("SELECT c ")
 			.append("FROM org.infoglue.cms.entities.management.impl.simple.CategoryImpl c ")
 			.append("WHERE is_undefined(c.parentId) ")
 			.append("ORDER BY c.name ASC").toString();
 
 	private static final String findActiveRootCategories = new StringBuffer("SELECT c ")
 			.append("FROM org.infoglue.cms.entities.management.impl.simple.CategoryImpl c ")
 			.append("WHERE is_undefined(c.parentId) ")
 			.append("AND c.active = $1 ")
 			.append("ORDER BY c.name ASC").toString();
 
 	public static CategoryController getController()
 	{ return instance; }
 
 	private CategoryController()
 	{}
 
 	/**
 	 * Find a Category by it's identifier.
 	 *
 	 * @param	id The id of the Category to find
 	 * @return	The CategoryVO identified by the provided id
 	 * @throws	SystemException If an error happens
 	 */
 	public CategoryVO findById(Integer id) throws SystemException
 	{
 		return (CategoryVO)getVOWithId(CategoryImpl.class, id);
 	}
 
 	/**
 	 * Find a Category by it's identifier.
 	 *
 	 * @param	id The id of the Category to find
 	 * @return	The Category identified by the provided id
 	 * @throws	SystemException If an error happens
 	 */
 	public Category findById(Integer id, Database db) throws SystemException
 	{
 		return (Category)getObjectWithId(CategoryImpl.class, id, db);
 	}
 
 	/**
 	 * Find a Category by it's name path.
 	 *
 	 * @param	path The path of the Category to find in the form /categoryName/categoryName/categoryName
 	 * @return	The CategoryVO identified by the provided path
 	 * @throws	SystemException If an error happens
 	 */
 	public CategoryVO findByPath(String path) throws SystemException
 	{
 	    CategoryVO categoryVO = null;
 	    
 	    String[] nodes = path.substring(1).split("/");
         
 	    if(nodes.length > 0)
 	    {
 	        List rootCategories = findRootCategories();
 	        String name = nodes[0];
 	        categoryVO = getCategoryVOWithNameInList(rootCategories, name);
 	        
 	        for(int i = 1; i < nodes.length; i++)
 	        {
 	            categoryVO = getCategoryVOWithNameInList(findByParent(categoryVO.getId()), nodes[i]);
 		    }
 	    }
 	    
 	    return categoryVO;
 	}
 
 	/**
 	 * Find a Category by it's name path.
 	 *
 	 * @param	path The path of the Category to find in the form /categoryName/categoryName/categoryName
 	 * @return	The CategoryVO identified by the provided path
 	 * @throws	SystemException If an error happens
 	 */
 	public CategoryVO findByPath(String path, Database db) throws SystemException
 	{
 	    CategoryVO categoryVO = null;
 	    
 	    String[] nodes = path.substring(1).split("/");
         
 	    if(nodes.length > 0)
 	    {
	        List rootCategories = findRootCategoryVOList(db);
 	        String name = nodes[0];
 	        categoryVO = getCategoryVOWithNameInList(rootCategories, name);
 	        
 	        for(int i = 1; i < nodes.length; i++)
 	        {
 	            categoryVO = getCategoryVOWithNameInList(findByParent(categoryVO.getId(), db), nodes[i]);
 		    }
 	    }
 	    
 	    return categoryVO;
 	}
 
 	/**
 	 * Iterates the list of categories and returns the first one that matches your name.
 	 * @param categoryVOList
 	 * @param name
 	 * @return
 	 */
 
 	private CategoryVO getCategoryVOWithNameInList(List categoryVOList, String name)
 	{
 	    CategoryVO categoryVO = null;
 	    
         Iterator categoryVOListIterator = categoryVOList.iterator();
         while(categoryVOListIterator.hasNext())
         {
             CategoryVO currentCategoryVO = (CategoryVO)categoryVOListIterator.next();
 	        getLogger().info("currentCategoryVO:" + currentCategoryVO.getName() + "=" + name);
             if(currentCategoryVO.getName().equalsIgnoreCase(name))
             {
                 categoryVO = currentCategoryVO;
             	break;
             }
         }
         
         return categoryVO;
 	}
 
 
 	/**
 	 * Find a List of Categories by parent.
 	 *
 	 * @param	parentId The parent id of the Category to find
 	 * @return	A list of CategoryVOs that have the provided parentId
 	 * @throws	SystemException If an error happens
 	 */
 	public List findByParent(Integer parentId) throws SystemException
 	{
 		List params = new ArrayList();
 		params.add(parentId);
 		return executeQuery(findByParent, params);
 	}
 
 	/**
 	 * Find a List of Categories by parent.
 	 *
 	 * @param	parentId The parent id of the Category to find
 	 * @return	A list of CategoryVOs that have the provided parentId
 	 * @throws	SystemException If an error happens
 	 */
 	public List findByParent(Integer parentId, Database db) throws SystemException
 	{
 		List params = new ArrayList();
 		params.add(parentId);
 		return executeQuery(findByParent, params, db);
 	}
 
 	/**
 	 * Find a List of active Categories by parent.
 	 *
 	 * @param	parentId The parent id of the Category to find
 	 * @return	A list of CategoryVOs that have the provided parentId
 	 * @throws	SystemException If an error happens
 	 */
 	public List findActiveByParent(Integer parentId) throws SystemException
 	{
 		List params = new ArrayList();
 		params.add(parentId);
 		params.add(Boolean.TRUE);
 		return executeQuery(findActiveByParent, params);
 	}
 
 	/**
 	 * Find a Category with it's children populated.
 	 *
 	 * @param	id The id of the Category to find
 	 * @return	A list of CategoryVOs that are at the root of the category tree
 	 * @throws	SystemException If an error happens
 	 */
 	public CategoryVO findWithChildren(Integer id) throws SystemException
 	{
 		CategoryVO c = findById(id);
 		c.setChildren(findByParent(c.getId()));
 		return c;
 	}
 
 	/**
 	 * Find a Category with it's children populated.
 	 *
 	 * @param	id The id of the Category to find
 	 * @return	A list of CategoryVOs that are at the root of the category tree
 	 * @throws	SystemException If an error happens
 	 */
 	public CategoryVO findWithChildren(Integer id, Database db) throws SystemException
 	{
 		Category c = findById(id, db);
 		c.getValueObject().setChildren(findByParent(c.getId(), db));
 		return c.getValueObject();
 	}
 
 	/**
 	 * Find a List of Categories that have no parent.
 	 *
 	 * @return	A list of CategoryVOs that are at the root of the category tree
 	 * @throws	SystemException If an error happens
 	 */
 	public List findRootCategories() throws SystemException
 	{
 		return executeQuery(findRootCategories);
 	}
 
 	/**
 	 * Find a List of Categories that have no parent.
 	 *
 	 * @return	A list of CategoryVOs that are at the root of the category tree
 	 * @throws	SystemException If an error happens
 	 */
 	public List findRootCategories(Database db) throws SystemException
 	{
 		return executeQuery(findRootCategories, db);
 	}
 
 	/**
	 * Find a List of Categories that have no parent.
	 *
	 * @return	A list of CategoryVOs that are at the root of the category tree
	 * @throws	SystemException If an error happens
	 */
	public List findRootCategoryVOList(Database db) throws SystemException
	{
	    List categories = executeQuery(findRootCategories, db);
		return (categories != null) ? toVOList(categories) : null;
	}

	/**
 	 * Find a list of all Categories in the system.
 	 *
 	 * @return	A list of CategoryVOs starting at the root of the category tree
 	 * @throws	SystemException If an error happens
 	 */
 	public List findAllActiveCategories() throws SystemException
 	{
 		List params = new ArrayList();
 		params.add(Boolean.TRUE);
 		List roots = executeQuery(findActiveRootCategories, params);
 		for (Iterator iter = roots.iterator(); iter.hasNext();)
 		{
 			CategoryVO root = (CategoryVO) iter.next();
 			root.setChildren(findAllActiveChildren(root.getId()));
 		}
 		return roots;
 	}
 
 	
 	/**
 	 * Finds all authorized categories parent id, recursively until no children are found.
 	 *
 	 * @return A list of children nodes, with thier children populated
 	 */
 	public List getAuthorizedActiveChildren(Integer parentId, InfoGluePrincipal infogluePrincipal) throws SystemException
 	{
 		List children = findActiveByParent(parentId);
 		for (Iterator iter = children.iterator(); iter.hasNext();)
 		{
 			CategoryVO child = (CategoryVO) iter.next();
 			if(!getIsAccessApproved(child.getCategoryId(), infogluePrincipal))
 			{
 			    iter.remove();
 			}
 			
 			List subChildren = findAllActiveChildren(child.getId());
 			Iterator subChildrenIterator = subChildren.iterator();
 			while(subChildrenIterator.hasNext())
 			{
 			    CategoryVO subChild = (CategoryVO) subChildrenIterator.next();
 			    if(getIsAccessApproved(subChild.getCategoryId(), infogluePrincipal))
 				{
 				    child.getChildren().add(subChild);
 				}
 			}
 		}
 		return children;
 	}
 	
 	/**
 	 * Finds all children for a given parent id, recursively until no children are found.
 	 *
 	 * @return A list of children nodes, with thier children populated
 	 */
 	public List findAllActiveChildren(Integer parentId) throws SystemException
 	{
 		List children = findActiveByParent(parentId);
 		for (Iterator iter = children.iterator(); iter.hasNext();)
 		{
 			CategoryVO child = (CategoryVO) iter.next();
 			child.setChildren(findAllActiveChildren(child.getId()));
 		}
 		return children;
 	}
 
 	/**
 	 * Saves a CategoryVO whether it is new or not.
 	 *
 	 * @param	c The CategoryVO to save
 	 * @return	The saved CategoryVO
 	 * @throws	SystemException If an error happens
 	 */
 	public CategoryVO save(CategoryVO c) throws SystemException
 	{
 		return (c.isUnsaved())
 					? create(c)
 					: (CategoryVO)updateEntity(CategoryImpl.class, c);
 	}
 
 	/**
 	 * Creates a Category from a CategoryVO
 	 */
 	private CategoryVO create(CategoryVO c) throws SystemException
 	{
 		CategoryImpl impl = new CategoryImpl(c);
 		return ((CategoryImpl)createEntity(impl)).getValueObject();
 	}
 
 	/**
 	 * Moves a CategoryVO to a different parent category
 	 *
 	 * @param	categoryId The id of the CategoryVO to move
 	 * @param	newParentId The id of the parent to move the CategoryVO
 	 * @return	The saved CategoryVO
 	 * @throws	SystemException If an error happens
 	 */
 	public CategoryVO moveCategory(Integer categoryId, Integer newParentId) throws SystemException
 	{
 		CategoryVO category = findById(categoryId);
 		category.setParentId(newParentId);
 		return save(category);
 	}
 
 	/**
 	 * Deletes a CategoryVO, and all children.
 	 *
 	 * TODO: The reason we delete the ContentCategory first is that once the Category
 	 * TODO: is gone, Castor will never find them again. When we move to Hibernate we
 	 * TODO: can probalby put this afterwards, in it's more logical place.
 	 *
 	 * @param	id The id of the Category to delete
 	 * @throws	SystemException If an error happens
 	 */
 	public void delete(Integer id) throws SystemException
 	{
 		contentCategoryStore.deleteByCategory(id);
 		deleteEntity(CategoryImpl.class, id);
 		deleteChildren(id);
 	}
 
 	/**
 	 * Deletes the children of the supplied category
 	 */
 	private void deleteChildren(Integer id) throws SystemException
 	{
 		List children = findByParent(id);
 		for (Iterator iter = children.iterator(); iter.hasNext();)
 			delete(((CategoryVO) iter.next()).getId());
 	}
 
 	/**
 	 * Implemented for BaseController
 	 */
 	public BaseEntityVO getNewVO()
 	{
 		return new CategoryVO();
 	}
 	
 	
 	/**
 	 * This method returns true if the user should have access to the contentTypeDefinition sent in.
 	 */
     
 	public boolean getIsAccessApproved(Integer categoryId, InfoGluePrincipal infoGluePrincipal) throws SystemException
 	{
 		getLogger().info("getIsAccessApproved for " + categoryId + " AND " + infoGluePrincipal);
 		boolean hasAccess = false;
     	
 		Database db = CastorDatabaseService.getDatabase();
        
 		beginTransaction(db);
 
 		try
 		{ 
 			hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Category.Read", categoryId.toString());
 		
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			getLogger().error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
     
 		return hasAccess;
 	}
 }
