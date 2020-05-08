 package com.nullprogram.wheel;
 
 import java.util.Vector;
 import java.util.Random;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseEvent;
 
 import javax.swing.Timer;
 import javax.swing.JFrame;
 import javax.swing.JComponent;
 
 /**
  * Simulates and displays a chaotic water wheel.
  */
 public class ChaosWheel extends JComponent implements MouseListener {
 
     private static final long serialVersionUID = 4764158473501226728L;
 
     /* Simulation constants. */
     private static final int SIZE = 300; // display size in pixels
     private static final int DELAY = 30; // milliseconds
     private static final int DEFAULT_BUCKETS = 9;
     private static final int MIN_BUCKETS = 5;
 
     /* Simulation parameters. */
     private double radius = 1;          // feet
     private double wheelIntertia = .1;  // slug * ft ^ 2
     private double damping = 2.5;       // ft * lbs / radians / sec
     private double gravity = 10.7;      // ft / sec ^ 2
     private double bucketFull = 1.0;    // slug
     private double drainRate = 0.3;     // slug / sec / slug
     private double fillRate = 0.33;     // slug / sec
 
     /* Current state of the wheel. */
     private double theta;               // radians
     private double thetadot;            // radians / sec
    private Vector<Double> buckets;     // slug
     private Timer timer;
 
     /**
      * Create a water wheel with the default number of buckets.
      */
     public ChaosWheel() {
         this(DEFAULT_BUCKETS);
     }
 
     /**
      * Create a water wheel with a specific number of buckets.
      *
      * @param numBuckets number of buckets.
      */
     public ChaosWheel(final int numBuckets) {
         Random rng = new Random();
         theta = rng.nextDouble() * 2d * Math.PI;
         thetadot = (rng.nextDouble() - 0.5);
         buckets = new Vector<Double>();
         for (int i = 0; i < numBuckets; i++) {
             buckets.add(0d);
         }
         setPreferredSize(new Dimension(SIZE, SIZE));
         addMouseListener(this);
         ActionListener listener = new ActionListener() {
             public void actionPerformed(final ActionEvent evt) {
                 updateState(DELAY / 1000.0);
                 repaint();
             }
         };
         timer = new Timer(DELAY, listener);
     }
 
     /**
      * The main function when running standalone.
      *
      * @param args command line arguments
      */
     public static void main(final String[] args) {
         JFrame frame = new JFrame("Lorenz Water Wheel");
         ChaosWheel wheel = null;
         if (args.length == 0) {
             wheel = new ChaosWheel();
         } else {
             int num = 0;
             try {
                 num = Integer.parseInt(args[0]);
             } catch (NumberFormatException e) {
                 System.out.println("Argument must be an integer.");
                 System.exit(1);
             }
             if (num < MIN_BUCKETS) {
                 System.out.println("Minimum # of buckets: " + MIN_BUCKETS);
                 System.exit(1);
             }
             wheel = new ChaosWheel(num);
         }
         frame.add(wheel);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
         wheel.start();
     }
 
     /**
      * Draw the water wheel to the display.
      *
      * @param g the graphics to draw on
      */
     public final void paintComponent(final Graphics g) {
         super.paintComponent(g);
 
         /* Draw the buckets. */
         double diff = Math.PI * 2d / buckets.size();
         int size = Math.min(getWidth(), getHeight());
         int bucketSize = size / (int) (buckets.size() / 1.25);
         int drawRadius = size / 2 - bucketSize;
         int centerx = size / 2;
         int centery = size / 2;
         for (int i = 0; i < buckets.size(); i++) {
             double angle = i * diff + theta - Math.PI / 2;
             int x = centerx + (int) (Math.cos(angle) * drawRadius);
             int y = centery + (int) (Math.sin(angle) * drawRadius);
             g.setColor(Color.black);
             g.drawRect(x - bucketSize / 2, y - bucketSize / 2,
                        bucketSize, bucketSize);
             g.setColor(Color.blue);
             int height = (int) (bucketSize * buckets.get(i) / bucketFull);
             g.fillRect(x - bucketSize / 2,
                        y - bucketSize / 2 + (bucketSize - height),
                        bucketSize, height);
         }
     }
 
     /**
      * Start running the wheel simulation.
      */
     public final void start() {
         timer.start();
     }
 
     /**
      * Tell the wheel to stop running.
      */
     public final void stop() {
         timer.stop();
     }
 
     /**
      * Update the state by the given amount of seconds.
      *
      * @param tdot number of seconds to update by.
      */
     public final void updateState(final double tdot) {
         theta += thetadot * tdot;
         while (theta < 0) {
             theta += Math.PI * 2;
         }
         while (theta > Math.PI * 2) {
             theta -= Math.PI * 2;
         }
 
         /* Calculate inertia */
         double inertia = wheelIntertia;
         for (int i = 0; i < buckets.size(); i++) {
             inertia += buckets.get(i) * radius * radius;
         }
 
         /* Calculate torque */
         double torque = -1 * (damping * thetadot);
         double diff = Math.PI * 2d / buckets.size();
         for (int i = 0; i < buckets.size(); i++) {
             torque += buckets.get(i) * radius * gravity
                       * Math.sin(theta + diff * i);
         }
         thetadot += torque / inertia * tdot;
 
         /* Update buckets */
         for (int i = 0; i < buckets.size(); i++) {
             double oldVal = buckets.get(i);
             double newVal = oldVal;
             newVal += buckets.get(i) * -drainRate * tdot
                       + tdot * inflow(theta + diff * i);
             newVal = Math.max(0, newVal);
             newVal = Math.min(bucketFull, newVal);
             buckets.set(i, newVal);
         }
     }
 
     /**
      * The fill rate for a bucket at the given position.
      *
      * @param angle position of the bucket
      * @return fill rate of the bucket (slugs / sec)
      */
     private double inflow(final double angle) {
        double lim = Math.abs(Math.cos(Math.PI * 2d / buckets.size()));
        if (Math.cos(angle) > lim) {
             return fillRate / 2d
                    * (Math.cos(buckets.size()
                                * Math.atan2(Math.tan(angle), 1) / 2d) + 1);
         } else {
             return 0;
         }
     }
 
     /**
      * Add one bucket to the display.
      */
    private void addBucket() {
         buckets.add(0d);
     }
 
     /**
      * Remove one bucket from the display.
      */
    private void removeBucket() {
         if (buckets.size() > MIN_BUCKETS) {
             buckets.remove(0);
         }
     }
 
     /** {@inheritDoc} */
     public final void mouseReleased(final MouseEvent e) {
         System.out.println("click!");
         switch (e.getButton()) {
         case MouseEvent.BUTTON1:
             addBucket();
             break;
         case MouseEvent.BUTTON3:
             removeBucket();
             break;
         default:
             /* do nothing */
             break;
         }
     }
 
     /** {@inheritDoc} */
     public void mouseExited(final MouseEvent e) {
         /* Do nothing */
     }
 
     /** {@inheritDoc} */
     public void mouseEntered(final MouseEvent e) {
         /* Do nothing */
     }
 
     /** {@inheritDoc} */
     public void mouseClicked(final MouseEvent e) {
         /* Do nothing */
     }
 
     /** {@inheritDoc} */
     public void mousePressed(final MouseEvent e) {
         /* Do nothing */
     }
 }
