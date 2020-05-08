 import javax.swing.*;
 import java.awt.*;
 
 public class GUI extends JFrame {
     private static final String TITLE = "NPC World";
 
     private Container     container;
     private LayoutManager layout;
 
     private static final int gapH = 3;
     private static final int gapV = 3;
 
     private Grid    grid;
     private Toolbar toolbar;
     private Infobar infobar;
     private World   world;
     private NpcUniverse universe;
 
     private WorldSelector selector;
 
     private Thread  thread;
     private boolean running;
 
     public GUI() {
         Util.tryForNiceTheme();
 
         universe = new NpcUniverse();
 
         thread   = null;
         running  = false;
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
         setTitle(TITLE);
 
         layout = new BorderLayout(gapH, gapV);
 
         container = getContentPane();
 
         selector = new WorldSelector(this);
         selector.selectWorldAt(0, 0);
 
         grid    = new Grid(6, 12, this);
         toolbar = new Toolbar(this);
         infobar = new Infobar(this);
 
         container.add(grid,    BorderLayout.CENTER);
         container.add(toolbar, BorderLayout.NORTH);
         container.add(infobar, BorderLayout.SOUTH);
 
         setVisible(true);
 
         pack();
 
         redraw();
     }
 
     public NpcUniverse getUniverse() {
         return universe;
     }
 
     public void step() {
         universe.step();
         redraw();
     }
 
     public void redraw() {
         Debug.echo("HEY YOU HEY YOU >>>>>>>>> grid =", grid);
         grid.repopulate();
         infobar.fillInfo();
         repaint();
         selector.repaint();
     }
 
     public World getWorld() {
         return world;
     }
 
     public void setWorld(World world) {
         this.world = world;
     }
 
     public void toggleRunning() {
         if (! running) startRun();
         else           endRun();
     }
 
     public void pause() {
         endRun();
     }
 
     public void play() {
         startRun();
     }
 
     private void startRun() {
         running = true;
         thread  = new RunnerThread();
 
         thread.start();
     }
 
     private void endRun() {
         running = false;
         if (thread != null) {
             Util.joinThread(thread);
             thread = null;
         }
     }
 
     public boolean isRunning() {
         return running;
     }
 
     public void selectWorldAt(int row, int col) {
         setWorld(universe.getWorld(row, col));
     }
 
     private class RunnerThread extends Thread {
         public void run() {
			long startTime, stepTime;
             while (running) {
				startTime = System.currentTimeMillis();
                 step();
				stepTime = startTime - System.currentTimeMillis();
				if(stepTime < Settings.runDelay)	Util.sleep((int) (Settings.runDelay-stepTime));
             }
         }
     }
 }
