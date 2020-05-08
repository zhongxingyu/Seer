 package com.mpower.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.mpower.dao.PersonDao;
 import com.mpower.domain.CustomField;
 import com.mpower.domain.Person;
 import com.mpower.domain.customization.FieldDefinition;
 import com.mpower.domain.customization.FieldRelationship;
 import com.mpower.service.exception.PersonValidationException;
 import com.mpower.service.relationship.PersonTreeNode;
 import com.mpower.service.relationship.TooManyLevelsException;
 import com.mpower.type.FieldType;
 import com.mpower.type.RelationshipDirection;
 import com.mpower.type.RelationshipType;
 
 @Service("relationshipService")
 public class RelationshipServiceImpl implements RelationshipService {
 	
 	public static final int MAX_TREE_DEPTH = 200;
  
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name = "personDao")
     private PersonDao personDao;
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED, rollbackFor = PersonValidationException.class)
     public Person maintainRelationships(Person person) throws PersonValidationException {
     	
     	PersonValidationException ex = new PersonValidationException();
     	
     	Map<String, FieldDefinition> map = person.getFieldTypeMap();
     	for (Map.Entry<String, FieldDefinition> e: map.entrySet()) {
     		String key = e.getKey();
     		FieldDefinition fd = e.getValue();
     		boolean isReferenceTypeField = fd.getFieldType() == FieldType.QUERY_LOOKUP || fd.getFieldType() == FieldType.MULTI_QUERY_LOOKUP;
     		if (isReferenceTypeField && fd.isCustom()) {
     			
     			// Determine if there is a relationship defined with another field.
     			List<FieldRelationship> masters = fd.getSiteMasterFieldRelationships(person.getSite());
     			List<FieldRelationship> details = fd.getSiteDetailFieldRelationships(person.getSite());
     			if (masters.size() == 0 && details.size() == 0) continue;
 
     			String fieldlabel = fd.getDefaultLabel();
     			String customFieldName = fd.getCustomFieldName();
 
     			// Get old and new reference field value for comparison
     			String oldFieldValue = getOldFieldValue(person, key);
        			String newFieldValue = getNewFieldValue(person, key);  
        		    List<Long> oldids = getIds(oldFieldValue);
     			List<Long> newids = getIds(newFieldValue);
     			
     			if (!oldFieldValue.equals(newFieldValue)) {
  
 	       			logger.debug("Maintaining relationships for fieldname = " + key + ", oldids = " + oldFieldValue + ", newids = " + newFieldValue);
 	
 	    			// Setup for recursion checks
 	    			boolean needToCheckForRecursion = false;
 		   			for (FieldRelationship fr : masters) {
 		   				if (fr.isRecursive()) needToCheckForRecursion = true;
 		   			}
 		   			for (FieldRelationship fr : details) {
 		   				if (fr.isRecursive()) needToCheckForRecursion = true;
 		   			}
 		   			List<Long> descendants = new ArrayList<Long>();
 	    			
 	    			// Maintain the other related fields
 		   			for (FieldRelationship fr : masters) {
 			   			if (needToCheckForRecursion) {
 			   				descendants = new ArrayList<Long>();
 			   				getDescendantIds(descendants, person, fr.getMasterField().getCustomFieldName(), 0);
 			   			}
 	   			    	maintainRelationShip(fieldlabel, customFieldName, person, fr.getMasterField(), RelationshipDirection.MASTER, fr, oldids, newids, descendants, ex);
 	 	   			}
 		   			for (FieldRelationship fr : details) {
 	   			    	maintainRelationShip(fieldlabel, customFieldName, person, fr.getDetailField(), RelationshipDirection.DETAIL, fr, oldids, newids, descendants, ex);
 	 	   			}
 		   			
		   	    	if (logger.isDebugEnabled()) debugPrintTree(person, masters);
 		   			
     			}
 	   			
     		}
     	}
     	if (!ex.getValidationResults().isEmpty()) throw ex;
     	
         return person;
     }
     
     
     // Return the entire tree reachable from Person, using the "parent" field name (e.g. "organization.parent")
     @Override
     @Transactional(readOnly=true, propagation = Propagation.REQUIRED, rollbackFor = PersonValidationException.class)
 	public PersonTreeNode getEntireTree(Person person, String parentCustomFieldName) throws PersonValidationException {
     	
        	Map<String, FieldDefinition> map = person.getFieldTypeMap();
     	for (Map.Entry<String, FieldDefinition> e: map.entrySet()) {
     		FieldDefinition fd = e.getValue();
     		if (fd.getCustomFieldName().equals(parentCustomFieldName)) {
     			List<FieldRelationship> masters = fd.getSiteMasterFieldRelationships(person.getSite());
     			if (masters.size() == 0) return null;
     			FieldRelationship fr = masters.get(0);
     			if (fr.isRecursive()) {
         			String childCustomFieldName = fr.getMasterField().getCustomFieldName();
             		PersonTreeNode tree = getEntireTree(person, parentCustomFieldName, childCustomFieldName);
     				return tree;
     			}
     		}
     	}
     	return null;
 		
 	}
 
     
    private void debugPrintTree(Person person, List<FieldRelationship> masters) throws PersonValidationException {
    	for (FieldRelationship fr: masters) {
     		if (fr.isRecursive()) {
     			String parentCustomFieldName = fr.getDetailField().getCustomFieldName();
     			
     		   	PersonTreeNode tree = getEntireTree(person, parentCustomFieldName);
     	    	String result = debugPrintTree(tree);
     	    	logger.debug("Tree for "+parentCustomFieldName+":\r\n"+result);
     		   	PersonTreeNode thisnode = findPersonNodeInTree(person, tree);
        	    	result = debugPrintTree(thisnode);
     	    	logger.debug("Subtree for "+person.getDisplayValue()+":\r\n"+result);
     	    	Person head = getHeadOfTree(person, parentCustomFieldName);
     	    	logger.debug("Head of tree: "+head.getDisplayValue());
     	    	Person common = getFirstCommonAncestor(person, head, parentCustomFieldName);
     	    	logger.debug("Common ancestor: "+common.getDisplayValue());
     	    }
     	}
     }
     
     private String getOldFieldValue(Person person, String fieldname) {
 		String oldFieldValue = (String)person.getFieldValueMap().get(fieldname);  
 		return (oldFieldValue == null) ? "" : oldFieldValue;
     }
     
     private String getNewFieldValue(Person person, String fieldname) {
     	BeanWrapperImpl bean = new BeanWrapperImpl(person);
     	String result = (String) bean.getPropertyValue(fieldname + ".value");
     	return (result == null) ? "" : result;
     }
     
     private List<Long> getIds(String fieldValue) {
 		List<Long> ids = new ArrayList<Long>();
 		if (fieldValue == null) return ids;
 		String[] sids = fieldValue.split(",");
 		for (String sid: sids) {
 			if (sid.length() > 0) {
 		   	   Long id = new Long(sid); 
 			   ids.add(id);
 			}
 		}
         return ids;
     }
     
     private String getIdString(List<Long> ids) {
     	StringBuilder sb = new StringBuilder();
     	for (Long id: ids) {
     		if (sb.length() > 0) sb.append(",");
     		sb.append(id);
     	}
     	return sb.toString();
     }
     
 	private void maintainRelationShip(String thisFieldLabel,
 			String customFieldName, 
 			Person person, 
 			FieldDefinition otherField,
 			RelationshipDirection direction,
 			FieldRelationship fieldRelationship, 
 			List<Long> oldIds,
 			List<Long> newIds, 
 			List<Long> checkDescendents,
 			PersonValidationException ex) throws PersonValidationException { 
 
         logger.debug("maintainRelationShip() called for " + otherField.getFieldName() + ", " + direction + ", " + fieldRelationship.getRelationshipType() + ", recursive=" + fieldRelationship.isRecursive());	
         
         if (!otherField.isCustom()) { 
         	logger.error("Field Id specified in relationship is not a custom field: " + otherField.getId());
         	return;
         }
 
         // Check if this field can be multi-valued per this relationship.
 		boolean thisCanBeMultiValued = thisCanBeMultiValued(direction, fieldRelationship.getRelationshipType());
 		boolean thisIsMultiValued = newIds.size() > 1;
 		if (!thisCanBeMultiValued && thisIsMultiValued) {
 			String thisFieldTooManyValues = "Value for " + thisFieldLabel + " can only have one selected value.";
 			ex.addValidationResult(thisFieldTooManyValues);
 			return;
 		}
 
 		// Get merged list of all ids, both adds and deletes.
         List<Long> allids = new ArrayList<Long>();
         allids.addAll(oldIds);
         for (Long id : newIds) if (!allids.contains(id)) allids.add(id);
         
 		// Check other person's related field for deletion or addition of a reference to this id.
 		boolean otherCanBeMultiValued = otherCanBeMultiValued(direction, fieldRelationship.getRelationshipType());
         Long thisId = person.getId();
 		String otherfieldname = otherField.getCustomFieldName();  
 		
 		List<Person> otherPersons = personDao.readPersons(person.getSite().getName(), allids);
 		for (Person otherPerson : otherPersons) {
 			
 			Long otherId = otherPerson.getId();
 			
 			// Check for recursion loops
 			if (fieldRelationship.isRecursive() && newIds.contains(otherId)) {
 				// Self reference
 				if (thisId.equals(otherId)) {
 					ex.addValidationResult("fieldSelfReference", new Object[]{thisFieldLabel});
 					continue;
 				}
 				if (thisCanBeMultiValued) {
 					// Attempt to add an ancestor as a child
 					List<Long> descendants = new ArrayList<Long>();
 					getDescendantIds(descendants, otherPerson, customFieldName, 0);
 					if (descendants.contains(thisId)) {
 						ex.addValidationResult("childReferenceError", new Object[]{thisFieldLabel});
 						continue;
 					}
 				} else {
 					// Attempt to set parent to a descendant
 					if (checkDescendents.contains(otherId)) {
 						ex.addValidationResult("parentReferenceError", new Object[]{thisFieldLabel});
 						continue;
 					}
 				}
 			}
 			
 			// Check for additions or deletions
 			CustomField otherCustomField = otherPerson.getCustomFieldMap().get(otherfieldname);
 			String otherCustomFieldValue = otherCustomField.getValue();
 			List<Long> otherFieldIds = getIds(otherCustomFieldValue);
 			boolean found = otherFieldIds.contains(thisId);
 			boolean shouldBeFound = newIds.contains(otherId);
 			
 			// Maintain reverse references
 			boolean needToPersist = false;
 			if (found && !shouldBeFound) {
 				otherFieldIds.remove(thisId);
 				needToPersist = true;
 			} else if (!found && shouldBeFound) {
 				if (!otherCanBeMultiValued) otherFieldIds.clear();
 				otherFieldIds.add(thisId);
 				needToPersist = true;
 			}
 			
 			if (needToPersist) {
 				String newOtherFieldValue = getIdString(otherFieldIds);
 				logger.debug("Updating related field "+otherCustomField.getName()+" value on "+otherPerson.getDisplayValue() + " to " + newOtherFieldValue);
 				otherCustomField.setValue(newOtherFieldValue);
 				personDao.savePerson(otherPerson); 
 			}
 			
 		}
 
 	}
 
 	// Field must be a detail (child list) custom field.
 	private void getDescendantIds(List<Long> list, Person person, String customFieldName, int level) throws PersonValidationException {
 		
		if (level > MAX_TREE_DEPTH) throw new TooManyLevelsException(customFieldName);
 
 		List<Person> referencedPersons = getPersons(person, customFieldName);
 		for (Person referencedPerson : referencedPersons) {
 			Long referencedId = referencedPerson.getId();
 			if (list.contains(referencedId)) {
 				// This will normally happen if they place a child over a parent or vice-versa.  It is caught in the recursion check logic when the item causing the loop is edited.
 				String recursionLoop = "Relationship tree for "+customFieldName+" forms a loop due to reference from " + person.getDisplayValue() + " to " + referencedPerson.getDisplayValue();
 				logger.debug(recursionLoop);
 				return;
 			}
 			list.add(referencedId);
 			getDescendantIds(list, referencedPerson, customFieldName, level + 1);
 		}
 		
 	}
 	
 	// Returns list of person Ids in the reference type custom field.
 	private List<Long> getIdList(Person person, String customFieldName) {
 		
 		CustomField customField = person.getCustomFieldMap().get(customFieldName);
 		String customFieldValue = customField.getValue();
 		List<Long> ids = getIds(customFieldValue);
         return ids;		
         
 	}
 	
 	// Returns list of Person objects referenced by the custom field
 	private List<Person> getPersons(Person person, String customFieldName) {
 		
 		List<Person> persons = personDao.readPersons(person.getSite().getName(), getIdList(person, customFieldName));
 		return persons;
 		
 	}
 	
 	// Person is any member of the tree.  Returns the entire tree based on the recursive relationship defined by the custom fields.
 	public PersonTreeNode getEntireTree(Person person, String parentCustomFieldName, String childCustomFieldName) throws PersonValidationException {
 		person = getHeadOfTree(person, parentCustomFieldName);
 		PersonTreeNode personNode = new PersonTreeNode(person, 0);
 		getSubTree(personNode, childCustomFieldName);
 		return personNode;
 	}
 	
 	// Field must be a master (parent id) custom field.
 	public Person getHeadOfTree(Person person, String parentCustomFieldName) throws PersonValidationException {
 	
 		int level = 0;
 		while (true) {
 			level++;
			if (level > MAX_TREE_DEPTH) throw new TooManyLevelsException(parentCustomFieldName);
 			List<Person> referencedPersons = getPersons(person, parentCustomFieldName);
 			if (referencedPersons.size() == 0) return person;
 			person = referencedPersons.get(0);
 		}
 
 	}
 	
 	public void getSubTree(PersonTreeNode personNode, String childCustomFieldName) throws PersonValidationException {
 		
		if (personNode.getLevel() > MAX_TREE_DEPTH) throw new TooManyLevelsException(childCustomFieldName);
 
 		List<Person> referencedPersons = getPersons(personNode.getPerson(), childCustomFieldName);
 		for (Person referencedPerson : referencedPersons) {
 			PersonTreeNode child = new PersonTreeNode(referencedPerson, personNode.getLevel() + 1);
 			personNode.getChildren().add(child);
 			getSubTree(child, childCustomFieldName);
 		}
 		
 	}
 	
 	// If person is in tree, return the node, otherwise return null.
 	public PersonTreeNode findPersonNodeInTree(Person person, PersonTreeNode tree) {
 		if (equals(person, tree.getPerson())) return tree;
 	    for (PersonTreeNode personNode: tree.getChildren()) {
 	    	PersonTreeNode node = findPersonNodeInTree(person, personNode);
 	    	if (node != null) return node;
 	    }
 	    return null;
 	}
 
 	public Person getFirstCommonAncestor(Person p1, Person p2, String parentCustomFieldName) throws PersonValidationException {
 		
 		List<Person> p1Parents = new ArrayList<Person>();
 		p1Parents.add(p1);
 		List<Person> p2Parents = new ArrayList<Person>();
 		p2Parents.add(p2);
 		int level = 0;
 		while (true) {
 			level++;
			if (level > MAX_TREE_DEPTH) throw new TooManyLevelsException(parentCustomFieldName);
 			List<Person> referencedPersons1 = getPersons(p1, parentCustomFieldName);
 			List<Person> referencedPersons2 = getPersons(p2, parentCustomFieldName);
 			if (referencedPersons1.size() == 0 && referencedPersons2.size() == 0) return null;
 			if (referencedPersons1.size() > 0) p1 = referencedPersons1.get(0);
 			if (referencedPersons2.size() > 0) p2 = referencedPersons2.get(0);
 			if (contains(p1Parents, p2)) return p2;
 			if (contains(p2Parents, p1)) return p1;
 		}
 		
 	}
 	
 	public String debugPrintTree(PersonTreeNode tree) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < tree.getLevel(); i++) sb.append("\t");
 		sb.append(tree.getPerson().getDisplayValue()).append("\r\n");
 		for (PersonTreeNode child: tree.getChildren()) sb.append(debugPrintTree(child));
 		return sb.toString();
 	}
 	
 	private boolean contains(List<Person> persons, Person person) {
 		for (Person aperson: persons) if (equals(person, aperson)) return true;
 		return false;
 	}
 
 	// Don't want to add an equals method to the JPA object
 	private boolean equals(Person p1, Person p2) {
 		if (p1 == null && p2 == null) return true;
 		if (p1 == null || p2 == null) return false;
 		return p1.getId().equals(p2.getId());
 	}
 	
 	private boolean thisCanBeMultiValued(RelationshipDirection direction, RelationshipType fieldRelationshipType) {
 		return fieldRelationshipType.equals(RelationshipType.MANY_TO_MANY) ||
 		( fieldRelationshipType.equals(RelationshipType.ONE_TO_MANY) && direction.equals(RelationshipDirection.DETAIL) );
 	}
 	
 	private boolean otherCanBeMultiValued(RelationshipDirection direction, RelationshipType fieldRelationshipType) {
 		return fieldRelationshipType.equals(RelationshipType.MANY_TO_MANY) ||
 		( fieldRelationshipType.equals(RelationshipType.ONE_TO_MANY) && direction.equals(RelationshipDirection.MASTER) );
 	}
     
 }
