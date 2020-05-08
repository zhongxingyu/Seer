 package builders;
 
 import repast.simphony.parameter.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.poi.hssf.record.formula.functions.T;
 
 import probtools.Distributions;
 import probtools.NDParams;
 import agents.BaseHuman;
 import agents.Watchman;
 
 import edu.uci.ics.jung.graph.Graph;
 
 import repast.simphony.context.Context;
 import repast.simphony.context.space.graph.NetworkBuilder;
 import repast.simphony.context.space.graph.NetworkGenerator;
 import repast.simphony.context.space.graph.WattsBetaSmallWorldGenerator;
 import repast.simphony.dataLoader.ContextBuilder;
 import repast.simphony.engine.environment.RunEnvironment;
 import repast.simphony.space.graph.Network;
 import repast.simphony.space.graph.RepastEdge;
 import repast.simphony.util.collections.IndexedIterable;
 
 import graphtools.generators.ScaleFree;
 import graphtools.generators.SmallWorld;
 import graphtools.io.converters.*;
 import graphtools.io.exporters.CSVExporter;
 import graphtools.io.importers.GraphMLImporter;
 import graphtools.samplers.GeneralTools;
 public class ModelDevBuilder implements ContextBuilder<Object> {
 
 	@Override
 	public Context build(Context<Object> context) {
 		// TODO Auto-generated method stub
 		context.setId("SmokeSNET-Main");
 		Parameters parameters = RunEnvironment.getInstance().getParameters();
 		Network<BaseHuman> network = null;
 		
 		boolean generateOnAddition = true;
 		int mode = (Integer) parameters.getValue("mode");
 		if(mode == 1)
 		{
 			NDParams nd = new NDParams(0.8, 0.4, 0, 1);
 			network = ScaleFree.createRSF(context, "TestNet", 4, (Integer) parameters.getValue("numNodes"), true, 0.8, true, nd);
 			RepastSummary rs = GeneralTools.trimNodesByDegree(context, network, 0);
 			context = rs.getContext();
 			network = rs.getNetwork();
 		}
 		else if (mode == 2)
 		{
 			//network = SmallWorld.generateRSW(context, "TestNet", 100, 0.5, 5);
 			RepastSummary rs = GraphMLImporter.GraphMLToRepast("SIMBASE.graphml", "TestNet", context, true, false);
 			context = rs.getContext();
 			network = rs.getNetwork();
 		}
 		else if (mode == 3)
 		{
 			//network = SmallWorld.generateRSW(context, "TestNet", 100, 0.5, 5);
 			RepastSummary rs = GraphMLImporter.GraphMLToRepast("SIMBASE.graphml", "TestNet", context, false, false);
 			context = rs.getContext();
 			network = rs.getNetwork();
 		}
 		else if (mode == 4)
 		{
 			//network = SmallWorld.generateRSW(context, "TestNet", 100, 0.5, 5);
 			RepastSummary rs = GraphMLImporter.GraphMLToRepast("SIMBASE.graphml", "TestNet", context, false, true);
 			context = rs.getContext();
 			network = rs.getNetwork();
 		}
 		else if(mode == 5)
 		{
 			RepastSummary rs = GraphMLImporter.GraphMLToRepast("basetest.graphml", "TestNet", context, true, false);
 			context = rs.getContext();
 			network = rs.getNetwork();
 			NDParams nd = new NDParams(0.8, 0.4, 0, 1);
 			network = ScaleFree.SFOnBase(context, network, (Integer) parameters.getValue("numNodes"), true, 0.5, true, nd);
 			rs = GeneralTools.trimNodesByDegree(context, network, 0);
 			context = rs.getContext();
 			network = rs.getNetwork();
 		}
 		else if(mode == 6)
 		{
 			RepastSummary rs = GraphMLImporter.GraphMLToRepast("basetest.graphml", "TestNet", context, false, false);
 			context = rs.getContext();
 			network = rs.getNetwork();
 			NDParams nd = new NDParams(0.8, 0.4, 0, 1);
 			network = ScaleFree.SFOnBase(context, network, (Integer) parameters.getValue("numNodes"), true, 0.5, true, nd);
 			rs = GeneralTools.trimNodesByDegree(context, network, 0);
 			context = rs.getContext();
 			network = rs.getNetwork();
 		}
 		
 		String outputFile = (String) parameters.getValue("outName");
 		if(outputFile.length() == 0)
 			outputFile = "TEST-000";
 		
 		System.out.println(parameters.getValue("numNodes"));
		Watchman watch = new Watchman(100, true, true, true, outputFile, context, network);
 		context.add(watch);
 		
 		return context;
 	}
 	
 	
 
 }
