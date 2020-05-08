 package com.crypticbit.javelin.neo4j.nodes.json;
 
 import java.io.IOException;
 
 import org.neo4j.graphdb.Relationship;
 
 import com.crypticbit.javelin.IllegalJsonException;
 import com.crypticbit.javelin.JsonPersistenceException;
 import com.crypticbit.javelin.neo4j.nodes.ComplexNode;
 import com.crypticbit.javelin.neo4j.nodes.PotentialRelationship;
 import com.crypticbit.javelin.neo4j.nodes.RelationshipHolder;
 import com.crypticbit.javelin.neo4j.strategies.FundementalDatabaseOperations.NullUpdateOperation;
 import com.crypticbit.javelin.neo4j.strategies.FundementalDatabaseOperations.UpdateOperation;
 import com.crypticbit.javelin.neo4j.strategies.operations.JsonWriteUpdateOperation;
 import com.crypticbit.javelin.neo4j.types.NodeTypes;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.jayway.jsonpath.internal.PathToken;
 import com.jayway.jsonpath.internal.PathTokenizer;
 
 public class JsonNodeFactory implements ComplexGraphNode {
 
     private ComplexNode holder;
     private RelationshipHolder incomingRelationship;
     private JsonGraphNode jsonNode;
 
     public JsonNodeFactory(ComplexNode holder, RelationshipHolder incomingRelationship) {
 	this.holder = holder;
 	this.incomingRelationship = incomingRelationship;
     }
 
     @Override
     public ComplexNode add() throws IllegalJsonException, JsonPersistenceException {
 	if (getJsonNode() == null) return createComplexNodeFromUpdateOperation(new ArrayGraphNode.CreateNewArrayElementUpdateOperation(
 		0, NullUpdateOperation.INSTANCE));
 	else
 	    return getJsonNode().add();
 
     }
 
     @Override
     public ComplexGraphNode navigate(final PathToken token) throws IllegalJsonException {
 	if (getJsonNode() == null) return createComplexNodeFromUpdateOperation(getUpdateOperationForNavToken(token));
 	else
 	    return getJsonNode().navigate(token);
     }
 
     @Override
     public ComplexGraphNode navigate(String jsonPath) throws IllegalJsonException {
 	PathTokenizer tokens = new PathTokenizer(jsonPath);
 	ComplexGraphNode currentNode = this;
 	for (PathToken token : tokens) {
 	    if (!token.isRootToken()) {
 		currentNode = currentNode.navigate(token);
 	    }
 	}
 	return currentNode;
     }
 
     @Override
     public ComplexNode put(final String key) throws IllegalJsonException, JsonPersistenceException {
 	if (getJsonNode() == null) return createComplexNodeFromUpdateOperation(new MapGraphNode.CreateNewMapElementUpdateOperation(
 		key, NullUpdateOperation.INSTANCE));
 	else
 	    return getJsonNode().put(key);
     }
 
     @Override
     public JsonNode toJsonNode() {
 	return getJsonNode().toJsonNode();
     }
 
     @Override
     public String toJsonString() {
 	return getJsonNode().toJsonString();
     }
 
     @Override
     public void write(String json) throws IllegalJsonException, JsonPersistenceException {
 	try {
 	    final JsonNode values = new ObjectMapper().readTree(json);
 	    incomingRelationship.createOrUpdateRelationship(new JsonWriteUpdateOperation(values), holder.getStrategy());
 	}
 	catch (JsonProcessingException jpe) {
 	    throw new IllegalJsonException("The JSON string was badly formed: " + json, jpe);
 	}
 	catch (IOException e) {
 	    throw new JsonPersistenceException("IOException whilst writing data to database", e);
 	}
     }
 
     private ComplexNode createComplexNodeFromUpdateOperation(final UpdateOperation operationToMakeIncomingRelationship) {
 	return new ComplexNode(new RelationshipHolder(new PotentialRelationship() {
 	    @Override
 	    public Relationship create(UpdateOperation updateOperation) {
 		Relationship realisedIncomingRelationship = incomingRelationship.createOrUpdateRelationship(
			NullUpdateOperation.INSTANCE, holder.getStrategy());
		holder.getStrategy().update(realisedIncomingRelationship, false, operationToMakeIncomingRelationship);
 		assert (operationToMakeIncomingRelationship.getNewRelationships().length == 1);
 		Relationship relationshipToNewNode = operationToMakeIncomingRelationship.getNewRelationships()[0];
 		holder.getStrategy().update(relationshipToNewNode, true, updateOperation);
 		return relationshipToNewNode;
 	    }
 
 	}), holder.getStrategy());
     }
 
     private JsonGraphNode getJsonNode() {
 	if (jsonNode == null) if (incomingRelationship.isRealRelationship()) {
 	    jsonNode = NodeTypes.wrapAsGraphNode(holder.getStrategy()
 		    .read(incomingRelationship.getRelationship(), null).getEndNode(), holder);
 	}
 	return jsonNode;
     }
 
     private UpdateOperation getUpdateOperationForNavToken(PathToken token) {
 	if (token.isArrayIndexToken()) return new ArrayGraphNode.CreateNewArrayElementUpdateOperation(
 		token.getArrayIndex(), NullUpdateOperation.INSTANCE);
 	else
 	    return new MapGraphNode.CreateNewMapElementUpdateOperation(token.getFragment(),
 		    NullUpdateOperation.INSTANCE);
     }
 
 }
