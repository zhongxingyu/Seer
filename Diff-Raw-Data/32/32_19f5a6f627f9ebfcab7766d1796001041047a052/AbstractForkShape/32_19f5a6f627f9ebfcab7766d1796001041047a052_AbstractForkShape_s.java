 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.gfx.shapes.forkes;
 
 import de.weltraumschaf.jebnf.gfx.Point;
 import de.weltraumschaf.jebnf.gfx.shapes.other.RectangularShape;
 import java.awt.Graphics2D;
 
 /**
 * Forks are a combination of either a {@link de.weltraumschaf.jebnf.gfx.shapes.other.StraightNorthSouthShape} or
 * {@link de.weltraumschaf.jebnf.gfx.shapes.other.StraightWestEastShape} shape and a curve shape
 * ({@link de.weltraumschaf.jebnf.gfx.shapes.curves.CurveNorthEastShape},
 * {@link de.weltraumschaf.jebnf.gfx.shapes.curves.CurveNorthWestShape},
 * {@link de.weltraumschaf.jebnf.gfx.shapes.curves.CurveSouthEastShape}, or
 * {@link de.weltraumschaf.jebnf.gfx.shapes.curves.CurveSouthWestShape}).
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public abstract class AbstractForkShape extends RectangularShape {
 
     /**
      * The straight shape part.
      */
     final RectangularShape straight;
 
     /**
      * The curve shape part.
      */
     final RectangularShape curve;
 
     /**
      * Initializes {@link #straight} and {@link  #curve}.
      *
      * @param straight {@link de.weltraumschaf.jebnf.gfx.shapes.other.StraightNorthSouthShape} or
      *                 {@link de.weltraumschaf.jebnf.gfx.shapes.other.StraightWestEastShape}.
      * @param curve One of {@link de.weltraumschaf.jebnf.gfx.shapes.curves.CurveNorthEastShape},
      *              {@link de.weltraumschaf.jebnf.gfx.shapes.curves.CurveNorthWestShape},
      *              {@link de.weltraumschaf.jebnf.gfx.shapes.curves.CurveSouthEastShape}, or
      *              {@link de.weltraumschaf.jebnf.gfx.shapes.curves.CurveSouthWestShape}.
      */
     public AbstractForkShape(final RectangularShape straight, final RectangularShape curve) {
         super();
         this.straight = straight;
         this.curve    = curve;
     }
 
     @Override
     public void paint(final Graphics2D graphic) {
         super.paint(graphic);
         final Point pos = getPosition();
 
         straight.setPosition(pos);
         straight.setTransparent(true);
         straight.paint(graphic);
 
         curve.setPosition(pos);
         curve.setTransparent(true);
         curve.paint(graphic);
     }
 
 }
