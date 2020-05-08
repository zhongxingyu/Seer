 package edu.uca.info2.routing;
 
 import aima.core.agent.Action;
 import aima.core.search.framework.ActionsFunction;
 import aimax.osm.data.MapWayAttFilter;
 import aimax.osm.data.MapWayFilter;
 import aimax.osm.data.entities.MapNode;
 import aimax.osm.data.entities.MapWay;
 import aimax.osm.data.entities.WayRef;
 import aimax.osm.routing.OsmMoveAction;
 import edu.uca.info2.components.Area;
import edu.uca.info2.components.Segment;
 import edu.uca.info2.util.MapUtils;
 
import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 public class ZAActionsFunction implements ActionsFunction {
 
     @Override
     public Set<Action> actions(Object s) {
         Set<Action> neighbors = new LinkedHashSet<Action>();
         MapWayFilter filter = MapWayAttFilter.createCarWayFilter();
 
         ZASearchState state = (ZASearchState) s;
         MapNode node = state.getNode();
         Area area = state.getArea();
 
         for (WayRef wref : node.getWayRefs()) {
             if (filter == null || filter.isAccepted(wref.getWay())) {
                 MapWay way = wref.getWay();
                 int nodeIdx = wref.getNodeIdx();
                 List<MapNode> wayNodes = way.getNodes();
                 MapNode to;
 
                 for (int idx = nodeIdx + 1; idx < wayNodes.size(); idx++) {
                     to = wayNodes.get(idx);
 
                     // Si el nodo to esta fuera del area continuamos
                     if (!area.isNodeInArea(to)) {
                         continue;
                     }
 
                     if (to.getWayRefs().size() > 1
                             || idx == wayNodes.size() - 1) {
                         neighbors.add(new OsmMoveAction(way, node, to));
                         break;
                     }
                 }
                 if (!way.isOneway() || MapUtils.getIgnoreOneways()) {
                     for (int idx = nodeIdx - 1; idx >= 0; idx--) {
                         to = wayNodes.get(idx);
 
                         // Si el nodo to esta fuera del area continuamos
                         if (!area.isNodeInArea(to)) {
                             continue;
                         }
 
                         if (to.getWayRefs().size() > 1 || idx == 0) {
                             neighbors.add(new OsmMoveAction(way, node, to));
                             break;
                         }
                     }
                 }
             }
         }
 
//        int beforeFiltering = neighbors.size();
//        System.out.println("Before filtering" + beforeFiltering);

        int k = state.getMaxSegmentPassThrough();

        for (Iterator<Action> it = neighbors.iterator(); it.hasNext();) {
            OsmMoveAction action = (OsmMoveAction) it.next();
            Segment segment = new Segment(node, action.getTo());
            ZASearchState nextState = new ZASearchState(segment,
                    state.getSegmentsCounter(), state.getArea());

            if (nextState.passThroughCountForSegment(segment) > k) {
                it.remove();
            }
        }

//        int afterFiltering = neighbors.size();
//        assert beforeFiltering >= afterFiltering;
//        System.out.println("After filtering" + afterFiltering);


         return neighbors;
     }
 
 }
