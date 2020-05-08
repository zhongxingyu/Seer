 // This software is released into the Public Domain.  See copying.txt for details.
 package de.vwistuttgart.openstreetmap.osmosis.fastusedfilter.v0_6;
 
 import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
 import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
 import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
 import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
 import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
 import org.openstreetmap.osmosis.core.domain.v0_6.Way;
 import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
 import org.openstreetmap.osmosis.core.filter.common.BitSetIdTracker;
 import org.openstreetmap.osmosis.core.filter.common.DynamicIdTracker;
 import org.openstreetmap.osmosis.core.filter.common.IdTracker;
 import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
 import org.openstreetmap.osmosis.core.filter.common.ListIdTracker;
 import org.openstreetmap.osmosis.core.store.DataPostbox;
 import org.openstreetmap.osmosis.core.task.v0_6.MultiSinkRunnableSource;
 import org.openstreetmap.osmosis.core.task.v0_6.Sink;
 
 import de.vwistuttgart.openstreetmap.osmosis.fastusedfilter.v0_6.impl.DataPostboxSink;
 
 
 /**
  * Restricts output of nodes to those that are used in ways and relations
  * without an intermediate store.
  * 
  * This implementation is basically a merge with an id tracker. It takes the
  * ways and relations from one input stream, records the required node IDs, then
  * takes the nodes from the other input stream and filters them with the
  * recorded data.
  * 
  * Note that the ways and nodes need to come from two different <i>sources</i>
  * and not only from two different <i>threads</i>. If both node and way/relation
 * data ultimately comes from the same source, <b>this task will deadlock</b>
  * except for some lucky cases.
  * 
  * @author Igor Podolskiy
  */
 public class FastUsedNodeFilter implements MultiSinkRunnableSource {
 
 	private Sink sink;
 
 	private DataPostbox<EntityContainer> nodePostbox;
 	private DataPostbox<EntityContainer> wayRelationPostbox;
 
 	private Sink nodeSink;
 	private Sink wayRelationSink;
 
 	private IdTracker idTracker;
 
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param idTrackerType
 	 *            the ID tracker type to use
 	 * @param bufferCapacity
 	 *            the capacity to use for the input buffers
 	 */
 	public FastUsedNodeFilter(IdTrackerType idTrackerType, int bufferCapacity) {
 
 		switch (idTrackerType) {
 		case IdList:
 			idTracker = new ListIdTracker();
 			break;
 		case Dynamic:
 			idTracker = new DynamicIdTracker();
 			break;
 		case BitSet:
 			idTracker = new BitSetIdTracker();
 		default:
 			throw new OsmosisRuntimeException("Invalid ID tracker type " + idTrackerType.toString() + " requested");
 		}
 
 		nodePostbox = new DataPostbox<EntityContainer>(bufferCapacity);
 		wayRelationPostbox = new DataPostbox<EntityContainer>(bufferCapacity);
 		nodeSink = new DataPostboxSink(nodePostbox);
 		wayRelationSink = new DataPostboxSink(wayRelationPostbox);
 	}
 
 
 	@Override
 	public Sink getSink(int instance) {
 		switch (instance) {
 		case 0:
 			return nodeSink;
 		case 1:
 			return wayRelationSink;
 		default:
 			throw new OsmosisRuntimeException("Invalid sink instance " + instance + " requested.");
 		}
 	}
 
 
 	@Override
 	public int getSinkCount() {
 		return 2;
 	}
 
 
 	@Override
 	public void setSink(Sink sink) {
 		this.sink = sink;
 	}
 
 
 	@Override
 	public void run() {
 		// Collect all node ids we need from ways and relations
 		// while holding off nodes.
 		while (wayRelationPostbox.hasNext()) {
 			EntityContainer entityContainer = wayRelationPostbox.getNext();
 			processMaybeWay(entityContainer);
 			processMaybeRelation(entityContainer);
 			processMaybeBound(entityContainer);
 		}
 
 		// Process all the nodes and only pass on what we need.
 		while (nodePostbox.hasNext()) {
 			EntityContainer entityContainer = nodePostbox.getNext();
 			processMaybeNode(entityContainer);
 		}
 
 		sink.complete();
 	}
 
 
 	private void processMaybeBound(EntityContainer entityContainer) {
 		if (entityContainer.getEntity().getType() != EntityType.Bound) {
 			// We only care about bounds.
 			return;
 		}
 		sink.process(entityContainer);
 	}
 
 
 	private void processMaybeNode(EntityContainer entityContainer) {
 		if (entityContainer.getEntity().getType() != EntityType.Node) {
 			// We only care about nodes.
 			return;
 		}
 
 		if (idTracker.get(entityContainer.getEntity().getId())) {
 			sink.process(entityContainer);
 		}
 	}
 
 
 	private void processMaybeWay(EntityContainer entityContainer) {
 		if (entityContainer.getEntity().getType() != EntityType.Way) {
 			// We only care about ways.
 			return;
 		}
 		Way way = (Way) entityContainer.getEntity();
 		for (WayNode wn : way.getWayNodes()) {
 			idTracker.set(wn.getNodeId());
 		}
 
 		// pass on the way
 		sink.process(entityContainer);
 	}
 
 
 	private void processMaybeRelation(EntityContainer entityContainer) {
 		if (entityContainer.getEntity().getType() != EntityType.Relation) {
 			// We only care about relations.
 			return;
 		}
 		Relation rel = (Relation) entityContainer.getEntity();
 		for (RelationMember member : rel.getMembers()) {
 			if (member.getMemberType() == EntityType.Node) {
 				idTracker.set(member.getMemberId());
 			}
 		}
 
 		// pass on the relation
 		sink.process(entityContainer);
 	}
 }
