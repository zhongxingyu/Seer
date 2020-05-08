 package org.triple_brain.module.model.graph;
 
 import org.junit.Test;
 import org.triple_brain.module.common_utils.Uris;
 import org.triple_brain.module.model.FriendlyResource;
 import org.triple_brain.module.model.suggestion.Suggestion;
 
 import javax.inject.Inject;
 import java.net.URI;
 import java.util.HashSet;
 import java.util.Set;
 
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNot.not;
 import static org.hamcrest.core.IsNull.nullValue;
 import static org.junit.Assert.*;
 
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class VertexTest extends AdaptableGraphComponentTest {
 
     @Inject
     protected VertexFactory vertexFactory;
 
     @Test
     public void can_update_label() {
         Edge newEdge = vertexA.addVertexAndRelation();
         Vertex vertex = newEdge.destinationVertex();
         vertex.label("Ju-Ji-Tsu");
         assertThat(vertex.label(), is("Ju-Ji-Tsu"));
     }
 
     @Test
     public void can_update_comment(){
         assertThat(vertexA().comment(), is(""));
         vertexA().comment("Its vertex a !");
         assertThat(vertexA().comment(), is("Its vertex a !"));
     }
 
     @Test
     public void can_add_vertex_and_relation() {
         Integer numberOfEdgesAndVertices = numberOfEdgesAndVertices();
         Edge edge = vertexA.addVertexAndRelation();
         assertThat(edge, is(not(nullValue())));
         Integer newNumberOfEdgesAndVertices = numberOfEdgesAndVertices();
         assertThat(newNumberOfEdgesAndVertices, is(numberOfEdgesAndVertices + 2));
         assertTrue(vertexA.hasEdge(edge));
 
         assertThat(edge.sourceVertex().uri(), is(vertexA.uri()));
 
         Vertex destinationVertex = edge.destinationVertex();
         assertThat(destinationVertex, is(not(nullValue())));
     }
 
     @Test
     public void can_remove_a_vertex() {
         Integer numberOfEdgesAndVertices = numberOfEdgesAndVertices();
 
         URI vertexBId = vertexB.uri();
 
         assertTrue(userGraph.haveElementWithId(vertexBId));
         vertexB.remove();
         assertFalse(userGraph.haveElementWithId(vertexBId));
 
         Integer updatedNumberOfEdgesAndVertices = numberOfEdgesAndVertices();
         assertThat(updatedNumberOfEdgesAndVertices, is(numberOfEdgesAndVertices - 3));
     }
 
     @Test
     public void can_add_an_additional_type_to_vertex() throws Exception {
         assertTrue(
                 vertexA.getAdditionalTypes().isEmpty()
         );
         vertexA.addType(
                 modelTestScenarios.personType()
         );
         assertFalse(
                 vertexA.getAdditionalTypes().isEmpty()
         );
     }
 
     @Test
     public void can_add_multiple_additional_types_to_a_vertex() throws Exception {
         assertTrue(
                 vertexA.getAdditionalTypes().isEmpty()
         );
         vertexA.addType(
                 modelTestScenarios.personType()
         );
         vertexA.addType(
                 modelTestScenarios.computerScientistType()
         );
         assertThat(
                 vertexA.getAdditionalTypes().size(),
                 is(2)
         );
     }
 
     @Test
     public void can_remove_an_additional_type_to_vertex() throws Exception {
         vertexA.addType(
                 modelTestScenarios.personType()
         );
         FriendlyResource computerScientistType = modelTestScenarios.computerScientistType();
         vertexA.addType(
                 computerScientistType
         );
         assertThat(
                 vertexA.getAdditionalTypes().size(),
                 is(2)
         );
         vertexA.removeIdentification(modelTestScenarios.personType());
         assertThat(
                 vertexA.getAdditionalTypes().size(),
                 is(1)
         );
         FriendlyResource remainingType = vertexA.getAdditionalTypes().iterator().next();
         assertThat(
                 remainingType.label(),
                 is(computerScientistType.label())
         );
     }
 
     @Test
     public void when_removing_an_external_resource_the_suggestions_that_depend_on_it_are_removed(){
         vertexA.addType(
                 modelTestScenarios.person()
         );
         vertexA.addType(
                 modelTestScenarios.event()
         );
         vertexA.addSuggestions(
                 modelTestScenarios.nameSuggestion(),
                 modelTestScenarios.startDateSuggestion()
         );
         assertTrue(
                 modelTestScenarios.nameSuggestion().origins().iterator().next()
                         .isRelatedToFriendlyResource(
                                 modelTestScenarios.person()
                         )
         );
         assertThat(vertexA.suggestions().size(), is(2));
         vertexA.removeIdentification(
                 modelTestScenarios.person()
         );
         assertThat(vertexA.suggestions().size(), is(1));
         Suggestion remainingSuggestion = vertexA.suggestions().iterator().next();
         assertSame(
                 remainingSuggestion.sameAs().uri().toString(),
                 modelTestScenarios.startDateSuggestion().sameAs().uri().toString()
         );
     }
 
     @Test
     public void can_add_suggestions_to_a_vertex() throws Exception {
         assertTrue(vertexA.suggestions().isEmpty());
         vertexA.addSuggestions(
                 modelTestScenarios.startDateSuggestion()
         );
         assertFalse(vertexA.suggestions().isEmpty());
         Suggestion getSuggestion = vertexA.suggestions().iterator().next();
         assertThat(getSuggestion.label(), is("Start date"));
     }
 
     @Test
     public void can_add_same_as(){
         Edge newEdge = vertexA.addVertexAndRelation();
         Vertex newVertex = newEdge.destinationVertex();
         newVertex.label("Tim Berners Lee");
         assertTrue(newVertex.getSameAs().isEmpty());
         newVertex.addSameAs(modelTestScenarios.timBernersLee());
         assertFalse(newVertex.getSameAs().isEmpty());
     }
 
     @Test
     public void can_get_empty_list_after_removing_last_same_as(){
         vertexA.addSameAs(
                 modelTestScenarios.timBernersLee()
         );
         vertexA.removeIdentification(
                 modelTestScenarios.timBernersLee()
         );
         assertTrue(vertexA.getSameAs().isEmpty());
     }
 
     @Test
     public void deleting_a_vertex_does_not_delete_its_identifications_in_the_graph(){
         assertFalse(
                 userGraph.haveElementWithId(
                         Uris.get("http://www.w3.org/People/Berners-Lee/card#i")
                 )
         );
         FriendlyResource timBernersLee = modelTestScenarios.timBernersLee();
         vertexA.addSameAs(
                 timBernersLee
         );
         assertTrue(
                 userGraph.haveElementWithId(
                         timBernersLee.uri()
                 )
         );
         vertexA.remove();
         assertTrue(
                 userGraph.haveElementWithId(
                         timBernersLee.uri()
                 )
         );
     }
 
     @Test
     public void can_assign_the_same_identification_to_2_vertices(){
         FriendlyResource timBernersLee = modelTestScenarios.timBernersLee();
         vertexA.addSameAs(
                 timBernersLee
         );
         vertexB.addSameAs(
                 timBernersLee
         );
         assertTrue(vertexA.getSameAs().iterator().next().equals(timBernersLee));
         assertTrue(vertexB.getSameAs().iterator().next().equals(timBernersLee));
     }
 
     @Test
     public void can_get_same_as(){
         Edge newEdge = vertexA.addVertexAndRelation();
         Vertex newVertex = newEdge.destinationVertex();
         newVertex.label("Tim Berners Lee");
         assertTrue(newVertex.getSameAs().isEmpty());
         newVertex.addSameAs(modelTestScenarios.timBernersLee());
         FriendlyResource sameAs = newVertex.getSameAs().iterator().next();
         assertThat(sameAs.label(), is(modelTestScenarios.timBernersLee().label()));
     }
 
     @Test
     public void can_add_generic_identification(){
         assertFalse(vertexA.getGenericIdentifications().contains(
                 modelTestScenarios.extraterrestrial()
         ));
         vertexA.addGenericIdentification(
                 modelTestScenarios.extraterrestrial()
         );
         assertTrue(vertexA.getGenericIdentifications().contains(
                 modelTestScenarios.extraterrestrial()
         ));
     }
 
     @Test
     public void can_test_if_vertex_has_destination_vertex(){
         assertFalse(vertexA.hasDestinationVertex(vertexC));
         vertexA.addRelationToVertex(vertexC);
         assertTrue(vertexA.hasDestinationVertex(vertexC));
     }
 
     @Test
     public void source_vertex_is_not_a_destination_vertex(){
         vertexA.addRelationToVertex(vertexC);
         assertTrue(vertexA.hasDestinationVertex(vertexC));
         assertFalse(vertexC.hasDestinationVertex(vertexA));
     }
 
     @Test
     public void there_is_a_creation_date(){
         assertThat(
                 vertexA.creationDate(),
                 is(not(nullValue()))
         );
     }
 
     @Test
     public void there_is_a_last_modification_date(){
         assertThat(
                 vertexA.lastModificationDate(),
                 is(not(nullValue()))
         );
     }
 
     @Test
     public void a_vertex_is_private_by_default(){
         Vertex newVertex = vertexA.addVertexAndRelation().destinationVertex();
         assertFalse(newVertex.isPublic());
     }
 
     @Test
     public void can_make_a_vertex_public(){
         assertFalse(vertexA.isPublic());
         vertexA.makePublic();
         assertTrue(vertexA.isPublic());
     }
 
     @Test
     public void can_make_a_vertex_private(){
         vertexA.makePublic();
         assertTrue(vertexA.isPublic());
         vertexA.makePrivate();
         assertFalse(vertexA.isPublic());
     }
 
     @Test
     public void can_check_equality(){
         assertTrue(vertexA.equals(vertexA));
         assertFalse(vertexA.equals(vertexB));
     }
 
     @Test
     public void can_compare_to_friendly_resource(){
         FriendlyResource vertexAAsFriendlyResource = (FriendlyResource) vertexA;
         assertTrue(vertexA.equals(vertexAAsFriendlyResource));
     }
 
     @Test
     public void can_get_empty_set_of_included_vertices_for_a_vertex_that_have_none(){
         assertTrue(
                 vertexA.getIncludedVertices().isEmpty()
         );
     }
 
     @Test
     public void can_create_vertex_from_vertices_set(){
         Vertex newVertex = vertexFactory.createFromVertices(
                 vertexBAndC()
         );
         Set<Vertex> includedVertices = newVertex.getIncludedVertices();
         assertTrue(includedVertices.contains(vertexB));
         assertTrue(includedVertices.contains(vertexC));
         assertFalse(includedVertices.contains(vertexA));
     }
 
     @Test
     public void more_than_one_vertex_is_required_to_create_vertex_from_vertices(){
         Set<Vertex> empty = new HashSet<>();
         try{
             vertexFactory.createFromVertices(
                     empty
             );
             fail();
         }catch(Exception exception){
             //continue
         }
         Set<Vertex> one = new HashSet<>();
         one.add(vertexA);
         try{
             vertexFactory.createFromVertices(
                     one
             );
             fail();
         }catch(Exception exception){
             //continue
         }
     }
 
     @Test
     public void removing_a_vertex_removes_it_from_included_vertices_as_well(){
         Vertex newVertex = vertexFactory.createFromVertices(
                 vertexBAndC()
         );
         assertThat(
                 newVertex.getIncludedVertices().size(),
                 is(2)
         );
         vertexB.remove();
         assertThat(
                 newVertex.getIncludedVertices().size(),
                 is(1)
         );
     }
 
     @Test
     public void removing_vertex_that_has_included_vertices_doesnt_remove_its_included_vertices(){
         Vertex newVertex = vertexFactory.createFromVertices(
                 vertexBAndC()
         );
         newVertex.remove();
         assertTrue(
                 wholeGraph().containsVertex(vertexB)
         );
         assertTrue(
                 wholeGraph().containsVertex(vertexC)
         );
     }
 
     @Test
     public void including_a_vertex_doesnt_add_to_it_any_included_vertices(){
         vertexFactory.createFromVertices(
                 vertexBAndC()
         );
         assertTrue(vertexB.getIncludedVertices().isEmpty());
     }
 
     @Test
     public void can_get_number_of_connected_edges(){
         assertThat(
                 vertexB().getNumberOfConnectedEdges(),
                 is(2)
         );
     }
 
     @Test
     public void when_deleting_a_vertex_it_decrements_the_number_of_connected_vertices_of_its_neighbors(){
         vertexC.addVertexAndRelation();
         assertThat(
                 vertexC.getNumberOfConnectedEdges(),
                 is(2)
         );
         assertThat(
                 vertexA.getNumberOfConnectedEdges(),
                 is(1)
         );
         vertexB.remove();
         assertThat(
                 vertexC.getNumberOfConnectedEdges(),
                 is(1)
         );
         assertThat(
                 vertexA.getNumberOfConnectedEdges(),
                 is(0)
         );
     }
 
     private Set<Vertex> vertexBAndC(){
         Set<Vertex> vertexBAndC = new HashSet<>();
         vertexBAndC.add(vertexB);
         vertexBAndC.add(vertexC);
         return vertexBAndC;
     }
 }
 
