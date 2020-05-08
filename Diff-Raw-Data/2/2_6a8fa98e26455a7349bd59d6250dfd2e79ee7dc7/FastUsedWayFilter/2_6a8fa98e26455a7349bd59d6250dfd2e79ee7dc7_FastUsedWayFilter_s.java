 // This software is released into the Public Domain.  See copying.txt for details.
 package de.vwistuttgart.openstreetmap.osmosis.fastusedfilter.v0_6;
 
 import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
 import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
 import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
 import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
 import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
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
 public class FastUsedWayFilter implements MultiSinkRunnableSource {
 
 	private Sink sink;
 
 	private DataPostbox<EntityContainer> wayPostbox;
 	private DataPostbox<EntityContainer> relationPostbox;
 
 	private Sink waySink;
 	private Sink relationSink;
 
 	private IdTracker idTracker;
 
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param idTrackerType
 	 *            the ID tracker type to use
 	 * @param bufferCapacity
 	 *            the capacity to use for the input buffers
 	 */
 	public FastUsedWayFilter(IdTrackerType idTrackerType, int bufferCapacity) {
 
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
 
 		wayPostbox = new DataPostbox<EntityContainer>(bufferCapacity);
 		relationPostbox = new DataPostbox<EntityContainer>(bufferCapacity);
 		waySink = new DataPostboxSink(wayPostbox);
 		relationSink = new DataPostboxSink(relationPostbox);
 	}
 
 
 	@Override
 	public Sink getSink(int instance) {
 		switch (instance) {
 		case 0:
 			return waySink;
 		case 1:
 			return relationSink;
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
 		while (relationPostbox.hasNext()) {
 			EntityContainer entityContainer = relationPostbox.getNext();
 			processMaybeRelation(entityContainer);
 			processMaybeBound(entityContainer);
 			processMaybeNode(entityContainer);
 		}
 
 		// Process all the nodes and only pass on what we need.
 		while (wayPostbox.hasNext()) {
 			EntityContainer entityContainer = wayPostbox.getNext();
 			processMaybeWay(entityContainer);
 		}
 
 		sink.complete();
 	}
 
 
 	private void processMaybeNode(EntityContainer entityContainer) {
		if (entityContainer.getEntity().getType() != EntityType.Bound) {
 			// We only care about nodes.
 			return;
 		}
 		
 		// Pass nodes unchanged.
 		sink.process(entityContainer);
 	}
 
 
 	private void processMaybeBound(EntityContainer entityContainer) {
 		if (entityContainer.getEntity().getType() != EntityType.Bound) {
 			// We only care about bounds.
 			return;
 		}
 		sink.process(entityContainer);
 	}
 
 
 	private void processMaybeWay(EntityContainer entityContainer) {
 		if (entityContainer.getEntity().getType() != EntityType.Way) {
 			// We only care about ways.
 			return;
 		}
 
 		if (idTracker.get(entityContainer.getEntity().getId())) {
 			sink.process(entityContainer);
 		}
 	}
 
 
 	private void processMaybeRelation(EntityContainer entityContainer) {
 		if (entityContainer.getEntity().getType() != EntityType.Relation) {
 			// We only care about relations.
 			return;
 		}
 		Relation rel = (Relation) entityContainer.getEntity();
 		for (RelationMember member : rel.getMembers()) {
 			if (member.getMemberType() == EntityType.Way) {
 				idTracker.set(member.getMemberId());
 			}
 		}
 
 		// pass on the relation
 		sink.process(entityContainer);
 	}
 }
