 package org.triple_brain.module.model.graph;
 
 import org.triple_brain.module.model.ExternalFriendlyResource;
 import org.triple_brain.module.model.suggestion.PersistedSuggestion;
 import org.triple_brain.module.model.suggestion.Suggestion;
 
 import java.net.URI;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public interface Vertex extends GraphElement{
     public static final String EMPTY_LABEL = "concept";

     public abstract boolean hasEdge(Edge edge);
     public abstract void addOutgoingEdge(Edge edge);
     public abstract void removeOutgoingEdge(Edge edge);
     public abstract Edge edgeThatLinksToDestinationVertex(Vertex destinationVertex);
     public abstract Boolean hasDestinationVertex(Vertex destinationVertex);
     public abstract void addNeighbor(Vertex neighbor);
     public abstract Edge addVertexAndRelation();
     public abstract Edge addRelationToVertex(Vertex destinationVertex);
     public abstract void remove();
     public abstract void removeNeighbor(Vertex neighbor);
     public abstract Set<Edge> outGoingEdges();
     public abstract Set<Edge> connectedEdges();
     public abstract List<String> hiddenConnectedEdgesLabel();
     public abstract void hiddenConnectedEdgesLabel(List<String> hiddenEdgeLabel);
     public abstract void addSuggestions(Suggestion ... suggestions);
     public abstract Set<PersistedSuggestion> suggestions();
     public abstract ExternalFriendlyResource friendlyResourceWithUri(URI uri);
     public abstract void addType(ExternalFriendlyResource type);
     public abstract void removeFriendlyResource(ExternalFriendlyResource type);
     public abstract Set<ExternalFriendlyResource> getAdditionalTypes();
     public abstract void addSameAs(ExternalFriendlyResource friendlyResource);
     public abstract Set<ExternalFriendlyResource> getSameAs();
 }
 
