 package model;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 
 import junit.framework.Assert;
 import model.ConnectedComponentInfoRecorder.ConnectedComponentInfoDataStructure;
 
 import org.junit.Test;
 
 import dotInterface.DotFileUtilHandler;
 
 public class ConnectedComponentInfoRecorderUnitTest {
 
 	@Test
 	public void just_created_recorder_should_have_empty_map() {
 
 		ConnectedComponentInfoRecorder recorder = new ConnectedComponentInfoRecorder();
 
 		String species = "my_species_identifier";
 
 		TreeMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>> someMap = new TreeMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>>();
 
 		someMap.put(species,
 				new TreeMap<String, SortedMap<Integer, SortedSet<String>>>());
 
 		Assert.assertTrue(recorder
 				.isDataStructureEquals(new ConnectedComponentInfoDataStructure()));
 
 		Assert.assertFalse(recorder
 				.isDataStructureEquals(new ConnectedComponentInfoDataStructure(
 						someMap)));
 
 		Assert.assertFalse(recorder.isDataStructureEquals(null));
 	}
 
 	@Test
 	public void putting_a_species_into_new_recorder_should_contains_only_the_putted_species() {
 
 		ConnectedComponentInfoRecorder recorder = new ConnectedComponentInfoRecorder();
 
 		String species = "my_species";
 		String componentType = VertexType.Sources.toString();
 		int cardinality = 1;
 		String modelName = "model1";
 
 		recorder.putTuple(species, componentType, cardinality, modelName);
 
 		TreeMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>> expectedMap = new TreeMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>>();
 
 		ConnectedComponentInfoDataStructure.putIntoMap(expectedMap, species,
 				componentType, cardinality, modelName);
 
 		TreeMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>> unexpectedMap = new TreeMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>>();
 
 		// do some permutation on the expected values in order to distinguish
 		// this map from the previous one
 		ConnectedComponentInfoDataStructure.putIntoMap(unexpectedMap, species
 				+ "hello", componentType + "hello", cardinality + 2, modelName);
 
 		Assert.assertTrue(recorder
 				.isDataStructureEquals(new ConnectedComponentInfoDataStructure(
 						expectedMap)));
 
 		Assert.assertFalse(recorder
 				.isDataStructureEquals(new ConnectedComponentInfoDataStructure(
 						unexpectedMap)));
 
 		Assert.assertFalse(recorder.isDataStructureEquals(null));
 	}
 
 	@Test
 	public void serializing_the_map_should_produce_an_non_empty_file() {
 
 		ConnectedComponentInfoRecorder recorder = new ConnectedComponentInfoRecorder();
 
 		String species = "my_species";
 		String componentType = VertexType.Sources.toString();
 		int cardinality = 1;
 		String modelName = "model1";
 
 		recorder.putTuple(species, componentType, cardinality, modelName);
 
 		String pathname = DotFileUtilHandler
 				.dotOutputFolderPathName()
 				.concat("simple-serialization-of-connected-component-data-structure.txt");
 
 		File destinationFile = new File(pathname);
 
 		if (destinationFile.exists() == true) {
 			destinationFile.delete();
 		}
 
 		try {
 			Assert.assertFalse(destinationFile.exists());
 
 			FileOutputStream fileOutputStream = new FileOutputStream(
 					destinationFile);
 
 			recorder.writeDataStructure(fileOutputStream);
 
 			fileOutputStream.close();
 
 			Assert.assertTrue(destinationFile.exists());
 
 		} catch (FileNotFoundException e) {
 			Assert.fail("The file should be written in the destination path.");
 		} catch (IOException e) {
 			Assert.fail("The file should be written in the destination path.");
 		}
 	}
 
 	@Test
 	public void serializing_the_map_and_deserializing_it_should_produce_an_identity() {
 
 		ConnectedComponentInfoRecorder recorder = new ConnectedComponentInfoRecorder();
 
 		String species = "my_species";
 		String componentType = VertexType.Sources.toString();
 		int cardinality = 1;
 		String modelName = "model1";
 
 		recorder.putTuple(species, componentType, cardinality, modelName);
 
 		String pathname = DotFileUtilHandler
 				.dotOutputFolderPathName()
 				.concat("serialization-for-inverse-checking-of-connected-component-data-structure.txt");
 
 		File destinationFile = new File(pathname);
 
 		if (destinationFile.exists() == true) {
 			destinationFile.delete();
 		}
 
 		try {
 			Assert.assertFalse(destinationFile.exists());
 
 			FileOutputStream fileOutputStream = new FileOutputStream(
 					destinationFile);
 
 			recorder.writeDataStructure(fileOutputStream);
 
 			fileOutputStream.close();
 
 			Assert.assertTrue(destinationFile.exists());
 
 		} catch (FileNotFoundException e) {
 			Assert.fail("The file should be written in the destination path.");
 		} catch (IOException e) {
 			Assert.fail("The file should be written in the destination path.");
 		}
 
 		try {
 			InputStream fileInputStream = new FileInputStream(destinationFile);
 			ConnectedComponentInfoDataStructure deserialized = new ConnectedComponentInfoDataStructure(
 					fileInputStream);
 
 			Assert.assertTrue(recorder.isDataStructureEquals(deserialized));
 		} catch (FileNotFoundException e) {
 			Assert.fail("The file should be present, inconsistency with the previous assertions.");
 		}
 	}
 
 	@Test
 	public void recording_an_isolated_connected_component_should_modify_the_data_structure() {
 
 		ConnectedComponentInfoRecorder recorder = new ConnectedComponentInfoRecorder();
 
 		ConnectedComponentWrapperVertex componentWrapperVertex = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		String species = "my_species_identifier";
 		String componentType = VertexType.Sinks.toString();
 		int cardinality = 1;
 		String modelName = "model";
 
 		// setting up the connected component to have one member and one
 		// neighbor in order to be a source (otherwise we cannot distinguish if
 		// it is a source or a sink)
 		Vertex vertex = VertexFactory.makeSimpleVertex(species,
 				OurModel.getDefaultCompartmentId());
 
 		Set<Vertex> vertices = new HashSet<Vertex>();
 		vertices.add(vertex);
 
 		@SuppressWarnings("unused")
 		OurModel model = OurModel.makeOurModelFrom(vertices, modelName);
 
 		componentWrapperVertex.includeMember(vertex);
 
 		componentWrapperVertex.publishYourContentOn(recorder);
 
 		SortedMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>> expectedMap = new TreeMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>>();
 
 		ConnectedComponentInfoDataStructure.putIntoMap(expectedMap,
				"MY_SPECIES_IDENTIFIER-()-(COMPARTMENT_ID)", componentType,
 				cardinality, modelName);
 
 		Assert.assertTrue(recorder
 				.isDataStructureEquals(new ConnectedComponentInfoDataStructure(
 						expectedMap)));
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void connected_component_without_members_should_throw_exception_when_asked_for_model_name() {
 
 		ConnectedComponentWrapperVertex connected_component = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		connected_component.findModelName();
 	}
 
 	@Test
 	public void connected_component_with_some_members_should_find_something_when_asked_for_model_name() {
 
 		ConnectedComponentWrapperVertex componentWrapperVertex = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		String modelName = "model";
 
 		Vertex vertex_one = VertexFactory.makeSimpleVertex("species_one",
 				OurModel.getDefaultCompartmentId());
 
 		Vertex vertex_two = VertexFactory.makeSimpleVertex("species_two",
 				OurModel.getDefaultCompartmentId());
 
 		Vertex vertex_three = VertexFactory.makeSimpleVertex("species_three",
 				OurModel.getDefaultCompartmentId());
 
 		Set<Vertex> vertices = new HashSet<Vertex>();
 		vertices.add(vertex_one);
 		vertices.add(vertex_two);
 		vertices.add(vertex_three);
 
 		@SuppressWarnings("unused")
 		OurModel model = OurModel.makeOurModelFrom(vertices, modelName);
 
 		componentWrapperVertex.includeMember(vertex_one);
 		componentWrapperVertex.includeMember(vertex_two);
 		componentWrapperVertex.includeMember(vertex_three);
 
 		Assert.assertEquals(modelName, componentWrapperVertex.findModelName());
 	}
 
 	@Test
 	public void building_map_from_only_one_model_should_produce_flat_result() {
 		ConnectedComponentInfoRecorder recorder = new ConnectedComponentInfoRecorder();
 
 		SortedMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>> expectedMap = new TreeMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>>();
 
 		this.build_same_context_with_changes_on_model(recorder, "model_one",
 				expectedMap);
 
 		Assert.assertTrue(recorder
 				.isDataStructureEquals(new ConnectedComponentInfoDataStructure(
 						expectedMap)));
 	}
 
 	@Test
 	public void building_map_from_two_models_with_the_same_vertices_structures() {
 
 		ConnectedComponentInfoRecorder recorder = new ConnectedComponentInfoRecorder();
 
 		SortedMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>> expectedMap = new TreeMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>>();
 
 		this.build_same_context_with_changes_on_model(recorder, "model_one",
 				expectedMap);
 
 		this.build_same_context_with_changes_on_model(recorder, "model_two",
 				expectedMap);
 
 		Assert.assertTrue(recorder
 				.isDataStructureEquals(new ConnectedComponentInfoDataStructure(
 						expectedMap)));
 	}
 
 	private void build_same_context_with_changes_on_model(
 			ConnectedComponentInfoRecorder recorder,
 			String modelName,
 			SortedMap<String, SortedMap<String, SortedMap<Integer, SortedSet<String>>>> expectedMap) {
 
 		// creating the components
 		ConnectedComponentWrapperVertex scc_one = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex scc_two = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex scc_three = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		// creating the vertices
 		Vertex vertex_one = VertexFactory.makeSimpleVertex("species_one",
 				OurModel.getDefaultCompartmentId());
 
 		Vertex vertex_two = VertexFactory.makeSimpleVertex("species_two",
 				OurModel.getDefaultCompartmentId());
 
 		Vertex vertex_three = VertexFactory.makeSimpleVertex("species_three",
 				OurModel.getDefaultCompartmentId());
 
 		Vertex vertex_four = VertexFactory.makeSimpleVertex("species_four",
 				OurModel.getDefaultCompartmentId());
 
 		Vertex vertex_five = VertexFactory.makeSimpleVertex("species_five",
 				OurModel.getDefaultCompartmentId());
 
 		Vertex vertex_six = VertexFactory.makeSimpleVertex("species_six",
 				OurModel.getDefaultCompartmentId());
 
 		// setting up neighborhood relations
 		vertex_one.addNeighbour(vertex_two).addNeighbour(vertex_six);
 		vertex_two.addNeighbour(vertex_one).addNeighbour(vertex_three);
 		vertex_three.addNeighbour(vertex_four).addNeighbour(vertex_six);
 		vertex_four.addNeighbour(vertex_five);
 		vertex_five.addNeighbour(vertex_three);
 
 		// building the set in order to build the model
 		Set<Vertex> vertices = new HashSet<Vertex>();
 		vertices.add(vertex_one);
 		vertices.add(vertex_two);
 		vertices.add(vertex_three);
 		vertices.add(vertex_four);
 		vertices.add(vertex_five);
 		vertices.add(vertex_six);
 
 		// the following statement is needed only to update
 		// the vertices respect the container model relation
 		@SuppressWarnings("unused")
 		OurModel model = OurModel.makeOurModelFrom(vertices, modelName);
 
 		// including the vertices in the respective components
 		scc_one.includeMember(vertex_one);
 		scc_one.includeMember(vertex_two);
 
 		scc_two.includeMember(vertex_three);
 		scc_two.includeMember(vertex_four);
 		scc_two.includeMember(vertex_five);
 
 		scc_three.includeMember(vertex_six);
 
 		// building neighborhood relation about connected components
 		scc_one.addNeighbour(scc_two);
 		scc_two.addNeighbour(scc_three);
 
 		// publishing the informations
 		scc_one.publishYourContentOn(recorder);
 		scc_two.publishYourContentOn(recorder);
 		scc_three.publishYourContentOn(recorder);
 
		String species_identifier_suffix = "-()-(COMPARTMENT_ID)";
 
 		ConnectedComponentInfoDataStructure.putIntoMap(expectedMap,
 				"SPECIES_ONE".concat(species_identifier_suffix),
 				VertexType.Sources.toString(), 2, modelName);
 		ConnectedComponentInfoDataStructure.putIntoMap(expectedMap,
 				"SPECIES_TWO".concat(species_identifier_suffix),
 				VertexType.Sources.toString(), 2, modelName);
 		ConnectedComponentInfoDataStructure.putIntoMap(expectedMap,
 				"SPECIES_THREE".concat(species_identifier_suffix),
 				VertexType.Whites.toString(), 3, modelName);
 		ConnectedComponentInfoDataStructure.putIntoMap(expectedMap,
 				"SPECIES_FOUR".concat(species_identifier_suffix),
 				VertexType.Whites.toString(), 3, modelName);
 		ConnectedComponentInfoDataStructure.putIntoMap(expectedMap,
 				"SPECIES_FIVE".concat(species_identifier_suffix),
 				VertexType.Whites.toString(), 3, modelName);
 		ConnectedComponentInfoDataStructure.putIntoMap(expectedMap,
 				"SPECIES_SIX".concat(species_identifier_suffix),
 				VertexType.Sinks.toString(), 1, modelName);
 	}
 }
