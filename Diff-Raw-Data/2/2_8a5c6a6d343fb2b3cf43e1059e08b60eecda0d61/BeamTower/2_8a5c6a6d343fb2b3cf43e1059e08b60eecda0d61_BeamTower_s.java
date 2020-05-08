 /*
  *  This file is part of Pac Defence.
  *
  *  Pac Defence is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Pac Defence is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  (C) Liam Byrne, 2008.
  */
 
 package towers;
 
 import gui.Circle;
 import gui.GameMapPanel;
 import gui.Helper;
 import images.ImageHelper;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Stroke;
 import java.awt.geom.Arc2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import sprites.Sprite;
 import sprites.Sprite.DamageReport;
 
 
 public class BeamTower extends AbstractTower {
    
    // The number of ticks the beam lasts for
    private double beamLastTicks = GameMapPanel.CLOCK_TICKS_PER_SECOND / 2;
    private static final double upgradeBeamLastTicks = GameMapPanel.CLOCK_TICKS_PER_SECOND / 20;
    
    public BeamTower() {
       this(new Point(), null);
    }
 
    public BeamTower(Point p, Rectangle2D pathBounds) {
       super(p, pathBounds, "Beam", 40, 100, 40, 4.5, 50, 0, "beam.png", "BeamTower.png", false);
       // This is a grossly overpowered version for testing performance.
       /*super(p, pathBounds, "Beam", 0, 1000, 100, 0.1, 50, 0, "beam.png", "BeamTower.png", false);
       for(int i = 0; i < 20; i++) {
          upgradeSpecial();
       }*/
    }
    
    @Override
    public String getStat(Attribute a) {
       if(a == Attribute.Speed) {
          return super.getStat(a) + "/s";
       } else {
          return super.getStat(a);
       }
    }
    
    @Override
    public String getStatName(Attribute a) {
       if(a == Attribute.Speed) {
          return "Beam Speed";
       } else {
         return super.getStat(a);
       }
    }
 
    @Override
    protected String getSpecial() {
       return Helper.format(beamLastTicks / GameMapPanel.CLOCK_TICKS_PER_SECOND, 2) + "s";
    }
 
    @Override
    protected String getSpecialName() {
       return "Beam time";
    }
 
    @Override
    protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
          double damage, Point p, Sprite s, Rectangle2D pathBounds) {
       double angle = ImageHelper.vectorAngle(dx, dy);
       return new Beam(this, p, angle, range, speed, damage, pathBounds, s, (int)beamLastTicks);
    }
    
    @Override
    protected void upgradeSpecial() {
       beamLastTicks += upgradeBeamLastTicks;
    }
    
    private static class Beam implements Bullet {
 
       private Color beamColour = new Color(138, 138, 138);
       private static final int minAlpha = 60;
       private final int deltaAlpha;
       private final Rectangle2D pathBounds;
       private final Tower launchedBy;
       private final Set<Sprite> hitSprites = new CopyOnWriteArraySet<Sprite>();
       private final Line2D beam = new Line2D.Double();
       private final Point2D centre;
       private final Circle circle;      
       private double currentAngle;
       private double deltaAngle;
       private Sprite target;
       private int ticksLeft;
       private final double damage;
       private double moneyEarnt = 0;
       //private List<Point2D> points = Collections.emptyList();
       
       private Beam(Tower t, Point2D centre, double angle, int range, double speed, double damage,
             Rectangle2D pathBounds, Sprite target, int numTicks) {
          deltaAlpha = (beamColour.getAlpha() - minAlpha) / numTicks;
          this.centre = centre;
          this.pathBounds = pathBounds;
          this.launchedBy = t;
          currentAngle = Math.toDegrees(angle);
          deltaAngle = speed / GameMapPanel.CLOCK_TICKS_PER_SECOND;
          circle = new Circle(centre, range);
          this.target = target;
          ticksLeft = numTicks;
          setBeam();
          this.damage = damage;
       }
 
       @Override
       public void draw(Graphics g) {
          Graphics2D g2D = (Graphics2D) g;
          g2D.setColor(beamColour);
          Stroke s = g2D.getStroke();
          g2D.setStroke(new BasicStroke(4));
          g2D.draw(beam);
          g2D.setStroke(s);
          // Debugging code to make sure the points are in the right place
          /*g2D.setColor(Color.RED);
          for(Point2D p : points) {
             g2D.drawRect((int)p.getX(), (int)p.getY(), 1, 1);
          }*/
       }
 
       @Override
       public double tick(List<Sprite> sprites) {
          if(target != null) {
             setCorrectDirection();
          }
          if(ticksLeft <= 0) {
             return moneyEarnt;
          }
          ticksLeft--;
          if(beamColour.getAlpha() > minAlpha) {
             beamColour = new Color(beamColour.getRed(), beamColour.getGreen(), beamColour.getBlue(),
                      beamColour.getAlpha() - deltaAlpha);
          }
          List<Sprite> hittableSprites = new ArrayList<Sprite>(sprites);
          hittableSprites.removeAll(hitSprites);
          List<Point2D> points = makePoints();
          hitSprites(hittableSprites, points);
          currentAngle += deltaAngle;
          setBeam();
          return -1;
       }
       
       private void setBeam() {
          beam.setLine(centre, circle.getPointAt(Math.toRadians(currentAngle)));
       }
       
       private void setCorrectDirection() {
          Point2D p = target.getPosition();
          Point2D centre = circle.getCentre();
          double angleBetween = ImageHelper.vectorAngle(p.getX() - centre.getX(),
                p.getY() - centre.getY());
          int mult = (Math.toDegrees(angleBetween) > currentAngle) ? -1 : 1;
          deltaAngle *= mult;
          target = null;
       }
       
       private List<Point2D> makePoints() {
          List<Point2D> points = new ArrayList<Point2D>();
          Arc2D a = new Arc2D.Double();
          double angleStart = currentAngle - 90;
          double radAngleStart = Math.toRadians(currentAngle);
          double sinAngle = Math.sin(radAngleStart);
          double cosAngle = Math.cos(radAngleStart);
          double numPointsMult = 2 * Math.PI * deltaAngle / 360;
          for(int i = 1; i <= circle.getRadius(); i++) {
             a.setArcByCenter(centre.getX(), centre.getY(), i, angleStart, deltaAngle,
                   Arc2D.OPEN);
             //points.addAll(Helper.getPointsOnArc(a, pathBounds));
             points.addAll(getPointsOnArc(a, i, i * numPointsMult, sinAngle, cosAngle,
                   pathBounds));
          }
          //this.points = points;
          return points;
       }
       
       public static List<Point2D> getPointsOnArc(Arc2D a, double radius, double numPoints, double sinAngle, double cosAngle, Rectangle2D containingRect) {
          // Copied from the one in helper with a few optimisations as the arcs
          // are the same except with different radii. Gave an ~20% improvement
          // in a simple test.
          double deltaAngle = 1 / radius;
          List<Point2D> points = new ArrayList<Point2D>();
          Point2D p = a.getStartPoint();
          if(containingRect.contains(p)) {
             points.add(p);
          }
          double x = a.getCenterX();
          double y = a.getCenterY();
          double sinDeltaAngle = Math.sin(deltaAngle);
          double cosDeltaAngle = Math.cos(deltaAngle);
          for(int i = 0; i < numPoints; i++) {
             double newSinAngle = sinAngle * cosDeltaAngle + cosAngle * sinDeltaAngle;
             cosAngle = cosAngle * cosDeltaAngle - sinAngle * sinDeltaAngle;
             sinAngle = newSinAngle;
             p = new Point2D.Double(x + radius * sinAngle, y + radius * cosAngle);
             if(containingRect.contains(p)) {
                points.add(p);
             }
          }
          p = a.getEndPoint();
          if(containingRect.contains(p)) {
             points.add(p);
          }
          return points;
       }
       
       private void hitSprites(List<Sprite> sprites, List<Point2D> points) {
          for(Sprite s : sprites) {
             if(s.intersects(points) != null) {
                DamageReport d = s.hit(damage);
                if(d != null) {
                   moneyEarnt += BasicBullet.processDamageReport(d, launchedBy);
                   hitSprites.add(s);
                }
             }
          }
       }
       
    }
 
 }
