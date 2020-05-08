 package org.triple_brain.module.model.graph;
 
 import org.junit.Test;
 import org.triple_brain.module.model.ExternalFriendlyResource;
 import org.triple_brain.module.model.ModelTestScenarios;
 import org.triple_brain.module.model.suggestion.PersistedSuggestion;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNot.not;
 import static org.hamcrest.core.IsNull.nullValue;
 import static org.junit.Assert.*;
 
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class VertexTest extends AdaptableGraphComponentTest {
     @Test
     public void can_update_label() {
         Edge newEdge = vertexA.addVertexAndRelation();
         Vertex vertex = newEdge.destinationVertex();
         vertex.label("Ju-Ji-Tsu");
         assertThat(vertex.label(), is("Ju-Ji-Tsu"));
     }
 
     @Test
     public void can_update_note(){
         assertThat(vertexA().note(), is(""));
         vertexA().note("Its vertex a !");
         assertThat(vertexA().note(), is("Its vertex a !"));
     }
 
     @Test
     public void can_add_vertex_and_relation() {
         Integer numberOfEdgesAndVertices = numberOfEdgesAndVertices();
         Edge edge = vertexA.addVertexAndRelation();
 
         assertThat(edge, is(not(nullValue())));
         assertTrue(edge.hasLabel());
 
         Integer newNumberOfEdgesAndVertices = numberOfEdgesAndVertices();
         assertThat(newNumberOfEdgesAndVertices, is(numberOfEdgesAndVertices + 2));
         assertTrue(vertexA.hasEdge(edge));
 
         assertThat(edge.sourceVertex().id(), is(vertexA.id()));
 
         Vertex destinationVertex = edge.destinationVertex();
         assertThat(destinationVertex, is(not(nullValue())));
         assertTrue(destinationVertex.hasLabel());
     }
 
     @Test
     public void can_remove_a_vertex() {
         Integer numberOfEdgesAndVertices = numberOfEdgesAndVertices();
 
         String vertexBId = vertexB.id();
 
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
                 ModelTestScenarios.personType()
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
                 ModelTestScenarios.personType()
         );
         vertexA.addType(
                 ModelTestScenarios.computerScientistType()
         );
         assertThat(
                 vertexA.getAdditionalTypes().size(),
                 is(2)
         );
     }
 
     @Test
     public void can_remove_an_additional_type_to_vertex() throws Exception {
         vertexA.addType(
                 ModelTestScenarios.personType()
         );
         ExternalFriendlyResource computerScientistType = ModelTestScenarios.computerScientistType();
         vertexA.addType(
                 computerScientistType
         );
         assertThat(
                 vertexA.getAdditionalTypes().size(),
                 is(2)
         );
         vertexA.removeFriendlyResource(ModelTestScenarios.personType());
         assertThat(
                 vertexA.getAdditionalTypes().size(),
                 is(1)
         );
         ExternalFriendlyResource remainingType = vertexA.getAdditionalTypes().iterator().next();
         assertThat(
                 remainingType.label(),
                 is(computerScientistType.label())
         );
     }
 
     @Test
     public void when_removing_an_external_resource_the_suggestions_that_depend_on_it_are_removed(){
         vertexA.addType(
                 ModelTestScenarios.person()
         );
         vertexA.addType(
                 ModelTestScenarios.event()
         );
         vertexA.addSuggestions(
                 ModelTestScenarios.nameSuggestion(),
                 ModelTestScenarios.startDateSuggestion()
         );
         assertTrue(
                 ModelTestScenarios.nameSuggestion().origins().iterator().next()
                         .isTheIdentificationWithUri(
                                 ModelTestScenarios.person().uri()
                         )
         );
         assertThat(vertexA.suggestions().size(), is(2));
         vertexA.removeFriendlyResource(
                 ModelTestScenarios.person()
         );
         assertThat(vertexA.suggestions().size(), is(1));
         PersistedSuggestion remainingSuggestion = vertexA.suggestions().iterator().next();
         assertSame(
                 remainingSuggestion.get().sameAsUri().toString(),
                 ModelTestScenarios.startDateSuggestion().sameAsUri().toString()
         );
     }
 
     @Test
     public void can_add_suggestions_to_a_vertex() throws Exception {
         assertTrue(vertexA.suggestions().isEmpty());
         vertexA.addSuggestions(
                 ModelTestScenarios.startDateSuggestion()
         );
         assertFalse(vertexA.suggestions().isEmpty());
         PersistedSuggestion getSuggestion = vertexA.suggestions().iterator().next();
         assertThat(getSuggestion.get().label(), is("Start date"));
     }
 
     @Test
     public void can_add_same_as(){
         Edge newEdge = vertexA.addVertexAndRelation();
         Vertex newVertex = newEdge.destinationVertex();
         newVertex.label("Tim Berners Lee");
         assertTrue(newVertex.getSameAs().isEmpty());
         newVertex.addSameAs(ModelTestScenarios.timBernersLee());
         assertFalse(newVertex.getSameAs().isEmpty());
     }
 
     @Test
     public void deleting_a_vertex_does_not_delete_its_identifications_in_the_graph(){
         ExternalFriendlyResource timBernersLee = ModelTestScenarios.timBernersLee();
         assertFalse(
                 userGraph.haveElementWithId(
                         timBernersLee.uri().toString()
                 )
         );
         vertexA.addSameAs(
                 timBernersLee
         );
         assertTrue(
                 userGraph.haveElementWithId(
                         timBernersLee.uri().toString()
                 )
         );
         vertexA.remove();
         assertTrue(
                 userGraph.haveElementWithId(
                         timBernersLee.uri().toString()
                 )
         );
     }
 
     @Test
     public void can_assign_the_same_identification_to_2_vertices(){
         ExternalFriendlyResource timBernersLee = ModelTestScenarios.timBernersLee();
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
         newVertex.addSameAs(ModelTestScenarios.timBernersLee());
         ExternalFriendlyResource sameAs = newVertex.getSameAs().iterator().next();
         assertThat(sameAs.label(), is(ModelTestScenarios.timBernersLee().label()));
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
 
 }
