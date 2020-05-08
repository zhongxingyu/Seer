 /*
  * Copyright 2012 jccastrejon
  *  
  * This file is part of ExSchema.
  *
  * ExSchema is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * ExSchema is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with ExSchema. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.imag.exschema.neo4j;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.NormalAnnotation;
 import org.eclipse.jdt.core.dom.ParameterizedType;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.Type;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 
 import fr.imag.exschema.SchemaFinder;
 import fr.imag.exschema.Util;
 import fr.imag.exschema.model.Attribute;
 import fr.imag.exschema.model.Relationship;
 import fr.imag.exschema.model.Set;
 import fr.imag.exschema.model.Struct;
 
 /**
  * Neo4j utility methods.
  * 
  * @author jccastrejon
  * 
  */
 public class Neo4jUtil implements SchemaFinder {
 
     /**
      * Class logger.
      */
     private static Logger logger = Logger.getLogger(Neo4jUtil.class.getName());
 
     @Override
     public List<Set> discoverSchemas(final IJavaProject project) throws JavaModelException {
         List<Set> returnValue;
 
         returnValue = Neo4jUtil.discoverNodeEntitiesSchemas(project);
         returnValue.addAll(Neo4jUtil.discoverNodesSchemas(project));
 
         return returnValue;
     }
 
     /**
      * Discover Spring's NodeEntities.
      * 
      * @param project
      * @throws JavaModelException
      */
     private static List<Set> discoverNodeEntitiesSchemas(final IJavaProject project) throws JavaModelException {
         Set currentGraph;
         Struct currentNode;
         Struct currentFields;
         List<Set> returnValue;
         String relationshipName;
         Type relationshipEndType;
         NodeEntityVisitor entityVisitor;
         List<Relationship> relationships;
         Relationship currentRelationship;
 
         // Identify node entities
         returnValue = new ArrayList<Set>();
         entityVisitor = new NodeEntityVisitor();
         relationships = new ArrayList<Relationship>();
         Util.analyzeJavaProject(project, entityVisitor);
 
         if (!entityVisitor.getNodeEntities().isEmpty()) {
             currentGraph = new Set();
             returnValue.add(currentGraph);
             currentGraph.addAttribute(new Attribute("implementation", "Neo4j"));
             Neo4jUtil.logger.log(Util.LOGGING_LEVEL, "Neo4J node entities: ");
             for (String node : entityVisitor.getNodeEntities().keySet()) {
                 currentNode = new Struct();
                 currentFields = new Struct();
                 currentNode.addStruct(currentFields);
                 currentGraph.addStruct(currentNode);
                 currentNode.addAttribute(new Attribute("name", node));
                 Neo4jUtil.logger.log(Util.LOGGING_LEVEL, "\n--Node: " + node);
                 for (FieldDeclaration field : entityVisitor.getNodeEntities().get(node)) {
                     // Try to identify relationships:
                     // We set only the name of the endingStruct, and once
                     // all of the nodes have been identified, we'll update
                     // the end node with the appropriate reference
                     // Identify relationships by the use of Annotations
                     relationshipEndType = null;
                     relationshipName = null;
                     if (Neo4jUtil.hasRelatedToAnnotation(field.modifiers())) {
                         relationshipEndType = field.getType();
                         relationshipName = Util.getValidName(field.fragments().get(0).toString());
                     }
 
                     // Identify relationships by the use of collections
                     else if (field.getType().isParameterizedType()) {
                         relationshipEndType = (Type) ((ParameterizedType) field.getType()).typeArguments().get(0);
                         relationshipName = Util.getValidName(field.fragments().get(0).toString());
                     }
 
                     // Decide whether to save a relationship or just a field
                     if (relationshipEndType != null) {
                         // Relationship data
                         currentRelationship = new Relationship();
                         currentRelationship.setEndStruct(new Struct());
                        currentRelationship.getEndStruct().addAttribute(
                                new Attribute("name", relationshipEndType.toString()));
                         currentRelationship.setStartStruct(currentNode);
                         currentRelationship.addAttribute(new Attribute("name", relationshipName));
 
                         // Save relationship
                         currentNode.addRelationship(currentRelationship);
                         relationships.add(currentRelationship);
                         Neo4jUtil.logger.log(Util.LOGGING_LEVEL, "------Related to: " + field.getType());
                     } else {
                         currentFields.addAttribute(new Attribute("name", field.fragments().get(0).toString()));
                         Neo4jUtil.logger.log(Util.LOGGING_LEVEL, "----Field: " + field.fragments().get(0));
                     }
                 }
             }
 
             // Update relationships' endStruct references
             Neo4jUtil.updateEndStructReferences(relationships, currentGraph);
         }
 
         return returnValue;
     }
 
     /**
      * Discover Neo4j nodes.
      * 
      * @param project
      * @throws JavaModelException
      */
     private static List<Set> discoverNodesSchemas(final IJavaProject project) throws JavaModelException {
         String endNode;
         Set currentGraph;
         String nodeClass;
         String startNode;
         String node2Class;
         Struct currentNode;
         Struct currentFields;
         List<Set> returnValue;
         List<String> currentRelations;
         Map<String, List<String>> nodes;
         Relationship currentRelationship;
         NodeDeclareVisitor declareVisitor;
         ContainerUpdateVisitor updateVisitor;
         List<Relationship> structRelationships;
         Map<String, List<String>> relationships;
         RelationshipCreateVisitor relationVisitor;
         VariableDeclarationFragment endDeclaration;
         Map<String, String> currentRelationshipType;
         Map<String, List<String>> nodesRelationships;
         VariableDeclarationFragment startDeclaration;
         Map<String, Map<String, String>> relationshipTypes;
         Map<String, List<String>> currentRelationshipFields;
         RelationshipDeclareVisitor relationshipDeclareVisitor;
         Map<String, Map<String, List<String>>> relationshipFields;
 
         // Identify when nodes are being declared
         declareVisitor = new NodeDeclareVisitor();
         Util.analyzeJavaProject(project, declareVisitor);
 
         // Identify when nodes are assigned properties
         updateVisitor = new ContainerUpdateVisitor();
         Util.analyzeJavaProject(project, updateVisitor);
 
         // Identify when nodes are being related
         relationVisitor = new RelationshipCreateVisitor();
         Util.analyzeJavaProject(project, relationVisitor);
 
         // Identify when relationships are declared
         relationshipDeclareVisitor = new RelationshipDeclareVisitor();
         Util.analyzeJavaProject(project, relationshipDeclareVisitor);
 
         // Match nodes to their corresponding fields and relations
         nodes = Neo4jUtil.associateContainersFields(declareVisitor.getVariableDeclarations(),
                 updateVisitor.getUpdateInvocations());
 
         // Match relationships to their corresponding fields and relations
         relationships = Neo4jUtil.associateContainersFields(relationshipDeclareVisitor.getVariableDeclarations(),
                 updateVisitor.getUpdateInvocations());
 
         // Identify relations between the identified nodes
         nodesRelationships = new HashMap<String, List<String>>();
         relationshipTypes = new HashMap<String, Map<String, String>>();
         for (MethodInvocation invocation : relationVisitor.getUpdateInvocations()) {
             // TODO: Consider operations that don't rely on variables
             // Only work with variables
             startNode = invocation.getExpression().toString();
             endNode = invocation.arguments().get(0).toString();
             if (Util.isVariableName(startNode) && Util.isVariableName(endNode)) {
                 endDeclaration = Neo4jUtil.getVariableDeclaration(Util.getInvocationBlock(invocation), endNode);
                 startDeclaration = Neo4jUtil.getVariableDeclaration(Util.getInvocationBlock(invocation), startNode);
                 if ((endDeclaration != null) && (startDeclaration != null)) {
                     // Try to match the previously identified nodes with the
                     // relationships' nodes
                     for (String node : nodes.keySet()) {
                         nodeClass = node.substring(0, node.indexOf('.'));
                         // Match startNode
                         if (startDeclaration.getInitializer().toString().contains(nodeClass)) {
                             currentRelations = nodesRelationships.get(node);
                             if (currentRelations == null) {
                                 currentRelations = new ArrayList<String>();
                             }
 
                             // Match endNode
                             for (String node2 : nodes.keySet()) {
                                 node2Class = node2.substring(0, node2.indexOf('.'));
                                 if (endDeclaration.getInitializer().toString().contains(node2Class)) {
                                     currentRelationshipType = relationshipTypes.get(node);
                                     if (currentRelationshipType == null) {
                                         currentRelationshipType = new HashMap<String, String>();
                                         relationshipTypes.put(node, currentRelationshipType);
                                     }
 
                                     // Add the relation and its type
                                     currentRelations.add(node2);
                                     currentRelationshipType.put(node2, invocation.arguments().get(1).toString());
                                     break;
                                 }
                             }
 
                             // Save relationships (if any)
                             if (currentRelations.size() > 0) {
                                 nodesRelationships.put(node, currentRelations);
                             }
                         }
                     }
                 }
             }
         }
 
         // Associate relationships found between nodes and the combinations of
         // relationships-fields
         relationshipFields = new HashMap<String, Map<String, List<String>>>();
         for (String relationStart : nodes.keySet()) {
             if (nodesRelationships.get(relationStart) != null) {
                 for (String relationEnd : nodesRelationships.get(relationStart)) {
                     for (String relationship : relationships.keySet()) {
                         // Look for references to the start and end nodes for
                         // the relationship
                         RelationshipStartNodeVisitor startNodeVisitor = new RelationshipStartNodeVisitor();
                         startNodeVisitor.setVariableName(relationship.substring(relationship.lastIndexOf('.') + 1));
                         RelationshipEndNodeVisitor endNodeVisitor = new RelationshipEndNodeVisitor();
                         endNodeVisitor.setVariableName(relationship.substring(relationship.lastIndexOf('.') + 1));
                         Util.analyzeJavaProject(project, startNodeVisitor);
                         Util.analyzeJavaProject(project, endNodeVisitor);
 
                         // Look for classes between the classes associated to
                         // the start and end nodes of the relation, and
                         // previously discovered nodes
                         if ((startNodeVisitor.getUpdateInvocations().size() > 0)
                                 && (endNodeVisitor.getUpdateInvocations().size() > 0)) {
                             if ((Util.isMatchingClass(startNodeVisitor.getUpdateInvocations().get(0), relationStart))
                                     && (Util.isMatchingClass(endNodeVisitor.getUpdateInvocations().get(0), relationEnd))) {
                                 currentRelationshipFields = relationshipFields.get(relationStart);
                                 if (currentRelationshipFields == null) {
                                     currentRelationshipFields = new HashMap<String, List<String>>();
                                     relationshipFields.put(relationStart, currentRelationshipFields);
                                 }
 
                                 currentRelationshipFields.put(relationEnd, relationships.get(relationship));
                             }
                         }
                     }
                 }
             }
         }
 
         returnValue = new ArrayList<Set>();
         if (!nodes.isEmpty()) {
             currentGraph = new Set();
             returnValue.add(currentGraph);
             structRelationships = new ArrayList<Relationship>();
             currentGraph.addAttribute(new Attribute("implementation", "Neo4j"));
             Neo4jUtil.logger.log(Util.LOGGING_LEVEL, "\nNeo4J nodes: ");
             for (String node : nodes.keySet()) {
                 currentNode = new Struct();
                 currentFields = new Struct();
                 currentNode.addStruct(currentFields);
                 currentGraph.addStruct(currentNode);
                 currentNode.addAttribute(new Attribute("name", node));
                 Neo4jUtil.logger.log(Util.LOGGING_LEVEL, "\n--Node: " + node);
                 for (String field : nodes.get(node)) {
                     currentFields.addAttribute(new Attribute("name", field));
                     Neo4jUtil.logger.log(Util.LOGGING_LEVEL, "\n----Field: " + field);
                 }
 
                 if (nodesRelationships.get(node) != null) {
                     for (String relationship : nodesRelationships.get(node)) {
                         currentRelationship = new Relationship();
                         currentNode.addRelationship(currentRelationship);
                         currentRelationship.addAttribute(new Attribute("type", relationshipTypes.get(node).get(
                                 relationship)));
                         currentRelationship.setStartStruct(currentNode);
                         currentRelationship.setEndStruct(new Struct());
                         // We set only the name of the endingStruct, and once
                         // all of the nodes have been identified, we'll update
                         // the end node with the appropriate reference
                         currentRelationship.getEndStruct().addAttribute(new Attribute("name", relationship));
                         structRelationships.add(currentRelationship);
                         Neo4jUtil.logger.log(Util.LOGGING_LEVEL, "\n----Relationship: " + relationship + " ["
                                 + relationshipTypes.get(node).get(relationship) + "]");
                         if (relationshipFields.get(node).get(relationship) != null) {
                             for (String relationField : relationshipFields.get(node).get(relationship)) {
                                 Neo4jUtil.logger
                                         .log(Util.LOGGING_LEVEL, "\n------Relationship field: " + relationField);
                             }
                         }
                     }
                 }
             }
 
             // Update relationships' endStruct references
             Neo4jUtil.updateEndStructReferences(structRelationships, currentGraph);
         }
 
         return returnValue;
     }
 
     /**
      * Update an end side of a relationship with the correct struct reference.
      * 
      * @param relationships
      * @param graph
      */
     private static void updateEndStructReferences(final List<Relationship> relationships, final Set graph) {
         String currentName;
 
         // Update relationships' endStruct references
         for (Relationship relationship : relationships) {
             // Get the structName to look for
             currentName = null;
             for (Attribute attribute : relationship.getEndStruct().getAttributes()) {
                 if (attribute.getName().equals("name")) {
                     currentName = attribute.getValue();
                     break;
                 }
             }
 
             // Update the reference with the appropriate struct
             if (currentName != null) {
                 for (Struct node : graph.getStructs()) {
                     for (Attribute attribute : node.getAttributes()) {
                         if ((attribute.getName().equals("name")) && (attribute.getValue().equals(currentName))) {
                             relationship.setEndStruct(node);
                             break;
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Associate fields to their corresponding Neo4j's containers.
      * 
      * @param declarations
      * @param updateInvocations
      * @return
      */
     private static Map<String, List<String>> associateContainersFields(
             final List<VariableDeclarationFragment> declarations, List<MethodInvocation> updateInvocations) {
         String fragmentKey;
         String fragmentRoot;
         String invocationRoot;
         List<String> currentFields;
         Map<String, List<String>> returnValue;
 
         returnValue = new HashMap<String, List<String>>();
         // Declarations
         for (VariableDeclarationFragment fragment : declarations) {
             currentFields = new ArrayList<String>();
             fragmentRoot = ((CompilationUnit) fragment.getRoot()).getTypeRoot().getElementName();
 
             // Fields
             for (MethodInvocation invocation : updateInvocations) {
                 invocationRoot = ((CompilationUnit) invocation.getRoot()).getTypeRoot().getElementName();
                 // Match name of declare and update variables, in the same file
                 if (fragmentRoot.equals(invocationRoot)) {
                     if (fragment.getName().toString().equals(invocation.getExpression().toString())) {
                         currentFields.add(((SimpleName) invocation.arguments().get(0)).toString());
                     }
                 }
             }
 
             fragmentKey = fragmentRoot + "." + fragment.toString();
             if ((currentFields.size() > 0) && (returnValue.get(fragmentKey) == null)) {
                 returnValue.put(fragmentKey, currentFields);
             }
         }
 
         return returnValue;
     }
 
     /**
      * Get the corresponding variable declarations.
      * 
      * @param block
      * @param variableName
      * @return
      */
     private static VariableDeclarationFragment getVariableDeclaration(final Block block, final String variableName) {
         VariableDeclarationFragment returnValue;
         NodeDeclareVisitor variableDeclareVisitor;
 
         variableDeclareVisitor = new NodeDeclareVisitor();
         variableDeclareVisitor.setVariableName(variableName);
         block.accept(variableDeclareVisitor);
         returnValue = null;
         if (!variableDeclareVisitor.getVariableDeclarations().isEmpty()) {
             returnValue = variableDeclareVisitor.getVariableDeclarations().get(0);
         }
 
         return returnValue;
     }
 
     /**
      * Identify if the list of modifiers contain Spring's RelatedTo annotation.
      * 
      * @param modifier
      * @return
      */
     private static boolean hasRelatedToAnnotation(final List<?> modifiers) {
         boolean returnValue;
         String annotationName;
 
         returnValue = false;
         for (Object modifier : modifiers) {
             if (NormalAnnotation.class.isAssignableFrom(modifier.getClass())) {
                 annotationName = ((NormalAnnotation) modifier).resolveTypeBinding().getQualifiedName();
                 if (annotationName.equals("org.springframework.data.neo4j.annotation.RelatedTo")) {
                     returnValue = true;
                     break;
                 }
             }
         }
 
         return returnValue;
     }
 }
