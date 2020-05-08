 package org.chris_martin.delaunay;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Ordering;
 import org.chris_martin.delaunay.Geometry.Line;
 import org.chris_martin.delaunay.Geometry.Side;
 import org.chris_martin.delaunay.Geometry.Vec;
 
 import static com.google.common.collect.Sets.newHashSet;
 import static java.util.Arrays.asList;
 import static java.util.Collections.min;
 import static java.util.Collections.unmodifiableCollection;
 import static org.chris_martin.delaunay.Geometry.aToB;
 import static org.chris_martin.delaunay.Geometry.circle;
 import static org.chris_martin.delaunay.Geometry.origin;
 import static org.chris_martin.delaunay.Geometry.xy;
 import static org.testng.collections.Lists.newArrayList;
 import static org.testng.collections.Maps.newHashMap;
 
 public final class Mesh {
 
   private int previousVertexId;
 
   List<Triangle> triangles = newArrayList();
   public Collection<Triangle> triangles() { return unmodifiableCollection(triangles); }
 
   List<Vertex> vertices = newArrayList();
   public Collection<Vertex> vertices() { return unmodifiableCollection(vertices); }
 
   private LoadingCache<Edge, Double> springLength = CacheBuilder.newBuilder().build(
     new CacheLoader<Edge, Double>() { public Double load(Edge edge) {
       return edge.a.loc.sub(edge.b.loc).mag(); }});
 
   public Mesh() {}
   public Mesh(Collection<VertexConfig> points) { setPoints(points); }
 
   private static final double GRAVITY = 0.04;
   private static final double SPRING = .05;
   private static final double INERTIA = 10;
   private static final double DAMPING = .001;
 
   private boolean meshIsValid() {
     for (Vertex v : vertices) {
       assert v.corner.vertex == v;
       Lists.<Corner>newArrayList(v.corners());
     }
     for (Triangle t : triangles) {
       for (Corner c : t.corners()) {
         assert vertices.contains(c.vertex);
       }
     }
     return true;
   }
 
   public void setPoints(Collection<VertexConfig> points) {
     Delaunay d = new Delaunay(points);
     triangles = d.triangles;
     vertices = d.vertices;
   }
 
   public Collection<Edge> edges() {
     Set<Edge> edges = newHashSet();
     for (Triangle t : triangles) edges.addAll(t.edges());
     return edges;
   }
 
   public void physics(double timeStep) {
     for (Vertex v : vertices) {
       v.nextVelocity = v.velocity;
     }
     Collections.shuffle(vertices);
     for (int i = 0; i < 20; i++) {
       for (Vertex v : vertices) {
         if (v.physics == VertexPhysics.FREE) {
           Vec accel = xy(0, GRAVITY);
           List<Vertex> adjs = newArrayList();
           for (Corner c : v.corners()) adjs.add(c.next().vertex());
           Collections.shuffle(adjs);
           for (Vertex adj : adjs) {
             double desiredLength = springLength.getUnchecked(new Edge(v, adj));
             double actualLength = adj.nextPosition(timeStep).sub(v.nextPosition(timeStep)).mag();
             double stretch = actualLength - desiredLength;
             accel = accel.add(adj.loc.sub(v.loc).mag(stretch * SPRING));
           }
           v.nextVelocity = v.velocity.mult(INERTIA-1).add(accel).div(INERTIA);
           v.nextVelocity = v.nextVelocity.mag(Math.max(0, v.nextVelocity.mag() - DAMPING));
         }
       }
     }
     for (Vertex v : vertices) {
       v.loc = v.nextPosition(timeStep);
       v.velocity = v.nextVelocity;
       v.nextVelocity = null;
     }
   }
 
   public void remove(Edge e) {
     if (!vertices.contains(e.a) || !vertices.contains(e.b)) return;
     for (Triangle t : e.triangles()) remove(t);
   }
   public void remove(Triangle t) {
     for (Corner c : t.corners()) {
       c.swings.prev.corner.swings.next = new Swing(c.swings.next.corner, true);
       c.swings.next.corner.swings.prev = new Swing(c.swings.prev.corner, true);
     }
     for (Corner c : t.corners()) if (c.vertex.corner == c) c.vertex.corner = c.swings.next.corner;
     assert meshIsValid();
     for (Corner c : t.corners()) {
       if (c.swings.next.corner == c) {
         vertices.remove(c.vertex);
       } else {
         ensureManifold(c.vertex);
       }
     }
     triangles.remove(t);
     assert meshIsValid();
   }
 
   private void ensureManifold(final Vertex v) {
     final List<Vertex> resultingVertices = newArrayList(v);
     new Object() {
       List<List<Corner>> sections = Lists.newArrayList();
       List<Corner> currentSection;
       Corner currentCorner = v.corner;
       void newSection() { sections.add(currentSection = Lists.<Corner>newArrayList()); }
       {
         newSection();
         do {
           currentSection.add(currentCorner);
           Swing swing = currentCorner.swings.next;
           if (swing.isSuper) newSection();
           currentCorner = swing.corner;
         } while (currentCorner != v.corner);
         sections.get(sections.size()-1).addAll(sections.get(0));
         sections.remove(0);
         if (sections.size() == 2) {
           for (int i = 0; i < 2; i++) {
             List<Corner> section = sections.get(i);
             Corner first = section.get(0), last = section.get(section.size()-1);
             first.swings.prev.corner = last;
             last.swings.next.corner = first;
             if (i != 0) {
               Vertex clone = new Vertex(new VertexConfig(v.loc, v.physics));
               clone.corner = first;
               vertices.add(clone);
               resultingVertices.add(clone);
               for (Corner c : section) c.vertex = clone;
             } else {
               v.corner = first;
             }
           }
         }
       }
     };
     for (Vertex rv : resultingVertices) {
       assert Lists.<Corner>newArrayList(rv.corners()) != null;
     }
   }
 
   public enum VertexPhysics { PINNED, FREE }
 
   public static class VertexConfig {
     Vec loc; VertexPhysics physics;
     public VertexConfig(Vec loc, VertexPhysics physics) {
       this.loc = loc; this.physics = physics; } }
 
   public class Vertex {
     private final int id = ++previousVertexId; public int id() { return id; }
     public int hashCode() { return id; }
     private Vec loc; public Vec loc() { return loc; }
     private final VertexPhysics physics;
     private Vertex(VertexConfig config) { this.loc = config.loc; this.physics = config.physics; }
     private Corner corner; public Corner corner() { return corner; }
     private Vec velocity = origin(), nextVelocity;
     Vec nextPosition(double timeStep) { return nextVelocity.mult(timeStep).add(loc); }
     public Iterable<Corner> corners() { return new Iterable<Corner>() {
       public Iterator<Corner> iterator() { return cornersIter(); } }; }
     public Iterator<Corner> cornersIter() { return new Iterator<Corner>() {
       Corner c = Vertex.this.corner;
       Set<Corner> visited; { assert (visited = newHashSet()) != null; }
       public boolean hasNext() { return c != null; }
       public Corner next() {
         if (c == null) throw new NoSuchElementException();
         Corner retval = c;
         c = c.swings.next.corner;
         if (c == Vertex.this.corner) c = null;
         assert visited.add(retval);
         return retval;
       }
       public void remove() { throw new UnsupportedOperationException(); }
     }; }
   }
 
   public class Corner {
     private Triangle triangle; private Corner next, prev;
     private Vertex vertex; private Swings swings = new Swings();
     private Corner(Vertex vertex, Triangle triangle) {
       this.vertex = vertex; this.triangle = triangle;
       if (vertex.corner == null) vertex.corner = this; }
     public Triangle triangle() { return triangle; }
    public Vertex vertex() { assert vertices.contains(vertex); return vertex; }
     public Corner next() { return next; } public Corner prev() { return prev; }
     public Swings swing() { return swings; }
   }
   public static class Swings {
     private Swing prev = new Swing(), next = new Swing();
     public Swing prev() { return prev; } public Swing next() { return next; } }
   public static class Swing {
     private Corner corner; public Corner corner() { return corner; }
     private boolean isSuper; public boolean isSuper() { return isSuper; }
     public Swing() {}
     public Swing(Corner corner, boolean isSuper) { this.corner = corner; this.isSuper = isSuper; }
     public Swing copy() { Swing x = new Swing(); x.corner = corner; x.isSuper = isSuper; return x; }
   }
 
   public class Edge {
     private final Vertex a, b;
     private Edge(Vertex a, Vertex b) {
       boolean flip = a.id > b.id;
       this.a = flip ? b : a; this.b = flip ? a : b; }
     public Vertex a() { return a; } public Vertex b() { return b; }
     public List<Vertex> vertices() { return asList(a(), b()); }
     public Line line() { return aToB(a.loc, b.loc); }
     public boolean equals(Object o) {
       return this == o || (o instanceof Edge && a == ((Edge) o).a && b == ((Edge) o).b); }
     public int hashCode() { return 31 * a.hashCode() + b.hashCode(); }
     public List<Triangle> triangles() {
       List<Triangle> ts = newArrayList(2);
       Corner c1=null; for (Corner c : a.corners()) if (c.next.vertex == b) c1 = c; if (c1!=null) ts.add(c1.triangle);
       Corner c2=null; for (Corner c : b.corners()) if (c.next.vertex == a) c2 = c; if (c2!=null) ts.add(c2.triangle);
       return ts; }
   }
 
   public class Triangle {
     private final Corner a, b, c;
     public Triangle(Vertex a, Vertex b, Vertex c) {
       // vertices are sorted in clockwise rotation about the circumcenter
       final Vec cc = circle(a.loc(), b.loc(), c.loc()).center();
       class X { final double ang; final Vertex v; X(Vertex v) { this.v = v; ang = v.loc().sub(cc).ang(); } }
       X[] xs = { new X(a), new X(b), new X(c) };
       Arrays.sort(xs, new Comparator<X>() { public int compare(X a, X b) { return Double.compare(a.ang, b.ang); }});
       this.a = new Corner(xs[0].v, this); this.b = new Corner(xs[1].v, this); this.c = new Corner(xs[2].v, this);
       initNextPrev();
     }
     public Corner a() { return a; } public Corner b() { return b; } public Corner c() { return c; }
     private void initNextPrev() { a.next = b; b.next = c; c.next = a; a.prev = c; b.prev = a; c.prev = b; }
     public List<Corner> corners() { return asList(a, b, c); }
     public List<Edge> edges() { Vertex a = this.a.vertex, b = this.b.vertex, c = this.c.vertex;
       return asList(new Edge(a, b), new Edge(b, c), new Edge(c, a)); }
     public List<Line> lines() { Vec a = this.a.vertex.loc, b = this.b.vertex.loc, c = this.c.vertex.loc;
       return asList(aToB(a, b), aToB(b, c), aToB(c, a)); }
     public boolean contains(Vec p) { for (Line l : lines()) if (l.side(p) != Side.LEFT) return false; return true; }
     public Corner earCorner() {
       for (Corner corner : asList(a, b, c)) if (corner.swings.next.corner == corner) return corner; return null; }
   }
 
   private class Delaunay {
 
     List<Triangle> triangles = newArrayList();
     List<Vertex> vertices = newArrayList();
     List<Edge> edges = newArrayList();
     List<Edge> convexHull = newArrayList();
     Map<Edge, Vertex> openEdges = newHashMap();
 
     Delaunay(Collection<VertexConfig> points) {
       if (points.size() < 3) throw new IllegalArgumentException();
       for (VertexConfig p : points) vertices.add(new Vertex(p));
       calculateConvexHull();
       for (Edge edge : convexHull.subList(0, 1)) { edges.add(edge); openEdges.put(edge, null); }
       while (openEdges.size() != 0) tryNextEdge();
       calculateSwing();
     }
 
     void calculateConvexHull() {
       final Vertex start = min(vertices, new Comparator<Vertex>() {
         public int compare(Vertex i, Vertex j) { return Double.compare(key(i), key(j)); }
         double key(Vertex v) {return v.loc().y(); }
       });
       Vertex a = start;
       while (true) {
         final Vertex a$ = a;
         Vertex b = min(vertices, new Comparator<Vertex>() {
           public int compare(Vertex i, Vertex j) { return Double.compare(key(i), key(j)); }
           double key(Vertex v) { return v == a$ ? Double.MAX_VALUE : (v.loc().sub(a$.loc())).ang(); }
         });
         convexHull.add(new Edge(a, b)); a = b; if (a == start) break;
       }
     }
 
     void tryNextEdge() {
       final Edge edge; Vertex previousVertex; {
         Entry<Edge, Vertex> entry = openEdges.entrySet().iterator().next();
         edge = entry.getKey(); previousVertex = entry.getValue(); openEdges.remove(edge); }
       final Line line = edge.line();
       Iterable<Vertex> candidateVertices;
       if (previousVertex == null) {
         candidateVertices = Iterables.filter(vertices, new Predicate<Vertex>() { public boolean apply(Vertex vertex) {
           return !edge.vertices().contains(vertex); } });
       } else {
         if (convexHull.contains(edge)) return;
         final Side side = line.side(previousVertex.loc()).opposite();
         candidateVertices = Iterables.filter(vertices, new Predicate<Vertex>() { public boolean apply(Vertex vertex) {
           return !edge.vertices().contains(vertex) && line.side(vertex.loc()) == side; } });
       }
       if (!candidateVertices.iterator().hasNext()) return;
       final Vertex v = Ordering.natural().onResultOf(new Function<Vertex, Double>() { public Double apply(Vertex v) {
         return line.bulge(v.loc()); }}).min(candidateVertices);
       Triangle t = new Triangle(edge.a(), edge.b(), v);
       triangles.add(t);
       for (List<Vertex> vertexPair : ImmutableList.of(edge.vertices(), Lists.reverse(edge.vertices()))) {
         Vertex u = vertexPair.get(0), w = vertexPair.get(1); Edge uv = new Edge(u, v);
         if (openEdges.remove(uv) == null) { edges.add(uv); openEdges.put(uv, w); }
       }
     }
 
     void calculateSwing() {
       Multimap<Vertex, Corner> v2c = ArrayListMultimap.create();
       for (Triangle t : triangles) for (Corner c : t.corners()) v2c.put(c.vertex, c);
       for (Collection<Corner> cs : v2c.asMap().values()) {
         for (Corner i : cs) for (Corner j : cs) if (i.next.vertex == j.prev.vertex)
           { j.swings.next.corner = i; i.swings.prev.corner = j; }
         Corner si = null, sj = null;
         for (Corner i : cs) {
           if (i.swings.next.corner == null) si = i;
           if (i.swings.prev.corner == null) sj = i;
         }
         if (si != null) {
           si.swings.next.corner = sj; si.swings.next.isSuper = true;
           sj.swings.prev.corner = si; sj.swings.prev.isSuper = true;
         }
       }
     }
 
   }
 
 }
