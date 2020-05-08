 package net.cscott.sdr.calls;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NavigableMap;
 import java.util.Set;
 import java.util.Stack;
 import java.util.TreeMap;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import net.cscott.jutil.Factories;
 import net.cscott.jutil.GenericMultiMap;
 import net.cscott.sdr.calls.TaggedFormation.Tag;
 import net.cscott.sdr.calls.ast.Prim;
 import net.cscott.sdr.util.Fraction;
 import net.cscott.sdr.util.SdrToString;
 import net.cscott.sdr.util.Tools.ListMultiMap;
 import static net.cscott.sdr.util.Tools.mml;
 
 /**
  * {@link DanceState} captures all the <i>dynamic</i> information about a
  * dance: the current formation and the queues of dancer actions and calls. It
  * includes a link to a {@link DanceProgram} which is the static information
  * about the dance.
  *
  * @author C. Scott Ananian
  */
 public class DanceState {
     public final DanceProgram dance;
     private final NavigableMap<Fraction, Formation> formations;
     private final ListMultiMap<Dancer, TimedAction> actions; // XXX?
     private final Map<Dancer, NavigableMap<Fraction, DancerPath>> movements;
     // this is used to keep track of a 'designated dancer' stack
     private final Stack<Set<Dancer>> designatedStack;
     /** This is an interface into the environment of the dance engine.
      *  If a {@link java.util.concurrent.ConcurrentMap} is used, then the
      *  environment can be updated asynchronously. */
     private final Map<String,String> properties;
 
     public DanceState(DanceProgram dance, Formation f,
                       Map<String,String> properties) {
         this.dance = dance;
         this.formations = new TreeMap<Fraction,Formation>();
         this.formations.put(Fraction.ZERO, f);
         this.actions = mml();
         // initialize movements
         this.movements = new LinkedHashMap<Dancer,NavigableMap<Fraction,DancerPath>>
             (f.dancers().size());
         for (Dancer d: f.dancers())
             this.movements.put(d, new TreeMap<Fraction,DancerPath>());
         // xxx: initialize actions?
         this.designatedStack = new Stack<Set<Dancer>>();
         this.properties = properties;
     }
     // convenience constructor.
     public DanceState(DanceProgram dance, Formation f) {
         this(dance, f, Collections.<String,String>emptyMap());
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this, SdrToString.STYLE)
         .append("dance", dance)
         .append("formations", formations)
         .append("actions", actions)
         .append("movements", movements)
         .append("designated", designated())
         .toString();
     }
 
     /** Return the last in the list of timed formations. */
     public Formation currentFormation() {
         return formations.lastEntry().getValue();
     }
     /** Return the time of the last formation. */
     public Fraction currentTime() {
         return formations.lastKey();
     }
 
     /**
      * Return a new independent dance state with the same static state and
      * current formation, but empty movements and actions. The timing of the
      * current formation is reset to zero. This is used for independently
      * evaluation subcalls, when we then want to apply a transformation to the
      * timing, positions, movements, formations, etc, before adding them to the
      * parent state.
      *
      * @return a new clean DanceState
      */
     public DanceState cloneAndClear() {
         return cloneAndClear(currentFormation());
     }
 
     /**
      * Similar to {@link #cloneAndClear()}, but allows you to specify the new
      * 'current formation' of the result. This is useful for concepts which
      * alter the shape of the current formation (say, creating a mirror image)
      * before applying the subcall.
      *
      * @param formation
      *            what {@code result.currentFormation()} should return.
      * @return a new clean DanceState.
      */
     public DanceState cloneAndClear(Formation formation) {
         // XXX: should revisit this?
         DanceState nds = new DanceState(dance, formation, properties);
         for (Set<Dancer> designated : this.designatedStack)
             nds.pushDesignated(designated);
         return nds;
     }
 
     /** Add the set of designated dancers to the stack stored in the
      *  {@link DanceState}.
      * @param designated
      */
     public void pushDesignated(Set<Dancer> designated) {
         this.designatedStack.push(Collections.unmodifiableSet(designated));
     }
     /** Pop the top off the designated dancer stack. */
     public void popDesignated() {
         this.designatedStack.pop();
     }
     /** Look at the top of the designated dancer stack. */
     public Set<Dancer> designated() {
         if (this.designatedStack.isEmpty())
             return Collections.<Dancer>emptySet();
         return this.designatedStack.peek();
     }
     /** Add the 'DESIGNATED' tag to the given Formation. */
     public TaggedFormation tagDesignated(Formation f) {
         TaggedFormation tf = TaggedFormation.coerce(f);
         GenericMultiMap<Dancer,Tag> newTags =
             new GenericMultiMap<Dancer,Tag>
                 (Factories.enumSetFactory(Tag.class));
         for (Dancer d : this.designated())
             if (f.dancers().contains(d))
                 newTags.add(d, Tag.DESIGNATED);
         return tf.addTags(newTags);
     }
     /** Add the 'DESIGNATED' tag to the given FormationMatch. */
     public FormationMatch tagDesignated(FormationMatch fm) {
         if (this.designatedStack.isEmpty() ||
             this.designatedStack.peek().isEmpty())
             return fm; // fast out for common case
         Map<Dancer,TaggedFormation> nMatches =
             new LinkedHashMap<Dancer,TaggedFormation>();
         for (Dancer d : fm.matches.keySet())
             nMatches.put(d, this.tagDesignated(fm.matches.get(d)));
         return new FormationMatch(fm.meta, nMatches, fm.unmatched);
     }
     /** Access the environment's property map.
      *  @see ExprList#_PROPERTY
      */
     public String property(String name, String defaultValue) {
         String v = properties.get(name);
         return (v==null) ? defaultValue : v;
     }
 
     /**
      * Move the given dancer along the specified dancer path. This will alter
      * the {@link #currentFormation()}, and may create additional intermediate
      * formations, if the {@link DancerPath#time} falls in between existing
      * formations.
      */
     public void add(Dancer d, DancerPath dp) {
         // add to list of dancer paths
 	NavigableMap<Fraction,DancerPath> dmove = this.movements.get(d);
 	Fraction last = dmove.isEmpty() ? Fraction.ZERO : dmove.lastKey();
         Fraction next = last.add(dp.time);
         dmove.put(next, dp);
         // get formation with time == next, or "just before"
         if (!this.formations.containsKey(next)) {
             // clone the formation "just before"
             this.formations.put(next,
                                 this.formations.floorEntry(next).getValue());
         }
         // okay, now iterate from 'next' forward, adjusting this dancer's
         // location
         for (Map.Entry<Fraction, Formation> me:
              this.formations.tailMap(next).entrySet()) {
             me.setValue(me.getValue().move(d, dp.to));
         }
         // done!
     }
 
     /**
      * Add "do nothing" actions as necessary so that every dancer's next
      * action will occur at the same time.
      */
     public void syncDancers() {
         this.syncDancers(this.currentTime());
     }
     /**
      * Add "do nothing" actions as necessary so that every dancer's next
      * action will occur at the given time.
      */
     public void syncDancers(Fraction time) {
         for (Map.Entry<Dancer,NavigableMap<Fraction,DancerPath>> me :
                 this.movements.entrySet()) {
             Dancer d = me.getKey();
             NavigableMap<Fraction,DancerPath> dmove = me.getValue();
             Fraction lastTime = dmove.isEmpty()? Fraction.ZERO: dmove.lastKey();
             assert lastTime.compareTo(time) <= 0;
             if (lastTime.equals(time))
                 continue;
             Prim nothingPrim =
                 Prim.STAND_STILL.scaleTime(time.subtract(lastTime));
             DancerPath nothingPath =
                 EvalPrim.apply(d, formations.get(lastTime), nothingPrim);
             dmove.put(time, nothingPath);
         }
     }
     /**
      * Remove any trailing "do nothing" actions so that the next action
      * flows directly from the previous one.  This is used in "roll" --
      * I don't know if it's useful for any other calls.
      */
     public void unsyncDancers() {
        eachDancer:
         for (NavigableMap<Fraction,DancerPath> dmove : this.movements.values()){
             Iterator<Fraction> it= dmove.navigableKeySet().descendingIterator();
             while (it.hasNext()) {
                 if (dmove.get(it.next()).isStandStill())
                     it.remove();
                else
                    continue eachDancer;
             }
         }
     }
 
     /**
      * Return all the {@link DancerPath}s, in order, performed by the given
      * {@link Dancer}.
      */
     public List<DancerPath> movements(Dancer d) {
         return Collections.unmodifiableList
             (new ArrayList<DancerPath>(this.movements.get(d).values()));
     }
 
     /** Return all the {@link Dancer}s in this {@link DanceState}. */
     public Set<Dancer> dancers() {
         return this.movements.keySet();
     }
 
     /**
      * Return a sorted list of {@link TimedFormation}s describing the net
      * effect of the {@link DancerPath}s in this {@link DanceState}. The first
      * element in the list will match the starting formation provided when this
      * {@link DanceState} was constructed, will have an absolute time of
      * {@link Fraction#ZERO}, and will have all dancers selected. The final
      * element in the list will represent the final state. Formations should not
      * be considered animation targets: not all dancers will have moved in all
      * formations. For example, if dancer #1 has a movement which takes 2 beats,
      * and dancer #2 has two movements which take 1 each, the result list will
      * include the starting formation, with time 0, an intermediate formation
      * with time 1 where dancer #1 is in their starting position and dancer #2
      * has taken their first movement, and a final position with time 2 where
      * both dancer #1 and dancer #2 have moved.  Consult the list of
      * {@link DancerPath}s for accurate animation information.
      */
     public List<TimedFormation> formations() {
         List<TimedFormation> result =
             new ArrayList<TimedFormation>(this.formations.size());
         for (Map.Entry<Fraction,Formation> me: this.formations.entrySet()) {
             result.add(new TimedFormation(me.getValue(), me.getKey(), true));
         }
         return Collections.unmodifiableList(result);
     }
     /** Return the latest formation at or preceding the given time. */
     public Formation formationAt(Fraction time) {
         return this.formations.floorEntry(time).getValue();
     }
 }
