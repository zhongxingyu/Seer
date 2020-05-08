 package net.cscott.sdr.calls;
 
 import static net.cscott.sdr.calls.StandardDancer.COUPLE_1_BOY;
 import static net.cscott.sdr.calls.StandardDancer.COUPLE_1_GIRL;
 import static net.cscott.sdr.calls.StandardDancer.COUPLE_2_BOY;
 import static net.cscott.sdr.calls.StandardDancer.COUPLE_2_GIRL;
 import static net.cscott.sdr.calls.StandardDancer.COUPLE_3_BOY;
 import static net.cscott.sdr.calls.StandardDancer.COUPLE_3_GIRL;
 import static net.cscott.sdr.calls.StandardDancer.COUPLE_4_BOY;
 import static net.cscott.sdr.calls.StandardDancer.COUPLE_4_GIRL;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.util.Box;
 import net.cscott.sdr.util.Fraction;
 import net.cscott.sdr.util.Point;
 import net.cscott.sdr.util.SdrToString;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.junit.runner.RunWith;
 
 /** A Formation is a set of dancers and positions for them.
  * Certain dancers in the formation can be selected. 
  * In addition, all dancers can be tagged with modifiers
  * such as 'BEAU', 'BELLE', 'LEADER', 'TRAILER', etc.
  */
 @RunWith(value=JDoctestRunner.class)
 public class Formation {
     protected final Map<Dancer,Position> location;
     protected final Set<Dancer> selected;
     protected Formation(Map<Dancer,Position> location, Set<Dancer> selected) {
         this.location = location;
         this.selected = selected;
     }
     public Formation(Map<Dancer,Position> location) {
         this.location = Collections.unmodifiableMap(new LinkedHashMap<Dancer,Position>(location));
         this.selected = this.location.keySet();
     }
 
     /** Create a new formation containing only the selected dancers from this
      * formation.
      * @doc.test
      *  Create a formation containing only the sides from a squared set:
      *  js> importPackage(java.util)
      *  js> f = Formation.SQUARED_SET ; undefined
      *  js> sides = [d for each (d in Iterator(f.sortedDancers())) if (d.isSide())]
      *  COUPLE 4 BOY,COUPLE 2 GIRL,COUPLE 4 GIRL,COUPLE 2 BOY
      *  js> f2 = f.select(Arrays.asList(sides)).onlySelected()
      *  net.cscott.sdr.calls.Formation[
      *    location={COUPLE 4 BOY=-3,1,e, COUPLE 2 GIRL=3,1,w, COUPLE 4 GIRL=-3,-1,e, COUPLE 2 BOY=3,-1,w}
      *    selected=[COUPLE 4 BOY, COUPLE 2 GIRL, COUPLE 4 GIRL, COUPLE 2 BOY]
      *  ]
      *  js> f2.toStringDiagram()
      *  4B>            2G<
      *  
      *  4G>            2B<
      */
     public Formation onlySelected() {
 	Map<Dancer,Position> nloc = new LinkedHashMap<Dancer,Position>
 	    (this.location);
 	nloc.keySet().retainAll(this.selected);
 	return new Formation(nloc);
     }
     /** Return true iff the given dancer is selected.
      * @doc.test
      *  Select the sides, and verify that isSelected() returns
      *  the expected results:
      *  js> importPackage(java.util)
      *  js> f = Formation.SQUARED_SET ; undefined
      *  js> heads = [d for each (d in Iterator(f.sortedDancers())) if (d.isHead())]
      *  COUPLE 3 GIRL,COUPLE 3 BOY,COUPLE 1 BOY,COUPLE 1 GIRL
      *  js> sides = [d for each (d in Iterator(f.sortedDancers())) if (d.isSide())]
      *  COUPLE 4 BOY,COUPLE 2 GIRL,COUPLE 4 GIRL,COUPLE 2 BOY
      *  js> f2 = f.select(Arrays.asList(sides)); undefined
      *  js> [f2.isSelected(d) for each (d in sides)]
      *  true,true,true,true
      *  js> [f2.selectedDancers().contains(d) for each (d in sides)]
      *  true,true,true,true
      *  js> [f2.isSelected(d) for each (d in heads)]
      *  false,false,false,false
      *  js> [f2.selectedDancers().contains(d) for each (d in heads)]
      *  false,false,false,false
      */
     public boolean isSelected(Dancer d) {
 	return selected.contains(d);
     }
     public Set<Dancer> selectedDancers() {
 	return selected;
     }
     public Set<Dancer> dancers() {
 	return location.keySet();
     }
     public Position location(Dancer d) {
 	return location.get(d);
     }
     /** Return the bounds of this formation, which is the bounding
      * box around all the dancers' bounding boxes.  This means that
      * the 1 unit border from {@link #bounds(Dancer)} applies here
      * as well. If there are no dancers, returns a zero-width,
      * zero-height box centered at the origin.
      * @doc.test
      *  js> Formation.SQUARED_SET.bounds()
      *  -4,-4,4,4
      *  js> Formation.SQUARED_SET.select(java.util.Collections.EMPTY_SET).onlySelected().bounds()
      *  0,0,0,0
      */
     public Box bounds() {
         Fraction minx=null,miny=null,maxx=null,maxy=null;
         for (Position p : location.values()) {
             if (minx==null || minx.compareTo(p.x) > 0) minx = p.x;
             if (maxx==null || maxx.compareTo(p.x) < 0) maxx = p.x;
             if (miny==null || miny.compareTo(p.y) > 0) miny = p.y;
             if (maxy==null || maxy.compareTo(p.y) < 0) maxy = p.y;
         }
         Point ll, ur;
         if (minx==null) {
             assert minx==null && miny==null && maxx==null && maxy==null;
             ll = new Point(Fraction.ZERO, Fraction.ZERO);
             ur = ll;
         } else {
             ll = new Point(minx.subtract(Fraction.ONE),
                            miny.subtract(Fraction.ONE));
             ur = new Point(maxx.add(Fraction.ONE),
                            maxy.add(Fraction.ONE));
         }
         return new Box(ll, ur);
     }
     /** Return the bounds of the given dancer -- always its position
      * plus or minus 1 unit on the x and y axes. */
     public Box bounds(Dancer d) {
         Position p = location(d);
         return new Box
               (new Point(p.x.subtract(Fraction.ONE),p.y.subtract(Fraction.ONE)),
                new Point(p.x.add(Fraction.ONE),p.y.add(Fraction.ONE)));
     }
     /** Build a new formation with only the given dancers
      * selected.
      * @doc.test
      *  js> importPackage(java.util)
      *  js> f = Formation.SQUARED_SET ; undefined
      *  js> heads = [d for each (d in Iterator(f.sortedDancers())) if (d.isHead())]
      *  COUPLE 3 GIRL,COUPLE 3 BOY,COUPLE 1 BOY,COUPLE 1 GIRL
      *  js> f2 = f.select(Arrays.asList(heads))
      *  net.cscott.sdr.calls.Formation[
      *    location={COUPLE 3 GIRL=-1,3,s, COUPLE 3 BOY=1,3,s, COUPLE 4 BOY=-3,1,e, COUPLE 2 GIRL=3,1,w, COUPLE 4 GIRL=-3,-1,e, COUPLE 2 BOY=3,-1,w, COUPLE 1 BOY=-1,-3,n, COUPLE 1 GIRL=1,-3,n}
      *    selected=[COUPLE 3 GIRL, COUPLE 3 BOY, COUPLE 1 BOY, COUPLE 1 GIRL]
      *  ]
      */
     public Formation select(Collection<Dancer> d) {
         Set<Dancer> nSel = new LinkedHashSet<Dancer>(dancers());
         nSel.retainAll(d);
         return new Formation(location, Collections.unmodifiableSet(nSel));
     }
     /** Convenience method for {@link #select(Collection)}. */
     public Formation select(Dancer... d) {
         return this.select(Arrays.asList(d));
     }
     /**
      * Build a new formation, centered on 0,0.
      * @doc.test Isolate the #1 couple, then recenter:
      *  js> importPackage(java.util)
      *  js> couple1 = [StandardDancer.COUPLE_1_BOY, StandardDancer.COUPLE_1_GIRL]
      *  COUPLE 1 BOY,COUPLE 1 GIRL
      *  js> f = Formation.SQUARED_SET.select(Arrays.asList(couple1)).onlySelected()
      *  net.cscott.sdr.calls.Formation[
      *    location={COUPLE 1 BOY=-1,-3,n, COUPLE 1 GIRL=1,-3,n}
      *    selected=[COUPLE 1 BOY, COUPLE 1 GIRL]
      *  ]
      *  js> f.isCentered()
      *  false
      *  js> f = f.recenter()
      *  net.cscott.sdr.calls.Formation[
      *    location={COUPLE 1 BOY=-1,0,n, COUPLE 1 GIRL=1,0,n}
      *    selected=[COUPLE 1 BOY, COUPLE 1 GIRL]
      *  ]
      *  js> f.isCentered()
      *  true
      */
     public Formation recenter() {
         Box bounds = bounds();
         Fraction ox = bounds.ll.x.add(bounds.ur.x).divide(Fraction.TWO);
         Fraction oy = bounds.ll.y.add(bounds.ur.y).divide(Fraction.TWO);
        if (ox.equals(Fraction.ZERO) && oy.equals(Fraction.ZERO))
            return this; // efficiency
         Map<Dancer,Position> m = new LinkedHashMap<Dancer,Position>(location.size());
         for (Map.Entry<Dancer,Position> me : location.entrySet())
             m.put(me.getKey(), new Position(me.getValue().x.subtract(ox),
                     me.getValue().y.subtract(oy), me.getValue().facing));
         return new Formation(Collections.unmodifiableMap(m), selected);
     }
     /**
      * Build a new formation, like this one except rotated around 0,0.
      * We rotate CW by the amount given in the {@code rotation} parameter;
      * "north" corresponds to no rotation.
      * @doc.test
      *  Rotate the squared set 90 degrees CW:
      *  js> Formation.SQUARED_SET.rotate(ExactRotation.ONE_QUARTER).toStringDiagram()
      *       4Gv  4Bv
      *  
      *  1B>            3G<
      *  
      *  1G>            3B<
      *  
      *       2B^  2G^
      * @doc.test Non-orthogonal rotation:
      *  js> Formation.SQUARED_SET.rotate(ExactRotation.ONE_EIGHTH).toStringDiagram()
      *       4BQ       3GL
      *  
      *  4GQ                 3BL
      *  
      *  
      *  
      *  1B7                 2G`
      *  
      *       1G7       2B`
      */
     public Formation rotate(ExactRotation rotation) {
         Map<Dancer,Position> m = new LinkedHashMap<Dancer,Position>(location.size());
         for (Map.Entry<Dancer,Position> me : location.entrySet())
             m.put(me.getKey(), me.getValue().rotateAroundOrigin(rotation));
         return new Formation(Collections.unmodifiableMap(m), selected);
     }
     /**
      * Return true if the given formation is centered at the origin.
      * @doc.test
      *  js> Formation.SQUARED_SET.isCentered()
      *  true
      */
     public boolean isCentered() {
         Box bounds = bounds();
         return
         (bounds.ll.x.add(bounds.ur.x).compareTo(Fraction.ZERO) == 0) &&
         (bounds.ll.y.add(bounds.ur.y).compareTo(Fraction.ZERO) == 0);
     }
     /**
      * Return a {@link Dancer} {@link Comparator} that compares dancers
      * based on their positions within this {@link Formation}.
      */
     public Comparator<Dancer> dancerComparator() {
         return new Comparator<Dancer>() {
             public int compare(Dancer d1, Dancer d2) {
                 Position p1 = Formation.this.location(d1);
                 Position p2 = Formation.this.location(d2);
                 return p1.compareTo(p2);
             }
         };
     }
     /** Return the dancers of {@link #dancers()}, in the order given by
      * {@link #dancerComparator()}. */
     public List<Dancer> sortedDancers() {
         List<Dancer> result = new ArrayList<Dancer>(this.dancers());
         Collections.sort(result, this.dancerComparator());
         return Collections.unmodifiableList(result);
     }
     // utility functions.
     @Override
     public boolean equals(Object o) {
 	if (!(o instanceof Formation)) return false;
 	Formation f = (Formation) o;
 	return new EqualsBuilder()
 	    .append(location, f.location)
 	    .append(selected, f.selected)
 	    .isEquals();
     }
     @Override
     public int hashCode() {
 	return new HashCodeBuilder()
 	    .append(location).append(selected)
 	    .toHashCode();
     }
     @Override
     public String toString() {
 	// order dancers by location so that string representation is
 	// consistent
 	SortedMap<Dancer,Position> location =
 	    new TreeMap<Dancer,Position>(this.dancerComparator());
 	location.putAll(this.location);
 	List<Dancer> selected = new ArrayList<Dancer>(this.selected);
 	Collections.sort(selected, this.dancerComparator());
 	// build the result string
 	return new ToStringBuilder(this, SdrToString.STYLE)
 	    .append("location", location)
 	    .append("selected", selected)
 	    .toString();
     }
     /**
      * Return an ascii-art diagram of this formation.
      * Each 2x2 dancer becomes a 5x2 char array, with facing direction
      * indicator centered in the 5 unit width.
      * ie, a box (bounds -2,-2 to 2,2) is:
      * <pre>
      * "1B^  1Gv  \n"
      * "          \n"
      * "3B^  3Gv  "
      * </pre>
      * Note that the return value has no trailing \n.
      *  js> Formation.SQUARED_SET.toStringDiagram()
      *       3Gv  3Bv
      *  
      *  4B>            2G<
      *  
      *  4G>            2B<
      *  
      *       1B^  1G^
      */
     public String toStringDiagram() {
 	/* invoke the full toStringDiagram() with default dancer names*/
 	return toStringDiagram("");
     }
     /**
      * Return an ascii-art diagram of this formation with the specified
      * prefix on each line.
      * Note that the return value has no trailing \n.
      * @see #toStringDiagram()
      *  js> Formation.SQUARED_SET.toStringDiagram("!")
      *  !     3Gv  3Bv
      *  !
      *  !4B>            2G<
      *  !
      *  !4G>            2B<
      *  !
      *  !     1B^  1G^
      */
     public String toStringDiagram(String prefix) {
 	/* invoke the full toStringDiagram() with default dancer names*/
 	return toStringDiagram(prefix, dancerNames);
     }
     /**
      * Return an ascii-art diagram of this formation, using a custom
      * mapping from {@link Dancer}s to 2-character strings.  Each line
      * of the diagram is preceded with the specified prefix.
      * Note that the return value has no trailing \n.
      * @see #toStringDiagram()
      * @doc.test Use AA through HH to represent dancers
      *  js> m = java.util.LinkedHashMap()
      *  {}
      *  js> for (i in Iterator(StandardDancer.values())) {
      *    >   m.put(i[1], String.fromCharCode(65+i[0],65+i[0]))
      *    > }
      *  null
      *  js> Formation.SQUARED_SET.toStringDiagram("|", m)
      *  |     FFv  EEv
      *  |
      *  |GG>            DD<
      *  |
      *  |HH>            CC<
      *  |
      *  |     AA^  BB^
      */
     // xxx add "include detail" boolean arg that includes tag and
     //     'selected' information?
     public String toStringDiagram(String prefix,
                                   Map<Dancer,String> dancerNames) {
         GridString gs = new GridString(prefix);
         for (Dancer d : sortedDancers()) {
             Position p = location(d);
             int x = Math.round(p.x.multiply(Fraction.valueOf(5,2)).floatValue());
             int y = Math.round(p.y.floatValue());
 	    char facing = p.facing.toDiagramChar();
             gs.set(x,y,facing);
 	    String name = dancerNames.get(d);
 	    if (name != null) {
 		assert name.length()==2;
 		gs.set(x-2,y,name.charAt(0));
 		gs.set(x-1,y,name.charAt(1));
             }
         }
         return gs.toString();
     }
     private static final Map<Dancer,String> _dancerNames =
 	new LinkedHashMap<Dancer,String>(8);
     /**
      * Map from {@link StandardDancer}s to 2-character dancer representations.
      * @doc.test
      *  js> Formation.dancerNames.get(StandardDancer.COUPLE_1_BOY)
      *  1B
      *  js> Formation.dancerNames.get(StandardDancer.COUPLE_4_GIRL)
      *  4G
      */
     public static final Map<Dancer,String> dancerNames =
 	Collections.unmodifiableMap(_dancerNames);
     static {
 	/* initialize the static _dancer_names map */
 	for (StandardDancer sd: StandardDancer.values()) {
 	    char a = (char) (sd.coupleNumber()+'0');
 	    char b = sd.isBoy()?'B':'G';
 	    _dancerNames.put(sd, new String(new char[] { a, b }).intern());
 	}
     }
     
     /**
      * A {@link GridString} lets you write ascii-art graphics to an
      * array of {@link StringBuilder}s, and then turn the results
      * into a new-line separated string at the end.
      */
     private static class GridString {
         private final String prefix;
         private int minx=0, miny=0;
         private final List<StringBuilder> grid = new ArrayList<StringBuilder>();
         GridString(String prefix) { this.prefix=prefix; }
 	/** Set the character at position (x,y) to c.  */
         void set(int x, int y, char c) {
             // insert left padding if we need it.
             while (x<minx) {
                 for (StringBuilder sb : grid)
                     if (sb.length()>0)
                         sb.insert(0,' ');
                 minx--;
             }
             // insert new rows at top if we need it.
             while (y<miny) {
                 grid.add(0,new StringBuilder());
                 miny--;
             }
             x -= minx; y -= miny;
             // make new rows at bottom if we need to.
             while (grid.size() <= y)
                 grid.add(new StringBuilder());
             // find the correct row.
             StringBuilder sb = grid.get(y);
             // make it longer if we must.
             while (sb.length() <= x)
                 sb.append(' ');
             sb.setCharAt(x,c);
             // done!
         }
 	/**
          * Return a newline-separated string.
          * The return value has no trailing \n.
          */
         @Override
         public String toString() {
             StringBuilder sb = new StringBuilder();
             // need to reverse y direction.
             for (int i=grid.size()-1; i>=0; i--) {
                 sb.append(prefix);
                 sb.append(grid.get(i));
                 if (i>0) sb.append('\n');
             }
             return sb.toString();
         }
     }
     
     /**
      * Starting formation for 8-couple dancing.
      * @doc.test
      *  Check the diagram for this formation:
      *  js> Formation.SQUARED_SET.toStringDiagram()
      *       3Gv  3Bv
      *  
      *  4B>            2G<
      *  
      *  4G>            2B<
      *  
      *       1B^  1G^
      */
     public static final Formation SQUARED_SET = new Formation
 	(new DancerInfo(COUPLE_1_BOY,
 			Position.getGrid(-1,-3,ExactRotation.ZERO)),
 	 new DancerInfo(COUPLE_1_GIRL,
 			Position.getGrid(+1,-3,ExactRotation.ZERO)),
 	 new DancerInfo(COUPLE_2_BOY,
 			Position.getGrid(+3,-1,ExactRotation.THREE_QUARTERS)),
 	 new DancerInfo(COUPLE_2_GIRL,
 			Position.getGrid(+3,+1,ExactRotation.THREE_QUARTERS)),
 	 new DancerInfo(COUPLE_3_BOY,
 			Position.getGrid(+1,+3,ExactRotation.ONE_HALF)),
 	 new DancerInfo(COUPLE_3_GIRL,
 			Position.getGrid(-1,+3,ExactRotation.ONE_HALF)),
 	 new DancerInfo(COUPLE_4_BOY,
 			Position.getGrid(-3,+1,ExactRotation.ONE_QUARTER)),
 	 new DancerInfo(COUPLE_4_GIRL,
 			Position.getGrid(-3,-1,ExactRotation.ONE_QUARTER))
 	 );
     /**
      * Starting formation for 2-couple dancing.
      * @doc.test
      *  Check the diagram for this formation:
      *  js> Formation.FOUR_SQUARE.toStringDiagram()
      *  3Gv  3Bv
      *  
      *  1B^  1G^
      */
     public static final Formation FOUR_SQUARE = new Formation
         (new DancerInfo(COUPLE_1_BOY,
                 Position.getGrid(-1,-1,ExactRotation.ZERO)),
          new DancerInfo(COUPLE_1_GIRL,
                 Position.getGrid(+1,-1,ExactRotation.ZERO)),
          new DancerInfo(COUPLE_3_BOY,
                 Position.getGrid(+1,+1,ExactRotation.ONE_HALF)),
          new DancerInfo(COUPLE_3_GIRL,
                 Position.getGrid(-1,+1,ExactRotation.ONE_HALF))
         );
 
     protected Formation(Formation f, Map<Dancer,Dancer> map) {
         Map<Dancer,Position> m = new LinkedHashMap<Dancer,Position>();
         Set<Dancer> s = new LinkedHashSet<Dancer>();
         for (Map.Entry<Dancer,Position> me : f.location.entrySet())
             m.put(map.get(me.getKey()), me.getValue());
         for (Dancer d : f.selected)
             s.add(map.get(d));
         this.location = Collections.unmodifiableMap(m);
         this.selected = Collections.unmodifiableSet(s);
     }
     public Formation map(Map<Dancer,Dancer> map) {
         return new Formation(this, map);
     }
     /** Replace the dancers in this formation with the given dancers,
      * specified in the "sorted dancers" order (left-to-right,
      * top-to-bottom).
      */
     public Formation map(Dancer... dancers) {
         Map<Dancer,Dancer> m = new LinkedHashMap<Dancer,Dancer>();
         List<Dancer> sortedDancers = this.sortedDancers();
         for (int i=0; i<dancers.length; i++)
             m.put(sortedDancers.get(i), dancers[i]);
         return this.map(m);
     }
     /** Special case of {@link Formation#map(Dancer...)} which allows you to specify
      *  half the dancers, and fills in the rest with their opposites to
      *  result in a symmetric formation.
      */
     public Formation mapStd(StandardDancer... dancers) {
         if (dancers.length==4 && this.dancers().size()==8) {
             StandardDancer[] ndancers = new StandardDancer[8];
             for (int i=0; i<dancers.length; i++) {
                 ndancers[i] = dancers[i];
                 ndancers[8 - i - 1] =
                     StandardDancer.values()[(dancers[i].ordinal()+4)%8];
             }
             dancers = ndancers;
         }
         return this.map((Dancer[])dancers);
     }
     
     Formation(DancerInfo... dis) {
 	Map<Dancer,Position> m = new LinkedHashMap<Dancer,Position>();
 	Set<Dancer> s = new LinkedHashSet<Dancer>();
 	for (DancerInfo di : dis) {
 	    m.put(di.dancer, di.position);
             if (di.isSelected)
 		s.add(di.dancer);
 	}
 	this.location = Collections.unmodifiableMap(m);
 	this.selected = Collections.unmodifiableSet(s);
     }
     static class DancerInfo {
 	final Dancer dancer;
 	final Position position;
         final boolean isSelected;
 	DancerInfo(Dancer d, Position p, boolean s) {
 	    this.dancer = d; this.position = p;
 	    this.isSelected = s;
 	}
 	DancerInfo(Dancer d, Position p) {
             this(d,p,true);
         }
     }
     /** Return a formation like this one, except that the given dancer is
      *  moved to the specified position.
      *  js> f = Formation.SQUARED_SET; f.toStringDiagram("|")
      *  |     3Gv  3Bv
      *  |
      *  |4B>            2G<
      *  |
      *  |4G>            2B<
      *  |
      *  |     1B^  1G^
      *  js> f.move(StandardDancer.COUPLE_1_BOY, Position.getGrid(-1,0,"e")).toStringDiagram("|")
      *  |     3Gv  3Bv
      *  |
      *  |4B>            2G<
      *  |     1B>
      *  |4G>            2B<
      *  |
      *  |          1G^
      */
     public Formation move(Dancer d, Position p) {
 	assert this.location.containsKey(d);
 	Map<Dancer,Position> nmap = new LinkedHashMap<Dancer,Position>
 	    (this.location);
 	nmap.put(d, p);
 	return new Formation(nmap, this.selected);
     }
     /** Return a formation like this one, except with all positions mirrored.
      * @doc.test
      *  js> f = Formation.SQUARED_SET; f.toStringDiagram("|");
      *  |     3Gv  3Bv
      *  |
      *  |4B>            2G<
      *  |
      *  |4G>            2B<
      *  |
      *  |     1B^  1G^
      *  js> f.mirror(false).toStringDiagram("|")
      *  |     3Bv  3Gv
      *  |
      *  |2G>            4B<
      *  |
      *  |2B>            4G<
      *  |
      *  |     1G^  1B^
      *  js> f.mirror(false).location(StandardDancer.COUPLE_1_BOY)
      *  1,-3,n
      *  js> f.mirror(true).toStringDiagram("|")
      *  |     3Bv  3Gv
      *  |
      *  |2G>            4B<
      *  |
      *  |2B>            4G<
      *  |
      *  |     1G^  1B^
      *  js> f.mirror(true).location(StandardDancer.COUPLE_1_BOY)
      *  1,-3,n,[PASS_LEFT]
      */
     public Formation mirror(boolean mirrorShoulderPass) {
         Map<Dancer,Position> nmap = new LinkedHashMap<Dancer,Position>();
         for (Map.Entry<Dancer, Position> me: this.location.entrySet()) {
             nmap.put(me.getKey(), me.getValue().mirror(mirrorShoulderPass));
         }
         return new Formation(nmap, this.selected);
     }
 }
