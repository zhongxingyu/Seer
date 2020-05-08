 package polly.rx.core.orion.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import polly.rx.core.orion.Graph;
 import polly.rx.core.orion.Graph.EdgeCosts;
 import polly.rx.core.orion.Graph.LazyBuilder;
 import de.skuzzle.polly.tools.Equatable;
 
 
 public final class QuadrantUtils {
 
     private QuadrantUtils() {}
     
     
     private final static EdgeCosts<Costs> COST_CALCULATOR = new EdgeCosts<Costs>() {
         @Override
         public double calculate(Costs data) {
             return data.isDiagonal ? 1.0 : 2.0;
         }
     };
     
     
     
     private static class Costs {
         private final boolean isDiagonal;
         
         public Costs(boolean isDiagonal) {
             this.isDiagonal = isDiagonal;
         }
     }
     
     
     
     public interface SectorFilter {
         public boolean accept(Sector sector);
     }
     
     
     
     public final static SectorFilter ACCEPT_ALL = new SectorFilter() {
         @Override
         public boolean accept(Sector sector) {
             return true;
         }
     };
     
     
     
     
     
     public static boolean sectorsEqual(Sector s1, Sector s2) {
        return s1.getX() == s2.getX() && s1.getY() == s1.getY() && 
                 s1.getQuadName().equals(s2.getQuadName());
     }
     
     
     
     public static String createMapKey(int x, int y) {
         return x + "_" + y; //$NON-NLS-1$
     }
     
     
     
     public static String createMapKey(Sector sector) {
         return createMapKey(sector.getX(), sector.getY());
     }
     
     
     
     public static Sector noneSector(final String quadName, final int x, final int y) {
         return new Sector() {
             @Override
             public Class<?> getEquivalenceClass() {
                 return Sector.class;
             }
             
             @Override
             public boolean actualEquals(Equatable o) {
                 final Sector other = (Sector) o;
                 return quadName.equals(quadName) && x == other.getX() && 
                         y == other.getY();
             }
             
             @Override
             public int getY() {
                 return y;
             }
             
             @Override
             public int getX() {
                 return x;
             }
             
             @Override
             public SectorType getType() {
                 return SectorType.NONE;
             }
             
             @Override
             public int getSectorGuardBonus() {
                 return 0;
             }
             
             @Override
             public Collection<? extends Production> getRessources() {
                 return Collections.emptyList();
             }
             
             @Override
             public String getQuadName() {
                 return quadName;
             }
             
             @Override
             public int getDefenderBonus() {
                 return 0;
             }
             
             @Override
             public Date getDate() {
                 return new Date();
             }
             
             @Override
             public int getAttackerBonus() {
                 return 0;
             }
         };
     }
     
     
     
     public static int getDistance(Sector source, Sector target, final Quadrant quadrant) {
         final Graph<Sector, Costs> g = new Graph<>();
         return getDistance(source, target, quadrant, g);
     }
     
     
     
     private static int getDistance(Sector source, Sector target, final Quadrant quadrant, 
             Graph<Sector, Costs> g) {
         final LazyBuilder<Sector, Costs> builder = new LazyBuilder<Sector, Costs>() {
             
             final Set<Sector> done = new HashSet<>();
             
             @Override
             public void collectIncident(Graph<Sector, Costs> source, Sector currentNode) {
                 if (!done.add(currentNode)) {
                     return;
                 }
                 for (int i = -1; i < 2; ++i) {
                     for (int j = -1; j < 2; ++j) {
                         if (i == 0 && j == 0) {
                             // exclude sector itself
                             continue;
                         }
                         final int x = currentNode.getX() + i;
                         final int y = currentNode.getY() + j;
                         final boolean diagonal = Math.abs(i) == 1 && Math.abs(j) == 1;
                         
                         if (x >= 0 && y >= 0 && x <= quadrant.getMaxX() && y <= quadrant.getMaxY()) {
                             final Sector neighbor = quadrant.getSector(x, y);
                             
                             if (neighbor.getType() != SectorType.NONE) {
                                 final Graph<Sector, Costs>.Node current = source.getNode(currentNode);
                                 final Graph<Sector, Costs>.Node node = source.getNode(neighbor, neighbor);
                                 current.edgeTo(node, new Costs(diagonal));
                             }
                         }
                     }
                 }
             }
         };
         return g.findShortestPath(source, target, builder, 
                 Graph.<Sector>noHeuristic(), COST_CALCULATOR).getPath().size();
     }
     
     
     
     public static List<Sector> getNearSectors(Sector source, Quadrant quadrant, 
             int maxDistance, SectorFilter filter) {
         final List<Sector> result = new ArrayList<>();
         final Graph<Sector, Costs> g = new Graph<>();
         
         for (int i = -maxDistance; i < maxDistance; ++i) {
             for (int j = -maxDistance; j < maxDistance; ++j) {
                 final int x = source.getX() + i;
                 final int y = source.getY() + j;
                 if (x >= 0 && y >= 0 && x <= quadrant.getMaxX() && y <= quadrant.getMaxY()) {
                     final Sector sector = quadrant.getSector(x, y);
                     
                     if (sector.getType() != SectorType.NONE && filter.accept(sector)) {
                         final int dist = getDistance(source, sector, quadrant, g);
                         if (dist <= maxDistance) {
                             result.add(sector);
                         }
                     }
                         
                 }
             }
         }
         return result;
     }
 }
