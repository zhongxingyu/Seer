 package net.cscott.sdr.calls;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.Set;
 
 import net.cscott.sdr.util.Fraction;
 import net.cscott.sdr.util.Point;
 import net.cscott.sdr.util.Bezier.Bezier2D;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 /** A {@link DancerPath} is the result of evaluating a call for a specific
  * dancer in a formation.  Unlike a {@link net.cscott.sdr.calls.ast.Prim},
  * a {@link DancerPath} is an absolute motion, and includes computed
  * information about flow, roll, sweep, and timing.
  * <p>
  * The flow information we collect is based on Lynette Bellini's
  * <a href="http://www.lynette.org/flow.html">Rules of Flow</a>,
  * along with Dave Wilson's theories about dancer kinematics; to wit:
  * certain "bad flow" motions actually work if there is hand contact
  * between certain dancers, as it lets them push off of one another.
  *
  * @author C. Scott Ananian
  * @version $Id: DancerPath.java,v 1.3 2007-03-07 22:05:29 cananian Exp $
  * @doc.test Simple pass thru movement:
  *  js> importPackage(net.cscott.sdr.util);
  *  js> dp = new DancerPath(Position.getGrid(-1,-3,"n"),
  *    >                     Position.getGrid(-1,-1,"n"),
  *    >                     Fraction.valueOf(2), null);
  *  DancerPath[from=-1,-3,n,to=-1,-1,n,time=2,pointOfRotation=<null>]
  *  js> dp.tangentStart()
  *  0,1
  *  js> dp.tangentFinish()
  *  0,1
  *  js> dp.bezierPath()
 *  [-1,-3, -1,-2 5/6, -1,-1 1/6, -1,-1]
  *  js> dp.bezierDirection()
 *  [0,1/2, 0,5, 0,1/2]
  *  js> dp.isStandStill()
  *  false
  *  js> dp.scaleTime(Fraction.valueOf(2));
  *  DancerPath[from=-1,-3,n,to=-1,-1,n,time=4,pointOfRotation=<null>]
  *  js> dp.scaleTime(Fraction.valueOf(2)).bezierPath()
 *  [-1,-3, -1,-2 11/12, -1,-1 1/12, -1,-1]
  *  js> dp.mirror(false)
  *  DancerPath[from=1,-3,n,to=1,-1,n,time=2,pointOfRotation=<null>]
  *  js> dp.mirror(true)
  *  DancerPath[from=1,-3,n,[PASS_LEFT],to=1,-1,n,[PASS_LEFT],time=2,pointOfRotation=<null>]
  * @doc.test Half-sashay, twice (boy's part):
  *  js> importPackage(net.cscott.sdr.util);
  *  js> dp1 = new DancerPath(Position.getGrid(-1,-3,"n"),
  *    >                      Position.getGrid( 0,-4,"n"),
  *    >                      Fraction.valueOf(2), null,
  *    >                      DancerPath.Flag.SASHAY_FINISH);
  *  DancerPath[from=-1,-3,n,to=0,-4,n,time=2,pointOfRotation=<null>,flags=[SASHAY_FINISH]]
  *  js> dp1.tangentStart()
  *  0,-1
  *  js> dp1.tangentFinish()
  *  1,0
  *  js> dp1.bezierPath()
 *  [-1,-3, -1,-3 1/6, -1/6,-4, 0,-4]
  *  js> dp1.bezierDirection()
  *  [0,1, 0,1]
  *  js> dp1.isStandStill()
  *  false
  *  js> dp2 = new DancerPath(Position.getGrid(0, -4, "n"),
  *    >                      Position.getGrid(1, -3, "n"),
  *    >                      Fraction.valueOf(2), null,
  *    >                      DancerPath.Flag.SASHAY_START);
  *  DancerPath[from=0,-4,n,to=1,-3,n,time=2,pointOfRotation=<null>,flags=[SASHAY_START]]
  *  js> dp2.tangentStart()
  *  1,0
  *  js> dp2.tangentFinish()
  *  0,1
  *  js> dp2.bezierPath()
 *  [0,-4, 1/6,-4, 1,-3 1/6, 1,-3]
  *  js> dp2.bezierDirection()
  *  [0,1, 0,1]
  *  js> dp3 = new DancerPath(Position.getGrid(1, -3, "n"),
  *    >                      Position.getGrid(0, -2, "n"),
  *    >                      Fraction.valueOf(2), null,
  *    >                      DancerPath.Flag.SASHAY_FINISH);
  *  DancerPath[from=1,-3,n,to=0,-2,n,time=2,pointOfRotation=<null>,flags=[SASHAY_FINISH]]
  *  js> dp3.tangentStart()
  *  0,1
  *  js> dp3.tangentFinish()
  *  -1,0
  *  js> dp3.bezierPath()
 *  [1,-3, 1,-2 5/6, 1/6,-2, 0,-2]
  *  js> dp3.bezierDirection()
  *  [0,1, 0,1]
  *  js> dp4 = new DancerPath(Position.getGrid(0, -2, "n"),
  *    >                      Position.getGrid(-1, -3, "n"),
  *    >                      Fraction.valueOf(2), null,
  *    >                      DancerPath.Flag.SASHAY_START);
  *  DancerPath[from=0,-2,n,to=-1,-3,n,time=2,pointOfRotation=<null>,flags=[SASHAY_START]]
  *  js> dp4.tangentStart()
  *  -1,0
  *  js> dp4.tangentFinish()
  *  0,-1
  *  js> dp4.bezierPath()
 *  [0,-2, -1/6,-2, -1,-2 5/6, -1,-3]
  *  js> dp4.bezierDirection()
  *  [0,1, 0,1]
  * @doc.test An path with an unusual combination of sashay; facing direction
  *  is a linear transition between starting and ending direction.
  *  js> importPackage(net.cscott.sdr.util);
  *  js> dp = new DancerPath(Position.getGrid(0,0,"n"),
  *    >                     Position.getGrid(2,2,"e"),
  *    >                     Fraction.valueOf(2), null,
  *    >                     DancerPath.Flag.SASHAY_FINISH);
  *  DancerPath[from=0,0,n,to=2,2,e,time=2,pointOfRotation=<null>,flags=[SASHAY_FINISH]]
  *  js> dp.tangentStart()
  *  0,1
  *  js> dp.tangentFinish()
  *  0,1
  *  js> dp.bezierPath()
 *  [0,0, 0,1/6, 2,1 5/6, 2,2]
  *  js> dp.bezierDirection()
  *  [0,1, 1,1, 1,0]
  * @doc.test Make sure "stand still" paths work:
  *  js> importPackage(net.cscott.sdr.util);
  *  js> dp = new DancerPath(Position.getGrid(0,0,"n"),
  *    >                     Position.getGrid(0,0,"n"),
  *    >                     Fraction.valueOf(2), null);
  *  DancerPath[from=0,0,n,to=0,0,n,time=2,pointOfRotation=<null>]
  *  js> dp.tangentStart()
  *  0,0
  *  js> dp.tangentFinish()
  *  0,0
  *  js> dp.bezierPath()
  *  [0,0, 0,0, 0,0, 0,0]
  *  js> dp.bezierDirection()
  *  [0,1, 0,1]
  *  js> dp.bezierDirection().raise()
  *  [0,1, 0,1, 0,1]
  * @doc.test "Turn in place" paths turn in the 'minimum sweep' direction:
  *  js> importPackage(net.cscott.sdr.util);
  *  js> dp = new DancerPath(Position.getGrid(0,0,"ne"),
  *    >                     Position.getGrid(0,0,"nw"),
  *    >                     Fraction.valueOf(2), null);
  *  DancerPath[from=0,0,ne,to=0,0,nw,time=2,pointOfRotation=<null>]
  *  js> dp.tangentStart()
  *  0,0
  *  js> dp.tangentFinish()
  *  0,0
  *  js> dp.bezierPath()
  *  [0,0, 0,0, 0,0, 0,0]
  *  js> dp.bezierDirection()
  *  [1,1, 0,1, -1,1]
  *  js> dp = new DancerPath(Position.getGrid(0,0,"nw"),
  *    >                     Position.getGrid(0,0,"ne"),
  *    >                     Fraction.valueOf(2), null);
  *  DancerPath[from=0,0,nw,to=0,0,ne,time=2,pointOfRotation=<null>]
  *  js> dp.bezierDirection()
  *  [-1,1, 0,1, 1,1]
  */
 public class DancerPath {
     /**
      * In order to have a method of discussing the various motions encountered
      * in square dance choreography, Lynette Bellini identified several points
      * of rotation:<dl>
      * <dt>A single dancer:</dt>
      * <dd>the point of rotation is about the center of a single dancer, as in
      * the call roll.</dd>
      * <dt>Two dancers:</dt>
      * <dd>the point of rotation is about a point between two dancers.</dd>
      * <dt>Four dancers:</dt>
      * <dd>the point of rotation is about a point in the center of four
      * dancers.</dd>
      * <dt>Center of the square:</dt>
      * <dd>the point of rotation is about the center point of the square.</dd>
      * </dl>
      * There are multiple ways to pair dancers; swing and slip are both
      * examples of the two dancer point of rotation, but they are different.
      * The same thing applies for the four dancer situations: from parallel
      * ocean waves,
      * lockit and split counter rotate operate around different points, but
      * they are each four dancers large.
      * <p>(From Lynette Bellini's
      * <a href="http://www.lynette.org/flow.html">Rules of Flow</a>.)
      */
     // XXX use integer for rotation set size, and arcCenter for the point
     //     of rotation?
     public static enum PointOfRotation {
         /** The point of rotation is about the center of a single dancer, as in
          * the call roll. */
         SINGLE_DANCER,
         /** The point of rotation is about a point between two dancers. */
         TWO_DANCERS,
         /** The point of rotation is about a point in the center of four
          *  dancers. */
         FOUR_DANCERS,
         /** The point of rotation is about the center point of the square. */
         SQUARE_CENTER
     }
     public static enum Flag {
         /** Start movement with a sashay instead of stepping forward. */
         SASHAY_START,
         /** Finish movement with a sashay instead of stepping forward. */
         SASHAY_FINISH,
     }
 
     /** The rotations on the to and from positions should be exact. */
     public final Position from, to;
     /** The time this motion should take. */
     public final Fraction time;
     /** The point of rotation, for flow computations.  May be null for
      * straight paths. */
     public final PointOfRotation pointOfRotation; // 1,2,4,8 person, for flow
     /** Additional properties of the path. */
     public final Set<Flag> flags;
 
     /**
      * Create an immutable {@link DancerPath} object.
      */
     public DancerPath(Position from, Position to, Fraction time,
                       PointOfRotation pointOfRotation, Flag...flags) {
         this(from, to, time, pointOfRotation, Arrays.asList(flags));
     }
     public DancerPath(Position from, Position to, Fraction time,
                        PointOfRotation pointOfRotation, Collection<Flag> flags){
         assert from != null && to != null && time != null;
         assert from.facing.isExact() && to.facing.isExact();
         assert time.compareTo(Fraction.ZERO) >= 0;
         this.from = from;
         this.to = to;
         this.time = time;
         this.pointOfRotation = pointOfRotation;
         // somewhat awkward initialization of read-only this.flags
         Set<Flag> es = EnumSet.noneOf(Flag.class);
         es.addAll(flags);
         this.flags = Collections.unmodifiableSet(es);
     }
 
     /** Return the approximate "center" of this arcing {@link DancerPath}, or
      * null if this is a straight-line movement.
      */
     public Point arcCenter() {
         // xxx: compute intersection of vectors orthogonal to tangents at
         //      start and finish of movement.  If vectors do not intersect,
         //      (or intersect at every point) this is not an arcing movement;
         //      return null.  Set to dancer's location if only rotation in
         //      place is involved in this movement
         assert false : "unimplemented";
         return null;
     }
 
     /** Return a unit vector describing the initial tangent of the dancer's
      *  path, as a vector from (0,0) to the returned {@link Point}.
      */
     public Point tangentStart() {
         return tangent((ExactRotation)from.facing,
                 new Point(to.x.subtract(from.x), to.y.subtract(from.y)),
                 flags.contains(Flag.SASHAY_START));
     }
     /** Return a unit vector describing the final tangent of the dancer's
      *  path, as a vector from (0,0) to the returned {@link Point}.
      */
     public Point tangentFinish() {
         return tangent((ExactRotation)to.facing,
                 new Point(to.x.subtract(from.x), to.y.subtract(from.y)),
                 flags.contains(Flag.SASHAY_FINISH));
     }
     /** Compute linear vector from start to finish, then quantize to dancer's
      * facing direction or reverse facing direction, whichever is closer.
      * If a sashay flag is set, rotate the dancer's facing direction 90-degrees
      * before doing the computation. */
     private static Point tangent(ExactRotation facing, Point motionDir,
                                  boolean isSashay) {
         // if standing still, return 0,0
         if (motionDir.equals(Point.ZERO)) return Point.ZERO;
         // motionDir is the linear vector from start to finish
         ExactRotation option1 = facing.normalize();
         if (isSashay) option1 = option1.add(Fraction.ONE_QUARTER).normalize();
         ExactRotation option2 = option1.add(Fraction.ONE_HALF).normalize();
         ExactRotation motion = ExactRotation.fromXY(motionDir.x, motionDir.y);
         ExactRotation result;
         // pick the option which is "closer" to the motionDir
         if (minSweep(option1, motion).compareTo(minSweep(option2, motion)) < 0)
             result = option1;
         else
             result = option2;
         // convert the result into a "unit" vector.
         return new Point(result.toX(), result.toY());
     }
     /** Compute the minimum angle between heading a and heading b.
      * @doc.test
      *  js> DancerPath.minSweep(ExactRotation.ONE_EIGHTH, ExactRotation.SEVEN_EIGHTHS);
      *  1/4
      *  js> DancerPath.minSweep(ExactRotation.SEVEN_EIGHTHS, ExactRotation.ONE_EIGHTH);
      *  1/4
      */
     // only public so we can target it with doc tests
     public static Fraction minSweep(ExactRotation a, ExactRotation b) {
         Fraction aa = a.normalize().amount, bb = b.normalize().amount;
         Fraction s1 = (aa.compareTo(bb)<0) ? bb.subtract(aa) : aa.subtract(bb);
         Fraction s2 = Fraction.ONE.subtract(s1);
         return s1.compareTo(s2) < 0 ? s1 : s2;
     }
     /** Compute midpoint between given exact rotations, in minimum sweep
      *  direction.  Breaks ties in CCW direction.
      * @doc.test
      *  js> DancerPath.midPoint(ExactRotation.ONE_EIGHTH, ExactRotation.SEVEN_EIGHTHS).normalize();
      *  0
      *  js> DancerPath.midPoint(ExactRotation.SEVEN_EIGHTHS, ExactRotation.ONE_EIGHTH).normalize();
      *  0
      *  js> DancerPath.midPoint(ExactRotation.ONE_EIGHTH, ExactRotation.FIVE_EIGHTHS).normalize();
      *  7/8
      *  js> DancerPath.midPoint(ExactRotation.FIVE_EIGHTHS, ExactRotation.ONE_EIGHTH).normalize();
      *  7/8
      */
     // only public so we can target it with doc tests
     public static ExactRotation midPoint(ExactRotation a, ExactRotation b) {
         Fraction aa = a.normalize().amount, bb = b.normalize().amount;
         boolean aaSmaller = aa.compareTo(bb) < 0;
         Fraction s1 = (aaSmaller) ? bb.subtract(aa) : aa.subtract(bb);
         Fraction s2 = Fraction.ONE.subtract(s1);
         boolean s1Smaller = s1.compareTo(s2) < 0;
         boolean isCW = !(aaSmaller ^ s1Smaller);
         Fraction minSweep = (s1Smaller ? s1 : s2).divide(Fraction.TWO);
         return isCW ? a.add(minSweep) : a.subtract(minSweep);
     }
     /** Return a 2D bezier describing the dancer's complete path.  The 't'
      *  parameter of the bezier should vary from 0 to 1 over {@link #time}
      *  beats.
      */
     public Bezier2D bezierPath() {
         /* Box pass through takes 2 beats, and travels 2 of our units;
          * therefore nominal dancer speed is 1 unit/beat.  Our output t
          * corresponds to {@link #time} beats, so we scale by that.
          * Also, the derivative of a cubic bezier gets a '3' term, so
          * further multiply by one-third.
          */
         Fraction scale = this.time.divide(Fraction.valueOf(3));
         Point p1 = new Point(this.from.x, this.from.y);
         Point p1tan = tangentStart();
         Point p2 = new Point(this.from.x.add(p1tan.x.multiply(scale)),
                              this.from.y.add(p1tan.y.multiply(scale)));
         Point p4 = new Point(this.to.x, this.to.y);
         Point p4tan = tangentFinish();
         Point p3 = new Point(this.to.x.subtract(p4tan.x.multiply(scale)),
                              this.to.y.subtract(p4tan.y.multiply(scale)));
         return new Bezier2D(p1, p2, p3, p4);
     }
     /** Return a 2D bezier describing the dancer's facing direction.  The 't'
      *  parameter of the bezier should vary from 0 to 1 over {@link #time}
      *  beats.  The ratio between the 'y' and 'x' components of the returned
      *  bezier gives the arctangent of the facing direction at time 't'.
      */
     public Bezier2D bezierDirection() {
         // if tangentStart and tangentFinish are lined up with the path,
         // then just return bezierPath().
         Point startTan = tangentStart(), endTan = tangentFinish();
         if (!(startTan.equals(Point.ZERO) || endTan.equals(Point.ZERO))) {
             ExactRotation startRot=ExactRotation.fromXY(startTan.x, startTan.y);
             ExactRotation endRot = ExactRotation.fromXY(endTan.x, endTan.y);
             if (startRot.equals(from.facing) && endRot.equals(to.facing))
                 return bezierPath().tangent();
         }
         // if facingStart == facingFinish, return a constant path for that
         // facing direction.
         if (from.facing.equals(to.facing)) {
             ExactRotation facing = (ExactRotation) from.facing;
             Point p = new Point(facing.toX(), facing.toY());
             return new Bezier2D(p, p); // linear/constant
         }
         // otherwise return a bezier which just linearly transitions from
         // facingStart to facingFinish
         // note that facingStart and facingFinish must not be directly opposed
         // or the resulting bezier will travel through 0,0.
         ExactRotation startRot = (ExactRotation) from.facing;
         ExactRotation endRot = (ExactRotation) to.facing;
         Point start = new Point(startRot.toX(), startRot.toY());
         Point end = new Point(endRot.toX(), endRot.toY());
         // We'd really want the bezier approximation to a circular arc from
         // start to end, but approximate it roughly by adding a midpoint.
         // This also ensures that the resulting bezier doesn't travel though
         // 0,0 if startRot and endRot are 180-degrees apart (but we'll
         // arbitrarily decide to turn CCW in that case).
         ExactRotation midRot = midPoint(startRot, endRot);
         Point mid = new Point(midRot.toX(), midRot.toY());
         return new Bezier2D(start, mid, end); // quasi-linear
     }
 
     /** Return true iff this {@link DancerPath} corresponds to
      *  "standing still"; ie, it just syncs time and doesn't change any of
      *  a dancer's state.
      */
     public boolean isStandStill() {
         // xxx: are we going to have to remove position flags from this
         //      comparison at some point?
         return this.from.equals(this.to);
     }
     /** Return an equivalent {@link DancerPath} that completes in an adjusted
      * amount of time.
      */
     public DancerPath scaleTime(Fraction amount) {
         if (amount.equals(Fraction.ONE)) return this; // easy case!
         return new DancerPath
             (from, to, time.multiply(amount), pointOfRotation, flags);
     }
     /** Return a {@link DancerPath} like this one, except with the 'to' and
      * 'from' positions warped to those given.
      */
     public DancerPath translate(Position from, Position to) {
         return new DancerPath(from, to, this.time, this.pointOfRotation,
                               this.flags);
     }
     /** Return a {@link DancerPath} like this one, except with mirrored
      *  position.
      */
     public DancerPath mirror(boolean mirrorShoulderPass) {
         return new DancerPath(this.from.mirror(mirrorShoulderPass),
                               this.to.mirror(mirrorShoulderPass),
                               this.time, this.pointOfRotation, this.flags);
     }
 
     @Override
     public String toString() {
         ToStringBuilder tsb =
             new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
 	    .append("from", from)
 	    .append("to", to)
 	    .append("time", time.toProperString())
 	    .append("pointOfRotation", pointOfRotation);
         if (!flags.isEmpty())
             tsb = tsb.append("flags", flags);
         return tsb.toString();
     }
 }
