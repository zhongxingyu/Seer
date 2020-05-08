 package net.cscott.sdr.calls.ast;
 
 import static net.cscott.sdr.calls.transform.AstTokenTypes.PRIM;
 import net.cscott.sdr.calls.ExactRotation;
 import net.cscott.sdr.calls.transform.TransformVisitor;
 import net.cscott.sdr.calls.transform.ValueVisitor;
 import net.cscott.sdr.util.Fraction;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 /**
  * A Prim represents a primitive action: a certain distance travelled
  * forward and to the side, while rotating a certain amount, performed
  * in a certain number of beats.  PRIM is a leaf node in a our AST.
  * @author C. Scott Ananian
  * @version $Id: Prim.java,v 1.12 2007-03-07 19:56:21 cananian Exp $
  */
 public class Prim extends SeqCall {
     /** The {@link Direction} enumeration tells whether the movement
      * is relative to the center of the formation. */
     public static enum Direction {
         /** Directions are not relative to formation: positive is
          *  right/forward/cw; negative is left/backward/ccw. */
         ASIS,
         /** Directions are relative to formation: positive is "toward the
          * center" and negative is "away from the center". */
         IN
     }
     /** Amount of movement in the 'x' direction (dancer's right/left). */
     public final Fraction x;
     /** Amount of movement in the 'y' direction (dancer forward and back). */
     public final Fraction y;
     /** Amount of rotation. */
     public final ExactRotation rot;
     /** Is the movement direction relative to the center of the formation? */
     public final Direction dirX, dirY, dirRot;
     /** The number of beats which this motion should take. */
     public final Fraction time;
     /** This parameter indicates whether this motion involves a right
      * shoulder pass (as most motions do, including "cross" calls where the
      * crossers start far apart) or else a left shoulder pass
      * ("cross" calls which start with adjacent crossers).
      */
     public final boolean passRight;
     /** The @{link #forceArc} parameter helps distinguish between (say)
      * "pass thru and quarter in" and "split counter rotate 1/4".  Both
      * of these involve traveling forward and turning, but the latter
      * is an arcing motion (hence {@link #forceArc} would be true) while
      * the former is a straight line path (hence {@link #forceArc} would
      * be false).
      */
     public final boolean forceArc;
 
     public Prim(Direction dirX, Fraction x, Direction dirY, Fraction y,
             Direction dirRot, ExactRotation rot, Fraction time,
             boolean passRight, boolean forceArc) {
         super(PRIM);
         this.x = x; this.y = y; this.rot = rot; this.time = time;
         this.dirX = dirX; this.dirY = dirY; this.dirRot = dirRot;
         this.passRight = passRight;
         this.forceArc = forceArc;
     }
     public static final Prim STAND_STILL =
         new Prim(Direction.ASIS,Fraction.ZERO, Direction.ASIS,Fraction.ZERO,
                 Direction.ASIS,ExactRotation.ZERO, Fraction.ONE, true, false);
     // support visitor
     @Override
     public <T> SeqCall accept(TransformVisitor<T> v, T t) {
         return v.visit(this, t);
     }
     @Override
     public <RESULT,CLOSURE>
     RESULT accept(ValueVisitor<RESULT,CLOSURE> v, CLOSURE cl) {
         return v.visit(this, cl);
     }
     // utility methods
     @Override
     public String argsToString() {
         return (dirX==Direction.IN?"in ":"")+
 	    x.toProperString()+", "+
 	    (dirY==Direction.IN?"in ":"")+
 	    y.toProperString()+", "+
 	    (dirRot==Direction.IN?"in ":"")+
             rot.toRelativeString()+", "+
 	    time.toProperString()+
             (passRight?"":", pass-left")+
             (forceArc?", force-arc":"");
     }
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof Prim)) return false;
         Prim p = (Prim) o;
         return new EqualsBuilder()
             .append(dirX, p.dirX)
             .append(x, p.x)
             .append(dirY, p.dirY)
             .append(y, p.y)
             .append(dirRot, p.dirRot)
             .append(rot, p.rot)
             .append(time, p.time)
             .append(passRight, p.passRight)
             .append(forceArc, p.forceArc)
             .isEquals();
     }
     @Override
     public int hashCode() {
         if (hashCode==0)
             hashCode = new HashCodeBuilder()
             .append(x).append(y).append(rot).append(time)
             .append(dirX).append(dirY).append(dirRot)
             .append(passRight).append(forceArc)
             .toHashCode();
         return hashCode;
     }
     private transient int hashCode = 0;
     
     // Factory methods
     /** Create a new Prim, identical to this one except that the time
      * is scaled by the given fraction. */
     public Prim scaleTime(Fraction f) {
         if (Fraction.ONE.equals(f)) return this;
         return new Prim(dirX, x, dirY, y, dirRot, rot,
                 time.multiply(f), passRight, forceArc);
     }
     /** Factory: creates new Prim only if it would differ from this. */
     public Prim build(Direction dirX, Fraction x, Direction dirY, Fraction y,
             Direction dirRot, ExactRotation rot, Fraction time,
             boolean passRight, boolean forceArc) {
         Prim p = new Prim(dirX, x, dirY, y, dirRot, rot, time, passRight, forceArc);
         if (this.equals(p)) return this;
         return p;
     }
 }
