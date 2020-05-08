 /*
  * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
  * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
  * triple-shield Mayo logo are trademarks and service marks of MFMER.
  *
  * Except as contained in the copyright notice above, or as used to identify 
  * MFMER as the author of this software, the trade names, trademarks, service
  * marks, or product names of the copyright holder shall not be used in
  * advertising, promotion or otherwise in connection with this software without
  * prior written authorization of the copyright holder.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.mayo.cts2.framework.plugin.service.bioportal.transform;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.google.common.collect.Iterables;
 
 import edu.mayo.cts2.framework.model.core.Definition;
 import edu.mayo.cts2.framework.model.core.PredicateReference;
 import edu.mayo.cts2.framework.model.core.Property;
 import edu.mayo.cts2.framework.model.core.ScopedEntityName;
 import edu.mayo.cts2.framework.model.core.StatementTarget;
 import edu.mayo.cts2.framework.model.core.URIAndEntityName;
 import edu.mayo.cts2.framework.model.directory.DirectoryResult;
 import edu.mayo.cts2.framework.model.entity.Designation;
 import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
 import edu.mayo.cts2.framework.model.entity.NamedEntityDescription;
 import edu.mayo.cts2.framework.model.entity.NamedIndividualDescription;
 import edu.mayo.cts2.framework.model.entity.PredicateDescription;
 import edu.mayo.cts2.framework.model.entity.types.DesignationRole;
 import edu.mayo.cts2.framework.model.util.ModelUtils;
 import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
 
 /**
  * The Class EntityDescriptionTransform.
  *
  * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
  */
 @Component
 public class EntityDescriptionTransform extends AbstractTransform {
 	
 	private static Log log = LogFactory.getLog(EntityDescriptionTransform.class);
 
 	private static String CLASS = "class";
 	private static String INDIVIDUAL = "individual";
 	private static String PREDICATE = "property";
 	
 	private final static String ABOUT = "fullId";
 	private final static String NAME = "id";
 	private final static String LABEL = "label";
 	private final static String TYPE = "type";
 	private final static String NODE = "success.data.classBean";
 	private final static String NODELIST = "success.data.page.contents.classBeanResultList.classBean";
 	private final static String SEARCH_NODELIST = "success.data.page.contents.searchResultList.searchBean";
 	
 	private final static String SKOS_CONCEPT_NAME = "Concept";
 	private final static String SKOS_URI = "http://www.w3.org/2004/02/skos/core#";
 	private final static String SKOS_NAMESPACE = "skos";
 	private static final String PARENT_PREDICATE = "SuperClass";
 	private static final String CHILD_COUNT = "ChildCount";
 
 	@Resource
 	private AssociationTransform associationTransform;
 	
 	/**
 	 * Transform entity description.
 	 *
 	 * @param xml the xml
 	 * @param codeSystemName the code system name
 	 * @param codeSystemVersionName the code system version name
 	 * @return the named entity description
 	 */
 	public NamedEntityDescription transformEntityDescription(
 			String xml,
 			String codeSystemName,
 			String codeSystemVersionName) {
 		
 		Document doc = BioportalRestUtils.getDocument(xml);
 
 		Node node = TransformUtils.getNamedChildWithPath(doc, NODE);
 		
 		String type = TransformUtils.getNamedChildText(node, TYPE);
 		String about = TransformUtils.getNamedChildText(node, ABOUT);
 		String name = TransformUtils.getNamedChildText(node, NAME);
 		name= name.replaceFirst(":", "_");
 		String label = TransformUtils.getNamedChildText(node, LABEL);
 		
 		NamedEntityDescription entity = this.createNamedEntityDescription(type);
 		
 		entity.setAbout(about);
 		
 		entity.setEntityID(
 				buildScopedEntityName(name, codeSystemName));
 		String version= this.getIdentityConverter().codeSystemVersionNameToVersion(codeSystemVersionName);
 		
 		Designation designation = new Designation();
 		designation.setValue(ModelUtils.toTsAnyType(label));
 		designation.setDesignationRole(DesignationRole.PREFERRED);
 		//designation.setAssertedInCodeSystemVersion(this.buildCodeSystemVersionReference(codeSystemName, codeSystemVersionName));
 		
 		entity.addDesignation(designation);
 	
 		entity.setProperty(this.transformEntityProperties(codeSystemName, node));
 		
 		entity.setDefinition(this.transformEntityDefinitions(codeSystemName, codeSystemVersionName, node));
 		
 		for(Designation synonym : this.transformEntitySynonyms(codeSystemName, codeSystemVersionName, node)){
 			entity.addDesignation(synonym);
 		}
 		
 		entity.setDescribingCodeSystemVersion(this.buildCodeSystemVersionReference(codeSystemName, codeSystemVersionName));
 		
 		int children = this.getChildCount(node);
 		if(children != 0){
 			entity.setChildren(
 				this.getUrlConstructor().createChildrenUrl(codeSystemName, version, name));
		} else {
			entity.setChildren("");
 		}
 		
 		entity.setSubjectOf(this.getUrlConstructor().createSourceUrl(codeSystemName, version, name));
 		
 		entity.addEntityType(this.getEntityType(type));
 		
 		entity.setParent(associationTransform.transformURIAndEntityNameForRelationships(xml, codeSystemName, codeSystemVersionName, PARENT_PREDICATE));
 
 		return entity;
 	}
 	
 	/**
 	 * Creates the named entity description.
 	 *
 	 * @param type the type
 	 * @return the named entity description
 	 */
 	private NamedEntityDescription createNamedEntityDescription(String type){
 		if(StringUtils.isBlank(type)){
 			return new NamedEntityDescription();
 		} else if(type.equals(CLASS)){
 			return new NamedEntityDescription();
 		} else if(type.equals(INDIVIDUAL)){
 			return new NamedIndividualDescription();
 		} else if(type.equals(PREDICATE)){
 			return new PredicateDescription();
 		}
 		
 		log.warn("Unexpected type: " + type);
 		return new NamedEntityDescription();
 	}
 	
 	
 	//TODO: determine what we want to put here... there should be global
 	//CTS2 standards.
 	/**
 	 * Gets the entity type.
 	 *
 	 * @param type the type
 	 * @return the entity type
 	 */
 	private URIAndEntityName getEntityType(String type){
 		URIAndEntityName name = new URIAndEntityName();
 		name.setName(SKOS_CONCEPT_NAME);
 		name.setNamespace(SKOS_NAMESPACE);
 		name.setUri(SKOS_URI);
 		
 		return name;
 	}
 
 	/**
 	 * Transform entity definitions.
 	 *
 	 * @param codeSystemName the code system name
 	 * @param codeSystemVersionName the code system version name
 	 * @param node the node
 	 * @return the definition[]
 	 */
 	private Definition[] transformEntityDefinitions(String codeSystemName, String codeSystemVersionName, Node node) {
 		List<Definition> returnList = new ArrayList<Definition>();
 		
 		Node definitions = TransformUtils.getNamedChild(node, "definitions");
 		
 		if(definitions != null){
 		
 			for(Node definition : TransformUtils.getNodeList(definitions, "string")){
 				Definition def = new Definition();
 				def.setValue(ModelUtils.toTsAnyType(TransformUtils.getNodeText(definition)));
 				def.setAssertedInCodeSystemVersion(codeSystemVersionName);
 			
 				returnList.add(def);
 			}
 		}
 		
 		return Iterables.toArray(returnList, Definition.class);
 	}
 	
 	/**
 	 * Transform entity synonyms.
 	 *
 	 * @param codeSystemName the code system name
 	 * @param codeSystemVersionName the code system version name
 	 * @param node the node
 	 * @return the designation[]
 	 */
 	private Designation[] transformEntitySynonyms(String codeSystemName, String codeSystemVersionName, Node node) {
 		List<Designation> returnList = new ArrayList<Designation>();
 		
 		Node synonyms = TransformUtils.getNamedChild(node, "synonyms");
 		
 		if(synonyms != null){
 			
 			for(Node synonym : TransformUtils.getNodeList(synonyms, "string")){
 				Designation designation = new Designation();
 				designation.setValue(ModelUtils.toTsAnyType(TransformUtils.getNodeText(synonym)));
 				designation.setDesignationRole(DesignationRole.ALTERNATIVE);
 				//designation.setAssertedInCodeSystemVersion(
 				//		this.buildCodeSystemVersionReference(codeSystemName, codeSystemVersionName));
 				returnList.add(designation);
 			}
 		}
 		
 		return Iterables.toArray(returnList, Designation.class);
 		
 	}
 	
 	/**
 	 * Transform entity properties.
 	 *
 	 * @param codeSystemName the code system name
 	 * @param node the node
 	 * @return the property[]
 	 */
 	private Property[] transformEntityProperties(String codeSystemName, Node node) {
 		List<Property> returnList = new ArrayList<Property>();
 
 		List<Node> propertyNodes = TransformUtils.getNodeListWithPath(node,
 		"relations.entry");
 	
 		for(Node property : propertyNodes){
 			List<Node> entryList = TransformUtils.getNodeList(
 					property, "list");
 			
 			String predicateName = 
 				TransformUtils.getNamedChildText(property, "string");
 			
 				for(Node entry : entryList){ 
 					
 					for(Node propertyValue : 
 						TransformUtils.getNodeList(entry, "string")){
 						Property prop = new Property();
 											
 						String value = 
 							TransformUtils.getNodeText(propertyValue);
 						
 						PredicateReference predicateRef = new PredicateReference();
 						predicateRef.setName(predicateName);
 						predicateRef.setNamespace(codeSystemName);
 						
 						prop.setPredicate(predicateRef);
 						
 						StatementTarget target = new StatementTarget();
 						target.setLiteral(ModelUtils.createOpaqueData(value));
 						
 						prop.addValue(target);
 						
 						returnList.add(prop);
 					}
 				}
 			}
 
 		return Iterables.toArray(returnList, Property.class);
 	}
 	
 	public int getChildCount(Node node){
 		for(Node entryNode : TransformUtils.getNodeListWithPath(node, "relations.entry")){
 			String string = TransformUtils.getNamedChildText(entryNode, "string");
 			if(string.equals(CHILD_COUNT)){
 				return Integer.parseInt(TransformUtils.getNamedChildText(entryNode, "int"));
 			}
 		}
 		
 		return 0;
 	}
 
 	/**
 	 * Transform entity directory.
 	 *
 	 * @param xml the xml
 	 * @param codeSystemName the code system name
 	 * @param codeSystemVersionName the code system version name
 	 * @return the directory result
 	 */
 	public DirectoryResult<EntityDirectoryEntry>  transformEntityDirectory(
 			String xml, 
 			String codeSystemName,
 			String codeSystemVersionName) {
 		List<EntityDirectoryEntry> entryList = new ArrayList<EntityDirectoryEntry>();
 	
 		Document doc = BioportalRestUtils.getDocument(xml);
 		
 		List<Node> nodeList = TransformUtils.getNodeListWithPath(doc, NODELIST);
 		
 		for(int i=0;i<nodeList.size();i++){
 			EntityDirectoryEntry entry = new EntityDirectoryEntry();
 		
 			Node node = nodeList.get(i);
 		
 			String about = TransformUtils.getNamedChildText(node, ABOUT);
 			String name = TransformUtils.getNamedChildText(node, NAME);
 			name= name.replaceFirst(":", "_");
 			String label = TransformUtils.getNamedChildText(node, LABEL);
 			
 			entry.setAbout(about);
 			
 			ScopedEntityName scopedEntityName = this.buildScopedEntityName(name, codeSystemName);
 			entry.setName(scopedEntityName);
 			
 			entry.setResourceName(this.buildResourceName(scopedEntityName));
 			String version= this.getIdentityConverter().codeSystemVersionNameToVersion(codeSystemVersionName);
 			entry.setHref(this.getUrlConstructor().createEntityUrl(codeSystemName, version, name));
 
 			entry.addKnownEntityDescription(this.createKnownEntityDescription(
 					codeSystemName, 
 					codeSystemVersionName, 
 					label));
 			
 			entryList.add(entry);
 			
 		}
 		
 		int totalCount = TransformUtils.getTotalCount(xml);
 		int page = TransformUtils.getPageNumber(xml);
 		int pageSize = TransformUtils.getPageSize(xml);
 		int numOfResultsOnPage = TransformUtils.getNumberOfResultsOnPage(xml);
 	
 		boolean atEnd = totalCount <= ( ( (page - 1) * pageSize) + numOfResultsOnPage);
 		
 		return new DirectoryResult<EntityDirectoryEntry>(entryList, atEnd);
 	}
 	
 
 	/**
 	 * Builds the resource name.
 	 *
 	 * @param scopedEntityName the scoped entity name
 	 * @return the string
 	 */
 	private String buildResourceName(ScopedEntityName scopedEntityName) {
 		return scopedEntityName.getNamespace() + ":" + scopedEntityName.getName();
 	}
 
 	/**
 	 * Transform entity directory from search.
 	 *
 	 * @param start the start
 	 * @param end the end
 	 * @param xml the xml
 	 * @return the directory result
 	 */
 	public DirectoryResult<EntityDirectoryEntry> transformEntityDirectoryFromSearch(
 			int start,
 			int end,
 			String xml) {
 		List<EntityDirectoryEntry> entryList = new ArrayList<EntityDirectoryEntry>();
 		
 		long time = System.currentTimeMillis();
 
 		long time2 = System.currentTimeMillis();
 		
 		Document doc = BioportalRestUtils.getDocument(xml);
 		
 		List<Node> nodeList = TransformUtils.getNodeListWithPath(doc, SEARCH_NODELIST);
 		log.debug("transformEntityDirectoryFromSearch" + (System.currentTimeMillis() - time2) + " ms inner");
 
 		int skipped = 0;
 
 		for(int i = start; i<nodeList.size() && i <= end; i++){
 
 			EntityDirectoryEntry entry = new EntityDirectoryEntry();
 
 			Node node = nodeList.get(i);
 
 			String conceptIdKey = "conceptId";
 			String conceptIdShortKey = "conceptIdShort";
 			String preferredNameKey = "preferredName";
 			String ontologyIdKey = "ontologyId";
 			String ontologyVersionIdKey = "ontologyVersionId";
 
 			Map<String, String> resultMap = this
 					.getChildrenTextMap(node, conceptIdKey, conceptIdShortKey,
 							preferredNameKey, ontologyIdKey, ontologyVersionIdKey);
 
 			String about = resultMap.get(conceptIdKey);
 			String name = resultMap.get(conceptIdShortKey);
 			name= name.replaceFirst(":", "_");
 			String label = resultMap.get(preferredNameKey);
 			String ontologyId = resultMap.get(ontologyIdKey);
 			String ontologyVersionId = resultMap.get(ontologyVersionIdKey);
 
 			String codeSystemName;
 			try {
 				codeSystemName = this.getIdentityConverter()
 						.ontologyIdToCodeSystemName(ontologyId);
 			} catch (Exception e) {
 				//it seems there are invalid ontologyIds from time to time 
 				//1634 for example. Warn and continue.
 				log.warn(e);
 				continue;
 			}
 			
 			String codeSystemVersionName;
 			String version="";
 			
 			//if there is no codesystemname, it must be matching a view, so throw it out
 			if(StringUtils.isBlank(codeSystemName)){
 				log.warn("Result matched ontologyVersionId: " + ontologyVersionId + ", which is a view.");
 				codeSystemName = 
 					this.getIdentityConverter().ontologyIdToValueSetName(ontologyId);
 				
 				codeSystemVersionName = this.getIdentityConverter().ontologyVersionIdToValueSetDefinitionName(ontologyId, ontologyVersionId);
 
 			} else {
 				codeSystemVersionName = this.getIdentityConverter().ontologyVersionIdToCodeSystemVersionName(ontologyId, ontologyVersionId);
 				version= this.getIdentityConverter().codeSystemVersionNameToVersion(codeSystemVersionName);
 				entry.addKnownEntityDescription(this.createKnownEntityDescription(
 						codeSystemName, 
 						codeSystemVersionName, 
 						label));
 			}
 		
 			entry.setAbout(about);
 			
 			entry.setName(this.buildScopedEntityName(name, codeSystemName));
 			
 			entry.setHref(this.getUrlConstructor().createEntityUrl(
 					codeSystemName, 
 					version, 
 					name));
 			
 			entryList.add(entry);
 		}
 		
 		log.debug("transformEntityDirectoryFromSearch" + (System.currentTimeMillis() - time) + " ms outer");
 		
 		int totalCount = TransformUtils.getTotalCount(xml) - skipped;
 		int page = TransformUtils.getPageNumber(xml);
 		int pageSize = TransformUtils.getPageSize(xml);
 		int numOfResultsOnPage = TransformUtils.getNumberOfResultsOnPage(xml);
 	
 		boolean atEnd = totalCount <= ( ( (page - 1) * pageSize) + numOfResultsOnPage);
 	
 		return new DirectoryResult<EntityDirectoryEntry>(entryList, atEnd);
 	}
 	
 	/**
 	 * Gets the children text map.
 	 *
 	 * @param parentNode the parent node
 	 * @param children the children
 	 * @return the children text map
 	 */
 	public Map<String,String> getChildrenTextMap(Node parentNode, String... children){
 		Set<String> childrenSet = new HashSet<String>(Arrays.asList(children));
 		
 		Map<String,String> returnMap = new HashMap<String,String>();
 		
 		NodeList childList = parentNode.getChildNodes();
 		
 		for(int i=0;i<childList.getLength();i++){
 			Node child = childList.item(i);
 			
 			String nodeName = child.getNodeName();
 			
 			if(childrenSet.contains(nodeName)){
 				returnMap.put(nodeName, TransformUtils.getNodeText(child));
 			}
 		}
 		return returnMap;
 	}
 
 	public AssociationTransform getAssociationTransform() {
 		return associationTransform;
 	}
 
 	public void setAssociationTransform(AssociationTransform associationTransform) {
 		this.associationTransform = associationTransform;
 	}
 }
