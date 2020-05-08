 package net.cscott.sdr.calls;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Set;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.util.Fraction;
 import org.apache.commons.lang.builder.*;
 import org.junit.runner.RunWith;
 
 /** Position objects represent the position and orientation of a dancer.
  *  The (0,0) coordinate represents the center of the square (or formation),
  *  and dancers
  *  are nominally at least two units away from each other (although breathing
  *  may change this).  A zero rotation for 'facing direction' means
  *  "facing away from the caller".  Positive y is "away from the caller".  Positive
  *  x is "toward the caller's right".  The boy in couple number one
  *  starts out at <code>(-1, -3)</code> facing <code>0</code>.
  *  The <code>facing</code> field may not be <code>null</code>;
  *  to indicate "rotation unspecified" (for example, for phantoms
  *  or when specifying "general lines") use a {@link Rotation} with a
  *  modulus of 0.
  */
 @RunWith(value=JDoctestRunner.class)
 public class Position implements Comparable<Position> {
     /** Various flags describing boolean properties of a {@link Position}. */
     public enum Flag {
 	/** A left-shoulder pass/collide to lefts is indicated. */
 	PASS_LEFT,
 	/** Roll direction was to the dancer's left. */
 	ROLL_LEFT,
 	/** Roll direction was to the dancer's right. */
 	ROLL_RIGHT,
 	/** Sweep direction was to the dancer's left. */
 	SWEEP_LEFT,
 	/** Sweep direction was to the dancer's right. */
 	SWEEP_RIGHT,
     };
     /** Location. Always non-null. */
     public final Fraction x, y;
     /** Facing direction. Note that {@code facing} should always be an
      * {@link ExactRotation} for real (non-phantom) dancers. */
     public final Rotation facing;
     /** Flags describing the history of this {@link Position}. */
     public final Set<Flag> flags;
     /** Mutable set implementing the flags field, for efficiency. */
     private final EnumSet<Flag> _flags;
 
     /**
      * Create a Position object from the given x and y coordinates,
      * facing {@link Rotation}, and {@link Flag}s. */
     public Position(Fraction x, Fraction y, Rotation facing, Flag... flags) {
 	assert x!=null; assert y!=null; assert facing!=null;
 	this.x = x; this.y = y; this.facing = facing;
 	// somewhat awkward creation/encapsulation of an EnumSet
 	this._flags = EnumSet.noneOf(Flag.class);
 	this._flags.addAll(Arrays.asList(flags));
 	this.flags = Collections.unmodifiableSet(this._flags);
 	// ROLL_RIGHT and ROL
 	assert !(this._flags.contains(Flag.ROLL_RIGHT) &&
 		 this._flags.contains(Flag.ROLL_LEFT)) :
 	       "ROLL_RIGHT and ROLL_LEFT are exclusive.";
 	assert !(this._flags.contains(Flag.SWEEP_RIGHT) &&
 		 this._flags.contains(Flag.SWEEP_LEFT)):
 	       "SWEEP_RIGHT and SWEEP_LEFT are exclusive.";
     }
     /**
      * Create a Position object with integer-valued x and y coordinates,
      * a {@link Rotation} and {@link Flag}s. */
     public Position(int x, int y, Rotation facing, Flag... flags) {
         this(Fraction.valueOf(x),Fraction.valueOf(y),facing, flags);
     }
     /**
      * Create a Position object from the given x and y coordinates,
      * facing {@link Rotation}, and the given {@link Set} of {@link Flag}s. */
     protected Position(Fraction x,Fraction y,Rotation facing, Set<Flag> flags){
 	this(x, y, facing);
 	assert !(flags.contains(Flag.ROLL_RIGHT) &&
 		 flags.contains(Flag.ROLL_LEFT)) :
 	       "ROLL_RIGHT and ROLL_LEFT are exclusive.";
 	assert !(flags.contains(Flag.SWEEP_RIGHT) &&
 		 flags.contains(Flag.SWEEP_LEFT)):
 	       "SWEEP_RIGHT and SWEEP_LEFT are exclusive.";
 	this._flags.addAll(flags); // efficient if both are EnumSets.
     }
     /**
      * Move the given distance in the facing direction, clearing any
      * {@link Flag}s.  Requires that the {@code facing} direction be
      * an {@link ExactRotation}.  If {@code stepIn} is true, the
      * distance is negated if the result would end up closer to the
      * origin for positive distance (stepping "in" towards the center
      * of the formation, or further to the origin for negative
      * distance (stepping "out" away from the center).
      *
      * @doc.test Move the couple #1 boy forward two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(-1,-3,"n").forwardStep(Fraction.TWO, false)
      *  -1,-1,n
      * @doc.test Move the couple #1 boy backward two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(-1,-3,"n").forwardStep(Fraction.TWO.negate(), false)
      *  -1,-5,n
      * @doc.test Move the couple #1 boy "in" two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(-1,-3,"n").forwardStep(Fraction.TWO, true)
      *  -1,-1,n
      * @doc.test Move the couple #1 boy "out" two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(-1,-3,"n").forwardStep(Fraction.TWO.negate(), true)
      *  -1,-5,n
      * @doc.test Couple #2 boy facing out; move forward two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(3,-1,"e").forwardStep(Fraction.TWO, false)
      *  5,-1,e
      * @doc.test Couple #2 boy facing out; move backward two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(3,-1,"e").forwardStep(Fraction.TWO.negate(), false)
      *  1,-1,e
      * @doc.test Couple #2 boy facing out; move "in" two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(3,-1,"e").forwardStep(Fraction.TWO, true)
      *  1,-1,e
      * @doc.test Couple #2 boy facing out; move "out" two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(3,-1,"e").forwardStep(Fraction.TWO.negate(), true)
      *  5,-1,e
      * @doc.test Any flags present are cleared.
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ZERO, Fraction.ZERO, ExactRotation.ZERO,
      *    >                  Position.Flag.PASS_LEFT, Position.Flag.ROLL_RIGHT);
      *  0,0,n,[PASS_LEFT, ROLL_RIGHT]
      *  js> p.forwardStep(Fraction.ZERO, false)
      *  0,0,n
      * @doc.test Can't step in if there's no clear in direction.
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ONE, Fraction.ZERO, ExactRotation.ZERO);
      *  1,0,n
      *  js> try { p.forwardStep(Fraction.ONE, true); }
      *    > catch(e) { print(e.javaException); }
      *  net.cscott.sdr.calls.BadCallException: no clear 'in' direction
      */
     public Position forwardStep(Fraction distance, boolean stepIn) {
         if (distance.equals(Fraction.ZERO) && this.flags.isEmpty())
 	    return this; // no op.
 	assert facing!=null : "rotation unspecified!";
 	Fraction dx = ((ExactRotation)facing).toX().multiply(distance);
 	Fraction dy = ((ExactRotation)facing).toY().multiply(distance);
         Position p1 = new Position(x.add(dx), y.add(dy), facing);
         if (!stepIn) return p1; // simple case!
         Position p2 = new Position(x.subtract(dx), y.subtract(dy), facing);
         Fraction d1 = p1.x.multiply(p1.x).add(p1.y.multiply(p1.y));
         Fraction d2 = p2.x.multiply(p2.x).add(p2.y.multiply(p2.y));
         int c = d1.compareTo(d2);
         if (c==0) throw new BadCallException("no clear 'in' direction");
         if (distance.compareTo(Fraction.ZERO) > 0)
             return (c>0) ? p2 : p1;
         else
             return (c>0) ? p1 : p2;
     }
     /**
      * Move the given distance perpendicular to the facing direction,
      * clearing any {@link Flag}s.  Requires that the {@code facing}
      * direction be an {@link ExactRotation}.  If {@code stepIn} is
      * true, the distance is negated if the result would end up closer
      * to the origin for positive distance (stepping "in" towards the
      * center of the formation, or further to the origin for negative
      * distance (stepping "out" away from the center).
      *
      * @doc.test Couple #2 girl move "right" two steps (truck):
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(3,1,"w").sideStep(Fraction.TWO, false)
      *  3,3,w
      * @doc.test Couple #1 boy move "left" two steps (truck):
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(-1,-3,"n").sideStep(Fraction.TWO.negate(), false)
      *  -3,-3,n
      * @doc.test Couple #1 boy facing west move "in" two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(-1,-3,"w").sideStep(Fraction.TWO, true)
      *  -1,-1,w
      * @doc.test Couple #1 boy facing west move "out" two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(-1,-3,"w").sideStep(Fraction.TWO.negate(), true)
      *  -1,-5,w
      * @doc.test Couple #1 boy facing east move "out" two steps:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> Position.getGrid(-1,-3,"e").sideStep(Fraction.TWO.negate(), true)
      *  -1,-5,e
      * @doc.test Any flags present are cleared.
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ZERO, Fraction.ZERO, ExactRotation.ZERO,
      *    >                  Position.Flag.PASS_LEFT, Position.Flag.ROLL_RIGHT);
      *  0,0,n,[PASS_LEFT, ROLL_RIGHT]
      *  js> p.sideStep(Fraction.ZERO, false)
      *  0,0,n
      * @doc.test Can't step in if there's no clear in direction.
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ZERO, Fraction.ONE, ExactRotation.ZERO);
      *  0,1,n
      *  js> try { p.sideStep(Fraction.ONE, true); }
      *    > catch(e) { print(e.javaException); }
      *  net.cscott.sdr.calls.BadCallException: no clear 'in' direction
      */
     public Position sideStep(Fraction distance, boolean stepIn) {
         if (distance.equals(Fraction.ZERO) && this.flags.isEmpty())
 	    return this; // no op.
         assert facing!=null : "rotation unspecified!";
         ExactRotation f = (ExactRotation) facing.add(Fraction.ONE_QUARTER);
         Fraction dx = f.toX().multiply(distance);
         Fraction dy = f.toY().multiply(distance);
         Position p1 = new Position(x.add(dx), y.add(dy), facing);
         if (!stepIn) return p1; // simple case!
         Position p2 = new Position(x.subtract(dx), y.subtract(dy), facing);
         Fraction d1 = p1.x.multiply(p1.x).add(p1.y.multiply(p1.y));
         Fraction d2 = p2.x.multiply(p2.x).add(p2.y.multiply(p2.y));
         int c = d1.compareTo(d2);
         if (c==0) throw new BadCallException("no clear 'in' direction");
         if (distance.compareTo(Fraction.ZERO) > 0)
             return (c>0) ? p2 : p1;
         else
             return (c>0) ? p1 : p2;
     }
     /**
      * Turn in place the given amount, clearing any {@link Flag}s.  If
      * {@code faceIn} is true, a positive amount will turn towards the
      * origin; otherwise a positive amount turns clockwise.
      *
      * @doc.test Exercise the turn method; amounts aren't normalized in order
      *  to preserve proper roll/sweep directions:
      *  js> ONE_HALF = net.cscott.sdr.util.Fraction.ONE_HALF
      *  1/2
      *  js> p = Position.getGrid(0,0,"n").turn(ONE_HALF, false)
      *  0,0,s
      *  js> p = p.turn(ONE_HALF, false)
      *  0,0,n
      *  js> p.facing.amount.toProperString()
      *  1
      * @doc.test Turning "in" when in is clockwise:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = Position.getGrid(1,1,"s").turn(Fraction.ONE_QUARTER, true)
      *  1,1,w
      * @doc.test Turning "out" when in is clockwise:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = Position.getGrid(1,1,"s").turn(Fraction.ONE_QUARTER.negate(), true)
      *  1,1,e
      * @doc.test Turning "in" when in is counter-clockwise:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = Position.getGrid(1,1,"n").turn(Fraction.ONE_QUARTER, true)
      *  1,1,w
      * @doc.test Turning "out" when in is counter-clockwise:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = Position.getGrid(1,1,"n").turn(Fraction.ONE_QUARTER.negate(), true)
      *  1,1,e
      * @doc.test Any flags present are cleared.
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ZERO, Fraction.ZERO, ExactRotation.ZERO,
      *    >                  Position.Flag.PASS_LEFT, Position.Flag.ROLL_RIGHT);
      *  0,0,n,[PASS_LEFT, ROLL_RIGHT]
      *  js> p.turn(Fraction.ZERO, false)
      *  0,0,n
      * @doc.test Can't face in if facing exactly into the center:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ONE, Fraction.ONE, ExactRotation.FIVE_EIGHTHS);
      *  1,1,sw
      *  js> try { p = p.turn(Fraction.ONE_QUARTER, true); }
      *    > catch(e) { print(e.javaException); }
      *  net.cscott.sdr.calls.BadCallException: no clear 'in' direction
      * @doc.test Can't face in if facing exactly away from the center:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ZERO, Fraction.ONE, ExactRotation.ZERO);
      *  0,1,n
      *  js> try { p = p.turn(Fraction.ONE_QUARTER, true); }
      *    > catch(e) { print(e.javaException); }
      *  net.cscott.sdr.calls.BadCallException: no clear 'in' direction
      * @doc.test We can face in from some inexact directions:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ONE, Fraction.ZERO, Rotation.fromAbsoluteString("|"));
      *  1,0,|
      *  js> p = p.turn(Fraction.ONE_QUARTER, true)
      *  1,0,w
      *  js> p = new Position(Fraction.ONE, Fraction.ZERO, Rotation.fromAbsoluteString("|"));
      *  1,0,|
      *  js> p = p.turn(Fraction.ONE_QUARTER.negate(), true)
      *  1,0,e
      */
     public Position turn(Fraction amount, boolean faceIn) {
 	return this.turn(amount, faceIn, this);
     }
     /**
      * Turn in place the given amount, clearing any {@link Flag}s.  If
      * {@code faceIn} is true, a positive amount will turn towards the
      * origin; otherwise a positive amount turns clockwise.
      *
      * <p>This version of the method takes an additional argument
      * specifying a point at which to evaluate the "in/out" direction.
      */
     public Position turn(Fraction amount, boolean faceIn, Position reference) {
         if (amount.equals(Fraction.ZERO) && this.flags.isEmpty()) return this;
 	assert facing!=null : "rotation unspecified!";
 	Position p1 = new Position(x, y, facing.add(amount));
         if (!faceIn) return p1; // simple case!
         if (!facing.isExact()) {
             if (facing.modulus.compareTo(Fraction.ZERO)==0)
                 return this; // no change
             // implement by iterating over the included ExactRotations,
             // performing the turn, and then unioning the results.
             List<Rotation> rl = new ArrayList<Rotation>();
             for (ExactRotation er : facing.included())
                 rl.add(new Position(x, y, er).turn(amount, faceIn, reference)
                         .facing);
             return new Position(x, y, Rotation.union(rl));
         }
         Position p2 = new Position(x, y, facing.subtract(amount));
         // don't allow in/out if facing direction toward the center
         // direction from dancer to center point
         ExactRotation awayCenter =
 	    ExactRotation.fromXY(reference.x, reference.y);
         Fraction f = facing.subtract(awayCenter.amount).normalize().amount;
         int czero = f.compareTo(Fraction.ZERO);
         int chalf = f.compareTo(Fraction.ONE_HALF);
         if (czero==0 || chalf==0)
             throw new BadCallException("no clear 'in' direction");
         assert czero > 0;
         if (chalf > 0)
             return p2; // "in" is ccw here.
         else
             return p1; // "in" is cw.
     }
     /**
      * Return a new {@link Position} identical to this one, except
      * with exactly the given flags set.
      *
      * @doc.test Set the SWEEP_LEFT and ROLL_RIGHT flags:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ZERO, Fraction.ZERO, ExactRotation.ZERO,
      *    >                  Position.Flag.PASS_LEFT, Position.Flag.ROLL_RIGHT);
      *  0,0,n,[PASS_LEFT, ROLL_RIGHT]
      *  js> p.setFlags(Position.Flag.ROLL_RIGHT, Position.Flag.SWEEP_LEFT)
      *  0,0,n,[ROLL_RIGHT, SWEEP_LEFT]
      */
     public Position setFlags(Flag... flags) {
 	return new Position(this.x, this.y, this.facing, flags);
     }
     /**
      * Return a new {@link Position} identical to this one, except
      * with exactly the given flags set.
      * @see #setFlags(Flag...)
      */
     public Position setFlags(Collection<Flag> f) {
         return setFlags(f.toArray(new Flag[f.size()]));
     }
     /**
      * Return a new {@link Position} identical to this one, except
      * with the given flags set.
      *
      * @doc.test Add the SWEEP_LEFT flag:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ZERO, Fraction.ZERO, ExactRotation.ZERO,
      *    >                  Position.Flag.PASS_LEFT, Position.Flag.ROLL_RIGHT);
      *  0,0,n,[PASS_LEFT, ROLL_RIGHT]
      *  js> p=p.addFlags(Position.Flag.ROLL_RIGHT, Position.Flag.SWEEP_LEFT)
      *  0,0,n,[PASS_LEFT, ROLL_RIGHT, SWEEP_LEFT]
      *  js> p=p.addFlags(Position.Flag.ROLL_RIGHT, Position.Flag.SWEEP_LEFT)
      *  0,0,n,[PASS_LEFT, ROLL_RIGHT, SWEEP_LEFT]
      */
     public Position addFlags(Flag... flags) {
 	EnumSet<Flag> es = EnumSet.copyOf(this._flags);
 	es.addAll(Arrays.asList(flags));
 	if (es.equals(this._flags)) return this; // optimization
 	return new Position(this.x, this.y, this.facing, es);
     }
     /**
      * Move a {@link Position}, preserving its flags.
      *
      * @doc.test Add the SWEEP_LEFT flag:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ZERO, Fraction.ZERO, ExactRotation.ZERO,
      *    >                  Position.Flag.PASS_LEFT, Position.Flag.ROLL_RIGHT);
      *  0,0,n,[PASS_LEFT, ROLL_RIGHT]
      *  js> p = p.relocate(Fraction.ONE, Fraction.TWO, ExactRotation.ONE_QUARTER);
      *  1,2,e,[PASS_LEFT, ROLL_RIGHT]
      */
     public Position relocate(Fraction x, Fraction y, Rotation facing) {
 	return new Position(x, y, facing, this._flags);
     }
     /**
      * Return a roll amount from the {@link Flag#ROLL_LEFT} and
      * {@link Flag#ROLL_RIGHT} {@link Flag}s set on this {@link Position}.
      *
      * @doc.test
      *  js> p = Position.getGrid(0,0,"n");
      *  0,0,n
      *  js> p.roll();
      *  0/1
      *  js> p = p.addFlags(Position.Flag.PASS_LEFT, Position.Flag.ROLL_RIGHT, Position.Flag.SWEEP_LEFT); 
      *  0,0,n,[PASS_LEFT, ROLL_RIGHT, SWEEP_LEFT]
      *  js> p.roll()
      *  1/4
      *  js> p = p.setFlags(Position.Flag.SWEEP_RIGHT, Position.Flag.ROLL_LEFT)
      *  0,0,n,[ROLL_LEFT, SWEEP_RIGHT]
      *  js> p.roll()
      *  -1/4
      */
     public Fraction roll() {
 	if (this._flags.contains(Flag.ROLL_RIGHT)) {
 	    assert !this._flags.contains(Flag.ROLL_LEFT);
 	    return ExactRotation.ONE_QUARTER.amount;
 	}
 	if (this._flags.contains(Flag.ROLL_LEFT)) {
 	    return ExactRotation.mONE_QUARTER.amount;
 	}
 	return ExactRotation.ZERO.amount;
     }
     /**
      * Return a sweep amount from the {@link Flag#SWEEP_LEFT} and
      * {@link Flag#SWEEP_RIGHT} {@link Flag}s set on this {@link Position}.
      *
      * @doc.test
      *  js> p = Position.getGrid(0,0,"n");
      *  0,0,n
      *  js> p.sweep();
      *  0/1
      *  js> p = p.addFlags(Position.Flag.PASS_LEFT, Position.Flag.ROLL_RIGHT, Position.Flag.SWEEP_LEFT); 
      *  0,0,n,[PASS_LEFT, ROLL_RIGHT, SWEEP_LEFT]
      *  js> p.sweep()
      *  -1/4
      *  js> p = p.setFlags(Position.Flag.SWEEP_RIGHT, Position.Flag.ROLL_LEFT)
      *  0,0,n,[ROLL_LEFT, SWEEP_RIGHT]
      *  js> p.sweep()
      *  1/4
      */
     public Fraction sweep() {
 	if (this._flags.contains(Flag.SWEEP_RIGHT)) {
 	    assert !this._flags.contains(Flag.SWEEP_LEFT);
 	    return ExactRotation.ONE_QUARTER.amount;
 	}
 	if (this._flags.contains(Flag.SWEEP_LEFT)) {
 	    return ExactRotation.mONE_QUARTER.amount;
 	}
 	return ExactRotation.ZERO.amount;
     }
     /**
      * Rotate this position around the origin by the given amount,
      * preserving any {@link Flag}s.
      *
      * @doc.test Rotating the #1 boy by 1/4 gives the #4 boy position:
      *  js> p = Position.getGrid(-1,-3,0)
      *  -1,-3,n
      *  js> p = p.setFlags(Position.Flag.PASS_LEFT)
      *  -1,-3,n,[PASS_LEFT]
      *  js> p.rotateAroundOrigin(ExactRotation.ONE_QUARTER)
      *  -3,1,e,[PASS_LEFT]
      */
     public Position rotateAroundOrigin(ExactRotation rot) {
         // x' =  x*cos(rot) + y*sin(rot)
         // y' = -x*sin(rot) + y*cos(rot)
         // where sin(rot) = rot.toX() and cos(rot) = rot.toY()
         Fraction cos = rot.toY(), sin = rot.toX();
         Fraction nx = this.x.multiply(cos).add(this.y.multiply(sin));
         Fraction ny = this.y.multiply(cos).subtract(this.x.multiply(sin));
         return new Position(nx, ny, facing.add(rot.amount), this._flags);
     }
 
     /**
      * Return a new position, mirrored around the y-axis, including the
      * pass, roll, and sweep flags.
      * @doc.test
      *  js> p=Position.getGrid(1,2,"e",Position.Flag.SWEEP_LEFT,Position.Flag.ROLL_RIGHT);
      *  1,2,e,[ROLL_RIGHT, SWEEP_LEFT]
      *  js> p.mirror(false);
      *  -1,2,w,[ROLL_LEFT, SWEEP_RIGHT]
      *  js> p.mirror(true);
      *  -1,2,w,[PASS_LEFT, ROLL_LEFT, SWEEP_RIGHT]
      */
     public Position mirror(boolean mirrorShoulderPass) {
         EnumSet<Flag> nflags = EnumSet.noneOf(Flag.class);
         for (Flag f : this.flags) {
             switch (f) {
             case ROLL_LEFT: nflags.add(Flag.ROLL_RIGHT); break;
             case ROLL_RIGHT: nflags.add(Flag.ROLL_LEFT); break;
             case SWEEP_LEFT: nflags.add(Flag.SWEEP_RIGHT); break;
             case SWEEP_RIGHT: nflags.add(Flag.SWEEP_LEFT); break;
             case PASS_LEFT: /* we'll deal with this later */ break;
             }
         }
         if (mirrorShoulderPass ^ this.flags.contains(Flag.PASS_LEFT))
             nflags.add(Flag.PASS_LEFT);
         return new Position(this.x.negate(), this.y,
                             this.facing.negate().add(Fraction.ONE),
                             nflags);
     }
     /**
      * Normalize (restrict to 0-modulus) the rotation of the given position,
      * preserving any {@link Flag}s.
      *
      * @doc.test Show normalization after two 180-degree turns:
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = Position.getGrid(0,0,"e").turn(Fraction.ONE_HALF, false)
      *  0,0,w
      *  js> p = p.turn(Fraction.ONE_HALF, false)
      *  0,0,e
      *  js> p.facing.amount.toProperString()
      *  1 1/4
      *  js> p = p.setFlags(Position.Flag.PASS_LEFT)
      *  0,0,e,[PASS_LEFT]
      *  js> p = p.normalize()
      *  0,0,e,[PASS_LEFT]
      *  js> p.facing.amount.toProperString()
      *  1/4
      */
     public Position normalize() {
         return new Position(x, y, facing.normalize(), this._flags);
     }
 
     // positions in the standard 4x4 grid.
     /** Returns a position corresponding to the standard square
      *  dance grid.  0,0 is the center of the set, and odd coordinates
      *  between -3 and 3 correspond to the standard 4x4 grid.
      * @doc.test Some sample grid locations:
      *  js> Position.getGrid(0,0,ExactRotation.ZERO)
      *  0,0,n
      *  js> Position.getGrid(-3,3,ExactRotation.WEST)
      *  -3,3,w
      */
     public static Position getGrid(int x, int y, Rotation r,
 				   Flag... flags) {
         assert r != null;
 	return new Position
 	    (Fraction.valueOf(x), Fraction.valueOf(y), r, flags);
     }
     /** Returns a position corresponding to the standard square
      *  dance grid.  0,0 is the center of the set, and odd coordinates
      *  between -3 and 3 correspond to the standard 4x4 grid.
      *  For convenience, the direction is specified as a string
      *  valid for <code>ExactRotation.valueOf(String)</code>.
      * @doc.test Some sample grid locations:
      *  js> Position.getGrid(0,0,"n")
      *  0,0,n
      *  js> Position.getGrid(1,2,"e")
      *  1,2,e
      */
     public static Position getGrid(int x, int y, String direction,
 				   Flag... flags) {
 	return getGrid(x,y,ExactRotation.fromAbsoluteString(direction), flags);
     }
 
     // utility functions.
     @Override
     public boolean equals(Object o) {
 	if (!(o instanceof Position)) return false;
 	Position p = (Position) o;
 	return new EqualsBuilder()
 	    .append(x, p.x)
 	    .append(y, p.y)
 	    .append(facing, p.facing)
 	    .append(_flags, p._flags)
 	    .isEquals();
     }
     @Override
     public int hashCode() {
         if (hashCode==0)
             hashCode = new HashCodeBuilder()
 	    .append(x).append(y).append(facing).append(_flags)
             .toHashCode();
         return hashCode;
     }
     private transient int hashCode = 0;
     @Override
     public String toString() {
 	ToStringBuilder tsb = new ToStringBuilder
 	    (this, ToStringStyle.SIMPLE_STYLE)
 	    .append("x", x.toProperString())
 	    .append("y", y.toProperString())
 	    .append("facing", facing.toAbsoluteString());
 	if (!flags.isEmpty())
 	    tsb = tsb.append("flags", flags);
 	return tsb.toString();
     }
     /** Return a short string describing the flags on this position.
      * @doc.test
      *  js> p=Position.getGrid(1,2,"e",Position.Flag.SWEEP_LEFT,Position.Flag.ROLL_RIGHT);
      *  1,2,e,[ROLL_RIGHT, SWEEP_LEFT]
      *  js> new String(p.shortFlagString()).toSource()
      *  (new String("RL"))
      *  js> p = p.addFlags(Position.Flag.PASS_LEFT)
      *  1,2,e,[PASS_LEFT, ROLL_RIGHT, SWEEP_LEFT]
      *  js> new String(p.shortFlagString()).toSource()
      *  (new String("rl"))
      *  js> p = p.setFlags(Position.Flag.PASS_LEFT, Position.Flag.ROLL_LEFT)
      *  1,2,e,[PASS_LEFT, ROLL_LEFT]
      *  js> new String(p.shortFlagString()).toSource()
      *  (new String("l_"))
      *  js> p = p.setFlags(Position.Flag.PASS_LEFT)
      *  1,2,e,[PASS_LEFT]
      *  js> new String(p.shortFlagString()).toSource()
      *  (new String("__"))
      *  js> p = p.addFlags(Position.Flag.SWEEP_RIGHT)
      *  1,2,e,[PASS_LEFT, SWEEP_RIGHT]
      *  js> new String(p.shortFlagString()).toSource()
      *  (new String("_r"))
      *  js> p = p.setFlags(Position.Flag.SWEEP_RIGHT)
      *  1,2,e,[SWEEP_RIGHT]
      *  js> new String(p.shortFlagString()).toSource()
      *  (new String(" R"))
      *  js> p = p.setFlags()
      *  1,2,e
      *  js> new String(p.shortFlagString()).toSource()
      *  (new String("  "))
      */
     public String shortFlagString() {
         String roll = flags.contains(Flag.ROLL_RIGHT) ? "R" :
                       flags.contains(Flag.ROLL_LEFT) ? "L" : " ";
         String sweep = flags.contains(Flag.SWEEP_RIGHT) ? "R" :
                        flags.contains(Flag.SWEEP_LEFT) ? "L" : " ";
         String s = roll + sweep;
         if (flags.contains(Position.Flag.PASS_LEFT))
             s = s.toLowerCase().replace(' ', '_');
         return s;
     }
     /** Emit an executable representation of this position.
      * @doc.test
      *  js> importPackage(net.cscott.sdr.util)
      *  js> p = new Position(Fraction.ZERO, Fraction.ONE_HALF, ExactRotation.ONE_EIGHTH,
      *    >                  Position.Flag.PASS_LEFT, Position.Flag.ROLL_RIGHT);
      *  0,1/2,ne,[PASS_LEFT, ROLL_RIGHT]
      *  js> p.repr()
      *  new Position(Fraction.valueOf(0), Fraction.valueOf(1,2), ExactRotation.ONE_EIGHTH, Position.Flag.PASS_LEFT, Position.Flag.ROLL_RIGHT)
      */
     public String repr() {
         StringBuilder sb = new StringBuilder();
         sb.append("new Position(");
         sb.append(x.repr()); sb.append(", ");
         sb.append(y.repr()); sb.append(", ");
         sb.append(facing.repr());
         for (Flag f : _flags) {
             sb.append(", Position.Flag.");
             sb.append(f.name());
         }
         sb.append(")");
         return sb.toString();
     }
     /**
      * Compare two {@link Position}s.  We use reading order: top to bottom,
      * then left to right.  Ties are broken first by facing direction: first
      * the most specific rotation modulus, then by normalized direction.
      * Remaining ties are broken by comparing the flag sets.
      * @doc.test Top to bottom:
      *  js> Position.getGrid(0,0,"n").compareTo(Position.getGrid(1,1,"n")) > 0
      *  true
      * @doc.test Left to right:
      *  js> Position.getGrid(1,0,"n").compareTo(Position.getGrid(0,0,"n")) > 0
      *  true
      * @doc.test Most specific rotation modulus first:
      *  js> new Position["(int,int,net.cscott.sdr.calls.Rotation,net.cscott.sdr.calls.Position$Flag[])"](
      *    >              0,0,Rotation.fromAbsoluteString("|")
      *    >              ).compareTo(Position.getGrid(0,0,"n")) > 0
      *  true
      * @doc.test Break ties by comparing position flags:
      *  js> Position.getGrid(1,2,"e").compareTo(Position.getGrid(1,2,"e",Position.Flag.PASS_LEFT)) != 0
      *  true
      * @doc.test Normalized direction:
      *  js> Position.getGrid(0,0,"e").compareTo(Position.getGrid(0,0,"n")) > 0
      *  true
      * @doc.test Equality:
      *  js> Position.getGrid(1,2,"w").compareTo(Position.getGrid(1,2,"w")) == 0
      *  true
      */
     public int compareTo(Position p) {
         int c = -this.y.compareTo(p.y);
         if (c!=0) return c;
         c = this.x.compareTo(p.x);
         if (c!=0) return c;
         c = -this.facing.modulus.compareTo(p.facing.modulus);
         if (c!=0) return c;
         c = this.facing.normalize().amount.compareTo
             (p.facing.normalize().amount);
         if (c!=0) return c;
 	for (Flag f: Flag.values()) {
 	    c = Boolean.valueOf(this.flags.contains(f))
 		.compareTo(Boolean.valueOf(p.flags.contains(f)));
 	    if (c!=0) return c;
 	}
 	return c;
     }
 }
