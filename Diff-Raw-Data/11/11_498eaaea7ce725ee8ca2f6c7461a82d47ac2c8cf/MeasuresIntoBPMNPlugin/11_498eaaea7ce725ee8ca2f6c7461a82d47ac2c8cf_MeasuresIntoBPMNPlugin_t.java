 package org.processmining.plugins.bpmn.exporting;
 
 
 
 
 import java.util.Collection;
 import java.util.Map;
 
 
 import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
 import org.processmining.framework.connections.ConnectionCannotBeObtained;
 import org.processmining.framework.plugin.PluginContext;
 import org.processmining.framework.plugin.annotations.Plugin;
 import org.processmining.framework.plugin.annotations.PluginVariant;
 import org.processmining.framework.plugin.events.Logger.MessageLevel;
 
 import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
 import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramExt;
 
 import org.processmining.models.graphbased.directed.petrinet.Petrinet;
 import org.processmining.models.graphbased.directed.petrinet.elements.Place;
 
 import org.processmining.plugins.bpmn.BPMNtoPNConnection;
 
 import org.processmining.plugins.petrinet.replay.conformance.ConformanceResult;
 import org.processmining.plugins.petrinet.replay.conformance.TotalConformanceResult;
 import org.processmining.plugins.petrinet.replay.performance.PerformanceResult;
 import org.processmining.plugins.petrinet.replay.performance.TotalPerformanceResult;
 import org.processmining.plugins.petrinet.replay.util.ReplayAnalysisConnection;
 
 
 
 @Plugin(name = "BPMN Measures with Analisys Details", parameterLabels = {  "TotalConformanceResult" , "TotalPerformanceResult" , "ConformanceResult" , "Petrinets", "PerformanceResult"}, returnLabels = { "BPMN  traslate" }, returnTypes = {
 		BPMNDiagramExt.class }, userAccessible = true)
 public class MeasuresIntoBPMNPlugin {
 
 	private BPMNDiagram bpmnext;
 	
 	
 	@UITopiaVariant(affiliation = "Department of Computer Science University of Pisa", author = "R.Guanciale,G.Spagnolo et al.", email = "spagnolo@di.unipi.it", pack = "BPMNMeasures")
 	@PluginVariant(requiredParameterLabels = { 0 })
 	public Object exportBPMNexportXPDL(PluginContext context, TotalConformanceResult totalconformanceresult) throws Exception {
 
 		try {
 			ReplayAnalysisConnection connection = context.getConnectionManager().getFirstConnection(
 					ReplayAnalysisConnection.class, context, totalconformanceresult);
 
 			// connection found. Create all necessary component to instantiate inactive visualization panel
 			// log = connection.getObjectWithRole(ReplayRuposConnection.XLOG);
 			Petrinet net= connection.getObjectWithRole(ReplayAnalysisConnection.PNET);
 
 			BPMNtoPNConnection connection2 = context.getConnectionManager().getFirstConnection(
 					BPMNtoPNConnection.class, context, net);
 
 			// connection found. Create all necessary component to instantiate inactive visualization panel
 
 			BPMNDiagram bpmn = connection2.getObjectWithRole(BPMNtoPNConnection.BPMN);
 			Collection<Place> placeFlowCollection = connection2.getObjectWithRole(BPMNtoPNConnection.PLACEFLOWCONNECTION);
 
 			bpmnext = BPMNDecorateUtil.exportConformancetoBPMN(bpmn, net, totalconformanceresult.getTotal(), placeFlowCollection);
 
 
 
 		} catch (ConnectionCannotBeObtained e) {
 			// No connections available
 			context.log("Connection does not exist", MessageLevel.DEBUG);
 
 		}
 
 
 		return bpmnext;
 
 	}
 
 	@UITopiaVariant(affiliation = "Department of Computer Science University of Pisa", author = "R.Guanciale,G.Spagnolo et al.", email = "spagnolo@di.unipi.it", pack = "BPMNMeasures")
 	@PluginVariant(requiredParameterLabels = { 3,2 })
 	public Object exportBPMNexportXPDL(PluginContext context,Petrinet net, ConformanceResult conformanceresult) throws Exception {
 
 		try {
 			/*ReplayRuposConnection connection = context.getConnectionManager().getFirstConnection(
 					ReplayRuposConnection.class, context, totalconformanceresult);
 
 			// connection found. Create all necessary component to instantiate inactive visualization panel
 			// log = connection.getObjectWithRole(ReplayRuposConnection.XLOG);
 			Petrinet net= connection.getObjectWithRole(ReplayRuposConnection.PNET);*/
 
 			BPMNtoPNConnection connection2 = context.getConnectionManager().getFirstConnection(
 					BPMNtoPNConnection.class, context, net);
 
 			// connection found. Create all necessary component to instantiate inactive visualization panel
 
 			BPMNDiagram bpmn = connection2.getObjectWithRole(BPMNtoPNConnection.BPMN);
 			Collection<Place> placeFlowCollection = connection2.getObjectWithRole(BPMNtoPNConnection.PLACEFLOWCONNECTION);
 
 			bpmnext = BPMNDecorateUtil.exportConformancetoBPMN(bpmn, net, conformanceresult, placeFlowCollection);
 
 
 
 		} catch (ConnectionCannotBeObtained e) {
 			// No connections available
 			context.log("Connection does not exist", MessageLevel.DEBUG);
            context.getFutureResult(0).cancel(true);
 
 		}
 
 
 		return bpmnext;
 
 	}
 
 	@UITopiaVariant(affiliation = "Department of Computer Science University of Pisa", author = "R.Guanciale,G.Spagnolo et al.", email = "spagnolo@di.unipi.it", pack = "BPMNMeasures")
 	@PluginVariant(requiredParameterLabels = { 1 })
 	public Object exportBPMNexportXPDL(PluginContext context,  TotalPerformanceResult totalPerformanceresult) throws Exception {
 
 		try {
 			ReplayAnalysisConnection connection = context.getConnectionManager().getFirstConnection(
 					ReplayAnalysisConnection.class, context, totalPerformanceresult);
 
 			// connection found. Create all necessary component to instantiate inactive visualization panel
 			//XLog log = connection.getObjectWithRole(ReplayRuposConnection.XLOG);
 			Petrinet net= connection.getObjectWithRole(ReplayAnalysisConnection.PNET);
 
 			BPMNtoPNConnection connection2 = context.getConnectionManager().getFirstConnection(
 					BPMNtoPNConnection.class, context, net);
 
 			// connection found. Create all necessary component to instantiate inactive visualization panel
 
 			BPMNDiagram bpmn = connection2.getObjectWithRole(BPMNtoPNConnection.BPMN);
 			Collection< Place>  placeFlowCollection = connection2.getObjectWithRole(BPMNtoPNConnection.PLACEFLOWCONNECTION);
 
 			//cambiare con total
 			return BPMNDecorateUtil.exportPerformancetoBPMN(bpmn,  totalPerformanceresult.getListperformance().get(0), placeFlowCollection,net);
 
 
 
 		} catch (ConnectionCannotBeObtained e) {
 			// No connections available
 			context.log("Connection does not exist", MessageLevel.DEBUG);
            context.getFutureResult(0).cancel(true);
 			return null;
 		}
 
 	}
 
 	@UITopiaVariant(affiliation = "Department of Computer Science University of Pisa", author = "R.Guanciale,G.Spagnolo et al.", email = "spagnolo@di.unipi.it", pack = "BPMNMeasures")
 	@PluginVariant(requiredParameterLabels = { 3,4 })
 	public Object exportBPMNexportXPDL(PluginContext context, Petrinet net,PerformanceResult Performanceresult) throws Exception {
 
 		try {
 
 
 
 			BPMNtoPNConnection connection2 = context.getConnectionManager().getFirstConnection(
 					BPMNtoPNConnection.class, context, net);
 
 			// connection found. Create all necessary component to instantiate inactive visualization panel
 
 			BPMNDiagram bpmn = connection2.getObjectWithRole(BPMNtoPNConnection.BPMN);
 			Collection< Place>  placeFlowCollection = connection2.getObjectWithRole(BPMNtoPNConnection.PLACEFLOWCONNECTION);
 
 
 			return BPMNDecorateUtil.exportPerformancetoBPMN(bpmn,  Performanceresult, placeFlowCollection,net);
 
 
 
 		} catch (ConnectionCannotBeObtained e) {
 			// No connections available
 			context.log("Connection does not exist", MessageLevel.DEBUG);
 			return null;
 		}
 
 	}
 
 
 }
 
 
 
 
