 package org.processmining.plugins.petrinet.replay.performance;
 
 
 
 
 import javax.swing.JComponent;
 
 import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
 import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
 import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.contexts.util.StringVisualizer;
 import org.processmining.framework.connections.ConnectionCannotBeObtained;
 import org.processmining.framework.plugin.PluginContext;
 import org.processmining.framework.plugin.Progress;
 import org.processmining.framework.plugin.annotations.Plugin;
 import org.processmining.framework.plugin.annotations.PluginVariant;
 import org.processmining.framework.plugin.events.Logger.MessageLevel;
 import org.processmining.models.graphbased.directed.petrinet.Petrinet;
 import org.processmining.plugins.petrinet.replay.util.ReplayAnalysisConnection;
 
 
 
 @Visualizer
 @Plugin(name = "Performance Result Visualizer", parameterLabels = "Performance Rupos Analisys", returnLabels = "Visualized Performance", returnTypes = JComponent.class)
 public class ReplayPerformanceAnalysisVisualizze {
 
 	@PluginVariant(requiredParameterLabels = { 0 })
 	@UITopiaVariant(affiliation = "Department of Computer Science University of Pisa", author = "R.Guanciale,G.Spagnolo et al.", email = "spagnolo@di.unipi.it", pack = "PetriNetReplayAnalysis")
 	public JComponent visualize(PluginContext context, TotalPerformanceResult tovisualize) {
		if(context instanceof UIPluginContext){
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
		}else {

			return StringVisualizer.visualize(context, tovisualize.toString());
		}
 
 	}
 
 
 
 	private JComponent getVisualizationPanel(PluginContext context,
 			Petrinet net, XLog log, TotalPerformanceResult tovisualize) {
 		Progress progress = context.getProgress();
 		
 		ReplayPerformanceAnalysisPanel panel = new ReplayPerformanceAnalysisPanel(context, net, log, progress, tovisualize);
 		
 		return panel;
 
 
 	}
 	
 
 }
