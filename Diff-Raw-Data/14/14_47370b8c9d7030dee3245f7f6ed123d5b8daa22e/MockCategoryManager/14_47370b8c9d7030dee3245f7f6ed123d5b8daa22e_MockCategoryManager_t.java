 
 package edu.common.dynamicextensions.categoryManager;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import edu.common.dynamicextensions.bizlogic.BizLogicFactory;
 import edu.common.dynamicextensions.domain.Association;
 import edu.common.dynamicextensions.domain.CategoryAttribute;
 import edu.common.dynamicextensions.domain.CategoryEntity;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.Path;
 import edu.common.dynamicextensions.domain.PathAssociationRelationInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.DynamicExtensionBaseDomainObjectInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PathInterface;
 import edu.common.dynamicextensions.entitymanager.MockEntityManager;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.util.dbManager.DAOException;
 
 /**
  * 
  * @author mandar_shidhore
  *
  */
 public class MockCategoryManager
 {
 
 	/**
 	 * Create following hierarchy.
 	 * 
 	 * 											Category 1
 	 * 												|				 
 	 * 												|				|---> Category Attribute 1
 	 * 										Root Category Entity ---|
 	 *          									|				|---> Category Attribute 2
 	 *          									|				 	 
 	 *          									|
 	 *          									|
 	 *                  ____________________________|____________________________
 	 *     				|	 						|							|							
 	 *     				|							|							|							
 	 *  		Child Category Entity 1	    Child Category Entity 2	    Child Category Entity 3	    
 	 *         			|							|							|						
 	 *     		   _____|_____				   _____|_____				   _____|_____				  
 	 *    		  |			  |				  |			  |				  |			  |				  
 	 *  		Attr 1      Attr 2			Attr 1      Attr 2			Attr 1      Attr 2			      
 	 *       
 	 */
 	public CategoryInterface createCategory() throws DynamicExtensionsApplicationException
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("Entity Group " + new Double(Math.random()).toString());
 		entityGroup.setCreatedDate(new Date());
 		entityGroup.setDescription("This is a description for entity group");
 
 		CategoryInterface category = factory.createCategory();
 		category.setName("Category 1");
 		category.setCreatedDate(new Date());
 
 		// Create root category entity.
 		CategoryEntityInterface rootCategoryEntity = factory.createCategoryEntity();
 		rootCategoryEntity.setCreatedDate(new Date());
 		rootCategoryEntity.setName("Root Category Entity");
 
 		EntityInterface entity1 = new MockEntityManager().initializeEntity(entityGroup);
 		entity1.setName("Entity 1");
 		rootCategoryEntity.setEntity((Entity) entity1);
 
 		List<AttributeInterface> entity1AttributeCollection = new ArrayList<AttributeInterface>(entity1.getAttributeCollection());
 
 		CategoryAttributeInterface rootCategoryEntityAttribute1 = factory.createCategoryAttribute();
 		rootCategoryEntityAttribute1.setCreatedDate(new Date());
 		rootCategoryEntityAttribute1.setName("Category Attribute 1");
 		rootCategoryEntityAttribute1.setAttribute(entity1AttributeCollection.get(0));
 		rootCategoryEntity.getCategoryAttributeCollection().add((CategoryAttribute) rootCategoryEntityAttribute1);
 
 		CategoryAttributeInterface rootCategoryEntityAttribute2 = factory.createCategoryAttribute();
 		rootCategoryEntityAttribute2.setCreatedDate(new Date());
 		rootCategoryEntityAttribute2.setName("Category Attribute 2");
 		rootCategoryEntityAttribute2.setAttribute(entity1AttributeCollection.get(1));
 		rootCategoryEntity.getCategoryAttributeCollection().add((CategoryAttribute) rootCategoryEntityAttribute2);
 
 		// Create another category entity.
 		CategoryEntityInterface childCategoryEntity1 = factory.createCategoryEntity();
 		childCategoryEntity1.setCreatedDate(new Date());
 		childCategoryEntity1.setName("Child Category Entity 1");
 
 		EntityInterface entity2 = new MockEntityManager().initializeEntity(entityGroup);
 		entity2.setName("Entity 2");
 		childCategoryEntity1.setEntity((Entity) entity2);
 
 		List<AttributeInterface> entity2AttributeCollection = new ArrayList<AttributeInterface>(entity2.getAttributeCollection());
 
 		CategoryAttributeInterface childCategoryEntity1CategoryAttribute1 = factory.createCategoryAttribute();
 		childCategoryEntity1CategoryAttribute1.setCreatedDate(new Date());
 		childCategoryEntity1CategoryAttribute1.setName("Attr 1");
 		childCategoryEntity1CategoryAttribute1.setAttribute(entity2AttributeCollection.get(0));
 		childCategoryEntity1.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity1CategoryAttribute1);
 
 		CategoryAttributeInterface childCategoryEntity1CategoryAttribute2 = factory.createCategoryAttribute();
 		childCategoryEntity1CategoryAttribute2.setCreatedDate(new Date());
 		childCategoryEntity1CategoryAttribute2.setName("Attr 2");
 		childCategoryEntity1CategoryAttribute2.setAttribute(entity2AttributeCollection.get(1));
 		childCategoryEntity1.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity1CategoryAttribute2);
 
 		// Create another category entity.
 		CategoryEntityInterface childCategoryEntity2 = factory.createCategoryEntity();
 		childCategoryEntity2.setCreatedDate(new Date());
 		childCategoryEntity2.setName("Child Category Entity 2");
 
 		EntityInterface entity3 = new MockEntityManager().initializeEntity(entityGroup);
 		entity3.setName("Entity 3");
 		childCategoryEntity2.setEntity((Entity) entity3);
 
 		List<AttributeInterface> entity3AttributeCollection = new ArrayList<AttributeInterface>(entity3.getAttributeCollection());
 
 		CategoryAttributeInterface childCategoryEntity2CategoryAttribute1 = factory.createCategoryAttribute();
 		childCategoryEntity2CategoryAttribute1.setCreatedDate(new Date());
 		childCategoryEntity2CategoryAttribute1.setName("Attr 1");
 		childCategoryEntity2CategoryAttribute1.setAttribute(entity3AttributeCollection.get(0));
 		childCategoryEntity2.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity2CategoryAttribute1);
 
 		CategoryAttributeInterface childCategoryEntity2CategoryAttribute2 = factory.createCategoryAttribute();
 		childCategoryEntity2CategoryAttribute2.setCreatedDate(new Date());
 		childCategoryEntity2CategoryAttribute2.setName("Attr 2");
 		childCategoryEntity2CategoryAttribute2.setAttribute(entity3AttributeCollection.get(1));
 		childCategoryEntity2.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity2CategoryAttribute2);
 
 		// Create another category entity.
 		CategoryEntityInterface childCategoryEntity3 = factory.createCategoryEntity();
 		childCategoryEntity3.setCreatedDate(new Date());
 		childCategoryEntity3.setName("Child Category Entity 3");
 
 		EntityInterface entity4 = new MockEntityManager().initializeEntity(entityGroup);
 		entity4.setName("Entity 4");
 		childCategoryEntity3.setEntity((Entity) entity4);
 
 		List<AttributeInterface> entity4AttributeCollection = new ArrayList<AttributeInterface>(entity4.getAttributeCollection());
 
 		CategoryAttributeInterface childCategoryEntity3CategoryAttribute1 = factory.createCategoryAttribute();
 		childCategoryEntity3CategoryAttribute1.setCreatedDate(new Date());
 		childCategoryEntity3CategoryAttribute1.setName("Attr 1");
 		childCategoryEntity3CategoryAttribute1.setAttribute(entity4AttributeCollection.get(0));
 		childCategoryEntity3.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity3CategoryAttribute1);
 
 		CategoryAttributeInterface childCategoryEntity3CategoryAttribute2 = factory.createCategoryAttribute();
 		childCategoryEntity3CategoryAttribute2.setCreatedDate(new Date());
 		childCategoryEntity3CategoryAttribute2.setName("Attr 2");
 		childCategoryEntity3CategoryAttribute2.setAttribute(entity4AttributeCollection.get(1));
 		childCategoryEntity3.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity3CategoryAttribute2);
 
 		// Add child category entities to root category entity.
 		rootCategoryEntity.getChildCategories().add((CategoryEntity) childCategoryEntity1);
 		rootCategoryEntity.getChildCategories().add((CategoryEntity) childCategoryEntity2);
 		rootCategoryEntity.getChildCategories().add((CategoryEntity) childCategoryEntity3);
 
 		// Set root category element of the category.
 		category.setRootCategoryElement((CategoryEntity) rootCategoryEntity);
 
 		return category;
 	}
 	
 	public CategoryInterface createCategoryWithCategoryAssociations() throws DynamicExtensionsApplicationException
 	{
 		EntityGroupInterface group = DomainObjectFactory.getInstance().createEntityGroup();
 		group.setName("Group 1");
 
 		// Study entity.
 		EntityInterface study = DomainObjectFactory.getInstance().createEntity();
 		study.setName("Study");
 
 		AbstractAttributeInterface studyAttribute1 = DomainObjectFactory.getInstance().createStringAttribute();
 		studyAttribute1.setName("Study Name");
 		studyAttribute1.setEntity(study);
 
 		AbstractAttributeInterface studyAttribute2 = DomainObjectFactory.getInstance().createStringAttribute();
 		studyAttribute2.setName("Study Type");
 		studyAttribute2.setEntity(study);
 
 		study.getAbstractAttributeCollection().add(studyAttribute1);
 		study.getAbstractAttributeCollection().add(studyAttribute2);
 
 		// Experiment entity.
 		EntityInterface experiment = DomainObjectFactory.getInstance().createEntity();
 		experiment.setName("Experiment");
 
 		AbstractAttributeInterface experimentAttribute1 = DomainObjectFactory.getInstance().createStringAttribute();
 		experimentAttribute1.setName("Experiment Name");
 		experimentAttribute1.setEntity(experiment);
 
 		AbstractAttributeInterface experimentAttribute2 = DomainObjectFactory.getInstance().createStringAttribute();
 		experimentAttribute2.setName("Experiment Type");
 		experimentAttribute2.setEntity(experiment);
 
 		experiment.getAbstractAttributeCollection().add(experimentAttribute1);
 		experiment.getAbstractAttributeCollection().add(experimentAttribute2);
 
 		// Study-Experiment association.
 		AssociationInterface studyExperimentAssociation = DomainObjectFactory.getInstance().createAssociation();
 		studyExperimentAssociation.setName("Study-Experiment association");
 		studyExperimentAssociation.setEntity(study);
 		studyExperimentAssociation.setTargetEntity(experiment);
 
 		study.getAbstractAttributeCollection().add(studyExperimentAssociation);
 
 		// Create a category.
 		CategoryInterface category = DomainObjectFactory.getInstance().createCategory();
 		category.setName("Category 1");
 
 		CategoryEntityInterface rootCategoryEntity = DomainObjectFactory.getInstance().createCategoryEntity();
 		rootCategoryEntity.setName("Study category entity");
 		rootCategoryEntity.setEntity(study);
 
 		List<AttributeInterface> studyAttributeCollection = new ArrayList<AttributeInterface>(study.getAttributeCollection());
 
 		CategoryAttributeInterface rootCategoryEntityAttribute1 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		rootCategoryEntityAttribute1.setName("Study Name");
 		rootCategoryEntityAttribute1.setAttribute(studyAttributeCollection.get(0));
 		rootCategoryEntityAttribute1.setCategoryEntity(rootCategoryEntity);
 		rootCategoryEntity.getCategoryAttributeCollection().add(rootCategoryEntityAttribute1);
 
 		CategoryAttributeInterface rootCategoryEntityAttribute2 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		rootCategoryEntityAttribute2.setName("Study Type");
 		rootCategoryEntityAttribute2.setAttribute(studyAttributeCollection.get(1));
 		rootCategoryEntityAttribute2.setCategoryEntity(rootCategoryEntity);
 		rootCategoryEntity.getCategoryAttributeCollection().add(rootCategoryEntityAttribute2);
 
 		CategoryAssociationInterface rootCategoryEntityAssociation = DomainObjectFactory.getInstance().createCategoryAssociation();
 		rootCategoryEntityAssociation.setName("Study Category Entity Association");
		rootCategoryEntityAssociation.setCategoryEntity((CategoryEntity) rootCategoryEntity);
 		rootCategoryEntity.setCategoryAssociation(rootCategoryEntityAssociation);		
 
 		CategoryEntityInterface childCategoryEntity = DomainObjectFactory.getInstance().createCategoryEntity();
 		childCategoryEntity.setName("Experiment category entity");
 		childCategoryEntity.setEntity(experiment);
 
 		List<AttributeInterface> experimentAttributeCollection = new ArrayList<AttributeInterface>(experiment.getAttributeCollection());
 
 		CategoryAttributeInterface childCategoryEntityAttribute1 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		childCategoryEntityAttribute1.setName("Experiment Name");
 		childCategoryEntityAttribute1.setAttribute(experimentAttributeCollection.get(0));
 		childCategoryEntityAttribute1.setCategoryEntity(childCategoryEntity);
 		childCategoryEntity.getCategoryAttributeCollection().add(childCategoryEntityAttribute1);
 
 		CategoryAttributeInterface childCategoryEntityAttribute2 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		childCategoryEntityAttribute2.setName("Experiment Type");
 		childCategoryEntityAttribute2.setAttribute(experimentAttributeCollection.get(1));
 		childCategoryEntityAttribute2.setCategoryEntity(childCategoryEntity);
 		childCategoryEntity.getCategoryAttributeCollection().add(childCategoryEntityAttribute2);
 		
 		CategoryAssociationInterface childCategoryEntityAssociation = DomainObjectFactory.getInstance().createCategoryAssociation();
 		childCategoryEntityAssociation.setName("Experiment Category Entity Association");
		childCategoryEntityAssociation.setCategoryEntity((CategoryEntity) childCategoryEntity);
 		childCategoryEntity.setCategoryAssociation(childCategoryEntityAssociation);	
 
 		// Add path information
 		PathInterface path = DomainObjectFactory.getInstance().createPath();
 		PathAssociationRelationInterface pathAssociationRelation = DomainObjectFactory.getInstance().createPathAssociationRelation();
 		pathAssociationRelation.setAssociation((Association) studyExperimentAssociation);
 		pathAssociationRelation.setPathSequenceNumber(1);
 		pathAssociationRelation.setPath((Path) path);
 
 		path.getPathAssociationRelationCollection().add(pathAssociationRelation);
 
 		childCategoryEntity.setPath(path);
 
 		rootCategoryEntity.getChildCategories().add(childCategoryEntity);
 
 		category.setRootCategoryElement((CategoryEntity) rootCategoryEntity);
 
 		return category;
 	}
 
 	/**
 	 * Create a new category entity with two new category attributes and add this category entity 
 	 * to the root category entity of the category.
 	 * @param category
 	 * @return category entity
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public CategoryEntityInterface addNewCategoryEntityToExistingCategory(CategoryInterface category) throws DynamicExtensionsApplicationException
 	{
 		CategoryEntityInterface childCategoryEntity4 = DomainObjectFactory.getInstance().createCategoryEntity();
 		childCategoryEntity4.setCreatedDate(new Date());
 		childCategoryEntity4.setName("Child Category Entity 4");
 
 		EntityInterface entity5 = new MockEntityManager().initializeEntity(category.getRootCategoryElement().getEntity().getEntityGroup());
 		entity5.setName("Entity 5");
 		childCategoryEntity4.setEntity((Entity) entity5);
 
 		List<AttributeInterface> entity5AttributeCollection = new ArrayList<AttributeInterface>(entity5.getAttributeCollection());
 
 		CategoryAttributeInterface childCategoryEntity4CategoryAttribute1 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		childCategoryEntity4CategoryAttribute1.setCreatedDate(new Date());
 		childCategoryEntity4CategoryAttribute1.setName("Attr 15");
 		childCategoryEntity4CategoryAttribute1.setAttribute(entity5AttributeCollection.get(0));
 		childCategoryEntity4.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity4CategoryAttribute1);
 
 		CategoryAttributeInterface childCategoryEntity4CategoryAttribute2 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		childCategoryEntity4CategoryAttribute2.setCreatedDate(new Date());
 		childCategoryEntity4CategoryAttribute2.setName("Attr 25");
 		childCategoryEntity4CategoryAttribute2.setAttribute(entity5AttributeCollection.get(1));
 		childCategoryEntity4.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity4CategoryAttribute2);
 
 		CategoryEntityInterface rootCategoryEntity = category.getRootCategoryElement();
 		rootCategoryEntity.getChildCategories().add(childCategoryEntity4);
 
 		return childCategoryEntity4;
 	}
 
 	/**
 	 * Create a category, create category entities from entities present in the database.
 	 * @return category
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public CategoryInterface createCategoryFromExistingEntities() throws DynamicExtensionsApplicationException
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		CategoryInterface category = factory.createCategory();
 		category.setName("Category 1");
 		category.setCreatedDate(new Date());
 
 		// Create root category entity.
 		CategoryEntityInterface rootCategoryEntity = factory.createCategoryEntity();
 		rootCategoryEntity.setCreatedDate(new Date());
 		rootCategoryEntity.setName("Root Category Entity");
 
 		EntityGroupInterface entityGroup = (EntityGroup) getObjectByIdentifier(EntityGroup.class.getName(), 1);
 
 		EntityInterface entity1 = entityGroup.getEntityByName("Entity One");
 		rootCategoryEntity.setEntity((Entity) entity1);
 
 		List<AttributeInterface> entity1AttributeCollection = new ArrayList<AttributeInterface>(entity1.getAttributeCollection());
 
 		CategoryAttributeInterface rootCategoryEntityCategoryAttribute1 = factory.createCategoryAttribute();
 		rootCategoryEntityCategoryAttribute1.setCreatedDate(new Date());
 		rootCategoryEntityCategoryAttribute1.setName("Category Attribute 1");
 		rootCategoryEntityCategoryAttribute1.setAttribute(entity1AttributeCollection.get(0));
 		rootCategoryEntity.getCategoryAttributeCollection().add((CategoryAttribute) rootCategoryEntityCategoryAttribute1);
 
 		CategoryAttributeInterface rootCategoryEntityCategoryAttribute2 = factory.createCategoryAttribute();
 		rootCategoryEntityCategoryAttribute2.setCreatedDate(new Date());
 		rootCategoryEntityCategoryAttribute2.setName("Category Attribute 2");
 		rootCategoryEntityCategoryAttribute2.setAttribute(entity1AttributeCollection.get(1));
 		rootCategoryEntity.getCategoryAttributeCollection().add((CategoryAttribute) rootCategoryEntityCategoryAttribute2);
 
 		// Create another category entity.
 		CategoryEntityInterface childCategoryEntity1 = factory.createCategoryEntity();
 		childCategoryEntity1.setCreatedDate(new Date());
 		childCategoryEntity1.setName("Child Category Entity 1");
 
 		EntityInterface entity2 = entityGroup.getEntityByName("Entity 2");
 		childCategoryEntity1.setEntity((Entity) entity2);
 
 		List<AttributeInterface> entity2AttributeCollection = new ArrayList<AttributeInterface>(entity2.getAttributeCollection());
 
 		CategoryAttributeInterface childCategoryEntity1CategoryAttribute1 = factory.createCategoryAttribute();
 		childCategoryEntity1CategoryAttribute1.setCreatedDate(new Date());
 		childCategoryEntity1CategoryAttribute1.setName("Attr 1");
 		childCategoryEntity1CategoryAttribute1.setAttribute(entity2AttributeCollection.get(0));
 		childCategoryEntity1.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity1CategoryAttribute1);
 
 		CategoryAttributeInterface childCategoryEntity1CategoryAttribute2 = factory.createCategoryAttribute();
 		childCategoryEntity1CategoryAttribute2.setCreatedDate(new Date());
 		childCategoryEntity1CategoryAttribute2.setName("Attr 2");
 		childCategoryEntity1CategoryAttribute2.setAttribute(entity2AttributeCollection.get(1));
 		childCategoryEntity1.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity1CategoryAttribute2);
 
 		// Create another category entity.
 		CategoryEntityInterface childCategoryEntity2 = factory.createCategoryEntity();
 		childCategoryEntity2.setCreatedDate(new Date());
 		childCategoryEntity2.setName("Child Category Entity 2");
 
 		EntityInterface entity3 = entityGroup.getEntityByName("Entity 3");
 		childCategoryEntity2.setEntity((Entity) entity3);
 
 		List<AttributeInterface> entity3AttributeCollection = new ArrayList<AttributeInterface>(entity3.getAttributeCollection());
 
 		CategoryAttributeInterface childCategoryEntity2CategoryAttribute1 = factory.createCategoryAttribute();
 		childCategoryEntity2CategoryAttribute1.setCreatedDate(new Date());
 		childCategoryEntity2CategoryAttribute1.setName("Attr 1");
 		childCategoryEntity2CategoryAttribute1.setAttribute(entity3AttributeCollection.get(0));
 		childCategoryEntity2.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity2CategoryAttribute1);
 
 		CategoryAttributeInterface childCategoryEntity2CategoryAttribute2 = factory.createCategoryAttribute();
 		childCategoryEntity2CategoryAttribute2.setCreatedDate(new Date());
 		childCategoryEntity2CategoryAttribute2.setName("Attr 2");
 		childCategoryEntity2CategoryAttribute2.setAttribute(entity3AttributeCollection.get(1));
 		childCategoryEntity2.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity2CategoryAttribute2);
 
 		// Create another category entity.
 		CategoryEntityInterface childCategoryEntity3 = factory.createCategoryEntity();
 		childCategoryEntity3.setCreatedDate(new Date());
 		childCategoryEntity3.setName("Child Category Entity 3");
 
 		EntityInterface entity4 = entityGroup.getEntityByName("Entity 4");
 		childCategoryEntity3.setEntity((Entity) entity4);
 
 		List<AttributeInterface> entity4AttributeCollection = new ArrayList<AttributeInterface>(entity4.getAttributeCollection());
 
 		CategoryAttributeInterface childCategoryEntity3CategoryAttribute1 = factory.createCategoryAttribute();
 		childCategoryEntity3CategoryAttribute1.setCreatedDate(new Date());
 		childCategoryEntity3CategoryAttribute1.setName("Attr 1");
 		childCategoryEntity3CategoryAttribute1.setAttribute(entity4AttributeCollection.get(0));
 		childCategoryEntity3.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity3CategoryAttribute1);
 
 		CategoryAttributeInterface childCategoryEntity3CategoryAttribute2 = factory.createCategoryAttribute();
 		childCategoryEntity3CategoryAttribute2.setCreatedDate(new Date());
 		childCategoryEntity3CategoryAttribute2.setName("Attr 2");
 		childCategoryEntity3CategoryAttribute2.setAttribute(entity4AttributeCollection.get(1));
 		childCategoryEntity3.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntity3CategoryAttribute2);
 
 		// Add child category entities to root category entity.
 		rootCategoryEntity.getChildCategories().add((CategoryEntity) childCategoryEntity1);
 		rootCategoryEntity.getChildCategories().add((CategoryEntity) childCategoryEntity2);
 		rootCategoryEntity.getChildCategories().add((CategoryEntity) childCategoryEntity3);
 
 		// Set root category element of the category.
 		category.setRootCategoryElement((CategoryEntity) rootCategoryEntity);
 
 		return category;
 	}
 
 	/**
 	 * Create a category with one root category entity, one child category entity and
 	 * two category attributes. Create multiple Paths, Associations and PathAssociationRelation objects with 
 	 * their appropriate metadata. Add the path information to the root category entity and save the category.
 	 * 
 	 * Three entities : Entity 1, Entity 2, Entity 3
 	 * 
 	 * 
 	 * 							Path :			Entity 1 ------> Entity 2 ------> Entity 3	
 	 * 
 	 * @return category
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public CategoryInterface createCategoryWithPath() throws DynamicExtensionsApplicationException
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("Entity Group " + new Double(Math.random()).toString());
 		entityGroup.setCreatedDate(new Date());
 		entityGroup.setDescription("This is a description for entity group");
 
 		CategoryInterface category = DomainObjectFactory.getInstance().createCategory();
 		category.setName("Category 1");
 		category.setCreatedDate(new Date());
 
 		// Create root category entity.
 		CategoryEntityInterface rootCategoryEntity = DomainObjectFactory.getInstance().createCategoryEntity();
 		rootCategoryEntity.setCreatedDate(new Date());
 		rootCategoryEntity.setName("Root Category Entity");
 
 		EntityInterface entity = new MockEntityManager().initializeEntity(entityGroup);
 		entity.setName("Entity 1");
 		rootCategoryEntity.setEntity((Entity) entity);
 
 		List<AttributeInterface> attributeCollection = new ArrayList<AttributeInterface>(entity.getAttributeCollection());
 
 		CategoryAttributeInterface rootCategoryAttribute1 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		rootCategoryAttribute1.setCreatedDate(new Date());
 		rootCategoryAttribute1.setName("Category Attribute 1");
 		rootCategoryAttribute1.setAttribute(attributeCollection.get(0));
 		rootCategoryEntity.getCategoryAttributeCollection().add((CategoryAttribute) rootCategoryAttribute1);
 
 		CategoryAttributeInterface rootCategoryAttribute2 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		rootCategoryAttribute2.setCreatedDate(new Date());
 		rootCategoryAttribute2.setName("Category Attribute 2");
 		rootCategoryAttribute2.setAttribute(attributeCollection.get(1));
 		rootCategoryEntity.getCategoryAttributeCollection().add((CategoryAttribute) rootCategoryAttribute2);
 
 		EntityInterface entityTwo = new MockEntityManager().initializeEntity(entityGroup);
 		entityTwo.setName("Entity 2");
 
 		// Create another category entity.
 		CategoryEntityInterface childCategoryEntity = DomainObjectFactory.getInstance().createCategoryEntity();
 		childCategoryEntity.setCreatedDate(new Date());
 		childCategoryEntity.setName("Child Category Entity");
 
 		EntityInterface entityThree = new MockEntityManager().initializeEntity(entityGroup);
 		entityThree.setName("Entity 3");
 		childCategoryEntity.setEntity((Entity) entityThree);
 
 		List<AttributeInterface> attributeCollection2 = new ArrayList<AttributeInterface>(entityThree.getAttributeCollection());
 
 		CategoryAttributeInterface childCategoryEntityCategoryAttribute1 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		childCategoryEntityCategoryAttribute1.setCreatedDate(new Date());
 		childCategoryEntityCategoryAttribute1.setName("Attr 1");
 		childCategoryEntityCategoryAttribute1.setAttribute(attributeCollection2.get(0));
 		childCategoryEntity.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntityCategoryAttribute1);
 
 		CategoryAttributeInterface childCategoryEntityCategoryAttribute2 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		childCategoryEntityCategoryAttribute2.setCreatedDate(new Date());
 		childCategoryEntityCategoryAttribute2.setName("Attr 2");
 		childCategoryEntityCategoryAttribute2.setAttribute(attributeCollection2.get(1));
 		childCategoryEntity.getCategoryAttributeCollection().add((CategoryAttribute) childCategoryEntityCategoryAttribute2);
 
 		// Create association between Entity 1 and Entity 2
 		AssociationInterface association1 = DomainObjectFactory.getInstance().createAssociation();
 		association1.setCreatedDate(new Date());
 		association1.setName("Association 1");
 		association1.setEntity(entity);
 		association1.setTargetEntity(entityTwo);
 
 		entity.getAbstractAttributeCollection().add(association1);
 
 		// Create association between Entity 2 and Entity 3
 		AssociationInterface association2 = DomainObjectFactory.getInstance().createAssociation();
 		association2.setCreatedDate(new Date());
 		association2.setName("Association 2");
 		association2.setEntity(entityTwo);
 		association2.setTargetEntity(entityThree);
 
 		entityTwo.getAbstractAttributeCollection().add(association2);
 
 		PathInterface path = DomainObjectFactory.getInstance().createPath();
 
 		PathAssociationRelationInterface pathAssociationRelation1 = DomainObjectFactory.getInstance().createPathAssociationRelation();
 		pathAssociationRelation1.setAssociation((Association) association1);
 		pathAssociationRelation1.setPath((Path) path);
 		pathAssociationRelation1.setPathSequenceNumber(1);
 
 		PathAssociationRelationInterface pathAssociationRelation2 = DomainObjectFactory.getInstance().createPathAssociationRelation();
 		pathAssociationRelation2.setAssociation((Association) association2);
 		pathAssociationRelation2.setPath((Path) path);
 		pathAssociationRelation2.setPathSequenceNumber(2);
 
 		path.getPathAssociationRelationCollection().add(pathAssociationRelation1);
 		path.getPathAssociationRelationCollection().add(pathAssociationRelation2);
 
 		// Add all paths to root category element.
 		rootCategoryEntity.setPath(path);
 
 		// Add child category entities to root category entity.
 		rootCategoryEntity.getChildCategories().add((CategoryEntity) childCategoryEntity);
 
 		// Set root category element of the category.
 		category.setRootCategoryElement((CategoryEntity) rootCategoryEntity);
 
 		return category;
 	}
 
 	/**
 	 * Create a category with one root category entity, one child category entity and
 	 * two category attributes. Create a Path, an Association and a PathAssociationRelation objects with 
 	 * their appropriate metadata. Add the path information to the root category entity and save the category.
 	 * @return category
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public CategoryInterface createCategoryWithPath1() throws DynamicExtensionsApplicationException
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("Entity Group " + new Double(Math.random()).toString());
 		entityGroup.setCreatedDate(new Date());
 		entityGroup.setDescription("This is a description for entity group");
 
 		CategoryInterface category = DomainObjectFactory.getInstance().createCategory();
 		category.setName("Category 1");
 		category.setCreatedDate(new Date());
 
 		// Create root category entity.
 		CategoryEntityInterface rootCategoryEntity = DomainObjectFactory.getInstance().createCategoryEntity();
 		rootCategoryEntity.setCreatedDate(new Date());
 		rootCategoryEntity.setName("Root Category Entity");
 
 		EntityInterface entity = new MockEntityManager().initializeEntity(entityGroup);
 		entity.setName("Entity 1");
 		rootCategoryEntity.setEntity((Entity) entity);
 
 		EntityInterface entityTwo = new MockEntityManager().initializeEntity(entityGroup);
 		entity.setName("Entity 2");
 
 		AssociationInterface association = DomainObjectFactory.getInstance().createAssociation();
 		association.setCreatedDate(new Date());
 		association.setName("Association 1");
 		association.setEntity(entity);
 		association.setTargetEntity(entityTwo);
 
 		entity.getAbstractAttributeCollection().add(association);
 
 		PathInterface path = DomainObjectFactory.getInstance().createPath();
 		PathAssociationRelationInterface pathAssociationRelation = DomainObjectFactory.getInstance().createPathAssociationRelation();
 		pathAssociationRelation.setAssociation((Association) association);
 		pathAssociationRelation.setPath((Path) path);
 		pathAssociationRelation.setPathSequenceNumber(1);
 
 		path.getPathAssociationRelationCollection().add(pathAssociationRelation);
 
 		rootCategoryEntity.setPath(path);
 
 		List<AttributeInterface> attributeCollection = new ArrayList<AttributeInterface>(entity.getAttributeCollection());
 
 		CategoryAttributeInterface categoryAttribute1 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		categoryAttribute1.setCreatedDate(new Date());
 		categoryAttribute1.setName("Category Attribute 1");
 		categoryAttribute1.setAttribute(attributeCollection.get(0));
 		rootCategoryEntity.getCategoryAttributeCollection().add((CategoryAttribute) categoryAttribute1);
 
 		CategoryAttributeInterface categoryAttribute2 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		categoryAttribute2.setCreatedDate(new Date());
 		categoryAttribute2.setName("Category Attribute 2");
 		categoryAttribute2.setAttribute(attributeCollection.get(1));
 		rootCategoryEntity.getCategoryAttributeCollection().add((CategoryAttribute) categoryAttribute2);
 
 		// Create another category entity.
 		CategoryEntityInterface categoryEntityA = DomainObjectFactory.getInstance().createCategoryEntity();
 		categoryEntityA.setCreatedDate(new Date());
 		categoryEntityA.setName("Child Category Entity 1");
 
 		EntityInterface entity2 = new MockEntityManager().initializeEntity(entityGroup);
 		entity2.setName("Entity 2");
 		categoryEntityA.setEntity((Entity) entity2);
 
 		List<AttributeInterface> attributeCollection2 = new ArrayList<AttributeInterface>(entity2.getAttributeCollection());
 
 		CategoryAttributeInterface categoryAttribute11 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		categoryAttribute11.setCreatedDate(new Date());
 		categoryAttribute11.setName("Attr 1");
 		categoryAttribute11.setAttribute(attributeCollection2.get(0));
 		categoryEntityA.getCategoryAttributeCollection().add((CategoryAttribute) categoryAttribute11);
 
 		CategoryAttributeInterface categoryAttribute22 = DomainObjectFactory.getInstance().createCategoryAttribute();
 		categoryAttribute22.setCreatedDate(new Date());
 		categoryAttribute22.setName("Attr 2");
 		categoryAttribute22.setAttribute(attributeCollection2.get(1));
 		categoryEntityA.getCategoryAttributeCollection().add((CategoryAttribute) categoryAttribute22);
 
 		// Add child category entities to root category entity.
 		rootCategoryEntity.getChildCategories().add((CategoryEntity) categoryEntityA);
 
 		// Set root category element of the category.
 		category.setRootCategoryElement((CategoryEntity) rootCategoryEntity);
 
 		return category;
 	}
 
 	protected AbstractMetadataInterface getObjectByIdentifier(String objectClassName, int identifier)
 	{
 		Object object = null;
 
 		DefaultBizLogic defaultBizLogic = BizLogicFactory.getDefaultBizLogic();
 		List objectList = new ArrayList();
 
 		try
 		{
 			objectList = defaultBizLogic.retrieve(objectClassName, "id", identifier);
 		}
 		catch (DAOException e)
 		{
 			e.printStackTrace();
 		}
 
 		if (objectList != null && objectList.size() > 0)
 		{
 			object = (DynamicExtensionBaseDomainObjectInterface) objectList.get(0);
 		}
 
 		return (AbstractMetadataInterface) object;
 	}
 
 }
