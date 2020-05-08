 package JSBMLInterface;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import model.Vertex;
 
 import org.sbml.jsbml.ListOf;
 import org.sbml.jsbml.Reaction;
 import org.sbml.jsbml.SpeciesReference;
 
 public class Connector {
 
 	private Connector() {
 	}
 
 	public static Connector makeConnector() {
 		return new Connector();
 	}
 
 	public Set<Vertex> readReaction(Reaction reaction,
 			VertexGenerationWithSourceListener listener) {
 
 		Map<Vertex, Vertex> knownVertices = new HashMap<Vertex, Vertex>();
 
 		Set<Vertex> reactants = this.convertToVertexSet(
 				reaction.getListOfReactants(), knownVertices,
 				new FromReactorDriver(listener));
 
 		Set<Vertex> products = this.convertToVertexSet(
 				reaction.getListOfProducts(), knownVertices,
 				new FromProductDriver(listener));
 
 		this.updateNeighbourhoodByCrossProduct(reactants, products);
 
 		if (reaction.isReversible()) {
 			this.updateNeighbourhoodByCrossProduct(products, reactants);
 		}
 
		Set<Vertex> result = new HashSet<Vertex>();
		result.addAll(reactants);
		result.addAll(products);

		return result;
 	}
 
 	private void updateNeighbourhoodByCrossProduct(Set<Vertex> domainSet,
 			Set<Vertex> rangeSet) {
 		for (Vertex domainVertex : domainSet) {
 			for (Vertex rangeVertex : rangeSet) {
 				domainVertex.addNeighbour(rangeVertex);
 			}
 		}
 	}
 
 	public Set<Vertex> convertToVertexSet(
 			ListOf<SpeciesReference> listOfSpeciesReference,
 			Map<Vertex, Vertex> knownVertices, VertexGenerationListener listener) {
 
 		Set<Vertex> result = new HashSet<Vertex>();
 
 		for (SpeciesReference sRef : listOfSpeciesReference) {
 
 			Vertex newVertex = Vertex.makeVertex(sRef.getSpeciesInstance());
 
 			if (knownVertices.containsKey(newVertex) == true) {
 				newVertex = knownVertices.get(newVertex);
 			} else {
 				knownVertices.put(newVertex, newVertex);
 			}
 
 			listener.newVertexGenerated(newVertex);
 			result.add(newVertex);
 		}
 
 		return result;
 	}
 
 	public class FromReactorDriver implements VertexGenerationListener {
 
 		private final VertexGenerationWithSourceListener listener;
 
 		public FromReactorDriver(VertexGenerationWithSourceListener listener) {
 			this.listener = listener;
 		}
 
 		@Override
 		public void newVertexGenerated(Vertex vertex) {
 			listener.newVertexFromReactor(vertex);
 			listener.newVertexGenerated(vertex);
 		}
 	}
 
 	public class FromProductDriver implements VertexGenerationListener {
 
 		private final VertexGenerationWithSourceListener listener;
 
 		public FromProductDriver(VertexGenerationWithSourceListener listener) {
 			this.listener = listener;
 		}
 
 		@Override
 		public void newVertexGenerated(Vertex vertex) {
 			this.listener.newVertexFromProduct(vertex);
 			listener.newVertexGenerated(vertex);
 		}
 	}
 
 }
