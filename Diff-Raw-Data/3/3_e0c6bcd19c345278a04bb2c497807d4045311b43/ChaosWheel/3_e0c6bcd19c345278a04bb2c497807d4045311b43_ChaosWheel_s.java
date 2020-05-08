 /* Copyright (c) 2010 Christopher Wellons <mosquitopsu@gmail.com>
  *
  * Permission to use, copy, modify, and distribute this software for
  * any purpose with or without fee is hereby granted, provided that
  * the above copyright notice and this permission notice appear in all
  * copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
  * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
  * AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
  * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
  * OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
  * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
  * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 package com.nullprogram.wheel;
 
 import java.util.Vector;
 import java.util.ArrayDeque;
 import java.util.Iterator;
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
  *
  * Left-clicking adds a bucket and right-clicking removes a
  * bucket. The simulation discrete steps are uniform, making this a
  * bit rudimentary.
  *
  * This code is based on a Matlab program written by my friend Michael
  * Abraham.
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
     private double wheelIntertia = 0.1; // slug * ft ^ 2
     private double damping = 2.5;       // ft * lbs / radians / sec
     private double gravity = 32.2;      // ft / sec ^ 2
     private double bucketFull = 1.0;    // slug
     private double drainRate = 0.3;     // slug / sec / slug
     private double fillRate = 0.33;     // slug / sec
 
     /* Current state of the wheel. */
     private double theta;               // radians
     private double thetadot;            // radians / sec
     private Vector<Double> buckets;     // slug
     private Timer timer;
     private boolean graphMode;
 
     /* Histotic state information. */
     private static final int MAX_HISTORY = 1000;
     private ArrayDeque<Double> rlRatio;      // left/right water ratio
     private ArrayDeque<Double> tbRatio;      // top/bottom water ratio
     private double rlRatioMax = 0;
     private double rlRatioMin = 0;
     private double tbRatioMax = 0;
     private double tbRatioMin = 0;
 
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
         rlRatio = new ArrayDeque<Double>();
         tbRatio = new ArrayDeque<Double>();
         setPreferredSize(new Dimension(SIZE, SIZE));
         addMouseListener(this);
         ActionListener listener = new ActionListener() {
             public void actionPerformed(final ActionEvent evt) {
                 updateState(DELAY / 1500.0);
                 repaint();
             }
         };
         graphMode = false;
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
         if (graphMode) {
             paintGraph(g);
             return;
         }
 
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
      * Paint a graph of historical data.
      *
      * @param g graphics to be painted
      */
     private void paintGraph(final Graphics g) {
         if (rlRatio.size() < 2) {
             return;
         }
         g.setColor(Color.black);
         Iterator<Double> rlit = rlRatio.iterator();
         Iterator<Double> tbit = tbRatio.iterator();
         Double rlLast = rlit.next();
         Double tbLast = tbit.next();
         while (rlit.hasNext()) {
             Double rl = rlit.next();
             Double tb = tbit.next();
             int x0 = (int) (rlLast / (rlRatioMax - rlRatioMin) * getWidth());
             int y0 = (int) (tbLast / (tbRatioMax - tbRatioMin) * getHeight());
             int x1 = (int) (rl / (rlRatioMax - rlRatioMin) * getWidth());
             int y1 = (int) (tb / (tbRatioMax - tbRatioMin) * getHeight());
             g.drawLine(x0, y0, x1, y1);
             rlLast = rl;
             tbLast = tb;
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
 
         /* Store the original system state */
         double thetaOrig = theta;
         double thetadotOrig = thetadot;
         Vector<Double> bucketsOrig =  new Vector<Double>(buckets);
 
         /* These are variables needed for intermediate steps in RK4 */
         double dt = 0.0;
         double rateWeight = 1.0;
 
         /* Time derivatives of states */
         double ddtTheta = 0.0;
         double ddtThetadot = 0.0;
         Vector<Double> ddtBuckets = new Vector<Double>();
         for (int i = 0; i < buckets.size(); i++) {
             ddtBuckets.add(0d);
         }
 
         /* Total derivative approximations */
         double ddtThetaTotal = 0.0;
         double ddtThetadotTotal = 0.0;
         Vector<Double> ddtBucketsTotal = new Vector<Double>();
         for (int i = 0; i < buckets.size(); i++) {
             ddtBucketsTotal.add(0.0);
         }
 
         /* RK4 Integration */
         for (int rk4idx = 1; rk4idx <= 4; rk4idx++) {
             if ((rk4idx > 1) && (rk4idx < 4)) {
                 rateWeight = 2.0;
                 dt = tdot / 2.0;
             } else if (rk4idx == 4) {
                 rateWeight = 1.0;
                 dt = tdot;
             }
 
             /* System states to be used in this RK4 step */
 
             theta = thetaOrig + dt * ddtTheta;
             while (theta < 0) {
                 theta += Math.PI * 2;
             }
             while (theta > Math.PI * 2) {
                 theta -= Math.PI * 2;
             }
             thetadot = thetadotOrig + dt * ddtThetadot;
 
             for (int i = 0; i < buckets.size(); i++) {
                 double val = bucketsOrig.get(i) + dt * ddtBuckets.get(i);
                 buckets.set(i, Math.min(bucketFull, Math.max(0, val)));
             }
 
             /* Differential equation for ddtTheta (kinematics) */
             ddtTheta = thetadot;
 
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
 
             /* Differential equation for ddtThetadot (physics) */
             ddtThetadot = torque / inertia;
 
             /* Differential equation for ddtBuckets (drain rate equation) */
             for (int i = 0; i < buckets.size(); i++) {
                 ddtBuckets.set(i, buckets.get(i) * -drainRate
                                + inflow(theta + diff * i));
             }
 
             /* Log the derivative approximations */
             ddtThetaTotal += ddtTheta * rateWeight;
             ddtThetadotTotal += ddtThetadot * rateWeight;
             for (int i = 0; i < ddtBucketsTotal.size(); i++) {
                 ddtBucketsTotal.set(i, ddtBucketsTotal.get(i)
                                     + ddtBuckets.get(i) * rateWeight);
             }
 
         } /* End of RK4 for loop */
 
         /* Update the system state.
          * THIS is where time actually moves forward. */
         theta = thetaOrig + 1.0 / 6.0 * ddtThetaTotal * tdot;
         thetadot = thetadotOrig + 1.0 / 6.0 * ddtThetadotTotal * tdot;
         for (int i = 0; i < ddtBucketsTotal.size(); i++) {
             double val = buckets.get(i)
                          + 1d / 6d * ddtBucketsTotal.get(i) * tdot;
             buckets.set(i, Math.min(bucketFull, Math.max(0, val)));
         }
 
         logState();
     }
 
     /**
      * Append some info about the current wheel state to the log.
      */
     private void logState() {
         double left = 0;
         double right = 0;
         double top = 0;
         double bottom = 0;
         double diff = Math.PI * 2d / buckets.size();
         for (int i = 0; i < buckets.size(); i++) {
             double angle = theta + diff * i;
             if (Math.cos(angle) > 0) {
                 right += buckets.get(i);
             }
             left += buckets.get(i);
             if (Math.sin(angle) > 0) {
                 top += buckets.get(i);
             }
             bottom += buckets.get(i);
         }
         double rl = left / right;
         double tb = top / bottom;
         rlRatioMax = Math.max(rl, rlRatioMax);
         tbRatioMax = Math.max(tb, tbRatioMax);
         rlRatioMin = Math.min(rl, rlRatioMin);
         tbRatioMin = Math.min(tb, tbRatioMin);
         rlRatio.add(rl);
         tbRatio.add(tb);
         if (rlRatio.size() > MAX_HISTORY) {
             rl = rlRatio.remove();
             tb = tbRatio.remove();
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
         switch (e.getButton()) {
         case MouseEvent.BUTTON1:
             addBucket();
             break;
         case MouseEvent.BUTTON2:
             graphMode ^= true;
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
