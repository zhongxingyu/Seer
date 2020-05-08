 package model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import javax.xml.stream.XMLStreamException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.sbml.jsbml.Model;
 import org.sbml.jsbml.SBMLDocument;
 import org.sbml.jsbml.SBMLReader;
 import org.sbml.jsbml.Species;
 
 import piping.PipeFilter;
 import piping.PipeFilterFactory;
 import piping.PlainTextInfoComputationListener;
 import util.IntegerCounter;
 import dotInterface.DotFileUtilHandler;
 import dotInterface.DotFileUtilHandler.DotUtilAction;
 
 public class VertexStatsRecorderUnitTest {
 
 	@Test
 	public void record_stats_information_from_simple_vertices() {
 		VertexStatsRecorder recorder = new VertexStatsRecorder();
 
 		Vertex simple = VertexFactory.makeSimpleVertex();
 		Vertex simple2 = VertexFactory.makeSimpleVertex();
 		Vertex simple3 = VertexFactory.makeSimpleVertex();
 		Vertex simple4 = VertexFactory.makeSimpleVertex();
 
 		simple.addNeighbour(simple2).addNeighbour(simple3)
 				.addNeighbour(simple4);
 
 		simple2.addNeighbour(simple3).addNeighbour(simple4);
 
 		simple3.addNeighbour(simple4);
 
 		simple.publishYourStatsOn(recorder);
 		simple2.publishYourStatsOn(recorder);
 		simple3.publishYourStatsOn(recorder);
 		simple4.publishYourStatsOn(recorder);
 
 		Map<PlainTextStatsComponents, Integer> expected = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expected.put(PlainTextStatsComponents.NOfVertices, 4);
 		expected.put(PlainTextStatsComponents.NOfEdges, 6);
 		expected.put(PlainTextStatsComponents.NOfSources, 1);
 		expected.put(PlainTextStatsComponents.NOfSinks, 1);
 		expected.put(PlainTextStatsComponents.NOfWhites, 2);
 
 		Assert.assertTrue(recorder.isSimpleVerticesVotesEquals(expected));
 		Assert.assertFalse(recorder
 				.isSimpleVerticesVotesEquals(new HashMap<PlainTextStatsComponents, Integer>()));
 
 	}
 
 	@Test
 	public void record_average_with_one_model_stats_information_from_simple_vertices_should_return_the_same() {
 		VertexStatsRecorder recorder = new VertexStatsRecorder();
 
 		Vertex simple = VertexFactory.makeSimpleVertex();
 		Vertex simple2 = VertexFactory.makeSimpleVertex();
 		Vertex simple3 = VertexFactory.makeSimpleVertex();
 		Vertex simple4 = VertexFactory.makeSimpleVertex();
 
 		simple.addNeighbour(simple2).addNeighbour(simple3)
 				.addNeighbour(simple4);
 
 		simple2.addNeighbour(simple3).addNeighbour(simple4);
 
 		simple3.addNeighbour(simple4);
 
 		simple.publishYourStatsOn(recorder);
 		simple2.publishYourStatsOn(recorder);
 		simple3.publishYourStatsOn(recorder);
 		simple4.publishYourStatsOn(recorder);
 
 		Map<PlainTextStatsComponents, Integer> expected = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expected.put(PlainTextStatsComponents.NOfVertices, 4);
 		expected.put(PlainTextStatsComponents.NOfEdges, 6);
 		expected.put(PlainTextStatsComponents.NOfSources, 1);
 		expected.put(PlainTextStatsComponents.NOfSinks, 1);
 		expected.put(PlainTextStatsComponents.NOfWhites, 2);
 
 		Assert.assertTrue(recorder.average(1).isSimpleVerticesVotesEquals(
 				expected));
 		Assert.assertFalse(recorder.average(1).isSimpleVerticesVotesEquals(
 				new HashMap<PlainTextStatsComponents, Integer>()));
 
 	}
 
 	@Test
 	public void record_average_with_two_model_stats_information_from_simple_vertices_should_change() {
 		VertexStatsRecorder recorder = new VertexStatsRecorder();
 
 		Vertex simple = VertexFactory.makeSimpleVertex();
 		Vertex simple2 = VertexFactory.makeSimpleVertex();
 		Vertex simple3 = VertexFactory.makeSimpleVertex();
 		Vertex simple4 = VertexFactory.makeSimpleVertex();
 
 		simple.addNeighbour(simple2).addNeighbour(simple3)
 				.addNeighbour(simple4);
 
 		simple2.addNeighbour(simple3).addNeighbour(simple4);
 
 		simple3.addNeighbour(simple4);
 
 		simple.publishYourStatsOn(recorder);
 		simple2.publishYourStatsOn(recorder);
 		simple3.publishYourStatsOn(recorder);
 		simple4.publishYourStatsOn(recorder);
 
 		Map<PlainTextStatsComponents, Integer> expected = new HashMap<PlainTextStatsComponents, Integer>();
 
 		int counter = 2;
 
 		expected.put(PlainTextStatsComponents.NOfVertices,
 				VertexVoteAccepter.DivideAndRound(4, counter));
 		expected.put(PlainTextStatsComponents.NOfEdges,
 				VertexVoteAccepter.DivideAndRound(6, counter));
 		expected.put(PlainTextStatsComponents.NOfSources,
 				VertexVoteAccepter.DivideAndRound(1, counter));
 		expected.put(PlainTextStatsComponents.NOfSinks,
 				VertexVoteAccepter.DivideAndRound(1, counter));
 		expected.put(PlainTextStatsComponents.NOfWhites,
 				VertexVoteAccepter.DivideAndRound(2, counter));
 
 		Assert.assertTrue(recorder.average(counter)
 				.isSimpleVerticesVotesEquals(expected));
 		Assert.assertFalse(recorder.average(counter)
 				.isSimpleVerticesVotesEquals(
 						new HashMap<PlainTextStatsComponents, Integer>()));
 
 	}
 
 	@Test
 	public void check_divide_and_round_utility_method() {
 
 		Assert.assertEquals(1, VertexVoteAccepter.DivideAndRound(4, 4));
 		Assert.assertEquals(2, VertexVoteAccepter.DivideAndRound(4, 2));
 		Assert.assertEquals(1, VertexVoteAccepter.DivideAndRound(1, 4));
 		Assert.assertEquals(0, VertexVoteAccepter.DivideAndRound(0, 4));
 		Assert.assertEquals(4, VertexVoteAccepter.DivideAndRound(16, 4));
 		Assert.assertEquals(4, VertexVoteAccepter.DivideAndRound(16, 5));
 	}
 
 	@Test
 	public void record_average_with_one_model_stats_information_from_connected_components() {
 		VertexStatsRecorder recorder = new VertexStatsRecorder();
 
 		ConnectedComponentWrapperVertex simple = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple2 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple3 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple4 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple.addNeighbour(simple2);
 
 		simple2.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple2.addNeighbour(simple3).addNeighbour(simple4);
 
 		simple3.includeMember(VertexFactory.makeSimpleVertex());
 		simple3.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple3.addNeighbour(simple4);
 
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple.publishYourStatsOn(recorder);
 		simple2.publishYourStatsOn(recorder);
 		simple3.publishYourStatsOn(recorder);
 		simple4.publishYourStatsOn(recorder);
 
 		Map<PlainTextStatsComponents, Integer> expectedFor3members = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expectedFor3members.put(PlainTextStatsComponents.NOfComponents, 2);
 		expectedFor3members.put(PlainTextStatsComponents.NOfSources, 1);
 		expectedFor3members.put(PlainTextStatsComponents.NOfSinks, 1);
 		expectedFor3members.put(PlainTextStatsComponents.NOfWhites, 0);
 		expectedFor3members.put(PlainTextStatsComponents.NOfEdges, 1);
 
 		Map<PlainTextStatsComponents, Integer> expectedFor2members = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expectedFor2members.put(PlainTextStatsComponents.NOfComponents, 1);
 		expectedFor2members.put(PlainTextStatsComponents.NOfSources, 0);
 		expectedFor2members.put(PlainTextStatsComponents.NOfSinks, 0);
 		expectedFor2members.put(PlainTextStatsComponents.NOfWhites, 1);
 		expectedFor2members.put(PlainTextStatsComponents.NOfEdges, 1);
 
 		Map<PlainTextStatsComponents, Integer> expectedFor1members = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expectedFor1members.put(PlainTextStatsComponents.NOfComponents, 1);
 		expectedFor1members.put(PlainTextStatsComponents.NOfSources, 0);
 		expectedFor1members.put(PlainTextStatsComponents.NOfSinks, 0);
 		expectedFor1members.put(PlainTextStatsComponents.NOfWhites, 1);
 		expectedFor1members.put(PlainTextStatsComponents.NOfEdges, 2);
 
 		int counter = 1;
 
 		Assert.assertTrue(recorder.average(counter).isComponentsVotesEquals(3,
 				expectedFor3members));
 		Assert.assertTrue(recorder.average(counter).isComponentsVotesEquals(2,
 				expectedFor2members));
 		Assert.assertTrue(recorder.average(counter).isComponentsVotesEquals(1,
 				expectedFor1members));
 
 	}
 
 	@Test
 	public void record_average_with_two_model_stats_information_from_connected_components() {
 		VertexStatsRecorder recorder = new VertexStatsRecorder();
 
 		ConnectedComponentWrapperVertex simple = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple2 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple3 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple4 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple.addNeighbour(simple2);
 
 		simple2.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple2.addNeighbour(simple3).addNeighbour(simple4);
 
 		simple3.includeMember(VertexFactory.makeSimpleVertex());
 		simple3.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple3.addNeighbour(simple4);
 
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple.publishYourStatsOn(recorder);
 		simple2.publishYourStatsOn(recorder);
 		simple3.publishYourStatsOn(recorder);
 		simple4.publishYourStatsOn(recorder);
 
 		int counter = 1;
 
 		Map<PlainTextStatsComponents, Integer> expectedFor3members = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expectedFor3members.put(PlainTextStatsComponents.NOfComponents,
 				VertexVoteAccepter.DivideAndRound(2, counter));
 		expectedFor3members.put(PlainTextStatsComponents.NOfSources,
 				VertexVoteAccepter.DivideAndRound(1, counter));
 		expectedFor3members.put(PlainTextStatsComponents.NOfSinks,
 				VertexVoteAccepter.DivideAndRound(1, counter));
 		expectedFor3members.put(PlainTextStatsComponents.NOfWhites,
 				VertexVoteAccepter.DivideAndRound(0, counter));
 		expectedFor3members.put(PlainTextStatsComponents.NOfEdges,
 				VertexVoteAccepter.DivideAndRound(1, counter));
 
 		Map<PlainTextStatsComponents, Integer> expectedFor2members = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expectedFor2members.put(PlainTextStatsComponents.NOfComponents,
 				VertexVoteAccepter.DivideAndRound(1, counter));
 		expectedFor2members.put(PlainTextStatsComponents.NOfSources,
 				VertexVoteAccepter.DivideAndRound(0, counter));
 		expectedFor2members.put(PlainTextStatsComponents.NOfSinks,
 				VertexVoteAccepter.DivideAndRound(0, counter));
 		expectedFor2members.put(PlainTextStatsComponents.NOfWhites,
 				VertexVoteAccepter.DivideAndRound(1, counter));
 		expectedFor2members.put(PlainTextStatsComponents.NOfEdges,
 				VertexVoteAccepter.DivideAndRound(1, counter));
 
 		Map<PlainTextStatsComponents, Integer> expectedFor1members = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expectedFor1members.put(PlainTextStatsComponents.NOfComponents,
 				VertexVoteAccepter.DivideAndRound(1, counter));
 		expectedFor1members.put(PlainTextStatsComponents.NOfSources,
 				VertexVoteAccepter.DivideAndRound(0, counter));
 		expectedFor1members.put(PlainTextStatsComponents.NOfSinks,
 				VertexVoteAccepter.DivideAndRound(0, counter));
 		expectedFor1members.put(PlainTextStatsComponents.NOfWhites,
 				VertexVoteAccepter.DivideAndRound(1, counter));
 		expectedFor1members.put(PlainTextStatsComponents.NOfEdges,
 				VertexVoteAccepter.DivideAndRound(2, counter));
 
 		Assert.assertTrue(recorder.average(counter).isComponentsVotesEquals(3,
 				expectedFor3members));
 		Assert.assertTrue(recorder.average(counter).isComponentsVotesEquals(2,
 				expectedFor2members));
 		Assert.assertTrue(recorder.average(counter).isComponentsVotesEquals(1,
 				expectedFor1members));
 
 	}
 
 	@Test
 	public void record_stats_information_from_connected_components() {
 		VertexStatsRecorder recorder = new VertexStatsRecorder();
 
 		ConnectedComponentWrapperVertex simple = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple2 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple3 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple4 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple.addNeighbour(simple2);
 
 		simple2.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple2.addNeighbour(simple3).addNeighbour(simple4);
 
 		simple3.includeMember(VertexFactory.makeSimpleVertex());
 		simple3.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple3.addNeighbour(simple4);
 
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple.publishYourStatsOn(recorder);
 		simple2.publishYourStatsOn(recorder);
 		simple3.publishYourStatsOn(recorder);
 		simple4.publishYourStatsOn(recorder);
 
 		Map<PlainTextStatsComponents, Integer> expectedFor3members = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expectedFor3members.put(PlainTextStatsComponents.NOfComponents, 2);
 		expectedFor3members.put(PlainTextStatsComponents.NOfSources, 1);
 		expectedFor3members.put(PlainTextStatsComponents.NOfSinks, 1);
 		expectedFor3members.put(PlainTextStatsComponents.NOfWhites, 0);
 		expectedFor3members.put(PlainTextStatsComponents.NOfEdges, 1);
 
 		Map<PlainTextStatsComponents, Integer> expectedFor2members = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expectedFor2members.put(PlainTextStatsComponents.NOfComponents, 1);
 		expectedFor2members.put(PlainTextStatsComponents.NOfSources, 0);
 		expectedFor2members.put(PlainTextStatsComponents.NOfSinks, 0);
 		expectedFor2members.put(PlainTextStatsComponents.NOfWhites, 1);
 		expectedFor2members.put(PlainTextStatsComponents.NOfEdges, 1);
 
 		Map<PlainTextStatsComponents, Integer> expectedFor1members = new HashMap<PlainTextStatsComponents, Integer>();
 
 		expectedFor1members.put(PlainTextStatsComponents.NOfComponents, 1);
 		expectedFor1members.put(PlainTextStatsComponents.NOfSources, 0);
 		expectedFor1members.put(PlainTextStatsComponents.NOfSinks, 0);
 		expectedFor1members.put(PlainTextStatsComponents.NOfWhites, 1);
 		expectedFor1members.put(PlainTextStatsComponents.NOfEdges, 2);
 
 		Assert.assertTrue(recorder.isComponentsVotesEquals(3,
 				expectedFor3members));
 		Assert.assertTrue(recorder.isComponentsVotesEquals(2,
 				expectedFor2members));
 		Assert.assertTrue(recorder.isComponentsVotesEquals(1,
 				expectedFor1members));
 
 	}
 
 	@Test
 	public void check_consistency_for_connected_components_vertices_votes() {
 		VertexStatsRecorder recorder = new VertexStatsRecorder();
 
 		ConnectedComponentWrapperVertex simple = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple2 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple3 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		ConnectedComponentWrapperVertex simple4 = VertexFactory
 				.makeConnectedComponentWrapperVertex();
 
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 		simple.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple.addNeighbour(simple2);
 
 		simple2.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple2.addNeighbour(simple3).addNeighbour(simple4);
 
 		simple3.includeMember(VertexFactory.makeSimpleVertex());
 		simple3.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple3.addNeighbour(simple4);
 
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 		simple4.includeMember(VertexFactory.makeSimpleVertex());
 
 		simple.publishYourStatsOn(recorder);
 		simple2.publishYourStatsOn(recorder);
 		simple3.publishYourStatsOn(recorder);
 		simple4.publishYourStatsOn(recorder);
 
 		Assert.assertTrue(recorder
 				.isConnectedComponentsVoteAccepterConsistent());
 
 	}
 
 	@Test
 	public void check_consistency_for_simple_vertices_votes() {
 		VertexStatsRecorder recorder = new VertexStatsRecorder();
 
 		Vertex simple = VertexFactory.makeSimpleVertex();
 		Vertex simple2 = VertexFactory.makeSimpleVertex();
 		Vertex simple3 = VertexFactory.makeSimpleVertex();
 		Vertex simple4 = VertexFactory.makeSimpleVertex();
 
 		simple.addNeighbour(simple2).addNeighbour(simple3)
 				.addNeighbour(simple4);
 
 		simple2.addNeighbour(simple3).addNeighbour(simple4);
 
 		simple3.addNeighbour(simple4);
 
 		simple.publishYourStatsOn(recorder);
 		simple2.publishYourStatsOn(recorder);
 		simple3.publishYourStatsOn(recorder);
 		simple4.publishYourStatsOn(recorder);
 
 		Assert.assertTrue(recorder.isSimpleVerticesVoteAccepterConsistent());
 
 	}
 
 	public void scan_all_standard_sbml_models_should_produce_consistent_informations() {
 
 		DotUtilAction<File> action = new DotUtilAction<File>() {
 
 			@Override
 			public void apply(File element) {
 
 				PipeFilter firstPlainTextStatsPipeFilter = PipeFilterFactory
 						.MakePlainTextStatsPipeFilter();
 
 				PipeFilter tarjanPipeFilter = PipeFilterFactory
 						.MakeTarjanPipeFilter();
 
 				PipeFilter secondPlainTextStatsPipeFilter = PipeFilterFactory
 						.MakePlainTextStatsPipeFilter();
 
 				tarjanPipeFilter.pipeAfter(firstPlainTextStatsPipeFilter);
 				secondPlainTextStatsPipeFilter.pipeAfter(tarjanPipeFilter);
 
 				PlainTextInfoComputationListener plainTextInfoComputationListener = new PlainTextInfoComputationListener();
 
 				String pipeline_name = "massive-stats-info-check-consistency-"
 						.concat(element.getName().substring(0,
 								element.getName().lastIndexOf(".")));
 
 				secondPlainTextStatsPipeFilter.applyWithListener(pipeline_name,
 						OurModel.makeOurModelFrom(element.getAbsolutePath()),
 						plainTextInfoComputationListener);
 
 				// no output is produced.
 				Assert.assertTrue(plainTextInfoComputationListener
 						.arePlainTextInfoConsistent());
 
 			}
 		};
 
 		DotFileUtilHandler.mapOnFilesInFolderFilteringByExtension(
 				DotFileUtilHandler.getSbmlExampleModelsFolder(),
 				DotFileUtilHandler.getSBMLFileExtension(), action, false);
 
 	}
 
 	@Test
 	public void check_model_presence_in_various_sbml_models_contained_in_BioCyc_folder() {
 
 		final SortedMap<String, IntegerCounter> count_by_models = new TreeMap<String, IntegerCounter>();
 
 		DotUtilAction<File> action = new DotUtilAction<File>() {
 
 			@Override
 			public void apply(File element) {
 
 				Model model = null;
 				SBMLDocument document = null;
 
 				try {
 					document = (new SBMLReader()).readSBML(element);
 				} catch (FileNotFoundException e) {
 				} catch (XMLStreamException e) {
 				} catch (Exception e) {
 				}
 
 				if (document != null) {
 
 					model = document.getModel();
 
 					String model_name = model.getName();
 
 					if (model_name == null) {
 						Assert.fail("Impossible to have a model without a name");
 					}
 
					if (count_by_models.containsKey(model_name)) {
						count_by_models.get(model_name).increment();
					} else {
 						count_by_models.put(model_name, new IntegerCounter());
 					}
 				}
 
 			}
 		};
 
 		DotFileUtilHandler.mapOnAllFilesInFolder(DotFileUtilHandler
 				.getSbmlExampleModelsFolder().concat("BioCyc15.0/"), action,
 				true);
 
 		// now we can generate the output file
 		Writer writer;
 		try {
 			writer = new FileWriter(
 					DotFileUtilHandler
 							.dotOutputFolderPathName()
 							.concat("maps-of-models-presence-among-multiple-models-in-ByoCyc-folder")
 							.concat(DotFileUtilHandler
 									.getPlainTextFilenameExtensionToken()));
 
 			writer.write(count_by_models.toString());
 
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		Assert.assertEquals(92, count_by_models.size());
 
 		int exploded_count = 0;
 		for (Entry<String, IntegerCounter> entry : count_by_models.entrySet()) {
 			exploded_count = exploded_count + entry.getValue().getCount();
 		}
 
		Assert.assertEquals(166, exploded_count);
 
 	}
 
 	public void check_species_presence_in_various_sbml_models_contained_in_curated_folder() {
 
 		this.internal_check_species_presence_in_sbml_models(
 				"maps-of-species-presence-among-multiple-new-curated-models",
 				DotFileUtilHandler.getSbmlExampleModelsFolder().concat(
 						"curated/"), false);
 	}
 
 	public void check_species_presence_in_various_sbml_models_contained_in_aae_folder() {
 
 		this.internal_check_species_presence_in_sbml_models(
 				"maps-of-species-presence-among-multiple-models-in-aae-folder",
 				DotFileUtilHandler.getSbmlExampleModelsFolder().concat("aae/"),
 				false);
 	}
 
 	public void check_species_presence_in_a_huge_number_of_sbml_models_contained_in_kyoto_database() {
 
 		IntegerCounter analyzedModels = this
 				.internal_check_species_presence_in_sbml_models(
 						"maps-of-species-presence-among-a-huge-number-of-models-in-kyoto-folder",
 						DotFileUtilHandler.getSbmlExampleModelsFolder().concat(
 								"KEGG_R47-SBML_L2V1_nocd-20080728/"), true);
 
 		Assert.assertEquals((Integer) 72095, analyzedModels.getCount());
 	}
 
 	public void check_species_presence_in_standard_sbml_models() {
 
 		this.internal_check_species_presence_in_sbml_models(
 				"maps-of-species-presence-among-multiple-old-models",
 				DotFileUtilHandler.getSbmlExampleModelsFolder(), false);
 	}
 
 	private IntegerCounter internal_check_species_presence_in_sbml_models(
 			String outputFilename, String modelsContainingDirectory,
 			boolean recursively) {
 
 		final SortedMap<String, Integer> countBySpecies = new TreeMap<String, Integer>();
 
 		final IntegerCounter analyzedModels = new IntegerCounter();
 
 		DotUtilAction<File> action = new DotUtilAction<File>() {
 
 			@Override
 			public void apply(File element) {
 
 				Model model = null;
 				SBMLDocument document = null;
 
 				try {
 					document = (new SBMLReader()).readSBML(element);
 				} catch (FileNotFoundException e) {
 				} catch (XMLStreamException e) {
 				} catch (Exception e) {
 				}
 
 				if (document != null) {
 
 					analyzedModels.increment();
 
 					model = document.getModel();
 
 					for (Species species : model.getListOfSpecies()) {
 
 						String id = (species.getId() + "-(" + species.getName()
 								+ ")" + "-(" + species.getCompartment() + ")")
 								.toUpperCase(new Locale("(all)"));
 
 						if (countBySpecies.containsKey(id)) {
 							int value = countBySpecies.get(id);
 							countBySpecies.remove(id);
 							countBySpecies.put(id, value + 1);
 						} else {
 							countBySpecies.put(id, 1);
 						}
 					}
 				}
 
 			}
 		};
 
 		DotFileUtilHandler.mapOnAllFilesInFolder(modelsContainingDirectory,
 				action, recursively);
 
 		// now we can generate the output file
 		Writer writer;
 		try {
 			writer = new FileWriter(DotFileUtilHandler
 					.dotOutputFolderPathName()
 					.concat(outputFilename)
 					.concat(DotFileUtilHandler
 							.getPlainTextFilenameExtensionToken()));
 
 			writer.write(countBySpecies.toString());
 
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return analyzedModels;
 	}
 }
