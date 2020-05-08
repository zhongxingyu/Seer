 package main;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.concurrent.TimeUnit;
 
 import javax.management.BadAttributeValueExpException;
 import javax.xml.ws.Service.Mode;
 
 import graph_generators.HashRingNode;
 import graph_generators.SymphonyGenerator;
 
 import model.AbstractPubSubModel;
 import model.DirectedBroadcastModel;
 import model.MulticastModel;
 
 import org.jgrapht.Graph;
 import org.jgrapht.ext.VisioExporter;
 import org.jgrapht.graph.DefaultEdge;
 
 import simulations.TreeStatistics;
 
 import desmoj.core.simulator.Experiment;
 import desmoj.core.simulator.TimeInstant;
 
 public class Simulation
 {
 
 	/**
 	 * @param args
 	 * @throws FileNotFoundException 
 	 * @throws BadAttributeValueExpException 
 	 */
 	public static void main(String[] args) throws FileNotFoundException, BadAttributeValueExpException
 	{
		Graph<HashRingNode, DefaultEdge> g = SymphonyGenerator.generateSymphonyGraph(10000, 9, true,  2);
 		
 		VisioExporter<HashRingNode,DefaultEdge> e = new VisioExporter<HashRingNode,DefaultEdge>();
 		File file = new File("out.csv");
 		e.export(new FileOutputStream(file), g);
 		
 		//AbstractPubSubModel model = new MulticastModel(g, 0.2);
 		AbstractPubSubModel model = new DirectedBroadcastModel(g, 0.2);
 		Experiment exp = new Experiment("Experiment1");
 		model.connectToExperiment(exp);
 		exp.setShowProgressBar(true);
 		exp.stop(new TimeInstant(10, TimeUnit.MINUTES));
 		exp.tracePeriod(new TimeInstant(0), new TimeInstant(0, TimeUnit.SECONDS));
 		exp.debugPeriod(new TimeInstant(0), new TimeInstant(0, TimeUnit.MINUTES));
 		exp.start();
 		exp.report();
 		exp.finish();
 		
 		file = new File("tree.csv");
 		e.export(new FileOutputStream(file), model.networkTree);
 		TreeStatistics s = new TreeStatistics(	model.getHashRingTraverser().
 													getNodeById(model.getPublisherIndex()),
 												model.networkTree);
 		System.out.println("Average distance = " + s.avg + ", Min distance = " + s.min + ", Max distance = " + s.max);
 
 		
 	}
 
 }
