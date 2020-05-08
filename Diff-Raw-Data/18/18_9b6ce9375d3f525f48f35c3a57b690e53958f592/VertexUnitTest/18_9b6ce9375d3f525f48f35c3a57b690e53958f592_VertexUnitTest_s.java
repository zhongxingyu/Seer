 package model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import junit.framework.Assert;
 import model.SimpleVertex.VertexIntegerEnumerator;
 
 import org.junit.Test;
 import org.sbml.jsbml.Compartment;
 import org.sbml.jsbml.Model;
 import org.sbml.jsbml.Species;
 
 /**
  * @author massimo
  *
  */
 /**
  * @author massimo
  * 
  */
 public class VertexUnitTest {
 	@Test
 	public void createVertex() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		assertNotNull(vertex);
 	}
 
 	@Test
 	public void checkingIsSinkFalse() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a = VertexFactory.makeSimpleVertex();
 		Vertex b = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(a).addNeighbour(b);
 
 		assertFalse(vertex.isSink());
 	}
 
 	@Test
 	public void checkingIsSinkTrue() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a = VertexFactory.makeSimpleVertex();
 		Vertex b = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(a).addNeighbour(b);
 
 		assertTrue(a.isSink());
 		assertTrue(b.isSink());
 	}
 
 	@Test
 	public void checkingIsSourceFalse() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a = VertexFactory.makeSimpleVertex();
 		Vertex b = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(a).addNeighbour(b);
 
 		assertFalse(a.isSource());
 		assertFalse(b.isSource());
 	}
 
 	@Test
 	public void checkingIsSourceTrue() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a = VertexFactory.makeSimpleVertex();
 		Vertex b = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(a).addNeighbour(b);
 
 		assertTrue(vertex.isSource());
 	}
 
 	@Test
 	public void checkingIsolatedConsequence() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 
 		assertFalse(vertex.isSource());
 		assertTrue(vertex.isSink());
 		assertTrue(vertex.isYourNeighborhoodEmpty());
 	}
 
 	@Test
 	public void checkingSelfLoopSourceSinkConsequence() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(vertex);
 
 		// TODO: this situation is correct? We really need to model a self loop,
 		// however for now we are consistent
 		assertFalse(vertex.isSource());
 		assertFalse(vertex.isSink());
 		assertFalse(vertex.isYourNeighborhoodEmpty());
 	}
 
 	@Test
 	public void checkingIsNotSourceNorSink() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a = VertexFactory.makeSimpleVertex();
 		Vertex b = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(a);
 		a.addNeighbour(b);
 
 		assertTrue(vertex.isSource());
 		assertFalse(vertex.isSink());
 
 		assertFalse(a.isSource());
 		assertFalse(a.isSink());
 
 		assertFalse(b.isSource());
 		assertTrue(b.isSink());
 	}
 
 	@Test
 	public void containsNeighbours() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a = VertexFactory.makeSimpleVertex();
 		Vertex b = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(a).addNeighbour(b);
 
 		Vertex c = VertexFactory.makeSimpleVertex();
 
 		assertFalse(vertex.isYourNeighborhoodEmpty());
 		assertTrue(vertex.isYourNeighbour(a));
 		assertTrue(vertex.isYourNeighbour(b));
 		assertFalse(vertex.isYourNeighbour(c));
 	}
 
 	@Test
 	public void containsReverseNeighbours() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(a);
 
 		assertTrue(vertex.isYourNeighbour(a));
 		assertFalse(a.isYourNeighbour(vertex));
 	}
 
 	@Test
 	public void addingNeighborsOrderIsPreservedByVertex() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a1 = VertexFactory.makeSimpleVertex();
 		Vertex a2 = VertexFactory.makeSimpleVertex();
 		Vertex a3 = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(a1);
 		vertex.addNeighbour(a2);
 		vertex.addNeighbour(a3);
 
 		List<Vertex> desiredNeighborsSequence = new LinkedList<Vertex>(
 				Arrays.asList(a1, a2, a3));
 
 		final List<Vertex> actualNeighborsSequence = new LinkedList<Vertex>();
 
 		vertex.doOnNeighbors(new VertexLogicApplier() {
 
 			@Override
 			public void apply(Vertex neighbourVertex) {
 				actualNeighborsSequence.add(neighbourVertex);
 			}
 		});
 
 		Assert.assertEquals(desiredNeighborsSequence, actualNeighborsSequence);
 	}
 
 	/**
 	 * This test method assures that the neighbors of a vertex are always
 	 * processed second the topological ordering (see the override of equals()
 	 * to capture the criterion)
 	 */
 	@Test
 	public void neighborsRelationIsTopologicOrdered() {
 
 		String compartment_id = "compartment_id";
 		Vertex vertex = VertexFactory.makeSimpleVertex("A", compartment_id);
 		Vertex a1 = VertexFactory.makeSimpleVertex("B", compartment_id);
 		Vertex a2 = VertexFactory.makeSimpleVertex("C", compartment_id);
 		Vertex a3 = VertexFactory.makeSimpleVertex("D", compartment_id);
 
 		vertex.addNeighbour(a3);
 		vertex.addNeighbour(a2);
 		vertex.addNeighbour(a1);
 
 		List<Vertex> desiredNeighborsSequence = new LinkedList<Vertex>(
 				Arrays.asList(a1, a2, a3));
 
 		final List<Vertex> actualNeighborsSequence = new LinkedList<Vertex>();
 
 		vertex.doOnNeighbors(new VertexLogicApplier() {
 
 			@Override
 			public void apply(Vertex neighbourVertex) {
 				actualNeighborsSequence.add(neighbourVertex);
 			}
 		});
 
 		Assert.assertEquals(desiredNeighborsSequence, actualNeighborsSequence);
 	}
 
 	@Test
 	public void fluentAddNeighbourReturnTheSameVertex() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a = VertexFactory.makeSimpleVertex();
 
 		Vertex vertex2 = vertex.addNeighbour(a);
 		assertSame("The vertex returned bySimpleVertex.addNeighbour(...) "
 				+ "is a completely new object.", vertex, vertex2);
 	}
 
 	@Test
 	public void encapsulatedNeighboursDoApply() {
 		Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a = VertexFactory.makeSimpleVertex();
 		Vertex b = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(a).addNeighbour(b);
 
 		Vertex c = VertexFactory.makeSimpleVertex();
 
 		final Set<Vertex> collectedNeighbours = new HashSet<Vertex>();
 
 		VertexLogicApplier applier = new VertexLogicApplier() {
 
 			@Override
 			public void apply(Vertex vertex) {
 				collectedNeighbours.add(vertex);
 			}
 		};
 
 		vertex.doOnNeighbors(applier);
 
 		assertEquals(2, collectedNeighbours.size());
 		assertTrue(collectedNeighbours.contains(a));
 		assertTrue(collectedNeighbours.contains(b));
 		assertFalse(collectedNeighbours.contains(c));
 		assertTrue(vertex.isYourNeighborhoodEquals(collectedNeighbours));
 	}
 
 	@Test
 	public void encapsulatedNeighboursDoApplyWithNeighborhoodRelation() {
 
 		final Vertex vertex = VertexFactory.makeSimpleVertex();
 		Vertex a = VertexFactory.makeSimpleVertex();
 		Vertex b = VertexFactory.makeSimpleVertex();
 
 		vertex.addNeighbour(a).addNeighbour(b);
 
 		Vertex c = VertexFactory.makeSimpleVertex();
 
 		final Set<Vertex> collectedNeighbours = new HashSet<Vertex>();
 
 		VertexLogicApplierWithNeighborhoodRelation applier = new VertexLogicApplierWithNeighborhoodRelation() {
 
 			@Override
 			public void apply(Vertex parent, Vertex neighbour) {
 				collectedNeighbours.add(neighbour);
 
 				Assert.assertEquals(vertex, parent);
 				Assert.assertSame(vertex, parent);
 			}
 
 		};
 
 		vertex.doOnNeighbors(applier);
 
 		assertEquals(2, collectedNeighbours.size());
 		assertTrue(collectedNeighbours.contains(a));
 		assertTrue(collectedNeighbours.contains(b));
 		assertFalse(collectedNeighbours.contains(c));
 		assertTrue(vertex.isYourNeighborhoodEquals(collectedNeighbours));
 	}
 
 	@Test
 	public void makeVertexWithId() {
 		String species_id = "species_id";
 		String compartment_id = "compartment_id";
 
 		Species prototypeSpecies = new Species(species_id);
 		prototypeSpecies.setCompartment(compartment_id);
 
 		Vertex v = VertexFactory.makeSimpleVertex(species_id, compartment_id);
 
 		Assert.assertNotNull(v);
 		assertTrue(v.isYourOrigin(prototypeSpecies));
 		assertTrue(v.isYourSpeciesId(species_id));
 		assertTrue(v.isYourCompartmentId(compartment_id));
 	}
 
 	@Test
 	public void makeVertexWithoutSpecifingId() {
 		Vertex v = VertexFactory.makeSimpleVertex();
 
 		Assert.assertNotNull(v);
 		Assert.assertFalse(v.isYourSpeciesId(""));
 		Assert.assertFalse(v.isYourCompartmentId(""));
 		Assert.assertFalse(v.isYourOrigin(new Species()));
 	}
 
 	@Test
 	public void check_vertex_creation_with_species_name() {
 
 		String species_id = "species_id";
 		String species_name = "species_name";
 		String compartment_id = "compartment_id";
 
 		Vertex v = VertexFactory.makeSimpleVertex(species_id, species_name,
 				compartment_id);
 
 		Assert.assertNotNull(v);
 		Assert.assertTrue(v.isYourSpeciesId(species_id));
 		Assert.assertTrue(v.isYourSpeciesName(species_name));
 		Assert.assertTrue(v.isYourCompartmentId(compartment_id));
 		Assert.assertFalse(v.isYourOrigin(new Species()));
 	}
 
 	@Test
 	public void check_vertex_unique_identifier_with_species_name() {
 
 		String species_id = "species_id";
 		String species_name = "species_name";
 		String compartment_id = "compartment_id";
 
 		Vertex v = VertexFactory.makeSimpleVertex(species_id, species_name,
 				compartment_id);
 
 		String expected = species_id + "-(" + species_name + ")-("
 				+ compartment_id + ")";
 
 		Assert.assertEquals("SPECIES_ID-(SPECIES_NAME)-(COMPARTMENT_ID)",
 				expected.toUpperCase());
 
 		Assert.assertEquals(expected.toUpperCase(),
 				v.buildVertexUniqueIdentifier());
 	}
 
 	@Test
 	public void check_vertex_unique_identifier_without_species_name() {
 
 		String species_id = "species_id";
 		String species_name = "";
 		String compartment_id = "compartment_id";
 
 		Vertex v = VertexFactory.makeSimpleVertex(species_id, species_name,
 				compartment_id);
 
		String expected = species_id + "-(" + species_name + ")-("
				+ compartment_id + ")";
 
		Assert.assertEquals("SPECIES_ID-()-(COMPARTMENT_ID)",
 				expected.toUpperCase());
 
 		Assert.assertEquals(expected.toUpperCase(),
 				v.buildVertexUniqueIdentifier());
 	}
 
 	@Test
 	public void matchSpeciesCompartement() {
 
 		Vertex v1 = VertexFactory.makeSimpleVertex();
 		Vertex v2 = VertexFactory.makeSimpleVertex();
 
 		Assert.assertNotSame(v1, v2);
 		Assert.assertTrue(v1.matchCompartmentWith(v2));
 		Assert.assertTrue(v2.matchCompartmentWith(v1));
 
 		Assert.assertFalse(v1.matchSpeciesWith(v2));
 		Assert.assertFalse(v2.matchSpeciesWith(v1));
 
 	}
 
 	@Test
 	public void checkConsecutiveGeneretionMakesDifferentVertices() {
 
 		// catch the count of the vertices added so far
 		int count = VertexIntegerEnumerator.getCurrentEnumerationValue();
 
 		// increment the counter
 		Vertex v1 = VertexFactory.makeSimpleVertex();
 
 		// catch the new value of the counter
 		int secondCount = VertexIntegerEnumerator.getCurrentEnumerationValue();
 
 		// increment the counter another time
 		Vertex v2 = VertexFactory.makeSimpleVertex();
 
 		Assert.assertFalse(v1.equals(v2));
 
 		Assert.assertEquals(secondCount, count + 1);
 
 	}
 
 	@Test
 	public void emptyNeighborhood() {
 
 		Vertex v1 = VertexFactory.makeSimpleVertex();
 
 		Assert.assertTrue(v1.isYourNeighborhoodEmpty());
 
 		Assert.assertTrue(v1.isNeighborsCountEquals(0));
 	}
 
 	@Test
 	public void nonEmptyNeighborhood() {
 
 		Vertex v1 = VertexFactory.makeSimpleVertex();
 		Vertex v2 = VertexFactory.makeSimpleVertex();
 
 		v1.addNeighbour(v2);
 
 		Assert.assertFalse(v1.isYourNeighborhoodEmpty());
 	}
 
 	@Test
 	public void neighborhoodEquals() {
 
 		Vertex v1 = VertexFactory.makeSimpleVertex();
 		Vertex v2 = VertexFactory.makeSimpleVertex();
 		Vertex v3 = VertexFactory.makeSimpleVertex();
 		Vertex v4 = VertexFactory.makeSimpleVertex();
 
 		v1.addNeighbour(v2);
 		v1.addNeighbour(v3);
 		v1.addNeighbour(v4);
 
 		Set<Vertex> expected = new HashSet<Vertex>();
 
 		expected.add(v2);
 		expected.add(v3);
 		expected.add(v4);
 
 		Assert.assertFalse(v1.isYourNeighborhoodEmpty());
 		Assert.assertTrue(v1.isYourNeighborhoodEquals(expected));
 	}
 
 	@Test
 	public void neighborhoodNotEquals() {
 
 		Vertex v1 = VertexFactory.makeSimpleVertex();
 		Vertex v2 = VertexFactory.makeSimpleVertex();
 		Vertex v3 = VertexFactory.makeSimpleVertex();
 		Vertex v4 = VertexFactory.makeSimpleVertex();
 
 		v1.addNeighbour(v2);
 		v1.addNeighbour(v3);
 		v1.addNeighbour(v4);
 
 		Set<Vertex> anotherNeighbourhood = new HashSet<Vertex>();
 		Vertex v5 = VertexFactory.makeSimpleVertex();
 
 		anotherNeighbourhood.add(v3);
 		anotherNeighbourhood.add(v5);
 
 		Assert.assertFalse(v1.isYourNeighborhoodEmpty());
 		Assert.assertFalse(v1.isYourNeighborhoodEquals(anotherNeighbourhood));
 	}
 
 	@Test
 	public void neighbourhoodEqualsVacouslyTrue() {
 
 		Vertex v1 = VertexFactory.makeSimpleVertex();
 
 		// v1 has empty neighborhood
 
 		Set<Vertex> anotherNeighborhood = new HashSet<Vertex>();
 		Vertex v3 = VertexFactory.makeSimpleVertex();
 		Vertex v4 = VertexFactory.makeSimpleVertex();
 		Vertex v5 = VertexFactory.makeSimpleVertex();
 
 		anotherNeighborhood.add(v3);
 		anotherNeighborhood.add(v4);
 		anotherNeighborhood.add(v5);
 
 		Assert.assertFalse(v1.isYourNeighborhoodEquals(anotherNeighborhood));
 	}
 
 	@Test
 	public void neighborhoodEqualsVacouslyTrueReversed() {
 
 		Vertex v1 = VertexFactory.makeSimpleVertex();
 		Vertex v2 = VertexFactory.makeSimpleVertex();
 		Vertex v3 = VertexFactory.makeSimpleVertex();
 		Vertex v4 = VertexFactory.makeSimpleVertex();
 
 		v1.addNeighbour(v2);
 		v1.addNeighbour(v3);
 		v1.addNeighbour(v4);
 
 		// empty testing neighborhood
 		Set<Vertex> anotherNeighborhood = new HashSet<Vertex>();
 
 		Assert.assertFalse(v1.isYourNeighborhoodEquals(anotherNeighborhood));
 	}
 
 	@Test
 	public void neighborhoodDoesntContainsDoubles() {
 
 		Vertex v1 = VertexFactory.makeSimpleVertex();
 		Vertex v2 = VertexFactory.makeSimpleVertex();
 		Vertex v3 = VertexFactory.makeSimpleVertex();
 		Vertex v4 = VertexFactory.makeSimpleVertex();
 
 		v1.addNeighbour(v2);
 		v1.addNeighbour(v3);
 		v1.addNeighbour(v4);
 
 		// doubling
 		v1.addNeighbour(v4);
 		v1.addNeighbour(v3);
 		v1.addNeighbour(v2);
 
 		// empty testing neighborhood
 		Set<Vertex> anotherNeighborhood = new HashSet<Vertex>();
 		anotherNeighborhood.add(v2);
 		anotherNeighborhood.add(v3);
 		anotherNeighborhood.add(v4);
 
 		Assert.assertTrue(v1.isNeighborsCountEquals(3));
 		Assert.assertTrue(v1.isYourNeighborhoodEquals(anotherNeighborhood));
 	}
 
 	@Test
 	public void fetchSpeciesCharacteristicData() {
 		Model sbmlModel = new Model();
 
 		String compartmentId = "compartment_id";
 		Compartment compartment = sbmlModel.createCompartment(compartmentId);
 
 		String speciesId = "species_id";
 		Species firstAdded = sbmlModel.createSpecies(speciesId, compartment);
 
 		Vertex vertex = VertexFactory.makeSimpleVertex(firstAdded);
 
 		Assert.assertNotNull(vertex);
 		Assert.assertTrue(vertex.isYourSpeciesId(speciesId));
 		Assert.assertTrue(vertex.isYourCompartmentId(compartmentId));
 		Assert.assertFalse(vertex.isYourSpeciesId(""));
 		Assert.assertFalse(vertex.isYourCompartmentId(""));
 	}
 
 	@Test
 	public void vertexEquivalenceSameSpeciesIdDifferentCompartmentId() {
 
 		String compartmentId = "compartment_id";
 		Compartment compartment = new Compartment(compartmentId);
 
 		String anotherCompartmentId = "anotherCompartment_id";
 		Compartment anotherCompartment = new Compartment(anotherCompartmentId);
 
 		String speciesId = "species_id";
 		Species firstAdded = new Species(speciesId);
 		firstAdded.setCompartment(compartment);
 
 		Species secondAdded = new Species(speciesId);
 		secondAdded.setCompartment(anotherCompartment);
 
 		Vertex vertex = VertexFactory.makeSimpleVertex(firstAdded);
 		Vertex anotherVertex = VertexFactory.makeSimpleVertex(secondAdded);
 
 		Assert.assertNotSame(vertex, anotherVertex);
 		Assert.assertFalse(vertex.equals(anotherVertex));
 	}
 
 	@Test
 	public void vertexEquivalenceDifferentSpeciesIdSameCompartmentId() {
 
 		String compartmentId = "compartment_id";
 		Compartment compartment = new Compartment(compartmentId);
 
 		Species firstAdded = new Species("species_id");
 		firstAdded.setCompartment(compartment);
 
 		Species secondAdded = new Species("another_species_id");
 		secondAdded.setCompartment(compartment);
 
 		Vertex vertex = VertexFactory.makeSimpleVertex(firstAdded);
 		Vertex anotherVertex = VertexFactory.makeSimpleVertex(secondAdded);
 
 		Assert.assertNotSame(vertex, anotherVertex);
 		Assert.assertFalse(vertex.equals(anotherVertex));
 	}
 
 	@Test
 	public void vertexIsYourOrigin() {
 
 		String compartmentId = "compartment_id";
 		Compartment compartment = new Compartment(compartmentId);
 
 		Species firstAdded = new Species("species_id");
 		firstAdded.setCompartment(compartment);
 
 		Species secondAdded = new Species("another_species_id");
 		secondAdded.setCompartment(compartment);
 
 		Vertex vertex = VertexFactory.makeSimpleVertex(firstAdded);
 
 		Assert.assertTrue(vertex.isYourOrigin(firstAdded));
 		Assert.assertFalse(vertex.isYourOrigin(secondAdded));
 	}
 
 	@Test
 	public void vertexComparableMathContract() {
 
 		String compartmentId = "compartment_id";
 		Compartment compartment = new Compartment(compartmentId);
 
 		Species firstAdded = new Species("species_id");
 		firstAdded.setCompartment(compartment);
 
 		Species secondAdded = new Species("another_species_id");
 		secondAdded.setCompartment(compartment);
 
 		Species thirdAdded = new Species("another_another_species_id");
 		thirdAdded.setCompartment(compartment);
 
 		Vertex vertex = VertexFactory.makeSimpleVertex(firstAdded);
 		Vertex otherVertex = VertexFactory.makeSimpleVertex(secondAdded);
 		Vertex anotherVertex = VertexFactory.makeSimpleVertex(thirdAdded);
 
 		Assert.assertEquals(vertex.compareTo(otherVertex),
 				-otherVertex.compareTo(vertex));
 
 		Assert.assertEquals(
 				vertex.compareTo(otherVertex) > 0
 						&& otherVertex.compareTo(anotherVertex) > 0,
 				vertex.compareTo(anotherVertex) > 0);
 	}
 
 	@Test
 	public void vertexComparableEquals() {
 
 		String compartmentId = "compartment_id";
 		Compartment compartment = new Compartment(compartmentId);
 
 		Species firstAdded = new Species("species_id");
 		firstAdded.setCompartment(compartment);
 
 		Species secondAdded = new Species("another_species_id");
 		secondAdded.setCompartment(compartment);
 
 		Vertex vertex = VertexFactory.makeSimpleVertex(firstAdded);
 		Vertex otherVertex = VertexFactory.makeSimpleVertex(firstAdded);
 		Vertex anotherVertex = VertexFactory.makeSimpleVertex(secondAdded);
 
 		Assert.assertEquals(vertex.compareTo(otherVertex) == 0, vertex
 				.compareTo(anotherVertex) == otherVertex
 				.compareTo(anotherVertex));
 	}
 
 	@Test
 	public void checkCloneStaticMethod() {
 
 		String compartmentId = "compartment_id";
 		Compartment compartment = new Compartment(compartmentId);
 
 		Species firstAdded = new Species("species_id");
 		firstAdded.setCompartment(compartment);
 
 		Vertex vertex = VertexFactory.makeSimpleVertex(firstAdded);
 
 		Vertex clonedVertex = VertexFactory.makeSimpleVertex(vertex);
 
 		Assert.assertEquals(vertex, clonedVertex);
 		Assert.assertNotSame(vertex, clonedVertex);
 		Assert.assertTrue(clonedVertex.isYourNeighborhoodEmpty());
 	}
 
 	@Test
 	public void brokeYourNeighborhoodRelationsVacously() {
 
 		Vertex source = VertexFactory.makeSimpleVertex();
 
 		source.brokeYourNeighborhoodRelations();
 
 		Assert.assertTrue(source.isYourNeighborhoodEmpty());
 	}
 
 	@Test
 	public void brokeDirectAncestorRelationWithSimple() {
 
 		Vertex source = VertexFactory.makeSimpleVertex();
 		Vertex sink = VertexFactory.makeSimpleVertex();
 
 		source.addNeighbour(sink);
 
 		sink.brokeDirectAncestorRelationWith(source);
 
 		Assert.assertFalse(source.isYourNeighborhoodEmpty());
 		Assert.assertTrue(sink.isYourNeighborhoodEmpty());
 	}
 
 	@Test
 	public void brokeDirectAncestorRelationWithVacously() {
 
 		Vertex source = VertexFactory.makeSimpleVertex();
 		Vertex sink = VertexFactory.makeSimpleVertex();
 
 		source.addNeighbour(sink);
 
 		sink.brokeDirectAncestorRelationWith(VertexFactory.makeSimpleVertex());
 
 		Assert.assertFalse(source.isYourNeighborhoodEmpty());
 		Assert.assertFalse(sink.isYourAncestorsEmpty());
 	}
 
 	@Test
 	public void brokeDirectAncestorRelationWithIbridSituation() {
 
 		Vertex source = VertexFactory.makeSimpleVertex();
 		Vertex sink = VertexFactory.makeSimpleVertex();
 		Vertex sink2 = VertexFactory.makeSimpleVertex();
 
 		source.addNeighbour(sink).addNeighbour(sink2);
 
 		sink.brokeDirectAncestorRelationWith(source);
 
 		Set<Vertex> expectedNeighborhood = new HashSet<Vertex>();
 		expectedNeighborhood.add(sink2);
 
 		Assert.assertFalse(source
 				.isYourNeighborhoodEquals(expectedNeighborhood));
 		Assert.assertFalse(sink2.isYourAncestorsEmpty());
 		Assert.assertTrue(sink.isYourAncestorsEmpty());
 	}
 
 	@Test
 	public void brokeYourNeighborhoodRelations() {
 
 		Vertex source = VertexFactory.makeSimpleVertex();
 		Vertex sink = VertexFactory.makeSimpleVertex();
 		Vertex sink2 = VertexFactory.makeSimpleVertex();
 
 		source.addNeighbour(sink).addNeighbour(sink2);
 
 		source.brokeYourNeighborhoodRelations();
 
 		Assert.assertTrue(source.isYourNeighborhoodEmpty());
 		Assert.assertTrue(sink2.isYourAncestorsEmpty());
 		Assert.assertTrue(sink.isYourAncestorsEmpty());
 	}
 }
