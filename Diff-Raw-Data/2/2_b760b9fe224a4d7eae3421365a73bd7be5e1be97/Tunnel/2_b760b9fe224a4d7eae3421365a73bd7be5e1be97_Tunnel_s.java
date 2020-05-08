 package com.servegame.abendstern.tunnelblick.game;
 
 import java.util.*;
 import javax.media.opengl.*;
 
 /**
  * Creates a visual grid representing the path along the virtual Z axis. The
  * path and walls are a grid coloured by moving "pulses".
  */
 public final class Tunnel {
   public static final int GRID_WIDTH = 4;
   public static final int GRID_LENGTH = 256;
   public static final float GSQ_SZ = 1.0f / GRID_WIDTH;
   public static final float GSQ_LEN = GSQ_SZ*2;
 
   private final float grid[][][] = new float[GRID_LENGTH][GRID_WIDTH][3];
   private static final class Pulse {
     public float coord, speed, dr, dg, db;
   }
   private final LinkedList<Pulse> pulses[] =
     //(The cast works since generics don't exist at runtime.)
     (LinkedList<Pulse>[])new LinkedList[GRID_WIDTH];
   private static final int MAX_PULSES = 1024;
 
   private static final class QueuedPulse
   implements Comparable<QueuedPulse> {
     public int column;
     public float when;
     public Pulse pulse;
 
     public boolean equals(Object o) {
       if (o == null) return false;
       return ((QueuedPulse)o).when == when;
     }
 
     public int compareTo(QueuedPulse qp) {
       if (when < qp.when) return -1;
       if (when > qp.when) return +1;
       return 0;
     }
   }
   private static final PriorityQueue<QueuedPulse> pulseQueue =
     new PriorityQueue<QueuedPulse>();
 
   private float offset = 0.0f, clock = 0.0f;
 
   /**
    * Creates a new Tunnel, with only an initial pair of white/black pulses.
    */
   public Tunnel() {
     for (int i = 0; i < pulses.length; ++i)
       pulses[i] = new LinkedList<Pulse>();
 
     for (int off = 0; off < GRID_LENGTH; off += 32) {
       for (int i = 0; i < GRID_WIDTH; ++i) {
         pulse(off*GSQ_LEN, i, +0.5f, +0.5f, +0.5f, +5.0f, 0);
         pulse(off*GSQ_LEN, i, -0.5f, -0.5f, -0.5f, -5.0f, 0);
       }
     }
   }
 
   /**
    * Updates the tunnel, given the time elapsed since last update.
    */
   public void update(float et) {
     clock += et;
     while (pulseQueue.size() > 0 && pulseQueue.peek().when < clock) {
       QueuedPulse qp = pulseQueue.poll();
       pulses[qp.column].add(qp.pulse);
       if (pulses[qp.column].size() > MAX_PULSES)
         pulses[qp.column].removeFirst();
     }
 
     for (LinkedList<Pulse> llp: pulses) {
       for (Pulse p: llp) {
         p.coord += p.speed * et;
         while (p.coord < 0)            p.coord += GRID_LENGTH;
         while (p.coord >= GRID_LENGTH) p.coord -= GRID_LENGTH;
       }
     }
   }
 
   /**
    * Draws the tunnel.
    */
   public void draw(GL2 gl, Distortion d) {
     //Calculate the colours for the grid squares
     //First, reset to neutral
     for (int i = 0; i < grid.length; ++i)
       for (int j = 0; j < grid[i].length; ++j)
         for (int k = 0; k < grid[i][j].length; ++k)
           grid[i][j][k] = 0.5f;
 
     //Add in pulses
     for (int i = 0; i < pulses.length; ++i) {
       for (Pulse p: pulses[i]) {
         int lower = (int)Math.floor(p.coord);
         int upper = (int)Math.ceil(p.coord);
         if (upper == grid.length)
           upper = 0;
 
         float uw = p.coord - lower;
         float lw = 1.0f - uw;
 
         grid[lower][i][0] += lw*p.dr;
         grid[lower][i][1] += lw*p.dg;
         grid[lower][i][2] += lw*p.db;
         grid[upper][i][0] += uw*p.dr;
         grid[upper][i][1] += uw*p.dg;
         grid[upper][i][2] += uw*p.db;
       }
     }
 
     //Translate for partial squares
     gl.glPushMatrix();
     gl.glTranslatef(0, 0, -(float)(Math.floor(offset)-offset)*GSQ_LEN);
 
     gl.glBegin(GL2.GL_TRIANGLES);
 
     //Draw the floor
     int firstFloorTile = (int)Math.floor(offset);
     //Manually handle negative case since % is broken
     if (firstFloorTile < 0)
       firstFloorTile += GRID_LENGTH;
     float halfSpace = GSQ_SZ * 0.05f;
     for (int z = 0; z < GRID_LENGTH/2; ++z) {
       for (int x = 0; x < GRID_WIDTH; ++x) {
         float[] colour = grid[(firstFloorTile+z) % GRID_LENGTH][x];
         gl.glColor3f(colour[0], colour[1], colour[2]);
         d.v(gl, (x+0)*GSQ_SZ + halfSpace, 0, -((z+0)*GSQ_LEN + halfSpace));
         d.v(gl, (x+1)*GSQ_SZ - halfSpace, 0, -((z+0)*GSQ_LEN + halfSpace));
         d.v(gl, (x+0)*GSQ_SZ + halfSpace, 0, -((z+1)*GSQ_LEN - halfSpace));
 
         d.v(gl, (x+1)*GSQ_SZ - halfSpace, 0, -((z+0)*GSQ_LEN + halfSpace));
         d.v(gl, (x+0)*GSQ_SZ + halfSpace, 0, -((z+1)*GSQ_LEN - halfSpace));
         d.v(gl, (x+1)*GSQ_SZ - halfSpace, 0, -((z+1)*GSQ_LEN - halfSpace));
       }
     }
 
     //Draw the walls
     int firstWallTile = firstFloorTile + GRID_LENGTH/2;
     for (int z = 0; z < GRID_LENGTH/2; ++z) {
       for (int i = 0; i < GRID_WIDTH; ++i) {
         float[] colour =
           grid[(firstWallTile + (GRID_LENGTH/2-z-1)) % GRID_LENGTH][i];
         float x = (i < GRID_WIDTH/2? 0 : 1);
         float y = 2*(i < GRID_WIDTH/2? i*GSQ_SZ : (GRID_WIDTH - i - 1)*GSQ_SZ);
         gl.glColor3f(colour[0], colour[1], colour[2]);
         d.v(gl, x, y +   0.0f*2 + halfSpace, -((z+0)*GSQ_LEN + halfSpace));
         d.v(gl, x, y + GSQ_SZ*2 - halfSpace, -((z+0)*GSQ_LEN + halfSpace));
         d.v(gl, x, y +   0.0f*2 + halfSpace, -((z+1)*GSQ_LEN - halfSpace));
 
         d.v(gl, x, y + GSQ_SZ*2 - halfSpace, -((z+0)*GSQ_LEN + halfSpace));
         d.v(gl, x, y +   0.0f*2 + halfSpace, -((z+1)*GSQ_LEN - halfSpace));
         d.v(gl, x, y + GSQ_SZ*2 - halfSpace, -((z+1)*GSQ_LEN - halfSpace));
       }
     }
 
     gl.glEnd();
     gl.glPopMatrix();
   }
 
   /**
    * Creates a "pulse" which moves through a column of grid lines in the tunnel.
    *
    * @param z The initial z coordinate of the pulse, in word coordinates.
    * @param col The column the pulse lives in, between 0 and GRID_WIDTH-1.
    * @param r The delta-red for this pulse.
    * @param g The delta-green for this pulse.
    * @param b The delta-blue for this pulse.
    * @param speed The rate at which the pulse moves, in grid squares per
    * second. Positive is forward (ie, away from the player).
    * @param delay The time, in seconds, before the pulse actually comes into
    * existence.
    */
   public void pulse(float z, int col,
                     float r, float g, float b, float speed, float delay) {
     Pulse pulse = new Pulse();
    pulse.coord = z/GSQ_LEN - offset;
     pulse.speed = speed;
     pulse.dr = r;
     pulse.dg = g;
     pulse.db = b;
     QueuedPulse qp = new QueuedPulse();
     qp.pulse = pulse;
     qp.when = clock + delay;
     qp.column = col;
     pulseQueue.add(qp);
   }
 
   /**
    * Translates along Z to match GameField.
    */
   public void translateZ(GameObject reference, float offset) {
     this.offset -= (reference.getZ() - offset) / GSQ_LEN;
     while (this.offset > GRID_LENGTH)
       this.offset -= GRID_LENGTH;
   }
 }
