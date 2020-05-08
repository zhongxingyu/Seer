 package org.processmining.plugins.petrinet.addtransition;
 
 
 import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
 import org.processmining.framework.connections.ConnectionCannotBeObtained;
 import org.processmining.framework.plugin.PluginContext;
 
 import org.processmining.framework.plugin.annotations.Plugin;
 import org.processmining.framework.plugin.annotations.PluginVariant;
 
 import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
 
 import org.processmining.models.graphbased.directed.petrinet.Petrinet;
 import org.processmining.models.graphbased.directed.petrinet.elements.ExpandableSubNet;
 import org.processmining.models.graphbased.directed.petrinet.elements.Place;
 import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
 import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
 
 import org.processmining.models.semantics.petrinet.Marking;
 
 
 
 
 
 @Plugin(name = "Add Artificial End Transition", parameterLabels = { "PetriNet","Name End Transition" }, returnLabels = { "PetriNet","Initial Marking" }, returnTypes = { Petrinet.class, Marking.class })
 public class AddEndTransitionPlugin {
 
 	
 	@PluginVariant(requiredParameterLabels = { 0 })
 	@UITopiaVariant(affiliation = "UNIPI", author = "GOs", email = "")
 	public Object addTransition(PluginContext context, Petrinet oldnet){
 		
 		return  this.addTransition(context, oldnet, "ArtificialEnd");
 		
 	}
 
 	@PluginVariant(requiredParameterLabels = { 0,1 })
 	public Object addTransition(PluginContext context, Petrinet oldnet, String name){
 		name = name.replaceAll ("[ \\p{Punct}]", "");
 		Marking oldmarking=null;
 
 		try {
 			InitialMarkingConnection connection = context.getConnectionManager().getFirstConnection(
 					InitialMarkingConnection.class, context, oldnet);
 			oldmarking = connection.getObjectWithRole(InitialMarkingConnection.MARKING);
 		} catch (ConnectionCannotBeObtained ex) {
 			context.log("Petri net lacks initial marking");
 			System.out.println("**************** NO MARKING **************");
 			return null;
 		}
 		
 		Petrinet net = PetrinetFactory.clonePetrinet(oldnet);
 		
 		ExpandableSubNet subNet = null;
 		//cerca la piazza finale
 		for(Place p : net.getPlaces()){
 			//questa è il place finale
 			if(p.getGraph().getOutEdges(p).size()==0){
				Transition t = net.addTransition(name+"complete", subNet);
 				Place place = net.addPlace(name, subNet);
 				net.addArc(t, place, 1, subNet);
 				net.addArc(p, t, 1, subNet);
 			}
 		}
 		
 		Marking newmarking = new Marking();
 		
 		for (Place p :oldmarking.toList()){
 			for(Place pnew : net.getPlaces()){
 				if(p.getLabel()==pnew.getLabel()){
 					newmarking.add(pnew);
 					break;
 				}
 				
 			}
 			
 		}
 		
 		context.addConnection(new InitialMarkingConnection(net, newmarking));
 
 		
 		Object[] result = new Object[2];
 		result[0] = net;
 		result[1] = newmarking;
 		
 		return result;
 	}
 	
 	
 }
