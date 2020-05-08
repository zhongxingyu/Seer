 import java.awt.AlphaComposite;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class ShuffleSolverStep extends Step implements Runnable {
     BufferedImage sandbox;
     BufferedImage display;
     List<Piece> bestLayout;
     double bestError;
 
     public ShuffleSolverStep(Listener listener) {
         super(listener);
     }
 
     @Override
     public void begin(Object input) {
         bestLayout = (List<Piece>) input;
         sandbox = makeSandbox(bestLayout);
         bestError = Double.POSITIVE_INFINITY;
         setPreferredSize(new Dimension(sandbox.getWidth(), sandbox.getHeight()));
 
         new Thread(this).start();
     }
 
     @Override
     public void paint(Graphics g) {
         if (display != null) {
             g.drawImage(display, 0, 0, display.getWidth(), display.getHeight(), null);
         }
     }
 
     private void solve() {
         double area = 0;
         for (Piece piece : bestLayout) {
            area += Utility.getSumOfPixels(UtilityScala.showAlpha(piece.image));
         }
 
         while (true) {
             List<Piece> layout = new ArrayList<Piece>(bestLayout.size());
             for (Piece piece : bestLayout) {
                 layout.add(new Piece(piece));
             }
 
             shuffle(layout);
 
             // clear sandbox
             Graphics2D graphics = (Graphics2D) sandbox.getGraphics();
             graphics.setComposite(AlphaComposite.Clear);
             graphics.fillRect(0, 0, sandbox.getWidth(), sandbox.getHeight());
             graphics.setComposite(AlphaComposite.SrcOver);
             // Utility.drawChecker(graphics, sandbox.getWidth(), sandbox.getHeight(), 10, Color.LIGHT_GRAY, Color.DARK_GRAY);
 
             // draw the layout
             Utility.drawLayout(layout, sandbox);
 
             // calculate error
             BufferedImage filtered = EdgeDetectionFilter.getEdgeDetectionFilter(sandbox);
             double error = Utility.getSumOfPixels(filtered);
            error += Math.abs(area - Utility.getSumOfPixels(UtilityScala.showAlpha(sandbox)));
 
             // assign to best if error is less
             if (error < bestError) {
                 bestLayout = layout;
                 bestError = error;
                 display = filtered;
             }
 
             System.out.println("Best error: " + bestError + ", this error: " + error);
             repaint();
         }
     }
 
     private void shuffle(List<Piece> layout) {
         Random r = new Random();
 
         for (Piece piece : layout) {
             piece.position.x += 3 * r.nextGaussian();
             piece.position.y += 3 * r.nextGaussian();
             piece.rotation += 0.1 * r.nextGaussian();
         }
     }
 
     private BufferedImage makeSandbox(List<Piece> pieces) {
         int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
 
         for (Piece piece : pieces) {
             minX = (int) Math.min(minX, piece.position.x - piece.image.getWidth() / 2.0);
             minY = (int) Math.min(minY, piece.position.y - piece.image.getHeight() / 2.0);
             maxX = (int) Math.max(maxX, piece.position.x + piece.image.getWidth() / 2.0);
             maxY = (int) Math.max(maxY, piece.position.y + piece.image.getHeight() / 2.0);
         }
 
         int width = 2 * (maxX - minX);
         int height = 2 * (maxY - minY);
 
         return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
     }
 
     @Override
     public void run() {
         solve();
     }
 }
