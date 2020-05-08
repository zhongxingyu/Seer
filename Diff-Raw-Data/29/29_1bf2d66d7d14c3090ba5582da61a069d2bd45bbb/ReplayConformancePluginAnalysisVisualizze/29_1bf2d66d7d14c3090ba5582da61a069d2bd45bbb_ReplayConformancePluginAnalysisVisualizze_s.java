 package org.processmining.plugins.petrinet.replay.conformance;
 
 
 
 import javax.swing.JComponent;
 
 import org.deckfour.xes.model.XLog;
 import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
 import org.processmining.contexts.uitopia.annotations.Visualizer;
 import org.processmining.framework.connections.ConnectionCannotBeObtained;
 import org.processmining.framework.plugin.PluginContext;
 import org.processmining.framework.plugin.Progress;
 import org.processmining.framework.plugin.annotations.Plugin;
 import org.processmining.framework.plugin.annotations.PluginVariant;
 import org.processmining.framework.plugin.events.Logger.MessageLevel;
 import org.processmining.models.graphbased.directed.petrinet.Petrinet;
 import org.processmining.plugins.petrinet.replay.util.ReplayAnalysisConnection;
 
 
 @Visualizer
 @Plugin(name = "Conformance Result Visualizer", parameterLabels = "Conformance Rupos Analisys", returnLabels = "Visualized Fitness", returnTypes = JComponent.class)
 public class ReplayConformancePluginAnalysisVisualizze {
 
 	@PluginVariant(requiredParameterLabels = { 0 })
 	@UITopiaVariant(affiliation = "Department of Computer Science University of Pisa", author = "R.Guanciale,G.Spagnolo et al.", email = "spagnolo@di.unipi.it", pack = "PetriNetReplayAnalysis")
 	public JComponent visualize(PluginContext context, TotalConformanceResult tovisualize) {
		
 			try {
 				ReplayAnalysisConnection connection = context.getConnectionManager().getFirstConnection(
 						ReplayAnalysisConnection.class, context, tovisualize);
 
 				// connection found. Create all necessary component to instantiate inactive visualization panel
 				XLog log = connection.getObjectWithRole(ReplayAnalysisConnection.XLOG);
 				Petrinet netx = connection.getObjectWithRole(ReplayAnalysisConnection.PNET);
 				
 				return getVisualizationPanel(context, netx, log, tovisualize);
 
 			} catch (ConnectionCannotBeObtained e) {
 				// No connections available
 				context.log("Connection does not exist", MessageLevel.DEBUG);
 				return null;
 			}
		
 
 	}
 
 
 	private JComponent getVisualizationPanel(PluginContext context,
 			Petrinet net, XLog log, TotalConformanceResult tovisualize) {
 		Progress progress = context.getProgress();
 
 		ReplayConformanceAnalysisPanel panel = new ReplayConformanceAnalysisPanel(context, net, log, progress, tovisualize);
 		return panel;
 
 
 	}
 
 
 }
