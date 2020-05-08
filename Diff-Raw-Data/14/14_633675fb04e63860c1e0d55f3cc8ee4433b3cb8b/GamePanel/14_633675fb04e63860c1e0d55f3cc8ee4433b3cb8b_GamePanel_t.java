 package bounze;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Graphics;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Path2D;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.JComponent;
 import lombok.NonNull;
 import lombok.RequiredArgsConstructor;
 import lombok.val;
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.collision.shapes.Shape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.Fixture;
 
 @SuppressWarnings("serial")
 public class GamePanel extends JComponent
     implements Observer, MouseMotionListener, MouseListener, KeyListener {
 
     private static final Color BACK = new Color(  0, 102, 153);
     private static final Color FORE = new Color(255, 255, 255);
     private static final Color FILL = new Color(  0,  51, 153);
 
    private static final float SCALE = 10;
     private static final Stroke STROKE = new BasicStroke((2f / SCALE));
 
     private final Game game;
 
     @NonNull
     private Vec2 mouseLast = new Vec2(0, 0);
 
     private Path2D pointer = new Path2D.Double();
 
     public GamePanel(Game game) {
         this.game = game;
        Dimension d = new Dimension((int) (Game.WIDTH * SCALE + 1),
                                    (int) (Game.HEIGHT * SCALE + 1));
         setPreferredSize(d);
 
         pointer.moveTo(3, 1);
         pointer.lineTo(3 + 1, 0);
         pointer.lineTo(3, -1);
 
         addMouseListener(this);
         addMouseMotionListener(this);
         addKeyListener(this);
         game.addObserver(this);
     }
 
     @Override
     public void paintComponent(Graphics graphics) {
         Graphics2D g = (Graphics2D) graphics;
         g.setColor(BACK);
         g.fillRect(0, 0, getWidth(), getHeight());
 
         g.setStroke(STROKE);
         g.scale(SCALE, SCALE);
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
 
         g.setColor(FORE);
         for (Body body : game.getEdges()) {
             Vec2 pos = body.getPosition();
             float angle = body.getAngle();
             Fixture fixture = body.getFixtureList();
             while (fixture != null) {
                 Shape shape = fixture.getShape();
                 draw(g, (PolygonShape) shape, pos, angle);
                 fixture = fixture.getNext();
             }
         }
 
         for (Edge e : game.getOldedges()) {
             int age = (int) (game.getTick() - e.getDeathTick());
             if (age < Game.FPS) {
                 int alpha = 255 - age * 255 / Game.FPS;
                 g.setColor(new Color(FORE.getRed(), FORE.getGreen(),
                                      FORE.getBlue(), alpha));
                 drawEdge(g, e);
             }
         }
 
         Body ball = game.getBall();
         Fixture ballfix = ball.getFixtureList();
         draw(g, (CircleShape) ballfix.getShape(), ball.getPosition());
 
         if (game.ballStopped()) {
             AffineTransform at = new AffineTransform();
             Vec2 pos = game.getBall().getPosition();
             at.translate(pos.x, pos.y);
             at.rotate(Math.atan2(mouseLast.y - pos.y, mouseLast.x - pos.x));
             g.draw(at.createTransformedShape(pointer));
         }
     }
 
     private void drawEdge(Graphics2D g, Edge e) {
         Path2D line = new Path2D.Float();
         line.moveTo(e.getA().x, e.getA().y);
         line.lineTo(e.getB().x, e.getB().y);
         g.draw(line);
     }
 
     private void draw(Graphics2D g, CircleShape s, Vec2 pos) {
         double x = pos.x;
         double y = pos.y;
         double r = s.m_radius;
         val circle = new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
         g.setColor(FILL);
         g.fill(circle);
         g.setColor(FORE);
         g.draw(circle);
     }
 
     private void draw(Graphics2D g, PolygonShape s, Vec2 pos, float angle) {
         Path2D path = new Path2D.Float();
         Vec2 first = s.getVertex(0);
         path.moveTo(first.x, first.y);
         for (int i = 1; i < s.getVertexCount(); i++) {
             Vec2 v = s.getVertex(i);
             path.lineTo(v.x, v.y);
         }
         AffineTransform at = new AffineTransform();
         at.translate(pos.x, pos.y);
         at.rotate(angle);
         g.draw(at.createTransformedShape(path));
     }
 
     @Override
     public void update(Observable o, Object arg) {
         requestFocusInWindow();
         repaint();
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
         mouseLast = new Vec2(e.getX() / SCALE, e.getY() / SCALE);
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
         mouseLast = new Vec2(e.getX() / SCALE, e.getY() / SCALE);
     }
 
     @Override
     public void	mouseClicked(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         if (game.ballStopped()) {
             Vec2 pos = game.getBall().getPosition();
             Vec2 dir = new Vec2(e.getX() / SCALE - pos.x,
                                 e.getY() / SCALE - pos.y);
             game.shoot(dir);
         }
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
         if (e.getKeyChar() == 'g') {
             game.generate();
         }
     }
 }
