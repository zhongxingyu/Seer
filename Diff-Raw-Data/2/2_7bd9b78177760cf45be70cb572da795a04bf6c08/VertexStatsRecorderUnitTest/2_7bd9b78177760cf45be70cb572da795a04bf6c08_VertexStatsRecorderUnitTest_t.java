 package model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
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
 import dotInterface.DotFileUtilHandler;
 import dotInterface.DotFileUtilHandler.DotUtilAction;
 
 public class VertexStatsRecorderUnitTest {
 
 	@Test
 	public void recordSimpleVerticesVotes() {
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
 	public void recordConnectedComponentsVotes() {
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
 
 	@Test
 	public void generating_massive_stats_reports_forall_sbml_models() {
 
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
 
 				String test_method_name = "massive_stats_report-"
 						.concat(element.getName().substring(0,
 								element.getName().lastIndexOf(".")));
 
 				secondPlainTextStatsPipeFilter.applyWithListener(
 						test_method_name,
 						OurModel.makeOurModelFrom(element.getAbsolutePath()),
 						plainTextInfoComputationListener);
 
 				Assert.assertTrue(plainTextInfoComputationListener
 						.arePlainTextInfoConsistent());
 
 			}
 		};
 
 		DotFileUtilHandler.mapOnAllFilesInFolder(
 				DotFileUtilHandler.getSbmlExampleModelsFolder(), action);
 
 	}
 
 	@Test
 	public void check_species_presence_in_various_sbml_models() {
 
 		this.internal_check_species_presence_in_sbml_models(
 				"maps-of-species-presence-among-multiple-new-curated-models",
 				DotFileUtilHandler.getSbmlExampleModelsFolder().concat(
 						"curated/"), false);
 	}
 
 	@Test
 	public void check_species_presence_in_various_sbml_models_contained_in_aae_folder() {
 
 		this.internal_check_species_presence_in_sbml_models(
 				"maps-of-species-presence-among-multiple-models-in-aae-folder",
 				DotFileUtilHandler.getSbmlExampleModelsFolder().concat("aae/"),
 				false);
 	}
 
 	@Test
 	public void check_species_presence_in_a_huge_number_of_sbml_models_recursively() {
 
 		this.internal_check_species_presence_in_sbml_models(
 				"maps-of-species-presence-among-a-huge-number-of-models-in-kyoto-folder",
 				DotFileUtilHandler.getSbmlExampleModelsFolder().concat(
 						"KEGG_R47-SBML_L2V1_nocd-20080728/"), true);
 	}
 
 	@Test
 	public void check_species_presence_in_old_sbml_models() {
 
 		this.internal_check_species_presence_in_sbml_models(
 				"maps-of-species-presence-among-multiple-old-models",
 				DotFileUtilHandler.getSbmlExampleModelsFolder(), false);
 	}
 
 	private static class IntegerForClosure {
 		private int count = 0;
 
 		public void Increment() {
 			count = count + 1;
 		}
 
 		@Override
 		public String toString() {
 			return "(COUNT " + String.valueOf(count) + ")";
 		}
 
 		public boolean isCountEquals(int other) {
 			return this.count == other;
 		}
 
 	}
 
 	private void internal_check_species_presence_in_sbml_models(
 			String outputFilename, String modelsContainingDirectory,
 			boolean recursively) {
 
 		final SortedMap<String, Integer> countBySpecies = new TreeMap<String, Integer>();
 
 		final IntegerForClosure analyzedModels = new IntegerForClosure();
 
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
 
 					analyzedModels.Increment();
 
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
 
		Assert.assertTrue(analyzedModels.isCountEquals(72095));
 		Assert.assertFalse(analyzedModels.isCountEquals(0));
 
 	}
 }
