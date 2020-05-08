 package polly.rx.core.orion;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import polly.rx.core.orion.Graph.EdgeCosts;
 import polly.rx.core.orion.Graph.Heuristic;
 import polly.rx.core.orion.Graph.LazyBuilder;
 import polly.rx.core.orion.model.EntryPortalWormhole;
 import polly.rx.core.orion.model.Quadrant;
 import polly.rx.core.orion.model.QuadrantDecorator;
 import polly.rx.core.orion.model.QuadrantUtils;
 import polly.rx.core.orion.model.Sector;
 import polly.rx.core.orion.model.SectorDecorator;
 import polly.rx.core.orion.model.SectorType;
 import polly.rx.core.orion.model.Wormhole;
 import de.skuzzle.polly.sdk.Types.TimespanType;
 
 
 public class PathPlanner {
     
     
     
     public static class RouteOptions {
         private final TimespanType totalJumpTime;
         private final TimespanType currentJumpTime;
         private final int maxWaitSpotDistance;
         
         public RouteOptions(TimespanType totalJumpTime, TimespanType currentJumpTime) {
             this.totalJumpTime = totalJumpTime;
             this.currentJumpTime = currentJumpTime;
             this.maxWaitSpotDistance = 3;
         }
         
         public TimespanType getCurrentJumpTime() {
             return this.currentJumpTime;
         }
         
         public TimespanType getTotalJumpTime() {
             return this.totalJumpTime;
         }
     }
     
     
     
     public static class EdgeData {
         
         public static enum EdgeType {
             NORMAL, DIAGONAL, WORMHOLE, ENTRYPORTAL;
         }
         
         public static EdgeData wormhole(Wormhole wormhole) {
             final EdgeData d = new EdgeData(EdgeType.WORMHOLE);
             d.wormhole = wormhole;
             return d;
         }
         
         public static EdgeData entryPortal(Sector source, Sector target) {
             final EdgeData d = new EdgeData(EdgeType.ENTRYPORTAL);
             d.wormhole = new EntryPortalWormhole(source, target);
             return d;
         }
         
         public static EdgeData sector(boolean diagonal) {
             if (diagonal) {
                 return new EdgeData(EdgeType.DIAGONAL);
             }
             return new EdgeData(EdgeType.NORMAL);
         }
         
         
         private final EdgeType type;
         private Wormhole wormhole;
         private int waitMin;
         private int waitMax;
         private final List<Sector> waitSpots;
         
         private EdgeData(EdgeType type) {
             this.type = type;
             this.waitSpots = new ArrayList<>(MAX_SAFE_SPOT_OUTPUT);
         }
         
         
        private void clear() {
            this.waitSpots.clear();
            this.waitMax = 0;
            this.waitMin = 0;
        }
        
         public EdgeType getType() {
             return this.type;
         }
         
         public List<Sector> getWaitSpots() {
             return this.waitSpots;
         }
         
         public boolean hasWaitSpots() {
             return !this.waitSpots.isEmpty();
         }
         
         public boolean isWormhole() {
             return this.type == EdgeType.WORMHOLE || this.type == EdgeType.ENTRYPORTAL;
         }
         
         public Wormhole getWormhole() {
             return this.wormhole;
         }
         
         public boolean mustWait() {
             return this.waitMin > 0;
         }
         
         public int getWaitMin() {
             return this.waitMin;
         }
         
         public int getWaitMax() {
             return this.waitMax;
         }
     }
     
     
     
     private class UniverseBuilder implements LazyBuilder<Sector, EdgeData>, 
             EdgeCosts<EdgeData> {
         
         private final double COST_DIAGONAL = 1.5 / 60;
         private final double COST_NORMAL = 1.0 / 60;
         private final double COST_ENTRYPORTAL = COST_DIAGONAL * 3.0;
         private final double WORMHOLE_OFFSET = 100000.0;
         
         private final Set<Sector> done;
         
         public UniverseBuilder(RouteOptions options) {
             this.done = new HashSet<>();
         }
         
         @Override
         public double calculate(EdgeData data) {
             switch (data.getType()) {
             case NORMAL:   return COST_NORMAL;
             case DIAGONAL: return COST_DIAGONAL;
             case ENTRYPORTAL: return COST_ENTRYPORTAL;
             case WORMHOLE:
                 final Wormhole hole = data.getWormhole();
                 double modifier = 1.0;
                 switch (hole.requiresLoad()) {
                 case FULL:
                     modifier = 50.0;
                     break;
                 case PARTIAL:
                     modifier = 10.0;
                     break;
                 case NONE:
                 }
                 return WORMHOLE_OFFSET + modifier * Math.max(1, data.getWormhole().getMinUnload());
             default: return Double.MAX_VALUE;
             }
         }
 
         @Override
         public void collectIncident(Graph<Sector, EdgeData> graph, Sector source) {
             if (this.done.add(source)) {
                 // add wormhole edges
                 final Collection<Wormhole> holes = holeProvider.getWormholes(
                         source, quadProvider);
                 
                 for (final Wormhole hole : holes) {
                     final Quadrant targetQuad = quadProvider.getQuadrant(
                             hole.getTarget());
                     
                     final EdgeData d = EdgeData.wormhole(hole);
                     this.addNeighbour(targetQuad, hole.getTarget().getX(), 
                             hole.getTarget().getY(), graph, source, d);
                 }
                 
                 // add entry portals
                 for (final Sector portal : quadProvider.getEntryPortals()) {
                     final EdgeData d = EdgeData.entryPortal(source, portal);
                     final Quadrant targetQuad = quadProvider.getQuadrant(portal);
                     this.addNeighbour(targetQuad, portal.getX(), 
                             portal.getY(), graph, source, d);
                 }
                 
                 // add direct neighbours
                 final int x = source.getX();
                 final int y = source.getY();
                 final Quadrant quad = quadProvider.getQuadrant(source);
                 for (int i = -1; i < 2; ++i) {
                     for (int j = -1; j < 2; ++j) {
                         final boolean diagonal = Math.abs(i) == 1 && Math.abs(j) == 1;
                         final EdgeData d = EdgeData.sector(diagonal);
                         this.addNeighbour(quad, x + i, y + j, graph, source, d);
                     }
                 }
             }
         }
         
         
         
         private void addNeighbour(Quadrant quad, int x, int y, 
                 Graph<Sector, EdgeData> graph, Sector source, EdgeData edgeData) {
             if (x < 0 || x > quad.getMaxX() || y < 0 || y > quad.getMaxY() || 
                     (x == source.getX() && y == source.getY())) {
                 return;
             }
             final Sector neighbour = quad.getSector(x, y);
             if (neighbour.getType() != SectorType.NONE) {
                 final Graph<Sector, EdgeData>.Node vSource = graph.getNode(source);
                 final Graph<Sector, EdgeData>.Node vTarget = graph.getNode(neighbour, neighbour); 
                 vSource.edgeTo(vTarget, edgeData);
             }
         }
     }
     
     
     
     private class SectorHeuristic implements Heuristic<Sector> {
         
         @Override
         public double calculate(Sector v1, Sector v2) {
             return 0.0;
             /*if (v1.getQuadName().equals(v2.getQuadName())) {
                 final double dx = v1.getX() - v2.getX();
                 final double dy = v1.getY() - v2.getY();
                 return Math.sqrt(dx * dx + dy * dy);
             } else {
                 final Quadrant target = quadProvider.getQuadrant(v2);
                 // longest possible path in target quadrant
                 return Math.sqrt(
                         target.getMaxX() * target.getMaxX() + 
                         target.getMaxY() * target.getMaxY());
             }*/
         }
     }
     
     
     
     private final static Comparator<Sector> SAFE_SPOT_COMP = new Comparator<Sector>() {
         @Override
         public int compare(Sector o1, Sector o2) {
             // attacker bonus: the less, the better
             int c = Integer.compare(o1.getAttackerBonus(), o2.getAttackerBonus());
             if (c == 0) {
                 // defender bonus: the more, the better
                 c = Integer.compare(o2.getDefenderBonus(), o1.getDefenderBonus());
             }
             return c;
         }
     };
     
     
     
     private final static int MAX_SAFE_SPOT_OUTPUT = 2;
     
     private final QuadrantProvider quadProvider;
     private final WormholeProvider holeProvider;
     private final Graph<Sector, EdgeData> graph;
     private final Heuristic<Sector> heuristic;
     
     
     
     public PathPlanner(QuadrantProvider quadProvider, WormholeProvider holeProvider) {
         this.graph = new Graph<>();
         this.heuristic = new SectorHeuristic();
         this.quadProvider = quadProvider;
         this.holeProvider = holeProvider;
     }
     
     
     
     private final static class HighlightedSector extends SectorDecorator {
 
         private final SectorType highlight;
         
         public HighlightedSector(Sector wrapped, SectorType highlight) {
             super(wrapped);
             this.highlight = highlight;
         }
 
         @Override
         public SectorType getType() {
             return this.highlight;
         }
     }
     
     
     
     public final static class HighlightedQuadrant extends QuadrantDecorator {
         
         private static int IDS = 0;
         
         private final Map<String, SectorType> highlights;
         private final int id;
         
         public HighlightedQuadrant(Quadrant wrapped) {
             super(wrapped);
             this.highlights = new HashMap<>();
             this.id = IDS++;
         }
         
         public int getId() {
             return this.id;
         }
         
         public void highlight(Sector sector, SectorType type) {
             this.highlight(sector.getX(), sector.getY(), type);
         }
 
         public void highlight(int x, int y, SectorType type) {
             final String key = x + "_" + y; //$NON-NLS-1$
             SectorType st = this.highlights.get(key);
             if (st == null) {
                 this.highlights.put(key, type);
             } else {
                 // highlight for this sector already exists, keep the one with higher id
                 if (st.getId() < type.getId()) {
                     this.highlights.put(key, type);
                 }
             }
         }
         
         @Override
         public Sector getSector(int x, int y) {
             final String key = x + "_" + y; //$NON-NLS-1$
             final Sector sector = super.getSector(x, y);
             final SectorType type = this.highlights.get(key);
             if (type != null) {
                 return new HighlightedSector(sector, type);
             } else if (sector.getType() != SectorType.NONE){
                 // render all unrelated sectors dark
                 return new HighlightedSector(sector, SectorType.UNKNOWN);
             }
             return sector;
         }
     }
     
     
     
     public final static class Group {
         final List<Graph<Sector, EdgeData>.Edge> edges;
         final HighlightedQuadrant quad;
         
         private Group(Quadrant quadrant) {
             super();
             this.quad = new HighlightedQuadrant(quadrant);
             this.edges = new ArrayList<>();
         }
         
         public List<Graph<Sector, EdgeData>.Edge> getEdges() {
             return this.edges;
         }
         
         public Quadrant getQuadrant() {
             return this.quad;
         }
         
         public String getQuadName() {
             return this.quad.getName();
         }
     }
     
     
     
     public class UniversePath {
 
         private final Graph<Sector, EdgeData>.Path path;
         private final List<Group> groups;
         private final List<Wormhole> wormholes;
         private final int sectorJumps;
         private final int quadJumps;
         private final int minUnload;
         private final int maxUnload;
         private final int maxWaitTime;
         private final RouteOptions options;
         
         private UniversePath(Graph<Sector, EdgeData>.Path path, RouteOptions options) {
             this.path = path;
             this.options = options;
             this.groups = new ArrayList<>();
             this.wormholes = new ArrayList<>();
 
             String lastQuad = ""; //$NON-NLS-1$
             Group currentGroup = null;
             
             // always consider to be unloaded
             final int jtMinutes = (int) (options.totalJumpTime.getSpan() / 60.0);
             final int cjtMinutes = (int) (options.currentJumpTime.getSpan() / 60.0);
             int currentMinUnload = cjtMinutes;
             int currentMaxUnload = cjtMinutes;
             
             int sumMinUnload = 0;
             int sumMaxUnload = 0;
             
             int maximumWaitTime = 0;
             
             boolean first = true;
             Graph<Sector, EdgeData>.Edge lastEdge = null;
             final Iterator<Graph<Sector, EdgeData>.Edge> it = path.getPath().iterator();
 
             while (it.hasNext()) {
                 final Graph<Sector, EdgeData>.Edge e = it.next();
                // before processing edge, reset its data
                e.getData().clear();
                
                 final Sector source = e.getSource().getData();
                 SectorType highlight = SectorType.HIGHLIGHT_SECTOR;
                 
                 if (currentGroup == null || !source.getQuadName().equals(lastQuad)) {
                     final Quadrant quad = quadProvider.getQuadrant(source.getQuadName());
                     currentGroup = new Group(quad);
                     this.groups.add(currentGroup);
                 }
                 
                 if (lastEdge != null && lastEdge.getData().isWormhole()) {
                     // if last edge was a WH, current source node is a WH drop
                     highlight = SectorType.HIGHLIGHT_WH_DROP;
                 }
                 currentGroup.edges.add(e);
                 lastQuad = source.getQuadName();
                 
                 if (e.getData().isWormhole()) {
                     final Wormhole hole = e.getData().getWormhole();
                     sumMinUnload += hole.getMinUnload();
                     sumMaxUnload += hole.getMaxUnload();
                     this.wormholes.add(hole);
                     highlight = SectorType.HIGHLIGHT_WH_START;
                     
                     switch (hole.requiresLoad()) {
                     case FULL:
                         e.getData().waitMin = currentMinUnload;
                         e.getData().waitMax = currentMaxUnload;
                         
                         currentMinUnload = hole.getMinUnload();
                         currentMaxUnload = hole.getMaxUnload();
                         break;
                     case PARTIAL:
                         e.getData().waitMin = currentMinUnload + hole.getMaxUnload() - jtMinutes;
                         e.getData().waitMax = currentMaxUnload + hole.getMaxUnload() - jtMinutes;
                         
                         currentMinUnload += Math.max(hole.getMinUnload() - e.getData().waitMin, 0);
                         currentMaxUnload += Math.max(hole.getMaxUnload() - e.getData().waitMax, 0);
                         break;
                     case NONE:
                         currentMinUnload += hole.getMinUnload();
                         currentMaxUnload += hole.getMaxUnload();
                     default:
                     }
                     maximumWaitTime = Math.max(maximumWaitTime, e.getData().waitMax);
                     
                     if (e.getData().mustWait()) {
                         // find good spots
                         final Quadrant quad = quadProvider.getQuadrant(source.getQuadName());
                         final List<Sector> spots = QuadrantUtils.getNearSectors(source, 
                                 quad, options.maxWaitSpotDistance, QuadrantUtils.ACCEPT_ALL);
                         Collections.sort(spots, SAFE_SPOT_COMP);
                         final int bound = Math.min(spots.size(), MAX_SAFE_SPOT_OUTPUT);
                         for (int i = 0; i < bound; ++i) {
                             final Sector safeSpot = spots.get(i);
                             e.getData().waitSpots.add(safeSpot);
                             
                             if (safeSpot.equals(e.getSource().getData())) {
                                 currentGroup.quad.highlight(safeSpot, 
                                         SectorType.HIGHLIGHT_SAFE_SPOT_WL);
                             } else {
                                 currentGroup.quad.highlight(safeSpot, 
                                     SectorType.HIGHLIGHT_SAFE_SPOT);
                             }
                         }
                     }
                 }
                 if (first) {
                     highlight = SectorType.HIGHLIGHT_START;
                 }
                 currentGroup.quad.highlight(e.getSource().getData(), highlight);
                 first = false;
                 lastEdge = e;
             }
             // lastEdge is null if no way was found
             if (lastEdge != null) {
                 currentGroup.quad.highlight(lastEdge.getTarget().getData(), 
                     SectorType.HIGHLIGHT_TARGET);
             }
             this.quadJumps = wormholes.size();
             this.sectorJumps = path.getPath().size() - this.quadJumps;
             this.minUnload = sumMinUnload;
             this.maxUnload = sumMaxUnload;
             this.maxWaitTime = maximumWaitTime;
         }
         
         public int getMaxSafeSpotDistance() {
             return options.maxWaitSpotDistance;
         }
         
         public int getMaxWaitTime() {
             return this.maxWaitTime;
         }
         
         public boolean pathFound() {
             return !this.path.getPath().isEmpty();
         }
         
         public int getMaxUnload() {
             return this.maxUnload;
         }
         
         public int getMinUnload() {
             return this.minUnload;
         }
         
         public int getSectorJumps() {
             return this.sectorJumps;
         }
         
         public List<Wormhole> getWormholes() {
             return this.wormholes;
         }
         
         public int getQuadJumps() {
             return this.quadJumps;
         }
         
         public List<Group> getGroups() {
             return this.groups;
         }
     }
     
     
     
     public UniversePath findShortestPath(Sector start, Sector target, 
             RouteOptions options) {
         final UniverseBuilder builder = new UniverseBuilder(options);
         final Graph<Sector, EdgeData>.Path path = this.graph.findShortestPath(
                 start, target, builder, this.heuristic, builder);
         final UniversePath result = new UniversePath(path, options);
         return result;
     }
 }
