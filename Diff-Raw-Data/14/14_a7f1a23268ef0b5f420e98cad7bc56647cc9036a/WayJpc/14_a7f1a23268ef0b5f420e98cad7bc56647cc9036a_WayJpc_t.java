 package org.jpc.examples.osm.model.imp;
 
 import static java.util.Arrays.asList;
 import static org.jpc.util.concurrent.ThreadLocalPrologEngine.getPrologEngine;
 
 import java.util.List;
 import java.util.Map;
 
 import org.jpc.converter.toterm.tolistterm.IterableToTermConverter;
 import org.jpc.converter.toterm.tolistterm.MapToTermConverter;
 import org.jpc.engine.logtalk.LogtalkObject;
 import org.jpc.examples.osm.model.Coordinate;
 import org.jpc.examples.osm.model.Node;
 import org.jpc.examples.osm.model.Way;
 import org.jpc.examples.osm.model.jpcconverters.TermToNodeConverter;
 import org.jpc.query.Query;
 import org.jpc.term.Compound;
 import org.jpc.term.IntegerTerm;
 import org.jpc.term.Term;
 import org.jpc.term.Variable;
 
 public class WayJpc implements Way {
 
 	public static final String WAY_FUNCTOR = "way"; //way prolog functor
 	
 	private Long id;
 	private List<String> nodesIds;
 	private Map<String,String> tags;
 	
 	public WayJpc(Long id, List<String> nodesIds, Map<String,String> tags) {
 		this.id = id;
 		this.nodesIds = nodesIds;
 		this.tags = tags;
 	}
 	
 	@Override
 	public Long getId() {
 		return id;
 	}
 
 	@Override
 	public List<String> getNodesIds() {
 		return nodesIds;
 	}
 
 	/**
 	 * Use with care: it can be an expensive call if there are many nodes in the way object and if the osm source is big
 	 * @return
 	 */
 	public List<Node> nodes() {
		String nodeVarName = "Node";
		Term message = new Compound("node", asList(new Variable(nodeVarName)));
 		Query query = new LogtalkObject(this, getPrologEngine()).perform(message);
		return query.select(nodeVarName).adapt(new TermToNodeConverter()).allSolutions();
 	}
 	
 	@Override
 	public Map<String, String> getTags() {
 		return tags;
 	}
 
 	@Override
 	public Term asTerm() {
 		return new Compound(WAY_FUNCTOR, asList(new IntegerTerm(id), new IterableToTermConverter().apply(nodesIds), new MapToTermConverter().apply(tags)));
 	}
 
 	@Override
 	public long distanceKm(Coordinate other) {
 		String distanceVarName = "Distance";
 		Term message = new Compound("distancekm", asList(other, new Variable(distanceVarName)));
 		Query query = new LogtalkObject(this, getPrologEngine()).perform(message);
 		return ((IntegerTerm)query.oneSolution().get(distanceVarName)).longValue();
 	}
 
 	@Override
 	public boolean near(Coordinate other, long deltaKm) {
 		Term message = new Compound("near", asList(other, new IntegerTerm(deltaKm)));
 		Query query = new LogtalkObject(this, getPrologEngine()).perform(message);
 		return query.hasSolution();
 	}
 
 }
