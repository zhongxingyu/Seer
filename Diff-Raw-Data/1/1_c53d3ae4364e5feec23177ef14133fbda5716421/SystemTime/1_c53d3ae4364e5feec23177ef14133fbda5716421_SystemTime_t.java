 package org.swows.time;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.swows.graph.events.DynamicGraph;
 import org.swows.graph.events.DynamicGraphFromGraph;
 import org.swows.runnable.RunnableContext;
 import org.swows.runnable.RunnableContextFactory;
 import org.swows.vocabulary.Instance;
 
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.sparql.graph.GraphFactory;
 
 public class SystemTime implements Runnable {
 	
 	private static final int DEFAULT_PERIOD = 5;
 	
 	private long updatePeriod;
 	private DynamicGraphFromGraph timeGraph;
 	private Triple currTriple;
 		
 	public SystemTime() {
 		this(DEFAULT_PERIOD);
 	}
 	
 	public SystemTime(long updatePeriod) {
 		this.updatePeriod = updatePeriod;
 	}
 	
 	public void run() {
 		Triple newTriple = tripleFromTime();
 		timeGraph.delete(currTriple);
 		timeGraph.add(newTriple);
 		timeGraph.sendUpdateEvents();
		currTriple = newTriple;
 	}
 	
 //	private static Triple tripleFromTime(long time) {
 //		return new Triple(
 //				Instance.GraphRoot.asNode(),
 //				org.swows.vocabulary.time.systemTime.asNode(),
 //				Node.createLiteral( "" + time, (String) null, XSDDatatype.XSDinteger ) );
 //	}
 	
 	private static Triple tripleFromTime() {
 		return new Triple(
 				Instance.GraphRoot.asNode(),
 				org.swows.vocabulary.time.systemTime.asNode(),
 				Node.createLiteral( "" + System.currentTimeMillis(), (String) null, XSDDatatype.XSDinteger ) );
 	}
 	
 	public DynamicGraph getGraph() {
 		timeGraph = new DynamicGraphFromGraph( GraphFactory.createGraphMem() );
 		currTriple = tripleFromTime();
 		timeGraph.add(currTriple);
 //		if (timeGraph == null) {
 //		LocalTimer.get().schedule(this, 0, updatePeriod);
 		final RunnableContext runnableCtxt = RunnableContextFactory.getDefaultRunnableContext();
 		Timer updateTimer = new Timer();
 //		Timer updateTimer = LocalTimer.get();
 		updateTimer.schedule( new TimerTask() {
 			@Override
 			public void run() {
 				runnableCtxt.run(SystemTime.this);
 			}
 		}, 0, updatePeriod);
 //		}
 		return timeGraph;
 	}
 
 }
